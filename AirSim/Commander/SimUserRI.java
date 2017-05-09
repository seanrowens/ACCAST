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
 * SimUserRI.java
 *
 * Created on March 1, 2008, 12:41 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package AirSim.Commander;

import AirSim.Machinetta.PathDB;
import AirSim.Machinetta.UAVRI;
import AirSim.Machinetta.Beliefs.AssetStateBelief;
import AirSim.Machinetta.Beliefs.VehicleBelief;
import AirSim.Machinetta.CostMaps.BinaryBayesFilterCostMap;
import Machinetta.RAPInterface.InputMessages.InputMessage;
import Machinetta.RAPInterface.OutputMessages.OutputMessage;
import Machinetta.RAPInterface.RAPInterfaceImplementation;
import Machinetta.State.BeliefID;
import Machinetta.State.BeliefType.Belief;
import Machinetta.State.ProxyState;
import Machinetta.State.StateChangeListener;
import Machinetta.Coordination.MAC.BeliefShareRequirement;
import Machinetta.Coordination.MAC.InformationAgentFactory;
import Machinetta.State.ProxyState;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * This proxy class doesn't do much, really you probably want to look
 * at SimUserCoordinator.run() and SimUserUtils.manageSimUser(belief) - SRO
 * 
 * 
 * This class contains the main routine for SimUserRI module mimicking human intelligence.
 * Mainly this starts the coodinators and manager.
 * @NOTE: There are two kinds of plan in the proxy - one from other proxies, the other from SimUserRI itself.
 *        Plans from other proxies are dealt with SimUserCoordinator(SUC) to respond with relevant actions.
 *        According to the change of ProxyState, SUC generates the alternative or re-instantiated plans,
 *        and they are added to Proxy with "LocallySensedPlans(?)" flag to indicate these newly generated
 *        plans need to be distributed to other proxies having adequate roles.
 *
 * @author junyounk
 *
 *
 * To clarify the above - proxies may have TeamPlanTemplates (TPTs) -
 * each TPT has a set of preconditions - when new beliefs come in they
 * are matched against TPT preconditions and if a match is found, the
 * TPT is used to automatically generate a TeamPlan.  Quite often when
 * using Machinetta this is how the Team Plans are generated.
 *
 * However, in the SimUser we're using a-priori plans, i.e. we're
 * creating them ourselves as we wish them to be rather than using the
 * templating system.   - SRO
 *
 */
public class SimUserRI extends RAPInterfaceImplementation {

    private BinaryBayesFilterCostMap bbfCM = null;
    private SimOperatorGUI gui = null;    
    private Clustering clustering = null;    
    private SimUserCoordinator suc = null;
    private WorldStateMgr wsm = null;
    private SimUserGui sumUserGui = null;
    
    public ProxyState state = new ProxyState();
   
    /** 
     * Creates a new instance of SimUserRI    
     */
    public SimUserRI() {
        SimConfiguration.getConfigFields();
	SimUserSubTeams sust = new SimUserSubTeams();
	sust = null;
	SimUserSubTeams.createAirsupportSubTeam(SimConfiguration.NUM_AUAVS, SimConfiguration.NUM_EOIRUAVs);
	
	// @TODO:  prob don't need
        if(SimConfiguration.BINARY_BAYES_FILTER_ON) {
            BinaryBayesFilterGUI bbfGui = new BinaryBayesFilterGUI();
            bbfCM = bbfGui.generateBBF();
	    bbfCM.start();
        }

	// @TODO:  prob don't need
        if (SimConfiguration.SHOW_GUI) {     
            gui = new SimOperatorGUI(SimConfiguration.CTDB_BASE_NAME, SimConfiguration.ROAD_FILE_NAME, SimConfiguration.REPAINT_INTERVAL_MS);
        }

        // SimUserTester
        if (SimConfiguration.GUI_ON) {     
            sumUserGui = new SimUserGui();
            sumUserGui.setVisible(true);
        }
        
	// @TODO: prob/maybe don't need - are we usign the clustering
	// mechanism?  Only if we're using the binary bayes filter
	// which I _think_ we're not...
        if(SimConfiguration.CLUSTERING_ON) {
	    Machinetta.Debugger.debug("ClusteringOn parameter set to true, starting clustering thread.", 1, this);
	    clustering = new Clustering(bbfCM, gui,(int)(SimConfiguration.MAP_WIDTH_METERS/SimConfiguration.BBF_GRID_SCALE_FACTOR));
	    clustering.start();
	} else {
	    Machinetta.Debugger.debug("ClusteringOn parameter set to false, no clustering performed.", 1, this);
	}

	// @TODO:  prob don't need
        // Of course, this shouldn't really be here, but will have to do for now.
        try {
            InformationAgentFactory.addBeliefShareRequirement(
                    new BeliefShareRequirement(Class.forName("AirSim.Machinetta.Beliefs.NoFlyZone"), 10));            
        } catch (ClassNotFoundException e) {
            Machinetta.Debugger.debug("Could not find class : " + "AirSim.Machinetta.Beliefs.NoFlyZone", 3, this);
        }
        
        // For testing .... 

	// @TODO: prob don't need - or do we?  I think we need this
	// simply because if we don't then we may get deconfliction
	// messages, and rather than re-sharing them they'll just
	// disappear.  Unless we're not using deconfliction.
	if(UAVRI.PATH_DECONFLICTION_ON) {
	    PathDB pathDB = new PathDB(UAVRI.PATHDB_PATH_CONFLICT_MIN_DIST_METERS);
	    pathDB.start();        
	}
        
	// TESTING
	boolean testingWsm = true;
	if(testingWsm) {
	    SimUserGuiTest sugt = null;
	    if (SimConfiguration.GUI_ON)   
		sugt = new SimUserGuiTest();
	    if(wsm == null) {
		wsm = new WorldStateMgr(sugt);
	    }
	}
	else {
	    if(wsm == null) {
		wsm = new WorldStateMgr();
	    }
        }

        if(suc == null) {
            suc = new SimUserCoordinator(wsm,wsm.sust);
        }                          
        
        //ConfigurationOperator thread start.
        ConfigurationOperator co = new ConfigurationOperator();
        co.start();
        
    }  
    
    /**
     * Sends a message to the RAP
     *
     *
     * @param msg The message to send
     */
    public void sendMessage(OutputMessage msg) {
        Machinetta.Debugger.debug(1, "message to RAP = " + msg);
    }
    
    /**
     * Needed to implement Thread
     */
    public void run() {
        
	// Get my self - wait if I have to.
        while (state.getSelf() == null) {
            try {
                sleep(100);
            } catch (InterruptedException ex) {
		//                ex.printStackTrace();
            }
        }
        
	// Wait for 3 seconds for no apparent reason...
        try {
            sleep(8000);
        } catch (InterruptedException ex) {
	    //            ex.printStackTrace();
        }
        
        /*
        PlanAgent pah = createHoldPlan();
        
        try {
            sleep(WAIT_BEFORE_MOVE_PLAN_MS);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        
        pah.terminate();
        */
        
	//        Machinetta.Debugger.debug("Not using hold plan (code commented out)", 3, this);
	
	// Wait for 2 more seconds for no apparent reason...        
        try {
            sleep(2000);
        } catch (InterruptedException ex) {
	    //            ex.printStackTrace();
        }
        
        //Machinetta.Debugger.debug(1, "      !!!!!!!!!!!!!!!! SUU generated!");
	//PlanAgent pam = suu.createMovePlan();
        
        //Generate team plans. Currently generate all plans at one time.
        suc.start();

	state.addChangeListener(new StateChangeListener() {
		public void stateChanged(BeliefID[] b) {
		    for (BeliefID bel: b) {
			//                    Machinetta.Debugger.debug("Sim Operator new belief: " + bel, 1, this);
                    
			Belief belief = state.getBelief(bel);
// 			if(null != belief) {
// 			    Machinetta.Debugger.debug(1, "stateChanged: belief of class "+belief.getClass().getName());
// 			}
// 			else {
// 			    Machinetta.Debugger.debug(1, "stateChanged: null belief!");
// 			}
                          
			wsm.addBelief(belief);
                    
			//Manage any new beliefs in SimUserCoordinator
			suc.manageSimUser(belief);
                    
			if(belief instanceof AssetStateBelief)
			    state.removeBelief(bel);
			else if(belief instanceof VehicleBelief)
			    state.removeBelief(bel);
		    }
		}
	    });
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
