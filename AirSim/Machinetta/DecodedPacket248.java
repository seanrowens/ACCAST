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
package AirSim.Machinetta;
import java.io.*;
import java.text.DecimalFormat;

public class DecodedPacket248 {
    private final static DecimalFormat fmt5 = new DecimalFormat("0.000000");
    private final static DecimalFormat fmt2 = new DecimalFormat("0.00");

    final static int PASS_THROUGH_PACKET_TYPE_OFFSET = 0;
    final static int PASS_THROUGH_DATA_SIZE_OFFSET = 4;
    final static int PASS_THROUGH_OFFSET = 8;

    final static int OFF_GPS_VELOCITY = 6 + PASS_THROUGH_OFFSET;	// UINT
    final static int OFF_GPS_ALT = 8 + PASS_THROUGH_OFFSET;		// UINT
    final static int OFF_GPS_HEADING = 10 + PASS_THROUGH_OFFSET;	// UINT
    final static int OFF_TEMPERATURE_R_GYRO = 12 + PASS_THROUGH_OFFSET;	// UCHAR (1 byte!)
    final static int OFF_GPS_LAT = 14 + PASS_THROUGH_OFFSET;		// FLOAT
    final static int OFF_GPS_LON = 20 + PASS_THROUGH_OFFSET;		// FLOAT
    final static int OFF_GPS_LAT_HOME = 24 + PASS_THROUGH_OFFSET;	// FLOAT
    final static int OFF_GPS_LON_HOME = 28 + PASS_THROUGH_OFFSET;	// FLOAT
    final static int OFF_HOME_ALT = 69 + PASS_THROUGH_OFFSET;		// UINT

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

    long time = -1;
    int dataSize = -1;

    int passThroughPacketType = -1;
    int passThroughDataSize = -1;

    byte startByte = 0;
    byte endByte = 0;

    int type = -1;
    int srcAddr = -1;
    int destAddr = -1;
    int packetID = -1;	
    int xorCheckLowByte = 0;
    int xorCheckHighByte = 0;
    int xorCheck = 0;

    double gpsVelocity = -1;
    double gpsAlt = -1;
    double gpsHeading = -1;

    double temperatureRGyro = -1;
    double gpsLat = -1;
    double gpsLon = -1;
    double gpsLatHome = -1;
    double gpsLonHome = -1;
    double homeAlt = -1;

    public void init() {
    }

    public DecodedPacket248() {
	init();
    }

    public DecodedPacket248(long time, int dataSize) {
	this.time = time ;
	this.dataSize = dataSize ;
    }

    public String toString() {
	return ""
	    +time
	    +"\t"+passThroughPacketType
	    +"\t"+passThroughDataSize
	    // 		+"\t"+NetByte.byteToHexString(startByte)
	    // 		+"\t"+NetByte.byteToHexString(endByte)
	    +"\t"+type
	    // 		+"\t"+srcAddr
	    // 		+"\t"+destAddr 
	    // 		+"\t"+packetID
	    // 		+"\t"+xorCheck
	    +"\t"+fmt2.format(gpsVelocity)
	    +"\t"+fmt2.format(gpsAlt)
	    +"\t"+fmt2.format(gpsHeading)
	    +"\t"+fmt2.format(temperatureRGyro)
	    +"\t"+fmt5.format(gpsLat)
	    +"\t"+fmt5.format(gpsLon)
	    +"\t"+fmt5.format(gpsLatHome)
	    +"\t"+fmt5.format(gpsLonHome)
	    +"\t"+fmt2.format(homeAlt);
    }

    public String toString2() {
	return ""
	    // 		+time
	    +"\npassThroughPacketType\t"+passThroughPacketType
	    +"\npassThroughDataSize\t"+passThroughDataSize
	    // 		+"\nstartByte\t"+NetByte.byteToHexString(startByte)
	    // 		+"\nendByte\t"+NetByte.byteToHexString(endByte)
	    +"\ntype\t"+type
	    // 		+"\nsrcAddr\t"+srcAddr
	    // 		+"\ndestAddr\t"+destAddr 
	    // 		+"\npacketID\t"+packetID
	    // 		+"\nxorCheck\t"+xorCheck
	    +"\ngpsVelocity\t"+fmt2.format(gpsVelocity)
	    +"\ngpsAlt\t"+fmt2.format(gpsAlt)
	    +"\ngpsHeading\t"+fmt2.format(gpsHeading)
	    +"\ntemperatureRGyro\t"+fmt2.format(temperatureRGyro)
	    +"\ngpsLat\t"+fmt5.format(gpsLat)
	    +"\ngpsLon\t"+fmt5.format(gpsLon)
	    +"\ngpsLatHome\t"+fmt5.format(gpsLatHome)
	    +"\ngpsLonHome\t"+fmt5.format(gpsLonHome)
	    +"\nhomeAlt\t"+fmt2.format(homeAlt);
    }
    public void printHeader(PrintStream out) {
	out.println("time"
		    +"\t"+"ptType"
		    +"\t"+"ptSize"
		    // 			+"\t"+"startB"
		    // 			+"\t"+"endB"
		    +"\t"+"type"+type
		    // 			+"\t"+"srcAddr"
		    // 			+"\t"+"dstAddr"
		    // 			+"\t"+"pktID"
		    // 			+"\t"+"xorChk"
		    +"\t"+"gpsVel"
		    +"\t"+"gpsAlt"
		    +"\t"+"gpsHdng"
		    +"\t"+"tmpRGyr"
		    +"\t"+"gpsLat"
		    +"\t"+"gpsLon"
		    +"\t"+"LatHome"
		    +"\t"+"LonHome"
		    +"\t"+"AltHome");
    }

    public void decodeBuffer(byte[] buf) throws NetByteDecodingException {
	this.passThroughPacketType = NetByte.decodeInt(buf,PASS_THROUGH_PACKET_TYPE_OFFSET);
	this.passThroughDataSize = NetByte.decodeInt(buf,PASS_THROUGH_DATA_SIZE_OFFSET);

	this.startByte = buf[OFF_START];
	this.type = 0x000000FF & ((int)buf[OFF_TYPE]);
	int srcAddrLow = buf[OFF_SRC_ADDR_LOW];
	int srcAddrHigh = buf[OFF_SRC_ADDR_HIGH];
	this.srcAddr = (srcAddrHigh << 8) | srcAddrLow;
	int destAddrLow = buf[OFF_DEST_ADDR_LOW];
	int destAddrHigh = buf[OFF_DEST_ADDR_HIGH];
	this.destAddr = (destAddrHigh << 8) | destAddrLow;

	this.endByte = buf[this.dataSize - 1 - MINUS_OFF_END];
	this.xorCheckLowByte = 0xFF & buf[this.dataSize - 1 - MINUS_OFF_XOR_LOW_BYTE];
	this.xorCheckHighByte = 0xFF & buf[this.dataSize - 1 - MINUS_OFF_XOR_HIGH_BYTE];
	this.xorCheck = (this.xorCheckHighByte << 8) | this.xorCheckLowByte;
	this.packetID = buf[this.dataSize - 1 - MINUS_OFF_PACKET_ID];

	// I tried to add in converting from the funky units they use,
	// to something rational but something didn't work.  Commented
	// out for now.
	//
	// 6	UINT	GPS Velocity	GPS computed velocity (m/s+10)* 20
	// 8	UINT	GPS Alt	GPS computed altitude (meters+1000)* 6
	// 10	UINT	GPS Heading (0->2pi)	GPS computed heading rad*1000
	// 13	UCHAR	Temperature R	Temperature of R Gyro calculated as (deg C +10) * 2.8 (degress C+10) * 2.8
	// 69	Uint	Home Alt	Home Altitude in Meters*6 MSL (Mean Sea Level) (Meters+1000)*6


	// 	    this.gpsVelocity = (this.gpsVelocity/20) - 10;
	// 	    this.gpsAlt = (this.gpsAlt/6) - 1000;
	// 	    this.gpsHeading = Math.toDegrees(((double)this.gpsHeading)/1000);
	// 	    this.temperatureRGyro = ((this.temperatureRGyro/2.8) - 10);	// to Celsius
	// 	    this.temperatureRGyro = ((9.0/5.0)* (this.temperatureRGyro)) + 32;	// to farenheit
	// 	    this.homeAlt = (this.homeAlt/6) - 1000;


	this.gpsVelocity = Unsigned.readLittleUnsignedShortToInt(buf[OFF_GPS_VELOCITY], buf[OFF_GPS_VELOCITY+1]) ; // NetByte.decodeShort(buf,OFF_GPS_VELOCITY);
	this.gpsVelocity = (this.gpsVelocity / 20.0f) - 10.0f;	// (this.gpsVelocity/20) - 10;
	this.gpsAlt = Unsigned.readLittleUnsignedShortToInt(buf[OFF_GPS_ALT], buf[OFF_GPS_ALT+1]) ;	// NetByte.decodeShort(buf,OFF_GPS_ALT);
	this.gpsAlt = (this.gpsAlt / 6.0f) - 1000.0f;	//(this.gpsAlt/6) - 1000;
	this.gpsHeading = Unsigned.readLittleUnsignedShortToInt(buf[OFF_GPS_HEADING], buf[OFF_GPS_HEADING+1]) ;	// NetByte.decodeShort(buf,OFF_GPS_HEADING);
	this.gpsHeading = this.gpsHeading * 57.3f / 1000.0f;	// Math.toDegrees(((double)this.gpsHeading)/1000);
	this.temperatureRGyro = (float)(0xFF & buf[OFF_TEMPERATURE_R_GYRO]);
	this.temperatureRGyro = ((this.temperatureRGyro/2.8) - 10);	 // to Celsius
	this.temperatureRGyro = ((9.0/5.0)* (this.temperatureRGyro)) + 32;	 // to farenheit
	this.gpsLat = NetByte.decodeFloat(buf,OFF_GPS_LAT);
	this.gpsLon = NetByte.decodeFloat(buf,OFF_GPS_LON);
	this.gpsLatHome = NetByte.decodeFloat(buf,OFF_GPS_LAT_HOME);
	this.gpsLonHome = NetByte.decodeFloat(buf,OFF_GPS_LON_HOME);
	this.homeAlt = Unsigned.readLittleUnsignedShortToInt(buf[OFF_HOME_ALT], buf[OFF_HOME_ALT+1]) ;	// NetByte.decodeShort(buf,OFF_HOME_ALT);
	this.homeAlt = (this.homeAlt / 6.0f) - 1000.0f;// (this.homeAlt/6) - 1000;
    }

}
