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
package AirSim.Machinetta;

import java.text.DecimalFormat;

public class SimTime {

    private static DecimalFormat fmt = new DecimalFormat("0.00000");
    private static long prevWallClockMs  = 0;
    private static long prevSimTimeMs = 0;
    private static long latestWallClockMs = 0;
    private static long latestSimTimeMs = 0;
    private static long elapsedSimTimeMs = 0;
    private static long elapsedWallClockMs = 0;
    private static double rate = 0;

    {
	latestSimTimeMs = 0;
	latestWallClockMs = System.currentTimeMillis();
	prevSimTimeMs = 0;
	prevWallClockMs = System.currentTimeMillis();
    }

    public static void updateTime(long newTime) {
	prevSimTimeMs = latestSimTimeMs;
	prevWallClockMs = latestWallClockMs;
	latestWallClockMs = System.currentTimeMillis();
	latestSimTimeMs = newTime;
	elapsedSimTimeMs = latestSimTimeMs - prevSimTimeMs;
	elapsedWallClockMs = latestWallClockMs - prevWallClockMs;
	rate = ((double)elapsedSimTimeMs / (double)elapsedWallClockMs);
	//	Machinetta.Debugger.debug("Updated simtime, prev simMs="+prevSimTimeMs+" , prev wallMs= "+prevWallClockMs+" , now simMs= "+latestSimTimeMs+" now wallMs= "+latestWallClockMs+" , simMs elapsed= "+elapsedSimTimeMs+" , wallMs elapsed= "+elapsedWallClockMs+", simMs/wallMs="+rate,1,"SimTime");
	Machinetta.Debugger.debug("Updated simtime= "+latestSimTimeMs+" walltime= "+latestWallClockMs+" simMs/wallMs="+rate,0,"SimTime");
    }

    // Note this doesn't account for the time it takes the UDP packet
    // to reach us.  We could maybe estimate that if we were sending
    // messages to the server and timing time it took to get a
    // response.
    public static long getEstimatedTime() {
	double sinceSimTime = System.currentTimeMillis() - latestWallClockMs;
	return (latestSimTimeMs + ((long)(sinceSimTime * rate)));
    }

}
