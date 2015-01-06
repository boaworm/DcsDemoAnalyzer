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
public class SteakAnalyzer implements Analyzer<Booking>, ContextAware {
    private Context context;

    private static final Logger log = Logger.getLogger(SteakAnalyzer.class);
    public static final String ASSIGNED_STEAK = "SteakType";

    @Override
    public void analyze(Booking booking) {
        log.debug("Preparing to analyze booking: " + booking.getRloc());

        // Clear. Probably need to extend DataObject to do this in a different way
        for (BookingName name : booking.getBookingNames()) {
            name.setDynamicAttribute(ASSIGNED_STEAK, null);
        }

        ReferenceTable steakTable = context.getReferenceTableServices().getReferenceTable("SteakAssignment");
        Short sweetSteakThreshold = context.getParameterServices().getOptionalParameter("SweetSteakThreshold", (short) 2);
        int SteakCount = 0;
        if (steakTable == null) {
            log.warn("Failed to find Steak reference table");
        } else {
            for (BookingName name : booking.getBookingNames()) {

                List<ReferenceTableRow> applicableSteaks;
                if (name.getBookingNameItems().size() >= sweetSteakThreshold) {
                    // We want to assign a juicy Steak for people travelling far
                    applicableSteaks = steakTable.getRowsByColumnValuePair("FlavourType", "Juicy");
                } else {
                    // And a chewy Steak for those travelling short
                    applicableSteaks = steakTable.getRowsByColumnValuePair("FlavourType", "Chewy");
                }

                if (applicableSteaks.isEmpty()) {
                    // todo: Define standard way of reporting errors from the analyzers
                    throw new RuntimeException("Could not find applicable Steak!");
                } else {
                    Random random = new Random();
                    int i = random.nextInt(applicableSteaks.size());
                    String steak = applicableSteaks.get(i).getColumnData("SteakName");

                    name.setDynamicAttribute(ASSIGNED_STEAK, steak);
                    name.setTransientAttribute("Transitive" + ASSIGNED_STEAK, steak);
                    ++SteakCount;

                    log.debug("Assigned Steak " + steak + " to bookingName " + name);
                }
            }
        }
        booking.setTransientAttribute("Transitive" + ASSIGNED_STEAK, SteakCount);
    }

    @Override
    public void setContext(Context context) {
        this.context = context;
    }
}
