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
public class FruitAnalyzer implements Analyzer<Booking>, ContextAware {
    private Context context;

    private static final Logger log = Logger.getLogger(FruitAnalyzer.class);
    public static final String ASSIGNED_FRUIT = "FruitType";
    static final String TRANSIENT_ATTRIBUTE_NAME = "OverrideFruitAssignment";

    @Override
    public void analyze(Booking booking) {
        log.debug("Preparing to analyze booking: " + booking.getRloc());

        String overrideFruitAssignment = (String) booking.getTransientAttribute(TRANSIENT_ATTRIBUTE_NAME);
        log.debug("Received override fruit assignment :" + overrideFruitAssignment +":");
        boolean overrideAssignment = false;
        if (overrideFruitAssignment != null && !overrideFruitAssignment.isEmpty()) {
            overrideAssignment = true;
        }

        // Clear. Probably need to extend DataObject to do this in a different way
        for (BookingName name : booking.getBookingNames()) {
            name.setDynamicAttribute(ASSIGNED_FRUIT, null);
        }

        ReferenceTable fruitTable = context.getReferenceTableServices().getReferenceTable("FruitAssignment");
        Short sweetFruitThreshold = context.getParameterServices().getOptionalParameter("SweetFruitThreshold", (short)2);
        int fruitCount = 0;
        if (fruitTable == null) {
            log.warn("Failed to find fruit reference table");
        } else {
            for (BookingName name : booking.getBookingNames()) {

                List<ReferenceTableRow> applicableFruits;
                if (name.getBookingNameItems().size() >= sweetFruitThreshold) {
                    // We want to assign a sweet fruit for people travelling far
                    applicableFruits = fruitTable.getRowsByColumnValuePair("FlavourType", "Sweet");
                } else {
                    // And a sour fruit for those travelling short
                    applicableFruits = fruitTable.getRowsByColumnValuePair("FlavourType", "Sour");
                }

                if (applicableFruits.isEmpty()) {
                    // todo: Define standard way of reporting errors from the analyzers
                    throw new RuntimeException("Could not find applicable fruit!");
                } else {
                    Random random = new Random();
                    int i = random.nextInt(applicableFruits.size());
                    String fruit = applicableFruits.get(i).getColumnData("FruitName");

                    name.setDynamicAttribute(ASSIGNED_FRUIT, fruit);
                    name.setTransientAttribute("Transitive" + ASSIGNED_FRUIT, fruit);
                    ++fruitCount;

                    log.debug("Assigned fruit " + fruit + " to bookingName " + name);
                }

                if (overrideAssignment) {
                    name.setDynamicAttribute(ASSIGNED_FRUIT, overrideFruitAssignment);
                    log.debug("Overriding fruit assignment to " + overrideFruitAssignment);
                }
            }
        }
        booking.setTransientAttribute("Transitive" + ASSIGNED_FRUIT, fruitCount);
    }

    @Override
    public void setContext(Context context) {
        this.context = context;
    }
}
