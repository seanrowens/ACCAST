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

 * SimpleAA.java

 *

 * Created on 11 July 2002, 16:04

 */



package Machinetta.AA;



import Machinetta.State.*;

import Machinetta.State.BeliefType.*;

import Machinetta.State.BeliefType.TeamBelief.*;

import Machinetta.RAPInterface.InputMessages.InputMessage;

import Machinetta.AA.RoleAssignment.*;



import java.util.Vector;



/**

 * Very simple version of AA

 *

 * Not exactly sure what it will do yet, probably pass all tasks to RAP

 *

 * @author  scerri

 */

public abstract class SimpleAA extends AAImplementation {

    

    /** These are the role BeliefIDs that have been given to the RAP */

    Vector<BeliefID> currentRoles = new Vector<BeliefID>();

    

    /** Creates a new instance of SimpleAA */

    public SimpleAA() {

        // Allow the AA to process all the beliefs that were loaded in the belief file

        // proxyStateChange((new ProxyState()).getAllBeliefs());

    }

    

    /** Tell the RAP that it is no longer performing this role.

     * Note: this is not supposed to be "intelligent", AA reasoning

     * for deciding to get rid of role should have been performed already

     */

    abstract protected void removeRole(RoleBelief r);

    

    /** Tell the RAP that it is to perform this role.

     * Note: this is not supposed to be "intelligent", AA reasoning

     * for deciding to accept role should have been performed already

     */

    abstract protected void addRole(RoleBelief r);

        

    /** Handler for changes in Roles

     * Notice that this handler could potentially send the role to the RAP multiple times

     *

     * Notice that this handler sees a RoleBelief with this RAP assigned and gets the role

     * to the RAP, removing existing roles (currently assumes one role at a time).  Reasoning

     * about whether to accept this role or not would have been done elsewhere.

     */

    protected BeliefChangeHandler roleHandler = new BeliefChangeHandler() {

        public void beliefChange(Belief b) {

            RoleBelief bel = (RoleBelief)b;

            

            // Roles that are handled by proxy should be ignored here ...

            if (bel instanceof RoleAllocationBelief) return;

            

            if (bel.getResponsible() != null && bel.getResponsible().isSelf()) {

                

                if (!bel.RAPNotified()) {

                    if (!bel.waitForConstrained() && bel.isActive()) {

                        // Let RAP know about a new role.

                        Machinetta.Debugger.debug( 1,"Telling RAP to start: " + bel);

                        currentRoles.add(bel.getID());

                        addRole(bel);

                        bel.setRAPNotified(true);

                        state.addBelief(bel);

                    } else if (bel.waitForConstrained()) {

                        // Waiting for go ahead from PlanAgent

                        Machinetta.Debugger.debug( 1,"Waiting for PlanAgent go ahead before starting: " + bel);

                    } else if (!bel.isActive()) {

                        // Role is not active, possibly should delete it?

                        Machinetta.Debugger.debug( 1,"Not sending inactive role to RAP: " + bel);

                        state.getSelf().removeRole(bel);

                    }
		    else {
                        Machinetta.Debugger.debug( 1,"Nothing to do for this belief: "+bel);
		    }
                    

                } else {

                    

                    if (!bel.isActive()) {

                        // RAP was told to start, but now tell to stop
                        Machinetta.Debugger.debug( 1,"RAPNotified is false and isActive is false, Telling RAP role is now inactive: " + bel);

                        bel.setRAPNotified(false);

                        currentRoles.remove(bel.getID());

                        removeRole(bel);

                        state.addBelief(bel);

                    } else if (bel.waitForConstrained()) {

                        // RAP was told to start, but now tell to wait
                        Machinetta.Debugger.debug( 1,"RAPNotified is false, isActive is true, but waitForConstrained is true, telling RAP to wait before starting role: " + bel);

                        bel.setRAPNotified(false);

                        currentRoles.remove(bel.getID());

                        removeRole(bel);

                        state.addBelief(bel);

                    } else {

                        // Nothing to do, RAP has been informed

                        Machinetta.Debugger.debug( 1,"Have already informed RAP about : " + bel);

                    }

                }

            } else if (bel.getResponsible() == null) {

                // Check whether we had given this role to the RAP and now require it to be taken away

                if (currentRoles.contains(bel.getID())) {

                    Machinetta.Debugger.debug( 1,"Removing taken away role from RAP: " + bel);

                    currentRoles.remove(bel.getID());

                    removeRole(bel);

                } else {

                    Machinetta.Debugger.debug( 0,"Do not need to take role from RAP: " + bel);

                }

            }
	    else {
                    Machinetta.Debugger.debug( 0,"For role belief, responsible is set but it is not isSelf, so ignoring: " + bel);
	    }

        }

    };

    

    /**

     * Sets resource needs.

     */

    protected BeliefChangeHandler resourceNeedHandler = new BeliefChangeHandler() {

        public void beliefChange(Belief b) {

            ResourceNeedBelief bel = (ResourceNeedBelief)b;

            

            if (!bel.isConsidered()) {

                int need = requestResourceNeed(bel.getResource());

                bel.setNeed(need);

                state.addBelief(bel);

                state.notifyListeners();

            } else if (bel.isGiven()) {

                givenResource(bel.getResource());

            } else if (bel.isTaken()) {

                removeResource(bel.getResource());

            }

        }

    };

    

    /* This code sets up the handlers for belief changes and input messages*/

    {

        addBCHandler("Machinetta.State.BeliefType.TeamBelief.RoleBelief", roleHandler);

        

        setRoleAllocationAA(new SingleShotRoleAssignmentAA());

    }

}

