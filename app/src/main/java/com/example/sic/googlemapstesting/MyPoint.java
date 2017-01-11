package com.example.sic.googlemapstesting;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class MyPoint implements ClusterItem {
    private final LatLng mPosition;
    private final String message;
    public boolean visible = true;

    public MyPoint(double lat, double lng, String messageInMarker) {
        mPosition = new LatLng(lat, lng);
        message = messageInMarker;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public LatLng getPosition() {
        return mPosition;
    }
}