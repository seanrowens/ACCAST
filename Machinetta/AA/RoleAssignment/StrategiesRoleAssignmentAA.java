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
 * StrategiesRoleAssignmentAA.java
 *
 * Created on 18 September 2002, 22:15
 */

package Machinetta.AA.RoleAssignment;

import Machinetta.State.BeliefType.TeamBelief.*;
import Machinetta.State.ProxyState;

/**
 * Uses strategy model from JAIR'02 paper
 *
 * Not implemented .....
 *
 * @author  scerri
 */
public class StrategiesRoleAssignmentAA implements RoleAssignmentAA {
    
    /** Creates a new instance of StrategiesRoleAssignmentAA */
    public StrategiesRoleAssignmentAA() {
    }
        
    /** This method should eventually result in the role either being
     * accepted or rejected by the RAP.
     *
     * If the method is going to take non-negligible time to complete
     * then it should create a new thread and return immediately.
     */
    public RoleAssignmentThread offeredRole(RoleAllocationBelief role) {
        return null;
    }
    
    private ProxyState state = new ProxyState();
}
