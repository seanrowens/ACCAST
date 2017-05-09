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
 * RoleConstraint.java
 *
 * Created on March 12, 2004, 2:53 PM
 */

package Machinetta.State.BeliefType.TeamBelief.Constraints;

import Machinetta.State.BeliefType.TeamBelief.*;
/**
 *
 * @author  pscerri
 */
public abstract class RoleConstraint extends Machinetta.State.BeliefType.Belief {
    
    /** Creates a new instance of RoleConstraint */
    public RoleConstraint() {
    }
    
    /** Should return true iff it is OK to start allocating this role. */
    public abstract boolean canActivate(Machinetta.State.BeliefID r, java.util.Collection completed);
    
    /** Should return true iff it is OK for the agent to start acting on r, given
     * that roles in otherAllocated have agents allocated to them */
    public abstract boolean canStart(Machinetta.State.BeliefID r, java.util.Collection otherAllocated, java.util.Collection completed);
    
    /** Instantiate this constraint with real BeliefIDs. <br>
     * should assume that Role in index i in templateRoles corresponds to Role at index i in instRoles
     */
    public abstract RoleConstraint instantiate(java.util.Vector templateRoles, java.util.Vector instRoles);
}
