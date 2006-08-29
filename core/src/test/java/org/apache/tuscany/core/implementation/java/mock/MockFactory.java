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
package org.apache.tuscany.core.implementation.java.mock;

import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.tuscany.spi.builder.BuilderConfigException;
import org.apache.tuscany.spi.component.AtomicComponent;
import org.apache.tuscany.spi.component.ScopeContainer;
import org.apache.tuscany.spi.model.Scope;
import org.apache.tuscany.spi.services.work.WorkScheduler;
import org.apache.tuscany.spi.wire.InboundInvocationChain;
import org.apache.tuscany.spi.wire.InboundWire;
import org.apache.tuscany.spi.wire.Interceptor;
import org.apache.tuscany.spi.wire.MessageHandler;
import org.apache.tuscany.spi.wire.OutboundInvocationChain;
import org.apache.tuscany.spi.wire.OutboundWire;
import org.apache.tuscany.spi.wire.TargetInvoker;
import org.apache.tuscany.spi.wire.WireService;
import org.apache.tuscany.spi.idl.java.JavaServiceContract;

import org.apache.tuscany.core.implementation.PojoConfiguration;
import org.apache.tuscany.core.implementation.java.JavaAtomicComponent;
import org.apache.tuscany.core.implementation.java.JavaTargetInvoker;
import org.apache.tuscany.core.injection.PojoObjectFactory;
import org.apache.tuscany.core.util.MethodHashMap;
import org.apache.tuscany.core.wire.InboundInvocationChainImpl;
import org.apache.tuscany.core.wire.InboundWireImpl;
import org.apache.tuscany.core.wire.InvokerInterceptor;
import org.apache.tuscany.core.wire.MessageChannelImpl;
import org.apache.tuscany.core.wire.OutboundInvocationChainImpl;
import org.apache.tuscany.core.wire.OutboundWireImpl;
import org.apache.tuscany.core.wire.jdk.JDKWireService;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;

/**
 * @version $$Rev$$ $$Date$$
 */
public final class MockFactory {

    private static final WireService WIRE_SERVICE = new JDKWireService();

    private MockFactory() {
    }

    /**
     * Creates a JavaAtomicComponent which returns the given instance
     */
    @SuppressWarnings("unchecked")
    public static <T> JavaAtomicComponent<T> createJavaComponent(T instance) {
        ScopeContainer scope = createMock(ScopeContainer.class);
        scope.getScope();
        expectLastCall().andReturn(Scope.MODULE);
        scope.getInstance(isA(JavaAtomicComponent.class));
        expectLastCall().andReturn(instance).anyTimes();
        replay(scope);
        PojoConfiguration configuration = new PojoConfiguration();
        configuration.setScopeContainer(scope);
        try {
            configuration.setInstanceFactory(new PojoObjectFactory(DummyImpl.class.getConstructor()));
        } catch (NoSuchMethodException e) {
            throw new AssertionError(e);
        }
        configuration.addServiceInterface(DummyImpl.class);
        configuration.setWireService(WIRE_SERVICE);
        return new JavaAtomicComponent(instance.getClass().getName(), configuration, null);
    }

    @SuppressWarnings("unchecked")
    public static <T> JavaAtomicComponent<T> createJavaComponent(String name, ScopeContainer scope, Class<T> clazz)
        throws NoSuchMethodException {
        PojoConfiguration configuration = new PojoConfiguration();
        configuration.setScopeContainer(scope);
        configuration.setInstanceFactory(new PojoObjectFactory(clazz.getConstructor()));
        configuration.addServiceInterface(clazz);
        configuration.setWireService(WIRE_SERVICE);
        return new JavaAtomicComponent(name, configuration, null);

    }

    @SuppressWarnings("unchecked")
    public static <T> JavaAtomicComponent<T> createJavaComponent(String name,
                                                                 ScopeContainer scope,
                                                                 Class<T> clazz,
                                                                 WorkScheduler scheduler)
        throws NoSuchMethodException {
        PojoConfiguration configuration = new PojoConfiguration();
        configuration.setScopeContainer(scope);
        configuration.setInstanceFactory(new PojoObjectFactory(clazz.getConstructor()));
        configuration.addServiceInterface(clazz);
        configuration.setWireService(WIRE_SERVICE);
        return new JavaAtomicComponent(name, configuration, null);

    }

    /**
     * Wires two contexts together where the reference interface is the same as target service
     *
     * @param sourceName
     * @param sourceClass
     * @param sourceScope
     * @param members
     * @param targetName
     * @param targetService
     * @param targetClass
     * @param targetScope
     * @return
     * @throws Exception
     */
    public static Map<String, AtomicComponent> createWiredComponents(String sourceName,
                                                                     Class<?> sourceClass,
                                                                     ScopeContainer sourceScope,
                                                                     Map<String, Member> members,
                                                                     String targetName,
                                                                     Class<?> targetService,
                                                                     Class<?> targetClass,
                                                                     ScopeContainer targetScope) throws Exception {
        return createWiredComponents(sourceName, sourceClass, targetService, sourceScope, members, targetName,
            targetService, targetClass, targetScope);

    }

    /**
     * Wires two contexts together where the reference interface may be different from the target service
     */
    public static Map<String, AtomicComponent> createWiredComponents(String sourceName, Class<?> sourceClass,
                                                                     Class<?> sourceReferenceClass,
                                                                     ScopeContainer sourceScope,
                                                                     Map<String, Member> members,
                                                                     String targetName,
                                                                     Class<?> targetService,
                                                                     Class<?> targetClass,
                                                                     ScopeContainer targetScope) throws Exception {
        return createWiredComponents(sourceName, sourceClass, sourceReferenceClass, sourceScope, null, null, null,
            members, targetName, targetService,
            targetClass, targetScope, null, null, null);
    }

    @SuppressWarnings("unchecked")
    public static Map<String, AtomicComponent> createWiredComponents(String sourceName, Class<?> sourceClass,
                                                                     Class<?> sourceReferenceClass,
                                                                     ScopeContainer sourceScope,
                                                                     Interceptor sourceHeadInterceptor,
                                                                     MessageHandler sourceHeadRequestHandler,
                                                                     MessageHandler sourceHeadResponseHandler,
                                                                     Map<String, Member> members,
                                                                     String targetName, Class<?> targetService,
                                                                     Class<?> targetClass,
                                                                     ScopeContainer targetScope,
                                                                     Interceptor targetHeadInterceptor,
                                                                     MessageHandler targetRequestHeadHandler,
                                                                     MessageHandler targetResponseHeadHandler)
        throws Exception {
        JavaAtomicComponent targetContext =
            createJavaComponent(targetName, targetScope, targetClass);
        InboundWire inboundWire = createServiceWire(targetService.getName().substring(
            targetService.getName().lastIndexOf('.') + 1), targetService, targetHeadInterceptor,
            targetRequestHeadHandler, targetResponseHeadHandler);
        targetContext.addInboundWire(inboundWire);

        PojoConfiguration configuration = new PojoConfiguration();
        configuration.setScopeContainer(sourceScope);
        configuration.setInstanceFactory(new PojoObjectFactory(sourceClass.getConstructor()));
        configuration.addServiceInterface(sourceClass);
        configuration.setWireService(WIRE_SERVICE);
        for (Map.Entry<String, Member> entry : members.entrySet()) {
            configuration.addReferenceSite(entry.getKey(), entry.getValue());
        }
        JavaAtomicComponent sourceContext = new JavaAtomicComponent(sourceName, configuration, null);
        OutboundWire outboundWire = createReferenceWire(targetName, sourceReferenceClass, sourceHeadInterceptor,
            sourceHeadRequestHandler, sourceHeadResponseHandler);
        sourceContext.addOutboundWire(outboundWire);
        targetScope.register(targetContext);
        sourceScope.register(sourceContext);
        connect(outboundWire, inboundWire, targetContext, false);
        Map<String, AtomicComponent> contexts = new HashMap<String, AtomicComponent>();
        contexts.put(sourceName, sourceContext);
        contexts.put(targetName, targetContext);
        return contexts;
    }


    /**
     * Wires two contexts using a multiplicity reference
     *
     * @param sourceName
     * @param sourceClass
     * @param sourceReferenceClass
     * @param sourceScope
     * @param targetName
     * @param targetService
     * @param targetClass
     * @param members
     * @param targetScope
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public static Map<String, AtomicComponent> createWiredMultiplicity(String sourceName, Class<?> sourceClass,
                                                                       Class<?> sourceReferenceClass,
                                                                       ScopeContainer sourceScope,
                                                                       String targetName, Class<?> targetService,
                                                                       Class<?> targetClass,
                                                                       Map<String, Member> members,
                                                                       ScopeContainer targetScope) throws Exception {
        JavaAtomicComponent targetContext =
            createJavaComponent(targetName, targetScope, targetClass);
        InboundWire inboundWire = createServiceWire(targetService.getName().substring(
            targetService.getName().lastIndexOf('.') + 1), targetService, null, null, null);
        targetContext.addInboundWire(inboundWire);

        PojoConfiguration configuration = new PojoConfiguration();
        configuration.setScopeContainer(sourceScope);
        configuration.setInstanceFactory(new PojoObjectFactory(sourceClass.getConstructor()));
        configuration.addServiceInterface(sourceClass);
        configuration.setWireService(WIRE_SERVICE);
        for (Map.Entry<String, Member> entry : members.entrySet()) {
            configuration.addReferenceSite(entry.getKey(), entry.getValue());
        }
        JavaAtomicComponent sourceContext = new JavaAtomicComponent(sourceName, configuration, null);
        OutboundWire outboundWire = createReferenceWire(targetName, sourceReferenceClass, null, null, null);
        List<OutboundWire> factories = new ArrayList<OutboundWire>();
        factories.add(outboundWire);
        sourceContext.addOutboundWires(sourceReferenceClass, factories);
        targetScope.register(targetContext);
        sourceScope.register(sourceContext);
        connect(outboundWire, inboundWire, targetContext, false);
        Map<String, AtomicComponent> contexts = new HashMap<String, AtomicComponent>();
        contexts.put(sourceName, sourceContext);
        contexts.put(targetName, targetContext);
        return contexts;
    }

    public static <T> InboundWire<T> createTargetWire(String serviceName, Class<T> interfaze) {
        return createServiceWire(serviceName, interfaze, null, null, null);
    }


    public static <T> InboundWire<T> createServiceWire(String serviceName, Class<T> interfaze,
                                                       Interceptor headInterceptor,
                                                       MessageHandler headRequestHandler,
                                                       MessageHandler headResponseHandler) {
        InboundWire<T> wire = new InboundWireImpl<T>();
        JavaServiceContract contract = new JavaServiceContract(interfaze);
        wire.setServiceContract(contract);
        wire.setServiceName(serviceName);
        wire.addInvocationChains(
            createInboundChains(interfaze, headInterceptor, headRequestHandler, headResponseHandler));
        return wire;
    }

    public static <T> OutboundWire<T> createReferenceWire(String refName, Class<T> interfaze,
                                                          Interceptor headInterceptor,
                                                          MessageHandler headRequestHandler,
                                                          MessageHandler headResponseHandler) {

        OutboundWire<T> wire = new OutboundWireImpl<T>();
        wire.setReferenceName(refName);
        wire.addInvocationChains(
            createOutboundChains(interfaze, headInterceptor, headRequestHandler, headResponseHandler));
        JavaServiceContract contract = new JavaServiceContract(interfaze);
        wire.setServiceContract(contract);
        return wire;
    }

    public static <T> OutboundWire<T> createReferenceWire(String refName, Class<T> interfaze) {
        OutboundWire<T> wire = new OutboundWireImpl<T>();
        wire.setReferenceName(refName);
        wire.addInvocationChains(createOutboundChains(interfaze));
        JavaServiceContract contract = new JavaServiceContract(interfaze);
        wire.setServiceContract(contract);
        return wire;
    }


    /**
     * @param outboundWire
     * @param inboundWire
     * @param targetContext
     * @param cacheable
     * @throws Exception
     */
    public static void connect(OutboundWire<?> outboundWire,
                               InboundWire<?> inboundWire,
                               JavaAtomicComponent targetContext,
                               boolean cacheable) throws Exception {
        if (inboundWire != null) {
            // if null, the target side has no interceptors or handlers
            Map<Method, InboundInvocationChain> targetInvocationConfigs = inboundWire.getInvocationChains();
            for (OutboundInvocationChain outboundInvocationConfig : outboundWire.getInvocationChains().values()) {
                // match wire chains
                InboundInvocationChain inboundInvocationConfig =
                    targetInvocationConfigs.get(outboundInvocationConfig.getMethod());
                if (inboundInvocationConfig == null) {
                    BuilderConfigException e =
                        new BuilderConfigException("Incompatible source and target interface types for reference");
                    e.setIdentifier(outboundWire.getReferenceName());
                    throw e;
                }
                // if handler is configured, add that
                if (inboundInvocationConfig.getRequestHandlers() != null) {
                    outboundInvocationConfig.setTargetRequestChannel(new MessageChannelImpl(inboundInvocationConfig
                        .getRequestHandlers()));
                    outboundInvocationConfig.setTargetResponseChannel(new MessageChannelImpl(inboundInvocationConfig
                        .getResponseHandlers()));
                } else {
                    // no handlers, just connect interceptors
                    if (inboundInvocationConfig.getHeadInterceptor() == null) {
                        BuilderConfigException e =
                            new BuilderConfigException("No target handler or interceptor for operation");
                        e.setIdentifier(inboundInvocationConfig.getMethod().getName());
                        throw e;
                    }
                    if (!(outboundInvocationConfig.getTailInterceptor() instanceof InvokerInterceptor
                        && inboundInvocationConfig.getHeadInterceptor() instanceof InvokerInterceptor)) {
                        // check that we do not have the case where the only interceptors are invokers since we just
                        // need one
                        outboundInvocationConfig.setTargetInterceptor(inboundInvocationConfig.getHeadInterceptor());
                    }
                }
            }

            for (OutboundInvocationChain outboundInvocationConfig : outboundWire.getInvocationChains()
                .values()) {
                //FIXME should use target method, not outboundInvocationConfig.getMethod()
                TargetInvoker invoker = new JavaTargetInvoker(outboundInvocationConfig.getMethod(), targetContext);
                invoker.setCacheable(cacheable);
                outboundInvocationConfig.setTargetInvoker(invoker);
            }
        }
    }

    private static Map<Method, OutboundInvocationChain> createOutboundChains(Class<?> interfaze) {
        return createOutboundChains(interfaze, null, null, null);
    }

    private static Map<Method, OutboundInvocationChain> createOutboundChains(Class<?> interfaze,
                                                                             Interceptor headInterceptor,
                                                                             MessageHandler headRequestHandler,
                                                                             MessageHandler headResponseHandler) {
        Map<Method, OutboundInvocationChain> invocations = new HashMap<Method, OutboundInvocationChain>();
        Method[] methods = interfaze.getMethods();
        for (Method method : methods) {
            OutboundInvocationChain chain = new OutboundInvocationChainImpl(method);
            if (headInterceptor != null) {
                chain.addInterceptor(headInterceptor);
            }
            if (headRequestHandler != null) {
                chain.addRequestHandler(headRequestHandler);
            }
            if (headResponseHandler != null) {
                chain.addRequestHandler(headResponseHandler);
            }
            invocations.put(method, chain);
        }
        return invocations;
    }

    private static Map<Method, InboundInvocationChain> createInboundChains(Class<?> interfaze,
                                                                           Interceptor headInterceptor,
                                                                           MessageHandler headRequestHandler,
                                                                           MessageHandler headResponseHandler) {
        Map<Method, InboundInvocationChain> invocations = new MethodHashMap<InboundInvocationChain>();
        Method[] methods = interfaze.getMethods();
        for (Method method : methods) {
            InboundInvocationChain chain = new InboundInvocationChainImpl(method);
            if (headInterceptor != null) {
                chain.addInterceptor(headInterceptor);
            }
            if (headRequestHandler != null) {
                chain.addRequestHandler(headRequestHandler);
            }
            if (headResponseHandler != null) {
                chain.addRequestHandler(headResponseHandler);
            }
            // add tail interceptor
            chain.addInterceptor(new InvokerInterceptor());
            invocations.put(method, chain);
        }
        return invocations;
    }

    private static class DummyImpl {
        public DummyImpl() {
        }
    }

}
