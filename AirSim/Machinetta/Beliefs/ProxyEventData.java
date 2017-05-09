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
 * ProxyEventData.java
 *
 * Created Sun Jan 18 20:22:32 EST 2009
 */

package AirSim.Machinetta.Beliefs;

import AirSim.Environment.Vector3D;

import Machinetta.State.BeliefID;
import Machinetta.State.BeliefNameID;
import Machinetta.State.BeliefType.Belief;
import Machinetta.State.BeliefType.ProxyID;

import java.awt.geom.Rectangle2D;
import java.awt.geom.Point2D;

/**
 * ProxyEventData is used to share information about a proxy's RAP.
 *
 * @author owens
 */
public class ProxyEventData extends Belief {
    
    public enum EventType {
	NONE("none"),
	LANDED("Landed"),
	DISMOUNTED_CONTENTS("Dismounted contents/passengers of RAP"),
	    IN_PATROL_AREA("Entered Patrol Area"),
	    ATTACK_FROM_AIR_EXECUTED("Attack from air executed")
	;
        
        public final String name;
        
        EventType(String name) {this.name = name;}
        public String toString() { return this.name; }
    }
        
    public EventType type = EventType.NONE;
    public ProxyID proxyID;
    public long simTime = -1;
    public boolean flag = false;
    public Point2D.Double location2D = null;
    public Vector3D location3D = null;
    public Rectangle2D.Double area = null;

    /** Creates a new instance of ProxyEventData */
    public ProxyEventData(ProxyID proxyID, EventType type) {
        this.proxyID = proxyID;
	this.type = type;
    }

    /** Creates a new instance of ProxyEventData */
    public ProxyEventData(ProxyID proxyID, EventType type, long simTime) {
        this.proxyID = proxyID;
	this.type = type;
	this.simTime = simTime;
    }

    /** Creates a new instance of ProxyEventData */
    public ProxyEventData(ProxyID proxyID, EventType type, boolean flag) {
        this.proxyID = proxyID;
	this.type = type;
	this.flag = flag;
    }

    /** Creates a new instance of ProxyEventData */
    public ProxyEventData(ProxyID proxyID, EventType type, Point2D.Double location2D) {
        this.proxyID = proxyID;
	this.type = type;
	this.location2D = location2D;
    }

    /** Creates a new instance of ProxyEventData */
    public ProxyEventData(ProxyID proxyID, EventType type, Vector3D location3D) {
        this.proxyID = proxyID;
	this.type = type;
	this.location3D = location3D;
    }

    /** Creates a new instance of ProxyEventData */
    public ProxyEventData(ProxyID proxyID, EventType type, Rectangle2D.Double area) {
        this.proxyID = proxyID;
	this.type = type;
	this.area = area;
    }

    public BeliefID makeID() {
        return new BeliefNameID("PED "+type+": "+proxyID+" "+simTime+" "+flag+" "+location2D+" "+location3D+" "+area);
    }

    public String toString() { 
        return "ProxyEventData "+type+": pid="+proxyID+" simtime="+simTime+" flag="+flag+" location2D="+location2D+" location3D="+location3D+" area="+area;
    }
}
