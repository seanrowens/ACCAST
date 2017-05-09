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
 * GeneratedInformationRequirement.java
 *
 * Created on September 23, 2005, 11:31 AM
 *
 */

package Machinetta.State.BeliefType.TeamBelief.Constraints;

import Machinetta.State.BeliefID;
import Machinetta.State.BeliefNameID;
import Machinetta.State.BeliefType.Belief;

/**
 * This class describes a piece of information that a role needs
 * before it can be instantiated.
 *
 *
 * @author pscerri
 */
public class GeneratedInformationRequirement extends Belief {
    
    /**
     * The specific class type for the belief.
     */
    public Class type = null;
    
    /** Some label for distinguishing between DirectedInformationRequirements.
     *
     * This name will be used as the key when placing the belief in the 
     * role belief hashtable.
     */
    public String name = null;
    
    /**
     * If there is going to be a very specific belief id, this will be non-null
     */
    public BeliefID proscribed = null;
    
    /** Creates a new instance of GeneratedInformationRequirement */
    public GeneratedInformationRequirement(String name, Class type) {
        this.type = type;
        this.name = name;
    }
    
    /** Creates a new instance of GeneratedInformationRequirement */
    public GeneratedInformationRequirement(String name, Class type, BeliefID proscribed) {
        this.type = type;
        this.proscribed = proscribed;
        this.name = name;
    }
    
    public Machinetta.State.BeliefID makeID() {
        if (id == null) {
            id = new BeliefNameID("GIR: " + type + ":" + (name == null? proscribed : name));
        }
        return id;
    }
    
    public static final long serialVersionUID = 1L;
    
}
