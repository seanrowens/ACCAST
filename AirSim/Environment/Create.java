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
package AirSim.Environment;

import java.util.Random;

public class Create {
    
    
    int blueForMaxX = 2000, blueForMinX = 0, redForMinX = 2500, redForMaxX = 5000;
    int blueForMaxY = 5000, blueForMinY = 0, redForMinY = 0, redForMaxY = 5000;
    
    public Create() {
        Random rand = new Random();
        
        // Create BlueFor
        System.out.println("# WASMs");
        for (int i = 0; i < 10; i++) {
            System.out.println("WASM W"+i+" " + rand.nextInt(blueForMaxX - blueForMinX)+blueForMinX + " " + rand.nextInt(blueForMaxY - blueForMinY)+blueForMinY + " 250" + " BLUEFOR");
        }
        System.out.println("# M1A1 Tanks");
        for (int i = 0; i < 10; i++) {
            System.out.println("M1A1TANK M1A1T-"+i+" " + rand.nextInt(blueForMaxX - blueForMinX)+blueForMinX + " " + rand.nextInt(blueForMaxY - blueForMinY)+blueForMinY + " BLUEFOR");
        }
        System.out.println("# M2 Tanks");
        for (int i = 0; i < 10; i++) {
            System.out.println("M2TANK M2T-"+i+" " + rand.nextInt(blueForMaxX - blueForMinX)+blueForMinX + " " + rand.nextInt(blueForMaxY - blueForMinY)+blueForMinY + " BLUEFOR");
        }
        
        System.out.println("# Hummers");
        for (int i = 0; i < 10; i++) {
            System.out.println("HUMMER H-"+i+" " + rand.nextInt(blueForMaxX - blueForMinX)+blueForMinX + " " + rand.nextInt(blueForMaxY - blueForMinY)+blueForMinY + " BLUEFOR");
        }
        
        System.out.println("# Scuds");
        for (int i = 0; i < 10; i++) {
            System.out.println("SCUD S-"+i+" " + rand.nextInt(blueForMaxX - blueForMinX)+blueForMinX + " " + rand.nextInt(blueForMaxY - blueForMinY)+blueForMinY + " BLUEFOR");
        }
        
        System.out.println("# Trucks");
        for (int i = 0; i < 30; i++) {
            System.out.println("TRUCK TR-"+i+" " + rand.nextInt(blueForMaxX - blueForMinX)+blueForMinX + " " + rand.nextInt(blueForMaxY - blueForMinY)+blueForMinY + " BLUEFOR");
        }
        
        String [] acs = {"A10", "C130", "F14", "F15", "F16", "F18"};
        for (int i = 0; i < acs.length; i++) {
            System.out.println("# " + acs[i]);
            for (int j = 0; j < 3; j++) {
                System.out.println(acs[i] + " " + acs[i] + "-" + j + " " + rand.nextInt(blueForMaxX - blueForMinX)+blueForMinX + " " + rand.nextInt(blueForMaxY - blueForMinY)+blueForMinY  + " 250" + " BLUEFOR");
            }
        }
        
        // Create RedFor
        System.out.println("# M2 Tanks");
        for (int i = 0; i < 10; i++) {
            System.out.println("M2TANK M2T-"+i+" " + rand.nextInt(redForMaxX - redForMinX)+redForMinX + " " + rand.nextInt(redForMaxY - redForMinY)+redForMinY + " REDFOR");
        }
    }
    
    public static void main(String argv[]) {
        new Create();
    }
}
