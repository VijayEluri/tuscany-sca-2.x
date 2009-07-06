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
package org.apache.tuscany.sca.assembly.impl;

import javax.xml.namespace.QName;

import org.apache.tuscany.sca.assembly.DistributedSCABinding;
import org.apache.tuscany.sca.assembly.SCABinding;

/**
 * The Distributed SCA binding wrapper for the SCA binding model object. This is currently
 * just used to locate the remote binding extension and pass the SCA binding to the remote
 * extension. It isn't used in the model itself
 *
 * @version $Rev: 564307 $ $Date: 2007-08-09 18:48:29 +0100 (Thu, 09 Aug 2007) $
 */
public class DistributedSCABindingImpl implements DistributedSCABinding {

    private SCABinding scaBinding;

    public SCABinding getSCABinding() {
        return scaBinding;
    }

    public void setSCABinding(SCABinding scaBinding) {
        this.scaBinding = scaBinding;
    }

    public String getURI() {
        return null;
    }

    public void setURI(String uri) {
    }

    public String getName() {
        return null;
    }

    public void setName(String name) {

    }

    public boolean isUnresolved() {
        return false;
    }

    public void setUnresolved(boolean unresolved) {

    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public QName getType() {
        return TYPE;
    }
}
