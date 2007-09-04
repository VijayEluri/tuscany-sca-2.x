/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */
package org.apache.tuscany.sca.assembly;

/**
 * Represent a binding that supports SCA wiring between component references and services
 * 
 * @version $Rev$ $Date$
 * 
 * @deprecated To be factored in the base Binding as reported in TUSCANY-1534:
 * 
 * - all bindings should be "wireable", i.e. can be configured using an SCA wire
 * 
 * - only some bindings will care about pointers to the in-memory model objects
 * representing the target component, service and binding
 * 
 * - all this stuff is only relevant for references so it's confusing to have it on
 * bindings which apply to services as well.
 */
@Deprecated
public interface WireableBinding extends Binding, Cloneable {

    /**
     * @param component
     */
    void setTargetComponent(Component component);
    
    /**
     * @param service
     */
    void setTargetComponentService(ComponentService service);
    
    /**
     * @param binding
     */
    void setTargetBinding(Binding binding);
    
    /**
     * @return
     */
    Binding getTargetBinding();
    
    /**
     * @return
     */
    Component getTargetComponent();
    
    /**
     * @return
     */
    ComponentService getTargetComponentService();

    /**
     * Clone the binding
     * @return
     */
    Object clone() throws CloneNotSupportedException;

}
