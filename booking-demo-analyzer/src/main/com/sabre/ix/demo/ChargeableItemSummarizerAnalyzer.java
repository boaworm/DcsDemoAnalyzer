package com.sabre.ix.demo;

import com.sabre.ix.client.Booking;
import com.sabre.ix.client.BookingName;
import com.sabre.ix.client.ChargeableItem;
import com.sabre.ix.client.analyzer.Analyzer;

/**
 * Creator: Henrik Thorburn (sg0211570)
 * Date:    2014-11 05
 * Copyright (C) Sabre Inc
 * <p/>
 * This analyzer will sum up the chargeable  items and set the total count
 * as a dynamic attribute.
 */
public class ChargeableItemSummarizerAnalyzer implements Analyzer<Booking> {

    public static final String CHARGEABLE_SUM = "ChargeableSum";

    @Override
    public void analyze(Booking booking) {

        double sum = 0.0;

        for (ChargeableItem ci : booking.getChargeableItems()) {
            sum += ci.getBaseFareAmount();
        }

        for (BookingName name : booking.getBookingNames()) {
            for (ChargeableItem ci : name.getChargeableItems()) {
                sum += ci.getBaseFareAmount();
            }
        }

        booking.setDynamicAttribute(CHARGEABLE_SUM, sum);
    }
}
