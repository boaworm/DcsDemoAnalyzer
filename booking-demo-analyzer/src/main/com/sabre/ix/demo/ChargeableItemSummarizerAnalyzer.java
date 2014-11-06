package com.sabre.ix.demo;

import com.sabre.ix.client.Booking;
import com.sabre.ix.client.BookingName;
import com.sabre.ix.client.ChargeableItem;
import com.sabre.ix.client.analyzer.Analyzer;
import com.sabre.ix.client.analyzer.ContextAware;
import com.sabre.ix.client.analyzer.LifecycleAware;
import com.sabre.ix.client.context.Context;
import com.sabre.ix.client.dao.*;
import com.sabre.ix.client.services.MetaModelServices;

import java.util.List;

/**
 * Creator: Henrik Thorburn (sg0211570)
 * Date:    2014-11 05
 * Copyright (C) Sabre Inc
 * <p/>
 * This analyzer will sum up the chargeable  items and set the total count
 * as a dynamic attribute.
 */
public class ChargeableItemSummarizerAnalyzer implements Analyzer<Booking>, ContextAware, LifecycleAware {

    public static final String CHARGEABLE_SUM = "ChargeableSum";
    public static final String POINT_EQ = "PointEquivalence";
    private Context context;

    @Override
    public void analyze(Booking booking) {

        double sum = 0.0;

        for (ChargeableItem ci : booking.getChargeableItems()) {
            sum += ci.getBaseFareAmount();
        }

        for (BookingName name : booking.getBookingNames()) {
            double nameSum = 0.0;
            for (ChargeableItem ci : name.getChargeableItems()) {
                nameSum += ci.getBaseFareAmount();
            }
            sum += nameSum;

            double points;
            ReferenceTable pointAssignment = context.getReferenceTableServices().getReferenceTable("PointAssignment");
            List<ReferenceTableRow> carrierPointData = pointAssignment.getRowsByColumnValuePair("CarrierCode", name.getFqtProgram());
            if(carrierPointData.isEmpty()) {
                // Fall back to default point association
                points = 50 * nameSum;
            } else {
                // Use Carrier-specific point assignment
                points = Double.parseDouble(carrierPointData.get(0).getColumnData("PointMultiplier")) * nameSum;
            }
            name.setDynamicAttribute(POINT_EQ, points);
        }

        booking.setDynamicAttribute(CHARGEABLE_SUM, Double.toString(sum));
    }

    @Override
    public void initialize() {
        // Ensure the attribute is available in this IX instance
        MetaModelServices metaModelServices = context.getMetaModelServices();
        MetaModel metaModel = metaModelServices.getMetaModel();
        Element bookingElement = metaModel.getElementByName("Booking");
        Entity bookingEntity = bookingElement.getEntityByName("Booking");
        Attribute attributeByName = bookingEntity.getAttributeByName(CHARGEABLE_SUM);
        if(attributeByName == null ){
            throw new RuntimeException("AnalyzerAttribute " + CHARGEABLE_SUM + " does not exist!");
        }
    }

    @Override
    public void destroy() {
        // Nothing to do
    }

    @Override
    public void setContext(Context context) {
        this.context = context;
    }
}
