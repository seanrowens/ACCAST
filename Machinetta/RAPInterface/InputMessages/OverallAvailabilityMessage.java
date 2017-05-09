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

 * OverallAvailabilityMessage.java

 *

 * Created on 11 July 2002, 16:25

 */



package Machinetta.RAPInterface.InputMessages;



/**

 * Message with the amount of free time in RAP's "schedule"

 *

 * Could be extended to support things like confidence in assesment, likelihood of changing

 *

 * @author  scerri

 */

public class OverallAvailabilityMessage extends AvailabilityMessage {

    

    /** Creates a new instance of OverallAvailabilityMessage */

    public OverallAvailabilityMessage(float availability) {

        this.availability = availability;

        if (availability > 1.0f) this.availability = 1.0f;

        if (availability < 0.0f) this.availability = 0.0f;

    }



    /** Access

     * @return Amount of time the RAP has free in schedule, 1.0 is completely free

     */

    public float getAvailability () { return availability; }

    

    /** Access

     * @return String with availability details 

     */

    public String toString () { return "Availability " + availability; }

    

    /** Amount of time the RAP has free in its schedule,

     * 1.0 means completely free

     */

    private float availability = 0.0f;

    

    public static final long serialVersionUID = 1L;

}
