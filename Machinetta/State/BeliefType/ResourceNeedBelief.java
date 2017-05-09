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
 * ResourceNeedBelief.java
 *
 * Created on June 14, 2007, 1:54 PM
 *
 */

package Machinetta.State.BeliefType;

import Machinetta.State.BeliefID;
import Machinetta.State.BeliefNameID;
import Machinetta.State.BeliefType.TeamBelief.ResourceBelief;

/**
 *
 * @author pscerri
 */
public class ResourceNeedBelief extends Belief {
    
    ResourceBelief resource = null;
    int need = 0;
    
    boolean considered = false;
    
    // Some overloading of this belief
    boolean given = false, taken = false;    
    
    /** Creates a new instance of ResourceNeedBelief */
    public ResourceNeedBelief(ResourceBelief r) {
        resource = r;
    }

    public BeliefID makeID() {
        return new BeliefNameID("RNB:"+resource.getID());
    }

    public ResourceBelief getResource() {
        return resource;
    }

    public boolean isConsidered() {
        return considered;
    }

    public void setNeed(int need) {
        this.need = need;
        considered = true;
    }

    public boolean isGiven() {
        return given;
    }

    public boolean isTaken() {
        return taken;
    }

    public void setGiven(boolean given) {
        this.given = given;
    }

    public void setTaken(boolean taken) {
        this.taken = taken;
    }

    public void setConsidered(boolean considered) {
        this.considered = considered;
    }

    public int getNeed() {
        return need;
    }


}
