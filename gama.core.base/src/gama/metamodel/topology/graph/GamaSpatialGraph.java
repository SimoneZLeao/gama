/*******************************************************************************************************
 *
 * gama.metamodel.topology.graph.GamaSpatialGraph.java, in plugin gama.core, is part of the source code of the
 * GAMA modeling and simulation platform (v. 1.8)
 *
 * (c) 2007-2018 UMI 209 UMMISCO IRD/SU & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.metamodel.topology.graph;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.Graphs;

import org.locationtech.jts.geom.Coordinate;

import gama.common.interfaces.IAgent;
import gama.common.interfaces.IContainer;
import gama.common.util.StringUtils;
import gama.metamodel.population.IPopulation;
import gama.metamodel.shape.GamaPoint;
import gama.metamodel.shape.IShape;
import gama.metamodel.topology.ITopology;
import gama.metamodel.topology.filter.IAgentFilter;
import gama.runtime.exceptions.GamaRuntimeException;
import gama.runtime.scope.IScope;
import gama.util.graph.GamaGraph;
import gama.util.graph.GraphEvent;
import gama.util.graph.IGraph;
import gama.util.graph._Edge;
import gama.util.graph.GraphEvent.GraphEventType;
import gama.util.list.GamaListFactory;
import gama.util.list.IList;
import gama.util.map.GamaMapFactory;
import gama.util.map.IMap;
import gama.util.path.GamaSpatialPath;
import gama.util.path.PathFactory;
import gaml.species.ISpecies;
import gaml.types.GamaGeometryType;
import gaml.types.IType;
import gaml.types.Types;

@SuppressWarnings ({ "unchecked", "rawtypes" })
public class GamaSpatialGraph extends GamaGraph<IShape, IShape>
		implements IGraph<IShape, IShape>, IAgentFilter, IPopulation.Listener {

	/*
	 * Own topology of the graph. Lazily instantiated, and invalidated at each modification of the graph.
	 */
	private ITopology topology;
	private double tolerance = 0;
	private final Map<Integer, IShape> verticesBuilt; // only used for
														// optimization
														// purpose of
														// spatial graph

	// building.

	/**
	 * Determines the relationship among two polygons.
	 */
	public interface VertexRelationship<T> {

		/**
		 * @param scope
		 *            TODO Determines if two vertex geometries are to be treated as related in any way.
		 * @param p1
		 *            a geometrical object
		 * @param p2
		 *            another geometrical object
		 */
		boolean related(IScope scope, T p1, T p2);

		boolean equivalent(IScope scope, T p1, T p2);

		// Double distance(T p1, T p2);

	}

	public GamaSpatialGraph(final IContainer edgesOrVertices, final boolean byEdge, final boolean directed,
			final VertexRelationship rel, final ISpecies edgesSpecies, final IScope scope, final IType nodeType,
			final IType edgeType) {
		this(scope, nodeType, edgeType);
		init(scope, edgesOrVertices, byEdge, directed, rel, edgesSpecies);
	}

	public GamaSpatialGraph(final IContainer edgesOrVertices, final boolean byEdge, final boolean directed,
			final VertexRelationship rel, final ISpecies edgesSpecies, final IScope scope, final IType nodeType,
			final IType edgeType, final Double tolerance) {
		this(scope, nodeType, edgeType);
		this.tolerance = tolerance;
		init(scope, edgesOrVertices, byEdge, directed, rel, edgesSpecies, tolerance);
	}

	public GamaSpatialGraph(final IContainer edges, final IContainer vertices, final IScope scope) {
		this(scope, vertices.getGamlType().getContentType(), edges.getGamlType().getContentType());
		init(scope, edges, vertices);
	}

	public GamaSpatialGraph(final IScope scope, final IType nodeType, final IType edgeType) {
		super(scope, nodeType, edgeType);
		verticesBuilt = new HashMap();
	}

	@Override
	public GamaSpatialGraph copy(final IScope scope) {
		final GamaSpatialGraph g = new GamaSpatialGraph(GamaListFactory.EMPTY_LIST, true, directed, vertexRelation,
				edgeSpecies, scope, type.getKeyType(), type.getContentType());

		Graphs.addAllVertices(g, this.getVertices());
		Graphs.addAllEdges(g, this, this.edgeSet());
		return g;
	}

	@Override
	protected GamaSpatialPath pathFromEdges(final IScope scope, final IShape source, final IShape target,
			final IList<IShape> edges) {
		return PathFactory.create(scope, getTopology(scope), source, target, edges);
	}

	@Override
	protected void buildByVertices(final IScope scope, final IContainer<?, IShape> list) {
		for (final IShape p : list.iterable(scope)) {
			super.addVertex(p);
		}
		for (final IShape o1 : list.iterable(scope)) { // Try to create
														// automatic edges
			if (o1.getAgent() != null) {
				o1.getAgent().setAttribute("attached_graph", this);
			}
			for (final IShape o2 : list.iterable(scope)) {
				if (vertexRelation.equivalent(scope, o1, o2)) {
					continue;
				}
				if (vertexRelation.related(scope, o1, o2)) {
					addEdge(o1, o2);
				}
			}
		}
	}

	@Override
	public _SpatialEdge getEdge(final Object e) {
		return (_SpatialEdge) edgeMap.get(e);
	}

	@Override
	public _SpatialVertex getVertex(final Object v) {
		return (_SpatialVertex) vertexMap.get(v);
	}

	@Override
	protected _SpatialEdge newEdge(final Object e, final Object v1, final Object v2) throws GamaRuntimeException {
		return new _SpatialEdge(this, e, v1, v2);
	}

	@Override
	protected _SpatialVertex newVertex(final Object v) throws GamaRuntimeException {
		return new _SpatialVertex(this, v);
	}

	@Override
	public boolean addVertex(final IShape v) {
		final boolean added = super.addVertex(v);
		if (added && vertexRelation != null) {
			for (final IShape o : vertexSet()) {
				if (!vertexRelation.equivalent(graphScope, v, o) && vertexRelation.related(graphScope, v, o)) {
					addEdge(v, o);
				}
			}
		}
		return added;
	}

	public ITopology getTopology(final IScope scope) {
		if (topology == null) {
			setTopology(new GraphTopology(scope, this));
		}
		return topology;
	}

	protected void setTopology(final ITopology topology) {
		this.topology = topology;
	}

	private void refreshEdges() {
		final Set<? extends IShape> vSet = vertexSet();
		boolean related, already;
		for (final IShape s1 : vSet) {
			for (final IShape s2 : vSet) {
				if (graphScope.interrupted()) { return; }
				if (vertexRelation.equivalent(graphScope, s1, s2)) {
					continue;
				}
				already = this.containsEdge(s1, s2);
				if ((related = vertexRelation.related(graphScope, s1, s2)) && !already) {
					addEdge(s1, s2);
				} else if (already && !related) {
					removeEdge(s1, s2);

				}
			}
		}
	}

	@Override
	protected Object generateEdgeObject(final Object v1, final Object v2) {
		if (v1 instanceof IShape && v2 instanceof IShape) {
			return GamaGeometryType.buildLink(graphScope, (IShape) v1, (IShape) v2);
		}
		return super.generateEdgeObject(v1, v2);
	}

	@Override
	public IList<IShape> getEdges() {
		return super.getEdges();
	}

	@Override
	public IList<IShape> getVertices() {
		return super.getVertices();
	}

	@Override
	public void notifyAgentRemoved(final IScope scope, final IPopulation pop, final IAgent agent) {
		this.removeVertex(agent);
	}

	@Override
	public void notifyAgentAdded(final IScope scope, final IPopulation pop, final IAgent agent) {
		this.addVertex(agent);
	}

	@Override
	public void notifyAgentsAdded(final IScope scope, final IPopulation pop, final Collection agents) {
		for (final Object o : agents) {
			addVertex((IAgent) o);
		}
	}

	@Override
	public void notifyAgentsRemoved(final IScope scope, final IPopulation pop, final Collection agents) {
		for (final Object o : agents) {
			removeVertex(o);
		}
	}

	@Override
	public void notifyPopulationCleared(final IScope scope, final IPopulation pop) {
		removeAllVertices(vertexSet());
	}

	public void postRefreshManagementAction(final IScope scope) {
		scope.getSimulation().postEndAction(scope1 -> {
			GamaSpatialGraph.this.refreshEdges();
			return null;
		});
	}

	@Override
	public Set<IShape> vertexSet() {
		return vertexMap.keySet();
	}

	public void addBuiltVertex(final IShape vertex) {
		verticesBuilt.put(vertex.getLocation().hashCode(), vertex);
	}

	public IShape getBuiltVertex(final Coordinate vertex) {
		if (tolerance == 0) { return verticesBuilt.get(vertex.hashCode()); }
		final IShape sh = verticesBuilt.get(vertex.hashCode());
		if (sh != null) { return sh; }
		for (final Object v : verticesBuilt.values()) {
			if (vertex.distance3D(((IShape) v).getLocation()) <= tolerance) { return (IShape) v; }
		}
		return null;
	}

	protected void buildByEdgeWithNode(final IScope scope, final IContainer edges, final IContainer vertices) {

		/*
		 * Do we want to have intersections that are not connected to any road ? At least put this in the next loop to
		 * avoid duplicates
		 *
		 * for (final Object ag : vertices.iterable(scope)) { super.addVertex(ag); }
		 */

		final IMap<GamaPoint, IAgent> nodes = GamaMapFactory.create(Types.POINT, getGamlType().getKeyType());
		for (final Object ag : vertices.iterable(scope)) {
			super.addVertex(ag);
			nodes.put(((IAgent) ag).getLocation(), (IAgent) ag);
		}
		for (final Object p : edges.iterable(scope)) {
			final boolean addEdge = addDrivingEdge(scope, (IShape) p, nodes);
			if (!addEdge) {
				continue;
			}
			getEdge(p).setWeight(((IShape) p).getPerimeter());
		}
	}

	public boolean addDrivingEdge(final IScope scope, final IShape e, final IMap<GamaPoint, IAgent> nodes) {
		if (containsEdge(e)) { return false; }
		final Coordinate[] coord = e.getInnerGeometry().getCoordinates();
		final IShape ptS = new GamaPoint(coord[0]);
		final IShape ptT = new GamaPoint(coord[coord.length - 1]);
		final IAgent v1 = nodes.get(ptS);
		if (v1 == null) { return false; }
		final IAgent v2 = nodes.get(ptT);
		if (v2 == null) { return false; }

		final IAgent ag = e.getAgent();
		final List v1ro = (List) v1.getAttribute("roads_out");
		v1ro.add(ag);
		final List v2ri = (List) v2.getAttribute("roads_in");
		v2ri.add(ag);
		ag.setAttribute("source_node", v1);
		ag.setAttribute("target_node", v2);
		addVertex(v1);
		addVertex(v2);
		_Edge<IShape, IShape> edge;
		try {
			edge = newEdge(e, v1, v2);
		} catch (final GamaRuntimeException e1) {
			e1.addContext("Impossible to create edge from " + StringUtils.toGaml(e, false) + " in graph " + this);
			throw e1;
		}
		// if ( edge == null ) { return false; }
		edgeMap.put(e, edge);
		dispatchEvent(scope, new GraphEvent(scope, this, this, e, null, GraphEventType.EDGE_ADDED));
		return true;
	}

	protected void init(final IScope scope, final IContainer edges, final IContainer vertices) {
		this.directed = true;
		edgeBased = true;
		vertexRelation = null;
		edgeSpecies = null;
		agentEdge = true;
		buildByEdgeWithNode(scope, edges, vertices);
		version = 1;
	}

	/**
	 * Method getSpecies(): returns the species of the edges if any.
	 *
	 * @see gama.metamodel.topology.filter.IAgentFilter#getSpecies()
	 */
	@Override
	public ISpecies getSpecies() {
		if (edgeSpecies != null) { return edgeSpecies; }
		final IType contents = getGamlType().getContentType();
		return getScope().getModel().getSpecies(contents.getSpeciesName());
		// return null; // See if we can identify the species of edges / vertices
	}

	@Override
	public IPopulation<? extends IAgent> getPopulation(final IScope scope) {
		return getScope().getSimulation().getPopulationFor(getSpecies());
		// return null;// See if we can identify the populations of edges / vertices
	}

	/**
	 * Method getAgents()
	 *
	 * @see gama.metamodel.topology.filter.IAgentFilter#getAgents()
	 */
	@Override
	public IContainer<?, ? extends IAgent> getAgents(final IScope scope) {
		return (IContainer<?, ? extends IAgent>) getEdges();
	}

	@Override
	public boolean hasAgentList() {
		return true;
	}

	@Override
	public IShape getEdgeSource(final IShape e) {
		if (!containsEdge(e)) { return null; }
		return (IShape) getEdge(e).getSource();
	}

	@Override
	public IShape getEdgeTarget(final IShape e) {
		if (!containsEdge(e)) { return null; }
		return (IShape) getEdge(e).getTarget();
	}

	/**
	 * Method accept()
	 *
	 * @see gama.metamodel.topology.filter.IAgentFilter#accept(gama.runtime.scope.IScope,
	 *      gama.metamodel.shape.IShape, gama.metamodel.shape.IShape)
	 */
	@Override
	public boolean accept(final IScope scope, final IShape source, final IShape a) {
		return a.getGeometry() != source.getGeometry() && containsEdge(a);
	}

	/**
	 * Method filter()
	 *
	 * @see gama.metamodel.topology.filter.IAgentFilter#filter(gama.runtime.scope.IScope,
	 *      gama.metamodel.shape.IShape, java.util.Collection)
	 */
	@Override
	public void filter(final IScope scope, final IShape source, final Collection<? extends IShape> results) {
		results.removeIf(each -> !edgeMap.containsKey(each));
	}

	public double getTolerance() {
		return tolerance;
	}

	public void setTolerance(final double tolerance) {
		this.tolerance = tolerance;
	}

}