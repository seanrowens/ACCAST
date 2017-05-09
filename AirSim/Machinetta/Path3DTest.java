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
 * Path3D.java
 *
 * Created on March 8, 2006, 11:43 AM
 *
 */

package AirSim.Machinetta;

import AirSim.Environment.Waypoint;
import AirSim.Environment.Vector3D;
import Machinetta.State.BeliefType.ProxyID;
import java.io.Serializable;
import java.util.ArrayList;
import java.text.DecimalFormat;

/**
 * @fix This needs to have time added to it.
 *
 * @author pscerri
 */
public class Path3DTest {
    private final static DecimalFormat fmt = new DecimalFormat("0.0");

    public static Path3D interpolatePath(Path3D path, int startTime, int endTime, int increment) {
	Waypoint[] ary = path.getWaypointsAry();
	Path3D newp = new Path3D();
	Waypoint last=ary[0];
	Waypoint next = ary[1];
	int loopAry = 2;
	
	for(int loopTime = startTime; loopTime < endTime; loopTime += increment) {
	    while(loopTime > next.time) {
		if((loopAry+1) > ary.length)
		    break;
		last = next;
		next = ary[loopAry++];
	    }
	    newp.addPoint(Waypoint.interpolate(last,next,loopTime));
	}
	return newp;
    }

    public static void comparePath(Path3D path1, Path3D path2, double range, double zrange) {
	double rangeSqd = range*range;
        Waypoint[] ary1 = path1.getWaypointsAry();
        Waypoint[] ary2 = path2.getWaypointsAry();
	int ind1 = 0;
	int ind2 = 0;
	while(true) {
	    if(ary1[ind1].time > ary2[ind2].time) {
		while(ary1[ind1].time > ary2[ind2].time) {
		    ind2++;
		    if(ind2 >= ary2.length)
			return;
		}
	    }
	    else if(ary1[ind1].time < ary2[ind2].time) {
		while(ary1[ind1].time < ary2[ind2].time) {
		    ind1++;
		    if(ind1 >= ary1.length)
			return;
		}
	    }
	    Waypoint wp1 = ary1[ind1];
	    Waypoint wp2 = ary2[ind2];
	    double zdiff = (wp1.z - wp2.z);
	    if(zdiff < 0) zdiff = -zdiff;
	    if(zdiff < zrange) {
		double distSqd = (wp2.x-wp1.x)*(wp2.x-wp1.x) + (wp2.y-wp1.y)*(wp2.y-wp1.y) + (wp2.z-wp1.z)*(wp2.z-wp1.z);
		if(distSqd <= rangeSqd) {
		    double dist = Math.sqrt(distSqd);
		    Machinetta.Debugger.debug("CONFLICT " +wp1.time+" "+fmt.format(dist)+" "+wp1+" "+wp2,1,"Path3DTest");
		    return;
		}
	    }
	    ind1++;
	    ind2++;
	    
	    if(ind2 >= ary2.length)
		return;
	    if(ind1 >= ary1.length)
		return;
	}
    }

    public static void main(String argv[]) {
        Path3D path23 = new Path3D();
	path23.addPoint(new Waypoint(40550.0,23440.0,1651.0,63648));
	path23.addPoint(new Waypoint(38780.0,21047.0,1656.0,135155));
	path23.addPoint(new Waypoint(36298.0,19648.0,1656.0,203603));
	path23.addPoint(new Waypoint(34548.0,18226.0,1656.0,257775));
	path23.addPoint(new Waypoint(32185.0,18330.0,1654.0,314599));
	path23.addPoint(new Waypoint(30373.0,17074.0,1655.0,367566));
        Waypoint[] path23Ary = path23.getWaypointsAry();
        
        Path3D path48 = new Path3D();
	path48.addPoint(new Waypoint(36767.0,8404.0,1554.0,80883));
	path48.addPoint(new Waypoint(34445.0,10106.0,1558.0,150048));
	path48.addPoint(new Waypoint(33708.0,12089.0,1557.0,200872));
	path48.addPoint(new Waypoint(31964.0,13372.0,1562.0,252887));
	path48.addPoint(new Waypoint(30616.0,14304.0,1563.0,292258));
	path48.addPoint(new Waypoint(29980.0,16671.0,1563.0,351140));
	path48.addPoint(new Waypoint(30408.0,17683.0,1562.0,377537));
        Waypoint[] path48Ary = path48.getWaypointsAry();

	
	long startTime;
	long endTime;

	Machinetta.Debugger.debug("interpolating path23",1,"Path3DTest");
	startTime = System.currentTimeMillis();
	Path3D ipath23 = interpolatePath(path23,80900,367500,1000);
	endTime = System.currentTimeMillis();
	Machinetta.Debugger.debug("elapsed="+(endTime - startTime),1,"Path3DTest");
	Machinetta.Debugger.debug("interpolated path23 has "+ipath23.size()+" waypoints",1,"Path3DTest");
	Machinetta.Debugger.debug("interpolating path48",1,"Path3DTest");
	startTime = System.currentTimeMillis();
	Path3D ipath48 = interpolatePath(path48,80900,367500,1000);
	endTime = System.currentTimeMillis();
	Machinetta.Debugger.debug("elapsed="+(endTime - startTime),1,"Path3DTest");
	Machinetta.Debugger.debug("interpolated path48 has "+ipath48.size()+" waypoints",1,"Path3DTest");
// 	Path3D ipath23 = interpolatePath(path23,80883,367566,1);
// 	Path3D ipath48 = interpolatePath(path48,80883,367566,1);

	Machinetta.Debugger.debug("comparing",1,"Path3DTest");
	startTime = System.currentTimeMillis();
	comparePath(ipath23, ipath48,2000,150);
	endTime = System.currentTimeMillis();
	Machinetta.Debugger.debug("elapsed="+(endTime - startTime),1,"Path3DTest");

        Path3D path23actual = getPath23();
        Waypoint[] path23actualAry = path23actual.getWaypointsAry();

        Path3D path48actual = getPath48();
        Waypoint[] path48actualAry = path48actual.getWaypointsAry();

	Machinetta.Debugger.debug("Comparing path23actual to path23 plan",1,"Path3DTest");
	    
	Waypoint last;
	Waypoint next;
	int loopPlan;

	last = path23Ary[0];
	next = path23Ary[1];
	loopPlan = 2;
	for(int loopi = 0; loopi < path23actualAry.length; loopi++) {
	    Waypoint actual = path23actualAry[loopi];
	    while(actual.time > next.time) {
		Machinetta.Debugger.debug("actual.time="+actual.time+", last="+last.time+", next="+next.time+", skipping ahead to plan Waypoint "+loopPlan,1,"Path3DTest");
		if((loopPlan+1) > path23Ary.length)
		    break;
		last = next;
		next = path23Ary[loopPlan++];
	    }
	    Waypoint expected = Waypoint.interpolate(last,next,actual.time);
	    Vector3D toExpected = actual.toVector(expected);
	    double dist = toExpected.length();
            Machinetta.Debugger.debug(actual.time+"\t"+dist+"\t"+actual+"\t"+expected,1,"Path3DTest");
	}

	Machinetta.Debugger.debug("Comparing path48actual to path48 plan",1,"Path3DTest");

	last = path48Ary[0];
	next = path48Ary[1];
	loopPlan = 2;
	for(int loopi = 0; loopi < path48actualAry.length; loopi++) {
	    Waypoint actual = path48actualAry[loopi];
	    while(actual.time > next.time) {
		Machinetta.Debugger.debug("actual.time="+actual.time+", last="+last.time+", next="+next.time+", skipping ahead to plan Waypoint "+loopPlan,1,"Path3DTest");
		if((loopPlan+1) > path48Ary.length)
		    break;
		last = next;
		next = path48Ary[loopPlan++];
	    }
	    Waypoint expected = Waypoint.interpolate(last,next,actual.time);
	    Vector3D toExpected = actual.toVector(expected);
	    double dist = toExpected.length();
            Machinetta.Debugger.debug(actual.time+"\t"+dist+"\t"+actual+"\t"+expected,1,"Path3DTest");
	}

	if(true) System.exit(0);

	Path3D.DONE_AFTER_FIRST_CONFLICT=false;
	Path3D.DEBUG=true;
	Path3D.ITERATIONS_PER_PATH = 1000;

        Machinetta.Debugger.debug("Checking path23 against path48 (SHOULD CONFLICT)",1,"Path3D"); 
        if(Path3D.conflicts(path23Ary,path48Ary,2000,150)) {
            Machinetta.Debugger.debug("    path23 CONFLICTs with path48",1,"Path3D");
        } else {
            Machinetta.Debugger.debug("    path23 does NOT conflict with path48",1,"Path3D");
        }

        Machinetta.Debugger.debug("Checking path23actual against path48actual (SHOULD CONFLICT)",1,"Path3D"); 
        if(Path3D.conflicts(path23actualAry,path48actualAry,2000,150)) {
            Machinetta.Debugger.debug("    path23actual CONFLICTs with path48actual",1,"Path3D");
        } else {
            Machinetta.Debugger.debug("    path23actual does NOT conflict with path48actual",1,"Path3D");
        }

        Machinetta.Debugger.debug("Checking path23actual against path23 (SHOULD CONFLICT)",1,"Path3D"); 
        if(Path3D.conflicts(path23actualAry,path23Ary,2000,150)) {
            Machinetta.Debugger.debug("    path23actual CONFLICTs with path23",1,"Path3D");
        } else {
            Machinetta.Debugger.debug("    path23actual does NOT conflict with path23",1,"Path3D");
        }

        Machinetta.Debugger.debug("Checking path48actual against path48 (SHOULD CONFLICT)",1,"Path3D"); 
        if(Path3D.conflicts(path48actualAry,path48Ary,2000,150)) {
            Machinetta.Debugger.debug("    path48actual CONFLICTs with path48",1,"Path3D");
        } else {
            Machinetta.Debugger.debug("    path48actual does NOT conflict with path48",1,"Path3D");
        }



	Path3D.DONE_AFTER_FIRST_CONFLICT=true;
	Path3D.DEBUG=false;
	Path3D.ITERATIONS_PER_PATH = 100;
    }


    public static Path3D getPath23() {
        Path3D path23actual = new Path3D();

	path23actual.addPoint(new Waypoint(30938.01,17429.89,1655.00,351300));
	path23actual.addPoint(new Waypoint(30934.53,17427.70,1655.00,351400));
	path23actual.addPoint(new Waypoint(30931.06,17425.52,1655.00,351500));
	path23actual.addPoint(new Waypoint(30927.59,17423.33,1655.00,351600));
	path23actual.addPoint(new Waypoint(30924.11,17421.14,1655.00,351700));
	path23actual.addPoint(new Waypoint(30920.64,17418.95,1655.00,351800));
	path23actual.addPoint(new Waypoint(30917.17,17416.77,1655.00,351900));
	path23actual.addPoint(new Waypoint(30913.69,17414.58,1655.00,352000));
	path23actual.addPoint(new Waypoint(30910.22,17412.39,1655.00,352100));
	path23actual.addPoint(new Waypoint(30906.75,17410.20,1655.00,352200));
	path23actual.addPoint(new Waypoint(30903.27,17408.01,1655.00,352300));
	path23actual.addPoint(new Waypoint(30899.80,17405.83,1655.00,352400));
	path23actual.addPoint(new Waypoint(30896.33,17403.64,1655.00,352500));
	path23actual.addPoint(new Waypoint(30892.85,17401.45,1655.00,352600));
	path23actual.addPoint(new Waypoint(30889.38,17399.26,1655.00,352700));
	path23actual.addPoint(new Waypoint(30885.91,17397.07,1655.00,352800));
	path23actual.addPoint(new Waypoint(30882.43,17394.89,1655.00,352900));
	path23actual.addPoint(new Waypoint(30878.96,17392.70,1655.00,353000));
	path23actual.addPoint(new Waypoint(30875.49,17390.51,1655.00,353100));
	path23actual.addPoint(new Waypoint(30872.01,17388.32,1655.00,353200));
	path23actual.addPoint(new Waypoint(30868.54,17386.13,1655.00,353300));
	path23actual.addPoint(new Waypoint(30865.06,17383.95,1655.00,353400));
	path23actual.addPoint(new Waypoint(30861.59,17381.76,1655.00,353500));
	path23actual.addPoint(new Waypoint(30858.12,17379.57,1655.00,353600));
	path23actual.addPoint(new Waypoint(30854.64,17377.38,1655.00,353700));
	path23actual.addPoint(new Waypoint(30851.17,17375.20,1655.00,353800));
	path23actual.addPoint(new Waypoint(30847.70,17373.01,1655.00,353900));
	path23actual.addPoint(new Waypoint(30844.22,17370.82,1655.00,354000));
	path23actual.addPoint(new Waypoint(30840.75,17368.63,1655.00,354100));
	path23actual.addPoint(new Waypoint(30837.28,17366.44,1655.00,354200));
	path23actual.addPoint(new Waypoint(30833.80,17364.26,1655.00,354300));
	path23actual.addPoint(new Waypoint(30830.33,17362.07,1655.00,354400));
	path23actual.addPoint(new Waypoint(30826.86,17359.88,1655.00,354500));
	path23actual.addPoint(new Waypoint(30823.38,17357.69,1655.00,354600));
	path23actual.addPoint(new Waypoint(30819.91,17355.50,1655.00,354700));
	path23actual.addPoint(new Waypoint(30816.44,17353.32,1655.00,354800));
	path23actual.addPoint(new Waypoint(30812.96,17351.13,1655.00,354900));
	path23actual.addPoint(new Waypoint(30809.49,17348.94,1655.00,355000));
	path23actual.addPoint(new Waypoint(30806.02,17346.75,1655.00,355100));
	path23actual.addPoint(new Waypoint(30802.54,17344.56,1655.00,355200));
	path23actual.addPoint(new Waypoint(30799.07,17342.38,1655.00,355300));
	path23actual.addPoint(new Waypoint(30795.60,17340.19,1655.00,355400));
	path23actual.addPoint(new Waypoint(30792.12,17338.00,1655.00,355500));
	path23actual.addPoint(new Waypoint(30788.65,17335.81,1655.00,355600));
	path23actual.addPoint(new Waypoint(30785.17,17333.62,1655.00,355700));
	path23actual.addPoint(new Waypoint(30781.70,17331.44,1655.00,355800));
	path23actual.addPoint(new Waypoint(30778.23,17329.25,1655.00,355900));
	path23actual.addPoint(new Waypoint(30774.75,17327.06,1655.00,356000));
	path23actual.addPoint(new Waypoint(30771.28,17324.87,1655.00,356100));
	path23actual.addPoint(new Waypoint(30767.81,17322.69,1655.00,356200));
	path23actual.addPoint(new Waypoint(30764.33,17320.50,1655.00,356300));
	path23actual.addPoint(new Waypoint(30760.86,17318.31,1655.00,356400));
	path23actual.addPoint(new Waypoint(30757.39,17316.12,1655.00,356500));
	path23actual.addPoint(new Waypoint(30753.91,17313.93,1655.00,356600));
	path23actual.addPoint(new Waypoint(30750.44,17311.75,1655.00,356700));
	path23actual.addPoint(new Waypoint(30746.97,17309.56,1655.00,356800));
	path23actual.addPoint(new Waypoint(30743.49,17307.37,1655.00,356900));
	path23actual.addPoint(new Waypoint(30740.02,17305.18,1655.00,357000));
	path23actual.addPoint(new Waypoint(30736.55,17302.99,1655.00,357100));
	path23actual.addPoint(new Waypoint(30733.07,17300.81,1655.00,357200));
	path23actual.addPoint(new Waypoint(30729.60,17298.62,1655.00,357300));
	path23actual.addPoint(new Waypoint(30726.13,17296.43,1655.00,357400));
	path23actual.addPoint(new Waypoint(30722.65,17294.24,1655.00,357500));
	path23actual.addPoint(new Waypoint(30719.18,17292.05,1655.00,357600));
	path23actual.addPoint(new Waypoint(30715.71,17289.87,1655.00,357700));
	path23actual.addPoint(new Waypoint(30712.23,17287.68,1655.00,357800));
	path23actual.addPoint(new Waypoint(30708.76,17285.49,1655.00,357900));
	path23actual.addPoint(new Waypoint(30705.28,17283.30,1655.00,358000));
	path23actual.addPoint(new Waypoint(30701.81,17281.12,1655.00,358100));
	path23actual.addPoint(new Waypoint(30698.34,17278.93,1655.00,358200));
	path23actual.addPoint(new Waypoint(30694.86,17276.74,1655.00,358300));
	path23actual.addPoint(new Waypoint(30691.39,17274.55,1655.00,358400));
	path23actual.addPoint(new Waypoint(30687.92,17272.36,1655.00,358500));
	path23actual.addPoint(new Waypoint(30684.44,17270.18,1655.00,358600));
	path23actual.addPoint(new Waypoint(30680.97,17267.99,1655.00,358700));
	path23actual.addPoint(new Waypoint(30677.50,17265.80,1655.00,358800));
	path23actual.addPoint(new Waypoint(30674.02,17263.61,1655.00,358900));
	path23actual.addPoint(new Waypoint(30670.55,17261.42,1655.00,359000));
	path23actual.addPoint(new Waypoint(30667.08,17259.24,1655.00,359100));
	path23actual.addPoint(new Waypoint(30663.60,17257.05,1655.00,359200));
	path23actual.addPoint(new Waypoint(30660.13,17254.86,1655.00,359300));
	path23actual.addPoint(new Waypoint(30656.66,17252.67,1655.00,359400));
	path23actual.addPoint(new Waypoint(30653.18,17250.48,1655.00,359500));
	path23actual.addPoint(new Waypoint(30649.71,17248.30,1655.00,359600));
	path23actual.addPoint(new Waypoint(30646.24,17246.11,1655.00,359700));
	path23actual.addPoint(new Waypoint(30642.76,17243.92,1655.00,359800));
	path23actual.addPoint(new Waypoint(30639.29,17241.73,1655.00,359900));
	path23actual.addPoint(new Waypoint(30635.82,17239.54,1655.00,360000));
	path23actual.addPoint(new Waypoint(30632.34,17237.36,1655.00,360100));
	path23actual.addPoint(new Waypoint(30628.87,17235.17,1655.00,360200));
	path23actual.addPoint(new Waypoint(30625.39,17232.98,1655.00,360300));
	path23actual.addPoint(new Waypoint(30621.92,17230.79,1655.00,360400));
	path23actual.addPoint(new Waypoint(30618.45,17228.61,1655.00,360500));
	path23actual.addPoint(new Waypoint(30614.97,17226.42,1655.00,360600));
	path23actual.addPoint(new Waypoint(30611.50,17224.23,1655.00,360700));
	path23actual.addPoint(new Waypoint(30608.03,17222.04,1655.00,360800));
	path23actual.addPoint(new Waypoint(30604.55,17219.85,1655.00,360900));
	path23actual.addPoint(new Waypoint(30601.08,17217.67,1655.00,361000));
	path23actual.addPoint(new Waypoint(30597.61,17215.48,1655.00,361100));
	path23actual.addPoint(new Waypoint(30594.13,17213.29,1655.00,361200));
	path23actual.addPoint(new Waypoint(30590.66,17211.10,1655.00,361300));
	path23actual.addPoint(new Waypoint(30587.19,17208.91,1655.00,361400));
	path23actual.addPoint(new Waypoint(30583.71,17206.73,1655.00,361500));
	path23actual.addPoint(new Waypoint(30580.24,17204.54,1655.00,361600));
	path23actual.addPoint(new Waypoint(30576.77,17202.35,1655.00,361700));
	path23actual.addPoint(new Waypoint(30573.29,17200.16,1655.00,361800));
	path23actual.addPoint(new Waypoint(30569.82,17197.97,1655.00,361900));
	path23actual.addPoint(new Waypoint(30566.35,17195.79,1655.00,362000));
	path23actual.addPoint(new Waypoint(30562.87,17193.60,1655.00,362100));
	path23actual.addPoint(new Waypoint(30559.40,17191.41,1655.00,362200));
	path23actual.addPoint(new Waypoint(30555.93,17189.22,1655.00,362300));
	path23actual.addPoint(new Waypoint(30552.45,17187.03,1655.00,362400));
	path23actual.addPoint(new Waypoint(30548.98,17184.85,1655.00,362500));
	path23actual.addPoint(new Waypoint(30545.50,17182.66,1655.00,362600));
	path23actual.addPoint(new Waypoint(30542.03,17180.47,1655.00,362700));
	path23actual.addPoint(new Waypoint(30538.56,17178.28,1655.00,362800));
	path23actual.addPoint(new Waypoint(30535.08,17176.10,1655.00,362900));
	path23actual.addPoint(new Waypoint(30531.61,17173.91,1655.00,363000));
	path23actual.addPoint(new Waypoint(30528.14,17171.72,1655.00,363100));
	path23actual.addPoint(new Waypoint(30526.33,17168.30,1654.62,363200));
	path23actual.addPoint(new Waypoint(30526.48,17164.43,1654.23,363300));
	path23actual.addPoint(new Waypoint(30527.76,17160.76,1654.00,363400));
	path23actual.addPoint(new Waypoint(30529.03,17157.09,1654.00,363500));
	path23actual.addPoint(new Waypoint(30530.31,17153.42,1654.00,363600));
	path23actual.addPoint(new Waypoint(30531.58,17149.75,1654.00,363700));
	path23actual.addPoint(new Waypoint(30532.86,17146.07,1654.00,363800));
	path23actual.addPoint(new Waypoint(30534.13,17142.40,1654.00,363900));
	path23actual.addPoint(new Waypoint(30535.41,17138.73,1654.00,364000));
	path23actual.addPoint(new Waypoint(30536.68,17135.05,1654.00,364100));
	path23actual.addPoint(new Waypoint(30537.96,17131.38,1654.00,364200));
	path23actual.addPoint(new Waypoint(30539.23,17127.71,1654.00,364300));
	path23actual.addPoint(new Waypoint(30540.51,17124.04,1654.00,364400));
	path23actual.addPoint(new Waypoint(30541.78,17120.36,1654.00,364500));
	path23actual.addPoint(new Waypoint(30543.06,17116.69,1654.00,364600));
	path23actual.addPoint(new Waypoint(30544.33,17113.02,1654.00,364700));
	path23actual.addPoint(new Waypoint(30545.61,17109.34,1654.00,364800));
	path23actual.addPoint(new Waypoint(30546.88,17105.67,1654.00,364900));
	path23actual.addPoint(new Waypoint(30548.16,17102.00,1654.00,365000));
	path23actual.addPoint(new Waypoint(30549.43,17098.33,1654.00,365100));
	path23actual.addPoint(new Waypoint(30550.71,17094.65,1654.00,365200));
	path23actual.addPoint(new Waypoint(30551.98,17090.98,1654.00,365300));
	path23actual.addPoint(new Waypoint(30553.26,17087.31,1654.00,365400));
	path23actual.addPoint(new Waypoint(30554.53,17083.63,1654.00,365500));
	path23actual.addPoint(new Waypoint(30555.81,17079.96,1654.00,365600));
	path23actual.addPoint(new Waypoint(30557.08,17076.29,1654.00,365700));
	path23actual.addPoint(new Waypoint(30558.36,17072.62,1654.00,365800));
	path23actual.addPoint(new Waypoint(30559.63,17068.94,1654.00,365900));
	path23actual.addPoint(new Waypoint(30560.91,17065.27,1654.00,366000));
	path23actual.addPoint(new Waypoint(30562.19,17061.60,1654.00,366100));
	path23actual.addPoint(new Waypoint(30563.46,17057.92,1654.00,366200));
	path23actual.addPoint(new Waypoint(30564.74,17054.25,1654.00,366300));
	path23actual.addPoint(new Waypoint(30566.01,17050.58,1654.00,366400));
	path23actual.addPoint(new Waypoint(30567.29,17046.90,1654.00,366500));
	path23actual.addPoint(new Waypoint(30568.56,17043.23,1654.00,366600));
	path23actual.addPoint(new Waypoint(30569.84,17039.56,1654.00,366700));
	path23actual.addPoint(new Waypoint(30571.11,17035.89,1654.00,366800));
	path23actual.addPoint(new Waypoint(30572.39,17032.21,1654.00,366900));
	path23actual.addPoint(new Waypoint(30573.66,17028.54,1654.00,367000));
	path23actual.addPoint(new Waypoint(30574.94,17024.87,1654.00,367100));
	path23actual.addPoint(new Waypoint(30576.21,17021.19,1654.00,367200));
	path23actual.addPoint(new Waypoint(30577.49,17017.52,1654.00,367300));
	path23actual.addPoint(new Waypoint(30578.76,17013.85,1654.00,367400));
	path23actual.addPoint(new Waypoint(30580.04,17010.18,1654.00,367500));
	path23actual.addPoint(new Waypoint(30581.31,17006.50,1654.00,367600));
	path23actual.addPoint(new Waypoint(30582.59,17002.83,1654.00,367700));
	path23actual.addPoint(new Waypoint(30583.86,16999.16,1654.00,367800));
	path23actual.addPoint(new Waypoint(30585.14,16995.48,1654.00,367900));
	path23actual.addPoint(new Waypoint(30586.41,16991.81,1654.00,368000));
	path23actual.addPoint(new Waypoint(30587.69,16988.14,1654.00,368100));
	path23actual.addPoint(new Waypoint(30588.96,16984.47,1654.00,368200));
	path23actual.addPoint(new Waypoint(30590.24,16980.79,1654.00,368300));
	path23actual.addPoint(new Waypoint(30591.51,16977.12,1654.00,368400));
	path23actual.addPoint(new Waypoint(30592.79,16973.45,1654.00,368500));
	path23actual.addPoint(new Waypoint(30594.07,16969.77,1654.00,368600));
	path23actual.addPoint(new Waypoint(30595.34,16966.10,1654.00,368700));
	path23actual.addPoint(new Waypoint(30596.62,16962.43,1654.00,368800));
	path23actual.addPoint(new Waypoint(30597.89,16958.75,1654.00,368900));
	path23actual.addPoint(new Waypoint(30599.17,16955.08,1654.00,369000));
	path23actual.addPoint(new Waypoint(30600.44,16951.41,1654.00,369100));
	path23actual.addPoint(new Waypoint(30601.72,16947.74,1654.00,369200));
	path23actual.addPoint(new Waypoint(30602.99,16944.06,1654.00,369300));
	path23actual.addPoint(new Waypoint(30604.27,16940.39,1654.00,369400));
	path23actual.addPoint(new Waypoint(30605.54,16936.72,1654.00,369500));
	path23actual.addPoint(new Waypoint(30606.82,16933.04,1654.00,369600));
	path23actual.addPoint(new Waypoint(30608.09,16929.37,1654.00,369700));
	path23actual.addPoint(new Waypoint(30609.37,16925.70,1654.00,369800));
	path23actual.addPoint(new Waypoint(30610.64,16922.03,1654.00,369900));
	path23actual.addPoint(new Waypoint(30611.92,16918.35,1654.00,370000));
	path23actual.addPoint(new Waypoint(30613.19,16914.68,1654.00,370100));
	path23actual.addPoint(new Waypoint(30614.47,16911.01,1654.00,370200));
	path23actual.addPoint(new Waypoint(30615.74,16907.33,1654.00,370300));
	path23actual.addPoint(new Waypoint(30617.02,16903.66,1654.00,370400));
	path23actual.addPoint(new Waypoint(30618.29,16899.99,1654.00,370500));
	path23actual.addPoint(new Waypoint(30619.57,16896.32,1654.00,370600));
	path23actual.addPoint(new Waypoint(30620.84,16892.64,1654.00,370700));
	path23actual.addPoint(new Waypoint(30622.12,16888.97,1654.00,370800));
	path23actual.addPoint(new Waypoint(30623.39,16885.30,1654.00,370900));
	path23actual.addPoint(new Waypoint(30624.67,16881.62,1654.00,371000));
	path23actual.addPoint(new Waypoint(30625.95,16877.95,1654.00,371100));
	path23actual.addPoint(new Waypoint(30627.22,16874.28,1654.00,371200));
	path23actual.addPoint(new Waypoint(30628.50,16870.61,1654.00,371300));
	path23actual.addPoint(new Waypoint(30629.77,16866.93,1654.00,371400));
	path23actual.addPoint(new Waypoint(30631.05,16863.26,1654.00,371500));
	path23actual.addPoint(new Waypoint(30632.32,16859.59,1654.00,371600));
	path23actual.addPoint(new Waypoint(30633.60,16855.91,1654.00,371700));
	path23actual.addPoint(new Waypoint(30634.87,16852.24,1654.00,371800));
	path23actual.addPoint(new Waypoint(30636.15,16848.57,1654.00,371900));
	path23actual.addPoint(new Waypoint(30637.42,16844.89,1654.00,372000));
	path23actual.addPoint(new Waypoint(30638.70,16841.22,1654.00,372100));
	path23actual.addPoint(new Waypoint(30639.97,16837.55,1654.00,372200));
	path23actual.addPoint(new Waypoint(30641.25,16833.88,1654.00,372300));
	path23actual.addPoint(new Waypoint(30642.52,16830.20,1654.00,372400));
	path23actual.addPoint(new Waypoint(30643.80,16826.53,1654.00,372500));
	path23actual.addPoint(new Waypoint(30645.07,16822.86,1654.00,372600));
	path23actual.addPoint(new Waypoint(30646.35,16819.18,1654.00,372700));
	path23actual.addPoint(new Waypoint(30647.62,16815.51,1654.00,372800));
	path23actual.addPoint(new Waypoint(30648.90,16811.84,1654.00,372900));
	path23actual.addPoint(new Waypoint(30650.17,16808.17,1654.00,373000));
	path23actual.addPoint(new Waypoint(30651.45,16804.49,1654.00,373100));
	path23actual.addPoint(new Waypoint(30652.72,16800.82,1654.00,373200));
	path23actual.addPoint(new Waypoint(30654.00,16797.15,1654.00,373300));
	path23actual.addPoint(new Waypoint(30655.27,16793.47,1654.00,373400));
	path23actual.addPoint(new Waypoint(30656.55,16789.80,1654.00,373500));
	path23actual.addPoint(new Waypoint(30657.83,16786.13,1654.00,373600));
	path23actual.addPoint(new Waypoint(30659.10,16782.46,1654.00,373700));
	path23actual.addPoint(new Waypoint(30660.38,16778.78,1654.00,373800));
	path23actual.addPoint(new Waypoint(30661.65,16775.11,1654.00,373900));
	path23actual.addPoint(new Waypoint(30662.93,16771.44,1654.00,374000));
	path23actual.addPoint(new Waypoint(30664.20,16767.76,1654.00,374100));
	path23actual.addPoint(new Waypoint(30665.48,16764.09,1654.00,374200));
	path23actual.addPoint(new Waypoint(30666.75,16760.42,1654.00,374300));
	path23actual.addPoint(new Waypoint(30668.03,16756.75,1654.00,374400));
	path23actual.addPoint(new Waypoint(30669.30,16753.07,1654.00,374500));
	path23actual.addPoint(new Waypoint(30670.58,16749.40,1654.00,374600));
	path23actual.addPoint(new Waypoint(30671.85,16745.73,1654.00,374700));
	path23actual.addPoint(new Waypoint(30673.13,16742.05,1654.00,374800));
	path23actual.addPoint(new Waypoint(30674.40,16738.38,1654.00,374900));
	path23actual.addPoint(new Waypoint(30675.68,16734.71,1654.00,375000));
	path23actual.addPoint(new Waypoint(30676.95,16731.03,1654.00,375100));
	path23actual.addPoint(new Waypoint(30678.23,16727.36,1654.00,375200));
	path23actual.addPoint(new Waypoint(30679.50,16723.69,1654.00,375300));
	path23actual.addPoint(new Waypoint(30680.78,16720.02,1654.00,375400));
	path23actual.addPoint(new Waypoint(30682.05,16716.34,1654.00,375500));
	path23actual.addPoint(new Waypoint(30683.33,16712.67,1654.00,375600));
	path23actual.addPoint(new Waypoint(30684.60,16709.00,1654.00,375700));
	path23actual.addPoint(new Waypoint(30685.88,16705.32,1654.00,375800));
	path23actual.addPoint(new Waypoint(30687.15,16701.65,1654.00,375900));
	path23actual.addPoint(new Waypoint(30688.43,16697.98,1654.00,376000));
	path23actual.addPoint(new Waypoint(30689.71,16694.31,1654.00,376100));
	path23actual.addPoint(new Waypoint(30690.98,16690.63,1654.00,376200));
	path23actual.addPoint(new Waypoint(30692.26,16686.96,1654.00,376300));
	path23actual.addPoint(new Waypoint(30693.53,16683.29,1654.00,376400));
	path23actual.addPoint(new Waypoint(30694.81,16679.61,1654.00,376500));
	path23actual.addPoint(new Waypoint(30696.08,16675.94,1654.00,376600));
	path23actual.addPoint(new Waypoint(30697.36,16672.27,1654.00,376700));
	path23actual.addPoint(new Waypoint(30698.63,16668.60,1654.00,376800));
	path23actual.addPoint(new Waypoint(30699.91,16664.92,1654.00,376900));
	path23actual.addPoint(new Waypoint(30701.18,16661.25,1654.00,377000));
	path23actual.addPoint(new Waypoint(30702.46,16657.58,1654.00,377100));
	path23actual.addPoint(new Waypoint(30703.73,16653.90,1654.00,377200));
	path23actual.addPoint(new Waypoint(30705.01,16650.23,1654.00,377300));
	path23actual.addPoint(new Waypoint(30706.28,16646.56,1654.00,377400));
	path23actual.addPoint(new Waypoint(30707.56,16642.89,1654.00,377500));
	path23actual.addPoint(new Waypoint(30708.83,16639.21,1654.00,377600));
	return path23actual;
    }

    public static Path3D getPath48() {
        Path3D path48actual = new Path3D();
	path48actual.addPoint(new Waypoint(30079.51,16674.46,1562.00,351300));
	path48actual.addPoint(new Waypoint(30080.76,16678.30,1562.00,351400));
	path48actual.addPoint(new Waypoint(30082.01,16682.14,1562.00,351500));
	path48actual.addPoint(new Waypoint(30083.26,16685.98,1562.00,351600));
	path48actual.addPoint(new Waypoint(30084.51,16689.82,1562.00,351700));
	path48actual.addPoint(new Waypoint(30085.76,16693.67,1562.00,351800));
	path48actual.addPoint(new Waypoint(30087.02,16697.51,1562.00,351900));
	path48actual.addPoint(new Waypoint(30088.27,16701.35,1562.00,352000));
	path48actual.addPoint(new Waypoint(30089.52,16705.19,1562.00,352100));
	path48actual.addPoint(new Waypoint(30090.78,16709.05,1562.00,352200));
	path48actual.addPoint(new Waypoint(30092.03,16712.90,1562.00,352300));
	path48actual.addPoint(new Waypoint(30093.28,16716.74,1562.00,352400));
	path48actual.addPoint(new Waypoint(30094.53,16720.58,1562.00,352500));
	path48actual.addPoint(new Waypoint(30095.78,16724.42,1562.00,352600));
	path48actual.addPoint(new Waypoint(30097.03,16728.26,1562.00,352700));
	path48actual.addPoint(new Waypoint(30098.28,16732.10,1562.00,352800));
	path48actual.addPoint(new Waypoint(30099.53,16735.94,1562.00,352900));
	path48actual.addPoint(new Waypoint(30100.79,16739.79,1562.00,353000));
	path48actual.addPoint(new Waypoint(30102.04,16743.63,1562.00,353100));
	path48actual.addPoint(new Waypoint(30103.30,16747.49,1562.00,353200));
	path48actual.addPoint(new Waypoint(30104.55,16751.33,1562.00,353300));
	path48actual.addPoint(new Waypoint(30105.80,16755.17,1562.00,353400));
	path48actual.addPoint(new Waypoint(30107.05,16759.02,1562.00,353500));
	path48actual.addPoint(new Waypoint(30108.30,16762.86,1562.00,353600));
	path48actual.addPoint(new Waypoint(30109.55,16766.70,1562.00,353700));
	path48actual.addPoint(new Waypoint(30110.80,16770.54,1562.00,353800));
	path48actual.addPoint(new Waypoint(30112.05,16774.38,1562.00,353900));
	path48actual.addPoint(new Waypoint(30113.31,16778.22,1562.00,354000));
	path48actual.addPoint(new Waypoint(30114.56,16782.07,1562.00,354100));
	path48actual.addPoint(new Waypoint(30115.81,16785.93,1562.00,354200));
	path48actual.addPoint(new Waypoint(30117.07,16789.77,1562.00,354300));
	path48actual.addPoint(new Waypoint(30118.32,16793.61,1562.00,354400));
	path48actual.addPoint(new Waypoint(30119.57,16797.45,1562.00,354500));
	path48actual.addPoint(new Waypoint(30120.82,16801.29,1562.00,354600));
	path48actual.addPoint(new Waypoint(30122.07,16805.14,1562.00,354700));
	path48actual.addPoint(new Waypoint(30123.32,16808.98,1562.00,354800));
	path48actual.addPoint(new Waypoint(30124.57,16812.82,1562.00,354900));
	path48actual.addPoint(new Waypoint(30125.82,16816.66,1562.00,355000));
	path48actual.addPoint(new Waypoint(30127.08,16820.50,1562.00,355100));
	path48actual.addPoint(new Waypoint(30128.33,16824.37,1562.00,355200));
	path48actual.addPoint(new Waypoint(30129.59,16828.21,1562.00,355300));
	path48actual.addPoint(new Waypoint(30130.84,16832.05,1562.00,355400));
	path48actual.addPoint(new Waypoint(30132.09,16835.89,1562.00,355500));
	path48actual.addPoint(new Waypoint(30133.34,16839.73,1562.00,355600));
	path48actual.addPoint(new Waypoint(30134.59,16843.57,1562.00,355700));
	path48actual.addPoint(new Waypoint(30135.84,16847.41,1562.00,355800));
	path48actual.addPoint(new Waypoint(30137.09,16851.26,1562.00,355900));
	path48actual.addPoint(new Waypoint(30138.34,16855.10,1562.00,356000));
	path48actual.addPoint(new Waypoint(30139.60,16858.94,1562.00,356100));
	path48actual.addPoint(new Waypoint(30140.85,16862.80,1562.00,356200));
	path48actual.addPoint(new Waypoint(30142.10,16866.64,1562.00,356300));
	path48actual.addPoint(new Waypoint(30143.36,16870.49,1562.00,356400));
	path48actual.addPoint(new Waypoint(30144.61,16874.33,1562.00,356500));
	path48actual.addPoint(new Waypoint(30145.86,16878.17,1562.00,356600));
	path48actual.addPoint(new Waypoint(30147.11,16882.01,1562.00,356700));
	path48actual.addPoint(new Waypoint(30148.36,16885.85,1562.00,356800));
	path48actual.addPoint(new Waypoint(30149.61,16889.69,1562.00,356900));
	path48actual.addPoint(new Waypoint(30150.86,16893.54,1562.00,357000));
	path48actual.addPoint(new Waypoint(30152.11,16897.38,1562.00,357100));
	path48actual.addPoint(new Waypoint(30153.37,16901.24,1562.00,357200));
	path48actual.addPoint(new Waypoint(30154.62,16905.08,1562.00,357300));
	path48actual.addPoint(new Waypoint(30155.88,16908.92,1562.00,357400));
	path48actual.addPoint(new Waypoint(30157.13,16912.76,1562.00,357500));
	path48actual.addPoint(new Waypoint(30158.38,16916.61,1562.00,357600));
	path48actual.addPoint(new Waypoint(30159.63,16920.45,1562.00,357700));
	path48actual.addPoint(new Waypoint(30160.88,16924.29,1562.00,357800));
	path48actual.addPoint(new Waypoint(30162.13,16928.13,1562.00,357900));
	path48actual.addPoint(new Waypoint(30163.38,16931.97,1562.00,358000));
	path48actual.addPoint(new Waypoint(30164.63,16935.81,1562.00,358100));
	path48actual.addPoint(new Waypoint(30165.89,16939.68,1562.00,358200));
	path48actual.addPoint(new Waypoint(30167.14,16943.52,1562.00,358300));
	path48actual.addPoint(new Waypoint(30168.39,16947.36,1562.00,358400));
	path48actual.addPoint(new Waypoint(30169.65,16951.20,1562.00,358500));
	path48actual.addPoint(new Waypoint(30170.90,16955.04,1562.00,358600));
	path48actual.addPoint(new Waypoint(30172.15,16958.89,1562.00,358700));
	path48actual.addPoint(new Waypoint(30173.40,16962.73,1562.00,358800));
	path48actual.addPoint(new Waypoint(30174.65,16966.57,1562.00,358900));
	path48actual.addPoint(new Waypoint(30175.90,16970.41,1562.00,359000));
	path48actual.addPoint(new Waypoint(30177.15,16974.25,1562.00,359100));
	path48actual.addPoint(new Waypoint(30178.41,16978.11,1562.00,359200));
	path48actual.addPoint(new Waypoint(30179.66,16981.96,1562.00,359300));
	path48actual.addPoint(new Waypoint(30180.91,16985.80,1562.00,359400));
	path48actual.addPoint(new Waypoint(30182.17,16989.64,1562.00,359500));
	path48actual.addPoint(new Waypoint(30183.42,16993.48,1562.00,359600));
	path48actual.addPoint(new Waypoint(30184.67,16997.32,1562.00,359700));
	path48actual.addPoint(new Waypoint(30185.92,17001.16,1562.00,359800));
	path48actual.addPoint(new Waypoint(30187.17,17005.01,1562.00,359900));
	path48actual.addPoint(new Waypoint(30188.42,17008.85,1562.00,360000));
	path48actual.addPoint(new Waypoint(30189.67,17012.69,1562.00,360100));
	path48actual.addPoint(new Waypoint(30190.93,17016.55,1562.00,360200));
	path48actual.addPoint(new Waypoint(30192.18,17020.39,1562.00,360300));
	path48actual.addPoint(new Waypoint(30193.43,17024.24,1562.00,360400));
	path48actual.addPoint(new Waypoint(30194.68,17028.08,1562.00,360500));
	path48actual.addPoint(new Waypoint(30195.94,17031.92,1562.00,360600));
	path48actual.addPoint(new Waypoint(30197.19,17035.76,1562.00,360700));
	path48actual.addPoint(new Waypoint(30198.44,17039.60,1562.00,360800));
	path48actual.addPoint(new Waypoint(30199.69,17043.44,1562.00,360900));
	path48actual.addPoint(new Waypoint(30200.94,17047.28,1562.00,361000));
	path48actual.addPoint(new Waypoint(30202.19,17051.13,1562.00,361100));
	path48actual.addPoint(new Waypoint(30203.45,17054.99,1562.00,361200));
	path48actual.addPoint(new Waypoint(30204.70,17058.83,1562.00,361300));
	path48actual.addPoint(new Waypoint(30205.95,17062.67,1562.00,361400));
	path48actual.addPoint(new Waypoint(30207.20,17066.51,1562.00,361500));
	path48actual.addPoint(new Waypoint(30208.46,17070.36,1562.00,361600));
	path48actual.addPoint(new Waypoint(30209.71,17074.20,1562.00,361700));
	path48actual.addPoint(new Waypoint(30210.96,17078.04,1562.00,361800));
	path48actual.addPoint(new Waypoint(30212.21,17081.88,1562.00,361900));
	path48actual.addPoint(new Waypoint(30213.46,17085.72,1562.00,362000));
	path48actual.addPoint(new Waypoint(30214.71,17089.56,1562.00,362100));
	path48actual.addPoint(new Waypoint(30215.97,17093.43,1562.00,362200));
	path48actual.addPoint(new Waypoint(30217.22,17097.27,1562.00,362300));
	path48actual.addPoint(new Waypoint(30218.47,17101.11,1562.00,362400));
	path48actual.addPoint(new Waypoint(30219.72,17104.95,1562.00,362500));
	path48actual.addPoint(new Waypoint(30220.97,17108.79,1562.00,362600));
	path48actual.addPoint(new Waypoint(30222.23,17112.63,1562.00,362700));
	path48actual.addPoint(new Waypoint(30223.48,17116.48,1562.00,362800));
	path48actual.addPoint(new Waypoint(30224.73,17120.32,1562.00,362900));
	path48actual.addPoint(new Waypoint(30225.98,17124.16,1562.00,363000));
	path48actual.addPoint(new Waypoint(30227.23,17128.00,1562.00,363100));
	path48actual.addPoint(new Waypoint(30228.49,17131.86,1562.00,363200));
	path48actual.addPoint(new Waypoint(30229.74,17135.71,1562.00,363300));
	path48actual.addPoint(new Waypoint(30230.99,17139.55,1562.00,363400));
	path48actual.addPoint(new Waypoint(30232.24,17143.39,1562.00,363500));
	path48actual.addPoint(new Waypoint(30233.49,17147.23,1562.00,363600));
	path48actual.addPoint(new Waypoint(30234.75,17151.07,1562.00,363700));
	path48actual.addPoint(new Waypoint(30236.00,17154.91,1562.00,363800));
	path48actual.addPoint(new Waypoint(30237.25,17158.75,1562.00,363900));
	path48actual.addPoint(new Waypoint(30238.50,17162.60,1562.00,364000));
	path48actual.addPoint(new Waypoint(30239.75,17166.44,1562.00,364100));
	path48actual.addPoint(new Waypoint(30241.01,17170.30,1562.00,364200));
	path48actual.addPoint(new Waypoint(30242.26,17174.14,1562.00,364300));
	path48actual.addPoint(new Waypoint(30243.51,17177.98,1562.00,364400));
	path48actual.addPoint(new Waypoint(30244.76,17181.83,1562.00,364500));
	path48actual.addPoint(new Waypoint(30246.01,17185.67,1562.00,364600));
	path48actual.addPoint(new Waypoint(30247.26,17189.51,1562.00,364700));
	path48actual.addPoint(new Waypoint(30248.52,17193.35,1562.00,364800));
	path48actual.addPoint(new Waypoint(30249.77,17197.19,1562.00,364900));
	path48actual.addPoint(new Waypoint(30251.02,17201.03,1562.00,365000));
	path48actual.addPoint(new Waypoint(30252.27,17204.87,1562.00,365100));
	path48actual.addPoint(new Waypoint(30253.53,17208.74,1562.00,365200));
	path48actual.addPoint(new Waypoint(30254.78,17212.58,1562.00,365300));
	path48actual.addPoint(new Waypoint(30256.03,17216.42,1562.00,365400));
	path48actual.addPoint(new Waypoint(30257.28,17220.26,1562.00,365500));
	path48actual.addPoint(new Waypoint(30258.53,17224.10,1562.00,365600));
	path48actual.addPoint(new Waypoint(30259.78,17227.95,1562.00,365700));
	path48actual.addPoint(new Waypoint(30261.04,17231.79,1562.00,365800));
	path48actual.addPoint(new Waypoint(30262.29,17235.63,1562.00,365900));
	path48actual.addPoint(new Waypoint(30263.54,17239.47,1562.00,366000));
	path48actual.addPoint(new Waypoint(30264.79,17243.31,1562.00,366100));
	path48actual.addPoint(new Waypoint(30266.05,17247.18,1562.00,366200));
	path48actual.addPoint(new Waypoint(30267.30,17251.02,1562.00,366300));
	path48actual.addPoint(new Waypoint(30268.55,17254.86,1562.00,366400));
	path48actual.addPoint(new Waypoint(30269.80,17258.70,1562.00,366500));
	path48actual.addPoint(new Waypoint(30271.05,17262.54,1562.00,366600));
	path48actual.addPoint(new Waypoint(30272.30,17266.38,1562.00,366700));
	path48actual.addPoint(new Waypoint(30273.55,17270.22,1562.00,366800));
	path48actual.addPoint(new Waypoint(30274.81,17274.07,1562.00,366900));
	path48actual.addPoint(new Waypoint(30276.06,17277.91,1562.00,367000));
	path48actual.addPoint(new Waypoint(30277.31,17281.75,1562.00,367100));
	path48actual.addPoint(new Waypoint(30278.57,17285.61,1562.00,367200));
	path48actual.addPoint(new Waypoint(30279.82,17289.45,1562.00,367300));
	path48actual.addPoint(new Waypoint(30281.07,17293.30,1562.00,367400));
	path48actual.addPoint(new Waypoint(30282.32,17297.14,1562.00,367500));
	path48actual.addPoint(new Waypoint(30283.57,17300.98,1562.00,367600));
	path48actual.addPoint(new Waypoint(30284.82,17304.82,1562.00,367700));
	path48actual.addPoint(new Waypoint(30286.07,17308.66,1562.00,367800));
	path48actual.addPoint(new Waypoint(30287.33,17312.50,1562.00,367900));
	path48actual.addPoint(new Waypoint(30288.58,17316.35,1562.00,368000));
	path48actual.addPoint(new Waypoint(30289.83,17320.19,1562.00,368100));
	path48actual.addPoint(new Waypoint(30291.09,17324.05,1562.00,368200));
	path48actual.addPoint(new Waypoint(30292.34,17327.89,1562.00,368300));
	path48actual.addPoint(new Waypoint(30293.59,17331.73,1562.00,368400));
	path48actual.addPoint(new Waypoint(30294.84,17335.57,1562.00,368500));
	path48actual.addPoint(new Waypoint(30296.09,17339.42,1562.00,368600));
	path48actual.addPoint(new Waypoint(30297.34,17343.26,1562.00,368700));
	path48actual.addPoint(new Waypoint(30298.59,17347.10,1562.00,368800));
	path48actual.addPoint(new Waypoint(30299.84,17350.94,1562.00,368900));
	path48actual.addPoint(new Waypoint(30301.10,17354.78,1562.00,369000));
	path48actual.addPoint(new Waypoint(30302.35,17358.62,1562.00,369100));
	path48actual.addPoint(new Waypoint(30303.61,17362.49,1562.00,369200));
	path48actual.addPoint(new Waypoint(30304.86,17366.33,1562.00,369300));
	path48actual.addPoint(new Waypoint(30306.11,17370.17,1562.00,369400));
	path48actual.addPoint(new Waypoint(30307.36,17374.01,1562.00,369500));
	path48actual.addPoint(new Waypoint(30308.61,17377.85,1562.00,369600));
	path48actual.addPoint(new Waypoint(30309.86,17381.69,1562.00,369700));
	path48actual.addPoint(new Waypoint(30311.11,17385.54,1562.00,369800));
	path48actual.addPoint(new Waypoint(30312.36,17389.38,1562.00,369900));
	path48actual.addPoint(new Waypoint(30313.62,17393.22,1562.00,370000));
	path48actual.addPoint(new Waypoint(30314.87,17397.06,1562.00,370100));
	path48actual.addPoint(new Waypoint(30316.13,17400.92,1562.00,370200));
	path48actual.addPoint(new Waypoint(30317.38,17404.77,1562.00,370300));
	path48actual.addPoint(new Waypoint(30318.63,17408.61,1562.00,370400));
	path48actual.addPoint(new Waypoint(30319.88,17412.45,1562.00,370500));
	path48actual.addPoint(new Waypoint(30321.13,17416.29,1562.00,370600));
	path48actual.addPoint(new Waypoint(30322.38,17420.13,1562.00,370700));
	path48actual.addPoint(new Waypoint(30323.63,17423.97,1562.00,370800));
	path48actual.addPoint(new Waypoint(30324.88,17427.82,1562.00,370900));
	path48actual.addPoint(new Waypoint(30326.14,17431.66,1562.00,371000));
	path48actual.addPoint(new Waypoint(30327.39,17435.50,1562.00,371100));
	path48actual.addPoint(new Waypoint(30328.64,17439.36,1562.00,371200));
	path48actual.addPoint(new Waypoint(30329.90,17443.20,1562.00,371300));
	path48actual.addPoint(new Waypoint(30331.15,17447.04,1562.00,371400));
	path48actual.addPoint(new Waypoint(30332.40,17450.89,1562.00,371500));
	path48actual.addPoint(new Waypoint(30333.65,17454.73,1562.00,371600));
	path48actual.addPoint(new Waypoint(30334.90,17458.57,1562.00,371700));
	path48actual.addPoint(new Waypoint(30336.15,17462.41,1562.00,371800));
	path48actual.addPoint(new Waypoint(30337.40,17466.25,1562.00,371900));
	path48actual.addPoint(new Waypoint(30338.65,17470.09,1562.00,372000));
	path48actual.addPoint(new Waypoint(30339.91,17473.94,1562.00,372100));
	path48actual.addPoint(new Waypoint(30341.16,17477.80,1562.00,372200));
	path48actual.addPoint(new Waypoint(30342.42,17481.64,1562.00,372300));
	path48actual.addPoint(new Waypoint(30343.67,17485.48,1562.00,372400));
	path48actual.addPoint(new Waypoint(30344.92,17489.32,1562.00,372500));
	path48actual.addPoint(new Waypoint(30346.17,17493.17,1562.00,372600));
	path48actual.addPoint(new Waypoint(30347.42,17497.01,1562.00,372700));
	path48actual.addPoint(new Waypoint(30348.67,17500.85,1562.00,372800));
	path48actual.addPoint(new Waypoint(30349.92,17504.69,1562.00,372900));
	path48actual.addPoint(new Waypoint(30351.17,17508.53,1562.00,373000));
	path48actual.addPoint(new Waypoint(30352.43,17512.37,1562.00,373100));
	path48actual.addPoint(new Waypoint(30351.64,17516.12,1562.00,373200));
	path48actual.addPoint(new Waypoint(30349.10,17518.95,1562.00,373300));
	path48actual.addPoint(new Waypoint(30346.01,17521.18,1562.00,373400));
	path48actual.addPoint(new Waypoint(30342.92,17523.40,1562.00,373500));
	path48actual.addPoint(new Waypoint(30339.83,17525.63,1562.00,373600));
	path48actual.addPoint(new Waypoint(30336.75,17527.86,1562.00,373700));
	path48actual.addPoint(new Waypoint(30333.66,17530.09,1562.00,373800));
	path48actual.addPoint(new Waypoint(30330.57,17532.31,1562.00,373900));
	path48actual.addPoint(new Waypoint(30327.48,17534.54,1562.00,374000));
	path48actual.addPoint(new Waypoint(30324.39,17536.77,1562.00,374100));
	path48actual.addPoint(new Waypoint(30321.33,17538.97,1562.00,374200));
	path48actual.addPoint(new Waypoint(30318.24,17541.20,1562.00,374300));
	path48actual.addPoint(new Waypoint(30315.15,17543.43,1562.00,374400));
	path48actual.addPoint(new Waypoint(30312.06,17545.66,1562.00,374500));
	path48actual.addPoint(new Waypoint(30308.98,17547.88,1562.00,374600));
	path48actual.addPoint(new Waypoint(30305.89,17550.11,1562.00,374700));
	path48actual.addPoint(new Waypoint(30302.80,17552.34,1562.00,374800));
	path48actual.addPoint(new Waypoint(30299.71,17554.56,1562.00,374900));
	path48actual.addPoint(new Waypoint(30296.62,17556.79,1562.00,375000));
	path48actual.addPoint(new Waypoint(30293.54,17559.02,1562.00,375100));
	path48actual.addPoint(new Waypoint(30290.47,17561.23,1562.00,375200));
	path48actual.addPoint(new Waypoint(30287.39,17563.45,1562.00,375300));
	path48actual.addPoint(new Waypoint(30284.30,17565.68,1562.00,375400));
	path48actual.addPoint(new Waypoint(30281.21,17567.91,1562.00,375500));
	path48actual.addPoint(new Waypoint(30278.12,17570.13,1562.00,375600));
	path48actual.addPoint(new Waypoint(30275.03,17572.36,1562.00,375700));
	path48actual.addPoint(new Waypoint(30271.95,17574.59,1562.00,375800));
	path48actual.addPoint(new Waypoint(30268.86,17576.82,1562.00,375900));
	path48actual.addPoint(new Waypoint(30265.77,17579.04,1562.00,376000));
	path48actual.addPoint(new Waypoint(30262.68,17581.27,1562.00,376100));
	path48actual.addPoint(new Waypoint(30259.62,17583.48,1562.00,376200));
	path48actual.addPoint(new Waypoint(30256.53,17585.71,1562.00,376300));
	path48actual.addPoint(new Waypoint(30253.44,17587.93,1562.00,376400));
	path48actual.addPoint(new Waypoint(30250.35,17590.16,1562.00,376500));
	path48actual.addPoint(new Waypoint(30247.27,17592.39,1562.00,376600));
	path48actual.addPoint(new Waypoint(30244.18,17594.61,1562.00,376700));
	path48actual.addPoint(new Waypoint(30241.09,17596.84,1562.00,376800));
	path48actual.addPoint(new Waypoint(30238.00,17599.07,1562.00,376900));
	path48actual.addPoint(new Waypoint(30234.91,17601.29,1562.00,377000));
	path48actual.addPoint(new Waypoint(30231.83,17603.52,1562.00,377100));
	path48actual.addPoint(new Waypoint(30228.76,17605.73,1562.00,377200));
	path48actual.addPoint(new Waypoint(30225.68,17607.96,1562.00,377300));
	path48actual.addPoint(new Waypoint(30222.59,17610.18,1562.00,377400));
	path48actual.addPoint(new Waypoint(30219.50,17612.41,1562.00,377500));
	path48actual.addPoint(new Waypoint(30216.41,17614.64,1562.00,377600));
	return path48actual;
    }
}
