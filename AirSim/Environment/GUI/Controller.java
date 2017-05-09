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
 * Controller.java
 *
 * Created on February 26, 2005, 12:10 PM
 */

package AirSim.Environment.GUI;

import AirSim.Environment.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;

import java.io.*;
import java.net.*;

/**
 *
 * @author  pscerri
 */
public class Controller extends JFrame implements ActionListener {
    
    JButton createPlanB = new JButton("CreatePlan");
    JPanel ctrlPanel, planStatusPanel;
    JLabel noPlansL = new JLabel("No outstanding plans");
    JFrame self;
    Hashtable plans = new Hashtable();
    
    BufferedInputStream in;
    BufferedOutputStream out;
    
    // Notice that we are cheating and giving Controller full access to state
    Env env = new Env();
    
    /** Creates a new instance of Controller */
    public Controller() {
        
        createSocketServer();
        
        self = this;
        
        createPlanB.addActionListener(this);
        
        ctrlPanel = new JPanel(new GridLayout(1,0));
        ctrlPanel.add(createPlanB);
        
        planStatusPanel = new JPanel();
        planStatusPanel.setLayout(new GridLayout(0,1));
        planStatusPanel.add(noPlansL);
        
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(ctrlPanel, BorderLayout.SOUTH);
        getContentPane().add(planStatusPanel, BorderLayout.CENTER);
        pack();
        setSize(500, 300);
        setVisible(true);
        
    }
    
    private void createSocketServer() {
        ServerSocket serverS;
        Socket s;
        try {
            serverS = new ServerSocket(2003);
            s = serverS.accept();
        } catch (IOException e) {
            Machinetta.Debugger.debug("Could not open server socket", 5, this);
            return;
        }
        
        try {
            out = new BufferedOutputStream(s.getOutputStream());
            in = new BufferedInputStream(s.getInputStream());
        } catch (IOException e) {
            Machinetta.Debugger.debug("Client exception " + e, 5, this);
        }
        
        Machinetta.Debugger.debug("Connection to client open.", 1, this);
        
        (new Thread() {
            public void run() {
                int errors = 0;
                int len = 1;
                while (len > 0 && in != null && errors < 10) {
                    try  {
                        byte [] input = new byte[256];
                        len = in.read(input);
                        if (len >= 0) input[len] = '\0';
                        // System.out.println("Read " + len + " bytes : " + new String(input));
                        interpret(new String(input));
                    } catch (EOFException e) {
                        Machinetta.Debugger.debug("End of file, server closed", 1, this);
                        errors++;
                    } catch (SocketException e) {
                        Machinetta.Debugger.debug("Socket exception: " + e, 3, this);
                        errors++;
                    } catch (IOException e) {
                        Machinetta.Debugger.debug("Read failed : " + e, 5, this);
                        errors++;
                    } catch (Exception e) {
                        Machinetta.Debugger.debug("Client error : " + e, 5, this);
                        errors++;
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }
    
    private void interpret(String s) {
        Machinetta.Debugger.debug("Message from OITL proxy: " + s, 1, this);
        
        StringTokenizer st = new StringTokenizer(s);
        String type = st.nextToken();
        if (type.equalsIgnoreCase("PlanID")) {
            System.out.println("Getting information about plan");
            String planID = st.nextToken();
            Vector roles = new Vector();
            while (st.hasMoreTokens()) {
                roles.add(st.nextToken());
            }
            PlanStatusP panel = new PlanStatusP(planID, roles);
            if (plans.size() == 0)
                planStatusPanel.remove(noPlansL);
            planStatusPanel.add(panel);
            planStatusPanel.revalidate();
            plans.put(planID, panel);
            
        } else if (type.equalsIgnoreCase("PlanDetails")) {
            System.out.println("Getting message for authorization");
            String planID = st.nextToken();
            Controller.PlanStatusP panel = (Controller.PlanStatusP)plans.get(planID);
            panel.statusL.setText("Authorize?");
            panel.statusL.setBackground(Color.YELLOW);
            panel.addAuthorizeButton();
            panel.statusL.repaint();
        } else if (type.equalsIgnoreCase("Complete")) {
            System.out.println("Getting message for complete");
            String planID = st.nextToken();
            Controller.PlanStatusP panel = (Controller.PlanStatusP)plans.get(planID);
            panel.roleComplete(st.nextToken());
        }
    }
    
    private synchronized void sendMessage(String s) {
        System.out.println("Controller sending: " + s);
        try {
            out.write(s.trim().getBytes());
            out.write('\0');
            out.flush();
        } catch (IOException e) {
            Machinetta.Debugger.debug("Failed to send message " + s + " due to " + e, 5, this);
        }
    }
    
    static int planID = 0;
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == createPlanB) {
            CreatePlanD diag = new CreatePlanD();
            diag.show();
            if (diag.approved) {
                StringBuffer sb = new StringBuffer("NewPlan Plan" + (planID++) + " STRIKE AND SIMULTANEOUS ");
                for (Enumeration tps = diag.targetPanels.elements(); tps.hasMoreElements(); ) {
                    CreatePlanD.TargetP panel = (CreatePlanD.TargetP)tps.nextElement();
                    for (int i = 0; i <= panel.weaponsC.getSelectedIndex(); i++) {
                        sb.append("TARGET 2 3 " + ((AirSim.Environment.Assets.Tank)panel.targetsC.getSelectedItem()).getID().hashCode() + " 5 6 7 8 21.1 22.3 ");
                    }
                }
                sendMessage(sb.toString());
            }
        }
    }
    
    class PlanStatusP extends JComponent {
        
        JLabel planIDL, statusL = new JLabel("Initiated");
        { statusL.setOpaque(true); }
        JButton authorizeB = new JButton("Authorize"), cancelB = new JButton("Cancel");
        JPanel roleP = new JPanel(), leftP = new JPanel();
        Hashtable roleLs = new Hashtable();
        
        public PlanStatusP(String planID, Vector roleIds) {
            setLayout(new BorderLayout());
            
            // Info on left
            leftP.setLayout(new GridLayout(0,1));
            planIDL = new JLabel(planID);
            leftP.add(planIDL);
            leftP.add(statusL);
            leftP.add(cancelB);
            add(leftP, BorderLayout.WEST);
            
            // Role stuff
            roleP.setLayout(new GridLayout(0, 1));
            for (Enumeration e = roleIds.elements(); e.hasMoreElements(); ) {
                String roleId = (String)e.nextElement();
                roleId = roleId.trim();
                Machinetta.Debugger.debug("Role ID was " + roleId, 1, this);
                JLabel rl = new JLabel(" " + roleId + "\n");
                roleLs.put(roleId, rl);
                roleP.add(rl);
            }
            add(roleP, BorderLayout.CENTER);
            
            cancelB.addActionListener( new ActionListener()  {
                public void actionPerformed(ActionEvent e) {
                    sendMessage("Cancel " + planIDL.getText());
                    statusL.setBackground(Color.red);
                    leftP.remove(cancelB);
                    leftP.remove(authorizeB);
                    leftP.revalidate();
                }
            });
            
            authorizeB.addActionListener( new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    for (Enumeration en = roleLs.keys(); en.hasMoreElements(); ) {
                        sendMessage("Authorize " + en.nextElement() + "\0");
                    }
                    statusL.setBackground(Color.green);
                    statusL.setText("Executing");
                    leftP.remove(authorizeB);
                    leftP.revalidate();
                }
            });
            
        }
        
        public void addAuthorizeButton() {
            leftP.add(authorizeB);
            leftP.revalidate();
        }
        public void roleComplete(String roleID) {
            // @todo
            leftP.remove(cancelB);
            leftP.revalidate();
            
            statusL.setText("Complete");
            statusL.setBackground(Color.GRAY);
            statusL.repaint();
        }
    }
    
    class CreatePlanD extends JDialog implements ActionListener {
        public boolean approved = false;
        
        JButton addTargetB = new JButton("Add Target"), OKB = new JButton("OK"), cancelB = new JButton("Cancel");
        JPanel targetP = new JPanel(), buttP = new JPanel();
        Vector targetPanels = new Vector();
        public CreatePlanD() {
            super(self, true);
            
            buttP.add(addTargetB);
            buttP.add(OKB);
            buttP.add(cancelB);
            
            addTargetB.addActionListener(this);
            OKB.addActionListener(this);
            cancelB.addActionListener(this);
            
            targetP.setLayout(new GridLayout(0, 1));
            TargetP init = new TargetP();
            targetPanels.add(init);
            targetP.add(init);
            init.removeB.addActionListener(this);
            
            getContentPane().setLayout(new BorderLayout());
            getContentPane().add(targetP, BorderLayout.CENTER);
            getContentPane().add(buttP, BorderLayout.SOUTH);
            
            setSize(300, 300);
            //pack();
        }
        
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == addTargetB) {
                TargetP newT = new TargetP();
                targetPanels.add(newT);
                targetP.add(newT);
                newT.removeB.addActionListener(this);
                targetP.revalidate();
            } else if (e.getSource() == cancelB) {
                approved = false;
                hide();
            } else if (e.getSource() == OKB) {
                approved = true;
                hide();
            } else {
                for (Enumeration tps = targetPanels.elements(); tps.hasMoreElements(); ) {
                    TargetP panel = (TargetP)tps.nextElement();
                    if (e.getSource() == panel.removeB) {
                        targetPanels.remove(panel);
                        targetP.remove(panel);
                        targetP.revalidate();
                        break;
                    }
                }
            }
        }
        
        class TargetP extends JPanel {
            JComboBox targetsC = new JComboBox(env.getTargets());
            Integer [] numbers = { new Integer(1), new Integer(2), new Integer(3) };
            JComboBox weaponsC = new JComboBox(numbers);
            JButton removeB = new JButton("Remove");
            public TargetP() {
                add(targetsC);
                add(weaponsC);
                add(removeB);
            }
        }
    }
}
