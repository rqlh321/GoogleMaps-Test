package com.example.sic.googlemapstesting;

import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.util.LineStringExtracter;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.operation.polygonize.Polygonizer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Utils {
    private static GeometryFactory geometryFactory = new GeometryFactory();

    public static void handlePolyline(GoogleMap googleMap, Geometry geometry) {
        Coordinate[] coordinates = geometry.getCoordinates();
        PolylineOptions polygonOptions = new PolylineOptions();
        for (Coordinate coordinate : coordinates) {
            LatLng latLng = new LatLng(coordinate.y, coordinate.x);
            polygonOptions.add(latLng);
        }
        googleMap.addPolyline(polygonOptions);
    }

    public static void handlePoint(GoogleMap googleMap, Coordinate coordinate) {
        MarkerOptions point = new MarkerOptions();
        LatLng latLng = new LatLng(coordinate.y, coordinate.x);
        point.position(latLng);
        googleMap.addMarker(point);
    }

    public static void handlePolygon(GoogleMap googleMap, CopyOnWriteArrayList<Geometry> geometryList) {
        for (Geometry geometry : geometryList) {
            Coordinate[] coordinates = geometry.getCoordinates();
            PolygonOptions polygonOptions = new PolygonOptions();
            polygonOptions.strokeWidth(3);
            for (Coordinate coordinate : coordinates) {
                LatLng latLng = new LatLng(coordinate.y, coordinate.x);
                polygonOptions.add(latLng);
            }
            googleMap.addPolygon(polygonOptions);
        }
    }

    public static Geometry getGeometry(String string) {
        try {
            GeometryFactory factory = new GeometryFactory();
            WKTReader wktReader = new WKTReader(factory);
            return wktReader.read(string);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static Geometry polygonize(Geometry geometry) {
        List lines = LineStringExtracter.getLines(geometry);
        Polygonizer polygonizer = new Polygonizer();
        polygonizer.add(lines);
        Collection polys = polygonizer.getPolygons();
        Polygon[] polyArray = GeometryFactory.toPolygonArray(polys);
        return geometry.getFactory().createGeometryCollection(polyArray);
    }

    private static Geometry splitPolygon(Geometry poly, Geometry line) {
        Geometry nodedLinework = poly.getBoundary().union(line);
        Geometry polys = polygonize(nodedLinework);

        // Only keep polygons which are inside the input
        List output = new ArrayList();
        for (int i = 0; i < polys.getNumGeometries(); i++) {
            Polygon candpoly = (Polygon) polys.getGeometryN(i);
            if (poly.contains(candpoly.getInteriorPoint())) {
                output.add(candpoly);
            }
        }
        return poly.getFactory().createGeometryCollection(GeometryFactory.toGeometryArray(output));
    }

    private static Geometry divideByY(Geometry geometry) {
        Coordinate borderPoint = geometry.getCoordinate();
        Coordinate centerPoint = geometry.getCentroid().getCoordinate();
        double halfHeight = Math.abs(borderPoint.y - centerPoint.y);

        LineString yLine = geometryFactory.createLineString(new Coordinate[]{
                new Coordinate(centerPoint.x, centerPoint.y + halfHeight + 1),
                new Coordinate(centerPoint.x, centerPoint.y - halfHeight - 1)
        });

        return Utils.splitPolygon(geometry, yLine.getGeometryN(0));
    }

    private static Geometry divideByX(Geometry geometry) {
        Coordinate borderPoint = geometry.getCoordinate();
        Coordinate centerPoint = geometry.getCentroid().getCoordinate();
        double halfWeight = Math.abs(borderPoint.x - centerPoint.x);

        LineString xLine = geometryFactory.createLineString(new Coordinate[]{
                new Coordinate(centerPoint.x + halfWeight + 1, centerPoint.y),
                new Coordinate(centerPoint.x - halfWeight - 1, centerPoint.y)
        });

        return Utils.splitPolygon(geometry, xLine.getGeometryN(0));
    }

    public static CopyOnWriteArrayList<Geometry> divide(Geometry fullPolygon) {
        CopyOnWriteArrayList<Geometry> geometries = new CopyOnWriteArrayList<>();
        geometries.add(fullPolygon);

        Coordinate borderPoint = fullPolygon.getCoordinate();
        Coordinate centerPoint = fullPolygon.getCentroid().getCoordinate();
        double weight = Math.abs(borderPoint.x - centerPoint.x) * 2;
        long roundedWeigh = Math.round(weight);
        double count = Math.pow(2, (int) roundedWeigh / 2);

        while (geometries.size() < count) {
            for (Geometry polygon : geometries) {
                Geometry dividedByX = divideByX(polygon);
                geometries.remove(polygon);
                for (int i = 0; i < dividedByX.getNumGeometries(); i++) {
                    Geometry geometry = dividedByX.getGeometryN(i);
                    Geometry dividedByY = divideByY(geometry);
                    for (int j = 0; j < dividedByY.getNumGeometries(); j++) {
                        geometries.add(dividedByY.getGeometryN(j));
                    }
                }
            }
        }
        return geometries;
    }

    public static CopyOnWriteArrayList<Geometry> gluePolygons(CopyOnWriteArrayList<Geometry> polygonList) {
        boolean unite = true;
        if (polygonList.size() > 1) {
            while (unite) {
                unite = false;
                for (Geometry cmp : polygonList) {
                    for (Geometry next : polygonList) {
                        if (!next.equals(cmp)) {
                            if (cmp.contains(next) || next.contains(cmp) || next.intersects(cmp)) {
                                try {
                                    Geometry union = cmp.union(next);
                                    Polygon polygon = geometryFactory.createPolygon(union.getCoordinates());
                                    polygonList.add(0, polygon);
                                    polygonList.remove(next);
                                    polygonList.remove(cmp);
                                    unite = true;
                                } catch (IllegalArgumentException e) {
                                    Log.d(Utils.class.getSimpleName(), e.getMessage(), e);
                                }
                            }
                        }
                    }
                }
            }
        }
        return polygonList;
    }
}
