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
package org.apache.tuscany.spi.wire;

import java.net.URI;
import java.util.LinkedList;

import org.apache.tuscany.spi.component.WorkContext;

/**
 * The default implementation of a message flowed through a wire during an invocation
 *
 * @version $Rev $Date
 */
public class MessageImpl implements Message {
    private Object body;
    private TargetInvoker invoker;
    private LinkedList<URI> callbackUris;
    private LinkedList<Wire> callbackWires;
    private Object messageId;
    private Object correlationId;
    private boolean isFault;
    private short conversationSequence;
    private WorkContext workContext;

    public MessageImpl(WorkContext workContext, short conversationSequence, Object body) {
        this.workContext = workContext;
        this.conversationSequence = conversationSequence;
        this.body = body;
    }

    public MessageImpl() {
    }

    public Object getBody() {
        return body;
    }

    public void setBody(Object body) {
        this.isFault = false;
        this.body = body;
    }

    public WorkContext getWorkContext() {
        return workContext;
    }

    public void setWorkContext(WorkContext workContext) {
        this.workContext = workContext;
    }

    public short getConversationSequence() {
        return conversationSequence;
    }

    public void setConversationSequence(short conversationSequence) {
        this.conversationSequence = conversationSequence;
    }

    public void setTargetInvoker(TargetInvoker invoker) {
        this.invoker = invoker;
    }

    public TargetInvoker getTargetInvoker() {
        return invoker;
    }

    public void pushCallbackUri(URI uri) {
        if (callbackUris == null) {
            callbackUris = new LinkedList<URI>();
        }
        callbackUris.add(uri);
    }

    public LinkedList<URI> getCallbackUris() {
        return callbackUris;
    }

    public void setCallbackUris(LinkedList<URI> callbackRoutingChain) {
        this.callbackUris = callbackRoutingChain;
    }

    public void pushCallbackWire(Wire wire) {
        if (callbackWires == null) {
            callbackWires = new LinkedList<Wire>();
        }
        callbackWires.add(wire);
    }

    public LinkedList<Wire> getCallbackWires() {
        return callbackWires;
    }

    public void setCallbackWires(LinkedList<Wire> wires) {
        this.callbackWires = wires;
    }

    public Object getMessageId() {
        return messageId;
    }

    public void setMessageId(Object messageId) {
        this.messageId = messageId;
    }

    public Object getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(Object correlationId) {
        this.correlationId = correlationId;
    }

    public boolean isFault() {
        return isFault;
    }

    public void setBodyWithFault(Object fault) {
        this.isFault = true;
        this.body = fault;
    }

    public TargetInvoker getInvoker() {
        return invoker;
    }

    public void setInvoker(TargetInvoker invoker) {
        this.invoker = invoker;
    }
}
