package com.sabre.ix.demo;

import com.sabre.ix.client.Booking;
import com.sabre.ix.client.BookingName;
import com.sabre.ix.client.analyzer.Analyzer;
import com.sabre.ix.client.analyzer.ContextAware;
import com.sabre.ix.client.context.Context;
import com.sabre.ix.client.dao.ReferenceTable;
import com.sabre.ix.client.dao.ReferenceTableRow;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Random;

/**
 * Creator: Henrik Thorburn (sg0211570)
 * Date:    2015-01-06
 * Copyright (C) Sabre Inc
 */
public class VegetableAnalyzer implements Analyzer<Booking>, ContextAware {
    private Context context;

    private static final Logger log = Logger.getLogger(VegetableAnalyzer.class);
    public static final String ASSIGNED_VEGETABLE = "VegetableType";

    @Override
    public void analyze(Booking booking) {
        log.debug("Preparing to analyze booking: " + booking.getRloc());

        // Clear. Probably need to extend DataObject to do this in a different way
        for (BookingName name : booking.getBookingNames()) {
            name.setDynamicAttribute(ASSIGNED_VEGETABLE, null);
        }

        ReferenceTable VegetableTable = context.getReferenceTableServices().getReferenceTable("VegetableAssignment");
        Short sweetVegetableThreshold = context.getParameterServices().getOptionalParameter("SweetVegetableThreshold", (short)2);
        int VegetableCount = 0;
        if (VegetableTable == null) {
            log.warn("Failed to find Vegetable reference table");
        } else {
            for (BookingName name : booking.getBookingNames()) {

                List<ReferenceTableRow> applicableVegetables;
                if (name.getBookingNameItems().size() >= sweetVegetableThreshold) {
                    // We want to assign a sweet Vegetable for people travelling far
                    applicableVegetables = VegetableTable.getRowsByColumnValuePair("FlavourType", "Sweet");
                } else {
                    // And a sour Vegetable for those travelling short
                    applicableVegetables = VegetableTable.getRowsByColumnValuePair("FlavourType", "Sour");
                }

                if (applicableVegetables.isEmpty()) {
                    // todo: Define standard way of reporting errors from the analyzers
                    throw new RuntimeException("Could not find applicable Vegetable!");
                } else {
                    Random random = new Random();
                    int i = random.nextInt(applicableVegetables.size());
                    String Vegetable = applicableVegetables.get(i).getColumnData("VegetableName");

                    name.setDynamicAttribute(ASSIGNED_VEGETABLE, Vegetable);
                    name.setTransientAttribute("Transitive" + ASSIGNED_VEGETABLE, Vegetable);
                    ++VegetableCount;

                    log.debug("Assigned Vegetable " + Vegetable + " to bookingName " + name);
                }
            }
        }
        booking.setTransientAttribute("Transitive" + ASSIGNED_VEGETABLE, VegetableCount);
    }

    @Override
    public void setContext(Context context) {
        this.context = context;
    }
}
