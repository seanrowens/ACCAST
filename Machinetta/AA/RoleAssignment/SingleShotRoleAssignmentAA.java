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
 * SingleShotRoleAssignmentAA.java
 *
 * Created on 18 September 2002, 22:25
 */

package Machinetta.AA.RoleAssignment;

import Machinetta.State.BeliefType.TeamBelief.*;
import Machinetta.State.BeliefType.TeamBelief.Priority.*;
import Machinetta.State.BeliefType.*;
import Machinetta.State.*;
import Machinetta.RAPInterface.OutputMessages.ProposeRoleMessage;
//import Machinetta.DomainSpecific.DisasterSim.OutputMessages.*;
import Machinetta.Configuration;

import java.util.Random;
import java.util.Vector;

/**
 * Decides to either transfer control or not, then leaves control
 * indefinately in the hands of the entity to which control
 * is transferred.
 *
 * @author  scerri
 */
public class SingleShotRoleAssignmentAA implements RoleAssignmentAA {
    
    /** Creates a new instance of SingleShotRoleAssignmentAA */
    public SingleShotRoleAssignmentAA() {
    }
    
    /** This method should eventually result in the role either being
     * accepted or rejected by the RAP.
     *
     * If the method is going to take non-negligible time to complete
     * then it should create a new thread and return immediately.
     *
     * Accepts role autonomously if capable (and RAP is agent or robot)
     *
     * Should consider priority of the role ...
     */
    public RoleAssignmentThread offeredRole(RoleAllocationBelief ra) {
        
        Machinetta.Debugger.debug(1, "Invoking SingleShotRA");
        state.getSelf().debugPrintRoles(0);
        
        // Check this RA is not for a role RAP already has ...
        // Problem here is that there are some funny race conditions
        // that come about because the equals method on role ignores the
        // plan specific id associated with the role ...
        if (state.getSelf().getRoles().contains(ra.getRole())) {
            Machinetta.Debugger.debug(3, "Proxy asked to role allocate a role it already has! " + ra.getRole());
            state.getSelf().debugPrintRoles(3);
        }
        
        if (!ra.hasConsidered() && !ra.waitingForRAP() && ra.isActive() && ra.getRole().isActive()) {
            RAPBelief self = state.getSelf();
            
            Machinetta.Debugger.debug(1, "Considering RA " + ra);
            if (self.canPerformRole(ra.getRole())) {
                // If agent or robot act autonomously, for person transfer control
                if (self.isAgent() || self.isRobot()) {
                    Machinetta.Debugger.debug(0, "Autonomous RA decision for " + ra);
                    
                    // If taking on this role exceeds resources consider a change
                    if (!haveCapacity(ra.getRole())) {
                        Machinetta.Debugger.debug(0, "Have another role, considering switching");
                        if (Configuration.ROLE_ALLOCATION_ALGORITHM.startsWith("BINARY_BINARY")) {
                            return new BinaryBinaryRAT(ra);
                        } else if (Configuration.ROLE_ALLOCATION_ALGORITHM.startsWith("SCALAR_BINARY")) {
                            return new ScalarBinaryRAT(self, ra);
                        } else if (Configuration.ROLE_ALLOCATION_ALGORITHM.startsWith("BINARY_SCALAR")) {
                            return new BinaryScalarRAT(self, ra);
                        } else if (Configuration.ROLE_ALLOCATION_ALGORITHM.startsWith("SCALAR_SCALAR")) {
                            return new ScalarScalarRAT(self, ra);
                        } else {
                            Machinetta.Debugger.debug(3, "Unimplemented role allocation algorithm. Rejecting role.");
                            return new RejectT(ra);
                        }
                    } else {
                        Machinetta.Debugger.debug(1, "Have resources for " + ra.getRole());
                        if (Configuration.ROLE_ALLOCATION_ALGORITHM.startsWith("BINARY_BINARY") ||
                                Configuration.ROLE_ALLOCATION_THRESHOLD < 0.0) {
                            Machinetta.Debugger.debug(1, "Accepted role " + ra.getRole() + ", have resources");
                            return new AcceptT(ra);
                        } else {
                            Machinetta.Debugger.debug(0, "Creating threshold RA thread");
                            return new SingleShotRoleAssignmentAA.ThresholdRAT(state.getSelf(), ra);
                        }
                    }
                } else {  // I.e., self.isPerson
                    Machinetta.Debugger.debug(0, "Chose to ask RAP about : " + ra.getRole());
                    return new AskT(ra);
                }
            } else {
                Machinetta.Debugger.debug(0, "RAP is not capable of " + ra.getRole());
                return new RejectT(ra);
            }
        } else {
            Machinetta.Debugger.debug(1, "Proxy ignoring " + ra);
        }
        return null;
    }
    
    /**
     * Notice that this does not take into account wait constrained roles.
     * Shouldn't take wait constrained roles into account when calculating availability.
     *
     * FIX.
     */
    protected synchronized boolean haveCapacity(RoleBelief newRole) {
        RAPBelief self = state.getSelf();
        // Determine whether RAP can take on role without exhausting resources
        int reqCap = self.getCapability(newRole.getRoleName()).getLoad(newRole.getParams());
        Machinetta.Debugger.debug(0, "Calculating capacity, space reqd " + reqCap+" for "+ newRole);
        
        for (int i = 0; i < self.getRoles().size(); i++) {
            RoleBelief role = (RoleBelief) state.getBelief(((Belief)self.getRoles().elementAt(i)).getID());
            if (role != null && role.isActive()) {
                String cap = role.getRoleName();
                if (self.getCapability(cap) != null) {
                    reqCap += self.getCapability(cap).getLoad(role.getParams());
                } else {
                    Machinetta.Debugger.debug(3, "Unknown capacity for " + cap + " assuming load = 100");
                    reqCap += 100;
                }
                Machinetta.Debugger.debug(0, "After considering capacity  " + cap + " for role " + role + " required is " + reqCap);
            }
        }
        return reqCap <= 100;
    }
    
    /*++++++++++++++++++++++++++++++++++++++++++++++++++++++++
     * These are the threads that are returned under different
     * circumstances.
     */
    
    /** This class autonomously rejects a role */
    class RejectT extends RoleAssignmentThread {
        public RejectT(RoleAllocationBelief ra) {
            super(ra);
            ra.setAccepted(false);
            Machinetta.Debugger.debug(0, "Rejecting role: " + ra.getRole());
            complete = true;
            state.addBelief(ra.getRole());
            state.addBelief(ra);
            state.notifyListeners();
        }
        
        public void run() {}
    }
    
    /** This class autonomously accepts a role */
    class AcceptT extends RoleAssignmentThread {
        public AcceptT(RoleAllocationBelief ra) {
            super(ra);
            ra.setAccepted(true);
            Machinetta.Debugger.debug(0, "Accepting role: " + ra.getRole());
            complete = true;
            state.addBelief(ra);
            state.notifyListeners();
        }
        
        public void run() {}
    }
    
    /** This class asks the RAP about a role
     *
     * Is something more required here? Possibly not.
     */
    class AskT extends RoleAssignmentThread {
        
        public AskT(RoleAllocationBelief ra) {
            super(ra);
            Machinetta.Debugger.debug(0, "Asking RAP: " + ra.getRole());
            sendMessage(new ProposeRoleMessage(ra));
            ra.askedRAP();
            complete = true;
            state.addBelief(ra);
            state.notifyListeners();
        }
        
        public void run() {}
    }
    
    /** Binary role allocation reasoning
     *
     * This is only created if insufficient resources, hence currently just reject
     */
    class BinaryBinaryRAT extends RoleAssignmentThread {
        
        public BinaryBinaryRAT(RoleAllocationBelief ra) {
            super(ra);
            
            Machinetta.Debugger.debug(1, "Rejected: " + ra.getRole());
            ra.setAccepted(false);
            complete = true;
            state.addBelief(ra);
            state.notifyListeners();
        }
        
        public void run() {}
    }
    
    
    /** Uses threshold to decide whether to take a task */
    class ThresholdRAT extends RoleAssignmentThread {
        
        private boolean haveOfferedCap = false;
        private CapabilityBelief capOffered = null;
        private int offCap = 0;
        
        public ThresholdRAT(RAPBelief self, RoleAllocationBelief ra) {
            super(ra);
            
            capOffered = self.getCapability(ra.getRole().getRoleName());
            
            if (capOffered == null) {
                // Notice that if this message occurs the problem is serious
                // We just kill the thread
                // Should not occur, however, since thread should not be created
                // if capability was not known.
                Machinetta.Debugger.debug(5, "No capability for role ... confused");
                complete = true;
            } else {
                if (capOffered.isDynamic()) {
                    if (ra.getRole().getParams() != null) {
                        capOffered.removeCapability(ra.getRole().getParams());
                    } else {
                        Machinetta.Debugger.debug(3, "Why does role " + ra.getRole() + " have no params?");
                    }
                } else {
                    try {
                        offCap = capOffered.getCapability(ra.getRole().getParams());
                        haveOfferedCap = true;
                    } catch (IllegalArgumentException e) {
                        // Proxy does not know offered capability, request it (in run())
                    }
                }
                
            }
        }
        
        public void beliefChange() {
            if (capOffered == null) {
                Machinetta.Debugger.debug(5, "No capability for role ... confused");
                // What to do? (see above)
                complete = true;
            } else {
                Machinetta.Debugger.debug(0, "Trying again to get capability information.");
                if (!haveOfferedCap) {
                    try {
                        offCap = capOffered.getCapability(ra.getRole().getParams());
                        haveOfferedCap = true;
                    } catch (IllegalArgumentException e) {
                        // Not arrived yet
                        Machinetta.Debugger.debug(0, "Capability information not yet available");
                    }
                }
                if (haveOfferedCap) checkAccept();
            }
        }
        
        public void run() {
            // This is done here so AAImp will be set.
            if (!haveOfferedCap) {
                
                AAObj.requestCapability(ra.getRole());
                
                ra.askedRAP();
                // Following taken out, probably unnecessary
                //state.addBelief(ra);
                //state.notifyListeners();
            } else {
                checkAccept();
            }
        }
        
        private void checkAccept() {
            complete = true;
            if (offCap > Configuration.ROLE_ALLOCATION_THRESHOLD) {
                if (haveCapacity(ra.getRole())) {
                    Machinetta.Debugger.debug(1, "Accepting " + ra.getRole() + " above threshold " + offCap + " > " + Configuration.ROLE_ALLOCATION_THRESHOLD);
                    ra.setAccepted(true);
                } else {
                    // We have accepted another role while getting capability information about this one
                    Machinetta.Debugger.debug(2, "Accepted role while getting capability info for another, will try again: " + ra);
                    ra.reconsider();
                }
            } else {
                Machinetta.Debugger.debug(1, "Rejecting " + ra.getRole() + " below threshold " + offCap + " < " + Configuration.ROLE_ALLOCATION_THRESHOLD);
                ra.setAccepted(false);
            }
            state.addBelief(ra);
            state.notifyListeners();
        }
    }
    
    /** Uses scalar capability information (just distance to fire)
     * and binary priority information (if it is a fire it is important)
     */
    class ScalarBinaryRAT extends RoleAssignmentThread {
        
        public ScalarBinaryRAT(RAPBelief self, RoleAllocationBelief ra) {
            super(ra);
            this.self = self;
            
            capOffered = self.getCapability(ra.getRole().getRoleName());
            
            Vector roles = self.getRoles();
            // Assume only 1 (non-negligible) role for now
            int count = 0;
            currRole = (RoleBelief)roles.elementAt(count);
            while (currRole instanceof RoleAllocationBelief) {
                currRole = (RoleBelief)roles.elementAt(++count);
            }
            
            capCurrent = self.getCapability(currRole.getRoleName());
            
            if (capCurrent == null || capOffered == null) {
                Machinetta.Debugger.debug(5, "No capability for role ... confused");
                // What to do? (see above)
                complete = true;
            } else {
                if (capCurrent.isDynamic() || capOffered.isDynamic()) {
                    // If either of these are dynamic capabilities, request new
                    if (capCurrent.isDynamic()) {
                        capCurrent.removeCapability(currRole.getParams());
                    }
                    if (capOffered.isDynamic()) {
                        capOffered.removeCapability(ra.getRole().getParams());
                    }
                } else {
                    try {
                        currCap = capCurrent.getCapability(currRole.getParams());
                        haveCurrCap = true;
                    } catch (IllegalArgumentException e) {
                        // Proxy does not know current capability, request it (in run())
                    }
                    try {
                        offCap = capOffered.getCapability(ra.getRole().getParams());
                        haveOfferedCap = true;
                    } catch (IllegalArgumentException e) {
                        // Proxy does not know offered capability, request it (in run())
                    }
                }
            }
        }
        
        public void run() {
            if (!haveCurrCap) {
                AAObj.requestCapability(currRole);
            }
            if (!haveOfferedCap) {
                AAObj.requestCapability(ra.getRole());
            }
            if (haveOfferedCap && haveCurrCap) roleChange();
            else {
                ra.askedRAP();
                // Taken out, should be unnecessary
                //state.addBelief(ra);
                //state.notifyListeners();
            }
        }
        
        public void beliefChange() {
            if (capCurrent == null || capOffered == null) {
                Machinetta.Debugger.debug(5, "No capability for role ... confused");
                // What to do?
            } else {
                Machinetta.Debugger.debug(0, "Trying again to get capability information.");
                if (!haveCurrCap) {
                    try {
                        currCap = capCurrent.getCapability(currRole.getParams());
                        haveCurrCap = true;
                    } catch (IllegalArgumentException e) {
                        // Not arrived yet
                    }
                }
                if (!haveOfferedCap) {
                    try {
                        offCap = capOffered.getCapability(ra.getRole().getParams());
                        haveOfferedCap = true;
                    } catch (IllegalArgumentException e) {
                        // Not arrived yet
                    }
                }
                if (haveCurrCap && haveOfferedCap) roleChange();
            }
        }
        
        // Looks at capabilities and decides what to do
        private void roleChange() {
            if (complete) {
                Machinetta.Debugger.debug(0, "Role change called in completed RA:" + ra);
                return;
            }
            Machinetta.Debugger.debug(1, "Have capability information, considering role change: " + currRole.getID() + ": " + currCap + ", " + ra.getRole().getID() + ": " + offCap);
            // This is here to avoid recursive calls
            complete = true;
            if (currCap >= offCap) {
                Machinetta.Debugger.debug(1, "Rejecting " + ra.getRole() + " more capable of current");
                ra.setAccepted(false);
                state.addBelief(ra);
                state.notifyListeners();
            } else {
                // Exchange roles, creating a RA for (former) current
                Machinetta.Debugger.debug(2, "Role change, accepting: " + ra.getRole() + ", giving up: " + currRole);
                
                // Give up current role
                self.removeRole(currRole);
                currRole.setResponsible(null);
                state.addBelief(currRole);
                
                // Now that old role has been dumped check that we have available capacity
                if (haveCapacity(ra.getRole())) {
                    ra.setAccepted(true);
                } else {
                    // We have accepted another role while getting capability information about this one
                    Machinetta.Debugger.debug(2, "Accepted role while switching others, will try again: " + ra);
                    ra.reconsider();
                }
                state.addBelief(ra);
                state.notifyListeners();
            }
        }
        
        private boolean haveOfferedCap = false, haveCurrCap = false;
        private int currCap = 0, offCap = 0;
        private RAPBelief self = null;
        private CapabilityBelief capOffered = null, capCurrent = null;
        private RoleBelief currRole = null;
    }
    
    /** Does allocation using binary capability (which assumes has already been confirmed)
     * and scalar priority
     */
    class BinaryScalarRAT extends RoleAssignmentThread {
        
        public BinaryScalarRAT(RAPBelief self, RoleAllocationBelief ra) {
            super(ra);
            this.self = self;
            
            // Assume only 1 role for now
            currRole = (RoleBelief)((self.getRoles()).elementAt(0));
            currPlan = currRole.getPlan();
            
            offeredPlan = ra.getRole().getPlan();
            
            currPriority = currPlan.prioritize();
            offeredPriority = offeredPlan.prioritize();
            
            if (currPriority == null || offeredPriority == null)
                Machinetta.Debugger.debug(5, "Failed to make one of the priorities! " + currPriority + " " + offeredPriority);
            
            if (currPriority.isDetermined() && offeredPriority.isDetermined())
                roleChange();
            else {
                Machinetta.Debugger.debug(3, "Requesting priority information for " + currPlan.getID() + " (+ " + currPriority.isDetermined() + ") and " + offeredPlan.getID() + "( "+offeredPriority.isDetermined() +"), waiting");
                
                // Just make sure the meta reasoning knows that the priority needs to be found
                state.addBelief(currPriority);
                state.addBelief(offeredPriority);
                state.notifyListeners();
            }
        }
        
        public void run() {}
        
        public void beliefChange() {
            Machinetta.Debugger.debug(0, "Checking priorities again");
            if (currPriority == null || offeredPriority == null) {
                Machinetta.Debugger.debug(3, "Why has one of the priority beliefs been lost? " + currPriority + " " + offeredPriority);
            } else {
                PriorityBelief newCurrPriority = (PriorityBelief)state.getBelief(currPriority.getID());
                currPriority = newCurrPriority == null ? currPriority : newCurrPriority;
                PriorityBelief newOfferedPriority = (PriorityBelief)state.getBelief(offeredPriority.getID());
                offeredPriority = newOfferedPriority == null? offeredPriority : newOfferedPriority;
                
                // Machinetta.Debugger.debug(">>> " + newOfferedPriority + " " + newCurrPriority, 1, this);
                
                if (currPriority.isDetermined() && offeredPriority.isDetermined())
                    roleChange();
                else
                    Machinetta.Debugger.debug(2, "Priority check failed : " + currPriority.getID() + " " + currPriority.isDetermined() + " " + offeredPriority.getID() + " " + offeredPriority.isDetermined());
            }
        }
        
        /** There may be bugs in here */
        private void roleChange() {
            if (complete) {
                Machinetta.Debugger.debug(0, "Role change called in completed RA:" + ra);
                return;
            }
            Machinetta.Debugger.debug(2, "Have priority information, considering role change.");
            // This is here to avoid recursive calls
            complete = true;
            Machinetta.Debugger.debug(1, "Current role priority: "+currPriority.getPriority());
            Machinetta.Debugger.debug(1, "New role priority: "+offeredPriority.getPriority());
            if (currPriority.getPriority().intValue() >= offeredPriority.getPriority().intValue()) {
                Machinetta.Debugger.debug(2, "Rejecting " + offeredPlan.getID() + ", " + currPlan.getID() + " has higher priority.");
                ra.setAccepted(false);
                state.addBelief(ra);
                state.notifyListeners();
            } else {
                // Exchange roles, creating a RA for (former) current
                Machinetta.Debugger.debug(2, "Changing role to " + offeredPlan.getID() + " has higher priority than " + currPlan.getID());
                ra.setAccepted(true);
                self.removeRole(currRole);
                currRole.setResponsible(null);
                RoleAllocationBelief newRA = new RoleAllocationBelief(currRole);
                state.addBelief(newRA);
                state.addBelief(currRole);
                state.addBelief(ra);
                state.notifyListeners();
            }
        }
        
        private PriorityBelief currPriority = null, offeredPriority = null;
        private TeamPlanBelief currPlan = null, offeredPlan = null;
        private RAPBelief self = null;
        private RoleBelief currRole = null;
    }
    
    /**
     * First takes into account priority, if equal, do more capable.
     */
    class ScalarScalarRAT extends RoleAssignmentThread {
        
        ScalarBinaryRAT capTest = null;
        
        public ScalarScalarRAT(RAPBelief self, RoleAllocationBelief ra) {
            super(ra);
            this.self = self;
            
            // Assume only 1 role for now
            currRole = (RoleBelief)((self.getRoles()).elementAt(0));
            currPlan = currRole.getPlan();
            
            offeredPlan = ra.getRole().getPlan();
            
            currPriority = currPlan.prioritize();
            offeredPriority = offeredPlan.prioritize();
            
            if (currPriority == null || offeredPriority == null)
                Machinetta.Debugger.debug(5, "Failed to make one of the priorities! " + currPriority + " " + offeredPriority);
            
            if (currPriority.isDetermined() && offeredPriority.isDetermined())
                roleChange();
            else {
                Machinetta.Debugger.debug(1, "Requesting priority information for " + currPlan.getID() + " (+ " + currPriority.isDetermined() + ") and " + offeredPlan.getID() + "( "+offeredPriority.isDetermined() +"), waiting");
                
                // Just make sure the meta reasoning knows that the priority needs to be found
                state.addBelief(currPriority);
                state.addBelief(offeredPriority);
                state.notifyListeners();
            }
        }
        
        public void run() {}
        
        public void beliefChange() {
            Machinetta.Debugger.debug(0, "Checking priorities again");
            if (capTest != null) {
                capTest.beliefChange();
                if (capTest.complete()) complete = true;
            } if (currPriority == null || offeredPriority == null) {
                Machinetta.Debugger.debug(3, "Why has one of the priority beliefs been lost? " + currPriority + " " + offeredPriority);
            } else {
                PriorityBelief newCurrPriority = (PriorityBelief)state.getBelief(currPriority.getID());
                currPriority = newCurrPriority == null ? currPriority : newCurrPriority;
                PriorityBelief newOfferedPriority = (PriorityBelief)state.getBelief(offeredPriority.getID());
                offeredPriority = newOfferedPriority == null? offeredPriority : newOfferedPriority;
                
                // Machinetta.Debugger.debug(">>> " + newOfferedPriority + " " + newCurrPriority, 1, this);
                
                if (currPriority.isDetermined() && offeredPriority.isDetermined())
                    roleChange();
                else
                    Machinetta.Debugger.debug(2, "Priority check failed : " + currPriority.getID() + " " + currPriority.isDetermined() + " " + offeredPriority.getID() + " " + offeredPriority.isDetermined());
            }
        }
        
        /** There may be bugs in here */
        private void roleChange() {
            if (complete) {
                Machinetta.Debugger.debug(0, "Role change called in completed RA:" + ra);
                return;
            }
            
            Machinetta.Debugger.debug(2, "Have priority information, considering role change.");
            // This is here to avoid recursive calls
            complete = true;
            Machinetta.Debugger.debug(1, "Current role priority: "+currPriority.getPriority());
            Machinetta.Debugger.debug(1, "New role priority: "+offeredPriority.getPriority());
            if (currPriority.getPriority().intValue() > offeredPriority.getPriority().intValue()) {
                Machinetta.Debugger.debug(2, "Rejecting " + offeredPlan.getID() + ", " + currPlan.getID() + " has higher priority.");
                ra.setAccepted(false);
                state.addBelief(ra);
                state.notifyListeners();
            } else if (currPriority.getPriority().intValue() == offeredPriority.getPriority().intValue()) {
                capTest = new ScalarBinaryRAT(self, ra);
                capTest.setAAObj(AAObj);
                capTest.run();
            } else {
                // Exchange roles, creating a RA for (former) current
                Machinetta.Debugger.debug(2, "Changing role to " + offeredPlan.getID() + " has higher priority than " + currPlan.getID());
                ra.setAccepted(true);
                self.removeRole(currRole);
                currRole.setResponsible(null);
                RoleAllocationBelief newRA = new RoleAllocationBelief(currRole);
                state.addBelief(newRA);
                state.addBelief(currRole);
                state.addBelief(ra);
                state.notifyListeners();
            }
        }
        
        private PriorityBelief currPriority = null, offeredPriority = null;
        private TeamPlanBelief currPlan = null, offeredPlan = null;
        private RAPBelief self = null;
        private RoleBelief currRole = null;
    }
    
    /*++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/
    private ProxyState state = new ProxyState();
}
