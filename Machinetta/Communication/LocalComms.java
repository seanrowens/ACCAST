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
 * LocalComms.java
 *
 * Created on 2 August 2002, 15:21
 */

package Machinetta.Communication;

import Machinetta.Configuration;
import Machinetta.State.ProxyState;
import Machinetta.State.BeliefType.NamedProxyID;
import Machinetta.State.BeliefType.ProxyID;
import Machinetta.Communication.Message;
import Machinetta.Communication.TextMessage;
import Machinetta.RAPInterface.InputMessages.InputMessage;
import Machinetta.RAPInterface.RAPInterface;
import Machinetta.RAPInterface.LocalCommsRAP;

import LocalComms.*;

import java.io.*;
import java.net.*;
import java.util.Vector;
import Machinetta.Proxy;

/**
 *
 * @author  scerri
 */
public class LocalComms extends CommImplementation implements Serializable {
    
    /** Creates a new instance of LocalComms */
    public LocalComms() {
        try {
	        // Changed by Jijun Wang
            Socket s = new Socket (Configuration.LOCAL_COMMS_SERVER, Server.SERVER_PORT);
            out = new ObjectOutputStream (s.getOutputStream ());
            in = new ObjectInputStream (s.getInputStream ());            
        } catch (IOException e) {
            Machinetta.Debugger.debug( 5,"Could not open local comms socket to "+Configuration.LOCAL_COMMS_SERVER+" port "+Server.SERVER_PORT+" : " + e);
        }
    }
    
    public void setProxyID (ProxyID id) { 
    	this.id = id; 
    	(new Monitor ()).start ();
    }
    
    /** Implement the receiving of messages  */
    public Message[] recvMessages() {
        Message [] ret = new Message[newMessages.size()];
        newMessages.toArray(ret);
        newMessages.clear();
        return ret;
    }
    
    /** Implement the sending of messages  */
    public void sendMessage(Message m) {
        try {
            synchronized(out) {
                out.writeObject(m);
                out.flush();
                out.reset();
            }
        } catch (IOException e) {
            Machinetta.Debugger.debug( 5,"Send failed : " + e);
        }
    }
    
    /** Monitors socket waiting for incoming messages */
    class Monitor extends Thread {
        public void run() {
            
            synchronized(out) {
                // Wait a couple of seconds then send clientID message
                // Need to wait until RAPBelief is created
                try {
                    sleep(1000);
                } catch (InterruptedException e) {}
                
                // The following is changed by Jijun Wang to fix the register bug.
                try {
                    int count = 0;
                    while(id==null && count++<600)
                        Thread.sleep(100); // wait untill I have a valid id
                    Machinetta.Debugger.debug( 1,"Sending id to server: " + id);
                    out.writeObject(new ClientIDMessage(id.toString()));
                    out.flush();
                } catch (Exception e) {
                    Machinetta.Debugger.debug( 5,"Unable to send clientID message to LocalComms Server\n"+e);
                    e.printStackTrace();
                }
            }
            
            int errors = 0;
            Object o = null;
            do {
                try  {
                    o = in.readObject();
                    if (o instanceof Message) {
                        Message m = (Message)o;
                        newMessages.add(m);
                        incomingMessage();
                    } else {
                        Machinetta.Debugger.debug( 5,"Unknown type received: " + o.getClass());
                    }
                } catch (java.lang.OutOfMemoryError e) {
                    Machinetta.Debugger.debug( 8,"Out of memory trying to read message:" + e);
                    e.printStackTrace();
                } catch (Exception e) {
                    Machinetta.Debugger.debug( 5,"Read failed : " + e);
                    e.printStackTrace();
                    errors++;
                }
            } while (o != null && errors < 10);
            Machinetta.Debugger.debug( 3,"Closing comms socket");
            try {
                in.close();
                out.close();
            } catch (IOException e) {
                Machinetta.Debugger.debug( 1,"Failed to close sockets");
            }
        }
    }
    
    private ObjectOutputStream out = null;
    private ObjectInputStream in = null;
    private Vector<Message> newMessages = new Vector<Message>();
    
    public static void main(String argv[]) {
        Machinetta.Debugger.debug( 1,"Local comms running.");
        LocalComms comms = new LocalComms();
        
        ProxyID id = new NamedProxyID("Sid");
        for (int i = 0; i < 10; i++) {
            String content = (new Integer(i)).toString();
            comms.sendMessage(new TextMessage(id, content));
        }
    }
    
    public static final long serialVersionUID = 1L;
}
