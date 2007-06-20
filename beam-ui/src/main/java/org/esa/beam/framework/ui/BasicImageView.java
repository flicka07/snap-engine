/*
 * $Id: BasicImageView.java,v 1.4 2007/04/23 13:50:27 marcop Exp $
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
package org.esa.beam.framework.ui;

/**
 *  Histogram for single band images
 */

import com.bc.swing.ViewPane;
import org.esa.beam.util.Guardian;

import javax.media.jai.PlanarImage;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.MouseWheelEvent;
import java.awt.image.RenderedImage;

/**
 * A component which displays a JAI <code>PlanarImage</code> in an <code>ImageDisplay</code>.
 * <p/>
 * This component itself is not opaque, because the <code>ImageDisplay</code> fills the entire area.
 *
 * @author Norman Fomferra
 * @version $Revision: 1.4 $  $Date: 2007/04/23 13:50:27 $
 * @see ImageDisplay
 */
public class BasicImageView extends BasicView {

    private ImageDisplay _imageDisplay;

    /**
     * Constructs a new image view. The magnifier is initially visible.
     */
    public BasicImageView() {
        setOpaque(false);
    }

    /**
     * Constructs a new image view. The image will be owned by this view (and thus disposed after disposing the view)
     * and the magnifier is initially visible.
     *
     * @param sourceImage the source image, must not be <code>null</code>
     */
    public BasicImageView(PlanarImage sourceImage) {
        setOpaque(false);
        setSourceImage(sourceImage);
    }

    /**
     * If the <code>preferredSize</code> has been set to a
     * non-<code>null</code> value just returns it.
     * If the UI delegate's <code>getPreferredSize</code>
     * method returns a non <code>null</code> value then return that;
     * otherwise defer to the component's layout manager.
     *
     * @return the value of the <code>preferredSize</code> property
     *
     * @see #setPreferredSize
     * @see javax.swing.plaf.ComponentUI
     */
    @Override
    public Dimension getPreferredSize() {
        if (isPreferredSizeSet()) {
            return super.getPreferredSize();
        } else if (_imageDisplay != null) {
            return _imageDisplay.getPreferredSize();
        } else {
            return super.getPreferredSize();
        }
    }

    /**
     * Gets the source image displayed in this view.
     *
     * @return the source image
     */
    public RenderedImage getSourceImage() {
        return _imageDisplay != null ? _imageDisplay.getImage() : null;
    }

    /**
     * Gets the source image to be displayed in this view.
     *
     * @param sourceImage the source image
     */
    public void setSourceImage(RenderedImage sourceImage) {
        Guardian.assertNotNull("sourceImage", sourceImage);
        if (_imageDisplay == null) {
            initUI(sourceImage);
        }
        _imageDisplay.setImage(sourceImage);
        revalidate();
        repaint();
    }

    /**
     * Gets the actual component used to display the source image.
     *
     * @return the image display component
     */
    public ImageDisplay getImageDisplay() {
        return _imageDisplay;
    }

    public PixelInfoFactory getPixelInfoFactory() {
        return getImageDisplay().getPixelInfoFactory();
    }

    public void setPixelInfoFactory(PixelInfoFactory pixelInfoFactory) {
        getImageDisplay().setPixelInfoFactory(pixelInfoFactory);
    }

    /**
     * Creates the popup menu for the given component. This method is called by the <code>PopupMenuHandler</code>
     * registered on the given component.
     *
     * @param component the source component
     *
     * @see PopupMenuFactory
     * @see PopupMenuHandler
     */
    public JPopupMenu createPopupMenu(Component component) {
        JPopupMenu popupMenu = new JPopupMenu();
        addStandardPopupMenuItems(popupMenu);
        addCopyPixelInfoToClipboardMenuItem(popupMenu);
        getCommandUIFactory().addContextDependentMenuItems("image", popupMenu);
        return popupMenu;
    }

    /**
     * Creates the popup menu for the given mouse event. This method is called by the <code>PopupMenuHandler</code>
     * registered on the event fired component.
     *
     * @param event the fired mouse event
     *
     * @see PopupMenuFactory
     * @see PopupMenuHandler
     */
    public JPopupMenu createPopupMenu(MouseEvent event) {
        return null;
    }

    /**
     * Adds a new pixel position listener to this image display component.
     *
     * @param listener the pixel position listener to be added
     */
    public void addPixelPositionListener(PixelPositionListener listener) {
        _imageDisplay.addPixelPositionListener(listener);
    }

    /**
     * Removes a pixel position listener from this image display component.
     *
     * @param listener the pixel position listener to be removed
     */
    public void removePixelPositionListener(PixelPositionListener listener) {
        _imageDisplay.removePixelPositionListener(listener);
    }

    /**
     * Sets the internal source references to <code>null</code>. This should help the garbage collector to quickly
     * release image resources.
     * <p/>
     * <p> The results of referencing an instance of this class after a call to <code>dispose()</code> are undefined.
     */
    @Override
    public void dispose() {

        if (_imageDisplay != null) {
            _imageDisplay.dispose();
        }

        _imageDisplay = null;

        super.dispose();
    }


    protected void initUI(RenderedImage sourceImage) {
        PopupMenuHandler popupMenuHandler = new PopupMenuHandler(this);

        _imageDisplay = new ImageDisplay(sourceImage);
        _imageDisplay.setOpaque(true);
        _imageDisplay.addMouseListener(popupMenuHandler);
        _imageDisplay.addMouseWheelListener(new ZoomHandler());
        _imageDisplay.addKeyListener(popupMenuHandler);

        setLayout(new BorderLayout());
        ViewPane imageDisplayScroller = _imageDisplay.createViewPane();
        add(imageDisplayScroller, BorderLayout.CENTER);
    }

    private static void addStandardPopupMenuItems(JPopupMenu popupMenu) {
        popupMenu.addSeparator();
    }

    private void addCopyPixelInfoToClipboardMenuItem(JPopupMenu popupMenu) {
        if (_imageDisplay.getPixelInfoFactory() != null) {
            JMenuItem menuItem = new JMenuItem("Copy Pixel-Info to Clipboard");
            menuItem.setMnemonic('C');
            menuItem.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    _imageDisplay.copyPixelInfoStringToClipboard();
                }
            });
            popupMenu.add(menuItem);
            popupMenu.addSeparator();
        }
    }
    private final class ZoomHandler implements MouseWheelListener {

        public void mouseWheelMoved(MouseWheelEvent e) {
            int notches = e.getWheelRotation();
            double currentViewScale = _imageDisplay.getViewModel().getViewScale();
            if (notches < 0) {
                _imageDisplay.zoom(currentViewScale * 1.1f);
            } else {
                _imageDisplay.zoom(currentViewScale * 0.9f);
            }
        }
    }


}


