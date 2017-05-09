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
 * Communication.java
 *
 * Created on 4 July 2002, 13:13
 */

package Machinetta.Communication;

import Machinetta.Proxy;
import Machinetta.Configuration;
import Machinetta.State.BeliefType.ProxyID;

/**
 * Handles the communications
 *
 * Uses singleton pattern
 *
 * @author  scerri
 */
public class Communication {
    
    /** Creates a new instance of Communication */
    public Communication() {
        if (impl == null) {
            init();
        }
    }
    
    /** Used to send messages to other proxies
     * @param m Message to be sent
     *
     */
    public void sendMessage(Message m) { impl.sendMessage(m); }
    
    /** Receive an array of messages from other proxies
     * @return List of messages received since last called
     */
    public Message [] recvMessages() { return impl.recvMessages(); }
    
    /** Allows the proxy to register, so it can get information about incoming events
     * @param proxy The main Proxy object
     */
    public void registerForEvents(Proxy proxy) { impl.registerProxy(proxy); }
    
    /** Allows proxy to set the ID of this proxy
     * @param id The id to use as identifier for this proxy
     */
    public void setProxyID(ProxyID id) { impl.setProxyID(id); }
    
    /** Access to implementation of communication */
    private static CommImplementation impl = null;
    
    /** Creates singleton
     */
    private void init() {
        if (impl == null) {
            
            String className = Configuration.COMMS_IMPLEMENTATION_TYPE;
            
            try {
                Class type = Class.forName(className);
                impl = (CommImplementation)type.newInstance();
                Machinetta.Debugger.debug( 1,"CommImplementation " + type + " created");
            } catch (ClassNotFoundException e) {
                Machinetta.Debugger.debug( 5,"Could not find Comms : " + Configuration.COMMS_IMPLEMENTATION_TYPE);
            } catch (InstantiationException e2) {
                Machinetta.Debugger.debug( 5,"Could not instantiate : " + e2);
            } catch (IllegalAccessException e3) {
                Machinetta.Debugger.debug( 5,"Could not instantiate : " + e3);
            } catch (ClassCastException e4) {
                Machinetta.Debugger.debug( 5,"CommImplementation specified was not a CommImplementation");
            }
            
            if (impl == null) {
                Machinetta.Debugger.debug( 1,"Using default CommImplementation");
                impl = new DummyComms();
            }
        }
    }
    
}
