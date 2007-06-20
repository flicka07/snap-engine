package org.esa.beam.framework.ui.application.support;

import com.bc.ceres.core.Assert;
import org.esa.beam.framework.ui.application.PageComponentContext;
import org.esa.beam.framework.ui.application.PageComponentDescriptor;
import org.esa.beam.framework.ui.application.ToolView;
import org.esa.beam.util.Debug;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeListener;

/**
 * An abstract base class for client {@link org.esa.beam.framework.ui.application.ToolView}s.
 * <p>Clients should use this class a base class for their tool view implementations.</p>
 */
public abstract class AbstractToolView extends AbstractControlFactory implements ToolView {

    private PageComponentContext context;
    private PageComponentDescriptor descriptor;

    protected AbstractToolView() {
    }

    public PageComponentDescriptor getDescriptor() {
        return descriptor;
    }

    public final void setDescriptor(PageComponentDescriptor descriptor) {
        Assert.notNull(descriptor, "descriptor");
        Assert.state(this.descriptor == null, "this.descriptor == null");
        this.descriptor = descriptor;
    }

    /**
     * Gets the actual tool window component as part of the application's main frame window.
     *
     * @return The tool window part instance as passed into the {@link #setContext(PageComponentContext)} method
     *         or <code>null</code> if the too window has not yet been initialised.
     */
    public PageComponentContext getContext() {
        return context;
    }

    /**
     * Sets the tool window's context.
     * <p/>
     * <p>Clients may override this method in order configure their tool window.
     * However, after calling this method,
     * {@link #getContext()} shall return the same {@code context}.</p>
     * <p/>
     * <p>Clients must not call this method directly, it is called only once by the framework after a {@link PageComponentContext}
     * has been created for this tool window.</p>
     *
     * @param context The tool window part.
     */
    public final void setContext(PageComponentContext context) {
        Assert.notNull(context, "context");
        Assert.state(this.context == null, "this.context == null");
        this.context = context;
        registerLocalCommandExecutors(context);
    }

    /**
     * Gets the tool window identifier.
     *
     * @return The tool window identifier.
     */
    public String getId() {
        return descriptor.getId();
    }

    public String getTitle() {
        return descriptor.getTitle();
    }

    public Icon getIcon() {
        return descriptor.getSmallIcon();
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        descriptor.addPropertyChangeListener(listener);
    }

    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        descriptor.addPropertyChangeListener(propertyName, listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        descriptor.removePropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        descriptor.removePropertyChangeListener(propertyName, listener);
    }

    /**
     * Template method called once when this view is initialized; allows
     * subclasses to register local executors for shared commands with the view
     * context.
     *
     * @param context the view context
     */
    protected void registerLocalCommandExecutors(PageComponentContext context) {
    }

    // todo - remove following methods

    /**
     * The default implementation does nothing.
     * <p>Clients shall not call this method directly.</p>
     */
    public void dispose() {
    }

    /**
     * Gets the content pane of this tool window's part.
     *
     * @return The content pane of this tool window's part.
     */
    public Container getContentPane() {
        return context != null ? context.getPane().getControl() : null;
    }

    /**
     * Gets the first {@link Window} ancestor of this tool window's content pane, or
     * {@code null} if it is (currently) not contained inside a {@code Window}.
     *
     * @return The first {@code Window} ancestor, or {@code null}.
     */
    public Window getWindowAncestor() {
        Container contentPane = getContentPane();
        return contentPane != null ? SwingUtilities.getWindowAncestor(contentPane) : null;
    }


    /**
     * Sets the actual window title which may be different from what {@link org.esa.beam.framework.ui.application.PageComponentDescriptor#getTitle()} returns.
     *
     * @param title The window's title.
     */
    public void setTitle(String title) {
        // todo
        Debug.trace("TODO: implement setTitle(\"" + title + "\")");
    }


    /**
     * The default implementation does nothing.
     * <p>Clients shall not call this method directly.</p>
     */
    public void componentOpened() {
    }

    /**
     * The default implementation does nothing.
     * <p>Clients shall not call this method directly.</p>
     */
    public void componentClosed() {
    }

    /**
     * The default implementation does nothing.
     * <p>Clients shall not call this method directly.</p>
     */
    public void componentShown() {
    }

    /**
     * The default implementation does nothing.
     * <p>Clients shall not call this method directly.</p>
     */
    public void componentHidden() {
    }

    /**
     * The default implementation does nothing.
     * <p>Clients shall not call this method directly.</p>
     */
    public void componentFocusGained() {
    }

    /**
     * The default implementation does nothing.
     * <p>Clients shall not call this method directly.</p>
     */
    public void componentFocusLost() {
    }
}
