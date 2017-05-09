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
 * Point2D.java
 *
 * Created on July 19, 2005, 1:08 PM
 *
 * Some overkill to have as a class for now, but might eventually be useful.
 *
 */

package AirSim.Machinetta;

import Machinetta.State.BeliefType.*;
import java.io.*;

/**
 * A point in 2D (i.e. at ground level.)  Typically used to specify
 * strike points for armed UAVs, destinations for Move and Attack
 * plans, etc.
 *
 * @author pscerri
 */
public class Point2D extends Belief {
    
    public int x, y;
    
    public Point2D() {
	x = 0;
	y = 0;
    }

     public Point2D(int x, int y) {
        this.x = x;
        this.y = y;
        id = makeID();
    }
     
    public int getY() {
        return y;
    }

    public int getX() {
        return x;
    }

    /**
     * This is obviously a bit dodgy, but don't want to fix it either ... 
     */
    public Machinetta.State.BeliefID makeID() {
        return new Machinetta.State.BeliefNameID(x+":"+y);
    }

    public final static long serialVersionUID = 1L;

    public String toString() {
	return x+", "+y;
    }

}
