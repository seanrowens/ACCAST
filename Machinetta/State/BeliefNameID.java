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
 * BeliefNameID.java
 *
 * Created on 3 July 2002, 22:53
 */

package Machinetta.State;

import Machinetta.State.BeliefType.Belief;
/**
 *
 * @author  scerri
 */
public class BeliefNameID extends BeliefID {
    
    /** Creates a new instance of BeliefNameID
     *
     * @param name Name of this belief id
     */
    public BeliefNameID(String name) {
        this.name = name.intern();
    }
    
    /** Prints out representation of this ID  */
    public String toString() {
        return name;
    }
    
    /** Checks whether id matches this BeliefID  */
    public boolean equals(Object id) {
        try {
	    if(id == this) return true;
            boolean ret = ((BeliefNameID)id).getName().equalsIgnoreCase(name);
            // Machinetta.Debugger.debug("Equals : " + id + " and " + this + " ? " + ret, 3, this);
            return ret;
        } catch (ClassCastException e) {
            return false;
        }
    }
    
    /** Returns a hashcode based on the the name of this ID */
    public int hashCode() { return name.hashCode(); }
    
    /** Access name */
    public String getName() { return name; }
    
    /** Name of this belief */
    private String name = "None";
    
    public static final long serialVersionUID = 1L;
}
