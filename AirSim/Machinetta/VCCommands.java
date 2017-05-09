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
 * VCCommands.java
 *
 * Created on August 24, 2007, 1:17 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package AirSim.Machinetta;
import java.nio.ByteBuffer;

/**
 *
 * @author Owner
 */
public class VCCommands {
    public static final int TYPE_WAYPOINT = 0;
    public static final int TYPE_LOITER = 1;
    public static final int TYPE_GOTO = 2;
    public static final int TYPE_TAKEOFF = 3;
    public static final int TYPE_APPR_LAND = 4;	//approach land
    SocketConnector m_VCConnector;
    
    //char m_UAVAddress = 1032;
    /** Creates a new instance of VCCommands */
    public VCCommands(SocketConnector sC) {
        m_VCConnector = sC;
    }
    void requestAllVCPackets(){
        //Make sure we are connected
        if(m_VCConnector == null) return;
        Machinetta.Debugger.debug(1, "Sending request for all packets");

	//Send a forward setup packet
	sVCPacket ForwardSetup = new sVCPacket();		//Interface packet
	ForwardSetup.VCPacketType = SocketConnector.VC_FRWD_PKT_SETUP;
        ForwardSetup.DataSize = 2;		//Always 2 for forward setup

	//First enable the acks packet forwarding
        ForwardSetup.PktData = new byte[10];
        ForwardSetup.PktData[3] = SocketConnector.VC_FRWD_PKT_SETUP >> 24;
        ForwardSetup.PktData[2] = SocketConnector.VC_FRWD_PKT_SETUP >> 16;
        ForwardSetup.PktData[1] = SocketConnector.VC_FRWD_PKT_SETUP >> 8;
        ForwardSetup.PktData[0] = (byte)ForwardSetup.VCPacketType;
        ForwardSetup.PktData[7] = (byte)(ForwardSetup.DataSize >> 24);
        ForwardSetup.PktData[6] = (byte)(ForwardSetup.DataSize >> 16);
        ForwardSetup.PktData[5] = (byte)(ForwardSetup.DataSize >> 8);
        ForwardSetup.PktData[4] = (byte)ForwardSetup.DataSize;
        
	ForwardSetup.PktData[8] = 0;
        //ForwardSetup.PktData[8] = 27; 
	ForwardSetup.PktData[9] = 1;
        m_VCConnector.SendData(ForwardSetup);	//Send it to the VC

    }

    void requestNavigationDataOnly(){
        //Make sure we are connected
        if(m_VCConnector == null) return;
        Machinetta.Debugger.debug(1,"Sending request for all packets");

	//Send a forward setup packet
	sVCPacket ForwardSetup = new sVCPacket();		//Interface packet
	ForwardSetup.VCPacketType = SocketConnector.VC_FRWD_PKT_SETUP;
        ForwardSetup.DataSize = 2;		//Always 2 for forward setup

	//First enable the acks packet forwarding
        ForwardSetup.PktData = new byte[10];
        ForwardSetup.PktData[3] = SocketConnector.VC_FRWD_PKT_SETUP >> 24;
        ForwardSetup.PktData[2] = SocketConnector.VC_FRWD_PKT_SETUP >> 16;
        ForwardSetup.PktData[1] = SocketConnector.VC_FRWD_PKT_SETUP >> 8;
        ForwardSetup.PktData[0] = (byte)ForwardSetup.VCPacketType;
        ForwardSetup.PktData[7] = (byte)(ForwardSetup.DataSize >> 24);
        ForwardSetup.PktData[6] = (byte)(ForwardSetup.DataSize >> 16);
        ForwardSetup.PktData[5] = (byte)(ForwardSetup.DataSize >> 8);
        ForwardSetup.PktData[4] = (byte)ForwardSetup.DataSize;
        
	ForwardSetup.PktData[8] = 1;
	ForwardSetup.PktData[9] = 0;
        m_VCConnector.SendData(ForwardSetup);	//Send it to the VC
	
	ForwardSetup.PktData[8] = 0;
	ForwardSetup.PktData[9] = 0;
        m_VCConnector.SendData(ForwardSetup);	//Send it to the VC

 	ForwardSetup.PktData[8] = (byte)249;
 	ForwardSetup.PktData[9] = 0;
	m_VCConnector.SendData(ForwardSetup);	//Send it to the VC
	
	
 	ForwardSetup.PktData[8] = (byte)248;
 	ForwardSetup.PktData[9] = 1;
	m_VCConnector.SendData(ForwardSetup);	//Send it to the VC
    }

    void request248Only() {
        //Make sure we are connected
        if(m_VCConnector == null) return;
	//        Machinetta.Debugger.debug(1,"Sending request for all packets");

	//Send a forward setup packet
	sVCPacket ForwardSetup = new sVCPacket();		//Interface packet
	ForwardSetup.VCPacketType = SocketConnector.VC_FRWD_PKT_SETUP;
        ForwardSetup.DataSize = 2;		//Always 2 for forward setup

	//First enable the acks packet forwarding
        ForwardSetup.PktData = new byte[10];
        ForwardSetup.PktData[3] = SocketConnector.VC_FRWD_PKT_SETUP >> 24;
        ForwardSetup.PktData[2] = SocketConnector.VC_FRWD_PKT_SETUP >> 16;
        ForwardSetup.PktData[1] = SocketConnector.VC_FRWD_PKT_SETUP >> 8;
        ForwardSetup.PktData[0] = (byte)ForwardSetup.VCPacketType;
        ForwardSetup.PktData[7] = (byte)(ForwardSetup.DataSize >> 24);
        ForwardSetup.PktData[6] = (byte)(ForwardSetup.DataSize >> 16);
        ForwardSetup.PktData[5] = (byte)(ForwardSetup.DataSize >> 8);
        ForwardSetup.PktData[4] = (byte)ForwardSetup.DataSize;
        

 	ForwardSetup.PktData[8] = (byte)248;	// navigation packet type
 	ForwardSetup.PktData[9] = 1;		// on
	m_VCConnector.SendData(ForwardSetup);	//Send it to the VC
    }

    void request249Only(){
        //Make sure we are connected
        if(m_VCConnector == null) return;
	//        Machinetta.Debugger.debug(1,"Sending request for all packets");

	//Send a forward setup packet
	sVCPacket ForwardSetup = new sVCPacket();		//Interface packet
	ForwardSetup.VCPacketType = SocketConnector.VC_FRWD_PKT_SETUP;
        ForwardSetup.DataSize = 2;		//Always 2 for forward setup

	//First enable the acks packet forwarding
        ForwardSetup.PktData = new byte[10];
        ForwardSetup.PktData[3] = SocketConnector.VC_FRWD_PKT_SETUP >> 24;
        ForwardSetup.PktData[2] = SocketConnector.VC_FRWD_PKT_SETUP >> 16;
        ForwardSetup.PktData[1] = SocketConnector.VC_FRWD_PKT_SETUP >> 8;
        ForwardSetup.PktData[0] = (byte)ForwardSetup.VCPacketType;
        ForwardSetup.PktData[7] = (byte)(ForwardSetup.DataSize >> 24);
        ForwardSetup.PktData[6] = (byte)(ForwardSetup.DataSize >> 16);
        ForwardSetup.PktData[5] = (byte)(ForwardSetup.DataSize >> 8);
        ForwardSetup.PktData[4] = (byte)ForwardSetup.DataSize;
        
 	ForwardSetup.PktData[8] = (byte)249;	// telemetry packet type
 	ForwardSetup.PktData[9] = 1;		// on
	m_VCConnector.SendData(ForwardSetup);	//Send it to the VC
    }

    void request248and249(){
        //Make sure we are connected
        if(m_VCConnector == null) return;
	//        Machinetta.Debugger.debug(1,"Sending request for all packets");

	//Send a forward setup packet
	sVCPacket ForwardSetup = new sVCPacket();		//Interface packet
	ForwardSetup.VCPacketType = SocketConnector.VC_FRWD_PKT_SETUP;
        ForwardSetup.DataSize = 2;		//Always 2 for forward setup

	//First enable the acks packet forwarding
        ForwardSetup.PktData = new byte[10];
        ForwardSetup.PktData[3] = SocketConnector.VC_FRWD_PKT_SETUP >> 24;
        ForwardSetup.PktData[2] = SocketConnector.VC_FRWD_PKT_SETUP >> 16;
        ForwardSetup.PktData[1] = SocketConnector.VC_FRWD_PKT_SETUP >> 8;
        ForwardSetup.PktData[0] = (byte)ForwardSetup.VCPacketType;
        ForwardSetup.PktData[7] = (byte)(ForwardSetup.DataSize >> 24);
        ForwardSetup.PktData[6] = (byte)(ForwardSetup.DataSize >> 16);
        ForwardSetup.PktData[5] = (byte)(ForwardSetup.DataSize >> 8);
        ForwardSetup.PktData[4] = (byte)ForwardSetup.DataSize;
        
 	ForwardSetup.PktData[8] = (byte)249;	// telemetry packet type
 	ForwardSetup.PktData[9] = 1;		// on
	m_VCConnector.SendData(ForwardSetup);	//Send it to the VC

 	ForwardSetup.PktData[8] = (byte)248;	// telemetry packet type
 	ForwardSetup.PktData[9] = 1;		// on
	m_VCConnector.SendData(ForwardSetup);	//Send it to the VC
    }

    void sendPassthrough26and27(int uavid){
        //Make sure we are connected
        if(m_VCConnector == null) return;
	//        Machinetta.Debugger.debug(1,"Sending request for all packets");

	//Send a forward setup packet
	sVCPacket ForwardSetup = new sVCPacket();		//Interface packet
	ForwardSetup.VCPacketType = SocketConnector.VC_PASSTHROUGH;
        ForwardSetup.DataSize = 3;		// 3 for passthrough with header only, dest low byte, dest high byte, 1 byte kestrel packet type

	//First enable the acks packet forwarding
        ForwardSetup.PktData = new byte[11];
        ForwardSetup.PktData[3] = SocketConnector.VC_PASSTHROUGH >> 24;
        ForwardSetup.PktData[2] = SocketConnector.VC_PASSTHROUGH >> 16;
        ForwardSetup.PktData[1] = SocketConnector.VC_PASSTHROUGH >> 8;
        ForwardSetup.PktData[0] = (byte)SocketConnector.VC_PASSTHROUGH;
        ForwardSetup.PktData[7] = (byte)(ForwardSetup.DataSize >> 24);
        ForwardSetup.PktData[6] = (byte)(ForwardSetup.DataSize >> 16);
        ForwardSetup.PktData[5] = (byte)(ForwardSetup.DataSize >> 8);
        ForwardSetup.PktData[4] = (byte)ForwardSetup.DataSize;
        
 	ForwardSetup.PktData[8] = (byte)uavid;	// dest low byte
 	ForwardSetup.PktData[9] = 0;		// dest high byte
 	ForwardSetup.PktData[10] = (byte)26;	// request standard telemetry
	m_VCConnector.SendData(ForwardSetup);	//Send it to the VC

 	ForwardSetup.PktData[8] = (byte)uavid;	// dest low byte
 	ForwardSetup.PktData[9] = 0;		// dest high byte
 	ForwardSetup.PktData[10] = (byte)27;	// request navigation telemetry
	m_VCConnector.SendData(ForwardSetup);	//Send it to the VC
    }

    void sendAcksStdTelemetry(){
	//Make sure we are connected
        if(m_VCConnector == null) return;
        Machinetta.Debugger.debug(1,"Sending request for Acks and Std telemetry packets");

	//Send a forward setup packet
	sVCPacket ForwardSetup = new sVCPacket();		//Interface packet
	ForwardSetup.VCPacketType = SocketConnector.VC_FRWD_PKT_SETUP;
        ForwardSetup.DataSize = 2;		//Always 2 for forward setup

	//First enable the acks packet forwarding
        ForwardSetup.PktData = new byte[10];
        ForwardSetup.PktData[3] = SocketConnector.VC_FRWD_PKT_SETUP >> 24;
        ForwardSetup.PktData[2] = SocketConnector.VC_FRWD_PKT_SETUP >> 16;
        ForwardSetup.PktData[1] = SocketConnector.VC_FRWD_PKT_SETUP >> 8;
        ForwardSetup.PktData[0] = (byte)ForwardSetup.VCPacketType;
        ForwardSetup.PktData[7] = (byte)(ForwardSetup.DataSize >> 24);
        ForwardSetup.PktData[6] = (byte)(ForwardSetup.DataSize >> 16);
        ForwardSetup.PktData[5] = (byte)(ForwardSetup.DataSize >> 8);
        ForwardSetup.PktData[4] = (byte)ForwardSetup.DataSize;
       
        try{
            ForwardSetup.PktData[8] = 1;	//Ack Packet Type
            //NetByte.encodeChar((char)1, ForwardSetup.PktData, 8);
            ForwardSetup.PktData[9] = 1;	//Turn it on
            //NetByte.encodeChar((char)1, ForwardSetup.PktData, 9);
        }
        catch(Exception e){
	    Machinetta.Debugger.debug(3,"Exception e="+e);
	    e.printStackTrace();
	}
	m_VCConnector.SendData(ForwardSetup);	//Send it to the VC

	//Now set std telem packets
	try{
            ForwardSetup.PktData[9] = 1;	//Std Telem Packet Type
            //NetByte.encodeChar((char)249, ForwardSetup.PktData, 8);
           // ForwardSetup.PktData[8] = (byte)249;;	//Turn it off
              ForwardSetup.PktData[8] = (byte)249;
            //NetByte.encodeChar((char)1, ForwardSetup.PktData, 9);
        }
        catch(Exception e){
	    Machinetta.Debugger.debug(1,"Exception e="+e);
	    e.printStackTrace();
	}
	m_VCConnector.SendData(ForwardSetup);	//Send it to the VC

	//Shut off the all packets flag
	try{
            ForwardSetup.PktData[8] = 0;	//The all packets flag
            //NetByte.encodeChar((char)0, ForwardSetup.PktData, 8);
            ForwardSetup.PktData[9] = 0;	//Turn it off
            //NetByte.encodeChar((char)0, ForwardSetup.PktData, 9);
        }
        catch(Exception e){
	    Machinetta.Debugger.debug(1,"Exception e="+e);
	    e.printStackTrace();
	}
	m_VCConnector.SendData(ForwardSetup);	//Send it to the VC
    }
    public void uploadCmdsToAutopilot(char UAVAddress) throws Exception{
        
        if(m_VCConnector == null) return;
        Machinetta.Debugger.debug(1,"Upload Commands to autopilot called");
        
        sCommandPacket command = new sCommandPacket();
        sVCPacket SetListPacket = new sVCPacket();
        SetListPacket.VCPacketType = SocketConnector.VC_UPLOAD_CMDS;
        SetListPacket.DataSize = 2;//
        
        SetListPacket.PktData = new byte[10];
        SetListPacket.PktData[3] = SocketConnector.VC_UPLOAD_CMDS >> 24;
        SetListPacket.PktData[2] = SocketConnector.VC_UPLOAD_CMDS >> 16;
        SetListPacket.PktData[1] = SocketConnector.VC_UPLOAD_CMDS >> 8;
        SetListPacket.PktData[0] = (byte)SetListPacket.VCPacketType;
        SetListPacket.PktData[7] = (byte)(SetListPacket.DataSize >> 24);
        SetListPacket.PktData[6] = (byte)(SetListPacket.DataSize >> 16);
        SetListPacket.PktData[5] = (byte)(SetListPacket.DataSize >> 8);
        SetListPacket.PktData[4] = (byte)SetListPacket.DataSize;
        
        ///////////////////////////////////////////
        // Command 1
        ///////////////////////////////////////////
        command.DestAddr = UAVAddress;
        //bytes reversed to be in network byte order
        SetListPacket.PktData[8] = (byte)command.DestAddr;
        SetListPacket.PktData[9] = (byte)(command.DestAddr >> 8);
        //Finally send the packets
        Machinetta.Debugger.debug(1,"Return from SocketConnector.SendData="+m_VCConnector.SendData(SetListPacket));
    }
    //This method sends a list of commands to virtual cockpit. Note virtual
    //cockpit will not actually upload the commands to the autopilot unless
    //and "upload commands" packet is sent to virtual cockpit
    // public void SendCmdsToVc() throws Exception{
//         //Make sure we are connected
//         System.out.println("Send Commands to VC called");
//         if(m_VCConnector == null) return;
        
//         sCommandPacket[] CommandPackets = new sCommandPacket[2];
//         CommandPackets[0] = new sCommandPacket();
//         CommandPackets[1] = new sCommandPacket();
//         sVCPacket SetListPacket = new sVCPacket();
//         SetListPacket.VCPacketType = SocketConnector.VC_SET_CMD_LIST;
//         SetListPacket.DataSize = 59;//118
        
//         SetListPacket.PktData = new byte[67];
//         SetListPacket.PktData[3] = SocketConnector.VC_SET_CMD_LIST >> 24;
//         SetListPacket.PktData[2] = SocketConnector.VC_SET_CMD_LIST >> 16;
//         SetListPacket.PktData[1] = SocketConnector.VC_SET_CMD_LIST >> 8;
//         SetListPacket.PktData[0] = (byte)SetListPacket.VCPacketType;
//         SetListPacket.PktData[7] = (byte)(SetListPacket.DataSize >> 24);
//         SetListPacket.PktData[6] = (byte)(SetListPacket.DataSize >> 16);
//         SetListPacket.PktData[5] = (byte)(SetListPacket.DataSize >> 8);
//         SetListPacket.PktData[4] = (byte)SetListPacket.DataSize;
        
//         ///////////////////////////////////////////
//         // Command 1
//         ///////////////////////////////////////////
//         CommandPackets[0].DestAddr = m_UAVAddress;
//         //bytes reversed to be in network byte order
//         SetListPacket.PktData[8] = (byte)CommandPackets[0].DestAddr;
//         SetListPacket.PktData[9] = (byte)(CommandPackets[0].DestAddr >> 8);
        
//         CommandPackets[0].CommandType = TYPE_WAYPOINT;
//         SetListPacket.PktData[10] = (byte)(CommandPackets[0].CommandType);
        
//         CommandPackets[0].Latitude = 40.234f;
//         NetByte.encodeFloat(CommandPackets[0].Latitude, SetListPacket.PktData, 11);
//         CommandPackets[0].Longitude = -111.658f;
//         //CommandPackets[0].Longitude = 96.658f;
//         //CommandPackets[0].Longitude = 10f;
//         NetByte.encodeFloat(CommandPackets[0].Longitude, SetListPacket.PktData, 15);
//         CommandPackets[0].Speed = 14f;
//         NetByte.encodeFloat(CommandPackets[0].Speed, SetListPacket.PktData, 19);
//         CommandPackets[0].Altitude = 100f;
//         NetByte.encodeFloat(CommandPackets[0].Altitude, SetListPacket.PktData, 23);
//         CommandPackets[0].GotoIndex = 0; //Only used in goto type should be 2 bytes
//         NetByte.encodeChar(CommandPackets[0].GotoIndex, SetListPacket.PktData, 27);
//         SetListPacket.PktData[27] = (byte)204f;
//         SetListPacket.PktData[28] = (byte)204f;
//         CommandPackets[0].Time = 0; //should be 2 bytes
//         NetByte.encodeChar(CommandPackets[0].Time, SetListPacket.PktData, 29);
//         CommandPackets[0].Radius = 50f;
//         NetByte.encodeFloat(CommandPackets[0].Radius, SetListPacket.PktData, 31);
//         CommandPackets[0].FlareSpeed = 0f;		//Approach Land Only - Flare Speed
//         NetByte.encodeFloat(CommandPackets[0].FlareSpeed, SetListPacket.PktData, 35);
//         CommandPackets[0].FlareAltitude = 0f;	//Approach Land Only - Flaring Altitude
//         NetByte.encodeFloat(CommandPackets[0].FlareAltitude, SetListPacket.PktData, 39);
//         CommandPackets[0].BreakAltitude = 0f;	//Approach Land Only - Altitude used when breaking out of approach
//         NetByte.encodeFloat(CommandPackets[0].BreakAltitude, SetListPacket.PktData, 43);
//         CommandPackets[0].DescentRate = 0f;		//Approach Land Only - Descent rate in spiral
//         NetByte.encodeFloat(CommandPackets[0].DescentRate, SetListPacket.PktData, 47);
//         CommandPackets[0].ApproachLat = 0f;		//Approach Land Only - Approach circle latitude
//         NetByte.encodeFloat(CommandPackets[0].ApproachLat, SetListPacket.PktData, 51);
//         CommandPackets[0].ApproachLong = 0f;		//Approach Land Only - Approach circle longitude
//         NetByte.encodeFloat(CommandPackets[0].ApproachLong, SetListPacket.PktData, 55);
//         CommandPackets[0].FutureUseInt = 0;		//Potential Future Use Int (i.e. Payload Command)
//         NetByte.encodeInt(CommandPackets[0].FutureUseInt, SetListPacket.PktData, 59);
//         CommandPackets[0].FutureUseFloat = 0f;	//Potential Future Use Int (i.e. Payload Command)
//         NetByte.encodeFloat(CommandPackets[0].FutureUseFloat, SetListPacket.PktData, 63);
        
//         for(int i = 35; i<SetListPacket.PktData.length; i++){
//             SetListPacket.PktData[i] = (byte)204;
//         }
        
//         for(int i = 0; i<SetListPacket.PktData.length; i++){
//             System.out.println((SetListPacket.PktData[i]&0xFF)+" "+(byte)(SetListPacket.PktData[i]&0xFF)+" "+SetListPacket.PktData[i]);
//             SetListPacket.PktData[i] = (byte)(SetListPacket.PktData[i]&0xFF);
//         }
        
//         //Finally send the packets
//         System.out.println(m_VCConnector.SendData(SetListPacket));
//     }
    
//     //This method sends a list of commands to virtual cockpit. Note virtual
//     //cockpit will not actually upload the commands to the autopilot unless
//     //and "upload commands" packet is sent to virtual cockpit
//     public void SendCmdsToVc(sCommandPacket command){
        
//     }
    public void SendCmdsToVc(sCommandPacket[] commands) throws Exception{
        //Make sure we are connected
        Machinetta.Debugger.debug(1,"Send Commands to VC called");
        if(m_VCConnector == null) return;
        
        int numWaypoints = commands.length;
        sVCPacket SetListPacket = new sVCPacket();
        SetListPacket.VCPacketType = SocketConnector.VC_SET_CMD_LIST;
        SetListPacket.DataSize = numWaypoints*59;//118
        
        SetListPacket.PktData = new byte[SetListPacket.DataSize+8];
        
        SetListPacket.PktData[3] = SocketConnector.VC_SET_CMD_LIST >> 24;
        SetListPacket.PktData[2] = SocketConnector.VC_SET_CMD_LIST >> 16;
        SetListPacket.PktData[1] = SocketConnector.VC_SET_CMD_LIST >> 8;
        SetListPacket.PktData[0] = (byte)SetListPacket.VCPacketType;
        SetListPacket.PktData[7] = (byte)(SetListPacket.DataSize >> 24);
        SetListPacket.PktData[6] = (byte)(SetListPacket.DataSize >> 16);
        SetListPacket.PktData[5] = (byte)(SetListPacket.DataSize >> 8);
        SetListPacket.PktData[4] = (byte)SetListPacket.DataSize;
        int currentOffset = 8;
	sCommandPacket command = null;
        
        for(int waypointi=0; waypointi < numWaypoints; waypointi++ ){
            command = commands[waypointi];
            SetListPacket.PktData[currentOffset++] = (byte)command.DestAddr;
            SetListPacket.PktData[currentOffset++] = (byte)(command.DestAddr >> 8);
            SetListPacket.PktData[currentOffset++] = (byte)(command.CommandType);
        
            NetByte.encodeFloat(command.Latitude, SetListPacket.PktData, currentOffset);
            currentOffset += 4;
            NetByte.encodeFloat(command.Longitude, SetListPacket.PktData, currentOffset);
            currentOffset += 4;
            NetByte.encodeFloat(command.Speed, SetListPacket.PktData, currentOffset);
            currentOffset += 4;
            NetByte.encodeFloat(command.Altitude, SetListPacket.PktData, currentOffset);
            currentOffset += 4;
            NetByte.encodeChar(command.GotoIndex, SetListPacket.PktData, currentOffset);
            SetListPacket.PktData[currentOffset++] = (byte)204f;
            SetListPacket.PktData[currentOffset++] = (byte)204f;
            NetByte.encodeChar(command.Time, SetListPacket.PktData, currentOffset);
            currentOffset += 2;
            NetByte.encodeFloat(command.Radius, SetListPacket.PktData, currentOffset);
            currentOffset += 4;
            NetByte.encodeFloat(command.FlareSpeed, SetListPacket.PktData, currentOffset);
            currentOffset += 4;
            NetByte.encodeFloat(command.FlareAltitude, SetListPacket.PktData, currentOffset);
            currentOffset += 4;
            NetByte.encodeFloat(command.BreakAltitude, SetListPacket.PktData, currentOffset);
            currentOffset += 4;
            NetByte.encodeFloat(command.DescentRate, SetListPacket.PktData, currentOffset);
            currentOffset += 4;
            NetByte.encodeFloat(command.ApproachLat, SetListPacket.PktData, currentOffset);
            currentOffset += 4;
            NetByte.encodeFloat(command.ApproachLong, SetListPacket.PktData, currentOffset);
            currentOffset += 4;
            NetByte.encodeInt(command.FutureUseInt, SetListPacket.PktData, currentOffset);
            currentOffset += 4;
            NetByte.encodeFloat(command.FutureUseFloat, SetListPacket.PktData, currentOffset);
        }
        for(int i = currentOffset - 28; i<SetListPacket.PktData.length; i++){
            SetListPacket.PktData[i] = (byte)204;
        }
        //Finally send the packet

        Machinetta.Debugger.debug(1,"Return from SocketConnector.SendData="+m_VCConnector.SendData(SetListPacket));
        uploadCmdsToAutopilot(command.DestAddr);
    }
}
