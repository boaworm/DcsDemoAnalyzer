package com.sabre.ix.demo;

import com.sabre.ix.client.Booking;
import com.sabre.ix.client.BookingServices;
import com.sabre.ix.client.context.Context;
import com.sabre.ix.client.dao.MetaModel;
import com.sabre.ix.client.datahandler.DataHandler;
import com.sabre.ix.client.services.MetaModelServices;
import com.sabre.ix.client.spi.ActionHandler;
import org.dom4j.DocumentException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * Creator: Henrik Thorburn (sg0211570)
 * Date:    2015-01-08
 * Copyright (C) Sabre Inc
 */

@RunWith(MockitoJUnitRunner.class)
public class MissingVowelInNameAnalyzerTest {

    private MissingVowelInNameAnalyzer analyzer;

    @Before
    public void setUp() throws IOException, DocumentException {
        analyzer = new MissingVowelInNameAnalyzer();
    }

    @After
    public void tearDown() {
        analyzer = null;
    }


    @Test
    public void verifyVowelAnalysis() {
        assertThat(analyzer.stringContainsVowel("JOE"), equalTo(true));
        assertThat(analyzer.stringContainsVowel("HENRIK"), equalTo(true));
        assertThat(analyzer.stringContainsVowel("A"), equalTo(true));
        assertThat(analyzer.stringContainsVowel("AL"), equalTo(true));
        assertThat(analyzer.stringContainsVowel("LA"), equalTo(true));
        assertThat(analyzer.stringContainsVowel("NG"), equalTo(false));
    }

}
