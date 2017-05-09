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
 * LogFilePlayer.java
 *
 * Created on June 30, 2004, 10:37 PM
 */

package Machinetta.Coordination.MAC;

import Machinetta.State.BeliefType.Belief;
import Machinetta.State.BeliefNameID;
import java.util.*;
import java.io.*;
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
public class LogFilePlayer extends JFrame {
    
    
    // GUI stuff
    JPanel agentsP = new JPanel(); {
        agentsP.setLayout(new GridLayout(0,2));
    }
    Hashtable<String,AgentDisplay> agentPanels = new Hashtable<String,AgentDisplay>();
    DetailedAgentDisplay dad = null;
    public static final long serialVersionUID = 1L;
    
    /** Creates a new instance of LogFilePlayer */
    public LogFilePlayer(String logfile) {
        // For playing
        ObjectInputStream in = null;
        
        try {
            in = new ObjectInputStream(new FileInputStream(logfile));
        } catch (IOException e) {
            Machinetta.Debugger.debug( 5,"Failed to open logfile: " + logfile);
            System.exit(-1);
        }
        
        doit(in);
    }
    
    protected void doit(ObjectInputStream in) {
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        dad = new DetailedAgentDisplay();
        
        JSplitPane splitP = new JSplitPane(JSplitPane.VERTICAL_SPLIT, dad, agentsP);
        splitP.setDividerLocation(100);
        getContentPane().add(splitP);
        
        pack();
        setSize(1000,400);
        setVisible(true);
        
        boolean OK = true;
        while (OK) {
            try {
                Object o = in.readObject();
                System.out.println("Object: " + o);
                if (o instanceof Agent) {
                    Agent a = (Agent)o;
                    if (agentPanels.get(a.makeComparisonString(a.getDefiningBeliefs())) != null) {
                        System.out.println("Agent repeated");
                    } else {
                        AgentDisplay ad = new AgentDisplay((Agent)o);
                        agentsP.add(ad);
                        agentsP.validate();
                        dad.setAgent(ad);
                        agentPanels.put(a.makeComparisonString(a.getDefiningBeliefs()), ad);
                    }
                }
                
                try {
                    Thread.sleep(2000);
                } catch (Exception e) {}
                
            } catch (IOException e) {
                Machinetta.Debugger.debug( 1,"Object read failed: " + e);
                OK = false;
            } catch (Exception e2) {
                Machinetta.Debugger.debug( 1,"Object read failed: " + e2);
            }
        }
        
    }
    
    class AgentDisplay extends JPanel implements MouseListener {
        Agent agent = null;
        String labelText = null;
        
        public AgentDisplay(Agent a) {
            agent = a;
            String className = (agent.getClass().toString());
            className = className.substring(className.lastIndexOf('.')+1);
            labelText = className + " : " + agent.makeComparisonString(agent.getDefiningBeliefs());
            add(new JLabel(labelText));
            addMouseListener(this);
        }

        public void setSelected() {
            setBackground(Color.green);
            repaint();
        }
        
        public void unsetSelected() {
            setBackground(Color.lightGray);
            repaint();
        }
        
        public void mouseClicked(MouseEvent e) {
            dad.setAgent(this);
        }
        
        public void mouseEntered(MouseEvent e) {}
        public void mouseExited(MouseEvent e) {}
        public void mousePressed(MouseEvent e) {}
        public void mouseReleased(MouseEvent e) {}
        public static final long serialVersionUID = 1L;
    }
    
    
    class DetailedAgentDisplay extends JPanel {
        JLabel idL = new JLabel("None");
        AgentDisplay curr = null;
        
        Agent agent = null;
        
        public DetailedAgentDisplay() {
            
            setLayout(new GridLayout(0, 2));
            add(new JPanel());
            add(idL);
        }
        
        public void setAgent(AgentDisplay bp) {
            
            if (curr != null) {
                curr.unsetSelected();
            }
            bp.setSelected();
            curr = bp;
            
            Agent b = bp.agent;
            
            removeAll();
            idL.setText(curr.labelText);
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
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java Machinetta.Coordination.MAC.LogFilePlayer logfile");
        } else {
            new LogFilePlayer(args[0]);
        }
    }
    
}
