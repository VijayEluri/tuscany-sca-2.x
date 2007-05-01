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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.tuscany.assembly.Composite;
import org.apache.tuscany.contribution.Contribution;
import org.apache.tuscany.contribution.ContributionImport;
import org.apache.tuscany.contribution.DeployedArtifact;

/**
 * The representation of a deployed contribution
 *
 * @version $Rev: 531146 $ $Date: 2007-04-21 22:40:50 -0700 (Sat, 21 Apr 2007) $
 */
public class ContributionImpl extends BaseArtifactImpl implements Contribution {
    protected List<String> exports = new ArrayList<String>();
    protected List<ContributionImport> imports = new ArrayList<ContributionImport>();
    protected List<Composite> deployables = new ArrayList<Composite>();
    
    /**
     * A list of artifacts in the contribution
     */
    protected Map<URI, DeployedArtifact> artifacts = new HashMap<URI, DeployedArtifact>();

    protected ContributionImpl() {
    }
    
    public List<String> getExports() {
        return exports;
    }

    public List<ContributionImport> getImports() {
        return imports;
    }

    public List<Composite> getDeployables() {
        return deployables;
    }

    public Map<URI, DeployedArtifact> getArtifacts() {
        return Collections.unmodifiableMap(artifacts);
    }
    
    public void addArtifact(DeployedArtifact artifact) {
        artifact.setContribution(this);
        artifacts.put(artifact.getUri(), artifact);
    }
    
    public DeployedArtifact getArtifact(URI uri) {
        return artifacts.get(uri);
    }
}
