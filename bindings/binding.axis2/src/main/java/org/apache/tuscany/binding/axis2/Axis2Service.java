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
package org.apache.tuscany.binding.axis2;


import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Set;
import java.util.Vector;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.wsdl.Definition;
import javax.wsdl.Operation;
import javax.wsdl.PortType;
import javax.xml.namespace.QName;

import org.osoa.sca.annotations.Destroy;

import org.apache.tuscany.spi.CoreRuntimeException;
import org.apache.tuscany.spi.builder.BuilderConfigException;
import org.apache.tuscany.spi.component.CompositeComponent;
import org.apache.tuscany.spi.extension.ServiceExtension;
import org.apache.tuscany.spi.host.ServletHost;
import org.apache.tuscany.spi.wire.WireService;

import commonj.sdo.helper.TypeHelper;
import org.apache.axis2.AxisFault;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.WSDL11ToAxisServiceBuilder;
import org.apache.axis2.description.WSDLToAxisServiceBuilder;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.axis2.wsdl.WSDLConstants.WSDL20_2004Constants;
import org.apache.tuscany.binding.axis2.util.SDODataBinding;
import org.apache.tuscany.binding.axis2.util.WebServiceOperationMetaData;
import org.apache.tuscany.binding.axis2.util.WebServicePortMetaData;

/**
 * An implementation of a {@link ServiceExtension} configured with the Axis2 binding
 *
 * @version $Rev$ $Date$
 */
public class Axis2Service<T> extends ServiceExtension<T> {

    private ServletHost host;
    private WebServiceBinding wsBinding;

    public Axis2Service(String theName,
                        Class<T> interfaze,
                        CompositeComponent parent,
                        WireService wireService,
                        WebServiceBinding binding,
                        ServletHost servletHost) {
        super(theName, interfaze, parent, wireService);
        wsBinding = binding;
        host = servletHost;
    }

    public void start() {
        super.start();
        initServlet();
    }

    @Destroy
    public void stop() throws CoreRuntimeException {
        super.stop();
    }

    protected Method getMethod(Class<?> serviceInterface, String operationName) {
        // Note: this doesn't support overloaded operations
        Method[] methods = serviceInterface.getMethods();
        for (Method m : methods) {
            if (m.getName().equals(operationName)) {
                return m;
            }
            // tolerate WSDL with capatalized operation name
            StringBuilder sb = new StringBuilder(operationName);
            sb.setCharAt(0, Character.toLowerCase(sb.charAt(0)));
            if (m.getName().equals(sb.toString())) {
                return m;
            }
        }
        throw new BuilderConfigException(
            "no operation named " + operationName + " found on service interface: " + serviceInterface.getName());
    }

    private void initServlet() {
        AxisService axisService;
        try {
            axisService = createAxisService(wsBinding);
        } catch (AxisFault e) {
            throw new BuilderConfigException(e);
        }
        WebServiceEntryPointServlet servlet = new WebServiceEntryPointServlet(axisService);
        ServletConfig sc = createDummyServletConfig();
        try {
            servlet.init(sc);
        } catch (ServletException e) {
            throw new BuilderConfigException(e);
        }

        String servletMapping = wsBinding.getWebAppName() + "/services/" + this.getName();
        host.registerMapping(servletMapping, servlet);
    }

    private AxisService createAxisService(WebServiceBinding wsBinding) throws AxisFault {
        Definition definition = wsBinding.getWSDLDefinition();
        WebServicePortMetaData wsdlPortInfo =
            new WebServicePortMetaData(definition, wsBinding.getWSDLPort(), null, false);

        // AxisServiceGroup serviceGroup = new AxisServiceGroup(axisConfig);
        // serviceGroup.setServiceGroupName(wsdlPortInfo.getServiceName().getLocalPart());
        // axisConfig.addServiceGroup(serviceGroup);

//TODO investigate if this is 20 wsdl what todo?        
        WSDLToAxisServiceBuilder builder =
            new WSDL11ToAxisServiceBuilder(definition, wsdlPortInfo.getServiceName(), wsdlPortInfo.getPort().getName());
        builder.setServerSide(true);
        AxisService axisService = builder.populateService();

        axisService.setName(this.getName());
        // axisService.setParent(serviceGroup);
        axisService.setServiceDescription("Tuscany configured AxisService for Service: '" + this.getName() + '\'');

        //FIXME:
        //TypeHelper typeHelper = wsBinding.getTypeHelper();
        //ClassLoader cl = wsBinding.getResourceLoader().getClassLoader();
        TypeHelper typeHelper = null;
        ClassLoader cl = null;

        Class<?> serviceInterface = this.getInterface();

        PortType wsdlPortType = wsdlPortInfo.getPortType();
        for (Object o : wsdlPortType.getOperations()) {
            Operation wsdlOperation = (Operation) o;
            String operationName = wsdlOperation.getName();
            QName operationQN = new QName(definition.getTargetNamespace(), operationName);
            Object entryPointProxy = this.getServiceInstance();

            WebServiceOperationMetaData omd = wsdlPortInfo.getOperationMetaData(operationName);

            Method operationMethod = getMethod(serviceInterface, operationName);
            //outElementQName is not needed when calling fromOMElement method, and we can not get elementQName for
            // oneway operation.
            SDODataBinding dataBinding = new SDODataBinding(cl, typeHelper, omd.isDocLitWrapped(), null);
            WebServiceEntryPointInOutSyncMessageReceiver msgrec =
                new WebServiceEntryPointInOutSyncMessageReceiver(entryPointProxy, operationMethod,
                    dataBinding, cl);

            AxisOperation axisOp = axisService.getOperation(operationQN);
            axisOp.setMessageExchangePattern(WSDL20_2004Constants.MEP_URI_IN_OUT);
            axisOp.setMessageReceiver(msgrec);
        }

        return axisService;
    }

    private ServletConfig createDummyServletConfig() {
        return new DummyServletConfig();
    }


    private class DummyServletConfig implements ServletConfig {

        public String getServletName() {
            return "TuscanyWSServlet";
        }

        public ServletContext getServletContext() {
            return new DummyServletContext();
        }

        public String getInitParameter(String arg0) {
            return null;
        }

        public Enumeration getInitParameterNames() {
            return new Vector().elements();
        }
    }


    private class DummyServletContext implements ServletContext {

        public ServletContext getContext(String arg0) {
            return null;
        }

        public int getMajorVersion() {
            return 0;
        }

        public int getMinorVersion() {
            return 0;
        }

        public String getMimeType(String arg0) {
            return null;
        }

        public Set getResourcePaths(String arg0) {
            return null;
        }

        public URL getResource(String arg0) throws MalformedURLException {
            return null;
        }

        public InputStream getResourceAsStream(String arg0) {
            return null;
        }

        public RequestDispatcher getRequestDispatcher(String arg0) {
            return null;
        }

        public RequestDispatcher getNamedDispatcher(String arg0) {
            return null;
        }

        public Servlet getServlet(String arg0) throws ServletException {
            return null;
        }

        public Enumeration getServlets() {
            return null;
        }

        public Enumeration getServletNames() {
            return null;
        }

        public void log(String arg0) {
        }

        public void log(Exception arg0, String arg1) {
        }

        public void log(String arg0, Throwable arg1) {
        }

        public String getRealPath(String arg0) {
            return null;
        }

        public String getServerInfo() {
            return null;
        }

        public String getInitParameter(String arg0) {
            return null;
        }

        public Enumeration getInitParameterNames() {
            return null;
        }

        public Object getAttribute(String arg0) {
            return null;
        }

        public Enumeration getAttributeNames() {
            return null;
        }

        public void setAttribute(String arg0, Object arg1) {
        }

        public void removeAttribute(String arg0) {
        }

        public String getServletContextName() {
            return null;
        }
    }
}
