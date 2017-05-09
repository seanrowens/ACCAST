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
 * Configuration.java
 *
 * Created on 4 July 2002, 11:31
 */

package Machinetta;

import java.lang.reflect.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.util.StringTokenizer;
import java.util.HashMap;

/**
 * This class provides configuration information for a proxy
 *
 * Details can be loaded in from file, but this must be performed at the
 * start.
 *
 * It is not intended that callers change values of fields, though this
 * is not enforced.
 *
 * Configuration file is a series of lines, each with "Field value".  Incorrect types, names, etc.
 * result in defaults being used.
 *
 * @author  scerri
 */
public class Configuration {
    
    public static HashMap allMap = new HashMap();
    public static HashMap notFoundMap = new HashMap();

    /*****************************************************************************/
    /* Implementation types                                                      */
    /*****************************************************************************/
    
    /** State implementation type
     * Valid values : HASHTABLE
     * @see Machinetta.State.ProxyState
     */
    public static String STATE_IMPLEMENTATION_TYPE = "HASHTABLE";
    
    /** RAP Interface implementation type
     * Valid values : TERMINAL, FIRE_CHIEF, FIRE_BRIGADE, ANALYST, SIMPLE_SIM_FB,
     * SIMPLE_SIM_SHEPERD
     * @see Machinetta.RAPInterface.RAPInterface
     */
    public static String RAP_INTERFACE_IMPLEMENTATION_TYPE = "TERMINAL";
    
    /** AA Implementation type
     * Valid values : SIMPLE, TEST_FC, SIMPLE_FC, SIMPLE_FB, ANALYST, SHEPERD
     * @see Machinetta.Intelligence.AA
     */
    public static String AA_IMPLEMENTATION_TYPE = "SIMPLE";
    
    /** Coordination implementation type
     * Valid values : TEST, TEAM
     * @see Machinetta.Intelligence.Coordination
     */
    public static String COORD_IMPLEMENTATION_TYPE = "TEAM";
    
    /** Coordination policy type
     * Valid values : STEAM, NAIVE
     * @see Machinetta.Intelligence.Policy.CoordinationPolicy
     */
    public static String COORD_POLICY_TYPE = "NAIVE";
    
    /** Communication implementation type
     * Valid types : DUMMY, LOCAL, SS_LOCAL, KQML
     * @see Machinetta.Communication.Communication
     */
    public static String COMMS_IMPLEMENTATION_TYPE = "KQML";
    
    /*****************************************************************************/
    
    /*****************************************************************************/
    /* Coordination Configuration                                                */
    /*        Only of relevance if using the MACoordination.                     */
    /*****************************************************************************/
    /**
     * Dictates the way that new plans are instantiated by a plan agent <br>
     * "LOCAL" - at least one plan precondition must have been detected locally
     * by the Proxy <br>
     * "ALWAYS" - instantiate the plan when any condition is met <br>
     * "PROBABILISTIC" - instantiate the plan, if another plan has not been instantiated
     * after a random amount of time up to PROBABILISTIC_INSTANTIATE_TIME <br>
     * "NONE" - this proxy may not instantiate plans
     */
    public static String PLAN_INSTANTIATION_POLICY = "LOCAL";
    
    /** Maximum time to wait before instantiating a plan, if using PROBABILISTIC policy
     *  In Seconds
     */
    public static int PROBABILISTIC_INSTANTIATE_TIME = 25;
    
    /** Whether or not to use the associates network (and all that entails) <br>
     *
     * Only of relevance if using the MACoordination.
     */
    public static boolean USE_ASSOCIATES_NETWORK = true;
    
    /** If true then proxies will inform associates of a plan
     * when they accept a role in the plan
     */
    public static boolean ASSOCIATE_INFORM_ROLE_ACCEPT = false;
    
    /** If true then proxies will inform associates whenever a
     * role agent visits.
     */
    public static boolean ASSOCIATE_INFORM_ROLE_OFFER = false;
    
    /** Determines which algorithm is used to do role reallocation
     * First term represents the information level of capability
     * Second term represent the information level of priority
     * E.g., BINARY_PROB represents binary information about capability, probabilistic information
     * about priority
     *
     * Valid values : BINARY_BINARY, SCALAR_BINARY, PROBABILISTIC_BINARY
     */
    public static String ROLE_ALLOCATION_ALGORITHM = "BINARY_BINARY";
    
    /** Determines the default RAP for assigning responsibility for role allocation.
     * If null, role allocations are initially assigned to the RAP who creates the
     * role allocation belief (i.e., the self).  Otherwise, the string is taken to be
     * the Proxy ID of the RAP to whom initial responsibility should be given.
     */
    public static String DEFAULT_ALLOCATION_RESPONSIBLITY = null;
    
    /** When passing on roles, the following two parameters control the behavior of
     * the allocation function (binaryAllocation method in RoleAllocationBelief).
     */
    /** This parameter controls the number of RAPs who are asked before giving up
     * (integer in [0,100])
     * @see Machinetta.State.BeliefType.TeamBelief.RoleAllocationBelief;
     */
    public static int ALLOCATION_EFFORT_PERCENTAGE = 100;
    
    /** This parameter controls the minimum capability of RAPS to consider
     * before giving up (integer in [0,100])
     * @see Machinetta.State.BeliefType.TeamBelief.RoleAllocationBelief;
     */
    public static int ALLOCATION_EFFORT_THRESHOLD = 1;
    
    /** When finding a RAP to allocate a role to, we allocate it to the
     * FIRST/BEST one we find.
     */
    public static String ALLOCATION_SELECTION = "BEST";
    
    /** If > 0 then do not accept tasks for which capability is less than
     * this.
     */
    public static int ROLE_ALLOCATION_THRESHOLD = 3;
    
    /** Amount of time (in ms) that an unfilled role should be put to sleep */
    public static int UNFILLED_ROLE_SLEEP_TIME = 500;
    
    /** Number of proxies a role agent should visit before sleeping */
    public static int PROXY_VISITS_BEFORE_ROLE_SLEEP = 20;
    
    /** If TRUE agents will consider using Fire Chief, otherwise not.
     * @deprecated Use the boolean ROLE_ALLOCATION_META_REASONING
     */
    public static String USE_CHIEF = "FALSE";
    
    
    /**************************************************************************/
    /* Meta-reasoning parameter                                               */
    /**************************************************************************/
    /** Determines whether meta-reasoning should be invoked when role is not allocated */
    public static boolean ROLE_ALLOCATION_META_REASONING = false;
    
    /** Determines whether meta-reasoning should be invoked if plans run over time */
    public static boolean PLAN_COMPLETION_META_REASONING = false;
    
    /** Determines whether meta-reasoning should be invoked if an agent has nothing to do */
    public static boolean AVAILABLE_RAP_META_REASONING = false;
    
    /*****************************************************************************/
    /* Information levels                                                        */
    /*****************************************************************************/
    /** Level for priority
     * Valid types: "binary", "static quantitative", "dynamic quantitative"
     * @see Machinetta.State.BeliefType.TeamBelief.Priority.PriorityBelief;
     * @deprecated Use enum PLAN_PRIORITY_TYPES instead
     */
    public static String INFO_LEVEL_PRIORITY = "dynamic quantitative";
        
    public enum PLAN_PRIORITY_TYPES { BINARY, STATIC_QUANTITATIVE, DYNAMIC_QUANTITATIVE };
    /**
     * Notice that ConfigReader does not currently support reading enums from configuration
     * hence these cannot be read from file.
     */
    public static PLAN_PRIORITY_TYPES PLAN_PRIORITY_TYPE = PLAN_PRIORITY_TYPES.STATIC_QUANTITATIVE;
    
    /** When role allocation is invoked
     *
     * Valid types: NEW_ROLE
     */
    public static String ROLE_ALLOCATION_TRIGGER = "NEW_ROLE";
    
    /*****************************************************************************/
    /* Files                                                                     */
    /*****************************************************************************/
    /** File where starting, default beliefs will be found
     * Should be an XML file conforming to DTD : Machinetta/State/Beliefs.dtd
     */
    public static String DEFAULT_BELIEFS_FILE = "Z:\\Code\\Machinetta\\State\\SampleBeliefs_1.xml";
    
    /** Belief class file locations */
    public static String BELIEF_CLASS_FILE_LOCATIONS =
            "Machinetta.DomainSpecific.DisasterSim. " + "Machinetta.DomainSpecific.AirSim. " +
            "Machinetta.DomainSpecific.SDRSim. ";
    
    public static String DEFAULT_BELIEF_CLASS_FILE_LOCATIONS = "Machinetta.State.BeliefType. " +
            "Machinetta.State.BeliefType.TeamBelief. " + "Machinetta.State.BeliefType.Match. " +
            "Machinetta.State.BeliefType.MAC. " + "Machinetta.State.BeliefType.TeamBelief.Constraints. ";
    
    /** Location to write a detailed log of beliefs to (if not null)
     *
     * Notice that this is likely to be a very large file, so do sparingly and
     * do not try to write for many proxies on the same machine at once!
     */
    public static String BELIEF_LOG_FILE = null;
    
    /** Location to write a detailed log of MACoordination to (if not null)
     *
     * Notice that this is likely to be a very large file, so do sparingly and
     * do not try to write for many proxies on the same machine at once!
     */
    public static String MACOORDINATION_LOG_FILE = null;
    
    /*****************************************************************************/
    /*****************************************************************************/
    /* Communication specific                                                                      */
    /*****************************************************************************/
    /** Hostname for KQML ANS */
    public static String KQML_ANS_HOST = "vibhagam.isi.edu";
    
    /** Port for KQML ANS */
    public static int KQML_ANS_PORT = 5500;
    
    /** Location of LocalComms server */
    public static String LOCAL_COMMS_SERVER = "localhost";
    
    /** Name of class to use for plan agents */
    public static String PLAN_AGENT_CLASS = "Machinetta.Coordination.MAC.PlanAgent";
    
    /** Multicast group and port to use for DIS **/
    public static String DIS_MULTICAST_IP_STRING = "192.168.1.255";
    public static int DIS_RAT_MULTICAST_PORT = 1900;
    public static int DIS_RI_MULTICAST_PORT = 1900;
    
    public static String OITL_IP_STRING = "192.168.1.10";
    //public static String OITL_IP_STRING = "localhost";
    
    public static int OITL_SERVER_PORT = 2003;
    
    public static int FIRST_WASM_ID = 1;
    
    /** If true, then the list of visited proxies is remembered, otherwise just a count. */
    public static boolean REMEMBER_MAC_AGENT_HISTORY = false;
    
    public static short DIS_SITE_ID = 300;
    public static short DIS_HOST_ID = 40;
    public static short DIS_EXERCISE_ID = 40;
    
    // How long to wait before we assume something has gone wrong with a STRIKE request and
    // tell the OITL the plan is unallocatable
    public static int OITL_SCHEDULING_TIMEOUT = 3000;
    
    public static String UDP_SWITCH_IP_STRING = "mavrodafni.cimds.ri.cmu.edu";
    public static int UDP_SWITCH_IP_PORT = 4002;
    public static String UDP_SWITCH_GROUP = "239.1.4.69";
    public static int UDP_MULTICAST_PORT = 4003;
    
    /** The default value for Boolean beliefs */
    public static String DEFAULT_BOOLEAN_VALUE = "true";
    
    /** The default value for Integer beliefs */
    public static String DEFAULT_INTEGER_VALUE = "0";
    
    /** How much debugging, higher the number, less messages */
    public static int DEBUG_LEVEL = 1;
    
    /*****************************************************************************/
    
    /*****************************************************************************/
    /* Fire Brigade Specific                                                     */
    /*****************************************************************************/
    
    /* Width of city */
    public static int CITY_WIDTH = 50;
    
    /* Height of city */
    public static int CITY_HEIGHT = 20;
    
    /**
     * Creates a new instance of Configuration.
     *
     * @param configFile Name of file to read configuration from.  If null then
     * default configuration is used.
     */
    public Configuration(String configFile) {
        if (configFile != null) {
            (new ConfigReader()).readConfig(this, configFile);
        }
    }

    public Configuration(String configFile, HashMap<String, String> extraConfigValues) {
        if (configFile != null) {
            (new ConfigReader()).readConfig(this, configFile);
        }
	(new ConfigReader()).extraValues(this, extraConfigValues);
    }
    
    private Class Config = null; {
        Config = this.getClass();
    }
    
    private void setField(String field, String value) {
         try {
             java.lang.reflect.Field f = Config.getDeclaredField(field);
             String type = f.getType().getName();
             Machinetta.Debugger.debug(1,"Setting " + f.toString().substring(f.toString().lastIndexOf('.') + 1) + " to " + value);
             if (type.equalsIgnoreCase("java.lang.String")) {
                 try {
                     f.set(this, value);
                 } catch (Exception e) {
                     Machinetta.Debugger.debug(5,"Could not set " + field + " to " + value);
                 }
             } else if (type.equalsIgnoreCase("boolean")) {
                 try {
                     f.setBoolean(this, (new Boolean(value)).booleanValue());
                 } catch (Exception e) {
                     Machinetta.Debugger.debug(5,"Could not set boolean " + value);
                 }
             } else if (type.equalsIgnoreCase("int")) {
                 try {
                     f.setInt(this, (new Integer(value)).intValue());
                 } catch (Exception e) {
                     Machinetta.Debugger.debug(5,"Could not set int " + value);
                 }
             } else {
                 Machinetta.Debugger.debug(5,"Unknown field type " + type);
             }
         } catch (NoSuchFieldException e) {
             Machinetta.Debugger.debug(5,"No field : " + field);
         }
     }
    
}
