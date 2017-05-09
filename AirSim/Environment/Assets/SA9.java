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
 * SA9.java
 *
 * Created on May 13, 2005, 4:01 PM
 */

package AirSim.Environment.Assets;

import AirSim.Environment.Assets.Tasks.DefendAir;

/**
 *
 * @author pscerri
 */
public class SA9 extends Tank implements Emitter {
        
    private long intermittentPeriodMs = -1;
    private long intermittentPeriodStartMs;
    private double intermittentPercent;

    private int[] channels;

    /** Creates a new instance of SA9 */
    public SA9(String id, int x, int y, int channel) {
        super(id, x, y);
        setSpeed(0.01);
// 	SEE_STATE_DIST = 6000;
// 	FIRE_DIST = 5000;
// 	SENSE_RADIUS = 6000;
 	SEE_STATE_DIST = 3000;
 	FIRE_DIST = 2500;
 	SENSE_RADIUS = 3000;

	fireAtSurfaceCapable = false;
	fireAtAirCapable = true;
	samMaxSpeedMetersPerSecond = kphToms(machTokph(1.8));

	setStepsBetweenFiring((int)(10 * env.getStepsPerSecond()));

	//        setStepsBetweenFiring(10);
        // SA9 default behavior is to defend the air
        DefendAir task = new DefendAir(true, false);
        addTask(task);
        
	channels = new int[1];
	channels[0] = channel;
    }
    
    public SA9(String id, int x, int y) {
	this(id,x,y,Emitter.CHANNEL_DEFAULT);
    }

    public Asset.Types getType() { return Asset.Types.SA9; }
    
    public boolean isDetected() {
        return false;
    }
    
    public boolean inteferer() {
        return false;
    }

    /** SA-9s always emit a signal. */
    public boolean currentlyEmitting() {
	if(intermittentPeriodMs == -1)
	    return true;
	else {
	    long simtime = env.getSimTimeMs();
	    long sinceStartMs = simtime - intermittentPeriodStartMs;
	    long remainderMs = sinceStartMs % intermittentPeriodMs;
	    if(remainderMs < (intermittentPeriodMs * intermittentPercent))
		return true;
	    else
		return false;
	}
    }

    public void setIntermittent(long periodMs, double percent) {
	this.intermittentPeriodMs = periodMs;
	this.intermittentPeriodStartMs = env.getSimTimeMs();
	this.intermittentPercent = percent;
    }

    public int[] getChannels() {
	return channels;
    }
}
