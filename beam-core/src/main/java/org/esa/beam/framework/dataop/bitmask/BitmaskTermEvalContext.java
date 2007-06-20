/*
 * $Id: BitmaskTermEvalContext.java,v 1.1.1.1 2006/09/11 08:16:45 norman Exp $
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

package org.esa.beam.framework.dataop.bitmask;

/**
 * Defines a context in which bit-mask terms can be evaluated. The main purpose of this interface is to resolve dataset
 * names as part of flag references contained in bit-mask terms
 *
 * @author Norman Fomferra
 * @version $Revision: 1.1.1.1 $ $Date: 2006/09/11 08:16:45 $
 */
public interface BitmaskTermEvalContext {

    /**
     * Returns the dataset for the given dataset name. The method performs a case-insensitive search on the given name.
     *
     * @param datasetName the name of the flag dataset to be resolved
     *
     * @return the flag dataset associated with the given name
     */
    FlagDataset getFlagDataset(String datasetName);

    /**
     * Disposes the context by releasing all allocated resources. The implementation should explicitely set all dataset
     * references to null and force a garbage collection.
     */
    void dispose();
}
