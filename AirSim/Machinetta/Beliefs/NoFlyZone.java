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
 * NoFlyZone.java
 *
 * Created on April 28, 2006, 1:36 PM
 *
 */

package AirSim.Machinetta.Beliefs;

import AirSim.Environment.Area;
import Machinetta.State.BeliefID;
import Machinetta.State.BeliefNameID;
import Machinetta.State.BeliefType.Belief;

/**
 * NoFlyZone is used by anything that uses the SimpleDynamicCostMap to
 * avoid flying into specified areas.  This was an earlier attempt to
 * control for UAV airspace conflicts.  We didn't want to do anything
 * sophisticated at this point, rather we simply wanted a way to tell
 * the UAV "never fly over this spot here".  (Note,
 * AirSim.Environment.Area is a simple 2d box which does not take
 * altitude into account.)
 *
 * @author pscerri
 */
public class NoFlyZone extends Belief {
    
    AirSim.Environment.Area area = null;
    
    /** Creates a new instance of NoFlyZone */
    public NoFlyZone(AirSim.Environment.Area a) {
        this.area = a;
    }

    public BeliefID makeID() {
        return new BeliefNameID("NFZ:"+area.toString());
    }

    public Area getArea() {
        return area;
    }
    
}
