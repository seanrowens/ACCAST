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
 * HashtableImplementation.java
 *
 * Created on 4 July 2002, 00:19
 */
package Machinetta.State;

import Machinetta.State.BeliefType.*;
import Machinetta.State.BeliefID;
import Machinetta.State.BeliefNameID;

import java.util.Hashtable;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author  scerri
 */
public class HashtableImplementation extends ProxyStateImplementation {

    /** Creates a new instance of HashtableImplementation */
    public HashtableImplementation() {
    }

    /** Adds a belief to the proxy state  
     *
     * BeliefID is used as key 
     * Also add RAPBeliefs with ProxyID so they can be easily accessed
     */
    public void addBelief(Belief b) {

        table.put(b.getID(), b);
        if (b instanceof RAPBelief) {
            table.put(new BeliefNameID(((RAPBelief) b).getProxyID().toString()), b);
            if (((RAPBelief) b).isSelf()) {
                self = (RAPBelief) b;
            }
        }

        if (table.size() >= warnLimit) {
            (new Thread() {

                public void run() {
                    Machinetta.Debugger.debug(3, "WARNING: state contains over " + warnLimit + " beliefs: " + table.size());
                    warnLimit += WARN_LIMIT_INC;
                    
                    Hashtable<Class,Integer> types = new Hashtable<Class,Integer>();
                    for (Belief b: table.values()) {
                        Integer CI = types.get(b.getClass());
                        if (CI == null) {
                            types.put(b.getClass(), 1);
                        } else {
                            types.put(b.getClass(), CI+1);
                        }
                    }
                    StringBuffer sb = new StringBuffer();
                    for (Class c: types.keySet()) {
                        sb.append(c + ": " + types.get(c) + "; ");
                    }
                    sb.deleteCharAt(sb.length()-1);
                    Machinetta.Debugger.debug(5, "State makeup: " + sb);
                }
                
            }).start();
        }
    }

    /** Returns the belief with the id supplied  */
    public Belief getBelief(BeliefID id) {
        return (Belief) table.get(id);
    }

    /** Returns all of the beliefs in the current proxy state
     *  @return Array of all current beliefs
     */
    public BeliefID[] getAllBeliefs() {
        if (table.isEmpty()) {
            return null;
        }
        return table.keySet().toArray(new BeliefID[table.size()]);
    }

    /** Prints out the current contents of the proxy state  */
    public void printState() {
        System.out.println("PROXY STATE START");
        for (Belief b : table.values()) {
            System.out.println(b);
        }
        System.out.println("PROXY STATE END");
    }

    /** Gets the RAPBelief corresponding to this proxyID  */
    public RAPBelief getRAPBelief(final ProxyID id) {
        return (RAPBelief) table.get(new BeliefNameID(id.toString()));
    }

    /** Gets the belief representing self  */
    public RAPBelief getSelf() {
        return self;
    }

    public void removeBelief(BeliefID bid) {
        if (null == table.remove(bid)) {
            Machinetta.Debugger.debug(3, "Attempted to remove non-existent belief: " + bid);
        }
    }

    public void removeAll() {
        table.clear();
    }
    /** Stores the beliefs */
    private ConcurrentHashMap<BeliefID, Belief> table = new ConcurrentHashMap<BeliefID, Belief>();
    /** Stores the RAPBelief for self */
    private RAPBelief self = null;
    int warnLimit = WARN_LIMIT_START;
    private final static int WARN_LIMIT_START = 1000;
    private final static int WARN_LIMIT_INC = 50;
}
