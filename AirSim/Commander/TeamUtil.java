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

import Machinetta.State.ProxyState;
import Machinetta.State.BeliefType.TeamBelief.TeamBelief;
import Machinetta.State.BeliefType.RAPBelief;
import Machinetta.State.BeliefType.CapabilityBelief;
import Machinetta.State.BeliefType.ProxyID;
import Machinetta.State.BeliefType.TeamBelief.RoleBelief;
import Machinetta.State.BeliefNameID;

import java.util.*;

// NOTE: This class has some simple utils to look up some specific
// team related beliefs in ProxyState.  These beliefs are created in
// ACCASTCreate and are serialized into each and every proxy's .blf
// file when the ProxyState gets serialized.  In other words, as soon
// as a Proxy loads the .blf file these beliefs are available in
// ProxyState.  Hence each and every proxy has available to it;
//
// 1) An instance of Machinetta.State.BeliefType.TeamBelief.TeamBelief
// that contains a Vector<ProxyID> of the id's of every proxy on the
// team
//
// 2) For each proxyid in the above Vector, a
// Machinetta.State.BeliefType.RAPBelief containing the specified's
// proxies capabilities;
//
// (java.util.Hashtable<String,Machinetta.State.BeliefType.CapabilityBelief>
// 
// RAPBelief has two methods on it that can be used to check if a role
// is suitable for a proxy;
//
//     /** Returns true if the RAP could possibly be capable of fulfilling
//      * this role.  Present circumstances or other roles are not be
//      * taken into account. */
//     public boolean canPerformRole(RoleBelief role)
//
//     /** Returns the capability object matching a particular role name */
//     public CapabilityBelief getCapability(String roleName) 
public class TeamUtil {

    private String teamName;
    private ProxyState state;
    private TeamBelief team;
    private ArrayList<RAPBelief> rapBeliefs;
    
    public TeamUtil() {
	teamName = "TeamAll";
	state = new ProxyState();
	team = getTeam();
	if(null != team)
	    rapBeliefs = getTeamRAPBeliefs();
    }

    private TeamBelief getTeam() {
	Object o = state.getBelief(new BeliefNameID(teamName));
	if(null == o) {
	    Machinetta.Debugger.debug(1,"Null TeamBelief for teamName "+teamName);
	    return null;
	}
	if(!(o instanceof TeamBelief)) {
	    Machinetta.Debugger.debug(1,"NonNull TeamBelief but wrong class ("+o.getClass().toString()+" instead of TeamBelief) for teamName "+teamName);
	    return null;
	}
	return (TeamBelief)o;
    }

    private ArrayList<RAPBelief> getTeamRAPBeliefs() {
	if(null == team) {
	    Machinetta.Debugger.debug(1,"TeamBelief is null, couldn't get RAPBeliefs for teamName "+teamName);
	    return null;
	}

	ArrayList<RAPBelief> rapBeliefs = new ArrayList<RAPBelief>();
	Iterator iter = team.getMembers();
	while(iter.hasNext()) {
	    ProxyID pid = (ProxyID)iter.next();
	    Object o = state.getBelief(new BeliefNameID(pid.toString()));
	    if(null != o) {
		if(o instanceof RAPBelief) {
		    rapBeliefs.add((RAPBelief)o);
		}
	    }
	}
	return rapBeliefs;
    }

    public ArrayList<RAPBelief> rapsThatCanPerform(RoleBelief role) {
	ArrayList<RAPBelief> canPerformRapBeliefs = new ArrayList<RAPBelief>();
	for(RAPBelief rapBelief: rapBeliefs) {
	    if(rapBelief.canPerformRole(role))
		canPerformRapBeliefs.add(rapBelief);
	}
	return canPerformRapBeliefs;
    }
}
