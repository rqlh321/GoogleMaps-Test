package com.example.sic.googlemapstesting;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.vividsolutions.jts.geom.Geometry;

import java.util.concurrent.CopyOnWriteArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

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
        CopyOnWriteArrayList<Geometry> divideList = Utils.divide(fullPolygon);
        CopyOnWriteArrayList<Geometry> gluedList = Utils.gluePolygons(divideList);
        Utils.handlePolygon(googleMap, gluedList);

        /*for (Geometry geometry : divideList) {
            Utils.handlePolygon(googleMap, geometry);
        }*/

    }

}
