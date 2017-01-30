package com.example.sic.googlemapstesting;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    GeometryFactory geometryFactory = new GeometryFactory();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Geometry fullPolygon = Utils.getGeometry(Constants.TEST_POLYGON);
        Geometry dividedByX = divideByX(fullPolygon);
        for (int i = 0; i < dividedByX.getNumGeometries(); i++) {
            Geometry geometry = dividedByX.getGeometryN(i);
            Geometry dividedByY = divideByY(geometry);
            System.out.print(dividedByY.getNumGeometries());
            Utils.handlePolygon(googleMap, dividedByY);
        }
    }

    private Geometry divideByY(Geometry geometry) {
        Coordinate borderPoint = geometry.getCoordinate();
        Coordinate centerPoint = geometry.getCentroid().getCoordinate();
        double halfHeight = Math.abs(borderPoint.y - centerPoint.y);

        LineString yLine = geometryFactory.createLineString(new Coordinate[]{
                new Coordinate(centerPoint.x, centerPoint.y + halfHeight + 1),
                new Coordinate(centerPoint.x, centerPoint.y - halfHeight - 1)
        });

        return Utils.splitPolygon(geometry, yLine.getGeometryN(0));
    }

    private Geometry divideByX(Geometry geometry) {
        Coordinate borderPoint = geometry.getCoordinate();
        Coordinate centerPoint = geometry.getCentroid().getCoordinate();
        double halfWeight = Math.abs(borderPoint.x - centerPoint.x);

        LineString xLine = geometryFactory.createLineString(new Coordinate[]{
                new Coordinate(centerPoint.x + halfWeight + 1, centerPoint.y),
                new Coordinate(centerPoint.x - halfWeight - 1, centerPoint.y)
        });

        return Utils.splitPolygon(geometry, xLine.getGeometryN(0));
    }
}
