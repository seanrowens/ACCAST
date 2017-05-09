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
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package AirSim.Commander;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.event.*;
import javax.swing.*;
import AirSim.Machinetta.CostMaps.BinaryBayesFilterCostMap;
import AirSim.Machinetta.CostMaps.BBFTabPanel;
import AirSim.Machinetta.MiniWorldState;

/**
 *
 * @author junyounk
 */
public class BinaryBayesFilterGUI {
    private MiniWorldState miniWorldState = null;
    private JFrame bbfBeliefFrame = null;
    private JFrame bbfEntropyFrame = null;
    private BBFTabPanel bbfBeliefTabPanel = null;
    private BBFTabPanel bbfEntropyTabPanel = null;
    private BinaryBayesFilterCostMap bbfCM = null;
    
    private void buildBBFFrames() {
	String proxyIDString = "Operator";
        if(SimConfiguration.BAYES_FILTER_PANEL_ON) {
	    bbfBeliefFrame = new JFrame("Bayes Filter Beliefs "+proxyIDString);
	    bbfBeliefTabPanel = new BBFTabPanel(miniWorldState);
	    bbfBeliefFrame.setLocation(SimConfiguration.BAYES_FILTER_PANEL_X,SimConfiguration.BAYES_FILTER_PANEL_Y);
	    bbfBeliefFrame.getContentPane().setLayout(new BorderLayout());
	    bbfBeliefFrame.getContentPane().add(bbfBeliefTabPanel, BorderLayout.CENTER);
	    bbfBeliefFrame.pack();
	    bbfBeliefFrame.setSize((int)(SimConfiguration.MAP_WIDTH_METERS/(SimConfiguration.BBF_GRID_SCALE_FACTOR*2)), (int)(SimConfiguration.MAP_HEIGHT_METERS/(SimConfiguration.BBF_GRID_SCALE_FACTOR*2)));
	    bbfBeliefFrame.addWindowListener(new WindowAdapter() {
                    public void windowClosing(WindowEvent e) {
                        System.exit(0);
                    }
                });
	    bbfBeliefFrame.setVisible(true);
	} 
        
        if(SimConfiguration.ENTROPY_PANEL_ON) {
	    bbfEntropyFrame = new JFrame("Entropy "+proxyIDString);
	    bbfEntropyTabPanel = new BBFTabPanel(miniWorldState);
	    bbfEntropyFrame.setLocation(SimConfiguration.ENTROPY_PANEL_X,SimConfiguration.ENTROPY_PANEL_Y);
	    bbfEntropyFrame.getContentPane().setLayout(new BorderLayout());
	    bbfEntropyFrame.getContentPane().add(bbfEntropyTabPanel, BorderLayout.CENTER);
	    bbfEntropyFrame.pack();
	    bbfEntropyFrame.setSize((int)(SimConfiguration.MAP_WIDTH_METERS/(SimConfiguration.BBF_GRID_SCALE_FACTOR*2)), (int)(SimConfiguration.MAP_HEIGHT_METERS/(SimConfiguration.BBF_GRID_SCALE_FACTOR*2)));
	    bbfEntropyFrame.addWindowListener(new WindowAdapter() {
                    public void windowClosing(WindowEvent e) {
                        System.exit(0);
                    }
                });
	    bbfEntropyFrame.setVisible(true);
	}
    }
    
    public BinaryBayesFilterCostMap generateBBF() {        
        miniWorldState = new MiniWorldState();
	buildBBFFrames();
        bbfCM = new BinaryBayesFilterCostMap(SimConfiguration.MAP_WIDTH_METERS, SimConfiguration.MAP_HEIGHT_METERS, miniWorldState, bbfBeliefTabPanel, bbfEntropyTabPanel);
	return bbfCM;       
    }
}
