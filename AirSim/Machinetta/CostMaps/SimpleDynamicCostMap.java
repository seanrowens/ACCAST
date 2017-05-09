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
 * SimpleDynamicCostMap.java
 *
 * Created on March 21, 2006, 7:03 PM
 *
 */

package AirSim.Machinetta.CostMaps;

import AirSim.Machinetta.UAVRI;
import AirSim.Machinetta.Beliefs.NoFlyZone;
import AirSim.Machinetta.Beliefs.VehicleBelief;
import Machinetta.State.BeliefID;
import Machinetta.State.BeliefType.Belief;
import Machinetta.State.ProxyState;
import Machinetta.State.StateChangeListener;
import java.awt.Rectangle;

/**
 * @todo Stupidly named, since this is for ground obstacles (perhaps shift obst to subclass)
 *
 * @author pscerri
 */
public class SimpleDynamicCostMap extends SimpleStaticCostMap implements StateChangeListener {
    
    static private ProxyState state = new ProxyState();
    /** Creates a new instance of SimpleDynamicCostMap */
    public SimpleDynamicCostMap() {
        state.addChangeListener(this);
    }
    
    public void timeElapsed(long t) {
        /* @fix Dynamic cost maps are static
        ArrayList<SimpleStaticCostMap.WeightedRect> remove = null;
        for (SimpleStaticCostMap.WeightedRect wr: rects) {
            wr.value *= 1.0 - Math.exp(t/1000.0);
            if (wr.value < 0.001) {
                if (remove == null) {
                    remove = new ArrayList<SimpleStaticCostMap.WeightedRect>();
                }
                remove.add(wr);
            } else if (wr.value < minCost) {
                minCost = wr.value;
            }
            Machinetta.Debugger.debug("Updated dynamic cost map: " + wr.value, 1, this);
        }
        if (remove != null) rects.removeAll(remove);                
         */
    }
    
    /**
     * @todo There is an issue here that multiple readings will just be dumped
     * on top of each other.
     */
    public void stateChanged(BeliefID[] b) {
        for (BeliefID id: b) {
            Belief bel = state.getBelief(id);
            if (bel instanceof VehicleBelief) {
                VehicleBelief vb = (VehicleBelief)bel;
                if (vb.getType() == AirSim.Environment.Assets.Asset.Types.SA9) {
                    addCostRect(new Rectangle(vb.getX() - ((int)UAVRI.SA9_COST_SQUARE_METERS/2), vb.getY() - ((int)UAVRI.SA9_COST_SQUARE_METERS/2), UAVRI.SA9_COST_SQUARE_METERS, UAVRI.SA9_COST_SQUARE_METERS), 200.0);
                    Machinetta.Debugger.debug("Added cost due to vehicle on SimpleDynamicCostMap", 1, this);
                }
            } else if (bel instanceof NoFlyZone) {
                addCostRect(((NoFlyZone)bel).getArea().getAsRect(), 1000.0);
                Machinetta.Debugger.debug("Added NoFlyZone", 1, this);
            }
        }
    }
    
}
