// Copyright (c) 2002-2007, Michael Rudolf
// All rights reserved.
// 
// Redistribution and use in source and binary forms, with or without modification, are
// permitted provided that the following conditions are met:
// 
// 1.) Redistributions of source code must retain the above copyright notice, this list of
//     conditions and the following disclaimer.
// 2.) Redistributions in binary form must reproduce the above copyright notice, this
//     list of conditions and the following disclaimer in the documentation and/or other
//     materials provided with the distribution.
// 3.) Neither the name Michael Rudolf nor the names of its contributors may
//     be used to endorse or promote products derived from this software without
//     specific prior written permission.
// 
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
// CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
// BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
// FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
// THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
// INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
// (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
// SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
// HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
// STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING
// IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
// POSSIBILITY OF SUCH DAMAGE.

package net.javaprog.ui.wizard;

import java.util.Hashtable;
import java.util.Map;

/**
 * Provides a data container that can be shared by steps aiming to
 * collect data. A step can register a data source (such as a text field)
 * with this data model enabling other steps to look up the value of
 * that data source.
 *
 * @see DataLookup
 *
 * @author Michael Rudolf
 */
public class DataModel {
    
    /**
     * Maps identifier keys to DataLookup instances.
     *
     * @see #registerDataLookup(String, DataLookup)
     */
    protected final Map data = new Hashtable();
    
    /**
     * Creates a new data model.
     */
    public DataModel() {}

    /**
     * Returns the value of a previously registered data source. This
     * method forwards exceptions thrown by {@link DataLookup#lookupData()}.
     *
     * @param key   the name of the data source for which the value should be 
     *              retrieved
     *
     * @see #registerDataLookup(String, DataLookup)
     */
    public Object getData(String key) {
        Object o = data.get(key);
        if (o == null) {
            return null;
        } else {
            return ((DataLookup)o).lookupData();
        }
    }
    
    /**
     * Registers the given data lookup mechanism with this model using
     * the key as identifier.
     *
     * @param key       the name under which the data source will be registered
     * @param lookup    the lookup strategy for retrieving the value from the 
     *                  data source
     *
     * @see #getData(String)
     * @see #unregisterDataLookup
     * @see DataLookup
     */
    public void registerDataLookup(String key, DataLookup lookup) {
        data.put(key, lookup);
    }
    
    /**
     * Unregisters the data lookup mechanism previously registered
     * with this model using the given key.
     *
     * @param key the name of the data source to remove
     *
     * @see #registerDataLookup
     */
    public void unregisterDataLookup(String key) {
        data.remove(key);
    }
}
