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
 * ProxyInterface.java
 *
 * Created on August 12, 2005, 11:44 AM
 *
 */

package AirSim.Commander;

import Machinetta.Debugger;
import Machinetta.RAPInterface.InputMessages.InputMessage;
import Machinetta.RAPInterface.OutputMessages.OutputMessage;
import Machinetta.RAPInterface.RAPInterfaceImplementation;
import Machinetta.State.StateChangeListener;
import Machinetta.State.BeliefID;
import Machinetta.State.BeliefType.Belief;

/**
 * @deprecated
 *
 * @author pscerri
 */
public class ProxyInterface extends RAPInterfaceImplementation {
    CommanderGUI gui = null;
    /** Creates a new instance of ProxyInterface */
    public ProxyInterface() {
	gui = new CommanderGUI();
    }
    
    public void run() {
        Machinetta.Debugger.debug("Not implemented (run) ", 3, this);
    }
    
    /** Sends a message to the RAP
     *
     * @param msg The message to send
     */
    public void sendMessage(OutputMessage msg) {
        Machinetta.Debugger.debug("Not implemented (sendMessage) ", 3, this);
    }
    
    /** Called to get list of new messages
     * Should return only those messages received since last called.
     *
     * @return List of InputMessage objects received from RAP since last called.
     */
    public InputMessage [] getMessages() {
        Machinetta.Debugger.debug("Not implemented (getMessages)", 3, this);
        return null;
    }
}
