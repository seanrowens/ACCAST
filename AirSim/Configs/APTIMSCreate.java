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
 * L3Create.java
 *
 * Created on February 7, 2006, 5:24 PM
 *
 */

package AirSim.Configs;

import AirSim.Environment.Assets.Tasks.TaskType;
import AirSim.Machinetta.BasicRole;
import Machinetta.State.BeliefID;
import Machinetta.State.BeliefNameID;
import Machinetta.State.BeliefType.Belief;
import Machinetta.State.BeliefType.CapabilityBelief;
import Machinetta.State.BeliefType.Match.MatchCondition;
import Machinetta.State.BeliefType.NamedProxyID;
import Machinetta.State.BeliefType.ProxyID;
import Machinetta.State.BeliefType.RAPBelief;
import Machinetta.State.BeliefType.TeamBelief.Associates;
import Machinetta.State.BeliefType.TeamBelief.Constraints.DirectedInformationRequirement;
import Machinetta.State.BeliefType.TeamBelief.TeamBelief;
import Machinetta.State.BeliefType.TeamBelief.TeamPlanTemplate;
import Machinetta.State.ProxyState;
import java.awt.Rectangle;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Hashtable;
import java.util.Random;
import java.util.Vector;

/**
 *
 * @author pscerri
 */
public class APTIMSCreate {
    public final static String TERRAIN_COSTMAP_LOCATION="/home/pscerri/Code/TerrainCM";
    public final static String CLASSPATH="/usr0/pscerri/Code/swing-layout-1.0.jar:/usr0/pscerri/Code/AirSim/build/classes:/usr0/pscerri/Code/Util/build/classes/:/usr0/pscerri/Code/Machinetta/build/classes:/usr0/pscerri/Code/SanjayaGUI/build/classes";
    public final static String CLASSPATH2="/home/pscerri/Code/swing-layout-1.0.jar:/home/pscerri/Code/classes";
    
    public final static String MASTER_MACHINE="tsipouro";
    public final static String [] SLAVE_MACHINES = {"zeta", "kappa", "iota"};
    
    public final static String SYNCHRONIZATION_SCRIPT="/usr0/pscerri/Code/scripts/syncall.sh";
    
    public static String loc = ".";
    public static int noUAVs = 3;
    public static int noOperators = 1;
    
    public TeamBelief team = null;
    
    public boolean useAssociates = true;
    public int noAssociates = 3;
    
    Random rand = new Random();
    
    public static String masterMachine = MASTER_MACHINE;
    public static String [] slaveMachines = SLAVE_MACHINES;
    
    /**
     * Writes all the cfg and xml file for a L3Comms type scenario
     */
    public APTIMSCreate() {
        Vector teamMembers = new Vector();
        
        // For now, add all proxy ids to the team
        for (int i = 0; i < noUAVs; i++) {
            teamMembers.add(new NamedProxyID("UAV"+i));
        }
        
        for (int i = 0; i < noOperators; i++) {
            teamMembers.add(new NamedProxyID("Operator"+i));
        }
        
        team = new TeamBelief("TeamAll", teamMembers, new Hashtable());
        
        // Now create specific files
        for (int i = 0; i < noUAVs; i++) {
            createUAVCfgFile("UAV"+i);
            makeUAVBeliefs("UAV"+i, i);
        }
        
        for (int i = 0; i < noOperators; i++) {
            createOperatorCfgFile("Operator"+i);
            makeOperatorBeliefs("Operator"+i);
        }
        
        makeRunScript();
        makeEnvFile();
    }
    
    
    public static void main(String argv[]) {
        loc = argv[0];
        
        new APTIMSCreate();
    }
    
    /**
     * This will (obviously) only work on linux/unix (and obviously, the classpath needs to change per user.)
     */
    public void makeRunScript() {
        // Make scripts for all machines
        try {
            DataOutputStream out = new DataOutputStream(new FileOutputStream(loc + File.separatorChar + "run.sh"));
            
            out.writeBytes("#!/bin/tcsh\n\n");
            out.writeBytes("setenv CLASSPATH "+CLASSPATH+"\n\n");
            
            out.writeBytes("echo Synchronizing files on slaves \n");
            out.writeBytes(SYNCHRONIZATION_SCRIPT+"\n");
            
            out.writeBytes("\n\n");
            out.writeBytes("java Machinetta.Communication.UDPSwitch >& output/SwitchOut &\n\n");
            out.writeBytes("sleep 1\n\n");
            out.writeBytes("java -Xmx800m AirSim.Environment.GUI.MainFrame " + loc + File.separatorChar + "Env.txt --showgui >& output/SimOut &\n\n");
            out.writeBytes("sleep 1\n\n");
            
            for (int i = 0; i < slaveMachines.length; i++) {
                out.writeBytes("\n\necho Starting processes on " + slaveMachines[i] +"\n");
                out.writeBytes("ssh -2 " + slaveMachines[i] + " chmod +x " + loc + File.separatorChar + slaveMachines[i] + ".sh\n");
                out.writeBytes("ssh -2 " + slaveMachines[i] + " " + loc + File.separatorChar + slaveMachines[i] + ".sh &\n");
            }
            
            out.writeBytes("\necho Starting Operators\n");
            for (int i = 0; i < noOperators; i++) {
                out.writeBytes("java -Xmx128M Machinetta.Proxy " + loc + File.separatorChar + "Operator"+i+".cfg > output" + File.separatorChar + "Operator"+i+".out &\n");
                out.writeBytes("sleep 0.3\n");
            }
            
            out.flush();
            out.close();
            
            // The switch, the simulator and the operator all run on the master machine
            DataOutputStream [] slaveScripts = new DataOutputStream[slaveMachines.length];
            for (int i = 0; i < slaveScripts.length; i++) {
                slaveScripts[i] = new DataOutputStream(new FileOutputStream(loc + File.separatorChar + slaveMachines[i] + ".sh"));
                
                slaveScripts[i].writeBytes("#!/bin/tcsh\n\n");
                slaveScripts[i].writeBytes("setenv CLASSPATH "+CLASSPATH2+"\n\n");
            }
            
            int outIndex = 0;
            
            for (int i = 0; i < noUAVs; i++) {
                slaveScripts[outIndex].writeBytes("\necho Starting UAV " + i + "\n");
                slaveScripts[outIndex].writeBytes("/usr1/java/linux/old/jdk1.5.0_06/bin/java -Xmx400m Machinetta.Proxy " + loc + File.separatorChar + "UAV"+i+".cfg > output" + File.separatorChar + "UAV"+i+".out &\n");
                slaveScripts[outIndex].writeBytes("sleep 0.3\n");
                outIndex = (++outIndex) % slaveScripts.length;
            }
            
            for (int i = 0; i < slaveScripts.length; i++) {
                slaveScripts[i].flush();
                slaveScripts[i].close();
            }
        } catch (Exception e) {
            System.out.println("Failed to write script file: " + e);
        }
        
    }
    
    public void makeEnvFile() {
        try {
            DataOutputStream out = new DataOutputStream(new FileOutputStream(loc + File.separatorChar + "Env.txt"));
            
            out.writeBytes("CTDB_BASE_NAME /usr1/grd/hood/hood_v2.0_062098_0_0_50000_50000_050\n");
            
            for (int i = 0; i < noUAVs; i++) {
                out.writeBytes("SMALL_UAV UAV"+i+ " " + (13119 + i * 100) + " 34805 1000 BLUEFOR\n");
            }
            
            out.flush();
            out.close();
        } catch (Exception e) {
            System.out.println("Failed to write config file: " + e);
        }
    }
    
    public ProxyState _makeBeliefs() {
        ProxyState state = new ProxyState();
        state.removeAll();
        
        state.addBelief(team);
        
        return state;
    }
    
    public Associates makeAssociates(ProxyID self) {
        Associates ass = new Associates();
        // System.out.println("Creating associates for : " + self);
        for (int i = 0; i < noAssociates; i++) {
            ProxyID other = null;
            do {
                other = team.members.get(rand.nextInt(team.members.size()));
                // System.out.println("Considering " + other);
            } while (self.equals(other) || ass.isAssociate(other));
            // System.out.println("Adding: " + other);
            ass.addAssociate(other);
        }
        return ass;
    }
    
    /**
     *
     * UAV initially knows about itself and the team.
     */
    
    public ProxyState _makeUAVBeliefs(String name, RAPBelief self) {
        ProxyState state = _makeBeliefs();
        
        if (useAssociates) {
            state.addBelief(makeAssociates(self.getProxyID()));
        }
        
        return state;
    }
    
    public void makeUAVBeliefs(String name, int uavNo) {
        
        RAPBelief self = new RAPBelief(new NamedProxyID(name), true, UAVCaps);
        
        ProxyState state = _makeUAVBeliefs(name, self);
        state.addBelief(self);
        
        /*
        // Give them an initial scan role
        BasicRole scanRole = new BasicRole(TaskType.scan);
        scanRole.params = new Hashtable();
        // scanRole.params.put("Area", new Rectangle(10000*uavNo, 10000*uavNo, 20000, 20000));
        scanRole.params.put("Area", new Rectangle(0,0,50000,50000));
        scanRole.setResponsible(self);
        scanRole.constrainedWait = false;
        state.addBelief(scanRole);
        */
        
        writeBeliefs(loc + File.separatorChar + name + ".blf", state);
    }
    
    /**
     *
     * Operator initially knows about itself and the team.
     */
    public void makeOperatorBeliefs(String name) {
        
        ProxyState state = _makeBeliefs();
        
        RAPBelief self = new RAPBelief(new NamedProxyID(name), true, operatorCaps);
        state.addBelief(self);
        if (useAssociates) {
            state.addBelief(makeAssociates(self.getProxyID()));
        }
        
        writeBeliefs(loc + File.separatorChar + name + ".blf", state);
    }
    
    private void writeBeliefs(String fileName, ProxyState state) {
        try {
            ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(fileName));
            BeliefID [] beliefs = state.getAllBeliefs();
            for (int i = 0; i < beliefs.length; i++) {
                Belief b = state.getBelief(beliefs[i]);
                if (b != null) {
                    os.writeObject(b);
                }
            }
            os.flush();
            os.close();
        } catch (FileNotFoundException ex) {
            Machinetta.Debugger.debug("Problem writing beliefs: " + ex, 5, this);
            ex.printStackTrace();
        } catch (IOException ex) {
            Machinetta.Debugger.debug("Problem writing beliefs: " + ex, 5, this);
            ex.printStackTrace();
        }
    }
    
    /**
     * Defines the plan template for Geolocate missions.
     *
     * @todo Define the plan template
     */
    public TeamPlanTemplate makeGeolocateTPT() {
        Hashtable <String, Object> params = new Hashtable <String, Object>();
        Vector  preconditions = new Vector(),
                postconditions = new Vector(),
                roles = new Vector(),
                constraints = new Vector();
        
        //----------------------------------------------------------
        // No params for now
        
        //----------------------------------------------------------
        // Precondition is that there is an un"located" GeoLocateRequest
        Vector <String> inKeys = new Vector<String>();
        inKeys.add("class");
        inKeys.add("located");
        
        Vector <String> outKeys = new Vector<String>();
        outKeys.add("id");
        
        MatchCondition cond = new MatchCondition(
                "AirSim.Machinetta.Beliefs.GeoLocateRequest false",    // String to be matched
                inKeys,
                outKeys,
                "request");  // This is the key to the belief in the role's param string
        Vector first = new Vector();
        first.add(cond);
        preconditions.add(first);
        
        //----------------------------------------------------------
        // No post conditions for now
        Vector <String> inKeysPost = new Vector<String>();
        inKeysPost.add("class");
        inKeysPost.add("located");
        
        Vector <String> outKeysPost = new Vector<String>();
        
        MatchCondition condPost = new MatchCondition(
                "AirSim.Machinetta.Beliefs.GeoLocateRequest true",    // String to be matched
                inKeysPost,
                outKeysPost,
                "request");  // This is the key to the belief in the role's param string
        Vector firstPost = new Vector();
        firstPost.add(condPost);
        postconditions.add(firstPost);
        
        //----------------------------------------------------------
        // Create the roles
        
        // One Geolocate Compute Role
        BasicRole computeRole = new BasicRole(TaskType.geolocateCompute);
        computeRole.infoSharing = new Vector<DirectedInformationRequirement>();
        try {
            computeRole.infoSharing.add(new DirectedInformationRequirement(Class.forName("AirSim.Machinetta.Beliefs.GeoLocateResult"), new NamedProxyID("Operator0")));
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        roles.add(computeRole);
        
        // Geolocate sense roles
        for (int no = 0; no < 3; no++) {
            BasicRole basic = new BasicRole(TaskType.geolocateSense);
            basic.params = new Hashtable();
            basic.params.put("Label", "Role"+no);
            basic.params.put("ScanDataLabel", "TMAGLData"+no);
            
            basic.infoSharing = new Vector<DirectedInformationRequirement>();
            try {
                basic.infoSharing.add(new DirectedInformationRequirement(Class.forName("AirSim.Machinetta.Beliefs.TMAScanResult"), computeRole.getID(), new BeliefNameID("TMAGLData"+no)));
                // basic.infoSharing.add(new DirectedInformationRequirement(Class.forName("AirSim.Machinetta.Beliefs.TMAScanResult"), new NamedProxyID("Geolocator0"), new BeliefNameID("TMAGLData"+no)));
            } catch (ClassNotFoundException ex) {
                ex.printStackTrace();
            }
            
            roles.add(basic);
        }
        
        //----------------------------------------------------------
        // No constraints for now
        
        // Finally, create the template
        TeamPlanTemplate tpt = new TeamPlanTemplate("Geolocate", team.getID(), params, preconditions, postconditions, roles, constraints);
        
        return tpt;
    }
    
    /**
     * Defines the plan template for Geolocate missions.
     *
     * @todo Define the plan template
     */
    public TeamPlanTemplate makeCheckSensorReadingTPT() {
        Hashtable <String, Object> params = new Hashtable <String, Object>();
        Vector  preconditions = new Vector(),
                postconditions = new Vector(),
                roles = new Vector(),
                constraints = new Vector();
        
        //----------------------------------------------------------
        // No params for now
        
        //----------------------------------------------------------
        // Precondition is that there is an unevaluated TMA sensor reading
        // (In a roundabout way this says that the field isDiscriminated of an
        // object of class AirSim.Machinetta.Beliefs.TMAScanResult must be false
        // for this template to be instantiated
        Vector <String> inKeys = new Vector<String>();
        inKeys.add("class");
        inKeys.add("isDiscriminated");
        inKeys.add("isGeolocateData");
        
        Vector <String> outKeys = new Vector<String>();
        outKeys.add("id");
        
        MatchCondition cond = new MatchCondition(
                "AirSim.Machinetta.Beliefs.TMAScanResult false false",    // String to be matched
                inKeys,
                outKeys,
                "sensorReading");
        Vector first = new Vector();
        first.add(cond);
        preconditions.add(first);
        //----------------------------------------------------------
        // No post conditions for now
        
        //----------------------------------------------------------
        // Create the roles
        
        // Role for providing the data, will be assigned to the UAV creating the plan
        BasicRole sensor = new BasicRole(TaskType.provideScanData);
        roles.add(sensor);
        
        // Role for processing the data
        BasicRole detector = new BasicRole(TaskType.emitterDiscriminate);
        roles.add(detector);
        
        sensor.infoSharing = new Vector<DirectedInformationRequirement>();
        
        // Tell proxy with sensor role to send the plan parameters to the proxy
        // with detector role
        // Constructor signature is bad and will change !
        sensor.infoSharing.add(new DirectedInformationRequirement("sensorReading", detector.getID()));
        
        //----------------------------------------------------------
        // No constraints for now
        
        // Finally, create the template
        TeamPlanTemplate tpt = new TeamPlanTemplate("CheckSensorReading", team.getID(), params, preconditions, postconditions, roles, constraints);
        
        return tpt;
    }
    
    /**
     * This hashtable contains a specification of the capabilities
     * of a generic Geolocate agent.
     */
    java.util.Hashtable<String, CapabilityBelief> operatorCaps = null;
    {
        operatorCaps = new Hashtable<String, CapabilityBelief>();
        operatorCaps.put("RoleAllocation", new CapabilityBelief("RoleAllocation", 0));
    }
    
    /**
     * This hashtable contains a specification of the capabilities
     * of a generic UAV.
     */
    java.util.Hashtable<String, CapabilityBelief> UAVCaps = null;
    {
        UAVCaps = new Hashtable<String, CapabilityBelief>();
        UAVCaps.put("scan", new CapabilityBelief("scan", 5));
        UAVCaps.put("patrol", new CapabilityBelief("patrol", 55));
        UAVCaps.put("RoleAllocation", new CapabilityBelief("RoleAllocation", 0));
    }
    
    /**
     * For creating config files for UAVs
     */
    private DataOutputStream _createUAVCfgFile(String name) {
        try {
            DataOutputStream out = new DataOutputStream(new FileOutputStream(loc + File.separatorChar + name + ".cfg"));
            out.writeBytes("UDP_SWITCH_IP_STRING " + masterMachine +"\n");
            out.writeBytes("AA_IMPLEMENTATION_TYPE AirSim.Machinetta.UAVAA\n");
            // Eventually want to change this to SCALAR_BINARY
            out.writeBytes("ROLE_ALLOCATION_ALGORITHM BINARY_BINARY\n");
            out.writeBytes("COMMS_IMPLEMENTATION_TYPE Machinetta.Communication.UDPComms\n");
            out.writeBytes("RAP_INTERFACE_IMPLEMENTATION_TYPE AirSim.Machinetta.UAVRI\n");
            out.writeBytes("COORD_IMPLEMENTATION_TYPE Machinetta.Coordination.MACoordination\n");
            out.writeBytes("BELIEF_CLASS_FILE_LOCATIONS AirSim.Machinetta.\n");
            out.writeBytes("DEBUG_LEVEL 1\n");
            out.writeBytes("DEFAULT_BELIEFS_FILE " + loc + File.separatorChar + name + ".blf\n");
            out.writeBytes("TERRAIN_COSTMAP_LOCATION "+TERRAIN_COSTMAP_LOCATION);
            return out;
            
        } catch (Exception e) {
            System.out.println("Failed to write config file: " + e);
        }
        
        return null;
    }
    
    public void createUAVCfgFile(String name) {
        
        DataOutputStream out = _createUAVCfgFile(name);
        
        try {
            out.flush();
            out.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
    }
    
    
    /**
     * For creating config files for Operators
     */
    public void createOperatorCfgFile(String name) {
        try {
            DataOutputStream out = new DataOutputStream(new FileOutputStream(loc + File.separatorChar + name + ".cfg"));
            out.writeBytes("AA_IMPLEMENTATION_TYPE AirSim.Commander.AA\n");
            out.writeBytes("UDP_SWITCH_IP_STRING " + masterMachine +"\n");
// Eventually want to change this to SCALAR_BINARY
            out.writeBytes("ROLE_ALLOCATION_ALGORITHM BINARY_BINARY\n");
            out.writeBytes("COMMS_IMPLEMENTATION_TYPE Machinetta.Communication.UDPComms\n");
            out.writeBytes("RAP_INTERFACE_IMPLEMENTATION_TYPE AirSim.Commander.APTIMSOperatorRI\n");
            out.writeBytes("COORD_IMPLEMENTATION_TYPE Machinetta.Coordination.MACoordination\n");
            out.writeBytes("BELIEF_CLASS_FILE_LOCATIONS AirSim.Machinetta.\n");
            out.writeBytes("DEBUG_LEVEL 1\n");
            out.writeBytes("DEFAULT_BELIEFS_FILE " + loc + File.separatorChar + name + ".blf\n");
            // out.writeBytes("CTDB_BASE_NAME /usr1/grd/ntc/ntc_tin_v2.5_071898_0_0_50000_50000_050\n");
            out.writeBytes("CTDB_BASE_NAME /usr1/grd/hood/hood_v2.0_062098_0_0_50000_50000_050\n");
            out.flush();
            out.close();
        } catch (Exception e) {
            System.out.println("Failed to write config file: " + e);
        }
    }
}
