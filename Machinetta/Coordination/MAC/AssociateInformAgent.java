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
 * AssociateInformAgent.java
 *
 * Created on February 25, 2004, 4:34 PM
 */

package Machinetta.Coordination.MAC;

import Machinetta.Debugger;
import Machinetta.Coordination.MACoordination;
import Machinetta.State.BeliefID;
import Machinetta.State.BeliefType.ProxyID;
import Machinetta.State.BeliefType.MAC.AssociateInformBelief;
import Machinetta.State.BeliefType.TeamBelief.TeamPlanBelief;

/**
 * This type of agent is used to inform associates of a proxy
 * about another proxies role in a particular plan
 *
 * @author  pscerri
 */
public class AssociateInformAgent extends Agent {
    
    /** The plan which this agent is informing about */
    public TeamPlanBelief plan = null;
    
    /** The RAP on who's behalf this agent is informing */
    public ProxyID rap = null;
    
    /** If not null, indicates that a conflict has been detected
     * the id gives the location of the RAP where the conflict was
     * detected, not necessarily the location of the Plan Agent
     */
    public ProxyID conflictDetected = null;
    
    /** Unique id of plan against which this conflicts */
    public int uniqueIDofConflicted = 0;
    
    /** Creates a new instance of AssociateInformAgent */
    public AssociateInformAgent() {
    }
    
    /** Assumes that it is being created for this RAP */
    public AssociateInformAgent(TeamPlanBelief plan) {
        this.plan = plan;
        rap = state.getSelf().getProxyID();
    }
    
    /** This constructor is only called when belief arrives from another agent
     */
    public AssociateInformAgent(AssociateInformBelief ab) {
        super(ab);
        this.plan = ab.tpb;
        this.rap = ab.RAPID;
        this.conflictDetected = ab.conflictDetected;
        uniqueIDofConflicted = ab.uniqueIDofConflicted;
        act();
    }
    
    /** This is the "brains" of the agent, when it arrives at a proxy. <br>
     *  One of three things happens: <br>
     * 1. if a conflict has been detected, work out how to get back to the plan agent
     * 2. if there is a conflicting inform agent here, the conflict resolution process starts <br>
     * 3. otherwise, the agent just goes into the state <br>
     */
    private void act() {
        if (conflictDetected != null) {
            // A moving agent has arrived here, let it keep going back to Plan Agent
            moveToPlanAgent();
        } else {
            boolean merged = MACoordination.addAgent(this);
            // This may not be exactly correct ...
            if (merged) return;
            
            java.util.LinkedList conflicts = MACoordination.getConflicting(this);
            if (conflicts != null) {
                
                Debugger.debug( 1,"Conflict(s) detected for " + plan.getID() + " : " + conflicts);
                // Leaves essentially a "clone" here, so that when conflict resolution agent
                // returns it knows how to find the plan agent
                
                // This stores the id of the current proxy, giving the conflict
                // resolution agent a place to start its "search"
                // for the plan agent
                conflictDetected = state.getSelf().getProxyID();
                // Also let it know the id of the other plan
                AssociateInformAgent conflictee = (AssociateInformAgent)conflicts.getFirst();
                // Need to tell the "conflictees" that there is a conflict
                // Wanted to just call act, but recursion is a problem
                conflictee.informConflict(this);
                
                uniqueIDofConflicted = conflictee.uniqueID;
                // Now try to get back to PlanAgent to let it know
                moveToPlanAgent();
                
                // If there are multiple, create extra agents with the info
                if (conflicts.size() > 1) {
                    Machinetta.Debugger.debug( 1,"Multiple conflicts detected: " + conflicts);
                    java.util.ListIterator li = conflicts.listIterator();
                    // Get rid of first, it is handled below
                    li.next();
                    for ( ; li.hasNext(); ) {
                        AssociateInformAgent a = new AssociateInformAgent(plan);
                        a.uniqueID = uniqueID;
                        a.rap = rap;
                        // See above for comments ...
                        a.conflictDetected = state.getSelf().getProxyID();
                        conflictee = (AssociateInformAgent)li.next();
                        a.uniqueIDofConflicted = conflictee.uniqueID;
                        conflictee.informConflict(a);
                        MACoordination.addAgent(a);
                        a.moveToPlanAgent();
                    }
                }
                
                // Want to ensure someone stays here, so add a "clone"
                MACoordination.addAgent(this);
            } else {
                state.addBelief(plan);
            }
        }
    }
    
    /** For comments, see act() */
    private void informConflict(AssociateInformAgent conflictee) {
        conflictDetected = state.getSelf().getProxyID();
        uniqueIDofConflicted = conflictee.uniqueID;
        moveToPlanAgent();
        MACoordination.addAgent(this);
    }
    
    /** Go back to PlanAgent to inform it of conflict */
    private void moveToPlanAgent() {
        // Head back to Plan Agent
        java.util.LinkedList others = MACoordination.getAgentsForPlan(uniqueID);
        
        if (others != null) {
            PlanAgent pa = null;
            RoleAgent ra = null;
            // First look for the PlanAgent, otherwise a RoleAgent
            for (java.util.ListIterator li = others.listIterator(); pa == null && li.hasNext(); ) {
                Agent a = (Agent)li.next();
                if (a instanceof PlanAgent) {
                    pa = (PlanAgent)a;
                } else if (a instanceof RoleAgent) {
                    ra = (RoleAgent)a;
                }
            }
            if (pa != null) {
                // Found what we were looking for, inform it then done
                Machinetta.Debugger.debug( 0,"Found PlanAgent!! " + this);
                pa.informConflict(this);
                // This agent's service is done
                
            } else if (ra != null) {
                // Role agent will know where the plan agent is
                Machinetta.Debugger.debug( 0,"Found RA, moving: " + this);
                MACoordination.moveAgent(this, ra.planAgentRAP);
                
            } else {
                // Use "rap" to get back to an agent that knows where the plan agent is
                Machinetta.Debugger.debug( 0,"Going back to sender: " + this);

                if ( state.getRAPBelief(rap) != null && state.getRAPBelief(rap).isSelf() ) {
                    Machinetta.Debugger.debug( 4,"Something wrong, going to self: " + this);
                } else {
                    MACoordination.moveAgent(this, rap);
                }
            }
        }
    }
    
    public void _merge(Agent a) {
        // This should not be required
        Debugger.debug( 0,"Proxy " + rap + " has sent multiple messages about " + plan.getID());
    }
    
    public Machinetta.State.BeliefType.Belief getAgentAsBelief() {
        return new AssociateInformBelief(this);
    }
    
    public ProxyID getRAP() { return rap; }
    public TeamPlanBelief getPlan() { return plan; }
    
    /** These agents are defined by the plan and the RAP
     *
     * Notice that these are not going to be unique and will sometimes cause overwrites ...
     */
    public BeliefID[] getDefiningBeliefs() {
        BeliefID[] bids = new BeliefID[2];
        bids[0] = plan.getID();
        bids[1] = rap.getID();
        return bids;
    }
    
    public void stateChanged() {
        // This should not be required
    }
    
    /** Creates an exact copy of this inform agent */
    public Object clone() {
        AssociateInformAgent a = new AssociateInformAgent(plan);
        a.rap = rap;
        a.conflictDetected = conflictDetected;
        a.uniqueIDofConflicted = uniqueIDofConflicted;
        a.visitedAgents = visitedAgents;
        a.uniqueID = uniqueID;
        a.state = state;
        return a;
    }
    
    public String toString() {
        return "AssocInform for " + plan.getID() + " from " + rap  + ", id: " + uniqueID + (conflictDetected != null? " reporting conflict":"");
    }
    
    public static final long serialVersionUID = 1L;
}
