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
 * SSLocalComms.java
 *
 * Created on 2 August 2002, 15:21
 */

package Machinetta.Communication;

import Machinetta.State.BeliefType.NamedProxyID;
import Machinetta.State.BeliefType.ProxyID;
import Machinetta.Communication.Message;
import Machinetta.Communication.TextMessage;
import Machinetta.RAPInterface.InputMessages.InputMessage;
import Machinetta.RAPInterface.LocalCommsRAP;
import LocalComms.Server;
import LocalComms.ClientIDMessage;

import java.io.*;
import java.net.*;
import java.util.Vector;
import Machinetta.Proxy;


/**
 *
 * @author  scerri
 */
public class SSLocalComms extends CommImplementation implements Serializable {
    
    /** Creates a new instance of LocalComms */
    public SSLocalComms() {
        if (out == null || in == null) {
            try {
                Socket s = new Socket(Machinetta.Configuration.LOCAL_COMMS_SERVER, Proxy.serverPort);
                out = new ObjectOutputStream(s.getOutputStream());
                in = new ObjectInputStream(s.getInputStream());
                (new Monitor()).start();
            } catch (IOException e) {
                Machinetta.Debugger.debug( 5,"Could not open local comms socket to "+Machinetta.Configuration.LOCAL_COMMS_SERVER+" port "+Proxy.serverPort+" : " + e);
            }
	    catch (Exception e) {
                Machinetta.Debugger.debug( 10,"Exception, not open local comms socket to "+Machinetta.Configuration.LOCAL_COMMS_SERVER+" port "+Proxy.serverPort+" : " + e);
		e.printStackTrace();
	    }
        } else {
            Machinetta.Debugger.debug( 5,"Reinstantiating SSLocalComms!");
        }
    }
    
    /** Implement the receiving of messages
     * watch out for incoming messages while transferring ...
     */
    public Message[] recvMessages() {
        Message [] ret = new Message[newMessages.size()];
        for (int i = 0; i < newMessages.size(); i++) {
            //Machinetta.Debugger.debug( 1,"Got message " + newMessages.elementAt (i));
            ret[i] = (Message)newMessages.elementAt(i);
        }
        newMessages.clear();
        return ret;
    }
    
    /** Implement the sending of messages  */
    public void sendMessage(Message m) {
        allSend(m);
    }
    
    /** For the RAP Interface to send messages */
    public static void sendToRAP(Machinetta.RAPInterface.OutputMessages.OutputMessage m) {
        allSend(m);
    }
    
    static long lastSendTime = 0;
    
    private static void allSend(final Object o) {
        try {
            synchronized(out) {
                out.writeObject(o);
                out.flush();
                out.reset();
            }
        } catch (IOException e) {
            Machinetta.Debugger.debug( 5,"Send failed : " + e);
        } catch (Exception e2) {
            Machinetta.Debugger.debug(5,"Some error sending " + e2);
        }
        lastSendTime = System.currentTimeMillis();       
    }
    
    public void setProxyID(ProxyID id) {
        super.setProxyID(id);
        Machinetta.Debugger.debug( 1,"Sending ID to LocalComms server");
        ClientIDMessage msg = new ClientIDMessage(id.toString());
        allSend(msg);
    }
    
    /** If this is being used as both an input stream for communication messages and RAP messages
     * this variable should be set */
    private static LocalCommsRAP RAPInt = null;
    public static void setRAPInterface(LocalCommsRAP r) { RAPInt = r; }
    
    
    /** Monitors socket waiting for incoming messages */
    class Monitor extends Thread {
        public void run() {
            Object o = null;
            int exceptions = 0;
            boolean exceptionCaught = false;
            do {
                o = null;
                try  {
                    exceptionCaught = false;
                    o = in.readObject();
                    if (o instanceof Message) {
                        Message m = (Message)o;
                        newMessages.add(m);
                        incomingMessage();
                    } else if (o instanceof Machinetta.RAPInterface.InputMessages.InputMessage) {
                        RAPInt.incomingMessage((InputMessage)o);
                    }
                } catch (EOFException e1) {
                    Machinetta.Debugger.debug( 5,"End of file?");
                    break;
                } catch (Exception e) {
                    Machinetta.Debugger.debug( 5,"Read failed : " + e + " for message " + o);
                    e.printStackTrace (System.out);
                    exceptions++;
                    exceptionCaught = true;
                }
            } while ((o != null || exceptionCaught) && exceptions < 6);
            try {
                in.close();
                out.close();
		//		System.exit(1);
            } catch (IOException e) {
                Machinetta.Debugger.debug( 1,"Failed to close sockets");
            }
        }
    }
    
    private static ObjectOutputStream out = null;
    private static ObjectInputStream in = null;
    private Vector<Message> newMessages = new Vector<Message>();
    public static final long serialVersionUID = 1L;
    
    /** Just for testing */
    public static void main(String argv[]) {
        Machinetta.Debugger.debug( 1,"Local comms running.");
        
        
        final ProxyID id = new NamedProxyID("Sid");
        /*
        for (int i = 0; i < 10; i++) {
            String content = (new Integer(i)).toString();
            comms.sendMessage(new TextMessage(id, content));
        }
         **/
        /*
        for (int i = 0; i < 10; i++) {
            (new Thread() {
                public void run() {
                    SSLocalComms comms = new SSLocalComms();
                    for (int i = 0; i < 20; i++) {
                        try {
                            sleep(200);
                        } catch (Exception e) {}
                        Machinetta.DomainSpecific.AirSim.DestroyTarget b = new Machinetta.DomainSpecific.AirSim.DestroyTarget();
                        b.targetID = 1;
                        String content = "<Beliefs>"+ Machinetta.State.BeliefsXML.toXML(b)+"</Beliefs>";
                        comms.sendMessage(new TextMessage(id, content));
                    }
                }
            }).start();
        }
        */
    }
}
