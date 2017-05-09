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
 * KestrelDecoder:  A tool for decoding captured Procerus Kestrel autopilot logs
 * 
 * Author: Sean Owens   
 */
package AirSim.Machinetta;

import java.net.*;
import java.io.*;
import java.util.*;
import java.text.DecimalFormat;

public class KestrelDecoder {
    private final static DecimalFormat fmt5 = new DecimalFormat("0.00000");
    private final static DecimalFormat fmt2 = new DecimalFormat("0.00");

    final boolean PLAYBACK_IN_REAL_TIME = false;
    final static boolean IGNORE_RESENDS = true;
    final static boolean IGNORE_ACKS = true;
    
    final static byte P_START_BYTE = (byte)0xFF;
    final static byte P_END_BYTE = (byte)0xFE;
    final static byte P_ESCAPE_BYTE = (byte)0xFD;
    final static byte P_ESCAPE_BYTE_00 = (byte)0xFD;
    final static byte P_ESCAPE_BYTE_01 = (byte)0xFE;
    final static byte P_ESCAPE_BYTE_02 = (byte)0xFF;
    final static byte P_ESCAPE_BYTE_03 = (byte)0xCC;

    // From pdf file Kestrel_Communications_MA7.1_B.pdf page 70, we're
    // using 'pass through' mode, so Aviones wraps the packets from
    // the comm box in the following struct;
    //
    // // PacketData is the array that holds the entire packet
    // // before special characters are removed
    // struct sVCPacket
    // {
    //     int VCPacketType; //Packet interface type to the VC server
    //     int DataSize; //The size of the data in the char array below
    //     unsigned char PktData[1024]; //The data associated with this packet
    // };
    //
    // So basically all of the docs that reference byte positions have
    // to be offset by the size of VCPacketType and DataSize, i.e. 8 bytes.

    final static int PASS_THROUGH_PACKET_TYPE_OFFSET = 0;
    final static int PASS_THROUGH_DATA_SIZE_OFFSET = 4;
    final static int PASS_THROUGH_OFFSET = 8;

    // The first 6 bytes and the 4 bytes are standard for every packet
    // type, specified on page 14 of the docs; the portion between the
    // header and footer is referred as the 'data' segment and varies
    // in length and content depending on the type of the packet.

    final static int HEADER_SIZE = 6;
    
    final static int OFF_START = 0 + PASS_THROUGH_OFFSET;
    final static int OFF_TYPE = 1 + PASS_THROUGH_OFFSET;
    final static int OFF_SRC_ADDR_LOW = 2 + PASS_THROUGH_OFFSET;
    final static int OFF_SRC_ADDR_HIGH = 3 + PASS_THROUGH_OFFSET;
    final static int OFF_DEST_ADDR_LOW = 4 + PASS_THROUGH_OFFSET;
    final static int OFF_DEST_ADDR_HIGH = 5 + PASS_THROUGH_OFFSET;
    
    final static int FOOTER_SIZE = 4;
    final static int MINUS_OFF_END = 0;
    final static int MINUS_OFF_XOR_HIGH_BYTE = 1;
    final static int MINUS_OFF_XOR_LOW_BYTE = 2;
    final static int MINUS_OFF_PACKET_ID = 3;

    // WRONG We're interested in the packet specified on page 42 of the
    // WRONG docs, Table 3.34 - Request Navigation Telemetry Response
    //
    // Ok that was wrong, the code in VCCommands.java, that Robin
    // wrote, sends a passthrough request, and specifies it wants
    // packet 248, which is 3.61. Navigation Packet (248) on page 64!
    // But it's very similar, if not identical;

    // Byte   Type   Name                  Description                                             Units
    // Index
    // 6      UINT   GPS Velocity          GPS computed velocity                                   (m/s+10)* 20
    // 8      UINT   GPS Alt               GPS computed altitude                                   (meters+1000)* 6
    // 10     UINT   GPS Heading (0->2pi)  GPS computed heading                                    rad*1000
    // 12     UCHAR  Dev Byte              Take Off countdown timer                                N/A
    // 13     UCHAR  Temperature R         Temperature of R Gyro calculated as (deg C +10) * 2.8   (degress C+10) * 2.8
    // 14     FLOAT  GPS Latitude          Latitude of UAV in decimal degrees                      deg
    // 18     INT    Sys flags 1           System Flags 1                                          #
    //                     0 0x0001 Gimbal Mode0 bit
    //                     1 0x0002 Gimbal Mode 1 bit
    //                     2 0x0004 Empty
    //                     3 0x0008 MSL Alt calibrated
    //                     4 0x0010 Home Alt set
    //                     5 0x0020 Alt over-ride enabled
    //                     6 0x0040 Airspeed over-ride enabled
    //                     7 0x0080 Flair enabled
    //                     8 0x0100 Side look camera selected
    //                     9 0x0200 Holding single servo pos
    //                     10 0x0400 Holding single servo pos in rad
    //                     11 0x0800 Servo cal flash write needed
    //                     12 0x1000 HAG good (valid terrain data)
    //                     13 0x2000 Empty
    //                     14 0x4000 Empty
    //                     15 0x8000 Empty
    // 20     FLOAT  GPS Longitude         Longitude of UAV in decimal degrees                     Deg
    // 24     FLOAT  GPS lat home position Latitude of Home Position of UAV in Degrees             deg
    // 28     FLOAT  GPS lon home position Longitude of Home Position of UAV in Degrees            deg
    // 32     UCHAR  Current Command       Current command of flight script (0 =first waypoint)    #
    // 33     UCHAR  Nav State             Navigation state of UAV (Check Appendix A)              #
    // 34     FLOAT  Desired lat           Desired latitude of UAV in Degrees                      deg
    // 38     FLOAT  Desired lon           Desired longitude of UAV in Degrees                     deg
    // 42     FLOAT  Time (time            Estimated time to target or in the case of a loiter,    seconds
    //               overtarget, time      time remaining (255 = indefinite loiter)
    //               left loiter)
    // 46     FLOAT  Distance from Target  Distance from desired UAV target                        meters
    // 50     UINT   Heading to            Heading to desired UAV target                           rad*1000
    //               Target (0->2*pi)
    // 52     UINT   FLC                   Feedback loop configuration                             int
    // 54     UINT   Wind Heading          Estimated wind heading                                  rad*1000
    //               (from) (0->2*pi)
    // 56     UCHAR  Wind Speed (m/s)      Estimated wind speed                                    (m/s) *6
    // 57     UINT   User IO pins(camera,  Used for io pins (camera, gimbal, etc)-see IO pin state bitmask
    //               gimbal, etc) - see 
    //               IO pin state
    // 59     UCHAR  UTC Year ( 2 digit)   GPS date (YEAR)                                         year
    // 60     UCHAR  UTC Month (1-12)      GPS date (MONTH)                                        month
    // 61     UCHAR  UTC Day               GPS date (DAY)                                          day number
    // 62     UCHAR  UTC Hour              GPS time (HOUR)                                         hours
    // 63     UCHAR  UTC Min               GPS time (MINUTE)                                       minutes
    // 64     UCHAR  UTC Sec               GPS time (SECOND)                                       seconds
    // 65     Float  Avx time              Time since bootup                                       Seconds
    // 69     Uint   Home Alt              Home Altitude in Meters*6 MSL (Mean Sea Level)          (Meters+1000)*6


    final static int OFF_GPS_VELOCITY = 6 + PASS_THROUGH_OFFSET;	// UINT
    final static int OFF_GPS_ALT = 8 + PASS_THROUGH_OFFSET;		// UINT
    final static int OFF_GPS_HEADING = 10 + PASS_THROUGH_OFFSET;	// UINT
    final static int OFF_TEMPERATURE_R_GYRO = 12 + PASS_THROUGH_OFFSET;	// UCHAR (1 byte!)
    final static int OFF_GPS_LAT = 14 + PASS_THROUGH_OFFSET;		// FLOAT
    final static int OFF_GPS_LON = 20 + PASS_THROUGH_OFFSET;		// FLOAT
    final static int OFF_GPS_LAT_HOME = 24 + PASS_THROUGH_OFFSET;	// FLOAT
    final static int OFF_GPS_LON_HOME = 28 + PASS_THROUGH_OFFSET;	// FLOAT
    final static int OFF_HOME_ALT = 69 + PASS_THROUGH_OFFSET;		// UINT

    // One thing that isn't yet clear to me - the 'data' part of a
    // packet is checksummed with XOR (described on page 16 of the
    // docs) and then 'escaped' to remove values of;
    //
    // 0xFF - the start byte marker value - marks the start of the data section
    // 0xFE - the end byte - marks the end of the data section
    // 0xFD - the escape byte marker)
    // 0xCC - something related to RSSI that isn't clear to me.  
    //
    // Decoding of packets (in general) is described on page 17;
    // 
    // 1.6.4 Packet Decoding Steps
    // Listed below are the steps required when receiving a packet. These must be followed in
    // order.
    // 1. Replace all the special character codes with the original data content using Table 1.5.
    // 2. Generate an XOR Check value over that data and compare it to the value stored in
    // the packet. If it does not match discard this packet.
    // 3. Check the destination address. If it does not match the address stored, discard the
    // packet.
    // 4. Process the packet using the packet type.
    //
    // Of course this would apply 


    // Where the HELL is Robin getting these field offset values from?
    // I suspect he may be using the 'passthrough' mode but I'm still
    // not entirely clear.  
    //
    //     public static final int LATITUDE_OFFSET = 22;
    //     public static final int LONGITUDE_OFFSET = 28;
    //     public static final int UAV_ADDRESS_OFFSET = 10;
    //     public static final int DISTANCE_TO_TARGET_OFFSET =54;
    //     public static final int NAV_PACKET_DATA_SIZE = 75;
    //     public static final int DATA_SIZE_OFFSET = 4;
    //
    // In passthrough mode you have a struct with int PacketType, int
    // DatasIze, and an array of 1024 bytes.  So that explains the
    // offset to DataSize, now where are the rest of them specified?
    // 
    // I figure they might be specified in offsets from the beginning
    // of the array, in the docs, whereas in the RObin's code they are
    // specified in offset from the beginning of the packet - if so
    // then each offset in the code will be set to the offset in the
    // docs + 8, if so these values would be;
    //
    //     public static final int LATITUDE_OFFSET = 14;
    //     public static final int LONGITUDE_OFFSET = 20;
    //     public static final int UAV_ADDRESS_OFFSET = 2;
    //     public static final int DISTANCE_TO_TARGET_OFFSET = 46;
    // 
    // Note that in the overall packet structure on page 15, SRC
    // address is at bytes 2,3
    //
    // Page 42, Table 3.34 - Request Navigation Telemetry Response
    // 
    // 14	GPS Latitude - Latitude of UAV in decimal degrees
    // 20	GPS Longitude - Longitude of UAV in decimal degrees
    // 46	Distance from Target - Distance from desired UAV target
    // 
    // buh looks very likely.
    // 
    // OK, here is my estimate as to what is going on; The docs define
    // a packet header/footer, which is 6 bytes of header, some
    // variable amount of 'data', then 4 bytes of footer at the end.
    // The variable length data has no 'size' value for it, but
    // packets are started and ended by 'special characters', so the
    // data has to be 'escaped' and also a checksum is generated and
    // put in the footer.
    //
    // then there's 'passthrough' which has a 'packet type', a
    // dataSize, and some data.  So you hand that to the socket and it
    // takes care of doing all that escaping and checksumming and
    // adding footer and header - and likewise doing all that in
    // reverse when it gives data back to you.  Excpet that it doesn't
    // realyl strip off the header, those 6 bytes are still there so
    // you have to look at index value + 8 to find each packet field.
    //
    // So, all of the docs specify things for each packet type by
    // starting at packet 6 (0 indexed), which would normally be the
    // first byte in a packet, but in packet mode you have to instead
    // start at the index in the docs + 8... which doesn't really make
    // sense.

    // Byte Index Type Name Description Units
    // 6	UINT	GPS Velocity	GPS computed velocity (m/s+10)* 20
    // 8	UINT	GPS Alt	GPS computed altitude (meters+1000)* 6
    // 10	UINT	GPS Heading (0->2pi)	GPS computed heading rad*1000
    // 12	UCHAR	Dev Byte	Take Off countdown timer N/A
    // 13	UCHAR	Temperature R	Temperature of R Gyro calculated as (deg C +10) * 2.8 (degress C+10) * 2.8
    // 14	FLOAT	GPS Latitude	Latitude of UAV in decimal degrees deg
    // 18	INT	Sys flags 1
    //
    // System Flags 1
    // bit Bitmask Description
    // 0 0x0001 Gimbal Mode0 bit
    // 1 0x0002 Gimbal Mode 1 bit
    // 2 0x0004 Empty
    // 3 0x0008 MSL Alt calibrated
    // 4 0x0010 Home Alt set
    // 5 0x0020 Alt over-ride enabled
    // 6 0x0040 Airspeed over-ride enabled
    // 7 0x0080 Flair enabled
    // 8 0x0100 Side look camera selected
    // 9 0x0200 Empty
    // 10 0x0400 Empty
    // 11 0x0800 Empty
    // 12 0x1000 Empty
    // 13 0x2000 Empty
    // 14 0x4000 Empty
    // 15 0x8000 Empty
    //
    // 20	FLOAT	GPS Longitude	Longitude of UAV in decimal degrees Deg
    // 24	FLOAT	GPS lat home position	Latitude of Home Position of UAV in Degrees deg
    // 28	FLOAT	GPS lon home position	Longitude of Home Position of UAV in Degrees deg
    // 32	UCHAR	Current Command	Current command of flight script (0 =first waypoint) 
    // 33	UCHAR	Nav State	Navigation state of UAV (Check Appendix A) 
    // 34	FLOAT	Desired lat	Desired latitude of UAV in Degrees deg
    // 38	FLOAT	Desired lon	Desired longitude of UAV in Degrees deg
    // 42	FLOAT	Time (time over target, time left loiter)	Estimated time to target or in the case of a loiter, time remaining (255 = indefinite loiter)	seconds
    // 46	FLOAT	Distance from Target	Distance from desired UAV target meters
    // 50	UINT	Heading to Target (0->2*pi)	Heading to desired UAV target rad*1000
    // 52	UINT	FLC	Feedback loop configuration int
    // 54	UINT	Wind Heading(from) (0->2*pi)	Estimated wind heading rad*1000
    // 56	UCHAR	Wind Speed (m/s)	Estimated wind speed (m/s) *6
    // 57	UINT	User IO pins	(camera, gimbal, etc) - see IO pin state	Used for io pins (camera, gimbal, etc) - see IO pin state bitmask
    // 59	UCHAR	UTC Year ( 2 digit)	GPS date (YEAR) year
    // 60	UCHAR	UTC Month (1-12)	GPS date (MONTH) month
    // 61	UCHAR	UTC Day	GPS date (DAY) daynumber
    // 62	UCHAR	UTC Hour	GPS time (HOUR) hours
    // 63	UCHAR	UTC Min	GPS time (MINUTE) minutes
    // 64	UCHAR	UTC Sec	GPS time (SECOND) seconds
    // 65	Float	Avx time	Time since bootup Seconds
    // 69	Uint	Home Alt	Home Altitude in Meters*6 MSL (Mean Sea Level) (Meters+1000)*6


    final static int BUF_SIZE=129*1024;
    String logFileName = null;

    int infoNumPackets = 0;
    
    PrintStream infoStream = System.out;

    DataInputStream logStream = null;
    boolean count = true;

    HashMap<Integer,Long> countSizes = new HashMap<Integer,Long>();

    public KestrelDecoder(String logFileName) {
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

	int count = 0;
	// Now, start decoding the packets.
	try {
	    byte[] buffer = new byte[BUF_SIZE]; 
	    long firstPacketTime = -1;
	    while(true) {
		count++;
		if(count>150) {
		    System.exit(0);
		}
		// Note that the log format is very simple - a long
		// representing the time the packet was received, in
		// seconds since Jan 1, 1970, then a long with the
		// size of the packet, then the raw data of the packet
		// itself.
		long packetTime = logStream.readLong(); 
		//		long packetTime = 0;
		int bufferLen = (int)logStream.readLong();
		logStream.read(buffer, 0, bufferLen);
		
		// Note that the 'packetTime' saved when we created
		// the log was saved as an absolute value (i.e. number
		// of milliseconds since Jan 1, 1970), so we have to
		// keep track of and calculate the time since we
		// started the playback.
		if(-1 == firstPacketTime) {
		    firstPacketTime = packetTime;
		}
		long relativePacketTime = (packetTime - firstPacketTime);
		countSizes(bufferLen);
		//		System.err.println(bufferLen+"\t"+relativePacketTime);
		decodePacket(packetTime, buffer, bufferLen);
		// 		if(IGNORE_RESENDS && (dp.sentMsgCount > 1)) 
		// 		    continue;
		// 		if(IGNORE_ACKS && dp.isAck) 
		// 		    continue;
		//		infoStream.println(dp);
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

    public void decodePacket(long packetTime, byte[] buf, int length) {
	//	infoStream.print("packet len="+length+" time="+packetTime);
	try {
	    int packetType = 0x000000FF & ((int)buf[OFF_TYPE]);
	    if (248 == packetType) {
		DecodedPacket248 dp = new DecodedPacket248(packetTime, length) ;
		dp.decodeBuffer(buf) ;
		dp.printHeader(System.err);
		System.err.println(dp);
	    }
	    else if (249 == packetType) {
		DecodedPacket249 dp = new DecodedPacket249(packetTime, length) ;
		dp.decodeBuffer(buf) ;
		dp.printHeader(System.err);
		System.err.println(dp);
	    }
	    else {
		System.err.println("Unknown packet type = "+packetType);
	    }
	}
	catch(NetByteDecodingException e) {
	    infoStream.println("Exception decoding fields of packet, e="+e);
	    e.printStackTrace();
	}
	
	// 	int dataStart = HEADER_SIZE+PASS_THROUGH_OFFSET;
	// 	int dataLen = length - HEADER_SIZE - FOOTER_SIZE - PASS_THROUGH_OFFSET;
	
	//  	byte[] original = new byte[dataLen];
	
	//  	for(int loopi = 0, loopj = dataStart; loopi < dataLen; loopi++,loopj++) {
	//  	    original[loopi] = buf[loopj];
	//  	}
	//  	int originalXorCheckSum = calculateXorCheckSum(original, original.length);
	
	//  	byte[] unescaped = unescapePacket(original, 0, dataLen);
	//  	int xorCheckSum = calculateXorCheckSum(unescaped, unescaped.length);
	
	// 	infoStream.print("packet xor= "+dp.xorCheck+"\t xor before unescape= "+originalXorCheckSum+"\t xor after unescaping= "+xorCheckSum);
	// 	if(originalXorCheckSum != xorCheckSum) {
	// 	    infoStream.print("\toriginal vs escaped xor do not match");
	// 	}
	// 	if(dp.xorCheck != xorCheckSum) {
	// 	    infoStream.print("\tpacket XOR does not match calculated XOR");
	// 	}
	// 	infoStream.println();
	// infoStream.println(dp);
    }

    public void printCounts() {
 	ArrayList<Integer> sizeKeys = new ArrayList<Integer>(countSizes.keySet());
 	Collections.sort(sizeKeys);
 	System.err.println("Printing "+sizeKeys.size()+" size keys");
 	for(Integer key: sizeKeys) {
 	    infoStream.println("SIZE\t"+key+"\t"+countSizes.get(key));
 	}

	// 	ArrayList<String> sentKeys = new ArrayList<String>(countSent.keySet());
	// 	Collections.sort(sentKeys);
	// 	System.err.println("Printing "+sentKeys.size()+" sentKeys");
	// 	for(String key: sentKeys) {
	// 	    //	    System.err.println("COUNT\t"+key+"\t"+countSent.get(key));
	// 	    infoStream.println("SENT\t"+key+"\t"+countSent.get(key));
	// 	}
	// 	ArrayList<String> receivedKeys = new ArrayList<String>(countReceived.keySet());
	// 	Collections.sort(receivedKeys);
	// 	System.err.println("Printing "+receivedKeys.size()+" receivedKeys");
	// 	for(String key: receivedKeys) {
	// 	    //	    System.err.println("COUNT\t"+key+"\t"+countReceived.get(key));
	// 	    infoStream.println("RECV\t"+key+"\t"+countReceived.get(key));
	// 	}
	infoStream.flush();
    }

    //     public void countAck(UDPMessage msg) {
    // 	String source = msg.getSource().toString();
    // 	String dest = null;
    // 	if(msg.getDest() != null)
    // 	    dest = msg.getDest().toString();
    // 	String className = "Ack";
    // 	if(null!= source) {
    // 	    Long sent = countSent.get(source+"\t"+className);
    // 	    if(null == sent) 
    // 		sent = new Long(1);
    // 	    else
    // 		sent++;
    // 	    countSent.put(source+"\t"+className, sent);
    // 	}
    // 	if(null != dest) {
    // 	    Long received = countReceived.get(dest+"\t"+className);
    // 	    if(null == received) 
    // 		received = new Long(1);
    // 	    else
    // 		received++;
    // 	    countReceived.put(source+"\t"+className, received);
    // 	}
    //     }

    //     public void countNull(UDPMessage msg) {
    // 	String source = msg.getSource().toString();
    // 	String dest = msg.getDest().toString();
    // 	String className = "null";
    // 	if(null!= source) {
    // 	    Long sent = countSent.get(source+"\t"+className);
    // 	    if(null == sent) 
    // 		sent = new Long(1);
    // 	    else
    // 		sent++;
    // 	    countSent.put(source+"\t"+className, sent);
    // 	}
    // 	if(null != dest) {
    // 	    Long received = countReceived.get(dest+"\t"+className);
    // 	    if(null == received) 
    // 		received = new Long(1);
    // 	    else
    // 		received++;
    // 	    countReceived.put(source+"\t"+className, received);
    // 	}
    //     }

    //     public void countMsg(UDPMessage msg) {
    // 	String source = msg.getSource().toString();
    // 	String dest = msg.getDest().toString();
    // 	String className = msg.getMsg().getClass().getName();
    // 	if(null!= source) {
    // 	    Long sent = countSent.get(source+"\t"+className);
    // 	    if(null == sent) 
    // 		sent = new Long(1);
    // 	    else
    // 		sent++;
    // 	    countSent.put(source+"\t"+className, sent);
    // 	}
    // 	if(null != dest) {
    // 	    Long received = countReceived.get(dest+"\t"+className);
    // 	    if(null == received) 
    // 		received = new Long(1);
    // 	    else
    // 		received++;
    // 	    countReceived.put(source+"\t"+className, received);
    // 	}
    //     }

    //     public void countObjectMsg(UDPMessage msg, Object omsg) {
    // 	String source = msg.getSource().toString();
    // 	String dest = msg.getDest().toString();
    // 	String className = omsg.getClass().getName();
    // 	if(null!= source) {
    // 	    Long sent = countSent.get(source+"\t"+className);
    // 	    if(null == sent) 
    // 		sent = new Long(1);
    // 	    else
    // 		sent++;
    // 	    countSent.put(source+"\t"+className, sent);
    // 	}
    // 	if(null != dest) {
    // 	    Long received = countReceived.get(dest+"\t"+className);
    // 	    if(null == received) 
    // 		received = new Long(1);
    // 	    else
    // 		received++;
    // 	    countReceived.put(source+"\t"+className, received);
    // 	}
    //     }

    public void countSizes(int size) {
	Long sizeCount = countSizes.get(size);
	if(null == sizeCount) 
	    sizeCount = new Long(1);
	else
	    sizeCount++;
	countSizes.put(size, sizeCount);
    }

    // Here is where all the heavy lifting is done;
    //
    // From pdf file Kestrel_Communications_MA7.1_B.pdf;
    //
    // Table 1.4 - Packet Structure
    // Byte	Name		Description
    // 0 	Start Byte 	(Always 0xFF) Signals start of packet
    // 1 	Type		This particular packet type
    // 2 	Src Addr Low 	Low byte of packet source address (sender)
    // 3 	Src Addr High	High byte of packet source address
    // 4 	Dest Addr Low	Low byte of packet destination address (receiver)
    // 5 	Dest Addr High	High byte of packet destination address
    // 6 	Packet Data 	Start of packet data, determined by packet type
    // ...	...
    // n-3	Packet ID	A sequential number generated by the ground station and returned by
    //		number		the autopilot
    // n-2	XOR Check 1 	Low byte of XOR Check
    // n-1	XOR Check 2	High byte of XOR Check
    // n 	End Byte 	(Always 0xFE) Signals end of packet

    // Each packet has a start byte and an end byte of 0xFF and 0xFE
    // respectively. This allows for quickly determining the beginning
    // and end of a packet when parsing through the serial stream.  To
    // eliminate the possibility of a 0xFF and 0xFE showing up in the
    // normal data, giving the perception of a new packet, these
    // characters are removed from the stream. These special
    // characters are removed from the stream and replaced by a two
    // byte value. The following table shows how they are replaced.
    //
    // Table 1.5 - Character Replacement
    // Before 	After    	Method
    // 0xFD	0xFD, 0x00	Start byte is replaced by two characters
    // 0xFE	0xFD, 0x01	End byte is replaced by two characters
    // 0xFF	0xFD, 0x02	0xFD is replaced by two characters
    // 0xCC	0xFD, 0x03	0xCC is replaced by two characters
    //
    // 0xFD is removed because it becomes the key for recognizing when to replace the data stream
    // bytes with the original contents. Also 0xCC is included in removal to provide support for
    // measuring RSSI from the Aerocomm modems and future development work.


    public byte[] unescapePacket(byte[] buf, int start, int len) {
	ArrayList<Byte> byteList = new ArrayList<Byte>();
	for(int loopi = start; loopi < (start + len); loopi++) {
	    if(buf[loopi] == P_ESCAPE_BYTE) {
		if(buf[loopi+1] == (byte)0) 
		    byteList.add(P_ESCAPE_BYTE_00);
		else if(buf[loopi+1] == (byte)1) 
		    byteList.add(P_ESCAPE_BYTE_01);
		else if(buf[loopi+1] == (byte)2) 
		    byteList.add(P_ESCAPE_BYTE_02);
		else if(buf[loopi+1] == (byte)3) 
		    byteList.add(P_ESCAPE_BYTE_03);
		loopi++;
	    }
	    else 
		byteList.add(buf[loopi]);
	}
	byte[] newbuf = new byte[byteList.size()];
	for(int loopi = 0; loopi < byteList.size(); loopi++)
	    newbuf[loopi] = byteList.get(loopi);
	return newbuf;
    }

    // Code from Kestrel_Communications_MA7.1_B.pdf page 16, slightly
    // modified to work in java.
    private int calculateXorCheckSum(byte[] PacketData, int PacketSize) {
	// PacketData is the array that holds the entire packet
	// before special characters are removed
	int CheckValue = 0; //16 bit check value
	//Skip Start, Last Byte, Check Value 
	for (int i=1; i<PacketSize-1; i += 2 ) {
	    CheckValue ^= PacketData[i] << 8; //Shift bits to high order byte and XOR
	    CheckValue ^= PacketData [i+1]; //XOR low order byte
	}
	if(((PacketSize - 1) % 2) != 0) //Check to see if packet is odd in size
	    CheckValue ^= PacketData[PacketSize-1] << 8;
	return CheckValue;
    }

    private static void printUsageAndExit(String errorMsg) {
	if(errorMsg != null) 
	    System.err.println("ERROR:"+errorMsg);
	System.err.println("Usage: java KestrelDecoder -l logfilename");
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

	KestrelDecoder decode;
	decode = new KestrelDecoder(logFileName);
	decode.decode();
    }

}
