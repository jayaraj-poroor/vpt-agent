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

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Default implementation for DataLookup using reflection. This is how
 * you would register a text field with a data model using this default
 * implementation:
 * 
 * <pre>
 * DataModel dataModel;
 * JTextField textField;
 * ...
 * Method method = null;
 * try {
 *     method = textField.getClass().getMethod("getText", null);
 * } catch (NoSuchMethodException nsme) {}
 * DataLookup dataLookup = new DefaultDataLookup(textField, method, null);
 * dataModel.registerDataLookup("MyTextField", dataLookup);
 * </pre>
 *
 * @see DataModel
 *
 * @author Michael Rudolf
 */
public class DefaultDataLookup implements DataLookup {
    protected Method lookupMethod;
    protected Object[] parameterList;
    protected Object object;
    
    /**
     * Creates a new data lookup mechanism using the given method on
     * the specified object passing the given parameters.
     *
     * @param object        the object on which the given method will be invoked
     * @param lookupMethod  the method that should be invoked on the given 
     *                      object
     * @param arguments     the arguments that should be passed when invoking 
     *                      the given method on the given object
     */
    public DefaultDataLookup(Object object, Method lookupMethod, Object[] arguments) {
        this.object = object;
        if (lookupMethod == null) {
            throw new IllegalArgumentException("lookupMethod cannot be null.");
        }
        this.lookupMethod = lookupMethod;
        this.parameterList = arguments;
    }
    
    /**
     * Creates a new data lookup mechanism using the given property on the
     * specified object.
     *
     * @param object    the object of which the given property will be queried
     * @param property  the property that will be queried of the given object
     *
     * @since 0.1.4
     */
    public DefaultDataLookup(Object object, PropertyDescriptor property) {
        if (object == null) {
            throw new IllegalArgumentException("object cannot be null.");
        } else if (property == null) {
            throw new IllegalArgumentException("property cannot be null.");
        }
        this.object = object;
        this.lookupMethod = property.getReadMethod();
    }
    
    /**
     * Creates a new data lookup mechanism using the given property on the
     * specified object.
     *
     * @param object    the object of which the given property will be queried
     * @param property  the property that will be queried of the given object
     *
     * @since 0.1.4
     */
    public DefaultDataLookup(Object object, String property) 
            throws IntrospectionException {
        if (object == null) {
            throw new IllegalArgumentException("object cannot be null.");
        } else if (property == null) {
            throw new IllegalArgumentException("property cannot be null.");
        }
        this.object = object;
        BeanInfo info = Introspector.getBeanInfo(object.getClass());
        PropertyDescriptor[] properties = info.getPropertyDescriptors();
        for (int i = 0; i < properties.length; i++) {
            if (properties[i].getName().equals(property)) {
                lookupMethod = properties[i].getReadMethod();
                break;
            }
        }
        if (lookupMethod == null) {
            throw new IllegalArgumentException(
                    "Property " + property + " not found.");
        }
    }
    
    public Object lookupData() {
        try {
            return lookupMethod.invoke(object, parameterList);
        } catch (InvocationTargetException ite) {
            throw new RuntimeException(ite.getTargetException());
        } catch (IllegalAccessException iae) {
            throw new RuntimeException(iae);
        }
    }
}
