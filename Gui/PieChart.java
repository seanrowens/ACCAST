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
 * PieChart.java
 *
 * Created on September 17, 2005, 8:25 PM
 *
 */

package Gui;

// @author      Sean R. Owens
// @version     $Id: PieChart.java,v 1.2 2006/07/10 21:54:22 owens Exp $ 

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Hashtable;
import java.util.*;
import javax.swing.JComponent;
import javax.swing.JFrame;

/**
 *
 * @author pscerri
 */
public class PieChart extends JComponent {
    
    Hashtable<String, Double> values = new Hashtable<String, Double>();
    static public int width = 150;
    int margin = 50;

    static Map<String, Color> pieColors = new HashMap<String,Color>();
    static {
	pieColors.put("T80",Color.blue);
	pieColors.put("T72M",Color.cyan);
	pieColors.put("M2",Color.darkGray);
	pieColors.put("M1A1",Color.gray);
	pieColors.put("M1A2",Color.green);
	pieColors.put("TWOS6",Color.lightGray);
	pieColors.put("ZSU23_4M",Color.magenta);
	pieColors.put("M977",Color.orange);
	pieColors.put("M35",Color.pink);
	pieColors.put("AVENGER",Color.red);
	pieColors.put("HMMMV",Color.white);
	pieColors.put("SA9",Color.yellow);
	pieColors.put("CLUTTER",Color.black);
    }


    /** Creates a new instance of PieChart */
    public PieChart() {
    }

    public void clear() {
	values.clear();
    }
    
    public void addValue(String s, double d) {
        values.put(s, d);
    }
    

    final Color [] colors = { Color.BLUE, Color.CYAN, Color.GREEN, Color.MAGENTA, Color.ORANGE, Color.PINK, Color.RED };
    public void paint(Graphics g) {
        double tot = 0.0;
        for (Double d: values.values()) {
            tot += d;
        }
        double da = 360/tot;
        int sa = 0;
        int count = 0;
        int h = (width - 2*margin)/2 + 10;
        int cx = width/2;
	try {
	    synchronized(values) {
		for (String s: values.keySet()) {
		    int ang = (int) (da * values.get(s));
		    Color sliceColor = pieColors.get(s);
		    if(null == sliceColor)
			sliceColor = Color.black;
		    if (ang > 10) {
			g.setColor(sliceColor);
			g.fillArc(margin, margin, width - 2*margin, width - 2*margin, sa, ang);
			sa += ang;
			double ar = Math.PI*2.0 - (((double)(sa - (ang/2)))/360.0*Math.PI*2.0);
			int x = (int)(h * Math.cos(ar));
			int y = (int)(h * Math.sin(ar));
			if (x < 0) x -= s.length()*8;
			System.out.println("Drawing at " + x + " " + y + "  " + cx);
			g.drawString(s, cx+x, cx+y);
		    }
		}
	    }
	}
	catch(Exception e) {
	    System.err.println("ERROR:Piechart.paint: exception while painting, e="+e);
	    e.printStackTrace();
	}
        g.setColor(Color.DARK_GRAY);
        g.fillArc(margin, margin, width - 2*margin, width - 2*margin, sa, 360-sa);
    }
    
    public static void main(String argv[]) {
        JFrame f = new JFrame("Test");
        PieChart pc = new PieChart();
        pc.width = 300;
        pc.addValue("Test", 3.2);
        pc.addValue("Blah", 10.6);
        pc.addValue("skdf", 3.1);
        pc.addValue("fdgfd", 0.1);
        pc.addValue("fdsdffd", 0.1);
        pc.addValue("Fiona", 90.0);
        f.getContentPane().add(pc);
        f.pack();
        f.setSize(400, 400);
        f.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        
        
        f.setVisible(true);
    }
}
