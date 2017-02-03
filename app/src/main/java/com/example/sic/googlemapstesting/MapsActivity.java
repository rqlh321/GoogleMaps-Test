package com.example.sic.googlemapstesting;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.vividsolutions.jts.geom.Geometry;

import java.util.ArrayList;
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
        ArrayList<Geometry> ring = new ArrayList<>();
        ring.add(Utils.getGeometry(Constants.TOP));
        ring.add(Utils.getGeometry(Constants.RIGHT));
        ring.add(Utils.getGeometry(Constants.BOTTOM));
        ring.add(Utils.getGeometry(Constants.LEFT));
        ring.add(Utils.getGeometry(Constants.CENTER));

        ArrayList<Geometry> fullRing = Utils.glue(ring);
        Utils.handlePolygon(googleMap, fullRing);
    }

}
