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

package org.apache.tuscany.contribution.impl;

import java.net.URI;

import org.apache.tuscany.contribution.ContributionImport;

/**
 * The representation of an import for the contribution
 * 
 * @version $Rev: 527398 $ $Date: 2007-04-10 23:43:31 -0700 (Tue, 10 Apr 2007) $
 */
public class ContributionImportImpl implements ContributionImport {
    private String namespace; // The namespace to be imported
    private URI location; // Optional location to hint the where it should be imported
    
    protected ContributionImportImpl() {
        
    }
    
    public URI getLocation() {
        return location;
    }

    public void setLocation(URI location) {
        this.location = location;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }
}
