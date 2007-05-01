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

package org.apache.tuscany.contribution;

import java.net.URI;

/**
 * 
 */
public interface ContributionFactory {
    
    /**
     * Create a deployedArtifact model object
     * @return
     */
    DeployedArtifact createDeplyedArtifact();
    
    /**
     * Create a deployedArtifact model object setting it's URI
     * @return
     */
    DeployedArtifact createDeplyedArtifact(URI uri);
    
    /**
     * Create a contribution model object
     * @return
     */
    Contribution createContribution();
    
    /**
     * Create a contribution model object setting it's URI
     * @param uri
     * @return
     */
    Contribution createContribution(URI uri);
    
    /**
     * Create a contribution import model
     * @return
     */
    ContributionImport createContributionImport();
}
