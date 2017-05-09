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
 * JIAgentBelief.java
 *
 * Created on March 2, 2004, 2:48 PM
 */

package Machinetta.State.BeliefType.MAC;

import Machinetta.Coordination.MAC.JIAgent;
import Machinetta.State.BeliefID;
import Machinetta.State.BeliefType.ProxyID;

/**
 *
 * @author  pscerri
 */
public class JIAgentBelief extends MABelief {
    
    public JIAgent.JI_STATE JIstate = null;
    
    public Machinetta.State.BeliefID planID, roleID = null;
    
    public Machinetta.State.BeliefType.ProxyID informer = null;
    
    public int capability = -1;
    public int time = -1;
        
    public BeliefID DIRRole = null;

    public ProxyID teammate = null;
    
    public ProxyID DIRPerformer = null;
    
    /** Creates a new instance of JIAgentBelief */
    public JIAgentBelief() {
    }
    
    public JIAgentBelief(Machinetta.Coordination.MAC.JIAgent jia) {
        super(jia);
        planID = jia.getPlanID();
        roleID = jia.getRoleID();
        JIstate = jia.getJIState();
        informer = jia.getInformer();
        capability = jia.getCapability();
        time = jia.getScheduleTime();
        DIRPerformer = jia.getDIRPerformer();
        DIRRole = jia.getDIRRole();
        teammate = jia.getTeammate();
    }
    
    public String toString() { return "JIAgent for " + planID +":" + roleID + " in state " + JIstate + " from " + informer; }
    
    public final static long serialVersionUID = 1L;
}
