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
 * DecisionMakingRole.java
 *
 * Created on May 28, 2004, 4:42 PM
 */

package Machinetta.State.BeliefType.TeamBelief;

import Machinetta.State.*;

import java.util.*;

/**
 *
 * @author  pscerri
 */
public class DecisionMakingRole extends RoleBelief {
    
    public Vector decisionOptions = null;
    public Hashtable decisionInfo = null;
    Machinetta.State.BeliefID def = null;
    String decision = null;
    
    /** Creates a new instance of DecisionMakingRole - this here in case ever sent */
    public DecisionMakingRole() {
    }
    
    public DecisionMakingRole(Machinetta.State.BeliefID def, Vector decisionOptions, Hashtable decisionInfo) {
        Machinetta.Debugger.debug( 0,"META_REASON role created");
        roleName = "META_REASON";
        this.decisionInfo = decisionInfo;
        this.decisionOptions = decisionOptions;
        id = makeID(def);
        this.def = def;
    }
    
    /**
     * The reason this is not currently required is that decision making roles
     * are not currently included in plans, hence they are not instantiated from 
     * plans.  If they are ever added, implement this!
     */
    public RoleBelief instantiate(java.util.Hashtable params) {
        Machinetta.Debugger.debug( 5,"Unimplemented instantiate called in DecisionMakingRoleBelief");
        return null;
    }
    
    public String getDecision() { return decision; }
    public void setDecision(String dec) { this.decision = dec; }
    
    public BeliefID makeID() { return id; }
    
    /** The belief id is new BeliefNameID("MetaAgent:"+agent.uniqueID) with the agent being the agent that created this. */
    static public BeliefID makeID(Machinetta.State.BeliefID def) { return new BeliefNameID("MakeDecision"+def.toString()); }
    
    public String toString() { return "META_REASON: " + decisionInfo + " -> " + decisionOptions; }
    
    public static final long serialVersionUID = 1L;
}
