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
 * RoleAgent.java
 *
 * Created on December 15, 2003, 8:49 AM
 */

package Machinetta.State.BeliefType.MAC;
import Machinetta.State.BeliefType.ProxyID;
import java.util.Hashtable;
import java.util.Vector;

/**
 * Belief representing a role agent
 *
 * (Does not have history, yet)
 *
 * @author  pscerri
 */
public class RoleAgentBelief extends MABelief {
    
    public Machinetta.State.BeliefType.TeamBelief.RoleBelief role = null;
    
    public Machinetta.State.BeliefID planID = null;
    
    public Machinetta.State.BeliefType.ProxyID planAgentRAP = null;
    
    public double conflictNumber = 0.0;
    
    public Machinetta.State.BeliefType.RAPBelief takingOver = null;
    
    public java.util.Vector<Integer> mergedIDs = null;
    public Vector<ProxyID> mergedProxies = null;

    public int noVisits = 0;
    
    public Vector postconditions = null;
    public Hashtable<String,Object> planParams = null;
    
    /** Creates a new instance of RoleAgent */
    public RoleAgentBelief() { }
    
    public RoleAgentBelief(Machinetta.Coordination.MAC.RoleAgent ra) {
        super(ra);
        role = ra.role;
        takingOver = ra.takingOver;
        planAgentRAP = ra.planAgentRAP;
        mergedIDs = ra.mergedIDs;
        mergedProxies = ra.mergedProxies;
        planID = ra.planID;
        noVisits = ra.noVisits;
        postconditions = ra.postconditions;
        planParams = ra.planParams;
    }
    
    public Machinetta.State.BeliefID makeID() {
        return new Machinetta.State.BeliefNameID("RoleAgent"+role.getID());
    }

    public String toString() { return "plan="+planID+" role="+role.toString()+" noVisits="+noVisits+" planAgentRAP="+planAgentRAP; }
    
    public static final long serialVersionUID = 1L;
}
