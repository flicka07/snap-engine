/*
 * $Id: ProductNodeList.java,v 1.1.1.1 2006/09/11 08:16:45 norman Exp $
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
package org.esa.beam.framework.datamodel;

import org.esa.beam.util.Guardian;

import java.util.Collection;
import java.util.List;
import java.util.Vector;

/**
 * A type-safe list for elements of the type <code>ProductNode</code>.
 *
 * @author Norman Fomferra
 * @version $Revision: 1.1.1.1 $ $Date: 2006/09/11 08:16:45 $
 */
public final class ProductNodeList {

    private final Class _elemType;
    private final List _nodes;
    private final List _removedNodes;

    /**
     * Constructs a new list named nodes.
     */
    public ProductNodeList() {
        this(ProductNode.class);
    }

    /**
     * Constructs a new list named nodes.
     */
    public ProductNodeList(final Class elemType) {
        Guardian.assertNotNull("elemType", elemType);
        if (!ProductNode.class.isAssignableFrom(elemType)) {
            throw new IllegalArgumentException("'elemType' is not a ProductNode type");
        }
        _elemType = elemType;
        _nodes = new Vector();
        _removedNodes = new Vector();
    }

    /**
     * Gets the element type of this list.
     */
    public Class getElemType() {
        return _elemType;
    }

    /**
     * Gets the size of this list.
     */
    public final int size() {
        return _nodes.size();
    }

    /**
     * Gets the element at the spcified index.
     *
     * @param index the index, must be in the range zero to <code>size()</code>
     */
    public final ProductNode getAt(int index) {
        return (ProductNode) _nodes.get(index);
    }

    /**
     * Gets the names of all nodes contained in this list. If this list is empty a zero-length array is returned.
     *
     * @return a string array containing all node names, never <code>null</code>
     */
    public final String[] getNames() {
        String[] names = new String[size()];
        for (int i = 0; i < size(); i++) {
            names[i] = getAt(i).getName();
        }
        return names;
    }

    /**
     * Gets the display names of all nodes contained in this list.
     * If this list is empty a zero-length array is returned.
     *
     * @return a string array containing all node display names, never <code>null</code>
     * @see ProductNode#getDisplayName()
     */
    public String[] getDisplayNames() {
        String[] displayNames = new String[size()];
        for (int i = 0; i < size(); i++) {
            displayNames[i] = getAt(i).getDisplayName();
        }
        return displayNames;
    }

    /**
     * Gets the element with the given name. The method performs a case insensitive search.
     *
     * @param name the name of the node, must not be <code>null</code>
     *
     * @return the node with the given name or <code>null</code> if a node with the given name is not contained in this
     *         list
     *
     * @throws IllegalArgumentException if the name is <code>null</code>
     */
    public final ProductNode get(String name) {
        int index = indexOf(name);
        return index >= 0 ? getAt(index) : null;
    }

    /**
     * Gets the element with the given display name.
     *
     * @param displayName the display name of the node, must not be <code>null</code>
     *
     * @return the node with the given display name or <code>null</code> if a node with the given display name is not contained in this
     *         list
     *
     * @throws IllegalArgumentException if the display name is <code>null</code>
     * @see ProductNode#getDisplayName()
     */
    public ProductNode getByDisplayName(String displayName) {
        Guardian.assertNotNull("displayName", displayName);
        for (int i = 0; i < size(); i++) {
            if (getAt(i).getDisplayName().equals(displayName)) {
                return getAt(i);
            }
        }
        return  null;
    }

    /**
     * Tests if this list contains a node with the given name.
     *
     * @param name the name of the node, must not be <code>null</code>
     *
     * @return true if this list contains a node with the given name.
     *
     * @throws IllegalArgumentException if the name is <code>null</code>
     */
    public final boolean contains(String name) {
        return indexOf(name) >= 0;
    }

    /**
     * Tests if this list contains the given node.
     *
     * @param node the node
     *
     * @return true if this list contains the given node.
     *
     * @throws IllegalArgumentException if the node is <code>null</code>
     */
    public final boolean contains(ProductNode node) {
        if (node == null) {
            return false;
        }
        return _nodes.contains(node);
    }

    /**
     * Adds a new node to this list. Note that <code>null</code> nodes are not added to this list.
     *
     * @param node the node to be added, ignored if <code>null</code>
     *
     * @return true if the node was added, otherwise false.
     */
    public final boolean add(ProductNode node) {
        if (node != null && _elemType.isInstance(node)) {
            return _nodes.add(node);
        }
        return false;
    }

    /**
     * Inserts a new node to this list at the given index. Note that <code>null</code> nodes are not added to this
     * list.
     *
     * @param node  the node to be added, ignored if <code>null</code>
     * @param index the insert index
     *
     * @throws ArrayIndexOutOfBoundsException if the index was invalid.
     */
    public final void insert(ProductNode node, int index) {
        if (node != null && _elemType.isInstance(node)) {
            _nodes.add(index, node);
        }
    }

    /**
     * Clears the internal removed product nodes list.
     */
    public void clearRemovedList() {
        _removedNodes.clear();
    }

    /**
     * Gets all removed product nodes.
     *
     * @return a collection of all removed product nodes.
     */
    public Collection getRemovedNodes() {
        return _removedNodes;
    }

    /**
     * Removes the given node from this list. The removed nodes will be added to the internal list of removed product
     * nodes.
     *
     * @param node the node to be removed, ignored if <code>null</code>
     *
     * @return <code>true</code> if the node is a member of this list and could successfully be removed,
     *         <code>false</code> otherwise
     */
    public final boolean remove(ProductNode node) {
        if (node != null) {
            _removedNodes.add(node);
            return _nodes.remove(node);
        }
        return false;
    }

    /**
     * Removes all nodes from this list.
     */
    public final void removeAll() {
        _removedNodes.addAll(_nodes);
        _nodes.clear();
    }

    /**
     * Releases all of the resources used by this object instance and all of its owned children. Its primary use is to
     * allow the garbage collector to perform a vanilla job.
     * <p/>
     * <p>This method should be called only if it is for sure that this object instance will never be used again. The
     * results of referencing an instance of this class after a call to <code>dispose()</code> are undefined.
     */
    public final void dispose() {
        for (int i = 0; i < size(); i++) {
            getAt(i).dispose();
        }
        removeAll();
        disposeRemovedList();
    }

    /**
     * Creates a subset of this list using the given filter.
     *
     * @param filter the product node filter to be used, if <code>null</code> a clone of this list is created
     */
    public ProductNodeList createSubset(ProductNodeFilter filter) {
        ProductNodeList list = new ProductNodeList(_elemType);
        for (int i = 0; i < size(); i++) {
            ProductNode node = getAt(i);
            if (filter.accept(node)) {
                list.add(node);
            }
        }
        return list;
    }

    /**
     * Returns the list of named nodes as an array. If this list is empty a zero-length array is returned.
     *
     * @return a string array containing all node names, never <code>null</code>
     */
    public final ProductNode[] toArray() {
        return toArray(new ProductNode[0]);
    }

    /**
     * Returns the list of named nodes as an array. If this list is empty a zero-length array is returned.
     *
     * @param array the array into which the elements of the Vector are to be stored, if it is big enough; otherwise, a
     *              new array of the same runtime type is allocated for this purpose.
     *
     * @return an array containing the elements of the list. never <code>null</code>
     */
    public final ProductNode[] toArray(ProductNode[] array) {
        return (ProductNode[]) _nodes.toArray(array);
    }

    /**
     * Copies the product nodes of this product node list into the specified array. The array must be big enough to hold
     * all the product nodes in this product node list, else an <tt>IndexOutOfBoundsException</tt> is thrown.
     *
     * @param array the array into which the product nodes get copied.
     *
     * @throws NullPointerException      if the given array is null.
     * @throws IndexOutOfBoundsException if the given array is to small.
     */
    public final void copyInto(ProductNode[] array) {
        for (int i = 0; i < array.length; i++) {
            add(array[i]);
        }
    }

    /**
     * Gets the index of the node with the given name. The method performs a case insensitive search.
     *
     * @param name the name of the node, must not be <code>null</code>
     *
     * @return the index of the node with the given name or <code>-1</code> if a node with the given name is not
     *         contained in this list
     *
     * @throws IllegalArgumentException if the name is <code>null</code>
     */
    public final int indexOf(String name) {
        Guardian.assertNotNull("name", name);
        int n = size();
        for (int i = 0; i < n; i++) {
            if (getAt(i).getName().equalsIgnoreCase(name)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Gets the index of the given node.
     *
     * @param node the node to get the index, must not be <code>null</code>
     *
     * @return the index of the given node or <code>-1</code> if the node is not contained in this list
     *
     * @throws IllegalArgumentException if the node is <code>null</code>
     */
    public final int indexOf(ProductNode node) {
        Guardian.assertNotNull("node", node);
        return _nodes.indexOf(node);
    }

    private void disposeRemovedList() {
        for (int i = 0; i < _removedNodes.size(); i++) {
            ((ProductNode) _removedNodes.get(i)).dispose();
        }
        clearRemovedList();
    }
}
