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
 * ConfusionMatrixReading.java
 *
 * Created on September 12, 2005, 5:27 PM
 *
 */

package AirSim;

import AirSim.Environment.Assets.Asset;
import Machinetta.State.BeliefType.ProxyID;
import Machinetta.Debugger;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Random;
import java.text.DecimalFormat;

/**
 *
 * @author pscerri
 */
public class ConfusionMatrixReading extends SensorReading {
    public final static DecimalFormat fmt = new DecimalFormat("0.000");    
    static private Random rand = new Random();
    
    public Hashtable<Asset.Types, Double> probs = new Hashtable<Asset.Types, Double>();
    
    /** Creates a new instance of ConfusionMatrixReading */
    public ConfusionMatrixReading(int x, int y, ProxyID sensor) {
        this.x = x;
        this.y = y;
        this.sensor = sensor;
    }
    
    /** I made this private, doesn't seem to hurt anything. Paul 23/11/05 */
    // Thu Feb 19 18:12:40 EST 2009 - SRO - made it public again, need
    // it to add probs for things that aren't in our tables below, so
    // MarkOneEyeball can see Infantry as somethign other than clutter.
    public void addProb(Asset.Types type, double p) {
        probs.put(type, p);
    }
    
    public double getProb(Asset.Types type) {
        if (probs.containsKey(type)) return probs.get(type);
        else return 0.0;
    }
    
    public Asset.Types getMostLikely() {
//         Asset.Types type = null;
//         double p = -1.0;
//         for (Enumeration<Asset.Types> e = probs.keys(); e.hasMoreElements(); ) {
//             Asset.Types key = e.nextElement();
//             if (probs.get(key) > p) {
//                 p = probs.get(key);
//                 type = key;
//             }
//         }
//         return type;
	calculateMostLikely();
	return mostLikely;
    }
    
    public double getHighestProb() {
        return probs.get(getMostLikely());
    }
    
    public Asset.Types getMostLikelyExClutter() {
//         Asset.Types type = null;
//         double p = -1.0;
//         for (Enumeration<Asset.Types> e = probs.keys(); e.hasMoreElements(); ) {
//             Asset.Types key = e.nextElement();
//             if (key != Asset.Types.CLUTTER && probs.get(key) > p) {
//                 p = probs.get(key);
//                 type = key;
//             }
//         }
//         return type;
	calculateMostLikely();
	return mostLikelyExClutter;
    }
    
    public double getHighestProbExClutter() {
        return probs.get(getMostLikelyExClutter());
    }
    
    /**
     * This sets probabilities for different types, given that what we really
     * see is of type type.
     *
     * @param visibility 0.0 corresponds to no visibility, 0.5 to "normal" visibility and 1.0 to "perfect" visibility.  For example,
     * if asset is under a tree at night using vision sensor you might use 0.0, UAV flying at normal height at normal speed during the day
     * might use 0.5 and GroundVehicle 50m from something in the middle of a clear sunny day might use 1.0
     */
    public void setProbs(Asset.Types type, double visibility) {
        
        double [][] confusion = is;
        
        // Unfortunately, we play with the whole table, because we don't know which column might be required.
        for (int row = 0; row < confusion.length; row++) {
            double tot = 0;
            confusion = is.clone();
            if (visibility < 0.5) {
                for (int column = 0; column < confusion.length; column++) {
                    confusion[row][column] = confusion[row][column] + (1.0 - visibility*2.0) * (confusion[row][row] - confusion[row][column]);
                    tot += confusion[row][column];
                }
            } else if (visibility > 0.5) {
                for (int column = 0; column < confusion.length; column++) {
                    if (row != column)
                        confusion[row][column] = (1.0 - visibility) * confusion[row][column];
                    tot += confusion[row][column];
                }
            } else {
                tot = 1.0;
            }
            
            for (int i = 0; i < confusion.length; i++) {
                confusion[row][i] /= tot;
            }
        }
        
        // We don't always want to return the confidences for the
        // correct type, but only based on probability that we
        // work out correct type (does that help explain? ha ha.)
        double cProb = rand.nextDouble(), c = 0.0;
        int r = typeToIndex(type), col = -1;
        do {
            col++;
            c += confusion[r][col];
        } while (c < cProb);
        
        setFor(indexToType(col), confusion);
        
	Debugger.debug(0,"Set confidences for " + type + " based on confusion of " + indexToType(col));
    }
    
    /**
     * This sets probabilities for different types, given that what we really
     * see is of type type.
     *
     * @see public void setProbs(Asset.Types type, double visibility)
     */
    public void setProbs(Asset.Types type) {
        setProbs(type, 0.5);
    }
    
    /**
     * Actually sets the confidences, given a type
     */
    private void setFor(Asset.Types type, double [][] confusion ) {
        
        int row = typeToIndex(type);
        
        for (int i = 0; i < confusion[row].length; i++) {
            addProb(indexToType(i), confusion[row][i]);
        }
        
    }
    
    public static final long serialVersionUID = 1L;
    
    private int typeToIndex(Asset.Types type) {
        switch(type) {
            case T80:
                return 0;
            case T72M:
                return 1;
            case M2:
                return 2;
            case M1A1:
                return 3;
            case M1A2:
                return 4;
            case TWOS6:
                return 5;
            case ZSU23_4M:
                return 6;
            case M977:
                return 7;
            case M35:
                return 8;
            case AVENGER:
                return 9;
            case HMMMV:
                return 10;
            case SA9:
                return 11;
            case CLUTTER:
                return 12;
        }
	Debugger.debug(0,"SARSim trying to confuse unknown type: " + type + " mapping to clutter");
        return 12;
    }
    
    private Asset.Types indexToType(int index) {
        switch(index) {
            case 0:
                return Asset.Types.T80;
            case 1:
                return Asset.Types.T72M;
            case 2:
                return Asset.Types.M2;
            case 3:
                return Asset.Types.M1A1;
            case 4:
                return Asset.Types.M1A2;
            case 5:
                return Asset.Types.TWOS6;
            case 6:
                return Asset.Types.ZSU23_4M;
            case 7:
                return Asset.Types.M977;
            case 8:
                return Asset.Types.M35;
            case 9:
                return Asset.Types.AVENGER;
            case 10:
                return Asset.Types.HMMMV;
            case 11:
                return Asset.Types.SA9;
            case 12:
                return Asset.Types.CLUTTER;
        }
        Debugger.debug(3,"Trying to get type from out of range index, mapping to clutter, index: " + index);
        return Asset.Types.CLUTTER;
    }
    
    
    public static void main(String argv[]) {
        double is [] = { 0.6, 0.3, 0.1 };
        System.out.println("Before " + Util.PrintHelpers.arrayToString(is, 6));
        for (double v = 0.0; v <= 1.0; v += 0.1) {
            double[] c = is.clone();
            double tot = c[0];
            if (v < 0.5) {
                for (int i = 1; i < c.length; i++) {
                    c[i] = c[i] + (1.0 - v*2.0) * (c[0] - c[i]);
                    tot += c[i];
                }
            } else if (v > 0.5) {
                for (int i = 1; i < c.length; i++) {
                    c[i] = (1.0 - v) * c[i];
                    tot += c[i];
                }
            } else {
                tot = 1.0;
            }
            
            for (int i = 0; i < c.length; i++) {
                c[i] /= tot;
            }
            System.out.println("v = " + Util.PrintHelpers.doubleToString(v, 3) + " : " + Util.PrintHelpers.arrayToString(c, 6));
        }
    }
    
    private double is[][] = new double[13][13];
    {
        // We will fill it row by row which is Truth By Truth
        
        // USSR_T80_____
        is[0][0]  = 0.400;	// USSR_T80_____
        is[0][1]  = 0.300;	// USSR_T72M____
        is[0][2]  = 0.050;	// US___M1______
        is[0][3]  = 0.050;	// US___M1A1____
        is[0][4]  = 0.050;	// US___M1A2____
        is[0][5]  = 0.020;	// USSR_2S6_____
        is[0][6]  = 0.030;	// USSR_ZSU23_4M
        is[0][7]  = 0.001;	// US___M977____
        is[0][8]  = 0.001;	// US___M35_____
        is[0][9]  = 0.001;	// US___AVENGER_
        is[0][10] = 0.001;	// US___HMMWV___
        is[0][11] = 0.001;	// USSR_SA_9____
        is[0][12] = 0.095;	// CLUTTER______
        
        // USSR_T72M____
        is[1][0]  = 0.300;	// USSR_T80_____
        is[1][1]  = 0.400;	// USSR_T72M____
        is[1][2]  = 0.050;	// US___M1______
        is[1][3]  = 0.050;	// US___M1A1____
        is[1][4]  = 0.050;	// US___M1A2____
        is[1][5]  = 0.020;	// USSR_2S6_____
        is[1][6]  = 0.030;	// USSR_ZSU23_4M
        is[1][7]  = 0.001;	// US___M977____
        is[1][8]  = 0.001;	// US___M35_____
        is[1][9]  = 0.001;	// US___AVENGER_
        is[1][10] = 0.001;	// US___HMMWV___
        is[1][11] = 0.001;	// USSR_SA_9____
        is[1][12] = 0.095;	// CLUTTER______
        
        // US___M1______
        is[2][0]  = 0.050;	// USSR_T80_____
        is[2][1]  = 0.050;	// USSR_T72M____
        is[2][2]  = 0.280;	// US___M1______
        is[2][3]  = 0.240;	// US___M1A1____
        is[2][4]  = 0.230;	// US___M1A2____
        is[2][5]  = 0.020;	// USSR_2S6_____
        is[2][6]  = 0.030;	// USSR_ZSU23_4M
        is[2][7]  = 0.001;	// US___M977____
        is[2][8]  = 0.001;	// US___M35_____
        is[2][9]  = 0.001;	// US___AVENGER_
        is[2][10] = 0.001;	// US___HMMWV___
        is[2][11] = 0.001;	// USSR_SA_9____
        is[2][12] = 0.095;	// CLUTTER______
        
        // US___M1A1____
        is[3][0]  = 0.050;	// USSR_T80_____
        is[3][1]  = 0.050;	// USSR_T72M____
        is[3][2]  = 0.200;	// US___M1______
        is[3][3]  = 0.280;	// US___M1A1____
        is[3][4]  = 0.270;	// US___M1A2____
        is[3][5]  = 0.020;	// USSR_2S6_____
        is[3][6]  = 0.030;	// USSR_ZSU23_4M
        is[3][7]  = 0.001;	// US___M977____
        is[3][8]  = 0.001;	// US___M35_____
        is[3][9]  = 0.001;	// US___AVENGER_
        is[3][10] = 0.001;	// US___HMMWV___
        is[3][11] = 0.001;	// USSR_SA_9____
        is[3][12] = 0.095;	// CLUTTER______
        
        // US___M1A2____
        is[4][0]  = 0.050;	// USSR_T80_____
        is[4][1]  = 0.050;	// USSR_T72M____
        is[4][2]  = 0.200;	// US___M1______
        is[4][3]  = 0.270;	// US___M1A1____
        is[4][4]  = 0.280;	// US___M1A2____
        is[4][5]  = 0.020;	// USSR_2S6_____
        is[4][6]  = 0.030;	// USSR_ZSU23_4M
        is[4][7]  = 0.001;	// US___M977____
        is[4][8]  = 0.001;	// US___M35_____
        is[4][9]  = 0.001;	// US___AVENGER_
        is[4][10] = 0.001;	// US___HMMWV___
        is[4][11] = 0.001;	// USSR_SA_9____
        is[4][12] = 0.095;	// CLUTTER______
        
        // USSR_2S6_____
        is[5][0]  = 0.001;	// USSR_T80_____
        is[5][1]  = 0.001;	// USSR_T72M____
        is[5][2]  = 0.001;	// US___M1______
        is[5][3]  = 0.001;	// US___M1A1____
        is[5][4]  = 0.001;	// US___M1A2____
        is[5][5]  = 0.400;	// USSR_2S6_____
        is[5][6]  = 0.200;	// USSR_ZSU23_4M
        is[5][7]  = 0.001;	// US___M977____
        is[5][8]  = 0.001;	// US___M35_____
        is[5][9]  = 0.050;	// US___AVENGER_
        is[5][10] = 0.001;	// US___HMMWV___
        is[5][11] = 0.001;	// USSR_SA_9____
        is[5][12] = 0.341;	// CLUTTER______
        
        // USSR_ZSU23_4M
        is[6][0]  = 0.001;	// USSR_T80_____
        is[6][1]  = 0.001;	// USSR_T72M____
        is[6][2]  = 0.001;	// US___M1______
        is[6][3]  = 0.001;	// US___M1A1____
        is[6][4]  = 0.001;	// US___M1A2____
        is[6][5]  = 0.200;	// USSR_2S6_____
        is[6][6]  = 0.400;	// USSR_ZSU23_4M
        is[6][7]  = 0.001;	// US___M977____
        is[6][8]  = 0.001;	// US___M35_____
        is[6][9]  = 0.050;	// US___AVENGER_
        is[6][10] = 0.001;	// US___HMMWV___
        is[6][11] = 0.001;	// USSR_SA_9____
        is[6][12] = 0.341;	// CLUTTER______
        
        // US___M977____
        is[7][0]  = 0.001;	// USSR_T80_____
        is[7][1]  = 0.001;	// USSR_T72M____
        is[7][2]  = 0.001;	// US___M1______
        is[7][3]  = 0.001;	// US___M1A1____
        is[7][4]  = 0.001;	// US___M1A2____
        is[7][5]  = 0.001;	// USSR_2S6_____
        is[7][6]  = 0.001;	// USSR_ZSU23_4M
        is[7][7]  = 0.300;	// US___M977____
        is[7][8]  = 0.150;	// US___M35_____
        is[7][9]  = 0.050;	// US___AVENGER_
        is[7][10] = 0.001;	// US___HMMWV___
        is[7][11] = 0.001;	// USSR_SA_9____
        is[7][12] = 0.491;	// CLUTTER______
        
        // US___M35_____
        is[8][0]  = 0.001;	// USSR_T80_____
        is[8][1]  = 0.001;	// USSR_T72M____
        is[8][2]  = 0.001;	// US___M1______
        is[8][3]  = 0.001;	// US___M1A1____
        is[8][4]  = 0.001;	// US___M1A2____
        is[8][5]  = 0.001;	// USSR_2S6_____
        is[8][6]  = 0.001;	// USSR_ZSU23_4M
        is[8][7]  = 0.100;	// US___M977____
        is[8][8]  = 0.350;	// US___M35_____
        is[8][9]  = 0.100;	// US___AVENGER_
        is[8][10] = 0.050;	// US___HMMWV___
        is[8][11] = 0.050;	// USSR_SA_9____
        is[8][12] = 0.343;	// CLUTTER______
        
        // US___AVENGER_
        is[9][0]  = 0.001;	// USSR_T80_____
        is[9][1]  = 0.001;	// USSR_T72M____
        is[9][2]  = 0.001;	// US___M1______
        is[9][3]  = 0.001;	// US___M1A1____
        is[9][4]  = 0.001;	// US___M1A2____
        is[9][5]  = 0.001;	// USSR_2S6_____
        is[9][6]  = 0.001;	// USSR_ZSU23_4M
        is[9][7]  = 0.050;	// US___M977____
        is[9][8]  = 0.100;	// US___M35_____
        is[9][9]  = 0.350;	// US___AVENGER_
        is[9][10] = 0.150;	// US___HMMWV___
        is[9][11] = 0.050;	// USSR_SA_9____
        is[9][12] = 0.293;	// CLUTTER______
        
        // US___HMMWV___
        is[10][0]  = 0.001;	// USSR_T80_____
        is[10][1]  = 0.001;	// USSR_T72M____
        is[10][2]  = 0.001;	// US___M1______
        is[10][3]  = 0.001;	// US___M1A1____
        is[10][4]  = 0.001;	// US___M1A2____
        is[10][5]  = 0.001;	// USSR_2S6_____
        is[10][6]  = 0.001;	// USSR_ZSU23_4M
        is[10][7]  = 0.001;	// US___M977____
        is[10][8]  = 0.050;	// US___M35_____
        is[10][9]  = 0.150;	// US___AVENGER_
        is[10][10] = 0.200;	// US___HMMWV___
        is[10][11] = 0.100;	// USSR_SA_9____
        is[10][12] = 0.492;	// CLUTTER______
        
        // USSR_SA_9____
        is[11][0]  = 0.001;	// USSR_T80_____
        is[11][1]  = 0.001;	// USSR_T72M____
        is[11][2]  = 0.001;	// US___M1______
        is[11][3]  = 0.001;	// US___M1A1____
        is[11][4]  = 0.001;	// US___M1A2____
        is[11][5]  = 0.001;	// USSR_2S6_____
        is[11][6]  = 0.001;	// USSR_ZSU23_4M
        is[11][7]  = 0.001;	// US___M977____
        is[11][8]  = 0.050;	// US___M35_____
        is[11][9]  = 0.050;	// US___AVENGER_
        is[11][10] = 0.100;	// US___HMMWV___
        is[11][11] = 0.150;	// USSR_SA_9____
        is[11][12] = 0.642;	// CLUTTER______
        
        // REJECTED_____
        is[12][0]  = 0.001;	// USSR_T80_____
        is[12][1]  = 0.001;	// USSR_T72M____
        is[12][2]  = 0.001;	// US___M1______
        is[12][3]  = 0.001;	// US___M1A1____
        is[12][4]  = 0.001;	// US___M1A2____
        is[12][5]  = 0.001;	// USSR_2S6_____
        is[12][6]  = 0.001;	// USSR_ZSU23_4M
        is[12][7]  = 0.005;	// US___M977____
        is[12][8]  = 0.010;	// US___M35_____
        is[12][9]  = 0.005;	// US___AVENGER_
        is[12][10] = 0.010;	// US___HMMWV___
        is[12][11] = 0.010;	// USSR_SA_9____
        is[12][12] = 0.953;	// CLUTTER______
        
    }
    
    public String probsToString() {
        Double prob;
        StringBuffer buf = new StringBuffer();
        
        prob = probs.get(Asset.Types.T80);
        if(prob != null)
            buf.append(fmt.format(prob)).append("\t");
        else
            buf.append(0.0).append("\t");
        prob = probs.get(Asset.Types.T72M);
        if(prob != null)
            buf.append(fmt.format(prob)).append("\t");
        else
            buf.append(0.0).append("\t");
        prob = probs.get(Asset.Types.M2);
        if(prob != null)
            buf.append(fmt.format(prob)).append("\t");
        else
            buf.append(0.0).append("\t");
        prob = probs.get(Asset.Types.M1A1);
        if(prob != null)
            buf.append(fmt.format(prob)).append("\t");
        else
            buf.append(0.0).append("\t");
        prob = probs.get(Asset.Types.M1A2);
        if(prob != null)
            buf.append(fmt.format(prob)).append("\t");
        else
            buf.append(0.0).append("\t");
        prob = probs.get(Asset.Types.TWOS6);
        if(prob != null)
            buf.append(fmt.format(prob)).append("\t");
        else
            buf.append(0.0).append("\t");
        prob = probs.get(Asset.Types.ZSU23_4M);
        if(prob != null)
            buf.append(fmt.format(prob)).append("\t");
        else
            buf.append(0.0).append("\t");
        prob = probs.get(Asset.Types.M977);
        if(prob != null)
            buf.append(fmt.format(prob)).append("\t");
        else
            buf.append(0.0).append("\t");
        prob = probs.get(Asset.Types.M35);
        if(prob != null)
            buf.append(fmt.format(prob)).append("\t");
        else
            buf.append(0.0).append("\t");
        prob = probs.get(Asset.Types.AVENGER);
        if(prob != null)
            buf.append(fmt.format(prob)).append("\t");
        else
            buf.append(0.0).append("\t");
        prob = probs.get(Asset.Types.HMMMV);
        if(prob != null)
            buf.append(fmt.format(prob)).append("\t");
        else
            buf.append(0.0).append("\t");
        prob = probs.get(Asset.Types.SA9);
        if(prob != null)
            buf.append(fmt.format(prob)).append("\t");
        else
            buf.append(0.0).append("\t");
        prob = probs.get(Asset.Types.CLUTTER);
        if(prob != null)
            buf.append(fmt.format(prob));
        else
            buf.append(0.0);
        
        return buf.toString();
    }

    protected Asset.Types mostLikely = null;
    protected double mostLikelyProb = -1;
    protected Asset.Types mostLikelyExClutter = null;
    protected double mostLikelyExClutterProb = -1;

    protected void calculateMostLikely() {
	mostLikelyProb = -1;
	mostLikelyExClutterProb = -1;
	//	int keySetSize = probs.keySet().size();
	//	Asset.Types[] keys = probs.keySet().toArray(new Asset.Types[keySetSize]);
	//	for(int loopi = 0; loopi < keys.length; loopi++) {
	for(Asset.Types key: Asset.Types.values()) {
	    Double prob = probs.get(key);
	    if(null == prob)
		continue;
	    if(prob > mostLikelyProb) {
		mostLikelyProb = prob;
		mostLikely = key;
	    }
	    if(key != Asset.Types.CLUTTER) {
		if(prob > mostLikelyExClutterProb) {
		    mostLikelyExClutterProb = prob;
		    mostLikelyExClutter = key;
		}
	    }
	}
    }
    public String toString() { 
	calculateMostLikely();

	return "ConfusionMatrixReading: mostlikely="+mostLikely+" ("+fmt.format(mostLikelyProb)+")"
	    +" mostlikelyExClutter="+mostLikelyExClutter+" ("+fmt.format(mostLikelyExClutterProb)+")"
	    + " id: " + id 
	    + " loc: ("+x+","+y+","+z+")"
	    + " state: " + state 
	    + " forceId: " +forceId
	    +" sensor: " +sensor
	    +" heading " +heading
	    +" type: "+ probsToString()
	    ; 
    }

}
