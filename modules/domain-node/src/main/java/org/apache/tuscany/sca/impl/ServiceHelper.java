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

package org.apache.tuscany.sca.impl;

import java.util.List;

import org.apache.tuscany.sca.assembly.AssemblyFactory;
import org.apache.tuscany.sca.assembly.Component;
import org.apache.tuscany.sca.assembly.ComponentReference;
import org.apache.tuscany.sca.assembly.ComponentService;
import org.apache.tuscany.sca.assembly.Endpoint;
import org.apache.tuscany.sca.assembly.EndpointReference;
import org.apache.tuscany.sca.assembly.Multiplicity;
import org.apache.tuscany.sca.assembly.Service;
import org.apache.tuscany.sca.context.CompositeContext;
import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.apache.tuscany.sca.core.FactoryExtensionPoint;
import org.apache.tuscany.sca.core.invocation.ExtensibleProxyFactory;
import org.apache.tuscany.sca.core.invocation.ProxyFactory;
import org.apache.tuscany.sca.core.invocation.ProxyFactoryExtensionPoint;
import org.apache.tuscany.sca.deployment.Deployer;
import org.apache.tuscany.sca.interfacedef.Interface;
import org.apache.tuscany.sca.interfacedef.InterfaceContract;
import org.apache.tuscany.sca.interfacedef.InvalidInterfaceException;
import org.apache.tuscany.sca.interfacedef.java.JavaInterface;
import org.apache.tuscany.sca.interfacedef.java.JavaInterfaceFactory;
import org.apache.tuscany.sca.runtime.EndpointRegistry;
import org.apache.tuscany.sca.runtime.RuntimeComponent;
import org.apache.tuscany.sca.runtime.RuntimeComponentReference;
import org.apache.tuscany.sca.runtime.RuntimeEndpointReference;
import org.oasisopen.sca.NoSuchServiceException;
import org.oasisopen.sca.ServiceRuntimeException;

/**
 * All the code for creating a service proxy in this helper class as it feels like 
 * it doesn't all belong in the node but i can't see where to refactor it to yet.
 * (perhaps its just the remote proxy bit that needs to go somewhere else?)
 */
public class ServiceHelper {
    
    public static <T> T getService(Class<T> interfaze, String serviceURI, EndpointRegistry endpointRegistry, ExtensionPointRegistry extensionPointRegistry, Deployer deployer) throws NoSuchServiceException {

        List<Endpoint> endpoints = endpointRegistry.findEndpoint(serviceURI);
        if (endpoints.size() < 1) {
            throw new NoSuchServiceException(serviceURI);
        }

        String serviceName = null;
        if (serviceURI.contains("/")) {
            int i = serviceURI.indexOf("/");
            if (i < serviceURI.length() - 1) {
                serviceName = serviceURI.substring(i + 1);
            }
        }

        Endpoint ep = endpoints.get(0);
        if (((RuntimeComponent)ep.getComponent()).getComponentContext() != null) {
            return ((RuntimeComponent)ep.getComponent()).getServiceReference(interfaze, serviceName).getService();
        } else {
            return getRemoteProxy(interfaze, ep, endpointRegistry, extensionPointRegistry, deployer);
        }
    }

    private static <T> T getRemoteProxy(Class<T> serviceInterface, Endpoint endpoint, EndpointRegistry endpointRegistry, ExtensionPointRegistry extensionPointRegistry, Deployer deployer) throws NoSuchServiceException {
        FactoryExtensionPoint factories = extensionPointRegistry.getExtensionPoint(FactoryExtensionPoint.class);
        AssemblyFactory assemblyFactory = factories.getFactory(AssemblyFactory.class);
        JavaInterfaceFactory javaInterfaceFactory = factories.getFactory(JavaInterfaceFactory.class);
        ProxyFactory proxyFactory =
            new ExtensibleProxyFactory(extensionPointRegistry.getExtensionPoint(ProxyFactoryExtensionPoint.class));

        CompositeContext compositeContext =
            new CompositeContext(extensionPointRegistry, endpointRegistry, null, null, null,
                                 deployer.getSystemDefinitions());

        RuntimeEndpointReference epr;
        try {
            epr =
                createEndpointReference(javaInterfaceFactory,
                                        compositeContext,
                                        assemblyFactory,
                                        endpoint,
                                        serviceInterface);
        } catch (Exception e) {
            throw new ServiceRuntimeException(e);
        }

        return proxyFactory.createProxy(serviceInterface, epr);
    }

    private static RuntimeEndpointReference createEndpointReference(JavaInterfaceFactory javaInterfaceFactory,
                                                             CompositeContext compositeContext,
                                                             AssemblyFactory assemblyFactory,
                                                             Endpoint endpoint,
                                                             Class<?> businessInterface)
        throws CloneNotSupportedException, InvalidInterfaceException {
        Component component = endpoint.getComponent();
        ComponentService service = endpoint.getService();
        ComponentReference componentReference = assemblyFactory.createComponentReference();
        componentReference.setName("sca.client." + service.getName());

        componentReference.setCallback(service.getCallback());
        componentReference.getTargets().add(service);
        componentReference.getPolicySets().addAll(service.getPolicySets());
        componentReference.getRequiredIntents().addAll(service.getRequiredIntents());
        componentReference.getBindings().add(endpoint.getBinding());

        InterfaceContract interfaceContract = service.getInterfaceContract();
        Service componentTypeService = service.getService();
        if (componentTypeService != null && componentTypeService.getInterfaceContract() != null) {
            interfaceContract = componentTypeService.getInterfaceContract();
        }
        interfaceContract = getInterfaceContract(javaInterfaceFactory, interfaceContract, businessInterface);
        componentReference.setInterfaceContract(interfaceContract);
        componentReference.setMultiplicity(Multiplicity.ONE_ONE);
        // component.getReferences().add(componentReference);

        // create endpoint reference
        EndpointReference endpointReference = assemblyFactory.createEndpointReference();
        endpointReference.setComponent(component);
        endpointReference.setReference(componentReference);
        endpointReference.setBinding(endpoint.getBinding());
        endpointReference.setUnresolved(false);
        endpointReference.setStatus(EndpointReference.Status.WIRED_TARGET_FOUND_AND_MATCHED);

        endpointReference.setTargetEndpoint(endpoint);

        componentReference.getEndpointReferences().add(endpointReference);
        ((RuntimeComponentReference)componentReference).setComponent((RuntimeComponent)component);
        ((RuntimeEndpointReference)endpointReference).bind(compositeContext);

        return (RuntimeEndpointReference)endpointReference;
    }

    private static InterfaceContract getInterfaceContract(JavaInterfaceFactory javaInterfaceFactory,
                                                   InterfaceContract interfaceContract,
                                                   Class<?> businessInterface) throws CloneNotSupportedException,
        InvalidInterfaceException {
        if (businessInterface == null) {
            return interfaceContract;
        }
        boolean compatible = false;
        if (interfaceContract != null && interfaceContract.getInterface() != null) {
            Interface interfaze = interfaceContract.getInterface();
            if (interfaze instanceof JavaInterface) {
                Class<?> cls = ((JavaInterface)interfaze).getJavaClass();
                if (cls != null && businessInterface.isAssignableFrom(cls)) {
                    compatible = true;
                }
            }
        }

        if (!compatible) {
            // The interface is not assignable from the interface contract
            interfaceContract = javaInterfaceFactory.createJavaInterfaceContract();
            JavaInterface callInterface = javaInterfaceFactory.createJavaInterface(businessInterface);
            interfaceContract.setInterface(callInterface);
            if (callInterface.getCallbackClass() != null) {
                interfaceContract.setCallbackInterface(javaInterfaceFactory.createJavaInterface(callInterface
                    .getCallbackClass()));
            }
        }

        return interfaceContract;
    }
}