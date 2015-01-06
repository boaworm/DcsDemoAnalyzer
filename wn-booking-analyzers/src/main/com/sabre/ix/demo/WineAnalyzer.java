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
public class WineAnalyzer implements Analyzer<Booking>, ContextAware {
    private Context context;

    private static final Logger log = Logger.getLogger(WineAnalyzer.class);
    public static final String ASSIGNED_WINE = "WineType";

    @Override
    public void analyze(Booking booking) {
        log.debug("Preparing to analyze booking: " + booking.getRloc());

        // Clear. Probably need to extend DataObject to do this in a different way
        for (BookingName name : booking.getBookingNames()) {
            name.setDynamicAttribute(ASSIGNED_WINE, null);
        }

        ReferenceTable wineTable = context.getReferenceTableServices().getReferenceTable("WineAssignment");
        Short sweetWineThreshold = context.getParameterServices().getOptionalParameter("SweetWineThreshold", (short) 2);
        int wineCount = 0;
        if (wineTable == null) {
            log.warn("Failed to find Wine reference table");
        } else {
            for (BookingName name : booking.getBookingNames()) {

                List<ReferenceTableRow> applicableWines;
                switch (name.getBookingNameItems().size()) {
                    case 0:
                    case 1:
                        // Zero or One segment, a red wine
                        applicableWines = wineTable.getRowsByColumnValuePair("FlavourType", "Red");
                        break;
                    case 2:
                        // Two segments, a white wine
                        applicableWines = wineTable.getRowsByColumnValuePair("FlavourType", "White");
                        break;
                    case 3:
                        // Three segments, rose wine
                        applicableWines = wineTable.getRowsByColumnValuePair("FlavourType", "Rose");
                        break;
                    default:
                        // Else (4+ segments, sparkling)
                        applicableWines = wineTable.getRowsByColumnValuePair("FlavourType", "Sparkling");
                        break;
                }

                if (applicableWines.isEmpty()) {
                    // todo: Define standard way of reporting errors from the analyzers
                    throw new RuntimeException("Could not find applicable Wine!");
                } else {
                    Random random = new Random();
                    int i = random.nextInt(applicableWines.size());
                    String wine = applicableWines.get(i).getColumnData("WineName");

                    name.setDynamicAttribute(ASSIGNED_WINE, wine);
                    name.setTransientAttribute("Transitive" + ASSIGNED_WINE, wine);
                    ++wineCount;

                    log.debug("Assigned Wine " + wine + " to bookingName " + name);
                }
            }
        }
        booking.setTransientAttribute("Transitive" + ASSIGNED_WINE, wineCount);
    }

    @Override
    public void setContext(Context context) {
        this.context = context;
    }
}
