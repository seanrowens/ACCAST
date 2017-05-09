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
 * Asset.java
 *
 * Created on June 8, 2004, 6:51 PM
 */

package AirSim.Environment.Assets;

import AirSim.Environment.Assets.Tasks.Patrol;
import AirSim.Machinetta.Messages.NavigationDataAP;
import AirSim.Machinetta.Messages.RPMessage;
import AirSim.Machinetta.Messages.RPMessage.MessageTypes;

import AirSim.ConfusionMatrixReading;
import Machinetta.RAPInterface.InputMessages.InputMessage;

import AirSim.Environment.*;
import AirSim.Environment.Buildings.*;
import AirSim.ProxyServer;
import AirSim.Environment.Assets.Memory.*;
import AirSim.Environment.Assets.Weapons.Weapon;
import AirSim.Environment.Assets.Sensors.Sensor;
import AirSim.Environment.Assets.Sensors.*;
import AirSim.Environment.Assets.Tasks.*;
import AirSim.Machinetta.Messages.NextWaypointPA;
import AirSim.Machinetta.Messages.DismountedContentsAP;
import AirSim.Machinetta.Path2D;
import AirSim.SensorReading;
import Machinetta.Communication.Message;
import Machinetta.State.BeliefType.NamedProxyID;
import Machinetta.Debugger;
import java.awt.Point;
import Gui.DoubleGrid;

import java.util.*;
import java.text.DecimalFormat;

/**
 *
 * @author  paul
 */
abstract public class Asset {
    
    public static final double HACK_DEFAULT_INFANTRY_DAMAGE = .2;
    public static double HACK_BLUEFOR_INFANTRY_DAMAGE = .2;
    
    /*------------------
     * Static variables
     *-----------------*/
    public static final boolean LOG_SENSOR_READINGS = false;
    public static final boolean NO_COMMS = false;
    
    /**
     *
     * @todo Add default speed to this enum?
     */
    public enum Types {
        UNKNOWN("Unknown"),
	    A10("A10"),
	    C130("C130"),
	    TWOS6("2S6"),
	    F14("F14"),
	    F15("F15"),
	    F16("F16"),
	    F18("F18"),
	    M1A1("M1A1"),
	    M1A2("M1A2"),
	    M2("M2"),
	    MUAV("MUAV"),
	    SMALL_UAV("UAV"),
	    T72M("T72M"),
	    T80("T80"),
	    TRUCK("TRUCK"),
	    M977("M977"),
	    M35("M35"),
	    AVENGER("AVENGER"),
	    HMMMV("HMMMV"),
	    SA9("SA9"),
	    CLUTTER("CLUTTER"),
	    WASM("WASM"),
	    ZSU23_4M("ZSU23 4M"),
	    UGS("UGS"),
	    CIVILIAN("Civilian"),
	    INFANTRY("Infantry"),
	    FAARP("FAARP"),
	    IM("Intelligent Mine"),
	    UGV("Unmanned Ground Vehicle"),
	    AUGV("Armed Unmanned Ground Vehicle"),
	    SCUD("SS-1C Scud-B/Al-Hussein")
	    ;
        
        private String name;
        
        Types(String n) { this.name = n; }
        public String toString() { return this.name; }
        
    };

    // NOTE: Because there are so many instance variables, all simple
    // getters and setters have been moved below to a section with a
    // big comment GETTERS AND SETTERS

    private HashMap mountedAssets = null;
    
    private Asset mountingAsset = null;
    
    private Building containingBuilding = null;
    
    // @TODO: this should be different - should instead of different
    // weapons for opfor/bluefor infantry with different damage
    // values.  Should be able to specify per weapon class damage
    // values in Env.txt.
    private Weapon weapon = null;
    public Weapon getWeapon() { 
	if(getType() == Types.INFANTRY) {
	    if(forceId == ForceId.BLUEFOR)
		weapon.setDamage(HACK_BLUEFOR_INFANTRY_DAMAGE);
	    else
		weapon.setDamage(HACK_DEFAULT_INFANTRY_DAMAGE);
	}

	return weapon; 
    }
    public void setWeapon(Weapon value) { weapon = value; }
    
    private static double defaultAtLocationDistance = 200.0;
    private double atLocationDistance = defaultAtLocationDistance;
    private static double moveFinishedHoldRadius= 500.0;
    private static long endOfPathAtStep=-1;
    private final static double AT_GOAL_DISTANCE = 500.0;
    public final static DecimalFormat fmt = new DecimalFormat("0.000");
    protected static Random rand = new Random();
    static protected Env env = new Env();
    private double curFuelAmount = 0.0;
    private double lastFuelAmountPrint=Double.MAX_VALUE;
    private double fuelAmountPrintEvery=1000;
    private double maxFuelAmount = 0.0;
    
    //@NOTE: fuelConsumingRatio: liter/km?
    private double fuelConsumingRatio = 0.0;
    
    private Asset refuelingAsset = null;
    
    // Sensing FAARP range is 10km.
    private double faarpSensingRange = 3900.0;
    // We need to check the minimum fuel amount to go to FAARP!
    public double getLowFuelLimit() { return faarpSensingRange*fuelConsumingRatio; }
    
    // To test, we need to set some threshold value!
    private double refuelThreshold = 0.0;
    
    // To use Fuel option, any asset should set this flag as true.
    private boolean useFuelFlag = false;

    /**
     * @NOTE: we need this field, b/c sometimes FAARP units do not refuel fully for some reasons.
     * In this case we need a way to explicitly express whether refuel task is done or not.
     */
    private boolean refuelDone = false;
    public boolean getRefuelDone() { return refuelDone; }
    public void setRefuelDone(boolean refuelDone) { this.refuelDone = refuelDone; lastFuelAmountPrint = Double.MAX_VALUE;}

    // Especially for Intelligent mines.
    private boolean droppedFlag = false;
    
    /*------------------
     * Generic variables for an asset
     *-----------------*/
    
    /** If a vehicle doesn't have a proxy this should be true, to tell it not
     * to send messages off into space.
     *
     * @todo Could/Should generalize.
     */
    protected boolean hasProxy = false;
    
    /**
     * This is true iff the ground truth display should show this type of Asset.
     */
    protected boolean visibleOnGT = true;
    
    // @TODO: Added getter/setter, make it private or protected later
    // and fix all the breakage.  
    public Vector3D location = null;

    public boolean withinRangeSqd(double rangeSqd, Asset a) {
	if(a.location.toVectorLengthSqd(location) < rangeSqd)
	    return true;
	return false;
    }

    public boolean withinConflictRangeSqd(double rangeSqd, double minZdist, Asset a) {
	double zdiff = a.location.z - location.z;
	if(zdiff<0) zdiff = -1 * zdiff;
	if(zdiff > minZdist)
	    return false;
	if(a.location.toVectorLengthSqd(location) < rangeSqd)
	    return true;
	return false;
    }

    public boolean withinRange(double range, Asset a) { return withinRangeSqd(range*range,a);}

    public Vector3D heading = null;
    public double rollDegrees=0.0;
    // @TODO: We're now trying to do some coordination involving
    // 'simulated time' in the proxies - so we're estimating how long
    // it'll take for a UAV to follow a path.  Given that, we can't
    // muck around with SPEED_SCALE or it'll throw all of those
    // calculations off.
    public double SPEED_SCALE = 1.0;

    public boolean detected = false;
    public int detectedCount = 0;
    
    /** The tasks this asset is currently performing. */
    public Task interruptedTask=null;
    public void resumeInterrupt() { 
	if(null != interruptedTask) {
	    addTask(interruptedTask); 
	    interruptedTask = null;
	}
    }

    public LinkedList<Task> tasks = new LinkedList<Task>();
    
    /** The sensors that this asset has. */
    protected ArrayList<Sensor> sensors = new ArrayList<Sensor>();
    public boolean hasEOIRSensor() {
	for (int i = 0; i < sensors.size(); i++) {
	    if(sensors.get(i) instanceof EOIRSensor) 
		return true;
	}
	return false;
    }
    
    /**
     * In order to reduce bandwidth we (RAP) only report our location
     * to proxy every n steps.  If n is set to < 0 then we never
     * report.  Likewise, to 'level' communications load (so we don't
     * have EVERY proxy reporting every 20th step) we pick a random
     * number from 0 to (reportLocationRate-1) and add that to our
     * step number before computing the modulo reportLocationRate.
     */
    public int reportLocationRate = 20;
    public int randomStepOffset = 0;

    /**
     * Likewise, to reduce CPU (for sensing) and bandwidth (for
     * reporting sensed assets) we only call sense ever n steps.
     *
     * @TODO: This is kinda broken in step where we call sense(). (And
     * may be broken yet again in other ways in subclasses that
     * override sense().)  Right after we call sense(), we then call
     * step on each of the sensors 'attached' to the asset.  Really we
     * should straighten out that entire sense()/sensors mess.  But
     * the Sensor base class only actualyl senses every so many steps,
     * based on parameters to its constructor.
     */
    public int senseRate = 10;
    protected boolean dontReportSense = false;

    public long steps = 0;
    
    public State state = State.LIVE;
    
    // @TODO: We need to sort out our entire model of speed/max
    // speed/cruising speed/economy speed and fuel consumption.  (So
    // to speak, with regards to DI, they don't really consume fule
    // per se but they do get tired.)

    private double speed = mphToms(40); // SPEED_40MPH;

    private double maxSpeed = mphToms(60);
    public double getMaxSpeed() { return maxSpeed; }
    public void setMaxSpeed(double metersPerSecond) { 
	maxSpeed = metersPerSecond; 
	if(speed > maxSpeed)
	    speed = maxSpeed;
    }
    
    // By default an asset is unarmoured and will be killed by anything
    public double armor = 0.0;
    public double damage = 0.0;

    private int stepsBetweenFiring = (int)(60 * env.getStepsPerSecond());
    private long stepLastFired = 0;
    protected boolean fireAtAirCapable = false;
    protected boolean fireAtSurfaceCapable = false;
    protected String id = "X";
    
    /** Indicates that this asset is aligned with some group, e.g., platoon1 */
    protected String group = "None";
    
    private NamedProxyID pid = null;
    protected ForceId forceId = ForceId.BLUEFOR;
    
    protected static ProxyServer proxy = null;
    public static int[] getSentMsgCounts() {
	if(null != proxy)
	    return proxy.getSentMsgCounts();
	else
	    return null;
    }

    public static long getTotalSent() {
	if(null != proxy)
	    return proxy.getTotalSent(); 
    	else
	    return -1;
    }
    public static long getTotalAcksSent() {
	if(null != proxy)
	    return proxy.getTotalAcksSent(); 
    	else
	    return -1;
    }
    public static long getTotalAckReqSent() {
	if(null != proxy)
	    return proxy.getTotalAckReqSent(); 
    	else
	    return -1;
    }
    public static long getTotalNoAckReqSent() {
	if(null != proxy)
	    return proxy.getTotalNoAckReqSent(); 
    	else
	    return -1;
    }
    public static long getTotalResentAtLeastOnce() {
	if(null != proxy)
	    return proxy.getTotalResentAtLeastOnce(); 
    	else
	    return -1;
    }
    public static long getTotalResentSum() {
	if(null != proxy)
	    return proxy.getTotalResentSum(); 
    	else
	    return -1;
    }
    public static long getTotalFailed() {
	if(null != proxy)
	    return proxy.getTotalFailed(); 
    	else
	    return -1;
    }
    
    /**
     * Returns only the first task this UAV is doing.
     */
    public Task getCurrentTask() {
	if(tasks.size() <= 0)
	    return null;
	else
	    return tasks.getFirst();
    }
    
    // @fix Need some way to change the memory type via configuration files
    protected Memory memory = new SimpleMemory(this);
    
    //
    // Stats
    //
    private long stepLastShotAt = 0;
    private Asset lastShotAtBy = null;
    public boolean beingShotAt = false;
    // Note, we should avoid using these variables - they're really just for statistics tracking.
    public int shotsFired = 0;
    public int shotAtCounter = 0;	// Unique shots
    public int theySeeUsTotal = 0;	// total detections - how many times were we sensed by an enemy
    public int theySeeUsUnique = 0;	// unique detections - how many enemies sensed us one or more times
    public int weSeeThemTotal = 0;	// How many times did we detect an enemy?
    public HashMap<String,Asset> weSeeThemUnique = new HashMap<String,Asset>();
    public double distanceToClosestEnemyRightNow = Double.MAX_VALUE;
    public double distanceToClosestEnemyEver = Double.MAX_VALUE;
    public double distanceToClosestEnemyRightNowSquared = Double.MAX_VALUE;
    public double distanceToClosestEnemyEverSquared = Double.MAX_VALUE;
    public int kills = 0;
   
    private boolean collision = false;

    public String lastWaypointPlanId=null;
    public Waypoint lastWaypoint=null;
    public long lastWaypointRcvdSimTimeMs=0;
    public String curWaypointPlanId=null;
    public Waypoint curWaypoint=null;
    public long curWaypointRcvdSimTimeMs=0;
    public Waypoint getExpectedWaypoint(long simTimeMs) {
	if((null == lastWaypoint) || (null == curWaypoint))
	    return null;
	return Waypoint.interpolate(lastWaypoint,curWaypoint,simTimeMs);
    }


    /*------------------
     * Constructors
     *-----------------*/
    
    /** Creates a new instance of Asset */
    public Asset(String id, int x, int y, int z, Vector3D heading) {
        this(id, x, y, z, heading, 0.01);
    }
    
    public Asset(String id, int x, int y, int z, Vector3D heading, double speed) {
        this.id = id;
        setPid(new NamedProxyID(id));
        
        location = new Vector3D(x, y, z);
        this.heading = heading;
        this.speed = speed;
        this.randomStepOffset = rand.nextInt(reportLocationRate);
        this.weapon = new Weapon();
        this.mountedAssets = new HashMap();
                
        if(this.getUseFuelFlag() == false) {
            this.fuelConsumingRatio = 0;
        }
        
	atLocationDistance = defaultAtLocationDistance;

        env.addAsset(this);
    }
    
    
   /*------------------
    * Actions
    *-----------------*/
    
    /** @Deprecated Use the generic Sensor mechanism */
    public abstract void sense();
    
    public void step() { step(1); }
    
    /**
     * This is the default implementation of step in an asset, which just executes any
     * tasks that have been set.
     *
     * Subclasses are free to override.
     */
    ArrayList<SensorReading> srs = new ArrayList<SensorReading>();
    
    public void step(long time) {
        if(State.DESTROYED == state)
            return;
        
        // First do sensing
        // This if statement will eventually (i.e., ASAP) disappear
        // To be subsumed by the next loop
        if ((steps + randomStepOffset) % senseRate == 0) {
            sense();
        }
        
	// @NOTE: Sensor base class has code in step() that does
	// something like senseRate (above), so these sensors aren't
	// performing each and every step like it may appear.
        for (int i = 0; i < sensors.size(); i++) {
            ArrayList<SensorReading> snew = sensors.get(i).step(this,env);
            if (snew != null)
                srs.addAll(snew);
        }
        if (srs.size() > 0 && !dontReportSense) {
// 	    if(forceId == ForceId.BLUEFOR) 
// 		Debugger.debug(1,"DELETEME: step: "+getID()+" sending "+srs.size()+" SensorReadings to sendSensorReadingToProxy()");
            for (int i = 0; i < srs.size(); i++) {
		//                Machinetta.Debugger.debug(1, "Sending sensor reading to proxy " + pid + " : " + srs.get(i));
                sendSensorReadingToProxy(srs.get(i));
            }
        }
// 	else {
// 	    if((forceId == ForceId.BLUEFOR) && srs.size() > 0)
// 		Debugger.debug(1,"DELETEME: step: "+getID()+" NOT sending "+srs.size()+" SensorReadings to sendSensorReadingToProxy()");
// 	}
        srs.clear();
        
        // Then execute tasks
        executeTasks(time);
        _step(time);
    }
    
    protected void addTask(Task t) {
        if (tasks.size() > 0) {
	    if(!(this instanceof Civilian))
		Machinetta.Debugger.debug(3, getID()+ " adding task "+t.toString()+" replacing previous task "+tasks);
            tasks.clear();
        }
        //isRemove = false;
        tasks.add(t);
	if(!(this instanceof Civilian))
	    env.setEvent("new task "+t, this);
    }
    
    // @TODO: Buh, why is this here?  Is this a Jun-ism?
    public void addTaskToAsset(Task t) {
        addTask(t);
    }
    
    protected boolean removeTask(Task t) {
	Machinetta.Debugger.debug(1, "Asset "+getID()+" removing task "+t);
        return tasks.remove(t);
    }
    
    /**
     * Subclasses should just call this if they want to execute the current tasks.
     */
    protected void executeTasks(long time) {
        try {
            if (tasks.size() > 0) {
                if(!tasks.getFirst().finished(this)) {
                    tasks.getFirst().step(this, time);
                } else {
                    removeTask(tasks.getFirst());
                }
            }
        } catch(Exception e) {
            Machinetta.Debugger.debug("ERROR ERROR ERROR: "+getID()+".executeTasks: exception during step, ignoring, e="+e, 5, this);
            e.printStackTrace();
        } catch(Throwable t) {
            Machinetta.Debugger.debug("ERROR ERROR ERROR: "+getID()+".executeTasks: exception/throwable during step, ignoring, t="+t, 5, this);
            t.printStackTrace();
        }
    }
    
    /**
     * Mount/Dismount & Load/Unload given assets in HashMap
     */
    
    // mount this asset to 'a' asset
    public boolean mount(Asset a) {
        if(a.mountedAssets.containsKey(this.getID()) == true) {
            Machinetta.Debugger.debug(1, "Mount Error: " + a.getID() + " is already containing given asset "+ this.getID());
            return false;
        } else {
            a.mountedAssets.put(this.getID(), this);
            this.mountingAsset = a;
            this.droppedFlag = false;
            Machinetta.Debugger.debug(1, "Mounting "+getID()+" on "+a.getID());
	    env.setEvent("mount Asset "+a.getID(),this);
	    this.speed = 0;
	    sendNavigationDataToProxyMed();
            return true;
        }
    }
    
    // dismount this asset from mounting asset
    public boolean dismount() {
        if(this.mountingAsset == null) {
            Machinetta.Debugger.debug(1, "Dismount Error: " + this.getID() + " isn't mounting to any asset!!!");
            return false;
        } else {
            this.mountingAsset.mountedAssets.remove(this.getID());
            this.mountingAsset = null;
            this.droppedFlag = true;
	    env.setEvent("Dismount", this);
	    sendNavigationDataToProxyMed();
            return true;
        }
    }
    
    // mount 'a' asset to this
    public boolean loadAsset(Asset a) {
        if(this.mountedAssets.containsKey(a.getID()) == true) {
            Machinetta.Debugger.debug(1, "Load Error: " + getID() + " is already containing given asset "+ a.getID());
            return false;
        } else {
            this.mountedAssets.put(a.getID(), a);
            a.mountingAsset = this;
            a.droppedFlag = false;
	    env.setEvent("Loading "+a.getID(), this);
	    a.sendNavigationDataToProxyMed();
            return true;
        }
    }
    
    // dismount 'a' asset from this
    public boolean unloadAsset(Asset a) {
        if(this.mountedAssets.containsKey(a.getID()) == false) {
            Machinetta.Debugger.debug(1, "Unload Error: " + getID() + " isn't containing given asset " + a.getID());
            return false;
        } else {
            this.mountedAssets.remove(a.getID());
            a.mountingAsset = null;
            a.droppedFlag = true;
	    env.setEvent("unloading "+a.getID(), this);
	    a.sendNavigationDataToProxyMed();
            return true;
        }
    }

    public boolean unloadAll() {
	boolean retval;
        if(this.mountedAssets.size() == 0) {
            Machinetta.Debugger.debug(1, "UnloadAll Error: " + getID() + " isn't containing any asset.");
            retval = false;
        } else {
            Iterator iterator = this.mountedAssets.keySet().iterator();
            while( iterator. hasNext() ) {
                //Thread.sleep(3000);
                String id = (String)iterator.next();
                Asset a = env.getAssetByID(id);
                a.mountingAsset = null;
                a.droppedFlag = true;
		a.sendNavigationDataToProxyMed();
                iterator.remove();
		Machinetta.Debugger.debug(1, "UnloadAll: Unloaded asset "+a.getID()+", it's cur loc "+a.location.toString());
            }
            //Machinetta.Debugger.debug(1, "After UnloadAll: " + getID() + " has " + this.mountedAssets.size() + " units.");
            retval = true;
        }
	// Tell our proxy we unloaded everything.
	// @TODO: Should we include proxyid's of the asset's we unmounted?
	DismountedContentsAP dm = new DismountedContentsAP(new Vector3D(location));
	dm.setCriticality(Message.Criticality.HIGH);
	sendToProxy(dm);
	return retval;
    }


    public boolean holdAll() {
        if(this.mountedAssets.size() == 0) {
            return false;
        } else {
            Iterator iterator = this.mountedAssets.keySet().iterator();
            while( iterator. hasNext() ) {
                String id = (String)iterator.next();
                Asset a = env.getAssetByID(id);
		Task h = new Hold();
		a.addTask(h);
            }
            return true;
        }
    }
        
    // mount this asset to building
    public boolean enterBuilding(Building b) {
        return b.addAsset(this);        
    }
    
    // dismount this asset from building
    public boolean leaveBuilding() {
	if(null == containingBuilding)
	    return false;
        return containingBuilding.removeAsset(this);
    }
    
    /**
     * Change all mounted assets location as a final destination location
     */
    public void changeMountedAssetsLoc(Vector3D dest) {
        Iterator iterator = this.mountedAssets.keySet().iterator();
        while( iterator. hasNext() ) {
            Asset a = env.getAssetByID((String)iterator.next());
            
            Random rand = new java.util.Random();
            double randVal = 100*(rand.nextDouble()-0.5);  
         
            a.location.x = dest.x + randVal;
            a.location.y = dest.y + randVal;
            a.location.z = dest.z;

            env.moveAsset(a);            
        }
    }

    /**
     * This function computes how far a vehicle is going to move,
     * given the simulation speed and the current terrain.
     */
    public double moveDistance() {
        double ret = SPEED_SCALE * speed/env.getStepsPerSecond();
	if(this instanceof Munition) {
	    return ret;
	}
	else if(location.z <= 10) {
            ret *= env.movementDelta((int)location.x,(int)location.y);
        }
        
        if (ret == 0.0 && this instanceof Aircraft) {
	    //            Machinetta.Debugger.debug("Saying no move: " + getID() + " " + SPEED_SCALE + " " + speed + " " + env.getStepsPerSecond() + " " + location.z, 1, this);
        } else if (ret == 0.0) {
	    //            Machinetta.Debugger.debug("Ground Vehicle stuck!! Unsticking", 1, this);
            ret = 0.1;
        }
        
        return ret;
    }
    
    public double neededFuelAmountOneStep() {
        return this.fuelConsumingRatio * moveDistance();
    }
    
    public double neededFuelAmountFromDest() {
        double distance = distanceToGoal();
        
        if(distance == -1) {
            distance = 0;
        }
        
        return this.fuelConsumingRatio * distance;
    }
    
    public void moveToConfiguredDest() {
	//Machinetta.Debugger.debug(1, getID()+"===========> current location of z: " + location.z);
	env.moveAsset(this);
    }
    
    private Building wouldEnterBuilding(double x, double y,double z) {
        for (ListIterator li = env.getAllBuildings().listIterator();
	    li.hasNext(); ) {
	    Building b = (Building)li.next();            
	    if(b instanceof Pavement)
		continue;
            
            if(b.contains(x, y)) {
		Debugger.debug(1,"Asset "+getID()+" move to "+fmt.format(x)+","+fmt.format(y)+","+fmt.format(z)+" would enter building "+b.getID());
		return b;
	    }
	}
	return null;
    }

    // checkBuilding basically checks if we're spatially in a building
    // and, if so, 'enters' the building, or if we're in a building
    // but spatially no longer in it, 'leaves' the building.
    private void checkBuilding() {

	// If we're already in a building, check if we've moved out of
	// the building.
	if(null != containingBuilding) {
	    if(!containingBuilding.contains(location.x,location.y)) {
		Debugger.debug(1,"Asset "+getID()+" leaving building "+getID());
		containingBuilding.removeAsset(this);
	    }
	    return;
	}

	// @TODO: This should be done right...  What we should do is
	// have some descendant Foo of GroundVehicle that is "things
	// that walk like humans", i.e. civilians, infantry, small
	// robots, and then have civ/infantry extend Foo and only
	// allow descendants of Foo into the buildings.
	if(!(this instanceof Civilian) 
	   && !(this instanceof Infantry))
	    return;

        for (ListIterator li = env.getAllBuildings().listIterator();
	    li.hasNext(); ) {
	    Building b = (Building)li.next();            
	    if(b instanceof Pavement)
		continue;
            
            if(b.contains(location.x, location.y)) {
		Debugger.debug(1,"Asset "+getID()+" entering building "+getID());
		b.addAsset(this);
		return;
	    }
	}
    }
    
    private double getRoadFactor(Vector3D loc) {
        WorldDS wds = env.getWorldDS();
        HashMap rinfos = wds.getallRDs();
        
        //Machinetta.Debugger.debug(1, "Number of RoadInfo in WorldDS: " + rinfos.size());
        
        Iterator iterator = rinfos.values().iterator();
        while( iterator. hasNext() ) {
            //Thread.sleep(3000);
            RoadInfo rinfo = (RoadInfo)iterator.next();
            //Machinetta.Debugger.debug(1, "RoadInfo - x: " + rinfo.x + "     , y: " + rinfo.y);
            //Machinetta.Debugger.debug(1, "Loc - x: " + loc.x + "     , y: " + loc.y);
            
            if(rinfo.isContain((int)loc.x, (int)loc.y)) {
                return 1.2;                
            }
        }
        
        return 1.0;
    }
    
    /** Does an update for the asset, assuming speed and heading have been set. */
    protected void _step(long time) {
        
        if (speed > 0 && heading != null && heading.length() > 0.0) {
            heading.setLength(moveDistance());
            double oldx = location.x;
            double oldy = location.y;
            double oldz = location.z;
            double factor = 1.0;
            if(!(this instanceof Aircraft)) {
                factor = getRoadFactor(new Vector3D(oldx, oldy, oldz));
            }

	    double newx = location.x + heading.x * time * factor;
            double newy = location.y + heading.y * time * factor;
            double newz = location.z + heading.z * time * factor;

// 	    if(this instanceof Munition) 
// 		Debugger.debug(1,"missile "+getID()+" oldloc="+location
// 			       +" heading="+heading
// 			       +" move="
// 			       +" "+(heading.x * time * factor)
// 			       +","+(heading.y * time * factor)
// 			       +","+(heading.z * time * factor)
// 			       +" newloc="
// 			       +" "+(location.x + heading.x * time * factor)
// 			       +","+(location.y + heading.y * time * factor)
// 			       +","+(location.z + heading.z * time * factor)
// 			       );

	    // Make sure cars/planes/anything not an infantry or
	    // civilian, doesn't drive into a building.
	    if(!(this instanceof Infantry) 
	       && !(this instanceof Civilian) 
	       && !(this instanceof Aircraft)
	       && !(this instanceof Munition)
	       ) {
		   double degreesTurned = 0;
		   int randomCount = 0;
		   while(wouldEnterBuilding(newx, newy,newz) != null) {
		       if(randomCount >= 10) {
			   Debugger.debug(2, getID()+ " seems to be stuck in a building, despite trying 45 degree turns  until we've gone all 360 degrees AND 10 random turns.  Giving up for now.  Loc="+location+" heading="+heading);
			   break;
		       }

		       if(degreesTurned  <  360) {
			   // Right turn Clyde.
			   heading.turn(-45);
			   degreesTurned += 45;
			   Debugger.debug(1, getID()+ " to avoid building turning 45 degrees for total of "+degreesTurned);
		       }
		       else {
			   // Give up, just turn randomly and hope we get out
			   // of this.
			   double randTurn = rand.nextDouble() * 360;
			   heading.turn(randTurn);
			   randomCount++;
			   Debugger.debug(1, getID()+ " to avoid building now random turning "+randomCount+"th time, to "+randTurn);
		       }

		       newx = location.x + heading.x * time * factor;
		       newy = location.y + heading.y * time * factor;
		       newz = location.z + heading.z * time * factor;
		   }
	       }

            location.x = newx;
            location.y = newy;
            location.z = newz;
            
            if(Double.isNaN(location.x)
	       || Double.isNaN(location.y)
	       || Double.isNaN(location.z)) {
                Machinetta.Debugger.debug(getID()+"_step processing resulted in a move from "
                        +oldx+","+oldy+","+oldz+" to "
                        +location.x+","+location.y+","+location.z
                        +", one or more of which is NaN, printing stack trace", 5, this);
                try {
                    throw new Exception("new loc is NaN");
                } catch(Exception e) {
                    e.printStackTrace();
                }
                
                location.x = oldx;
                location.y = oldy;
                location.z = oldz;
            }
            
	    // @TODO: NOTE - SHOULD THIS BE HIGHER UP?  IS MOUNTING
	    // THE THING WE ARE MOUNTING OR THE THING MOUNTING US?

	    // If we're inside a vehicle then don't move.
	    if(getMountingAsset() != null)
		return;

            checkBuilding();
            
	    // @TODO: this code originally only checked fuel stuff if
	    // 'this' is an instanceof smallUAV.  This appears to have
	    // introduced a bug where mounted assets don't get moved
	    // along with the asset.  I commented out that test to fix
	    // it, but it needs a closer going over.
	    //
	    //             // For test,
	    //             if(this instanceof SmallUAV) {
                
            if(this.getUseFuelFlag()) {
                
                if(this.getCurFuelAmount() >= this.getRefuelThreshold()) {
                    env.moveAsset(this);
                    this.setCurFuelAmount(this.getCurFuelAmount() - this.neededFuelAmountOneStep());
		    double diffFuel = lastFuelAmountPrint - curFuelAmount;
		    if(diffFuel > fuelAmountPrintEvery) {
			Machinetta.Debugger.debug(1, getID()+" curFuel="+fmt.format(curFuelAmount)
						  +" lowFuelLimit="+fmt.format(getRefuelThreshold())
						  +" maxFuelAmount="+fmt.format(maxFuelAmount));
			lastFuelAmountPrint = curFuelAmount;
		    }

                    // Following method contains env.moveAsset(each contained asset!)
                    changeMountedAssetsLoc(new Vector3D(location.x, location.y, location.z));
                } else if (this.getCurFuelAmount() < this.getRefuelThreshold()) {
                    sendRefuelToProxy();
                    
		    // TODO: change this to be more general - i.e.
		    // find the closest FAARP or something.

		    // This if attempts to make sure we are not
		    // ALREADY in the refueling process.  We should
		    // probably just have a flag, cause I'm entirely
		    // sure this is bulletproof.  Maybe an int with
		    // several values/steps?  on_way_to_faarp,
		    // landing_at_farp, waiting_to_refuel_at_faarp, etc.
                    if(!(this.getCurrentTask() instanceof MoveToFAARP) 
		       && !(this.getCurrentTask() instanceof Land) 
		       && !(this.getCurrentTask() instanceof AskRefuel)) {
			if(null == refuelingAsset)
			    refuelingAsset = env.getAssetByID("FAARP");
			if(null == refuelingAsset) {
			    Machinetta.Debugger.debug(1, this.getID() + " wants to refuel but can't find FAARP id FAARP");
			}
			else {
			    MoveToFAARP moveTask = new MoveToFAARP(new Vector3D(refuelingAsset.location.x, refuelingAsset.location.y, this.location.z), refuelingAsset);
			    if(tasks.size() > 0)
				interruptedTask = tasks.getFirst();
			    addTask(moveTask);
			    Machinetta.Debugger.debug(1, this.getID() + " started to move to FAARP to refuel!");
			}
                    }
                }
            } else {
                env.moveAsset(this);
                this.setCurFuelAmount(this.getCurFuelAmount() - this.neededFuelAmountOneStep());
                    
                // Following method contains env.moveAsset(each contained asset!)
                changeMountedAssetsLoc(new Vector3D(location.x, location.y, location.z));
            }
            
        } else if (this instanceof Aircraft) {
	    //            Machinetta.Debugger.debug("No heading for aircraft! " + getID(), 1, this);
        }
	routineNavigationDataToProxy();
        steps++;
        
        // Communicate with proxy
        
    }
    
    public void sendRefuelToProxy() {
	if(null == proxy) return;
	if(!hasProxy) return;
	
	// @TODO: Getting rid of Location, not sure quite what to
	// replace it with here.

// 	AirSim.Machinetta.Beliefs.Location loc;
// 	loc = new AirSim.Machinetta.Beliefs.Location((int)location.x, (int)location.y, (int)location.z,  (heading != null ? heading.angle() : 0.0), getSimTimeMs(), getPid(), getType());
// 	RPMessage msg = new RPMessage(RPMessage.MessageTypes.REFUEL, loc);
// 	msg.setCriticality(Message.Criticality.HIGH);
// 	sendToProxy(msg);
    }

    public void sendOutofFuelToProxy() {
	if(null == proxy) return;
	if(!hasProxy) return;
	RPMessage msg = new RPMessage(RPMessage.MessageTypes.OUT_OF_FUEL, this.location);
	msg.setCriticality(Message.Criticality.HIGH);
	sendToProxy(msg);
    }
        
    /**
     * Overload to change how sensor readings are reported to proxies for a particular asset (type?).
     */
    public void sendSensorReadingToProxy(SensorReading sr) {
	if(null == proxy) return;
	if(!hasProxy) return;
        RPMessage msg = new RPMessage(sr);
        msg.setCriticality(Message.Criticality.NONE);
        sendToProxy(msg);
	//	Debugger.debug(1,"sendSensorReadingToProxy: "+getID()+" sending "+sr.toString());
    }
    
    /**
     * Report current location to the proxy
     * Doesn't make much (conceptual) sense to make this smarter, since conceptually the
     * proxy and the agent are on the same processor.  Thus, this communication is very cheap
     * Machinetta should be working out when to pass locations on to others.
     *
     * Overload to change how an asset communicates with its proxy.
     */
    public void routineNavigationDataToProxy() {
	// @TODO: We should probably add something here that varies
	// reportLocationRate based on speed - i.e. if we're sitting
	// around not moving at all we should still send navdata, just
	// not as often.  If we're moving slowly, send it more often.
	// If we're moving quickly, send it more often.  Etc.  I
	// suppose another approach is to record state since last sent
	// and if not changed or not changed much, then don't send.
	if(null == proxy) return;
	if(!hasProxy) return;
	if(reportLocationRate < 0)
	    return;
	if(0 == steps)
	    return;
        if(0 != ((steps + randomStepOffset) % reportLocationRate))
	    return;
	sendNavigationDataToProxy();
    }

    // @TODO: need to go find things that send nav data and fix them
    // to sent the right priority.
    //
    // From Machinetta/Communication/Message;
    // NONE: Doesn't matter too much whether this gets through or not.
    // Of course, we wouldn't be sending it if was completely useless
    // but don't stress about getting it through.
    public void sendNavigationDataToProxy() {
	sendNavigationDataToProxy(Message.Criticality.NONE);
    }

    // From Machinetta/Communication/Message;
    // LOW: More routine message, coordination needs this message to
    // get through but likely things will work fine even if it doesn't
    // get through.  Communication should try to get the message
    // through, but prioritize other things.
    public void sendNavigationDataToProxyLow() {
	sendNavigationDataToProxy(Message.Criticality.LOW);
    }

    // From Machinetta/Communication/Message;
    // MEDIUM: Default level, coordination is going to break if this
    // message doesn't get through, but in a fixable and
    // non-catastrophic way.  Communication should really endeavour to
    // get it through.
    public void sendNavigationDataToProxyMed() {
	sendNavigationDataToProxy(Message.Criticality.MEDIUM);
    }

    // From Machinetta/Communication/Message;
    // HIGH: This message must get through, communication should do
    // everything it can to get it through.
    public void sendNavigationDataToProxyHigh() {
	sendNavigationDataToProxy(Message.Criticality.HIGH);
    }

    public void sendNavigationDataToProxy(Message.Criticality criticality) {
	if(null == proxy) return;
	if(!hasProxy) return;

	// SEE BELOW, if mountingAsset!=null then send it's location
	// instead of ours.
	//
// 	// @TODO: Wed Feb 18 22:25:43 EST 2009 - SRO - I added this
// 	// because I couldn't get Infantry in the HMMWV's and the
// 	// C130's to sit still, so when those vehicles were moving
// 	// they traces for the Infantry were not exactly the same as
// 	// the vehicles, resulting in a thick fuzzy line for the
// 	// trace.  This is less than ideal because really they should
// 	// still report to Operator "I'm alive, I'm mounted" etc.
// 	if(null != mountingAsset) return;

	long simTimeMsNow = getSimTimeMs();
	Waypoint expWp = getExpectedWaypoint(simTimeMsNow);
	double distFromExp = -1;
	if(null != expWp) {
	    distFromExp = location.toVector(expWp).length();
	}
	double curFuelRatio;
	if(0 == getMaxFuelAmount())
	    curFuelRatio = 0;
	else 
	    curFuelRatio = getCurFuelAmount()/getMaxFuelAmount();
        int isMounted = ((getMountingAsset() == null) ? 0 : 1);
        int isLive = ((isLive() == true) ? 1 : 0);
        

	NavigationDataAP msg = new NavigationDataAP();
	if(null != mountingAsset) {
	    msg.xMeters = mountingAsset.location.x;
	    msg.yMeters = mountingAsset.location.y;
	    msg.zMeters = mountingAsset.location.z;
	}
	else {
	    msg.xMeters = location.x;
	    msg.yMeters = location.y;
	    msg.zMeters = location.z;
	}
	if(heading  == null) {
	    msg.headingDegrees = 0;
	    msg.verticalVelocity = 0;
	}
	else  {
	    msg.headingDegrees = heading.angle();
	    msg.verticalVelocity = heading.z;
	}
	msg.groundSpeed = getSpeed();
	msg.curFuelRatio = curFuelRatio;
        msg.isMounted = isMounted;
        msg.isLive = isLive;
	msg.type = this.getType();
	msg.state = state;
	msg.forceId = forceId;
	msg.armor = armor;
	msg.damage = damage;
	msg.setCriticality(criticality);

	sendToProxy(msg);
	//	Machinetta.Debugger.debug(1, "NavData: "+getID()+" sent to proxy: "+msg);
    }

    // @TODO: Mon Nov 5 23:26:27 EST 2007 SRO - The entire set of
    // detonated, explosion, causeExplosion, causeExplosionAt,
    // directFireAt, etc is a tad confusing, need to sort that out and
    // make it clear how they work.  Should be separated into two
    // categories 1) methods that reflect 'this' exploding, and 2)
    // methods that reflect some other asset exploding and having an
    // effect on 'this'.  For that matter, we should probably
    // distinguish between 'detonate' - meaning cause an explosion and
    // then remove yourself from the simulator, versus other
    // explosions - i.e. when a tank is killed but the dead tank
    // sticks around.

    /** When self explodes, uses location here. */
    public void causeExplosion(double dist) {
        causeExplosionAt(location, dist, this);
    }
    
    public void causeExplosionAt(Vector3D location, double dist) {
        causeExplosionAt(location, dist, this);
    }
    
    private boolean detonated = false;
    public boolean isDetonated() { return detonated; }
    private double detonatedRange = 0.0;

    /** Causes explosion at location, can be used for direct fire,
     * if an additional asset class of "DirectFire" or something is
     * created.
     */
    public void causeExplosionAt(Vector3D location, double dist, Asset munition) {
	env.setEvent("being exploded on by "+munition.getID(), this);
	if( (this == munition) && !(munition instanceof Infantry) ) {
	    detonated = true;
	    detonatedRange = dist;
	}
        // Need to check for damage
        for (ListIterator li = env.getAssetsInBox((int)(location.x - dist/2.0), (int)(location.y - dist/2.0), (int)(location.z - dist/2.0), (int)(location.x + dist/2.0), (int)(location.y + dist/2.0), (int)(location.z + dist/2.0)).listIterator(); li.hasNext(); ) {
	    // @TODO: should probably check actual distance between
	    // asset and munition.  Should probably also do something
	    // to factor in explosive power of the munition and actual
	    // range from munition to asset - an asset at the edge of
	    // the blast won't be damaged as much as an asset at the
	    // heart of the explosion.
            Asset a = (Asset)li.next();
	    if(a.state == State.DESTROYED)
		continue;
            if (!(munition instanceof Infantry) && a != munition) {
                a.explosion(munition);
                //return true;
            }
            else if (munition instanceof Infantry && a != munition && (a.forceId != munition.forceId))
                a.directFiredBy(munition);
        }
        
        for (ListIterator li = env.getBuildingsInArea((int)(location.x - dist/2.0), (int)(location.y - dist/2.0), (int)(location.x + dist/2.0), (int)(location.y + dist/2.0)).listIterator(); li.hasNext(); ) {
	    // @TODO: should probably check actual distance between
	    // asset and munition.  Should probably also do something
	    // to factor in explosive power of the munition and actual
	    // range from munition to asset - an asset at the edge of
	    // the blast won't be damaged as much as an asset at the
	    // heart of the explosion.
            Building b = (Building)li.next();
            if (!(munition instanceof Infantry)) {
                b.explosion(munition);
                //return true;
            }
            else if (munition instanceof Infantry)
                b.directFiredBy(munition);
        }
    }

    private boolean directFiring = false;
    public boolean isDirectFiring() { return directFiring; }
    public void setDirectFiring(boolean value) { directFiring = value; }
    
    private Asset directFireTargetAsset = null;
    public Asset getDirectFireTargetAsset() { return directFireTargetAsset; }
    private Vector3D directFireTarget = null;
    public Vector3D getDirectFireTarget() { return directFireTarget; }


        
    public void directFireAt(Asset them) {
	firedAt(them);
	them.directFiredBy(this);
    }

    /** This asset is fired by a. */
    public void directFiredBy(Asset a) {
	
	directFiring = true;
	directFireTarget = new Vector3D(a.location);
	directFireTargetAsset = a;
	// @TODO: no assets have weapons and I don't have time to set
	// them up now.
        if ((a instanceof Infantry  || a instanceof AUGV )&& env.getCumulativeDamageMode()) {
            double threshold = 0.5;
        
            if(damage >= armor) {
		Machinetta.Debugger.debug(1, "directFiredBy: "+a.getIDGroup()+" kills "+getIDGroup()+", damage="+damage+", armor="+armor+", Should have died last time around");
                killedBy(a);
            } else { 
                if(rand.nextGaussian() > threshold) {
		    
		    // @TODO: HACK HACK HACK
		    // Do this properly.
		    //		    double newDamage = a.getWeapon().getDamage();
		    double newDamage = HACK_DEFAULT_INFANTRY_DAMAGE;
		    if((a.getForceId() == ForceId.BLUEFOR)
		       && (a instanceof Infantry)) {
			newDamage = HACK_BLUEFOR_INFANTRY_DAMAGE;
		    }
		    damage += newDamage;
		    
		    try {
		    if(damage >= armor) {
			Machinetta.Debugger.debug(1, "directFiredBy: "+a.getIDGroup()+" kills "+getIDGroup() +" new damage="+newDamage+", total damage="+damage+", armor="+armor);
			killedBy(a);
		    }
		    else {
			Machinetta.Debugger.debug(1, "directFiredBy: "+a.getIDGroup()+" fired on "+getIDGroup() +" new damage="+newDamage+", total damage="+damage+", armor="+armor+", not dead.");

		    }
		    }
		    catch(Exception e) {
			Machinetta.Debugger.debug(3, "Exception e="+e);
			e.printStackTrace();
		    }
                }
		else {
		    Machinetta.Debugger.debug(1, "directFiredBy: "+a.getIDGroup()+" fired on "+getIDGroup()+" MISSED, total damage="+damage+", armor="+armor+", not dead.");
		}
           }
        } else {
	    double roll = rand.nextGaussian();
            if(roll > armor) {
		Machinetta.Debugger.debug(1, "directFiredBy: "+a.getIDGroup()+" kills "+getIDGroup()+", noncumulative damage mode roll = "+roll);
                killedBy(a);
            }
        }
    }
    
    /** The asset a has exploded, update as required. */
    public void explosion(Asset a) {
        // killedBy(a);
        if(rand.nextGaussian() > armor) {
            killedBy(a);
        }
    }
    
    public void weSeeThem(Asset them) {
        if(them.forceId == this.forceId)
            return;
        weSeeThemTotal++;
        them.theySeeUsTotal++;
        if(null == weSeeThemUnique.get(them.getID())) {
            them.theySeeUsUnique++;	// First time we've seen them, so bump their "theySeeUsUnique" counter
            weSeeThemUnique.put(them.getID(), them);
        }
    }
    
    public void firedAt(Asset them) {
	env.setEvent("fired at "+them.getID(),this);
        shotsFired++;
        them.firedAtBy(this);
    }
    
    public void firedAtBy(Asset them) {
        beingShotAt = true;
        stepLastShotAt = env.getStep();
        lastShotAtBy = them;
        shotAtCounter++;
    }
    
    public void died() {
 	if(State.DESTROYED == state)
	    return;
	if(damage < armor)
	    damage = armor;
	env.setEvent("Died!", this);
	if(!(this instanceof Munition)) {
	    env.amDead(this);
	}
        state = State.DESTROYED;
	Machinetta.Debugger.debug(getID()+" has died.", 2, this);
        setSpeed(0.0);
        heading = new Vector3D(0.0, 0.0, -1.8288);	// 6 feet = 1.8288 meters
    }
    
    public void killedBy(Asset killer) {
        if(State.DESTROYED == state) 
	    return;
	env.setEvent("Killed by "+killer.getID(), this);
	died();
	killer.kills++;
	Machinetta.Debugger.debug(getID()+"."+getGroup()+" at "+location+" KILLED by "+killer.getID()+"."+killer.getGroup(), 2, this);
            
	Iterator iterator = this.mountedAssets.keySet().iterator();
	// kill all!
	if(env.getContainedKillOpt() == 0) {                
	    while( iterator. hasNext() ) {
		Asset a = env.getAssetByID((String)iterator.next());
		if(a.state == State.DESTROYED)
		    continue;
		a.died();
		killer.kills++;
		Machinetta.Debugger.debug(a.getID()+" at "+location+" KILLED by "+killer.getID(), 2, this);
	    }
	} else if(env.getContainedKillOpt() == 1) {
	    //Do nothing (because mode 1 means do not kill any contained asset.                
	    while( iterator. hasNext() ) {
		Asset a = env.getAssetByID((String)iterator.next());
		a.setMountingAsset(null);
	    }
	} else if(env.getContainedKillOpt() == 2) {
	    // Kill contained assets based on some probability distributions.                
	    while( iterator. hasNext() ) {
		Asset a = env.getAssetByID((String)iterator.next());
		if(a.state == State.DESTROYED)
		    continue;
		if(rand.nextGaussian() > 0.5) {                        
		    a.died();
		    killer.kills++;
		    Machinetta.Debugger.debug(a.getID()+" at "+location+" KILLED by "+killer.getID(), 2, this);
		} else {
		    a.setMountingAsset(null);
		}
	    }
	}
    }
    
    private boolean isMunitionRemove = false;
    public boolean getIsMunitionRemove() { return isMunitionRemove; }
    public void setIsMunitionRemove(boolean isMunitionRemove) { this.isMunitionRemove = isMunitionRemove; }
    
    public void suicide(boolean removeFromEnv, String reason) {
        died();
        if(removeFromEnv) {
            Machinetta.Debugger.debug(getID()+": Asset has died: "+reason+" : Change flag to remove self from Env.", 2, this);
            //env.removeAsset(this);
            isMunitionRemove = true;
        } else {
            Machinetta.Debugger.debug(getID()+": Asset has died: "+reason+" : Leaving self in Env.", 2, this);
        }
    }
    
   /*------------------
    * Accessors
    *-----------------*/
    
    public abstract Types getType();
    
    public void addSensor(Sensor s) { sensors.add(s); }
    
    public boolean hasGoal() {
        if(tasks.size() > 0) {
            if(tasks.get(0) instanceof Move) {
                return true;
            }
        }
        return false;
    }
    
    public double distanceToGoal() {
        if(tasks.size() > 0) {
            if(tasks.get(0) instanceof Move) {
                Move moveTask = (Move)tasks.get(0);
                return moveTask.distanceToGoal(this);
            }
        }
        return -1;
    }
    
    public boolean atGoal() {
        if(State.LIVE != state)
            return false;
        double dist = distanceToGoal();
        if(-1 == dist)
            return false;
        if(dist < AT_GOAL_DISTANCE)
            return true;
        return false;
    }
    
    public static String getStatHeaderString() {
        return " |ID\t"
	    +"type\t"
	    +"state\t"
	    +"shotat\t"
	    +"they_see_us_total\t"
	    +"they_see_us_unique\t"
	    +"shots_fired\t"
	    +"we_see_them_total\t"
	    +"we_see_them_unique\t"
	    +"distance_to_goal\t"
	    +"unformatted_distance\t"
	    +"forceId\t"
	    +"armor\t"
	    +"damage\t"
	    +"navmsg\t"
	    +"sensmsg";
    }
    public String getStatString() {
        StringBuffer stats = new StringBuffer(" |");
        stats.append(getID());
        stats.append("\t").append(getType());
        stats.append("\t").append(state);
        stats.append("\t").append(shotAtCounter);
        stats.append("\t").append(theySeeUsTotal);
        stats.append("\t").append(theySeeUsUnique);
        stats.append("\t").append(shotsFired);
        stats.append("\t").append(weSeeThemTotal);
        stats.append("\t").append(weSeeThemUnique.size());
        if(tasks.size() > 0) {
            if(tasks.get(0) instanceof Move) {
                Move moveTask = (Move)tasks.get(0);
                stats.append("\t").append(fmt.format(moveTask.distanceToGoal(this)));
                stats.append("\t").append(moveTask.distanceToGoal(this));
            }
	    else {
		stats.append("\t0");
		stats.append("\t0");
	    }
        } else {
            stats.append("\t0");
            stats.append("\t0");
        }
        stats.append("\t").append(forceId);
        stats.append("\t").append(fmt.format(armor));
        stats.append("\t").append(fmt.format(damage));
        stats.append("\t").append(navdataSent);
        stats.append("\t").append(sensorSent);
        return stats.toString();
    }
    
    public Memory getMemory() { return memory; }
    
    public boolean isLive() { return State.LIVE == state; }
    public boolean isDead() { return State.DESTROYED == state; }
    public String getStateString() {
        if(State.LIVE == state)
            return "Live";
        else if(State.DESTROYED == state)
            return "Dead";
        else
            return "Unknown";
    }
    
    // GETTERS AND SETTERS

    public HashMap getMountedAssets() { return this.mountedAssets; }
    public void setMountedAssets(HashMap mountedAssets) { this.mountedAssets = mountedAssets; }

    public Asset getMountingAsset() { return mountingAsset; }
    public void setMountingAsset(Asset mountingAsset) { this.mountingAsset = mountingAsset; }

    public Building getContainingBuilding() { return containingBuilding; }
    public void setContainingBuilding(Building containingBuilding) { this.containingBuilding = containingBuilding; }

    public static void setDefaultAtLocationDistance(double value) { 
//         Machinetta.Debugger.debug(2,"Changing atLocationDistance from "+atLocationDistance+" to "+value);
// 	Exception e = new Exception();
// 	e.printStackTrace();
	defaultAtLocationDistance = value; 
    }
    public void setAtLocationDistance(double value) { 
	atLocationDistance = value; 
    }
    public double getAtLocationDistance() { return atLocationDistance;}

    public static void setMoveFinishedHoldRadius(double value) { moveFinishedHoldRadius = value; }
    public double getMoveFinishedHoldRadius() { return moveFinishedHoldRadius;}

    public static void setEndOfPathAtStep(long value) { endOfPathAtStep = value; }
    public long getEndOfPathAtStep() { return endOfPathAtStep;}

    public int getStep() { return env.getStep();}
    public long getSimTimeMs() { return env.getSimTimeMs();}

    public double getCurFuelAmount() { return curFuelAmount; }
    public void setCurFuelAmount(double curFuelAmount) { this.curFuelAmount = curFuelAmount; }

    public double getMaxFuelAmount() { return maxFuelAmount; }
    public void setMaxFuelAmount(double maxFuelAmount) { this.maxFuelAmount = maxFuelAmount; }

    public double getFuelConsumingRatio() { return fuelConsumingRatio; }
    public void setFuelConsumingRatio(double fuelConsumingRatio) { this.fuelConsumingRatio = fuelConsumingRatio; }

    public Asset getRefuelingAsset() { return refuelingAsset; }
    public void setRefuelingAsset(Asset refuelingAsset) { this.refuelingAsset = refuelingAsset; }

    public double getRefuelThreshold() { return refuelThreshold; }
    public void setRefuelThreshold(double refuelThreshold) { this.refuelThreshold = refuelThreshold; }

    public boolean getUseFuelFlag() { return useFuelFlag; }
    public void setUseFuelFlag(boolean useFuelFlag) { this.useFuelFlag = useFuelFlag; }

    public boolean getDroppedFlag() { return droppedFlag; }
    public void setDroppedFlag(boolean droppedFlag) { this.droppedFlag = droppedFlag; }

    public boolean getHasProxy() { return hasProxy; }
    public void setHasProxy(boolean value) { hasProxy = value; }

    public Vector3D getLocation() { return location; }
    public void setLocation(Vector3D value) { location = value; }
    public String locAsString() { return location.toString(); }

    public boolean getDontReportSense() { return dontReportSense; }
    public void setDontReportSense(boolean value) { dontReportSense = value; }

    public boolean isFireAtAirCapable() { return fireAtAirCapable; }
    public boolean isFireAtSurfaceCapable() { return fireAtSurfaceCapable; }

    public NamedProxyID getPid() {return pid;}
    public void setPid(NamedProxyID pid) {this.pid = pid;}

    public ForceId getForceId() { return forceId; }
    public void setForceId(ForceId value) { forceId = value; }
    
    public boolean isCollision() { return collision; }
    public void setCollision(boolean value) { collision = value; }
    
    public String getID() { return id; }
    
    public double getSpeed() { return speed; }
    public void setSpeed(double speed) { this.speed = speed; }
    
    public void setStepsBetweenFiring(int value) {this.stepsBetweenFiring = value;}
    public int getStepsBetweenFiring() { return stepsBetweenFiring; }
    
    public void setStepLastFired(long value) {this.stepLastFired = value;}
    public long getStepLastFired() { return stepLastFired; }

    public boolean isReadyToFire() { return ((stepLastFired + stepsBetweenFiring) > steps); }
    
    public String getGroup() { return group; }
    public String getIDGroup() { return getID()+"."+getGroup(); }
    
    public void setGroup(String group) {
        Machinetta.Debugger.debug("Set group to " + group, 1, this);
        this.group = group;
    }
    
    public boolean isVisibleOnGT() { return visibleOnGT; }
    public void setVisibleOnGT(boolean visibleOnGT) {
        this.visibleOnGT = visibleOnGT;
    }
    
    public boolean isTasked() { return (tasks.size() > 0); }

    public boolean isFinished() { return (tasks.size() > 0) && (tasks.get(0).finished(this)); }

    
    /*------------------
     * Proxy Comms
     *-----------------*/
    /*-- For connecting to the proxy */
    public static void setProxy(ProxyServer c) {proxy = c;}
    
    public void msgFromProxy(AirSim.Machinetta.Messages.PRMessage msg) {
        switch (msg.type) {
            case NEW_TASK:
                Machinetta.Debugger.debug("msgFromProxy: "+getID()+" given a new task: " + msg.params.get("Task"),1,this);
                TaskType type = (TaskType)msg.params.get("Task");
                if (type != null) {
                    switch(type) {
                        
                        // Move
		    case move: {
			Machinetta.Debugger.debug(1,"msgFromProxy: "+getID()+" Creating move task");
			Vector3D loc = (Vector3D)msg.params.get("Location");
			Path2D path = (Path2D)msg.params.get("Path");
			if (path == null) Machinetta.Debugger.debug("msgFromProxy: "+getID()+" Move behavior did not get path!",1,this);
			if (loc != null) {
			    Move moveTask = new Move(loc, path);
			    addTask(moveTask);
			} else {
			    Machinetta.Debugger.debug("msgFromProxy: "+getID()+" No location given in move task request?? Ignoring.  Message: " + msg,1,this);
			}
		    }
			break;
                            
			// Patrol
		    case patrol: {
			Machinetta.Debugger.debug(1,"msgFromProxy: "+getID()+" Creating patrol task");
			Area area = (Area)msg.params.get("Area");
			Boolean spiralMode = (Boolean)msg.params.get("SpiralMode");
			if(null == spiralMode) spiralMode = Boolean.FALSE;
			if (area != null) {
			    Patrol patrol = new Patrol(area, this, spiralMode);
			    addTask(patrol);
			} else {
			    Machinetta.Debugger.debug("msgFromProxy: "+getID()+" No area given in patrol task?? Ignoring.  Message: " + msg,1,this);
			}
		    }
			break;
                            
		    case attack: {
			Machinetta.Debugger.debug(1,"msgFromProxy: id="+getID()+" Creating attack task");
			Vector3D loc = (Vector3D)msg.params.get("Location");
			if (loc != null) {
			    //NOTE: should I change Attack to MissileFire in this part?
			    Attack attackTask = new Attack(loc);
			    addTask(attackTask);
			} else {
			    Machinetta.Debugger.debug("msgFromProxy: "+getID()+" No location given in attack task request?? Ignoring.  Message: " + msg,1,this);
			}
		    }
			break;

		    case transport: {
			Vector3D loc = (Vector3D)msg.params.get("Location");
			if (loc != null) {
			    Transport transportTask = new Transport(loc);
			    addTask(transportTask);
			} else {
			    Machinetta.Debugger.debug("msgFromProxy: "+getID()+" No location given in transport task request?? Ignoring.  Message: " + msg,1,this);
			}
		    }
			break;

		    case airdrop: {
			Vector3D loc = (Vector3D)msg.params.get("Location");
			Vector3D base = (Vector3D)msg.params.get("Base");
			Double atDestRange = (Double)msg.params.get("AtDestRange");
			if ((loc != null) && (base != null) && (atDestRange != null)) {
			    Airdrop airdropTask = new Airdrop(loc,base,atDestRange);
			    addTask(airdropTask);
			} else {
			    Machinetta.Debugger.debug("msgFromProxy: "+getID()+" No location given in transport task request?? Ignoring.  Message: " + msg,1,this);
			}
		    }
			break;
                            
		    case hold: {
			addTask(new Hold());
		    }
			break;
                            
		    case attackFromAir: {
			Vector3D p = (Vector3D)msg.params.get("Location");
			if (p != null) {
			    LinkedList poss = env.getAssetsInBox((int)(p.x-1000), (int)(p.y-1000), -10, (int)(p.x + 1000), (int)(p.y + 1000), 10);
			    if (poss.size() > 0) {
				GroundVehicle targetVehicle = null;
				double smallestDistSqd = Double.MAX_VALUE;
				for(Object possTarget: poss) {
				    if(!(possTarget instanceof GroundVehicle))
				       continue;
				    GroundVehicle pTarget = (GroundVehicle)possTarget;
				    double distSqd = pTarget.location.toVectorLengthSqd(p.x, p.y,pTarget.location.z);
				    if(distSqd < smallestDistSqd) {
					smallestDistSqd = distSqd;
					targetVehicle = pTarget;
				    }
				}
				if(targetVehicle != null) {
				    Machinetta.Debugger.debug(3,"msgFromProxy: "+getID()+" attackFromAir - Firing Air to Surface missile at "+targetVehicle);
				    fireASMissile(targetVehicle);
				}
				else 
				    Machinetta.Debugger.debug(3,"msgFromProxy: "+getID()+" attackFromAir - could not find a target vehicle!");
			    } else {
				Machinetta.Debugger.debug(1,"msgFromProxy: attackFromAir - No target vehicles near target location");
			    }
			} else {
			    Machinetta.Debugger.debug(1,"msgFromProxy: "+getID()+" attackFromAir - No location given in attackFromAir request");
			}
		    }
			break;


                        default:
                            Machinetta.Debugger.debug("msgFromProxy: "+getID()+" Proxy requested unimplemented task type: " + type + ".  Sorry.",1,this);
                    }
                } else {
                    Machinetta.Debugger.debug("msgFromProxy: "+getID()+" New task requested, but no type found: " + msg,1,this);
                }
                break;
            case NEW_WAYPOINT:
                
                NextWaypointPA nwMsg = (NextWaypointPA)msg;
                
                Vector3D nwp = new Vector3D(nwMsg.longtitude, nwMsg.latitude, nwMsg.altitude);
                addTask(new Move(nwp));
                
                Machinetta.Debugger.debug("msgFromProxy: "+getID()+" given a new waypoint: " + nwp, 1, this);
                
                break;
            default:
                Machinetta.Debugger.debug("msgFromProxy: "+getID()+" got meaningless message: " + msg,3,this);
        }
    }
    
    public int navdataSent = 0;
    public int sensorSent = 0;
    
    public void sendToProxy(RPMessage msg) {
	if(!hasProxy) return;
	if(null == proxy) return;
        if(NO_COMMS)
            return;
	if(msg.type == MessageTypes.NAVIGATION_DATA) {
	    navdataSent++;
	}
	else if((msg.type == MessageTypes.SENSOR)
		|| (msg.type == MessageTypes.SAR_SENSOR)
		|| (msg.type == MessageTypes.SEARCH_SENSOR_READING)
		|| (msg.type == MessageTypes.EOIR_SENSOR_READING)
		|| (msg.type == MessageTypes.RSSI_SENSOR_READING)
		|| (msg.type == MessageTypes.DIRECTIONAL_RF_SENSOR_READING))
	    sensorSent++;

	if((msg.type != MessageTypes.NAVIGATION_DATA)
// 	    && (msg.type != MessageTypes.SENSOR)
// 	    && (msg.type != MessageTypes.SAR_SENSOR)
// 	    && (msg.type != MessageTypes.SEARCH_SENSOR_READING)
// 	    && (msg.type != MessageTypes.EOIR_SENSOR_READING)
	    && (msg.type != MessageTypes.RSSI_SENSOR_READING)
	    && (msg.type != MessageTypes.DIRECTIONAL_RF_SENSOR_READING)
	   ) {
// 	    if(msg instanceof RPMessage) {
// 		RPMessage rpMsg = (RPMessage)msg;
// 		if(rpMsg.type == RPMessage.MessageTypes.SENSOR) {
// 		    SensorReading foo = (SensorReading)rpMsg.params.get(0);
// 		    if(foo.forceId == null) {
// 			Machinetta.Debugger.debug(1,"id="+id+" Sending sensor reading with null forceid to proxy, printing stacktrace");
// 			new Exception().printStackTrace();
// 		    }
// 		}
// 	    }
	    Machinetta.Debugger.debug(1,"id="+id+" Sending to proxy: " + msg);
	}
	proxy.send(msg, id);
    }
    
    // @todo: SRO Thu Apr 13 22:23:09 EDT 2006
    // 
    // The more I think about it the more we probably want to
    // componentize things... i.e. much like the sensors, have each
    // asset have a map/list of what weapons it has, with each such
    // weapon having its own characteristics, firing rate, ammunition
    // count, and SS or SA capability.

    
    // @TODO: SRO Tue Feb 10 22:06:59 EST 2009
    // 
    // Really we should have missile subclasses and specify which kind
    // of missile to fire...  Or I guess that would really be, we
    // should have a weapon and then ask it to fire missiles.  Anyhow,
    // this is the current speed/maxspeed in SAMissile.  I'm changing
    // this so I can have the SA9 override this value with the proper
    // value (1.8 MACH == 1379 mph) for a SA9 Gaskin 9m31m missile.
    protected double ssmMaxSpeedMetersPerSecond = mphToms(800);
    protected double samMaxSpeedMetersPerSecond = mphToms(800);
    protected double asmMaxSpeedMetersPerSecond = mphToms(800);

    /** Fires a new surface-to-surface missile at targetLoc.
     *
     * Relies on asset knowing whether this is possible or not.
     *
     * @todo Probably move this all the way to Asset class?
     */
    public void fireSSMissile(GroundVehicle a) {
	String missileID = id+"M"+(missilesFired++);
	Waypoint targetLoc = new Waypoint((double)a.location.x, a.location.y, a.location.z);
	SSMissile missile = new SSMissile(missileID, (int)location.x, (int)location.y, targetLoc, a, ssmMaxSpeedMetersPerSecond);
	double dist = location.toVectorLength(a.location);
	Debugger.debug(1,"fireSSMissle: id="+id+" at "+location+" firing missile "+missileID+" at target "+a.getID()+" at"+a.location+" dist "+dist);
	missile.setForceId(forceId);
	setStepLastFired(steps);
	firedAt(a);
    }
  
    public void fireSAMissile(Aircraft a) {
	String missileID = id+"M"+(missilesFired++);
	SAMissile missile = new SAMissile(missileID, (int)location.x, (int)location.y, a, samMaxSpeedMetersPerSecond);
	double dist = location.toVectorLength(a.location);
	Debugger.debug(1,"fireSAMissle: id="+id+" at "+location+" firing missile "+missileID+" at target "+a.getID()+" at"+a.location+" dist "+dist);
	//	missile.setBlueForce(blueForce);
	missile.setForceId(forceId);
	setStepLastFired(steps);
	firedAt(a);
    }
    
    public void fireASMissile(GroundVehicle a) {
	String missileID = id+"M"+(missilesFired++);
	ASMissile missile = new ASMissile(missileID, (int)location.x, (int)location.y, (int)location.z, a, asmMaxSpeedMetersPerSecond);
	double dist = location.toVectorLength(a.location);
	Debugger.debug(1,"fireASMissle: id="+id+" at "+location+" firing missile "+missileID+" at target "+a.getID()+" at"+a.location+" dist "+dist);
	missile.setForceId(forceId);
	setStepLastFired(steps);
	firedAt(a);
    }
    
    private int missilesFired = 0;
    
    /*------------------
     * Some static helper functions
     *-----------------*/
    
    /** Conversions for speeds */
    public final static double mphToms(double mph) { return kphToms(mph * 1.6); }
    /** Conversions for speeds */
    public final static double kphToms(double kph) { return kph * 1000.0 / 3600.0; }
    public final static double msTokph(double ms) { return ms * 3600.0/1000.0; }
    public final static double msTomph(double ms) { return ms * 3600.0/1000.0 /1.6; }
    // according to google, mach 1 = 761.207051 mph = 1 225.044 kph
    // (The 'Mach' number for air really depends on air density which
    // means altitude and temperature, but this is good enough for
    // now.)
    public final static double machTokph(double mach) { return mach * 1225.044; }
    public final static double machTomph(double mach) { return mach * 761.207051; }
    
    
    // For generating data sets for information fusion experiments.
    public void logSensorReading(ConfusionMatrixReading ssr, Asset detectedAsset) {
        if(!LOG_SENSOR_READINGS)
            return;
        if(null != ssr) {
            Machinetta.Debugger.debug("SENSORLOG:"
                    +env.getStep()+"\t"
                    +getType()+"\t"
                    +getID()+"\t"
                    +fmt.format(location.x)+"\t"
                    +fmt.format(location.y)+"\t"
                    +fmt.format(location.z)+"\t"
                    +detectedAsset.getType()+"\t"
                    +detectedAsset.getID()+"\t"
                    +fmt.format(detectedAsset.location.x)+"\t"
                    +fmt.format(detectedAsset.location.y)+"\t"
                    +fmt.format(detectedAsset.location.z)+"\t"
                    +ssr.getState()+"\t"
                    +ssr.probsToString()
                    , 2, this);
        }
    }

    // Get the ratio of those assets (within specified range) that are
    // non-dead and opposing forceid, to assets that are the ratio of
    // non-dead and same forceid.  
    // 
    // @param radius radius in which to check for assets
    // @param altitude altitude in which to check for assets
    // @param useWeaponDamage if true, measure weapon damage ratio instead of just counting
    // 
    // @return ratio of force on my side vs force on their side - if
    // no force at all on their side then returns 100.
    protected double getForceRatio(double radius, double altitude, boolean useWeaponDamage) {
	double radiusSqd = radius*radius;

	double forceMySide = 0.0;
	double forceOtherSide = 0.0;

	int skipDeadCount = 0;
	int skipCivCount = 0;
	int skipMunCount = 0;
	int skipWrongTypeCount = 0;
	int skipDistCount = 0;
	int skipBuildingCount = 0;
	int mySideCount = 0;
	int otherSideCount = 0;
        LinkedList assetList = env.getAssetsInBox((int)(location.x - radius), (int)(location.y - radius), -10, (int)(location.x + radius), (int)(location.y + radius), (int)altitude);
	ListIterator li = assetList.listIterator();
        while(li.hasNext()) {

            Asset a = (Asset)li.next();
	    
            if(a.state == State.DESTROYED) {
		skipDeadCount++;
                continue;
	    }
            if(a.getType() == Asset.Types.CIVILIAN) {
		skipCivCount++;
                continue;
	    }
	    if(a instanceof Munition) {
		skipMunCount++;
		continue;
	    }

	    // Things on the ground ignore aircraft
	    if(this instanceof GroundVehicle) {
		if((a.getType() == Asset.Types.C130) 
		    || (a.getType() == Asset.Types.SMALL_UAV) ) {
		    skipWrongTypeCount++;
		    continue;
		}
	    }
	    else if(this instanceof Aircraft) {
		if((a.getType() == Asset.Types.INFANTRY)
		   || (a.getType() == Asset.Types.AUGV)
		    || (a instanceof Tank)) {
		    skipWrongTypeCount++;
		    continue;
		}
	    }
	    
	    // We should count ourselves in the estimate of force
	    // ratio , but obviously we can't compute distance to
	    // ourself; also if anyone is standing on the exact same
	    // spot as I am (can happen when more than one asset is
	    // mounted on the same mount) the same thing happens.
	    if(!this.location.equals(a.location)) {
		double distSqd = this.location.toVectorLengthSqd(a.location);
		if(distSqd > radiusSqd) {
		    skipDistCount++;
		    continue;
		}
	    }

	    // If we're not both outside or both in the same building
	    if(a.getContainingBuilding() != getContainingBuilding()) {
		// check if he's in a building that isn't observeable
		if(a.getContainingBuilding() != null) {
		    Building b = a.getContainingBuilding();
		    if(!b.getContentsVisible()) {
			skipBuildingCount++;
			continue;
		    }
		}
		// check if I'm in a building that isn't observeable
		if(getContainingBuilding() != null) {
		    Building b = getContainingBuilding();
		    if(!b.getContentsVisible()) {
			skipBuildingCount++;
			continue;
		    }
		}
	    }

	    // NOTE: This calculation does not take into account
	    // confusion - i.e. it assumes we can clearly see
	    // everyone.
	    
	    double force = 0.0;
	    if(env.getCumulativeDamageMode()) 
		force = a.getWeapon().getDamage();
	    else
		force = 1.0;
	    
	    if(a.forceId == this.forceId) {
		forceMySide += force;
		mySideCount++;
	    }
	    else {
		forceOtherSide += force;
		otherSideCount++;
	    }
	}
	
	double ratio = 100.0;
	// avoid divide by zero
	if(0.0 != forceOtherSide)
	    ratio= forceMySide/forceOtherSide;

	Debugger.debug(1, "getForceRatio: "+getID()+" ratio="+ratio+", found "+assetList.size()+" assets in radius box, mySideCount="+mySideCount+", otherSideCount="+otherSideCount+", skipDeadCount="+skipDeadCount+", skipCivCount="+skipCivCount+", skipMunCount="+skipMunCount+", skipWrongTypeCount="+skipWrongTypeCount+", skipDistCount="+skipDistCount+", skipBuildingCount="+skipBuildingCount);

	return ratio;
    }
}
