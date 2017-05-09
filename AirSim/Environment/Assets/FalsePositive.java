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
 * FalsePositive.java
 *
 * Created on August 26, 2005, 3:31 PM
 *
 */

package AirSim.Environment.Assets;

import AirSim.Environment.Vector3D;

/**
 * This is not really an asset, but a thing that might lead other
 * assets to think that it is a thing.
 *
 * @author pscerri
 */
public class FalsePositive extends Asset {
    
    public double falseDetectRate = 0.0;
    
    /** Creates a new instance of FalsePositive */
    public FalsePositive(String id, int x, int y, double fdr) {
        super(id, x, y, 0, new Vector3D(0.0, 0.0, 0.0));
        this.falseDetectRate = fdr;
        visibleOnGT = false;
    }
    
    /** Unsuprisingly, explosions don't do much to apparitions ... */
    public void explosion(Asset a) {}
    
    public AirSim.Environment.Assets.Asset.Types getType() { return Asset.Types.CLUTTER; }
    
    public void sense() {}
    
    public String toString() { return "False Positive @ " + location; }
}
