package com.example.sic.googlemapstesting;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;

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

    private GoogleMap mMap;

    public static void handleGeometry(GeoJsonObject object, GoogleMap mMap) {
        if (object instanceof Polygon) {
            List<LngLatAlt> polygonCoordinates = ((Polygon) object).getExteriorRing();
            PolygonOptions polygonOptions = new PolygonOptions();
            for (LngLatAlt coordinate : polygonCoordinates) {
                polygonOptions.add(new LatLng(coordinate.getLatitude(), coordinate.getLongitude()));
            }
            mMap.addPolygon(polygonOptions);
        } else if (object instanceof LineString) {
            List<LngLatAlt> lineCoordinates = ((LineString) object).getCoordinates();
            PolylineOptions polyLineOptions = new PolylineOptions();
            for (LngLatAlt coordinate : lineCoordinates) {
                polyLineOptions.add(new LatLng(coordinate.getLatitude(), coordinate.getLongitude()));
            }
            mMap.addPolyline(polyLineOptions);
        } else if (object instanceof Point) {
            LngLatAlt pointCoordinates = ((Point) object).getCoordinates();
            MarkerOptions markerOptions = new MarkerOptions();
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
