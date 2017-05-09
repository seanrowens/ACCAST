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
import AirSim.Environment.Waypoint;
import Gui.LatLonUtil;
import java.awt.geom.Point2D;
/**
 *
 * @author rglinton
 */


public class VCAutopilot {
    
    // @TODO: update ORIGIN_LAT and ORIGIN_LON whenever we go out to monroeville
    // also need to update the address of the uav that is sent in the command
    // check to make sure nothing in machinetta actually tries to command the zcoord (altitude)
    // 
    // I don't know what the below mean... I think Robin wrote those
    //  
    // update the static IP address of where VC is
    // is upload waypoints being called?????
    // UDP switch setting in Configuration.java in Machinetta-src/Machinetta

    public final static float DEFAULT_WAYPOINT_ALT = 80f;
    public final static float DEFAULT_WAYPOINT_SPEED = 14f;
    public final static float DEFAULT_WAYPOINT_RADIUS = 50f;

    public final static float ORIGIN_LAT = 40.4563444444f;
    public final static float ORIGIN_LON = -79.789486111f;
    
    //private double oldX = 0.0;
    //private double oldY = 0.0;
    private NavData oldNavData = null;
    private boolean firstUpdate = true;

    private VCBridge vcB = null;
    
    /** Creates a new instance of VCAutopilot */
    public VCAutopilot(String virtualCockpitIPString) {
	vcB = new VCBridge(virtualCockpitIPString);
    }
    public NavData waitForNavData() {
	return vcB.waitForNavData();
    }
    public NavData updateLocation(){
	NavData navData = null;
	Point2D.Double point = new Point2D.Double();
	navData = vcB.getNavData();
	if(navData != null){
	    if(navData.longitude != 0){
		Machinetta.Debugger.debug(1, "UAV believes it is at  "+navData.longitude+" "+navData.latitude);
		point.x = LatLonUtil.lonToLocalXMeters(ORIGIN_LAT, ORIGIN_LON, navData.longitude); 
		point.y = LatLonUtil.latToLocalYMeters(ORIGIN_LAT, ORIGIN_LON, navData.latitude);
		Machinetta.Debugger.debug(1, "Which translates to  "+point.x+" "+point.y);
		oldNavData = navData;
	    }
	    else{
		Machinetta.Debugger.debug(1, "Navigation data was invalid");
		navData.procerusUAVAddress = NavData.INVALID_UAV_ADDRESS;
	    }
 	}
	else{
	    if(firstUpdate){
		navData = new NavData();
		navData.procerusUAVAddress = NavData.INVALID_UAV_ADDRESS;
		firstUpdate = false;
	    }
	    else{
		navData = oldNavData;
	    }	
	}
	return navData;
    }

    public void sendCurrent(Waypoint[] path, int uavAddress){
	
	sCommandPacket command = null;
	//int uavAddress = 1032; //ATTENTION: HAVE TO FIX THE UAV ADDRESS
	Waypoint currWaypoint = null;
	
	sCommandPacket[] commands = new sCommandPacket[path.length];
	
	for(int loopi = 0; loopi <path.length; loopi++){
	    currWaypoint = path[loopi];
	    
	    command = new sCommandPacket();
	    command.DestAddr = (char)uavAddress;
	    command.CommandType = VCCommands.TYPE_WAYPOINT;
	    Machinetta.Debugger.debug(1, "Was requested to send UAV waypoint  "+currWaypoint.x+" "+currWaypoint.y);
	    command.Latitude = ORIGIN_LAT+(float)LatLonUtil.kmToDegreesLat(ORIGIN_LAT, (double)currWaypoint.y/(double)1000); 
	    command.Longitude = ORIGIN_LON+(float)LatLonUtil.kmToDegreesLon(ORIGIN_LAT, (double)currWaypoint.x/(double)1000); 
	    Machinetta.Debugger.debug(1, "Which translates to  "+command.Longitude+" "+command.Latitude);
	    command.Speed = DEFAULT_WAYPOINT_SPEED;
	    command.Altitude = DEFAULT_WAYPOINT_ALT;
	    command.Radius = DEFAULT_WAYPOINT_RADIUS;
	    
	    //These are not currently used and I doubt they ever will be.
	    command.GotoIndex = 0;
	    command.Time = 0;
	    command.FlareSpeed = 0f;
	    command.FlareAltitude = 0f;
	    command.BreakAltitude = 0f;
	    command.DescentRate = 0f;
	    command.ApproachLat = 0f;
	    command.ApproachLong = 0f;
	    command.FutureUseInt = 0;
	    command.FutureUseFloat = 0f;
	    //These are not currently used and I doubt they ever will be.
	    commands[loopi] = command;
	}
	vcB.addCommand(commands);
    }
        
}


