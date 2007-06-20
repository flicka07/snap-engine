/*
 * $Id: UserInputHistory.java,v 1.1 2006/10/10 14:47:39 norman Exp $
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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.esa.beam.util.Guardian;
import org.esa.beam.util.PropertyMap;

/**
 * <code>FileHistory</code> is a fixed-size array for the pathes of files opened/saved by a user. If a new file is added
 * and the file history is full, the list of registered files is shifted so that the oldest file path is beeing
 * skipped..
 *
 * @author Norman Fomferra
 * @version $Revision: 1.1 $  $Date: 2006/10/10 14:47:39 $
 */
public class UserInputHistory {

    private String _propertyKey;
    private int _maxNumEntries;
    private List _entriesList;

    public UserInputHistory(int maxNumEntries, String propertyKey) {
        Guardian.assertNotNullOrEmpty("propertyKey", propertyKey);
        _propertyKey = propertyKey;
        setMaxNumEntries(maxNumEntries);
    }

    public int getNumEntries() {
        if (_entriesList != null) {
            return _entriesList.size();
        }
        return 0;
    }

    public int getMaxNumEntries() {
        return _maxNumEntries;
    }

    public String getPropertyKey() {
        return _propertyKey;
    }

    public String[] getEntries() {
        if (_entriesList != null) {
            return (String[]) _entriesList.toArray(new String[_entriesList.size()]);
        }
        return null;
    }

    public void initBy(final PropertyMap propertyMap) {
        int maxNumEntries = propertyMap.getPropertyInt(getLengthKey(), 16);
        setMaxNumEntries(maxNumEntries);

        for (int i = maxNumEntries - 1; i >= 0; i--) {
            String entry = propertyMap.getPropertyString(getNumKey(i), null);
            if (entry != null) {
                push(entry);
            }
        }
    }

    protected boolean isValidItem(String item) {
        return true;
    }

    public void push(String entry) {
        if (entry != null && isValidItem(entry)) {
            if (_entriesList == null) {
                _entriesList = new LinkedList();
            }
            for (Iterator iterator = _entriesList.iterator(); iterator.hasNext();) {
                final String listedEntry = (String) iterator.next();
                if (listedEntry.equals(entry)) {
                    _entriesList.remove(listedEntry);
                    break;
                }
            }
            if (_entriesList.size() == _maxNumEntries) {
                _entriesList.remove(_entriesList.size() - 1);
            }
            _entriesList.add(0, entry);
        }
    }

    public void copyInto(PropertyMap propertyMap) {
        propertyMap.setPropertyInt(getLengthKey(), _maxNumEntries);
        for (int i = 0; i < 100; i++) {
            propertyMap.setPropertyString(getNumKey(i), null);
        }
        final String[] entries = getEntries();
        if (entries != null) {
            for (int i = 0; i < entries.length; i++) {
                propertyMap.setPropertyString(getNumKey(i), entries[i]);
            }
        }
    }

    private String getLengthKey() {
        return _propertyKey + ".length";
    }

    private String getNumKey(int index) {
        return _propertyKey + "." + index;
    }

    public void setMaxNumEntries(int maxNumEntries) {
        _maxNumEntries = maxNumEntries > 0 ? maxNumEntries : 16;
        if (_entriesList != null) {
            while (_maxNumEntries < _entriesList.size()) {
                _entriesList.remove(_entriesList.size() - 1);
            }
        }
    }
}
