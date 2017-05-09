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
 * Belief.java
 *
 * Created on 3 July 2002, 23:12
 */

package Machinetta.State.BeliefType;

import Machinetta.State.*;
import java.io.Serializable;
import java.util.Hashtable;

/**
 * Super class of all BeliefTypes
 *
 * @author  scerri
 */
public abstract class Belief implements Cloneable, Serializable {
    
    /** The unique identifier of this belief */
    public BeliefID id = null;    

    /** Meta-data about the belief */
    public Hashtable<String, Object>meta = null;
    
    /** This is true iff the local proxy sensed this information itself */
    private boolean locallySensed = false;
    
    /** Creates a new instance of Belief
     *
     * @param id The unique identifier for this belief
     */
    public Belief(BeliefID id) {
        this.id = id;
    }
    
    /** Needed for auto create from XML */
    public Belief() {}
    
    /** Access method for id
     *
     * @return Id of this method
     */
    public BeliefID getID() { 
        if (id == null) id = makeID();
        return id; 
    }
    
    /** Create a BeliefID for this particular Belief
     * This method should be overloaded by subclasses
     * It will be used by auto create from XML to make an ID
     * once all fields are filled in.
     * If fields are not properly filled in, this method should return null
     * and auto XML will give an error
     * <br>
     * Callees wanting to know the id of this object should call getID(), 
     * although the BeliefIDs returned by getID and this should be equal
     */
    public abstract BeliefID makeID();
    
    /** Returns a clone of this belief
     *
     * @ return A copy of this method.
     */
    public Belief makeClone() {
        try {
            return (Belief)clone();
        } catch (CloneNotSupportedException e) {
            Machinetta.Debugger.debug( 1,"Clone not supported for :" + getClass());
            return null;
        }        
    }
        
    /** Returns the id of the Belief
     * @return String with id of belief
     */
    public String toString() { 
        if (id == null) id = makeID();
        return "Belief " + id.toString(); 
    }
    
    /** Access function for whether locally sensed */
    public boolean locallySensed() { return locallySensed; }
    
    public void setLocallySensed(boolean b) { locallySensed = b; }
    
    /** called when the InformationAgent's state change */
    public boolean infoExchange() {return false;}
    
    /** Get the value of some meta-data */
    public Object getMeta(String s) { 
	if(null == meta)
	    return null;
	return meta.get(s); 
    }
    
    /** Set the value of some meta-data */
    public void setMeta(String s, Object o) { 
	if(null == meta)
	    meta = new Hashtable<String, Object>();
	meta.put(s, o); 
    }
}
