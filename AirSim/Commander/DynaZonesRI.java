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
 * DeconflictionRI.java
 *
 * Created on Mon Jul 23 18:27:43 EDT 2007
 *
 */

package AirSim.Commander;

import AirSim.Environment.Assets.Asset;
import AirSim.Environment.Assets.Tasks.TaskType;
import AirSim.Environment.Area;
import AirSim.Machinetta.BasicRole;
import AirSim.Machinetta.Beliefs.ImageData;
import AirSim.Machinetta.Beliefs.Location;
import AirSim.Machinetta.Beliefs.UAVLocation;
import AirSim.Machinetta.Beliefs.UGSSensorReading;
import AirSim.Machinetta.Beliefs.VehicleBelief;
import AirSim.Machinetta.PathDB;
import AirSim.Machinetta.UAVRI;
import Machinetta.Debugger;
import Machinetta.Coordination.MAC.PlanAgent;
import Machinetta.RAPInterface.InputMessages.InputMessage;
import Machinetta.RAPInterface.OutputMessages.OutputMessage;
import Machinetta.RAPInterface.RAPInterfaceImplementation;
import Machinetta.State.BeliefID;
import Machinetta.State.BeliefNameID;
import Machinetta.State.BeliefType.Belief;
import Machinetta.State.BeliefType.TeamBelief.RoleBelief;
import Machinetta.State.BeliefType.TeamBelief.TeamPlanBelief;
import Machinetta.State.ProxyState;
import Machinetta.State.StateChangeListener;
import Machinetta.Configuration;
import Machinetta.ConfigReader;
import Machinetta.Coordination.MAC.BeliefShareRequirement;
import Machinetta.Coordination.MAC.InformationAgentFactory;
import Gui.BackgroundConfig;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Vector;
import javax.imageio.ImageIO;
import java.util.Random;


/**
 *
 * @author owens
 */
public class DynaZonesRI extends RAPInterfaceImplementation {
    // ----------------------------------------------------------------------
    // BEGIN PARAMETERS LOADED FROM CONFIG FILE
    // ----------------------------------------------------------------------

    public static boolean SIM_OPERATOR=false;

    // ----------------------------------------------------------------------
    // END PARAMETERS LOADED FROM CONFIG FILE
    // ----------------------------------------------------------------------

    private WorldState worldState = null;
    private DynaZonesGUI gui = null;
    
    private Random rand = new Random();
    private HashMap<Point,Object> strikeLocsOrdered = new HashMap<Point,Object>();
    private HashMap<Point,Object> patrolLocsOrdered = new HashMap<Point,Object>();
    
    private ProxyState state = new ProxyState();
    {
        state.addChangeListener(new StateChangeListener() {
            public void stateChanged(BeliefID[] b) {
                for (BeliefID bel: b) {
		    //                    Machinetta.Debugger.debug("Sim Operator new belief: " + bel, 1, this);
                    Belief belief = state.getBelief(bel);
                    try {
			
			// @TODO: Do something intelligent with state
			// change listening here - I'm not sure we're
			// using this approach anymore.  WorldState
			// should be added as a stateChanged listener
			// and then most new stuff will happen via
			// that.

                    } catch (Exception e) {
                        Debugger.debug("stateChange:Exception processing changed belief='"+belief+"', e="+e,5, this);
                        e.printStackTrace();
                    }
                    
                }
            }
        });
    }

    
    /** Creates a new instance of DynaZonesRI */
    public DynaZonesRI() {
        GUIConfig.getConfigFields();
        if (GUIConfig.SHOW_GUI) {
            gui = new DynaZonesGUI(GUIConfig.CTDB_BASE_NAME, GUIConfig.ROAD_FILE_NAME, GUIConfig.REPAINT_INTERVAL_MS);
        }
        
        
        // Of course, this shouldn't really be here, but will have to do for now.
        try {
            InformationAgentFactory.addBeliefShareRequirement(
                    new BeliefShareRequirement(Class.forName("AirSim.Machinetta.Beliefs.NoFlyZone"), 10));            
        } catch (ClassNotFoundException e) {
            Machinetta.Debugger.debug("Could not find class : " + "AirSim.Machinetta.Beliefs.NoFlyZone", 3, this);
        }
        
        // For testing .... 
        
	if(UAVRI.PATH_DECONFLICTION_ON) {
	    PathDB pathDB = new PathDB(UAVRI.PATHDB_PATH_CONFLICT_MIN_DIST_METERS);
	    pathDB.start();        
	}
    }
    
    /**
     * Sends a message to the RAP
     *
     *
     * @param msg The message to send
     */
    // @TODO: This is empty - and in SimOperatorRI it didn't have an
    // actual send call, jsut gui stuff.  Should it be deleted from
    // here?  From SimOperatorRI?
    public void sendMessage(OutputMessage msg) {
    }
    
    /**
     * Needed to implement Thread
     */
    public void run() {
        
        while (state.getSelf() == null) {
            try {
                sleep(100);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
        
	// @TODO: Originally slept for a bit, sent a hold plan, then a
	// move plan
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
    
}
