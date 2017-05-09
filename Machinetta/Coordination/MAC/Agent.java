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
 * Agent.java
 *
 * Created on November 4, 2003, 10:28 AM
 */

package Machinetta.Coordination.MAC;

import Machinetta.Debugger;
import Machinetta.Coordination.MACoordination;
import Machinetta.State.BeliefID;
import Machinetta.State.ProxyState;
import Machinetta.State.BeliefType.Belief;
import Machinetta.State.BeliefType.ProxyID;
import Machinetta.State.BeliefType.RAPBelief;
import Machinetta.State.BeliefType.TeamBelief.Associates;
import Machinetta.State.BeliefType.TeamBelief.TeamPlanBelief;
import java.util.Enumeration;
import java.util.Vector;

/**
 *
 * @author  pscerri
 */
public abstract class Agent implements java.io.Serializable {
    
    // Need to make sure that state is reconnected at each new proxy
    // This should work
    protected static transient ProxyState state = new ProxyState();
    
    public String comparisonString = null;
    public Vector<ProxyID> visitedAgents = new Vector<ProxyID>();
    public int noVisits = 0;
    
    /** Number used to associate agents involved in the same plan */
    public int uniqueID = -1;
    
    /** Creates a new instance of Agent */
    public Agent() {
    }
    
    public Agent(Machinetta.State.BeliefType.MAC.MABelief bel) {
        visitedAgents = bel.visited;
        uniqueID = bel.uniqueID;
    }
    
    /** This function should return the same beliefs, in the same order, as would be used in makeComparison String */
    public abstract BeliefID[] getDefiningBeliefs();
    
    /** This function should return a belief representing this agent, used for "moving" agent */
    public abstract Belief getAgentAsBelief();
    
    /** This function is called to alloc the agent to decide what to do */
    public abstract void stateChanged();
    
    public void informAssociates(BeliefID tpbID) {
        TeamPlanBelief tpb = (TeamPlanBelief)state.getBelief(tpbID);
        if (tpb != null) {
            informAssociates(tpb);
        } else {
            Machinetta.Debugger.debug( 3,"Failed to inform associates, since plan not known: " + tpbID);
        }
    }
    
    /** Informs associates of this plan
     */
    public void informAssociates(TeamPlanBelief tpb) {
        Associates ass = (Associates)state.getBelief(Associates.getConstantID());
        if (ass == null) return;
        
        for (Enumeration e = ass.getAssociates(); e.hasMoreElements(); ) {
            AssociateInformAgent a = new AssociateInformAgent(tpb);
            // Set the id to associate with this plan agent
            a.uniqueID = uniqueID;
            MACoordination.addAgent(a);
            MACoordination.moveAgent(a, (ProxyID)e.nextElement());
        }
        // Also create an associate inform agent here, resulting in a uniform interface for conflict detection
        AssociateInformAgent a = new AssociateInformAgent(tpb);
        // Set the id to associate with this plan agent
        a.uniqueID = uniqueID;
        MACoordination.addAgent(a);
    }
    
    /** When two agents with identical defining beliefs are at the same agent,
     * this method gets called on the first agent, with the second.
     */
    public abstract void _merge(Agent a);
    
    /** Does the agent wide stuff for _merge (see above) */
    public void merge(Agent a) {
        visitedAgents.addAll(a.visitedAgents);
        _merge(a);
    }
    
    public boolean sameAgent(BeliefID[] bids) {
        return makeComparisonString(bids).equalsIgnoreCase(makeComparisonString(getDefiningBeliefs()));
    }
    
    public static String makeComparisonString(BeliefID[] bids) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < bids.length; i++) {
            sb.append(bids[i].toString() + ":");
        }
        return sb.toString();
    }
    
    /** Adds this proxy to the list of proxies this agent has visited */
    public void proxyVisited() {
        if (Machinetta.Configuration.REMEMBER_MAC_AGENT_HISTORY)
            visitedAgents.add(state.getSelf().getProxyID());
        noVisits++;
    }
    
    public boolean hasVisited(RAPBelief rap, boolean includeClones) {
        return hasVisited(rap.getProxyID(), includeClones);
    }
    
    public boolean hasVisited(ProxyID id, boolean includeClones) {
        /*
        System.out.println("Checking if " + rap.getProxyID() + " has been visited");
        for (java.util.Enumeration e = visitedAgents.elements(); e.hasMoreElements(); ) {
            System.out.println("Agent " + e.nextElement() + " has been visited");
        }
         **/
        if (id == null) {
            Debugger.debug( 3,"Trying to call hasVisited on null?!");
            return true;
        }
        if (Machinetta.Configuration.REMEMBER_MAC_AGENT_HISTORY && visitedAgents == null) visitedAgents = new Vector<ProxyID>();
        
        if (!Machinetta.Configuration.REMEMBER_MAC_AGENT_HISTORY) {
            // In this case, it is not possible to work out from the agent history whether
            // this agent has been visited at all, but can at least check that has not departed
            // for this agent before
            return MACoordination.agentHasLeftFor(getDefiningBeliefs(), id);
        }
        
        // First check whether this specific agent has visited the rap
        boolean visited = visitedAgents.contains(id);
        if (visited) return visited;
        
        if (includeClones) {
            // If not check whether some other version of this agent has visited the agent
            String idStr = makeComparisonString(getDefiningBeliefs());
            DepartureRecord dep = (DepartureRecord)MACoordination.leftAgents.get(idStr);
            if (dep == null)
                return false;
            else
                return dep.haveDepartedTo(id);
        } else {
            return false;
        }
    }
    
    public boolean hasVisited(RAPBelief rap) {
        return hasVisited(rap, true);
    }
    
    public String toString() { return "Agent"; }
}
