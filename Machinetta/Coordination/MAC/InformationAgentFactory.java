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
 * InformationAgentFactory.java
 *
 * Created on November 4, 2003, 3:35 PM
 */

package Machinetta.Coordination.MAC;

import Machinetta.Coordination.MACoordination;
import Machinetta.State.BeliefType.*;
import Machinetta.State.BeliefType.TeamBelief.*;
import Machinetta.State.*;
import Machinetta.State.BeliefType.TeamBelief.Constraints.DirectedInformationRequirement;

import java.util.*;

/**
 *
 * @author  pscerri
 */
public class InformationAgentFactory extends AgentFactory {
    
    /** @Deprecated */
    private Vector<ProxyID> informSelfRAPBelief = null;
    
    private static Hashtable<Class,LinkedList<DirectedInformationRequirement>> reqs = new Hashtable<Class, LinkedList<DirectedInformationRequirement>>();
    private static Hashtable<Class,BeliefShareRequirement> shareReqs = new Hashtable<Class,BeliefShareRequirement>();
    
    private static boolean started = false;
    
    /** Creates a new instance of InformationAgentFactory */
    public InformationAgentFactory() {
        // setInformSelfRAPBelief();
        
        if (!started) {
            if (Machinetta.Configuration.AVAILABLE_RAP_META_REASONING)
                new AvailableRAPMonitor();
            
            if (Machinetta.Configuration.allMap.get("DYNAMIC_TEAMING") != null) {
                Machinetta.Debugger.debug(1, "Sending out RAPBelief for dynamic teaming");                
                // @todo Work out what this TTL should be, but 6 should create a connected network for now
                Agent a = new InformationAgent(state.getSelf(), 6);
                MACoordination.addAgent(a);
                a.stateChanged();
            }
        }
    }
    
    /**
     * Might want to change this so these beliefs are retrieved from the state?
     */
    public static void addDirectedInformationRequirement(DirectedInformationRequirement dir) {
        
        if (dir.type != null) {
            LinkedList<DirectedInformationRequirement> l = null;
            l = reqs.get(dir.type);
            if (l == null) {
                l = new LinkedList<DirectedInformationRequirement>();
                reqs.put(dir.type, l);
            }
            synchronized(l) {
                l.add(dir);
            }
        }
        
        // In the special case that dir has a proscribed beliefID, we check in the
        // state to see if that belief has already been posted.
        if (dir.proscribedID != null && state.getBelief(dir.proscribedID) != null) {
            if (dir.to != null) {
                Agent a = new InformationAgent(state.getBelief(dir.proscribedID), dir.to);
                MACoordination.addAgent(a);
                // Let it immediately act
                a.stateChanged();
            } else {
                Machinetta.Debugger.debug(3, "Unimplemented version of DirectedInformationRequirement");
            }
        }
        
        Machinetta.Debugger.debug(1, "After addition of new DIR: " + reqs);
    }
        
    public static void removeDirectedInformationRequirement(DirectedInformationRequirement dir) {
        if (dir.type != null) {
            LinkedList<DirectedInformationRequirement> l = null;
            l = reqs.get(dir.type);
            if (l != null) {
                boolean ok = l.remove(dir);
                // This is probably because the dir was never added, i.e., it was never set up
                if (!ok) Machinetta.Debugger.debug(1, "Failed to remove dir, not in list!");
            }
        }
        
        // Don't need proscribedID version yet, because not added
        
        Machinetta.Debugger.debug(1, "After removal of DIR: " + reqs);
    }
    
    public static void addBeliefShareRequirement(BeliefShareRequirement req) {
        synchronized(shareReqs) {
            shareReqs.put(req.getType(), req);
        }
    }
    
    public void stateChange(Machinetta.State.BeliefID[] b) {
        Machinetta.Debugger.debug(0, "Information factory checking beliefs");
        for (int i = 0; i < b.length; i++) {
            Machinetta.State.BeliefID[] bid = new Machinetta.State.BeliefID[1];
            bid[0] = b[i];
            Machinetta.Debugger.debug(0, "Information factory checking belief: " + b[i] + " Class: " + b[i].getClass());
            
            Belief bel = state.getBelief(b[i]);
            if (bel == null) continue;
            
            if (bel.locallySensed()) {
                Class belClass = bel.getClass();
                
                // Check if there are any directed information requirements for this belief type
                LinkedList<DirectedInformationRequirement> l = reqs.get(belClass);
                
                Machinetta.Debugger.debug(0, "Class: " + belClass + " for " + (l == null? 0: l.size()));
                if (l == null) {
                    Machinetta.Debugger.debug(0, "IAF No match: " + reqs.keys());
                }
                
                if (l != null) {
                    synchronized(l) {
                        for (DirectedInformationRequirement d: l) {
                            String label = (String)bel.getMeta("Label");
                            boolean send = false;
                            if (d.name == null || d.name.equalsIgnoreCase(label)) {
                                if (d.to != null) {
				    //                                    Machinetta.Debugger.debug(1, "IAF Going to send " + bel + " to " + d.to + " due to label match (or no label)");
                                    send = true;
                                } else {
                                    Machinetta.Debugger.debug(3, "Unimplemented version of DirectedInformationRequirement");
                                }
                            } else if (d.proscribedID != null && d.proscribedID.equals(bel.getID())) {
                                if (d.to != null) {
                                    Machinetta.Debugger.debug(1, "Going to send " + bel + " to " + d.to + " due to id match");
                                    send = true;
                                } else {
                                    Machinetta.Debugger.debug(3, "Unimplemented version of DirectedInformationRequirement");
                                }
                            } else {
                                Machinetta.Debugger.debug(1, "IAF Not sending " + bel + " in directed information because label does not match: " + label + " != " + d.name);
                            }
                            
                            if (send) {
                                Agent a = new InformationAgent(bel, d.to);
                                MACoordination.addAgent(a);
                                // Let it immediately act
                                a.stateChanged();
                            }
                        }
                    }
                }
                
                // Check if there are any undirected information requirements for this belief type
                BeliefShareRequirement req = shareReqs.get(belClass);
                if (req != null) {
                    InformationAgent a = new InformationAgent(bel);
                    // Notice we check whether there was also a DirectedInformationRequirement, because that will lead to
                    // another InformationAgent with the same signature
                    if (!req.isAllowResend() && MACoordination.agentWasHere(a.getDefiningBeliefs()) && l == null) {
                        Machinetta.Debugger.debug(1, "Already created BeliefShareRequirement for " + bel);
                    } else {
                        if (req.getTtl() > 0) {
                            a.setTTL(req.getTtl());
                        }
                        Machinetta.Debugger.debug(1, "Sending " + bel + " due to BeliefShareRequirement, local? " + bel.locallySensed());
                        MACoordination.addAgent(a);
                        // Let it immediately act
                        a.stateChanged();
                    }
                } else {
                    Machinetta.Debugger.debug(0, "No BeliefShareRequirement for " + belClass);
                }
            }
        }
    }
    
    /** Recreates an agent from an incoming belief */
    public void createAgent(Machinetta.State.BeliefType.MAC.MABelief bel) {
	//        Machinetta.Debugger.debug(1, "Creating agent from : " + bel);
        
        Machinetta.State.BeliefType.MAC.InformationAgentBelief iBel = (Machinetta.State.BeliefType.MAC.InformationAgentBelief)bel;
        if (iBel != null && iBel.bel != null) {
            Machinetta.State.BeliefID[] bid = new Machinetta.State.BeliefID[1];
            bid[0] = iBel.bel.getID();
            if (!MACoordination.agentExists(bid)) {
                Agent a = new InformationAgent(iBel.bel, iBel.toRAP, iBel.forRole);
                ((InformationAgent)a).setTTL(iBel.TTL);
                
                MACoordination.addAgent(a);
                
		//                Machinetta.Debugger.debug(1, "MABelief arrived of type : " + iBel.bel.getClass());
                
                // Add this agent's information to the state
                // Special care with RAPBeliefs
                if (iBel.bel instanceof RAPBelief) {
                    if (((RAPBelief)iBel.bel).getProxyID().matches(state.getSelf().getProxyID())) {
                        // Do nothing, ignore beliefs about self
                    } else {
                        RAPBelief rap = (RAPBelief)iBel.bel;
                        Machinetta.Debugger.debug(1, "RAPBelief about " + rap.getProxyID() + " arrived");
                        rap.setIsSelf(false);
                        state.addBelief(rap);
                        
                        // Also add them to the team
                        // @todo Need to find a better way of getting team belief from state
                        // Notice that this assumes that everyone is in the one team
                        TeamBelief team = (TeamBelief)state.getBelief(new BeliefNameID("TeamAll"));
                        if (!team.isMember(rap.getProxyID())) {
                            Machinetta.Debugger.debug(1, "Added new member to the team: " + rap.getProxyID());
                            team.members.add(rap.getProxyID());
                            
                            if (Machinetta.Configuration.allMap.get("DYNAMIC_TEAMING") != null) {
                                Machinetta.Debugger.debug(1, "Sending out RAPBelief for dynamic teaming: " + state.getSelf().getProxyID());                               
                                Agent areply = new InformationAgent(state.getSelf(), rap.getProxyID());
                                MACoordination.addAgent(areply);
                                areply.stateChanged();
                            }
                        } else {
                            Machinetta.Debugger.debug(1, "RAPBelief Member was already part of team, not adding: " + rap.getProxyID());
                        }
                    }
                } else {
                    if (state.getBelief(iBel.bel.getID()) == null || !state.getBelief(iBel.bel.getID()).locallySensed()) {
                        iBel.bel.setLocallySensed(false);
                    }
                    state.addBelief(iBel.bel);
                    state.notifyListeners();
                }
                _createAgent(iBel, a);
                // As soon as this agent arrives, give it an opportunity to move on (or do whatever)
                a.stateChanged();
            } else {
                Machinetta.Debugger.debug(3, "Information agent already exists, ignoring: " + bid[0]);
            }
        } else {
            Machinetta.Debugger.debug(3, "Why is bel null? : " + iBel);
        }
        
    }
    
    /**
     * Sends the RAPBelief to all the RAPs in the list
     */
    private void informChangedRAPBelief(RAPBelief r) {
        InformationAgent a = new InformationAgent(r);
        for (int i = 0; informSelfRAPBelief != null && i < informSelfRAPBelief.size(); i++) {
            MACoordination.addAgent(a);
            MACoordination.moveAgent(a, (ProxyID)informSelfRAPBelief.elementAt(i));
        }
    }
    
    /**
     * Monitors whether this RAP is doing anything, then starts
     * a meta reasoning agent, if it does not for a while
     */
    class AvailableRAPMonitor extends Thread implements StateChangeListener {
        
        /** Time when RAP was known not to have a role, -1 if had at last check */
        long noRoleAt = 0;
        final long maxAvailTime = 300 * 1000;
        
        public AvailableRAPMonitor() {
            state.addChangeListener(this);
            start();
        }
        
        public void run() {
            while (true) {
                try {
                    sleep(maxAvailTime);
                } catch (InterruptedException e) {}
                if (System.currentTimeMillis() - noRoleAt > maxAvailTime) {
                    // Create a meta-reasoning agent to decide what to do
                    MetaReasoningAgent agent = new MetaReasoningAgent(state.getSelf());
                    agent.executeStrategy();
                    
                    // After sending the meta-agent, sleep for a while because
                    // don't want to send continuously nor never send again
                    // Might need something more systematic
                    try {
                        sleep(300 * 1000);
                    } catch (InterruptedException e) {}
                    
                }
            }
        }
        
        public void stateChanged(BeliefID[] b) {
            Vector roles = state.getSelf().getRoles();
            boolean haveRole = false;
            for (int i = 0; !haveRole && i < roles.size(); i++) {
                if (!(roles.elementAt(i) instanceof RoleAllocationBelief))
                    haveRole = true;
            }
            if (haveRole) noRoleAt = -1;
            else if (noRoleAt < 0) noRoleAt = System.currentTimeMillis();
        }
        
    }
}
