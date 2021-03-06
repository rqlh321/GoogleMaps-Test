package com.example.sic.googlemapstesting;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

public class OwnIconRendered extends DefaultClusterRenderer<MyPoint> {
    Context context;

    public OwnIconRendered(Context context, GoogleMap map, ClusterManager<MyPoint> clusterManager) {
        super(context, map, clusterManager);
        this.context = context;
    }

    @Override
    protected void onBeforeClusterItemRendered(MyPoint item, MarkerOptions markerOptions) {
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher));
        markerOptions.visible(item.visible);
    }

}