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
 * NaiveCoordination.java
 *
 * Created on September 6, 2002, 6:12 PM
 */

package Machinetta.Coordination.Policy;

import Machinetta.Communication.TextMessage;
import Machinetta.State.BeliefType.Belief;
import Machinetta.State.BeliefType.TeamBelief.*;
import Machinetta.State.BeliefType.TeamBelief.Priority.PriorityBelief;
import Machinetta.State.BeliefType.RAPBelief;
import Machinetta.State.*;
import Machinetta.State.BeliefType.ProxyID;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import java.util.Iterator;

/**
 *
 * @author  pynadath
 */
public class NaivePolicy extends CoordinationPolicy {
    
    /** Creates a new instance of NaiveCoordination */
    public NaivePolicy() {
        super();
        addHandler("Machinetta.State.BeliefType.Belief",new NaiveBeliefHandler());
    }
    
    /** Processes an incoming message belief and returns a set of actions to perform in response
     * @param message The incoming message belief
     * @return A list of actions (e.g., messages, new beliefs)
     */
    public Vector interpretMessage(Belief message) {
        Vector actions = super.interpretMessage(message);
        
        if (message instanceof RoleAllocationBelief) {
            // There's a RoleBelief nested in here
            RoleAllocationBelief allocation = (RoleAllocationBelief)message;
            RoleBelief role = allocation.getRole();
            
            // Ensure that allocation is active, before putting in state
            allocation.reactivate();
            
            Machinetta.Debugger.debug( 1,"Role allocation belief arrived " + allocation.getRole() + " , status (must be all true for proxy to look at) " + !allocation.hasConsidered() + " " + !allocation.waitingForRAP() + " " + allocation.isActive() + " " + allocation.getRole().isActive());
                        
            // Check for pre-existing beliefs about this role
            RoleBelief oldRole = (RoleBelief)state.getBelief(role.getID());
            if ((oldRole != null) && (oldRole.getResponsible() != null) && oldRole.getResponsible().isSelf()) {
                // Trying to allocate a role assigned to me.  This will not stand!
                allocation.deactivate();
                actions.remove(allocation);
                Machinetta.Debugger.debug( 1,"Ignoring RA, because I already have role : " + oldRole);
            } else {
                // This allocation is valid...unpack roles
                actions.add(role);
                // Extract nested roles from within role allocation
                while (role instanceof RoleAllocationBelief) {
                    RoleBelief newRole = ((RoleAllocationBelief)role).getRole();
                    // Check for pre-existing beliefs about this role
                    oldRole = (RoleBelief)state.getBelief(newRole.getID());
                    if ((oldRole != null) && (oldRole.getResponsible().isSelf())) {
                        // Trying to allocate an allocation assigned to me.  Stop recursion, too
                        Machinetta.Debugger.debug( 1,"Ignoring RA, because I already have it : " + role);
                        role.deactivate();
                        allocation.deactivate();
                        actions.remove(role);
                        break;
                    } else {
                        actions.add(newRole);
                        role = newRole;
                    }
                }
            }
            
            // Trouble is that this may be a duplicate RA!
            // Look up any existing role allocations
            RoleAllocationBelief oldAllocation = (RoleAllocationBelief)state.getBelief(allocation.getID());
            if (oldAllocation != null) {
                if (oldAllocation.equals(allocation)) {
                    Machinetta.Debugger.debug( 1,"RA has returned: " + oldAllocation);
                    if (!oldAllocation.isActive()) {
                        // Merge previous history
                        allocation.merge(oldAllocation);
                        // Remove the old allocation from the state (don't want multiple)
                        state.removeBelief(oldAllocation.getID());
                    } else  {
                        oldAllocation.merge(allocation);
                        actions.remove(allocation);
                    }
                } else if (oldAllocation.isDuplicate(allocation)) {
                    Machinetta.Debugger.debug( 1,"Duplicate RA has arrived, will destroy one " + oldAllocation);
                    if (oldAllocation.id < allocation.id) {
                        // If old has priority, merge history and remove new
                        oldAllocation.merge(allocation);
                        oldAllocation.reactivate();                        
                        oldAllocation.reconsider();
                        actions.remove(allocation);
                        actions.add(oldAllocation);
                        
                        // In case also RoleConflict, 
                        allocation = oldAllocation;
                        
                        Machinetta.Debugger.debug( 1,"After duplicate removed (incoming removed), remains: " + oldAllocation);
                    } else {
                        // If new has priority, merge history and remove old
                        allocation.merge(oldAllocation);
                        state.removeBelief(oldAllocation.getID());
                        Machinetta.Debugger.debug( 1,"After duplicate removed (outgoing removed), remains: " + allocation);
                    }
                }
            }
            
            // This is fraught with danger ....
            // If there is a conflict belief corresponding to this role allocation remove the conflict belief
            // The assumption is that there was a conflict but the proxy that took the role
            // after the conflict gave it up before the role was complete, hence the new RA.
            if (state.getBelief(RoleConflictBelief.makeID(allocation.getRole())) != null) {
                if (state.getSelf().hasRole(allocation.getRole())) {
                    // This should be redundant, it should have been taken out above.
                    // Still "if" is required so not to get rid of Conflict unneccessarily.
                    Machinetta.Debugger.debug( 0,"RA for conflicted belief, destroying RA " + allocation);
                    actions.remove(allocation);
                } else {
                    Machinetta.Debugger.debug( 1,"Attempting to remove conflict belief for " + allocation.getRole());
                    state.removeBelief(RoleConflictBelief.makeID(allocation.getRole()));
                }
            }
            
        } else if (message instanceof RAPBelief) {
            // RAP Beliefs shouldn't simply overwrite our previous beliefs...
            RAPBelief belief = (RAPBelief)message;
            RAPBelief previous = (RAPBelief)state.getBelief(belief.getID());
            if (previous != null) {
                // ...we instead perform a delicate merge
                actions.remove(belief);
                previous.merge(belief);
                actions.add(previous);
            }
            
        } else if (message instanceof RoleBelief) {
            RoleBelief belief = (RoleBelief)message;
            RAPBelief responsible = belief.getResponsible();
            RoleBelief oldRole = (RoleBelief)state.getBelief(belief.getID());
            if (responsible != null && !responsible.isSelf()) {
                if (oldRole != null) {
                    // We already have a belief about this role!
                    RAPBelief oldResponsible = oldRole.getResponsible();
                    if ((oldResponsible != null) && (oldResponsible != responsible) && oldResponsible.isSelf()) {
                        // Conflicting assignments!
                        Machinetta.Debugger.debug(3,"Role conflict! " + responsible.getProxyID() + " and " + oldResponsible.getProxyID() + " for " +belief);
                        RoleConflictBelief conflictB = (RoleConflictBelief)state.getBelief(RoleConflictBelief.makeID(oldRole));
                        if (conflictB == null) {
                            Machinetta.Debugger.debug( 3,"No conflict belief, creating");
                            conflictB = new RoleConflictBelief(oldRole);
                            actions.add(conflictB);
                        }
                        actions.addAll(generateMessages(responsible.getProxyID(),conflictB));
                        
                        // If there is a conflict with a role I thought I had, better not replace it
                        actions.remove(belief);
                    }
                }
                // Notice we add to responsible RAP regardless ...
                // Change of plan, this can only be dangerous ..
                // responsible.addRole(belief);
                
                // Check whether this role closes any outstanding role allocation beliefs
                RoleAllocationBelief allocation = (RoleAllocationBelief)state.getBelief(RoleAllocationBelief.makeID(belief));
                if (allocation != null && allocation.isActive()) {
                    state.removeBelief(allocation.getID());
                }
            } else if (responsible != null && responsible.isSelf()) {
                if (oldRole != null && oldRole.getResponsible() != null && oldRole.getResponsible().isSelf()) {
                    // OK, just being told I have role I already have
                    actions.remove(belief);
                } else {
                    Machinetta.Debugger.debug( 1,"Proxy recieved message saying it is responsible for role it is not responsible for:  " + belief);
                    ((RoleBelief)belief).setResponsible(null);
                    RoleAllocationBelief ra = new RoleAllocationBelief(belief);
                    actions.add(ra);
                }
            }
            
        } else if (message instanceof TeamPlanBelief) {
            TeamPlanBelief plan = (TeamPlanBelief)message;
            TeamPlanBelief oldPlan = (TeamPlanBelief)state.getBelief(plan.getID());
            if (oldPlan != null) {
                // Merge knowledge from oldPlan into new one
                for (Enumeration params=oldPlan.getAllParamKeys(); params.hasMoreElements(); ) {
                    String key = (String)params.nextElement();
                    plan.setParam(key, oldPlan.getParam(key));
                }
            }
            // Extract roles if necessary
            for (Enumeration roles=plan.getAllRoles().elements(); roles.hasMoreElements(); ) {
                RoleBelief role = (RoleBelief)roles.nextElement();
                if (state.getBelief(role.getID()) == null)
                    actions.add(role);
            }
            // Terminate plan if specified
            if (!plan.isActive()) {
                // Ideally, we should be able to call our handy terminatePlan method.
                // Suffice it to say, we do not live in an ideal world.
                plan.deactivate(); // This is redundant, but it prints out a debug msg
                for (Enumeration allRoles = plan.getAllRoles().elements(); allRoles.hasMoreElements(); ) {
                    RoleBelief role = (RoleBelief)allRoles.nextElement();
                    role = (RoleBelief)state.getBelief(role.getID());
                    if (role != null) {
                        // Deactivate and update
                        role.deactivate();
                        actions.add(role);
                    }
                }
            }
            
        } else if (message instanceof PriorityBelief) {
            // Update plan if necessary
            PriorityBelief priority = (PriorityBelief)message;
            Machinetta.Debugger.debug(1,"New Priority: "+priority);
            TeamPlanBelief plan = priority.getPlan();
            if (plan != null) {
                plan.prioritize(priority);
                actions.add(plan);
            }
            
        } else if (message instanceof RoleConflictBelief) {
            // Need to be careful to merge, not just add incoming RoleConflictBeliefs
            RoleConflictBelief incoming = (RoleConflictBelief)message;
            Machinetta.Debugger.debug( 0,"RC belief " + message);
            RoleConflictBelief previous = (RoleConflictBelief)state.getBelief(incoming.getID());
            
            RoleBelief role = (RoleBelief)state.getBelief(incoming.role.getID());
            if (role != null && role.getResponsible() != null && role.getResponsible().isSelf()) {
                if (previous == null) {
                    Machinetta.Debugger.debug( 1,"Previously unknown conflict : " + incoming);
                    //state.printState();
                    previous = new RoleConflictBelief(incoming.role);
                    actions.addAll(generateMessages(incoming.lowestRAP.getProxyID(),previous));
                }
                
                previous.merge(incoming);
                actions.remove(incoming);
                actions.add(previous);
            } else {
                // This is a serious hassle. Roleconflict discussion going on, but I no longer
                // want the role ...
                if (previous == null) {
                    // Phew, just ignore
                } else {
                    previous.merge(incoming);
                    actions.add(previous);
                    actions.remove(incoming);
                    if (previous.myRole()) {
                        // Now a major problem!!
                        // In theory, when giving up the role, the agent should have
                        // created a RA and receiving agents should have killed their
                        // conflict beliefs when recieving
                        Machinetta.Debugger.debug( 1,"Won conflict resolution for role already given up : " + previous);
                    } else {
                        // Phew, someone else is taking anyway
                    }
                }
            }
            
        }
        
        // Machinetta.Debugger.debug( 1,"For message " + message + " actions: " + actions);
        
        return actions;
    }
    
    /** The essence of naivete:
     * If we can convert this belief into XML, then we send to all members of all teams
     */
    private class NaiveBeliefHandler extends BeliefHandler {
        public Vector handleBelief(Belief belief,boolean isCoordination) {
            Vector result = super.handleBelief(belief,isCoordination);
            if (belief instanceof RoleAllocationBelief) {
                // Role allocation beliefs get passed on very precisely
                RoleAllocationBelief roleAllocBelief = (RoleAllocationBelief)belief;
                
                /*
                // Huge hack to only send target information when doing role allocation for the role
                if (roleAllocBelief.getRole() instanceof Machinetta.DomainSpecific.AirSim.DestroyTarget) {
                    int id = ((Machinetta.DomainSpecific.AirSim.DestroyTarget)roleAllocBelief.getRole()).targetID;
                    Belief b = state.getBelief(Machinetta.DomainSpecific.AirSim.TargetPresent.makeID(id));
                    if (b != null)
                        result.addAll(generateMessages(b));
                    else
                        Machinetta.Debugger.debug( 1,"Could not find TargetPresent belief for role: " + belief);
                    
                }
                */
                ProxyID receiver = roleAllocBelief.passOn();
                Machinetta.Debugger.debug(0,"Shall I pass on this role belief to "+receiver);
                if (receiver != null) {
                    result.addAll(generateMessages(receiver, roleAllocBelief));
                }
                
            /*    // Following two hack around too many plans being created for a single target
            } else if (belief instanceof Machinetta.DomainSpecific.AirSim.TargetPresent) {
                Machinetta.Debugger.debug( 0,"Ignoring TP belief");
            } else if (belief instanceof Machinetta.DomainSpecific.AirSim.DestroyTarget &&
            ((RoleBelief)belief).getResponsible() != null &&
            ((RoleBelief)belief).getResponsible().isSelf() &&
            !isCoordination
            ) {
                
                result.addAll(generateMessages(belief));
             */   
            } else if (belief instanceof RoleBelief && ((RoleBelief)belief).getResponsible() == null) {
                // We do not send unallocated roles around
                
            } else if (belief instanceof RoleBelief && !((RoleBelief)belief).getResponsible().isSelf()) {
                // Don't send roles when others are responsible
                
            } else if ((belief instanceof RAPBelief) && (!((RAPBelief)belief).isSelf())) {
                // We do not send beliefs about others...if they're incompetent, that's their business
                
                // Following three added by Paul specifically for traces to Peng
            } else if (belief instanceof RAPBelief) {
            } else if (belief instanceof TeamPlanTemplate) {
            } else if (belief instanceof TeamBelief) {
            } else if (!isCoordination) {
                // Pass this belief on to the proxies on all other teams
                Machinetta.Debugger.debug( 0,"Naively sending belief "+belief);
                result.addAll(generateMessages(belief));
            }
            
            return result;
        }
    }
    
    /** Method for handling the initiation of a new plan and generate any necessary actions
     * The naive policy method is largely the generic CoordinationPolicy method, with the
     * additional check of new role allocation beliefs in case of any possible messages
     * (i.e., a new role allocation that is the responsibility of some other RAP)
     * @param plan The new plan
     * @param triggers The conditions which triggered the initiation of this plan
     * @return A vector of actions to perform in response to this initiation
     */
    public Vector initiatePlan(TeamPlanBelief plan,Hashtable triggers) {
        Vector result = super.initiatePlan(plan, triggers);
        for (Enumeration actions = result.elements(); actions.hasMoreElements(); ) {
            Object action = actions.nextElement();
            if (action instanceof RoleAllocationBelief) {
                RoleAllocationBelief allocation = (RoleAllocationBelief)action;
                // If this role allocation is assigned to some other RAP, then it would
                // probably be a good idea to let it know
                if (!allocation.getResponsible().isSelf()) {
                    result.addAll(generateMessages(allocation.getResponsible().getProxyID(),allocation));
                }
            }
        }
        return result;
    }
    
    protected Vector generateMessages(ProxyID receiver, Belief belief) {
        Vector messages = new Vector();
        String content = "<Beliefs>"+ BeliefsXML.toXML(belief)+"</Beliefs>";
        messages.addElement(new TextMessage(receiver, content));
        return messages;
    }
    
    /** Notice that this currently also sends message to self. */
    protected Vector generateMessages(Belief belief) {
        Vector messages = new Vector();
        for (Iterator teamIterator = teams.iterator(); teamIterator.hasNext(); ) {
            BeliefID teamID = (BeliefID)teamIterator.next();
            TeamBelief team = (TeamBelief)state.getBelief(teamID);
            for (Iterator memberIterator = team.getMembers(); memberIterator.hasNext(); ) {
                ProxyID member = (ProxyID)memberIterator.next();
                messages.addAll(generateMessages(member,belief));
            }
        }
        return messages;
    }
}
