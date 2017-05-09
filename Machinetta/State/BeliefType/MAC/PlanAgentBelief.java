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
 * PlanAgentBelief.java
 *
 * Created on November 6, 2003, 12:00 PM
 */

package Machinetta.State.BeliefType.MAC;

/**
 *
 * @author  pscerri
 */
public class PlanAgentBelief extends MABelief {
    
    public Machinetta.State.BeliefType.TeamBelief.TeamPlanBelief plan;
    // public int uniqueID = 0;
    public boolean terminated = false;
    public double conflictNo = 0.0;
    
    /** Creates a new instance of PlanAgentBelief */
    public PlanAgentBelief() {
    }
    
    public PlanAgentBelief(Machinetta.Coordination.MAC.PlanAgent agent) {
        super(agent);
        plan = agent.getPlan();
        uniqueID = agent.uniqueID;
        conflictNo = agent.conflictNo;
        terminated = agent.terminated;
        id = new Machinetta.State.BeliefNameID("PA:"+plan.getID().toString());        
    }
    
    public static final long serialVersionUID = 1L;
}
