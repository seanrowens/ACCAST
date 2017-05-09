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
 * RAP.java
 *
 * Created on 10 September 2002, 15:12
 */

package Machinetta.State.BeliefType;

import Machinetta.State.*;
import Machinetta.State.BeliefType.TeamBelief.*;

import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Vector;
import org.w3c.dom.*;

// Temporary for position information
import java.awt.Point;
import java.util.StringTokenizer;

/**
 * Captures domain independant information about local or other RAPs
 *
 * @author  scerri
 */
public class RAPBelief extends Belief {
    
    /** ProxyID that refers to this RAP */
    private ProxyID proxyID = null;
    
    /** True if this RAPBelief contains information about local RAP */
    public boolean isSelf = false;
    
    /** A string describing the type of RAP this belief refers to
     * Should be one of : agent, robot or person
     */
    public String species = "agent";
    
    /** Hashtable of RAP CapabilityBeliefH, indexed by capability name */
    public Hashtable<String,CapabilityBelief> CapabilityBeliefH = new Hashtable<String,CapabilityBelief>();
    
    /** A hashtable of misc. information about the RAP */
    public Hashtable<String,Object> miscInfo = new Hashtable<String,Object>();
    
    /** Vector of current roles */
    public Vector<RoleBelief> roles = new Vector<RoleBelief>();
    
    
    /** Needed for auto create XML */
    public RAPBelief() {}
    
    /** Creates a new instance of RAPBelief
     *
     * ProxyID.toString used to generate a new BeliefNameID
     */
    public RAPBelief(ProxyID proxyID) {
        super(new BeliefNameID(proxyID.toString()));
        this.proxyID = proxyID;
    }
    
    public RAPBelief(ProxyID proxyID, boolean isSelf, Hashtable<String,CapabilityBelief> caps) {
        this(proxyID);
        this.isSelf = isSelf;
        this.CapabilityBeliefH = caps;
        Machinetta.Debugger.debug( 0,"Caps " + CapabilityBeliefH);
    }
    
    /** Version for create from XML, though can be used by anyone */
    public RAPBelief(ProxyID proxyID, boolean isSelf, Hashtable<String,CapabilityBelief> caps, Hashtable<String,Object> misc) {
        this(proxyID);
        this.isSelf = isSelf;
        this.CapabilityBeliefH = caps;
        this.miscInfo = misc;
        Machinetta.Debugger.debug( 0,"Caps " + CapabilityBeliefH);
    }
    
    /** Creates a new instance of RAPBelief
     *
     * @Deprecated Use RAPBelief(ProxyID id), which uses a standard method
     * to create a belief ID.
     */
    public RAPBelief(BeliefID id, ProxyID proxyID) {
        super(id);
        this.proxyID = proxyID;
    }
    
    /** Tries to combine the information from the new rap belief
     * with what is already here.
     *
     * For now, ignore everything in new because everything is "static" (i.e., not dynamic)!
     */
    public void merge(RAPBelief rap) {
        if (!isSelf()) {
            // Blindly assume for now that incoming information is better
            this.CapabilityBeliefH = rap.CapabilityBeliefH;
            this.miscInfo = rap.miscInfo;
            // Role stuff is not sent in XML
            //this.roles = rap.roles;
        }
    }
    
    /*++++++++++++++++++ ACCESS METHODS +++++++++++++++++++*/
    
    /** Returns true if this belief refers to the local RAP */
    public boolean isSelf() { return isSelf; }
    
    public void setIsSelf(boolean isSelf) { this.isSelf = isSelf; }
    
    /** Returns true if the RAP could possibly be capable of fulfilling
     * this role.  Present circumstances or other roles are not be
     * taken into account. */
    public boolean canPerformRole(RoleBelief role) {
               
        if (role == null) {
            Machinetta.Debugger.debug( 5,"Why is role null?");
            return false;
        } 
        
	String roleName = role.getRoleName();
	CapabilityBelief capBelief = CapabilityBeliefH.get(roleName);
        Machinetta.Debugger.debug(1,"Checking if RAP "+proxyID+" is capable of " + role.getRoleName() + " -> " + (null != capBelief));
        
        return (null != capBelief);
    }
    
    /** Returns the capability object matching a particular role name */
    public CapabilityBelief getCapability(String roleName) {
        return (CapabilityBelief)CapabilityBeliefH.get(roleName);
    }
    
    /** String with ID */
    public String toString() { return "RAP: ID : " + proxyID + " " + CapabilityBeliefH; }
    
    /** Returns ProxyID belief that refers to this RAP */
    public ProxyID getProxyID() { return proxyID; }
    
    /** Allows the ProxyID of this belief to be set - hack for RAPBelief */
    public void setProxyID(ProxyID id) { proxyID = id; }
    
    /** Returns a string describing type of this RAP
     * Will be one of: robot, agent or person
     */
    public String getRAPType() { return species; }
    
    /** True iff RAP is a person */
    public boolean isPerson() { return species.equalsIgnoreCase("person"); }
    
    /** True iff RAP is a robot */
    public boolean isRobot() { return species.equalsIgnoreCase("robot"); }
    
    /** True iff RAP is a agent */
    public boolean isAgent() { return species.equalsIgnoreCase("agent"); }
    
    /** Add a role to this RAPBelief
     *
     * This only records the fact that the RAP is doing the role
     * it does not give the role to the RAP.
     */
    public void addRole(RoleBelief role) {
        for (int i = 0; i < roles.size(); i++) {
            if (((RoleBelief)roles.elementAt(i)).getID().equals(role.getID())) {
                Machinetta.Debugger.debug( 2,"Attempted to add role to RAP more than once: " + role);
                return;
            }
        }
        Machinetta.Debugger.debug( 0,"Role " + role + " added to RAP belief " + this);
        // To avoid circular, this is taken out
        // role.setResponsible(this);
        roles.addElement(role);
    }
    
    /** Removes a role from this RAPBelief
     *
     * Only records the fact that it is now believed that this
     * RAP is not responsible for the role, does not do anything with RAP
     */
    public void removeRole(RoleBelief role) {
        boolean found = false;
        
        for (Enumeration e = roles.elements(); e.hasMoreElements(); ) {
            RoleBelief existingRole = (RoleBelief)e.nextElement();
            if (existingRole.getID().equals(role.getID())) {
                if (roles.remove(existingRole)) {
                    Machinetta.Debugger.debug( 0,"Successfully removed : " + existingRole);
                    found = true;
                } else {
                    Machinetta.Debugger.debug( 3,"Failed to remove : " + existingRole);
                }
            }
        }
        
        if (!found) {
            Machinetta.Debugger.debug( 3,"Attempted to remove role RAP does not have " + role);
            for (int i = 0; i < roles.size(); i++) {
                Machinetta.Debugger.debug( 3,"Does have role : " + roles.elementAt(i));
            }
        }
    }
    
    public boolean hasRole(RoleBelief role) {
        boolean found = false;
        
        for (Enumeration e = roles.elements(); e.hasMoreElements(); ) {
            RoleBelief existingRole = (RoleBelief)e.nextElement();
            if (existingRole.getID().equals(role.getID())) {                
                found = true;
                break;
            }
        }
        
        return found;
    }
    
    /** Returns a Vector of the roles that are currently known
     * to be assigned to this RAP
     *
     * Actual vector is returned - so don't break!
     */
    public Vector getRoles() { return roles; }
    
    /** Add a piece of miscellaneous information about this particular RAP */
    public void addMiscInformation(String name, Object value) {
        miscInfo.put(name, value);
    }
    
    /** Get miscellaneaous information about this particular RAP */
    public Object getMiscInformation(String name) {
        return miscInfo.get(name);
    }
    
    public BeliefID makeID() {
        if (proxyID != null)
            return new BeliefNameID(proxyID.toString());
        else
            return new BeliefNameID("AnonymousRAP");
    }
    
    public boolean equals(Object o) {
        if (! (o instanceof RAPBelief)) return false;
        RAPBelief rap = (RAPBelief)o;
        return proxyID.toString().equalsIgnoreCase(rap.getProxyID().toString());
    }
    
    /*++++++++++++++++ END ACCESS METHODS ++++++++++++++++++*/
    
    public void debugPrintRoles(int level) {
        Machinetta.Debugger.debug( level,"Current roles:");
        for (Enumeration e = roles.elements(); e.hasMoreElements(); ) {
            Machinetta.Debugger.debug( level,e.nextElement() + " ");
        }
        Machinetta.Debugger.debug( level,"End roles");
    }
    
    public static final long serialVersionUID = 1L;
}
