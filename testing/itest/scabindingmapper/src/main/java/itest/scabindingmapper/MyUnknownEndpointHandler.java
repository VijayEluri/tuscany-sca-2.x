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

package itest.scabindingmapper;

import org.apache.tuscany.sca.assembly.Binding;
import org.apache.tuscany.sca.assembly.EndpointReference;
import org.apache.tuscany.sca.assembly.SCABinding;
import org.apache.tuscany.sca.assembly.impl.SCABindingFactoryImpl;
import org.apache.tuscany.sca.runtime.UnknownEndpointHandler;

public class MyUnknownEndpointHandler implements UnknownEndpointHandler {

    @Override
    public Binding handleUnknownEndpoint(EndpointReference endpointReference) {
        
        if (endpointReference.getTargetEndpoint().getURI().endsWith("Service1")) {
            SCABinding b = new SCABindingFactoryImpl().createSCABinding();
            b.setURI("http://localhost:8085/Service1/Helloworld");
            return b;
            
        } else if (endpointReference.getTargetEndpoint().getURI().endsWith("Service2")) {
            SCABinding b = new SCABindingFactoryImpl().createSCABinding();
            b.setURI("http://localhost:8085/Service2/Helloworld");
            return b;
        }

        return null;
    }

}
