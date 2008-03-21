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

package org.apache.tuscany.sca.implementation.widget.provider;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import org.apache.tuscany.sca.assembly.Binding;
import org.apache.tuscany.sca.assembly.ComponentProperty;
import org.apache.tuscany.sca.assembly.ComponentReference;
import org.apache.tuscany.sca.assembly.OptimizableBinding;
import org.apache.tuscany.sca.invocation.Invoker;
import org.apache.tuscany.sca.invocation.Message;
import org.apache.tuscany.sca.runtime.RuntimeComponent;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * Implements an invoker for resource component implementations.
 */
class WidgetImplementationInvoker implements Invoker {
    private RuntimeComponent component;
    private String widgetName;
    private String widgetFolderURL;
    private String widgetLocationURL;
    
    WidgetImplementationInvoker(RuntimeComponent component, String widgetName, String widgetFolderURL, String widgetLocationURL) {
        this.component = component;
        this.widgetName = widgetName + ".js";
        this.widgetFolderURL = widgetFolderURL;
        this.widgetLocationURL = widgetLocationURL;
    }
    
    public Message invoke(Message msg) {
        
        // Get the resource id from the request message
        String id = (String)((Object[])msg.getBody())[0];
        try {
            
            if (id.length() == 0) {

                // Return an input stream for the widget resource
                URL url = new URL(widgetLocationURL);
                InputStream is = url.openStream();
                msg.setBody(is);
                
            } else if (id.equals(widgetName)) {
                
                // Generate JavaScript header for use in the Widget
                InputStream is = generateWidgetCode();
                msg.setBody(is);
                
            } else {

                // Return an input stream for a resource inside the
                // widget folder
                URL url = new URL(widgetFolderURL +'/' + id);
                InputStream is = url.openStream();
                msg.setBody(is);
            }
        } catch (MalformedURLException e) {

            // Report exception as a fault
            msg.setFaultBody(e);
        } catch (IOException e) {

            // Report exception as a fault
            msg.setFaultBody(e);
        }
        return msg;
    }

    /**
     * This helper class concatenates the necessary JavaScript client code into a
     * single JavaScript per component
     */
    private InputStream generateWidgetCode() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        PrintWriter pw = new PrintWriter(bos);

        pw.println();
        pw.println("/* Apache Tuscany SCA Widget header */");
        pw.println();
        
        for(ComponentReference reference : component.getReferences()) {
            for(Binding binding : reference.getBindings()) {
                String bindingProxyName = WidgetProxyHelper.getJavaScriptProxyFile(binding.getClass().getName());
                if(bindingProxyName != null) {
                    generateJavaScriptBindingProxy(pw,bindingProxyName);
                }
            }
        }
        
        //process properties
        generateJavaScriptPropertyFunction(pw);
        
        //process references
        generateJavaScriptReferenceFunction(pw);
        
       
        pw.println();
        pw.println("/** End of Apache Tuscany SCA Widget */");
        pw.println();
        pw.flush();
        pw.close();

        return new ByteArrayInputStream(bos.toByteArray());
    }

    /**
     * Retrieve the binding proxy based on the bind name
     * and embedded the JavaScript into this js
     */
    private void generateJavaScriptBindingProxy(PrintWriter pw, String bindingProxyName) throws IOException {
        //FIXME: Handle the case where the JavaScript binding client is not found
        InputStream is = getClass().getClassLoader().getResourceAsStream(bindingProxyName);
        if (is != null) {
            int i;
            while ((i = is.read()) != -1) {
                pw.write(i);
            }        	
        }
        
        pw.println();
        pw.println();
    }
    
    /**
     * Generate JavaScript code to inject SCA Properties
     * @param pw
     * @throws IOException
     */
    private void generateJavaScriptPropertyFunction(PrintWriter pw) throws IOException {

        pw.println("var propertyMap = new String();");
        for(ComponentProperty property : component.getProperties()) {
            String propertyName = property.getName();

            pw.println("propertyMap." + propertyName + " = \"" + getPropertyValue(property) + "\"");
        }
        
        pw.println("function Property(name) {");
        pw.println("    return propertyMap[name];");
        pw.println("}");
    }
    
    /**
     * Convert property value to String
     * @param property
     * @return
     */
    private String getPropertyValue(ComponentProperty property) {
    	Document doc = (Document)property.getValue();
    	Element rootElement = doc.getDocumentElement();
    	
    	String value = null;
    	
    	//FIXME : Provide support for isMany and other property types
    	
    	if (rootElement.getChildNodes().getLength() > 0) {
            value = rootElement.getChildNodes().item(0).getTextContent();
        }
    	
    	return value;
    }
    
    /**
     * Generate JavaScript code to inject SCA References
     * @param pw
     * @throws IOException
     */
    private void generateJavaScriptReferenceFunction (PrintWriter pw) throws IOException {
        
        pw.println("var referenceMap = new Object();");
        for(ComponentReference reference : component.getReferences()) {
            String referenceName = reference.getName();
            Binding binding = reference.getBindings().get(0);
            if (binding != null) {
                
                String proxyClient = WidgetProxyHelper.getJavaScriptProxyClient(binding.getClass().getName());
                if(proxyClient != null) {
                    
                    //FIXME Eventually the binding URI should be initialized from the SCA domain
                    // for now point to the target binding model directly, assuming that it's available
                    // in the same node
                    String targetURI = null;
                    if (binding instanceof OptimizableBinding) {
                        Binding targetBinding = ((OptimizableBinding)binding).getTargetBinding();
                        if (targetBinding != null) {
                            targetURI = URI.create(targetBinding.getURI()).getPath();
                        }
                    }
                    if (targetURI == null) {
                        targetURI = URI.create(binding.getURI()).getPath();
                        if (!targetURI.startsWith("/")) {
                            targetURI = "/" + targetURI;
                        }
                    }
                    
                    if(proxyClient.equals("JSONRpcClient")) {
                        pw.println("referenceMap." + referenceName + " = new " + proxyClient + "(\"" + targetURI + "\").Service;");
                    } else {
                        pw.println("referenceMap." + referenceName + " = new " + proxyClient + "(\"" + targetURI + "\");");
                    }
                }                
            }
        }
        
        pw.println("function Reference(name) {");
        pw.println("    return referenceMap[name];");
        pw.println("}");
    }

}
