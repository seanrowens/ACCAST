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
 * XPlaneController.java
 *
 * Created on November 12, 2007, 1:02 PM
 */

package AirSim.Commander;

import java.text.DecimalFormat;
import java.util.*;
import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import javax.swing.*;

/**
 *
 * @author  junyounk
 */
public class XPlaneController extends javax.swing.JFrame {
    private final static DecimalFormat fmt = new DecimalFormat("0.00000");
    
    double latDelta = .1;
    double lonDelta = .1;
    double altDelta = 100.0;
    double yawDelta = 3;
    double pitchDelta = 5;
    double rollDelta = 5;
    double moveFactor = .001;
    
    // http://www.world-airport-codes.com/
    
    private javax.swing.JComboBox latLonCombo;
    private HashMap<String,Location> latLonMap = null;
    private ArrayList<String> latLonAry = null;
    private String[] latLonNames = null;
    
    private class Location {
	double lat;
	double lon;
	double alt;
	public Location(double lat, double lon, double alt) {
	    this.lat = lat;
	    this.lon = lon;
	    this.alt = alt;
	}
    }
    public static String[] parseList(char delim, String list) {
	if(list == null)
	    return null;
	if(list.equals(""))
	    return null;

	Vector returnVec = new Vector();
	String[] returnArray = null;

	// Copy list into a char array.
	char listChars[];
	listChars = new char[list.length()];
	list.getChars(0, list.length(), listChars, 0);

	int count = 0;
	int itemStart = 0;
	int itemEnd = 0;
	String newItem = null;

	while(count < listChars.length) {
	    count = itemEnd;
	    if(count >= listChars.length)
		break;
	    itemStart = count;
	    itemEnd = itemStart;
	    while(itemEnd < listChars.length) {
		if(delim != listChars[itemEnd]) {
		    itemEnd++;
		}
		else
		    break;
	    }
	    newItem = new String(listChars, itemStart, itemEnd - itemStart);
	    itemEnd++;
	    count = itemEnd;
	    returnVec.add(newItem);
	}
	// Convert from vector to array, and return it.
	returnArray = new String[1];
	returnArray = (String[])returnVec.toArray((Object[])returnArray);
	return returnArray;
    }
    private void loadLatLons(String latLonFileName) {
	try {
	    FileReader fileReader = new FileReader(latLonFileName);
	    BufferedReader bufferedReader = new BufferedReader(fileReader);

	    latLonMap = new HashMap<String,Location>();
	    latLonAry = new ArrayList<String>();
    
	    while(true) {
		String line = bufferedReader.readLine();
		if(null == line)
		    break;
		if(line.startsWith("#$"))
		    continue;
// 		String[] fields = parseList('\t', line);
// 		if(fields.length != 3) {
// 		    Machinetta.Debugger.debug(3,"Unable to parse line in lat lon file, ignoring, line='"+line+"'");
// 		    continue;
// 		}
// 		double lat = Double.parseDouble(fields[0]);
// 		double lon = Double.parseDouble(fields[1]);
// 		String name = fields[2];
 		String[] fields = parseList(':', line);
 		if(fields.length != 14) {
 		    Machinetta.Debugger.debug(3,"Unable to parse line in lat lon file, got "+fields.length+" colon (:) separated fields, expecting 14, ignoring, line='"+line+"'");
 		    continue;
 		}
		String code = fields[0];
		String name = fields[2];
		String country = fields[4];
		double degrees, minutes, seconds;
		degrees = Integer.parseInt(fields[05]);
		minutes = Integer.parseInt(fields[06]);
		seconds = Integer.parseInt(fields[07]);
		double lat = degrees + minutes/60 + seconds/3600;
		if(fields[8].equals("S"))
		    lat = -lat;
		
		degrees = Integer.parseInt(fields[9]);
		minutes = Integer.parseInt(fields[10]);
		seconds = Integer.parseInt(fields[11]);
		double lon = degrees + minutes/60 + seconds/3600;
		if(fields[12].equals("U"))
		    lon = -lon;
		
		double alt = Double.parseDouble(fields[13]);

		latLonMap.put(name,new Location(lat,lon, alt));
		latLonAry.add(name);
	    }
	}
	catch(Exception e){
	    Machinetta.Debugger.debug(3,"Unable to read lat/lon file='"+latLonFileName+"', e=" + e);
	    e.printStackTrace();
	    return;
	}

    }

    /** Creates new form XPlaneController */
    public XPlaneController(String latLonFileName) {


	if(null != latLonFileName) 
	    loadLatLons(latLonFileName);
	if(null != latLonMap) {
            Machinetta.Debugger.debug(1, "Loaded "+latLonAry.size()+" lat/lon pairs from "+latLonFileName);
	    latLonNames = latLonAry.toArray( new String[1]);
	    latLonCombo = new javax.swing.JComboBox();
	    latLonCombo.setModel(new javax.swing.DefaultComboBoxModel(latLonNames));
	    
// 	    JFrame newFrame = new JFrame("lat lon dropdown");
// 	    JPanel newPanel = new JPanel();
// 	    newFrame.getContentPane().add(newPanel);
// 	    newPanel.add(latLonCombo);
// 	    newFrame.setVisible(true);
	}
	initComponents();

        connectToXserver();
        
        //@NOTE: Do we need to initilize at every initial time?
        //sendToXPlane(0.0, 0.0, 0.0, 0.0f, 0.0f, 0.0f);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
	this.setLocation(30,30);
        UAVNumLabel = new javax.swing.JLabel();
        jComboBox1 = new javax.swing.JComboBox();
        LatLabel = new javax.swing.JLabel();
        LatField = new javax.swing.JTextField();
        LonLabel = new javax.swing.JLabel();
        LonField = new javax.swing.JTextField();
        AltField = new javax.swing.JTextField();
        RolField = new javax.swing.JTextField();
        PitField = new javax.swing.JTextField();
        YawField = new javax.swing.JTextField();
        RolLabel = new javax.swing.JLabel();
        PitLabel = new javax.swing.JLabel();
        YawLabel = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        sendButton = new javax.swing.JButton();
        AltLabel = new javax.swing.JLabel();
        LatDecButton = new javax.swing.JButton();
        LatIncButton = new javax.swing.JButton();
        LonDecButton = new javax.swing.JButton();
        LonIncButton = new javax.swing.JButton();
        AltDecButton = new javax.swing.JButton();
        RolDecButton = new javax.swing.JButton();
        PitDecButton = new javax.swing.JButton();
        YawDecButton = new javax.swing.JButton();
        AltIncButton = new javax.swing.JButton();
        RolIncButton = new javax.swing.JButton();
        PitIncButton = new javax.swing.JButton();
        YawIncButton = new javax.swing.JButton();
        InitButton = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        FWButton = new javax.swing.JButton();
        BWButton = new javax.swing.JButton();
        TRButton = new javax.swing.JButton();
        TLButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("XPlane Controller v0.2");
        UAVNumLabel.setText("UAV Number");

	//        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "UAV 1", "UAV 2", "UAV 3", "UAV 4" }));
        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel(latLonNames));

        jComboBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox1ActionPerformed(evt);
            }
        });

        LatLabel.setText("LAT");

        LatField.setText("0.0");

        LonLabel.setText("LON");

        LonField.setText("0.0");

        AltField.setText("0.0");

        RolField.setText("0.0");

        PitField.setText("0.0");

        YawField.setText("0.0");

        RolLabel.setText("ROL");

        PitLabel.setText("PIT");

        YawLabel.setText("YAW (angle)");

        sendButton.setText("Send");
        sendButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sendButtonActionPerformed(evt);
            }
        });

        AltLabel.setText("ALT");

        LatDecButton.setText("<");
        LatDecButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                LatDecButtonActionPerformed(evt);
            }
        });

        LatIncButton.setText(">");
        LatIncButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                LatIncButtonActionPerformed(evt);
            }
        });

        LonDecButton.setText("<");
        LonDecButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                LonDecButtonActionPerformed(evt);
            }
        });

        LonIncButton.setText(">");
        LonIncButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                LonIncButtonActionPerformed(evt);
            }
        });

        AltDecButton.setText("<");
        AltDecButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AltDecButtonActionPerformed(evt);
            }
        });

        RolDecButton.setText("<");
        RolDecButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                RolDecButtonActionPerformed(evt);
            }
        });

        PitDecButton.setText("<");
        PitDecButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                PitDecButtonActionPerformed(evt);
            }
        });

        YawIncButton.setText("<");
        YawIncButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                YawIncButtonActionPerformed(evt);
            }
        });

        AltIncButton.setText(">");
        AltIncButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AltIncButtonActionPerformed(evt);
            }
        });

        RolIncButton.setText(">");
        RolIncButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                RolIncButtonActionPerformed(evt);
            }
        });

        PitIncButton.setText(">");
        PitIncButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                PitIncButtonActionPerformed(evt);
            }
        });

        YawDecButton.setText(">");
        YawDecButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                YawDecButtonActionPerformed(evt);
            }
        });

        InitButton.setText("Init");
        InitButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                InitButtonActionPerformed(evt);
            }
        });

        jLabel1.setText("MOVE");

        FWButton.setText("^");
        FWButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                FWButtonActionPerformed(evt);
            }
        });

        BWButton.setText("v");
        BWButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BWButtonActionPerformed(evt);
            }
        });

        TRButton.setText(">");
        TRButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TRButtonActionPerformed(evt);
            }
        });

        TLButton.setText("<");
        TLButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TLButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(LatLabel)
                    .add(AltLabel)
                    .add(UAVNumLabel)
                    .add(LonLabel)
                    .add(RolLabel)
                    .add(PitLabel)
                    .add(YawLabel))
                .add(23, 23, 23)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(YawField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 202, Short.MAX_VALUE)
                            .add(PitField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 202, Short.MAX_VALUE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(PitDecButton)
                            .add(YawDecButton))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(YawIncButton)
                            .add(PitIncButton)))
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(LatField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 202, Short.MAX_VALUE)
                            .add(LonField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 202, Short.MAX_VALUE)
                            .add(AltField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 202, Short.MAX_VALUE)
                            .add(RolField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 202, Short.MAX_VALUE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(RolDecButton)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(RolIncButton))
                            .add(layout.createSequentialGroup()
                                .add(AltDecButton)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(AltIncButton))
                            .add(layout.createSequentialGroup()
                                .add(LatDecButton)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(LatIncButton))
                            .add(layout.createSequentialGroup()
                                .add(LonDecButton)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(LonIncButton))))
                    .add(jComboBox1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 300, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(50, 50, 50))
            .add(jSeparator1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 466, Short.MAX_VALUE)
            .add(layout.createSequentialGroup()
                .add(41, 41, 41)
                .add(InitButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 82, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(37, 37, 37)
                .add(sendButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 89, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 38, Short.MAX_VALUE)
                .add(TLButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(BWButton)
                    .add(FWButton)
                    .add(layout.createSequentialGroup()
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jLabel1)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(TRButton)))
                .add(37, 37, 37))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(23, 23, 23)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(UAVNumLabel)
                    .add(jComboBox1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(15, 15, 15)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(LatDecButton)
                    .add(LatIncButton)
                    .add(LatLabel)
                    .add(LatField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(LonDecButton)
                    .add(LonIncButton)
                    .add(LonLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 15, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(LonField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(AltDecButton)
                    .add(AltIncButton)
                    .add(AltLabel)
                    .add(AltField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(RolDecButton)
                    .add(RolIncButton)
                    .add(RolLabel)
                    .add(RolField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(PitDecButton)
                    .add(PitIncButton)
                    .add(PitLabel)
                    .add(PitField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(YawDecButton)
                    .add(YawIncButton)
                    .add(YawLabel)
                    .add(YawField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(21, 21, 21)
                .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(49, 49, 49)
                        .add(InitButton))
                    .add(layout.createSequentialGroup()
                        .add(49, 49, 49)
                        .add(sendButton))
                    .add(layout.createSequentialGroup()
                        .add(17, 17, 17)
                        .add(FWButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(TRButton)
                            .add(jLabel1)
                            .add(TLButton))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(BWButton)))
                .add(22, 22, 22))
        );
        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void TRButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TRButtonActionPerformed
        float yaw = Float.parseFloat(this.YawField.getText());
        yaw -= yawDelta;
        while(yaw < 0.0) yaw += 360.0;
        while(yaw >= 360.0) yaw -= 360.0;
        
        this.YawField.setText(yaw+"");
	sendCurrentToXPlane();
    }//GEN-LAST:event_TRButtonActionPerformed

    private void TLButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TLButtonActionPerformed
        float yaw = Float.parseFloat(this.YawField.getText());
        yaw += yawDelta;
        while(yaw < 0.0) yaw += 360.0;
        while(yaw >= 360.0) yaw -= 360.0;
        
        this.YawField.setText(yaw+"");
	sendCurrentToXPlane();
    }//GEN-LAST:event_TLButtonActionPerformed

    private void BWButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BWButtonActionPerformed
        double lon = Double.parseDouble(this.LonField.getText());
        double lat = Double.parseDouble(this.LatField.getText());
        float yaw = Float.parseFloat(this.YawField.getText());

        lon -= moveFactor*Math.cos(yaw/360.0*2*Math.PI);
        lat -= moveFactor*Math.sin(yaw/360.0*2*Math.PI);
        this.LonField.setText(lon+"");
        this.LatField.setText(lat+"");
	sendCurrentToXPlane();
    }//GEN-LAST:event_BWButtonActionPerformed

    private void FWButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_FWButtonActionPerformed
        double lon = Double.parseDouble(this.LonField.getText());
        double lat = Double.parseDouble(this.LatField.getText());
        float yaw = Float.parseFloat(this.YawField.getText());

        lon += moveFactor*Math.cos(yaw/360.0*2*Math.PI);
        lat += moveFactor*Math.sin(yaw/360.0*2*Math.PI);
        this.LonField.setText(lon+"");
        this.LatField.setText(lat+"");
	sendCurrentToXPlane();
    }//GEN-LAST:event_FWButtonActionPerformed

    private void InitButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_InitButtonActionPerformed
        sendToXPlane(0.0, 0.0, 0.0, 0.0f, 0.0f, 0.0f);
    }//GEN-LAST:event_InitButtonActionPerformed

    private void YawIncButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_YawIncButtonActionPerformed
        float yaw = Float.parseFloat(this.YawField.getText());
        yaw += yawDelta;
        this.YawField.setText(yaw+"");
	sendCurrentToXPlane();
    }//GEN-LAST:event_YawIncButtonActionPerformed

    private void YawDecButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_YawDecButtonActionPerformed
        float yaw = Float.parseFloat(this.YawField.getText());
        yaw -= yawDelta;
        this.YawField.setText(yaw+"");
	sendCurrentToXPlane();
    }//GEN-LAST:event_YawDecButtonActionPerformed

    private void PitIncButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_PitIncButtonActionPerformed
        float pit = Float.parseFloat(this.PitField.getText());
        pit += pitchDelta;
        this.PitField.setText(pit+"");
	sendCurrentToXPlane();
    }//GEN-LAST:event_PitIncButtonActionPerformed

    private void PitDecButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_PitDecButtonActionPerformed
        float pit = Float.parseFloat(this.PitField.getText());
        pit -= pitchDelta;
        this.PitField.setText(pit+"");
	sendCurrentToXPlane();
    }//GEN-LAST:event_PitDecButtonActionPerformed

    private void RolIncButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_RolIncButtonActionPerformed
        float rol = Float.parseFloat(this.RolField.getText());
        rol += rollDelta;
        this.RolField.setText(rol+"");
	sendCurrentToXPlane();
    }//GEN-LAST:event_RolIncButtonActionPerformed

    private void RolDecButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_RolDecButtonActionPerformed
        float rol = Float.parseFloat(this.RolField.getText());
        rol -= rollDelta;
        this.RolField.setText(rol+"");
	sendCurrentToXPlane();
    }//GEN-LAST:event_RolDecButtonActionPerformed

    private void AltIncButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AltIncButtonActionPerformed
        double alt = Double.parseDouble(this.AltField.getText());
        alt += altDelta;
        this.AltField.setText(alt+"");
	sendCurrentToXPlane();
    }//GEN-LAST:event_AltIncButtonActionPerformed

    private void AltDecButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AltDecButtonActionPerformed
        double alt = Double.parseDouble(this.AltField.getText());
        alt -= altDelta;
        this.AltField.setText(alt+"");
	sendCurrentToXPlane();
    }//GEN-LAST:event_AltDecButtonActionPerformed

    private void LonIncButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_LonIncButtonActionPerformed
        double lon = Double.parseDouble(this.LonField.getText());
        lon += lonDelta;
        this.LonField.setText(lon+"");
	sendCurrentToXPlane();
    }//GEN-LAST:event_LonIncButtonActionPerformed

    private void LonDecButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_LonDecButtonActionPerformed
        double lon = Double.parseDouble(this.LonField.getText());
        lon -= lonDelta;
        this.LonField.setText(lon+"");
	sendCurrentToXPlane();
    }//GEN-LAST:event_LonDecButtonActionPerformed

    private void LatIncButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_LatIncButtonActionPerformed
        double lat = Double.parseDouble(this.LatField.getText());
        lat += latDelta;
        this.LatField.setText(lat+"");
	sendCurrentToXPlane();
    }//GEN-LAST:event_LatIncButtonActionPerformed

    private void LatDecButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_LatDecButtonActionPerformed
        double lat = Double.parseDouble(this.LatField.getText());
        lat -= latDelta;
        this.LatField.setText(lat+"");
	sendCurrentToXPlane();
    }//GEN-LAST:event_LatDecButtonActionPerformed

    private void sendButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sendButtonActionPerformed
	sendCurrentToXPlane();
    }//GEN-LAST:event_sendButtonActionPerformed

    private void jComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {
	String selected = (String)((JComboBox)evt.getSource()).getSelectedItem();
	if(null == selected) {
	    Machinetta.Debugger.debug(3,"Null selected from jcombobox1");
	    return;
	}
	Location location = latLonMap.get(selected);
	if(null == location) {
	    Machinetta.Debugger.debug(3,"Null location for selected from jcombobox1, selected='"+selected+"'");
	    return;
	}
	double lon = location.lon;
	double lat = location.lat;
	double alt = location.alt;
	this.LatField.setText(lat+"");
	this.LonField.setText(lon+"");
	this.AltField.setText((alt+50)+"");
	sendCurrentToXPlane();
    }
    
    private void sendCurrentToXPlane() {
        sendToXPlane(Double.parseDouble(this.LonField.getText()),
		     Double.parseDouble(this.LatField.getText()),
		     Double.parseDouble(this.AltField.getText()),
		     Float.parseFloat(this.RolField.getText()),
		     Float.parseFloat(this.PitField.getText()),
		     Float.parseFloat(this.YawField.getText()));
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
	String latLonFileName = null;
	if(null != args) {
	    if(args.length >= 1)
		latLonFileName = args[0];
	}
	final String flatLonFileName = latLonFileName;
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new XPlaneController(flatLonFileName).setVisible(true);
            }
        });
    }

   public void connectToXserver()  {
        try{
            //Machinetta.Debugger.debug(1, "       Trying to connect to xServer!");
            xserver = InetAddress.getByName(USE_XPLANE_CAMERA_HOST);
            outputSocket = new DatagramSocket();
            //Machinetta.Debugger.debug(1, "       Succeed to connect?!");
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private void sendToXPlane(double lon, double lat, double alt, float rol, float pit, float yaw) {
	//        Machinetta.Debugger.debug(1, "       Try to send data to xPlane "+lon+","+lat+","+alt);
        int count = 0;
        int cameraIndex = 255;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(out);
        try {
            dout.writeByte((byte)count); //This is set below to the number of UAVs
            dout.writeByte((byte)cameraIndex); //This is set below to the UAV we want to view from in XPlane
            
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
	try {
	    dout.writeDouble(lat);
	    dout.writeDouble(lon);
	    // @TODO: Fix altitude
	    //                    dout.writeDouble(a.location.z);
	    dout.writeDouble(alt);
                    
	    // @TODO: Fix roll and pitch?
	    dout.writeFloat(rol);//Should actually be roll
	    //                    dout.writeFloat((float)a.rollDegrees);//Should actually be roll
	    dout.writeFloat(pit);//Should actually be pitch
	    // yaw/heading works
	    dout.writeFloat((-(float)(yaw))+90.0f);//Should actually be yaw
	    // 		Machinetta.Debugger.debug("Sending to xplane id "+a.getID()+" heading "+(-a.heading.angle())+" roll "+a.rollDegrees+" at lat= "+lat+" lon= "+lon,1,this);
	    Machinetta.Debugger.debug("ToXplane:heading,lat,lon: "+fmt.format(-yaw+90.0f)+", 0.0, "+fmt.format(lat)+", "+fmt.format(lon),1,this);
	} catch (IOException ex) {
	    ex.printStackTrace();
	}
	cameraIndex=count;
	count++;

        byte[] buffer = out.toByteArray();
        buffer[0] = (byte)count;
        buffer[1] = (byte)cameraIndex;
        
        for(int i =0;i<count;i++) {
            reverseDouble(buffer, 2+i*36);
            reverseDouble(buffer, 10+i*36);
            reverseDouble(buffer, 18+i*36);
            reverseFloat(buffer, 26+i*36);
            reverseFloat(buffer, 30+i*36);
            reverseFloat(buffer, 34 +i*36);
        }
        
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, xserver, 27016);
        try {
            
            outputSocket.send(packet);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void sendToXPlaneOld(double lon, double lat, double alt, float rol, float pit, float yaw) {
        Machinetta.Debugger.debug(1, "       Try to send data to xPlane "+lon+","+lat+","+alt);
        int count = 0;
        int cameraIndex = 1;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(out);
        try {
            dout.writeByte((byte)count); //This is set below to the number of UAVs
            dout.writeByte((byte)cameraIndex); //This is set below.
            
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
        try {
            dout.writeDouble(lat);
            dout.writeDouble(lon);
            dout.writeDouble(alt);
                    
            dout.writeFloat(rol);//Should actually be roll
            dout.writeFloat(pit);//Should actually be pitch
            //dout.writeFloat((-(float)(a.heading.angle()))+90.0f);//Should actually be yaw
            dout.writeFloat(yaw);
        } catch (IOException ex) {
		ex.printStackTrace();
	    }

        count++;
           
        byte[] buffer = out.toByteArray();
        buffer[0] = (byte)count;
        buffer[1] = (byte)cameraIndex;
        
        for(int i =0;i<count;i++) {
            reverseDouble(buffer, 2+i*36);
            reverseDouble(buffer, 10+i*36);
            reverseDouble(buffer, 18+i*36);
            reverseFloat(buffer, 26+i*36);
            reverseFloat(buffer, 30+i*36);
            reverseFloat(buffer, 34 +i*36);
        }
        
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, xserver, 27016);
        try {
            
            outputSocket.send(packet);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
    }
    
    private void reverseFloat(byte [] buffer, int start) {
        swapBytes(buffer, start, start+3);
        swapBytes(buffer, start + 1, start+2);
    }
    
    private void reverseDouble(byte [] buffer, int start) {
        swapBytes(buffer, start, start+7);
        swapBytes(buffer, start + 1, start+6);
        swapBytes(buffer, start + 2, start+5);
        swapBytes(buffer, start + 3, start+4);
    }
    
    private void swapBytes(byte [] buffer, int a, int b) {
        byte temp = buffer[a];
        buffer[a] = buffer[b];
        buffer[b] = temp;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton AltDecButton;
    private javax.swing.JTextField AltField;
    private javax.swing.JButton AltIncButton;
    private javax.swing.JLabel AltLabel;
    private javax.swing.JButton BWButton;
    private javax.swing.JButton FWButton;
    private javax.swing.JButton InitButton;
    private javax.swing.JButton LatDecButton;
    private javax.swing.JTextField LatField;
    private javax.swing.JButton LatIncButton;
    private javax.swing.JLabel LatLabel;
    private javax.swing.JButton LonDecButton;
    private javax.swing.JTextField LonField;
    private javax.swing.JButton LonIncButton;
    private javax.swing.JLabel LonLabel;
    private javax.swing.JButton PitDecButton;
    private javax.swing.JTextField PitField;
    private javax.swing.JButton PitIncButton;
    private javax.swing.JLabel PitLabel;
    private javax.swing.JButton RolDecButton;
    private javax.swing.JTextField RolField;
    private javax.swing.JButton RolIncButton;
    private javax.swing.JLabel RolLabel;
    private javax.swing.JButton TLButton;
    private javax.swing.JButton TRButton;
    private javax.swing.JLabel UAVNumLabel;
    private javax.swing.JButton YawDecButton;
    private javax.swing.JTextField YawField;
    private javax.swing.JButton YawIncButton;
    private javax.swing.JLabel YawLabel;
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JButton sendButton;
    // End of variables declaration//GEN-END:variables
    
    public static String USE_XPLANE_CAMERA_HOST = "localhost";
    //    public static String USE_XPLANE_CAMERA_HOST = "128.237.239.214";
    static DatagramSocket outputSocket;
    static InetAddress xserver;
    
}
