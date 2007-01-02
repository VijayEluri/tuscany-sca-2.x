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
package org.apache.tuscany.spi.extension;

import java.util.Collections;

import org.apache.tuscany.spi.component.CompositeComponent;
import org.apache.tuscany.spi.component.ScopeContainer;
import org.apache.tuscany.spi.component.Service;
import org.apache.tuscany.spi.component.TargetInvokerCreationException;
import org.apache.tuscany.spi.model.Operation;
import org.apache.tuscany.spi.wire.InboundWire;
import org.apache.tuscany.spi.wire.TargetInvoker;

import junit.framework.TestCase;
import org.easymock.EasyMock;

/**
 * @version $Rev$ $Date$
 */
public class CompositeComponentExtensionTestCase extends TestCase {
    private CompositeComponent composite;

    public void testDefaultService() throws Exception {
        Service service = EasyMock.createMock(Service.class);
        EasyMock.expect(service.getName()).andReturn("service").atLeastOnce();
        EasyMock.expect(service.isSystem()).andReturn(false).atLeastOnce();
        service.getServiceBindings();
        EasyMock.expectLastCall().andReturn(Collections.emptyList()).atLeastOnce();
        EasyMock.replay(service);
        composite.register(service);
        assertEquals(service, composite.getService(null));
        assertNull(composite.getSystemService(null));
    }

    public void testSystemService() throws Exception {
        Service service = EasyMock.createMock(Service.class);
        EasyMock.expect(service.getName()).andReturn("service").atLeastOnce();
        EasyMock.expect(service.isSystem()).andReturn(true).atLeastOnce();
        service.getServiceBindings();
        EasyMock.expectLastCall().andReturn(Collections.emptyList()).atLeastOnce();
        EasyMock.replay(service);
        composite.register(service);
        assertNull(composite.getService(null));
        assertEquals(service, composite.getSystemService(null));
    }

    public void testMoreThanOneServiceGetDefault() throws Exception {
        Service service1 = EasyMock.createMock(Service.class);
        EasyMock.expect(service1.getName()).andReturn("service1").atLeastOnce();
        EasyMock.expect(service1.isSystem()).andReturn(false).atLeastOnce();
        service1.getServiceBindings();
        EasyMock.expectLastCall().andReturn(Collections.emptyList()).atLeastOnce();
        EasyMock.replay(service1);

        Service service2 = EasyMock.createMock(Service.class);
        EasyMock.expect(service2.getName()).andReturn("service2").atLeastOnce();
        EasyMock.expect(service2.isSystem()).andReturn(false).atLeastOnce();
        service2.getServiceBindings();
        EasyMock.expectLastCall().andReturn(Collections.emptyList()).atLeastOnce();
        EasyMock.replay(service2);

        composite.register(service1);
        composite.register(service2);
        assertNull(composite.getService(null));
        assertNull(composite.getSystemService(null));
    }

    protected void setUp() throws Exception {
        super.setUp();
        composite = new CompositeComponentExtension("foo", null, null, null) {

            public TargetInvoker createTargetInvoker(String targetName, Operation operation, InboundWire callbackWire)
                throws TargetInvokerCreationException {
                throw new UnsupportedOperationException();
            }

            public void setScopeContainer(ScopeContainer scopeContainer) {
                throw new UnsupportedOperationException();
            }
        };
    }
}
