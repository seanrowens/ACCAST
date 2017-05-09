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
 * StateGarbageCollection.java
 *
 * Created on January 30, 2008, 5:20 PM
 *
 */

package Machinetta.State;

import Machinetta.State.BeliefType.Belief;
import Machinetta.State.BeliefType.IntegerBelief;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.PriorityQueue;

/**
 *
 * A belief type creates a specification for removing garbage.
 *
 * This only triggers on state changes.
 *
 * @author pscerri
 */
public class StateGarbageCollection implements StateChangeListener {
    
    private ProxyState state = new ProxyState();
    
    /** Notice that only one of count or timed is allowed per class. */
    private Hashtable<Class, Belief> actions = new Hashtable<Class, Belief>();
    
    /** Hashtable of things to be counted and deleted */
    private Hashtable<StateGCCount, LinkedList<Object>> counts = new Hashtable<StateGCCount, LinkedList<Object>>();
    
    /** Queue of timed deletes */
    private PriorityQueue<TimedDelete> timeQ = new PriorityQueue<TimedDelete>();
    
    TimedDeleteThread tdt = null;
    
    /** Creates a new instance of StateGarbageCollection */
    public StateGarbageCollection() {
        state.addChangeListener(this);
        
        tdt = new TimedDeleteThread();
        tdt.start();
        
        // Test
        state.ready();
        
        /*  Timed delete test
        StateGarbageCollection.StateGCTimed bel = new StateGarbageCollection.StateGCTimed(IntegerBelief.class, 3000);
        IntegerBelief ib = new IntegerBelief(new BeliefNameID("TestInt1"), 1);
         
        state.addBelief(bel);
        state.notifyListeners();
         
        state.addBelief(ib);
        state.notifyListeners();
         
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {}
         
        IntegerBelief ib2 = new IntegerBelief(new BeliefNameID("TestInt2"), 2);
        state.addBelief(ib2);
        state.notifyListeners();
         */
        
        /* Count delete test         
        StateGCCount bel = new StateGCCount(IntegerBelief.class, 2);
        IntegerBelief ib = new IntegerBelief(new BeliefNameID("TestInt1"), 1);
        IntegerBelief ib2 = new IntegerBelief(new BeliefNameID("TestInt2"), 2);
        IntegerBelief ib3 = new IntegerBelief(new BeliefNameID("TestInt3"), 3);
        IntegerBelief ib4 = new IntegerBelief(new BeliefNameID("TestInt3"), 4);
        
        state.addBelief(bel);
        state.notifyListeners();
        
        state.addBelief(ib);
        state.notifyListeners();
        
        state.addBelief(ib2);
        state.notifyListeners();
        
        state.addBelief(ib3);
        state.notifyListeners();
        
        state.addBelief(ib4);
        state.notifyListeners();
        */
    }
    
    public void stateChanged(BeliefID[] bids) {
        for (BeliefID bid: bids) {
            Belief b = state.getBelief(bid);
            
            if (b instanceof StateGCCount) {
                actions.put(((StateGCCount)b).getC(), b);
                counts.put((StateGCCount)b, new LinkedList<Object>());
            } else if (b instanceof StateGCTimed) {
                actions.put(((StateGCTimed)b).getC(), b);
            } else {
                Class bc = b.getClass();
                Belief action = actions.get(bc);
                if (action != null) {
                    if (action instanceof StateGCCount) {
                        
                        LinkedList<Object> l = counts.get((StateGCCount)action);
                        l.add(b);
                        
                        if (l.size() > ((StateGCCount)action).getCount()) {
                            Belief bRemove = (Belief)l.removeFirst();
                            state.removeBelief(bRemove.getID());
                            
                            Machinetta.Debugger.debug(1, "Removed due to count: " + bRemove);
                        }
                        
                        
                    } else if (action instanceof StateGCTimed) {
                        
                        long time = System.currentTimeMillis() + ((StateGCTimed)action).getTime();
                        TimedDelete td = new TimedDelete(bid, time);
                        timeQ.offer(td);
                        
                        // Interupt thread for monitoring this queue
                        tdt.interrupt();
                    }
                }
            }
        }
    }
    
    public class StateGCCount extends Belief {
        
        private Class c = null;
        private int count = 0;
        
        public StateGCCount(Class c, int count) {
            this.c = c;
            this.count = count;
        }
        
        public BeliefID makeID() {
            return new BeliefNameID(c + ":" + count);
        }
        
        public int getCount() {
            return count;
        }
        
        public Class getC() {
            return c;
        }
        
    }
    
    public class StateGCTimed extends Belief {
        
        private Class c = null;
        private long time = 0;
        
        public StateGCTimed(Class c, long time) {
            this.c = c;
            this.time = time;
        }
        
        public BeliefID makeID() {
            return new BeliefNameID(c + ":" + time);
        }
        
        public Class getC() {
            return c;
        }
        
        public long getTime() {
            return time;
        }
        
    }
    
    private class TimedDelete implements Comparable {
        
        private BeliefID bid = null;
        private long time = 0L;
        
        public TimedDelete(BeliefID bid, long time) {
            this.bid = bid;
            this.time = time;
        }
        
        public int compareTo(Object o) {
            if (!(o instanceof TimedDelete)) return 0;
            return ((TimedDelete)o).time > time? -1 : 1;
        }
        
        private long getTime() {
            return time;
        }
        
        private BeliefID getBID() {
            return bid;
        }
    }
    
    private class TimedDeleteThread extends Thread {
        public void run() {
            while (true) {
                TimedDelete td = timeQ.peek();
                long sleepTime = Long.MAX_VALUE;
                if (td != null) {
                    sleepTime = (td.getTime() - System.currentTimeMillis());
                }
                try {
                    System.out.println("Waiting for: " + sleepTime);
                    sleep(sleepTime);
                } catch (InterruptedException e) {
                    System.out.println("Interrupted!");
                }
                
                td = timeQ.poll();
                if (td != null) {
                    if (td.getTime() < System.currentTimeMillis()) {
                        System.out.println("Before");
                        state.printState();
                        state.removeBelief(td.getBID());
                        System.out.println("After");
                        state.printState();
                    } else {
                        timeQ.offer(td);
                    }
                }
            }
        }
    }
    
    public static void main(String argv[]) {
        new StateGarbageCollection();
        
    }
}
