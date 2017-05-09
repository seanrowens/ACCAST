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
 * RoleAssignmentThread.java
 *
 * Created on 21 October 2002, 20:18
 */

package Machinetta.AA.RoleAssignment;

import Machinetta.AA.AAImplementation;
import Machinetta.RAPInterface.OutputMessages.OutputMessage;
import Machinetta.State.BeliefType.TeamBelief.RoleAllocationBelief;
import Machinetta.State.ProxyState;

import java.util.Vector;
/**
 * Runs the AA for a single role allocation
 *
 * @author  scerri
 */
public abstract class RoleAssignmentThread extends Thread {
    
    /** Creates a new instance of RoleAssignmentThread */
    public RoleAssignmentThread(RoleAllocationBelief bel) {
        this.ra = bel;
    }
    
    /** Runs any time dependant actions for this role assignment */
    public abstract void run();
    
    /** Called to indicate that the role allocation belief
     * that this thread is for has changed
     */
    public void beliefChange() {}
    
    /** Returns true if the thread has completed its work and can be destroyed */
    public boolean complete() { return complete; }
    
    protected void sendMessage(OutputMessage msg) {
        if (AAObj == null) {
            if (unsentMessages == null) unsentMessages = new Vector<OutputMessage>();
            unsentMessages.addElement(msg);
        }
    }
    
    public String toString() {
        return "Role Assignment AA for " + ra;
    }
    
    public void setAAObj(AAImplementation aa) {
        AAObj = aa;
        for (int i = 0; unsentMessages != null && i < unsentMessages.size(); i++) {
            AAObj.messageToRAP((OutputMessage)unsentMessages.elementAt(i));
        }
        unsentMessages = null;
    }
    
    private Vector<OutputMessage> unsentMessages = null;    
    
    /** The role allocation object that this is for */
    public RoleAllocationBelief ra = null;
    
    /** The AA obj for sending messages */
    public AAImplementation AAObj = null;
    
    /** True iff this object can be destroyed */
    protected boolean complete = false;
    
    /** A proxy state object for changing belief state */
    protected static ProxyState state = new ProxyState();
}
