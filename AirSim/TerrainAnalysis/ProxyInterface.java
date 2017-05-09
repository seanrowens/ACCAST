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
 * Interface.java
 *
 * Created on August 24, 2005, 12:48 PM
 *
 */

package AirSim.TerrainAnalysis;

import java.io.*;
import java.util.*;
import java.awt.Point;
import java.awt.geom.Point2D;
import AirSim.Environment.Assets.Tasks.TaskType;
import AirSim.Machinetta.BasicRole;
import AirSim.Machinetta.Beliefs.Location;
import AirSim.Machinetta.NAI;
import AirSim.Machinetta.Path2D;
import Machinetta.Configuration;
import Machinetta.Debugger;
import Machinetta.RAPInterface.InputMessages.InputMessage;
import Machinetta.RAPInterface.OutputMessages.NewRoleMessage;
import Machinetta.RAPInterface.OutputMessages.OutputMessage;
import Machinetta.RAPInterface.RAPInterfaceImplementation;
import Machinetta.State.BeliefType.TeamBelief.Constraints.DirectedInformationRequirement;
import Machinetta.State.BeliefType.TeamBelief.RoleBelief;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.ListIterator;
import Gui.*;

/**
 *
 * @author pscerri
 */


public class ProxyInterface extends RAPInterfaceImplementation {
    public boolean useTerrainAnalysisFlag = true;
    public boolean offlineTerrainAnalysisFlag = true;
    
    public class DummyTerrainAnalysis {
        
        public String baseGridFileName = "/usr1/grd/ntc/ntc_tin_v2.5_071898_0_0_50000_50000_050";
        public boolean startTerrainComm = false;
        public int preload = 51;
        public boolean alreadyInitialized = false;
        // public TerrainAnalysisModule terrainMod = null;
        
        public DummyTerrainAnalysis(boolean useTerrainAnalysisFlag, boolean offlineTerrainAnalysisFlag){
            if(useTerrainAnalysisFlag && !offlineTerrainAnalysisFlag) {
                Machinetta.Debugger.debug("Initializing terrain agent BUT CODE DOES NOT EXIST!!!!",5, this);
/*                terrainMod = new TerrainAnalysisModule("TerrainAnalysisModule",
                        null,
                        null,
                        6677,
                        false,
                        null,
                        baseGridFileName,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        startTerrainComm,
                        preload);
                
                
                if(!alreadyInitialized){
                    terrainMod.initializeTerrainInfo();
                    alreadyInitialized = true;
                }
                Machinetta.Debugger.debug("Done initializing terrain agent "+terrainMod,1, this);
                */
            } else {
                Machinetta.Debugger.debug("Using dummy terrain analysis.", 3, this);
            }
        }
        
        
        private LinkedList<NAI>  offlineTANAIs(int x1, int y1, int x2, int y2) {
            
            LinkedList<NAI> nais = new LinkedList<NAI>();
            
            if(48000 == x2 &&  13119 == x1 &&  4805 == y2 &&  4805 == y1) {
                
                Machinetta.Debugger.debug("Using offline TA 1",3, this);
                nais.add(new NAI("Engage0", 5914.0 - 2000, 47646.0 - 2000, 5914.0 + 2000, 47646.0+2000));
                nais.add(new NAI("Engage1", 17738.0 - 2000, 48267.0 - 2000, 17738.0 + 2000, 48267.0+2000));
                nais.add(new NAI("Engage2", 41422.0 - 2000, 38212.0 - 2000, 41422.0 + 2000, 38212.0+2000));
                nais.add(new NAI("Engage3", 33044.0 - 2000, 44581.0 - 2000, 33044.0 + 2000, 44581.0+2000));
                nais.add(new NAI("Engage4", 43993.0 - 2000, 34736.0 - 2000, 43993.0 + 2000, 34736.0+2000));
                return nais;
            } else if(48000 == x2 &&  13119 == x1 && 14805==y2 &&  14805 == y1) {
                Machinetta.Debugger.debug("Using offline TA 2",3, this);
                nais.add(new NAI("Engage0", 15540.0 - 2000, 35338.0 - 2000, 15540.0 + 2000, 35338.0+2000));
                nais.add(new NAI("Engage1", 18295.0 - 2000, 35576.0 - 2000, 18295.0 + 2000, 35576.0+2000));
                nais.add(new NAI("Engage2", 18568.0 - 2000, 41289.0 - 2000, 18568.0 + 2000, 41289.0+2000));
                nais.add(new NAI("Engage3", 22000.0 - 2000, 47859.0 - 2000, 22000.0 + 2000, 47859.0+2000));
                nais.add(new NAI("Engage4", 19498.0 - 2000, 44622.0 - 2000, 19498.0 + 2000, 44622.0+2000));
                return nais;
            } else if(48000 == x2 && 13119 == x1 && 24805 == y2 && 24805 == y1) {
                Machinetta.Debugger.debug("Using offline TA 3",3, this);
                nais.add(new NAI("Engage0", 48516.0 - 2000, 27058.0 - 2000, 48516.0 + 2000, 27058.0+2000));
                nais.add(new NAI("Engage1", 30534.0 - 2000, 25947.0 - 2000, 30534.0 + 2000, 25947.0+2000));
                nais.add(new NAI("Engage2", 27226.0 - 2000, 25694.0 - 2000, 27226.0 + 2000, 25694.0+2000));
                nais.add(new NAI("Engage3", 27622.0 - 2000, 23257.0 - 2000, 27622.0 + 2000, 23257.0+2000));
                nais.add(new NAI("Engage4", 27301.0 - 2000, 26152.0 - 2000, 27301.0 + 2000, 26152.0+2000));
                
                return nais;
            } else if(48000 == x2 && 13119 == x1 && 34805 == y2 && 34805 == y1) {
                Machinetta.Debugger.debug("Using offline TA 4",3, this);
                nais.add(new NAI("Engage0", 46799.0 - 2000, 17000.0 - 2000, 46799.0 + 2000, 17000.0+2000));
                nais.add(new NAI("Engage1", 26091.0 - 2000, 5619.0 - 2000, 26091.0 + 2000, 5619.0+2000));
                nais.add(new NAI("Engage2", 26614.0 - 2000, 22918.0 - 2000, 26614.0 + 2000, 22918.0+2000));
                nais.add(new NAI("Engage3", 28950.0 - 2000, 19597.0 - 2000, 28950.0 + 2000, 19597.0+2000));
                nais.add(new NAI("Engage4", 19717.0 - 2000, 10063.0 - 2000, 19717.0 + 2000, 10063.0+2000));
                
                return nais;
            } else if(48000 == x2 && 13119 == x1 && 44805 == y2 && 44805 == y1) {
                Machinetta.Debugger.debug("Using offline TA 5",3, this);
                nais.add(new NAI("Engage0", 43959.0 - 2000, 8821.0 - 2000, 43959.0 + 2000, 8821.0+2000));
                nais.add(new NAI("Engage1", 19895.0 - 2000, 13870.0 - 2000, 19895.0 + 2000, 13870.0+2000));
                nais.add(new NAI("Engage2", 26548.0 - 2000, 3863.0 - 2000, 26548.0 + 2000, 3863.0+2000));
                nais.add(new NAI("Engage3", 29972.0 - 2000, 11323.0 - 2000, 29972.0 + 2000, 11323.0+2000));
                nais.add(new NAI("Engage4", 21739.0 - 2000, 17384.0 - 2000, 21739.0 + 2000, 17384.0+2000));
                return nais;
            } else {
                Machinetta.Debugger.debug("Using 'hardcoded' TA",3, this);
                for(int loopi=0; loopi < 5; loopi++){
                    Machinetta.Debugger.debug("getNAIS: Adding to list dummy NAI at 0,0 size 50000, 50000", 1, this);
                    nais.add(new NAI("Engage"+(loopi+1), 0, 0,0+ 50000, 0+ 50000));
                }
                return nais;
            }
        }
        
        private Path2D offlineTAPath(int x1, int y1, int x2, int y2) {
            
            if(13119 == x1  &&  4805 == y1 && 48000 == x2 &&   4805 == y2) {
                // 4805 path
                Path2D path = new Path2D(-1, -1, new AirSim.Machinetta.Point2D( 12500, 5500));
                path.wps.add(new AirSim.Machinetta.Point2D( 12500, 5500));
                path.wps.add(new AirSim.Machinetta.Point2D( 13500, 5500));
                path.wps.add(new AirSim.Machinetta.Point2D( 14500, 5500));
                path.wps.add(new AirSim.Machinetta.Point2D( 15500, 5500));
                path.wps.add(new AirSim.Machinetta.Point2D( 16500, 5500));
                path.wps.add(new AirSim.Machinetta.Point2D( 17500, 5500));
                path.wps.add(new AirSim.Machinetta.Point2D( 18500, 5500));
                path.wps.add(new AirSim.Machinetta.Point2D( 19500, 5500));
                path.wps.add(new AirSim.Machinetta.Point2D( 20500, 5500));
                path.wps.add(new AirSim.Machinetta.Point2D( 21500, 6500));
                path.wps.add(new AirSim.Machinetta.Point2D( 22500, 6500));
                path.wps.add(new AirSim.Machinetta.Point2D( 23500, 6500));
                path.wps.add(new AirSim.Machinetta.Point2D( 24500, 6500));
                path.wps.add(new AirSim.Machinetta.Point2D( 25500, 6500));
                path.wps.add(new AirSim.Machinetta.Point2D( 26500, 6500));
                path.wps.add(new AirSim.Machinetta.Point2D( 27500, 6500));
                path.wps.add(new AirSim.Machinetta.Point2D( 28500, 6500));
                path.wps.add(new AirSim.Machinetta.Point2D( 29500, 6500));
                path.wps.add(new AirSim.Machinetta.Point2D( 30500, 6500));
                path.wps.add(new AirSim.Machinetta.Point2D( 31500, 6500));
                path.wps.add(new AirSim.Machinetta.Point2D( 32500, 6500));
                path.wps.add(new AirSim.Machinetta.Point2D( 33500, 6500));
                path.wps.add(new AirSim.Machinetta.Point2D( 34500, 6500));
                path.wps.add(new AirSim.Machinetta.Point2D( 35500, 6500));
                path.wps.add(new AirSim.Machinetta.Point2D( 36500, 6500));
                path.wps.add(new AirSim.Machinetta.Point2D( 37500, 6500));
                path.wps.add(new AirSim.Machinetta.Point2D( 38500, 6500));
                path.wps.add(new AirSim.Machinetta.Point2D( 39500, 6500));
                path.wps.add(new AirSim.Machinetta.Point2D( 40500, 5500));
                path.wps.add(new AirSim.Machinetta.Point2D( 41500, 5500));
                path.wps.add(new AirSim.Machinetta.Point2D( 42500, 5500));
                path.wps.add(new AirSim.Machinetta.Point2D( 43500, 5500));
                path.wps.add(new AirSim.Machinetta.Point2D( 44500, 5500));
                path.wps.add(new AirSim.Machinetta.Point2D( 45500, 4500));
                path.wps.add(new AirSim.Machinetta.Point2D( 46500, 5500));
                path.wps.add(new AirSim.Machinetta.Point2D( 47500, 4500));
                path.wps.add(new AirSim.Machinetta.Point2D( 48500, 5500));
                path.wps.add(new AirSim.Machinetta.Point2D( 47500, 6500));
                path.wps.add(new AirSim.Machinetta.Point2D( 48500, 6500));
                path.wps.add(new AirSim.Machinetta.Point2D( 48500, 5500));
                path.wps.add(new AirSim.Machinetta.Point2D( 47500, 5500));
                path.wps.add(new AirSim.Machinetta.Point2D(x2, y2));
                
                return path;
            } else if(13119 == x1  &&  14805 == y1 && 48000 == x2 &&  14805 == y2) {
                
                // 14805 path
                Path2D path = new Path2D(-1, -1, new AirSim.Machinetta.Point2D( 12500, 15500));
                path.wps.add(new AirSim.Machinetta.Point2D( 12500, 15500));
                path.wps.add(new AirSim.Machinetta.Point2D( 13500, 14500));
                path.wps.add(new AirSim.Machinetta.Point2D( 14500, 14500));
                path.wps.add(new AirSim.Machinetta.Point2D( 15500, 13500));
                path.wps.add(new AirSim.Machinetta.Point2D( 16500, 13500));
                path.wps.add(new AirSim.Machinetta.Point2D( 17500, 13500));
                path.wps.add(new AirSim.Machinetta.Point2D( 18500, 13500));
                path.wps.add(new AirSim.Machinetta.Point2D( 19500, 13500));
                path.wps.add(new AirSim.Machinetta.Point2D( 20500, 13500));
                path.wps.add(new AirSim.Machinetta.Point2D( 21500, 13500));
                path.wps.add(new AirSim.Machinetta.Point2D( 22500, 12500));
                path.wps.add(new AirSim.Machinetta.Point2D( 23500, 12500));
                path.wps.add(new AirSim.Machinetta.Point2D( 24500, 12500));
                path.wps.add(new AirSim.Machinetta.Point2D( 25500, 12500));
                path.wps.add(new AirSim.Machinetta.Point2D( 26500, 12500));
                path.wps.add(new AirSim.Machinetta.Point2D( 27500, 12500));
                path.wps.add(new AirSim.Machinetta.Point2D( 28500, 12500));
                path.wps.add(new AirSim.Machinetta.Point2D( 29500, 12500));
                path.wps.add(new AirSim.Machinetta.Point2D( 30500, 12500));
                path.wps.add(new AirSim.Machinetta.Point2D( 31500, 13500));
                path.wps.add(new AirSim.Machinetta.Point2D( 32500, 13500));
                path.wps.add(new AirSim.Machinetta.Point2D( 33500, 13500));
                path.wps.add(new AirSim.Machinetta.Point2D( 34500, 13500));
                path.wps.add(new AirSim.Machinetta.Point2D( 35500, 13500));
                path.wps.add(new AirSim.Machinetta.Point2D( 35500, 14500));
                path.wps.add(new AirSim.Machinetta.Point2D( 36500, 14500));
                path.wps.add(new AirSim.Machinetta.Point2D( 37500, 14500));
                path.wps.add(new AirSim.Machinetta.Point2D( 38500, 14500));
                path.wps.add(new AirSim.Machinetta.Point2D( 39500, 14500));
                path.wps.add(new AirSim.Machinetta.Point2D( 40500, 14500));
                path.wps.add(new AirSim.Machinetta.Point2D( 41500, 15500));
                path.wps.add(new AirSim.Machinetta.Point2D( 42500, 15500));
                path.wps.add(new AirSim.Machinetta.Point2D( 43500, 15500));
                path.wps.add(new AirSim.Machinetta.Point2D( 44500, 16500));
                path.wps.add(new AirSim.Machinetta.Point2D( 45500, 15500));
                path.wps.add(new AirSim.Machinetta.Point2D( 46500, 16500));
                path.wps.add(new AirSim.Machinetta.Point2D( 47500, 15500));
                return path;
            } else if(13119 == x1  && 24805 == y1 && 48000 == x2 && 24805 == y2) {
                
                // 24805 path
                Path2D path = new Path2D(-1, -1, new AirSim.Machinetta.Point2D( 12500, 25500));
                path.wps.add(new AirSim.Machinetta.Point2D( 12500, 25500));
                path.wps.add(new AirSim.Machinetta.Point2D( 13500, 25500));
                path.wps.add(new AirSim.Machinetta.Point2D( 14500, 24500));
                path.wps.add(new AirSim.Machinetta.Point2D( 15500, 24500));
                path.wps.add(new AirSim.Machinetta.Point2D( 16500, 24500));
                path.wps.add(new AirSim.Machinetta.Point2D( 17500, 23500));
                path.wps.add(new AirSim.Machinetta.Point2D( 18500, 23500));
                path.wps.add(new AirSim.Machinetta.Point2D( 19500, 23500));
                path.wps.add(new AirSim.Machinetta.Point2D( 20500, 23500));
                path.wps.add(new AirSim.Machinetta.Point2D( 21500, 23500));
                path.wps.add(new AirSim.Machinetta.Point2D( 22500, 23500));
                path.wps.add(new AirSim.Machinetta.Point2D( 23500, 23500));
                path.wps.add(new AirSim.Machinetta.Point2D( 24500, 23500));
                path.wps.add(new AirSim.Machinetta.Point2D( 25500, 23500));
                path.wps.add(new AirSim.Machinetta.Point2D( 26500, 24500));
                path.wps.add(new AirSim.Machinetta.Point2D( 27500, 24500));
                path.wps.add(new AirSim.Machinetta.Point2D( 28500, 24500));
                path.wps.add(new AirSim.Machinetta.Point2D( 29500, 24500));
                path.wps.add(new AirSim.Machinetta.Point2D( 30500, 24500));
                path.wps.add(new AirSim.Machinetta.Point2D( 31500, 24500));
                path.wps.add(new AirSim.Machinetta.Point2D( 32500, 24500));
                path.wps.add(new AirSim.Machinetta.Point2D( 33500, 23500));
                path.wps.add(new AirSim.Machinetta.Point2D( 34500, 23500));
                path.wps.add(new AirSim.Machinetta.Point2D( 35500, 23500));
                path.wps.add(new AirSim.Machinetta.Point2D( 36500, 24500));
                path.wps.add(new AirSim.Machinetta.Point2D( 37500, 24500));
                path.wps.add(new AirSim.Machinetta.Point2D( 38500, 24500));
                path.wps.add(new AirSim.Machinetta.Point2D( 39500, 24500));
                path.wps.add(new AirSim.Machinetta.Point2D( 40500, 24500));
                path.wps.add(new AirSim.Machinetta.Point2D( 41500, 24500));
                path.wps.add(new AirSim.Machinetta.Point2D( 42500, 24500));
                path.wps.add(new AirSim.Machinetta.Point2D( 43500, 25500));
                path.wps.add(new AirSim.Machinetta.Point2D( 44500, 25500));
                path.wps.add(new AirSim.Machinetta.Point2D( 45500, 25500));
                path.wps.add(new AirSim.Machinetta.Point2D( 46500, 25500));
                path.wps.add(new AirSim.Machinetta.Point2D( 47500, 25500));
                return path;
            } else if(13119 == x1  && 34805 == y1 && 48000 == x2 && 34805 == y2) {
                
                // path 34805
                Path2D path = new Path2D(-1, -1, new AirSim.Machinetta.Point2D( 12500, 35500));
                path.wps.add(new AirSim.Machinetta.Point2D( 12500, 35500));
                path.wps.add(new AirSim.Machinetta.Point2D( 13500, 34500));
                path.wps.add(new AirSim.Machinetta.Point2D( 14500, 34500));
                path.wps.add(new AirSim.Machinetta.Point2D( 15500, 33500));
                path.wps.add(new AirSim.Machinetta.Point2D( 16500, 33500));
                path.wps.add(new AirSim.Machinetta.Point2D( 16500, 32500));
                path.wps.add(new AirSim.Machinetta.Point2D( 17500, 32500));
                path.wps.add(new AirSim.Machinetta.Point2D( 18500, 31500));
                path.wps.add(new AirSim.Machinetta.Point2D( 19500, 31500));
                path.wps.add(new AirSim.Machinetta.Point2D( 20500, 30500));
                path.wps.add(new AirSim.Machinetta.Point2D( 21500, 30500));
                path.wps.add(new AirSim.Machinetta.Point2D( 22500, 30500));
                path.wps.add(new AirSim.Machinetta.Point2D( 23500, 29500));
                path.wps.add(new AirSim.Machinetta.Point2D( 24500, 29500));
                path.wps.add(new AirSim.Machinetta.Point2D( 25500, 29500));
                path.wps.add(new AirSim.Machinetta.Point2D( 26500, 29500));
                path.wps.add(new AirSim.Machinetta.Point2D( 27500, 30500));
                path.wps.add(new AirSim.Machinetta.Point2D( 28500, 29500));
                path.wps.add(new AirSim.Machinetta.Point2D( 29500, 30500));
                path.wps.add(new AirSim.Machinetta.Point2D( 30500, 30500));
                path.wps.add(new AirSim.Machinetta.Point2D( 31500, 30500));
                path.wps.add(new AirSim.Machinetta.Point2D( 32500, 30500));
                path.wps.add(new AirSim.Machinetta.Point2D( 33500, 31500));
                path.wps.add(new AirSim.Machinetta.Point2D( 34500, 31500));
                path.wps.add(new AirSim.Machinetta.Point2D( 34500, 31500));
                path.wps.add(new AirSim.Machinetta.Point2D( 35500, 32500));
                path.wps.add(new AirSim.Machinetta.Point2D( 36500, 32500));
                path.wps.add(new AirSim.Machinetta.Point2D( 37500, 32500));
                path.wps.add(new AirSim.Machinetta.Point2D( 38500, 33500));
                path.wps.add(new AirSim.Machinetta.Point2D( 39500, 33500));
                path.wps.add(new AirSim.Machinetta.Point2D( 40500, 33500));
                path.wps.add(new AirSim.Machinetta.Point2D( 41500, 33500));
                path.wps.add(new AirSim.Machinetta.Point2D( 42500, 34500));
                path.wps.add(new AirSim.Machinetta.Point2D( 43500, 34500));
                path.wps.add(new AirSim.Machinetta.Point2D( 44500, 35500));
                path.wps.add(new AirSim.Machinetta.Point2D( 45500, 35500));
                path.wps.add(new AirSim.Machinetta.Point2D( 46500, 35500));
                path.wps.add(new AirSim.Machinetta.Point2D( 47500, 36500));
                return path;
            } else if(13119 == x1  && 44805 == y1 && 48000 == x2 && 44805 == y2) {
                
                // path 44805
                Path2D path = new Path2D(-1, -1, new AirSim.Machinetta.Point2D( 12500, 45500));
                path.wps.add(new AirSim.Machinetta.Point2D( 12500, 45500));
                path.wps.add(new AirSim.Machinetta.Point2D( 13500, 44500));
                path.wps.add(new AirSim.Machinetta.Point2D( 14500, 43500));
                path.wps.add(new AirSim.Machinetta.Point2D( 14500, 43500));
                path.wps.add(new AirSim.Machinetta.Point2D( 15500, 42500));
                path.wps.add(new AirSim.Machinetta.Point2D( 16500, 41500));
                path.wps.add(new AirSim.Machinetta.Point2D( 16500, 41500));
                path.wps.add(new AirSim.Machinetta.Point2D( 17500, 40500));
                path.wps.add(new AirSim.Machinetta.Point2D( 18500, 39500));
                path.wps.add(new AirSim.Machinetta.Point2D( 19500, 39500));
                path.wps.add(new AirSim.Machinetta.Point2D( 20500, 38500));
                path.wps.add(new AirSim.Machinetta.Point2D( 21500, 38500));
                path.wps.add(new AirSim.Machinetta.Point2D( 22500, 38500));
                path.wps.add(new AirSim.Machinetta.Point2D( 23500, 38500));
                path.wps.add(new AirSim.Machinetta.Point2D( 24500, 38500));
                path.wps.add(new AirSim.Machinetta.Point2D( 25500, 37500));
                path.wps.add(new AirSim.Machinetta.Point2D( 26500, 37500));
                path.wps.add(new AirSim.Machinetta.Point2D( 27500, 37500));
                path.wps.add(new AirSim.Machinetta.Point2D( 28500, 37500));
                path.wps.add(new AirSim.Machinetta.Point2D( 29500, 37500));
                path.wps.add(new AirSim.Machinetta.Point2D( 30500, 37500));
                path.wps.add(new AirSim.Machinetta.Point2D( 31500, 38500));
                path.wps.add(new AirSim.Machinetta.Point2D( 31500, 38500));
                path.wps.add(new AirSim.Machinetta.Point2D( 32500, 38500));
                path.wps.add(new AirSim.Machinetta.Point2D( 33500, 38500));
                path.wps.add(new AirSim.Machinetta.Point2D( 34500, 39500));
                path.wps.add(new AirSim.Machinetta.Point2D( 35500, 39500));
                path.wps.add(new AirSim.Machinetta.Point2D( 36500, 39500));
                path.wps.add(new AirSim.Machinetta.Point2D( 37500, 40500));
                path.wps.add(new AirSim.Machinetta.Point2D( 38500, 40500));
                path.wps.add(new AirSim.Machinetta.Point2D( 39500, 41500));
                path.wps.add(new AirSim.Machinetta.Point2D( 40500, 41500));
                path.wps.add(new AirSim.Machinetta.Point2D( 41500, 42500));
                path.wps.add(new AirSim.Machinetta.Point2D( 42500, 42500));
                path.wps.add(new AirSim.Machinetta.Point2D( 42500, 43500));
                path.wps.add(new AirSim.Machinetta.Point2D( 43500, 43500));
                path.wps.add(new AirSim.Machinetta.Point2D( 44500, 43500));
                path.wps.add(new AirSim.Machinetta.Point2D( 45500, 44500));
                path.wps.add(new AirSim.Machinetta.Point2D( 46500, 45500));
                path.wps.add(new AirSim.Machinetta.Point2D( 47500, 45500));
                return path;
            } else {
                
                Path2D path = new Path2D(-1, -1, new AirSim.Machinetta.Point2D(x1, y1));
                path.wps.add(new AirSim.Machinetta.Point2D(x2, y2));
                return path;
            }
        }
        
        /**
         * Assume top of this list is most important ...
         */
        public LinkedList<NAI> getNAIs(int x1, int y1, int x2, int y2) {
            Machinetta.Debugger.debug("Adding NAIs ", 5, this);
            LinkedList<NAI> nais = new LinkedList<NAI>();
            
            if(!useTerrainAnalysisFlag) {
                for(int loopi=0; loopi < 5; loopi++){
                    Machinetta.Debugger.debug("getNAIS: Adding to list dummy NAI at 0,0 size 50000, 50000", 1, this);
                    nais.add(new NAI("Engage"+(loopi+1), 0, 0, 50000, 50000));
                }
                return nais;
            } else if(offlineTerrainAnalysisFlag) {
                return offlineTANAIs(x1, y1, x2, y2);
            }
            /*
            terrainMod.calculateNAIAndPath(new Point2D.Double((double)x1,(double)y1),new Point2D.Double((double)x2,(double)y2));
            LinkedList<MapObject> currEAs = terrainMod.getMostRecentEAS();
            Object[] EAS = currEAs.toArray();
            for(int i=0; i<EAS.length; i++){
                MapObject mo = (MapObject)EAS[i];
                // TODO: SRO Tue Oct 11 21:37:19 EDT 2005
                //
                // We multiply EA positions by 50 here - this is because
                // they are stored inside the terrain analysis as grid
                // coordinates - this really should be done inside of the
                // TerrainAnalysisModule, since we can multiply by the value
                double posx = mo.getPosX() * InferenceDriverNOGUI.CIRCUIT_RES_X;
                double posy = mo.getPosY() * InferenceDriverNOGUI.CIRCUIT_RES_Y;
                
                Machinetta.Debugger.debug("getNAIS: Adding to list NAI at "+posx+","+posy, 1, this);
                nais.add(new NAI("Engage"+(i+1), posx, posy,  posx+4000.0,  posy+4000.0));
            }
            */
            Machinetta.Debugger.debug("Done adding NAIs ", 5, this);
            
            return nais;
        }
        
        public Path2D getPath(int x1, int y1, int x2, int y2) {
            if(!useTerrainAnalysisFlag) {
                Path2D path = new Path2D(-1, -1, new AirSim.Machinetta.Point2D(x1, y1));
                path.wps.add(new AirSim.Machinetta.Point2D(x2, y2));
                return path;
            } else if(offlineTerrainAnalysisFlag) {
                return offlineTAPath(x1,y1,x2,y2);
            }
            
            /*
            terrainMod.calculateNAIAndPath(new Point2D.Double((double)x1,(double)y1),new Point2D.Double((double)x2,(double)y2));
            
            Point[] points = terrainMod.getMostRecentPath();
            
            Path2D path = new Path2D(-1, -1, new AirSim.Machinetta.Point2D(points[0].x* InferenceDriverNOGUI.CIRCUIT_RES_X,
                    points[0].y* InferenceDriverNOGUI.CIRCUIT_RES_Y));
            for(int loopi = 1; loopi < points.length; loopi++) {
                double posx = points[loopi].x * InferenceDriverNOGUI.CIRCUIT_RES_X;
                double posy = points[loopi].y * InferenceDriverNOGUI.CIRCUIT_RES_Y;
                path.add(new AirSim.Machinetta.Point2D((int)posx, (int)posy));
            }
            Machinetta.Debugger.debug("getPath: Returning path="+path, 5, this);
            return path;
             */
            return null;
        }
    }
    
    DummyTerrainAnalysis ta = null;
    
    /** Creates a new instance of ProxyInterface */
    public ProxyInterface() {
        String useTerrainAnalysis = (String) Configuration.allMap.get("USE_TERRAIN_ANALYSIS");
        if(null == useTerrainAnalysis) {
            Debugger.debug("USE_TERRAIN_ANALYSIS is not in Configuration.allMap, can't read USE_TERRAIN_ANALYSIS flag.", 5, this);
        } else {
            if(useTerrainAnalysis.equalsIgnoreCase("yes")
            || useTerrainAnalysis.equalsIgnoreCase("true")
            || useTerrainAnalysis.equalsIgnoreCase("1"))
                useTerrainAnalysisFlag = true;
            else
                useTerrainAnalysisFlag = false;
        }
        String offlineTerrainAnalysis = (String) Configuration.allMap.get("OFFLINE_TERRAIN_ANALYSIS");
        if(null == offlineTerrainAnalysis) {
            Debugger.debug("OFFLINE_TERRAIN_ANALYSIS is not in Configuration.allMap, can't read OFFLINE_TERRAIN_ANALYSIS flag.", 5, this);
        } else {
            if(offlineTerrainAnalysis.equalsIgnoreCase("yes")
            || offlineTerrainAnalysis.equalsIgnoreCase("true")
            || offlineTerrainAnalysis.equalsIgnoreCase("1"))
                offlineTerrainAnalysisFlag = true;
            else
                offlineTerrainAnalysisFlag = false;
        }
        
        Debugger.debug("USE_TERRAIN_ANALYSIS is set to "+useTerrainAnalysisFlag, 2, this);
        Debugger.debug("OFFLINE_TERRAIN_ANALYSIS is set to "+offlineTerrainAnalysisFlag, 2, this);
        Machinetta.Debugger.debug("Creating DummyTerrainAnalysis ", 0, this);
        ta = new DummyTerrainAnalysis(useTerrainAnalysisFlag,offlineTerrainAnalysisFlag);
        Machinetta.Debugger.debug("Done creating DummyTerrainAnalysis "+ta, 0, this);
    }
    
    /**
     * Probably not required, but needed to implement RAPInterfaceImplementation
     */
    public void run() {
        Machinetta.Debugger.debug("Not implemented (run) ", 3, this);
    }
    
    /** Sends a message to the RAP
     *
     * @param msg The message to send
     */
    public void sendMessage(OutputMessage msg) {
        Machinetta.Debugger.debug("In sendMessage ", 0, this);
        if (msg instanceof NewRoleMessage) {
            Machinetta.Debugger.debug("    sending NewRoleMessage ", 0, this);
            RoleBelief role = ((NewRoleMessage)msg).getRole();
            if (role instanceof BasicRole) {
                Machinetta.Debugger.debug("        sending BasicRole ", 0, this);
                BasicRole basic = (BasicRole)role;
                int x1 = ((Integer)basic.params.get("x1")).intValue();
                int y1 = ((Integer)basic.params.get("y1")).intValue();
                int x2 = ((Integer)basic.params.get("x2")).intValue();
                int y2 = ((Integer)basic.params.get("y2")).intValue();
                switch (basic.getType()) {
                    case determineEngagementAreas:
                        
                        // Here is the line of code where you actually work out the NAIs
                        // Might be smarter, if the call will take a while
                        // to put this out in a thread ....
                        LinkedList<NAI> nais = ta.getNAIs(x1, y1, x2, y2);
                        
                        // Now send them back.  As long as there are the
                        // correct amount, no changes required here
                        try {
                            if(null == nais) {
                                Machinetta.Debugger.debug("ERROR:  No (zero) NAIs returned by getNAIs("+x1+","+y1+","+x2+","+y2+")",5, this);
                            } else {
                                ListIterator<NAI> naiLi = nais.listIterator();
                                for (DirectedInformationRequirement dir: basic.infoSharing) {
                                    NAI nai = naiLi.next();
                                    nai.id = dir.proscribedID;
                                    state.addBelief(nai);
                                }
                                Machinetta.Debugger.debug("NAIs were successfully added to the state", 1, this);
                            }
                            state.notifyListeners();
                        } catch (Exception e) {
                            Machinetta.Debugger.debug("Problem delivering information from TA: " + e, 5, this);
                            e.printStackTrace();
                        }
                        break;
                        
                    case determineLeastResistancePath:
                        Path2D path = ta.getPath(x1, y1, x2, y2);
                        Machinetta.Debugger.debug("Asked to determine path from (" + x1 + "," + y1 + ") to (" + x2 + "," + y2 + ")", 1, this);
                        // Assumes that first (and only) dir is the request for the path
                        DirectedInformationRequirement dir = basic.infoSharing.get(0);
                        path.id = dir.proscribedID;
                        Machinetta.Debugger.debug("Set path id to " + path.id, 1, this);
                        state.addBelief(path);
                        state.notifyListeners();
                        break;
                        
                    default:
                        Machinetta.Debugger.debug("Trying to give terrain analysis unperformable role", 3, this);
                }
            } else {
                Machinetta.Debugger.debug("        not a BasicRole, don't know what to do", 5, this);
            }
        } else {
            Machinetta.Debugger.debug("Unhandled output message: " + msg, 1, this);
        }
    }
    
    /** Called to get list of new messages
     * Should return only those messages received since last called.
     *
     * @return List of InputMessage objects received from RAP since last called.
     */
    public InputMessage [] getMessages() {
        Machinetta.Debugger.debug("Not implemented (getMessages)", 3, this);
        return null;
    }
    
    
    public static void main(String argv[]) {
        ProxyInterface pi = new ProxyInterface();
        Hashtable<String, Integer> params = new Hashtable<String, Integer>();
        params.put("x1", 10000);
        params.put("y1", 10000);
        params.put("x2", 20000);
        params.put("y2", 20000);
        
        System.out.println("Request for NAIs");
        BasicRole basic = new BasicRole(TaskType.determineEngagementAreas);
        basic.params = params;
        
        pi.sendMessage(new NewRoleMessage(basic));
        
        System.out.println("Request for path");
        basic = new BasicRole(TaskType.determineLeastResistancePath);
        basic.params = params;
        
        pi.sendMessage(new NewRoleMessage(basic));
        
    }
    
}
