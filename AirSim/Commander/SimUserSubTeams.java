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
package AirSim.Commander;

import AirSim.UnitIDs;
import AirSim.Environment.Assets.Asset.Types;

import Machinetta.State.BeliefType.ProxyID;
import Machinetta.State.BeliefType.NamedProxyID;

import java.util.*;

public class SimUserSubTeams {

    public final static String AIRSTRIP_TEAM1 = "AirStripTeam1";
    public final static String AIRSTRIP_TEAM2 = "AirStripTeam2";
    public final static String TERMINAL_TEAM1 = "TerminalTeam1";
    public final static String AIRLIFT_PASSENGERS = "AirLiftPassengers";
    public final static String AIRLIFT_TEAM = "AirLiftTeam";
    public final static String AUGV_PATROL1 = "AUGVPatrol1";
    public final static String AUGV_PATROL2 = "AUGVPatrol2";
    public final static String CONVOY_TEAM = "ConvoyTeam";

    public final static String GATE_TEAM1 = "GateTeam1";
    public final static String GATE_TEAM2 = "GateTeam2";

    public final static String C2_TEAM = "C2Team";
    public final static String IM_TEAM1 = "IMTeam1";
    public final static String IM_TEAM2 = "IMTeam2";
    public final static String IM_TEAM3 = "IMTeam3";
    public final static String AIRDROP_TEAM1 = "AirDropTeam1";
    public final static String AIRDROP_TEAM2 = "AirDropTeam2";
    public final static String AIRDROP_TEAM3 = "AirDropTeam3";
    public final static String AIRSUPPORT_TEAM = "AirSupportTeam";
    
    {
	init();
    }

    public static HashMap<String,ArrayList<NamedProxyID>> subTeamMap = null;

    public ArrayList<NamedProxyID> getSubTeam(String subTeamName) {
	return subTeamMap.get(subTeamName);
    }

    HashMap<ProxyID,Types> unitTypeMap = null;

    public Types getUnitType(ProxyID proxyId) {
	return unitTypeMap.get(proxyId);
    }
    
    private void init() {
	subTeamMap = new HashMap<String,ArrayList<NamedProxyID>>();
	unitTypeMap = new HashMap<ProxyID,Types>();
	createUnitInstanceTypes();
	createSubTeams();
    }

    /** Creates a new instance of SimUserSubTeams */
    public SimUserSubTeams() {
    }
    

    private void createUnitInstanceTypes() {
	unitTypeMap.put(new NamedProxyID("AUGV0"),Types.AUGV);
	unitTypeMap.put(new NamedProxyID("AUGV1"),Types.AUGV);
	unitTypeMap.put(new NamedProxyID("AUGV2"),Types.AUGV);
	unitTypeMap.put(new NamedProxyID("AUGV3"),Types.AUGV);
	unitTypeMap.put(new NamedProxyID("C130-0"),Types.C130);
	unitTypeMap.put(new NamedProxyID("C130-1"),Types.C130);
	unitTypeMap.put(new NamedProxyID("C130-2"),Types.C130);
	unitTypeMap.put(new NamedProxyID("C130-3"),Types.C130);
	unitTypeMap.put(new NamedProxyID("FAARP"),Types.FAARP);
	unitTypeMap.put(new NamedProxyID("H0"),Types.HMMMV);
	unitTypeMap.put(new NamedProxyID("H1"),Types.HMMMV);
	unitTypeMap.put(new NamedProxyID("DI-BLUE0"),Types.INFANTRY);
	unitTypeMap.put(new NamedProxyID("DI-BLUE1"),Types.INFANTRY);
	unitTypeMap.put(new NamedProxyID("DI-BLUE2"),Types.INFANTRY);
	unitTypeMap.put(new NamedProxyID("DI-BLUE3"),Types.INFANTRY);
	unitTypeMap.put(new NamedProxyID("DI-BLUE4"),Types.INFANTRY);
	unitTypeMap.put(new NamedProxyID("DI-BLUE5"),Types.INFANTRY);
	unitTypeMap.put(new NamedProxyID("DI-BLUE6"),Types.INFANTRY);
	unitTypeMap.put(new NamedProxyID("DI-BLUE7"),Types.INFANTRY);
	unitTypeMap.put(new NamedProxyID("DI-BLUE8"),Types.INFANTRY);
	unitTypeMap.put(new NamedProxyID("DI-BLUE9"),Types.INFANTRY);
	unitTypeMap.put(new NamedProxyID("IM0"),Types.IM);
	unitTypeMap.put(new NamedProxyID("IM1"),Types.IM);
	unitTypeMap.put(new NamedProxyID("IM2"),Types.IM);
	unitTypeMap.put(new NamedProxyID("AUAV0"),Types.SMALL_UAV);
	unitTypeMap.put(new NamedProxyID("AUAV1"),Types.SMALL_UAV);
	unitTypeMap.put(new NamedProxyID("AUAV2"),Types.SMALL_UAV);
	unitTypeMap.put(new NamedProxyID("AUAV3"),Types.SMALL_UAV);
	unitTypeMap.put(new NamedProxyID("EOIR-UAV0"),Types.SMALL_UAV);
    }

    private void createSubTeams() {
	ArrayList<NamedProxyID> subteam;

	subteam = new ArrayList<NamedProxyID>();
	subteam.add(new NamedProxyID(UnitIDs.DIBLUE+"0"));
	subteam.add(new NamedProxyID(UnitIDs.DIBLUE+"1"));
	subTeamMap.put(AIRSTRIP_TEAM1,subteam);

	subteam = new ArrayList<NamedProxyID>();
	subteam.add(new NamedProxyID(UnitIDs.DIBLUE+"2"));
	subteam.add(new NamedProxyID(UnitIDs.DIBLUE+"3"));
	subTeamMap.put(AIRSTRIP_TEAM2,subteam);

	subteam = new ArrayList<NamedProxyID>();
	subteam.add(new NamedProxyID(UnitIDs.DIBLUE+"4"));
	subteam.add(new NamedProxyID(UnitIDs.DIBLUE+"5"));
	subTeamMap.put(TERMINAL_TEAM1,subteam);

	subteam = new ArrayList<NamedProxyID>();
	subteam.addAll(subTeamMap.get(AIRSTRIP_TEAM1));
	subteam.addAll(subTeamMap.get(AIRSTRIP_TEAM2));
	subteam.addAll(subTeamMap.get(TERMINAL_TEAM1));
	subTeamMap.put(AIRLIFT_PASSENGERS,subteam);

	subteam = new ArrayList<NamedProxyID>();
	subteam.add(new NamedProxyID(UnitIDs.C130+"0"));
	subTeamMap.put(AIRLIFT_TEAM,subteam);

	subteam = new ArrayList<NamedProxyID>();
	subteam.add(new NamedProxyID(UnitIDs.DIBLUE+"6"));
	subteam.add(new NamedProxyID(UnitIDs.DIBLUE+"7"));
	subTeamMap.put(GATE_TEAM1,subteam);

	subteam = new ArrayList<NamedProxyID>();
	subteam.add(new NamedProxyID(UnitIDs.DIBLUE+"8"));
	subteam.add(new NamedProxyID(UnitIDs.DIBLUE+"9"));
	subTeamMap.put(GATE_TEAM2,subteam);

	subteam = new ArrayList<NamedProxyID>();
	subteam.add(new NamedProxyID(UnitIDs.AUGV+"0"));
	subTeamMap.put(AUGV_PATROL1,subteam);

	subteam = new ArrayList<NamedProxyID>();
	subteam.add(new NamedProxyID(UnitIDs.AUGV+"1"));
	subTeamMap.put(AUGV_PATROL2,subteam);

	subteam = new ArrayList<NamedProxyID>();
	subteam.add(new NamedProxyID(UnitIDs.AUGV+"2"));
	subteam.add(new NamedProxyID(UnitIDs.AUGV+"3"));
	subteam.add(new NamedProxyID(UnitIDs.HUMVEE+"0"));
	subteam.add(new NamedProxyID(UnitIDs.HUMVEE+"1"));
	subTeamMap.put(CONVOY_TEAM,subteam);

	subteam = new ArrayList<NamedProxyID>();
	subteam.add(new NamedProxyID(UnitIDs.AUGV+"2"));
	subteam.add(new NamedProxyID(UnitIDs.AUGV+"3"));
	subteam.add(new NamedProxyID(UnitIDs.DIBLUE+"6"));
	subteam.add(new NamedProxyID(UnitIDs.DIBLUE+"7"));
	subteam.add(new NamedProxyID(UnitIDs.DIBLUE+"8"));
	subteam.add(new NamedProxyID(UnitIDs.DIBLUE+"9"));
	subTeamMap.put(C2_TEAM,subteam);

	subteam = new ArrayList<NamedProxyID>();
	subteam.add(new NamedProxyID(UnitIDs.IM+"0"));
	subTeamMap.put(IM_TEAM1,subteam);

	subteam = new ArrayList<NamedProxyID>();
	subteam.add(new NamedProxyID(UnitIDs.IM+"1"));
	subTeamMap.put(IM_TEAM2,subteam);

	subteam = new ArrayList<NamedProxyID>();
	subteam.add(new NamedProxyID(UnitIDs.IM+"2"));
	subTeamMap.put(IM_TEAM3,subteam);

	subteam = new ArrayList<NamedProxyID>();
	subteam.add(new NamedProxyID(UnitIDs.C130+"1"));
	subTeamMap.put(AIRDROP_TEAM1,subteam);

	subteam = new ArrayList<NamedProxyID>();
	subteam.add(new NamedProxyID(UnitIDs.C130+"2"));
	subTeamMap.put(AIRDROP_TEAM2,subteam);

	subteam = new ArrayList<NamedProxyID>();
	subteam.add(new NamedProxyID(UnitIDs.C130+"3"));
	subTeamMap.put(AIRDROP_TEAM3,subteam);

	subteam = new ArrayList<NamedProxyID>();
	subteam.add(new NamedProxyID(UnitIDs.AUAV+"0"));
	subteam.add(new NamedProxyID(UnitIDs.AUAV+"1"));
	subteam.add(new NamedProxyID(UnitIDs.AUAV+"2"));
	subteam.add(new NamedProxyID(UnitIDs.AUAV+"3"));
	subteam.add(new NamedProxyID(UnitIDs.EOIRUAV+"0"));
	subTeamMap.put(AIRSUPPORT_TEAM,subteam);
    }

    public static void createAirsupportSubTeam(int numAuav, int numEoiruav) {
	ArrayList<NamedProxyID> subteam;

	subteam = new ArrayList<NamedProxyID>();
	for(int loopi = 0; loopi < numAuav; loopi++) {
	    subteam.add(new NamedProxyID(UnitIDs.AUAV+loopi));
	}
	for(int loopi = 0; loopi < numEoiruav; loopi++) {
	    subteam.add(new NamedProxyID(UnitIDs.EOIRUAV+loopi));
	}
	subTeamMap.put(AIRSUPPORT_TEAM,subteam);
    }

    public static void createAugvPatrols(int num) {
	ArrayList<NamedProxyID> subteam1;
	ArrayList<NamedProxyID> subteam2;

	subteam1 = new ArrayList<NamedProxyID>();
	subteam2 = new ArrayList<NamedProxyID>();

	int half = num/2;
	int loopi =0;
	for(; loopi < half; loopi++) {
	    subteam1.add(new NamedProxyID(UnitIDs.AUGV+loopi));
	}
	for(; loopi < num; loopi++) {
	    subteam2.add(new NamedProxyID(UnitIDs.AUGV+loopi));
	}
	
	subTeamMap.put(AUGV_PATROL1,subteam1);
	subTeamMap.put(AUGV_PATROL2,subteam2);
    }

    public static void createAugvPatrol2(int num) {
	ArrayList<NamedProxyID> subteam;

	subteam = new ArrayList<NamedProxyID>();
	for(int loopi = 0; loopi < num; loopi++) {
	    subteam.add(new NamedProxyID(UnitIDs.AUGV+loopi));
	}
	subTeamMap.put(AUGV_PATROL2,subteam);
    }
}
