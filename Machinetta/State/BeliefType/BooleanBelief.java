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

 * BooleanBelief.java

 *

 * Created on 3 July 2002, 23:26

 */



package Machinetta.State.BeliefType;



import Machinetta.Configuration;

import Machinetta.State.*;



import org.w3c.dom.Node;



/**

 *

 * @author  scerri

 */

public class BooleanBelief extends Belief {

        

    /** BooleanBeliefs are intially false */

    public boolean value = false;

    

    /** Creates a new instance of BooleanBelief

     * @param id The unique identifier for this Belief

     */

    public BooleanBelief(BeliefID id, boolean value) {

        super(id);

        this.value = value;

    }

    

    /** Creates a new instance of BooleanBelief

     * @param id The unique identifier for this Belief

     */

    public BooleanBelief(BeliefID id) {

        this(id, new Boolean(Configuration.DEFAULT_BOOLEAN_VALUE).booleanValue());

    }

    

    /** Access method

     * @param value The new value of this belief

     */

    public void setValue(boolean value) { this.value = value; }

    

    /** Access method

     * @return Current value of belief

     */

    public boolean getValue() { return value; }

    

    /** Returns a string representing this Belief

     * @return String detailing Belief

     */

    public String toString() {

        return super.toString() + " : " + (new Boolean(value)).toString();

    }   

    

    public BeliefID makeID() {

        return new Machinetta.State.BeliefNameID("AnonymousBooleanBelief");

    }    

    

    public static final long serialVersionUID = 1L;



}

