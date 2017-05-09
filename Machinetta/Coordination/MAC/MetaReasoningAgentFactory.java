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
 * MetaReasoningAgentFactory.java
 *
 * Created on May 26, 2004, 3:22 PM
 */

package Machinetta.Coordination.MAC;

import Machinetta.State.*;
import Machinetta.State.BeliefType.TeamBelief.Priority.*;
/**
 * Notice that while this is the factory for meta reasoning agents,
 * they can be created elsewhere, including in role agents.
 *
 * @author  pscerri
 */
public class MetaReasoningAgentFactory extends AgentFactory {
    
    /** Creates a new instance of MetaReasoningAgentFactory */
    public MetaReasoningAgentFactory() {
    }
    
    public void createAgent(Machinetta.State.BeliefType.MAC.MABelief bel) {
        MetaReasoningAgent ma = ((Machinetta.State.BeliefType.MAC.MetaReasoningBelief)bel).getAsAgent();
        Machinetta.Debugger.debug( 1,"Created MRA: " + ma);
        
        initAgent(ma);
    }
    
    /**
     * Currently monitors only for PriorityBeliefs
     * 
     */
    public void stateChange(Machinetta.State.BeliefID[] b) {
        
        for (BeliefID bid: b) {
            Machinetta.State.BeliefType.Belief bel = state.getBelief(bid);
            if (bel instanceof PriorityBelief && !((PriorityBelief)bel).isDetermined()) {
                if (!priorPriorityMRAs.containsKey(bel.getID())) {
                    Machinetta.Debugger.debug( 1,"MRAFactory created MRA for determining priority of " + ((PriorityBelief)bel).plan);
                    MetaReasoningAgent ma = new MetaReasoningAgent((PriorityBelief)bel);
                    priorPriorityMRAs.put(bel.getID(), null);
                }
                
            } else if (bel instanceof PriorityBelief) {
                Machinetta.Debugger.debug( 1,"Did not create MRA for " + bel + " bid: " + bid);
            }
        }
    }
    java.util.HashMap<BeliefID,Object> priorPriorityMRAs = new java.util.HashMap<BeliefID,Object>();
            
    private void initAgent(MetaReasoningAgent ma) {
        // Make sure agent is registered with MA platform
        Machinetta.Debugger.debug( 1,"Adding agent: " + ma.getDefiningBeliefs()[0]);
        Machinetta.Coordination.MACoordination.addAgent(ma);
        
        // Once created, either allow the agent to perform its TOC strategy or execute the decision
        if (ma.isExecuting()) {
            Machinetta.Debugger.debug( 1,"Created MRA to execute its decision");
            ma.executeDecision();
        } else {
            ma.executeStrategy();
        }
    }
}
