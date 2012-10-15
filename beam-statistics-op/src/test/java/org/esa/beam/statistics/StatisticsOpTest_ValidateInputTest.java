package org.esa.beam.statistics;

import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.datamodel.ProductData;
import org.esa.beam.framework.gpf.OperatorException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class StatisticsOpTest_ValidateInputTest {

    private StatisticsOp statisticsOp;

    @Before
    public void setUp() throws Exception {
        statisticsOp = new StatisticsOp();
        statisticsOp.startDate = ProductData.UTC.parse("2010-01-31 14:45:23", "yyyy-MM-ss hh:mm:ss");
        statisticsOp.endDate = ProductData.UTC.parse("2010-01-31 14:46:23", "yyyy-MM-ss hh:mm:ss");
        statisticsOp.precision = 0;
        statisticsOp.sourceProducts = new Product[]{TestUtil.getTestProduct()};
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testValidation_PrecisionLessThanMinPrecision() {
        statisticsOp.precision = -1;

        try {
            statisticsOp.validateInput();
            fail();
        } catch (OperatorException expected) {
            assertEquals("Parameter 'precision' must be greater than or equal to 0", expected.getMessage());
        }
    }

    @Test
    public void testValidation_PrecisionGreaterThanMaxPrecision() {
        statisticsOp.precision = 7;

        try {
            statisticsOp.validateInput();
            fail();
        } catch (OperatorException expected) {
            assertEquals("Parameter 'precision' must be less than or equal to 6", expected.getMessage());
        }
    }

    @Test
    public void testStartDateHasToBeBeforeEndDate() throws Exception {
        statisticsOp.startDate = ProductData.UTC.parse("2010-01-31 14:46:23", "yyyy-MM-ss hh:mm:ss");
        statisticsOp.endDate = ProductData.UTC.parse("2010-01-31 14:45:23", "yyyy-MM-ss hh:mm:ss");

        try {
            statisticsOp.validateInput();
            fail();
        } catch (OperatorException expected) {
            assertTrue(expected.getMessage().contains("before start date"));
        }
    }

    @Test
    public void testSourceProductsMustBeGiven() {
        statisticsOp.sourceProducts = null;
        try {
            statisticsOp.validateInput();
            fail();
        } catch (OperatorException expected) {
            assertTrue(expected.getMessage().contains("must be given"));
        }

        statisticsOp.sourceProducts = new Product[0];
        try {
            statisticsOp.validateInput();
            fail();
        } catch (OperatorException expected) {
            assertTrue(expected.getMessage().contains("must be given"));
        }
    }

    @Test
    public void testInvalidBandConfiguration() throws IOException {
        final StatisticsOp.BandConfiguration configuration = new StatisticsOp.BandConfiguration();
        statisticsOp.bandConfigurations = new StatisticsOp.BandConfiguration[]{configuration};

        try {
            statisticsOp.validateInput();
            fail();
        } catch (OperatorException expected) {
            assertTrue(expected.getMessage().contains("must contain either a source band name or an expression"));
        }

        configuration.expression = "algal_2 * PI";
        configuration.sourceBandName = "bandname";
        try {
            statisticsOp.validateInput();
            fail();
        } catch (OperatorException expected) {
            assertTrue(expected.getMessage().contains("must contain either a source band name or an expression"));
        }
    }

    @Test
    public void testValidBandConfiguration() throws IOException {
        final StatisticsOp.BandConfiguration configuration = new StatisticsOp.BandConfiguration();
        statisticsOp.bandConfigurations = new StatisticsOp.BandConfiguration[]{configuration};

        configuration.expression = "algal_2 * PI";
        statisticsOp.validateInput();

        configuration.expression = null;
        configuration.sourceBandName = "bandname";
        statisticsOp.validateInput();
    }
}