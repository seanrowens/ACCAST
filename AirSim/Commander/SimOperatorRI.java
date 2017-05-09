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
 * SimOperatorRI.java
 *
 * Created on March 20, 2006, 9:34 AM
 *
 */

package AirSim.Commander;

import AirSim.Environment.Assets.Asset;
import AirSim.Environment.Assets.Tasks.TaskType;
import AirSim.Environment.Area;
import AirSim.Machinetta.BasicRole;
import AirSim.Machinetta.MiniWorldState;
import AirSim.Machinetta.PathDB;
import AirSim.Machinetta.UAVRI;
import AirSim.Machinetta.Beliefs.ImageData;
import AirSim.Machinetta.Beliefs.AssetStateBelief;
import AirSim.Machinetta.Beliefs.UAVLocation;
import AirSim.Machinetta.Beliefs.UGSSensorReading;
import AirSim.Machinetta.Beliefs.VehicleBelief;
import AirSim.Machinetta.CostMaps.BinaryBayesFilterCostMap;
import AirSim.Machinetta.CostMaps.BBFTabPanel;
import AirSim.Configs.TPTFactory;
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
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.event.*;
import javax.swing.*;


/**
 * RapInterfaceImplementation for the old SimOperator
 * @author pscerri
 */
public class SimOperatorRI extends RAPInterfaceImplementation {

    private static final Boolean TEST_META_TEAM_PLAN_TEMPLATES = true;
    // ----------------------------------------------------------------------
    // BEGIN PARAMETERS LOADED FROM CONFIG FILE
    // ----------------------------------------------------------------------

    private String CTDB_BASE_NAME;
    private String ROAD_FILE_NAME;
    private int REPAINT_INTERVAL_MS = 1000/1;
    private boolean SHOW_GUI = true;
    
    // @todo: SRO Thu Apr 27 22:14:25 EDT 2006 - I foolishly put this
    // GUI into SimOperator - I really should have put it somewhere
    // like Commander.  Anyway, for now, added this global to turn
    // on/off the sim operator.  This value can be overridden by
    // SIM_OPERATOR field in config file/command line.
    public static boolean SIM_OPERATOR=false;

    // Be aware that at low values (5, 10, 20, etc) the Thread.sleep()
    // method is unreliable.  Also be aware that this reaction delay
    // is in real, wall clock milliseconds.  If the simulation is
    // running much faster than real time then this will be
    // significantly faster.  (i.e. if a simulator step represents
    // 1/10 of a simulated second, and if the simulator is running at
    // 5 ms per step, hence simulating 20 times reality, then the
    // reaction delay will be similarly exaggerated - a delay of 100ms
    // will equate to approximately 2000ms (the simulator sleep()
    // sufferes from the same reliability issues) delay in the
    // simulator.
    private long REACTION_DELAY_MS = 0;

    private long WAIT_BEFORE_MOVE_PLAN_MS = 20000;
    private double DROPPED_PLAN_PROB = 0.0;

    public static int MAP_WIDTH_METERS = 50000;
    public static int MAP_HEIGHT_METERS = 50000;

    // the scaling factor to go from map coords to indices into the
    // bayes filter probability array - and also to figure out what
    // size array to use, based on map size.
    public static double BBF_GRID_SCALE_FACTOR = 100.0;

    public static boolean BINARY_BAYES_FILTER_ON = false;

    public static boolean BAYES_FILTER_PANEL_ON=false;
    public static int BAYES_FILTER_PANEL_X=0;
    public static int BAYES_FILTER_PANEL_Y=20;

    public static boolean ENTROPY_PANEL_ON = false;
    public static int ENTROPY_PANEL_X = 0;
    public static int ENTROPY_PANEL_Y = 0;

    public static boolean CLUSTERING_ON=false;

    // ----------------------------------------------------------------------
    // END PARAMETERS LOADED FROM CONFIG FILE
    // ----------------------------------------------------------------------

    private boolean SHOW_OLD_GUI = false;

    private BinaryBayesFilterCostMap bbfCM = null;

    private SimOperatorGUIOld guiOld = null;
    private SimOperatorGUI gui = null;
    
    private Clustering clustering = null;

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
                        
                        // Update the purty picture
                        if (SHOW_GUI) {
                            
                            // OLD GUI
                            
			    if(SHOW_OLD_GUI) {
				StringBuffer sb = new StringBuffer(guiOld.messagesTF.getText());
				if (sb.length() > 250) {
				    sb.delete(200, sb.length());
				}
				sb.insert(0, "Belief:" + bel + "\n");
				//                            guiOld.messagesTF.setText(sb.toString());
                            
				if (belief instanceof UAVLocation) {
				    guiOld.locDisplay.addUAVLoc((UAVLocation)belief);
				} else if (belief instanceof UGSSensorReading) {
				    guiOld.locDisplay.addUGSSensorReading((UGSSensorReading)belief);
				} else if (belief instanceof VehicleBelief) {
				    guiOld.locDisplay.addVehicleID((VehicleBelief)belief);
				} else if (belief instanceof AssetStateBelief) {
				    guiOld.locDisplay.addGroundLoc((AssetStateBelief)belief);
				} else if (belief instanceof ImageData) {
				    Machinetta.Debugger.debug("Got image data", 1, this);
				    ImageData id = (ImageData)belief;
				    try {
					BufferedImage img = ImageIO.read(new ByteArrayInputStream(id.data));
					guiOld.locDisplay.addImage(id.loc, img);
				    } catch (IOException ex) {
					Machinetta.Debugger.debug("Image read failed: " + ex, 3, this);
				    }
				    //				    guiOld.locDisplay.addGroundLoc((Location)belief);
				}
			    }
                            
                            // NEW GUI
                            gui.addBelief(belief);
                        }
			
                        if(SIM_OPERATOR) {

                        // Here is the "intelligence of the human commander"
                        if (belief instanceof VehicleBelief) {
                            Machinetta.Debugger.debug(1, "VehicleBelief!!!!!!!!!");
                            VehicleBelief vb = (VehicleBelief)belief;
                            if (vb.getType() == Asset.Types.SA9 && vb.getConfidence() > 0.0 &&
                                    !strikeLocsOrdered.containsKey(new Point(vb.getX()/100, vb.getY()/100))) {
                                System.out.println("Deciding to hit: " + vb);
                                
                                boolean drop = dropPlan("strike, veh type="+vb.getType()+", confidence="+vb.getConfidence()+", loc="+vb.getX()+","+vb.getY()+", beliefid="+vb.id);
                                if(!drop) {
                                    strikeLocsOrdered.put(new Point(vb.getX()/100, vb.getY()/100), null);
                                    final int strikex = vb.getX();
                                    final int strikey = vb.getY();
                                    new Thread() {
                                        public void run() {
                                            Machinetta.Debugger.debug("Running strike plan, delayed for "+REACTION_DELAY_MS,3,this);
                                            if(REACTION_DELAY_MS > 0) {
                                                try {Thread.sleep(REACTION_DELAY_MS);} catch (InterruptedException e) {}
                                            }
                                            createStrikePlan(strikex, strikey);
                                        }
                                    }.start();
                                }
                            }
                        } else if (belief instanceof UGSSensorReading) {
                            UGSSensorReading ub = (UGSSensorReading) belief;
			    if(ub.isPresent()) {
				if(!patrolLocsOrdered.containsKey(new Point(ub.getX()/100, ub.getY()/100))) {
				    boolean drop = dropPlan("patrol, time="+ub.getTime()+", loc="+ub.getX()+","+ub.getY()+", beliefid="+ub.id);
				    if(!drop) {
					patrolLocsOrdered.put(new Point(ub.getX()/100, ub.getY()/100), null);
					final int patrolx = ub.getX();
					final int patroly = ub.getY();
					new Thread() {
						public void run() {
						    Machinetta.Debugger.debug("Running patrol plan, delayed for "+REACTION_DELAY_MS,3,this);
						    if(REACTION_DELAY_MS > 0) {
							try {Thread.sleep(REACTION_DELAY_MS);} catch (InterruptedException e) {}
						    }
						    createPatrolPlan(patrolx, patroly);
						}
					    }.start();
				    }
				}
			    }
                        }
			}   
                    } catch (Exception e) {
                        Debugger.debug("stateChange:Exception processing changed belief='"+belief+"', e="+e,5, this);
                        e.printStackTrace();
                    }
                    
                }
            }
        });
    }

    private void getConfigFields() {
        String cnfCtdbBaseName = null;
        String cnfShowGui = null;
        String cnfSimOperator = null;
        String cnfRepaintIntervalMs = null;
        String cnfReactionDelayMs = null;
        String cnfWaitBeforeMovePlanMs = null;
        String cnfDroppedPlanProb = null;
        
        if(null == Configuration.allMap) {
            Debugger.debug("Configuration.allMap is null, can't read config options.", 5, this);
        }

	UAVRI.readConfigs();
	
	MAP_WIDTH_METERS = ConfigReader.getConfig("MAP_WIDTH_METERS", MAP_WIDTH_METERS, true);
	MAP_HEIGHT_METERS = ConfigReader.getConfig("MAP_HEIGHT_METERS", MAP_HEIGHT_METERS, true);
	BBF_GRID_SCALE_FACTOR = ConfigReader.getConfig("BBF_GRID_SCALE_FACTOR", BBF_GRID_SCALE_FACTOR, true);

	CLUSTERING_ON = ConfigReader.getConfig("CLUSTERING_ON", CLUSTERING_ON, true);
	BINARY_BAYES_FILTER_ON = ConfigReader.getConfig("BINARY_BAYES_FILTER_ON", BINARY_BAYES_FILTER_ON, true);

        BAYES_FILTER_PANEL_ON = ConfigReader.getConfig("BAYES_FILTER_PANEL_ON", BAYES_FILTER_PANEL_ON, false);
        BAYES_FILTER_PANEL_X = ConfigReader.getConfig("BAYES_FILTER_PANEL_X", BAYES_FILTER_PANEL_X, false);
        BAYES_FILTER_PANEL_Y = ConfigReader.getConfig("BAYES_FILTER_PANEL_Y", BAYES_FILTER_PANEL_Y, false);

        ENTROPY_PANEL_ON = ConfigReader.getConfigBoolean("ENTROPY_PANEL_ON", ENTROPY_PANEL_ON, false);
        ENTROPY_PANEL_X = ConfigReader.getConfigInt("ENTROPY_PANEL_X", ENTROPY_PANEL_X, false);
        ENTROPY_PANEL_Y = ConfigReader.getConfigInt("ENTROPY_PANEL_Y", ENTROPY_PANEL_Y, false);

	CTDB_BASE_NAME = ConfigReader.getConfig("CTDB_BASE_NAME", CTDB_BASE_NAME, true);
	ROAD_FILE_NAME = ConfigReader.getConfig("ROAD_FILE_NAME", ROAD_FILE_NAME, true);
	REPAINT_INTERVAL_MS = ConfigReader.getConfig("REPAINT_INTERVAL_MS", REPAINT_INTERVAL_MS, true);
	SHOW_GUI = ConfigReader.getConfig("SHOW_GUI", SHOW_GUI, true);
	SIM_OPERATOR = ConfigReader.getConfig("SIM_OPERATOR", SIM_OPERATOR, true);
	REACTION_DELAY_MS = ConfigReader.getConfig("REACTION_DELAY_MS", REACTION_DELAY_MS, true);
	WAIT_BEFORE_MOVE_PLAN_MS = ConfigReader.getConfig("WAIT_BEFORE_MOVE_PLAN_MS", WAIT_BEFORE_MOVE_PLAN_MS, true);
	DROPPED_PLAN_PROB = ConfigReader.getConfig("DROPPED_PLAN_PROB", DROPPED_PLAN_PROB, true);
	if(Configuration.allMap.containsKey("GUI_VIEWPORT_X")
	   || Configuration.allMap.containsKey("GUI_VIEWPORT_Y")
	   || Configuration.allMap.containsKey("GUI_VIEWPORT_WIDTH")
	   || Configuration.allMap.containsKey("GUI_VIEWPORT_HEIGHT")) {
	    SimOperatorGUI.setViewPort = true;
	    SimOperatorGUI.viewPortX = ConfigReader.getConfig("GUI_VIEWPORT_X", SimOperatorGUI.viewPortX, true);
	    SimOperatorGUI.viewPortY = ConfigReader.getConfig("GUI_VIEWPORT_Y", SimOperatorGUI.viewPortY, true);
	    SimOperatorGUI.viewPortWidth = ConfigReader.getConfig("GUI_VIEWPORT_WIDTH", SimOperatorGUI.viewPortWidth, true);
	    SimOperatorGUI.viewPortHeight = ConfigReader.getConfig("GUI_VIEWPORT_HEIGHT", SimOperatorGUI.viewPortHeight, true);
	}
	SimOperatorGUI.soilTypes = ConfigReader.getConfig("GUI_SOIL_TYPES", SimOperatorGUI.soilTypes, true);
	SimOperatorGUI.showTraces = ConfigReader.getConfig("GUI_SHOW_TRACES", SimOperatorGUI.showTraces, true);
	SimOperatorGUI.gridLinesOneKm = ConfigReader.getConfig("GUI_GRID_LINES_ONE_KM", SimOperatorGUI.gridLinesOneKm, true);
	SimOperatorGUI.showMapObjectNames = ConfigReader.getConfig("GUI_SHOW_MAP_OBJECT_NAMES", SimOperatorGUI.showMapObjectNames, true);
	if(Configuration.allMap.containsKey("GUI_CONTOUR_MULTIPLES")) {
	    int multiple = ConfigReader.getConfig("GUI_CONTOUR_MULTIPLES", 0, true);
	    if(multiple <= 0)
		SimOperatorGUI.contourMultiples = BackgroundConfig.CONTOUR_MULT_NONE;
	    else if(multiple == 25)
		SimOperatorGUI.contourMultiples = BackgroundConfig.CONTOUR_MULT_25;
	    else if(multiple == 50)
		SimOperatorGUI.contourMultiples = BackgroundConfig.CONTOUR_MULT_50;
	    else if(multiple == 100)
		SimOperatorGUI.contourMultiples = BackgroundConfig.CONTOUR_MULT_100;
	    else if(multiple == 250)
		SimOperatorGUI.contourMultiples = BackgroundConfig.CONTOUR_MULT_250;
	    else if(multiple == 500)
		SimOperatorGUI.contourMultiples = BackgroundConfig.CONTOUR_MULT_500;
	    else if(multiple == 1000)
		SimOperatorGUI.contourMultiples = BackgroundConfig.CONTOUR_MULT_1000;
	}
    }
    
    private MiniWorldState miniWorldState = null;
    private JFrame bbfBeliefFrame = null;
    private JFrame bbfEntropyFrame = null;
    private BBFTabPanel bbfBeliefTabPanel = null;
    private BBFTabPanel bbfEntropyTabPanel = null;

    private void buildBBFFrames() {
	String proxyIDString = "Operator";
        if(BAYES_FILTER_PANEL_ON) {
	    bbfBeliefFrame = new JFrame("Bayes Filter Beliefs "+proxyIDString);
	    bbfBeliefTabPanel = new BBFTabPanel(miniWorldState);
	    bbfBeliefFrame.setLocation(BAYES_FILTER_PANEL_X,BAYES_FILTER_PANEL_Y);
	    bbfBeliefFrame.getContentPane().setLayout(new BorderLayout());
	    bbfBeliefFrame.getContentPane().add(bbfBeliefTabPanel, BorderLayout.CENTER);
	    bbfBeliefFrame.pack();
	    bbfBeliefFrame.setSize((int)(MAP_WIDTH_METERS/(BBF_GRID_SCALE_FACTOR*2)), (int)(MAP_HEIGHT_METERS/(BBF_GRID_SCALE_FACTOR*2)));
	    bbfBeliefFrame.addWindowListener(new WindowAdapter() {
                    public void windowClosing(WindowEvent e) {
                        System.exit(0);
                    }
                });
	    bbfBeliefFrame.setVisible(true);
	} 
        
        if(ENTROPY_PANEL_ON) {
	    bbfEntropyFrame = new JFrame("Entropy "+proxyIDString);
	    bbfEntropyTabPanel = new BBFTabPanel(miniWorldState);
	    bbfEntropyFrame.setLocation(ENTROPY_PANEL_X,ENTROPY_PANEL_Y);
	    bbfEntropyFrame.getContentPane().setLayout(new BorderLayout());
	    bbfEntropyFrame.getContentPane().add(bbfEntropyTabPanel, BorderLayout.CENTER);
	    bbfEntropyFrame.pack();
	    bbfEntropyFrame.setSize((int)(MAP_WIDTH_METERS/(BBF_GRID_SCALE_FACTOR*2)), (int)(MAP_HEIGHT_METERS/(BBF_GRID_SCALE_FACTOR*2)));
	    bbfEntropyFrame.addWindowListener(new WindowAdapter() {
                    public void windowClosing(WindowEvent e) {
                        System.exit(0);
                    }
                });
	    bbfEntropyFrame.setVisible(true);
	}
    }

    /** Creates a new instance of SimOperatorRI */
    public SimOperatorRI() {
        getConfigFields();
	// Really we should build the GUI stuff in
	// SimOperatorGUI... but we want to build the bbf costmap in
	// RI, and it takes the panels as args;
        if(BINARY_BAYES_FILTER_ON) {
	    miniWorldState = new MiniWorldState();
	    buildBBFFrames();
            bbfCM = new BinaryBayesFilterCostMap(MAP_WIDTH_METERS, MAP_HEIGHT_METERS, miniWorldState, bbfBeliefTabPanel, bbfEntropyTabPanel);
	    bbfCM.start();
        }

        if (SHOW_GUI) {
	    if(SHOW_OLD_GUI) {
		guiOld = new SimOperatorGUIOld();
		guiOld.setVisible(true);
	    }
            
            gui = new SimOperatorGUI(CTDB_BASE_NAME, ROAD_FILE_NAME, REPAINT_INTERVAL_MS);
        }
        
        
	if(CLUSTERING_ON) {
	    Machinetta.Debugger.debug("ClusteringOn parameter set to true, starting clustering thread.", 1, this);
	    clustering = new Clustering(bbfCM, gui,(int)(MAP_WIDTH_METERS/BBF_GRID_SCALE_FACTOR));
	    clustering.start();
	}
	else {
	    Machinetta.Debugger.debug("ClusteringOn parameter set to false, no clustering performed.", 1, this);
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
    public void sendMessage(OutputMessage msg) {
        Machinetta.Debugger.debug("Sim Operator sent: " + msg, 1, this);
        if (SHOW_GUI) {
	    if(SHOW_OLD_GUI) {
		StringBuffer sb = new StringBuffer(guiOld.messagesTF.getText());
		if (sb.length() > 500) {
		    sb.delete(500, sb.length());
		}
		sb.insert(0, "Message: " + msg.toString() + "\n");
		//		guiOld.messagesTF.setText(sb.toString());
	    }
        }
        
    }
    
    /**
     * Needed to implement Thread
     */
    public void run() {
        
	// Wait until we've loaded our self belief from beliefs file
        while (state.getSelf() == null) {
            try {
                sleep(100);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
        
	// Pause for some reason
        try {
            sleep(3000);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
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
        
        Machinetta.Debugger.debug("Not using hold plan (code commented out)", 3, this);
        
	// Pause some more?
        try {
            sleep(2000);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        
	if(TEST_META_TEAM_PLAN_TEMPLATES) {
	    Machinetta.Debugger.debug(1, "GENERATING TEAM PLAN FROM META TEAM PLAN TEMPLATE!");
	    TPTFactory tptFactory = new TPTFactory();
	    tptFactory.test4();
	    Machinetta.Debugger.debug(1, "DONE GENERATING TEAM PLAN FROM META TEAM PLAN TEMPLATE!");
	}

	// Start everything going - create the move plan for the HMMWVs?
	if(SIM_OPERATOR) {
	    PlanAgent pam = createMovePlan();
	}
    }
    
    private PlanAgent createHoldPlan() {
        Vector<RoleBelief> roles = new Vector<RoleBelief>();
        
        // Currently only a single strike on the target.
        BasicRole basic = new BasicRole(TaskType.hold);
        Hashtable<String, Object> params = new Hashtable<String, Object>();
        basic = (BasicRole)basic.instantiate(params);
        basic.constrainedWait = false;
        roles.add(basic);
        TeamPlanBelief tpb = new TeamPlanBelief(new BeliefNameID("WaitAtStart"), "Wait", null, true, new Hashtable(), roles);
        PlanAgent pa = new PlanAgent(tpb);
        
        return pa;
    }
    
    private PlanAgent createMovePlan() {
        Machinetta.Debugger.debug(1, "createMovePlan!!!!!!1 in SimOperator!!!");
        
        Vector<RoleBelief> roles = new Vector<RoleBelief>();
        
        BasicRole basic = new BasicRole(TaskType.move);
        Hashtable<String, Object> params = new Hashtable<String, Object>();
        params.put("Destination", new java.awt.Point(47500,47500));
        basic = (BasicRole)basic.instantiate(params);
        basic.constrainedWait = false;
        roles.add(basic);
        TeamPlanBelief tpb = new TeamPlanBelief(new BeliefNameID("MoveToDest"), "Move", null, true, new Hashtable(), roles);
        PlanAgent pa = new PlanAgent(tpb);
        
        return pa;
    }
    
    private boolean dropPlan(String planDesc) {
        
        Machinetta.Debugger.debug(1, "dropPlan!!!!!!1 in SimOperator!!!     " + planDesc);
        
        double roll = rand.nextDouble();
        if(roll < DROPPED_PLAN_PROB) {
            Machinetta.Debugger.debug("dropped plan "+planDesc, 3, this);
            return true;
        }
        Machinetta.Debugger.debug("creating plan "+planDesc, 3, this);
        return false;
    }
    
    private PlanAgent createStrikePlan(int x, int y) {
        Machinetta.Debugger.debug("createStrikePlan: loc="+x+", "+y, 3, this);
        Vector<RoleBelief> roles = new Vector<RoleBelief>();
        
        // Currently only a single strike on the target.
        BasicRole basic = new BasicRole(TaskType.attackFromAir);
        Hashtable<String, Object> params = new Hashtable<String, Object>();
        params.put("TargetLocation", new java.awt.Point(x,y));
        basic = (BasicRole)basic.instantiate(params);
        basic.constrainedWait = false;
        roles.add(basic);
        TeamPlanBelief tpb = new TeamPlanBelief(new BeliefNameID("KillSAM"+x+":"+y), "KillSAM", null, true, new Hashtable(), roles);
        PlanAgent pa = new PlanAgent(tpb);
        
        return pa;
    }
    
    private PlanAgent createPatrolPlan(int x, int y) {
        Machinetta.Debugger.debug("createPatrolPlan: loc="+x+", "+y, 3, this);
        Vector<RoleBelief> roles = new Vector<RoleBelief>();
        
        // Currently only a single strike on the target.
        BasicRole basic = new BasicRole(TaskType.patrol);
        Hashtable<String, Object> params = new Hashtable<String, Object>();
        Area area = new Area(x-500,y-500,x+500, y+500);
        params.put("Area", area);
        basic = (BasicRole)basic.instantiate(params);
        basic.constrainedWait = false;
        roles.add(basic);
        TeamPlanBelief tpb = new TeamPlanBelief(new BeliefNameID("Patrol"+x+":"+y), "Patrol", null, true, new Hashtable(), roles);
        PlanAgent pa = new PlanAgent(tpb);
        
        return pa;
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
