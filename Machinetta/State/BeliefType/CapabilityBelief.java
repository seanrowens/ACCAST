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
 * CapabilityBelief.java
 *
 * Created on 30 September 2002, 15:23
 */

package Machinetta.State.BeliefType;

import Machinetta.State.BeliefNameID;
import Machinetta.State.BeliefID;
import Machinetta.Configuration;

import java.util.Hashtable;
import java.io.*;
import java.util.StringTokenizer;
import java.util.Enumeration;

/**
 * @todo Issue here is that expressability in broad strokes.  I.e., Cagle's example
 * was that some TDOA sensors might not be able to handle the full range.  Could
 * that be captured here?
 *
 * @author  scerri
 */
public class CapabilityBelief extends Belief {

    /** The name of this capability */
    public String name = null;

    /** Temporary trick (?) to help with interactions between roles
     * Specifies the % of the RAPs resources this capability 
     * takes up.
     *
     * Only used if loadFunction == null
     *
     * Default is 100% of resources.
     */
    public int load = 100;    
        
    /** Specific capabilities for specific tasks */
    public Hashtable<Object,Object> specific = new Hashtable<Object,Object>();

    /** This is true iff the information in this Capability needs to be updated at runtime */
    public boolean isDynamic = false;

    /** Function to use for calculating load 
     * @Deprecated Use hashtable
     */
    private CapabilityLoadFunction loadFunction = null;    
        
    /** Hashtable of load functions for specific capabilities
     * 
     * Functions are retrieved by capName
     */
    static private Hashtable<Object,CapabilityLoadFunction> loadFns = new Hashtable<Object,CapabilityLoadFunction>();    
    
    /** Creates a new instance of CapabilityBelief */
    public CapabilityBelief(String capName) {
        super(makeID(capName));
        this.name = capName;        
    }

    /** Creates a Capability specifying the load the capability 
     * places on the RAP
     *
     * Will change.
     */
    public CapabilityBelief(String capName, int load) {
        this(capName);
        this.load = load;
    }
    
    /** This constructor is for auto creation from XML */
    public CapabilityBelief() { }
    
    public static BeliefID makeID(String capName) { return new BeliefNameID("CAP:" + capName); }
    
    public BeliefID makeID() { return new BeliefNameID("CAP:" + name); }
    
    /** Return scalar capability given parameters
     *
     * Will need to change once probability beliefs go int
     *
     * @params Specifics of the requested capability
     * @throws UnknownException when cannot generate an integer capability
     */
    public int getCapability(Object params) throws IllegalArgumentException {
        try {
            return ((Integer)specific.get(params)).intValue();
        } catch (NullPointerException e) {
            // Not a "bad" exception, just a way of triggering capability acquisition ... 
            Machinetta.Debugger.debug( 0,"Capability unknown for " + name + " (" + params +")");
            throw(new IllegalArgumentException());
        }
    }
    
    /** Set the specific capability for a specific task
     * Value should either
     * - Integer for scalar capabilities
     * - Some probability model
     */
    public void setCapability(Object params, Object value) { specific.put(params, value); }
    
    public void removeCapability(Object params) { specific.remove(params); }
    
    public String toString() { return "RAP has capability " + name; }
    
    /** Returns string with name of belief */
    public String getName() { return name; }
    
    /** Returns all of the known specific capabilities (as key names, not values) */
    public Enumeration getSpecificCapabilities() { return specific.keys(); }
   
    /** Returns the % of the RAPs resources this capability consumes */
    public int getLoad() { return getLoad(null); }
    
    /** Version of load function that includes consideration of params */
    public int getLoad(Object params) { 
        CapabilityLoadFunction loadFunction = (CapabilityLoadFunction)loadFns.get(name);
        if (loadFunction != null) {
            return loadFunction.calculateLoad (params);
        } else  
            return load; 
    }

    /** True iff this capability will change dynamically */
    public boolean isDynamic() { return isDynamic; }
    
    /** Set a class to dynamically calculate load 
     * @Deprecated Use other version
     */
    public void setLoadFunction (CapabilityLoadFunction fn) { loadFunction = fn; }

    /** Add a class for calculating load
     *
     * String to store by should be capName, since that is how it will be retrieved
     */
    public static void setLoadFunction (String capName, CapabilityLoadFunction fn) {
        loadFns.put(capName, fn);
    }
    
    public static final long serialVersionUID = 1L;
            
}
