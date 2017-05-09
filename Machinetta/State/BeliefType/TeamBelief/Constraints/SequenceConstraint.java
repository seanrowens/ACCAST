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
 * SequenceConstraint.java
 *
 * Created on June 7, 2004, 7:33 PM
 */

package Machinetta.State.BeliefType.TeamBelief.Constraints;

import Machinetta.State.*;

import java.util.Vector;

/**
 * Currently, both roles are allowed to activate, but second
 * can only start once first is complete.
 *
 * @author  pscerri
 */
public class SequenceConstraint extends RoleConstraint {
    
    /** Actually just ids of roles ... */
    public Machinetta.State.BeliefID first = null, second = null;
    public int idNo = 0;
    
    /** Required for auto create XML */
    public SequenceConstraint() {}
    
    /** Creates a new instance of SequenceConstraint */
    public SequenceConstraint(Machinetta.State.BeliefID r1, Machinetta.State.BeliefID r2) {
        first = r1;
        second = r2;
        idNo = (new java.util.Random()).nextInt();
        id = makeID();
    }
    
    public boolean canActivate(Machinetta.State.BeliefID r, java.util.Collection completed) {
        return true;
    }
    
    public boolean canStart(Machinetta.State.BeliefID r, java.util.Collection otherAllocated, java.util.Collection completed) {
        if (r.equals(first)) {
            Machinetta.Debugger.debug( 3,"Allowing first in sequence to start");
            return true;
        } else if (r.equals(second)) {
            boolean canStart = completed.contains(first);
            Machinetta.Debugger.debug( 3,"Second in sequence can start? " + canStart);
        } else {
            Machinetta.Debugger.debug( 1,"Sequence constraint asked about unknown role: " + r + " (" + first + " -> " + second + ")");
            return false;
        }
        return false;
    }
    
    public RoleConstraint instantiate(java.util.Vector templateRoles, java.util.Vector instRoles) {
        Machinetta.State.BeliefID r1 = null, r2 = null;
        for (int j = 0; j < templateRoles.size(); j++) {
            Machinetta.State.BeliefType.TeamBelief.RoleBelief bel = (Machinetta.State.BeliefType.TeamBelief.RoleBelief)templateRoles.elementAt(j);
            if (bel.getID().equals(first)) {
                Machinetta.Debugger.debug( 0,"Instantiating first with " + instRoles.elementAt(j));
                r1 = ((Machinetta.State.BeliefType.Belief)instRoles.elementAt(j)).getID();
            } else if (bel.getID().equals(second)) {
                Machinetta.Debugger.debug( 0,"Instantiating second with " + instRoles.elementAt(j));
                r2 = ((Machinetta.State.BeliefType.Belief)instRoles.elementAt(j)).getID();
            }
        }
        
        if (r1 == null || r2 == null) {
            Machinetta.Debugger.debug( 3,"Failed to instantiate sequence constraint: " + first + ":  " + r1 + " " + second + " : " + r2);
            return null;
        }
        return new SequenceConstraint(r1, r2);
    }
    
    public Machinetta.State.BeliefID makeID() {
        return new BeliefNameID("SequenceConstraint:"+idNo);
    }
    
    public static final long serialVersionUID = 1L;    
}
