package com.sabre.ix.demo;

import com.sabre.ix.client.*;
import com.sabre.ix.client.context.Context;
import com.sabre.ix.client.context.ContextFactory;
import com.sabre.ix.client.dao.DataRow;
import com.sabre.ix.client.dao.MetaModel;
import com.sabre.ix.client.datahandler.DataHandler;
import com.sabre.ix.client.datahandler.MetaModelFactory;
import com.sabre.ix.client.services.MetaModelServices;
import com.sabre.ix.client.services.QueryServices;
import com.sabre.ix.client.spi.ActionHandler;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Collections;
import java.util.List;

import static junit.framework.TestCase.assertNotNull;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DcsDemoAnalyzerTest {

    private static final Logger log = Logger.getLogger(DcsDemoAnalyzerTest.class);

    DcsDemoAnalyzer analyzer;

    BookingServices bookingServices;
    DcsFlightLegServices dcsFlightLegServices;
    DcsBookingLinkServices linkServices;

    @Mock
    Context mockContext;
    @Mock
    DataHandler mockDataHandler;
    @Mock
    ActionHandler mockActionHandler;
    @Mock
    MetaModelServices mockMetaModelServices;
    MetaModel mockMetaModel;

    Booking booking;
    DcsFlightLeg dcsFlightLeg;

    private String testdataPath = "will be autodiscovered during setup";


    @Before
    public void setUp() throws IOException, DocumentException {
        analyzer = new DcsDemoAnalyzer();

        String hostName = InetAddress.getLocalHost().getHostName();
        if (hostName.contains("htlinux")) {
            testdataPath = "/home/henrik/src/DcsDemoAnalyzer/testdata/";
        } else if (hostName.contains("montecito")) {
            testdataPath = "/Volumes/2TB/src/DcsDemoAnalyzer/testdata/";
        } else if (hostName.contains("H9470305477081")) {
            testdataPath = "C:\\src\\DcsDemoAnalyzer\\testdata\\";
        } else {
            testdataPath = "C:\\dev\\DcsDemoAnalyzer\\testdata\\";
        }

        mockMetaModel = prepareMetaModel(DocumentHelper.parseText(loadTestData("metamodel.xml")));
        when(mockContext.getMetaModelServices()).thenReturn(mockMetaModelServices);
        when(mockMetaModelServices.getMetaModel()).thenReturn(mockMetaModel);
        bookingServices = new BookingServices(mockContext, mockDataHandler, mockActionHandler);
        dcsFlightLegServices = new DcsFlightLegServices(mockContext, mockDataHandler, mockActionHandler);
        linkServices = new DcsBookingLinkServices(mockContext, mockDataHandler, mockActionHandler);
        when(mockContext.getDomainServices(Booking.class)).thenReturn(bookingServices);
        when(mockContext.getDomainServices(DcsBookingLink.class)).thenReturn(linkServices);

        booking = null;
    }

    @After
    public void tearDown() {
        analyzer = null;
    }

    @Test
    @Ignore
    public void verifyBasicProcessingOfDcsFlightLeg() throws IOException {
        String dcsXml = loadTestData("dcs_AB_6436_2014_07_01.xml");
        dcsFlightLeg = new DcsFlightLeg(dcsFlightLegServices, dcsXml);

        assertNotNull(dcsFlightLeg);

        for(DcsPax dcsPax : dcsFlightLeg.getDcsPaxs()) {
            log.debug("Processing pax: " + dcsPax);
            for(DcsPaxSegment segment : dcsPax.getDcsPaxSegments()) {
                log.debug("\tProcessing segment: " + segment);
                List<DcsBookingLink> linkedDataObjects = segment.getLinkedDataObjects(DcsBookingLink.class);

            }
        }
    }

    @Test
    public void demoLivePullingOfDcsFlightLeg() {
        Context context = ContextFactory.createContext();
        DcsFlightLegServices services = (DcsFlightLegServices) context.getDomainServices(DcsFlightLeg.class);
        assertNotNull(services);

        List<DcsFlightLeg> dcsFlightLegs = services.retrieveByCCL("DcsFlightLeg.Origin=\"ATL\"");
        assertThat(dcsFlightLegs.size(), equalTo(2));

        QueryServices queryServices = context.getQueryServices();
        List<DataRow> dataRows = queryServices.retrieveData("SELECT count(*) FROM booking WHERE bookingStatus=1", Collections.emptyList());
        assertThat(dataRows.size(), equalTo(1));


        //DcsDemoAnalyzer anayzer = new DcsDemoAnalyzer();
        //anayzer.analyze(dcsFlightLegs.get(0));

    }


    // Test setup .. No tests below this line
    @SuppressWarnings("unchecked")
    private MetaModel prepareMetaModel(Document metaModel) {
        org.dom4j.Element root = metaModel.getRootElement();
        org.dom4j.Element body = root.element("Body");
        org.dom4j.Element response = body.element("getMetaModelFullRequestResponse");
        return MetaModelFactory.create(response);
    }

    private String loadTestData(String fileName) throws IOException {
        String s = readFile(fileName);
        if (s != null) {
            return s;
        } else {
            // Attempting to read file from IX system
            if(fileName.startsWith("dcs")) {
                Context liveContext = ContextFactory.createContext();
                DcsFlightLegServices liveDcsFlightLegServices = (DcsFlightLegServices) liveContext.getDomainServices(DcsFlightLeg.class);
                String[] arr = fileName.split("_");
                String operatingCarrier = arr[1];
                String operatingFlightNumber = arr[2];
                String scheduledDepartureYear = arr[3];
                String scheduledDepartureMonth = arr[4];
                String scheduledDepartureDay = arr[5];

                List<DcsFlightLeg> dcsFlightLegs = liveDcsFlightLegServices.retrieveByCCL("DcsFlightLeg[OperatingCarrier=" + operatingCarrier +
                        " AND OperatingFlightNumber=" + operatingFlightNumber +
                        " AND ScheduledDepartureDate=" + scheduledDepartureYear + "-" + scheduledDepartureMonth + "-" + scheduledDepartureDay + "T00:00:00");
                assertThat("Expected a unique match when looking up dcs flight leg", dcsFlightLegs.size(), equalTo(1));
                DcsFlightLeg leg = dcsFlightLegs.get(0);
                String xmlString = leg.toXml();
                writeFile("dcs_" + operatingCarrier + "_" + operatingFlightNumber + "_" + scheduledDepartureYear + "_" + scheduledDepartureMonth + "_" + scheduledDepartureDay + ".xml", xmlString);
                return xmlString;

            } else {
                // Assume booking:
                String rloc = fileName.substring(0, 6);
                try {
                    Context liveContext = ContextFactory.createContext();
                    BookingServices liveBookingServices = (BookingServices) liveContext.getDomainServices(Booking.class);
                    List<Booking> bookings = liveBookingServices.retrieveByCCL("Booking.Rloc=\"" + rloc + "\"");
                    assertThat("Expected exactly one booking when querying by CCL and RLOC=" + rloc, bookings.size(), equalTo(1));
                    Booking liveBooking = bookings.get(0);
                    String xmlString = liveBooking.toXml();
                    writeFile(rloc, xmlString);
                    return xmlString;
                } catch (Exception e) {
                    throw new RuntimeException("Failed to load RLOC from live system: " + rloc, e);
                }
            }
        }
    }

    private String readFile(String fileName) throws IOException {
        File testFile = new File(testdataPath + fileName);
        if (testFile.exists() && testFile.canRead()) {
            return FileUtils.readFileToString(testFile);
        } else {
            return null;
        }
    }

    private void writeFile(String rloc, String content) throws IOException {
        File testFile = new File(testdataPath + rloc + ".xml");
        FileUtils.writeStringToFile(testFile, content);
    }

}
