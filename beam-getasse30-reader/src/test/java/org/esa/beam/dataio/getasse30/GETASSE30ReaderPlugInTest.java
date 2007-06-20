package org.esa.beam.dataio.getasse30;

import junit.framework.TestCase;
import org.esa.beam.framework.dataio.ProductReader;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;


public class GETASSE30ReaderPlugInTest extends TestCase {

    private GETASSE30ReaderPlugIn _plugIn;

    protected void setUp() throws Exception {
        _plugIn = new GETASSE30ReaderPlugIn();
    }

    protected void tearDown() throws Exception {
        _plugIn = null;
    }

    public void testValidInputs() {
        testValidInput("./GETASSE30/00N015W.getasse30");
        testValidInput("./GETASSE30/00N015W.GETASSE30");
        testValidInput("./GETASSE30/00N015W.GETaSSe30");
    }

    private void testValidInput(final String s) {
        assertTrue(_plugIn.canDecodeInput(s));
        assertTrue(_plugIn.canDecodeInput(new File(s)));
    }

    public void testInvalidInputs() {
        testInvalidInput("10n143w.GETASSE30.zip");
        testInvalidInput("./GETASSE30/00N015W.getasse30.zip");
        testInvalidInput("./GETASSE30/00N015W.GETASSE30.zip");
        testInvalidInput("./GETASSE30/readme.txt");
        testInvalidInput("./GETASSE30/readme.txt.zip");
        testInvalidInput("./GETASSE30/readme");
        testInvalidInput("./GETASSE30/");
        testInvalidInput("./");
        testInvalidInput(".");
        testInvalidInput("");
        testInvalidInput("./GETASSE30/.hgt");
        testInvalidInput("./GETASSE30/.hgt.zip");
        testInvalidInput("./GETASSE30/.hgt");
        testInvalidInput("./GETASSE30/.hgt.zip");
    }

    private void testInvalidInput(final String s) {
        assertFalse(_plugIn.canDecodeInput(s));
        assertFalse(_plugIn.canDecodeInput(new File(s)));
    }

    public void testThatOtherTypesCannotBeDecoded() throws MalformedURLException {
        assertFalse(_plugIn.canDecodeInput(null));
        final URL url = new File("./GETASSE30/readme.txt").toURL();
        assertFalse(_plugIn.canDecodeInput(url));
        final Object object = new Object();
        assertFalse(_plugIn.canDecodeInput(object));
    }

    public void testCreateReaderInstance() {
        final ProductReader reader = _plugIn.createReaderInstance();
        assertTrue(reader instanceof GETASSE30Reader);
    }

    public void testGetInputTypes() {
        final Class[] inputTypes = _plugIn.getInputTypes();
        assertNotNull(inputTypes);
        assertTrue(inputTypes.length == 2);
        assertEquals(String.class, inputTypes[0]);
        assertEquals(File.class, inputTypes[1]);
    }

    public void testGetFormatNames() {
        final String[] formatNames = _plugIn.getFormatNames();
        assertNotNull(formatNames);
        assertTrue(formatNames.length == 1);
        assertEquals("GETASSE30", formatNames[0]);
    }

    public void testGetDefaultFileExtensions() {
        final String[] defaultFileExtensions = _plugIn.getDefaultFileExtensions();
        assertNotNull(defaultFileExtensions);
        assertTrue(defaultFileExtensions.length == 1);
        assertEquals(".getasse30", defaultFileExtensions[0]);
    }

}
