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
 * RoleAllocationBelief.java
 *
 * Created on 18 September 2002, 20:38
 */

package Machinetta.State.BeliefType.TeamBelief;

import Machinetta.Configuration;
import Machinetta.State.BeliefNameID;
import Machinetta.State.BeliefsXML;
import Machinetta.State.BeliefType.*;
import Machinetta.State.BeliefID;
import Machinetta.State.ProxyState;

import java.util.Hashtable;
import org.w3c.dom.*;
import java.util.HashMap;

/**
 * This is a role/belief that a role needs to be allocated.
 *
 * @author  scerri
 */
public class RoleAllocationBelief extends RoleBelief {
    
    /** Hashmap of proxy ids of RAPs that have already been offered (and rejected)
     * this role */
    public HashMap<String,Object> askedRAP = new HashMap<String,Object>();
    
    /** Number of times this role allocation belief has been passed completely around
     */
    public int cycles = 0;
    
    /** The role that this role is attempting to allocate */
    public RoleBelief role = null;
    
    /** This value is used to determine whether two RAs are the duplicates or the same RA
     *
     * If there is a conflict the one with the <b> higher </b> value for this number
     * should be <b> destroyed </b>.
     *
     * @Deprecated
     */
    public double id;
    { id = (new java.util.Random()).nextDouble(); }
    
    
    /** True if this RAP has considered accepting offer */
    protected boolean hasConsidered = false;
    
    /** True if this RAP has accepted role
     * value is meaningless unless hasConsidered == true.
     */
    protected boolean accepted = false;
    
    /** True if waiting for a decision from the RAP */
    protected boolean asked = false;
    
    /** True if this role has already been passed on */
    protected boolean passedOn = false;
    
    /** Required for auto create XML */
    public RoleAllocationBelief() {}
    
    /** Creates a new instance of RoleAllocationBelief */
    public RoleAllocationBelief(RoleBelief role) {
        this(role,0);
    }
    
    /** Creates a new instance of RoleAllocationBelief */
    public RoleAllocationBelief(RoleBelief role, int numCycles) {
        super(makeID(role), "RoleAllocation");
        
        Machinetta.Debugger.debug( 0,"Created RA for " + role);
        
        this.role = role;
        this.cycles = numCycles;
        
        // Who is this assigned to?  Look to configuration
        String defaultResponsible = Configuration.DEFAULT_ALLOCATION_RESPONSIBLITY;
        if (defaultResponsible == null)
            // If no one specified, then assign to self (default configuration)
            setResponsible(state.getSelf());
        else
            setResponsible(state.getRAPBelief(new NamedProxyID(defaultResponsible)));
    }
    
    /** Append a RAP to the history
     * (this would be private, but it's used for testing as well)
     * @param rap An ID string representing the RAP being asked
     */
    public void appendRAP(String rapID) {
        askedRAP.put(rapID, null);
    }
    
    /** Append a RAP to the history
     * (this would be private, but it's used for testing as well)
     * @param rap A RAPBelief representing the RAP being asked
     */
    public void appendRAP(RAPBelief rap) { appendRAP(rap.getProxyID().toString()); }
    
    
    /********************************
     * Access
     *******************************/
    public RoleBelief getRole() { return role; }
    public boolean hasConsidered() { return hasConsidered; }
    public boolean waitingForRAP() { return asked; }
    public void askedRAP() { asked = true; }
    public boolean hasAccepted() { return accepted; }
    
    public static BeliefNameID makeID(RoleBelief role) { return new BeliefNameID("RA:" + role.getID()); }
    
    public BeliefID makeID() { return new BeliefNameID("RA:" + role.getID()); }
    
    /**
     * Say whether this proxy's RAP has accepted this role. <br>
     * Will add to RAP if accepted
     */
    public void setAccepted(boolean val) {
        if (!isActive()) {
            Machinetta.Debugger.debug( 3,"Accepting a role in inactive RA: " + role);
        }
        
        hasConsidered = true;
        asked = false;
        deactivate();
        
        // If accepted by this RAP, set responsibility on the role
        if (val) {
            role.setResponsible(state.getSelf());
            state.addBelief(role);
        }
        accepted = val;
    }
    
    /** Force reconsideration of this role */
    public void reconsider() {
        hasConsidered = false;
        asked = false;
    }
    
    /** Merges the info from another role allocation belief into this belief
     * @param allocation Another role-allocation belief to merge into this one
     *
     * @Deprecated Not needed by MACoordination
     */
    public void merge(RoleAllocationBelief allocation) {
        /* Changed by Paul (10/2/03 10:48 AM)
         * Assume that active RA has arrived so just merge the histories
         *
        if (allocation.numCycles() < numCycles()) {
            // This belief is newer than the mergee, so ignore
        } else {
            // This belief is no newer than the mergee
            // First, bring cycle count into synch
            while (allocation.numCycles() > numCycles())
                resetHistory();
            // Then, merge in the history being passed in
            askedRAP.putAll(allocation.askedRAP);
        }
         **/
        cycles += allocation.numCycles();
        askedRAP.putAll(allocation.askedRAP);
    }
    
    /** @return The number of times this role allocation belief has made a complete cycle through the team
     */
    public int numCycles() { return cycles; }
    
    /** Clears the history and increments the record of cycles iterated
     * @return The number of times this role allocation belief has made a complete cycle through the team
     */
    public int resetHistory() {
        
        askedRAP.clear();
        // Add self to list of those that have been asked
        appendRAP(state.getSelf());
        hasConsidered = false;
        accepted = false;
        asked = false;
        passedOn = false;
        return ++cycles;
    }
    
    /** @return a proxyId if this role allocation is to be passed to another RAP
     * otherwise return null
     */
    public ProxyID passOn() {
        Machinetta.Debugger.debug( 0,"Passed On called in " + toString());
        if (!passedOn && hasConsidered && !asked && !hasAccepted() /* && role.getResponsible() == null */) {
            
            // Only allow this code to be entered once.
            passedOn = true;
            
            ProxyID candidate = null;
            
            // Make sure self is in history
            appendRAP(state.getSelf());
            
            candidate = binaryAllocation();
            
            // if no allocation is possible (sensible?) either create a
            // RA of this RA (i.e., meta reasoning) or let the RA sleep
            // for a while before restarting
            if (candidate == null) {
                if (Configuration.ROLE_ALLOCATION_META_REASONING) {
                    // Create role allocation belief
                    Machinetta.Debugger.debug( 1,"Creating RA RA for " + role);
                    RoleAllocationBelief rara = new RoleAllocationBelief(this);
                    state.addBelief(rara);
                    state.notifyListeners();
                } else {
                    // Put the belief to sleep for a while before starting the reallocation
                    // process again
                    final RoleAllocationBelief self = this;
                    (new Thread() {
                        public void run() {
                            Machinetta.Debugger.debug( 1,"Unfilled RA for " + role + " sleeping for " + (Configuration.UNFILLED_ROLE_SLEEP_TIME * Math.min(5, cycles)));
                            try { sleep( Configuration.UNFILLED_ROLE_SLEEP_TIME * Math.min(5, cycles) ); } catch (InterruptedException e) {}
                            
                            role = (RoleBelief)state.getBelief(role.getID());
                            if (role.isActive() /*&& role.getResponsible() == null */) {
                                cycles = resetHistory();
                                passedOn = false;
                                self.setResponsible(state.getSelf());
                                reactivate();
                                Machinetta.Debugger.debug( 1,"RA for " + role + " restarted");
                                state.addBelief(self);
                                state.notifyListeners();
                            } else {
                                Machinetta.Debugger.debug( 1,"While ra was asleep became irrelevant: " + role.isActive() + " or " + role.getResponsible());
                            }
                        }
                    }).start();
                }
            }
            
            Machinetta.Debugger.debug( 0,"Passing role to " + candidate + " " + role);
            return candidate;
            
        } else {
            Machinetta.Debugger.debug( 0,"RoleAllocation belief for " + role + " not passed on. Already passed on? " + passedOn + " RAP has considered? " + hasConsidered + " RAP has been asked? " + asked + " RAP has accepted? " + hasAccepted() + " Role filled: " + role.getResponsible());
            return null;
        }
    }
    
    /** Fills in instance-specific values of this belief by lookup in the provided table
     *
     * Only handles param "Role"
     */
    public RoleBelief instantiate(Hashtable params) {
        if (params.containsKey("Role")) {
            RoleBelief newRole = (RoleBelief)params.get("Role");
            return new RoleAllocationBelief(newRole);
        }
        return null;
    }
    
    public String toString() {
        return "Role allocation of " + role.toString() + " considered? " + hasConsidered + " RAPAsked? " + asked + " history " + askedRAP;
    }
    
    /********************************
     * End access
     *******************************/
    
    /* Different role allocation algorithms */
    
    /* Simple binary role allocation, passes to someone not checked before.
     *
     * Information must show that RAP is capable.
     */
    private ProxyID binaryAllocation() {
        RAPBelief [] allRAPs = state.getAllRAPBeliefs();
        int threshold = Configuration.ALLOCATION_EFFORT_THRESHOLD;
        int percentage = Configuration.ALLOCATION_EFFORT_PERCENTAGE;
        int capableCount = 0;
        int askedCount = 0;
        RAPBelief nextRAP = null;
        int maxCapability = 0;
        
        for (int i = 0; i < allRAPs.length; i++) {
            RAPBelief possibleRAP = allRAPs[i];
            
            int specificCapability = 0;
            
            CapabilityBelief capability = possibleRAP.getCapability(role.getRoleName());
            try {
                if (capability != null) {
                    specificCapability = capability.getCapability(role.getParams());
                } else {
                    Machinetta.Debugger.debug( 0,"RAP " + possibleRAP + " cannot perform " + role);
                }
            } catch (IllegalArgumentException e) {
                /** If there's no specific capability information about this role,
                 * use the generic "canPerformRole" method and assume maximum
                 * capability if true */
                if (possibleRAP.canPerformRole(role) && nextRAP == null) {
                    specificCapability = 100;
                    Machinetta.Debugger.debug( 0,"No specific information about whether RAP " + possibleRAP.getProxyID() + " can perform " + role + ", assuming 100");
                }
            }
            if (specificCapability >= threshold) {
                Machinetta.Debugger.debug( 0,"RAP " + possibleRAP + " has sufficient capability");
                // This RAP has the minimum required capability
                capableCount++;
                if (askedRAP.containsKey(possibleRAP.getProxyID().toString())) {
                    askedCount++;
                    Machinetta.Debugger.debug( 0,"But already asked");
                } else if (nextRAP == null) {
                    askedCount++;
                    nextRAP = possibleRAP;
                    maxCapability = specificCapability;
                } else if (Configuration.ALLOCATION_SELECTION.equalsIgnoreCase("BEST")) {
                    // We want to find the most capable RAP, not just the first available
                    if (specificCapability > maxCapability) {
                        nextRAP = possibleRAP;
                        maxCapability = specificCapability;
                    }
                }
            }
        }
        
        Machinetta.Debugger.debug(0,"Already asked "+(askedCount-1)+"/"+capableCount+" RAPs about role: "+role);
        if (capableCount > 0 && 100*askedCount/capableCount > percentage) {
            // We have exceeded our allotted percentage of RAP asking
            Machinetta.Debugger.debug(0,"Cannot ask any more RAPS about role: "+role);
            return null;
        } else if (nextRAP != null) {
            // Found candidate to send role to
            // Make sure that the role is not marked as being allocated to somone
            role.setResponsible(null);
            Machinetta.Debugger.debug( 1,"Offering " + nextRAP.getProxyID() + " role " + role + " @ " + System.currentTimeMillis());
            return nextRAP.getProxyID();
        } else {
            Machinetta.Debugger.debug( 1,"No candidate RAP found for " + role);
            return null;
        }
    }
    
    /**
     * Equals is true only if this is the same RA, governed by
     * the fact that ids are the same as everything else. <br>
     *
     * The isDuplicate() method can be used to see if two RAs refer to the
     * same role
     */
    public boolean equals(Object obj) {
        if (obj == null || obj.getClass() != getClass()) return false;
        RoleAllocationBelief rab = (RoleAllocationBelief) obj;
        boolean ret = rab.role.equals(role) && rab.id == id;
        //Machinetta.Debugger.debug( 3,"Saying: " + this + " and " + rab + " match? " + ret);
        return ret;
    }
    
    /**
     * Returns true only if these refer to the same role but are
     * not the same RA.
     */
    public boolean isDuplicate(RoleAllocationBelief rab) {
        return rab.role.equals(role) && rab.id != id;
    }
    
    public static final long serialVersionUID = 1L;
}
