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

package org.apache.tuscany.sca.policy.xml;

import javax.xml.namespace.QName;

import org.apache.tuscany.sca.contribution.processor.StAXArtifactProcessor;
import org.apache.tuscany.sca.policy.IntentAttachPointTypeFactory;
import org.apache.tuscany.sca.policy.PolicyFactory;


/* 
 * Processor for handling xml models of ImplementationType meta data definitions
 */
public class ImplementationTypeProcessor extends IntentAttachPointTypeProcessor {

    public ImplementationTypeProcessor(PolicyFactory policyFactory, IntentAttachPointTypeFactory intentAttachPointTypeFactory, StAXArtifactProcessor<Object> extensionProcessor) {
        super(policyFactory, intentAttachPointTypeFactory, extensionProcessor);
    }

    public QName getArtifactType() {
        return IMPLEMENTATION_TYPE_QNAME;
    }
}
