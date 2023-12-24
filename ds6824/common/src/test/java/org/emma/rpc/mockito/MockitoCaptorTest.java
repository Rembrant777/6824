package org.emma.rpc.mockito;

import org.emma.rpc.utils.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


/**
 * In this class show how to use the captor in Mockito
 */
public class MockitoCaptorTest {
    private static final Logger LOG = LoggerFactory.getLogger(MockitoCaptorTest.class);

    /**
     * MyDependency is a simple mock class used as a dependency
     */
    static class MyDependencyClass {
        public String handle(String input) {
            LOG.info("#handle recv input non-blank status {}", StringUtils.isNotEmpty(input));
            String ret = UUID.randomUUID().toString();
            LOG.info("#handle ret {}", ret);
            return ret;
        }
    }

    /**
     * MyClass is referring to the class that to be test via the Junit4.
     */
    static class MyClass {
        private MyDependencyClass dependencyClass;

        public MyClass(MyDependencyClass dependencyClass) {
            this.dependencyClass = dependencyClass;
        }

        public void executeOperation(String input) {
            this.dependencyClass.handle(input);
        }
    }


    /**
     * And the SelfContainedTestClass is referring to the class that
     * contains the setup, test method, and the main method for running the test.
     */
    static class SelfContainedTestClass {
        private MyDependencyClass depClazz;
        private MyClass myClazz;

        // here is the scenario for @Captor to use
        private ArgumentCaptor<String> argumentCaptor;

        @Before
        public void setup() {
            // here we execute the initialize and corresponding operations
            depClazz = mock(MyDependencyClass.class);
            myClazz = new MyClass(depClazz);
            argumentCaptor = ArgumentCaptor.forClass(String.class);
        }

        @Test
        public void executeOperation() {
            String input = "testInputContent";

            // act
            myClazz.executeOperation(input);

            // here we execute the assert and the capture
            verify(depClazz).handle(argumentCaptor.capture());

            // here we verify the capture value
            Assert.assertEquals(input, argumentCaptor.getValue());
        }
    }

    public static void main(String[] args) {
        SelfContainedTestClass testClazz = new SelfContainedTestClass();
        testClazz.setup();
        testClazz.executeOperation();
    }
}
