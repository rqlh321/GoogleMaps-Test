package com.example.sic.googlemapstesting;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.arellomobile.mvp.MvpAppCompatActivity;
import com.arellomobile.mvp.presenter.InjectPresenter;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.FileNotFoundException;
import java.io.InputStream;

import io.ticofab.androidgpxparser.parser.domain.Gpx;
import io.ticofab.androidgpxparser.parser.domain.Track;
import io.ticofab.androidgpxparser.parser.domain.TrackPoint;
import io.ticofab.androidgpxparser.parser.domain.TrackSegment;

public class MapActivity extends MvpAppCompatActivity implements OnMapReadyCallback, MapView {
    private static final int FILE_SELECT_CODE = 0;

    @InjectPresenter
    MapPresenter presenter;

    private GoogleMap mMap;
    private TextView message;
private Gpx gpx;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        findViewById(R.id.add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);

                try {
                    startActivityForResult(Intent.createChooser(intent, getString(R.string.title)), FILE_SELECT_CODE);
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(MapActivity.this, R.string.error_file_select, Toast.LENGTH_SHORT).show();
                }
            }
        });
        message = findViewById(R.id.message);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == FILE_SELECT_CODE && data.getData() != null) {
            try {
                InputStream stream = getContentResolver().openInputStream(Uri.parse(data.getData().toString()));
                presenter.parse(stream);
            } catch (FileNotFoundException e) {
                Toast.makeText(MapActivity.this, R.string.error_file, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        success(gpx);
    }

    @Override
    public void success(Gpx gpx) {
        this.gpx=gpx;
        message.setText("");
        if(gpx!=null&&mMap!=null){
            mMap.clear();
            for (Track track : gpx.getTracks()) {
                for (TrackSegment segment : track.getTrackSegments()) {
                    LatLngBounds.Builder builder = new LatLngBounds.Builder();

                    PolylineOptions options = new PolylineOptions();
                    for (TrackPoint point : segment.getTrackPoints()) {
                        LatLng latLng = new LatLng(point.getLatitude(), point.getLongitude());
                        options.add(latLng);
                        builder.include(latLng);
                    }
                    mMap.addPolyline(options);
                    mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 5));
                }
            }
        }

    }

    @Override
    public void error(String text) {
        message.setText(text);
    }
}
