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
 * TiledCostMap.java
 *
 * Created on April 26, 2006, 5:01 PM
 *
 */

package AirSim.Machinetta.CostMaps;

import java.awt.Rectangle;
import java.io.Serializable;
import java.util.ArrayList;

/**
 *
 * @author pscerri
 */
public class TiledCostMap implements CostMap, Serializable {
    
    private ArrayList<Rectangle> best = new ArrayList<Rectangle>(), worst = new ArrayList<Rectangle>();
    private int noBest = 10, noWorst = 10;
    private int dx, dy;
    private double [][] costs = null;
    
    /** Creates a new instance of TiledCostMap */
    public TiledCostMap(int width, int height, int dx, int dy) {
        this.dx = dx;
        this.dy = dy;
        costs = new double[(int)Math.ceil(width/dx)][(int)Math.ceil(height/dy)];
    }
    
    /**
     * Sets the value of the tile containing the point (x,y)
     */
    public void setValue(int x, int y, double val) {
       costs[(int)Math.floor(x/dx)][(int)Math.floor(y/dy)] = val; 
    }
    
    /**
     * Not implemented yet.
     */
    public ArrayList<Rectangle> getBadAreas(long t) {
        return worst;
    }
    
    /**
     * Not implemented yet.
     */
    public ArrayList<Rectangle> getGoodAreas(long t) {
        return best;
    }
        
    public double getCost(double x1, double y1, double z1, long t1, double x2, double y2, double z2, long t2) {
        
        double cost = 0.0;
        
        if (x1 > x2) {
            double temp = x2;
            x2 = x1;
            x1 = temp;
        }
        
        if (y1 > y2) {
            double temp = y2;
            y2 = y1;
            y1 = temp;
        }
        
        double steps = 10;
        double ldx = (x2-x1)/steps;
        double ldy = (y2-y1)/steps;
        
        int px = -1;
        int py = -1;
        
        for (int i = 0; i < steps; i++) {
            x1 += ldx; y1 += ldy;
            int x = (int)Math.floor(x1/dx);
            int y = (int)Math.floor(y1/dy);
            if (!(x == px && y == py)) {
                cost += costs[x][y];
            }
            px = x; py = y;
        }
        
        return cost;
    }
    
    public double getCost(double x1, double y1, long t1, double x2, double y2, long t2) {
        return getCost(x1, y1, 0, t1, x2, y2, 0, t2);
    }
    
    public void timeElapsed(long t) {
        // this is static, do nothing
    }
    
}
