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
 * CameraView.java
 *
 * Created on June 28, 2004, 8:47 PM
 */

package AirSim.Environment.GUI;

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.*;
import java.awt.GraphicsConfiguration;
import AirSim.Environment.Assets.*;

/*
import com.sun.j3d.utils.applet.MainFrame;
import com.sun.j3d.utils.universe.*;
import com.sun.j3d.utils.behaviors.mouse.*;
import com.sun.j3d.utils.geometry.*;
import javax.media.j3d.*;
import javax.vecmath.*;
*/
import java.util.Vector;
import java.util.Hashtable;
import java.util.Enumeration;

/**
 *
 * @author  paul
 */
public class CameraView extends javax.swing.JFrame {
      /*  
    protected SimpleUniverse simpleU;
    protected View view;
    protected BranchGroup scene;
    protected AirSim.Environment.Env env = new AirSim.Environment.Env();
    protected Hashtable vwasms = new Hashtable();
    
    TransformGroup tgGround = null;
    
    // Amount values are scaled before display
    public static float X_SCALE = 2.5f;
    public static float Y_SCALE = 4.5f;
    public static float Z_SCALE = 1.0f;
    
    // Amount to translate in each direction
    public static float X_TRANSLATE = -32.0f;
    public static float Y_TRANSLATE = 1.0f;
    public static float Z_TRANSLATE = -150.0f;
    
    public CameraView() {
        super("Camera");
        getContentPane().setLayout(new BorderLayout());
        GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();
        
        Canvas3D canvas3D;
        canvas3D = new Canvas3D(config);
        
        // SimpleUniverse is a Convenience Utility class
        simpleU = new SimpleUniverse(canvas3D);
        view = simpleU.getViewer().getView();
        view.setBackClipDistance(500.0);
        view.setBackClipPolicy(View.VIRTUAL_SCREEN);
        view.setWindowResizePolicy(View.VIRTUAL_WORLD);
        
        scene = createSceneGraph();
        scene.setCapability(scene.ALLOW_CHILDREN_EXTEND);
        //scene.compile();
        
        simpleU.addBranchGraph(scene);
        
        // This will move the ViewPlatform back a bit so the
        // objects in the scene can be viewed.
        simpleU.getViewingPlatform().setNominalViewingTransform();
        
        getContentPane().add(canvas3D);
        setSize(480, 240);
        show();
        
        // This is just a test
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.out.println("Window closed");
                System.exit(0);
            }
        });
        
        env.setCameraView(this);
    }
    
    protected BranchGroup createSceneGraph() {
        BranchGroup objRoot = new BranchGroup();
        
        // Set up ground
        Transform3D gt = new Transform3D();
        gt.set(1.0f, new Vector3f(X_TRANSLATE, -12.0f, Z_TRANSLATE));
        tgGround = new TransformGroup(gt);
        tgGround.setCapability(tgGround.ALLOW_CHILDREN_EXTEND);
        objRoot.addChild(tgGround);
        
        // Add Navigation
        TransformGroup vpTrans = null;
        BoundingSphere mouseBounds = null;
        
        vpTrans = simpleU.getViewingPlatform().getViewPlatformTransform();
        mouseBounds = new BoundingSphere(new Point3d(), 1000.0);
        
        MouseRotate myMouseRotate = new MouseRotate(MouseBehavior.INVERT_INPUT);
        myMouseRotate.setTransformGroup(vpTrans);
        myMouseRotate.setSchedulingBounds(mouseBounds);
        myMouseRotate.setFactor(0.01);
        objRoot.addChild(myMouseRotate);
        
        MouseTranslate myMouseTranslate = new MouseTranslate(MouseBehavior.INVERT_INPUT);
        myMouseTranslate.setTransformGroup(vpTrans);
        myMouseTranslate.setSchedulingBounds(mouseBounds);
        myMouseTranslate.setFactor(0.1);
        objRoot.addChild(myMouseTranslate);
        
        MouseZoom myMouseZoom = new MouseZoom(MouseBehavior.INVERT_INPUT);
        myMouseZoom.setTransformGroup(vpTrans);
        myMouseZoom.setSchedulingBounds(mouseBounds);
        myMouseZoom.setFactor(0.1);
        objRoot.addChild(myMouseZoom);
        
        Vector assets = env.getAllAssets();
        for (Enumeration e = assets.elements(); e.hasMoreElements(); ) {
            Object o = e.nextElement();
            if (o instanceof AirSim.Environment.Assets.WASM) {
                VWASM vw = new VWASM((AirSim.Environment.Assets.WASM)o);
                tgGround.addChild(vw);
                vwasms.put(o, vw);
            }
        }
        
        return objRoot;
    }
    
    int step = 0;
    public void step() {
        Vector assets = env.getAllAssets();
        for (Enumeration e = assets.elements(); e.hasMoreElements(); ) {
            Object o = e.nextElement();
            if (o instanceof WASM) {                
                VWASM vw = (VWASM)vwasms.get(o);
                if (vw != null) {
                    WASM w = (WASM)o;
                    vw.addPathPoint((float)w.location.x, (float)w.location.y, (float)w.location.z);
                } else {
                    System.out.println("VWASM for " + o + " is not found");
                }
            }
        }
    }
    */
    public static void main(String argv[]) {
        new CameraView();
    }
}
