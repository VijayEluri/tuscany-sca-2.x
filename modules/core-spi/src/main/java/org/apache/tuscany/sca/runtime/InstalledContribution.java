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

package org.apache.tuscany.sca.runtime;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InstalledContribution implements Serializable {
    private static final long serialVersionUID = 1L;

    private String uri;
    private String url;
    private List<String> dependentContributionURIs = new ArrayList<String>();

    // the URIs of the deployable composites within the contribution
    private List<String> deployables = new ArrayList<String>();

    private List<String> exports = new ArrayList<String>();
    
    private String metaData;
    private boolean overwriteMetaData;
    
    // the URI and XML content of composites to include in the contribution
    private Map<String, String> additionalDeployables = new HashMap<String, String>();

    public String getURI() {
        return uri;
    }
    public void setURI(String uri) {
        this.uri = uri;
    }
    public String getURL() {
        return url;
    }
    public void setURL(String url) {
        this.url = url;
    }
    public List<String> getDeployables() {
        return deployables;
    }
    public List<String> getExports() {
        return exports;
    }
    public void setDeployables(List<String> deployables) {
        this.deployables = deployables;
    }
    public List<String> getDependentContributionURIs() {
        return dependentContributionURIs;
    }
    public String getMetaData() {
        return metaData;
    }
    public void setMetaData(String metaData) {
        this.metaData = metaData;
    }
    public boolean isOverwriteMetaData() {
        return overwriteMetaData;
    }
    public void setOverwriteMetaData(boolean overwriteMetaData) {
        this.overwriteMetaData = overwriteMetaData;
    }
    public Map<String, String> getAdditionalDeployables() {
        return additionalDeployables;
    }
}
