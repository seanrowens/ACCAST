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

import AirSim.Machinetta.Beliefs.ProxyEventData;
import AirSim.Machinetta.Beliefs.AssetStateBelief;
import AirSim.Machinetta.Beliefs.VehicleBelief;
import AirSim.Machinetta.Beliefs.ProxyEventData.EventType;
import Gui.LatLonUtil;
import AirSim.Environment.Assets.Tasks.TaskType;
import AirSim.Environment.Vector3D;
import AirSim.Environment.Area;
import AirSim.Environment.Waypoint;
import AirSim.Machinetta.PlannedPath;
import AirSim.Machinetta.Beliefs.GeoLocateRequest;
import AirSim.Machinetta.Beliefs.FlyZone;
import AirSim.Machinetta.Beliefs.RSSIReading;
import AirSim.Machinetta.Beliefs.TDOACoordCommand;
import AirSim.Machinetta.Beliefs.UAVLocation;
import AirSim.Machinetta.CostMaps.CostMap;
import AirSim.Machinetta.CostMaps.MixGaussiansCostMap;
import AirSim.Machinetta.CostMaps.OtherVehicleCostMap;
import AirSim.Machinetta.CostMaps.SimpleStaticCostMap;
import AirSim.Machinetta.Messages.CameraCommandPA;
import AirSim.Machinetta.Messages.GeoLocatePA;
import AirSim.Machinetta.Messages.NextWaypointPA;
import AirSim.Machinetta.Messages.PRMessage;
import Machinetta.Debugger;
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

/**
 *
 * @author pscerri
 */
public class SimplePlanner implements Runnable, StateChangeListener {
    
    private enum CommandType {NONE, ADD_ROLE, REMOVE_ROLE,STATE_CHANGED};
    
    private class Command {
        public CommandType command=CommandType.NONE;
        BasicRole role = null;
        ArrayList<Belief> beliefs=null;
        public Command(CommandType type) {
            command = type;
        }
        public String toString() {
            return "Com: "+command+", role="+role;
        }
    }
    private BlockingQueue<Command> commandQueue = new LinkedBlockingQueue<Command>();

    private RAPInterfaceImplementation rapInt = null;
    private ProxyState state = new ProxyState();
    {
        state.addChangeListener(this);
    }

    private ProxyID proxyID = null;
    private ProxyID getProxyID() {
	if (proxyID == null) {
	    while (state.getSelf() == null) {
		Debugger.debug(3,"WARNING No self in beliefs!  Sleeping for 100ms and then trying again");
		try { Thread.sleep(100); } catch (InterruptedException e) {}
	    }
	    if (state.getSelf() == null) {
		Debugger.debug(5,"No self in beliefs!! - exiting");
		System.exit(-1);
	    }
	    proxyID = state.getSelf().getProxyID();
	}
	return proxyID;
    }


    private List<BasicRole> roles = Collections.synchronizedList(new ArrayList<BasicRole>());
    
    private Thread myThread;

    private GC gc = new GC();

    public SimplePlanner(RAPInterfaceImplementation rapInt) {
	this.rapInt = rapInt;
	state = new Machinetta.State.ProxyState();
	myThread = new Thread(this);
    }

    public void start() {
	myThread.start();
	gc.start();
    }

    public void addRole(BasicRole r) {
        Machinetta.Debugger.debug(1, "Adding role to command queue: " + r + ", queue size: " + commandQueue.size());
        Command com = new Command(CommandType.ADD_ROLE);
        com.role = r;
        commandQueue.add(com);
    }

    public void removeRole(BasicRole r) {
        Command com = new Command(CommandType.REMOVE_ROLE);
        com.role = r;
        commandQueue.add(com);
    }
    
    public void stateChanged(BeliefID[] b) {
        Command com = new Command(CommandType.STATE_CHANGED);

	ArrayList<Belief> beliefs = new ArrayList<Belief>(b.length);

        for (int i = 0; i < b.length; i++) {
            Belief bel = state.getBelief(b[i]);

	    if (bel instanceof AssetStateBelief) {
		gc.add(b[i]);
	    }
	    else if (bel instanceof VehicleBelief) {
		gc.add(b[i]);
	    } else
		if (bel instanceof RSSIReading) {
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
	    }
	    else 
		beliefs.add(bel);
	}

        com.beliefs = beliefs;
        commandQueue.add(com);
    }

    private void handleProxyEventData(ProxyEventData ped) {
	ArrayList<BasicRole> done = new ArrayList<BasicRole>();
	for (BasicRole role: roles) {
	    Debugger.debug(0, "Checking for todo: " + role);
	    switch(role.getType()) {

	    case attackFromAirOrGround:
	    case attackFromAir: {
	    }
		break;
	    case move: {
	    }
		break;
	    case attack: {
	    }
		break;
	    case patrol: {
	    }
		break;
	    case hold: {
	    }
		break;
	    case transport: {
	    }
		break;
	    case airdrop: {
		if(ped.proxyID.equals(getProxyID())) {
		    if(EventType.DISMOUNTED_CONTENTS == ped.type) {
			// ignore.
		    }
		    else if(EventType.LANDED == ped.type) {
			done.add(role);
		    }
		}
	    }
		break;
	    case intelSurveilRecon: {
	    }
		break;
	    case deploy: {
	    }
		break;
	    case activate: {
	    }
		break;
	    case deactivate: {
	    }
		break;
	    case detonate: {
	    }
		break;
	    case follow: {
	    }
		break;
	    case spreadOut: {
	    }
		break;
	    case defendGround: {
	    }
		break;
	    case defendAir: {
	    }
		break;
	    case coverFromAir: {
	    }
		break;
	    case coverFromGround: {
	    }
		break;
	    case retreat: {
	    }
		break;
	    case scoutObserve: {
	    }
		break;
	    case groundSupport: {
	    }
		break;
	    case BDAFromAir: {
	    }
		break;
	    case EOImage: {
	    }
		break;
	    case mount: {
	    }
		break;
	    case dismount: {
	    }
		break;
	    case randomMove: {
	    }
		break;
	    case land: {
	    }
		break;
	    case launch: {
	    }
		break;
	    case directFire: {
	    }
		break;
	    case missileFire: {
	    }
		break;
	    case circle: {
	    }
		break;
	    default:
		// do nothing
	    }
	}
	if (done != null) {
	    roles.removeAll(done);
	}

    }

    private int mb = 1024*1024;
    private void checkMemoryStats() {
	//Getting the runtime reference from system
	Runtime runtime = Runtime.getRuntime();
	long totalMem = runtime.totalMemory();
	long freeMem = runtime.freeMemory();
	long maxMem = runtime.maxMemory();
	long usedMem = totalMem - freeMem;
        Machinetta.Debugger.debug(1, "MEM: "
				  +" max mb= "+(maxMem/mb)
				  +" total mb= "+(totalMem/mb)
				  +" free mb = "+(freeMem/mb)
				  +" used mb = "+(usedMem/mb));
    }

    private void stateChanged(Command com) {
	//	checkMemoryStats();
        for (int loopi = 0; loopi < com.beliefs.size(); loopi++) {
            Belief bel = com.beliefs.get(loopi);
	    if (bel instanceof ProxyEventData) {
		handleProxyEventData((ProxyEventData) bel);
            } else if (bel instanceof PlannedPath) {
		// ignore
            } else if (bel instanceof BasicRole) {
		// ignore
	    }
	    else {
                if (bel != null) {
                    Machinetta.Debugger.debug(1, "Unknown class: "+bel.getClass().getName()+" for new belief: "+bel);
                } else {
                    Machinetta.Debugger.debug(1, "Null belief passed into stateChanged?");
                }

            }
        }
    }

    private void processCommandQueue() {
	Command com = null;
	int commandQueueSize =commandQueue.size();
	for(int loopi = 0; loopi < commandQueueSize; loopi++) {
	    try {
		com = commandQueue.take();
	    } catch (InterruptedException ex) {
		Debugger.debug(1, "Pathplanner interrupted");
	    }
	    if(null != com) {
		if(CommandType.ADD_ROLE == com.command) {
		    Debugger.debug(1, "Adding role");
		    roles.add(com.role);
		} else if(CommandType.REMOVE_ROLE == com.command) {
		    Debugger.debug(1, "Removing role");
		    roles.remove(com.role);
		}else if(CommandType.STATE_CHANGED == com.command) {
		    stateChanged(com);
		}
	    }
	}
    }

    public void run() {
        
        boolean commandQueueTooLong = false;
        while(true) {
	    try {

		processCommandQueue();

		if (roles.size() <= 0) {
		    try {Thread.sleep(50); } catch (Exception e) {}
		    continue;
		}

		// @TODO: A lot of the time we're creating a task
		// message and saying our role is done - when it's not
		// really done, the PLANNER is done, but the asset is
		// still carrying out the task/role.  So we have to
		// change this to somehow detect the task is
		// accomplished and then remove the role.  

		Debugger.debug(1, "run: processing role stuff since roles.size() == "+roles.size());
		ArrayList<BasicRole> done = new ArrayList<BasicRole>();
		for (BasicRole role: roles) {
		    Debugger.debug(0, "Checking for todo: " + role);
		    switch(role.getType()) {

		    case attackFromAir: {
			Debugger.debug(1, "Deciding what to do for attack from air");
			Point p = (Point)role.params.get("TargetLocation");
			if (p == null) {
			    Debugger.debug(3, "Could not get target location");
			} else {
			    Debugger.debug(1, "In position to strike target");
			    PRMessage msg = new PRMessage(PRMessage.MessageType.NEW_TASK);
			    msg.params.put("Task", TaskType.attackFromAir);
			    msg.params.put("Location", p);
			    rapInt.sendMessage(msg);
			    done.add(role);
			}
		    }
			break;
		    case move: {
			Debugger.debug(1, "Deciding what to do for 'move'");
			Vector3D loc = (Vector3D)role.params.get("Location");
			Path2D path = (Path2D)role.params.get("Path");

			if ((loc == null) && (path == null)) {
			    Debugger.debug(3, "Could not get move location or path, cannot execute role.");
			} else {
			    PRMessage msg = new PRMessage(PRMessage.MessageType.NEW_TASK);
			    msg.params.put("Task", TaskType.move);
			    if(path != null)
				msg.params.put("Location", path);
			    else
				msg.params.put("Location", loc);
			    rapInt.sendMessage(msg);
			    done.add(role);
			}
		    }
			break;

		    case attackFromAirOrGround:
		    case UGVAttack: {
			Debugger.debug(1, "Deciding what to do for 'UGVAttack'");
			Vector3D loc = (Vector3D)role.params.get("Location");

			if(loc == null) {
			    Debugger.debug(3, "Could not get attack location, cannot execute role.");
			} else {
			    PRMessage msg = new PRMessage(PRMessage.MessageType.NEW_TASK);
			    msg.params.put("Task", TaskType.attack);
			    msg.params.put("Location", loc);
			    rapInt.sendMessage(msg);
			    done.add(role);
			}
		    }
			break;
		    case attack: {
			Debugger.debug(1, "Deciding what to do for 'attack'");
			Vector3D loc = (Vector3D)role.params.get("Location");

			if(loc == null) {
			    Debugger.debug(3, "Could not get attack location, cannot execute role.");
			} else {
			    PRMessage msg = new PRMessage(PRMessage.MessageType.NEW_TASK);
			    msg.params.put("Task", TaskType.attack);
			    msg.params.put("Location", loc);
			    rapInt.sendMessage(msg);
			    done.add(role);
			}
		    }
			break;
		    case patrol: {
			Debugger.debug(1, "Deciding what to do for 'patrol'");
			Area area = (Area)role.params.get("Area");
			    
			if(area == null) {
			    Debugger.debug(3, "Could not get patrol area, cannot execute role.");
			} else {
			    PRMessage msg = new PRMessage(PRMessage.MessageType.NEW_TASK);
			    msg.params.put("Task", TaskType.patrol);
			    msg.params.put("Area", area);
			    rapInt.sendMessage(msg);
			    done.add(role);
			}
		    }
			break;
		    case hold: {
			Debugger.debug(1, "Deciding what to do for 'hold'");
			PRMessage msg = new PRMessage(PRMessage.MessageType.NEW_TASK);
			msg.params.put("Task", TaskType.hold);
			rapInt.sendMessage(msg);
			done.add(role);
		    }
			break;

		    case transport: {
			Debugger.debug(1, "Deciding what to do for 'transport'");
			Vector3D loc = (Vector3D)role.params.get("Location");

			if(loc == null) {
			    Debugger.debug(3, "Could not get transport destination, cannot execute role.");
			} else {
			    PRMessage msg = new PRMessage(PRMessage.MessageType.NEW_TASK);
			    msg.params.put("Task", TaskType.transport);
			    msg.params.put("Location", loc);
			    rapInt.sendMessage(msg);
			    done.add(role);
			}
		    }
			break;
		    case airdrop: {
			Debugger.debug(1, "Deciding what to do for 'airdrop'");
			Vector3D loc = (Vector3D)role.params.get("Location");
			Vector3D base = (Vector3D)role.params.get("Base");
			Double atDestRange = (Double)role.params.get("AtDestRange");

			if(loc == null) {
			    Debugger.debug(3, "Could not get transport destination, cannot execute role.");
			} else {
			    PRMessage msg = new PRMessage(PRMessage.MessageType.NEW_TASK);
			    msg.params.put("Task", TaskType.airdrop);
			    msg.params.put("Location", loc);
			    msg.params.put("Base", base);
			    msg.params.put("AtDestRange", atDestRange);
			    rapInt.sendMessage(msg);
			    done.add(role);
			}
		    }
			break;
		    case intelSurveilRecon: {
			Debugger.debug(1, "Deciding what to do for 'intelSurveilRecon'");
			Area area = (Area)role.params.get("Area");
			    
			if(area == null) {
			    Debugger.debug(3, "Could not get intelSurveilRecon area, cannot execute role.");
			} else {
			    PRMessage msg = new PRMessage(PRMessage.MessageType.NEW_TASK);
			    msg.params.put("Task", TaskType.intelSurveilRecon);
			    msg.params.put("Area", area);
			    rapInt.sendMessage(msg);
			    done.add(role);
			}
		    }
			break;
		    case deploy: {
		    }
			break;
		    case activate: {
		    }
			break;
		    case deactivate: {
		    }
			break;
		    case detonate: {
		    }
			break;
		    case follow: {
		    }
			break;
		    case spreadOut: {
		    }
			break;
		    case defendGround: {
		    }
			break;
		    case defendAir: {
		    }
			break;
		    case coverFromAir: {
		    }
			break;
		    case coverFromGround: {
		    }
			break;
		    case retreat: {
		    }
			break;
		    case scoutObserve: {
		    }
			break;
		    case groundSupport: {
		    }
			break;
		    case BDAFromAir: {
		    }
			break;
		    case EOImage: {
		    }
			break;
		    case mount: {
		    }
			break;
		    case dismount: {
		    }
			break;
		    case randomMove: {
		    }
			break;
		    case land: {
		    }
			break;
		    case launch: {
		    }
			break;
		    case directFire: {
		    }
			break;
		    case missileFire: {
		    }
			break;
		    case circle: {
		    }
			break;

//		    case geolocateSense:
//			break;
//                                    
//		    case attackFromAir:
//			Debugger.debug(1, "Deciding what to do for attack from air");
//			Point p = (Point)role.params.get("TargetLocation");
//			if (p == null) {
//			    Debugger.debug(3, "Could not get target location");
//			} else {
//			    if (autopilot.atWaypoint(p.x,p.y, 10000)) {
//				Debugger.debug(1, "In position to strike target");
//				PRMessage msg = new PRMessage(PRMessage.MessageType.NEW_TASK);
//				msg.params.put("Task", TaskType.attackFromAir);
//				msg.params.put("Location", p);
//				rapInt.sendMessage(msg);
//                                            
//				// @todo Find a better way of stopping roles.
//				removeCostMapByRole(role);
//				if (done == null) done = new ArrayList<BasicRole>();
//				done.add(role);
//			    } else {
//				Debugger.debug(1, "Too far from target: " + p.x + " " + p.y + " -> " + autopilot.getCurrXYString());
//			    }
//			}
//                                    
//			break;
//                                    
//		    case EOImage:
//			// This needs to be adjusted for angles, etc.
//			Debugger.debug(0, "Deciding what to do for EOImage");
//			p = (Point)role.params.get("ImageLocation");
//			if (p == null) {
//			    Debugger.debug(3, "Could not get image location");
//			} else {
//			    if(EOIMAGE_SEND_CAMERA_COMMAND_IMMEDIATELY) {
//				Debugger.debug(1, "INFOTECH: Deciding what to do for EOImage: Sending camera command");
//				CameraCommandPA cMsg = new CameraCommandPA();
//				cMsg.EO = true;
//				cMsg.altitude = 0;
//				cMsg.longtitude = imageLocation.x;
//				cMsg.latitude = imageLocation.y;
//				cMsg.zoom = 1.0;
//				rapInt.sendMessage(cMsg);
//				Debugger.debug(1, "INFOTECH: Deciding what to do for EOImage: Stopping role");
//				// @todo Find a better way of stopping roles.
//				removeCostMapByRole(role);
//				if (done == null) done = new ArrayList<BasicRole>();
//				done.add(role);
//			    } else if (autopilot.atWaypoint(p.x, p.y, UAVRI.EO_DISTANCE_TOLERANCE)) {
//				Debugger.debug(1, "Deciding what to do for EOImage: Sending camera command");
//				CameraCommandPA cMsg = new CameraCommandPA();
//				cMsg.EO = true;
//				cMsg.altitude = 0;
//				cMsg.longtitude = imageLocation.x;
//				cMsg.latitude = imageLocation.y;
//				cMsg.zoom = 1.0;
//				rapInt.sendMessage(cMsg);
//			    } else if (autopilot.atWaypoint(p.x, p.y, UAVRI.EO_DISTANCE_TOLERANCE/2)) {
//				Debugger.debug(1, "Deciding what to do for EOImage: Stopping role");
//				// @todo Find a better way of stopping roles.
//				removeCostMapByRole(role);
//				if (done == null) done = new ArrayList<BasicRole>();
//				done.add(role);
//			    } else {
//				double dist = autopilot.getXYDist(p.x,p.y);
//				Debugger.debug(1, "Deciding what to do for EOImage: Still too far from image location: " + p.x + " " + p.y + " -> currently at " + autopilot.getCurrXYString()+" dist is "+fmt2.format(dist)+", tolerance is "+UAVRI.EO_DISTANCE_TOLERANCE);
//			    }
//			}
//                                    
//			break;
//                                    
		    default:
			Debugger.debug(0, "SimplePlanner does nothing special for: " + role.getType());
		    }
		}
		if (done != null) {
		    roles.removeAll(done);
		}

                try {Thread.sleep(50); } catch (Exception e) {}
            } catch(Exception e) {
                Debugger.debug(4, "Exception in run, e="+e);
                e.printStackTrace();
                Debugger.debug(4, "Ignoring exception and continuing run loop.");
            }
        }

    }

    private final static long EXPIRE_LIMIT_MS = 500;
    private class GCEntry  {
	BeliefID bid;
	long timeIn;
	public GCEntry(BeliefID bid) {
	    this.bid = bid;
	    this.timeIn = System.currentTimeMillis();
	}
    }

    private class GC implements Runnable {
	private LinkedList<GCEntry> gcList = new LinkedList<GCEntry>();
	private Thread myThread = null;

	public void add(BeliefID bid) {
	    synchronized(gcList) {
		gcList.add(new GCEntry(bid));
	    }
	}
	public void start() {
	    myThread = new Thread(this);
	    myThread.start();
	}
	public void run() {
	    while(true) {
		long oldestTimeToKeep = System.currentTimeMillis() - EXPIRE_LIMIT_MS;
		synchronized(gcList) {
		    if(gcList.size() > 0) {
			GCEntry entry = gcList.element();
			if(entry.timeIn < oldestTimeToKeep) {
			    state.removeBelief(entry.bid);
			    gcList.remove();
			}
		    }
		}
                try {Thread.sleep(200); } catch (Exception e) {}
	    }
	}
    }
}
