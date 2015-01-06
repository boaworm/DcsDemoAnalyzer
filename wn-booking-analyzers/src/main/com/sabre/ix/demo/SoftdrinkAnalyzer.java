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
public class SoftdrinkAnalyzer implements Analyzer<Booking>, ContextAware {
    private Context context;

    private static final Logger log = Logger.getLogger(SoftdrinkAnalyzer.class);
    public static final String ASSIGNED_SOFTDRINK = "SoftdrinkType";

    @Override
    public void analyze(Booking booking) {
        log.debug("Preparing to analyze booking: " + booking.getRloc());

        // Clear. Probably need to extend DataObject to do this in a different way
        for (BookingName name : booking.getBookingNames()) {
            name.setDynamicAttribute(ASSIGNED_SOFTDRINK, null);
        }

        ReferenceTable SoftdrinkTable = context.getReferenceTableServices().getReferenceTable("SoftdrinkAssignment");
        Short sweetSoftdrinkThreshold = context.getParameterServices().getOptionalParameter("SweetSoftdrinkThreshold", (short) 2);
        int softdrinkCount = 0;
        if (SoftdrinkTable == null) {
            log.warn("Failed to find Softdrink reference table");
        } else {
            for (BookingName name : booking.getBookingNames()) {

                List<ReferenceTableRow> applicableSoftdrinks;
                if (name.getBookingNameItems().size() >= sweetSoftdrinkThreshold) {
                    // We want to assign a sweet Softdrink for people travelling far
                    applicableSoftdrinks = SoftdrinkTable.getRowsByColumnValuePair("FlavourType", "Sweet");
                } else {
                    // And a sour Softdrink for those travelling short
                    applicableSoftdrinks = SoftdrinkTable.getRowsByColumnValuePair("FlavourType", "Sour");
                }

                if (applicableSoftdrinks.isEmpty()) {
                    // todo: Define standard way of reporting errors from the analyzers
                    throw new RuntimeException("Could not find applicable Softdrink!");
                } else {
                    Random random = new Random();
                    int i = random.nextInt(applicableSoftdrinks.size());
                    String softdrink = applicableSoftdrinks.get(i).getColumnData("SoftdrinkName");

                    name.setDynamicAttribute(ASSIGNED_SOFTDRINK, softdrink);
                    name.setTransientAttribute("Transitive" + ASSIGNED_SOFTDRINK, softdrink);
                    ++softdrinkCount;

                    log.debug("Assigned Softdrink " + softdrink + " to bookingName " + name);
                }
            }
        }
        booking.setTransientAttribute("Transitive" + ASSIGNED_SOFTDRINK, softdrinkCount);
    }

    @Override
    public void setContext(Context context) {
        this.context = context;
    }
}
