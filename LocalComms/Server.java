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
 * Server.java
 *
 * Created on 2 August 2002, 15:28
 */

package LocalComms;

import Machinetta.Communication.Message;
import Machinetta.RAPInterface.OutputMessages.OutputMessage;
import Machinetta.State.BeliefType.*;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Runs a server for passing proxy messages on a local machine
 *
 * @author  scerri
 */
public class Server extends Thread {
    
    /** Creates a new instance of Server */
    public Server() {
        //start ();
    }
    
    public void run() {
        
        ServerSocket ss = null;
        
        try {
            ss = new ServerSocket(SERVER_PORT);
        } catch (IOException e) {
            Machinetta.Debugger.debug(5, "Could not open local comms server: " + e);
            System.exit(-1);
        }
        
        Machinetta.Debugger.debug(1, "Server waiting for connections.");
        while (true) {
            try {
                ClientThread c = new ClientThread(ss.accept());
                // Vector storage for legacy reasons, use hashtable version
                clients.addElement(c);
            } catch (IOException e) {
                Machinetta.Debugger.debug(5, "Server exception " + e);
                break;
            }
        }
        
        try {
            ss.close();
        } catch (IOException e) {
            Machinetta.Debugger.debug(3, "Could not close socket.");
        }
    }
    
    public static void main(String argv []) {
        (new Server()).start();
    }
    
    /** Port on which to communicate with the server */
    public static int SERVER_PORT = 7045;
    
    /** Vector of currently connected client threads
     * @Deprecated
     */
    protected Vector<ClientThread> clients = new Vector<ClientThread>();
    
    /** Hashtable of currently connected clientThreads
     *
     */
    protected Hashtable<String,ClientThread> clientThreads = new Hashtable<String,ClientThread>();
    
    /** What the server does with messages
     *
     * ThreadID so handler knows which client this message came from
     */
    protected void handleMessage(Message m, int threadID) {
        
        ClientThread c = null;
        c = (ClientThread) clientThreads.get(m.getDest().toString());
        
        //System.out.println("Sending message " + /* m + */ "from " + threadID + " to " + c);
        int id = -1;
        if (c == null) {
            // Hack to get id of dest in some special cases
            String name = m.getDest().toString();
            // A couple of hacks for FireBrigades and Sheperds
            if (name.startsWith("FireBrigade")) {
                try {
                    id = Integer.parseInt(name.substring(11)) - 1;
                } catch (NumberFormatException e) {
                    Machinetta.Debugger.debug(5, "Could not parse FB ID " + name);
                }
            } else if (name.startsWith("Sheperd")) {
                try {
                    id = Integer.parseInt(name.substring(7)) - 1;
                } catch (NumberFormatException e) {
                    Machinetta.Debugger.debug(5, "Could not parse FB ID " + name);
                }
            }
            
            if (id >= 0) {
                if (id != threadID) {
                    Machinetta.Debugger.debug(0, "Sending msg to special (FireBrigade or Sheperd) " + id);
                    c = (ClientThread)clients.elementAt(id);
                } else {
                    // Message for this proxy
                    // Machinetta.Debugger.debug("Message to myself?: " + m, 3, this);
                }
            }
        }
        
        if (c == null) {
            if (id != threadID)
                Machinetta.Debugger.debug(1, "Message to " + m.getDest() + " not sent because id unknown");
        } else {
            /*
            String p = m.toString().substring(18,80);
            p = p.substring(0, (p.indexOf("\n") > 0 ? p.indexOf("\n") : p.length()));
            Machinetta.Debugger.debug(0, "Sending " + threadID + " -> " + m.getDest() + ":" + p);
             */
            // Hack for getting message traces
            /*
             if (m instanceof Message) {
                System.out.println("\nMESSAGE from FireBrigade" + (threadID+1) + " to " + m.getDest() + "\n " + m + "\nEND MESSAGE");
            }
             */
            
            sendMessage(m, c);
        }
    }
    
    /** Message to be sent to RAP (for SimpleSim) */
    protected void RAPMessage(OutputMessage m, int threadID) {}
    
    /** Allows keeping of statistics on number of messages sent */
    protected int messagesSent = 0;
    
    /** More detailed message keeping */
    protected Hashtable messageData = new Hashtable();
    
    /** Access for the simple simulators to send messages to proxies */
    public void sendMessage(Object o, int id) {
        try {
            sendMessage(o, (ClientThread)clients.elementAt(id));
        } catch (ArrayIndexOutOfBoundsException e) {
            Machinetta.Debugger.debug(1, "Trying to send to non-existent client: " + id);
        }
    }
    
    /** Allows a message to be sent to a particular id **/
    public void sendMessage(Object o, ClientThread c) {
        
        // Just checking
        if (o == null) {
            System.out.println("Server tried to send null message");
            (new Exception()).printStackTrace();
            return;
        }
        
        // Update stats
        messagesSent++;
        
        //Machinetta.Debugger.debug(0, "Sending " + o.getClass() + " to " + c);
        if (c != null) {
            c.send(o);
        }
    }
    
    /** Class handling a single connection */
    class ClientThread extends Thread {
        public ClientThread(Socket s) {
            this.s = s;
            try {
                out = new ObjectOutputStream(s.getOutputStream());
                in = new ObjectInputStream(s.getInputStream());
            } catch (IOException e) {
                Machinetta.Debugger.debug(5, "Client exception " + e);
            }
            
            Machinetta.Debugger.debug(0, "Connection to client open.");
            
            start();
            sendThread.start();
        }
        
        /**
         * Getting rid of simulator lockups, testing:
         * - checked whether remaining bytes on in after read: no.
         * - having a new thread handle the message fixed the problem, will shift this to
         * handleMessage
         */
        public void run() {
            
            Object o = null;
            boolean done = false;
            int errors = 0;
            
            do {
                try {
                    
                    o = in.readObject();
                    if (o instanceof Message) {
                        handleMessage((Message)o, clients.indexOf(this));
                    } else if (o instanceof OutputMessage) {
                        RAPMessage((OutputMessage)o, clients.indexOf(this));
                    } else if (o instanceof ClientIDMessage) {
                        clientThreads.put(((ClientIDMessage)o).id.toString(), this);
                        Machinetta.Debugger.debug(1, "Registered client at ID " + ((ClientIDMessage)o).id);
                    } else {
                        Machinetta.Debugger.debug(5, "Unknown object recvd: " + o.getClass() + " : " + o);
                    }
                    
                } catch (ClassNotFoundException e) {
                    Machinetta.Debugger.debug(1, "Unknown class " + e);
                    errors++;
                } catch (EOFException e) {
                    Machinetta.Debugger.debug(1, "End of file, server closed");
                    done = true;
                } catch (SocketException e) {
                    Machinetta.Debugger.debug(3, "Socket exception: " + e);
                    //done = true;
                    errors++;
                } catch (IOException e) {
                    Machinetta.Debugger.debug(5, "Read failed : " + e);
                    errors++;
                } catch (ClassCastException e) {
                    Machinetta.Debugger.debug(5, "Class cast problem with " /* + o */ + " : " + e);
                    errors++;
                } catch (Exception e) {
                    Machinetta.Debugger.debug(5, "Client error : " + e);
                    e.printStackTrace();
                    errors++;
                }
            } while (o != null && !done && errors < 10);
            
            /*
            for (Enumeration e = clientThreads.keys(); e.hasMoreElements(); ) {
                Machinetta.Debugger.debug(1, "Key : " + e.nextElement());
            }
             */
            
            try {
                clientThreads.remove(this);
                out.close();
                in.close();
            } catch (IOException e) {
                Machinetta.Debugger.debug(1, "Problem closing client socket " + e);
            }
        }
        
        public void send(final Object o) {
            pending.add(o);
            synchronized(sendThread) {
                sendThread.notify();
            }
        }
        
        Vector<Object> pending = new Vector<Object>();
        Thread sendThread = new Thread() {
            public void run() {
                while(true) {
                    synchronized (out) {
                        try  {
                            if (pending.size() > 0) {
                                Object o = pending.remove(0);
                                out.writeObject(o);
                                out.flush();
                            }
                        } catch (SocketException e) {
                            Machinetta.Debugger.debug(3, "Client closed socket: " + e);
                        } catch (IOException e) {
                            Machinetta.Debugger.debug(5, "Write failed : " + e);
                        } catch (Exception e) {
                            Machinetta.Debugger.debug(5, "Error sending to client " + e);
                        }
                    }
                    if (pending.size() == 0) {
                        try {
                            synchronized(this) {
                                wait();
                            }
                        } catch (InterruptedException e) {}
                    }
                }
            }
        };
        
        private Socket s = null;
        private ObjectOutputStream out = null;
        private ObjectInputStream in = null;
    }
}
