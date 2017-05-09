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
 * VWASM.java
 *
 * Created on July 4, 2004, 2:01 PM
 */

package AirSim.Environment.GUI;

/*
import com.sun.j3d.utils.universe.*;
import com.sun.j3d.utils.geometry.*;
import javax.media.j3d.*;
import javax.vecmath.*;
*/

/**
 *
 * @author  paul
 */
public class VWASM extends VisualObject {

    /*
    int STEPS = 100, STEP_TIME = 500, INIT_POINTS = 4;
    protected boolean destroyed = false;
    private AirSim.Environment.Assets.WASM wasm = null;
    private float X_SCALE = 0.1f, Y_SCALE = 0.1f, Z_SCALE = 0.1f;
    
    protected static float [] knots; {
        knots = new float[STEPS];
        for (int i = 0; i < STEPS; i++) {
            knots[i] = 1.0f/(float)(STEPS-1)*(float)i;
        }
    }
    
    protected static Point3f [] path; {
        path = new Point3f[STEPS];        
    }
    protected int currentPoint = 0, totalPoints = 0;
    BoundingSphere bounds = new BoundingSphere();
    protected PositionPathInterpolator run;
    protected Alpha pathAlpha = null;
    final Color3f normColor = new Color3f(0.5f, 0.0f, 0.5f);
    final Color3f destroyedColor = new Color3f(0.0f, 0.0f, 0.0f);
    Material m = new Material(normColor, normColor, normColor, normColor, 0.7f); {
        m.setCapability(Material.ALLOW_COMPONENT_WRITE);
        m.setCapability(Material.ALLOW_COMPONENT_READ);
        m.setColorTarget(Material.AMBIENT_AND_DIFFUSE);
    }
        
    public VWASM(AirSim.Environment.Assets.WASM wasm) {
        this.wasm = wasm;
        
        // Apperance
        Sphere c = new Sphere(0.6f);
        Appearance a = new Appearance();
        a.setMaterial(m);
        c.setAppearance(a);
        
        tg.addChild(c);
        
        // Path interpolator
        tg.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        pathAlpha = new Alpha(-1, STEPS*STEP_TIME);
        //pathAlpha.pause();
        for (int i = 0; i < STEPS; i++) {
            path[i] = transform((float)wasm.location.x, (float)wasm.location.y, (float)wasm.location.z);
        }
        run = new PositionPathInterpolator(pathAlpha, tg, new Transform3D(), knots, path);
        
        // Set color change interpolator
        Alpha colorAlpha = new Alpha(1, 10);
        ColorInterpolator ci = new ColorInterpolator(colorAlpha, m, normColor, destroyedColor);
        
        
        // Eventually need to change this ...
        bounds.setRadius(500.0);
        run.setSchedulingBounds(bounds);
        ci.setSchedulingBounds(bounds);
        
        tg.addChild(run);
        tg.addChild(ci);
                        
        colorAlpha.resume();
        
        //compile();
        
        // For testing
        // (new TestThread()).start();
    }
    
    // Needs to be fixed to allow user to change speeds
    long startTime = -1;
    public void addPathPoint(float x, float y, float z) {
        // Update the timing        
        if (startTime > 0) {
            // Version that uses average
            // System.out.println("Before: Setting Alpha Duration to " + (diff / totalPoints * STEPS) + " currentPoint: " + currentPoint + " value: " + pathAlpha.value() + " " + pathAlpha.isPaused());
            // long diff = System.currentTimeMillis() - startTime;
            // pathAlpha.setIncreasingAlphaDuration(diff / totalPoints * STEPS);            
            // Version that uses instant
            long diff = System.currentTimeMillis() - startTime;
            pathAlpha.setIncreasingAlphaDuration(diff * STEPS);          
            startTime = System.currentTimeMillis();
        } else
            startTime = System.currentTimeMillis();
        
        // Add the point to the path
        run.setPosition(currentPoint, transform(x, y, z));
        
        currentPoint = (++currentPoint) % STEPS;
        totalPoints++;
        if (totalPoints == INIT_POINTS) {
            pathAlpha.setStartTime(System.currentTimeMillis());
            pathAlpha.resume();
        }
    }
    
    private Point3f transform (float x, float y, float z) {
        return new Point3f((float)x * X_SCALE, (float)z * Z_SCALE, -(float)y * Y_SCALE );
    }
    
    public void destroyed() {
        System.out.println("WASM destroyed");
        
    }
    
    class TestThread extends Thread {
        public void run() {
            int i = 0;
            boolean destroyed = false;
            java.util.Random rand = new java.util.Random();
            while (true) {
                try {
                    sleep(rand.nextInt(200) + 2000);
                } catch (Exception e) {}
                
                i = (i + 1) % path.length;
                
                if (!destroyed && rand.nextInt(200) == 1)
                    destroyed();
                
                addPathPoint(rand.nextInt(50), 0.0f, 0.0f);
            }
        }
    }
     */
}
