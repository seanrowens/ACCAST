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
 * SAMissile.java
 *
 * Created on Sept 27, 2005, 11:16 AM
 */

package AirSim.Environment.Assets;

import java.awt.Rectangle;
import Gui.*;
import AirSim.Environment.*;
import AirSim.Environment.GUI.MainFrame;
import java.awt.*;



/**
 * 
 *
 * @author rglinton
 */
// public class Emitter extends Asset {
public interface Emitter {
        
    public final static int CHANNEL_MIN = 1;
    public final static int CHANNEL_MAX = 1000;
    public final static int CHANNEL_DEFAULT = 1;

    public boolean isDetected();
    
    public boolean inteferer();
    
    /** True, iff this asset is currently emitting a detectable RF signal. */
    public boolean currentlyEmitting();

    // Set the percentage of the period that this emitter is emitting
    // - i.e. if period is set to 5 minutes, and percentage is set to
    // .20, then the emitter will be emitting for 1 minute out of 5,
    // off for four minutes, on for one, off for four minutes, on for
    // one, etc.
    public void setIntermittent(long periodMs, double percent);
    
    // Returns the channels (possibly multiple) the emitter is
    // emitting on
    public int[] getChannels();
}
