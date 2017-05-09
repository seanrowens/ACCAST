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

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * 
 * This class contains the methods for writing configuration files periodically.
 * Each parameter valus in SimConfiguration is changed with some strategies. * 
 * @author junyounk
 */
public class ConfigurationOperator extends Thread {
    private boolean needToWrite = false;
    private int count = 0;
    private int freq = 1000;
    
    /**
     * Changes the parameter values of SimConfiguration
     */
    public void changeParameters() {
        
    }
    
    /**
     * Writes the current SimConfiguration content to the file
     */
    public void writeConfigurationInFile() {
        String fileName = "Operator" + System.currentTimeMillis() + ".cfg";
        
    }
    
    /**
     * Needed to implement a Thread
     */
    public void run()
    {
        while(true) {
            if((count % freq) == 0) {
                changeParameters();
                needToWrite = true;
            } else {
                try {
                    count++;
                    sleep(1000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(ConfigurationOperator.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            if(needToWrite) {
                writeConfigurationInFile();
                needToWrite = false;
            }
        }        
        
    }
     
}
