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

package org.apache.tuscany.sca.implementation.java.xml;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;

import junit.framework.TestCase;

import org.apache.tuscany.sca.assembly.AssemblyFactory;
import org.apache.tuscany.sca.assembly.Composite;
import org.apache.tuscany.sca.assembly.DefaultAssemblyFactory;
import org.apache.tuscany.sca.assembly.xml.ComponentTypeProcessor;
import org.apache.tuscany.sca.assembly.xml.CompositeProcessor;
import org.apache.tuscany.sca.assembly.xml.ConstrainingTypeProcessor;
import org.apache.tuscany.sca.contribution.DefaultModelFactoryExtensionPoint;
import org.apache.tuscany.sca.contribution.ModelFactoryExtensionPoint;
import org.apache.tuscany.sca.contribution.processor.DefaultStAXArtifactProcessorExtensionPoint;
import org.apache.tuscany.sca.contribution.processor.ExtensibleStAXArtifactProcessor;
import org.apache.tuscany.sca.implementation.java.DefaultJavaImplementationFactory;
import org.apache.tuscany.sca.implementation.java.JavaImplementationFactory;
import org.apache.tuscany.sca.interfacedef.InterfaceContractMapper;
import org.apache.tuscany.sca.interfacedef.impl.InterfaceContractMapperImpl;
import org.apache.tuscany.sca.policy.DefaultPolicyFactory;
import org.apache.tuscany.sca.policy.PolicyFactory;

/**
 * Test writing Java implementations.
 * 
 * @version $Rev$ $Date$
 */
public class WriteTestCase extends TestCase {

    private DefaultStAXArtifactProcessorExtensionPoint staxProcessors;
    private ExtensibleStAXArtifactProcessor staxProcessor;
    private AssemblyFactory factory;
    private PolicyFactory policyFactory;
    
    @Override
    public void setUp() throws Exception {
        ModelFactoryExtensionPoint modelFactories = new DefaultModelFactoryExtensionPoint();
        factory = new DefaultAssemblyFactory();
        modelFactories.addFactory(factory);
        policyFactory = new DefaultPolicyFactory();
        modelFactories.addFactory(policyFactory);
        staxProcessors = new DefaultStAXArtifactProcessorExtensionPoint(modelFactories);
        staxProcessor = new ExtensibleStAXArtifactProcessor(staxProcessors, XMLInputFactory.newInstance(), XMLOutputFactory.newInstance());
        
        JavaImplementationFactory javaImplementationFactory = new DefaultJavaImplementationFactory();
        modelFactories.addFactory(javaImplementationFactory);

        staxProcessors.addArtifactProcessor(new CompositeProcessor(null, factory, policyFactory, staxProcessor));
        staxProcessors.addArtifactProcessor(new ComponentTypeProcessor(factory, policyFactory, staxProcessor));
        staxProcessors.addArtifactProcessor(new ConstrainingTypeProcessor(factory, policyFactory, staxProcessor));

        JavaImplementationProcessor javaProcessor = new JavaImplementationProcessor(modelFactories);
        staxProcessors.addArtifactProcessor(javaProcessor);
    }

    public void testReadWriteComposite() throws Exception {
        InputStream is = getClass().getResourceAsStream("Calculator.composite");
        Composite composite = staxProcessor.read(is, Composite.class);
        assertNotNull(composite);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        staxProcessor.write(composite, bos);
    }

}
