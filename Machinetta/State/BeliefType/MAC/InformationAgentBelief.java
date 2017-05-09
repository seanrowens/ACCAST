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
 * InformationAgentBelief.java
 *
 * Created on November 5, 2003, 12:33 PM
 */

package Machinetta.State.BeliefType.MAC;

import Machinetta.State.BeliefType.Belief;
import Machinetta.Coordination.MAC.InformationAgent;
import Machinetta.State.BeliefType.ProxyID;

/**
 *
 * @author  pscerri
 */
public class InformationAgentBelief extends MABelief {
    
    public Belief bel = null;
    public ProxyID toRAP = null;
    public Machinetta.State.BeliefType.TeamBelief.RoleBelief forRole = null;
    public int TTL = -1;
    
    /** Creates a new instance of InformationAgentBelief */
    public InformationAgentBelief() {
    }
    
    public InformationAgentBelief(InformationAgent agent) {
        super(agent);
        bel = agent.getBelief();
        toRAP = agent.getToRAP();
        forRole = agent.getForRole();
        this.TTL = agent.getTTL();
    }
    
    public String toString() { return "InformationAgent for : " + bel; }
    
    public static final long serialVersionUID = 1L;
}
