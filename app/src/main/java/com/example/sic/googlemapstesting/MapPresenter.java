package com.example.sic.googlemapstesting;

import com.arellomobile.mvp.InjectViewState;
import com.arellomobile.mvp.MvpPresenter;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;

import io.ticofab.androidgpxparser.parser.GPXParser;
import io.ticofab.androidgpxparser.parser.domain.Gpx;

@InjectViewState
public class MapPresenter extends MvpPresenter<MapView> {
    private GPXParser mParser = new GPXParser();

    void parse(InputStream stream) {
        try {
            Gpx gpx = mParser.parse(stream);
            getViewState().success(gpx);
        } catch (IOException | XmlPullParserException e) {
            getViewState().error(e.getMessage());
        }
    }

}
