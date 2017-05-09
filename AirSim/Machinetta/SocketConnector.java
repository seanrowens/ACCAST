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
 * SocketConnector.java
 *
 * Created on August 23, 2007, 2:05 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package AirSim.Machinetta;


import java.util.*;
import java.net.*;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.*;
import java.util.concurrent.LinkedBlockingDeque;
/**
 *
 * @author Owner
 */
public class SocketConnector{
    
    //Packet types defined in VC interface
    public static final int VC_PASSTHROUGH = 10;	//This packet when received by the VC will pass all data straight to the autopilots
    public static final int VC_PASSTHROUGH_GRNTD = 11;	//This packet when received by the VC will pass all data straight to the autopilots but gurantees it gets there through retries
    public static final int VC_FRWD_PKT_SETUP = 20;	//This packet setups the VC to forward messages it receives from the autopilots
    public static final int VC_UPLOAD_CMDS = 30;  //This packet instructs the VC to upload the current command list for the agent given
    public static final int VC_DOWNLOAD_CMDS = 31;  //This packet instructs the VC to download the commands from the agent given
    public static final int VC_SET_CMD_LIST = 32;	//This packet is used to setup the command list that the VC holds for the given agent
    public static final int VC_GET_CMD_LIST	= 33;	//This packet is used to get the command list that the VC holds for the given agent
    public static final int VC_GIMBAL_CMD = 40;  //This packet is used to set the gimbal mode and command values based on the mode
    
    public static final int GIMBAL_MODE_JOY = 0;
    public static final int GIMBAL_MODE_TARGET_MSL = 1;
    public static final int GIMBAL_MODE_TARGET_METERS_ABOVE_LAUNCH = 2;
    public static final int LATITUDE_OFFSET = 22;
    public static final int LONGITUDE_OFFSET = 28;
    public static final int UAV_ADDRESS_OFFSET = 10;
    public static final int DISTANCE_TO_TARGET_OFFSET =54;
    public static final int NAV_PACKET_DATA_SIZE = 75;
    public static final int DATA_SIZE_OFFSET = 4;
    
    public static final int VC_PORT = 5005;

    private static final int SOCKET_CONNECT_TIMEOUT=1000;
    
    Socket m_VCSocket;				//The Socket to send/reveieve the data to the VC
    PrintStream dataOutputStream = null;

    boolean  m_VCServerConnected;	//If a connect is established
    boolean  m_DataThreadRunning;		//If the Read Thread is still running
    
    LinkedBlockingDeque<NavData> m_PktContainer;	//Holds the packets that haven't been read by the dev app
    
    
    private String logFileName = null;
    private DataOutputStream logStream = null;
    private DecodedPacketLogger decodedPacketLogger = null ;

    // Given a time, get a string representing the current time in
    // format YYYY_MM_DD_HH_MM_SS, i.e. 2005_02_18_23_16_24.  Using
    // this format results in timestamp strings (or filenames) that
    // will sort to date order.  If 'time' is null it will be
    // filled in with the current time.
    private static String getTimeStamp(Calendar time) {
        String timestamp = "";
        String part = null;
        if(null == time)
            time = Calendar.getInstance();
        
        timestamp = Integer.toString(time.get(time.YEAR));
        
        part = Integer.toString((time.get(time.MONTH)+1));
        if(1 == part.length())
            part = "0"+part;
        timestamp += part;
        
        part = Integer.toString(time.get(time.DAY_OF_MONTH));
        if(1 == part.length())
            part = "0"+part;
        timestamp += part;
        
        part = Integer.toString(time.get(time.HOUR_OF_DAY));
        if(1 == part.length())
            part = "0"+part;
        timestamp += part;
        
        part = Integer.toString(time.get(time.MINUTE));
        if(1 == part.length())
            part = "0"+part;
        timestamp += part;
        
        part = Integer.toString(time.get(time.SECOND));
        if(1 == part.length())
            part = "0"+part;
        timestamp += part;
        
        return timestamp;
    }

    /** Creates a new instance of SocketConnector */
    public SocketConnector(String ipAddress) {
        //All we want to do is connect to the Dev Server created by Virtual Cockpit
        m_VCServerConnected = false;
        m_DataThreadRunning = false;
        m_VCSocket = null;
        
        m_PktContainer = new LinkedBlockingDeque<NavData>();
        
        //Try to connect to the VC
        try{
	    // @TODO: Change all these prints to Debugger statements.
            System.err.println("SocketConnector.constructor: Opening socket to "+ipAddress.toString()+" port "+VC_PORT);
            m_VCSocket = new Socket();
	    m_VCSocket.bind(null);
	    m_VCSocket.connect(new InetSocketAddress(ipAddress, VC_PORT),SOCKET_CONNECT_TIMEOUT);
	    //            m_VCSocket = new Socket(ipAddress, VC_PORT);

	    dataOutputStream = new PrintStream(m_VCSocket.getOutputStream());

            System.err.println("SocketConnector.constructor: Done opening socket to "+ipAddress.toString()+" port "+VC_PORT);
	} catch(SocketTimeoutException se) {
            System.err.println("SocketConnector.constructor: ERROR ERROR Timed out trying to connect to socket after "+SOCKET_CONNECT_TIMEOUT+" ms.");
        } catch(Exception e){
            System.err.println("SocketConnector.constructor: Unable to make a connection to virtual cockpit, e="+e);
	    e.printStackTrace();
        }
        if (m_VCSocket != null) {
            m_VCServerConnected = true;
            //this.run();
            m_DataThreadRunning = true;
        }
    }
    public boolean IsConnected(){
        return m_VCServerConnected;
    }
 
    private void openLogFile() {
	logFileName = "vcbridge_packet_"+getTimeStamp(null)+".log";
	try {
	    logStream = new DataOutputStream(new FileOutputStream(logFileName));
	    this.decodedPacketLogger = new DecodedPacketLogger(logFileName) ;
	} catch (java.io.FileNotFoundException e) {
	    Machinetta.Debugger.debug( 3,"ERROR: Problems opening vcbridge packet log file='"+logFileName+"': "+e);
	    e.printStackTrace();
	}
    }
    void ReadData() {
        //We should never really go over 1K in data
        InputStream dataStream;
        String dataLine = null;
        sVCPacket RecvPkt = new sVCPacket();
        NavData navData = new NavData();
        
        int numbytes = 0;
        int datasize = 0;
        try{
            dataStream = m_VCSocket.getInputStream();
            numbytes = dataStream.read(RecvPkt.PktData,0,RecvPkt.PktData.length);
            //System.err.println("Here is what is in the packet "+byteArrayToHexString(RecvPkt.PktData, RecvPkt.PktData.length));
	    //System.err.println(RecvPkt.PktData);
        }
        catch(Exception e){
            System.err.println("SocketConnector.ReadData: Problem receiving packet from VC "+e);
        }

	
        if (numbytes == 0){
            //m_VCServerConnected = false;
            System.err.println("SocketConnector.ReadData: No packets received from VC");
        }
        else { 
            
	    // log the raw packet!
	    long receiveTime = System.currentTimeMillis();                
	    if(null == logStream) {
		openLogFile();
	    } 

            try {
		logStream.writeLong(receiveTime);
		logStream.writeLong(numbytes);
		logStream.write(RecvPkt.PktData,0, numbytes);
		logStream.flush();
		this.decodedPacketLogger.decodePacket(receiveTime, RecvPkt.PktData, numbytes) ;
	    } catch(java.io.IOException e) {
                Machinetta.Debugger.debug( 3,"Error writing to packet log: " + e);
            }
            //Push it onto the deque
            try{
               // System.err.println("SocketConnector.ReadData: Got a packet from VC with "+numbytes+" bytes "+NetByte.decodeInt(RecvPkt.PktData, 0)+" "+NetByte.decodeInt(RecvPkt.PktData,4));
		
		// @TODO:  Does the binary format include yaw/pitch/roll data?  we need it!
		

		// @TODO: Altitude - we don't decode altitude??  Not
		// good...  Robin thought it was decoded, but ignored,
		// but I think he really was thinking about the other
		// way around, sending commands to the uavs, not the
		// navdata.

                datasize = NetByte.decodeInt(RecvPkt.PktData,DATA_SIZE_OFFSET);
                if(datasize==NAV_PACKET_DATA_SIZE){
                    navData.procerusUAVAddress =  NetByte.decodeInt(RecvPkt.PktData,UAV_ADDRESS_OFFSET);
                    navData.distToTargetMets = NetByte.decodeFloat(RecvPkt.PktData,DISTANCE_TO_TARGET_OFFSET);
                    navData.latitude = NetByte.decodeFloat(RecvPkt.PktData,LATITUDE_OFFSET);
                    navData.longitude = NetByte.decodeFloat(RecvPkt.PktData,LONGITUDE_OFFSET);

		    Machinetta.Debugger.debug(1, "NAVDATA: address "+NetByte.decodeInt(RecvPkt.PktData,10)+" latitude "+NetByte.decodeFloat(RecvPkt.PktData,22)+" longitude "+NetByte.decodeFloat(RecvPkt.PktData,28)+" time "+System.currentTimeMillis());
		}
            }
            catch(Exception e){Machinetta.Debugger.debug(1,"Problem decoding Navigation and Telemetry data "+e);}
            //System.err.println(packetsAvailable());
	    m_PktContainer.addLast(navData);
            
           // System.err.println(packetsAvailable());
        }
    }

    boolean SendData(sVCPacket Pkt) {
        //System.err.println("SocketConnector.SendData: Send Data called");
        byte[] dataLine;
        
        if(m_VCServerConnected) {
            
            try{
                dataLine = Pkt.PktData;
		//                System.err.println("SocketConnector.SendData: Connection established to VC with "+dataLine.length+" bytes to send");
                dataOutputStream.write(dataLine,0,Pkt.DataSize+8);
		dataOutputStream.flush();
                return true;
            }
            catch(Exception e){
                System.err.println("SocketConnector.SendData: This is the reason for the error "+e);
		e.printStackTrace();
                return false;
            }
        }
        return false;
    }
    
    NavData GetNavData() {
        //Send the front of the list
        NavData navData = m_PktContainer.poll();
        return navData;
    }

    NavData takeNavData() {
        //Send the front of the list
	NavData navData = null;
	try {
	    navData = m_PktContainer.take();
	} catch (InterruptedException e) {};

        return navData;
    }
    public boolean packetsAvailable(){
        //System.err.println("SocketConnector.packetsAvailable: Current number on queue"+m_PktContainer.size());
        return !m_PktContainer.isEmpty();        
    }
    private void PopVCPacket() {
        m_PktContainer.remove();
    }
    static String byteArrayToHexString(byte in[], int len) {
	byte ch = 0x00;
	int i = 0;
	if (in == null || len <= 0)
	    return null;
	String pseudo[] = {"0", "1", "2",
			   "3", "4", "5", "6", "7", "8",
			   "9", "A", "B", "C", "D", "E",
			   "F"};
	StringBuffer out = new StringBuffer(in.length * 2);
	
	while (i < len) {
	    ch = (byte) (in[i] & 0xF0);     // Strip off high nibble
	    ch = (byte) (ch >>> 4);         // shift the bits down
	    ch = (byte) (ch & 0x0F);         // must do this is high order bit is on!
	    out.append(pseudo[ (int) ch]);     // convert the nibble to a String Character
	    ch = (byte) (in[i] & 0x0F);     // Strip off low nibble
	    out.append(pseudo[ (int) ch]);     // convert the nibble to a String Character
	    i++;
	    out.append(',');
	}
	String rslt = new String(out);
	return rslt;
    }
    
}

