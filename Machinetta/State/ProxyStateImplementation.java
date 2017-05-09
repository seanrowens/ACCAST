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
 * ProxyStateImplementation.java
 *
 * Created on 4 July 2002, 00:16
 */

package Machinetta.State;

import Machinetta.State.BeliefType.*;
import Machinetta.State.BeliefID;

//import Machinetta.Communication.ProxyID;

/**
 * Super class of all implementations of ProxyState
 *
 * @author  scerri
 */
public abstract class ProxyStateImplementation {
    
    /** Creates a new instance of ProxyStateImplementation */
    public ProxyStateImplementation() {}
    
    /** Returns the belief with the id supplied */
    abstract public Belief getBelief(final BeliefID id);
    
    /** Returns all of the beliefs in the current proxy state
     *  @return Array of all current beliefs
     */
    abstract public BeliefID [] getAllBeliefs();
    
    /** Adds a belief to the proxy state
     *
     * If there is already a belief with the same id as the belief to be added
     * it should be removed.
     */
    abstract public void addBelief(final Belief b);
    
    /** Prints out the current contents of the proxy state */
    abstract public void printState();
    
    /** Gets an array of all RAP beliefs
     *
     * If implementation has a more efficient implementation then it
     * should override it.
     */
    public RAPBelief[] getAllRAPBeliefs() {
        BeliefID [] allBeliefs = getAllBeliefs();
        int RAPCount = 0;
        for (int i = 0; i < allBeliefs.length; i++) {
	    if(null != allBeliefs[i]) {
		Belief b = getBelief(allBeliefs[i]);
		if(b != null) 
		    if (b instanceof RAPBelief)
			RAPCount++;
	    }
        }
        RAPBelief [] raps = new RAPBelief[RAPCount];
        int rapIndex =  0;
        for (int i = 0; i < allBeliefs.length; i++) {
	    if(null != allBeliefs[i]) {
		Belief b = getBelief(allBeliefs[i]);
		if(b != null) 
		    if (b instanceof RAPBelief)
			raps[rapIndex++] = (RAPBelief)b;
	    }
        }
        return raps;
    }
    
    /** Gets the RAPBelief corresponding to this proxyID */
    abstract public RAPBelief getRAPBelief(final ProxyID id);
    
    /** Gets the belief representing self */
    abstract public RAPBelief getSelf();
    
    /** Removes a belief from state */
    abstract public void removeBelief (BeliefID bid);
    
    abstract public void removeAll();
}
