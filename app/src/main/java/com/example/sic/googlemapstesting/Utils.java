package com.example.sic.googlemapstesting;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.util.LineStringExtracter;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.operation.polygonize.Polygonizer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Utils {
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

    public static Geometry polygonize(Geometry geometry) {
        List lines = LineStringExtracter.getLines(geometry);
        Polygonizer polygonizer = new Polygonizer();
        polygonizer.add(lines);
        Collection polys = polygonizer.getPolygons();
        Polygon[] polyArray = GeometryFactory.toPolygonArray(polys);
        return geometry.getFactory().createGeometryCollection(polyArray);
    }

    public static Geometry splitPolygon(Geometry poly, Geometry line) {
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

    public static void handlePolygon(GoogleMap googleMap, Geometry geometry) {
        Coordinate[] coordinates = geometry.getCoordinates();
        PolygonOptions polygonOptions = new PolygonOptions();
        polygonOptions.strokeWidth(3);
        for (Coordinate coordinate : coordinates) {
            LatLng latLng = new LatLng(coordinate.y, coordinate.x);
            polygonOptions.add(latLng);
        }
        googleMap.addPolygon(polygonOptions);
    }

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
}
