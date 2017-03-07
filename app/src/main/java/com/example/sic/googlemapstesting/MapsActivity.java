package com.example.sic.googlemapstesting;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;


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
    public void onMapReady(final GoogleMap googleMap) {
        final ArrayList<LatLng> list = new ArrayList<>();
        list.add(new LatLng(0, 0));
        final PolygonOptions polygonOptions = new PolygonOptions();
        polygonOptions.strokeWidth(3);
        polygonOptions.fillColor(Color.BLUE);
        polygonOptions.strokeColor(Color.BLUE);
        final MarkerOptions options = new MarkerOptions();

        Consumer<? super LatLng> lines = new Consumer<LatLng>() {
            @Override
            public void accept(LatLng latLng) throws Exception {
                final PolylineOptions lineOptions = new PolylineOptions();
                lineOptions.width(5);
                lineOptions.color(Color.rgb((int) (Math.random() * 100), (int) (Math.random() * 100), (int) (Math.random() * 100)));
                if (list.size() == 2) {
                    list.remove(0);
                    list.add(latLng);
                    lineOptions.addAll(list);
                    googleMap.addPolyline(lineOptions);
                } else {
                    list.add(latLng);
                    lineOptions.addAll(list);
                    googleMap.addPolyline(lineOptions);
                }

            }
        };
        Consumer<? super LatLng> points = new Consumer<LatLng>() {
            @Override
            public void accept(LatLng latLng) throws Exception {
                options.position(latLng);
                googleMap.addMarker(options);
            }
        };
        Consumer<? super LatLng> polygon = new Consumer<LatLng>() {
            @Override
            public void accept(LatLng latLng) throws Exception {
                polygonOptions.add(latLng);
                googleMap.addPolygon(polygonOptions);
            }
        };

        Observable observable = Observable.interval(100, TimeUnit.MILLISECONDS)
                .map(new Function<Long, LatLng>() {
                    @Override
                    public LatLng apply(Long aLong) throws Exception {
                        return new LatLng(Math.random() * 100, Math.random() * 100);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());

        observable.subscribe(lines);
        // observable.subscribe(points);
        //   observable.subscribe(polygon);


    }

    private void timer(final GoogleMap googleMap) {
        Consumer<? super LatLng> subscriber = new Consumer<LatLng>() {
            @Override
            public void accept(LatLng latLng) throws Exception {
                MarkerOptions options = new MarkerOptions();
                options.position(latLng);
                googleMap.addMarker(options);
            }
        };

        Observable.timer(500, TimeUnit.MILLISECONDS)
                .map(new Function<Long, LatLng>() {
                    @Override
                    public LatLng apply(Long aLong) throws Exception {
                        return new LatLng(Math.random() * 10, Math.random() * 10);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

}
