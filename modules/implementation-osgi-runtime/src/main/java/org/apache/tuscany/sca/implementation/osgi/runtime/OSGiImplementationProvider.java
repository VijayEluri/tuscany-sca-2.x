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

package org.apache.tuscany.sca.implementation.osgi.runtime;

import static org.osgi.framework.Constants.SERVICE_RANKING;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.apache.tuscany.sca.assembly.ComponentReference;
import org.apache.tuscany.sca.assembly.ComponentService;
import org.apache.tuscany.sca.assembly.Extensible;
import org.apache.tuscany.sca.core.invocation.ProxyFactory;
import org.apache.tuscany.sca.core.invocation.ProxyFactoryExtensionPoint;
import org.apache.tuscany.sca.implementation.osgi.OSGiImplementation;
import org.apache.tuscany.sca.implementation.osgi.OSGiProperty;
import org.apache.tuscany.sca.interfacedef.InterfaceContract;
import org.apache.tuscany.sca.interfacedef.Operation;
import org.apache.tuscany.sca.interfacedef.java.JavaInterface;
import org.apache.tuscany.sca.invocation.Invoker;
import org.apache.tuscany.sca.provider.ImplementationProvider;
import org.apache.tuscany.sca.runtime.RuntimeComponent;
import org.apache.tuscany.sca.runtime.RuntimeComponentReference;
import org.apache.tuscany.sca.runtime.RuntimeComponentService;
import org.apache.tuscany.sca.runtime.RuntimeWire;
import org.oasisopen.sca.ServiceRuntimeException;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

/**
 *
 */
public class OSGiImplementationProvider implements ImplementationProvider {
    private RuntimeComponent component;
    private ProxyFactoryExtensionPoint proxyFactoryExtensionPoint;
    private Bundle osgiBundle;
    private OSGiImplementation implementation;
    private List<ServiceRegistration> registrations = new ArrayList<ServiceRegistration>();

    public OSGiImplementationProvider(RuntimeComponent component,
                                      OSGiImplementation impl,
                                      ProxyFactoryExtensionPoint proxyFactoryExtensionPoint) throws BundleException {
        this.component = component;
        this.proxyFactoryExtensionPoint = proxyFactoryExtensionPoint;
        this.implementation = impl;
        this.osgiBundle = impl.getBundle();
    }

    public Invoker createInvoker(RuntimeComponentService service, Operation operation) {
        return new OSGiTargetInvoker(operation, this, service);
    }

    public void start() {
        // First try to start the osgi bundle
        try {
            int state = osgiBundle.getState();
            if ((state & Bundle.STARTING) == 0 && (state & Bundle.ACTIVE) == 0) {
                osgiBundle.start();
            }
        } catch (BundleException e) {
            throw new ServiceRuntimeException(e);
        }

        for (ComponentReference ref : component.getReferences()) {
            RuntimeComponentReference reference = (RuntimeComponentReference)ref;
            InterfaceContract interfaceContract = reference.getInterfaceContract();
            JavaInterface javaInterface = (JavaInterface)interfaceContract.getInterface();
            final Class<?> interfaceClass = javaInterface.getJavaClass();

//            final Hashtable<String, Object> props = new Hashtable<String, Object>();
//            props.put(FILTER_MATCH_CRITERIA, "");
//            Collection<String> interfaceNames = new ArrayList<String>();
//            props.put(INTERFACE_MATCH_CRITERIA, interfaceNames);
//            interfaceNames.add(interfaceClass.getName());

            final Hashtable<String, Object> osgiProps = getOSGiProperties(reference);
            osgiProps.put(SERVICE_RANKING, Integer.MAX_VALUE);
            osgiProps.put("sca.reference", component.getURI() + "#reference(" + ref.getName() + ")");
            osgiProps.put(OSGiProperty.OSGI_REMOTE, "true");
            osgiProps.put(OSGiProperty.OSGI_REMOTE_CONFIGURATION_TYPE, "sca");
            osgiProps.put(OSGiProperty.OSGI_REMOTE_INTERFACES, new String[] {interfaceClass.getName()});

            ProxyFactory proxyService = proxyFactoryExtensionPoint.getInterfaceProxyFactory();
            if (!interfaceClass.isInterface()) {
                proxyService = proxyFactoryExtensionPoint.getClassProxyFactory();
            }

            for (RuntimeWire wire : reference.getRuntimeWires()) {
                final Object proxy = proxyService.createProxy(interfaceClass, wire);
                ServiceRegistration registration =
                    AccessController.doPrivileged(new PrivilegedAction<ServiceRegistration>() {
                        public ServiceRegistration run() {
                            // Register the proxy as OSGi service
                            BundleContext context = osgiBundle.getBundleContext();
                            ServiceRegistration registration =
                                context.registerService(interfaceClass.getName(), proxy, osgiProps);
                            // Create a DiscoveredServiceTracker to track the status of the remote service
//                            RemoteServiceTracker tracker = new RemoteServiceTracker(registration);
//                            context.registerService(DiscoveredServiceTracker.class.getName(), tracker, props);
                            return registration;
                        }
                    });
                registrations.add(registration);
            }
        }
    }

    public void stop() {
        for (ServiceRegistration registration : registrations) {
            registration.unregister();
        }
        registrations.clear();
        try {
            int state = osgiBundle.getState();
            if ((state & Bundle.STOPPING) == 0 && (state & Bundle.ACTIVE) != 0) {
                osgiBundle.stop();
            }
        } catch (BundleException e) {
            throw new ServiceRuntimeException(e);
        }
    }

    public boolean supportsOneWayInvocation() {
        return false;
    }

    /**
     * Get all the OSGi properties from the extension list
     * @param extensible
     * @return
     */
    protected Hashtable<String, Object> getOSGiProperties(Extensible extensible) {
        Hashtable<String, Object> props = new Hashtable<String, Object>();
        for (Object ext : extensible.getExtensions()) {
            if (ext instanceof OSGiProperty) {
                OSGiProperty p = (OSGiProperty)ext;
                props.put(p.getName(), p.getValue());
            }
        }
        return props;
    }
    
    protected Object getOSGiService(ComponentService service) {
        JavaInterface javaInterface = (JavaInterface)service.getInterfaceContract().getInterface();
        // String filter = getOSGiFilter(provider.getOSGiProperties(service));
        // FIXME: What is the filter?
        String filter = "(!(sca.reference=*))";
        // "(sca.service=" + component.getURI() + "#service-name\\(" + service.getName() + "\\))";
        BundleContext bundleContext = osgiBundle.getBundleContext();
        ServiceReference ref;
        try {
            ref = bundleContext.getServiceReferences(javaInterface.getName(), filter)[0];
        } catch (InvalidSyntaxException e) {
            throw new ServiceRuntimeException(e);
        }
        if (ref != null) {
            Object instance = bundleContext.getService(ref);
            return instance;
        } else {
            return null;
        }
    }

    RuntimeComponent getComponent() {
        return component;
    }

    OSGiImplementation getImplementation() {
        return implementation;
    }

//    private class RemoteServiceTracker implements DiscoveredServiceTracker {
//        private ServiceRegistration referenceRegistration;
//
//        private RemoteServiceTracker(ServiceRegistration referenceRegistration) {
//            super();
//            this.referenceRegistration = referenceRegistration;
//        }
//
//        public void serviceChanged(DiscoveredServiceNotification notification) {
//            ServiceEndpointDescription description = notification.getServiceEndpointDescription();
//            switch(notification.getType()) {
//                case UNAVAILABLE:
//                case AVAILABLE:
//                case MODIFIED:
//                case MODIFIED_ENDMATCH:
//            }
//        }
//    }

}
