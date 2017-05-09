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
 * ConflictResolutionAgent.java
 *
 * Created on March 1, 2004, 5:44 PM
 */

package Machinetta.Coordination.MAC;

/**
 *
 * @author  pscerri
 */
public class ConflictResolutionAgent extends Agent {
    
    private Machinetta.State.BeliefType.TeamBelief.TeamPlanBelief plan;
    /** Explicitly store the planID as well as the plan, in case the plan is 
     * not known to a proxy (plan is not sent with agent, only id)
     */
    private Machinetta.State.BeliefID planID;
    
    private double conflictNo = 0.0;
    
    private int uniqueIDofConflicted = 0;
    
    // This is the location of the plan on who's behalf this agent is acting
    private Machinetta.State.BeliefType.ProxyID rap = null;
    
    /** Creates a new instance of ConflictResolutionAgent */
    public ConflictResolutionAgent(Machinetta.State.BeliefType.TeamBelief.TeamPlanBelief plan, double conflictNo, int uniqueIDofConflicted) {
        this.plan = plan;
        this.conflictNo = conflictNo;
        this.uniqueIDofConflicted = uniqueIDofConflicted;
        this.planID = plan.getID();
        rap = state.getSelf().getProxyID();
    }
    
    /** Arriving conflict resolution agent */
    public ConflictResolutionAgent(Machinetta.State.BeliefType.MAC.ConflictResolutionBelief cb) {
        super(cb);
        plan = (Machinetta.State.BeliefType.TeamBelief.TeamPlanBelief)state.getBelief(cb.planID);
        if (plan == null) {
            // This is probably not important, since this agent should be just passing through
            Machinetta.Debugger.debug( 1,"Plan not known for conflict resolution agent: " + cb.planID);
        }
        planID = cb.planID;
        conflictNo = cb.conflictNo;        
        uniqueIDofConflicted = cb.uniqueIDofConflicted;
        rap = cb.rap;         
        
        act();
    }
    
    /**
     * Agent tries to get to the plan agent, once it does, it 
     * informs it of the conflict and is done.
     */
    public void act () {        
        java.util.LinkedList agents = Machinetta.Coordination.MACoordination.getAgentsForPlan(uniqueIDofConflicted);
        PlanAgent pa = null;
        RoleAgent ra = null;
        AssociateInformAgent ai = null;
        for (java.util.ListIterator li = agents.listIterator(); li.hasNext(); ) {
            Agent a = (Agent)li.next();
            if (a instanceof PlanAgent) { pa = (PlanAgent)a; break; }
            else if (a instanceof RoleAgent) ra = (RoleAgent)a;
            else if (ai == null && a instanceof AssociateInformAgent) ai = (AssociateInformAgent)a;
        }
        // Depending on which agents were found, work out what to do next
        if (pa != null) {            
            // Need to inform it!
            pa.resolveConflict(this);
            // This agents service is complete
        } else if (ra != null) {
            Machinetta.Debugger.debug( 0,"Conflict resolution agent found RA: " + this);
            // RoleAgents know where PlanAgents are
            Machinetta.Coordination.MACoordination.moveAgent(this, ra.planAgentRAP);
        } else if (ai != null) {
            Machinetta.Debugger.debug( 0,"Conflict resolution agent found AI: " + this);
            // AssociateInformAgents know where to go for someone who knows where the PlanAgent is
            Machinetta.Coordination.MACoordination.moveAgent(this, ai.rap);
        } else {
            Machinetta.Debugger.debug( 3,"Problem, conflict resolution agent is lost: " + this);
        }
    }
    
    public void _merge(Agent a) {
        Machinetta.Debugger.debug( 1,"Unimplemented _merge in ConflictResolutionAgent");
    }
    
    public Machinetta.State.BeliefType.Belief getAgentAsBelief() {
        return new Machinetta.State.BeliefType.MAC.ConflictResolutionBelief(this);
    }
    
    public Machinetta.State.BeliefID[] getDefiningBeliefs() {
        Machinetta.State.BeliefID [] ids = new Machinetta.State.BeliefID[2];        
        ids[0] = planID;
        ids[1] = rap.getID();
        return ids;
    }
    
    public void stateChanged() {
    }
    
    public Machinetta.State.BeliefID getPlanID() { 
        if (plan != null) return plan.getID();
        else return planID;
    }
    public double getConflictNo () { return conflictNo; }
    public Machinetta.State.BeliefType.ProxyID getSendingProxy() { return rap; }
    public int getIDofConflicted () { return uniqueIDofConflicted; }
    
    public String toString() { return "CRA: " + getPlanID()  + ", id: " + uniqueID; }
    
    public static final long serialVersionUID = 1L;
}
