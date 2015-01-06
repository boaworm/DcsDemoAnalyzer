package com.sabre.ix.demo;

import com.calidris.logging.Logger;
import com.sabre.ix.client.Booking;
import com.sabre.ix.client.analyzer.Analyzer;
import com.sabre.ix.client.analyzer.ContextAware;
import com.sabre.ix.client.context.Context;
import com.sabre.ix.client.services.ParameterServices;

import java.util.Random;

/**
 * Creator: Henrik Thorburn (sg0211570)
 * Date:    2015-01-06
 * Copyright (C) Sabre Inc
 */
public class CpuIntensiveAnalyzerBase implements Analyzer<Booking>, ContextAware {
    private static final Logger log = Logger.getLogger(CpuIntensiveAnalyzerBase.class);
    public static final String MINIMUM_CPU_MILLIS = "MinimumCpuMillis";
    public static final String MAXIMUM_CPU_MILLIS = "MaximumCpuMillis";
    private Random random = new Random();
    private Context context;

    @Override
    public void analyze(Booking dataObject) {
        ParameterServices parameterServices = context.getParameterServices();
        Integer minimumCpuMillis = parameterServices.getOptionalParameter(MINIMUM_CPU_MILLIS, 100);
        Integer maximumCpuMillis = parameterServices.getOptionalParameter(MAXIMUM_CPU_MILLIS, 300);
        if (minimumCpuMillis > maximumCpuMillis) {
            log.error("MinimumCpuMillis (" + minimumCpuMillis + ")is greater than MaximumCpuMillis! (" + maximumCpuMillis + "). Please correct configuration!");
            return;
        }

        int cpuMillis = random.nextInt(maximumCpuMillis - minimumCpuMillis + 1) + minimumCpuMillis;
        log.debug("Spending " + cpuMillis + " millis in full CPU usage mode");

        long startTime = System.currentTimeMillis();
        while(System.currentTimeMillis() < startTime + cpuMillis) {
            // Simple tight loop.
        }
        log.debug("Done spending CPU cycles");

    }

    @Override
    public void setContext(Context context) {
        this.context = context;
    }
}
