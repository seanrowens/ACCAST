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
 * AcceptRoleMessage.java
 *
 * Created on 20 September 2002, 08:59
 */

package Machinetta.RAPInterface.InputMessages;

import Machinetta.State.BeliefType.TeamBelief.RoleAllocationBelief;
import Machinetta.State.BeliefType.TeamBelief.RoleBelief;

/**
 * Message sent from RAP to proxy, accepting or rejecting a role.
 *
 * @author  scerri
 */
public class AcceptRoleMessage extends InputMessage {
    
    /** Creates a new instance of ProposeRoleMessage */
    public AcceptRoleMessage(RoleAllocationBelief ra, boolean accept) {
        this.ra = ra;
        this.accept = accept;
    }
  
    /** Returns the role being offered */
    public RoleBelief getRole() { return ra.getRole(); }

    /** Returns whether or not the role has been accepted */
    public boolean roleAccepted() { return accept; }
    
    /** Returns the role allocation belief for allocating this role */
    public RoleAllocationBelief getRoleAllocationBelief () { return ra; }
    
    public String toString () { 
        return "RAP offered role : " + ra.getRole();
    }
    
    /** The role that the proxy is offering to the RAP */    
    private RoleAllocationBelief ra = null;

    /** True iff the role has been accepted by the RAP */
    private boolean accept = false;
    
    public static final long serialVersionUID = 1L;
}
