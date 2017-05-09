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
 * HVAA.java
 *
 * Created on March 17, 2006, 5:03 PM
 *
 */

package AirSim.Machinetta;

import AirSim.Environment.Assets.Tasks.TaskType;
import Machinetta.AA.SimpleAA;
import Machinetta.RAPInterface.OutputMessages.NewRoleMessage;
import Machinetta.RAPInterface.OutputMessages.RoleCancelMessage;
import Machinetta.State.BeliefType.TeamBelief.RoleBelief;
import java.awt.Point;
import java.util.Hashtable;

/**
 *
 * @author pscerri
 */
public class HVAA extends SimpleAA {
    
    /** Creates a new instance of HVAA */
    public HVAA() {
        // Testing
        /*
        BasicRole basic = new BasicRole(TaskType.move);
        basic.params = new Hashtable();
        basic.params.put("Destination", new Point(49750, 49750));
        addRole(basic);
        
        BasicRole basic = new BasicRole(TaskType.hold);
        addRole(basic);
         *         
         */
    }
    
    protected void removeRole(RoleBelief r) {
        RoleCancelMessage msg = new RoleCancelMessage(r);
        messageToRAP(msg);
    }
    
    protected void addRole(RoleBelief r) {
        NewRoleMessage msg = new NewRoleMessage(r);
        messageToRAP(msg);
    }
    
}
