package com.smartgwt.sample.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.smartgwt.sample.client.ui.DefaultPage;

public class TreeGridFacadePatternModule implements EntryPoint {

    public void onModuleLoad() {
        GWT.setUncaughtExceptionHandler(new GWT.UncaughtExceptionHandler() {
            @Override
            public void onUncaughtException(Throwable e) {
                e.printStackTrace();
            }
        });

        new DefaultPage().draw();
    }
}
