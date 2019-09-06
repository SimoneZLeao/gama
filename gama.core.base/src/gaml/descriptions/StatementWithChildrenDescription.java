/*******************************************************************************************************
 *
 * gaml.descriptions.StatementWithChildrenDescription.java, in plugin gama.core, is part of the source code of
 * the GAMA modeling and simulation platform (v. 1.8)
 *
 * (c) 2007-2018 UMI 209 UMMISCO IRD/SU & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.descriptions;

import java.util.Arrays;
import java.util.List;

import org.eclipse.emf.ecore.EObject;

import com.google.common.collect.Iterables;

import gama.common.interfaces.IGamlIssue;
import gama.common.util.Collector;
import gama.util.map.GamaMapFactory;
import gama.util.map.IMap;
import gaml.expressions.IExpression;
import gaml.expressions.IVarExpression;
import gaml.statements.Arguments;
import gaml.statements.Facets;
import gaml.types.IType;

public class StatementWithChildrenDescription extends StatementDescription {

	protected IMap<String, IVarExpression> temps;
	protected final Collector.AsList<IDescription> children = Collector.newList();

	public StatementWithChildrenDescription(final String keyword, final IDescription superDesc,
			final Iterable<IDescription> cp, final boolean hasArgs, final EObject source, final Facets facets,
			final Arguments alreadyComputedArgs) {
		super(keyword, superDesc, hasArgs, /* cp, */source, facets, alreadyComputedArgs);
		addChildren(cp);
	}

	@Override
	public boolean visitChildren(final DescriptionVisitor<IDescription> visitor) {
		for (final IDescription d : children) {
			if (!visitor.process(d)) { return false; }
		}
		return true;
	}

	@Override
	public boolean visitOwnChildrenRecursively(final DescriptionVisitor<IDescription> visitor) {
		for (final IDescription d : children) {
			if (!visitor.process(d)) { return false; }
			if (!d.visitOwnChildrenRecursively(visitor)) { return false; }
		}
		return true;

	}

	@Override
	public boolean visitOwnChildren(final DescriptionVisitor<IDescription> visitor) {
		for (final IDescription d : children) {
			if (!visitor.process(d)) { return false; }
		}
		return true;
	}

	@Override
	public Iterable<IDescription> getOwnChildren() {
		return children;
	}

	@Override
	public void dispose() {
		super.dispose();
		Collector.release(children);
		if (temps != null) {
			temps.forEachValue(object -> {
				object.dispose();
				return true;
			});
		}
		temps = null;
	}

	@Override
	public boolean hasAttribute(final String name) {
		return temps != null && temps.containsKey(name);
	}

	@Override
	public IExpression getVarExpr(final String name, final boolean asField) {
		if (temps != null) { return temps.get(name); }
		return null;
	}

	public boolean hasTemps() {
		return getMeta().hasScope() /* canHaveTemps */ && temps != null;
	}

	public IExpression addTemp(final IDescription declaration, final String name, final IType<?> type) {
		// TODO Should separate validation from execution, here.

		if (!getMeta().hasScope() /* canHaveTemps */) {
			if (getEnclosingDescription() == null) { return null; }
			if (!(getEnclosingDescription() instanceof StatementWithChildrenDescription)) { return null; }

			return ((StatementWithChildrenDescription) getEnclosingDescription()).addTemp(declaration, name, type);
		}
		final String kw = getKeyword();
		final String facet = LET.equals(kw) || LOOP.equals(kw) ? NAME : RETURNS;
		if (temps == null) {
			temps = GamaMapFactory.createUnordered(4);
		}
		if (temps.containsKey(name) && !name.equals(MYSELF)) {
			declaration.warning("This declaration of " + name + " shadows a previous declaration",
					IGamlIssue.SHADOWS_NAME, facet);
		}
		final SpeciesDescription sd = declaration.getSpeciesContext();
		final ModelDescription md = declaration.getModelDescription();
		if (sd != null && sd != md && sd.hasAttribute(name)) {
			declaration.warning(
					"This declaration of " + name + " shadows the declaration of an attribute of " + sd.getName(),
					IGamlIssue.SHADOWS_NAME, facet);
		}
		if (md != null && md.hasAttribute(name)) {
			declaration.warning("This declaration of " + name + " shadows the declaration of a global attribute",
					IGamlIssue.SHADOWS_NAME, facet);
		}
		final IExpression result = gaml.GAML.getExpressionFactory().createVar(name, type, false,
				IVarExpression.TEMP, this);
		temps.put(name, (IVarExpression) result);
		return result;
	}

	@Override
	public IDescription addChild(final IDescription child) {
		final IDescription d = super.addChild(child);
		if (d != null) {
			children.add(child);
		}
		return d;
	}

	@Override
	public StatementWithChildrenDescription copy(final IDescription into) {
		final Iterable<IDescription> children = Iterables.transform(this.children, each -> each.copy(into));
		final StatementWithChildrenDescription desc = new StatementWithChildrenDescription(getKeyword(), into, children,
				false, element, getFacetsCopy(), passedArgs == null ? null : passedArgs.cleanCopy());
		desc.originName = getOriginName();
		return desc;
	}

	@Override
	public void replaceChildrenWith(final Iterable<IDescription> array) {
		final List<IDescription> descs = Arrays.asList(Iterables.toArray(array, IDescription.class));
		children.clear();
		children.addAll(descs);
	}

	/**
	 * @return
	 */
	public boolean isBreakable() {
		return getMeta().isBreakable();
	}

	@Override
	public IVarExpression addNewTempIfNecessary(final String facetName, final IType type) {
		if (getKeyword().equals(LOOP) && facetName.equals(NAME)) {
			// Case of loops: the variable is inside the loop (not outside)
			return (IVarExpression) addTemp(this, getLitteral(facetName), type);
		}
		return super.addNewTempIfNecessary(facetName, type);
	}

}