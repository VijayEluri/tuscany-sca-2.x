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

package org.apache.tuscany.sca.node.impl;


import junit.framework.Assert;

import org.apache.tuscany.sca.domain.SCADomain;
import org.apache.tuscany.sca.node.impl.SCANodeImpl;
import org.apache.tuscany.sca.node.impl.SCANodeUtil;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import calculator.CalculatorService;

/**
 * Runs a distributed domain in a single VM by using and in memory 
 * implementation of the distributed domain
 */
public class InMemoryTestCase {
    
    private static String DEFAULT_DOMAIN_URI = "http://localhost:8877";

    private static SCADomain registry;
    private static SCADomain domainNodeA;
    private static SCADomain domainNodeB;
    private static SCADomain domainNodeC;
    private static CalculatorService calculatorServiceA;
    private static CalculatorService calculatorServiceB;

    @BeforeClass
    public static void init() throws Exception {
             
        try {
            System.out.println("Setting up domain registry");
            
            registry = SCADomain.newInstance("domain.composite");
            
            System.out.println("Setting up calculator ");
                  
            // Create the domain representation
            domainNodeA = SCADomain.newInstance(DEFAULT_DOMAIN_URI, "nodeA", null, "nodeA/Calculator.composite");
            
            // Create the domain representation
            domainNodeB = SCADomain.newInstance(DEFAULT_DOMAIN_URI, "nodeB", null, "nodeB/Calculator.composite");

            // create the node that runs the 
            // subtract component 
            domainNodeC = SCADomain.newInstance(DEFAULT_DOMAIN_URI, "nodeC", null, "nodeC/Calculator.composite");
    
            // get a reference to the calculator service from domainA
            // which will be running this component
            calculatorServiceA = domainNodeA.getService(CalculatorService.class, "CalculatorServiceComponent");
            calculatorServiceB = domainNodeB.getService(CalculatorService.class, "CalculatorServiceComponent");
            
        } catch(Exception ex){
            System.err.println(ex.toString());
        }  
        
   }

    @AfterClass
    public static void destroy() throws Exception {
        // stop the nodes and hence the domains they contain        
        domainNodeA.close();
        domainNodeB.close();    
        domainNodeC.close();
    }

    @Test
    public void testCalculator() throws Exception {       
        
        // Calculate
        Assert.assertEquals(calculatorServiceA.add(3, 2), 5.0);
        Assert.assertEquals(calculatorServiceA.subtract(3, 2), 1.0);
        Assert.assertEquals(calculatorServiceA.multiply(3, 2), 6.0);
        Assert.assertEquals(calculatorServiceA.divide(3, 2), 1.5);
        Assert.assertEquals(calculatorServiceB.add(3, 2), 5.0);
        Assert.assertEquals(calculatorServiceB.subtract(3, 2), 1.0);
        Assert.assertEquals(calculatorServiceB.multiply(3, 2), 6.0);
        Assert.assertEquals(calculatorServiceB.divide(3, 2), 1.5);
        
    }
}
