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

 * RAPAvailabilityBelief.java

 *

 * Created on 11 July 2002, 17:33

 */



package Machinetta.State.BeliefType;



import Machinetta.State.BeliefID;



/**

 * Belief about a RAPs availability

 *

 * Intended that could be in various different formats, eventually

 *

 * @author  scerri

 */

public class RAPAvailabilityBelief extends Belief {

   

    /** Amount of time the RAP has free on schedule, 1.0 means all free */

    public float scheduleFreePercentage = 0.0f;

    

    /** For auto create from XML */

    public RAPAvailabilityBelief() {}

    

    /** Creates a new instance of RAPAvailabilityBelief

     * @param id Should refer to the RAP to which this availability info pertains

     */

    public RAPAvailabilityBelief(BeliefID id) {

        this(id, 0.0f);

    }

    

    /** Creates a new instance of RAPAvailabilityBelief

     * @param id Should refer to the RAP to which this availability info pertains

     * @param avail Percentage of schedule free

     */

    public RAPAvailabilityBelief(BeliefID id, float avail) {

        super(id);

        scheduleFreePercentage = avail;

    }

    

    /** Indication of how much free time entity has

     * @return Value between 1.0 (completely free) and 0.0 (completely booked)

     */

    public float getScheduleFreeTime() { return scheduleFreePercentage; }

    

    /** Indication of how much free time entity has

     * @param amount Value between 1.0 (completely free) and 0.0 (completely booked)

     */

    public void setScheduleFreeTime(float value) { scheduleFreePercentage = value; }    

    

    public String toString() { return "Availability : " + scheduleFreePercentage; }

    

    public BeliefID makeID() { return new Machinetta.State.BeliefNameID("Availability"); }

 

    public static final long serialVersionUID = 1L;

}
