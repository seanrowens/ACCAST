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
 * RoleConflict.java
 *
 * Created on August 25, 2003, 10:51 AM
 */

package Machinetta.State.BeliefType.TeamBelief;

import Machinetta.State.BeliefType.*;
import Machinetta.State.*;

import java.util.Random;

/**
 *
 * @author  pscerri
 */
public class RoleConflictBelief extends Belief {
    
    public double myValue = 0.0;
    public double lowestOther = 1.0; 
    public RAPBelief lowestRAP = null;
    public RoleBelief role;
    
    /** Creates a new instance of RoleConflict */
    public RoleConflictBelief() {
        Random r = new Random();
        myValue = r.nextDouble();
        lowestRAP = (new ProxyState()).getSelf();
    }
    
    public RoleConflictBelief(RoleBelief r) {
        this();
        role = r;
        id = makeID();
    }
    
    /** Assuming that the rc to merge comes from another proxy and the rc being merged with 
     * is local. */
    public void merge (RoleConflictBelief rc) {
        Machinetta.Debugger.debug( 0,"Merging RC belief " + rc);
        if (lowestOther >= rc.myValue) {
            lowestOther = rc.myValue;
            if (lowestOther <= myValue)
                lowestRAP = rc.lowestRAP;
        }         
    }
    
    public boolean myRole() { return lowestRAP.isSelf(); }
    
    /** Create a BeliefID for this particular Belief
     * This method should be overloaded by subclasses
     * It will be used by auto create from XML to make an ID
     * once all fields are filled in.
     * If fields are not properly filled in, this method should return null
     * and auto XML will give an error
     * <br>
     * Callees wanting to know the id of this object should call getID(),
     * although the BeliefIDs returned by getID and this should be equal
     */
    public BeliefID makeID() {
        return makeID(role);
    }
    
    public static BeliefID makeID(RoleBelief role) {
        return new BeliefNameID("RoleConflict:" + role.getID()); 
    }
    
    public String toString() {
        return "RoleConflict for " + role + ": myValue " + myValue + " others " + lowestOther + " by " + lowestRAP.getProxyID();
    }
    
    public static final long serialVersionUID = 1L;
}
