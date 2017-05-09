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
 * Associates.java
 *
 * Created on February 25, 2004, 3:01 PM
 */

package Machinetta.State.BeliefType.TeamBelief;

import Machinetta.State.BeliefType.*;

import java.util.Vector;

/**
 * @todo Use this ... 
 *
 * @author  pscerri
 */
public class Associates extends Belief {
    
    /** The associates of this RAP */
    public Vector<ProxyID> associates = new Vector<ProxyID>();
    
    /** Creates a new instance of Associates */
    public Associates() {
        id = bid;
    }
    
    /** Add a RAP to the associates of this proxy */
    public void addAssociate(RAPBelief associate) {
        associates.addElement(associate.getProxyID());
    }
    
    public void addAssociate(ProxyID id) {
        associates.addElement(id);
    }
    
    public boolean isAssociate(ProxyID id) {
        for(ProxyID p: associates) {
            if (p.equals(id)) return true;
        }
        return false;
    }
    
    /** Gets an enumeration of all associates */
    public java.util.Enumeration getAssociates() { return associates.elements(); }
    
    /** Sets up associates network from a linked list of ProxyIDs */
    public void create (java.util.LinkedList def) {
        Machinetta.State.ProxyState state = new Machinetta.State.ProxyState();
        for (java.util.ListIterator li = def.listIterator(); li.hasNext(); ) {
            NamedProxyID id = (NamedProxyID)li.next();            
            associates.addElement(id);
        }        
    }
    
    /** Should be only one "Associates" per RAP, hence simple BeliefID */
    public Machinetta.State.BeliefID makeID() { return bid; }        
    
    public static Machinetta.State.BeliefID getConstantID() { return bid; }
    
    static private Machinetta.State.BeliefNameID bid = new Machinetta.State.BeliefNameID("Associates");
    
    public String toString() { return "Associates: " + associates; }
    
    public static final long serialVersionUID = 1L;
}
