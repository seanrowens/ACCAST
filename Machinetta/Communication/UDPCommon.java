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
 * UDPCommon.java
 *
 * Created on June 9, 2005, 4:13 PM
 */

package Machinetta.Communication;

import Machinetta.State.BeliefType.TeamBelief.FailedCommunication;
import Machinetta.State.BeliefType.TeamBelief.FailedProxy;
import Machinetta.State.BeliefType.NamedProxyID;
import Machinetta.State.ProxyState;
import Machinetta.State.BeliefType.RAPBelief;
import Util.DoubleHash;
import java.util.*;
import java.io.*;
import java.net.*;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * This class is the back end for both UDPComms and UDPRAPInterface classes
 *
 * When both are used at once, they will share sockets.
 *
 * @author pscerri
 */
public abstract class UDPCommon {
    public final static int MAX_UDP_BUFFER_SIZE = 8 * 1024;

    public final static int MSG_SENDCOUNT_LIMIT=5;
    
    // Sockets that input and output messages go on
    private DatagramSocket udpSocket;

    // Address of the switch
    static InetAddress server;
    
    // @TODO: Should this be NOT static? In Sanjaya, we're using two
    // instances of UDPCommon for each proxy, one to talk to other
    // proxies and one to talk to the Sanjaya sim.
    // 
    // ID of the sender
    private Machinetta.State.BeliefType.ProxyID senderId = null;
    
    // Messages received since last cleared by client
    private Vector<Message> newMessages = new Vector<Message>();
    protected void newMessage(Message m) { newMessages.add(m);}
    
    // Tracking receipt and acknowledgement
    public DoubleHash<Long, UDPMessage, Long> unACKed = new DoubleHash<Long, UDPMessage, Long>();
    private HashMap<Long, Long> recvd = new HashMap<Long, Long>();
    
    // Producer/Consumer thread
    java.util.concurrent.LinkedBlockingQueue<DatagramPacket> packetQueue = new java.util.concurrent.LinkedBlockingQueue<DatagramPacket>();
    
    // Tunable parameters (ms)
    static final long WAIT_FOR_ACKNOWLEDGE = 5000;
    static final long KEEP_RECV = 100000;
    
    // count of ALL messages sent - incremented every time we send a UDP packet
    private long totalSent=0;
    public long getTotalSent() { return totalSent; }
    // count acks sent
    private long totalAcksSent=0;
    public long getTotalAcksSent() { return totalAcksSent; }

    // count messages sent that don't require an Ack
    private long totalNoAckReqSent=0;
    public long getTotalNoAckReqSent() { return totalNoAckReqSent; }

    // count messages sent that require an Ack - not including resends
    private long totalAckReqSent=0;
    public long getTotalAckReqSent() { return totalAckReqSent; }

    // count reqAck messages resent at least once
    private long totalResentAtLeastOnce=0;
    public long getTotalResentAtLeastOnce() { return totalResentAtLeastOnce; }

    // count reqAck message resends - all of them
    private long totalResentSum=0;
    public long getTotalResentSum() { return totalResentSum; }

    // count reqAck failures - resent more than MSG_SENDCOUNT_LIMIT times without getting ack
    private long totalFailed = 0;
    public long getTotalFailed() { return totalFailed; }

    int[] sentMsgCounts = new int[256];
    public int[] getSentMsgCounts() {
        int[] ret = new int[sentMsgCounts.length];
        for(int loopi = 0; loopi < ret.length; loopi++)
            ret[loopi] = sentMsgCounts[loopi];
        return ret;
    }
    
    private int acknowledgedMessages = 0;
    private int resentAcks = 0;
    private int sends = 0;

    private boolean onlyRP = true;
    public void setOnlyRP(boolean val) { onlyRP = val;}
    private boolean onlyPR = false;
    public void setOnlyPR(boolean val) { onlyPR = val;}
    private boolean onlyPP = false;
    public void setOnlyPP(boolean val) { onlyPP = val;}
    
    /** Creates a new instance of UDPCommon */
    public UDPCommon() {
	try {
	    Machinetta.Debugger.debug( 2,"Initializing UDP socket for host="+Machinetta.Configuration.UDP_SWITCH_IP_STRING);
	    server = InetAddress.getByName(Machinetta.Configuration.UDP_SWITCH_IP_STRING);
	    udpSocket = new DatagramSocket();
	    Machinetta.Debugger.debug( 2,"UPD socket has addr "+udpSocket.getLocalAddress().toString()+" port "+udpSocket.getLocalPort());
	    
	    // @TODO: Note the commented out call to
	    // DatagramSocket.connect() below.  The UDPSwitch uses one
	    // socket for receiving and a different socket for
	    // sending, presumably to avoid contention.  (This _may_
	    // be unnecessary but in any event...)  Only the receiving
	    // socket has a 'well known' port.  As long as we're also
	    // using one socket, if we connect to the switch 'in'
	    // socket, then we can't receive any packets from the
	    // switch 'out' socket.
	    //
	    //	    udpSocket.connect(server,Machinetta.Configuration.UDP_SWITCH_IP_PORT);
	} catch (IOException e) {
	    Machinetta.Debugger.debug( 5,"ERROR ERROR ERROR: Initialization failed: " + e);
	    e.printStackTrace();
	}

	HelloThread helloThread = new HelloThread();
        ClientThread processThread = new ClientThread();
        RecvThread recvThread = new RecvThread();
        ACKThread ackThread = new ACKThread();
        SendThread sendThread = new SendThread();
    }
    
    LinkedBlockingQueue<UDPMessage> outQueue = new LinkedBlockingQueue<UDPMessage>();
    
    public void sendMessage(UDPMessage uMsg) {
        outQueue.offer(uMsg);
    }
    
    /**
     * If subclasses want to use this method, they need
     * to add incoming messages to newMessages vector.
     */
    public Message[] recvMessages() {
        Message [] ret = null;
        synchronized(newMessages) {
            ret = new Message[newMessages.size()];
            for (int i = 0; i < newMessages.size(); i++) {
                ret[i] = (Message)newMessages.elementAt(i);
            }
            newMessages.clear();
        }
        return ret;
    }
    
    public void setProxyID(Machinetta.State.BeliefType.ProxyID id) {
        this.senderId = id;
    }
    
    /** This should return true iff the method consumed the message, i.e., it was to it. */
    //protected abstract boolean processMessage(DatagramPacket packet);
    public abstract boolean processMessage(DatagramPacket packet);
    
    class RecvThread extends Util.SimpleSafeThread {
        
        public RecvThread() {
            super(-1);
            setPriority(NORM_PRIORITY);
            safeStart();
        }
        
        public void mainLoop() {
            try {
                byte [] buffer = new byte[64000];

		//		statPrint();

		// @TODO: I'm really fuzzy on this and nto having mcuh
		// use digging up the details, but I'm fairly sure
		// that in earlier versions of java you could not
		// reuse datagram packets, meaning you had to create a
		// new one every time you called receive.  Apparently
		// that has been fixed, so, hopefully, we can allocate
		// the buffer and the packet _once_ and then reuse
		// them throughout the entire lifetime of the receive
		// socket.  Don't want to risk breaking things now but
		// check this out as an optimization later.
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                udpSocket.receive(packet);
		//		statReceived();
		//		Machinetta.Debugger.debug( 1,"RECEIVED PACKET WITH ID="+UDPMessage.getID(packet.getData()));

		if(!UDPMessage.isACK(packet.getData())) {
		    if(onlyPP) {
			if(!UDPMessage.isCommToLocal(packet.getData())) {
			    //			    Machinetta.Debugger.debug( 1,"Dropping non PP packet ID="+UDPMessage.getID(packet.getData()));
			    //			    statDroppedOnlyPP();
			    return;
			}
		    }
		    else if(onlyRP) {
			if(!UDPMessage.isRAPInput(packet.getData())) {
			    //			    Machinetta.Debugger.debug( 1,"Dropping non RP packet ID="+UDPMessage.getID(packet.getData()));
			    //			    statDroppedOnlyRP();
			    return;
			}
		    }
		}
		//		Machinetta.Debugger.debug( 1,"Queueing packet ID="+UDPMessage.getID(packet.getData()));
                boolean ok = packetQueue.offer(packet);
                if (!ok) {
		    Machinetta.Debugger.debug( 4,"Incoming packet queue is full!");
		    //		    statDroppedFullQueue();
		}
		//		else 
		    //		    statQueued();
            } catch(java.io.IOException e) {
                Machinetta.Debugger.debug( 5,"Failed to receive packet correctly: " + e);
            }
            
        }
    };
    
    class ClientThread extends Util.SimpleSafeThread {
        
        private int prevQueueLength = 0;
	boolean warningLogged = false;
        
        public ClientThread() {
            super(-1);
            setPriority(NORM_PRIORITY);
            safeStart();
        }
        
        
        public void mainLoop() {
            try {
                
                DatagramPacket packet = packetQueue.take();
                
		//		Machinetta.Debugger.debug( 1,"ClientTHread.mainLoop: Dequeueing packet ID="+UDPMessage.getID(packet.getData()));
                int queueSize = packetQueue.size();
                if (queueSize > (1.5 * prevQueueLength) && queueSize > 50) {
                    Machinetta.Debugger.debug( 2,"Incoming PacketQueue length now: " + packetQueue.size());
                    prevQueueLength = queueSize;
                } else if (queueSize < 20) {
                    prevQueueLength = queueSize;
		    if(warningLogged) {
			warningLogged = false;
			Machinetta.Debugger.debug( 2,"Incoming PacketQueue length now reduced to: " + packetQueue.size());
		    }
                }
                
                long msgID = UDPMessage.getID(packet.getData());
                long sendCount = UDPMessage.getSendCount(packet.getData());
                
                // If it is an acknowledge message, cross it off the list
                if (UDPMessage.isACK(packet.getData())) {
// 		    Machinetta.Debugger.debug( 1,"ClientTHread.mainLoop: Got ACK with packet ID="+UDPMessage.getID(packet.getData()));
                    synchronized(unACKed) {
                        if (unACKed.contains(msgID)) {
                            unACKed.remove(msgID);
                        } else {
                            Machinetta.Debugger.debug( 0,"Wrong id on message?");
                        }
                        acknowledgedMessages++;
			//			statAck();
                        return;
                    }
                }
                
                boolean inRecvd = false;

                synchronized(recvd) {
                    inRecvd = recvd.containsKey(msgID);
                }
                if(inRecvd) {
                    // We've already received this, this is a resend
                    // of the message. Just send another ack and
                    // ignore the message.

		    
		    
		    String destString = UDPMessage.getSourceID(packet.getData());
		    NamedProxyID destId = new NamedProxyID(destString);
                    UDPMessage ackM = new UDPMessage(senderId, destId, msgID);
                    sendMessage(ackM);
                    
                    resentAcks++;
		    //                    Machinetta.Debugger.debug( 1,"MultiAcks " + resentAcks);
		    //		    statDroppedResend();
                    return;
                }
                
                boolean consumed = processMessage(packet);
                // If this is the destination of the message, send an ACK
                if (consumed) {
		    //		    statConsumed();
// 		    Machinetta.Debugger.debug( 5,"MsgID="+msgID+" was consumed, putting in recvd and sending ack");
                    synchronized(recvd) {
                        recvd.put(msgID, System.currentTimeMillis());
                    }
                    if (UDPMessage.requiresAck(packet.getData())) {
			String destString = UDPMessage.getSourceID(packet.getData());
			NamedProxyID destId = new NamedProxyID(destString);
                        UDPMessage ackM = new UDPMessage(senderId, destId, msgID);
			//			Machinetta.Debugger.debug( 1,"MsgID="+msgID+" was consumed, sending ack = "+ackM.summary());
                        sendMessage(ackM);
                    }
                    return;
                } else {
		    //		    Machinetta.Debugger.debug( 1,"ClientTHread.mainLoop: Not consumed, packet ID="+UDPMessage.getID(packet.getData()));

		    //		    statNotConsumed();
                    return;
                }
                //                Machinetta.Debugger.debug( 0,this + " processed packet");
            } catch(InterruptedException e) {
                Machinetta.Debugger.debug( 5,"Failed to process packet correctly: " + e);
            }
            
        }
    };
    
    class ACKThread extends Util.SimpleSafeThread {
        public ACKThread() {
            super(1000);
            setPriority(NORM_PRIORITY);
            safeStart();
        }
        
        long resends = 0;
        
        public void mainLoop() {
            long time = System.currentTimeMillis();
            Vector<Long> toSend = null, failed = null;
            Vector<UDPMessage> msgs = null;
            long startTime;
            long endTime;
            
            synchronized(unACKed) {
                startTime = System.currentTimeMillis();
                if (unACKed.size() > 0) {
                    // Check for unacknowledged messages
                    // Machinetta.Debugger.debug( 3,"Number of unacknowledged messages: " + unACKed.size() + " @ " + time);
                    for (Long l: unACKed.getKeys()) {
                        UDPMessage msg = unACKed.getVal1(l);
                        if (time - unACKed.getVal2(l) > WAIT_FOR_ACKNOWLEDGE) {
                            // Machinetta.Debugger.debug( 1,"Message unacknowledged for > " + msg.sendCount*(WAIT_FOR_ACKNOWLEDGE/1000) +"s: count: " + msg.sendCount + " id=" + l + " source="+msg2.getSource()+" dest="+msg2.getDest() + " type: " + msg2.msgClass + "(total resends " + resends + ")");
                            
                            if (msg.sendCount > MSG_SENDCOUNT_LIMIT) {
                                // This constitutes message failure
				totalFailed++;
                                Machinetta.Debugger.debug( 4,"Appears proxy has failed: " + msg.dest);
                                Object msg3 = msg.getMsg();
                                Machinetta.Debugger.debug( 1,"Message to FAILED proxy, msg id=" + l + " source="+msg.getSource()+" dest="+msg.getDest()+", type="+msg.getTypeName()+" instanceof "+(msg3.getClass().getName())+" msg="+msg3.toString());
                                
                                // Need to remove from unACKed after loop.
                                if (failed == null) failed = new Vector<Long>();
                                failed.add(l);
                 
                                /*
                                // Add a failed message belief to the state with this message and indicate proxy failed
                                FailedCommunication fcom = new FailedCommunication((Message)msg.getMsg());
                                ProxyState state = new ProxyState();
                                state.addBelief(fcom);
                                state.notifyListeners();
                                 */
                            } else {
                                // Resend it
                                if (toSend == null) toSend = new Vector<Long>();
                                toSend.add(l);
                            }
                        }
                    }
                }
                
		// Note, to avoid concurrent modification of the
		// unAcked table, we could not remove these entries
		// until we were done iterating across them.  That is
		// why we collected them into a list of IDs and then
		// remove them here, instead of just removing them
		// above when we found them.  Same with 'failed' list.

                // Resend unACKed
                if (toSend != null) {
                    msgs = new Vector<UDPMessage>(toSend.size());
                    for (Long msgID: toSend) {
                        UDPMessage msg = unACKed.getVal1(msgID);
                        unACKed.remove(msgID);

			//			Machinetta.Debugger.debug( 1,"Requeueing for resend msg summary="+msg.summary());
                        msgs.add(msg);
                        
                        resends++;
                    }
                    
                    Machinetta.Debugger.debug( 1,"Resent " + toSend.size() + " messages for a total of " + resends + " resends, now outstanding: " +unACKed.size() + " acked: " + acknowledgedMessages);
                }
                
                // Notify proxy of failed communications
                if (failed != null) {
                    for (Long l: failed) {
                        UDPMessage msg = unACKed.getVal1(l);
                        unACKed.remove(l);
                        if (msg.getMessage() != null) {
                            FailedCommunication belief = new FailedCommunication(msg.getMessage());
                            ProxyState state = new ProxyState();
                            state.addBelief(belief);
                            state.notifyListeners();
                        } else {
                            Machinetta.Debugger.debug( 0,"Can't do FailedCommunication with no message: " + msg);
                        }
                    }
                }
                
                endTime = System.currentTimeMillis();
            }
            
            if((endTime - startTime) > 100)
                Machinetta.Debugger.debug( 3,"WARNING UNACKED: size="+unACKed.size()+", time to process==" +(endTime - startTime));
            
            if (msgs != null) {
                for (UDPMessage msg: msgs) sendMessage(msg);
            }
            
            // Remove any old received message ids from list of ids to ignore.
            Long[] keys = null;
            synchronized (recvd) {
                keys = recvd.keySet().toArray(new Long[1]);
            }
            if(keys.length > 0) {
                for (Long msgid: keys) {
                    if(null != msgid) {
                        synchronized (recvd) {
                            if ((time - recvd.get(msgid)) > KEEP_RECV) {
                                recvd.remove(msgid);
                            }
                        }
                    }
                }
            }
        }
    }
    
    class SendThread extends Util.SimpleSafeThread {
        public SendThread() {
            super(-1);
            setPriority(NORM_PRIORITY);
            safeStart();
        }
        
        public void mainLoop() {
            
            try {
                
                sends++;
                
                UDPMessage uMsg = outQueue.take();
                
// 		Machinetta.Debugger.debug( 1,"SendThread.mainLoop: senderID='"
// 					   +(senderId != null ? senderId.toString() : "null")
// 					   +"' writing to socket message with summary "+uMsg.summary());

                uMsg.sendCount++;
                
                // Update our counts of sent/resent messages
 		totalSent++;
 		if(uMsg.isACK())
 		    totalAcksSent++;
 		else {
 		    if(!uMsg.reqAck)
 			totalNoAckReqSent++;
 		    else {
 			if(uMsg.sendCount == 1)
			    totalAckReqSent++;
 			if(uMsg.sendCount == 2)
 			    totalResentAtLeastOnce++;
 			if(uMsg.sendCount >= 2)
 			    totalResentSum++;
 
 			int sentMsgCount = uMsg.sendCount;
 			if(sentMsgCount < 0)
 			    sentMsgCount = 128;
 			sentMsgCounts[sentMsgCount]++;
 		    }
 		}
                
                byte [] buffer = uMsg.getBytes();
                //            Machinetta.Debugger.debug( 1,"Sending message id "+uMsg.id+" with size: " + buffer.length);
                if (buffer != null) {
		    if(buffer.length > MAX_UDP_BUFFER_SIZE) {
			Machinetta.Debugger.debug( 4,"ERROR COULD NOT SEND UDP packet, bytes.length ("+buffer.length+") > max size allowed("+MAX_UDP_BUFFER_SIZE+"), for uMsg="+uMsg);
		    }
		    else {
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length,
								   server,
								   Machinetta.Configuration.UDP_SWITCH_IP_PORT);
			
			udpSocket.send(packet);

// 			Machinetta.Debugger.debug( 1,"Written to socket packet addressed to "+packet.getAddress()+" : "+packet.getPort()+" with data "+uMsg.summary());
			if(!uMsg.isACK() && uMsg.reqAck) {
			    synchronized(unACKed) {
				// Add to list of unacknowledged messages
				unACKed.put(uMsg.id, uMsg, System.currentTimeMillis());
			    }
			}
		    }
                }
            } catch(java.io.IOException e) {
                Machinetta.Debugger.debug( 4,"Failed to send packet correctly: " + e);
            } catch (InterruptedException e) {
                Machinetta.Debugger.debug( 5,"Send was interrupted: " + e);
            } catch (Exception e) {
                Machinetta.Debugger.debug( 5,"mainLoop: ERROR, exception, ignoring it, exception e=" + e);
		e.printStackTrace();
            }
	    
            
            if (outQueue.size() > 20) 
                Machinetta.Debugger.debug( 1,"outgoing Pending messages in outQueue: " + outQueue.size() + ", sent = " + sends);
        }
    };


    // queue a hello message every half a second.
    class HelloThread extends Util.SimpleSafeThread {
        public HelloThread() {
            super(-1);
            setPriority(NORM_PRIORITY);
            safeStart();
        }
        
        public void mainLoop() {
	    long loopDelay = 500;
            long startTime = System.currentTimeMillis();
	    while (System.currentTimeMillis() - loopDelay < startTime) {
		try {
		    long sleepTime = loopDelay - (System.currentTimeMillis() - startTime);
		    if (sleepTime > 0)
			sleep(sleepTime);
		} catch (InterruptedException e) {}
	    }
	    
	    if(senderId == null) {
		Machinetta.State.ProxyState state = new Machinetta.State.ProxyState();
		while(true) {
		    RAPBelief self = state.getSelf();
		    if(null != self) {
			senderId = self.getProxyID();
			break;
		    }
		    try { sleep(100); } catch (InterruptedException e) {}
		}
	    }

	    if(onlyPP) {
		sendMessage(UDPMessage.makeHelloMessage(senderId, UDPMessage.PP));
	    }
	    else if(onlyRP) {
		// NOTE: We only WANT RP (RAP to Proxy) messages which
		// means WE are sending PR (Proxy to RAP) type
		// messages.  If that makes sense.  When the switch
		// receives a RP message, to find the dest it looks
		// for someone that is sending PR messages.
		sendMessage(UDPMessage.makeHelloMessage(senderId, UDPMessage.PR));
	    }
	    else if(onlyPR) {
		// NOTE: See above - we only want PR (Proxy to RAP)
		// which means we are sending RP (RAP to Proxy).
		sendMessage(UDPMessage.makeHelloMessage(senderId, UDPMessage.RP));
	    }
	    else
		// I give up, I dunno _what_ we are.
		sendMessage(UDPMessage.makeHelloMessage(senderId, UDPMessage.UNKNOWN));
	}
    };

//     static Object packetStatLock = new Object();
//     static long pReceived = 0;
//     static long pDroppedOnlyPP = 0;
//     static long pDroppedOnlyRP = 0;
//     static long pDroppedFullQueue = 0;
//     static long pQueued = 0;
//     static long pAck = 0;
//     static long pDroppedResend = 0;
//     static long pConsumed =0;
//     static long pNotConsumed = 0;

//     static long pTotalReceived = 0;
//     static long pTotalDroppedOnlyPP = 0;
//     static long pTotalDroppedOnlyRP = 0;
//     static long pTotalDroppedFullQueue = 0;
//     static long pTotalQueued = 0;
//     static long pTotalAck = 0;
//     static long pTotalDroppedResend = 0;
//     static long pTotalConsumed =0;
//     static long pTotalNotConsumed = 0;

//     final static long PRINT_EVERY_N_MS = 1000;
//     static long timeNextPrint = System.currentTimeMillis()+PRINT_EVERY_N_MS;

//     private void statPrint() {
// 	synchronized(packetStatLock) {
// 	    long timeNow = System.currentTimeMillis();
// 	    if(timeNow < timeNextPrint)
// 		return;

// 	    timeNextPrint = timeNextPrint+PRINT_EVERY_N_MS;
// 	    String tag = (senderId != null ? senderId.toString() : "nullSender");
// 	    Machinetta.Debugger.debug( 1,tag
// 				       +"\t"+onlyPP
// 				       +"\t"+onlyRP
// 				       +"\t"+timeNow
// 				       +"\t"+pReceived
// 				       +"\t"+pDroppedOnlyPP
// 				       +"\t"+pDroppedOnlyRP
// 				       +"\t"+pQueued
// 				       +"\t"+pAck
// 				       +"\t"+pDroppedResend
// 				       +"\t"+pConsumed
// 				       +"\t"+pNotConsumed
// 				       +"\t"+pDroppedFullQueue
// 				       +"\ttotals"
// 				       +"\trec\t"+pTotalReceived
// 				       +"\tdOnlyPP\t"+pTotalDroppedOnlyPP
// 				       +"\tdOnlyRP\t"+pTotalDroppedOnlyRP
// 				       +"\tqueued\t"+pTotalQueued
// 				       +"\tack\t"+pTotalAck
// 				       +"\tdresend\t"+pTotalDroppedResend
// 				       +"\tconsumed\t"+pTotalConsumed
// 				       +"\tnotconsumed\t"+pTotalNotConsumed
// 				       +"\tfullqueue\t"+pTotalDroppedFullQueue
// 				       );
// 	    pReceived = 0;
// 	    pDroppedOnlyPP = 0;
// 	    pDroppedOnlyRP = 0;
// 	    pDroppedFullQueue = 0;
// 	    pQueued = 0;
// 	    pConsumed =0;
// 	    pNotConsumed = 0;
// 	}
//     }

//     private void statReceived() {
// 	synchronized(packetStatLock) {
// 	    pReceived++;
// 	    pTotalReceived++;
// 	}
//     }
//     private void statDroppedOnlyPP() {
// 	synchronized(packetStatLock) {
// 	    pDroppedOnlyPP++;
// 	    pTotalDroppedOnlyPP++;
// 	}
//     }

//     private void statDroppedOnlyRP() {
// 	synchronized(packetStatLock) {
// 	    pDroppedOnlyRP++;
// 	    pTotalDroppedOnlyRP++;
// 	}
//     }
//     private void statDroppedFullQueue() {
// 	synchronized(packetStatLock) {
// 	    pDroppedFullQueue++;
// 	    pTotalDroppedFullQueue++;
// 	}
//     }
//     private void statQueued() {
// 	synchronized(packetStatLock) {
// 	    pQueued++;
// 	    pTotalQueued++;
// 	}
//     }
//     private void statAck() {
// 	synchronized(packetStatLock) {
// 	    pAck++;
// 	    pTotalAck++;
// 	}
//     }
//     private void statDroppedResend() {
// 	synchronized(packetStatLock) {
// 	    pDroppedResend++;
// 	    pTotalDroppedResend++;
// 	}
//     }
//     private void statConsumed() {
// 	synchronized(packetStatLock) {
// 	    pConsumed++;
// 	    pTotalConsumed++;
// 	}
//     }
//     private void statNotConsumed() {
// 	synchronized(packetStatLock) {
//  	    pNotConsumed++;
// 	    pTotalNotConsumed++;
// 	}
//     }
}
