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
 * BinaryPriority.java
 *
 * Created on October 11, 2002, 4:47 PM
 */

package Machinetta.State.BeliefType.TeamBelief.Priority;

import Machinetta.State.BeliefID;
import Machinetta.State.BeliefNameID;
import Machinetta.State.BeliefType.TeamBelief.TeamPlanBelief;
import Machinetta.State.BeliefType.Belief;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Attr;

/**
 *
 * @author  pynadath
 */
public class BinaryPriority extends PriorityBelief {
    
    /** The binary value (true>false) */
    public Boolean value;
    
    /** For auto XML */
    public BinaryPriority() {}
    
    public BeliefID makeID() {
        if (plan != null)
            return new Machinetta.State.BeliefNameID("BinPriority"+plan);
        else return new Machinetta.State.BeliefNameID("AnonymousBinPriority");
    }

    /** Creates a new instance of BinaryPriority
     * @param id The ID for this belief
     */
    public BinaryPriority(BeliefID id) { super(id); }
    
    /** Creates a new (but initialized) instance of BinaryPriority
     * @param id The ID for this belief
     */
    public BinaryPriority(BeliefID id,BeliefID plan) { super(id,plan); }
    
    /** Creates a new (but initialized) instance of BinaryPriority
     * @param id The ID for this belief
     * @param value The determined initial value of this priority
     */
    public BinaryPriority(BeliefID id,BeliefID plan,boolean value) {
        super(id,plan);
        setPriority(value);
    }
    
    public void setPriority(boolean value) { setPriority(new Boolean(value)); }
    
    /** Returns a single scalar value that represents the current priority estimate
     * @return An integer whose magnitude indicates a priority along some total order
     */
    public Integer getPriority() {
        if (isDetermined()) {
            if (value.booleanValue())
                return new Integer(100);
            else
                return new Integer(0);
        } else
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
    
    /** Abstract method for setting priority to some value
     */
    public void setPriority(Object value) {
        if (value instanceof Boolean) {
            super._setPriority(value);
            this.value = (Boolean)value;
        } else if (value instanceof Integer) {
            if (((Integer)value).intValue() == 0)
                setPriority(false);
            else
                setPriority(true);
        }
    }
    
     public static final long serialVersionUID = 1L;
}
