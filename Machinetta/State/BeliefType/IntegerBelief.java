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
 /* IntegerBelief.java

 *

 * Created on 3 July 2002, 23:37

 */



package Machinetta.State.BeliefType;



import Machinetta.Configuration;

import Machinetta.State.BeliefID;



import org.w3c.dom.Node;



/**

 *

 * @author  scerri

 */

public class IntegerBelief extends Belief {



    /** The current value of the belief */

    public int value = 0;

    

    /** Lowest possible value of the belief */

    public int lowerBound = Integer.MIN_VALUE;

    

    /** Maximum possible value of the belief */

    public int upperBound = Integer.MAX_VALUE;



    public IntegerBelief() {}

    

    /** Creates a new instance of IntegerBelief

     * @param id The unique identifier for this belief

     */

    public IntegerBelief(BeliefID id) {

        this(id, new Integer(Configuration.DEFAULT_INTEGER_VALUE).intValue(), 

            Integer.MIN_VALUE, Integer.MAX_VALUE);

    }



    /** Creates a new instance of IntegerBelief

     * @param id The unique identifier for this belief

     * @param value The initial value of the belief

     */

    public IntegerBelief(BeliefID id, int value) {

        this(id, value, Integer.MIN_VALUE, Integer.MAX_VALUE);

    }

    

    /**

     * @param id The unique identifier for this belief

     * @param lb Lowest possible value of this belief

     * @param ub Highest possible value of this belief

     */

    public IntegerBelief(BeliefID id, int lb, int ub) {

        this(id, new Integer(Configuration.DEFAULT_INTEGER_VALUE).intValue(), lb, ub);

    }



    /**

     * @param id The unique identifier for this belief

     * @param lb Lowest possible value of this belief

     * @param ub Highest possible value of this belief

     */

    public IntegerBelief(BeliefID id, int value, int lb, int ub) {

        super(id);

        this.value = value;

        lowerBound = lb;

        upperBound = ub;

    }



    /** Set the value of the belief */

    public void setValue (int value) {

        this.value = value;

        if (value > upperBound) value = upperBound;

        if (value < lowerBound) value = lowerBound;

    }

    

    /** Get the value of the belief */

    public int getValue () { return value; }



    /** Print

     * @return String giving id and value

     */

    public String toString() {

        return super.toString() + " : " + new Integer(value).intValue();

    }

    

    public BeliefID makeID() {

        if (id != null) return id;

        return new Machinetta.State.BeliefNameID("AnonymousInteger");

    }    

    

    public static final long serialVersionUID = 1L;

}
