package com.example.sic.googlemapstesting;

import com.arellomobile.mvp.MvpView;
import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy;
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType;

import io.ticofab.androidgpxparser.parser.domain.Gpx;

public interface MapView extends MvpView {

    @StateStrategyType(AddToEndSingleStrategy.class)
    void success(Gpx gpx);

    @StateStrategyType(AddToEndSingleStrategy.class)
    void error(String text);

}
