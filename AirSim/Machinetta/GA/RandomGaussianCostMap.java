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
 * RandomGaussianCostMap.java
 *
 * Created on May 19, 2007, 3:43 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package AirSim.Machinetta.GA;

import AirSim.Machinetta.CostMaps.CostMap;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Random;

/**
 *
 * @author pscerri
 */
public class RandomGaussianCostMap implements CostMap {
    
    private static Random rand = new Random();
    
    public ArrayList<Gaussian> gaussians = null;
    private static double width, height;
    
    /** Creates a new instance of RandomGaussianCostMap */
    public RandomGaussianCostMap(int no, double width, double height) {
        this.width = width;
        this.height = height;
        gaussians = new ArrayList<Gaussian>(no);
        for (int i = 0; i < no; i++) {
            gaussians.add(new Gaussian());
        }
    }

    // Incomplete
    public double getCost(double x1, double y1, double z1, long t1, double x2, double y2, double z2, long t2) {
        double cost = 0.0;
        for (Gaussian g: gaussians) {
            cost += (g.getContrib(x1, y1) + g.getContrib(x2, y2))/2.0;
        }
        return cost;
    }
    
    // Incomplete
    public double getCost(double x1, double y1, long t1, double x2, double y2, long t2) {
        double cost = 0.0;
        int proc = 0;
        for (Gaussian g: gaussians) {
            cost += (g.getContrib(x1, y1) + g.getContrib(x2, y2))/2.0;            
        }        
        return cost;
    }
    
    public void timeElapsed(long t) {
    }
    
    public ArrayList<Rectangle> getGoodAreas(long t) {
        return null;
    }
    
    public ArrayList<Rectangle> getBadAreas(long t) {
        return null;
    }
    
    public static void main(String argv[]) {
        /*Gaussian g = new Gaussian();
        System.out.println("getContrib: " + g.getContrib(20.0, 20.0));
         */
        RandomGaussianCostMap cm = new RandomGaussianCostMap(3, 1000, 1000);
        System.out.println("Cost: " + cm.getCost(500.0, 500.0, 0, 500.0, 500.0, 0));
        System.out.println("Cost: " + cm.getCost(100.0, 100.0, 0, 100.0, 100.0, 0));
    }
    
    /**
     * From:
     * http://en.wikipedia.org/wiki/Gaussian_function
     */
    private static class Gaussian {
        double x, y, a, b;
        public Gaussian(double x, double y, double a, double b) {
            this.x = x;
            this.y = y;
            this.a = a;
            this.b = b;                       
        }
        
        public Gaussian() {
            x = width * rand.nextDouble();
            y = height * rand.nextDouble();
            a = 300 * rand.nextDouble();
            b = (width/10) * rand.nextDouble() + (width /20);                        
        }
        
        public double getContrib(double lx, double ly) {
            double cx = ((x-lx)/b)*((x-lx)/b);
            double cy = ((y-ly)/b)*((y-ly)/b);
            
            double r = a * Math.exp(-(cx + cy));
            // System.out.println("cx : " + cx + " cy: " + cy + " a: " + a + " b:" + b + " e-: " + Math.exp(-(cx + cy)) + " -> " + r);
            return r;
        }
    }
}
