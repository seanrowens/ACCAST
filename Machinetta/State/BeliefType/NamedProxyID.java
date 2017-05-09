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
  * NamedProxyID.java
  *
  * Created on 17 September 2002, 15:43
  */

package Machinetta.State.BeliefType;

import Machinetta.State.BeliefNameID;
import Machinetta.State.BeliefID;

import java.io.Serializable;
import java.util.*;
/**
 * Refers to a particular proxy by name
 *
 * @author  scerri
 */
public class NamedProxyID extends ProxyID implements Serializable {
    static Map<String,NamedProxyID> internMap = new HashMap<String,NamedProxyID>();
    
    /** The name of this proxy */
    public String name = "Unknown";

    /** Required for auto create XML */
    public NamedProxyID() {}
    
    public static NamedProxyID factory(String name) {
	name = name.intern();
	NamedProxyID result = (NamedProxyID)internMap.get(name);
	if(null == result) {
	    result = new NamedProxyID(name);
	    internMap.put(name, result);
	}
	return result;
    }

    /** Creates a new instance of NamedProxyID */
    public NamedProxyID(String name) {
        super(new BeliefNameID(name));
        this.name = name.intern();
    }      
        
    public BeliefID makeID () { return new BeliefNameID(name); }
    
    /** Return true if id refers to same agent as this id  */
    public boolean matches(ProxyID id) {
        try {
            return ((NamedProxyID)id).getName().equalsIgnoreCase(name);
        } catch (ClassCastException e) {
            return false;
        }
    }
    
    /** Get the name of this proxy */
    public String getName() { return name; }
    
    /** Returns the name of the proxy */
    public String toString() { return name; }
    
    public int hashCode () {         
        int code = name.hashCode();         
        return code;        
    }
    
    public static final long serialVersionUID = 1L;
}
