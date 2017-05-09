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
 * UDPSwitch.java
 *
 * Created on May 29, 2005, 3:43 PM
 */

package Machinetta.Communication;

import java.io.*;
import java.net.*;
import java.text.DecimalFormat;
import java.util.*;

/**
 *
 * @author paul
 */
public class UDPSwitch {
    public final int DATAGRAM_BUFFER_SIZE = 64000;
    public final static DecimalFormat fmt = new DecimalFormat("0.0");
    DatagramSocket inSocket;
    DatagramSocket outSocket;
    InetAddress group;
    
    static long sends = 0, recvs = 0;
    static long lastSends = 0;
    static long lastRecvs = 0;
    static long recvsResent = 0;
    static long lastRecvsResent = 0;
    long startTime  = System.currentTimeMillis();
    long lastSendTime  = System.currentTimeMillis();
    long lastRecvTime  = System.currentTimeMillis();
    int sendPrintCounter = 0;
    int recvPrintCounter = 0;
    int failRecvPrintCounter = 0;
    long totalBytesRecvd = 0;
    final static int PRINT_EVERY_N=1000;
    
    int[] sentMsgCounts = new int[256];
    
    DataOutputStream logStream = null;
    int infoNumPackets = 0;
    
    private static String makeKnownAddressKey(InetAddress iAddr, int port) {
	return iAddr.toString()+":"+port;
    }
    private static String makeKeyForSource(byte type, String sourceID) {
	if(type == UDPMessage.PP) {
	    return "PROXY."+sourceID;
	}
	else if(type == UDPMessage.PR) {
	    return "RIPROXY."+sourceID;
	}
	else if(type == UDPMessage.RP) {
	    return "RIRAP."+UDPMessage.SIM_NAME;
	}
	return null;
    }

    private static String makeKeyForDest(byte type, String destID) {
	if(type == UDPMessage.PP) {
	    return "PROXY."+destID;
	}
	else if(type == UDPMessage.PR) {
	    return "RIRAP."+destID;
	}
	else if(type == UDPMessage.RP) {
	    return "RIPROXY."+destID;
	}
	return null;
    }

    private class DestAddr { 
	String knownAddressKey;
	String destKey;
	InetAddress addr;
	int port;
	byte typeFromUDPMessage=-1;
	public String toString() {
	    return "kaKey="+knownAddressKey+" destKey="+destKey+" addr="+addr+" port="+port+" type="+typeFromUDPMessage;
	}
    }

    private class MsgAndDest {
	DatagramPacket packet;
	DestAddr dest;
    }
    
    HashMap<String,DestAddr> knownAddressMap = new HashMap<String,DestAddr>();
    HashMap<String,DestAddr> destMap = new HashMap<String,DestAddr>();

    java.util.concurrent.LinkedBlockingQueue<MsgAndDest> packetQueue = new java.util.concurrent.LinkedBlockingQueue<MsgAndDest>();
    
    Util.SimpleSafeThread recvThread = new Util.SimpleSafeThread(-1) {
        InetAddress client = null;
        int client_port = 0;
        
	    // NOTE: Most of the complicated stuff below is necessary
	    // because of how messages are exchanged and because we
	    // use this comms library for both proxy2proxy comms as
	    // well as comms between Proxy RAPImplemenation and Sim.
	    //
	    // The direct problem is, proxy2proxy has a source and a
	    // dest, as would be proper, but proxy to sim has only a
	    // source which is set the proxy's ID, and sim to proxy
	    // likewise only sets the sourceID to be the ID of the
	    // asset.  
	    // 
	    // Why do we do this?  Well, on the one hand,
	    // because... well I don't know.  For one thing all of the
	    // proxies are talking to the sim, but that really
	    // shouldn't matter.  They should just send to "sanjaya"
	    // and let the sim take care of demuxing based on the
	    // source.  But the only real problem is, I guess, it
	    // would be hard to configure.  
	    //
	    // We could use the ProxyID from the proxy belief to
	    // initialize the comms lib instance that talks to the
	    // sim, but the RAPInterface is created BEFORE we load the
	    // proxy state.  Hmm, I suppose we could have a thread in
	    // the RI that waits until the self RAPBelief (containing
	    // the ProxyID) is available, before instantiating the
	    // comms to sim.  Hmm.  Hmmmm. That's probably the _right_
	    // way to do this.  Instead of mucking around with message
	    // types below. (Which we really shouldn't even have in
	    // the comms lib, nor NamedProxyID for that matter, or
	    // anything else from the Machinetta tree. )

        public void mainLoop() {
            try {
                
		byte [] buffer = new byte[DATAGRAM_BUFFER_SIZE];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                inSocket.receive(packet);                                

                client = packet.getAddress();
                client_port = packet.getPort();
		
// 		Machinetta.Debugger.debug( 1,"Received packet from "+client+":"+client_port+" summary "+UDPMessage.summary(packet.getData()));
		

		byte type = UDPMessage.getType(packet.getData());

		String sourceAddressKey=null;;
		DestAddr sourceAddr=null;
		sourceAddressKey = makeKnownAddressKey(client,client_port);
		sourceAddr = knownAddressMap.get(sourceAddressKey);

		if(type != UDPMessage.ACK) {
		    if(null == sourceAddr) {
			sourceAddr = new DestAddr();
			sourceAddr.typeFromUDPMessage = type;
			sourceAddr.knownAddressKey = sourceAddressKey;
			sourceAddr.addr = client;
			sourceAddr.port = client_port;

			String sourceID = UDPMessage.getSourceID(packet.getData());
			sourceAddr.destKey = makeKeyForSource(type,sourceID);
			if(UDPMessage.getHello(packet.getData()))
			    Machinetta.Debugger.debug( 1,"HELLO: Adding new sourceAddr= {"+sourceAddr+" }");
			else
			    Machinetta.Debugger.debug( 1,"OVERHEARD: Adding new sourceAddr= {"+sourceAddr+" }");
			knownAddressMap.put(sourceAddr.knownAddressKey, sourceAddr);
			destMap.put(sourceAddr.destKey, sourceAddr);
		    }
		    else {
			// Check for a changed 'type' for the dest - this
			// should almost never happen, but there's a
			// chance someone will instantiate a UDPCommon and
			// then not set the onlyPP/onlyRP/onlyPR type
			// quickly enough to get it done before the first
			// hello packet is sent out.
			if(type != sourceAddr.typeFromUDPMessage) {

			    String oldDestKey = sourceAddr.destKey;
			    destMap.remove(sourceAddr.destKey);
			    String sourceID = UDPMessage.getSourceID(packet.getData());
			    sourceAddr.destKey = makeKeyForSource(type,sourceID);
			    sourceAddr.typeFromUDPMessage = type;
			    if(UDPMessage.getHello(packet.getData()))
				Machinetta.Debugger.debug( 1,"HELLO: Updating type/key, old destkey="+oldDestKey+", now sourceAddr= { "+sourceAddr+" }");
			    else 
				Machinetta.Debugger.debug( 1,"OVERHEARD: Updating type/key, old destkey="+oldDestKey+", now sourceAddr= { "+sourceAddr+" }");
			    destMap.put(sourceAddr.destKey, sourceAddr);
			}
		    }
		}

		// Check if packet is a 'hello' packet and if so
		// discard.
		if(UDPMessage.getHello(packet.getData())) {
		    //		    Machinetta.Debugger.debug( 1," Discarding hello from "+sourceAddr);
		    return;
		}


		if((type != UDPMessage.PP) 
		   && (type != UDPMessage.RP) 
		   && (type != UDPMessage.PR)
		   && (type != UDPMessage.ACK)
		   ) {
		    // Foul!
		    Machinetta.Debugger.debug( 3,"Warning, incoming packet with unknown type, ignoring, type == "+type);
		    return;
		}

		String destKey ="";

		if(type == UDPMessage.ACK)
		    destKey = makeKeyForDest(sourceAddr.typeFromUDPMessage,UDPMessage.getDestID(packet.getData()));
		else
		    destKey = makeKeyForDest(type,UDPMessage.getDestID(packet.getData()));

		DestAddr destAddr = destMap.get(destKey);
// 		if(type == UDPMessage.ACK) {
// 		    Machinetta.Debugger.debug( 1,"For ACK destKey="+destKey);
// 		}

		if(null == destAddr) {
		    Machinetta.Debugger.debug( 1,"destKey "+destKey+" has null destAddr");
		    // Can't find it, drop it and hope it gets resent.
		    int sendCount = UDPMessage.getSendCount(packet.getData());
		    if(sendCount > UDPCommon.MSG_SENDCOUNT_LIMIT)
			Machinetta.Debugger.debug( 3,"WARNING WARNING: sendCount ("+sendCount+") > limit ("+UDPCommon.MSG_SENDCOUNT_LIMIT+") and incoming packet with destAddr I've never heard of "+destKey+", this really shouldn't happen.  Dropping packet, which is going to result in some UDPCommon instance thinking this dest has 'failed'.  SourceAddr= { "+sourceAddr+" }");
		    else 
			Machinetta.Debugger.debug( 1,"Warning, incoming packet with destAddr I've never heard of, sendCount ="+sendCount+", dropping packet, hoping it gets resent.");
		    return;
		}

//		Machinetta.Debugger.debug( 1,"destKey "+destKey+" has destAddr={"+destAddr+"}");
		

		MsgAndDest mad = new MsgAndDest();
		mad.packet = packet;
		mad.dest = destAddr;

                boolean ok = packetQueue.offer(mad);
                
                if (!ok) Machinetta.Debugger.debug( 4,"UDPSwitch packet queue is full!");
                
            } catch(java.io.IOException e) {
                Machinetta.Debugger.debug( 5,"Failed to capture packet correctly: " + e);
            }
            
        }
        
    };
    
    Util.SimpleSafeThread sendThread = new Util.SimpleSafeThread(-1) {
        InetAddress client = null;
        int client_port = 0;
        
        public void mainLoop() {
            try {
                                
                MsgAndDest mad = packetQueue.take();
		byte [] buffer = mad.packet.getData();

                totalBytesRecvd += mad.packet.getLength();
                
                // Note the time.
		long receiveTime = System.currentTimeMillis();                
                if(null != logStream) {
		    logStream.writeLong(receiveTime);
                    logStream.writeLong(mad.packet.getLength());
                    logStream.write(buffer, 0, mad.packet.getLength());
                }
                infoNumPackets++;
                
		int sentMsgCount = UDPMessage.getSendCount(mad.packet.getData());
		if(sentMsgCount < 0)
		    sentMsgCount = 128;
		sentMsgCounts[sentMsgCount]++;
                
		send(mad);

		recvs++;
		if(sentMsgCount > 1)
		    recvsResent++;

            } catch(InterruptedException e) {
                Machinetta.Debugger.debug( 5,"Failed to get packet off the queue: " + e);
            } catch(java.io.IOException e) {
                Machinetta.Debugger.debug( 3,"Error writing to switch log: " + e);
            }
            
            recvPrintCounter++;
            if(recvPrintCounter > PRINT_EVERY_N) {
                recvPrintCounter = 0;
                long timeNow  = System.currentTimeMillis();
                long timeFromStart = timeNow - startTime;
                long timeFromLastPrint = timeNow - lastRecvTime;
                double timeFromLastPrintSec = ((double)timeFromLastPrint)/1000;
                long recvsFromLastPrint = recvs - lastRecvs;
                long recvsResentFromLastPrint = (recvsResent - lastRecvsResent);
                long recvsOriginalFromLastPrint = recvsFromLastPrint - recvsResentFromLastPrint;
                double avgFromLastPrint = recvsFromLastPrint/timeFromLastPrintSec;
                double avgOriginalFromLastPrint =  recvsOriginalFromLastPrint/timeFromLastPrintSec;
                double avgResentFromLastPrint =  recvsResentFromLastPrint/timeFromLastPrintSec;
                
                System.out.println("\tFromStart\t\tFromLastPrint\t\t\tAvgMsgs/Sec");
                System.out.println("\tsends\trecvs\ttime\trecvs\tresent\torig\ttime\torig\tresent\tall");
                System.out.println("STATS\t"+sends + "\t" + recvs + "\t"+timeFromStart+"\t"+recvsFromLastPrint+"\t"+recvsResentFromLastPrint+"\t"+recvsOriginalFromLastPrint+"\t"+timeFromLastPrint+"\t"+fmt.format(avgOriginalFromLastPrint)+"\t"+fmt.format(avgResentFromLastPrint)+"\t"+fmt.format(avgFromLastPrint));
                System.out.println("DATA: Total bytes recvd: " + totalBytesRecvd + " Average packet size = " + (totalBytesRecvd/recvs));
                //		System.out.println("FromStart: sends= " + sends + " recvs= " + recvs + " time= "+timeFromStart+" ms FromLastPrint: recvs= "+recvsFromLastPrint+" resent= "+recvsResentFromLastPrint+" original= "+recvsOriginalFromLastPrint+" time= "+timeFromLastPrint+" AvgMsgs/Sec: all= "+fmt.format(avgFromLastPrint)+" orig= "+fmt.format(avgOriginalFromLastPrint)+" resent= "+fmt.format(avgResentFromLastPrint));
                
                lastRecvs = recvs;
                lastRecvTime = timeNow;
                lastRecvsResent = recvsResent;
                for(int loopi = 0; loopi < sentMsgCounts.length; loopi++) {
                    if(sentMsgCounts[loopi] > 0) {
                        System.out.println("        msgs sent "+loopi+" times="+sentMsgCounts[loopi]+"\t%"+(((double)sentMsgCounts[loopi])/((double)sentMsgCounts[1]))*100);
                    }
                }
            }
            
            if (recvs % 10 == 0) {
                try {
                    sleep(1);
//                     Machinetta.Debugger.debug( 1,"Queue is now: " + packetQueue.size());
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
                        
        }
        
        protected void send(MsgAndDest mad) {
            try {
                sends++;
                mad.packet.setAddress(mad.dest.addr);
                mad.packet.setPort(mad.dest.port);
                outSocket.send(mad.packet);
            } catch(java.io.IOException e) {
                Machinetta.Debugger.debug( 5,"Failed to send packet correctly: " + e);
            }
            sendPrintCounter++;
            sendPrintCounter++;
            if(sendPrintCounter > PRINT_EVERY_N) {
                sendPrintCounter = 0;
                long timeNow  = System.currentTimeMillis();
                double sendsSince = sends - lastSends;
                double timeSince = timeNow - lastSendTime;
                double avg = sendsSince/(timeSince/1000);
                System.out.println("Recving: Sends: " + sends + " Recvs: " + recvs + " over "+(timeNow - startTime)+" since start, sends "+sendsSince+" over the last "+timeSince+", avg msgs/s recently="+avg);
                lastSends = sends;
                lastSendTime = timeNow;
            }
        }
    };
    
    /** Creates a new instance of UDPSwitch */
    public UDPSwitch(String logFileName) {
        try {
            inSocket = new DatagramSocket(Machinetta.Configuration.UDP_SWITCH_IP_PORT);
            outSocket = new DatagramSocket();
            Machinetta.Debugger.debug(1,"Switch reading from port "
				      +Machinetta.Configuration.UDP_SWITCH_IP_PORT);
        } catch (Exception e) {
            Machinetta.Debugger.debug( 3,"Failed to initialize: " + e);
        }
        if(null != logFileName) {
            try {
                logStream = new DataOutputStream(new FileOutputStream(logFileName));
            } catch (java.io.FileNotFoundException e) {
                Machinetta.Debugger.debug( 5,"ERROR: Problems opening log file='"+logFileName+"': "+e);
                e.printStackTrace();
            }
        }
        
        recvThread.start();
        sendThread.start();
    }
    
    private static void printUsageAndExit(String errorMsg) {
        if(errorMsg != null)
            System.err.println("ERROR:"+errorMsg);
        System.err.println("Usage: java UDPSwitch [-l filename]");
        
        System.err.println("    -l / --logtofile filename          : Log raw packets with timestamps to file");
        System.exit(1);
    }
    
    public static void main(String argv[]) {
        String logFileName = null;
        for(int loopi = 0; loopi < argv.length; loopi++) {
            if(argv[loopi].equals("-l") || argv[loopi].equalsIgnoreCase("--logtofile")) {
                if((loopi+1) >= argv.length)
                    printUsageAndExit("Must specify a logfile name with -l/--logtofile option.");
                logFileName = argv[loopi+1];
                loopi++;
            } else {
                printUsageAndExit("Unable to parse option argv["+loopi+"]="+argv[loopi]);
                System.exit(1);
            }
        }
        
        new UDPSwitch(logFileName);
    }
}
