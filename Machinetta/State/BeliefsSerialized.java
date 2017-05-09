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
 * BeliefsSerialized.java
 *
 * Created on April 17, 2007, 1:02 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package Machinetta.State;

import Machinetta.State.BeliefType.Belief;
import Machinetta.Debugger;
import java.util.ArrayList;
import java.io.*;

/**
 * This class provides functionality to read and write serialized belief files 
 * using Java object IO streams.  Note that errors are printed directly to the 
 * Machinetta debugger, and are not thrown.
 * @author pkv
 */
public class BeliefsSerialized {    
    /**
     * Opens specified file and reads out serialized belief objects until EOF or an 
     * error occurs, and returns these beliefs in array form.
     * @param fileName A string containing the path to the file that will be read.
     * @return An array of belief objects deserialized from the file.
     */
    public static Belief[] readBeliefs(String fileName) {
        ArrayList<Belief> beliefList = new ArrayList<Belief>();
        
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fileName));
            Machinetta.Debugger.debug(1, "Reading serialized beliefs: " + fileName);

            Object o = null;
            do {                    
                try {
                    o = ois.readObject();           
                } catch (EOFException ex) {
                    o = null;
                } catch (ClassNotFoundException ex) {
                    Machinetta.Debugger.debug(4, "Failed to read serialized object: " + ex);
                    o = null;
                } catch (IOException ex) {
                    Machinetta.Debugger.debug(4, "Failed to read serialized object: " + ex);
                    o = null;
                }
                if (o != null && o instanceof Belief) {
                    Machinetta.Debugger.debug(1, "Reading belief from file: " + o);
                    beliefList.add((Belief)o);
                } else if (o != null) {
                    Machinetta.Debugger.debug(3, "Deserialized something other than belief: " + o.getClass() + " " + o);
                }
            } while (o != null);

            ois.close();
        } catch (FileNotFoundException ex) {
            Machinetta.Debugger.debug(5, "Could not load serialized beliefs: " + ex);
        } catch (IOException ex) {
            Machinetta.Debugger.debug(5, "Could not load serialized beliefs: " + ex);
        }
        
        return beliefList.toArray(new Belief[beliefList.size()]);
    }
    
    /**
     * Opens specified file and reads out serialized belief objects until EOF or an 
     * error occurs, and adds these beliefs to the provided ProxyState object.
     * @param fileName A string containing the path to the file that will be read.
     * @param state A ProxyState to which the beliefs from the file will be added.
     */
    public static void readBeliefs(String fileName, ProxyState state) {
        Belief[] beliefArray = readBeliefs(fileName);
        for (int i = 0; i < beliefArray.length; i++) {
            Belief b = beliefArray[i];
            state.addBelief(beliefArray[i]);
            Machinetta.Debugger.debug(1, "Added belief to state: " + b);
        }
    }
    
    /**
     * Opens specified file and writes out serialized belief objects until the given
     * array is completely written to file or an error occurs.
     * @param fileName A string containing the path to the file that will be written.
     * @param beliefArray An array of belief objects that will be serialized to the file.
     */
    public static void writeBeliefs(String fileName, Belief[] beliefArray) {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fileName));
            Machinetta.Debugger.debug(1, "Writing serialized beliefs: " + fileName);
            
            for (int i = 0; i < beliefArray.length; i++) {
                Belief b = beliefArray[i];
                if (b != null) {
                    oos.writeObject(b);
                }
            }
            oos.flush();
            oos.close();
        } catch (FileNotFoundException ex) {
            Machinetta.Debugger.debug(5, "Problem writing beliefs: " + ex);
            ex.printStackTrace();
        } catch (IOException ex) {
            Machinetta.Debugger.debug(5, "Problem writing beliefs: " + ex);
            ex.printStackTrace();
        }
    }
    
    /**
     * Opens specified file and writes out serialized belief objects for every belief 
     * in the given ProxyState or until an error occurs.
     * @param fileName A string containing the path to the file that will be written.
     * @param state A ProxyState from which all beliefs will be written to file.
     */
    public static void writeBeliefs(String fileName, ProxyState state) {
        BeliefID[] beliefs = state.getAllBeliefs();
        Belief[] beliefArray = new Belief[beliefs.length];
        
        for (int i = 0; i < beliefs.length; i++) {
            Belief b = state.getBelief(beliefs[i]);
            beliefArray[i] = b;
            Machinetta.Debugger.debug(1, "Extracted belief from state: " + b);
        }
    }
}
    
