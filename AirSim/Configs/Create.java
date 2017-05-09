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
package AirSim.Configs;
import AirSim.Environment.Assets.Tasks.TaskType;
import AirSim.Machinetta.BasicRole;
import AirSim.Machinetta.NAI;
import Machinetta.State.*;
import Machinetta.State.BeliefType.*;
import Machinetta.State.BeliefType.TeamBelief.*;
import java.io.*;
import java.util.Hashtable;
import java.util.Random;

/**
 * @todo Add the commander.
 */
public class Create {
    
    static String loc = null;
    
    public enum AirAsset {
        WASM("WASM", "W", 6),
        A10("A10", "A10", 0),
        C130("C130", "C130", 0),
        F14("F14", "F14", 0),
        F15("F15", "F15", 0),
        F16("F16", "F16", 0),
        f18("F18", "F18", 0);
        
        public final String name, desig;
        public final int no;
        
        AirAsset(String name, String desig, int no) {
            this.name = name;
            this.desig = desig;
            this.no = no;
        }
    };
    
    public enum GroundAsset {
        M1A1("M1A1TANK", "M1A1", 0),
        M2("M2TANK", "M2", 0),
        HUMMER("HUMMER", "H", 0),
        SCUD("SCUD", "S", 0),
        TRUCK("TRUCK", "T", 0);
        
        public final String name, desig;
        public final int no;
        
        GroundAsset(String name, String desig, int no) {
            this.name = name;
            this.desig = desig;
            this.no = no;
        }
    };
    
    boolean blue = true;
    // int wasms = 6, m1a1s = 0, m2s = 0, hummers = 0, scuds = 0, trucks = 0, a10s = 0, c130s = 0, f14s = 0, f15s = 0, f16s = 0, f18s = 0;
    
    public Create() {
        Random rand = new Random();
        
        for (GroundAsset type: GroundAsset.values()) {
            for (int i = 0; i < type.no; i++) {
                // System.out.println(type.name + " " + makeName(type, i) + " "+ rand.nextInt(1000) + " " + rand.nextInt(1000));
                createCfgFile(makeName(type,  i));
            }
        }
        
        for (AirAsset type: AirAsset.values()) {
            for (int i = 0; i < type.no; i++) {
                // System.out.println(type.name + " " + makeName(type, i) + " "+ rand.nextInt(1000) + " " + rand.nextInt(1000) + " 250");                
                createCfgFile(makeName(type,  i));
                createXMLFile(makeName(type, i));                                
            }
        }
        
    }
    
    public String makeName(AirAsset type, int i) {
        return (blue? "B-":"R-") + type.desig + "-" + i;
    }
    
    public String makeName(GroundAsset type, int i) {
        return (blue? "B-":"R-") + type.desig + "-" + i;
    }
    
    public static void main(String argv[]) {
        loc = argv[0];
        
        new Create();
    }
    
    public void createCfgFile(String name) {
        try {
            DataOutputStream out = new DataOutputStream(new FileOutputStream(loc + File.separatorChar + name + ".cfg"));
            out.writeBytes("AA_IMPLEMENTATION_TYPE AirSim.Machinetta.AA\n");
            out.writeBytes("ROLE_ALLOCATION_ALGORITHM BINARY_BINARY\n");
            out.writeBytes("COMMS_IMPLEMENTATION_TYPE Machinetta.Communication.UDPComms\n");
            out.writeBytes("RAP_INTERFACE_IMPLEMENTATION_TYPE AirSim.Machinetta.RAPInterface\n");
            out.writeBytes("COORD_IMPLEMENTATION_TYPE Machinetta.Coordination.MACoordination\n");
            out.writeBytes("BELIEF_CLASS_FILE_LOCATIONS AirSim.Machinetta.\n");
            out.writeBytes("DEBUG_LEVEL 1\n");
            out.writeBytes("DEFAULT_BELIEFS_FILE " + loc + File.separatorChar + name + ".xml\n");
            out.flush();
            out.close();
        } catch (Exception e) {
            System.out.println("Failed to write config file: " + e);
        }
    }
    
    java.util.Hashtable<String, CapabilityBelief> wasmCaps;
    {
        wasmCaps = new java.util.Hashtable<String, CapabilityBelief>();
        wasmCaps.put("move", new CapabilityBelief("move", 100));
        wasmCaps.put("patrol", new CapabilityBelief("patrol", 100));
        wasmCaps.put("RoleAllocation", new CapabilityBelief("RoleAllocation", 0));
    }
    
    void createXMLFile(String name) {
        ProxyState state = new ProxyState();
        state.removeAll();
        addAllRAPBeliefs(name, state);        
        
        BeliefsXML.writeBeliefs(loc + File.separatorChar + name + ".xml");
    }
    
    void addAllRAPBeliefs(String myName, ProxyState state) {
        java.util.Vector<ProxyID> teamMembers = new java.util.Vector<ProxyID>();
        for (GroundAsset type: GroundAsset.values()) {
            for (int i = 0; i < type.no; i++) {
                // @todo Ground assets configuration files.
            }
        }
        
        for (AirAsset type: AirAsset.values()) {
            for (int i = 0; i < type.no; i++) {
                NamedProxyID proxyID = new NamedProxyID(makeName(type, i));
                RAPBelief bel = null;
                switch (type) {
                    case WASM:
                        bel = new RAPBelief(proxyID, proxyID.toString().equalsIgnoreCase(myName), wasmCaps);
                        break;
                }
                if (bel != null)
                    state.addBelief(bel);
                teamMembers.add(proxyID);
            }
        }
        
        // Finally, add the Commander
        Hashtable<String, CapabilityBelief> caps = new Hashtable<String, CapabilityBelief>();
        caps.put("RoleAllocation", new CapabilityBelief("RoleAllocation", 0)); 
        ProxyID commanderID = new NamedProxyID("Commander");
        RAPBelief bel = new RAPBelief(commanderID, false, caps);
        state.addBelief(bel);
        teamMembers.add(commanderID);
        
        TeamBelief teamAll = new TeamBelief((blue?"Blue":"Red") + "-Team", teamMembers, new java.util.Hashtable());
    }
}
