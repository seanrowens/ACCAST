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
 * ProbabilisticPriority.java
 *
 * Created on October 15, 2002, 11:45 AM
 */

package Machinetta.State.BeliefType.TeamBelief.Priority;

import Machinetta.State.BeliefID;
import Machinetta.State.BeliefType.TeamBelief.TeamPlanBelief;
import Machinetta.State.BeliefType.Probability.TableRV;

import java.util.Enumeration;
/**
 *
 * @author  pynadath
 */
public class ProbabilisticPriority extends PriorityBelief {
    
    
    /** The probability distribution of this priority */
    public TableRV distribution = null;
    
    /** For auto XML */
    public ProbabilisticPriority() {}
    
    /** Creates a new instance of ProbabilisticPriority */
    public ProbabilisticPriority(BeliefID id,BeliefID plan) { super(id,plan); }
    
    /** Creates a new instance of ProbabilisticPriority */
    public ProbabilisticPriority(BeliefID id) { super(id); }
    
    public BeliefID makeID() {
        if (plan != null)
            return new Machinetta.State.BeliefNameID("ProbPriority"+plan);
        else return new Machinetta.State.BeliefNameID("AnonymousProbPriority");
    }
    
    /** Returns a single scalar value that represents the current priority estimate
     * @return An integer whose magnitude indicates a priority along some total order
     */
    public Integer getPriority() {
        if (isDetermined())
            return new Integer((new Double(distribution.getExpectation())).intValue());
        else
            return null;
    }
    
    /** @return The distribution governing this priority belief */
    public TableRV getDistribution() { return distribution; }
    
    public String toString() {
        String result = getID().toString() + " ";
        if (isDetermined())
            result = result + distribution;
        else
            result = result + "unknown";
        return result;
    }
    
    /** Abstract method for setting priority to some value
     */
    public void setPriority(Object value) {
        if (value instanceof TableRV) {
            super._setPriority(distribution);
            this.distribution = (TableRV)distribution;
        } else if (value instanceof Integer) {
            Machinetta.Debugger.debug(1,"Unable to handle Integer values");
        }
    }
    
    public static final long serialVersionUID = 1L;
}
