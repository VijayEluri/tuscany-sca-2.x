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

package org.apache.tuscany.sca.itest.databindings.jaxb;

/**
 * The interface for GenericsServiceClient.
 * 
 * @version $Rev$ $Date$
 */
public interface GenericsServiceClient {
    Bean1<String> getTypeExplicitForward(Bean1<String> arg);

    <T> Bean1<T> getTypeUnboundForward(T[] anArray);
    
    <T extends Bean2> Bean1<T> getTypeExtendsForward(T[] anArray);

    <T extends Bean1<String>> Bean1<T> getRecursiveTypeBoundForward(T[] anArray);
    
    Bean1<?> getWildcardUnboundForward(Bean1<?> arg);
    
    Bean1<? super Bean3> getWildcardSuperForward(Bean1<? super Bean3> arg);
    
    Bean1<? extends Bean2> getWildcardExtendsForward(Bean1<? extends Bean2> arg);

    Bean2 getPolymorphicForward(Bean2 arg);
}
