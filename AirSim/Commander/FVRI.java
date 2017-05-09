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
 * FVRI.java
 *
 * Created on March 1, 2008, 12:41 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package AirSim.Commander;

import AirSim.Machinetta.BasicRole;
import AirSim.Environment.Assets.Tasks.TaskType;
import AirSim.Environment.Vector3D;
import AirSim.Machinetta.Beliefs.AssetStateBelief;
import Gui.LatLonUtil;
import Machinetta.State.BeliefType.ProxyID;
import Machinetta.State.BeliefType.TeamBelief.RoleBelief;
import Machinetta.ConfigReader;
import Machinetta.RAPInterface.InputMessages.InputMessage;
import Machinetta.RAPInterface.OutputMessages.OutputMessage;
import Machinetta.RAPInterface.RAPInterfaceImplementation;
import Machinetta.State.BeliefID;
import Machinetta.State.BeliefType.Belief;
import Machinetta.State.ProxyState;
import Machinetta.State.StateChangeListener;
import Machinetta.Coordination.MAC.BeliefShareRequirement;
import Machinetta.Coordination.MAC.InformationAgentFactory;
import Machinetta.State.BeliefType.TeamBelief.TeamPlanTemplate;
import Machinetta.Coordination.MAC.PlanAgentFactory;
import Machinetta.State.BeliefType.CapabilityBelief;
import Machinetta.State.BeliefType.RAPBelief;

import java.util.*;

import org.apache.log4j.Logger;

/**
 * This class is the RapInterfaceImplementation for the FalconView GUI
 * socket server.
 *
 * @author owens
 */
public class FVRI extends RAPInterfaceImplementation implements StateChangeListener {

        private static final Logger logger = Logger.getLogger(FVRI.class);
        
    // ----------------------------------------------------------------------
    // BEGIN PARAMETERS LOADED FROM CONFIG FILE
    // ----------------------------------------------------------------------
    public static int MAP_WIDTH_METERS = 50000;
    public static int MAP_HEIGHT_METERS = 50000;
    public static double MAP_LOWER_LEFT_LAT = -1000;
    public static double MAP_LOWER_LEFT_LON = -1000;

    // ----------------------------------------------------------------------
    // END PARAMETERS LOADED FROM CONFIG FILE
    // ----------------------------------------------------------------------

    protected WorldStateMgr wsm = null;
   
    public ProxyState state = null;
   
    /** 
     * Creates a new instance of FVRI    
     */
    public FVRI() {
	readConfigs();
	state = new ProxyState();
	wsm = new WorldStateMgr();        
	state.addChangeListener(this);
    }  
    
    public static void readConfigs() {
	MAP_WIDTH_METERS = ConfigReader.getConfig("MAP_WIDTH_METERS", MAP_WIDTH_METERS, true);
	MAP_HEIGHT_METERS = ConfigReader.getConfig("MAP_HEIGHT_METERS", MAP_HEIGHT_METERS, true);
	MAP_LOWER_LEFT_LAT = ConfigReader.getConfig("MAP_LOWER_LEFT_LAT", MAP_LOWER_LEFT_LAT, false);
	MAP_LOWER_LEFT_LON = ConfigReader.getConfig("MAP_LOWER_LEFT_LON", MAP_LOWER_LEFT_LON, false);
    }

    public void stateChanged(BeliefID[] b) {
	for (BeliefID bel: b) {
	    Belief belief = state.getBelief(bel);
	    wsm.addBelief(belief);
	    if(belief instanceof AssetStateBelief) 
		getLatitudeLongitude(((AssetStateBelief)belief).xMeters,((AssetStateBelief)belief).yMeters);
	    else if(belief instanceof TeamPlanTemplate) 
		printTemplate((TeamPlanTemplate) belief);
// 	    else
// 		Machinetta.Debugger.debug("new belief: " + bel, 1, this);
            
	}
    }

    /**
     * Sends a message to the RAP
     *
     *
     * @param msg The message to send
     */
    public void sendMessage(OutputMessage msg) {
        Machinetta.Debugger.debug("sent: " + msg, 1, this);
    }
  
    /**
     * Called to get list of new messages
     * Should return only those messages received since last called.
     *
     * @return List of InputMessage objects received from RAP since last called.
     */
    public InputMessage[] getMessages() {
        // Not used.
        return null;
    }
    
    /**
     * Needed to implement Thread
     */
    public void run() {
        
	// Wait until we've loaded our self belief from beliefs file
        while (state.getSelf() == null) {
	    Machinetta.Debugger.debug(1,"No self in belief yet, waiting...");

            try {
                sleep(100);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
	Machinetta.Debugger.debug(1,"Got self in belief!");
        
	// Pause for some reason
        try {
            sleep(10000);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        
	// Start doing whatever it is we do...
	printTemplates();
    }
    
    public class LatitudeLongitude
    {
            public double latitude;
            public double longitude;
    }

    public class LocalCoords
    {
            public double x;
            public double y;
    }

    public LatitudeLongitude getLatitudeLongitude(double xMeters, double yMeters)
    {
	double localKmX = xMeters / 1000.0;
	double localKmY = yMeters / 1000.0;
	LatitudeLongitude latlon = new LatitudeLongitude();
	latlon.latitude = MAP_LOWER_LEFT_LAT + (float) LatLonUtil.kmToDegreesLat(MAP_LOWER_LEFT_LAT, localKmY);
	latlon.longitude = MAP_LOWER_LEFT_LON + (float) LatLonUtil.kmToDegreesLon(MAP_LOWER_LEFT_LAT, localKmX);
	return latlon;
    }
 
 
    public LocalCoords getLocalCoords(double latitude, double longitude)
    {
	LocalCoords local = new LocalCoords();
	local.x = LatLonUtil.lonToLocalXMeters(MAP_LOWER_LEFT_LAT, MAP_LOWER_LEFT_LON, longitude);
	local.y = LatLonUtil.latToLocalYMeters(MAP_LOWER_LEFT_LAT, MAP_LOWER_LEFT_LON, latitude);
	return local;
    } 

    public List<TeamPlanTemplate> getTeamPlanTemplates() {
        BeliefID ids[] = state.getAllBeliefs();
        if(null == ids) {
            Machinetta.Debugger.debug(1, "no beliefs in state");
            logger.warn("no beliefs in state");
            return null;
        }

        List<TeamPlanTemplate> list = new LinkedList<TeamPlanTemplate>();
        for(int loopi = 0; loopi < ids.length; loopi++) {
            Belief bel = state.getBelief(ids[loopi]);
            if(bel instanceof TeamPlanTemplate) {
                list.add((TeamPlanTemplate) bel);
            }
        }
        return list;
    }
    
//     protected void exampleGetLatLon(ProxyID pid)
//     {
//             getLatitudeLongitude(pid);
//     }
    
    protected void printTemplates()
    {
            List<TeamPlanTemplate> templates = getTeamPlanTemplates();
            if (templates == null)
                    return;
            for (TeamPlanTemplate template: templates)
                    printTemplate(template);
    }

    public void printTemplate(TeamPlanTemplate templ) {
	Machinetta.Debugger.debug(1, "TEMPLATE id="+templ.getID().toString());
	Machinetta.Debugger.debug(1, "    NAME="+templ.getName());
	Machinetta.Debugger.debug(1, "    TEAM="+templ.getTeam());
	Machinetta.Debugger.debug(1, "    params="+templ.params.toString());
	Machinetta.Debugger.debug(1, "    preconditions="+templ.getPreconditions().toString());
	Machinetta.Debugger.debug(1, "    postconditions="+templ.getPostconditions().toString());
	Vector<RoleBelief> roles = templ.roles;
	Machinetta.Debugger.debug(1, "    Roles");
	for(int loopi = 0; loopi < roles.size(); loopi++)
	    Machinetta.Debugger.debug(1, "        Role["+loopi+"]="+roles.get(loopi).toString());
	Machinetta.Debugger.debug(1, "    roleConstraints="+templ.roleConstraints.toString());
	Machinetta.Debugger.debug(1, "    maximumReasonableCompletionTime="+templ.maximumReasonableCompletionTime);
    }
      
}
