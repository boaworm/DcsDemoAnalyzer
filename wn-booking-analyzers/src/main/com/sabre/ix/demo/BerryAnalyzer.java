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
public class BerryAnalyzer implements Analyzer<Booking>, ContextAware {
    private Context context;

    private static final Logger log = Logger.getLogger(BerryAnalyzer.class);
    public static final String ASSIGNED_BERRY = "BerryType";

    @Override
    public void analyze(Booking booking) {
        log.debug("Preparing to analyze booking: " + booking.getRloc());

        // Clear. Probably need to extend DataObject to do this in a different way
        for (BookingName name : booking.getBookingNames()) {
            name.setDynamicAttribute(ASSIGNED_BERRY, null);
        }

        ReferenceTable berryTable = context.getReferenceTableServices().getReferenceTable("BerryAssignment");
        Short sweetBerryThreshold = context.getParameterServices().getOptionalParameter("SweetBerryThreshold", (short) 2);
        int berryCount = 0;
        if (berryTable == null) {
            log.warn("Failed to find Berry reference table");
        } else {
            for (BookingName name : booking.getBookingNames()) {

                List<ReferenceTableRow> applicableBerrys;
                if (name.getBookingNameItems().size() >= sweetBerryThreshold) {
                    // We want to assign a sweet Berry for people travelling far
                    applicableBerrys = berryTable.getRowsByColumnValuePair("FlavourType", "Sweet");
                } else {
                    // And a sour Berry for those travelling short
                    applicableBerrys = berryTable.getRowsByColumnValuePair("FlavourType", "Sour");
                }

                if (applicableBerrys.isEmpty()) {
                    // todo: Define standard way of reporting errors from the analyzers
                    throw new RuntimeException("Could not find applicable Berry!");
                } else {
                    Random random = new Random();
                    int i = random.nextInt(applicableBerrys.size());
                    String berry = applicableBerrys.get(i).getColumnData("BerryName");

                    name.setDynamicAttribute(ASSIGNED_BERRY, berry);
                    name.setTransientAttribute("Transitive" + ASSIGNED_BERRY, berry);
                    ++berryCount;

                    log.debug("Assigned Berry " + berry + " to bookingName " + name);
                }
            }
        }
        booking.setTransientAttribute("Transitive" + ASSIGNED_BERRY, berryCount);
    }

    @Override
    public void setContext(Context context) {
        this.context = context;
    }
}
