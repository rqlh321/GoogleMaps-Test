package com.example.sic.googlemapstesting;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;

import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.geojson.GeoJsonObject;
import org.geojson.LineString;
import org.geojson.LngLatAlt;
import org.geojson.Point;
import org.geojson.Polygon;

import java.io.IOException;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    // Declare a variable for the cluster manager.
    ClusterManager<MyPoint> mClusterManager;
    private GoogleMap mMap;

    public static void handleGeometry(GeoJsonObject object, GoogleMap mMap) {
        if (object instanceof Polygon) {
            List<LngLatAlt> polygonCoordinates = ((Polygon) object).getExteriorRing();
            PolygonOptions polygonOptions = new PolygonOptions();
            polygonOptions.strokeWidth(3);
            polygonOptions.strokeColor(Color.argb(100, 255, 10, 10));
            polygonOptions.fillColor(Color.argb(40, 255, 10, 10));
            polygonOptions.clickable(true);
            for (LngLatAlt coordinate : polygonCoordinates) {
                polygonOptions.add(new LatLng(coordinate.getLatitude(), coordinate.getLongitude()));
            }
            mMap.addPolygon(polygonOptions);
        } else if (object instanceof LineString) {
            List<LngLatAlt> lineCoordinates = ((LineString) object).getCoordinates();
            PolylineOptions polyLineOptions = new PolylineOptions();
            polyLineOptions.width(2);
            polyLineOptions.color(Color.argb(100, 255, 0, 10));
            for (LngLatAlt coordinate : lineCoordinates) {
                polyLineOptions.add(new LatLng(coordinate.getLatitude(), coordinate.getLongitude()));
            }
            mMap.addPolyline(polyLineOptions);
        } else if (object instanceof Point) {
            LngLatAlt pointCoordinates = ((Point) object).getCoordinates();
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher));
            markerOptions.position(new LatLng(pointCoordinates.getLatitude(), pointCoordinates.getLongitude()));
            mMap.addMarker(markerOptions);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        setUpClusterer();

        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);

        mMap.setOnPolygonClickListener(new GoogleMap.OnPolygonClickListener() {
            @Override
            public void onPolygonClick(com.google.android.gms.maps.model.Polygon polygon) {
                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                polygon.setFillColor(Color.argb(100, 255, 10, 10));
            }
        });

        mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                mMap.clear();
                addFromGeoJson();
                mClusterManager.clearItems();
                addItems();
                mClusterManager.cluster();

            }
        });
    }

    private void setUpClusterer() {
        // Position the map.
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(51.503186, -0.126446), 10));

        // Initialize the manager with the context and the map.
        // (Activity extends context, so we can pass 'this' in the constructor.)
        mClusterManager = new ClusterManager<>(this, mMap);
        mClusterManager.setRenderer(new OwnIconRendered(this, mMap, mClusterManager));
        mClusterManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<MyPoint>() {
            @Override
            public boolean onClusterItemClick(MyPoint myPoint) {
                Toast.makeText(MapsActivity.this, myPoint.getMessage(), Toast.LENGTH_SHORT).show();
                return false;
            }
        });
        mClusterManager.setOnClusterClickListener(new ClusterManager.OnClusterClickListener<MyPoint>() {
            @Override
            public boolean onClusterClick(Cluster<MyPoint> cluster) {
                String messages = "";
                for (MyPoint myPoint : cluster.getItems()) {
                    messages += myPoint.getMessage() + "\n";
                }
                Toast.makeText(MapsActivity.this, messages, Toast.LENGTH_SHORT).show();
                return false;
            }
        });
        // Point the map's listeners at the listeners implemented by the cluster
        // manager.
        //mMap.setOnCameraIdleListener(mClusterManager);
        //mMap.setOnMarkerClickListener(mClusterManager);
    }

    private void addItems() {

        // Set some lat/lng coordinates to start with.
        double lat = 51.5145160;
        double lng = -0.1270060;

        // Add ten cluster items in close proximity, for purposes of this example.
        for (int i = 0; i < 30; i++) {
            double offset = i / 60d;
            lat = lat + offset;
            lng = lng + offset;
            MyPoint offsetItem = new MyPoint(lat, lng, String.valueOf(offset));
            mClusterManager.addItem(offsetItem);
        }
    }

    private void addFromGeoJson(){
        String geoJsonString =
                " { \"type\": \"FeatureCollection\",\n" +
                        "    \"features\": [\n" +
                        "      { \"type\": \"Feature\",\n" +
                        "        \"geometry\": {\"type\": \"Point\", \"coordinates\": [102.0, 0.5]},\n" +
                        "        \"properties\": {\"prop0\": \"value0\"}\n" +
                        "        },\n" +
                        "      { \"type\": \"Feature\",\n" +
                        "        \"geometry\": {\n" +
                        "          \"type\": \"LineString\",\n" +
                        "          \"coordinates\": [\n" +
                        "            [102.0, 0.0], [103.0, 1.0], [104.0, 0.0], [105.0, 1.0]\n" +
                        "            ]\n" +
                        "          },\n" +
                        "        \"properties\": {\n" +
                        "          \"prop0\": \"value0\",\n" +
                        "          \"prop1\": 0.0\n" +
                        "          }\n" +
                        "        },\n" +
                        "      { \"type\": \"Feature\",\n" +
                        "         \"geometry\": {\n" +
                        "           \"type\": \"Polygon\",\n" +
                        "           \"coordinates\": [\n" +
                        "             [ [100.0, 0.0], [101.0, 0.0], [101.0, 1.0],\n" +
                        "               [100.0, 1.0], [100.0, 0.0] ]\n" +
                        "             ]\n" +
                        "         },\n" +
                        "         \"properties\": {\n" +
                        "           \"prop0\": \"value0\",\n" +
                        "           \"prop1\": {\"this\": \"that\"}\n" +
                        "           }\n" +
                        "         }\n" +
                        "       ]\n" +
                        "     }";

        try {
            GeoJsonObject geoJsonObject = new ObjectMapper().readValue(geoJsonString, GeoJsonObject.class);
            if (geoJsonObject instanceof FeatureCollection) {
                List<Feature> features = ((FeatureCollection) geoJsonObject).getFeatures();
                for (Feature feature : features) {
                    GeoJsonObject object = feature.getGeometry();
                    handleGeometry(object, mMap);
                }
            } else {
                handleGeometry(geoJsonObject, mMap);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
