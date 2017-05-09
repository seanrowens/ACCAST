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
 * Created on December 8, 2003, 9:25 AM
 */

package Machinetta.Coordination.MAC;

import Machinetta.Debugger;
import Machinetta.Coordination.MACoordination;
import Machinetta.State.BeliefID;
import Machinetta.State.BeliefNameID;
import Machinetta.State.BeliefType.RAPBelief;
import Machinetta.State.BeliefType.Belief;
import Machinetta.State.BeliefType.ProxyID;
import Machinetta.State.BeliefType.MAC.RoleAgentBelief;
import Machinetta.State.BeliefType.TeamBelief.Constraints.DirectedInformationRequirement;
import Machinetta.State.BeliefType.TeamBelief.RoleAllocationBelief;
import Machinetta.State.BeliefType.TeamBelief.RoleBelief;
import Machinetta.State.BeliefType.CapabilityBelief;
import Machinetta.State.BeliefType.TeamBelief.TeamPlanBelief;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * When wait constrained roles are started, it is possible that
 * a potential token was being used, which means another role might be
 * being executed.
 *
 * @author  pscerri
 */
public class RoleAgent extends Agent {
    
    /** Instantiated role that this agent is responsible for */
    public RoleBelief role = null;
    
    /** Just in case the plan is not known to the proxy, at least store the id. */
    public BeliefID planID = null;
    
    /** Store the parameters of the plan, just in case they are needed (e.g., for a DirectedInformationRequirement)
     *
     */
    public Hashtable<String,Object> planParams = null;
    
    /**
     * Keep the stopping condition for the plan so that information can be sent back to the
     * PlanAgent
     */
    public Vector postconditions = null;
    
    /** Role allocation belief for this role, private since should not be sent to
     * others. */
    private RoleAllocationBelief rab = null;
    
    /** State variable indicating whether some proxy has accepted the role */
    private boolean accepted = false;
    
    /** When conflicting RAs meet, we don't know which to delete, so we add the incoming
     * to this list.  If this RA is ever terminated and the list is not empty, we let the
     * second one take over, and so on.
     */
    public Vector<Integer> mergedIDs = new Vector<Integer>();
    public Vector<ProxyID> mergedProxies = new Vector<ProxyID>();
    
    /** True if RoleBelief was waitconstrained when initially created */
    public boolean waitConstrainedAtInit = false;
    
    /** Number used when role there is a conflict
     *
     * @Deprecated
     */
    private double conflictNumber = 1.0;
    
    /** Has a value when there was a conflict and this agent should take over */
    public Machinetta.State.BeliefType.RAPBelief takingOver = null;
    
    /** This gives the location of the plan agent for the plan of which
     * this role is a part, so that the role agent can keep it informed */
    public ProxyID planAgentRAP = null;    
    
    /** State variables for the agent */
    private boolean terminated = false, sleeping = false;
    
    /** Creates a new instance of RoleAgent <br>
     *
     * Assumes that this is being created at the location of the plan agent
     * and sets planAgentRAP accordingly.
     */
    public RoleAgent(RoleBelief role, int uniqueID) {
        this.role = role;
        state.addBelief(role);
        
        if (role.getPlan() != null) {
            planID = role.getPlan().getID();
        } else {
            planID = new BeliefNameID("NoPlan");
        }
        
        planAgentRAP = state.getSelf().getProxyID();
        
        this.uniqueID = uniqueID;
        
        this.planParams = role.getPlan().params;
        this.postconditions = role.getPlan().getPostconditions();
        
        // This assumes that role was just created and wait for constrained has not changed
        waitConstrainedAtInit = role.waitForConstrained();
        
        Machinetta.Debugger.debug( 0,"Creating new RA: " + this);
        
        boolean merged = MACoordination.addAgent(this);
        
        if (merged)
	    return;

	RAPBelief respRAP = role.getResponsible();
	if(null != respRAP) {
	    // The role we were given already has a 'Responsible' set
	    // to some RAP, so just go to that RAP, don't start
	    // creating RoleAllocation stuff.
	    MACoordination.moveAgent(this, respRAP.getProxyID());
	}
	else {
            createRA();
	}
    }
    
    /** When incoming there are several possibilities: <br>
     * 1. Still looking for a proxy <br>
     * 2. Have a proxy informing others <br>
     * 3. Informing owner proxy that it is in conflict and not required. <br>
     *
     * XXX: Need to think about returning to origin to check whether kicked out
     */
    public RoleAgent(RoleAgentBelief rab, boolean init) {
        super(rab);
        role = rab.role;
        planID = rab.planID;
        planAgentRAP = rab.planAgentRAP;
        mergedIDs = rab.mergedIDs;
        mergedProxies = rab.mergedProxies;
        noVisits = rab.noVisits;
        postconditions = rab.postconditions;
        planParams = rab.planParams;
        
        boolean merged = MACoordination.addAgent(this);
        
        if (init && !merged) {
            initAct();
        }
    }
    
    public Machinetta.State.BeliefType.Belief getAgentAsBelief() {
        return new RoleAgentBelief(this);
    }
    
    public BeliefID[] getDefiningBeliefs() {
        BeliefID[] ids = new BeliefID[1];
        ids[0] = role.getID();
        //ids[1] = new Machinetta.State.BeliefNameID("ID:"+uniqueID);
        return ids;
    }
    
    /**
     * Things agent does when it first arrives at a proxy. <br>
     *
     * Notice, this is also called by MetaReasoningAgent
     */
    public void initAct() {
        Debugger.debug( 1,"Arriving RA" + this);
        
        if (Machinetta.Configuration.ASSOCIATE_INFORM_ROLE_OFFER) {
            informAssociates(planID);
        }
        
	// If responsible is already set on the role, then we don't
	// start the allocation process.   Probably the role

	RAPBelief respRAP = role.getResponsible();
	if(respRAP == null) {
	    createRA();
	}
	else {
	    if(respRAP.getProxyID().equals(state.getSelf().getProxyID())) 
		role.setResponsible(state.getSelf());
	    // SRO Sun Feb 15 20:58:57 EST 2009 - @TODO: Need to make
	    // sure I'm really doing this right - what (hopefully)
	    // happens here is that we are mimicing the rest of the
	    // role allocation process, i.e. we know we're been
	    // a-priori told to take the role without going through
	    // capabilities and threshold and etc, but we still go do
	    // the various JIAgent stuff so the rest of PlanAgent will
	    // work correctly, as well as assocInform and
	    // planAgent.terminate().  So far after testing I _think_
	    // this is working right.  The execution path that role
	    // allocation normally takes is tortuous and twisted.  The
	    // ONLY way I can think of to make our reaction to a
	    // RoleBelief with 'responsible' already set to our self
	    // is to set some kind of flag on it and then at the point
	    // in the allocation code where we do the
	    // capability/threshold stuff, check for hte flag and
	    // short circuit capability/threshold checking.  -
	    sendAccept();
	}

        state.addBelief(role);
        state.notifyListeners();
    }
    
    /** Tells the role agent that it can start executing this role. <br>
     *
     * Usually (always?) this will mean that all constrained roles have been
     * filled.
     */
    public void informRoleStart(JIAgent jia) {
        Machinetta.Debugger.debug( 2,"Role agent given go ahead to start: " + this);
        role = (RoleBelief)state.getBelief(role.getID());
        
        // Make sure that information requirements of this role are met.
        addDirectedInfoListeners();
        
        role.constrainedWait = false;
        Machinetta.Debugger.debug( 1,"Adding " + role + " to state " + role.getID());
        
        
        state.addBelief(role);
        state.notifyListeners();
    }
    
    /** Tells the role agent that, due to role constraints, the execution of
     * this role should be suspended until further notice.
     */
    public void informRoleSuspended(JIAgent jia) {
        Machinetta.Debugger.debug( 2,"Role agent told to suspend: " + this);
        role = (RoleBelief)state.getBelief(role.getID());
        role.constrainedWait = true;
        state.addBelief(role);
        state.notifyListeners();
    }
    
    /** Role agent is informed the role is complete.
     *
     * In theory this could happen either from the RAP or from other.  If from other,
     * then this probably should inform the RAP (it does not, because currently only works
     * if RAP informs that role is complete. <br>
     *
     * Notice that currently morged agents are not informed ... may be obscure cases where
     * this is a problem?
     *
     * @fix This does not correctly handled the case when the PlanAgent cancels the role
     */
    public void informRoleComplete() {
        // Agent's job is done, get rid of it
        MACoordination.removeAgent(this);
        // Just in case ...
        terminated = true;
        
        // Need to send a JIAgent back to the PlanAgent
        JIAgent informCompleteAgent = new JIAgent(planID, role.getID(), JIAgent.JI_STATE.ROLE_COMPLETE);
        informCompleteAgent.uniqueID = uniqueID;
        MACoordination.addAgent(informCompleteAgent);
        if (planAgentRAP.equals(state.getSelf().getProxyID())) {
            informCompleteAgent.act();
        } else {
            MACoordination.moveAgent(informCompleteAgent, planAgentRAP);
        }
        
        // Probably not necessary? (this is after above, because RATPDU needs the role to do stuff ... )
        state.removeBelief(role.getID());
        
        // What should associates be told?
    }
    
    /** Tells the role agent that the plan for the role has been terminated
     * and that this role should now be terminated.
     */
    public void informPlanTerminated(JIAgent jia) {
        
        Agent currAgent = MACoordination.getAgent(makeComparisonString(getDefiningBeliefs()));
        if (currAgent == null || currAgent.uniqueID != uniqueID) {
            Debugger.debug( 2,"RoleAgent must have left, nothing to be done ... : " + jia);
            //Debugger.debug( 3,"Reason null? " + currAgent + " comp string : " + makeComparisonString(getDefiningBeliefs()));
            MACoordination.printAgents(0);
            return;
        } else {
            Machinetta.Debugger.debug( 1,"Informing terminate: " + jia);
        }
        
        // This role agent has completed its service, remove it
        // Remove this instance from MACoordination even if transferring to merged, so can be added back with right uniqueID
        MACoordination.removeAgent(this);
        
        if (mergedIDs.size() > 0) {
            
            int uniqueID = ((Integer)mergedIDs.remove(0)).intValue();
            Debugger.debug( 2,"Role agent changing id due to plan termination: " + this + " to " + uniqueID);
            
            ProxyID planRAP = (ProxyID)mergedProxies.remove(0);
            this.uniqueID = uniqueID;
            planAgentRAP = planRAP;
            MACoordination.addAgent(this);
            
            // Now inform "new" plan agent of role allocation success
            JIAgent informAcceptAgent = new JIAgent(planID, role.getID(), JIAgent.JI_STATE.ROLE_ALLOCATED);
            informAcceptAgent.uniqueID = uniqueID;
            
            MACoordination.moveAgent(informAcceptAgent, planAgentRAP);
            
        } else {
            Debugger.debug( 2,"Deactivated role agent due to plan termination: " + this);
            terminated = true;
            role.activation = false;
        }
        
        state.addBelief(role);
        state.notifyListeners();
    }
    
    /** Tells the role agent that it has permission to execute this role. <br>
     *
     */
    public void informRoleAuthorized(JIAgent jia) {
        Machinetta.Debugger.debug( 2,"Role agent authorized to start: " + this);
        role = (RoleBelief)state.getBelief(role.getID());
        role.authorized = true;
        state.addBelief(role);
        state.notifyListeners();
    }
    
    /**
     * Requests a role rescheduling
     */
    public void requestReschedule(JIAgent jia) {
        Machinetta.Debugger.debug( 3,"RoleAgent asked to reschedule role to: " + jia.getScheduleTime());
        role.scheduled = jia.getScheduleTime();
        role.scheduleChangeRequest = true;
        state.addBelief(role);
        state.notifyListeners();
    }
    
    /**
     * Let agents "travel" together, so that when conflict is resolve, work is not redone
     */
    public void _merge(Agent a) {
        if (this.uniqueID == a.uniqueID) {
            // This indicates a serious bug
            // The only reason it might happen without a bug, is if two plans happened to pick
            // the same uniqueID
            Debugger.debug( 4,"For some reason there are RA duplicates for " + this + "( " + a + ")");
        } else {
            Debugger.debug( 2,"Merging role agents: " + this + " and " + a);
            mergedIDs.add(new Integer(a.uniqueID));
            mergedProxies.add(((RoleAgent)a).planAgentRAP);
            
            RoleAgent ra = (RoleAgent)a;
            if (ra.mergedIDs.size() > 0) {
                for (int i = 0; i < ra.mergedIDs.size(); i++) {
                    Integer o = ra.mergedIDs.elementAt(i);
                    if (!mergedIDs.contains(o)) {
                        mergedIDs.add(o);
                        mergedProxies.add(ra.mergedProxies.elementAt(i));
                    }
                }
            }
        }
    }
    
    public void stateChanged() {
        Machinetta.Debugger.debug( 0,"State change called in " + this);
        
        // First check whether the plan might be complete
        java.util.Hashtable matches = TeamPlanBelief.staticMatchPostconditions(PlanAgentFactory.getTriggers(), postconditions, planParams);
        if (matches != null) {
            // We match the post-conditions of this plan, so deactivate plan
            Machinetta.Debugger.debug(2,"POSTCONDITIONS match for "+planID+" : " + matches);
            
            for (Enumeration e = matches.elements(); e.hasMoreElements(); ) {
                Belief b = (Belief)e.nextElement();
                Machinetta.Debugger.debug( 1,"Sending " + b + " to PlanAgent so it knows postconditions");
                InformationAgent infoAgent = new InformationAgent(b);
                // @todo If the planAgent has moved, this infoAgent does not understand that it needs to find it
                // Probably smarter to use a JIAgent
                MACoordination.moveAgent(infoAgent, planAgentRAP);
            }
            
        } else {
            Machinetta.Debugger.debug( 0,"NO POST: " + PlanAgentFactory.getTriggers() + " for " + postconditions);
        }
        
        // Now handle the role stuff
        
        if (terminated || sleeping) return;
        
        RoleBelief oldRole = role;
        role = (RoleBelief)state.getBelief(role.getID());
        
        if (role == null) {
            state.addBelief(oldRole);
            role = oldRole;
        }
        if (role == null) {
            Machinetta.Debugger.debug( 4,"Major problem role == null!!");
        }
        
        // See whether agent is waiting for AA to process RoleAllocation
        if (rab != null) {
            
            // I don't know why there is a new RAB but there is - FIX.
            // After lots of debugging, I still can't work this out!
            // However, it doesn't seem to hurt.  If you leave this out
            // then roles are never accepted (or rejected), so it is something
            // to do with the interaction with SingleShotRoleAssignment, but I don't know what!
            // One possibility is that the classcasts are causing the problem .... but I cannot replicate
            // with a simple piece of code (got hint from other error.)
            RoleAllocationBelief newRAB = (RoleAllocationBelief)state.getBelief(rab.getID());
            if (rab != newRAB && newRAB != null) {
                Machinetta.Debugger.debug( 0,"Why is there a new RAB?");
                rab = newRAB;
            }
            
            if (rab.hasConsidered()) {
                //state.removeBelief(rab.getID());
                if (rab.hasAccepted()) {
                    if(null != role) {
                        if(null != role.getResponsible()) {
                            Debugger.debug( 2,"Role accepted : " + this + " by " + role.getResponsible().getID());
                        } else {
                            Debugger.debug( 5,"Role accepted : " + this + " ERROR ERROR role is not null but role.getResponsible() returns null. ");
                        }
                    } else {
                        Debugger.debug( 5,"Role accepted : " + this + " ERROR ERROR for some reason 'role' member of RoleAgent is null.");
                    }
                    
                    accepted = true;
                    
		    state.getSelf().removeRole(rab);
		    state.removeBelief(rab.getID());
		    rab = null;
                    // Since the belief has changed, reset the history
                    visitedAgents.clear();
                    
                    // Need to send a JIAgent back to the PlanAgent
                    sendAccept();
                    
                    if (Machinetta.Configuration.ASSOCIATE_INFORM_ROLE_ACCEPT && !Machinetta.Configuration.ASSOCIATE_INFORM_ROLE_OFFER) {
                        informAssociates(planID);
                    }
                } else {
                    Debugger.debug( 1,"RoleAgent (" + this + ") needs to look elsewhere, proxy does not accept");
                    boolean found = false;
                    if (Machinetta.Configuration.REMEMBER_MAC_AGENT_HISTORY)
                        found = MACoordination.moveAgentRandomly(this, false);
                    else {                        
                        if ((noVisits % Machinetta.Configuration.PROXY_VISITS_BEFORE_ROLE_SLEEP) == (Machinetta.Configuration.PROXY_VISITS_BEFORE_ROLE_SLEEP - 1)) found = false;
                        else {
                            found = MACoordination.moveAgentRandomly(this, true);
                        }
                    }
                    
                    if (!found) {
                        if (!Machinetta.Configuration.ROLE_ALLOCATION_META_REASONING) {
                            // If not found, sleep a bit, then clear history and start again
                            final Agent theAgent = this;
                            (new Thread() {
                                public void run() {
                                    try {
                                        sleeping = true;
                                        Debugger.debug( 2,"Unfilled role sleeping : " + theAgent);
                                        sleep(Machinetta.Configuration.UNFILLED_ROLE_SLEEP_TIME);
                                    } catch (InterruptedException e) {}
                                    sleeping = false;
                                    visitedAgents.clear();
                                    Debugger.debug( 2,"Unfilled role awoken : " + theAgent);
                                    boolean nowFound = MACoordination.moveAgentRandomly(theAgent, false);
                                    if (!nowFound) {
                                        // This indicates a bug, since visited agents should be clear ...
                                        Debugger.debug( 4,"Problem, after sleeping still could not find destination for " + theAgent + " dumping role");
                                    }
                                }
                            }).start();
                        } else {
                            // Create a MetaReasoningAgent to deal with this ...
                            MetaReasoningAgent ma = new MetaReasoningAgent(this);
                            boolean merged = MACoordination.addAgent(ma);
                            if (!merged) {
                                // Let the agent do something ...
                                Machinetta.Debugger.debug( 2,"MetaReasoning Agent created for unfilled RA");
                                ma.executeStrategy();
                            }
                        }
                    }
                }
		if(null != rab) {
		    state.getSelf().removeRole(rab);
		    state.removeBelief(rab.getID());
		    rab = null;
		}
                
                // Release the lock, allowing other role allocation to occur
                Machinetta.Debugger.debug( 0,"DONE RA of : " + role);
                roleAllocLock.v(roleAllocThread);
            }
        } else if (role.isComplete()) {
            // Role has been completed by RAP
            informRoleComplete();
        } else if (role.rescheduled) {
            // Send a message back to PlanAgent giving it the rescheduled time
            // @fix this should really use JIS_SCHEDULING_RESPONSE
            role.rescheduled = false;
            state.addBelief(role); // Notice don't call notify
            sendAccept();
            Machinetta.Debugger.debug( 1,"Did a reschedule sendAccept");
        } else if (role.getResponsible() != null && role.getResponsible().isSelf()) {
            // Things are going just fine ..
            Machinetta.Debugger.debug( 0,"Role agent decides everything fine : " + this);
        } else if (accepted == true) {
            // Was accepted but no longer is, i.e., kicked out - time to move on ....
            Debugger.debug( 2,"Role has been \"kicked out\" : " + this);
            
            // Stop doing any information delivery activities
            removeDirectedInfoListeners();
            
            accepted = false;
            // Since the belief has changed, reset the history
            visitedAgents.clear();
            
            // When kicked out, need to reset waitConstrained
            role.constrainedWait = waitConstrainedAtInit;
            
            // Need to send a JIAgent back to the PlanAgent
            JIAgent informAcceptAgent = new JIAgent(planID, role.getID(), JIAgent.JI_STATE.ROLE_DEALLOCATED);
            informAcceptAgent.uniqueID = uniqueID;
            MACoordination.addAgent(informAcceptAgent);
            MACoordination.moveAgent(informAcceptAgent, planAgentRAP);
            
            // Move the RoleAgent must move to reallocate the role
            MACoordination.moveAgentRandomly(this, true);
        } else {
            // The role allocation is pending, locked out by another RA
            Debugger.debug( 0,"Role allocation pending for: " + this);
        }
    }
    
    private void addDirectedInfoListeners() {
        if (role.infoSharing != null) {
            for (DirectedInformationRequirement dir: role.infoSharing) {
                if (!dir.added && dir.getTo() != null) {
                    
                    if (dir.sendParams) {
                        dir.proscribedID = (BeliefID)planParams.get(dir.paramName);
                    }
                    
                    Machinetta.Debugger.debug( 1,"Need to send directed information about " + dir.getType() + " to " + dir.getTo());
                    InformationAgentFactory.addDirectedInformationRequirement(dir);
                    dir.added = true;
                } else {
                    Machinetta.Debugger.debug( 1,"Not able to set up DirectedInformation yet");
                }
            }
        } else {
            Machinetta.Debugger.debug( 0,"Directed information sharing not required");
        }
    }
    
    private void removeDirectedInfoListeners() {
        // Don't forget to reset dir.added to false
        if (role.infoSharing != null) {
            for (DirectedInformationRequirement dir: role.infoSharing) {
                if (dir.added) {
                    InformationAgentFactory.removeDirectedInformationRequirement(dir);
                }
            }
        }
    }
    
    public void updateDirectedInfoInformation(BeliefID deliverRoleID, ProxyID dest) {
        if (role.infoSharing != null) {
            for (DirectedInformationRequirement dir: role.infoSharing) {
                if (deliverRoleID != null) {
                    if (dir.performer != null && dir.performer.equals(deliverRoleID)) {
                        Machinetta.Debugger.debug(1, "Got delivery information for " + deliverRoleID);
                        dir.to = dest;
                        addDirectedInfoListeners();
                    } else if (dir.performer != null) {                    
                        Machinetta.Debugger.debug(1, "Got delivery information for " + deliverRoleID + " needed " + dir.performer);
                    } else {
                        // Typically, this occurs when there are both dirs with fixed proxy destinations and ones with role performer destinations,
                        // in such a case it is fine.  I cannot see when else it would happen.
                        Machinetta.Debugger.debug(0, "dir.performer == null!! : " + dir);
                    }
                } else {
                    Machinetta.Debugger.debug(4, "deliverRoleID == null!");
                }
            }
        } else {
            Machinetta.Debugger.debug( 3,"JIAgent trying to provide DirectedInfo info when not required");
        }
    }
    
    private void sendAccept() {
        JIAgent informAcceptAgent = new JIAgent(planID, role.getID(), JIAgent.JI_STATE.ROLE_ALLOCATED);
        informAcceptAgent.uniqueID = uniqueID;
        CapabilityBelief cap = (CapabilityBelief)state.getSelf().getCapability(role.getRoleName());
        if (cap != null) {
            try {
                informAcceptAgent.setCapability(cap.getCapability(role.getParams()));
            } catch (IllegalArgumentException e) {
                informAcceptAgent.setCapability(100);
                Machinetta.Debugger.debug( 1,"Tried to get capability for " + cap + " with params " + role.getParams());
            }
        } else {
            Machinetta.Debugger.debug( 3,"Could not find capability for making JIAgent");
        }
        MACoordination.addAgent(informAcceptAgent);
        if (planAgentRAP.equals(state.getSelf().getProxyID())) {
            informAcceptAgent.act();
        } else {
            MACoordination.moveAgent(informAcceptAgent, planAgentRAP);
        }
    }
    
    /** Sets the conflict number for this role.
     *
     * Plan sets this number and ensures that all roles in same plan have
     * same number, hence conflicting plans either have all rejected or all
     * accepted.
     *
     * @Deprecated
     */
    public void setConflictNumber(double d) { conflictNumber = d; }
    
    public double getConflictNumber() { return conflictNumber; }
    
    /** Semaphore and thread for ensuring only one role allocation at a time */
    static private Semaphore roleAllocLock = new Semaphore(1);
    private Thread roleAllocThread = null;
    
    /** Creates a role allocation belief, but only when no other RoleAgent has one active. */
    private void createRA() {
        
        roleAllocThread = new Thread() {
            public void run() {
                roleAllocLock.p(roleAllocThread);
                Machinetta.Debugger.debug( 0,"START RA of : " + role);
                rab = new RoleAllocationBelief(role);
                state.addBelief(rab);
                state.notifyListeners();
            }
        };
        roleAllocThread.start();
    }
    
    public String toString() {
        try {
            return "RoleAgent (" + role.getID() + ") : " + (role.getResponsible() == null ? "unallocated" : "allocated to "+role.getResponsible()) + " has visited: " + visitedAgents  + ", id: " + uniqueID + " merged: " + mergedIDs;
        } catch (NullPointerException e) {
            return "RoleAgent with error";
        }
    }

    void teammateAdd(JIAgent jIAgent) {
        role.addTeammate(jIAgent.getTeammate());
        Machinetta.Debugger.debug(1, "Adding team mate " + jIAgent.getTeammate() + ", now: " + role.getTeammates());
    }

    void teammateRemove(JIAgent jIAgent) {
        role.removeTeammate(jIAgent.getTeammate());
        Machinetta.Debugger.debug(1, "Removing team mate " + jIAgent.getTeammate() + ", now: " + role.getTeammates());
    }
    
    public static final long serialVersionUID = 1L;
}
