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

import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.geom.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java.util.*;
import Gui.IntPanel;
import Gui.IntGrid;
import Gui.DoubleGrid;
import Gui.DoublePanel;

public class EntropyMap {
    public final static int FOOTPRINT_SIZE=50;
    public final static int GRID_SIZE=1000;
    public final static int PARTICLE_COUNT=10000;

    public static Random rand = new Random();

    private JFrame topLevelFrame = null;
    private JPanel topPanel = null;
    private JTabbedPane tabbedPane = null;

    private DoubleGrid entropyGrid = null;

    public static void addTabLater(JTabbedPane tabPane, String name, JPanel panel, String tip) {
	final String fname = name;
	final JPanel fpanel = panel;
	final String ftip = tip;
	final JTabbedPane fpane = tabPane;
	EventQueue.invokeLater(new Runnable() { public void run()
		{ fpane.addTab(fname, null, fpanel, ftip); }});
    }

    private void populateGrid2(int footprintSize, int gridSize, Point[] points) {
	IntGrid iEntropyGrid = new IntGrid(gridSize,gridSize);

	int[][] footprint = new int[footprintSize][footprintSize];
	double centerx = ((double)footprintSize)/2;
	double centery = ((double)footprintSize)/2;
	
	for(int loopx = 0; loopx < footprintSize; loopx++) {
	    for(int loopy = 0; loopy < footprintSize; loopy++) {
		double diffx = centerx - loopx;
		double diffy = centery - loopy;
		footprint[loopx][loopy] = (int)Math.hypot(diffx, diffy);
	    }
	}

	int footprintMax = 0;
	for(int loopx = 0; loopx < footprintSize; loopx++) {
	    for(int loopy = 0; loopy < footprintSize; loopy++) {
		if(footprint[loopx][loopy] > footprintMax)
		    footprintMax = footprint[loopx][loopy];
	    }
	}
	int footprintTotal = 0;
	for(int loopx = 0; loopx < footprintSize; loopx++) {
	    for(int loopy = 0; loopy < footprintSize; loopy++) {
		footprint[loopx][loopy] = footprintMax - footprint[loopx][loopy];
		footprintTotal += footprint[loopx][loopy]; 
	    }
	}


        iEntropyGrid.clear(0);
	
	long timeStart = System.currentTimeMillis();
	for(int loopi = 0; loopi < points.length; loopi++) {
	    iEntropyGrid.addFootPrint(points[loopi].x, points[loopi].y,footprint);
	}

	int gridAvg = (int)((points.length * (double)footprintTotal)/(gridSize * gridSize));


	entropyGrid = new DoubleGrid(iEntropyGrid);
	//	entropyGrid.computeHighestLowest();
	// 	Machinetta.Debugger.debug("entropyGrid lowest="+entropyGrid.getLowest()+", highest="+entropyGrid.getHighest()+", elapsed="+(System.currentTimeMillis() - timeStart),1,this);	
	// 	Machinetta.Debugger.debug("adding -gridAvg (gridAvg="+gridAvg+")", 1, this);
	entropyGrid.add(-gridAvg);
	//	entropyGrid.computeHighestLowest();
	// 	Machinetta.Debugger.debug("entropyGrid lowest="+entropyGrid.getLowest()+", highest="+entropyGrid.getHighest()+", elapsed="+(System.currentTimeMillis() - timeStart),1,this);	
	// 	Machinetta.Debugger.debug("calling abs() on entropyGrid",1,this);
	entropyGrid.abs();
	entropyGrid.computeHighestLowest();
	// 	Machinetta.Debugger.debug("entropyGrid lowest="+entropyGrid.getLowest()+", highest="+entropyGrid.getHighest()+", elapsed="+(System.currentTimeMillis() - timeStart),1,this);	
	// 	Machinetta.Debugger.debug("calling divide(entropyGrid.getHighest()) on entropyGrid",1,this);
	entropyGrid.divide(entropyGrid.getHighest());
	//	entropyGrid.computeHighestLowest();
	// 	Machinetta.Debugger.debug("entropyGrid lowest="+entropyGrid.getLowest()+", highest="+entropyGrid.getHighest()+", elapsed="+(System.currentTimeMillis() - timeStart),1,this);	
	

// 	for(int loopy = 0; loopy < gridSize; loopy++) {
// 	    for(int loopx = 0; loopx < gridSize; loopx++) {
// 		iEntropyGrid.fastSetValue(loopx, loopy, Math.abs(gridAvg - iEntropyGrid.getValue(loopx, loopy)));
// 	    }
// 	}
// 	entropyGrid.divide(entropyGrid.getHighest());
// 	for(int loopy = 0; loopy < gridSize; loopy++) {
// 	    for(int loopx = 0; loopx < gridSize; loopx++) {
// 		entropyGrid.fastSetValue(loopx, loopy, 1 - entropyGrid.getValue(loopx, loopy));
// 	    }
// 	}

	long timeEnd = System.currentTimeMillis();
	long timeElapsed = (timeEnd - timeStart);

	Machinetta.Debugger.debug("GridSize="+gridSize+", footprintSize="+footprintSize+", PARTICLES="+points.length+", elapsed="+timeElapsed,1,this);
    }

//     private void populateGrid(int footprintSize, int gridSize, int particleCount) {
// 	int[][] footprint = new int[footprintSize][footprintSize];
// 	double centerx = ((double)footprintSize)/2;
// 	double centery = ((double)footprintSize)/2;
	
// 	for(int loopx = 0; loopx < footprintSize; loopx++) {
// 	    for(int loopy = 0; loopy < footprintSize; loopy++) {
// 		double diffx = centerx - loopx;
// 		double diffy = centery - loopy;
// 		footprint[loopx][loopy] = (int)Math.hypot(diffx, diffy);
// 	    }
// 	}

// 	int max = 0;
// 	for(int loopx = 0; loopx < footprintSize; loopx++) {
// 	    for(int loopy = 0; loopy < footprintSize; loopy++) {
// 		if(footprint[loopx][loopy] > max)
// 		    max = footprint[loopx][loopy];
// 	    }
// 	}
// 	for(int loopx = 0; loopx < footprintSize; loopx++) {
// 	    for(int loopy = 0; loopy < footprintSize; loopy++) {
// 		footprint[loopx][loopy] = max - footprint[loopx][loopy];
// 	    }
// 	}

//         entropyGrid.clear(0);
	
// 	long timeStart = System.currentTimeMillis();
// 	for(int loopi = 0; loopi < particleCount; loopi++) {
// 	    int x = rand.nextInt(gridSize);
// 	    int y = rand.nextInt(gridSize);
// 	    entropyGrid.addFootPrint(x,y,footprint);
// 	}
// 	long timeEnd = System.currentTimeMillis();
// 	long timeElapsed = (timeEnd - timeStart);
// 	System.err.println("GridSize="+gridSize+", footprintSize="+footprintSize+", PARTICLES="+particleCount+", elapsed="+timeElapsed);
//     }

    /** Creates a new instance of EntropyMap */
    public EntropyMap(int footprintSize, int gridSize, int particleCount, int exitFlag) {
	//	entropyGrid = new IntGrid(gridSize,gridSize);
// 	for(int loopi = 0; loopi < 10; loopi++) 
//	    populateGrid(footprintSize, gridSize, particleCount);

	Point points[] = new Point[particleCount];
 	for(int loopi = 0; loopi < particleCount; loopi++) {
 	    int x = rand.nextInt(gridSize);
 	    int y = rand.nextInt(gridSize);
	    points[loopi] = new Point(x,y);
	}
	populateGrid2(footprintSize, gridSize, points);



	if(exitFlag != 0)
	    System.exit(0);

	topLevelFrame = new JFrame();
	topLevelFrame.addWindowListener(new WindowAdapter() {
		public void windowClosing(WindowEvent e) {System.exit(0);}
	    });
	// For some reason I don't understand, if I just use 0,0 here
	// (the top left of the screen), xwin-32 puts the window
	// slightly off the screen to the top and the left.
 	topLevelFrame.setLocation(5,30);
	topLevelFrame.setSize(Toolkit.getDefaultToolkit().getScreenSize());

	topPanel = new JPanel();
	topPanel.setLayout( new BorderLayout() );
	topLevelFrame.getContentPane().add( topPanel );

	tabbedPane = new JTabbedPane();
	topPanel.add(tabbedPane, BorderLayout.CENTER);

	DoubleGrid dEntropyGrid = new DoubleGrid(entropyGrid);
	DoublePanel dPanel = new DoublePanel(dEntropyGrid, false, true);
	addTabLater(tabbedPane, "dEntropy", dPanel, "");

// 	IntPanel iPanel = new IntPanel(entropyGrid);
// 	addTabLater(tabbedPane, "Entropy", iPanel, "");

	// Note, DON'T use .pack() - or we undo all the
	// setSize/setLocation crap above.
	topLevelFrame.setVisible(true);

//         // Display the costmap with path overlay
//         PathDisplayPanel panel = new PathDisplayPanel(cms, path);
//         JFrame display = new JFrame("Plan");
// 	display.setLocation(600,30);
//         display.getContentPane().setLayout(new BorderLayout());
//         display.getContentPane().add(panel, BorderLayout.CENTER);
//         display.pack();
//         display.setSize((size/step), (size/step));
//         display.addWindowListener(new WindowAdapter() {
//             public void windowClosing(WindowEvent e) {
//                 System.exit(0);
//             }
//         });
//         display.setVisible(true);
    }
    

    public static void main(String argv[]) {
	int footprintSize = 0;
	int gridSize = 0;
	int particleCount = 0;
	int exitFlag = 0;
	try {
	    footprintSize = Integer.parseInt(argv[0]);
	    gridSize = Integer.parseInt(argv[1]);
	    particleCount = Integer.parseInt(argv[2]);
	    exitFlag = Integer.parseInt(argv[3]);
	}
	catch(Exception e) {
	    System.err.println("Exception e="+e);
	    e.printStackTrace();
	}


	EntropyMap entropyMap = new EntropyMap(footprintSize, gridSize, particleCount, exitFlag);
        
    }
    
    

}
