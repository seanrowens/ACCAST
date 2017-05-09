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

public class DecodedPacket249 {
    private final static DecimalFormat fmt5 = new DecimalFormat("0.000000");
    private final static DecimalFormat fmt2 = new DecimalFormat("0.00");

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


    final static int OFF_ALTITUDE = 6 + PASS_THROUGH_OFFSET ; 
    final static int OFF_VELOCITY = 8 + PASS_THROUGH_OFFSET ; 
    final static int OFF_ROLL = 10 + PASS_THROUGH_OFFSET ; 
    final static int OFF_PITCH = 12 + PASS_THROUGH_OFFSET ; 
    final static int OFF_HEADING = 14 + PASS_THROUGH_OFFSET ; 
    final static int OFF_ALTITUDEMSL = 63 + PASS_THROUGH_OFFSET ; 

    long time = -1;
    int length = -1;

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

    public double altitude = 0 ; // UAV altitude (meters+1000)* 6
    public double velocity = 0; // UAV velocity (m/s+10) *20
    public double roll = 0; // Current roll angle (-pi->pi) rad*1000
    public double pitch = 0; // Current pitch angle (-pi->pi) rad*1000
    public double heading = 0; // Current heading angle (0->2pi) rad*1000
    public double altitudeMSL  = 0; // altitude above sea level (meters MSL)  (meters+1000)* 6

    public DecodedPacket249() {
    }

    public DecodedPacket249(long time, int length) {
	this.time = time ;
	this.length = length ;
    }

    public void printHeader(PrintStream out) {
	out.println("time"
		    +"\t"+"ptType"
		    +"\t"+"ptSize"
		    // 		+"\tstartByte"
		    // 		+"\tendByte"
		    +"\t"+"type"+type
		    // 		+"\tsrcAddr"
		    // 		+"\tdestAddr"
		    // 		+"\tpacketID"
		    // 		+"\txorCheck"
		    +"\talt"
		    +"\tvel"
		    +"\troll"
		    +"\tpitch"
		    +"\theading"
		    +"\taltMSL") ;
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
	    +"\t"+fmt2.format(this.altitude)
	    +"\t"+fmt2.format(this.velocity)
	    +"\t"+fmt2.format(this.roll)
	    +"\t"+fmt2.format(this.pitch)
	    +"\t"+fmt2.format(this.heading)
	    +"\t"+fmt2.format(this.altitudeMSL) ;
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
	    +"\naltitude\t"+fmt2.format(this.altitude)
	    +"\nvelocity\t"+fmt2.format(this.velocity)
	    +"\nroll\t"+fmt2.format(this.roll)
	    +"\npitch\t"+fmt2.format(this.pitch)
	    +"\nheading\t"+fmt2.format(this.heading)
	    +"\naltitudeMSL\t"+fmt2.format(this.altitudeMSL) ;
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
	
	this.endByte = buf[this.length - 1 - MINUS_OFF_END];
	this.xorCheckLowByte = 0x000000FF & ((int)buf[this.length - 1 - MINUS_OFF_XOR_LOW_BYTE]);
	this.xorCheckHighByte = 0x000000FF & ((int)buf[this.length - 1 - MINUS_OFF_XOR_HIGH_BYTE]);
	this.xorCheck = (this.xorCheckHighByte << 8) | this.xorCheckLowByte;
	this.packetID = buf[this.length - 1 - MINUS_OFF_PACKET_ID];

	this.altitude = Unsigned.readLittleUnsignedShortToInt(buf[OFF_ALTITUDE], buf[OFF_ALTITUDE + 1]) ;	// 2 byte unsigned int (i.e. short), Altitude above sea level (meters MSL) (meters +1000)*6
	this.velocity = Unsigned.readLittleUnsignedShortToInt(buf[OFF_VELOCITY], buf[OFF_VELOCITY + 1]) ;	// UINT, UAV velocity (m/s+10) *20
// 	this.roll = NetByte.decodeShort(buf, OFF_ROLL) ;	// 2 byte signed int (i.e. short), Current roll angle (-pi->pi) rad*1000
// 	this.pitch = NetByte.decodeInt(buf, OFF_PITCH) ;	// 2 byte signed int (i.e. short), Current pitch angle (-pi->pi) rad*1000
	this.roll = Unsigned.readLittleUnsignedShortToInt(buf[OFF_ROLL], buf[OFF_ROLL + 1]); // NetByte.decodeShort(buf, OFF_ROLL) ;	// 2 byte signed int (i.e. short), Current roll angle (-pi->pi) rad*1000
	this.pitch = Unsigned.readLittleUnsignedShortToInt(buf[OFF_PITCH], buf[OFF_PITCH + 1]); // NetByte.decodeInt(buf, OFF_PITCH) ;	// 2 byte signed int (i.e. short), Current pitch angle (-pi->pi) rad*1000
	this.heading = Unsigned.readLittleUnsignedShortToInt(buf[OFF_HEADING], buf[OFF_HEADING + 1]) ;	// 2 byte UNsigned int (i.e. short), although the devdemo treats it as a signed short, Current heading angle (0->2pi) rad*1000
	this.altitudeMSL = Unsigned.readLittleUnsignedShortToInt(buf[OFF_ALTITUDEMSL], buf[OFF_ALTITUDEMSL + 1]) ;	// Uint, Altitude above sea level (meters MSL) (meters +1000)*6


	this.roll = this.roll * 57.3f / 1000.0f;
	this.pitch = this.pitch * 57.3f / 1000.0f;
	this.heading = this.heading * 57.3f / 1000.0f;
	this.altitude = (this.altitude / 6.0f) - 1000.0f;
	this.altitudeMSL = (this.altitudeMSL / 6.0f) - 1000.0f;
	this.velocity = (this.velocity / 20.0f) - 10.0f;
    }

}

/** 
    3.62. Telemetry Packet (249)
    Packet 249 contains standard telemetry information from the autopilot. Packet 249 is sent by the
    autopilot on receipt of a request standard telemetry packet (26) or automatically at the rate set by
    the integer autopilot variable status to GCS send time when polling mode is disabled.
    Table 3.78 ó Standard Telemetry Response

    Byte
    Index Type Name Description Units
    6	UINT	Altitude	UAV altitude (meters+1000)* 6
    8	UINT	Velocity	UAV velocity (m/s+10) *20
    10	INT	Roll	Current roll angle (-pi->pi) rad*1000
    12	INT	Pitch	Current pitch angle (-pi->pi) rad*1000
    14	UINT	Heading	Current heading angle (0->2pi) rad*1000
    16	INT	TurnRate	UAV turn rate in radians per second rad/s*1000
    18	UCHAR	RSSI	Modem received signal strength indicator db
    19	UCHAR	PPS	Number of RC trainer packets per second received pps*2
    20	UCHAR	CurrentDraw	Amount of current being drawn (Requires current shunt circuitry)  amps*3
    21	UCHAR	BatteryVoltage	Input battery voltage volts*5
    22	UINT	SystemStatus	(see System Status section for bitmasks) bitmask
    24	UCHAR	GPSNumSats	Number of GPS satellites being tracked #
    25	UCHAR	AltTrackerMode	 state
    26	UINT	DesiredAlt	Desired UAV altitude (meters+1000)* 6
    28	UCHAR	DesiredVel	Desired UAV velocity (m/s+10) *2
    29	INT	DesiredRoll	Desired UAV roll rad*1000
    31	INT	DesiredPitch	Desired UAV pitch rad*1000
    33	UINT	DesiredHeading	Desired UAV heading (0->2pi) rad*1000
    35	INT	DesiredTurn	Rate Desired UAV turn rate rad/s*1000
    37	CHAR	Aileron	Aileron servo output rad*70
    38	CHAR	Elevator	Elevator servo output rad*70
    39	UCHAR	Throttle	Throttle servo output %
    40	CHAR	Rudder	Rudder servo output rad*70
    41	UCHAR	UAVMode	Current UAV Mode #
    42	UINT	FailsafeStatus	Failsafe status flags (Check Appendix A) bit mask
    44	UINT	MagHeading	Magnetometer heading (0->2pi) rad*1000
    46	FLOAT	AirborneTimer	Amount of time the autopilot has been airborne seconds
    50	FLOAT	AvionicsTimer	Amount of time the autopilot has been powered on seconds
    54	UINT	SystemFlags	BitMask
    56	UINT	GimbalAzimuth	Azimuth of gimbal (0=straight out nose, 1.57 = right wing) Rad*1000
    58	UINT	GimbalElevation	Elevation of gimbal (0=straight out nose, 1.57 = straight down)  Rad*1000
    60	Char	Rollrate	Angular rate around roll (P) Rad/sec*80
    61	Char	Pitchrate	Angular rate around pitch (Q) Rad/sec*80
    62	Char	Yawrate	Angular rate around yaw (R) Rad/sec*80
    63	Uint	AltitudeMSL	Altitude above sea level (meters MSL) (meters +1000) *6
    65	Float	GimbalTargetLat	Gimbal Target Latitude Deg
    69	Float	GimbalTargetLon	Gimbal Target Longitude Deg

    Individual Bitmasks Tables

    22	   UINT System Status (see System Status section for bitmasks)

    bit Bitmask Description
    0 0x0001 Servos are initialized
    1 0x0002 Pressure (airspeed, alt) initialized
    2 0x0004 GPS home initialized
    3 0x0008 2d or 3d GPS lock
    4 0x0010 Servo Cal is running
    5 0x0020 RC Mode enabled
    6 0x0040 Precision data logger has data (finished logging)
    7 0x0080 Valid GPS comm. (GPS receiver detected)
    8 0x0100 Flash needs to be written
    9 0x0200 Communications bad (no comm. Received for lost comm. Timeout time)
    10 0x0400 Low battery
    11 0x0800 RC check box not selected on this agent
    12 0x1000 Temp comp is running
    13 0x2000 Waypoints have been uploaded (airplane has waypoints)
    14 0x4000 Aircraft is airborne
    15 0x8000 Mag cal is running

    25 UCHAR Alt Tracker
    Mode
    Current mode of the Altitude Tracker
    0 = Altitude Tracker Off
    3 = Climb Mode
    4 = Hold Mode
    5 = Descent Mode

    41 UCHAR UAV Mode Current UAV Mode
    Index sensor
    0 Manual Mode
    1 Altitude Mode
    2 Nav Mode
    3 Home Mode
    4 Rally Mode
    5 Loiter Mode
    6 Take Off Mode
    7 Land Mode
    8 Circle Land Mode
    9 Circle Land Now Mode
    10 Pitch/Roll Mode (Joystick 1)
    11 Take Off to Waypoint Mode
    12 Joystick Land Mode
    13 Generic Land Mode
    14 Take Off Joystick Mode
    15 Take Off To WP Joystick Mode
    16 Follow Mode
    17 Loss comm. land rally
    18 Safe Mode

    54 UINT System Flags
    bit Bitmask Description
    0 0x0001 Sensor broadcast enabled
    1 0x0002 Sensor broadcast raw ad
    2 0x0004 Broadcast mag enabled
    3 0x0008 Broadcast servo ppm
    4 0x0010 Broadcast servo rad
    5 0x0020 Pid servo disconnect enabled
    6 0x0040 Servo pos hold enabled
    7 0x0080 Sensor check enabled
    8 0x0100 Psi initialized by GPS
    9 0x0200 Airspeed cal done
    10 0x0400 Take off timer expired
    11 0x0800 Take off timer enabled
    12 0x1000 Aerocomm pwr switch needed
    13 0x2000 Aercomm high pwr
    14 0x4000 Hil port A active
    15 0x8000 empty


    UCHAR	1	Unsigned Character	0 -> 255
    CHAR	1	Signed Character	-127 -> 127
    INT	2	Signed Integer		-32767 -> 32767
    UINT	2	Unsigned Integer	0 -> 65536
    FLOAT	4	IEEE 32bit floating point

*/






/** Leftovers   

final static int OFF_TURNRATE = 16 ; 
final static int OFF_RSSI = 18 ; 
final static int OFF_PPS = 19 ; 
final static int OFF_CURRENTDRAW = 20 ; 
final static int OFF_BATTERYVOLTAGE = 21 ; 
final static int OFF_SYSTEMSTATUS = 22 ; 
final static int OFF_GPSNUMSATS = 24 ; 
final static int OFF_ALTTRACKERMODE = 25 ; 
final static int OFF_DESIREDALT = 26 ; 
final static int OFF_DESIREDVEL = 28 ; 
final static int OFF_DESIREDROLL = 29 ; 
final static int OFF_DESIREDPITCH = 31 ; 
final static int OFF_DESIREDHEADING = 33 ; 
final static int OFF_DESIREDTURN = 35 ; 
final static int OFF_AILERON = 37 ; 
final static int OFF_ELEVATOR = 38 ; 
final static int OFF_THROTTLE = 39 ; 
final static int OFF_RUDDER = 40 ; 
final static int OFF_UAVMODE = 41 ; 
final static int OFF_FAILSAFESTATUS = 42 ; 
final static int OFF_MAGHEADING = 44 ; 
final static int OFF_AIRBORNETIMER = 46 ; 
final static int OFF_AVIONICSTIMER = 50 ; 
final static int OFF_SYSTEMFLAGS = 54 ; 
final static int OFF_GIMBALAZIMUTH = 56 ; 
final static int OFF_GIMBALELEVATION = 58 ; 
final static int OFF_ROLLRATE = 60 ; 
final static int OFF_PITCHRATE = 61 ; 
final static int OFF_YAWRATE = 62 ; 
final static int OFF_ALTITUDEMSL = 63 ; 
final static int OFF_GIMBALTARGETLAT = 65 ; 
final static int OFF_GIMBALTARGETLON = 69 ; 


public int   turnRate ; // UAV turn rate in radians per second rad/s*1000
public char  rSSI ; // Modem received signal strength indicator db
public char  pPS ; // Number of RC trainer packets per second received pps*2
public char  currentDraw ; // Amount of current being drawn (Requires current shunt circuitry)  amps*3
public char  batteryVoltage ; // Input battery voltage volts*5
public int   systemStatus ; // (see System Status section for bitmasks) bitmask
public char  gPSNumSats ; // Number of GPS satellites being tracked #
public char  altTrackerMode ; //  state
public int   desiredAlt ; // Desired UAV altitude (meters+1000)* 6
public char  desiredVel ; // Desired UAV velocity (m/s+10) *2
public int   desiredRoll ; // Desired UAV roll rad*1000
public int   desiredPitch ; // Desired UAV pitch rad*1000
public int   desiredHeading ; // Desired UAV heading (0->2pi) rad*1000
public int   desiredTurn ; // Rate Desired UAV turn rate rad/s*1000
public char  aileron ; // Aileron servo output rad*70
public char  elevator ; // Elevator servo output rad*70
public char  throttle ; // Throttle servo output %
public char  rudder ; // Rudder servo output rad*70
public char  uAVMode ; // Current UAV Mode #
public int   failsafeStatus ; // Failsafe status flags (Check Appendix A) bit mask
public int   magHeading ; // Magnetometer heading (0->2pi) rad*1000
public float airborneTimer ; // Amount of time the autopilot has been airborne seconds
public float avionicsTimer ; // Amount of time the autopilot has been powered on seconds
public int   systemFlags ; // BitMask
public int   gimbalAzimuth ; // Azimuth of gimbal (0=straight out nose, 1.57 = right wing) Rad*1000
public int   gimbalElevation ; // Elevation of gimbal (0=straight out nose, 1.57 = straight down)  Rad*1000
public char  rollrate ; // Angular rate around roll (P) Rad/sec*80
public char  pitchrate ; // Angular rate around pitch (Q) Rad/sec*80
public char  yawrate ; // Angular rate around yaw (R) Rad/sec*80
public int   altitudeMSL ; // Altitude above sea level (meters MSL) (meters +1000) *6
public float gimbalTargetLat ; // Gimbal Target Latitude Deg
public float gimbalTargetLon ; // Gimbal Target Longitude Deg


this.turnRate = Unsigned.readUnsignedInt(buf[OFF_TURNRATE]) ;	// INT, UAV turn rate in radians per second rad/s*1000
this.rSSI = Unsigned.readUnsignedInt(buf[OFF_RSSI]) ;	// UCHAR, Modem received signal strength indicator db
this.pPS = Unsigned.readUnsignedInt(buf[OFF_PPS]) ;	// UCHAR, Number of RC trainer packets per second received pps*2
this.currentDraw = Unsigned.readUnsignedInt(buf[OFF_CURRENTDRAW]) ;	// UCHAR, Amount of current being drawn (Requires current shunt circuitry)  amps*3
this.batteryVoltage = Unsigned.readUnsignedInt(buf[OFF_BATTERYVOLTAGE]) ;	// UCHAR, Input battery voltage volts*5
this.systemStatus = Unsigned.readUnsignedInt(buf[OFF_SYSTEMSTATUS]) ;	// UINT, (see System Status section for bitmasks) bitmask
this.gPSNumSats = Unsigned.readUnsignedInt(buf[OFF_GPSNUMSATS]) ;	// UCHAR, Number of GPS satellites being tracked #
this.altTrackerMode = Unsigned.readUnsignedInt(buf[OFF_ALTTRACKERMODE]) ;	// UCHAR,  state
this.desiredAlt = Unsigned.readUnsignedInt(buf[OFF_DESIREDALT]) ;	// UINT, Desired UAV altitude (meters+1000)* 6
this.desiredVel = Unsigned.readUnsignedInt(buf[OFF_DESIREDVEL]) ;	// UCHAR, Desired UAV velocity (m/s+10) *2
this.desiredRoll = Unsigned.readUnsignedInt(buf[OFF_DESIREDROLL]) ;	// INT, Desired UAV roll rad*1000
this.desiredPitch = Unsigned.readUnsignedInt(buf[OFF_DESIREDPITCH]) ;	// INT, Desired UAV pitch rad*1000
this.desiredHeading = Unsigned.readUnsignedInt(buf[OFF_DESIREDHEADING]) ;	// UINT, Desired UAV heading (0->2pi) rad*1000
this.desiredTurn = Unsigned.readUnsignedInt(buf[OFF_DESIREDTURN]) ;	// INT, Rate Desired UAV turn rate rad/s*1000
this.aileron = Unsigned.readUnsignedInt(buf[OFF_AILERON]) ;	// CHAR, Aileron servo output rad*70
this.elevator = Unsigned.readUnsignedInt(buf[OFF_ELEVATOR]) ;	// CHAR, Elevator servo output rad*70
this.throttle = Unsigned.readUnsignedInt(buf[OFF_THROTTLE]) ;	// UCHAR, Throttle servo output %
this.rudder = Unsigned.readUnsignedInt(buf[OFF_RUDDER]) ;	// CHAR, Rudder servo output rad*70
this.uAVMode = Unsigned.readUnsignedInt(buf[OFF_UAVMODE]) ;	// UCHAR, Current UAV Mode #
this.failsafeStatus = Unsigned.readUnsignedInt(buf[OFF_FAILSAFESTATUS]) ;	// UINT, Failsafe status flags (Check Appendix A) bit mask
this.magHeading = Unsigned.readUnsignedInt(buf[OFF_MAGHEADING]) ;	// UINT, Magnetometer heading (0->2pi) rad*1000
this.airborneTimer = Unsigned.readUnsignedInt(buf[OFF_AIRBORNETIMER]) ;	// FLOAT, Amount of time the autopilot has been airborne seconds
this.avionicsTimer = Unsigned.readUnsignedInt(buf[OFF_AVIONICSTIMER]) ;	// FLOAT, Amount of time the autopilot has been powered on seconds
this.systemFlags = Unsigned.readUnsignedInt(buf[OFF_SYSTEMFLAGS]) ;	// UINT, BitMask
this.gimbalAzimuth = Unsigned.readUnsignedInt(buf[OFF_GIMBALAZIMUTH]) ;	// UINT, Azimuth of gimbal (0=straight out nose, 1.57 = right wing) Rad*1000
this.gimbalElevation = Unsigned.readUnsignedInt(buf[OFF_GIMBALELEVATION]) ;	// UINT, Elevation of gimbal (0=straight out nose, 1.57 = straight down)  Rad*1000
this.rollrate = Unsigned.readUnsignedInt(buf[OFF_ROLLRATE]) ;	// Char, Angular rate around roll (P) Rad/sec*80
this.pitchrate = Unsigned.readUnsignedInt(buf[OFF_PITCHRATE]) ;	// Char, Angular rate around pitch (Q) Rad/sec*80
this.yawrate = Unsigned.readUnsignedInt(buf[OFF_YAWRATE]) ;	// Char, Angular rate around yaw (R) Rad/sec*80
this.gimbalTargetLat = Unsigned.readUnsignedInt(buf[OFF_GIMBALTARGETLAT]) ;	// Float, Gimbal Target Latitude Deg
this.gimbalTargetLon = Unsigned.readUnsignedInt(buf[OFF_GIMBALTARGETLON]) ;	// Float, Gimbal Target Longitude Deg

*/
