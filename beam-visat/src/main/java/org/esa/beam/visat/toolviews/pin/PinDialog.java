/*
 * $Id: PinDialog.java,v 1.1 2007/04/19 10:41:38 norman Exp $
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
package org.esa.beam.visat.toolviews.pin;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.Paint;
import java.awt.Window;
import java.awt.geom.Rectangle2D;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.text.JTextComponent;

import org.esa.beam.framework.datamodel.GeoCoding;
import org.esa.beam.framework.datamodel.GeoPos;
import org.esa.beam.framework.datamodel.Pin;
import org.esa.beam.framework.datamodel.PinSymbol;
import org.esa.beam.framework.datamodel.PixelPos;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.param.ParamChangeEvent;
import org.esa.beam.framework.param.ParamChangeListener;
import org.esa.beam.framework.param.Parameter;
import org.esa.beam.framework.ui.GridBagUtils;
import org.esa.beam.framework.ui.ModalDialog;
import org.esa.beam.util.ProductUtils;
import org.esa.beam.util.math.MathUtils;

/**
 * A dialog used to create new pins or edit existing pins.
 */
public class PinDialog extends ModalDialog {

    private final Product _selectedProduct;
    private Parameter _paramName;
    private Parameter _paramLabel;
    private Parameter _paramUsePixelPos;
    private Parameter _paramLat;
    private Parameter _paramLon;
    private Parameter _paramPixelX;
    private Parameter _paramPixelY;
    private Parameter _paramDescription;
    private PinSymbol _symbol;
    private JLabel _symbolLabel;
    private Parameter _paramColorOutline;
    private Parameter _paramColorFill;


    public PinDialog(final Window parent, final Product product) {
        super(parent, "New Pin", ModalDialog.ID_OK_CANCEL, null); /*I18N*/

        _selectedProduct = product;
        initParameter();
        creatUI();
    }

    protected void onOK() {
        if (!_selectedProduct.containsPixel(getPixelX(), getPixelY())) {
            showInformationDialog("The pin cannot be set because\n" +
                                  "its pixel coordinate is out of bounds."); /*I18N*/
            return;
        }
        if (Pin.isValidNodeName(getName())) {
            super.onOK();
        } else {
            showInformationDialog("'" + getName() + "' is not a valid pin name."); /*I18N*/
        }
    }

    public String getName() {
        return _paramName.getValueAsText();
    }

    public void setName(String name) {
        _paramName.setValueAsText(name, null);
    }

    public String getLabel() {
        return _paramLabel.getValueAsText();
    }

    public void setLabel(String label) {
        _paramLabel.setValueAsText(label, null);
    }

    public boolean isUsePixelPos() {
        return ((Boolean) _paramUsePixelPos.getValue()).booleanValue();
    }

    /**
     * Sets whether or not to use the pixel co-ordinates instead of geographic co-ordinates. Has no effect if the
     * current product is null.
     *
     * @param usePixelPos whether or not to use the pixel co-ordinates instead of geographic co-ordinates
     */
    public void setUsePixelPos(boolean usePixelPos) {
        _paramUsePixelPos.setValue(new Boolean(usePixelPos && _selectedProduct != null), null);
    }

    public float getLat() {
        return ((Float) _paramLat.getValue()).floatValue();
    }

    public void setLat(float lat) {
        _paramLat.setValue(new Float(lat), null);
    }

    public float getLon() {
        return ((Float) _paramLon.getValue()).floatValue();
    }

    public void setLon(float lon) {
        _paramLon.setValue(new Float(lon), null);
    }

    public float getPixelX() {
        return ((Integer) _paramPixelX.getValue()).floatValue() + 0.5f;
    }

    public void setPixelX(int pixelX) {
        _paramPixelX.setValue(new Integer(pixelX), null);
    }

    public float getPixelY() {
        return ((Integer) _paramPixelY.getValue()).floatValue() + 0.5f;
    }

    public void setPixelY(int pixelY) {
        _paramPixelY.setValue(new Integer(pixelY), null);
    }

    public String getDescription() {
        return _paramDescription.getValueAsText();
    }

    public void setDescription(String description) {
        _paramDescription.setValueAsText(description, null);
    }

    public PinSymbol getPinSymbol() {
        return _symbol;
    }

    public void setPinSymbol(PinSymbol symbol) {
        Color fillColor = (Color) symbol.getFillPaint();
        Color outlineColor = (Color) symbol.getOutlinePaint();
        _paramColorFill.setValue(fillColor, null);
        _paramColorOutline.setValue(outlineColor, null);
        _symbol = symbol;
    }

    private void initParameter() {
        boolean isGeocodetProduct = _selectedProduct != null && _selectedProduct.getGeoCoding() != null;

        _paramName = new Parameter("paramName", "");
        _paramName.getProperties().setLabel("Name");/*I18N*/

        _paramLabel = new Parameter("paramLabel", "");
        _paramLabel.getProperties().setLabel("Label");/*I18N*/

        _paramUsePixelPos = new Parameter("paramUsePixelPos", new Boolean(false));
        _paramUsePixelPos.getProperties().setLabel("Use pixel position");/*I18N*/
        _paramUsePixelPos.setUIEnabled(isGeocodetProduct);
        _paramUsePixelPos.addParamChangeListener(new ParamChangeListener() {
            public void parameterValueChanged(ParamChangeEvent event) {
                boolean value = isUsePixelPos();
                _paramLat.setUIEnabled(!value);
                _paramLon.setUIEnabled(!value);
                _paramPixelX.setUIEnabled(value);
                _paramPixelY.setUIEnabled(value);
            }
        });

        ParamChangeListener geoChangeListener = new ParamChangeListener() {
            public void parameterValueChanged(ParamChangeEvent event) {
                updatePixelValues();
            }
        };

        _paramLat = new Parameter("paramLat", new Float(0));
        _paramLat.getProperties().setLabel("Lat");/*I18N*/
        _paramLat.getProperties().setPhysicalUnit("deg"); /*I18N*/
        _paramLat.addParamChangeListener(geoChangeListener);

        _paramLon = new Parameter("paramLon", new Float(0));
        _paramLon.getProperties().setLabel("Lon");/*I18N*/
        _paramLon.getProperties().setPhysicalUnit("deg");/*I18N*/
        _paramLon.addParamChangeListener(geoChangeListener);

        ParamChangeListener pixelChangeListener = new ParamChangeListener() {
            public void parameterValueChanged(ParamChangeEvent event) {
                updateGeoValues();
            }
        };

        _paramPixelX = new Parameter("paramPixelX", new Integer(0));
        _paramPixelX.getProperties().setLabel("Pixel X");
        _paramPixelX.setUIEnabled(isGeocodetProduct);
        _paramPixelX.addParamChangeListener(pixelChangeListener);

        _paramPixelY = new Parameter("paramPixelY", new Integer(0));
        _paramPixelY.getProperties().setLabel("Pixel Y");
        _paramPixelY.setUIEnabled(isGeocodetProduct);
        _paramPixelY.addParamChangeListener(pixelChangeListener);

        _paramDescription = new Parameter("paramDesc", "");
        _paramDescription.getProperties().setLabel("Description"); /*I18N*/
        _paramDescription.getProperties().setNumRows(3);

        if (_symbol == null) {
            _symbol = PinSymbol.createDefaultPinSymbol();
        }

        ParamChangeListener colorChangelistener = new ParamChangeListener() {
            public void parameterValueChanged(ParamChangeEvent event) {
                if (_symbol != null) {
                    _symbol.setFillPaint((Paint) _paramColorFill.getValue());
                    _symbol.setOutlinePaint((Paint) _paramColorOutline.getValue());
                }
                _symbolLabel.repaint();
            }
        };

        _paramColorOutline = new Parameter("outlineColor", _symbol.getOutlinePaint());
        _paramColorOutline.getProperties().setLabel("Outline color");
        _paramColorOutline.addParamChangeListener(colorChangelistener);

        _paramColorFill = new Parameter("fillColor", _symbol.getFillPaint());
        _paramColorFill.getProperties().setLabel("Fill color");
        _paramColorFill.addParamChangeListener(colorChangelistener);
    }

    private void creatUI() {

        _symbolLabel = new JLabel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (g instanceof Graphics2D) {
                    Graphics2D g2d = (Graphics2D) g;
                    final PixelPos refPoint = _symbol.getRefPoint();
                    Rectangle2D bounds = _symbol.getBounds();
                    g2d.scale(1.9, 1.9);
                    double tx = refPoint.getX() - bounds.getX() / 2;
                    double ty = refPoint.getY() - bounds.getY() / 2;
                    g2d.translate(tx, ty);
                    _symbol.draw(g2d);
                    g2d.translate(-tx, -ty);
                    g2d.scale(1 / 1.9, 1 / 1.9);
                }
            }
        };
        _symbolLabel.setPreferredSize(new Dimension(40, 40));

        JPanel dialogPane = GridBagUtils.createPanel();
        final GridBagConstraints gbc = GridBagUtils.createDefaultConstraints();

        gbc.gridy++;
        gbc.gridwidth = 1;
        GridBagUtils.addToPanel(dialogPane, _paramName.getEditor().getLabelComponent(), gbc);
        gbc.gridwidth = 4;
        GridBagUtils.addToPanel(dialogPane, _paramName.getEditor().getComponent(), gbc,
                                "weightx=1, fill=HORIZONTAL");
        gbc.gridy++;
        gbc.gridwidth = 1;
        GridBagUtils.addToPanel(dialogPane, _paramLabel.getEditor().getLabelComponent(), gbc);
        gbc.gridwidth = 4;
        GridBagUtils.addToPanel(dialogPane, _paramLabel.getEditor().getComponent(), gbc,
                                "weightx=1, fill=HORIZONTAL");
        gbc.gridy++;
        gbc.gridwidth = 5;
        GridBagUtils.addToPanel(dialogPane, _paramUsePixelPos.getEditor().getComponent(), gbc);

        final int space = 30;
        gbc.gridy++;
        GridBagUtils.addToPanel(dialogPane, _paramPixelX.getEditor().getLabelComponent(), gbc,
                                "weightx=0, gridwidth=1");
        gbc.insets.right -= 2;
        GridBagUtils.addToPanel(dialogPane, _paramPixelX.getEditor().getComponent(), gbc, "weightx=1");
        gbc.insets.right += 2;
        gbc.insets.left -= 2;
        GridBagUtils.addToPanel(dialogPane, new JLabel(".5"), gbc, "weightx=0");
        gbc.insets.left += 2;
        gbc.insets.left += space;
        GridBagUtils.addToPanel(dialogPane, _paramLon.getEditor().getLabelComponent(), gbc, "weightx=0");
        gbc.insets.left -= space;
        GridBagUtils.addToPanel(dialogPane, _paramLon.getEditor().getComponent(), gbc, "weightx=1");
        GridBagUtils.addToPanel(dialogPane, _paramLon.getEditor().getPhysUnitLabelComponent(), gbc, "weightx=0");

        gbc.gridy++;
        GridBagUtils.addToPanel(dialogPane, _paramPixelY.getEditor().getLabelComponent(), gbc);
        gbc.insets.right -= 2;
        GridBagUtils.addToPanel(dialogPane, _paramPixelY.getEditor().getComponent(), gbc, "weightx=1");
        gbc.insets.right += 2;
        gbc.insets.left -= 2;
        GridBagUtils.addToPanel(dialogPane, new JLabel(".5"), gbc, "weightx=0");
        gbc.insets.left += 2;
        gbc.insets.left += space;
        GridBagUtils.addToPanel(dialogPane, _paramLat.getEditor().getLabelComponent(), gbc, "weightx=0");
        gbc.insets.left -= space;
        GridBagUtils.addToPanel(dialogPane, _paramLat.getEditor().getComponent(), gbc, "weightx=1");
        GridBagUtils.addToPanel(dialogPane, _paramLat.getEditor().getPhysUnitLabelComponent(), gbc, "weightx=0");


        final int symbolSpace = 10;

        gbc.gridy++;
        gbc.insets.top += symbolSpace;
        GridBagUtils.addToPanel(dialogPane, createSymbolPane(), gbc, "fill=NONE, gridwidth=5, weightx=0");

        gbc.gridy++;
        GridBagUtils.addToPanel(dialogPane, _paramDescription.getEditor().getLabelComponent(), gbc, "fill=BOTH");
        gbc.insets.top -= symbolSpace;
        gbc.gridy++;
        GridBagUtils.addToPanel(dialogPane, _paramDescription.getEditor().getComponent(), gbc, "weighty=1");

        setContent(dialogPane);

        final JComponent editorComponent = _paramName.getEditor().getEditorComponent();
        if (editorComponent instanceof JTextComponent) {
            JTextComponent tc = (JTextComponent) editorComponent;
            tc.selectAll();
        }
    }

    private JPanel createSymbolPane() {
        final GridBagConstraints gbc = GridBagUtils.createDefaultConstraints();
        final JPanel symbolPanel = GridBagUtils.createPanel();

        gbc.gridheight = 1;

        gbc.gridy = 0;
        gbc.gridx = 0;
        symbolPanel.add(_paramColorFill.getEditor().getLabelComponent(), gbc);
        gbc.gridx = 1;
        symbolPanel.add(_paramColorFill.getEditor().getComponent(), gbc);

        gbc.gridy = 1;
        gbc.gridx = 0;
        symbolPanel.add(_paramColorOutline.getEditor().getLabelComponent(), gbc);
        gbc.gridx = 1;
        symbolPanel.add(_paramColorOutline.getEditor().getComponent(), gbc);

        gbc.gridy = 0;
        gbc.gridx = 2;
        gbc.gridheight = 2;
        gbc.insets.left = 10;
        symbolPanel.add(_symbolLabel, gbc);
        gbc.insets.left = 0;


        return symbolPanel;
    }

    private void updatePixelValues() {
        if (isUsePixelPos()) {
            return;
        }
        if (!ProductUtils.canGetPixelPos(_selectedProduct)) {
            return;
        }
        GeoCoding geoCoding = _selectedProduct.getGeoCoding();
        GeoPos geoPos = new GeoPos();
        geoPos.lon = ((Float) _paramLon.getValue()).floatValue();
        geoPos.lat = ((Float) _paramLat.getValue()).floatValue();
        PixelPos pixelPos = geoCoding.getPixelPos(geoPos, null);
        _paramPixelX.setValue(new Integer(MathUtils.floorInt(pixelPos.getX())), null);
        _paramPixelY.setValue(new Integer(MathUtils.floorInt(pixelPos.getY())), null);
    }

    private void updateGeoValues() {
        if (!isUsePixelPos()) {
            return;
        }
        if (!ProductUtils.canGetPixelPos(_selectedProduct)) {
            return;
        }
        GeoCoding geoCoding = _selectedProduct.getGeoCoding();
        PixelPos pixelPos = new PixelPos();
        pixelPos.x = ((Integer) _paramPixelX.getValue()).floatValue() + 0.5f;
        pixelPos.y = ((Integer) _paramPixelY.getValue()).floatValue() + 0.5f;
        GeoPos geoPos = geoCoding.getGeoPos(pixelPos, null);
        _paramLon.setValue(new Float(geoPos.getLon()), null);
        _paramLat.setValue(new Float(geoPos.getLat()), null);
    }
}
