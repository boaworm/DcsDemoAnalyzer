package com.sabre.ix.demo;

import com.sabre.ix.client.Booking;
import com.sabre.ix.client.BookingServices;
import com.sabre.ix.client.context.Context;
import com.sabre.ix.client.dao.DataRow;
import com.sabre.ix.client.datahandler.DataHandler;
import com.sabre.ix.client.services.*;
import com.sabre.ix.client.spi.Parameter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Creator: Henrik Thorburn (sg0211570)
 * Date:    2015-01-06
 * Copyright (C) Sabre Inc
 */
@RunWith(MockitoJUnitRunner.class)
public class CpuIntensiveAnalyzerBaseTest {

    private CpuIntensiveAnalyzerBase analyzer;

    @Mock
    private Context mockContext;
    @Mock
    private ParameterServices parameterServices;
    @Mock
    private DataHandler mockDataHandler;

    @Before
    public void setUp() {
        parameterServices = new ParameterServicesImpl(mockDataHandler);
        when(mockContext.getParameterServices()).thenReturn(parameterServices);
        when(mockDataHandler.getParameter(anyString(), any(Parameter.class))).thenReturn(null);

        analyzer = new CpuIntensiveAnalyzerBase();
        analyzer.setContext(mockContext);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void verifyCpuWasting() {

        // We should waste between 100 and 300 millis per analyzer invocation
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 100; ++i) {
            analyzer.analyze(null);
        }
        long stopTime = System.currentTimeMillis();
        long elapsedTime = stopTime - startTime;

        assertTrue(elapsedTime > (100 * 100));
        assertTrue(elapsedTime < (100 * 350));
    }
}
