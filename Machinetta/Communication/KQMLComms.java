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
 * KQMLComms.java
 *
 * Created on August 5, 2002, 5:11 PM
 */

package Machinetta.Communication;

import Machinetta.State.BeliefType.ProxyID;
import Machinetta.State.BeliefType.NamedProxyID;
import Machinetta.Communication.Message;
import Machinetta.Communication.KQMLMessage;
import Machinetta.Communication.TextMessage;
import Machinetta.Configuration;

import java.io.*;
import java.net.*;
import java.util.Vector;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;

/** Communication implementation based on KQML.
 * @author pynadath
 */
public class KQMLComms extends CommImplementation implements Serializable {
    
    /** Creates a new instance of KQMLComms */
    public KQMLComms() {
        Machinetta.Debugger.debug( 1,"Starting");
        /** Start listening for connections */
        listener = new KQMLListener();
        listener.start();
        /** Establish connection with ANS */
        ANSHost = Configuration.KQML_ANS_HOST;
        ANSPort = Configuration.KQML_ANS_PORT;
        Connection conn = new Connection(ANS,ANSHost,ANSPort);
        conn.start();
    }
    
    /** Sets the proxy ID for use with this comm implementation.
     *  Also, translates ID into KQML-compatible name and registers the agent under said name with the ANS.
     * @param id
     */
    public void setProxyID(ProxyID id) {
        super.setProxyID(id);
        /** Determine my "KQML-friendly" name */
        myName = id.toString();
        /** On second thought, let's not do this
         * myName = myName.replace(' ','_');
         */
        /** Register myself with ANS */
        this.register();
    }
    
    /** Implement the sending of messages
     * @param m
     */
    public void sendMessage(final Message m) {
        if ((m.getDest() != null) && (m.getDest().matches(this.id))) {
            Machinetta.Debugger.debug(1,"Not sending message to myself: "+m);
        } else if (m instanceof TextMessage) {
            new Thread() {
                public void run() {
                    KQMLMessage msg = null;
                    if (m instanceof KQMLMessage)
                        msg = (KQMLMessage)m;
                    else {
                        String text = ((TextMessage)m).getText();
                        if (text.equalsIgnoreCase(STOP_MSG)) {
                            /** An inelegant way of getting this thing to shut down */
                            close();
                            return;
                        }
                        msg = new KQMLMessage(m.getDest(), "(tell :content ("+text+"))");
                    }
                    if (!sendKQMLMessage(msg))
                        Machinetta.Debugger.debug(4,"Unable to send message "+m);
                }
            }.start();
        }
        else
            Machinetta.Debugger.debug( 3,"Cannot send " + m.toString());
    }
    
    /** Sends a KQML message to its intended recipient
     * @param msg
     * KQML message to be sent
     * @return
     * true iff message is sent successfully
     */
    private boolean sendKQMLMessage(KQMLMessage msg) {
        String receiver = msg.get(":receiver");
        Connection conn = null;
        if (receiver == null) {
            /** Try to extract intended receiver from destination */
            ProxyID dest = msg.getDest();
            if ((dest instanceof NamedProxyID) && (dest != null)) {
                receiver = ((NamedProxyID)dest).getName();
                msg.put(":receiver",receiver);
            }
            else {
                Machinetta.Debugger.debug(4,"Missing receiver field in message "+msg.toString());
                return false;
            }
        }
        /* Add sender field if missing */
        if (msg.get(":sender") == null)
            msg.put(":sender",myName);
        Machinetta.Debugger.debug( 0,"Sending " + msg);
        /* Look for an existing connection first */
        if (receiver.equals(ANSName))
            conn = (Connection)connectionTable.get(ANS);
        else
            conn = (Connection)connectionTable.get(receiver);
        if (conn == null)
            Machinetta.Debugger.debug(1,"No existing connection to "+receiver);
        else
            /* Try to use existing connection first */
            try {
                conn.sendMsg(msg.toString());
                Machinetta.Debugger.debug(0,"Sent message along existing connection to "+receiver);
                return true;
            } catch (IOException e) {
                /* Existing connection is no longer valid */
                Machinetta.Debugger.debug(2,"Unable to use existing connection to "+receiver);
                conn.close();
            }
        if ((receiver.equals(ANSName)) || (receiver.equalsIgnoreCase(ANS))) {
            /** re-establish new connection to ANS */
            conn = new Connection(ANS,ANSHost,ANSPort);
            conn.start();
        } else
            /* Establish new connection */
            conn = lookupAgent(receiver);
        if (conn == null)
            Machinetta.Debugger.debug(1,"Unable to establish new connection to "+receiver);
        else {
            try {
                conn.sendMsg(msg.toString());
                Machinetta.Debugger.debug(0,"Sent message along new connection to "+receiver);
                return true;
            } catch (IOException e) {
                /* Unable to send message through the established connection */
                conn.close();
                Machinetta.Debugger.debug(2,"I/O Error sending message "+msg);
            }
        }
        /* We have failed, so hang our heads in shame */
        return false;
    }
    
    /** Implement the receiving of messages */
    public synchronized Message [] recvMessages() {
        Message [] ret = new Message[newMessages.size()];
        for (int i = 0; i < newMessages.size(); i++) {
            ret[i] = (Message)newMessages.elementAt(i);
        }
        newMessages.clear();
        return ret;
    }
    
    /** Adds a KQML message to the queue of received messages
     * @param msg
     * A received KQML message
     */
    private synchronized void addMessage(KQMLMessage msg) {
        newMessages.add(msg);
    }
    
    
    /** Registers the agent with the ANS. */
    private void register() {
        Machinetta.Debugger.debug(2,"Registering with ANS");
        Connection conn = (Connection)connectionTable.get(ANS);
        try {
            /** (register :content (<name> <host> <port>) :reply-with <label>) */
            String registerMsg = "(register :content ("+myName+" "+myHost+" " +myPort+") :sender "+myName+" :reply-with "+LABEL_REGISTER+")";
            conn.sendMsg(registerMsg);
        } catch (IOException e) {
            Machinetta.Debugger.debug(5,"Unable to register with ANS: "+e.getMessage());
        }
    }
    
    /** Looks up an agent by name in the ANS
     * @param name
     * The string name of the agent to look up
     * @return
     * A connection to that agent if able to successfully lookup and contact; otherwise, null
     */
    private Connection lookupAgent(String name) {
        return lookupAgent(name,5);
    }
    
    /** Looks up an agent by name in the ANS
     * @param name
     * The string name of the agent to look up
     * @param waitTime
     * The maximum number of seconds to wait before giving up on the lookup
     * @return
     * A connection to that agent if able to successfully lookup and contact; otherwise, null
     */
    private Connection lookupAgent(String name,int waitTime) {
        Machinetta.Debugger.debug(2,"Looking up agent "+name);
        if (ANSName == null)
            return null;
        KQMLMessage msg = new KQMLMessage(null, "(ask-one :receiver "+ANSName+
        " :reply-with "+LABEL_LOOKUP+" :content "+name+")");
        if (!sendKQMLMessage(msg))
            return null;
        for (int timer=0; timer<waitTime*10; timer++) {
            Connection conn = (Connection)connectionTable.get(name);
            if (conn != null)
                return conn;
            else
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    /** Should we do something here? */
                }
        }
        Machinetta.Debugger.debug(4,"Unable to find agent "+name);
        return null;
    }
    
    /** Process KQML administration messages received from the ANS
     *  @param msg
     *  KQML message
     */
    private void handleANSMessage(KQMLMessage msg) {
        String label = msg.get(":in-reply-to");
        if (label == null) {
            /* Message from ANS that is not a reply */
            Machinetta.Debugger.debug(3,"Query from ANS "+msg);
        }
        else if (label.equalsIgnoreCase(LABEL_REGISTER)) {
            /* Result of registration with ANS */
            /* Add ANS connection under ANS's registered name (extracted from :sender field) */
            ANSName = msg.get(":sender");
            Machinetta.Debugger.debug(1,"ANS name is "+ANSName);
        }
        else if (label.equalsIgnoreCase(LABEL_LOOKUP)) {
            /* Result of agent lookup */
            String performative = msg.get(":performative");
            String content = msg.get(":content");
            if (!performative.equalsIgnoreCase("sorry")) {
                /** (otherwise, desired agent is not registered with ANS) */
                /** Returned content is "<name>:<host>:port:" (including double-quotes) */
                StringTokenizer tokens = new StringTokenizer(content.substring(1,content.length()-1),":");
                String name = tokens.nextToken();
                String host = tokens.nextToken();
                int port = (new Integer(tokens.nextToken())).intValue();
                /* Establish new connection to returned host/port */
                Connection conn = new Connection(name,host,port);
                conn.start();
            }
        }
        else
            Machinetta.Debugger.debug(2,"Unknown message from ANS "+label);
    }
    
    /** Unregister agent from ANS.
     */
    private void unregister() {
        Machinetta.Debugger.debug(2,"Unregistering from KQML ANS");
        KQMLMessage msg = new KQMLMessage(null,"(unregister :receiver "+ANSName+
        ":content ("+myName+" "+myHost+" "+myPort +"))");
        if (!sendKQMLMessage(msg))
            Machinetta.Debugger.debug(4,"Unable to unregister from ANS");
    }
    
    /** Shut down communication (including listener and connection threads) */
    public void close() {
        unregister();
        /* Close all outstanding connections */
        while (connectionTable.size() > 0) {
            Connection conn = (Connection)connectionTable.elements().nextElement();
            conn.close();
        }
        /** Close listening thread */
        listener.close();
        Machinetta.Debugger.debug(3,"Stopped");
    }
    
    /***********************************************************************************************/
    /* A thread subclass that listens on a socket for incoming KQML connections and then launches a
     * client handler thread for the new connection */
    private class KQMLListener extends Thread {
        
        /** A listener needs a socket to listen on */
        private ServerSocket serverSocket;
        
        /** Creates a new instance of KQMLListener. */
        public KQMLListener() {
            /* Get host machine information */
            try {
                myHost = InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException e) {
                Machinetta.Debugger.debug(3,e.getMessage());
            }
            /* Try to find an open port to listen on */
            try {
                serverSocket = new ServerSocket(0);
                myPort = serverSocket.getLocalPort();
                Machinetta.Debugger.debug(2,"Listening on port "+ myPort);
            } catch (IOException e) {
                Machinetta.Debugger.debug(1,"Unable to listen on port "+myPort);
            }
        }
        
        /** Standard socket listening thread.  Listens for new requests on the socket,
         * and spawns new connections accordingly.  Shuts down if anything goes wrong. */
        public void run() {
            boolean running = true;
            while (running)
                try {
                    Machinetta.Debugger.debug(1,"Waiting for New Message...");
                    Socket clientSocket = serverSocket.accept();
                    Machinetta.Debugger.debug(1,"Accepted new socket connection on port "+clientSocket.getLocalPort());
                    Connection conn = new Connection(clientSocket);
                    conn.start();
                } catch (IOException e) {
                    Machinetta.Debugger.debug(4,"Unable to accept connection.  Exiting.");
                    running = false;
                }
        }
        
        /** Shuts down the listener */
        public void close() {
            try {
                serverSocket.close();
            } catch (IOException e) {
                /* I don't really care */
            }
            Machinetta.Debugger.debug(2,"No longer listening");
        }
        
    }
    
    
    /***********************************************************************************************/
    
    private class Connection extends Thread {
        
        /** Creates a new connection to a specified host/port pair.
         *  This constructor is used when proactively establishing a new connection.
         *
         * @param name
         * The name of the agent to connect to
         * @param host
         * The name of the host machine to connect to
         * @param port
         * The port number to connect to */
        public Connection(String name,String host, int port) {
            try {
                sock = new Socket(host,port);
            } catch (java.net.UnknownHostException e) {
                Machinetta.Debugger.debug(5,"Unknown host "+host);
            } catch (java.io.IOException e) {
                Machinetta.Debugger.debug(5,"Unable to open new socket to "+host+":"+port);
            }
            if (sock != null) {
                setupIO();
                setConnectionName(name);
            }
        }
        
        /** Creates a new connection using a specified socket.
         *  This constructor is used when establishing a connection in response to a request from another agent.
         *
         * @param newSocket
         * The socket on which the connection has been established
         */
        public Connection(Socket newSocket) {
            sock = newSocket;
            setupIO();
        }
        
        /** Sets up the buffered readers and writers on this connection */
        private void setupIO() {
            try {
                sockOut = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
                sockIn = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            } catch (java.io.IOException e) {
                Machinetta.Debugger.debug(5,"Unable to access socket I/O");
                sock = null;
            }
        }
        
        /** Sets the connection name and adds an entry to the connection table.
         *
         * @param name
         * The desired name of this connection
         */
        private void setConnectionName(String name) {
            connectionName = name;
            connectionTable.put(name,this);
            Machinetta.Debugger.debug(2,"New connection to "+name);
        }
        
        /** Sends a string message along the established connection
         *
         * @param msg
         * The string message to send
         */
        public synchronized void sendMsg(String msg) throws IOException {
            Machinetta.Debugger.debug(3,"Sending message: "+msg);
            sockOut.write(msg+endOfTransmission);
            sockOut.flush();
        }
        
        /** Reads a KQML message off of the established connection
         *
         * @return
         * A KQML message, or null if the connection is no longer alive
         */
        public KQMLMessage recvMsg() throws IOException {
            String msgStr = null;
            try {
                msgStr = sockIn.readLine();
            } catch (NullPointerException e) {
                Machinetta.Debugger.debug( 1,"Tried to read from null socket: "+e);
            }
            if (msgStr == null) {
                /** Stream has been closed---i.e., connection is dead */
                return null;
            }
            else {
                /** A real live message */
                Machinetta.Debugger.debug(3,"Received message: "+msgStr);
                KQMLMessage msg = new KQMLMessage(id,msgStr);
                /** If we don't have a name for this connection, then name it after the sender agent
                 *  that we're talking to */
                if (connectionName == null) {
                    String sender = msg.get(":sender");
                    if (sender != null)
                        setConnectionName(sender);
                }
                return msg;
            }
        }
        
        /** Thread body---continually listens for new KQML messages.
         *  Shuts down the connection if anything goes wrong
         */
        public void run() {
            boolean running=true;
            while (running)
                try {
                    KQMLMessage msg = recvMsg();
                    if (msg == null) {
                        running = false;
                    }
                    else if (connectionName.equals(ANS))
                        /** ANS messages handled internally */
                        handleANSMessage(msg);
                    else {
                        /** All other messages added to queue */
                        addMessage(msg);
                        incomingMessage();
                    }
                } catch(IOException e) {
                    if (connectionTable.containsKey(connectionName))
                        /** It's not a real error if we've already closed the connection */
                        Machinetta.Debugger.debug(4,"I/O Error reading socket");
                    running = false;
                }
            this.close();
        }
        
        /** Closes the connection and removes the entry from the connection table */
        public void close() {
            if ((connectionName != null) && (connectionTable.containsKey(connectionName))) {
                /* Do closing stuff only once */
                connectionTable.remove(connectionName);
            }
            if (sock != null) {
                try {
                    sock.close();
                } catch (IOException e) {
                    /* Ignore */
                    Machinetta.Debugger.debug( 1,"I/O Exception when closing socket: "+connectionName);
                }
            }
            Machinetta.Debugger.debug(2,"Closed connection to "+connectionName);
        }
        
        /** The name of this connection (typically the agent connected to) */
        String connectionName = null;
        /** The socket along which interaction between the connected agents takes place */
        Socket sock = null;
        /** The I/O handlers for the socket */
        BufferedWriter sockOut = null;
        BufferedReader sockIn = null;
        
    }
    /***********************************************************************************************/
    
    private Vector<Message> newMessages = new Vector<Message>();
    /** Records any connections already made to other KQML agents */
    private Hashtable<String,Connection> connectionTable = new Hashtable<String,Connection>();
    
    /** Special key for ANS entry in connection table (the "." ensures that there's no conflict
     * with "real" agent names, which can't have periods in them) */
    private final String ANS = ".ANS";
    /** Actual, registered name of the ANS */
    private String ANSName = null;
    
    private final String DEAD_CONNECTION = ".dead";
    private final String STOP_MSG = "(stop)";
    
    /** Reply-with tags */
    private final String LABEL_REGISTER = "register";
    private final String LABEL_LOOKUP = "lookup";
    
    /** Listener thread for handling new requests */
    private KQMLListener listener = null;
    
    /** For some reason, different versions of KQML set this differently */
    private final String endOfTransmission = "\r";
    
    /** Host/port of KQML ANS */
    private String ANSHost;
    private int ANSPort;
    
    /** KQML name, host, and port of this agent */
    private String myName;
    private String myHost;
    private int myPort = 7000;
    
    public static final long serialVersionUID = 1L;
}
