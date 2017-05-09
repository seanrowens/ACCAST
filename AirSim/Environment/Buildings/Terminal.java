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
 * Terminal.java
 *
 * Created on December 3, 2007, 10:35 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package AirSim.Environment.Buildings;

import java.awt.Polygon;
import java.awt.Point;

/**
 *
 * @author junyounk
 */
public class Terminal extends Building {
    public Polygon poly;
    
    /** Creates a new instance of Terminal */
    public Terminal(String id, int cx, int cy, int width, int height) {
        super(id, cx, cy, width, height);
        
        poly = new Polygon();
        poly.addPoint(cx-(width/2), cy-(height/2));
        poly.addPoint(cx-(width/2), cy+(height/2));
        poly.addPoint(cx-(width/4), cy+(height/2));
        poly.addPoint(cx-(width/4), cy+(5*height/6));
        poly.addPoint(cx+(width/4), cy+(5*height/6));
        poly.addPoint(cx+(width/4), cy+(height/2));
        poly.addPoint(cx+(width/2), cy+(height/2));
        poly.addPoint(cx+(width/2), cy-(height/2));
    }
    
    public boolean contains(double x, double y) {
        return poly.contains(x, y);
    }
}
