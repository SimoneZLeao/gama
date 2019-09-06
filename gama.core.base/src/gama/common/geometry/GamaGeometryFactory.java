/*******************************************************************************************************
 *
 * gama.common.geometry.GamaGeometryFactory.java, in plugin gama.core, is part of the source code of the GAMA
 * modeling and simulation platform (v. 1.8)
 *
 * (c) 2007-2018 UMI 209 UMMISCO IRD/SU & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.common.geometry;

import java.util.List;

import org.apache.commons.lang.ArrayUtils;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequenceFactory;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.impl.CoordinateArraySequenceFactory;

import gama.metamodel.shape.GamaPoint;

public class GamaGeometryFactory extends GeometryFactory {

	public static final GamaCoordinateSequenceFactory COORDINATES_FACTORY = new GamaCoordinateSequenceFactory();
	public static final CoordinateSequenceFactory JTS_COORDINATES_FACTORY = CoordinateArraySequenceFactory.instance();

	public GamaGeometryFactory() {
		super(COORDINATES_FACTORY);
	}

	public static boolean isRing(final Coordinate[] pts) {
		if (pts.length < 4) { return false; }
		if (!pts[0].equals(pts[pts.length - 1])) { return false; }
		return true;
	}

	public static boolean isRing(final List<GamaPoint> pts) {
		final int size = pts.size();
		if (size < 4) { return false; }
		if (!pts.get(0).equals(pts.get(size - 1))) { return false; }
		return true;
	}

	// public static boolean isRing(final double[] array) {
	// final int size = array.length;
	// if (size < 12) { return false; }
	// if (array[0] != array[size - 3] || array[1] != array[size - 2] || array[2] != array[size - 1]) { return false; }
	// return true;
	// }

	/**
	 * Linear rings are created using a simple coordinate array, without enforcing any clockwiseness condition.
	 */
	@Override
	public LinearRing createLinearRing(final Coordinate[] coordinates) {
		Coordinate[] coords = coordinates;
		if (!isRing(coords)) {
			coords = (Coordinate[]) ArrayUtils.add(coords, coords[0]);
		}
		return createLinearRing(JTS_COORDINATES_FACTORY.create(coords));
	}

	/**
	 * Polygons are created after ensuring that the coordinate sequence in them has been turned clockwise
	 */

	@Override
	public Polygon createPolygon(final LinearRing shell, final LinearRing[] holes) {
		final LinearRing shellClockwise = turnClockwise(shell);
		if (holes != null) {
			for (int i = 0; i < holes.length; i++) {
				holes[i] = turnClockwise(holes[i]);
			}
		}
		return super.createPolygon(shellClockwise, holes);
	}

	private LinearRing turnClockwise(final LinearRing ring) {
		if (ring == null || ring.isEmpty()) { return ring; }
		return createLinearRing(COORDINATES_FACTORY.create(ring.getCoordinateSequence()));
	}

	@Override
	public GamaCoordinateSequenceFactory getCoordinateSequenceFactory() {
		return COORDINATES_FACTORY;
	}

	public LineString createLineString(final GamaPoint[] coordinates, final boolean copyPoints) {
		return createLineString(COORDINATES_FACTORY.create(coordinates, copyPoints));
	}

}