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
 * ORConstraint.java
 *
 * Created on March 24, 2004, 12:52 PM
 */

package Machinetta.State.BeliefType.TeamBelief.Constraints;

import Machinetta.State.*;

import java.util.*;

/**
 *
 * @author  pscerri
 */
public class XORConstraint extends RoleConstraint {
    
    /** Actually just ids of roles ... */
    public Vector roles = null;
    public int idNo = 0;
    
    /** Creates a new instance of ORConstraint */
    public XORConstraint(Vector roles) {
        this.roles = roles;
        idNo = (new java.util.Random()).nextInt();
        id = makeID();
    }
    
    public XORConstraint() {}
    
    public boolean canActivate(Machinetta.State.BeliefID r, java.util.Collection completed) {
        return true;
    }
    
    /**
     * Assume that if another of the OR roles is in otherAllocated, then this should not
     * be.  Potentially, we could have a race condition where both arrive (and are added to otherAllocated)
     * before either has been given go ahead to start ...
     */
    public boolean canStart(Machinetta.State.BeliefID r, java.util.Collection otherAllocated, java.util.Collection  completed) {
        boolean ok = true;
        java.util.Collection c = null;
        for (int i = 0; ok && i < roles.size(); i++) {
            BeliefID rc = (BeliefID)roles.get(i);
            Machinetta.Debugger.debug( 0,"Checking: " + rc + " against " + r + " and " + otherAllocated);
            if (otherAllocated.contains(rc) && !rc.equals(r)) {
                ok = false;
                Machinetta.Debugger.debug( 0,"Failed constraint");
            }
        }
        return ok;
    }
    
    /**
     * This is ugly, but appears it might work.  Relies on templateRoles being the same objects
     * as those in roles, which they should be.
     */
    public RoleConstraint instantiate(java.util.Vector templateRoles, java.util.Vector instRoles) {
        Vector<BeliefID> newIDs = new Vector<BeliefID>(roles.size());
        // This is very ugly ....
        for (int i = 0; i < roles.size(); i++) {
            Machinetta.Debugger.debug( 0,"Trying to instantiate: " + roles.elementAt(i));
            for (int j = 0; j < templateRoles.size(); j++) {
                Machinetta.State.BeliefType.TeamBelief.RoleBelief bel = (Machinetta.State.BeliefType.TeamBelief.RoleBelief)templateRoles.elementAt(j);
                if (bel.getID().equals(roles.elementAt(i))) {
                    Machinetta.Debugger.debug( 0,"Instantiating with " + instRoles.elementAt(j));
                    newIDs.add(((Machinetta.State.BeliefType.Belief)instRoles.elementAt(j)).getID());
                    break;
                }
            }
        }
        return new XORConstraint(newIDs);
    }
    
    public Machinetta.State.BeliefID makeID() {
        return new BeliefNameID("ORconstraint:"+idNo);
    }
    
    public static final long serialVersionUID = 1L;
    
}
