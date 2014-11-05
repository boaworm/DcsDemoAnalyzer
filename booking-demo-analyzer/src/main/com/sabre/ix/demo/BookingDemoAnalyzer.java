package com.sabre.ix.demo;

import com.sabre.ix.client.Booking;
import com.sabre.ix.client.analyzer.Analyzer;
import com.sabre.ix.client.analyzer.ContextAware;
import com.sabre.ix.client.context.Context;

/**
 * Creator: Henrik Thorburn (sg0211570)
 * Date:    2014-11 05
 * Copyright (C) Sabre Inc
 */
public class BookingDemoAnalyzer implements Analyzer<Booking>, ContextAware {

    private Context context;

    @Override
    public void analyze(Booking dataObject) {

    }

    @Override
    public void setContext(Context context) {
        this.context = context;
    }
}
