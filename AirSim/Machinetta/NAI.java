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
 * NAI.java
 *
 * Created on August 16, 2005, 6:56 PM
 *
 */
package AirSim.Machinetta;

import AirSim.Environment.Area;
import Machinetta.State.BeliefID;
import Machinetta.State.BeliefNameID;
import Machinetta.State.BeliefType.Belief;

/**
 * An NAI is a military term for a "Named Area of Interest", which is
 * exactly what it sounds like, we're going to call this spot "alpha"
 * or some such.  It's just a way of labeling a place for a variety of
 * reasons - as tactically significant spots, rendevous points,
 * medevac points, etc.  It's implemented simply as a 2d box with a
 * name.  This class is used as a parameter for some roles (patrol)
 * but primarily as an output from automated terrain analysis.
 *
 * @author pscerri
 */
public class NAI extends Belief {
    
    public String name = null;
    public double x1, y1, x2, y2;
    
    /** For XML */
    public NAI() {
        
    }
    
    /** Creates a new instance of NAI */
    public NAI(String name, double x1, double y1, double x2, double y2) {
        this.name = name;
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        makeID();
    }
    
    public BeliefID makeID() { 
        if (id == null) {
            id = new BeliefNameID("NAI:" + name); 
        }
        return id;
    }
    
    private static final long serialVersionUID = 1L;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public java.awt.Point getCenter() {
        return new java.awt.Point((int)((x1+x2)/2), (int)((y1+y2)/2));
    }    
    
    public String toString() { return super.toString() + "@(" + x1 + "," + y1 + ")-(" + x2 + "," + y2 + ")"; }
}
