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
 * IndexedIntCount.java
 *
 * Created on December 12, 2006, 10:34 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package Util.Counting;

import Util.Pair;
//import TokenSim.Experiment.Statistics.StatsPrinter;
import java.util.Vector;

/**
 *
 * @author gregor
 */
public class IndexedIntCount extends IndexedCount{
    /* ttl Count keeps track of the sum, cCount is the current change */
    private int cCount, ttlCount;
    
    // private Vector<Pair<Integer, Integer>> runHistory;
    
    public IndexedIntCount(){
        super.initBase();
        initBase();
    }
    protected void initBase() {
        cCount = 0;
        ttlCount = 0;
    }
    
    public int addValue(int index, int value) {
        incrementIndex(index);
        cCount += value;
        ttlCount += value;
        return cCount;
    }
    
    /* @ deprecated */
    public int increment(int value) {
        addValue(cIndex, value);
        return cCount;
    }
    
    public boolean isEmpty() {
        if(ttlCount == 0)
            return true;
        return false;
    }
    
    public Number getTotal() {
        return (Number)ttlCount;
    }
    
    public int getIntTotal() {
        return ttlCount;
    }
    
    /*
    protected boolean printPair(Pair p, StatsPrinter out) {
        try{
            Pair<Integer, Integer> q = p;
            out.printIndexedValue(q.getFirst(), q.getSecond());
            return true;
        }catch(ClassCastException e) {
            return false;
        } 
    }*/
    
    
    
    protected void commitCount() {
        Pair<Integer, Integer> p = new Pair(cIndex, cCount);
        runHistory.add(p);
        cCount = 0;
    }
    
    protected boolean isFinalized() {
        if(cCount == 0)
            return true;
        return false;
    }

    /*
    public void printValue(StatsPrinter out) {
        out.printTotal(ttlCount);
    }*/
    
}

