package org.apache.tuscany.container.groovy;

import java.util.ArrayList;
import java.util.List;

import org.apache.tuscany.spi.component.AtomicComponent;
import org.apache.tuscany.spi.component.ScopeContainer;
import org.apache.tuscany.spi.wire.InboundInvocationChain;
import org.apache.tuscany.spi.wire.InboundWire;
import org.apache.tuscany.spi.wire.Message;
import org.apache.tuscany.spi.wire.MessageImpl;
import org.apache.tuscany.spi.wire.OutboundInvocationChain;
import org.apache.tuscany.spi.wire.OutboundWire;
import org.apache.tuscany.spi.wire.TargetInvoker;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;
import junit.framework.TestCase;
import org.apache.tuscany.container.groovy.mock.Greeting;
import static org.apache.tuscany.test.ArtifactFactory.createInboundWire;
import static org.apache.tuscany.test.ArtifactFactory.createOutboundWire;
import static org.apache.tuscany.test.ArtifactFactory.createWireService;
import static org.apache.tuscany.test.ArtifactFactory.terminateWire;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.getCurrentArguments;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reportMatcher;
import static org.easymock.EasyMock.verify;
import org.easymock.IAnswer;
import org.easymock.IArgumentMatcher;

/**
 * @version $$Rev$$ $$Date$$
 */
public class WireTestCase extends TestCase {

    private static final String SCRIPT = "import org.apache.tuscany.container.groovy.mock.Greeting;"
        + "class Foo implements Greeting{"
        + "   Greeting wire;"
        + "   "
        + "   void setWire(Greeting ref){"
        + "       wire = ref;"
        + "   };"
        + "   "
        + "   String greet(String name){"
        + "       return wire.greet(name);  "
        + "   };"
        + "}";

    private static final String SCRIPT2 = "import org.apache.tuscany.container.groovy.mock.Greeting;"
        + "class Foo implements Greeting{"
        + "   public String greet(String name){"
        + "       return name;  "
        + "   }"
        + "}";

    private Class<? extends GroovyObject> implClass1;
    private Class<? extends GroovyObject> implClass2;
    private ScopeContainer scopeContainer;

    /**
     * Tests a basic invocation down a source wire
     */
    public void testReferenceWireInvocation() throws Exception {
        List<Class<?>> services = new ArrayList<Class<?>>();
        services.add(Greeting.class);
        GroovyConfiguration configuration = new GroovyConfiguration();
        configuration.setName("source");
        configuration.setGroovyClass(implClass1);
        configuration.setServices(services);
        configuration.setScopeContainer(scopeContainer);
        configuration.setWireService(createWireService());
        GroovyAtomicComponent<Greeting> component = new GroovyAtomicComponent<Greeting>(configuration);
        OutboundWire<?> wire = createOutboundWire("wire", Greeting.class);
        terminateWire(wire);

        TargetInvoker invoker = createMock(TargetInvoker.class);
        expect(invoker.isCacheable()).andReturn(false);
        Message response = new MessageImpl();
        response.setBody("foo");
        expect(invoker.invoke(eqMessage())).andReturn(response);
        replay(invoker);

        for (OutboundInvocationChain chain : wire.getInvocationChains().values()) {
            chain.setTargetInvoker(invoker);
        }
        component.addOutboundWire(wire);
        Greeting greeting = component.getServiceInstance();
        assertEquals("foo", greeting.greet("foo"));
        verify(invoker);
    }

    // todo this could be generalized and moved to test module
    public static Message eqMessage() {
        reportMatcher(new IArgumentMatcher() {
            public boolean matches(Object object) {
                if (!(object instanceof Message)) {
                    return false;
                }
                final Message msg = (Message) object;
                Object[] body = (Object[]) msg.getBody();
                return "foo".equals(body[0]);
            }

            public void appendTo(StringBuffer stringBuffer) {
            }
        });
        return null;
    }


    /**
     * Tests a basic invocation to a target
     */
    public void testTargetInvocation() throws Exception {
        List<Class<?>> services = new ArrayList<Class<?>>();
        services.add(Greeting.class);
        GroovyConfiguration configuration = new GroovyConfiguration();
        configuration.setName("source");
        configuration.setGroovyClass(implClass2);
        configuration.setServices(services);
        configuration.setScopeContainer(scopeContainer);
        configuration.setWireService(createWireService());
        GroovyAtomicComponent<Greeting> component = new GroovyAtomicComponent<Greeting>(configuration);
        TargetInvoker invoker =
            component.createTargetInvoker("greeting", Greeting.class.getMethod("greet", String.class));
        assertEquals("foo", invoker.invokeTarget(new String[]{"foo"}));
    }


    /**
     * Tests a basic invocation down a target wire
     */
    public void testTargetWireInvocation() throws Exception {
        List<Class<?>> services = new ArrayList<Class<?>>();
        services.add(Greeting.class);
        GroovyConfiguration configuration = new GroovyConfiguration();
        configuration.setName("source");
        configuration.setGroovyClass(implClass2);
        configuration.setServices(services);
        configuration.setScopeContainer(scopeContainer);
        configuration.setWireService(createWireService());
        GroovyAtomicComponent<Greeting> component = new GroovyAtomicComponent<Greeting>(configuration);
        InboundWire<?> wire = createInboundWire("Greeting", Greeting.class);
        terminateWire(wire);
        for (InboundInvocationChain chain : wire.getInvocationChains().values()) {
            chain.setTargetInvoker(component.createTargetInvoker("Greeting", chain.getMethod()));
        }
        component.addInboundWire(wire);
        Greeting greeting = (Greeting) component.getServiceInstance("Greeting");
        assertEquals("foo", greeting.greet("foo"));
    }

    protected void setUp() throws Exception {
        super.setUp();
        GroovyClassLoader cl = new GroovyClassLoader(getClass().getClassLoader());
        implClass1 = cl.parseClass(SCRIPT);
        implClass2 = cl.parseClass(SCRIPT2);
        scopeContainer = createMock(ScopeContainer.class);
        expect(scopeContainer.getInstance(isA(AtomicComponent.class))).andStubAnswer(new IAnswer() {
            public Object answer() throws Throwable {
                return ((AtomicComponent) getCurrentArguments()[0]).createInstance();
            }
        });
        replay(scopeContainer);
    }
}
