package com.sabre.ix.demo;

import com.sabre.ix.client.Booking;
import com.sabre.ix.client.BookingName;
import com.sabre.ix.client.analyzer.Analyzer;
import com.sabre.ix.client.analyzer.ContextAware;
import com.sabre.ix.client.context.Context;
import org.apache.log4j.Logger;

/**
 * Creator: Henrik Thorburn (sg0211570)
 * Date:    2015-01-08
 * Copyright (C) Sabre Inc
 */
public class MissingVowelInNameAnalyzer implements Analyzer<Booking> {
    private static final Logger log = Logger.getLogger(MissingVowelInNameAnalyzer.class);
    public static final String MISSING_VOWEL_IN_NAME = "MissingVowelInName";

    @Override
    public void analyze(Booking booking) {

        // Only do this check for new bookings
        if (booking.getPreviousVersion() == null) {
            log.debug("First time we see booking [" + booking + "], checking for names without vowels");
            for (BookingName name : booking.getBookingNames()) {
                String firstName = name.getFirstName();
                String lastName = name.getLastName();

                if (stringContainsVowel(firstName) && stringContainsVowel(lastName)) {
                    // There is at least one vowel in the first and last name
                } else {
                    booking.setTransientAttribute(MISSING_VOWEL_IN_NAME, true);
                    return;
                }
            }
        } else {
            log.debug("Ignoring booking [" + booking + "] as we  have already processed it before");
        }

        // All names are OK
        booking.setTransientAttribute("MissingVowelInName", false);
    }

    public boolean stringContainsVowel(String s) {
        return s.matches(".*[aeiouAEIOU].*");
    }

}
