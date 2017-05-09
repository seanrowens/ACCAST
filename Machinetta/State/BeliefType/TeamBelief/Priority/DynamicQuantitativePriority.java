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
 * StaticQuantitativePriority.java
 *
 * Created on October 11, 2002, 5:25 PM
 */

package Machinetta.State.BeliefType.TeamBelief.Priority;

import Machinetta.State.BeliefID;
import Machinetta.State.BeliefNameID;
import Machinetta.State.BeliefType.Belief;
import Machinetta.State.BeliefType.TeamBelief.TeamPlanBelief;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Attr;

/**
 *
 * @author  pynadath
 */
public class DynamicQuantitativePriority extends PriorityBelief {
    
    /** Quantitative assessment of priority (>=0 once determined) */
    public Integer value = null;
    
    /** For auto XML */
    public DynamicQuantitativePriority() {}
    
    /** Creates a new instance of StaticQuantitativePriority */
    public DynamicQuantitativePriority(BeliefID id) { super(id); }
    
    /** Creates a new (but initialized) instance of DynamicQuantitativePriority
     * @param id The ID for this belief
     * @param plan The team plan with which this priority is associated
     */
    public DynamicQuantitativePriority(BeliefID id,BeliefID plan) { super(id,plan); }
    
    /** Creates a new (but initialized) instance of DynamicQuantitativePriority
     * @param id The ID for this belief
     * @param plan The team plan with which this priority is associated
     * @param value The determined initial value of this priority
     */
    public DynamicQuantitativePriority(BeliefID id,BeliefID plan,int value) {
        this(id,plan,new Integer(value));
    }
    
    /** Creates a new (but initialized) instance of DynamicQuantitativePriority
     * @param id The ID for this belief
     * @param plan The team plan with which this priority is associated
     * @param value The determined initial value of this priority
     */
    public DynamicQuantitativePriority(BeliefID id,BeliefID plan,Integer value) {
        super(id,plan);
        setPriority(value);
    }
    
    public BeliefID makeID() {
        if (plan != null)
            return new Machinetta.State.BeliefNameID("DynPriority"+plan);
        else return new Machinetta.State.BeliefNameID("AnonymousDynPriority");
    }
    
    public void setPriority(int value) {
        setPriority(new Integer(value));
    }
    
    /** Returns a single scalar value that represents the current priority estimate
     * @return An integer whose magnitude indicates a priority along some total order
     */
    public Integer getPriority() {
        if (isDetermined())
            return value;
        else
            return null;
    }
    
    public String toString() {
        String result = getID().toString() + " ";
        if (isDetermined())
            result = result + value;
        else
            result = result + "unknown";
        return result;
    }
    
    /** Abstract method for setting priority to some value (isDetermined now returns T)
     */
    public void setPriority(Object value) {
        if (value instanceof Integer) {
            if (((Integer)value).intValue() >= 0) {
                this.value = (Integer)value;
                _setPriority(value);
                Machinetta.Debugger.debug( 1,"Set priority to: "+getPriority());
            } else
                Machinetta.Debugger.debug(1,"Attempted to set negative priority "+value);
        }
    }
    
    public static final long serialVersionUID = 1L;
}
