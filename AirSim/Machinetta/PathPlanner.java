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
 * PathPlanner.java
 *
 * Created on March 8, 2006, 12:02 PM
 *
 */
package AirSim.Machinetta;

import Gui.LatLonUtil;
import AirSim.Environment.Area;
import AirSim.Environment.Assets.Tasks.TaskType;
import AirSim.Environment.Vector3D;
import AirSim.Environment.Waypoint;
import Machinetta.Debugger;
import AirSim.Machinetta.Beliefs.*;
import AirSim.Machinetta.Beliefs.ProxyEventData.EventType;
import AirSim.Machinetta.CostMaps.CostMap;
import AirSim.Machinetta.CostMaps.MixGaussiansCostMap;
import AirSim.Machinetta.CostMaps.OtherVehicleCostMap;
import AirSim.Machinetta.CostMaps.SimpleStaticCostMap;
import AirSim.Machinetta.Messages.CameraCommandPA;
import AirSim.Machinetta.Messages.GeoLocatePA;
import AirSim.Machinetta.Messages.NextWaypointPA;
import AirSim.Machinetta.Messages.PRMessage;
import Machinetta.ConfigReader;
import Machinetta.Coordination.MAC.InformationAgent;
import Machinetta.Coordination.MACoordination;
import Machinetta.RAPInterface.RAPInterfaceImplementation;
import Machinetta.State.BeliefID;
import Machinetta.State.BeliefType.Belief;
import Machinetta.State.BeliefType.ProxyID;
import Machinetta.State.BeliefType.RAPBelief;
import Machinetta.State.BeliefType.TeamBelief.Associates;
import Machinetta.State.BeliefType.TeamBelief.RoleAllocationBelief;
import Machinetta.State.BeliefType.TeamBelief.TeamBelief;
import Machinetta.State.BeliefType.TeamBelief.TeamPlanBelief;
import Machinetta.State.ProxyState;
import Machinetta.State.StateChangeListener;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Hashtable;
import java.text.DecimalFormat;
import java.awt.geom.Point2D;
import java.util.concurrent.*;
import java.util.*;

// This class is a mess and way too complicated.  We should refactor
// it into multiple subclasses.  A good start on this has already been
// made by moving much functionality out into Autopilot.
/**
 *
 * @author pscerri
 */
class PathPlanner extends Thread implements StateChangeListener {

    private static DecimalFormat fmt = new DecimalFormat("0.00000");
    private static DecimalFormat fmt2 = new DecimalFormat("0.0");

    private final static int MAX_REPLAN_COUNT = 2;
    private final static int REDIRECT_ELEVATION_AT_TARGET_METERS = 600;
    private final static int REDIRECT_APPROACH_DISTANCE_METERS = 500;
    private final static double REDIRECT_APPROACH_SPEED_METERS_PER_MS = (50000.0 / 3600.0) / 1000.0;

    private final static int EXPECTED_TIME_TO_PLAN = 500;
    private final static int EOIMAGE_COMPLETION_RANGE = 50;
    private final static boolean EOIMAGE_SEND_CAMERA_COMMAND_IMMEDIATELY = false;
    private boolean requireApproval = false;

    private enum CommandType {

        NONE, FORCE_REPLAN, SET_REQUIRE_APPROVAL, UPDATE_LOCATION, ADD_COST_MAP, REMOVE_COST_MAP, ADD_ROLE, REMOVE_ROLE, STATE_CHANGED
    };

    private class Command {

        public CommandType command = CommandType.NONE;
        public boolean requireApproval = false;
        double x = 0;
        double y = 0;
        double z = 0;
        CostMap map = null;
        BasicRole role = null;
        BeliefID[] belief = null;
        String forceReplanReason = null;

        public Command(CommandType type) {
            command = type;
        }

        public String toString() {
            return "Com: " + command + " " + x + "," + y + "," + z + ", reqApp=" + requireApproval + ", role=" + role;
        }
    }
    private BlockingQueue<Command> commandQueue = new LinkedBlockingQueue<Command>();

    private String queueInfo() {
        StringBuffer buf = new StringBuffer("");
        buf.append("size=" + commandQueue.size() + ", ");
        HashMap<CommandType, Integer> comTypes = new HashMap<CommandType, Integer>();
        HashMap<String, Integer> stateChangedTypes = new HashMap<String, Integer>();
        Command[] coms = commandQueue.toArray(new Command[1]);
        for (int loopi = 0; loopi < coms.length; loopi++) {
            Command com = coms[loopi];
            if (null == com) {
                continue;
            }
            if (null == com.command) {
                continue;
            }
            if (comTypes.containsKey(com.command)) {
                Integer comCount = comTypes.get(com.command);
                comTypes.put(com.command, new Integer(comCount + 1));
            } else {
                comTypes.put(com.command, new Integer(1));
            }
            if (com.command == CommandType.STATE_CHANGED) {
                for (int loopj = 0; loopj < com.belief.length; loopj++) {
                    BeliefID bid = com.belief[loopj];
                    Belief belief = state.getBelief(bid);
                    if (null != belief) {
                        String beliefClassName = belief.getClass().toString();
                        Integer beliefCount = stateChangedTypes.get(beliefClassName);
                        if (beliefCount == null) {
                            beliefCount = new Integer(1);
                        } else {
                            beliefCount = new Integer(beliefCount + 1);
                        }
                        stateChangedTypes.put(beliefClassName, beliefCount);
                    }
                }
            }
        }
        buf.append("Com types: ");
        Integer comCount;
        comCount = comTypes.get(CommandType.NONE);
        buf.append("NONE");
        if (null == comCount) {
            buf.append(" = 0, ");
        } else {
            buf.append(" = ").append(comCount).append(", ");
        }
        comCount = comTypes.get(CommandType.FORCE_REPLAN);
        buf.append("FORCE_REPLAN");
        if (null == comCount) {
            buf.append(" = 0, ");
        } else {
            buf.append(" = ").append(comCount).append(", ");
        }
        comCount = comTypes.get(CommandType.SET_REQUIRE_APPROVAL);
        buf.append("SET_REQUIRE_APPROVAL");
        if (null == comCount) {
            buf.append(" = 0, ");
        } else {
            buf.append(" = ").append(comCount).append(", ");
        }
        comCount = comTypes.get(CommandType.UPDATE_LOCATION);
        buf.append("UPDATE_LOCATION");
        if (null == comCount) {
            buf.append(" = 0, ");
        } else {
            buf.append(" = ").append(comCount).append(", ");
        }
        comCount = comTypes.get(CommandType.ADD_COST_MAP);
        buf.append("ADD_COST_MAP");
        if (null == comCount) {
            buf.append(" = 0, ");
        } else {
            buf.append(" = ").append(comCount).append(", ");
        }
        comCount = comTypes.get(CommandType.REMOVE_COST_MAP);
        buf.append("REMOVE_COST_MAP");
        if (null == comCount) {
            buf.append(" = 0, ");
        } else {
            buf.append(" = ").append(comCount).append(", ");
        }
        comCount = comTypes.get(CommandType.ADD_ROLE);
        buf.append("ADD_ROLE");
        if (null == comCount) {
            buf.append(" = 0, ");
        } else {
            buf.append(" = ").append(comCount).append(", ");
        }
        comCount = comTypes.get(CommandType.REMOVE_ROLE);
        buf.append("REMOVE_ROLE");
        if (null == comCount) {
            buf.append(" = 0, ");
        } else {
            buf.append(" = ").append(comCount).append(", ");
        }
        comCount = comTypes.get(CommandType.STATE_CHANGED);
        buf.append("STATE_CHANGED");
        if (null == comCount) {
            buf.append(" = 0, ");
        } else {
            buf.append(" = ").append(comCount).append(", ");
        }

        buf.append(" Belief Classes: ");
        for (String key : stateChangedTypes.keySet()) {
            buf.append(key + " = " + stateChangedTypes.get(key) + ", ");
        }

        return buf.toString();
    }

    private Path3D nextPath = null;
    private BeliefID nextPathID = null;
    private BeliefID currPathID = null;

    private ArrayList<BasicRole> roles = new ArrayList<BasicRole>();
    private ArrayList<CostMap> costMaps = new ArrayList<CostMap>();
    /**
     * Allows class to keep track of which cost map is which.
     */
    private Hashtable<Object, ArrayList<CostMap>> costMapDirectory = new Hashtable<Object, ArrayList<CostMap>>();
    private OtherVehicleCostMap otherVehicleCostMap = new OtherVehicleCostMap(UAVRI.OTHER_VEHICLE_COSTMAP_AVOID_RANGE_METERS, UAVRI.OTHER_VEHICLE_COSTMAP_CONFLICT_RANGE_METERS, UAVRI.OTHER_VEHICLE_COSTMAP_CONFLICT_Z_RANGE_METERS);

    // This waypoint is the waypoint associated with any role that has
    // a specific location as it's destination - i.e. move,
    // attackFromAir, EOImage - it is set in addRole.  @TODO: While
    // the architecture of PathPlanner allows for more than one role
    // to be held simultaneously, clearly this can only hold one
    // waypoint, the most recent one.  This is really a hack to make
    // the UAVs go where we want them to for EOImage, and will be
    // removed when the planner is working better.
    private Waypoint currTarget = null;

    private java.awt.Point imageLocation;

    // private static TiledCostMap terrainCM = null;
    ProxyState state = new ProxyState();

    {
        state.addChangeListener(this);
    }

    private RAPInterfaceImplementation rapInt = null;
    private boolean dynamicFlyZones = false;
    private Autopilot autopilot = null;

    private DynamicFlyZones dfz = null;

    // private double preferredPathLengthExponential = RRTPlanner.DEFAULT_NODE_DIST_COST_EXPONENTIAL_LONG_PATH;
    public PathPlanner(RAPInterfaceImplementation rapInt, boolean dynamicFlyZones, Autopilot autopilot) {
        this.rapInt = rapInt;
        this.dynamicFlyZones = dynamicFlyZones;
        this.autopilot = autopilot;

        if (UAVRI.OTHER_VEHICLE_COSTMAP_ON) {
            System.out.println("YEAH Adding other vehicle cost map");
            costMaps.add(otherVehicleCostMap);
        }
        
        if (dynamicFlyZones) {
            dfz = new DynamicFlyZones(this, rapInt, costMaps);
        }

        
        // Temp insertion of static cost maps
        final SimpleStaticCostMap sscm = new SimpleStaticCostMap();
        sscm.addCostCircle(2500, 2500, 500, 100000);
        costMaps.add(sscm);        
        // End static cost map insertion
        
        // Temp dynamic insertion of AA cost maps
        state.addChangeListener(new StateChangeListener() {

            @Override
            public void stateChanged(BeliefID[] b) {
                for (BeliefID b1 : b) {
                    Belief bel = state.getBelief(b1);
                    
                    if (bel instanceof GeoLocateRequest) {
                        GeoLocateRequest glr = (GeoLocateRequest)bel;
                        if (glr.located) {
                            sscm.addCostCircle(glr.longtitude, glr.latitude, 500, 10000);
                        }
                    }
                }
            }
        });
        // End dynamic insertion of AA cost maps
        
        /* Taken this out by default.  If an asset wants it, it should load it itself.
         if (terrainCM == null) {
         try {
         String terrainCMLoc = (String)Machinetta.Configuration.allMap.get("TERRAIN_COSTMAP_LOCATION");
         if (terrainCMLoc != null) {
         ObjectInputStream is = new ObjectInputStream(new FileInputStream(terrainCMLoc));
         terrainCM = (TiledCostMap)is.readObject();
         } else {
         Machinetta.Debugger.debug("No terrain cost map location provided.", 3, this);
         }
         } catch (IOException ex) {
         Machinetta.Debugger.debug("Failed to get Terrain cost map:" + ex, 3, this);
         } catch (ClassNotFoundException ex) {
         Machinetta.Debugger.debug("Failed to get Terrain cost map:" + ex, 3, this);
         }
         }
         
         if (terrainCM != null) {
         addCostMap(terrainCM);
         Machinetta.Debugger.debug("Added terrain cost map", 1, this);
         }
         */
        //}
        timeBetweenFilterPanelUpdatesMs = ConfigReader.getConfigInt("TIME_BETWEEN_FILTER_PANEL_UPDATES_MS", timeBetweenFilterPanelUpdatesMs, false);
    }

    public PathPlanner(RAPInterfaceImplementation rapInt, Autopilot autopilot) {
        this(rapInt, false, autopilot);
    }

    public void run() {

        boolean commandQueueTooLong = false;
        while (true) {
            try {
                if (!autopilot.getFirstUpdate()) {
		    // 1) TRIGGER PLANNING OF NEXT PATH
                    //		    Machinetta.Debugger.debug(1, "run: calling autopilot.currPathExistsAndIsReadyForNextPath()");
                    if (autopilot.currPathExistsAndIsReadyForNextPath()) {
                        Machinetta.Debugger.debug(1, "run: calling planNextPath, simtime= " + SimTime.getEstimatedTime() + ", less than " + UAVRI.PATHPLANNER_REPLAN_DIST_FROM_END_OF_PATH + " dist left in currPath");
                        planNextPath();
                    } else {
			// @TODO: Changing this to only be called if
                        // we didn't call planNextPath above... I
                        // _think_ this is the right thing to do, if
                        // we don't then we get this situation where
                        // there is a holding pattern for currpath and
                        // no next path and we end up planning a next
                        // path and then IMMEDIATELY planning another
                        // next path before the autopilot has had a
                        // chance to switch out of the holding pattern
                        // onto the first next path.
                        //
                        // Really we need to look at this and
                        // characterize the interactions between
                        // autopilot and pathplanner a bit better.
                        //
                        // Changing again to only re-call planNextPath()
                        // if there is no current path _and_ no existing nextPath
                        // After successfully planning a next path, stop re-planning.

			// 2) TRIGGER PLANNING OF CURRENT PATH (?)
			// If we have no current path, or if we're at the
                        // end of the current path, or if we're in a
                        // holding pattern, then we have to create a new
                        // path.  This should probably only happen at
                        // startup or forced replans
                        //			Machinetta.Debugger.debug(1, "run: calling autopilot.needCurrentPath()");

                        if ( autopilot.needCurrentPath() && autopilot.needNextPath() ) {
                            planNextPath();
                        }
                    }

		    // Now called by autopilot.updateLocation()
// 		    autopilot.checkForSendNextWaypoint();
		    // 4) Process any current ROLE stuff - i.e. if
                    // role is eoimage, if we're close enough to the
                    // target then send the RAP the command to take a
                    // picture.  This also removes finished roles and
                    // triggers next path generation when we're done
                    // with a role.
                    if (roles.size() > 0) {
                        Machinetta.Debugger.debug(0, "run: processing role stuff since roles.size() == " + roles.size());
                        ArrayList<BasicRole> done = null;
                        for (BasicRole role : roles) {
                            Machinetta.Debugger.debug(0, "run: Checking for todo: " + role);
                            switch (role.getType()) {
                                case geolocateSense:
                                    break;

                                case attackFromAirOrGround:
                                case attackFromAir: {
                                    Machinetta.Debugger.debug(1, "run: Deciding what to do for attack from air");
                                    Vector3D p = (Vector3D) role.params.get("TargetLocation");
                                    if (p == null) {
                                        Machinetta.Debugger.debug(3, "run: Could not get target location");
                                    } else {
                                        if (autopilot.atWaypoint(p.x, p.y, 10000)) {
                                            Machinetta.Debugger.debug(1, "run: In position to strike target");
                                            PRMessage msg = new PRMessage(PRMessage.MessageType.NEW_TASK);
                                            msg.params.put("Task", TaskType.attackFromAir);
                                            msg.params.put("Location", p);
                                            rapInt.sendMessage(msg);

                                            // @todo Find a better way of stopping roles.
                                            removeCostMapByRole(role);
                                            if (done == null) {
                                                done = new ArrayList<BasicRole>();
                                            }
					    // @TODO: I _think_ this
                                            // triggers the roleagent
                                            // to inform the planagent
                                            // that we've completed
                                            // the role - iff the
                                            // roleagent is still
                                            // around.  Definitely, if
                                            // it is aronud, when it
                                            // gets a stateChanged it
                                            // will check for
                                            // role.complete and if so
                                            // call informRoleComplete
                                            // on itself.
                                            role.setComplete();
                                            done.add(role);
                                            ProxyEventData ped = new ProxyEventData((state.getSelf().getProxyID()), EventType.ATTACK_FROM_AIR_EXECUTED, p);
                                            ped.setLocallySensed(true);
                                            state.addBelief(ped);
                                            state.notifyListeners();

                                        } else {
                                            Machinetta.Debugger.debug(1, "run: Too far from target: " + fmt2.format(p.x) + " " + fmt2.format(p.y) + " -> " + autopilot.getCurrXYString());
                                        }
                                    }
                                }
                                break;

                                case EOImage:
                                    // This needs to be adjusted for angles, etc.
                                    Machinetta.Debugger.debug(0, "run: Deciding what to do for EOImage");
                                    java.awt.Point p = (Point) role.params.get("ImageLocation");
                                    if (p == null) {
                                        Machinetta.Debugger.debug(3, "Could not get image location");
                                    } else {
                                        if (EOIMAGE_SEND_CAMERA_COMMAND_IMMEDIATELY) {
                                            Machinetta.Debugger.debug(1, "INFOTECH: Deciding what to do for EOImage: Sending camera command");
                                            CameraCommandPA cMsg = new CameraCommandPA();
                                            cMsg.EO = true;
                                            cMsg.altitude = 0;
                                            cMsg.longtitude = imageLocation.x;
                                            cMsg.latitude = imageLocation.y;
                                            cMsg.zoom = 1.0;
                                            rapInt.sendMessage(cMsg);
                                            Machinetta.Debugger.debug(1, "INFOTECH: Deciding what to do for EOImage: Stopping role");
                                            // @todo Find a better way of stopping roles.
                                            removeCostMapByRole(role);
                                            if (done == null) {
                                                done = new ArrayList<BasicRole>();
                                            }
                                            done.add(role);
                                        } else if (autopilot.atWaypoint(p.x, p.y, UAVRI.EO_DISTANCE_TOLERANCE)) {
                                            Machinetta.Debugger.debug(1, "run: Deciding what to do for EOImage: Sending camera command");
                                            CameraCommandPA cMsg = new CameraCommandPA();
                                            cMsg.EO = true;
                                            cMsg.altitude = 0;
                                            cMsg.longtitude = imageLocation.x;
                                            cMsg.latitude = imageLocation.y;
                                            cMsg.zoom = 1.0;
                                            rapInt.sendMessage(cMsg);
                                        } else if (autopilot.atWaypoint(p.x, p.y, UAVRI.EO_DISTANCE_TOLERANCE / 2)) {
                                            Machinetta.Debugger.debug(1, "Deciding what to do for EOImage: Stopping role");
                                            // @todo Find a better way of stopping roles.
                                            removeCostMapByRole(role);
                                            if (done == null) {
                                                done = new ArrayList<BasicRole>();
                                            }
                                            done.add(role);
                                        } else {
                                            double dist = autopilot.getXYDist(p.x, p.y);
                                            Machinetta.Debugger.debug(1, "run: Deciding what to do for EOImage: Still too far from image location: " + p.x + " " + p.y + " -> currently at " + autopilot.getCurrXYString() + " dist is " + fmt2.format(dist) + ", tolerance is " + UAVRI.EO_DISTANCE_TOLERANCE);
                                        }
                                    }

                                    break;

                                case intelSurveilRecon: {
                                    if (areaToBeIn != null) {
                                        Debugger.debug(1, "run: for intelSurveilRecon role, checking if we're in the ISR area yet");
                                        Vector3D startLoc = autopilot.getCurrLocation();
                                        double startx = startLoc.x;
                                        double starty = startLoc.y;
                                        if (areaToBeIn.inside(startx, starty)) {
                                            Debugger.debug(1, "run: for intelSurveilRecon role, we are inside ISR area, stop using direct path");
                                            areaToBeIn = null;
                                            hackDirectPath = false;
                                            // we made it to the isr area so set the request role to located->true
                                            GeoLocateRequest isr_role = (GeoLocateRequest) role.params.get("request");
                                            isr_role.located = true;
                                            state.addBelief( isr_role );
                                            state.notifyListeners();
// 					forceReplan("intelSurveilRecon: Inside ISR Area");
                                        }
                                    }
                                }
                                case patrol: {
                                    if (areaToBeIn != null) {
                                        Debugger.debug(1, "run: for patrol role, checking if we're in the patrol area yet");
                                        Vector3D startLoc = autopilot.getCurrLocation();
                                        double startx = startLoc.x;
                                        double starty = startLoc.y;
                                        if (areaToBeIn.inside(startx, starty)) {
                                            Debugger.debug(1, "run: for patrol role, we are inside the patrol area, stop using direct path");
                                            areaToBeIn = null;
                                            hackDirectPath = false;
// 					forceReplan("patrol: Inside patrol Area");
                                        }
                                    }
                                }
                                break;

                                default:
                                    Machinetta.Debugger.debug(0, "run: do nothing special for: " + role.getType());
                            }
                        }
                        if (done != null) {
                            roles.removeAll(done);
                            // Trigger replanning
                            //                    if(SimTime.getEstimatedTime() != 0) {
                            Machinetta.Debugger.debug(1, "run: calling planNextPath, simtime= " + SimTime.getEstimatedTime() + ", done is non null (why does this mean call plan()?)");
                            planNextPath();
                            //                    }
                            // Cause the new path to be used.
                            Machinetta.Debugger.debug(1, "run: calling autopilot.cancelCurrPath() to force taking on of new path for new role.");
                            autopilot.cancelCurrPath();
                        }
                    }
                } // if(firstUpdate)

                Command com = null;
                int commandQueueSize = commandQueue.size();
                if (commandQueueSize > 100 && !commandQueueTooLong) {
                    commandQueueTooLong = true;
                    Machinetta.Debugger.debug(1, "run: commandQueue.size() too long, = " + commandQueue.size());
                    Machinetta.Debugger.debug(1, "run: queue info: " + queueInfo());
                } else if (commandQueueSize <= 100 && commandQueueTooLong) {
                    commandQueueTooLong = false;
                    Machinetta.Debugger.debug(1, "run: commandQueue.size() no longer too long, = " + commandQueue.size());
                }

		// 5) Process incoming commands in command queue
                Machinetta.Debugger.debug(0, "run: processing incoming commands in queue, queue size = " + commandQueueSize);
                for (int loopi = 0; loopi < commandQueueSize; loopi++) {
                    //		    Machinetta.Debugger.debug(1, "This class is a steaming piece of CRAP");
                    try {
                        com = commandQueue.take();
                    } catch (InterruptedException ex) {
                        Machinetta.Debugger.debug(1, "Pathplanner interrupted");
                    }
// 		Machinetta.Debugger.debug("commandQueue.size() = "+commandQueue.size()+" next command= "+com, 1, this);
                    if (null != com) {
                        if (CommandType.STATE_CHANGED == com.command) {
                            stateChanged(com);
                        } // NOTE: THIS is obsolete because now UAVRI calls
                        // autopilot.updateLocation() directly.
                        // 			else if(CommandType.UPDATE_LOCATION == com.command)
                        // 			    updateLocation(com);
                        else if (CommandType.FORCE_REPLAN == com.command) {
                            Machinetta.Debugger.debug(1, "run: FORCE_REPLAN command forcing replan");
                            forceReplan(com);
                        } else if (CommandType.SET_REQUIRE_APPROVAL == com.command) {
                            setRequireApproval(com);
                        } else if (CommandType.ADD_ROLE == com.command) {
                            Machinetta.Debugger.debug(1, "run: Adding role and forcing replan");
                            addRole(com);
                            // This isn't great.  addRole can add costmaps without using addCostMap
                            // but in other cases it might be used.  Thus, it is possible there will
                            // be two forced replans.  Same for removing roles
                            forceReplan("Adding role=" + com.role);
                        } else if (CommandType.REMOVE_ROLE == com.command) {
                            Machinetta.Debugger.debug(1, "run: Removing role and forcing replan");
                            removeRole(com);
                            forceReplan("Removing role=" + com.role);
                        } else if (CommandType.ADD_COST_MAP == com.command) {
                            Machinetta.Debugger.debug(1, "run: Adding costmap and forcing replan");
                            addCostMap(com);
                            forceReplan("Adding costmap " + com.map);
                        } else if (CommandType.REMOVE_COST_MAP == com.command) {
                            Machinetta.Debugger.debug(1, "run: Removing and forcing replan");
                            removeCostMap(com);
                            forceReplan("Removing costmap " + com.map);
                        } else if (CommandType.NONE == com.command) {
                            Machinetta.Debugger.debug(3, "run: Got command NONE from commandQueue - that should never happen, programmer error?");
                        } else {
                            Machinetta.Debugger.debug(3, "run: Unknown CommandType = " + com.command + " from commandQueue - Someone added a new command type and forgot to add code here to catch it?");
                        }
                    }
                }

		// @TODO: This will work as long as this is a polling
                // loop, but eventually we want to change it to not be
                // a polling loop.  Autopilot can't do the update
                // because it doesn't have the costmaps.  Down the
                // road I guess we should split the update method into
                // two parts, one for costmaps and one for currx/y/z.
                updateRRTPanels();

                try {
                    Thread.sleep(50);
                } catch (Exception e) {
                }
            } catch (Exception e) {
                Machinetta.Debugger.debug(4, "run: Exception in pathPlanner, e=" + e);
                e.printStackTrace();
                Machinetta.Debugger.debug(4, "run: Ignoring exception and continuing run loop.");
            }
        }

    }

    private void animateRRTTree(double x, double y, double z, long simTimePathStart) {
        // This code is for animating the display of the rrt tree
        // that has just been generated, for recording for videos.
        // Don't want it on right now (because among other thing
        // it really messes up the UAV because nothing else
        // happens until it's done animating) but might need it
        // again later.
        boolean ANIMATE_RRT_TREE = false;
        if (!ANIMATE_RRT_TREE) {
            return;
        }
        while (true) {
            nextPath = RRTPlanner.plan(x, y, z, simTimePathStart, costMaps, UAVRI.RRT_MAX_PATH_METERS, UAVRI.UAV_SPEED_METERS_PER_SEC);
            if (null != RRTPlanner.rrtTreePanel) {
                RRTPlanner.depthToPaint = 0;
                for (int loopj = 0; loopj < 24; loopj++) {
                    RRTPlanner.depthToPaint = loopj;
                    RRTPlanner.rrtTreePanel.repaint();
                    try {
                        Thread.sleep(100);
                    } catch (Exception e) {
                    }
                }
            }
        }
    }

    public void pathApproved(PlannedPath pp) {
        // Get approval
        Stats.pathsShared(1);

        // The current deconfliction algorithm assumes OK unless conflict
        pp.path.setApproved(true);
        pp.setLocallySensed(true);

        Machinetta.Debugger.debug(4, "pathApproved: 4 Sharing path " + pp.getID() + " with ttl of " + UAVRI.PATHPLANNER_PLANNED_PATH_TTL);
        state.addBelief(pp);
        InformationAgent agent = new InformationAgent(pp, UAVRI.PATHPLANNER_PLANNED_PATH_TTL);
        MACoordination.addAgent(agent);
        // Let it act
        agent.stateChanged();
    }

    private Area areaToBeIn = null;

    private boolean hackDirectPath = false;

    private Path3D hackDirectPath() {
        Vector3D startLoc = autopilot.getCurrLocation();
        double startx = startLoc.x;
        double starty = startLoc.y;
        double startz = startLoc.z;

        double tx = currTarget.x;
        double ty = currTarget.y;
        double tz = startz;

        Waypoint start = new Waypoint(startx, starty, startz);
        Waypoint dest = new Waypoint(tx, ty, tz);

        Path3D newPath = new Path3D();
        newPath.setAssetID(state.getSelf().getProxyID());

        newPath.addPoint(start);
        for (double loopd = .1; loopd < 1.0; loopd += .1) {
            Waypoint interp = Waypoint.interpolate(start, dest, loopd);
            newPath.addPoint(interp);
        }
        newPath.addPoint(dest);

        if (false) {
            double SPIRAL_WIDTH = 1000;
            double CIRCLE_STEP_LENGTH = 600;
            double maxRadius = 3000;
            for (int loopi = 0; loopi < 1; loopi++) {
                for (double loopRadius = SPIRAL_WIDTH; loopRadius < maxRadius; loopRadius += SPIRAL_WIDTH) {
                    double circumference = Math.PI * (2 * loopRadius);
                    //		double stepsPerCircle = circumference/CIRCLE_STEP_LENGTH;
                    double stepsPerCircle = circumference / 4;
                    //	    double stepsPerCircle = 16;
                    double radIncr = (Math.PI * 2) / stepsPerCircle;
                    double radius2 = loopRadius - SPIRAL_WIDTH;
                    for (double radAngle = 0; radAngle < Math.PI * 2; radAngle += radIncr) {
                        radius2 += SPIRAL_WIDTH / stepsPerCircle;
                        // Note, we cast to int merely to truncate the part
                        // past the decimal point.
                        double x = (int) (tx + (radius2 * Math.cos(radAngle)));
                        double y = (int) (ty + (radius2 * Math.sin(radAngle)));
                        newPath.addPoint(new Waypoint(x, y, startz));
                    }
                }
            }
        } else if (null == areaToBeIn) {
            double radius = 1500;
            double twoPi = Math.PI * 2;
            double q1 = twoPi * 0;
            double q2 = twoPi * 1.0 / 8.0;
            double q3 = twoPi * 2.0 / 8.0;
            double q4 = twoPi * 3.0 / 8.0;
            double q5 = twoPi * 4.0 / 8.0;
            double q6 = twoPi * 5.0 / 8.0;
            double q7 = twoPi * 6.0 / 8.0;
            double q8 = twoPi * 7.0 / 8.0;
            double x, y;
            for (int loopi = 0; loopi < 10; loopi++) {
                x = (int) (tx + (radius * Math.cos(q1)));
                y = (int) (ty + (radius * Math.sin(q1)));
                newPath.addPoint(new Waypoint(x, y, startz));
                x = (int) (tx + (radius * Math.cos(q2)));
                y = (int) (ty + (radius * Math.sin(q2)));
                newPath.addPoint(new Waypoint(x, y, startz));
                x = (int) (tx + (radius * Math.cos(q3)));
                y = (int) (ty + (radius * Math.sin(q3)));
                newPath.addPoint(new Waypoint(x, y, startz));
                x = (int) (tx + (radius * Math.cos(q4)));
                y = (int) (ty + (radius * Math.sin(q4)));
                newPath.addPoint(new Waypoint(x, y, startz));
                x = (int) (tx + (radius * Math.cos(q5)));
                y = (int) (ty + (radius * Math.sin(q5)));
                newPath.addPoint(new Waypoint(x, y, startz));
                x = (int) (tx + (radius * Math.cos(q6)));
                y = (int) (ty + (radius * Math.sin(q6)));
                newPath.addPoint(new Waypoint(x, y, startz));
                x = (int) (tx + (radius * Math.cos(q7)));
                y = (int) (ty + (radius * Math.sin(q7)));
                newPath.addPoint(new Waypoint(x, y, startz));
                x = (int) (tx + (radius * Math.cos(q8)));
                y = (int) (ty + (radius * Math.sin(q8)));
                newPath.addPoint(new Waypoint(x, y, startz));
            }
        }

        RRTPlanner.recalculateTimes(startx, starty, startz, newPath, SimTime.getEstimatedTime(), UAVRI.UAV_SPEED_METERS_PER_SEC);

        return newPath;
    }

    private void redirectPathToTarget(double startx, double starty, double startz, long simTimePathStart) {
        Path3D oldNextPath = nextPath;

        Waypoint uavTarget = new Waypoint(currTarget.x, currTarget.y, currTarget.z);
        Machinetta.Debugger.debug(1, "redirectPathToTarget: Redirecting path to target at " + uavTarget);
        // We want to be 100 meters OVER the target...
        uavTarget.z = REDIRECT_ELEVATION_AT_TARGET_METERS;
        Path3D nextPath2 = new Path3D();

        Waypoint[] wps = nextPath.getWaypointsAry();
        Vector3D toVector = wps[0].toVector(uavTarget);
        int nearestIdx = 0;
        double nearestLength = toVector.length();
        Machinetta.Debugger.debug(1, "redirectPathToTarget: Redirecting: wp[0]=" + wps[0] + " length to currTarget  =" + nearestLength);
        for (int loopi = 1; loopi < wps.length; loopi++) {
            toVector = wps[loopi].toVector(uavTarget);
            double toLength = toVector.length();
            if (toLength < nearestLength) {
                Machinetta.Debugger.debug(1, "redirectPathToTarget: Redirecting: wp[" + loopi + "]=" + wps[loopi] + " length to currTarget  =" + toLength + " CLOSER than old wp");
                nearestIdx = loopi;
                nearestLength = toLength;
            } else {
                Machinetta.Debugger.debug(1, "redirectPathToTarget: Redirecting: wp[" + loopi + "]=" + wps[loopi] + " length to currTarget  =" + toLength + " NOT closer than old wp");
            }
        }
        Machinetta.Debugger.debug(1, "redirectPathToTarget: Redirecting: Closest waypoint is wp[" + nearestIdx + "]=" + wps[nearestIdx] + " length to currTarget  =" + nearestLength);

        for (int loopi = 0; loopi <= nearestIdx; loopi++) {
            Machinetta.Debugger.debug(1, "redirectPathToTarget: Redirecting: Adding waypoint wp[" + loopi + "]=" + wps[loopi] + " to redirected path");
            nextPath2.addPoint(wps[loopi]);
        }
        toVector = uavTarget.toVector(wps[nearestIdx]);
        if (nearestLength > REDIRECT_APPROACH_DISTANCE_METERS) {
            toVector.setLength(nearestLength - REDIRECT_APPROACH_DISTANCE_METERS);
            Waypoint wp = new Waypoint(wps[nearestIdx].x + toVector.x, wps[nearestIdx].y + toVector.y, wps[nearestIdx].z + toVector.z);
            Machinetta.Debugger.debug(1, "redirectPathToTarget: Redirecting: Adding interpolated waypoint 500m from target, wp=" + wp);
            nextPath2.addPoint(wp);
        }
        Machinetta.Debugger.debug(1, "redirectPathToTarget: Redirecting: Adding target waypoint=" + uavTarget);
        nextPath2.addPoint(uavTarget);

        toVector = uavTarget.toVector(wps[nearestIdx]);
        Waypoint wp = new Waypoint(uavTarget.x + toVector.x, uavTarget.y + toVector.y, uavTarget.z + toVector.z);
        nextPath2.addPoint(wp);
        nextPath = nextPath2;

        Machinetta.Debugger.debug(1, "redirectPathToTarget: Redirecting: Recalculating times");
        RRTPlanner.recalculateTimes(startx, starty, startz, nextPath, simTimePathStart, UAVRI.UAV_SPEED_METERS_PER_SEC);

	// We want to make sure the UAV travels at some
        // specific (slow) speed. Time is in millisceonds.  We
        // need to divide the distance of the segment by the
        // appropriate meters per ms.
        wps = nextPath.getWaypointsAry();
        Waypoint lastWp;
        Waypoint secondLastWp;
        double lastSegmentLength;
        long lastSegmentDurationMs;
        long newWaypointTime;

        lastWp = wps[wps.length - 2];
        secondLastWp = wps[wps.length - 3];
        lastSegmentLength = lastWp.toVector(secondLastWp).length();
        lastSegmentDurationMs = (long) (lastSegmentLength / REDIRECT_APPROACH_SPEED_METERS_PER_MS);
        newWaypointTime = secondLastWp.getTime() + lastSegmentDurationMs;
        Machinetta.Debugger.debug(1, "redirectPathToTarget: Redirecting: Setting second to last waypoint time to " + newWaypointTime);
        lastWp.setTime(newWaypointTime);

        lastWp = wps[wps.length - 1];
        secondLastWp = wps[wps.length - 2];
        lastSegmentLength = lastWp.toVector(secondLastWp).length();
        lastSegmentDurationMs = (long) (lastSegmentLength / REDIRECT_APPROACH_SPEED_METERS_PER_MS);
        newWaypointTime = secondLastWp.getTime() + lastSegmentDurationMs;
        Machinetta.Debugger.debug(1, "redirectPathToTarget: Redirecting: Setting last waypoint time to " + newWaypointTime);
        lastWp.setTime(newWaypointTime);

        nextPath.setAssetID(oldNextPath.getAssetID());
        Machinetta.Debugger.debug(1, "redirectPathToTarget: Finished redirecting path to target at " + currTarget + ", nextPath=" + nextPath);
    }

    private long lastFilterPanelShow = System.currentTimeMillis();
    private int timeBetweenFilterPanelUpdatesMs = 3000;

    private void updateRRTPanels() {
	// @TODO: this code should be moved
        // elsewhere... miniworldstate?  Or maybe just make
        // miniworldstate observable, have autopilot set the
        // currpath and then have rrtplanner panels observe
        // and update themselves?
        long now = System.currentTimeMillis();
        if (now >= (lastFilterPanelShow + timeBetweenFilterPanelUpdatesMs)) {
            lastFilterPanelShow = now;
            Vector3D loc = autopilot.getCurrLocation();
            RRTPlanner.updateRRTPanels(loc.x, loc.y, costMaps, autopilot.getCurrPath(), null);
        }
    }

    private void planNextPath() {
        planNextPath(false);
    }

    private void planNextPath(boolean useCurrLoc) {
        long afterPlanTime = 0;
        long elapsedPlanTime = 0;

        if (costMaps.size() > 0) {
            Machinetta.Debugger.debug(1, "planNextPath: 1 Have " + costMaps.size() + " cost maps, planning!");
            long beforePlanTime = System.currentTimeMillis();

            // @TODO: Most of the time we're going to start to plan a
            // path for later on - i.e. when the current plan is over.
            // At some points however, we're going to start a plan for
            // _as soon as possible_, i.e. when currPath is null, or
            // when we want to get rid of current path (conflict, new
            // role).
            //
            // When this happens, we're going to call plan() at time
            // t, return 500ms later with a plan for starting at time
            // t, yet we're already at time t+500.
            //
            // As a temporary hack (temporary I says - I'm not going
            // to even TELL you how long ago I added this comment),
            // when we're using the currloc, (either because we're
            // forced with the method param or because currPath is
            // null) we set simTimePathStart to now+500ms - which
            // still isn't ideal because our planner isn't 'anytime'.
            // (But we could make it anytime, and maybe we should.)
            //
            // currPath will be null when we're at the start of a run,
            // when we find a conflict for the currPath (i.e. we get a
            // conflict report after the new path has moved from
            // nextPath to currPath) and also any other time we set
            // currPath to null, like when we get a new role.
            // @TODO: the values for duration, length, size, should be configurable.
            int replanCount = 0;
            Path3D newPath = null;
            long simTimePathStart = 0;
            double startx = 0;
            double starty = 0;
            double startz = 0;

            while (true) {
		    // Move this inside the loop because right now
                // we're getting problems where we keep getting
                // short paths...
                if (hackDirectPath) {
                    newPath = hackDirectPath();
                    long newPathDuration = newPath.getDuration();
                    double newPathLength = newPath.getLength();
                    int newPathSize = newPath.size();
                    Machinetta.Debugger.debug(1, "planNextPath: direct path after plan(), " + replanCount + " size=" + newPathSize + ", duration=" + newPathDuration + ", length=" + newPathLength + ", path = " + newPath);
                    break;
                }

                    // @TODO: Change this to use
                // autopilot.getNextPathStartPoint() - note, it
                // may be trickier than it looks.
                if (autopilot.getCurrPath() == null || useCurrLoc) {
                    simTimePathStart = SimTime.getEstimatedTime() + EXPECTED_TIME_TO_PLAN;
                    Vector3D loc = autopilot.getCurrLocation();
                    startx = loc.x;
                    starty = loc.y;
                    startz = loc.z;
                } else {
                    Waypoint end = autopilot.getCurrPath().getEndPoint();
                    if (null != end) {
                        simTimePathStart = end.getTime();
                        startx = end.x;
                        starty = end.y;
                        startz = end.z;
                    } else {
                        simTimePathStart = SimTime.getEstimatedTime() + EXPECTED_TIME_TO_PLAN;
                        Vector3D loc = autopilot.getCurrLocation();
                        startx = loc.x;
                        starty = loc.y;
                        startz = loc.z;
                    }
                }

                Machinetta.Debugger.debug(1, "planNextPath: 2 Before calling plan(), current simtime=" + SimTime.getEstimatedTime() + ", wall clock time=" + beforePlanTime + ", path start at " + fmt2.format(startx) + "," + fmt2.format(starty) + "," + fmt2.format(startz) + " useCurrLoc as start=" + useCurrLoc + ", currPath " + (autopilot.getCurrPath() == null ? "IS" : "IS NOT") + " null.");

		    // First compute the likely elapsed time (probably should know this
                // from the path)
                long time = SimTime.getEstimatedTime();
                for (CostMap cm : costMaps) {
                    cm.timeElapsed(time);
                }

                newPath = RRTPlanner.plan(startx, starty, startz, simTimePathStart, costMaps, UAVRI.RRT_MAX_PATH_METERS, UAVRI.UAV_SPEED_METERS_PER_SEC);
                Stats.pathsGenerated(1);

                long newPathDuration = newPath.getDuration();
                double newPathLength = newPath.getLength();
                int newPathSize = newPath.size();

                Machinetta.Debugger.debug(1, "planNextPath: 3 after plan(), " + replanCount + " size=" + newPathSize + ", duration=" + newPathDuration + ", length=" + fmt2.format(newPathLength) + ", path = " + newPath);
                boolean isShort = false;
                if (newPathDuration < 5000) {
                    Machinetta.Debugger.debug(1, "planNextPath: 3a SHORT PATH duration = " + newPathDuration);
                    isShort = true;
                }
                // @todo: this should come from MIN_PATH_LENGTH in config file.
                if (newPathLength < UAVRI.PATHPLANNER_MIN_PATH_LENGTH) {
                    Machinetta.Debugger.debug(1, "planNextPath: 3b SHORT PATH length = " + newPathLength);
                    isShort = true;
                }
                if (newPathSize < 3) {
                    Machinetta.Debugger.debug(1, "planNextPath: 3c SHORT PATH size = " + newPathSize);
                    isShort = true;
                }

                if (!isShort) {
                    break;
                }
                Stats.pathsGeneratedShort(1);

                replanCount++;
                if (replanCount < MAX_REPLAN_COUNT) {
                    Machinetta.Debugger.debug(1, "planNextPath: 3d newPath is SHORT, replanning for the " + replanCount + " time");
                } else {
                    Machinetta.Debugger.debug(1, "planNextPath: 3e newPath is SHORT , but replanCount (" + replanCount + ") >= MAX_REPLAN_COUNT (" + MAX_REPLAN_COUNT + ") so giving up until next time through the loop.");
                    return;
                }
            }

            nextPath = newPath;
            Stats.pathsGeneratedGood(1);

            // This is temporarily taken out because it destroys the times on the paths.
            //ProxyID id = nextPath.getAssetID();
            //nextPath = RRTSmooth.smoothPerpShift(nextPath, costMaps, 1.0);
            if (UAVRI.PATHPLANNER_SMOOTH_GRAD_DESC_ON) {
                Machinetta.Debugger.debug(1, "planNextPath: Path before smoothing=" + nextPath);
                Path3D oldNextPath = nextPath;
                nextPath = RRTSmooth.smoothGradDesc(nextPath, costMaps, 1.0);
                nextPath.setAssetID(oldNextPath.getAssetID());
                RRTPlanner.recalculateTimes(startx, starty, startz, nextPath, simTimePathStart, UAVRI.UAV_SPEED_METERS_PER_SEC);
                Machinetta.Debugger.debug(1, "planNextPath: Path after smoothing=" + nextPath);
            }
            if (UAVRI.PATHPLANNER_REDIRECT_PATH_TO_TARGET && null != currTarget) {
                redirectPathToTarget(startx, starty, startz, simTimePathStart);
            }
            afterPlanTime = System.currentTimeMillis();
            elapsedPlanTime = afterPlanTime - beforePlanTime;

        } else {
            Machinetta.Debugger.debug(1, "planNextPath: Nothing to plan, no cost maps");
            return;
        }

	// @TODO: Because we clone nextPath here, just below when we
        // call approvePath we also set nextPath to approved - is
        // there anywhere else we have to fix this as well?
        PlannedPath pp = new PlannedPath(new Path3D(nextPath), (new ProxyState()).getSelf().getProxyID());
        nextPathID = pp.getID();

        // Finally get approval, if required
        if (isRequireApproval()) {

            boolean approved = false;
            if (dynamicFlyZones) {
                approved = dfz.checkFlyZone(pp);
            } else {
                approved = true;
            }

            // @todo: If dfz.checkFlyZone() returned false, then we
            // don't do this now... so when do we do it?
            if (approved) {
                pathApproved(pp);
		// When we created PlannedPath above we used the Path3D copy
                // constructor to clone nextPath, to avoid synchronization
                // issues when the comms layer serializes the path.  So we
                // need to set both approved here.
                nextPath.setApproved(true);
            }
        }

        long curTimeAtEnd = -1;
        long nextTimeAtEnd = -1;
        double nextPathLength = nextPath.getLength();
        if (null != autopilot.getCurrPath()) {
            curTimeAtEnd = autopilot.getCurrPath().getTimeAtEnd();
            nextTimeAtEnd = nextPath.getTimeAtEnd();
        }
        if (nextPath != null) {
            if (nextPath.size() <= 3) {
                Machinetta.Debugger.debug(3, "planNextPath: 4a SHORT PATH!  now simtime=" + SimTime.getEstimatedTime() + " path id = " + nextPathID + ", path=" + nextPath);
            }
        }
	// NOTE: when CPU gets tight, if we print out the 'next path'
        // AFTER giving it to the autopilot, the autopilot might
        // remove a waypoint while we're trying to print the path -
        // resulting in a ConcurrentModificationException.  So print
        // it _before_ giving it to autopilot.
        Machinetta.Debugger.debug(1, "planNextPath: 5 returning, now simtime=" + SimTime.getEstimatedTime() + ", current plan expected end= " + curTimeAtEnd + ", next plan expected end= " + nextTimeAtEnd + ", wall clock time=" + afterPlanTime + ", elapsed=" + elapsedPlanTime + " nextpath length=" + nextPathLength + ", nextpath id = " + nextPathID + ", path=" + nextPath);
        autopilot.setNextPath(nextPath, nextPathID);

        Machinetta.Debugger.debug(1, "                Path reward: " + nextPath.getScore());
    }

//     /**
//      * Will likely eventually want more nav detail here.
//      */
//     public void updateLocation(double x, double y, double z) {
//         Command com = new Command(CommandType.UPDATE_LOCATION);
//         com.x = x;
//         com.y = y;
//         com.z = z;
//         commandQueue.add(com);
//     }
    public void forceReplan(String forceReplanReason) {
        Command com = new Command(CommandType.FORCE_REPLAN);
        com.forceReplanReason = forceReplanReason;
        commandQueue.add(com);
    }

    private void forceReplan(Command com) {
        if (!autopilot.getFirstUpdate()) {
            Machinetta.Debugger.debug(1, "Replan forced, reason=" + com.forceReplanReason);
            autopilot.cancelCurrPath();
        } else {
            Machinetta.Debugger.debug(1, "Replan forced, reason=" + com.forceReplanReason + ", but haven't gotten first update yes so ignoring it.");
        }
    }

    /**
     * This access allows the RI to add arbitrary cost maps, e.g., for avoiding
     * obstacles.
     */
    public void addCostMap(CostMap map) {
        Command com = new Command(CommandType.ADD_COST_MAP);
        com.map = map;
        commandQueue.add(com);
    }

    private void addCostMap(Command com) {
        CostMap map = com.map;
        Machinetta.Debugger.debug(1, "Adding cost map of type " + map.getClass().getName());
        costMaps.add(map);
    }

    private void removeCostMapByRole(BasicRole r) {
        String id = r.getID().toString();
        ArrayList<CostMap> cmList = costMapDirectory.get(id);
        if (cmList != null) {
            for (CostMap cm : cmList) {
                costMaps.remove(cm);
            }
            costMapDirectory.remove(id);
        } else {
            Machinetta.Debugger.debug(1, "CostMap Removed " + id + " (no costmap found)");
        }
    }

    private void addCostMapByRole(BasicRole r, CostMap map) {
        String id = r.getID().toString();

        ArrayList<CostMap> cmList = costMapDirectory.get(id);
        if (null == cmList) {
            cmList = new ArrayList<CostMap>();
        }
        cmList.add(map);
        costMaps.add(map);
        costMapDirectory.put(id, cmList);
        Machinetta.Debugger.debug(1, "CostMap Added for " + id);
    }

    public void removeCostMap(CostMap map) {
        Command com = new Command(CommandType.REMOVE_COST_MAP);
        com.map = map;
        commandQueue.add(com);
    }

    private void removeCostMap(Command com) {
        CostMap map = com.map;
        costMaps.remove(map);
        Machinetta.Debugger.debug(1, "CostMap Removed for map " + map);
    }

    public void addRole(BasicRole r) {
        Machinetta.Debugger.debug(1, "Adding role to command queue: " + r + ", queue size: " + commandQueue.size());
        Machinetta.Debugger.debug(1, "    queue info: " + queueInfo());
        Command com = new Command(CommandType.ADD_ROLE);
        com.role = r;
        commandQueue.add(com);
    }

    private void addRole(Command com) {
        BasicRole r = com.role;

        Machinetta.Debugger.debug(1, "Before role addition roles are: " + roles);
        synchronized (roles) {
            roles.add(r);
        }
        Machinetta.Debugger.debug(1, "After role addition roles are: " + roles);

        MixGaussiansCostMap mgcm;
        Rectangle area = null;
        int destx = 0;
        int desty = 0;
        double mapScale = UAVRI.MAP_WIDTH_METERS / 1000;

        // Add a cost map
        switch (r.getType()) {

            case scan: {
                Machinetta.Debugger.debug(1, "Adding cost map for scan role");
                SimpleStaticCostMap cm = new SimpleStaticCostMap();
                Area scanArea = (Area) r.params.get("Area");
                Rectangle scanRect = new Rectangle((int) scanArea.getX1(), (int) scanArea.getY1(), (int) scanArea.getWidth(), (int) scanArea.getHeight());
                // Negative cost, because we actually want to go to the scan area
                cm.addCostRect(scanRect, -100.0);
                addCostMapByRole(r, cm);
            }
            break;

            case geolocateSense: {
                Machinetta.Debugger.debug(1, "Adding cost map for geolocateSense role");
                GeoLocateRequest request = (GeoLocateRequest) r.params.get("request");
                mgcm = new MixGaussiansCostMap();
                destx = (int) request.longtitude;
                desty = (int) request.latitude;
// 		mgcm.addGaussian(destx, desty, -10 * mapScale, 1.0 * mapScale);
// 		mgcm.addGaussian(destx, desty, -5 * mapScale, 20.0 * mapScale);
// 		mgcm.addGaussian(destx, desty, -100, 100*mapScale);
                mgcm.addGaussian(destx, desty, -15, 50 * mapScale);
                mgcm.addGaussian(destx, desty, -10, 500 * mapScale);

                //		mgcm.addGaussian(destx, desty, -60 * mapScale, 4.0 * mapScale);
                //		mgcm.addGaussian(destx, desty, -40 * mapScale, 10.0 * mapScale);
                //		mgcm.addGaussian(destx, desty, -20 * mapScale, 20.0 * mapScale);
                //		mgcm.addGaussian(destx, desty, -10 * mapScale, 40.0 * mapScale);
                Machinetta.Debugger.debug(1, "Created gaussian costmap centered on " + destx + "," + desty + " for geolocateSense");
                addCostMapByRole(r, mgcm);
                currTarget = new Waypoint(destx, desty, 0);
                // @TODO: Hack hack hack;
                hackDirectPath = true;
            }
            break;

            case move: {
                Machinetta.Debugger.debug(1, "Adding cost map for move role");
                SimpleStaticCostMap cm = new SimpleStaticCostMap();
                Point dest = (Point) r.params.get("Destination");
                area = new Rectangle(dest.x - 100, dest.y - 100, 200, 200);
                // Negative cost, because we actually want to go to the scan area
                cm.addCostRect(area, -20.0);
                addCostMapByRole(r, cm);

                currTarget = new Waypoint(dest.x, dest.y, 0);
            }
            break;

            case hold: {
                Machinetta.Debugger.debug(1, "Adding cost map for hold role");
                Vector3D loc = autopilot.getCurrLocation();
                SimpleStaticCostMap cm = new SimpleStaticCostMap();
                cm.addCostRect(new Rectangle((int) (loc.x - 10), (int) (loc.y - 10), 20, 20), -100.0);
                addCostMapByRole(r, cm);
            }
            break;

            case attackFromAirOrGround: {
                Machinetta.Debugger.debug(1, "Adding cost map for attack from air or ground role");
                SimpleStaticCostMap cm = new SimpleStaticCostMap();
                Vector3D targetLoc = (Vector3D) r.params.get("Location");
                cm.addCostRect(new Rectangle((int) (targetLoc.x - 2000), (int) (targetLoc.y - 2000), 4000, 4000), -100.0);
                addCostMapByRole(r, cm);
                currTarget = new Waypoint(targetLoc.x, targetLoc.y, 0);
            }
            break;

            case attackFromAir: {
                Machinetta.Debugger.debug(1, "Adding cost map for attack from air role");
                SimpleStaticCostMap cm = new SimpleStaticCostMap();
                Vector3D targetLoc = (Vector3D) r.params.get("TargetLocation");
                cm.addCostRect(new Rectangle((int) (targetLoc.x - 2000), (int) (targetLoc.y - 2000), 4000, 4000), -100.0);
                addCostMapByRole(r, cm);
                currTarget = new Waypoint(targetLoc.x, targetLoc.y, 0);
            }
            break;

            case EOImage: {
                Machinetta.Debugger.debug(1, "Adding cost map for EOImage");
                mgcm = new MixGaussiansCostMap();
                imageLocation = (java.awt.Point) r.params.get("ImageLocation");
                if (imageLocation != null) {
                    // Original
                    //		mgcm.addGaussian(imageLocation.x, imageLocation.y, -200, 50);
                    // WAY to big
                    //		mgcm.addGaussian(imageLocation.x, imageLocation.y, -100, 5000);
                    destx = imageLocation.x;
                    desty = imageLocation.y;
                    mgcm.addGaussian(destx, desty, -80 * mapScale, 1.0 * mapScale);
                    mgcm.addGaussian(destx, desty, -60 * mapScale, 4.0 * mapScale);
                    mgcm.addGaussian(destx, desty, -40 * mapScale, 10.0 * mapScale);
                    mgcm.addGaussian(destx, desty, -20 * mapScale, 20.0 * mapScale);
                    mgcm.addGaussian(destx, desty, -10 * mapScale, 40.0 * mapScale);
                    Machinetta.Debugger.debug(1, "Created gaussian costmap centered on " + destx + "," + desty + " for EOImage");
                    addCostMapByRole(r, mgcm);
                    //preferredPathLengthExponential = RRTPlanner.DEFAULT_NODE_DIST_COST_EXPONENTIAL_LONG_PATH;
                    currTarget = new Waypoint(destx, desty, 0);
                } else {
                    Machinetta.Debugger.debug(3, "No image location with EOImage request");
                }
            }
            break;

            case patrol: {
                Machinetta.Debugger.debug(1, "Adding cost map for patrol role");
                // cm = new SimpleStaticCostMap();
                Area patrolArea = (Area) r.params.get("Area");
                currTarget = new Waypoint(patrolArea.getCenterX(), patrolArea.getCenterY(), 0);
                areaToBeIn = patrolArea;
                hackDirectPath = true;
		    // Negative cost, because we actually want to go to the patrol area
                // cm.addCostRect(patrolArea, -100.0);
                // addCostMapByRole(r,cm);

                SimpleStaticCostMap sscm = new SimpleStaticCostMap();
                java.awt.Rectangle patrolRect = new java.awt.Rectangle((int) patrolArea.x1, (int) patrolArea.y1, (int) patrolArea.getWidth(), (int) patrolArea.getHeight());
                sscm.addCostRect(patrolRect, -200);
                addCostMapByRole(r, sscm);
// 		    mgcm = new MixGaussiansCostMap();
// 		    mgcm.addGaussian((int)patrolArea.getCenterX(), (int)patrolArea.getCenterY(), -200);
// 		    Machinetta.Debugger.debug(1, "Created gaussian costmap centered on "+(int)patrolArea.getCenterX() + " " + (int)patrolArea.getCenterY()+" for Patrol");
// 		    addCostMapByRole(r,mgcm);
                //preferredPathLengthExponential = RRTPlanner.DEFAULT_NODE_DIST_COST_EXPONENTIAL_LONG_PATH;
                forceReplan("patrol roles need direct path");
            }
            break;

            case intelSurveilRecon: {
		    // @TODO: We _could_ just send a patrol message,
                // much like the camera command - but doing it
                // this way lets us make use of the path planning
                // and the conflict avoidance, etc.

                // this request is stored when the geolocate plan is created
                //  unpack the request and make an area to surveil
                GeoLocateRequest request = (GeoLocateRequest) r.params.get("request");
                destx = (int) request.longtitude;
                desty = (int) request.latitude;
                Area isrArea = new Area( (destx - 100), (desty-100), (destx+100), (desty+100) );

                currTarget = new Waypoint(isrArea.getCenterX(), isrArea.getCenterY(), 0);
                areaToBeIn = isrArea;
                hackDirectPath = true;

                SimpleStaticCostMap sscm = new SimpleStaticCostMap();
                //		    java.awt.Rectangle isrRect = new java.awt.Rectangle((int)isrArea.x1, (int)isrArea.y1, (int)isrArea.getWidth(), (int)isrArea.getHeight());
                int botleftx;
                int botlefty;
                if (isrArea.x1 < isrArea.x2) {
                    botleftx = (int) isrArea.x1;
                } else {
                    botleftx = (int) isrArea.x2;
                }
                if (isrArea.y1 < isrArea.y2) {
                    botlefty = (int) isrArea.y1;
                } else {
                    botlefty = (int) isrArea.y2;
                }
                java.awt.Rectangle isrRect = new java.awt.Rectangle(botleftx, botlefty, (int) isrArea.getWidth(), (int) isrArea.getHeight());
                sscm.addCostRect(isrRect, -200);
                Debugger.debug(1, "Adding cost map rect corner " + botleftx + "," + botlefty + " width/height " + (int) isrArea.getWidth() + ", " + (int) isrArea.getHeight() + " for intelSurveilRecon role");
                addCostMapByRole(r, sscm);
// 		    mgcm = new MixGaussiansCostMap();
// 		    // Negative cost, because we actually want to go to the isr area
// 		    mgcm.addGaussian((int)isrArea.getCenterX(), (int)isrArea.getCenterY(), -200);
// 		    Machinetta.Debugger.debug(1, "For ISR role created gaussian costmap centered on "+(int)isrArea.getCenterX() + " " + (int)isrArea.getCenterY());
// 		    addCostMapByRole(r,mgcm);
                forceReplan("intelSurveilRecon role needs direct path");
            }
            break;

            default:
                Machinetta.Debugger.debug(3, "No cost map added for role of type: " + r.getType());
        }
    }

    public void removeRole(BasicRole r) {
        Command com = new Command(CommandType.REMOVE_ROLE);
        com.role = r;
        commandQueue.add(com);
    }

    private void removeRole(Command com) {
        BasicRole r = com.role;

        synchronized (roles) {
            roles.remove(r);
        }
        removeCostMapByRole(r);
        Machinetta.Debugger.debug(1, "After role removal roles are: " + roles);

        // HACK: Turn off direct paths.
        switch (r.getType()) {
            case geolocateSense:
                hackDirectPath = false;
                break;
            case intelSurveilRecon:
                hackDirectPath = false;
                break;
        }

    }

    private boolean isRequireApproval() {
        return requireApproval;
    }

    public void setRequireApproval(boolean requireApproval) {
        Command com = new Command(CommandType.SET_REQUIRE_APPROVAL);
        com.requireApproval = requireApproval;
        commandQueue.add(com);
    }

    private void setRequireApproval(Command com) {
        this.requireApproval = com.requireApproval;
    }

    public void stateChanged(BeliefID[] b) {
// 	for(int loopi = 0; loopi < b.length; loopi++) {
//             Belief bel = state.getBelief(b[loopi]);
// 	    Machinetta.Debugger.debug(1,"stateChanged: Belief class "+bel.getClass().getName()+" = "+bel.toString());
// 	}
        Command com = new Command(CommandType.STATE_CHANGED);
        com.belief = b;
        commandQueue.add(com);
    }

    private void stateChanged(Command com) {
        BeliefID[] b = com.belief;

        for (int i = 0; i < b.length; i++) {
            Belief bel = state.getBelief(b[i]);
            if (bel instanceof AssetStateBelief) {
                state.removeBelief(b[i]);
            } else if (bel instanceof PlannedPath) {
                PlannedPath path = (PlannedPath) bel;
                if (path.conflicted != null) {
                    if (nextPathID != null && path.originalPlannedPathID.equals(nextPathID)) {
                        Stats.conflictsFound(1);
                        Stats.conflictsFoundNextPath(1);
                        Machinetta.Debugger.debug(1, "Got information about own planned path");
                        PlannedPath ourPath = (PlannedPath) state.getBelief(b[i]);
                        if (ourPath.conflicted != null) {
                            Machinetta.Debugger.debug(1, "stateChanged: Simtime=" + SimTime.getEstimatedTime() + " FOUND CONFLICT MESSAGE for nextPath, conflict ID= " + ourPath.getID() + ", original plannedPath ID= " + ourPath.originalPlannedPathID + " conflict detected by " + ourPath.conflictDetectedBy + " for path from " + ourPath.conflicted.getAssetID() + ", conflicted path=" + ourPath.conflicted + ", setting nextPath to null to cause replan, cur loc=" + autopilot.getCurrXYZString());
                            // Add the path to the obstacle avoidance map
                            otherVehicleCostMap.addPath(ourPath.conflicted);
                            nextPath = null;
                        }
                    } else if (currPathID != null && path.originalPlannedPathID.equals(currPathID)) {
			// @TODO: Move conflict detection/response
                        // somewhere else?  Maybe somewhat higher
                        // level.  Detect the conflict and then just
                        // send a command to PathPlanner to throw away
                        // curpath and create a new one.
                        Stats.conflictsFound(1);
                        Stats.conflictsFoundCurrPath(1);
                        Machinetta.Debugger.debug(1, "Got information about own planned path");
                        PlannedPath ourPath = (PlannedPath) state.getBelief(b[i]);
                        if (ourPath.conflicted != null) {
                            Machinetta.Debugger.debug(1, "stateChanged: Simtime=" + SimTime.getEstimatedTime() + " FOUND CONFLICT MESSAGE for currPath, conflict ID= " + ourPath.getID() + ", original plannedPath ID= " + ourPath.originalPlannedPathID + " conflict detected by " + ourPath.conflictDetectedBy + " for path from " + ourPath.conflicted.getAssetID() + ", conflicted path=" + ourPath.conflicted + ", setting currPath to null to cause replan from cur loc=" + autopilot.getCurrXYZString());
                            // Add the path to the obstacle avoidance map
                            otherVehicleCostMap.addPath(ourPath.conflicted);
                            autopilot.cancelCurrPath();
                        }
                    }
                } else if (!path.owner.equals(state.getSelf().getProxyID())) {
                    // Add it to obstacle avoidance cost map
                    Machinetta.Debugger.debug(1, "Got " + path.owner + "'s planned path");
                    if (UAVRI.OTHER_VEHICLE_COSTMAP_ON) {
                        otherVehicleCostMap.addPath(path.path);
                    }

                    // @todo Check of any known conflicts
                    // Might need to do this on a thread to avoid communications problems
                }
            } else if (bel instanceof TDOACoordCommand) {

                TDOACoordCommand tb = (TDOACoordCommand) bel;

                if (tb.getTarget().equals(state.getSelf().getProxyID())) {
                    if (tb.getLoc() != null) {
                        Machinetta.Debugger.debug(5, "Got TDOACoordCommand with new location, resetting currTarget and forcing replan");
                        // Command was a new location
                        currTarget = new Waypoint(tb.getLoc().getX(), tb.getLoc().getY(), 0);
                        Command comNew = new Command(CommandType.FORCE_REPLAN);
                        commandQueue.add(comNew);
                    }

                    if (tb.getSenseTime() > 0) {
                        Machinetta.Debugger.debug(5, "Got TDOACoordCommand with senseTime, doing geolocate");
                        // Command was a time to take a sensor reading
                        // Being lazy, assuming *now*
                        doGeolocate();
                    }
                }

                /*} else if (bel instanceof RSSIReading) {
                 // ignore
                 } else if (bel instanceof UAVLocation) {
                 // ignore
                 } else if (bel instanceof RAPBelief) {
                 // ignore
                 } else if (bel instanceof Associates) {
                 // ignore
                 } else if (bel instanceof RoleAllocationBelief) {
                 // ignore
                 } else if (bel instanceof TeamBelief) {
                 // ignore
                 } else if (bel instanceof TeamPlanBelief) {
                 // ignore
                 */
            } else if (bel instanceof FlyZone) {
                dfz.checkFlyZoneResponse((FlyZone) bel);
            } else {
                /* Shouldn't care about this, there will be lots of random things turning up
                 
                 // @TODO: From Sean: Paul, while it's true that we're
                 // going to get lots of random things, I find it very
                 // helpful in debugging to flag the new/unexpected ones,
                 // which is why I added this here.
                 
                 // @TODO: From Paul: Sean, while that is fair enough, while debugging normal runs,
                 // it generates huge volumes of noise into a file that we are trying to use to
                 // understand what is going on.   Especially at level 3, which should be a warning
                 // that something is going on - you can't claim ignorance, now I have it in debugger
                 *
                 if (bel != null) {
                 Machinetta.Debugger.debug(3, "Unknown class: "+bel.getClass().getName()+" for new belief: "+bel);
                 } else {
                 Machinetta.Debugger.debug(3, "Null belief passed into stateChanged?");
                 }
                 */
            }
        }
    }

    private void doGeolocate() {
        Machinetta.Debugger.debug(1, "NEED TO GEOLOCATE!!!!");

        // Ugly, but assuming impending rewrite of the PathPlanner not going to change ....
        BasicRole role = null;
        for (BasicRole roleC : roles) {
            if (roleC.getType() == TaskType.geolocateSense) {
                role = roleC;
            }
        }

        GeoLocateRequest request = (GeoLocateRequest) role.params.get("request");
        // @TODO: hack hack hac
        // Taken out by Paul
        // hackDirectPath = false;
        // Then request a geolocation
        GeoLocatePA msg = new GeoLocatePA();
        msg.bandwidth = request.bandwidth;
        msg.emitterID = request.emitterID;
        msg.frequency = request.frequency;

        //  @todo Fix up this ugly hack ...
        ((UAVRI) rapInt).currentGLID = (String) ((Hashtable) role.getParams()).get("ScanDataLabel");
        ((UAVRI) rapInt).currentGLBID = ((Belief) ((Hashtable) role.getParams()).get("request")).getID();

        // Machinetta.Debugger.debug("Set GLID: " + currentGLID + " from " + role.getParams(), 1, this);
        rapInt.sendMessage(msg);

        // Mark as sent
        request.sentToRAPAt = System.currentTimeMillis();
    }
}
