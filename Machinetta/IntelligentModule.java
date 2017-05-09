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
 * IntelligentModule.java
 *
 * Created on 4 July 2002, 12:16
 */

package Machinetta;

import Machinetta.Proxy;
import Machinetta.State.BeliefID;
import Machinetta.State.StateChangeListener;
import Machinetta.State.ProxyState;

/**
 * Superclass of AA and Coordination modules
 *
 * @author  scerri
 */
public abstract class IntelligentModule implements StateChangeListener {
    
    /** Creates a new instance of IntelligentModule
     *
     * @param proxy Needed to provide access to the proxy so the
     * intelligent modules can pass messages around.
     */
    public IntelligentModule(Proxy proxy) {
        if (proxy != null) {
            this.proxy = proxy;
            proxyState.addChangeListener(this);
        }
    }
    
    /** Fix this - fix what? */
    public IntelligentModule() {}
    
    /** Implements state change listener
     * Once module finishes handling event, update is called if required. (Not finished.)
     *
     * @param b List of Beliefs that have changed
     */
    public void stateChanged(BeliefID [] b) {
        proxyStateChanged(b);
    }
    
    /** Implements StateChangeListener */
    protected abstract void proxyStateChanged(BeliefID [] b);
    
    /** Holder for proxy state */
    protected ProxyState proxyState = new ProxyState(); 
    
    /** Access to the proxy */
    static protected Proxy proxy = null;
}
