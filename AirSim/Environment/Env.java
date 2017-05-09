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
 * Env.java
 *
 * Created on June 8, 2004, 6:36 PM
 */

package AirSim.Environment;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.Vector;

import com.georgeandabe.tigerline.model.TLData;

import AirSim.Environment.Assets.A10;
import AirSim.Environment.Assets.AUGV;
import AirSim.Environment.Assets.Asset;
import AirSim.Environment.Assets.Avenger;
import AirSim.Environment.Assets.C130;
import AirSim.Environment.Assets.Civilian;
import AirSim.Environment.Assets.Emitter;
import AirSim.Environment.Assets.F14;
import AirSim.Environment.Assets.F15;
import AirSim.Environment.Assets.F16;
import AirSim.Environment.Assets.F18;
import AirSim.Environment.Assets.FAARP;
import AirSim.Environment.Assets.FalsePositive;
import AirSim.Environment.Assets.ForceId;
import AirSim.Environment.Assets.GroundVehicle;
import AirSim.Environment.Assets.Hummer;
import AirSim.Environment.Assets.Infantry;
import AirSim.Environment.Assets.IntelligentMine;
import AirSim.Environment.Assets.M1A1Tank;
import AirSim.Environment.Assets.M1A2Tank;
import AirSim.Environment.Assets.M2Tank;
import AirSim.Environment.Assets.M35A2CargoTruck;
import AirSim.Environment.Assets.M977CargoTruck;
import AirSim.Environment.Assets.MicroUAV;
import AirSim.Environment.Assets.Munition;
import AirSim.Environment.Assets.SA9;
import AirSim.Environment.Assets.SSMissile;
import AirSim.Environment.Assets.Scud;
import AirSim.Environment.Assets.SmallUAV;
import AirSim.Environment.Assets.T72MTank;
import AirSim.Environment.Assets.T80Tank;
import AirSim.Environment.Assets.Tank;
import AirSim.Environment.Assets.Truck;
import AirSim.Environment.Assets.TwoS6;
import AirSim.Environment.Assets.UGV;
import AirSim.Environment.Assets.UnattendedGroundSensor;
import AirSim.Environment.Assets.WASM;
import AirSim.Environment.Assets.ZSU234M;
import AirSim.Environment.Assets.Sensors.DirectionalRFSensor;
import AirSim.Environment.Assets.Sensors.EOIRSensor;
import AirSim.Environment.Assets.Sensors.Ladar;
import AirSim.Environment.Assets.Sensors.MarkOneEyeball;
import AirSim.Environment.Assets.Sensors.RSSISensor;
import AirSim.Environment.Assets.Sensors.SARSensor;
import AirSim.Environment.Assets.Sensors.Sensor;
import AirSim.Environment.Assets.Sensors.TDOASensor;
import AirSim.Environment.Assets.Tasks.Attack;
import AirSim.Environment.Assets.Tasks.Circle;
import AirSim.Environment.Assets.Tasks.Defend;
import AirSim.Environment.Assets.Tasks.DefendAir;
import AirSim.Environment.Assets.Tasks.Deploy;
import AirSim.Environment.Assets.Tasks.Hold;
import AirSim.Environment.Assets.Tasks.Land;
import AirSim.Environment.Assets.Tasks.Mount;
import AirSim.Environment.Assets.Tasks.MoveToBuilding;
import AirSim.Environment.Assets.Tasks.RandomMove;
import AirSim.Environment.Assets.Tasks.Transport;
import AirSim.Environment.Buildings.Building;
import AirSim.Environment.Buildings.Pavement;
import AirSim.Environment.Buildings.Terminal;
import AirSim.Environment.GUI.MainFrame;
import AirSim.Environment.GUI.MainFrameGUI;
import AirSim.Machinetta.CostMaps.TiledCostMap;
import Gui.BackgroundConfig;
import Gui.DoubleGrid;
import Gui.IntGrid;
import Gui.LatLonUtil;
import Gui.Roads;
import Machinetta.Configuration;

// @TODO: This class is way to big now and _really_ needs to be
// refactored.  Among other things, we can probably move the xplane
// stuff out to it's own (reusable) class.  And also the config file
// processing might be moved out as well as
// cleanedup/shrunk/refactored internally, currently it makes up way
// over half the file.  Perhaps the stats stuff can become it's own
// class.

/**
 * Implements a singleton pattern, so create instances at will
 *
 * @author  paul
 */
public class Env {
    // @TODO This assumes a 50mk x 50km map.
    public final static boolean CONSTRAIN_MAP_TO_TEN_KM_ON = false;

    public static double MIN_EMITTER_RANDOM_DIST_APART_MAP_PERCENT = .300;
    public static double MIN_EMITTER_RANDOM_DIST_APART_METERS = 300;

    private final static double SANJAYA_MAP_001_LAT_FUDGE = -0.084;
    private final static double SANJAYA_MAP_001_LON_FUDGE = -0.0126;

    public static double MAP_WIDTH_METERS=50000;
    public static double MAP_HEIGHT_METERS=50000;

    private static double latFudge = 0.0;
    private static double lonFudge = 0.0;

    private static boolean PRINT_EOIR_DIST_FROM_EMITTER = false;
    
    public static boolean USE_XPLANE_CAMERA = false;
    public static String USE_XPLANE_CAMERA_HOST = "localhost";

    public static Asset XPLANE_ASSET_VIEW = null;
    
    static DatagramSocket outputSocket;
    
    static InetAddress xserver;
    
    static _env env = null;
    public static String CONFIG_FILE = null;
    public static int numTrucks = -1;
    public static AirSim.Environment.GUI.CameraView cam = null;
    private final static DecimalFormat fmt = new DecimalFormat("0.000");
    private final static DecimalFormat fmtLong = new DecimalFormat("0.0000000000");
    private long randomSeed;
    private Random rand;

    // This option controls whether buildings that take damage from an
    // explosion allow assets contained within the building to take
    // damage as well.
    public int getContainedKillOpt() { return env.getContainedKillOpt(); }
    public void setContainedKillOpt(int containedKillOpt) { env.setContainedKillOpt(containedKillOpt); }

    // When cumulativeDamageMode is on, damage adds up and if it exceeds your
    // armor value then you die.  Without cumulativeDamageMode true, every time
    // you are subject to damage, we roll a random number and if we
    // exceed your armor level then you die, but otherwise your armor
    // level stays the same and damage does not accrue.  If I shoot
    // you ten times and you live through all ten times, you're just
    // as healthy as when you started.
    public boolean getCumulativeDamageMode() { return env.getCumulativeDamageMode(); }
    public void setCumulativeDamageMode(boolean value) { env.setCumulativeDamageMode(value); }
    
    // Singleton - this is generated only once.
    synchronized private void init() {
        if (env == null) {
            env = new _env();
            randomSeed = System.currentTimeMillis();
            rand = new java.util.Random(randomSeed);
            Machinetta.Debugger.debug("Env Random object initialized wtih random seed= "+randomSeed,0,this);
	       
            if (CONFIG_FILE != null)
                readConfig();
            else {
                Machinetta.Debugger.debug(5,"Please specify a configuration file!!!");
            }

            Machinetta.Debugger.debug("After config, UDP_SWITCH_IP_STRING/PORT= "+Configuration.UDP_SWITCH_IP_STRING+", "+Configuration.UDP_SWITCH_IP_PORT,1,this);

            if(-1 != this.numTrucks) {
		// @TODO: Should this be Neutral?
                generateRandomTrucks(this.numTrucks, ForceId.OPFOR);
            }
            connectToXserver();
        }
        

    }
    
    /** Creates a new instance of Env */
    public Env() {
        init();
    }

    public double getStepsPerSecond() { return env.getStepsPerSecond(); }
    public int getStep() { return env.getStep(); }
    public long getSimTimeMs() { return env.getSimTimeMs(); }
    
    public boolean scenarioFinished() {return env.scenarioFinished;}
    
    public boolean allOpforDead() {
        if((0 == env.opforAlive)
        & (0 < env.opforDead)) {
            Machinetta.Debugger.debug("env.opforALive="+env.opforAlive+" and env.opforDead="+env.opforDead+", hence allOpforDead returning true.",2,this);
            return true;
        } else
            return false;
    }
    
    public boolean allBlueforDead() {
        if((0 == env.blueforAlive)
        & (0 < env.blueforDead)) {
            Machinetta.Debugger.debug("env.blueforALive="+env.blueforAlive+" and env.blueforDead="+env.blueforDead+", hence allBlueforDead returning true.",2,this);
            return true;
        } else
            return false;
    }
    
    public boolean allHmmmvsDead() {
        //	Machinetta.Debugger.debug("allHmmmvsDead: alive="+env.hmmmvAlive+", dead="+env.hmmmvDead,5,this);
        if((0 == env.hmmmvAlive)
        & (0 < env.hmmmvDead)) {
            //	    Machinetta.Debugger.debug("ALL HMMMVS DEAD returning TRUE.",5,this);
            return true;
        } else {
            //	    Machinetta.Debugger.debug("ALL HMMMVS DEAD returning FALSE.",5,this);
            return false;
        }
    }
    
    public boolean allBlueforInfantryDead() {
        //	Machinetta.Debugger.debug("allBlueforInfantryDead: alive="+env.blueforInfantryAlive+", dead="+env.blueforInfantryDead,5,this);
        if((0 == env.blueforInfantryAlive)
	   & (0 < env.blueforInfantryDead)) {
            //	    Machinetta.Debugger.debug("ALL BLUEFOR INFANTRY DEAD returning TRUE.",5,this);
            return true;
        } else {
            //	    Machinetta.Debugger.debug("ALL BLUEFOR INFANTRY DEAD returning FALSE.",5,this);
            return false;
        }
    }
    
    public boolean allOpforInfantryDead() {
        //	Machinetta.Debugger.debug("allOpforInfantryDead: alive="+env.opforInfantryAlive+", dead="+env.opforInfantryDead,5,this);
        if((0 == env.opforInfantryAlive)
	   & (0 < env.opforInfantryDead)) {
            //	    Machinetta.Debugger.debug("ALL OPFOR INFANTRY DEAD returning TRUE.",5,this);
            return true;
        } else {
            //	    Machinetta.Debugger.debug("ALL OPFOR INFANTRY DEAD returning FALSE.",5,this);
            return false;
        }
    }
    
    public boolean allEmittersDetected() {
        if((0 == env.emittersUndetected) & (0 < env.emittersDetected)) {
            return true;
        } else {
            return false;
        }
    }
    
    /** Use sparingly, might change */
    public Vector<Asset> getAllAssets() { return env.assets; }
    public Vector getAllBuildings() { return env.buildings; }
    //    public Asset[] getAssetsAry() { return env.assetsAry; }
    //    public int getAssetsArySize() { return env.assetsArySize; }
    
    public Vector getTargets() {
        Vector<Tank> targets = new Vector<Tank>();
        for (Enumeration e = env.assets.elements(); e.hasMoreElements(); ) {
            Object a = e.nextElement();
            if (a instanceof Tank) {
                targets.add((Tank)a);
            }
        }
        return targets;
    }
    
    /**
     * Put objects both in the spatial representation and in a vector for easy access
     */
    public void addAsset(Asset a) {
        env.spatial.addObject((int)a.location.x, (int)a.location.y, (int)a.location.z, a);
        env.assets.addElement(a);
        env.ids.put(a.getID(), a);
// 	env.assetsAry[env.assetsArySize++] = a;
        Machinetta.Debugger.debug("Adding asset "+a.getID()+" at "+a.location.x+", "+a.location.y, 1, this);
    }
    
    public void removeAsset(Asset a) {
        Machinetta.Debugger.debug("Removing asset "+a.getID()+" at "+a.location.x+", "+a.location.y, 1, this);
        
        env.spatial.removeObject(a);
        env.assets.removeElement(a);
        env.ids.remove(a.getID());
        
// 	Asset temp;
// 	for(int loopi = 0; loopi < env.assetsArySize; loopi++) {
// 	    if(env.assetsAry[loopi] == a) {
// 		env.assetsAry[loopi] = env.assetsAry[env.assetsArySize];
// 		env.assetsArySize--;
// 	    }
// 	}
    }

    /**
     * Put objects both in the spatial representation and in a vector for easy access
     */
    public void addBuilding(Building a) {
        env.buildingsSpatial.addObject((int)a.getLocation().x, (int)a.getLocation().y, (int)a.getLocation().z, a);
        env.buildings.addElement(a);
        env.ids.put(a.getID(), a);
// 	env.assetsAry[env.assetsArySize++] = a;
        Machinetta.Debugger.debug("Adding building "+a.getID()+" at "+a.getLocation().x+", "+a.getLocation().y, 1, this);
    }
    
    public void removeBuilding(Building a) {
        Machinetta.Debugger.debug("Removing building "+a.getID()+" at "+a.getLocation().x+", "+a.getLocation().y, 1, this);
        
        env.buildingsSpatial.removeObject(a);
        env.buildings.removeElement(a);
        env.ids.remove(a.getID());
        
// 	Asset temp;
// 	for(int loopi = 0; loopi < env.assetsArySize; loopi++) {
// 	    if(env.assetsAry[loopi] == a) {
// 		env.assetsAry[loopi] = env.assetsAry[env.assetsArySize];
// 		env.assetsArySize--;
// 	    }
// 	}
    }
    
    public void setCameraView(AirSim.Environment.GUI.CameraView cam) {
        this.cam = cam;
    }
    public void stopCameraView() { cam = null; }
    
    public Asset getAssetByID(String id) {
        return (Asset)env.ids.get(id);
    }
    
    public Building getBuildingByID(String id) {
        return (Building)env.ids.get(id);
    }
    
    /** Changes the assets location */
    public void moveAsset(Asset a) {
        env.spatial.moveObject((int)a.location.x, (int)a.location.y, (int)a.location.z, a);
    }
    
    /** Get a linked list of all assets within this box
     *
     * @todo Change this method (and its friends) to return LinkedList<Asset>
     */
    public LinkedList getAssetsInBox(int x1, int y1, int z1, int x2, int y2, int z2) {
        return getAssetsInBox(x1, y1, z1, x2, y2, z2, false);
    }
    
    public LinkedList getAssetsInBox(int x1, int y1, int z1, int x2, int y2, int z2, boolean seeMounted) {
        //return env.spatial.getAll(x1, y1, z1, x2, y2, z2);
        LinkedList llt = env.spatial.getAll(x1, y1, z1, x2, y2, z2);
        LinkedList retList = new LinkedList();
        
        if(seeMounted) {
            retList = llt;
        } else {
            for (ListIterator li = llt.listIterator(); li.hasNext(); ) {
                Asset a = (Asset)li.next();
                if(a.getMountingAsset() == null) {
                    retList.add(a);
                }
            }
        }
        
        return retList;
    }
    
    public LinkedList getBuildingsInArea(int x1, int y1, int x2, int y2) {
        return env.buildingsSpatial.getAll(x1, y1, -10, x2, y2, 10);
    }
    
    public Building findClosestBuilding(double x, double y, int range, double distance) {
        double shortestDist = Double.MAX_VALUE;
        Building retBD = null;
        
        for (ListIterator li = getBuildingsInArea((int)(x - range), (int)(y - range), (int)(x + range), (int)(y + range)).listIterator(); li.hasNext(); ) {
            Building b = (Building)li.next();            
            Vector3D locVec = new Vector3D(b.getLocation().x, b.getLocation().y, b.getLocation().z);
            double actDist = locVec.toVector(x, y, 0).length();
            if(actDist < distance && actDist < shortestDist) {
                shortestDist = actDist;
                retBD = b;
            }
        }
        
        return retBD;
    }
    
    public void amDead(Asset a) {
        if(a.getForceId() == ForceId.BLUEFOR) {
            env.blueforAlive--;
            env.blueforDead++;
        } else if(a.getForceId() == ForceId.OPFOR){
            env.opforAlive--;
            env.opforDead++;
        } else if(a.getForceId() == ForceId.NEUTRAL){
	    env.neutralAlive--;
            env.neutralDead++;
        } else if(a.getForceId() == ForceId.UNKNOWN){
            env.unknownAlive--;
            env.unknownDead++;
        }
	else {
	    Machinetta.Debugger.debug(2, "WARNING: Somebody added a new enum to ForceId enums without updating the live/dead tracking code.");
	}
        if(a instanceof Hummer) {
            env.hmmmvAlive--;
            env.hmmmvDead++;
        } else if(a instanceof WASM) {
            env.wasmAlive--;
            env.wasmDead++;
        } else if(a instanceof SmallUAV) {
            env.smallUavAlive--;
            env.smallUavDead++;
        } else if(a instanceof Civilian) {
            env.civilianAlive--;
            env.civilianDead++;
        } else if(a instanceof Infantry) {
            env.infantryAlive--;
            env.infantryDead++;
	    if(ForceId.BLUEFOR == a.getForceId()) {
		env.blueforInfantryAlive--;
		env.blueforInfantryDead++;
	    }
	    if(ForceId.OPFOR == a.getForceId()) {
		env.opforInfantryAlive--;
		env.opforInfantryDead++;
	    }
        }
    }

    public void gatherStats() {
        env.hmmmvAlive = 0;
        env.hmmmvDead = 0;
        env.hmmmvTasked =0;
        env.hmmmvLiveAndAtGoal = 0;
        env.wasmAlive = 0;
        env.wasmDead = 0;
        env.wasmTasked =0;
        env.smallUavAlive = 0;
        env.smallUavDead = 0;
        env.smallUavTasked =0;
        env.opforAlive = 0;
        env.opforDead = 0;
        env.blueforAlive = 0;
        env.blueforDead = 0;
        env.neutralAlive = 0;
        env.neutralDead = 0;
        env.unknownAlive = 0;
        env.unknownDead = 0;
        env.scenarioFinished = true;
	env.emittersDetected = 0;
	env.emittersUndetected = 0;
        env.civilianAlive = 0;
        env.civilianDead = 0;
        env.civilianTasked =0;
        env.infantryAlive = 0;
        env.infantryDead = 0;
        env.infantryTasked =0;
        env.blueforInfantryAlive = 0;
        env.blueforInfantryDead = 0;
        env.blueforInfantryTasked =0;
        env.opforInfantryAlive = 0;
        env.opforInfantryDead = 0;
        env.opforInfantryTasked =0;
        
	env.totalNavdataSent = 0;
	env.totalSensorSent = 0;

        for (Enumeration e = env.assets.elements(); e.hasMoreElements(); ) {
            try {
                Asset a = (Asset)e.nextElement();
		
		env.totalNavdataSent+= a.navdataSent;
		env.totalSensorSent += a.sensorSent;

                // If a hummer exists, is alive, and is not at its
                // goal, then we're not done.
                if((a instanceof Hummer)
                && a.isLive()
                && !a.atGoal())
                    env.scenarioFinished = false;
                
                if(a.getForceId()== ForceId.BLUEFOR) {
                    if(a.isLive())
                        env.blueforAlive++;
                    else
                        env.blueforDead++;
                } else if(a.getForceId()== ForceId.OPFOR){
                    if(a.isLive())
                        env.opforAlive++;
                    else
                        env.opforDead++;
                } else if(a.getForceId()== ForceId.NEUTRAL){
                    if(a.isLive())
                        env.neutralAlive++;
                    else
                        env.neutralDead++;
                } else if(a.getForceId()== ForceId.UNKNOWN){
                    if(a.isLive())
                        env.unknownAlive++;
                    else
                        env.unknownDead++;
                }
		else {
		    Machinetta.Debugger.debug(2,"WARNING: Somebody added a new enum to ForceId enums without updating the live/dead tracking code.");
		}
                if(a instanceof Hummer) {
                    if(a.tasks.size() > 0)
                        env.hmmmvTasked++;
                    if(a.isLive()) {
                        env.hmmmvAlive++;
                        if(a.atGoal()) env.hmmmvLiveAndAtGoal++;
                    } else
                        env.hmmmvDead++;
                } else if(a instanceof WASM) {
                    if(a.tasks.size() > 0)
                        env.wasmTasked++;
                    if(a.isLive())
                        env.wasmAlive++;
                    else
                        env.wasmDead++;
                } else if(a instanceof SmallUAV) {
                    if(a.tasks.size() > 0)
                        env.smallUavTasked++;
                    if(a.isLive())
                        env.smallUavAlive++;
                    else
                        env.smallUavDead++;
                } else if(a instanceof Emitter) {
                    if(a.detected)
			env.emittersDetected++;
                    else
			env.emittersUndetected++;
                } else if(a instanceof Civilian) {
                    if(a.tasks.size() > 0)
                        env.civilianTasked++;
                    if(a.isLive())
                        env.civilianAlive++;
                    else
                        env.civilianDead++;
                } else if(a instanceof Infantry) {
                    if(a.tasks.size() > 0) 
                        env.infantryTasked++;
                    if(a.isLive())
                        env.infantryAlive++;
                    else
                        env.infantryDead++;
		    if(a.getForceId() == ForceId.BLUEFOR) {
			if(a.tasks.size() > 0) 
			    env.blueforInfantryTasked++;
			if(a.isLive())
			    env.blueforInfantryAlive++;
			else
			    env.blueforInfantryDead++;
		    }
		    if(a.getForceId() == ForceId.OPFOR) {
			if(a.tasks.size() > 0) 
			    env.opforInfantryTasked++;
			if(a.isLive())
			    env.opforInfantryAlive++;
			else
			    env.opforInfantryDead++;
		    }
                }
                
                // How close are you to something that can kill you
                
            } catch (Exception e2) {
                Machinetta.Debugger.debug("ERROR ERROR EXCEPTION DURING STEP:"+e2, 5, this);
                e2.printStackTrace();
            }
        }
    }
    
    public void dumpStats() {
	gatherStats();
        long timeNow = System.currentTimeMillis();
        long elapsed = timeNow - env.startTime;
        double timePerStep = ((double)elapsed)/((double)getStep());
        Machinetta.Debugger.debug(2,"STATS: STEP="+getStep()
				  +", SimTimeMs="+getSimTimeMs()
				  +", WALLCLOCKTIME="+timeNow
				  +", elapsed="+elapsed
				  +", timePerStep="+fmt.format(timePerStep)
				  +", hmmmvAlive="+env.hmmmvAlive
				  +", hmmmvDead="+env.hmmmvDead
				  +", hmmmvTasked="+env.hmmmvTasked
				  +", wasmAlive="+env.wasmAlive
				  +", wasmDead="+env.wasmDead
				  +", wasmTasked="+env.wasmTasked
				  +", smallUavAlive="+env.smallUavAlive
				  +", smallUavDead="+env.smallUavDead
				  +", smallUavTasked="+env.smallUavTasked
				  +", civilianAlive="+env.civilianAlive
				  +", civilianDead="+env.civilianDead
				  +", civilianTasked="+env.civilianTasked
				  +", infantryAlive="+env.infantryAlive
				  +", infantryDead="+env.infantryDead
				  +", infantryTasked="+env.infantryTasked
				  +", blueforInfantryAlive="+env.blueforInfantryAlive
				  +", blueforInfantryDead="+env.blueforInfantryDead
				  +", blueforInfantryTasked="+env.blueforInfantryTasked
				  +", opforInfantryAlive="+env.opforInfantryAlive
				  +", opforInfantryDead="+env.opforInfantryDead
				  +", opforInfantryTasked="+env.opforInfantryTasked
				  +", totalNavdataSent="+env.totalNavdataSent
				  +", totalSensorSent="+env.totalSensorSent
				  );
    }
    
    public void dumpPerAssetStats() {
        long timeNow = System.currentTimeMillis();
        long elapsed = timeNow - env.startTime;
        StringBuffer buf = new StringBuffer("DETAILSTATS: STEP="+getStep()+", SimTimeMs="+getSimTimeMs()+", WALLCLOCKTIME="+timeNow+", elapsed="+elapsed);
        
	long totalNavdataSent = 0;
	long totalSensorSent = 0;
        buf.append(Asset.getStatHeaderString());
        for (Enumeration e = env.assets.elements(); e.hasMoreElements(); ) {
            try {
                Asset a = (Asset)e.nextElement();
                if(!(a instanceof Munition)) {
                    buf.append(a.getStatString());
		    totalNavdataSent+= a.navdataSent;
		    totalSensorSent += a.sensorSent;
		}
            } catch (Exception e2) {
                Machinetta.Debugger.debug("ERROR ERROR EXCEPTION DURING STEP:"+e2, 5, this);
                e2.printStackTrace();
            }
        }
        Machinetta.Debugger.debug(2,buf.toString());
    }
    
    
    public void connectToXserver()  {
        try{
            xserver = InetAddress.getByName(USE_XPLANE_CAMERA_HOST);
            outputSocket = new DatagramSocket();
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }
    
    public void setEvent(String desc, Asset asset, Vector3D loc) {
	env.event = true;
	env.eventDesc = desc;
	env.eventAsset = asset;
	env.eventLoc.x = loc.x;
	env.eventLoc.y = loc.y;
	env.eventLoc.z = loc.z;
	env.eventTimeMs = getSimTimeMs();
    }
    public void setEvent(String desc, Asset asset) {
	setEvent(desc, asset, asset.location);
    }
    public void clearEvent() { env.event = false;}
    public boolean getEvent() { return env.event;}
    public String eventString() { 
	String eventString = env.eventDesc+" ";
	if(null != env.eventAsset)
	    eventString = eventString + env.eventAsset.getID()+"."+env.eventAsset.getGroup()+": ";
	eventString = eventString+env.eventLoc.toString();
	eventString = eventString+" at "+env.eventTimeMs;
	return eventString;
    }

    /**
     * Step the environment
     */
    public void step() {
        Machinetta.Debugger.debug("STEP CALLED IN ENVIRONMENT",0, this);
        if(0 == env.getStep())
            env.startTime = System.currentTimeMillis();
        
        env.incStep();
        // Let all the assets sense and move
        for (Enumeration e = env.assets.elements(); e.hasMoreElements(); ) {
            try {
                Asset a = (Asset)e.nextElement();
		// @TODO: The call to a.sense() was uncommented
		// originally - does that mean we're calling sense
		// twice for each asset?  Because we also call sense
		// inside Asset.step().  I just checked and there are
		// no asset subclasses with their own step method,
		// except for the missiles, which have empty sense
		// methods.  And Asset only calls sense once every
		// 'senseRate' steps.  So we shouldn't call it here.
		// But we need to make all that a little cleaner.
		// Best we should call sense here, have sense check
		// the senseRate and only sense if appropriate, then
		// call step next here.  So anyhow I commented it out,
		// everything seems to be working still.
		// 
		//                a.sense();
                a.step();
                Machinetta.Debugger.debug(a.getID()+"THIS IS THE ID OF THE ASSET",0, this);
            } catch (Exception e2) {
                Machinetta.Debugger.debug("ERROR ERROR EXCEPTION DURING STEP:"+e2, 5, this);
                e2.printStackTrace();
            }
        }
        if (cam != null) {
//            cam.step();
        }

	gatherStats();

// 	if((env.getStep() % 100) == 0)
// 	    dumpStats();
// 	if((env.getStep() % 1000) == 0)
// 	    dumpPerAssetStats();

        
        // Print out the locations of all the assets for log
        // printLocations();
	if(USE_XPLANE_CAMERA)
	    sendToXPlane();
        
    }
    
    public static void setXPlaneView(Asset a) {
	Machinetta.Debugger.debug("setXPlaneView!",1,"Env");

	if((null != XPLANE_ASSET_VIEW) && (null != a)) {
	    Machinetta.Debugger.debug("Changing xplane view from "+XPLANE_ASSET_VIEW.getID()+" to "+a.getID(), 1, "Env");
	}	    
	else if(null != a) {
	    Machinetta.Debugger.debug("Changing xplane view from NULL to "+a.getID(), 1, "Env");
	}	    
	else  {
	    Machinetta.Debugger.debug("Changing xplane view from "+XPLANE_ASSET_VIEW.getID()+" to NULL", 1, "Env");
	}	    
        XPLANE_ASSET_VIEW = a;
    }

    // @TODO: HACK - SRO Mon Apr 16 23:35:02 EDT 2007 
    // 
    // the lat/lon conversion is off a bit according to xplane - so
    // we tried this to fudge it for the demo
    private static double localToLat(double y) {
	return (env.elevationGrid.localToLat(y) + latFudge);
    }
    private static double localToLon(double x) {
	return ((0-env.elevationGrid.localToLong(x)) + lonFudge);
    }

    private static double latToLocalY(double lat) {
	return env.elevationGrid.latToLocalY(lat);
    }
    private static double lonToLocalX(double lon) {
	return env.elevationGrid.longToLocalX(0-lon);
    }

    private void sendToXPlane(double x, double y) {
	try {
	    int count = 0;
	    int cameraIndex = 255;
	    ByteArrayOutputStream out = new ByteArrayOutputStream();
	    DataOutputStream dout = new DataOutputStream(out);
	    try {
		dout.write(0x00); //This is set below to the number of UAVs
		dout.write(255); //This is set below.
            
	    } catch (IOException ex) {
		ex.printStackTrace();
	    }
        
        
	    try {
            
		dout.writeDouble(y);
		dout.writeDouble(x);
		dout.writeDouble(100);
            
		dout.writeFloat(0.0f);//Should actually be pitch
		dout.writeFloat(0.0f);//Should actually be roll
		dout.writeFloat(0.0f);//Should actually be yaw
            
            
	    } catch (IOException ex) {
		ex.printStackTrace();
	    }
        
        
	    count++;
        
        
        
	    byte[] buffer = out.toByteArray();
	    buffer[0] = 0x01;
	    buffer[1] = (byte)cameraIndex;
        
	    for(int i =0;i<count;i++) {
		reverseDouble(buffer, 2+i*36);
		reverseDouble(buffer, 10+i*36);
		reverseDouble(buffer, 18+i*36);
		reverseFloat(buffer, 26+i*36);
		reverseFloat(buffer, 30+i*36);
		reverseFloat(buffer, 34 +i*36);
	    }
        
	    DatagramPacket packet = new DatagramPacket(buffer, buffer.length, xserver, 27016);
	    try {
            
		outputSocket.send(packet);
	    } catch (IOException ex) {
		ex.printStackTrace();
	    }
	}
	catch(Exception e) {
            Machinetta.Debugger.debug(5,"Exception sending to xplane, ignoring, e="+e);
	    e.printStackTrace();
	}
    }
    
    private int printSendToXplaneCounter = 0;
    private void sendToXPlane() {
	try {
        int count = 0;
        int cameraIndex = 255;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(out);
        try {
            dout.write(0x00); //This is set below to the number of UAVs
            dout.write(255); //This is set below to the UAV we want to view from in XPlane
            
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
	Asset emitter = null;
        for (Enumeration e = env.assets.elements(); e.hasMoreElements() && count < 20; ) {
            Asset a = (Asset)e.nextElement();
	    if(a instanceof Emitter)
		emitter = a;
	}
        for (Enumeration e = env.assets.elements(); e.hasMoreElements() && count < 20; ) {
	    Asset a = null;
	    a = (Asset)e.nextElement();

//  	    if(!(a instanceof SmallUAV)) 
//  		continue;
 	    if(!(a instanceof SmallUAV)
// 	       && !(a instanceof C130)
// 	       && !(a instanceof Hummer)
// 	       && !(a instanceof SA9)
// 	       && !(a instanceof SSMissile)
// 	       && !(a instanceof SAMissile)
// 	       && !(a instanceof ASMissile)
	       ) 
 		continue;

	    if(null == XPLANE_ASSET_VIEW) {
		if(a.hasEOIRSensor())
		    setXPlaneView(a);
	    }

	    try {
		if(PRINT_EOIR_DIST_FROM_EMITTER && a.getID().startsWith("EOIR-UAV")) {
		    Vector3D assetLoc = new Vector3D(a.location);
		    assetLoc.z = emitter.location.z;
		    double distSqd = emitter.location.toVector(assetLoc).lengthSqd();
		    if(distSqd < (3000*3000)) {
			Machinetta.Debugger.debug("\tDIST_FROM_EMITTER\t"+a.getID()+"\t"+Math.sqrt(distSqd)+"\t",1,this);
		    }
		}

		double lon = localToLon(a.location.x);
		double lat = localToLat(a.location.y);
                    
		dout.writeDouble(lat);
		dout.writeDouble(lon);
		// @TODO: Fix altitude
		//                    dout.writeDouble(a.location.z);
		dout.writeDouble(600);
                    
		// @TODO: Fix roll and pitch?
		dout.writeFloat(0.0f);//Should actually be roll
		//                    dout.writeFloat((float)a.rollDegrees);//Should actually be roll
		dout.writeFloat(0.0f);//Should actually be pitch
		// yaw/heading works
		if(null != a.heading) {
		    dout.writeFloat((-(float)(a.heading.angle()))+90.0f);//Should actually be yaw
		    
		    if((printSendToXplaneCounter % 100) == 0)
			Machinetta.Debugger.debug(1,"Sending to xplane id "+a.getID()+" heading "+(-a.heading.angle())+" roll "+a.rollDegrees+" at lat= "+lat+" lon= "+lon+" (local coords "+fmt.format(a.location.x)+","+fmt.format(a.location.y)+","+fmt.format(a.location.z)+")");
		    printSendToXplaneCounter++;
		    //		    Machinetta.Debugger.debug("\tToXplane:ID,SimTime,heading,lat,lon:\t"+a.getID()+"\t"+getSimTimeMs()+"\t"+(-a.heading.angle()+90.0f)+"\t0.0\t"+lat+"\t"+lon+"\t",1,this);
		}
	    } catch (IOException ex) {
		ex.printStackTrace();
	    }
	    if(XPLANE_ASSET_VIEW != null && XPLANE_ASSET_VIEW == a)
		cameraIndex=count;
                
	    count++;
        }
        byte[] buffer = out.toByteArray();
        buffer[0] = (byte)count;
        buffer[1] = (byte)cameraIndex;
        
        for(int i =0;i<count;i++) {
            reverseDouble(buffer, 2+i*36);
            reverseDouble(buffer, 10+i*36);
            reverseDouble(buffer, 18+i*36);
            reverseFloat(buffer, 26+i*36);
            reverseFloat(buffer, 30+i*36);
            reverseFloat(buffer, 34 +i*36);
        }
        
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, xserver, 27016);
        try {
            
            outputSocket.send(packet);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
	}
	catch(Exception e) {
            Machinetta.Debugger.debug(5,"Exception sending to xplane, ignoring, e="+e);
	    e.printStackTrace();
	}
    }
    
    public void printState(int x1, int y1, int z1, int x2, int y2, int z2) {
        Machinetta.Debugger.debug(1,"Drawing state: ");
        List l = env.spatial.getAll(x1,y1,z1,x2,y2,z2);
        for (ListIterator li = l.listIterator(); li.hasNext(); )
            Machinetta.Debugger.debug(1,"Asset : " + li.next());
    }
    
    public void printLocations() {
        Machinetta.Debugger.debug(1,"Locations:");
        for (Asset a: env.assets) {
            Machinetta.Debugger.debug(1,a.getID() + " @ " + a.locAsString());
        }
    }
    
    public Road [] getRoadMap() {
        return env.roadMap.roads;
    }
    
    public Vector getTrees() { return env.trees; }
    
    private void loadCTDB(String baseGridFileName) {
        env.baseGridFileName = baseGridFileName;
        
	// @TODO: THis is a hack... should really fix this elsewhere -
	// I guess inside the map file.
	if(env.baseGridFileName.endsWith("sanjaya001_0_0_50000_50000_050")) {
	    latFudge = SANJAYA_MAP_001_LAT_FUDGE;
	    lonFudge = SANJAYA_MAP_001_LON_FUDGE;
	}

        if(null == env.elevationGridFileName)
            env.elevationGridFileName = env.baseGridFileName+"_e.grd";
        if(null == env.soilGridFileName)
            env.soilGridFileName = env.baseGridFileName+"_s.grd";
        if(null == env.xGridFileName)
            env.xGridFileName = env.baseGridFileName+"_x.grd";
        if(null == env.yGridFileName)
            env.yGridFileName = env.baseGridFileName+"_y.grd";
        if(null == env.zGridFileName)
            env.zGridFileName = env.baseGridFileName+"_z.grd";
        
        env.soilGrid = new IntGrid();
        env.soilGrid.loadSoilTypeFile(env.soilGridFileName);
        env.elevationGrid = new DoubleGrid();
        env.elevationGrid.loadGridFile(env.elevationGridFileName);
//         env.xGrid = new IntGrid();
//         env.xGrid.loadGridFile(env.xGridFileName);
//         env.yGrid = new IntGrid();
//         env.yGrid.loadGridFile(env.yGridFileName);
//         env.zGrid = new IntGrid();
//         env.zGrid.loadGridFile(env.zGridFileName);
        
    }

    private void loadRoads(String roadFileName) {
	env.roadFileNames.add(roadFileName);
	env.roadData = Roads.load(env.roadData, roadFileName);
    }
    
    private void readConfig() {
        try {
            BufferedReader in = new BufferedReader(new FileReader(CONFIG_FILE));
            String str;
            String scenarioStr = "";
	    boolean savingScenario = false;
            while ((str = in.readLine()) != null) {
		if(savingScenario) {
		    if (str.startsWith("*END")) {
			savingScenario = false;
			env.scenarios.add(scenarioStr);                    
		    }
		    else {
                        scenarioStr += str;
                        scenarioStr += "\n";
		    }
		    continue;
		}
                if(str.startsWith("*SCENARIO")) {
                    savingScenario = true;
                    scenarioStr = str+"\n";
		    continue;
                } 
		process(str);
            }

            in.close();
        } catch (IOException e) {
            Machinetta.Debugger.debug(5,"Error reading config file: " + e);
        }
        if (env.roadMap == null) env.roadMap = new RoadMap();
    }
    
    // @todo this needs to be made better - map size should not be
    // hardcoded - this is easy to do but I don't want to mess with it
    // now because I need to checking and without proper testing I
    // might break something.  Just use the elevation grid or the soil
    // grid and call getBottomLeftX(), getBottomLeftY(), and
    // getWidthMeters(), and getHeightMeters().
    public void generateRandomTrucks(int no, ForceId forceId) {
        String name;
        int botLeftX = 0;
        int botLeftY = 0;
        int rangeX = 0;
        int rangeY = 0;
        if(null != env.soilGrid) {
            botLeftX = env.soilGrid.getBottomLeftX();
            botLeftY = env.soilGrid.getBottomLeftY();
            rangeX = (int)env.soilGrid.getWidthMeters();
            rangeY = (int)env.soilGrid.getHeightMeters();
        } else if(null != env.elevationGrid) {
            botLeftX = env.elevationGrid.getBottomLeftX();
            botLeftY = env.elevationGrid.getBottomLeftY();
            rangeX = (int)env.elevationGrid.getWidthMeters();
            rangeY = (int)env.elevationGrid.getHeightMeters();
        }
// 	else {
// 	    rangeX = AirSim.Configuration.width;
// 	    rangeY = AirSim.Configuration.length;
// 	}
        java.util.Random rand = new java.util.Random(83729472);
        for (int i = 0; i < no; i++) {
            name = forceId.singleLetter() + "-T-" + i;
            int x = botLeftX + rand.nextInt(rangeX);
            int y = botLeftY + rand.nextInt(rangeY);
            Asset a = new Truck(name, x, y);
            a.setForceId(forceId);
        }
    }
    
    int x, y, z;
    private void randomPlace(boolean ground) {
	int botLeftX = 0;
	int botLeftY = 0;
	int rangeX = 0;
	int rangeY = 0;
	if(CONSTRAIN_MAP_TO_TEN_KM_ON) {
	    botLeftX = 21000;
	    botLeftY = 21000;
	    rangeX = 8000;
	    rangeY = 8000;
	} else  {
	    rangeX = (int)MAP_WIDTH_METERS;
	    rangeY = (int)MAP_HEIGHT_METERS;
	}
	x = botLeftX + rand.nextInt(rangeX);
	y = botLeftY + rand.nextInt(rangeY);
	if(!ground)
	    z = rand.nextInt(1000)+1000;
	else
	    z = 0;
    }

    String name = null;
    ForceId forceId = ForceId.UNKNOWN;
    String groupTag = null;
    private void parseNameXYZ(StringTokenizer tok, boolean parseZ, boolean parseForceId) {
	name = tok.nextToken();
	x = (Integer.decode(tok.nextToken())).intValue();
	y = (Integer.decode(tok.nextToken())).intValue();
	if(parseZ)
	    z = (Integer.decode(tok.nextToken())).intValue();
	else 
	    z = 0;
	if(parseForceId) {
	    String forceIdString =  null;
	    if(tok.hasMoreTokens()) {
		forceIdString = tok.nextToken();
		forceId = ForceId.parse(forceIdString);
		if(tok.hasMoreTokens()) {
		    groupTag = tok.nextToken();
		}
		else
		    groupTag = null;
	    }
	}
    }
    
    private void setForceIdAndGroup(Asset a) {
	a.setForceId(forceId);
	if(null != groupTag)
	    a.setGroup(groupTag);
    }


    public void process(String str) {
        if(str.startsWith("#"))
            return;   
        if(str.startsWith("*SCENARIO"))
            return;   
        try {
	    // @TODO: SRO Tue Feb  5 16:52:54 EST 2008 
	    //
	    // Need to change the code below - every line to place an
	    // entity should have a standard set of params and the
	    // code to parse them should be a subroutine.  Then we
	    // should have some kind of marker for variable/optional
	    // params that vary by entity.  Perhaps something as
	    // simple as "OPT FOO=1 BAR=2".
            Machinetta.Debugger.debug("Processing line="+str,1,this);
            StringTokenizer tok = new StringTokenizer(str);
            String type = (tok.hasMoreTokens()? tok.nextToken(): "###");
            if (type.equalsIgnoreCase("WASM")) {
		parseNameXYZ(tok,true,true);
                Asset a = new WASM(name, x, y, z);
		setForceIdAndGroup(a);                
            } else if (type.equalsIgnoreCase("A10")) {
		parseNameXYZ(tok,true,true);
                Asset a = new A10(name, x, y, z);
		setForceIdAndGroup(a); 
	    } else if (type.equalsIgnoreCase("SMALL_UAV")) {
                String name  = tok.nextToken();
                String xString = tok.nextToken();
                if(xString.equalsIgnoreCase("RANDOM")) {
                    while(true) {
			randomPlace(false);
                        Machinetta.Debugger.debug("For SmallUAV "+name+" x,y trying random position "+x+", "+y+", "+z,1,this);
                        
                        // Check if someone else is already too close...
                        // MainFrame.assetCollisionRangeMeters
                        // MainFrame.assetCollisionRangeSqdMeters
                        Vector3D location = new Vector3D(x,y,z);
                        Vector v = getAllAssets();
                        boolean collision = false;
                        for(int loopi = 0; loopi < v.size(); loopi++) {
                            Asset a = (Asset)v.get(loopi);
                            if(a instanceof SmallUAV) {
                                double zdiff = z - a.location.z;
                                if(zdiff < 0) zdiff = -zdiff;
                                if(zdiff > MainFrame.assetCollisionMinZDistMeters*2)
                                    continue;
                                double dist = Math.sqrt(a.location.toVectorLengthSqd(x,y,z));
                                if(dist < MainFrame.assetCollisionRangeMeters*4) {
                                    Machinetta.Debugger.debug("Location at "+x+", "+y+", "+z+" is too close ("+dist+") to collision range ("+MainFrame.assetCollisionRangeMeters+" * 4) to "+a+" at "+a.location,1,this);
                                    collision = true;
                                }
                            }
                        }
                        if(!collision)
                            break;
                    }
                    Machinetta.Debugger.debug("        For SmallUAV "+name+" x,y final random position "+x+", "+y+", z set to 1000",1,this);
                } else {
                    x = (int)Double.parseDouble(xString);
                    y = (int)Double.parseDouble(tok.nextToken());
                    z = (int)Double.parseDouble(tok.nextToken());
                    Machinetta.Debugger.debug("        For SmallUAV "+name+" x,y,z position specified by .cfg as "+x+", "+y+", "+z,1,this);
                }
                Machinetta.Debugger.debug("        Creating SmallUAV asset at "+x+", "+y+", "+z,1,this);
                double threshold = Double.parseDouble(tok.nextToken());
                Asset a = new SmallUAV(name, x, y, z, threshold);
                if (setForceId(tok, a))
                    setGroupFor(tok, a);
            } else if (type.equalsIgnoreCase("C130")) {
		parseNameXYZ(tok,true,true);
                Asset a = new C130(name, x, y, z);
		setForceIdAndGroup(a); 
		// @TODO: 'heading' optional param should probably be
		// done better...
		if(tok.hasMoreTokens()) {
		    double heading = Double.parseDouble(tok.nextToken());
		    a.heading.setXYHeading(heading);
		    Machinetta.Debugger.debug(1,"        Setting heading for asset "+name+" to "+a.heading.angle());
		}
                
            } else if (type.equalsIgnoreCase("SSMissile")) {
		parseNameXYZ(tok,false,false);
                String targetName  = tok.nextToken();
                GroundVehicle target = (GroundVehicle)getAssetByID(targetName);
                Asset a = new SSMissile(name, x, y, new Waypoint(target.location), target);
		
                if (setForceId(tok, a))
                    setGroupFor(tok, a);
            } else if (type.equalsIgnoreCase("F14")) {
		parseNameXYZ(tok,true,true);
                Asset a = new F14(name, x, y, z);
		setForceIdAndGroup(a);
                
            } else if (type.equalsIgnoreCase("F15")) {
		parseNameXYZ(tok,true,true);
                Asset a = new F15(name, x, y, z);
		setForceIdAndGroup(a);
                
            } else if (type.equalsIgnoreCase("F16")) {
		parseNameXYZ(tok,true,true);
                Asset a = new F16(name, x, y, z);
		setForceIdAndGroup(a);
                
            } else if (type.equalsIgnoreCase("F18")) {
		parseNameXYZ(tok,true,true);
                Asset a = new F18(name, x, y, z);
		setForceIdAndGroup(a);
                
            } else if (type.equalsIgnoreCase("MICRO_UAV")) {
		parseNameXYZ(tok,true,true);
                Asset a = new MicroUAV(name, x, y, z);
		setForceIdAndGroup(a);
                
            } else if (type.equalsIgnoreCase("M1A1TANK")) {
		parseNameXYZ(tok,false,true);
                Asset a = new M1A1Tank(name, x, y);
		setForceIdAndGroup(a);
                
            } else if (type.equalsIgnoreCase("M1A2TANK")) {
		parseNameXYZ(tok,false,true);
                Asset a = new M1A2Tank(name, x, y);
		setForceIdAndGroup(a);
                
            } else if (type.equalsIgnoreCase("M2TANK")) {
		parseNameXYZ(tok,false,true);
                Asset a = new M2Tank(name, x, y);
		setForceIdAndGroup(a);
                
            } else if (type.equalsIgnoreCase("HMMWV")
		       || type.equalsIgnoreCase("HMMVW") 	// Common mispelling
		       || type.equalsIgnoreCase("HUMMER")	// Common slang
		       || type.equalsIgnoreCase("HUMVEE")	// Common slang
		       ) {
		parseNameXYZ(tok,false,true);
                Asset a = new Hummer(name, x, y);
		setForceIdAndGroup(a);
                
            } else if (type.equalsIgnoreCase("UGV")) {
		parseNameXYZ(tok,false,true);
                Asset a = new UGV(name, x, y);
		setForceIdAndGroup(a);
                
            } else if (type.equalsIgnoreCase("AUGV")) {
		parseNameXYZ(tok,false,true);
                Asset a = new AUGV(name, x, y);
		setForceIdAndGroup(a);
                
            } else if (type.equalsIgnoreCase("T80")) {
		parseNameXYZ(tok,false,true);
                Asset a = new T80Tank(name, x, y);
		setForceIdAndGroup(a);
                
            } else if ((type.equalsIgnoreCase("T72M"))
            || (type.equalsIgnoreCase("T72"))) {
		parseNameXYZ(tok,false,true);
                Asset a = new T72MTank(name, x, y);
		setForceIdAndGroup(a);
                
            } else if (type.equalsIgnoreCase("TWOS6")) {
		parseNameXYZ(tok,false,true);
                Asset a = new TwoS6(name, x, y);
		setForceIdAndGroup(a);
                
            } else if ((type.equalsIgnoreCase("ZSU23"))
            || (type.equalsIgnoreCase("ZSU234M"))
            || (type.equalsIgnoreCase("ZSU23_4M")) ){
		parseNameXYZ(tok,false,true);
                Asset a = new ZSU234M(name, x, y);
		setForceIdAndGroup(a);
                
            } else if (type.equalsIgnoreCase("M977")) {
		parseNameXYZ(tok,false,true);
                Asset a = new M977CargoTruck(name, x, y);
		setForceIdAndGroup(a);
                
            } else if ((type.equalsIgnoreCase("M35"))
            || (type.equalsIgnoreCase("M35A2"))) {
		parseNameXYZ(tok,false,true);
                Asset a = new M35A2CargoTruck(name, x, y);
		setForceIdAndGroup(a);
                
            } else if (type.equalsIgnoreCase("AVENGER")) {
		parseNameXYZ(tok,false,true);
                Asset a = new Avenger(name, x, y);
		setForceIdAndGroup(a);
                
            } else if (type.equalsIgnoreCase("SA9")) {
		// @TODO: This parsing code sucks.  Because our format
		// sucks.  We need sometihng a tad more powerful,
		// format wise.  XML?

                String[] tokfields = str.split("\\s");
		int tokInd = 1;
                String name = tokfields[tokInd++];
                String xString = tokfields[tokInd++];
                if(xString.equalsIgnoreCase("RANDOM")) {
                    while(true) {
			randomPlace(true);
                        Machinetta.Debugger.debug("For SA9 "+name+" x,y trying random position "+x+", "+y,1,this);
                        
                        // Check if someone else is already too close...
                        // MainFrame.assetCollisionRangeMeters
                        // MainFrame.assetCollisionRangeSqdMeters
                        Vector3D location = new Vector3D(x,y,0);
                        Vector v = getAllAssets();
                        boolean collision = false;
                        for(int loopi = 0; loopi < v.size(); loopi++) {
                            Asset a = (Asset)v.get(loopi);
                            if(a instanceof SA9) {
                                double dist = Math.sqrt(a.location.toVectorLengthSqd(x,y,0));
                                if(dist < MIN_EMITTER_RANDOM_DIST_APART_METERS) {
                                    Machinetta.Debugger.debug("Location at "+x+", "+y+" is too close ("+dist+") to min dist apart ("+MIN_EMITTER_RANDOM_DIST_APART_METERS+") to "+a+" at "+a.location,1,this);
                                    collision = true;
                                }
                            }
                        }
                        if(!collision)
                            break;
                    }
                    Machinetta.Debugger.debug("        For SA9 "+name+" x,y final random position "+x+", "+y,1,this);

		}
		else {
		    x = (int)Double.parseDouble(xString);
		    y = (int)Double.parseDouble(tokfields[tokInd++]);
                    Machinetta.Debugger.debug("        For SA9 "+name+" x,y position specified by .cfg as "+x+", "+y,1,this);
		}

		int channel = Emitter.CHANNEL_DEFAULT;
		if(tokInd < tokfields.length) {
		    if(tokfields[tokInd].startsWith("CHANNEL")) {
			String channelStr = tokfields[tokInd++];
			channel = Integer.parseInt(channelStr.substring("CHANNEL".length()));
			Machinetta.Debugger.debug("        For SA9 "+name+" channel is "+channel,1,this);
		    }
		}
                Asset a = new SA9(name, x, y,channel);

		if(tokInd < tokfields.length) {
		    ForceId forceId = ForceId.parse(tokfields[tokInd++]);
		    a.setForceId(forceId);
		}
		if(tokInd < tokfields.length) {
		    a.setGroup(tokfields[tokInd++]);
		}

		if(tokInd < tokfields.length) {
                    long periodMs = (Long.decode(tokfields[tokInd++])).longValue();
                    double percent=1.0;
		    if(tokInd < tokfields.length) {
                        percent = (Double.valueOf(tokfields[tokInd++])).doubleValue();
                    }
                    Machinetta.Debugger.debug("Setting SA9 to be an intermittent emitter, periodMs="+periodMs+", percent="+percent,1,this);
                    ((Emitter)a).setIntermittent(periodMs, percent);
                }
                
            } else if (type.equalsIgnoreCase("SCUD")) {
		parseNameXYZ(tok,false,true);
                Asset a = new Scud(name, x, y);
		setForceIdAndGroup(a);
                
            } else if (type.equalsIgnoreCase("TRUCK")) {
                String name = tok.nextToken();
                if (name.equalsIgnoreCase("MULTI")) {
                    int no = (Integer.decode(tok.nextToken())).intValue();
		    ForceId forceId = ForceId.parse(tok.nextToken());
                    generateRandomTrucks(no,forceId);
                } else {
                    int x = (Integer.decode(tok.nextToken())).intValue();
                    int y = (Integer.decode(tok.nextToken())).intValue();
                    Asset a = new Truck(name, x, y);
                    if (setForceId(tok, a))
                        setGroupFor(tok, a);
                }
            } else if (type.equalsIgnoreCase("UGS") || type.equalsIgnoreCase("UnattendedGroundSensor")) {
		parseNameXYZ(tok,false,true);
                Asset a = new UnattendedGroundSensor(name, x, y);
                setForceId(tok, a);
                // Might want to process some parameters here.
                
            } else if (type.equalsIgnoreCase("FALSE_POSITIVE")) {
                int number = (Integer.decode(tok.nextToken())).intValue();
                double detectRate = Double.parseDouble(tok.nextToken());
                Random rand = new Random();
                for (int i = 0; i < number; i++) {
                    int x = rand.nextInt(AirSim.Configuration.width);
                    int y = rand.nextInt(AirSim.Configuration.length);
                    Asset a = new FalsePositive("FP"+i, x, y, detectRate);
                }
                

            } else if (type.equalsIgnoreCase("CIVILIAN")) {
                String name  = tok.nextToken();
                String xString = tok.nextToken();
                if(xString.equalsIgnoreCase("RANDOM")) {
		    randomPlace(true);
                    Machinetta.Debugger.debug("        For Civilian "+name+" x,y final random position "+x+", "+y+", z set to 1000",1,this);
                } else {
                    x = (int)Double.parseDouble(xString);
                    y = (int)Double.parseDouble(tok.nextToken());
                    //z = (int)Double.parseDouble(tok.nextToken());
                    z = 0;
                    
                    Machinetta.Debugger.debug("        For Civilian "+name+" x,y,z position specified by .cfg as "+x+", "+y+", "+z,1,this);
                }
                Machinetta.Debugger.debug("        Creating Civilian asset at "+x+", "+y+", "+z,1,this);
                Asset a = new Civilian(name, x, y, z);
                if (setForceId(tok, a))
                    setGroupFor(tok, a);

              // Code for old stuff removed. Use CVS to get it back ... (if you want to).  Paul 13/9/05
            } else if (type.equalsIgnoreCase("INFANTRY")) {
                String name  = tok.nextToken();
                String xString = tok.nextToken();
                if(xString.equalsIgnoreCase("RANDOM")) {
		    randomPlace(true);
                    Machinetta.Debugger.debug("        For Infantry "+name+" x,y final random position "+x+", "+y+", z set to 1000",1,this);
                } else {
                    x = (int)Double.parseDouble(xString);
                    y = (int)Double.parseDouble(tok.nextToken());
                    //z = (int)Double.parseDouble(tok.nextToken());
                    z = 0;
                    
                    Machinetta.Debugger.debug("        For Infantry "+name+" x,y,z position specified by .cfg as "+x+", "+y+", "+z,1,this);
                }
                Machinetta.Debugger.debug("        Creating Infantry asset at "+x+", "+y+", "+z,1,this);
                Asset a = new Infantry(name, x, y, z);
                if (setForceId(tok, a))
                    setGroupFor(tok, a);

            } else if (type.equalsIgnoreCase("FAARP")) {
                String name  = tok.nextToken();
                String xString = tok.nextToken();
                if(xString.equalsIgnoreCase("RANDOM")) {
		    randomPlace(true);
                    Machinetta.Debugger.debug("        For FAARP "+name+" x,y final random position "+x+", "+y+", z set to 1000",1,this);
                } else {
                    x = (int)Double.parseDouble(xString);
                    y = (int)Double.parseDouble(tok.nextToken());
                    //z = (int)Double.parseDouble(tok.nextToken());
                    z = 0;
                    
                    Machinetta.Debugger.debug("        For FAARP "+name+" x,y,z position specified by .cfg as "+x+", "+y+", "+z,1,this);
                }
                Machinetta.Debugger.debug("        Creating FAARP asset at "+x+", "+y+", "+z,1,this);
                Asset a = new FAARP(name, x, y, z);
                if (setForceId(tok, a))
                    setGroupFor(tok, a);

              // Code for old stuff removed. Use CVS to get it back ... (if you want to).  Paul 13/9/05
            } else if (type.equalsIgnoreCase("TERMINAL")) {
                String name  = tok.nextToken();
                int x;
                int y;
                int z;
                int width, height;
		x = (int)Double.parseDouble(tok.nextToken());
		y = (int)Double.parseDouble(tok.nextToken());
                    
		width = (int)Double.parseDouble(tok.nextToken());
		height = (int)Double.parseDouble(tok.nextToken());
		z = 0;
                    
                Machinetta.Debugger.debug(1, "        Creating Terminal building at "+x+", "+y+", "+z);
                Building b = new Terminal(name, x, y, width, height);

            } else if (type.equalsIgnoreCase("PAVEMENT")) {
                String name  = tok.nextToken();
		// PAVEMENT is a polygon specified by a series of
		// lat/lon pairs.  We read them in and translate them
		// to local coords.  The first lat/lon pair is used as
		// the x/y location of the object.
		boolean first = true;
		int x=Integer.MAX_VALUE;
		int y=Integer.MAX_VALUE;
		
		StringBuffer pavementLocalCoords = new StringBuffer("");
		Machinetta.Debugger.debug(1, "        Origin lat/lon = "+getOriginLat()+","+getOriginLon()+", height/width in meters = "+env.elevationGrid.getWidthMeters()+","+env.elevationGrid.getHeightMeters());
		ArrayList<Point> pointList = new ArrayList<Point>();
		while(tok.hasMoreTokens()) {
		    double lat;
		    double lon;
		    lat = Double.parseDouble(tok.nextToken());
		    lon = Double.parseDouble(tok.nextToken());

		    double localx = LatLonUtil.lonToLocalXMeters(getOriginLat(), getOriginLon(), lon);
		    double localy = LatLonUtil.latToLocalYMeters(getOriginLat(), getOriginLon(), lat);
		    if(first) {
			x = (int)localx;
			y = (int)localy;
			first = false;
		    }
		    pointList.add(new Point((int)localx,(int)localy));
		    pavementLocalCoords.append("("+(int)localx+","+(int)localy+") ");
		}
                    
                Machinetta.Debugger.debug(1, "        Creating Pavement "+name+" at "+x+", "+y +" = "+ pavementLocalCoords.toString());
                Pavement p = new Pavement(name, x, y, 0, 0);
		Point[] pointAry = pointList.toArray(new Point[1]);
		p.setPoly(pointAry);

              // Code for old stuff removed. Use CVS to get it back ... (if you want to).  Paul 13/9/05
            } else if (type.equalsIgnoreCase("INTELMINE")) {
                String name  = tok.nextToken();
                int x;
                int y;
                int z;
		x = (int)Double.parseDouble(tok.nextToken());
		y = (int)Double.parseDouble(tok.nextToken());
		z = 0;
                Machinetta.Debugger.debug("        Creating Intelligent Mine asset at "+x+", "+y+", "+z,1,this);
                Asset a = new IntelligentMine(name, x, y);
                if (setForceId(tok, a))
                    setGroupFor(tok, a);
		
              // Code for old stuff removed. Use CVS to get it back ... (if you want to).  Paul 13/9/05
            } else if (type.equalsIgnoreCase("TASK")) {
		String assetid = tok.nextToken();
                Asset asset = getAssetByID(assetid);
                String taskType = tok.nextToken();
		if(null == asset) {
                    Machinetta.Debugger.debug(3, "TASK for unknown asset "+assetid+", task is "+taskType);
		    return;
		}
                
                if(taskType.equalsIgnoreCase("DEFEND")) {
		    int x = (int)Double.parseDouble(tok.nextToken());
		    int y = (int)Double.parseDouble(tok.nextToken());
                        
		    boolean defendAir = true;
		    boolean defendGround = true;
		    if(tok.hasMoreTokens()) {
			String airString = tok.nextToken();
			if(airString.equalsIgnoreCase("true"))
			    defendAir = true;
			else
			    defendAir = false;
		    }
		    if(tok.hasMoreTokens()) {
			String groundString = tok.nextToken();
			if(groundString.equalsIgnoreCase("true"))
			    defendGround = true;
			else
			    defendGround = false;
		    }
                    Machinetta.Debugger.debug(1, "         Creating Task DEFEND("+defendAir+","+defendGround+","+x+","+y+") for " + asset.getID());
		    Defend defend = new Defend(defendAir, defendGround, x, y);
		    asset.addTaskToAsset(defend);
		} else if(taskType.equalsIgnoreCase("DEFENDAIR")) {
                    Machinetta.Debugger.debug(1, "         Creating Task DEFENDAIR for " + asset.getID());
		    String airString = tok.nextToken();
		    String surfaceString = tok.nextToken();
		    boolean air = false;
		    boolean surface = false;
		    if(airString.equalsIgnoreCase("true"))
			air = true;
		    if(surfaceString.equalsIgnoreCase("true"))
			surface = true;
		    String xString = null;
		    String yString = null;
		    String wString = null;
		    String hString = null;
		    if(tok.hasMoreTokens()) {
			 xString = tok.nextToken();
			 yString = tok.nextToken();
			 wString = tok.nextToken();
			 hString = tok.nextToken();
		    }
		    DefendAir defend = null;
		    if((null != xString) && (null != yString) 
		       && (null != wString) && (null != hString)) {
			int x = (int)Double.parseDouble(xString);
			int y = (int)Double.parseDouble(yString);
			int w = (int)Double.parseDouble(wString);
			int h = (int)Double.parseDouble(hString);
			defend = new DefendAir(air, surface, x, y, w, h);
		    }
		    else {
			defend = new DefendAir(air, surface);
		    }
		    
		    asset.addTaskToAsset(defend);
                } else if(taskType.equalsIgnoreCase("MOUNT")) {
                    Machinetta.Debugger.debug(1, "         Creating Task MOUNT for " + asset.getID());
                    Asset mountedAsset = getAssetByID(tok.nextToken());
                    Mount mountTask = new Mount(true, mountedAsset);
                    asset.addTaskToAsset(mountTask);
                } else if(taskType.equalsIgnoreCase("HOLD")) {
                    Machinetta.Debugger.debug(1, "         Creating Task HOLD for " + asset.getID());
                    Hold hold = new Hold();
                    asset.addTaskToAsset(hold);
                } else if(taskType.equalsIgnoreCase("CIRCLE")) {
                    Machinetta.Debugger.debug(1, "         Creating Task CIRCLE for " + asset.getID());
                    double x = Double.parseDouble(tok.nextToken());
                    double y = Double.parseDouble(tok.nextToken());
                    double z = Double.parseDouble(tok.nextToken());
                    double radius = Double.parseDouble(tok.nextToken());
                    double angle = Double.parseDouble(tok.nextToken());
		    Circle circleTask = new Circle(new Vector3D(x,y,z), radius, angle);
                    asset.addTaskToAsset(circleTask);
                } else if(taskType.equalsIgnoreCase("RANDOMMOVE")) {
                    Machinetta.Debugger.debug(1, "         Creating Task RANDOMMOVE for " + asset.getID());
                    int x = (int)Double.parseDouble(tok.nextToken());
                    int y = (int)Double.parseDouble(tok.nextToken());
                    int width = (int)Double.parseDouble(tok.nextToken());
                    int height = (int)Double.parseDouble(tok.nextToken());
		    RandomMove randomMoveTask = new RandomMove(x,y,width,height);
                    asset.addTaskToAsset(randomMoveTask);
                } else if(taskType.equalsIgnoreCase("TRANSPORT")) {
                    Machinetta.Debugger.debug(1, "         Creating Task TRANSPORT for " + asset.getID());
                    int x = (int)Double.parseDouble(tok.nextToken());
                    int y = (int)Double.parseDouble(tok.nextToken());
                    double z = Double.parseDouble(tok.nextToken());
                    Transport transport = new Transport(new Vector3D(x, y, z));
                    asset.addTaskToAsset(transport);
                } else if(taskType.equalsIgnoreCase("ENTER")) {
                    Machinetta.Debugger.debug(1, "         Creating Task ENTER for " + asset.getID());
                    Building building = getBuildingByID(tok.nextToken());
                    //int x = (int)Double.parseDouble(tok.nextToken());
                    //int y = (int)Double.parseDouble(tok.nextToken());
                    int x = (int)building.getLocation().x;
                    int y = (int)building.getLocation().y;                    

                    //Machinetta.Debugger.debug(1, "ENTER_BUILDING: Destination==> x: " + x + ", y: "+y);
                    //double z = Double.parseDouble(tok.nextToken());
                    MoveToBuilding mtbTask = new MoveToBuilding(new Vector3D(x, y, 0.0), building);
                    asset.addTaskToAsset(mtbTask);
                } else if(taskType.equalsIgnoreCase("DEPLOY")) {
                    Machinetta.Debugger.debug(1, "         Creating Task DEPLOY for " + asset.getID());
                    int x = (int)Double.parseDouble(tok.nextToken());
                    int y = (int)Double.parseDouble(tok.nextToken());
                    double z = Double.parseDouble(tok.nextToken());
                    Deploy deploy = new Deploy(new Vector3D(x, y, z));
                    asset.addTaskToAsset(deploy);
                } else if(taskType.equalsIgnoreCase("ATTACK")) {
                    Machinetta.Debugger.debug(1, "         Creating Task ATTACK for " + asset.getID());
                    int x = (int)Double.parseDouble(tok.nextToken());
                    int y = (int)Double.parseDouble(tok.nextToken());
                    double z = Double.parseDouble(tok.nextToken());
                    Attack attack = new Attack(new Vector3D(x, y, z));
                    asset.addTaskToAsset(attack);
                } else if(taskType.equalsIgnoreCase("LAND")) {
                    Machinetta.Debugger.debug(1, "         Creating Task LAND for " + asset.getID());
                    Land land = new Land(asset,null,0);
                    asset.addTaskToAsset(land);
                }
		else {
                    Machinetta.Debugger.debug(3, "Unknown task "+taskType+" for "+asset.getID());
		}
            } else if (type.equalsIgnoreCase("PROXY")) {
                Asset asset = getAssetByID(tok.nextToken());
                String onOrOff = tok.nextToken();
                
                if(onOrOff.equalsIgnoreCase("ON")) {
		    asset.setHasProxy(true);
		}
		else {
		    asset.setHasProxy(false);
		}
            } else if (type.equalsIgnoreCase("MOUNT")) {
                Asset asset = getAssetByID(tok.nextToken());
                Asset mount = getAssetByID(tok.nextToken());
		asset.mount(mount);
            } else if (type.equalsIgnoreCase("REPORT_SENSE")) {
                Asset asset = getAssetByID(tok.nextToken());
                String onOrOff = tok.nextToken();
                
                if(onOrOff.equalsIgnoreCase("ON")) {
		    asset.setDontReportSense(false);
		}
		else {
		    asset.setHasProxy(true);
		}

            // Code for old stuff removed. Use CVS to get it back ... (if you want to).  Paul 13/9/05
            } else if (type.equalsIgnoreCase("SENSOR")) {
                String sensorType = tok.nextToken();
                String mount =  tok.nextToken();
                Asset asset = getAssetByID(mount);
                
                Sensor sensor = null;
                if (sensorType.equalsIgnoreCase("TDOA")) {
                    sensor = new TDOASensor();
                } else if (sensorType.equalsIgnoreCase("RSSI")) {
                    sensor = new RSSISensor();
                } else if (sensorType.equalsIgnoreCase("DIRECTIONAL_RF")) {
                    String rangeString = tok.nextToken();
                    if(rangeString != null) {
                        int range = Integer.parseInt(rangeString);
                        sensor = new DirectionalRFSensor(range);
                    } else {
                        sensor = new DirectionalRFSensor();
                    }
                } else if (sensorType.equalsIgnoreCase("SAR")) {
                    sensor = new SARSensor(asset);
                } else if (sensorType.equalsIgnoreCase("LADAR")) {
                    sensor = new Ladar(asset);
                } else if (sensorType.equalsIgnoreCase("EOIR")) {
                    sensor = new EOIRSensor();
                } else if (sensorType.equalsIgnoreCase("MarkOneEyeball")) {
                    sensor = new MarkOneEyeball(asset);
                }
                
                if (sensor != null && asset != null) {
                    asset.addSensor(sensor);
                    Machinetta.Debugger.debug(1,"Adding a " + sensorType + " sensor to " + asset);
                } else {
                    Machinetta.Debugger.debug(3,"ERROR parsing sensor information: " + str);
                }
            } else if (type.equalsIgnoreCase("CTDB_BASE_NAME")) {
                String baseGridFileName = tok.nextToken();
                loadCTDB(baseGridFileName);
            } else if (type.equalsIgnoreCase("UDP_SWITCH_IP_STRING")) {
                Configuration.UDP_SWITCH_IP_STRING = tok.nextToken();
            } else if (type.equalsIgnoreCase("UDP_SWITCH_IP_PORT")) {
                Configuration.UDP_SWITCH_IP_PORT = Integer.parseInt(tok.nextToken());
            } else if (type.equalsIgnoreCase("UDP_SWITCH_GROUP")) {
                Configuration.UDP_SWITCH_GROUP = tok.nextToken();
            } else if (type.equalsIgnoreCase("UDP_MULTICAST_PORT")) {
                Configuration.UDP_MULTICAST_PORT = Integer.parseInt(tok.nextToken());
            } else if (type.equalsIgnoreCase("ROAD_FILE_NAME")) {
                String roadFileName = tok.nextToken();
                loadRoads(roadFileName);
            } else if (type.equalsIgnoreCase("UPDATE_RATE")) {
                int updateRate = (Integer.decode(tok.nextToken())).intValue();
                MainFrame.UPDATE_RATE = updateRate;
            } else if (type.equalsIgnoreCase("GUI_ON")) {
                boolean guiOn = Boolean.parseBoolean(tok.nextToken());
                MainFrame.SHOW_GUI = guiOn;
            } else if (type.equalsIgnoreCase("DAMAGE_MODE")) {
                String damageModeName = tok.nextToken();
		if(damageModeName.equalsIgnoreCase("Cumulative")) {
		    Machinetta.Debugger.debug(1,"Setting CumulativeDamageMode true.");
		    setCumulativeDamageMode(true);
		}
		else {
		    Machinetta.Debugger.debug(1,"Setting CumulativeDamageMode false.");
		    setCumulativeDamageMode(false);
		}
            } else if (type.equalsIgnoreCase("DAMAGE_MODE_BLUEFOR_DI")) {
		Asset.HACK_BLUEFOR_INFANTRY_DAMAGE = Double.parseDouble(tok.nextToken());
	    } else if (type.equalsIgnoreCase("MAP_WIDTH_METERS")) {
                MAP_WIDTH_METERS = Double.parseDouble(tok.nextToken());
		MIN_EMITTER_RANDOM_DIST_APART_METERS = 
		    MAP_WIDTH_METERS * MIN_EMITTER_RANDOM_DIST_APART_MAP_PERCENT;
            } else if (type.equalsIgnoreCase("MAP_HEIGHT_METERS")) {
                MAP_HEIGHT_METERS = Double.parseDouble(tok.nextToken());
            } else if (type.equalsIgnoreCase("END_CONDITION_CAPTURE_THE_FLAG")) {
                MainFrame.END_CONDITION_CAPTURE_THE_FLAG = Boolean.parseBoolean(tok.nextToken());
                Machinetta.Debugger.debug(1,"Setting "+type+" to "+MainFrame.END_CONDITION_CAPTURE_THE_FLAG);
            } else if (type.equalsIgnoreCase("END_CONDITION_ALL_HMMMVS_DEAD")) {
                MainFrame.END_CONDITION_ALL_HMMMVS_DEAD = Boolean.parseBoolean(tok.nextToken());
                Machinetta.Debugger.debug(1,"Setting "+type+" to "+MainFrame.END_CONDITION_ALL_HMMMVS_DEAD);
            } else if (type.equalsIgnoreCase("END_CONDITION_ALL_OPFOR_DEAD")) {
                MainFrame.END_CONDITION_ALL_OPFOR_DEAD = Boolean.parseBoolean(tok.nextToken());
                Machinetta.Debugger.debug(1,"Setting "+type+" to "+MainFrame.END_CONDITION_ALL_OPFOR_DEAD);
            } else if (type.equalsIgnoreCase("END_CONDITION_ALL_BLUEFOR_DEAD")) {
                MainFrame.END_CONDITION_ALL_BLUEFOR_DEAD = Boolean.parseBoolean(tok.nextToken());
                Machinetta.Debugger.debug(1,"Setting "+type+" to "+MainFrame.END_CONDITION_ALL_BLUEFOR_DEAD);
            } else if (type.equalsIgnoreCase("END_CONDITION_ALL_EMITTERS_DETECTED")) {
                MainFrame.END_CONDITION_ALL_EMITTERS_DETECTED = Boolean.parseBoolean(tok.nextToken());
                Machinetta.Debugger.debug(1,"Setting "+type+" to "+MainFrame.END_CONDITION_ALL_EMITTERS_DETECTED);
            } else if (type.equalsIgnoreCase("END_CONDITION_ALL_INFANTRY_DEAD")) {
                MainFrame.END_CONDITION_ALL_INFANTRY_DEAD = Boolean.parseBoolean(tok.nextToken());
                Machinetta.Debugger.debug(1,"Setting "+type+" to "+MainFrame.END_CONDITION_ALL_INFANTRY_DEAD);
            } else if (type.equalsIgnoreCase("END_CONDITION_ALL_GOALS_HELD")) {
                MainFrame.END_CONDITION_ALL_GOALS_HELD = Boolean.parseBoolean(tok.nextToken());
                Machinetta.Debugger.debug(1,"Setting "+type+" to "+MainFrame.END_CONDITION_ALL_GOALS_HELD);
            } else if (type.equalsIgnoreCase("END_CONDITION_GOAL")) {
		double x = Double.parseDouble(tok.nextToken());
		double y = Double.parseDouble(tok.nextToken());
		double z = Double.parseDouble(tok.nextToken());
		Vector3D goal = new Vector3D(x, y, z);
                MainFrame.endConditionGoals.add(goal);
                Machinetta.Debugger.debug(1,"Adding end condition goal at "+goal);
            } else if (type.equalsIgnoreCase("ASSET_COLLISION_DETECTION_ON")) {
                MainFrame.assetCollisionDetectionOn = Boolean.parseBoolean(tok.nextToken());
                Machinetta.Debugger.debug(1,"Setting "+type+" to "+MainFrame.assetCollisionDetectionOn);
            } else if (type.equalsIgnoreCase("ASSET_COLLISION_DETECTION_RANGE_METERS")) {
                MainFrame.assetCollisionRangeMeters = (Double.valueOf(tok.nextToken())).doubleValue();
                MainFrame.assetCollisionRangeSqdMeters = MainFrame.assetCollisionRangeMeters*MainFrame.assetCollisionRangeMeters;
                Machinetta.Debugger.debug(1,"Setting "+type+" to "+MainFrame.assetCollisionRangeMeters);
            } else if (type.equalsIgnoreCase("ASSET_COLLISION_DETECTION_MIN_Z_DIST_METERS")) {
                MainFrame.assetCollisionMinZDistMeters = (Double.valueOf(tok.nextToken())).doubleValue();
                Machinetta.Debugger.debug(1,"Setting "+type+" to "+MainFrame.assetCollisionMinZDistMeters);
            } else if (type.equalsIgnoreCase("ASSET_CONFIG")) {
                String assetConfigName = tok.nextToken();
                String assetConfigValueString = tok.nextToken();
                double assetConfigValue = Double.parseDouble(assetConfigValueString);
                if(assetConfigName.equalsIgnoreCase("AT_LOCATION_DISTANCE")) {
                    Machinetta.Debugger.debug(1,"Setting ASSET_CONFIG AT_LOCATION_DISTANCE to "+assetConfigValue);
                    Asset.setDefaultAtLocationDistance(assetConfigValue);
                } else if(assetConfigName.equalsIgnoreCase("MOVE_FINISHED_HOLD_RADIUS")) {
                    Machinetta.Debugger.debug(1,"Setting ASSET_CONFIG MOVE_FINISHED_HOLD_RADIUS to "+assetConfigValue);
                    Asset.setMoveFinishedHoldRadius(assetConfigValue);
                } else if(assetConfigName.equalsIgnoreCase("SMALL_UAV_MAX_SPEED_KPH")) {
                    Machinetta.Debugger.debug(1,"Setting ASSET_CONFIG UAV_MAX_SPEED_KPH to "+assetConfigValue);
                    SmallUAV.setUavMaxSpeedKph(assetConfigValue);
                } else if(assetConfigName.equalsIgnoreCase("SMALL_UAV_MAX_TURN_RATE_DEG")) {
                    Machinetta.Debugger.debug(1,"Setting ASSET_CONFIG SMALL_UAV_MAX_TURN_RATE_DEG to "+assetConfigValue);
                    SmallUAV.setUavMaxTurnRateDeg(assetConfigValue);
                } else if (assetConfigName.equalsIgnoreCase("RSSI_MAP_SCALE")) {
                    Machinetta.Debugger.debug(1,"Setting RSSISensor.MAP_SCALE to " + assetConfigValue);
                    RSSISensor.MAP_SCALE = assetConfigValue;
                } else {
                    Machinetta.Debugger.debug(3,"Error - unknown ASSET_CONFIG field name '"+assetConfigName+"' with value "+assetConfigValueString);
                }
            } else if(type.equalsIgnoreCase("GUI_VIEWPORT_X")) {
                double botLeftX = Double.parseDouble(tok.nextToken());
                MainFrameGUI.setViewPort = true;
                MainFrameGUI.viewPortX = botLeftX;
                Machinetta.Debugger.debug(1,"Setting "+type+" to "+MainFrameGUI.viewPortX);
            } else if(type.equalsIgnoreCase("GUI_VIEWPORT_Y")) {
                double botLeftY = Double.parseDouble(tok.nextToken());
                MainFrameGUI.setViewPort = true;
                MainFrameGUI.viewPortY = botLeftY;
                Machinetta.Debugger.debug(1,"Setting "+type+" to "+MainFrameGUI.viewPortY);
            } else if(type.equalsIgnoreCase("GUI_VIEWPORT_WIDTH")) {
                double width = Double.parseDouble(tok.nextToken());
                MainFrameGUI.setViewPort = true;
                MainFrameGUI.viewPortWidth = width;
                Machinetta.Debugger.debug(1,"Setting "+type+" to "+MainFrameGUI.viewPortWidth);
            } else if(type.equalsIgnoreCase("GUI_VIEWPORT_HEIGHT")) {
                double height = Double.parseDouble(tok.nextToken());
                MainFrameGUI.setViewPort = true;
                MainFrameGUI.viewPortHeight = height;
            } else if(type.equalsIgnoreCase("GUI_SOIL_TYPES")) {
                MainFrameGUI.soilTypes = Boolean.parseBoolean(tok.nextToken());
                Machinetta.Debugger.debug(1,"Setting "+type+" to "+MainFrameGUI.soilTypes);
            } else if(type.equalsIgnoreCase("GUI_SHOW_TRACES")) {
                MainFrameGUI.showTraces = Boolean.parseBoolean(tok.nextToken());
                Machinetta.Debugger.debug(1,"Setting "+type+" to "+MainFrameGUI.showTraces);
            } else if(type.equalsIgnoreCase("GUI_GRID_LINES_ONE_KM")) {
                MainFrameGUI.gridLinesOneKm = Boolean.parseBoolean(tok.nextToken());
                Machinetta.Debugger.debug(1,"Setting "+type+" to "+MainFrameGUI.gridLinesOneKm);
            } else if(type.equalsIgnoreCase("GUI_SHOW_MAP_OBJECT_NAMES")) {
                MainFrameGUI.showMapObjectNames = Boolean.parseBoolean(tok.nextToken());
                Machinetta.Debugger.debug(1,"Setting "+type+" to "+MainFrameGUI.showMapObjectNames);
            } else if(type.equalsIgnoreCase("GUI_SHOW_MAP_OBJECT_TYPES")) {
                MainFrameGUI.showMapObjectTypes = Boolean.parseBoolean(tok.nextToken());
                Machinetta.Debugger.debug(1,"Setting "+type+" to "+MainFrameGUI.showMapObjectTypes);
            } else if(type.equalsIgnoreCase("GUI_CONTOUR_MULTIPLES")) {
                int guiConfigValue = Integer.parseInt(tok.nextToken());
                if(guiConfigValue <= 0)
                    MainFrameGUI.contourMultiples = BackgroundConfig.CONTOUR_MULT_NONE;
                else if(guiConfigValue == 25)
                    MainFrameGUI.contourMultiples = BackgroundConfig.CONTOUR_MULT_25;
                else if(guiConfigValue == 50)
                    MainFrameGUI.contourMultiples = BackgroundConfig.CONTOUR_MULT_50;
                else if(guiConfigValue == 100)
                    MainFrameGUI.contourMultiples = BackgroundConfig.CONTOUR_MULT_100;
                else if(guiConfigValue == 250)
                    MainFrameGUI.contourMultiples = BackgroundConfig.CONTOUR_MULT_250;
                else if(guiConfigValue == 500)
                    MainFrameGUI.contourMultiples = BackgroundConfig.CONTOUR_MULT_500;
                else if(guiConfigValue == 1000)
                    MainFrameGUI.contourMultiples = BackgroundConfig.CONTOUR_MULT_1000;
                Machinetta.Debugger.debug(1,"Setting "+type+" to "+guiConfigValue);
            } else if(type.equalsIgnoreCase("GUI_MARK_XPLANE_ASSET")) {
		MainFrameGUI.markXplaneAsset = Boolean.parseBoolean(tok.nextToken());
                Machinetta.Debugger.debug(1,"Setting " + type + " to " + MainFrameGUI.markXplaneAsset);
            } else if (type.equalsIgnoreCase("")) {
                String baseGridFileName = tok.nextToken();
                loadCTDB(baseGridFileName);
            } else if (type.equalsIgnoreCase("USE_XPLANE_CAMERA")) {
                this.USE_XPLANE_CAMERA = Boolean.parseBoolean(tok.nextToken());
                Machinetta.Debugger.debug(1,"Setting " + type + " to " + USE_XPLANE_CAMERA);
            } else if (type.equalsIgnoreCase("USE_XPLANE_CAMERA_HOST")) {
                this.USE_XPLANE_CAMERA_HOST = tok.nextToken();
                Machinetta.Debugger.debug(1,"Setting " + type + " to " + USE_XPLANE_CAMERA_HOST);
            } else if (type.equalsIgnoreCase("###")) {
		// ignore
            } else {
                Machinetta.Debugger.debug(3,"ERROR - unknown CONFIG field '"+type+" for line "+str);
            }
            
        } catch (Exception e) {
            if (str.length() > 0) {
                Machinetta.Debugger.debug(5,"Reading failed: " + e);
                e.printStackTrace();
                Machinetta.Debugger.debug(5,"Could not read: " + str);
            }
        }
    }
    
    private boolean setForceId(StringTokenizer tok, Asset a) {
        String forceIdString =  null;
        boolean set = false;
        if(tok.hasMoreTokens()) {
            forceIdString = tok.nextToken();
            set = true;
        }
        if(null != forceIdString) {
	    a.setForceId(ForceId.parse(forceIdString));
        }
        return set;
    }
    
    private void setGroupFor(StringTokenizer tok, Asset a) {
        if(tok.hasMoreTokens()) {
            a.setGroup(tok.nextToken());
        }
    }
    
    /* Access methods for soil and elevation grids
     *
     * All coordinates local
     */
    
    /** Returns a value [0.0, 1.0] for ease of ground vehicle movement
     * in this area.  (Assumes that all vehicles are equally affected by
     * terrain, obviously unrealistic, but it is a start ...
     *
     * 1.0 represents the easiest movement (e.g., good, flat roads), 0.0
     * represents an inability to move (e.g., deep water.)
     *
     * @param x local x coordinate
     * @param y local y coordinate
     */
    public double movementDelta(int x, int y) {
        
        double ret = 1.0;
        int soilType = env.soilGrid.getValue(env.soilGrid.toGridX(x), env.soilGrid.toGridY(y));
        Machinetta.Debugger.debug("Soil type at " + x + " " + y + " is " + env.soilGrid.getValue(env.soilGrid.toGridX(x), env.soilGrid.toGridY(y)), 0, this);
        
        switch(soilType) {
            case Gui.SoilTypes.ASPHALT:
            case Gui.SoilTypes.PACKED_DIRT:
                ret = 1.0;
                break;
            case Gui.SoilTypes.DEEP_WATER:
            case Gui.SoilTypes.SWAMP:
            case Gui.SoilTypes.NO_GO:
                ret = 0.0;
                break;
            case Gui.SoilTypes.MUDDY_ROAD:
            case Gui.SoilTypes.SOFT_SAND:
            case Gui.SoilTypes.SHALLOW_WATER:
                ret = 0.75;
                break;
            case Gui.SoilTypes.BOULDERS:
            case Gui.SoilTypes.CANOPY_FOREST:
            case Gui.SoilTypes.ICE:
            case Gui.SoilTypes.MUD:
            case Gui.SoilTypes.UNKNOWN:
                ret = 0.5;
                break;
            default:
                Machinetta.Debugger.debug("Unhandled soil type, computing movement : " + soilType, 3, this);
        }
        
        Machinetta.Debugger.debug("Movement delta: " + ret, 0, this);
        return ret;
    }
    
    public void writeTerrainCostMap(String filename, int stepSize, int maxX, int maxY) {
        TiledCostMap cm = new TiledCostMap(maxX, maxY, stepSize, stepSize);
        for (int i = 0; i < maxX; i += stepSize) {
            for (int j = 0; j < maxY; j += stepSize) {
                double cost = 0.0;
                int soilType = env.soilGrid.getValue(env.soilGrid.toGridX(i), env.soilGrid.toGridY(j));
                
                switch(soilType) {
                    case Gui.SoilTypes.ASPHALT:
                        cost = 2.0;
                        break;
                    case Gui.SoilTypes.PACKED_DIRT:
                        cost = 1.0;
                        break;
                    case Gui.SoilTypes.DEEP_WATER:
                        cost = -5.0;
                        break;
                    case Gui.SoilTypes.SWAMP:
                        cost = -4.0;
                        break;
                    case Gui.SoilTypes.NO_GO:
                        cost = -5.0;
                        break;
                    case Gui.SoilTypes.MUDDY_ROAD:
                        cost = 0.0;
                        break;
                    case Gui.SoilTypes.SOFT_SAND:
                        cost = -0.5;
                        break;
                    case Gui.SoilTypes.SHALLOW_WATER:
                        cost = -1.5;
                        break;
                    case Gui.SoilTypes.BOULDERS:
                        cost = -1.0;
                        break;
                    case Gui.SoilTypes.CANOPY_FOREST:
                        cost = -0.2;
                        break;
                    case Gui.SoilTypes.ICE:
                        cost = 0.2;
                        break;
                    case Gui.SoilTypes.MUD:
                        cost = 0.3;
                        break;
                    case Gui.SoilTypes.UNKNOWN:
                        cost = -0.5;
                        break;
                    default:
                        Machinetta.Debugger.debug("Unhandled soil type, computing movement : " + soilType, 3, this);
                }
                
                cm.setValue(i, j, -10.0*cost);
            }
        }
        try {
            ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(filename));
            os.writeObject(cm);
        } catch (FileNotFoundException ex) {
            Machinetta.Debugger.debug("Failed to write costmap: " + ex, 5, this);
        } catch (IOException ex) {
            Machinetta.Debugger.debug("Failed to write costmap: " + ex, 5, this);
        }
        
    }
    
    private void reverseFloat(byte [] buffer, int start) {
        swapBytes(buffer, start, start+3);
        swapBytes(buffer, start + 1, start+2);
    }
    private void reverseDouble(byte [] buffer, int start) {
        swapBytes(buffer, start, start+7);
        swapBytes(buffer, start + 1, start+6);
        swapBytes(buffer, start + 2, start+5);
        swapBytes(buffer, start + 3, start+4);
    }
    private void swapBytes(byte [] buffer, int a, int b) {
        byte temp = buffer[a];
        buffer[a] = buffer[b];
        buffer[b] = temp;
    }
    
    public double getOriginLon() { 
	if(null != env.soilGrid) 
	    return -env.soilGrid.getWestLong();
	else if (null != env.elevationGrid)
	    return -env.elevationGrid.getWestLong();
	else
	    return Double.MAX_VALUE;
    }

    public double getOriginLat() {
	if(null != env.soilGrid) 
	    return env.soilGrid.getSouthLat();
	else if (null != env.elevationGrid)
	    return env.elevationGrid.getSouthLat();
	else
	    return Double.MAX_VALUE;
    }

    public int getSoilType(int x, int y) {
        return env.soilGrid.getValue(env.soilGrid.toGridX(x), env.soilGrid.toGridY(y));
    }
    
    public IntGrid getSoilGrid() { return env.soilGrid; }
    public DoubleGrid getElevationGrid() { return env.elevationGrid; }
    public TLData getRoadData() { return env.roadData; }
    
    /** @Deprecated Hopefully ... */
    public IntGrid getXGrid() { return env.xGrid; }
    
    /** @Deprecated Hopefully ... */
    public IntGrid getYGrid() { return env.yGrid; }
    
    /** @Deprecated Hopefully ... */
    public IntGrid getZGrid() { return env.zGrid; }
    
    public WorldDS getWorldDS() { return env.worldDS; }
    
    public Vector getScenarios() { return env.scenarios; }
    public String getScenario(int index) { return (String) env.scenarios.get(index); }
    
    /** Describes what is going on in the environment */
    class _env {
        // This is steps per second of SIMULATED time - i.e. our
        // resolution of simulation.  For example, if this is set to 10.0,
        // that means each step should simulate 1/10 of a second in our
        // simulated world.  How many steps we actually run per second of
        // real time is a different question.
        private double stepsPerSecond = 10.0;
        public double getStepsPerSecond() { return stepsPerSecond; }
        
        private int step = 0;
        public int getStep() { return step; }
        protected void incStep() { step++; }
        
        public long getSimTimeMs() { return (long)(step * (1000.0/stepsPerSecond)); }
        
        public Vector <Asset> assets = new Vector<Asset>();
        public Vector <Building> buildings = new Vector<Building>();
        //	public Asset[] assetsAry = new Asset[10000];
        //	public int assetsArySize = 0;
        public Hashtable ids = new Hashtable();
        public DataStructure3D spatial = null;
        public RoadMap roadMap = null;
        public Vector trees = new Vector();
        
        public DataStructure3D buildingsSpatial = null;
        
        public Vector <String> scenarios = new Vector<String>();
        
        String baseGridFileName = null;
        String elevationGridFileName = null;
        String soilGridFileName = null;
        String xGridFileName=null;
        String yGridFileName=null;
        String zGridFileName=null;
        ArrayList<String> roadFileNames = new ArrayList<String>();
        
        public IntGrid soilGrid = null;
        public DoubleGrid elevationGrid = null;
        // TODO: 5/16/2005 SRO
        //
        // These may not be necessary.  I think they may be needed for
        // the terrain gui but I need to check on that, and really
        // it'd be better if we can make them optional because I don't
        // think we're going to need DIS coords for AirSim stuff.
        public IntGrid xGrid = null;
        public IntGrid yGrid = null;
        public IntGrid zGrid = null;
        
	public TLData roadData = null;
       

        public long startTime = 0;
        
        public boolean scenarioFinished = false;
        public int opforAlive = 0;
        public int opforDead = 0;
        public int blueforAlive = 0;
        public int blueforDead = 0;
        public int neutralAlive = 0;
        public int neutralDead = 0;
        public int unknownAlive = 0;
        public int unknownDead = 0;
        
        public int hmmmvAlive = 0;
        public int hmmmvDead = 0;
        public int hmmmvTasked =0;
        public int hmmmvLiveAndAtGoal = 0;
        public int wasmAlive = 0;
        public int wasmDead = 0;
        public int wasmTasked =0;
        public int smallUavAlive = 0;
        public int smallUavDead = 0;
        public int smallUavTasked =0;
	public int emittersUndetected = 0;
	public int emittersDetected = 0;
        public int civilianAlive = 0;
        public int civilianDead = 0;
        public int civilianTasked =0;
        public int infantryAlive = 0;
        public int infantryDead = 0;
        public int infantryTasked =0;
        public int blueforInfantryAlive = 0;
        public int blueforInfantryDead = 0;
        public int blueforInfantryTasked =0;
        public int opforInfantryAlive = 0;
        public int opforInfantryDead = 0;
        public int opforInfantryTasked =0;

	public long totalNavdataSent = 0;
	public long totalSensorSent = 0;

        
        public WorldDS worldDS = null;
    
	public boolean event = false;
	public String eventDesc;
	public Asset eventAsset = null;
	public Vector3D eventLoc = new Vector3D(0,0,0);
	public long eventTimeMs = 0;

	// This option controls whether buildings that take damage from an
	// explosion allow assets contained within the building to take
	// damage as well.
	private int containedKillOpt = 0;
	public int getContainedKillOpt() { return containedKillOpt; }
	public void setContainedKillOpt(int containedKillOpt) { this.containedKillOpt = containedKillOpt; }

	// When cumulativeDamageMode is on, damage adds up and if it exceeds your
	// armor value then you die.  Without cumulativeDamageMode true, every time
	// you are subject to damage, we roll a random number and if we
	// exceed your armor level then you die, but otherwise your armor
	// level stays the same and damage does not accrue.  If I shoot
	// you ten times and you live through all ten times, you're just
	// as healthy as when you started.
	private boolean cumulativeDamageMode = false;
	public boolean getCumulativeDamageMode() { return cumulativeDamageMode; }
	public void setCumulativeDamageMode(boolean value) { this.cumulativeDamageMode = value; }

        public _env() {
	    startTime = System.currentTimeMillis();
            spatial = new DataStructure3D(0, 0, 0, AirSim.Configuration.width, AirSim.Configuration.length, AirSim.Configuration.height);
            buildingsSpatial = new DataStructure3D(0, 0, 0, AirSim.Configuration.width, AirSim.Configuration.length, AirSim.Configuration.height);
            worldDS = new WorldDS();
        }
    }
}
