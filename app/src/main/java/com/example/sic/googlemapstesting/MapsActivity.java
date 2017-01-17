package com.example.sic.googlemapstesting;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;
import com.google.android.gms.maps.model.UrlTileProvider;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.geojson.GeoJsonFeature;
import com.google.maps.android.geojson.GeoJsonLayer;
import com.google.maps.android.geojson.GeoJsonPoint;
import com.google.maps.android.geojson.GeoJsonPointStyle;
import com.google.maps.android.geojson.GeoJsonPolygon;
import com.google.maps.android.geojson.GeoJsonPolygonStyle;

import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.geojson.GeoJsonObject;
import org.geojson.LineString;
import org.geojson.LngLatAlt;
import org.geojson.Point;
import org.geojson.Polygon;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, SeekBar.OnSeekBarChangeListener {
    // Declare a variable for the cluster manager.
    ClusterManager<MyPoint> mClusterManager;
    ArrayList<MyPoint> points = new ArrayList<>();
    private GoogleMap mMap;
    private static final int TRANSPARENCY_MAX = 100;

    /**
     * This returns moon tiles.
     */
    private static final String MOON_MAP_URL_FORMAT =
            "http://mw1.google.com/mw-planetary/lunar/lunarmaps_v1/clem_bw/%d/%d/%d.jpg";

    private TileOverlay mMoonTiles;
    private SeekBar mTransparencyBar;

    public void handleGeometry(GeoJsonObject object) {
        if (object instanceof Polygon) {
            List<LngLatAlt> polygonCoordinates = ((Polygon) object).getExteriorRing();
            PolygonOptions polygonOptions = new PolygonOptions();
            polygonOptions.strokeWidth(3);
            polygonOptions.strokeColor(Color.argb(100, 255, 10, 10));
            polygonOptions.fillColor(Color.argb(40, 255, 10, 10));
            polygonOptions.clickable(true);
            polygonOptions.zIndex(1);
            for (LngLatAlt coordinate : polygonCoordinates) {
                polygonOptions.add(new LatLng(coordinate.getLatitude(), coordinate.getLongitude()));
            }
            mMap.addPolygon(polygonOptions);
        } else if (object instanceof LineString) {
            List<LngLatAlt> lineCoordinates = ((LineString) object).getCoordinates();
            PolylineOptions polyLineOptions = new PolylineOptions();
            polyLineOptions.width(2);
            polyLineOptions.color(Color.argb(100, 255, 0, 10));
            polyLineOptions.zIndex(1);
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

        mTransparencyBar = (SeekBar) findViewById(R.id.transparencySeekBar);
        mTransparencyBar.setMax(TRANSPARENCY_MAX);
        mTransparencyBar.setProgress(0);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        setupClusterManager();

        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        mMap.setOnPolygonClickListener(new GoogleMap.OnPolygonClickListener() {
            @Override
            public void onPolygonClick(com.google.android.gms.maps.model.Polygon polygon) {
                Toast.makeText(MapsActivity.this, polygon.getClass().getName(), Toast.LENGTH_SHORT).show();
            }
        });
        addFromGeoJson();
        addClusterItems();

        mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                mClusterManager.cluster();
            }
        });
        tileOverlayTest();
    }

    private void setupClusterManager() {
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
        mClusterManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<MyPoint>() {
            @Override
            public boolean onClusterItemClick(MyPoint myPoint) {
                Toast.makeText(MapsActivity.this, myPoint.getMessage(), Toast.LENGTH_SHORT).show();
                return false;
            }
        });
    }

    private void addClusterItems() {

        // Set some lat/lng coordinates to start with.
        double lat = 51.5145160;
        double lng = -0.1270060;

        // Add ten cluster items in close proximity, for purposes of this example.
        for (int i = 0; i < 30; i++) {
            double offset = i / 60d;
            lat = lat + offset;
            lng = lng + offset;
            MyPoint offsetItem = new MyPoint(lat, lng, String.valueOf(offset));
            points.add(offsetItem);
            mClusterManager.addItem(offsetItem);
        }
    }

    private void addFromGeoJson() {
        try {
            JSONObject jsonObject = new JSONObject(Constants.google);
            GeoJsonLayer geoJsonLayer = new GeoJsonLayer(mMap, jsonObject);
            for (GeoJsonFeature feature : geoJsonLayer.getFeatures()) {
                if (feature.getGeometry() instanceof GeoJsonPolygon) {
                    GeoJsonPolygonStyle style = new GeoJsonPolygonStyle();
                    style.setFillColor(Color.BLUE);
                    feature.setPolygonStyle(style);
                }
                if (feature.getGeometry() instanceof GeoJsonPoint) {
                    GeoJsonPointStyle style = new GeoJsonPointStyle();
                    feature.setPointStyle(style);
                }
            }
            geoJsonLayer.setOnFeatureClickListener(new GeoJsonLayer.GeoJsonOnFeatureClickListener() {
                @Override
                public void onFeatureClick(GeoJsonFeature geoJsonFeature) {
                    if (geoJsonFeature != null) {
                        Toast.makeText(MapsActivity.this, geoJsonFeature.getGeometry().getType(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
            geoJsonLayer.addLayerToMap();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            GeoJsonObject geoJsonObject = new ObjectMapper().readValue(Constants.geoJsonString, GeoJsonObject.class);
            if (geoJsonObject instanceof FeatureCollection) {
                List<Feature> features = ((FeatureCollection) geoJsonObject).getFeatures();
                for (Feature feature : features) {
                    GeoJsonObject object = feature.getGeometry();
                    handleGeometry(object);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void tileOverlayTest() {
        TileProvider tileProvider = new UrlTileProvider(256, 256) {
            @Override
            public synchronized URL getTileUrl(int x, int y, int zoom) {
                // The moon tile coordinate system is reversed.  This is not normal.
                int reversedY = (1 << zoom) - y - 1;
                String s = String.format(Locale.US, MOON_MAP_URL_FORMAT, zoom, x, reversedY);
                System.out.println(s);
                URL url = null;
                try {
                    url = new URL(s);
                } catch (MalformedURLException e) {
                    throw new AssertionError(e);
                }
                return url;
            }
        };
        TileOverlayOptions tileOverlay = new TileOverlayOptions().tileProvider(tileProvider);
        tileOverlay.zIndex(0);
        mMoonTiles = mMap.addTileOverlay(tileOverlay);
        mTransparencyBar.setOnSeekBarChangeListener(this);
    }

    public void setFadeIn(View v) {
        if (mMoonTiles == null) {
            return;
        }
        mMoonTiles.setFadeIn(((CheckBox) v).isChecked());
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (mMoonTiles != null) {
            mMoonTiles.setTransparency((float) progress / (float) TRANSPARENCY_MAX);
        }
    }
}
