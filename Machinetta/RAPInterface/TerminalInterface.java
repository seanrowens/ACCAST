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
 * Terminal.java
 *
 * Created on 9 July 2002, 14:22
 */

package Machinetta.RAPInterface;

import Machinetta.RAPInterface.InputMessages.*;
import Machinetta.RAPInterface.OutputMessages.*;
import Machinetta.Communication.Communication;
import Machinetta.Communication.TextMessage;
import Machinetta.State.BeliefType.*;
import Machinetta.State.ProxyState;
import Machinetta.State.BeliefsXML;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

/**
 * This is a simple RAP interface that allows a Person to test things from a terminal
 *
 * @author  scerri
 */
public class TerminalInterface extends RAPInterfaceImplementation {
    
    /** Creates a new instance of Terminal */
    public TerminalInterface() {
    }
    
    /** Main loop */
    public void run() {
        // Sits in a loop, getting user input
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        String line = null;
        boolean done = false;
        try {
            sleep(1000);
        } catch (InterruptedException e) {}
        while (!done) {
            Machinetta.Debugger.debug( 5,"What next master? ");
            InputMessage msg = null;
            try {
                line = in.readLine();
            } catch (java.io.IOException e) {
                Machinetta.Debugger.debug( 5,"Sorry, something went wrong, say it again");
            }
            if (line.equalsIgnoreCase("enough")) {
                done = true;
            } else if (line.startsWith("Avail")) {
                StringTokenizer tok = new StringTokenizer(line);
                tok.nextToken(); // Avail
                float f = new Float(tok.nextToken()).floatValue();
                msg = new OverallAvailabilityMessage(f);
            } else if (line.startsWith("send")) {
                /* KQML-specific command for testing the sending of messages */
                StringTokenizer tokens = new StringTokenizer(line);
                tokens.nextToken(); // ignore "send"
                NamedProxyID receiverID = new NamedProxyID(tokens.nextToken());
                String msgStr = "";
                while (tokens.hasMoreTokens())
                    msgStr = msgStr + tokens.nextToken() + " ";
                TextMessage outgoingMsg = new TextMessage(receiverID, msgStr.trim());
                comms.sendMessage(outgoingMsg);
            } else if (line.startsWith("belief")) {
                /** XML representation of belief to add */
                String xmlContent = line.substring(line.indexOf(' '));
                org.w3c.dom.Document doc = BeliefsXML.getDocumentFromString(xmlContent);
                if (doc != null) {
                    /** Extract beliefs from XML document and add them to ProxyState */
                    Belief newBeliefs [] = BeliefsXML.getBeliefs(doc);
                    for (int beliefIndex=0; beliefIndex < newBeliefs.length; beliefIndex++) {
                        Belief newBelief = newBeliefs[beliefIndex];
                        state.addBelief(newBelief);
                    }
                    state.notifyListeners();
                }
            } else {
                msg = new FreeformTextMessage(line);
            }
            if (msg != null) {
                // Assume last message retrieved
                retVal = new InputMessage[1];
                retVal[0] = msg;
                notifyProxy();
            }
        }
        /** Bit of a hack to get KQML comms to shut down */
        comms.sendMessage(new TextMessage(null,"(stop)"));
        Machinetta.Debugger.debug( 1,"Terminal Interface done");
    }
    
    /** Called to get list of new messages
     * Should return only those messages received since last called.
     *
     * @return List of InputMessage objects received from RAP since last called.
     */
    public InputMessage[] getMessages() {
        return retVal;
    }
    
    /** Sends a message to the RAP
     *
     * @param msg The message to send
     */
    public void sendMessage(OutputMessage msg) {
        Machinetta.Debugger.debug( 5,"MSG: " + msg);
    }
    
    // Handle for storing messages
    InputMessage [] retVal = null;
    
    private Communication comms = new Communication();
    private ProxyState state = new ProxyState();
}
