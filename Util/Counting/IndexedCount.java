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
 * IndexedCount.java
 *
 * Created on September 13, 2006, 1:29 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package Util.Counting;

//import TokenSim.Experiment.Statistics.StatsPrinter;
import Util.Pair;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Vector;

/**
 *
 * @author gregor
 */
public abstract class IndexedCount extends Count{
    /* This variable captures the current Index */
    protected int cIndex;
    protected Vector<Pair> runHistory;
    
    /*
    public boolean printIndexedValues(StatsPrinter out) {
        Enumeration<Pair> e = runHistory.elements();
        while(e.hasMoreElements()) {
            Pair p = e.nextElement();
            if(!printPair(p, out))
                return false;
        }
        return true;
    }*/
    
    /* To be called after the run is completed. */
    public boolean finalizeCollection(int index) {
        if(cIndex > index)
            return false;
        
        if(isFinalized()) {
            commitCount();
        }
        /* add final zero if needed */
        if(index > cIndex) {
            cIndex = index;
            commitCount();
        }
        return true;
    }
    
    public abstract boolean isEmpty();
    /* Sends the value to the statsprinter */
    //protected abstract boolean printPair(Pair p, StatsPrinter out);
    /* checks for hanging count*/
    protected abstract boolean isFinalized();
    /* commits a count to the vector */
    protected abstract void commitCount();
    
    protected void initBase() {
        cIndex = 0;
        runHistory = new Vector<Pair>();
    }
    /** Increments the index and commits the Count, iff the parameter is
     *  larger than the current index.
     *  @param index The new index.
     */
    protected int incrementIndex(int index) {
        if(index > cIndex) {
            commitCount();
            cIndex = index;
        }
        return cIndex;
    }
    
    /* If there are more values that can be retrieved */
    protected boolean hasMoreValues() {
        if(runHistory.size() > 0)
            return true;
        return false;
    }
    
    /**
     * @return the maximum index for this statistic
     */
    public int getMaxIndex() {
        if(!isFinalized())
            return -1;
        else
            return cIndex;
    }

    public int size() {
        return runHistory.size();
    }
    
    
}
