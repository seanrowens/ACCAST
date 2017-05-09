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
 * AAImplementation.java
 *
 * Created on 11 July 2002, 15:16
 */

package Machinetta.AA;

import Machinetta.Configuration;
import Machinetta.AA.AAModule;
import Machinetta.AA.RoleAssignment.RoleAssignmentAA;
import Machinetta.AA.RoleAssignment.RoleAssignmentThread;
import Machinetta.RAPInterface.InputMessages.*;
import Machinetta.RAPInterface.OutputMessages.*;
import Machinetta.State.*;
import Machinetta.State.BeliefType.Belief;
import Machinetta.State.BeliefType.ResourceNeedBelief;
import Machinetta.State.BeliefType.TeamBelief.*;

import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;

/**
 * Super class of all AAImplementations, shows basic functionality required
 *
 * @author  scerri
 */
public abstract class AAImplementation {
    
    /** Creates a new instance of AAImplementation */
    public AAImplementation() {
        AAimpl = this;
    }
    
    /** This method should be overridden by applicable implemenations
     *
     * A request should be placed with the RAP and the result
     * eventually posted to the state.
     */
    public void requestCapability(RoleBelief role) {
        Machinetta.Debugger.debug(3,"Unimplemented request capability");
    }
    
    /**
     * Request the proxy's need for this resource.
     *
     * This may be called even after the proxy has the resource, if it hurts to lose the resource
     * this should return a high number.
     */
    public int requestResourceNeed(ResourceBelief bel) {        
        Machinetta.Debugger.debug( 1,"Unimplemented requestResourceNeed");
        return 0;
    }
    
    /**
     * Resource is given.
     */
    public void givenResource(ResourceBelief bel) {
        Machinetta.Debugger.debug( 1,"Unimplemented given belief");
    }
    
    /**
     * Resource is taken back.
     */
    public void removeResource(ResourceBelief bel) {
        Machinetta.Debugger.debug( 1,"Unimplemented removeResource");
    }
    
    /** Called by proxy when it knows messages have arrived from RAP
     *
     * Default Implementation uses inputMessageHandler objects
     *
     * @param msgs Messages arrived since last called
     */
    public void messagesFromRAP(InputMessage [] msgs) {
        for (int i = 0; i < msgs.length; i++) {
            Class cls = msgs[i].getClass();
            Object val = null;
            
            // Search for superclass until we find the message handler
            // Unlike the belief, only the first mached message handler
            // will be called.
            //                              -- changed by Jijun Wang
            while (cls!=null && val==null) {
                val = inputMsgHandlers.get(cls);
                cls = cls.getSuperclass();
            }
            // end -- Jijun Wang
            
            if (val == null) {
                Machinetta.Debugger.debug( 1,"No handler for " + msgs[i].getClass());
                return;
            } else if (val instanceof InputMessageHandler) {
                InputMessageHandler h = (InputMessageHandler)val;
                h.handleMessage(msgs[i]);
            } else if (val instanceof Vector) {
                Vector v = (Vector)val;
                for (int j = 0; j < v.size(); j++) {
                    ((InputMessageHandler)v.elementAt(j)).handleMessage(msgs[i]);
                }
            }
        }
    }
    
    /** Called by implementation to send messages to RAP <br>
     *
     * Made public so RoleAssignment thread can access, not intended for general access. <br>
     *
     * @param msg The OutputMessage to send
     */
    public void messageToRAP(OutputMessage msg) {
        parent.messageToRAP(msg);
    }
    
    /** Handle changes to proxy state
     *
     * @param b An array of ids of beliefs that have changed.
     */
    static int threadsIn = 0;
    public void proxyStateChange(BeliefID [] b) {
        threadsIn++;
        
        // Prints a message if this is entered before being exited by previous call
        // Potentially a problem but seems to actually happen often without breaking!?
        if (threadsIn > 1) Machinetta.Debugger.debug( 0,"Warning interleaved proxyStateChange calls");
        
        /*
        System.out.println("\n ********************* Changed Messages \n");
        for (int i = 0; i < b.length; i++) {
            Belief belief = state.getBelief(b[i]);
            System.out.println(belief.toString());
        }
        System.out.println("\n ********************* End \n");
         */
        
        for (int i = 0; i < b.length; i++) {
            Belief belief = state.getBelief(b[i]);
            if (belief != null) {
                Class msgClass = belief.getClass();
                do {
                    Object val = bcHandlers.get(msgClass);
                    if (val == null) {
                        Machinetta.Debugger.debug( 0,"AA No handler for " + belief.getClass());
                    } else if (val instanceof BeliefChangeHandler) {
                        BeliefChangeHandler h = (BeliefChangeHandler)val;
                        h.beliefChange(belief);
                    } else {
                        Vector hs = (Vector)val;
                        for (int j = 0; j < hs.size(); j++) {
                            BeliefChangeHandler h = (BeliefChangeHandler)hs.elementAt(j);
                            Machinetta.Debugger.debug( 0,"Using belief handler: " + h);
                            h.beliefChange(belief);
                        }
                    }
                    msgClass = msgClass.getSuperclass();
                } while (msgClass != null);
                
                // @ToDo: this is a lazy hack, fix at some point
                if (belief instanceof ResourceNeedBelief) {
                    ResourceNeedBelief rnb = (ResourceNeedBelief)belief;
                    if (!rnb.isConsidered()) {
                        rnb.setNeed(requestResourceNeed(rnb.getResource()));
                        
                        state.addBelief(rnb);
                        state.notifyListeners();
                    }
                }
            } else {
                Machinetta.Debugger.debug( 0,"AA Could not find belief : " + b[i]);
            }
        }
        Machinetta.Debugger.debug( 0,"Done processing messages");
        // If using this classes version of role allocation, send message to threads
        if (roleAllocationThreads != null) {
            for (Enumeration e = roleAllocationThreads.elements(); e.hasMoreElements(); ) {
                RoleAssignmentThread raThread = (RoleAssignmentThread)e.nextElement();
                if (raThread.complete()) {
                    roleAllocationThreads.remove(raThread.ra);
                    Machinetta.Debugger.debug( 0,"Thread " + raThread + " finished");
                } else {
                    Machinetta.Debugger.debug( 0,"Sending new beliefs to " + raThread);
                    raThread.beliefChange();
                }
            }
        }
        
        threadsIn--;
    }
    
    /** Proxy State object that implementations can use to access beliefs.
     */
    protected ProxyState state = new ProxyState();
    
    /** Pointer to this for RoleAssignmentThread */
    protected AAImplementation AAimpl = null;
    
    /** An AAModule so the implementation can communicate */
    private AAModule parent = new AAModule();
    
    private Hashtable<Class,Object> bcHandlers = new Hashtable<Class,Object>();
    
    protected void addBCHandler(String className, BeliefChangeHandler h) {
        try {
            addBCHandler(Class.forName(className), h);
        } catch (ClassNotFoundException e) {
            Machinetta.Debugger.debug( 5,"Attempting to assign handler for unknown class : " + className + " " + e);
        }
    }
    
    /** Add a belief change handlers, make into Vector if there
     * are multiple handlers for the same beliefClass.
     */
    @SuppressWarnings("unchecked")
    protected void addBCHandler(Class beliefClass, BeliefChangeHandler h) {
        Machinetta.Debugger.debug( 0,"Adding BC Handler: " + h + " for " + beliefClass.toString());
        if (bcHandlers.containsKey(beliefClass)) {
            Object val = bcHandlers.get(beliefClass);
            if (val instanceof Vector) {
                ((Vector<BeliefChangeHandler>)val).addElement(h);
            } else {
                Vector<BeliefChangeHandler> v = new Vector<BeliefChangeHandler>();
                v.add((BeliefChangeHandler)val);
                v.add(h);
                bcHandlers.put(beliefClass, v);
            }
        } else {
            bcHandlers.put(beliefClass, h);
        }
    }
    
    /** Warning: I've never tested that this works ... */
    protected void removeBCHandler(String className, BeliefChangeHandler h) {
        try {
            boolean success = false;
            Class beliefClass = Class.forName(className);
            Object val = bcHandlers.get(beliefClass);
            if (val instanceof Vector) {
                success = ((Vector)val).removeElement(h);
            } else {
                success = (bcHandlers.remove(beliefClass) != null);
            }
            if (!success)
                Machinetta.Debugger.debug( 3,"Removing BC handler failed: " + h + " for " + className);
            else
                Machinetta.Debugger.debug( 0,"No problem removing BC handler " + h);
        } catch (ClassNotFoundException e) {
            Machinetta.Debugger.debug( 5,"Attempting to assign handler for unknown class : " + className + " " + e);
        }
    }
    
    /*++++++++++++++++++++++ Start role allocation stuff */
    /** Subclass can set RoleAllocationAA by calling this method */
    protected void setRoleAllocationAA(final RoleAssignmentAA impl) {
        BeliefChangeHandler raHandler = new BeliefChangeHandler() {
            public void beliefChange(Belief b) {
                RoleAllocationBelief bel = (RoleAllocationBelief)b;
                Machinetta.Debugger.debug( 0,"Processing role allocation belief : " + bel);
                if (roleAllocationThreads.get(bel) != null) {
                    Machinetta.Debugger.debug( 0,"Using existing thread for RA");
                } else if (!bel.waitingForRAP() && !bel.hasConsidered())  {
                    Machinetta.Debugger.debug( 0,"Creating new RA thread for " + bel);
                    RoleAssignmentThread raThread = impl.offeredRole(bel);
                    if (raThread != null) {
                        raThread.setAAObj(AAimpl);
                        raThread.start();
                        roleAllocationThreads.put(bel, raThread);
                    }
                }
            }
            private RoleAssignmentAA raImpl = impl;
        };
        try {
            addBCHandler(Class.forName("Machinetta.State.BeliefType.TeamBelief.RoleAllocationBelief"), raHandler);
            // This is added as well since the RoleAssignmentAA handles the accept messages
            addInputMessageHandler(Class.forName("Machinetta.RAPInterface.InputMessages.AcceptRoleMessage"), roleAcceptHandler);
        } catch (ClassNotFoundException e) {
            Machinetta.Debugger.debug( 5,"Error: " + e);
        }
        roleAllocationThreads = new Hashtable<Belief,RoleAssignmentThread>();
    }
    
    private Hashtable<Belief,RoleAssignmentThread> roleAllocationThreads = null;
    
    /** Handler for role acceptance messages */
    InputMessageHandler roleAcceptHandler = new InputMessageHandler() {
        public void handleMessage(InputMessage inputMsg) {
            Machinetta.RAPInterface.InputMessages.AcceptRoleMessage msg = (Machinetta.RAPInterface.InputMessages.AcceptRoleMessage) inputMsg;
            RoleAllocationBelief bel = msg.getRoleAllocationBelief();
            bel.setAccepted(msg.roleAccepted());
            if (roleAllocationThreads.get(bel) != null) {
                RoleAssignmentThread raThread = (RoleAssignmentThread)roleAllocationThreads.get(bel);
                raThread.beliefChange();
            } else {
                Machinetta.Debugger.debug( 1,"Changed belief but no RoleAssignmentThread");
            }
        }
    };
    
    /*++++++++++++++++++++++ End role allocation stuff */
    
    /** Value is either Vector of InputMessageHandler */
    private Hashtable<Class,Object> inputMsgHandlers = new Hashtable<Class,Object>();
    
    public void addInputMessageHandler(String className, InputMessageHandler h) {
        try {
            addInputMessageHandler(Class.forName(className), h);
        } catch (ClassNotFoundException e) {
            Machinetta.Debugger.debug( 5,"Attempting to assign handler for unknown class : " + className + " " + e);
        }
    }
    
    /** Public so that RoleAssignmentThreads can add and remove listeners */
    @SuppressWarnings("unchecked")
    public void addInputMessageHandler(Class msgClass, InputMessageHandler h) {
        Object currHandler = inputMsgHandlers.get(msgClass);
        if (currHandler == null) {
            inputMsgHandlers.put(msgClass, h);
        } else if (currHandler instanceof InputMessageHandler) {
            Vector<InputMessageHandler> v = new Vector<InputMessageHandler>();
            v.addElement((InputMessageHandler)currHandler);
            v.addElement(h);
            inputMsgHandlers.put(msgClass, v);
        } else if (currHandler instanceof Vector) {
            ((Vector<InputMessageHandler>)currHandler).addElement(h);
        } else {
            Machinetta.Debugger.debug( 5,"Strange handler type!");
        }
    }
    
    /** Public so that RoleAssignmentThreads can add and remove listeners */
    public void removeInputMessageHandler(Class msgClass, InputMessageHandler h) {
        Object currHandler = inputMsgHandlers.get(msgClass);
        if (currHandler == null) {
            Machinetta.Debugger.debug( 1,"Trying to remove non existant message handler");
        } else if (currHandler == h) {
            inputMsgHandlers.remove(h);
        } else if (currHandler instanceof Vector) {
            ((Vector)currHandler).removeElement(h);
        }
    }
    
}
