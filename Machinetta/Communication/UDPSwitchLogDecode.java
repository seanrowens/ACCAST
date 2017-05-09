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
/* Sat Oct  8 20:10:38 EDT 2005
 *
 * UDPSwitchLogDecode:  A tool for decoding captured UDP logs from the machinetta UDP comms
 * 
 * Author: Sean Owens   
 */
package Machinetta.Communication;

import Machinetta.State.BeliefType.MAC.RoleAgentBelief;
import Machinetta.State.BeliefType.MAC.JIAgentBelief;
import Machinetta.State.BeliefType.MAC.InformationAgentBelief;
import Machinetta.State.BeliefType.Belief;
import java.net.*;
import java.io.*;
import java.util.*;

public class UDPSwitchLogDecode {
    final static boolean IGNORE_RESENDS = true;
    final static boolean IGNORE_ACKS = true;
    
    // While most UDP packets will be smaller than 8K, I've
    // seen packets as large as at least 64K, at least on a
    // LAN.  Since we reuse this buffer, why be stingy?
    final static int BUF_SIZE=129*1024;
    String logFileName = null;

    int infoNumPackets = 0;
    int infoBiggestPacketSize = 0;
    int infoSmallestPacketSize = Integer.MAX_VALUE;
    long infoTotalPacketBytes = 0;
    
    PrintStream infoStream = System.out;

    DataInputStream logStream = null;
    boolean count = true;
    HashMap<String,Long> countReceived = new HashMap<String,Long>();
    HashMap<String,Long> countSent = new HashMap<String,Long>();

    private class DecodedPacket {
	long packetTime;
	long packetSize;
	int sentMsgCount;
	boolean isAck;
	long ID;
	String src;
	String dst;
	String commType;
	String contentTypeSimpleName;
	String contentSubTypeSimpleName;
	String content;
	String info;
	String contentSubTypeFullName;

	public void init() {
	    packetTime = -1;
	    packetSize = -1;
	    sentMsgCount = -1;
	    isAck = false;
	    ID = -1;
	    src = null;
	    dst = null;
	    commType = null;
	    contentTypeSimpleName = "";
	    contentSubTypeSimpleName = "";
	    content = "";
	    info = "";
	    contentSubTypeFullName = "";
	}

	public DecodedPacket() {
	    init();
	}

	public String toString() {
	    return packetTime
		+"\t"+packetSize
		+"\t"+sentMsgCount
		+"\t"+ID
		+"\t"+src
		+"\t"+dst
		+"\t"+commType
		+"\t"+contentTypeSimpleName
		+"\t"+contentSubTypeSimpleName
		+"\t"+content
		+"\t"+info
		+"\t"+contentSubTypeFullName;
	}
	public void printHeader(PrintStream out) {
	    out.println("TIME\t"
			+"SIZE\t"
			+"SENTCOUNT\t"
			+"ID\t"
			+"SRC\t"
			+"DST\t"
			+"COMMTYPE\t"
			+"TYPE\t"
			+"SUBTYPE\t"
			+"CONTENT\t"
			+"ADDITIONAL INFO\t"
			+"SUBTYPEFULLNAME"
			);   
	}
    }
    public UDPSwitchLogDecode(String logFileName) {
	this.logFileName = logFileName;

	// Open the log file for playback
	try {
	    if(null == logFileName)
		logStream = new DataInputStream(System.in);
	    else
		logStream = new DataInputStream(new FileInputStream(logFileName));
	}
	catch (java.io.FileNotFoundException e) {
	    System.err.println("ERROR: Problems opening log file=\""+logFileName+"\": "+e);
	    e.printStackTrace();
	}
    }

    public void decode() {
	DecodedPacket dp = new DecodedPacket();
	dp.printHeader(infoStream);
	// Now, start sending the packets.
	try {
	    byte[] buffer = new byte[BUF_SIZE]; 
	    long firstPacketTime = -1;
	    while(true) {
		// Note that the log format is very simple - a long
		// representing the time the packet was received, in
		// seconds since Jan 1, 1970, then an int with the
		// size of the packet, then the raw data of the packet
		// itself.
		long packetTime = logStream.readLong(); 
		//		long packetTime = 0;
		int bufferLen = (int)logStream.readLong();
		logStream.read(buffer, 0, bufferLen);
		//		System.err.println("Found packet of len="+bufferLen+" saved at time="+packetTime);

		// Note that the 'packetTime' saved when we created the
		// log was saved as an absolute value (i.e. number of
		// seconds since Jan 1, 1970), so we have to keep
		// track of and calculate the time since we started
		// the playback.
		if(-1 == firstPacketTime) {
		    firstPacketTime = packetTime;
		}
		long relativePacketTime = (packetTime - firstPacketTime);
		dp.init();
		decodePacket(relativePacketTime, buffer,bufferLen,dp);
		if(IGNORE_RESENDS && (dp.sentMsgCount > 1)) 
		    continue;
		if(IGNORE_ACKS && dp.isAck) 
		    continue;
		infoStream.println(dp);
	    }
	}
	catch (IOException e) {
	    System.err.println("ERROR: IO Exception e="+e);
	    e.printStackTrace();
	    System.err.println("Done with exception");
	}
	catch (Exception e) {
	    System.err.println("ERROR: Exception e="+e);
	    e.printStackTrace();
	    System.err.println("Done with exception");
	}
	System.err.println("Calling printCounts");
	printCounts();
	System.err.println("Done calling printCounts");
    }

    public void printCounts() {
	ArrayList<String> sentKeys = new ArrayList<String>(countSent.keySet());
	Collections.sort(sentKeys);
	System.err.println("Printing "+sentKeys.size()+" sentKeys");
	for(String key: sentKeys) {
	    //	    System.err.println("COUNT\t"+key+"\t"+countSent.get(key));
	    infoStream.println("SENT\t"+key+"\t"+countSent.get(key));
	}
	ArrayList<String> receivedKeys = new ArrayList<String>(countReceived.keySet());
	Collections.sort(receivedKeys);
	System.err.println("Printing "+receivedKeys.size()+" receivedKeys");
	for(String key: receivedKeys) {
	    //	    System.err.println("COUNT\t"+key+"\t"+countReceived.get(key));
	    infoStream.println("RECV\t"+key+"\t"+countReceived.get(key));
	}
	infoStream.flush();
    }

    public void oldDecodePacket(long packetTime, byte[] buf, int length) {
	int sentMsgCount = UDPMessage.getSendCount(buf);
	boolean isAck = UDPMessage.isACK(buf);
	long msgID = UDPMessage.getID(buf);

	if(IGNORE_RESENDS && (sentMsgCount > 1)) 
	    return;
	if(IGNORE_ACKS && isAck) 
	    return;

	UDPMessage msg = UDPMessage.getMessage(buf);

	if(isAck) {
	    infoStream.println(packetTime+"\t"+sentMsgCount+"\t"+msgID+"\t"+msg.getSource()+"\t"+msg.getDest()+"\t"+msg.getTypeName()+"\t"+msg.getClass().getName()+"\t"+msg.getClass().getPackage());
	    return;
	}
	if(null == msg.getMsg()) {
	    infoStream.println(packetTime+"\t"+sentMsgCount+"\t"+msgID+"\t"+msg.getSource()+"\t"+msg.getDest()+"\t"+msg.getTypeName()+"\t"+msg.getClass().getName()+" msg.getMsg() is null");
	    return;
	}

	if(msg.getMsg() instanceof ObjectMessage) {
	    Object omsg = ((ObjectMessage)msg.getMsg()).o;
	    if(null == omsg) {
		infoStream.println(packetTime+"\t"+sentMsgCount+"\t"+msgID+"\t"+msg.getSource()+"\t"+msg.getDest()+"\t"+msg.getTypeName()+"\t"+msg.getClass().getName()+" msg=ObjectMessage, is null");
		return;
	    }
	    if(omsg instanceof RoleAgentBelief)
		infoStream.println(packetTime+"\t"+sentMsgCount+"\t"+msgID+"\t"+msg.getSource()+"\t"+msg.getDest()+"\t"+msg.getTypeName()+"\t"+msg.getClass().getName()+" RoleAgentBelief="+omsg.toString());
	    else if(omsg instanceof JIAgentBelief)
		infoStream.println(packetTime+"\t"+sentMsgCount+"\t"+msgID+"\t"+msg.getSource()+"\t"+msg.getDest()+"\t"+msg.getTypeName()+"\t"+msg.getClass().getName()+" JIAgentBelief="+omsg.toString());
	    else 
		infoStream.println(packetTime+"\t"+sentMsgCount+"\t"+msgID+"\t"+msg.getSource()+"\t"+msg.getDest()+"\t"+msg.getTypeName()+"\t"+msg.getClass().getName()+"\tomsg\t"+omsg.toString());
	    return;
	}

	infoStream.println(packetTime+"\t"+sentMsgCount+"\t"+msgID+"\t"+msg.getSource()+"\t"+msg.getDest()+"\t"+msg.getTypeName()+"\t"+msg.getClass().getName()+"\tmsg\t"+msg.getMsg().toString());
    }

    public void countAck(UDPMessage msg) {
	String source = msg.getSource().toString();
	String dest = null;
	if(msg.getDest() != null)
	    dest = msg.getDest().toString();
	String className = "Ack";
	if(null!= source) {
	    Long sent = countSent.get(source+"\t"+className);
	    if(null == sent) 
		sent = new Long(1);
	    else
		sent++;
	    countSent.put(source+"\t"+className, sent);
	}
	if(null != dest) {
	    Long received = countReceived.get(dest+"\t"+className);
	    if(null == received) 
		received = new Long(1);
	    else
		received++;
	    countReceived.put(source+"\t"+className, received);
	}
    }

    public void countNull(UDPMessage msg) {
	String source = msg.getSource().toString();
	String dest = msg.getDest().toString();
	String className = "null";
	if(null!= source) {
	    Long sent = countSent.get(source+"\t"+className);
	    if(null == sent) 
		sent = new Long(1);
	    else
		sent++;
	    countSent.put(source+"\t"+className, sent);
	}
	if(null != dest) {
	    Long received = countReceived.get(dest+"\t"+className);
	    if(null == received) 
		received = new Long(1);
	    else
		received++;
	    countReceived.put(source+"\t"+className, received);
	}
    }

    public void countMsg(UDPMessage msg) {
	String source = msg.getSource().toString();
	String dest = msg.getDest().toString();
	String className = msg.getMsg().getClass().getName();
	if(null!= source) {
	    Long sent = countSent.get(source+"\t"+className);
	    if(null == sent) 
		sent = new Long(1);
	    else
		sent++;
	    countSent.put(source+"\t"+className, sent);
	}
	if(null != dest) {
	    Long received = countReceived.get(dest+"\t"+className);
	    if(null == received) 
		received = new Long(1);
	    else
		received++;
	    countReceived.put(source+"\t"+className, received);
	}
    }

    public void countObjectMsg(UDPMessage msg, Object omsg) {
	String source = msg.getSource().toString();
	String dest = msg.getDest().toString();
	String className = omsg.getClass().getName();
	if(null!= source) {
	    Long sent = countSent.get(source+"\t"+className);
	    if(null == sent) 
		sent = new Long(1);
	    else
		sent++;
	    countSent.put(source+"\t"+className, sent);
	}
	if(null != dest) {
	    Long received = countReceived.get(dest+"\t"+className);
	    if(null == received) 
		received = new Long(1);
	    else
		received++;
	    countReceived.put(source+"\t"+className, received);
	}
    }

    public void decodePacket(long packetTime, byte[] buf, int length, DecodedPacket dp) {
	dp.sentMsgCount = UDPMessage.getSendCount(buf);
	dp.isAck = UDPMessage.isACK(buf);
	dp.ID = UDPMessage.getID(buf);

	UDPMessage msg = UDPMessage.getMessage(buf);

	dp.packetTime = packetTime;
	dp.packetSize = length;
	if(msg.getSource() != null)
	    dp.src = msg.getSource().toString();
	if(msg.getDest() != null)
	    dp.dst = msg.getDest().toString();
	dp.commType = msg.getTypeName();

	if(dp.isAck) {
	    dp.info = "msg is ack";
	    countAck(msg);
	    return;
	}
	if(null == msg.getMsg()) {
	    dp.info="msg is null";
	    countNull(msg);
	    return;
	}

	// The msg might carry an ObjectMessage, in which case we're
	// really interested in the contents of it, i.e. the object it
	// is carrying.
	if(msg.getMsg() instanceof ObjectMessage) {
	    // ObjectMessage wraps an actual object, which we get here
	    Object omsg = ((ObjectMessage)msg.getMsg()).o;
	    countObjectMsg(msg, omsg);

	    // Or it might be carrying an InformationAgentBelief, in which
	    // case again we are really interested in the Belief that is
	    // inside the InformationAGent that is inside the
	    // InformationAgentBelief.  I think.  
	    if(omsg instanceof InformationAgentBelief) {
		dp.contentTypeSimpleName = "InformationAgentBelief";
		Belief bel = ((InformationAgentBelief)omsg).bel;
		dp.contentSubTypeSimpleName = bel.getClass().getSimpleName();
		dp.contentSubTypeFullName = bel.getClass().getName();
		if(null == bel) {
		    dp.content = "Belief is null";
		    return;
		}
		dp.content = bel.toString();
		return;
	    }

	    dp.contentTypeSimpleName = "ObjectMessage";
	    dp.contentSubTypeSimpleName = omsg.getClass().getSimpleName();
	    dp.contentSubTypeFullName = omsg.getClass().getName();

	    if(null == omsg) {
		dp.content = "Object is null";
		return;
	    }
	    dp.content = omsg.toString();
	    return;
	}

	dp.contentTypeSimpleName = msg.getMsg().getClass().getSimpleName();
	dp.info = msg.getMsg().getClass().getName();
	dp.content = msg.getMsg().toString();

	countMsg(msg);
    }

    private static void printUsageAndExit(String errorMsg) {
	if(errorMsg != null) 
	    System.err.println("ERROR:"+errorMsg);
	System.err.println("Usage: java UDPSwitchLogDecode -l logfilename");
	System.err.println("    -l / --logtofile filename          : filename of raw packets to decode");
	System.exit(1);
    }

    public static void main(String[] args) {
	String logFileName = null;

	for(int loopi = 0; loopi < args.length; loopi++) {
	    if(args[loopi].equals("-l") || args[loopi].equalsIgnoreCase("--logfilename")) {
		if((loopi+1) >= args.length) 
		    printUsageAndExit("Must specify a logfile name with -l/--logfilename option.");
		logFileName = args[loopi+1];
		loopi++;
	    }
	    else {
		printUsageAndExit("Unable to parse option args["+loopi+"]="+args[loopi]);
		System.exit(1);
	    }
	}

	UDPSwitchLogDecode decode;
	decode = new UDPSwitchLogDecode(logFileName);
	decode.decode();
    }
}
