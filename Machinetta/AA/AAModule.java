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
 * AAModule.java
 *
 * Created on 4 July 2002, 12:19
 */

package Machinetta.AA;

import Machinetta.Proxy;
import Machinetta.Configuration;
import Machinetta.RAPInterface.OutputMessages.OutputMessage;
import Machinetta.IntelligentModule;
import Machinetta.RAPInterface.InputMessages.InputMessage;
import Machinetta.State.ProxyState;
import Machinetta.State.BeliefID;

/**
 * Implements the AA reasoning for the proxy
 *
 * The AA manages the interaction between the DME and proxy.
 *
 * Implements singleton pattern.
 *
 * @author  scerri
 */
public class AAModule extends IntelligentModule {
    
    /** Creates a new instance of AAModule */
    public AAModule() {
        this(null);
    }
    
    /** Creates a new instance of AAModule */
    public AAModule(Proxy proxy) {
        super(proxy);
        if (!created) init();
    }
    
    /** Implements StateChangeListener
     * @param b List of beliefs that have changes
     */
    protected void proxyStateChanged(BeliefID[] b) {
        if (impl != null) {            
            impl.proxyStateChange(b);
        }
    }
    
    /** Called when message comes from RAP
     * @param msgs List of InputMessage objects from RAP
     */
    public void messagesFromRap(InputMessage [] msgs) {
        impl.messagesFromRAP(msgs);
        // When finished processing notify listeners
        ProxyState.notifyListeners();
    }
    
    /** Called with message to send to RAP
     * @param msg The OutputMessage to send
     */
    public void messageToRAP(OutputMessage msg) {
        proxy.outgoingRAPMessage(msg);
    }
    
    /** Creates implementation when required. 
     *
     * Modified 3/20/2003:
     * No longer is AA_IMPL_TYPE a string that is translated
     * by code into a class type, instead it is a string representing
     * the name of the class that should be used.  
     */
    private void init() {
        created = true;
        //String className = oldNameTranslate();
        String className = Configuration.AA_IMPLEMENTATION_TYPE;
        try {
            Class type = Class.forName(className);
            impl = (AAImplementation)type.newInstance();
            Machinetta.Debugger.debug( 1,"AAImplementation " + type + " created");
        } catch (ClassNotFoundException e) {
            Machinetta.Debugger.debug( 5,"Could not find AAImplementation : " + Configuration.AA_IMPLEMENTATION_TYPE);
        } catch (InstantiationException e2) {
            Machinetta.Debugger.debug( 5,"Could not instantiate : " + e2);
        } catch (IllegalAccessException e3) {
            Machinetta.Debugger.debug( 5,"Could not instantiate : " + e3);
        } catch (ClassCastException e4) {
            Machinetta.Debugger.debug( 5,"AAImplementation specified was not a AAImplementation");
        }

        if (impl == null) {
            Machinetta.Debugger.debug( 5,"No valid AAImplementation specified ... stopping");
            System.exit(-1);
        }
    }
    
    /** The object implementing the AA reasoning */
    private static AAImplementation impl = null;
    
    /** True if at least started creating AA Module */
    private static boolean created = false;
}
