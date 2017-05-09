/*******************************************************************************
 * Copyright (C) 2017, Paul Scerri, Sean R Owens
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. Neither the name of the copyright holder nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
/*
 * CommImplementation.java
 *
 * Created on 4 July 2002, 13:22
 */

package Machinetta.Communication;

import Machinetta.Communication.Message;
import Machinetta.Proxy;
import Machinetta.State.BeliefType.ProxyID;

/**
 *
 * @author  scerri
 */
public abstract class CommImplementation {
    
    /** Creates a new instance of CommImplementation */
    public CommImplementation() {
    }
    
    /** Implement the sending of messages */
    public abstract void sendMessage(Message m);
    
    /** Implement the receiving of messages */
    public abstract Message [] recvMessages();
    
    /** Notify proxy of incoming message(s) */
    protected final void incomingMessage() { proxy.incomingCommunicationMessages(); }
    
    /** Allow communication object to register the proxy to receive events 
     *
     * @param proxy The main Proxy object
     */
    final public void registerProxy(Proxy proxy) {
        this.proxy = proxy;
    }
    
    /** Allow communication object to indicate id of this proxy 
     * 
     * @param id The id used by this proxy
     */
    public void setProxyID (ProxyID id) { this.id = id; }
    
    /** The ID of this proxy */
    protected static ProxyID id = null;
    
    /** The proxy object to notify about incoming messages */
    private Proxy proxy = null;
}
