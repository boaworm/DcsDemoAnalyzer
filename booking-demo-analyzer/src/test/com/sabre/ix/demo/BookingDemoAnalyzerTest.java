package com.sabre.ix.demo;

import com.sabre.ix.client.Booking;
import com.sabre.ix.client.BookingServices;
import com.sabre.ix.client.context.Context;
import com.sabre.ix.client.context.ContextFactory;
import com.sabre.ix.client.dao.DataRow;
import com.sabre.ix.client.services.QueryServices;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Creator: Henrik Thorburn (sg0211570)
 * Date:    2014-11 05
 * Copyright (C) Sabre Inc
 */
@RunWith(MockitoJUnitRunner.class)
public class BookingDemoAnalyzerTest {

    private Context context;
    private BookingDemoAnalyzer analyzer;
    private BookingServices bookingServices;

    @Before
    public void setUp() {
        context = ContextFactory.createContext();
        analyzer = new BookingDemoAnalyzer();
        analyzer.setContext(context);
        bookingServices = (BookingServices) context.getDomainServices(Booking.class);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void verifyBasicLoadingOfBooking() {

        Booking booking = bookingServices.retrieveById(335685);
        assertNotNull(booking);

        // Pass booking into analyzer
        analyzer.analyze(booking);

        // Save any changes back to the ODS
        bookingServices.store(booking);

    }

    @Test
    public void verifySummingUpOfChargeableItems() {

        long bookingId = 335807;

        Booking booking = bookingServices.retrieveById(bookingId);
        Object dynamicAttribute = booking.getDynamicAttribute(ChargeableItemSummarizerAnalyzer.CHARGEABLE_SUM);
        assertNull(dynamicAttribute);

        // now run the analyzer
        ChargeableItemSummarizerAnalyzer ciAnalyzer = new ChargeableItemSummarizerAnalyzer();
        ciAnalyzer.analyze(booking);

        // Assert the attribute has been set
        assertNotNull(booking.getDynamicAttribute(ChargeableItemSummarizerAnalyzer.CHARGEABLE_SUM));

        // Now store it in ODS :
        bookingServices.store(booking);

        // Now load it again, making sure the attribute is now there
        Booking reloadedBooking = bookingServices.retrieveById(bookingId);
        assertNotNull(reloadedBooking.getDynamicAttribute(ChargeableItemSummarizerAnalyzer.CHARGEABLE_SUM));

    }

    @Test
    public void sumUpChargeablesUsingQueryServices() {
        long bookingId = 335807;

        QueryServices queryServices = context.getQueryServices();
        List<DataRow> dataRows = queryServices.retrieveData("SELECT SUM(BaseFareAmount) AS S FROM ChargeableItem WHERE BookingId=" + bookingId, Collections.emptyList());
        assertThat(dataRows.size(), equalTo(1));
        assertThat(dataRows.get(0).getDouble("S"), equalTo(202.0));

    }
}
