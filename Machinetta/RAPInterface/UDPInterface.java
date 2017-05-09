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
 * UDPInterface.java
 *
 * Created on June 14, 2005, 12:31 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package Machinetta.RAPInterface;

import Machinetta.Communication.UDPCommon;
import Machinetta.Communication.UDPMessage;
import Machinetta.State.BeliefType.NamedProxyID;
import java.io.*;
import java.net.*;

/**
 * I don't believe this is used, at present.  RAPInterfaces that are using UDP
 * could potentially subclass from it, but don't.
 *
 * @author pscerri
 */
public class UDPInterface extends RAPInterfaceImplementation {
    
    protected UDPCommon backend = new UDPCommon() {
        public boolean processMessage(DatagramPacket packet) {
            
            Machinetta.Debugger.debug( 3,"Recieved here ... unimplemented ... ");
            
            if (UDPMessage.isRAPOutput(packet.getData(), proxy.getProxyID())) {
                UDPMessage msg = UDPMessage.getMessage(packet.getData());
                                
                return true;
            } else {
                return false;
            }
            
        }
    };
    
    /** Creates a new instance of UDPInterface */
    public UDPInterface() {
    }
    
    public void sendMessage(Machinetta.RAPInterface.OutputMessages.OutputMessage msg) {
        backend.sendMessage(new UDPMessage(proxy.getProxyID(), new NamedProxyID(UDPMessage.SIM_NAME), msg));
    }
    
    public void run() {
        // Don't need this, the actual input processing is done in UDPCommon
    }
    
    public Machinetta.RAPInterface.InputMessages.InputMessage[] getMessages() {
        return null;
    }
    
}
