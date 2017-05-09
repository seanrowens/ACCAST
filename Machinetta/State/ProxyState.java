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
 * ProxyState.java
 *
 * Created on 4 July 2002, 00:02
 */

package Machinetta.State;

import Machinetta.State.BeliefType.*;

import java.util.Vector;
import java.io.*;
import java.util.ArrayList;

/**
 * Singleton, so that objects can safely declare instances
 *
 * @author  scerri
 */
public class ProxyState {
    
    /** Creates a new instance of ProxyState
     */
    public ProxyState() {
        // Create instance, if not already created
        if (impl == null) { init(); }
    }
    
    /**
     * This will either read in beliefs specified in XML or serialized,
     * depending on the extension (.xml for XML, .blf for serialized)
     */
    public ProxyState(String fileName) {
        this();
        Machinetta.Debugger.debug( 1,"Reading belief file : " + fileName);
        
        if (fileName.endsWith(".xml")) {
            Belief [] toAdd = BeliefsXML.getBeliefs(fileName);
            if (toAdd == null) {
                Machinetta.Debugger.debug( 5,"Could not parse beliefs file : " + fileName);
            } else {
                for (int i = 0; i < toAdd.length; i++) {
                    if (toAdd[i] != null) {
                        Machinetta.Debugger.debug( 0,"Adding " + toAdd[i].toString() + " to beliefs");
                        addBelief(toAdd[i]);
                    }
                }
            }
        } else if (fileName.endsWith(".blf")) {
            
            try {
                Machinetta.Debugger.debug(1, "Ready to read serialized beliefs from: " + fileName);
                FileInputStream fis = new FileInputStream(fileName);
                Machinetta.Debugger.debug(1, "Got file input stream");
                ObjectInputStream is = new ObjectInputStream(fis);
                Machinetta.Debugger.debug(1, "Got object input stream");
                // Machinetta.Debugger.debug("Reading serialized beliefs, bytes: " + is.available(), 1, this);                               
                
                Object o = null;
                do {                    
                    try {
                        o = is.readObject();           
                    } catch (EOFException ex) {
                        o = null;
                    } catch (ClassNotFoundException ex) {
                        Machinetta.Debugger.debug( 4,"Failed to read serialized object: " + ex);
                    } catch (IOException ex) {
                        Machinetta.Debugger.debug( 4,"Failed to read serialized object: " + ex);
                    }
                    if (o != null && o instanceof Belief) {
                        Machinetta.Debugger.debug( 1,"Added to beliefs: " + o);
                        addBelief((Belief)o);
                    } else if (o != null) {
                        Machinetta.Debugger.debug( 3,"Deserialization read something other than belief: " + o.getClass() + " " + o);
                    }
                } while (o != null);
                
            } catch (FileNotFoundException ex) {
                Machinetta.Debugger.debug( 5,"Could not load serialized beliefs: " + ex);
            } catch (IOException ex) {
                Machinetta.Debugger.debug( 5,"Could not load serialized beliefs: " + ex);
                ex.printStackTrace();
            }
        }
    }
    
    /*******************************************************************
     * MAIN ACCESS METHODS
     *******************************************************************/
    
    /** Get a belief from store */
    public Belief getBelief(BeliefID id) {
        try {
            Belief b = impl.getBelief(id);
            return (Belief)((Belief)impl.getBelief(id)).makeClone();
        } catch (NullPointerException e) {
            Machinetta.Debugger.debug( 0,"Could not find belief : " + id);
            //impl.printState();
            return null;
        }
    }
    
    /** Returns all of the beliefs in the current proxy state
     *  @return Array of all current beliefs
     */
    public BeliefID [] getAllBeliefs() {
        return impl.getAllBeliefs();
    }
    
    /** Add a belief to the beliefState
     * @param b The belief to be added
     */
    public void addBelief(Belief b) {
        
        if (b == null) return;
        if (b.id == null) { b.id = b.makeID(); }
        impl.addBelief(b);
        if (!changed.contains(b.getID()))
            changed.addElement(b.getID());
        
        /*
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < changed.size(); i++) {
            sb.append(changed.elementAt(i) + " ");
        }
        Machinetta.Debugger.debug( 1,"Changed : " + new String(sb));
         */
    }
    
    /** Get all RAP beliefs */
    public RAPBelief[] getAllRAPBeliefs() {
        return impl.getAllRAPBeliefs();
    }
    
    /** Returns the RAPBelief object for the RAP with a given ProxyID
     *
     * @param id ProxyID object for required RAPBelief
     */
    public RAPBelief getRAPBelief(ProxyID id) {
        return impl.getRAPBelief(id);
    }
    
    /** Returns a RAPBelief object of self
     */
    public RAPBelief getSelf() {
        return impl.getSelf();
    }
    
    /** Takes a belief out of state ...
     * Use of this method should be avoid where possible.
     * The problem is that listeners do not get to hear about it.
     */
    public void removeBelief(BeliefID bid) {
        impl.removeBelief(bid);
        changed.removeElement(bid);
    }
    
    public void removeAll() {
        impl.removeAll();
    }
    
    public void printState() {
        BeliefID [] bs = this.getAllBeliefs();
        Machinetta.Debugger.debug( 1,"State:");
	if(null != bs) {
	    for (int i = 0; i < bs.length; i++) {
		Machinetta.Debugger.debug( 1,"B: " + bs[i]);
	    }
	}
        Machinetta.Debugger.debug( 1,"End State");
    }
    
    /*******************************************************************
     * END MAIN ACCESS METHODS
     *******************************************************************/
    
    /** Notify listeners may not be called until this is true */
    private static boolean ready = false;
    
    /** Indicate that ProxyState can begin to notify listeners of changed beliefs
     * Including beliefs read in from XML.
     */
    public static void ready() { ready = true; }
    
    /** Allows checking with state to see if ready */
    public static boolean isReady() { return ready; }
    
    /** Notify listeners that beliefs have changed
     * Keep calling itself until nothing changes
     */
    static int threadsIn = 0;
    public static synchronized void notifyListeners() {
        if (!ready) {
            Machinetta.Debugger.debug(1,"Notify listeners called too early");
            return;
        }
        
        if (threadsIn > 0) { Machinetta.Debugger.debug( 0,"synch problem"); }
        // If nothing changed, no need to notify
        if (changed.size() == 0) return;
        
        threadsIn++;
        //Machinetta.Debugger.debug ("ENTERED notifyListeners " + threadsIn,1,"ProxyState");
        
        // Create an array of BeliefIDs of changed Beliefs
        BeliefID [] ids = new BeliefID[changed.size()];
        for (int i = 0; i < ids.length; i++) {
            ids[i] = (BeliefID)changed.elementAt(i);
        }
        changed.clear();
        // Notify
        for (int i = 0; i < noListeners; i++) {            
            listeners.get(i).stateChanged(ids);
        }
        
        //Machinetta.Debugger.debug ("EXITED notifyListeners " + threadsIn,1,"ProxyState");
        threadsIn--;
        
        // If there were changes in response to changes, let everyone know
        if (changed.size() != 0) {
            Machinetta.Debugger.debug(0,"Recursive call");
            notifyListeners();
        }
        
    }
    
    /** Add a change listener
     * @param l Listener to be added
     */
    public void addChangeListener(StateChangeListener l) {
        Machinetta.Debugger.debug( 1,"Listener " + l + " added");
        listeners.add(l);
        noListeners++;
    }

    public void removeChangeListener(StateChangeListener l) {
        Machinetta.Debugger.debug( 1,"Listener " + l + " removed");
        listeners.remove(l);
        noListeners--;
    }
    
    /**
     * Creates implementation as specified in Configuration
     */
    private void init() {
        String implType = Machinetta.Configuration.STATE_IMPLEMENTATION_TYPE;
        if (implType.equalsIgnoreCase("HASHTABLE")) {
            impl = new HashtableImplementation();
        } else {
            Machinetta.Debugger.debug( 3,"Unknown Proxy State type implementation requested");
            Machinetta.Debugger.debug( 3,"Creating default HASHTABLE implementation");
            impl = new HashtableImplementation();
        }
        
        if (Machinetta.Configuration.allMap.get("USE_GARBAGE_COLLECTION") != null) {
            new StateGarbageCollection();
        }
        
        new BeliefLogger();
    }
    
    /** Contains the implementation of this ProxyState - singleton */
    static private ProxyStateImplementation impl = null;
    
    /** List of listeners to this implementation
     *
     * @todo Change this to an ArrayList
     */
    static private ArrayList<StateChangeListener> listeners = new ArrayList<StateChangeListener>();
    
    /** Number of objects listening to this ProxyState */
    static private int noListeners = 0;
    
    /** The BeliefIDs of beliefs that have changed since last listeners
     * were notified.
     */
    static private Vector<BeliefID> changed = new Vector<BeliefID> ();
    
}
