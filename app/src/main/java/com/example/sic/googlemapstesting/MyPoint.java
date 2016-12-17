package com.example.sic.googlemapstesting;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class MyPoint implements ClusterItem {
    private final LatLng mPosition;

    public MyPoint(double lat, double lng) {
        mPosition = new LatLng(lat, lng);
    }

    @Override
    public LatLng getPosition() {
        return mPosition;
    }
}