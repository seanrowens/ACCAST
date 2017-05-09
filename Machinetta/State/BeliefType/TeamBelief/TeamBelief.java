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
 * TeamBelief.java
 *
 * Created on August 23, 2002, 11:30 AM
 */

package Machinetta.State.BeliefType.TeamBelief;

import Machinetta.State.*;
import Machinetta.State.BeliefType.*;
import java.util.Vector;
import java.util.Hashtable;
import java.util.Iterator;
import org.w3c.dom.*;

/**
 *
 * @author  pynadath
 */
public class TeamBelief extends Belief {
        
    /** Team name */
    public String name = null;
    
    /** Team members */
    public Vector<ProxyID> members = null;
    
    /** Specific parameters of this instance */
    public Hashtable params = null;

    /** Creates a new instance of TeamBelief
     * @param id
     * Belief ID of this instance
     * @param name
     * String name of this team
     * @param members
     * Vector of IDs of all team members (including this proxy)
     * @param params
     * Hashtable of any additional parameters to specialize this instance
     */
    public TeamBelief(BeliefID id, String name, Vector<ProxyID> members, Hashtable params) {
        super(id);
        this.name = name;
        this.members = members;
        this.params = params;
    }
    /** Creates a new instance of TeamBelief
     * @param name
     * String name of this team
     * @param members
     * Vector of IDs of all team members (including this proxy)
     * @param params
     * Hashtable of any additional parameters to specialize this instance
     */
    public TeamBelief(String name, Vector<ProxyID> members, Hashtable params) {
        this(new BeliefNameID(name),name,members,params);
    }
    
    /** For auto XML */
    public TeamBelief() {
        params = new Hashtable();
    }
    
    public boolean isMember(ProxyID memberID) {
        // Machinetta.Debugger.debug("isMember called !!", 1, this);
        return members.contains(memberID);
    }
    
    /** Returns the number of team members
     * @return Integer count of team members
     */
    public int size() { return members.size(); }
    
    /** Returns an iterator that covers the team members
     *  @return Iterator over team members
     */
    public Iterator getMembers() { return members.iterator(); }
    
    /** Returns the approach to coordination preferred by this team (follows STEAM semantics)
     * @return A string label corresponding to a STEAM style of coordination (not really enforced)
     * @Deprecated
     */
    public String getApproach() {
        Machinetta.Debugger.debug(1, "getApproach is no longer available");
        return "unknown";
        /*
         String approach = (String)params.get("Approach");
        if (approach == null)
            // Assume cautious by default
            return STEAMPolicy.CAUTIOUS_APPROACH;
        else
            return approach;
        */
    }
    
    /** Access specific parameters of this plan instance
     * @param key
     * The attribute name of the desired parameter
     * @return
     * The object that is the value of the specified parameter
     */
    public  Object getParam(String key) { return params.get(key); }
        
    public String toString() {
        String result = super.toString() + "(" + name + ")";
        if (members != null) 
            result += members.toString();
        if (!params.isEmpty())
            result += params.toString();
        return result;
    }
    
    public BeliefID makeID() {
        return new Machinetta.State.BeliefNameID("Team"+name);
    }
    
    public static final long serialVersionUID = 1L;
}
