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
 * BeliefLogger.java
 *
 * Created on June 25, 2004, 3:08 PM
 */

package Machinetta.State;

import Machinetta.State.BeliefType.Belief;

import java.util.*;
import java.io.*;
import java.nio.channels.*;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.*;
import javax.swing.border.*;

/**
 *
 * @author  pscerri
 */
public class BeliefLogger implements StateChangeListener {
    
    // For recording
    ObjectOutputStream out = null;
    ProxyState state = null;
    boolean failed = false;
    
    // For playing
    ObjectInputStream in = null;
    
    /** Creates a new instance of BeliefLogger. <br>
     *
     * Use this constructor to record the logfile. <br>
     */
    public BeliefLogger() {
        if (Machinetta.Configuration.BELIEF_LOG_FILE != null) {
            
            if (Machinetta.Configuration.BELIEF_LOG_FILE.equalsIgnoreCase("LIVE")) {
                state = new ProxyState();
                state.addChangeListener(this);
                
                try {
                    PipedOutputStream po = new PipedOutputStream();
                    PipedInputStream pi = new PipedInputStream(po);
                    
                    out = new ObjectOutputStream(po);
                    ObjectInputStream in = new ObjectInputStream(pi);
                    new BeliefLogger(in);
                } catch (Exception e) {
                    Machinetta.Debugger.debug( 1,"Failed to open piped stream: " + e);
                }
            } else {
                try {
                    out = new ObjectOutputStream(new FileOutputStream(Machinetta.Configuration.BELIEF_LOG_FILE));
                    
                    state = new ProxyState();
                    state.addChangeListener(this);
                    
                } catch (Exception e) {
                    Machinetta.Debugger.debug( 3,"Failed to create beliefs logfile: " + e);
                }
            }
        }
    }
    
    public void stateChanged(BeliefID[] b) {
        for (int i = 0; !failed && i < b.length; i++) {
            Belief bel = state.getBelief(b[i]);
            try {
                if (bel instanceof java.io.Serializable) {
                    out.writeObject(bel);
                    out.reset();
                }
            } catch (IOException e) {
                Machinetta.Debugger.debug( 4,"Failed to write " + bel.getID() + " to belief log: " + e);
                Machinetta.Debugger.debug( 4,"Belief logging stopped.");
                failed = true;
            }
        }
    }
    
    /** Use this version of the logger to play the logfile. */
    public BeliefLogger(String logfile) {
        try {
            in = new ObjectInputStream(new FileInputStream(logfile));
            
            new BeliefDisplay(in);
        } catch (Exception e) {
            Machinetta.Debugger.debug( 3,"Failed to open beliefs logfile: " + e);
        }
        
    }
    
    public BeliefLogger(ObjectInputStream in) {
        this.in = in;
        new BeliefDisplay(in);
    }
    
    public static void main(String argv[]) {
        if (argv.length < 1) {
            System.out.println("Usage: Machinetta.State.BeliefLogger logfile");
        } else {
            new BeliefLogger(argv[0]);
        }
    }
    
    class BeliefDisplay extends JFrame implements ActionListener {
        
        Hashtable<BeliefID, BeliefPane> currBeliefs = null;
        JButton nextB = new JButton("Step"), runB = new JButton("Run");
        boolean runGo = false;
        JSlider runSpeedS = new JSlider(0, 5000, 4000);
        Thread runThread = null;
        JPanel beliefP = new JPanel();
        DetailedBeliefDisplay detailsF = new DetailedBeliefDisplay();
        JScrollPane scrollP = null;
        public static final long serialVersionUID = 1L;
        public BeliefDisplay(ObjectInputStream in) {
            super("Beliefs");
            
            currBeliefs = new Hashtable<BeliefID, BeliefPane>();
            
            addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    System.exit(0);
                }
            });
            
            getContentPane().setLayout(new BorderLayout());
            JPanel controlsP = new JPanel();
            controlsP.add(nextB);
            nextB.addActionListener(this);
            controlsP.add(runB);
            runB.addActionListener(this);
            controlsP.add(runSpeedS);
            beliefP.setLayout(new GridLayout(0, 2));
            scrollP = new JScrollPane(beliefP, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            
            JSplitPane splitP = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(detailsF), scrollP);
            splitP.setDividerLocation(150);
            getContentPane().add(splitP, BorderLayout.CENTER);
            getContentPane().add(controlsP, BorderLayout.SOUTH);
            
            pack();
            setSize(1000,400);
            setVisible(true);
        }
        
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == nextB) {
                step();
            } else if (e.getSource() == runB) {
                if (runGo) {
                    nextB.setEnabled(true);
                    runGo = false;
                    runB.setText("Start");
                } else {
                    nextB.setEnabled(false);
                    runGo = true;
                    runB.setText("Stop");
                    runThread = new Thread() {
                        public void run() {
                            while(runGo) {
                                try {
                                    sleep(5000 - runSpeedS.getValue());
                                } catch (Exception e) {}
                                step();
                            }
                        }
                    };
                    runThread.start();
                }
            }
        }
        
        private void step() {
            Belief bel = getNext();
            if (bel != null) {
                System.out.println("Belief: " + bel);
                BeliefPane pane = (BeliefPane)currBeliefs.get(bel.getID());
                if (pane == null) {
                    pane = new BeliefPane(bel);
                    beliefP.add(pane);
                    beliefP.validate();
                    scrollP.validate();
                    currBeliefs.put(bel.getID(), pane);
                }
                detailsF.setBelief(pane);
            }
        }
        
        public Belief getNext() {
            
            Belief bel = null;
            try {
                try {
                    bel = (Belief)in.readObject();
                    //System.out.println("Belief was : " + bel.getID());
                } catch (EOFException e1) {}
            } catch (Exception e) {
                Machinetta.Debugger.debug( 3,"Read failure: " + e);
            }
            
            return bel;
        }
        
        class BeliefPane extends JPanel implements MouseListener {
            
            public Belief bel = null;
            
            public BeliefPane(Belief b) {
                bel = b;
                add(new JLabel(b.getID().toString()));
                addMouseListener(this);
                setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
            }
            
            public void update(Belief b) {
                bel = b;
            }
            
            public void mouseClicked(MouseEvent e) {
                detailsF.setBelief(this);
            }
            
            public void mouseEntered(MouseEvent e) {}
            public void mouseExited(MouseEvent e) {}
            public void mousePressed(MouseEvent e) {}
            public void mouseReleased(MouseEvent e) {}
            
            public Dimension getPreferredSize() { return new Dimension(100, 25); }
            
            public void setSelected() {
                setBackground(Color.green);
                repaint();
            }
            
            public void unsetSelected() {
                setBackground(Color.lightGray);
                repaint();
            }
            
            public static final long serialVersionUID = 1L;
                 
        }
        
        class DetailedBeliefDisplay extends JPanel {
            
            JLabel idL = new JLabel("None");
            BeliefPane curr = null;
            
            public DetailedBeliefDisplay() {
                setLayout(new GridLayout(0, 2));
                add(new JPanel());
                add(idL);
            }
            
            public void setBelief(BeliefPane bp) {
                
                if (curr != null) {
                    curr.unsetSelected();
                }
                bp.setSelected();
                curr = bp;
                
                Belief b = bp.bel;
                
                removeAll();
                idL.setText(b.getID().toString());
                add(idL);
                
                // Belief names
                String belType = b.getClass().toString();
                belType = belType.substring(belType.lastIndexOf('.') + 1);
                
                // Fields as attributes
                Field fs [] = b.getClass().getFields();
                
                for (int i = 0; i < fs.length; i++) {
                    Field f = fs[i];
                    Machinetta.Debugger.debug( 1,"Field " + f + " " + f.PUBLIC);
                    Machinetta.Debugger.debug( 1,"Type : " + f.getType());
                    String fname = f.getName();
                    Object o = null;
                    try {
                        o = f.get(b);
                    } catch (Exception e) {
                        Machinetta.Debugger.debug( 3,"Problem getting field " + f + " from object " + b);
                        continue;
                    }
                    if (fname.compareTo("id") == 0) {
                    } else if (o == null) {
                        add(new JLabel(fname + " : null"));
                    } else if (o.getClass().toString().startsWith("class java.lang."))
                        add(new JLabel(fname + " : " + o));
                    else if (o instanceof Vector)
                        add(vectorPanel(fname, (Vector)o));
                    else if (o instanceof Hashtable)
                        add(hashtablePanel(fname, (Hashtable)o));
                    else if (o instanceof Belief)
                        add(new JLabel(fname + " " + ((Belief)o).getID()));
                    else if (o instanceof BeliefNameID)
                        add(new JLabel(fname + " : " + o));
                    else
                        add(new JLabel(fname + " : " + o.getClass()));
                }
                revalidate();
                repaint();
            }
            
            JPanel vectorPanel(String name, Vector v) {
                JPanel panel = new JPanel();
                JComboBox combo = new JComboBox(v);
                panel.add(new JLabel(name));
                panel.add(combo);
                return panel;
            }
            
            JComponent hashtablePanel(String name, Hashtable h) {
                JComboBox combo = new JComboBox();
                for (Enumeration e = h.keys(); e.hasMoreElements(); ) {
                    Object key = e.nextElement();
                    combo.addItem(key + ":" + h.get(key));
                }
                JPanel panel = new JPanel();
                panel.add(new JLabel(name));
                panel.add(combo);
                return panel;
            }
            public static final long serialVersionUID = 1L;
        }
    }
}
