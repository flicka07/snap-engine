/*
 * $Id: ModisBandReader.java,v 1.3 2007/03/19 15:52:28 marcop Exp $
 *
 * Copyright (C) 2002 by Brockmann Consult (info@brockmann-consult.de)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation. This program is distributed in the hope it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *//*
 * $Id: ModisBandReader.java,v 1.3 2007/03/19 15:52:28 marcop Exp $
 *
 * Copyright (C) 2002 by Brockmann Consult (info@brockmann-consult.de)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation. This program is distributed in the hope it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.esa.beam.dataio.modis.bandreader;

import com.bc.ceres.core.ProgressMonitor;
import ncsa.hdf.hdflib.HDFConstants;
import ncsa.hdf.hdflib.HDFException;
import ncsa.hdf.hdflib.HDFLibrary;
import org.esa.beam.dataio.modis.ModisConstants;
import org.esa.beam.framework.dataio.ProductIOException;
import org.esa.beam.framework.datamodel.ProductData;
import org.esa.beam.util.math.Range;

abstract public class ModisBandReader {

    public static final int SCALE_UNKNOWN = 0;
    public static final int SCALE_LINEAR = 1;
    public static final int SCALE_EXPONENTIAL = 2;
    public static final int SCALE_POW_10 = 3;
    public static final int SCALE_SLOPE_INTERCEPT = 4;

    protected int _sdsId;
    protected int _layer;
    protected float _scale;
    protected float _offset;
    private String _name;
    protected int[] _start;
    protected int[] _stride;
    protected int[] _count;
    protected int _xCoord;
    protected int _yCoord;
    protected Range _validRange;
    protected double _fillValue;

    /**
     * Creates a band reader with given scientific dataset identifier
     *
     * @param sdsId
     */
    public ModisBandReader(final int sdsId, final int layer, final boolean is3d) {
        _sdsId = sdsId;
        _layer = layer;
        _count = new int[3];
        _stride = new int[3];
        _start = new int[3];

        _stride[0] = _stride[1] = _stride[2] = 1;
        _start[0] = _layer;
        _count[0] = 1;

        if (is3d) {
            _xCoord = 2;
            _yCoord = 1;
        } else {
            _xCoord = 1;
            _yCoord = 0;
        }
    }

    /**
     * Closes the band reader.
     *
     * @throws ncsa.hdf.hdflib.HDFException
     */
    public void close() throws HDFException {
        HDFLibrary.SDendaccess(_sdsId);
        _sdsId = HDFConstants.FAIL;
    }

    /**
     * Sets the name of the band
     *
     * @param name
     */
    public void setName(final String name) {
        _name = name;
    }

    /**
     * Retrieves the name of the band
     *
     * @return name
     */
    public String getName() {
        return _name;
    }

    /**
     * Converts a string to a scaling method enum value.
     *
     * @param scaleMethod
     *
     * @return the enum value either {@link #SCALE_LINEAR}, {@link #SCALE_EXPONENTIAL},
     *         {@link #SCALE_POW_10}, {@link #SCALE_SLOPE_INTERCEPT} or {@link #SCALE_UNKNOWN} if the given string
     *         can not be converted.
     */
    public static int decodeScalingMethod(final String scaleMethod) {
        if (scaleMethod != null) {
            if (scaleMethod.equalsIgnoreCase(ModisConstants.LINEAR_SCALE_NAME)) {
                return SCALE_LINEAR;
            } else if (scaleMethod.equalsIgnoreCase(ModisConstants.EXPONENTIAL_SCALE_NAME)) {
                return SCALE_EXPONENTIAL;
            } else if (scaleMethod.equalsIgnoreCase(ModisConstants.POW_10_SCALE_NAME)) {
                return SCALE_POW_10;
            } else if (scaleMethod.equalsIgnoreCase(ModisConstants.SLOPE_INTERCEPT_SCALE_NAME)) {
                return SCALE_SLOPE_INTERCEPT;
            }
        }
        return SCALE_UNKNOWN;
    }

    /**
     * Sets scale and offset for scaled bands (scaling that cannot be handled by the product io framework)
     *
     * @param scale
     * @param offset
     */
    public void setScaleAndOffset(final float scale, final float offset) {
        _scale = scale;
        _offset = offset;
    }

    /**
     * Sets the valid range for this specific band reader, value in unscaled counts
     *
     * @param validRange
     */
    public void setValidRange(Range validRange) {
        _validRange = validRange;
    }

    /**
     * Sets the fill value, i.e. the value set where the measurement data is out-of-scope
     * value in unscaled measurements counts
     *
     * @param fillValue
     */
    public void setFillValue(double fillValue) {
        _fillValue = fillValue;
    }

    /**
     * Retrieves the data type of the band
     *
     * @return the data type
     */
    abstract public int getDataType();

    /**
     * <p>The destination band, buffer and region parameters are exactly the ones passed to the original  call. Since
     * the <code>destOffsetX</code> and <code>destOffsetY</code> parameters are already taken into acount in the
     * <code>sourceOffsetX</code> and <code>sourceOffsetY</code> parameters, an implementor of this method is free to
     * ignore them.
     *
     * @param sourceOffsetX the absolute X-offset in source raster co-ordinates
     * @param sourceOffsetY the absolute Y-offset in source raster co-ordinates
     * @param sourceWidth   the width of region providing samples to be read given in source raster co-ordinates
     * @param sourceHeight  the height of region providing samples to be read given in source raster co-ordinates
     * @param sourceStepX   the sub-sampling in X direction within the region providing samples to be read
     * @param sourceStepY   the sub-sampling in Y direction within the region providing samples to be read
     * @param destOffsetX   the X-offset in the band's raster co-ordinates
     * @param destOffsetY   the Y-offset in the band's raster co-ordinates
     * @param destWidth     the width of region to be read given in the band's raster co-ordinates
     * @param destHeight    the height of region to be read given in the band's raster co-ordinates
     * @param destBuffer    the destination buffer which receives the sample values to be read
     *
     * @deprecated use {@link #readBandData(int, int, int, int, int, int, int, int, int, int, org.esa.beam.framework.datamodel.ProductData, com.bc.ceres.core.ProgressMonitor)} instead
     */
    public void readBandData(int sourceOffsetX, int sourceOffsetY, int sourceWidth,
                                      int sourceHeight, int sourceStepX, int sourceStepY,
                                      int destOffsetX, int destOffsetY, int destWidth,
                                      int destHeight, ProductData destBuffer) throws HDFException,
                                                                                     ProductIOException {
        readBandData(sourceOffsetX, sourceOffsetY,
                     sourceWidth, sourceHeight,
                     sourceStepX, sourceStepY,
                     destOffsetX, destOffsetY,
                     destWidth, destHeight,
                     destBuffer,
                     ProgressMonitor.NULL);
    }

    /**
     * <p>The destination band, buffer and region parameters are exactly the ones passed to the original  call. Since
     * the <code>destOffsetX</code> and <code>destOffsetY</code> parameters are already taken into acount in the
     * <code>sourceOffsetX</code> and <code>sourceOffsetY</code> parameters, an implementor of this method is free to
     * ignore them.
     *
     * @param sourceOffsetX the absolute X-offset in source raster co-ordinates
     * @param sourceOffsetY the absolute Y-offset in source raster co-ordinates
     * @param sourceWidth   the width of region providing samples to be read given in source raster co-ordinates
     * @param sourceHeight  the height of region providing samples to be read given in source raster co-ordinates
     * @param sourceStepX   the sub-sampling in X direction within the region providing samples to be read
     * @param sourceStepY   the sub-sampling in Y direction within the region providing samples to be read
     * @param destOffsetX   the X-offset in the band's raster co-ordinates
     * @param destOffsetY   the Y-offset in the band's raster co-ordinates
     * @param destWidth     the width of region to be read given in the band's raster co-ordinates
     * @param destHeight    the height of region to be read given in the band's raster co-ordinates
     * @param destBuffer    the destination buffer which receives the sample values to be read
     * @param pm            a monitor to inform the user about progress
     */
    abstract public void readBandData(int sourceOffsetX, int sourceOffsetY, int sourceWidth,
                                      int sourceHeight, int sourceStepX, int sourceStepY,
                                      int destOffsetX, int destOffsetY, int destWidth,
                                      int destHeight, ProductData destBuffer,
                                      ProgressMonitor pm) throws HDFException,
                                                                 ProductIOException;
}
