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

package AirSim.Configs;

import AirSim.Environment.Assets.Tasks.TaskType;
import AirSim.Environment.Vector3D;
import AirSim.Environment.Area;
import AirSim.Machinetta.BasicRole;
import Machinetta.State.BeliefNameID;
import Machinetta.State.BeliefType.ProxyID;
import Machinetta.State.BeliefType.TeamBelief.RoleBelief;
import Machinetta.State.BeliefType.TeamBelief.Constraints.ANDConstraint;
import Machinetta.State.BeliefType.TeamBelief.MTPTFieldDescriptor;
import Machinetta.State.BeliefType.TeamBelief.MTPTRoleDescriptor;
import Machinetta.State.BeliefType.TeamBelief.MetaTeamPlanTemplate;
import Machinetta.State.BeliefType.TeamBelief.TeamPlanTemplate;
import Machinetta.State.BeliefType.TeamBelief.Constraints.RoleConstraint;
import Machinetta.State.BeliefType.Match.*;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Vector;
import java.util.*;
import java.lang.reflect.*;

/**
 *
 * @author pscerri
 */
public class TPTFactory extends Machinetta.State.BeliefType.TeamBelief.TeamPlanTemplateFactory {

    private final static String PATROL_PLAN = "PatrolPlanTemplate";
    private final static String ROLE_NAME_PATROL_AREA = "patrol";
    private final static String ROLE_DESC_PATROL_AREA = "Patrol area";
    private final static String FIELD_DESCRIPTOR_PATROL_AREA = "Area";

    private final static String ATTACK_FROM_AIR_PLAN = "AttackFromAirPlan";
    private final static String ROLE_NAME_ATTACK_FROM_AIR = "attackFromAir";
    private final static String ROLE_DESC_ATTACK_FROM_AIR = "Attack From Air";
    private final static String FIELD_DESCRIPTOR_ATTACK_FROM_AIR = "TargetLocation";

    private final static String ISR_PLAN = "ISRPlan";
    private final static String ROLE_NAME_ISR = "intelSurveilRecon";
    private final static String ROLE_DESC_ISR = "Intelligence, Surveillance, Reconnaissance";
    private final static String FIELD_DESCRIPTOR_ISR_AREA = "Area";

    private final static String AIRDROP_PLAN = "AirdropPlan";
    private final static String ROLE_NAME_AIRDROP = "airdrop";
    private final static String ROLE_DESC_AIRDROP = "Airdrop an asset at a destination";
    private final static String FIELD_DESCRIPTOR_AIRDROP1 = "DropPoint";
    private final static String FIELD_DESCRIPTOR_AIRDROP2 = "BasePoint";
    private final static String FIELD_DESCRIPTOR_AIRDROP3 = "AtDestRange";

    private final static String TRANSPORT_PLAN = "TransportPlan";
    private final static String ROLE_NAME_TRANSPORT = "transport";
    private final static String ROLE_DESC_TRANSPORT = "Transport an asset to a destination by air or ground.";
    private final static String FIELD_DESCRIPTOR_TRANSPORT1 = "Destination";

    private final static String ATTACK_PLAN = "AttackPlan";
    private final static String ROLE_NAME_ATTACK = "attack";
    private final static String ROLE_DESC_ATTACK = "Attack an a ground asset.";
    private final static String FIELD_DESCRIPTOR_ATTACK1 = "Location";

    private final static String UGV_ATTACK_PLAN = "UGVAttackPlan";
    private final static String ROLE_NAME_UGV_ATTACK = "UGVAttack";
    private final static String ROLE_DESC_UGV_ATTACK = "UGV Attack on a ground asset.";
    private final static String FIELD_DESCRIPTOR_UGV_ATTACK1 = "Location";

    private final static String ATTACK_FROM_AIR_OR_GROUND_PLAN = "attackFromAirOrGroundPlan";
    private final static String ROLE_NAME_ATTACK_FROM_AIR_OR_GROUND = "attackFromAirOrGround";
    private final static String ROLE_DESC_ATTACK_FROM_AIR_OR_GROUND = "UGV or UAV Attack on a ground asset.";
    private final static String FIELD_DESCRIPTOR_ATTACK_FROM_AIR_OR_GROUND1 = "Location";


    private final static String ROLE_DESC_AIR_DEFENCE = "Air defence";
    private final static String FIELD_DESCRIPTOR_NAME_FREEFIRE = "FreeFire";
    private final static String FIELD_DESCRIPTOR_NAME_AGGRESSION = "Aggression";


    private final String TPTFileName = "./TPT.ser";
    private ArrayList<MetaTeamPlanTemplate> templates = null;
    
    // this is stolen from PlanAgentFactory.
    private Constructor planAgentConstructor = null;

    public TPTFactory() {
        try {
	Class planAgentClass = Class.forName(Machinetta.Configuration.PLAN_AGENT_CLASS);
	Class [] constructorParams = new Class[1];
	constructorParams[0] = Class.forName("Machinetta.State.BeliefType.TeamBelief.TeamPlanBelief");
	planAgentConstructor = planAgentClass.getConstructor(constructorParams);
        } catch (ClassNotFoundException e1) {
            Machinetta.Debugger.debug( 5,"Exiting: Could not find plan agent class:" + Machinetta.Configuration.PLAN_AGENT_CLASS);
            System.exit(1);
        } catch (NoSuchMethodException e2) {
            Machinetta.Debugger.debug( 5,"Exiting: Specified Plan Agent Class has no constructor for TeamPlanBelief");
            System.exit(1);
        }

	createTemplates();

	for(MetaTeamPlanTemplate mt: templates) {
	    mt.setCapableProxyLists();
	}

    }

    /**
     * Creates whichever type of plan agent is specified in the config file.
     */
    private void createAgent(Machinetta.State.BeliefType.TeamBelief.TeamPlanBelief newPlan) {
        Object [] args = new Object[1];
        args[0] = newPlan;
        try {
	    planAgentConstructor.newInstance(args);
        } catch (InstantiationException e1) {
            Machinetta.Debugger.debug( 5,e1 + ", could not create plan agent for : " + newPlan);
        } catch (IllegalAccessException e2) {
            Machinetta.Debugger.debug( 5,e2 + ", could not create plan agent for : " + newPlan);
        } catch (InvocationTargetException e3) {
            Machinetta.Debugger.debug( 5,e3 + ": " + e3.getCause() + " meant could not create plan agent for : " + newPlan);
        }
    }


    @Override
    protected File getTPTFile() {
        return new File(TPTFileName);
    }
    
    /**
     * This is where the designer (i.e., Sean for now) creates the templates
     *
     */
    public void createTemplates() {
        Machinetta.Debugger.debug(1, "Creating templates");
        
        templates = new ArrayList<MetaTeamPlanTemplate>();
        
	createPatrolTemplate(templates);
	createAttackFromAirTemplate(templates);
	createISRTemplate(templates);
	createAirdropTemplate(templates);
	createTransportTemplate(templates);
	createAttackTemplate(templates);
	createUGVAttackTemplate(templates);
	createAttackFromAirOrGroundTemplate(templates);
    }    
    
    private void createPatrolTemplate(ArrayList<MetaTeamPlanTemplate> templates) {
        Machinetta.Debugger.debug(1, "Creating PatrolTemplate");
        
        BeliefNameID id = new BeliefNameID("PatrolTemplateID");
        String planName = PATROL_PLAN;
        BeliefNameID teamID = new BeliefNameID("TeamAll");

        Hashtable<String, Object> params = new Hashtable<String, Object>();
        Vector preconditions = new Vector();
        Vector postconditions = new Vector();
        
        // Plan parameters - NONE FOR THIS PLAN
        ArrayList <MTPTFieldDescriptor> paramDescriptors = new ArrayList<MTPTFieldDescriptor>();

	// Role descriptors
        ArrayList<MTPTRoleDescriptor> roleDescriptors = new ArrayList<MTPTRoleDescriptor>();        

        BasicRole role1 = new BasicRole(TaskType.patrol);
        role1.roleName = ROLE_NAME_PATROL_AREA;
        
        MTPTFieldDescriptor role1ParamDescriptor = 
	    new MTPTFieldDescriptor(AirSim.Environment.Area.class, 
				    FIELD_DESCRIPTOR_PATROL_AREA, 
				    "Area to be patroled.",
				    new AirSim.Environment.Area(0,0,100,100),
				    new AirSim.Environment.Area(0,0,0,0),
				    new AirSim.Environment.Area(0,0,100000,100000)
				    );

        ArrayList<MTPTFieldDescriptor> role1params = new ArrayList<MTPTFieldDescriptor>();        
        role1params.add(role1ParamDescriptor);
        MTPTRoleDescriptor descRole1 = new MTPTRoleDescriptor(role1, ROLE_DESC_PATROL_AREA, role1params, 1, 8);
        roleDescriptors.add(descRole1);       
        
        // Roles
        Vector roles = new Vector();        

        // Role parameters/constraints - NONE
        Vector roleConstraints = new Vector();        

        MetaTeamPlanTemplate template1 = new MetaTeamPlanTemplate(id, 
								  planName, 
								  teamID, 
								  params, 
								  preconditions, 
								  postconditions, 
								  roles, 
								  roleConstraints, 
								  paramDescriptors,
								  roleDescriptors);
        
        templates.add(template1);
    }

    private void createAttackFromAirTemplate(ArrayList<MetaTeamPlanTemplate> templates) {
        Machinetta.Debugger.debug(1, "Creating AttackFromAirTemplate");
        
        BeliefNameID id = new BeliefNameID("AttackFromAirTemplateID");
        String planName = ATTACK_FROM_AIR_PLAN;
        BeliefNameID teamID = new BeliefNameID("TeamAll");

        Hashtable<String, Object> params = new Hashtable<String, Object>();
        Vector preconditions = new Vector();
        Vector postconditions = new Vector();
        
        // Plan parameters - NONE FOR THIS PLAN
        ArrayList <MTPTFieldDescriptor> paramDescriptors = new ArrayList<MTPTFieldDescriptor>();
                
	// Role descriptors
        ArrayList<MTPTRoleDescriptor> roleDescriptors = new ArrayList<MTPTRoleDescriptor>();        
        
        BasicRole role1 = new BasicRole(TaskType.attackFromAir);

	// NOTE: roleName must match the capabilty assigned to the
	// RAPBelief - this is done in L3Create or ACCASTCreate when
	// the configuration is built, and the RAP Capability beliefs
	// are created and serialized out to the .blf files.
        role1.roleName = ROLE_NAME_ATTACK_FROM_AIR;
        
        MTPTFieldDescriptor role1ParamDescriptor = 
	    new MTPTFieldDescriptor(AirSim.Environment.Vector3D.class, 
				    FIELD_DESCRIPTOR_ATTACK_FROM_AIR,
				    "Target to be attacked.",
				    new AirSim.Environment.Vector3D(100,100,0),
				    new AirSim.Environment.Vector3D(0,0,0), 
				    new AirSim.Environment.Vector3D(100000,100000,10000)
				    );

        ArrayList<MTPTFieldDescriptor> role1params = new ArrayList<MTPTFieldDescriptor>();        
        role1params.add(role1ParamDescriptor);
        MTPTRoleDescriptor descRole1 = new MTPTRoleDescriptor(role1, ROLE_DESC_ATTACK_FROM_AIR, role1params, 1, 8);
        roleDescriptors.add(descRole1);       
        
        // Roles
        Vector roles = new Vector();        

        // Role parameters/constraints - NONE
        Vector roleConstraints = new Vector();        

        MetaTeamPlanTemplate template1 = new MetaTeamPlanTemplate(id, 
								  planName, 
								  teamID, 
								  params, 
								  preconditions, 
								  postconditions, 
								  roles, 
								  roleConstraints, 
								  paramDescriptors,
								  roleDescriptors);
        
        templates.add(template1);
    }

    private void createISRTemplate(ArrayList<MetaTeamPlanTemplate> templates) {
        Machinetta.Debugger.debug(1, "Creating ISRTemplate");
        
        BeliefNameID id = new BeliefNameID("ISRTemplateID");
        String planName = ISR_PLAN;
        BeliefNameID teamID = new BeliefNameID("TeamAll");

        Hashtable<String, Object> params = new Hashtable<String, Object>();
        Vector preconditions = new Vector();
        Vector postconditions = new Vector();
        
        // Plan parameters - NONE FOR THIS PLAN
        ArrayList <MTPTFieldDescriptor> paramDescriptors = new ArrayList<MTPTFieldDescriptor>();

	// Role descriptors
        ArrayList<MTPTRoleDescriptor> roleDescriptors = new ArrayList<MTPTRoleDescriptor>();        

        BasicRole role1 = new BasicRole(TaskType.intelSurveilRecon);
        role1.roleName = ROLE_NAME_ISR;
        
        MTPTFieldDescriptor role1ParamDescriptor = 
	    new MTPTFieldDescriptor(AirSim.Environment.Area.class, 
				    FIELD_DESCRIPTOR_ISR_AREA, 
				    "Area to be ISR'd.",
				    new AirSim.Environment.Area(0,0,100,100),
				    new AirSim.Environment.Area(0,0,0,0),
				    new AirSim.Environment.Area(0,0,100000,100000)
				    );

        ArrayList<MTPTFieldDescriptor> role1params = new ArrayList<MTPTFieldDescriptor>();        
        role1params.add(role1ParamDescriptor);
        MTPTRoleDescriptor descRole1 = new MTPTRoleDescriptor(role1, ROLE_DESC_ISR, role1params, 1, 8);
        roleDescriptors.add(descRole1);       
        
        // Roles
        Vector roles = new Vector();        

        // Role parameters/constraints - NONE
        Vector roleConstraints = new Vector();        

        MetaTeamPlanTemplate template1 = new MetaTeamPlanTemplate(id, 
								  planName, 
								  teamID, 
								  params, 
								  preconditions, 
								  postconditions, 
								  roles, 
								  roleConstraints, 
								  paramDescriptors,
								  roleDescriptors);
        
        templates.add(template1);
    }


    private void createAirdropTemplate(ArrayList<MetaTeamPlanTemplate> templates) {
        Machinetta.Debugger.debug(1, "Creating AirdropTemplate");
        
        BeliefNameID id = new BeliefNameID("AirdropTemplateID");
        String planName = AIRDROP_PLAN;
        BeliefNameID teamID = new BeliefNameID("TeamAll");

        Hashtable<String, Object> params = new Hashtable<String, Object>();
        Vector preconditions = new Vector();
        Vector postconditions = new Vector();
        
        // Plan parameters - NONE FOR THIS PLAN
        ArrayList <MTPTFieldDescriptor> paramDescriptors = new ArrayList<MTPTFieldDescriptor>();

	// Role descriptors
        ArrayList<MTPTRoleDescriptor> roleDescriptors = new ArrayList<MTPTRoleDescriptor>();        

        BasicRole role1 = new BasicRole(TaskType.airdrop);
        role1.roleName = ROLE_NAME_AIRDROP;
        
        MTPTFieldDescriptor role1ParamDescriptor1 = 
	    new MTPTFieldDescriptor(AirSim.Environment.Vector3D.class,
				    FIELD_DESCRIPTOR_AIRDROP1,
				    "Point at which to drop",
				    new AirSim.Environment.Vector3D(100,100,0),
				    new AirSim.Environment.Vector3D(0,0,0), 
				    new AirSim.Environment.Vector3D(100000,100000,10000)
				    );

        MTPTFieldDescriptor role1ParamDescriptor2 = 
	    new MTPTFieldDescriptor(AirSim.Environment.Vector3D.class,
				    FIELD_DESCRIPTOR_AIRDROP2,
				    "Location of base for transport to return to.",
				    new AirSim.Environment.Vector3D(100,100,0),
				    new AirSim.Environment.Vector3D(0,0,0), 
				    new AirSim.Environment.Vector3D(100000,100000,10000)
				    );

        MTPTFieldDescriptor role1ParamDescriptor3 = 
	    new MTPTFieldDescriptor(Double.class,
				    FIELD_DESCRIPTOR_AIRDROP3,
				    "Max range from drop point at which we are considered 'at drop point'",
				    new Double(0),
				    new Double(150),
				    new Double(1000)
				    );

        ArrayList<MTPTFieldDescriptor> role1params = new ArrayList<MTPTFieldDescriptor>();        
        role1params.add(role1ParamDescriptor1);
        role1params.add(role1ParamDescriptor2);
        role1params.add(role1ParamDescriptor3);
        MTPTRoleDescriptor descRole1 = new MTPTRoleDescriptor(role1, ROLE_DESC_AIRDROP, role1params, 1, 8);
        roleDescriptors.add(descRole1);       
        
        // Roles
        Vector roles = new Vector();        

        // Role parameters/constraints - NONE
        Vector roleConstraints = new Vector();        

        MetaTeamPlanTemplate template1 = new MetaTeamPlanTemplate(id, 
								  planName, 
								  teamID, 
								  params, 
								  preconditions, 
								  postconditions, 
								  roles, 
								  roleConstraints, 
								  paramDescriptors,
								  roleDescriptors);
        
        templates.add(template1);
    }

    private void createTransportTemplate(ArrayList<MetaTeamPlanTemplate> templates) {
        Machinetta.Debugger.debug(1, "Creating TransportTemplate");
        
        BeliefNameID id = new BeliefNameID("TransportTemplateID");
        String planName = TRANSPORT_PLAN;
        BeliefNameID teamID = new BeliefNameID("TeamAll");

        Hashtable<String, Object> params = new Hashtable<String, Object>();
        Vector preconditions = new Vector();
        Vector postconditions = new Vector();
        
        // Plan parameters - NONE FOR THIS PLAN
        ArrayList <MTPTFieldDescriptor> paramDescriptors = new ArrayList<MTPTFieldDescriptor>();

	// Role descriptors
        ArrayList<MTPTRoleDescriptor> roleDescriptors = new ArrayList<MTPTRoleDescriptor>();        

        BasicRole role1 = new BasicRole(TaskType.transport);
        role1.roleName = ROLE_NAME_TRANSPORT;
        
        MTPTFieldDescriptor role1ParamDescriptor1 = 
	    new MTPTFieldDescriptor(AirSim.Environment.Vector3D.class,
				    FIELD_DESCRIPTOR_TRANSPORT1,
				    "Destination to transport asset too",
				    new AirSim.Environment.Vector3D(100,100,0),
				    new AirSim.Environment.Vector3D(0,0,0), 
				    new AirSim.Environment.Vector3D(100000,100000,10000)
				    );

        ArrayList<MTPTFieldDescriptor> role1params = new ArrayList<MTPTFieldDescriptor>();        
        role1params.add(role1ParamDescriptor1);
        MTPTRoleDescriptor descRole1 = new MTPTRoleDescriptor(role1, ROLE_DESC_TRANSPORT, role1params, 1, 8);
        roleDescriptors.add(descRole1);       
        
        // Roles
        Vector roles = new Vector();        

        // Role parameters/constraints - NONE
        Vector roleConstraints = new Vector();        

        MetaTeamPlanTemplate template1 = new MetaTeamPlanTemplate(id, 
								  planName, 
								  teamID, 
								  params, 
								  preconditions, 
								  postconditions, 
								  roles, 
								  roleConstraints, 
								  paramDescriptors,
								  roleDescriptors);
        
        templates.add(template1);
    }

    private void createAttackTemplate(ArrayList<MetaTeamPlanTemplate> templates) {
        Machinetta.Debugger.debug(1, "Creating AttackTemplate");
        
        BeliefNameID id = new BeliefNameID("AttackTemplateID");
        String planName = ATTACK_PLAN;
        BeliefNameID teamID = new BeliefNameID("TeamAll");

        Hashtable<String, Object> params = new Hashtable<String, Object>();
        Vector preconditions = new Vector();
        Vector postconditions = new Vector();
        
        // Plan parameters - NONE FOR THIS PLAN
        ArrayList <MTPTFieldDescriptor> paramDescriptors = new ArrayList<MTPTFieldDescriptor>();

	// Role descriptors
        ArrayList<MTPTRoleDescriptor> roleDescriptors = new ArrayList<MTPTRoleDescriptor>();        

        BasicRole role1 = new BasicRole(TaskType.attack);
        role1.roleName = ROLE_NAME_ATTACK;
        
        MTPTFieldDescriptor role1ParamDescriptor1 = 
	    new MTPTFieldDescriptor(AirSim.Environment.Vector3D.class,
				    FIELD_DESCRIPTOR_ATTACK1,
				    "Location to attack asset at",
				    new AirSim.Environment.Vector3D(100,100,0),
				    new AirSim.Environment.Vector3D(0,0,0), 
				    new AirSim.Environment.Vector3D(100000,100000,10000)
				    );

        ArrayList<MTPTFieldDescriptor> role1params = new ArrayList<MTPTFieldDescriptor>();        
        role1params.add(role1ParamDescriptor1);
        MTPTRoleDescriptor descRole1 = new MTPTRoleDescriptor(role1, ROLE_DESC_ATTACK, role1params, 1, 8);
        roleDescriptors.add(descRole1);       
        
        // Roles
        Vector roles = new Vector();        

        // Role parameters/constraints - NONE
        Vector roleConstraints = new Vector();        

        MetaTeamPlanTemplate template1 = new MetaTeamPlanTemplate(id, 
								  planName, 
								  teamID, 
								  params, 
								  preconditions, 
								  postconditions, 
								  roles, 
								  roleConstraints, 
								  paramDescriptors,
								  roleDescriptors);
        
        templates.add(template1);
    }

    private void createUGVAttackTemplate(ArrayList<MetaTeamPlanTemplate> templates) {
        Machinetta.Debugger.debug(1, "Creating UGVAttackTemplate");
        
        BeliefNameID id = new BeliefNameID("UGVAttackTemplateID");
        String planName = UGV_ATTACK_PLAN;
        BeliefNameID teamID = new BeliefNameID("TeamAll");

        Hashtable<String, Object> params = new Hashtable<String, Object>();
        Vector preconditions = new Vector();
        Vector postconditions = new Vector();
        
        // Plan parameters - NONE FOR THIS PLAN
        ArrayList <MTPTFieldDescriptor> paramDescriptors = new ArrayList<MTPTFieldDescriptor>();

	// Role descriptors
        ArrayList<MTPTRoleDescriptor> roleDescriptors = new ArrayList<MTPTRoleDescriptor>();        

        BasicRole role1 = new BasicRole(TaskType.UGVAttack);
        role1.roleName = ROLE_NAME_UGV_ATTACK;
        
        MTPTFieldDescriptor role1ParamDescriptor1 = 
	    new MTPTFieldDescriptor(AirSim.Environment.Vector3D.class,
				    FIELD_DESCRIPTOR_UGV_ATTACK1,
				    "Location to attack asset at",
				    new AirSim.Environment.Vector3D(100,100,0),
				    new AirSim.Environment.Vector3D(0,0,0), 
				    new AirSim.Environment.Vector3D(100000,100000,10000)
				    );

        ArrayList<MTPTFieldDescriptor> role1params = new ArrayList<MTPTFieldDescriptor>();        
        role1params.add(role1ParamDescriptor1);
        MTPTRoleDescriptor descRole1 = new MTPTRoleDescriptor(role1, ROLE_DESC_UGV_ATTACK, role1params, 1, 8);
        roleDescriptors.add(descRole1);       
        
        // Roles
        Vector roles = new Vector();        

        // Role parameters/constraints - NONE
        Vector roleConstraints = new Vector();        

        MetaTeamPlanTemplate template1 = new MetaTeamPlanTemplate(id, 
								  planName, 
								  teamID, 
								  params, 
								  preconditions, 
								  postconditions, 
								  roles, 
								  roleConstraints, 
								  paramDescriptors,
								  roleDescriptors);
        
        templates.add(template1);
    }

    private void createAttackFromAirOrGroundTemplate(ArrayList<MetaTeamPlanTemplate> templates) {
        Machinetta.Debugger.debug(1, "Creating AttackFromAirOrGroundTemplate");
        
        BeliefNameID id = new BeliefNameID("AttackFromAirOrGroundTemplateID");
        String planName = ATTACK_FROM_AIR_OR_GROUND_PLAN;
        BeliefNameID teamID = new BeliefNameID("TeamAll");

        Hashtable<String, Object> params = new Hashtable<String, Object>();
        Vector preconditions = new Vector();
        Vector postconditions = new Vector();
        
        // Plan parameters - NONE FOR THIS PLAN
        ArrayList <MTPTFieldDescriptor> paramDescriptors = new ArrayList<MTPTFieldDescriptor>();

	// Role descriptors
        ArrayList<MTPTRoleDescriptor> roleDescriptors = new ArrayList<MTPTRoleDescriptor>();        

        BasicRole role1 = new BasicRole(TaskType.attackFromAirOrGround);
        role1.roleName = ROLE_NAME_ATTACK_FROM_AIR_OR_GROUND;
        
        MTPTFieldDescriptor role1ParamDescriptor1 = 
	    new MTPTFieldDescriptor(AirSim.Environment.Vector3D.class,
				    FIELD_DESCRIPTOR_ATTACK_FROM_AIR_OR_GROUND1,
				    "Location to attack asset at",
				    new AirSim.Environment.Vector3D(100,100,0),
				    new AirSim.Environment.Vector3D(0,0,0), 
				    new AirSim.Environment.Vector3D(100000,100000,10000)
				    );

        ArrayList<MTPTFieldDescriptor> role1params = new ArrayList<MTPTFieldDescriptor>();        
        role1params.add(role1ParamDescriptor1);
        MTPTRoleDescriptor descRole1 = new MTPTRoleDescriptor(role1, ROLE_DESC_ATTACK_FROM_AIR_OR_GROUND, role1params, 1, 8);
        roleDescriptors.add(descRole1);       
        
        // Roles
        Vector roles = new Vector();        

        // Role parameters/constraints - NONE
        Vector roleConstraints = new Vector();        

        MetaTeamPlanTemplate template1 = new MetaTeamPlanTemplate(id, 
								  planName, 
								  teamID, 
								  params, 
								  preconditions, 
								  postconditions, 
								  roles, 
								  roleConstraints, 
								  paramDescriptors,
								  roleDescriptors);
        
        templates.add(template1);
    }

    protected ArrayList<MetaTeamPlanTemplate> getTemplatesForWriting() {
        return templates;
    }
    
    static public void main(String argv[]) {
        Machinetta.Debugger.debug(1, "Starting template creation");
        TPTFactory factory = new TPTFactory();
        
        factory.createTemplates();
        
        factory.writeTemplates();
        
        Machinetta.Debugger.debug(1, "Done with template creation");
        
	
        Machinetta.Debugger.debug(1, "Dumping template 0");
	dumpTemplate(0);
	//        Machinetta.Debugger.debug(1, "Starting test");
	//        factory.test4();
	//        Machinetta.Debugger.debug(1, "Test complete");
    }
    
    /**
     * Example usage
     */
    public static void test() {
//         TPTFactory factory = new TPTFactory();
//         ArrayList<MetaTeamPlanTemplate> metaTemplates = factory.getTemplates();
        
//         MetaTeamPlanTemplate metaTemplate = metaTemplates.get(0);
                
//         // Example of setting parameters        
//         System.out.println("Got meta plan: " + metaTemplate);
//         System.out.println("Number of params: " + (metaTemplate.paramDescriptors == null? 0 : metaTemplate.paramDescriptors.size()));
//         MTPTFieldDescriptor pDesc = metaTemplate.paramDescriptors.get(0);
//         System.out.println("Setting: " + pDesc.getFieldName() + "(" + pDesc.getDescription() + ") to value between " + pDesc.getMinLimit() + " and " + pDesc.getMaxLimit());
//         System.out.println("Type of param: " + pDesc.getFieldClass());
//         pDesc.setRequiredValue(new Rectangle(0,0,50,50));
        
//         MTPTRoleDescriptor rDesc = (MTPTRoleDescriptor)metaTemplate.roleDescriptors.get(1);
//         System.out.println("Setting number of " + rDesc.getRole() + " instances to something between " + rDesc.getMinCount() + " and " + rDesc.getMaxCount());
//         rDesc.setCount(rDesc.getMinCount());
        
// 	ArrayList<MTPTFieldDescriptor> planParams = null;
// 	ArrayList<MTPTRoleDescriptor> perRoleParams = null;

//         // Create an actual template for use

//         TeamPlanTemplate template = metaTemplate.generateTemplate(planParams,perRoleParams);
    }

    public static void dumpTemplate(int templateIndex) {

	System.err.println("DUMPING TEMPLATE INDEX "+templateIndex);
	
	TPTFactory factory = new TPTFactory();
	ArrayList<MetaTeamPlanTemplate> metaTemplates = factory.getTemplates();
	MetaTeamPlanTemplate metaTemplate = metaTemplates.get(templateIndex);
	 
	System.err.println("For meta plan '" + metaTemplate+ "'");

	ArrayList<MTPTFieldDescriptor> planParams = metaTemplate.getParamDescriptors();

	System.err.println("Number of Plan params: " + planParams.size());

	if(planParams.size() > 0) {

	    for(MTPTFieldDescriptor param : planParams) {
		System.err.println("    param '"+param.getFieldName()+"' ("+param.getDescription()+") has type "+param.getFieldClass().getName()+" defaults to '"+param.getDefaultValue()+"'");
		
	    }
	}

	System.err.println("Done with Plan params."); 


	ArrayList<MTPTRoleDescriptor> initRoleDescs = metaTemplate.getRoleDescriptors();

	System.err.println("Number of role descriptors: " + initRoleDescs.size());

	if(initRoleDescs.size() > 0) {
	    
	    for(MTPTRoleDescriptor roleDesc: initRoleDescs) {

		RoleBelief role = roleDesc.getRole();
		if(null == role) {
		    System.err.println("    ERROR, MTPT roleDesc ("+roleDesc.getRoleDescription()+") has null role!  Skipping!");
		}
		
		System.err.println("    role ("+roleDesc.getRoleDescription()+") has type "+role.getClass().getName()+ " minCount "+roleDesc.getMinCount() + " maxCount "+roleDesc.getMaxCount());

		if(roleDesc.getCount() <= 0) {
		    System.err.println("        This role is optional");
		}

		ArrayList<MTPTFieldDescriptor> roleParams = roleDesc.getGenericParams();
		if(roleParams.size() > 0) {

		    for(MTPTFieldDescriptor rParam: roleParams) {

			System.err.println("            role param '"+rParam.getFieldName()+"'"
					   +" ("+rParam.getDescription()+")"
					   +" has type "+rParam.getFieldClass().getName()
					   +" defaults to '"+rParam.getDefaultValue()+"'");
			    
		    }

		    System.err.println("        Done with role params for this role.");
		}
	    }
	}
    }

    public static void dumpParams(ArrayList<MTPTFieldDescriptor> planParams, ArrayList<MTPTRoleDescriptor> initRoleDescs) {

	Machinetta.Debugger.debug(1,"DUMPING PARAMS");
	Machinetta.Debugger.debug(1,"Number of Plan params: " + planParams.size());

	if(planParams.size() > 0) {

	    for(MTPTFieldDescriptor param : planParams) {
		Machinetta.Debugger.debug(1,"    param '"+param.getFieldName()+"' ("+param.getDescription()+") has type "+param.getFieldClass().getName()+" value '"+param.getRequiredValue()+"'");
	    }
	}

	Machinetta.Debugger.debug(1,"Done with Plan params."); 


	Machinetta.Debugger.debug(1,"Number of role descriptors: " + initRoleDescs.size());

	if(initRoleDescs.size() > 0) {
	    
	    for(MTPTRoleDescriptor roleDesc: initRoleDescs) {

		RoleBelief role = roleDesc.getRole();
		if(null == role) {
		    Machinetta.Debugger.debug(3,"    ERROR, MTPT roleDesc ("+roleDesc.getRoleDescription()+") has null role!  Skipping!");
		}
		
		Machinetta.Debugger.debug(1,"    role ("+roleDesc.getRoleDescription()+") has type "+role.getClass().getName()+ " minCount "+roleDesc.getMinCount() + " maxCount "+roleDesc.getMaxCount()+" count="+roleDesc.getCount());

		if(roleDesc.getCount() <= 0) {
		    Machinetta.Debugger.debug(1,"        This role is optional");
		}

		ArrayList<MTPTFieldDescriptor> roleParams = roleDesc.getGenericParams();
		if(roleParams.size() > 0) {

		    for(MTPTFieldDescriptor rParam: roleParams) {

			Machinetta.Debugger.debug(1,"            role param '"+rParam.getFieldName()+"'"
					   +" ("+rParam.getDescription()+")"
					   +" has type "+rParam.getFieldClass().getName()
					   +" value '"+rParam.getRequiredValue()+"'");
			    
		    }

		    Machinetta.Debugger.debug(1,"        Done with role params for this role.");
		}
	    }
	}
	Machinetta.Debugger.debug(1,"DONE DUMPING PARAMS");
    }

    public static void test2() {

	System.err.println("STARTING TEST2");
	

	TPTFactory factory = new TPTFactory();
	ArrayList<MetaTeamPlanTemplate> metaTemplates = factory.getTemplates();
	MetaTeamPlanTemplate metaTemplate = metaTemplates.get(0);
	 
	// Given a chosen MetaTeamPlanTemplate

	// 1) Get the plan params as an arrayList of
	// MTPTFieldDescriptors (the getter in MTPT should deep copy
	// these descriptors)

	System.err.println("For meta plan '" + metaTemplate+ "'");

	ArrayList<MTPTFieldDescriptor> planParams = metaTemplate.getParamDescriptors();

	System.err.println("Number of Plan params: " + planParams.size());

	// 2) For each fieldDescriptor in planParams, query the user
	// and fill in the requiredValue in the fieldDescriptor
	if(planParams.size() > 0) {

	    for(MTPTFieldDescriptor param : planParams) {
		System.err.println("    param '"+param.getFieldName()+"' ("+param.getDescription()+") has type "+param.getFieldClass().getName()+" defaults to '"+param.getDefaultValue()+"'");
	    }

	    // Note, in this example code we're assuming we only have
	    // 1 param - we really should be looping over all of the
	    // params.  It is assumed that for a particular
	    // configuration that we will only create templates with
	    // plan params of a type that the user interface code can
	    // handle, i.e. the GUI has code for that param class.
	    MTPTFieldDescriptor pDesc = planParams.get(0);

	    // Note, minLimit and maxLimit may not apply depending on
	    // the actual Class of the param.
	    System.err.println("    Setting: " + pDesc.getFieldName() + "(" + pDesc.getDescription() + ") class "+pDesc.getFieldClass()+" to value between " + pDesc.getMinLimit() + " and " + pDesc.getMaxLimit());

	    pDesc.setRequiredValue(new Rectangle(0,0,50,50));
	}

	System.err.println("Done with Plan params."); 

	// 3) Get a arrayList of MTPTRoleDescriptors (the getter in
	// MTPT should deep copy these descriptors)

	ArrayList<MTPTRoleDescriptor> initRoleDescs = metaTemplate.getRoleDescriptors();

	ArrayList<MTPTRoleDescriptor> finalRoleDescs = new ArrayList<MTPTRoleDescriptor>();

	System.err.println("Number of role descriptors: " + initRoleDescs.size());

	if(initRoleDescs.size() > 0) {
	    
	    for(MTPTRoleDescriptor roleDesc: initRoleDescs) {

		RoleBelief role = roleDesc.getRole();

		if(null == role) {
		    System.err.println("    ERROR, MTPT roleDesc ("+roleDesc.getRoleDescription()+") has null role!  Skipping!");
		    continue;
		}
		
		System.err.println("    role ("+roleDesc.getRoleDescription()+") has type "+role.getClass().getName());

		// Each roleDesc has a minCount, a maxCount, and
		// defaults to the minCount.  A minCount of 0 means
		// the role is optional and can be left out entirely.
		// If minCount == maxCount then we don't have to ask
		// the user how many he wants since there's no choice.
		if(roleDesc.getMinCount() != roleDesc.getMaxCount()) {
		    // For this sample code we're only going to set
		    // the number of roles for the air defence role
		    // descriptor in the list.
		    if(roleDesc.getRoleDescription().equals(ROLE_DESC_AIR_DEFENCE)) {
			roleDesc.setCount(2);
			System.err.println("        set role count to 2");
		    }
		    else {
			// Do nothing - take the default, i.e. minCount.
			System.err.println("        left role count as default value "+roleDesc.getCount());
		    }
		}

		// Now that we know how many instances of this role
		// are needed we need to create each and fill in the
		// per role params for each.  
		if(roleDesc.getCount() <= 0) {
		    System.err.println("        This role is optional, leaving it out.");
		    continue;
		}
		for(int loopj = 0; loopj < roleDesc.getCount(); loopj++) {

		    MTPTRoleDescriptor newRoleDesc = roleDesc.clone();
		    System.err.println("        Created "+loopj+"nth instance of role descriptor with roleID="+roleDesc.getRole().getID()+" new roleID="+newRoleDesc.getRole().getID());

		    // Now we fill in it's params;

		    ArrayList<MTPTFieldDescriptor> roleParams = newRoleDesc.getGenericParams();
		    if(roleParams.size() > 0) {

			for(MTPTFieldDescriptor rParam: roleParams) {

			    System.err.println("            role param '"+rParam.getFieldName()+"'"
					       +" ("+rParam.getDescription()+")"
					       +" has type "+rParam.getFieldClass().getName()
					       +" defaults to '"+rParam.getDefaultValue()+"'");
			    
			    // Again, in this sample code we're only
			    // going to set the first parameter
			    // ("Aggression") of BOTH instances of the
			    // second ("Air defense") roleDescriptor.
			    // Other params will all get their
			    // defaults.
			    if(newRoleDesc.getRoleDescription().equals(ROLE_DESC_AIR_DEFENCE)
			       && rParam.getFieldName().equals(FIELD_DESCRIPTOR_NAME_AGGRESSION)
			       ) {
				rParam.setRequiredValue(5);
				System.err.println("                Set role param to 5");
			    }
			    else {
				rParam.setRequiredValue(rParam.getDefaultValue());
				System.err.println("                Set role param to default value "+rParam.getDefaultValue());
			    }
// 			    String pName = rParam.getFieldName();
// 			    Class pClass = rParam.getFieldClass();
// 			    String pClassName = pClass.getName();
// 			    Object reqValue = rParam.getRequiredValue();
// 			    System.err.println("                We just set this, now role param '"+pName+"' ("+rParam.getDescription()+") has type "+pClassName+" value '"+reqValue+"'");

			}
		    }

		    System.err.println("        Done with role params for this role.");

		    finalRoleDescs.add(newRoleDesc);

		    System.err.println("        Added role to final roleDescs list.");
		}
	    }

	}
        
	System.err.println("Done with Role descriptors."); 

	System.err.println("Dumping plan and role params before generating");
	dumpParams(planParams, finalRoleDescs);
	System.err.println("Done Dumping plan and role params before generating");

	System.err.println("generating TPT from MTPT");
	
	TeamPlanTemplate template = metaTemplate.generateTemplate(planParams,finalRoleDescs);	

	System.err.println("instantiating PlanBelief from TPT");

	Hashtable<String,Matchable> triggers = new Hashtable<String,Matchable>();
	template.instantiatePlan(triggers);

	System.err.println("ENDING TEST2");

    }


    public void test3() {

	System.err.println("STARTING TEST3");
	
	BeliefNameID teamID = new BeliefNameID("TeamAll");

	TPTFactory factory = new TPTFactory();
	ArrayList<MetaTeamPlanTemplate> metaTemplates = factory.getTemplates();
	
	MetaTeamPlanTemplate metaTemplate = null;
	for(MetaTeamPlanTemplate mt: metaTemplates) {
	    if(mt.getName().equals(PATROL_PLAN))
	       metaTemplate = mt;
	}
	 
	// Given a chosen MetaTeamPlanTemplate

	// 1) Get the plan params as an arrayList of
	// MTPTFieldDescriptors (the getter in MTPT should deep copy
	// these descriptors)

	System.err.println("For meta plan '" + metaTemplate+ "'");

	ArrayList<MTPTFieldDescriptor> planParams = metaTemplate.getParamDescriptors();

	System.err.println("Number of Plan params: " + planParams.size());

	// 2) For each fieldDescriptor in planParams, query the user
	// and fill in the requiredValue in the fieldDescriptor
	if(planParams.size() > 0) {

	    for(MTPTFieldDescriptor param : planParams) {
		System.err.println("    param '"+param.getFieldName()+"' ("+param.getDescription()+") has type "+param.getFieldClass().getName()+" defaults to '"+param.getDefaultValue()+"'");
	    }

	    // In this example there are no plan params we need to set.
	}

	System.err.println("Done with Plan params."); 

	// 3) Get a arrayList of MTPTRoleDescriptors (the getter in
	// MTPT should deep copy these descriptors)

	ArrayList<MTPTRoleDescriptor> initRoleDescs = metaTemplate.getRoleDescriptors();

	ArrayList<MTPTRoleDescriptor> finalRoleDescs = new ArrayList<MTPTRoleDescriptor>();

	System.err.println("Number of role descriptors: " + initRoleDescs.size());

	if(initRoleDescs.size() > 0) {
	    
	    for(MTPTRoleDescriptor roleDesc: initRoleDescs) {

		RoleBelief role = roleDesc.getRole();

		if(null == role) {
		    System.err.println("    ERROR, MTPT roleDesc ("+roleDesc.getRoleDescription()+") has null role!  Skipping!");
		    continue;
		}
		
		System.err.println("    role ("+roleDesc.getRoleDescription()+") has type "+role.getClass().getName());

		// Each roleDesc has a minCount, a maxCount, and
		// defaults to the minCount.  A minCount of 0 means
		// the role is optional and can be left out entirely.
		// If minCount == maxCount then we don't have to ask
		// the user how many he wants since there's no choice.
		if(roleDesc.getMinCount() != roleDesc.getMaxCount()) {

		    // For this sample code we only want to make 5 Patrol roles.
		    if(roleDesc.getRoleDescription().equals(ROLE_DESC_PATROL_AREA)) {
			roleDesc.setCount(5);
			System.err.println("        set role count to 5");
		    }
		    else {
			// Do nothing - take the default, i.e. minCount.
			System.err.println("        left role count as default value "+roleDesc.getCount());
		    }
		}

		// Now that we know how many instances of this role
		// are needed we need to create each and fill in the
		// per role params for each.  
		if(roleDesc.getCount() <= 0) {
		    System.err.println("        This role is optional, leaving it out.");
		    continue;
		}
		for(int loopj = 0; loopj < roleDesc.getCount(); loopj++) {

		    
		    MTPTRoleDescriptor newRoleDesc = roleDesc.clone();
		    System.err.println("        Created "+loopj+"nth instance of role descriptor with roleID="+roleDesc.getRole().getID()+" new roleID="+newRoleDesc.getRole().getID());

		    // Now we fill in it's params;

		    ArrayList<MTPTFieldDescriptor> roleParams = newRoleDesc.getGenericParams();
		    if(roleParams.size() > 0) {

			for(MTPTFieldDescriptor rParam: roleParams) {

			    System.err.println("            role param '"+rParam.getFieldName()+"'"
					       +" ("+rParam.getDescription()+")"
					       +" has type "+rParam.getFieldClass().getName()
					       +" defaults to '"+rParam.getDefaultValue()+"'");
			    
			    // Again, in this sample code we're only
			    // going to set the first parameter
			    // ("Aggression") of BOTH instances of the
			    // second ("Air defense") roleDescriptor.
			    // Other params will all get their
			    // defaults.
			    if(newRoleDesc.getRoleDescription().equals(ROLE_DESC_PATROL_AREA)
			       && rParam.getFieldName().equals(FIELD_DESCRIPTOR_PATROL_AREA)
			       ) {
				rParam.setRequiredValue(new Rectangle(2000,3000,2000,1000));
				System.err.println("                Set role param to 5");
			    }
			    else {
				rParam.setRequiredValue(rParam.getDefaultValue());
				System.err.println("                Set role param to default value "+rParam.getDefaultValue());
			    }
			}
		    }

		    System.err.println("        Done with role params for this role.");

		    finalRoleDescs.add(newRoleDesc);

		    System.err.println("        Added role to final roleDescs list.");
		}
	    }

	}
        
	System.err.println("Done with Role descriptors."); 

	System.err.println("Dumping plan and role params before generating");
	dumpParams(planParams, finalRoleDescs);
	System.err.println("Done Dumping plan and role params before generating");

	System.err.println("generating TPT from MTPT");
	
	TeamPlanTemplate template = metaTemplate.generateTemplate(planParams,finalRoleDescs);	
	template.setLocallySensed(true);

	System.err.println("instantiating PlanBelief from TPT");

	Hashtable<String,Matchable> triggers = new Hashtable<String,Matchable>();
	Machinetta.State.BeliefType.TeamBelief.TeamPlanBelief newPlan = template.instantiatePlan(triggers);
	createAgent(newPlan);
	System.err.println("ENDING TEST3");
    }


    public void test4() {

	System.err.println("STARTING TEST4");
	
	BeliefNameID teamID = new BeliefNameID("TeamAll");

	TPTFactory factory = new TPTFactory();
	ArrayList<MetaTeamPlanTemplate> metaTemplates = factory.getTemplates();
	
	MetaTeamPlanTemplate metaTemplate = null;
	for(MetaTeamPlanTemplate mt: metaTemplates) {
	    if(mt.getName().equals(ATTACK_FROM_AIR_PLAN))
	       metaTemplate = mt;
	}
	if(null == metaTemplate) {
	    System.err.println("Where is the MTPT named '"+ATTACK_FROM_AIR_PLAN+"' ??? Giving up.");
	    System.exit(1);
	}
	 
	// Given a chosen MetaTeamPlanTemplate

	// 1) Get the plan params as an arrayList of
	// MTPTFieldDescriptors (the getter in MTPT should deep copy
	// these descriptors)

	System.err.println("For meta plan '" + metaTemplate+ "'");

	ArrayList<MTPTFieldDescriptor> planParams = metaTemplate.getParamDescriptors();

	System.err.println("Number of Plan params: " + planParams.size());

	// 2) For each fieldDescriptor in planParams, query the user
	// and fill in the requiredValue in the fieldDescriptor
	if(planParams.size() > 0) {

	    for(MTPTFieldDescriptor param : planParams) {
		System.err.println("    param '"+param.getFieldName()+"' ("+param.getDescription()+") has type "+param.getFieldClass().getName()+" defaults to '"+param.getDefaultValue()+"'");
	    }

	    // In this example there are no plan params we need to set.
	}

	System.err.println("Done with Plan params."); 

	// 3) Get a arrayList of MTPTRoleDescriptors (the getter in
	// MTPT should deep copy these descriptors)

	ArrayList<MTPTRoleDescriptor> initRoleDescs = metaTemplate.getRoleDescriptors();

	ArrayList<MTPTRoleDescriptor> finalRoleDescs = new ArrayList<MTPTRoleDescriptor>();

	System.err.println("Number of role descriptors: " + initRoleDescs.size());

	if(initRoleDescs.size() > 0) {
	    
	    for(MTPTRoleDescriptor roleDesc: initRoleDescs) {

		RoleBelief role = roleDesc.getRole();

		if(null == role) {
		    System.err.println("    ERROR, MTPT roleDesc ("+roleDesc.getRoleDescription()+") has null role!  Skipping!");
		    continue;
		}
		
		System.err.println("    role ("+roleDesc.getRoleDescription()+") has type "+role.getClass().getName());

		// Each roleDesc has a minCount, a maxCount, and
		// defaults to the minCount.  A minCount of 0 means
		// the role is optional and can be left out entirely.
		// If minCount == maxCount then we don't have to ask
		// the user how many he wants since there's no choice.
		if(roleDesc.getMinCount() != roleDesc.getMaxCount()) {

		    // For this sample code we want to make 5 attackFromAir roles.
		    if(roleDesc.getRoleDescription().equals(ROLE_DESC_ATTACK_FROM_AIR)) {
			roleDesc.setCount(5);
			System.err.println("        set role count to 5");
		    }
		    else {
			// Do nothing - take the default, i.e. minCount.
			System.err.println("        left role count as default value "+roleDesc.getCount());
		    }
		}

		// Now that we know how many instances of this role
		// are needed we need to create each and fill in the
		// per role params for each.  
		if(roleDesc.getCount() <= 0) {
		    System.err.println("        This role is optional, leaving it out.");
		    continue;
		}
		for(int loopj = 0; loopj < roleDesc.getCount(); loopj++) {

		    
		    MTPTRoleDescriptor newRoleDesc = roleDesc.clone();
		    System.err.println("        Created "+loopj+"nth instance of role descriptor with roleID="+roleDesc.getRole().getID()+" new roleID="+newRoleDesc.getRole().getID());

		    // Now we fill in it's params;

		    ArrayList<MTPTFieldDescriptor> roleParams = newRoleDesc.getGenericParams();
		    if(roleParams.size() > 0) {

			for(MTPTFieldDescriptor rParam: roleParams) {

			    System.err.println("            role param '"+rParam.getFieldName()+"'"
					       +" ("+rParam.getDescription()+")"
					       +" has type "+rParam.getFieldClass().getName()
					       +" defaults to '"+rParam.getDefaultValue()+"'");
			    
			    // Again, in this sample code we're only
			    // going to set the first parameter
			    // ("Aggression") of BOTH instances of the
			    // second ("Air defense") roleDescriptor.
			    // Other params will all get their
			    // defaults.
			    if(newRoleDesc.getRoleDescription().equals(ROLE_DESC_ATTACK_FROM_AIR)
			       && rParam.getFieldName().equals(FIELD_DESCRIPTOR_ATTACK_FROM_AIR)
			       ) {
				rParam.setRequiredValue(new AirSim.Environment.Vector3D(0,1000,0));
				System.err.println("                Set role param to 5");
			    }
			    else {
				rParam.setRequiredValue(rParam.getDefaultValue());
				System.err.println("                Set role param to default value "+rParam.getDefaultValue());
			    }
			}
		    }

		    System.err.println("        Done with role params for this role.");

		    finalRoleDescs.add(newRoleDesc);

		    System.err.println("        Added role to final roleDescs list.");
		}
	    }

	}
        
	System.err.println("Done with Role descriptors."); 

	System.err.println("Dumping plan and role params before generating");
	dumpParams(planParams, finalRoleDescs);
	System.err.println("Done Dumping plan and role params before generating");

	System.err.println("generating TPT from MTPT");
	
	TeamPlanTemplate template = metaTemplate.generateTemplate(planParams,finalRoleDescs);	
	template.setLocallySensed(true);

	System.err.println("instantiating PlanBelief from TPT");

	Hashtable<String,Matchable> triggers = new Hashtable<String,Matchable>();
	Machinetta.State.BeliefType.TeamBelief.TeamPlanBelief newPlan = template.instantiatePlan(triggers);
	createAgent(newPlan);
	System.err.println("ENDING TEST4");
    }

    private void dumpTPT(TeamPlanTemplate tpt) {
	Machinetta.Debugger.debug(1,"Dumping TeamPlanTemplate "+tpt.planName);
	Machinetta.Debugger.debug(1,"    roles.size()="+tpt.roles.size());
	for(RoleBelief role: tpt.roles) {
	    Machinetta.Debugger.debug(1,"    role id="+role.getID()+" name="+role.roleName+" in plan "+role.plan+" assigned to "+role.responsibleRAP);

	    Class fieldClass = null;
	    try {
		fieldClass = Class.forName("java.util.Hashtable");
		Machinetta.Debugger.debug(1,"Got Class for java.util.Hashtable");
	    }
	    catch (java.lang.ClassNotFoundException e) {
		// this should _never_ happen.
		Machinetta.Debugger.debug(3,"Can't get class for java.util.Hashtable??? How did THAT happen? Agh!");
	    }

	    String fieldName = "params";

	    Class roleClass = role.getClass();
	    Field paramsField=null;
	    try {
		paramsField = roleClass.getField(fieldName);
	    }
	    catch (java.lang.NoSuchFieldException e) {
		Machinetta.Debugger.debug(1,"    No public field "+fieldName+" class "+fieldClass.getName()+" is available on class "+roleClass.getName());
		continue;
	    }

	    if(null != paramsField && null != fieldClass) {
		int mod = paramsField.getModifiers();
		Class paramsFieldClass = paramsField.getType();
		if(!(Modifier.isPublic(mod) && paramsFieldClass.equals(fieldClass))) {
		    Machinetta.Debugger.debug(1,"    params field is not public on class "+roleClass.getName());
		    continue;
		}
	    }

	    Hashtable paramsHash = null;
	    
	    try {
		paramsHash = (Hashtable)paramsField.get(role);
		Machinetta.Debugger.debug(1,"    Found the public Hashtable named params.");
		if(null == paramsHash) {
		    Machinetta.Debugger.debug(1,"    Aw, nope, it's null.  Hmm.  @TODO:  Should we instantiate it now then?");
		    continue;
		}
	    }
	    catch (java.lang.IllegalAccessException e) {
		// @TODO: Can this ever happen under normal circumstances?
		Machinetta.Debugger.debug(1,"    TODO: Why can't I get the params field?  This should never happen - we shouldn't be trying to get it unless we've already found it.");
		continue;
	    }


	    Iterator it = paramsHash.keySet().iterator();
	    while (it.hasNext()) {
		// Retrieve key
		Object key = it.next();
		Object value = paramsHash.get(key);
		Machinetta.Debugger.debug(1,"        role param "+key+" = "+value);
	    }


	}	
    }

    // this test must be run from outside TPTFactory by a full proxy.
    public void test5(ArrayList<Area> areaList) {

	Machinetta.Debugger.debug(1,"STARTING TEST5 - full test of mtpt to team plan, via proxy calling TPTFactory.test5()");
	
	BeliefNameID teamID = new BeliefNameID("TeamAll");

	TPTFactory factory = new TPTFactory();
	ArrayList<MetaTeamPlanTemplate> metaTemplates = factory.getTemplates();
	
	MetaTeamPlanTemplate metaTemplate = null;
	for(MetaTeamPlanTemplate mt: metaTemplates) {
	    if(mt.getName().equals(ISR_PLAN))
	       metaTemplate = mt;
	}
	if(null == metaTemplate) {
	    Machinetta.Debugger.debug(1,"Where is the MTPT named '"+ISR_PLAN+"' ??? Giving up.");
	    System.exit(1);
	}
	 
	// Given a chosen MetaTeamPlanTemplate

	// 0) fill in the capableProxyList
	//	metaTemplate.setCapableProxyLists();	

	// 1) Get the plan params as an arrayList of
	// MTPTFieldDescriptors (the getter in MTPT should deep copy
	// these descriptors)

	Machinetta.Debugger.debug(1,"For meta plan '" + metaTemplate+ "'");

	ArrayList<MTPTFieldDescriptor> planParams = metaTemplate.getParamDescriptors();

	Machinetta.Debugger.debug(1,"Number of Plan params: " + planParams.size());

	// 2) For each fieldDescriptor in planParams, query the user
	// and fill in the requiredValue in the fieldDescriptor
	if(planParams.size() > 0) {

	    for(MTPTFieldDescriptor param : planParams) {
		Machinetta.Debugger.debug(1,"    param '"+param.getFieldName()+"' ("+param.getDescription()+") has type "+param.getFieldClass().getName()+" defaults to '"+param.getDefaultValue()+"'");
	    }

	    // In this example there are no plan params we need to set.
	}

	Machinetta.Debugger.debug(1,"Done with Plan params."); 

	// 3) Get a arrayList of MTPTRoleDescriptors (the getter in
	// MTPT should deep copy these descriptors)

	ArrayList<MTPTRoleDescriptor> initRoleDescs = metaTemplate.getRoleDescriptors();

	ArrayList<MTPTRoleDescriptor> finalRoleDescs = new ArrayList<MTPTRoleDescriptor>();

	Machinetta.Debugger.debug(1,"Number of role descriptors: " + initRoleDescs.size());


	if(initRoleDescs.size() <= 0) {
	    Machinetta.Debugger.debug(4,"    ERROR, this plan has NO ROLE Decsriptors");
	}
	else if(initRoleDescs.size() > 0) {
	    
	    for(MTPTRoleDescriptor roleDesc: initRoleDescs) {

		RoleBelief role = roleDesc.getRole();

		if(null == role) {
		    Machinetta.Debugger.debug(4,"    ERROR, MTPT roleDesc ("+roleDesc.getRoleDescription()+") has null role!  Skipping!");
		    continue;
		}
		
		Machinetta.Debugger.debug(1,"    role ("+roleDesc.getRoleDescription()+") has type "+role.getClass().getName());

		// Each roleDesc has a minCount, a maxCount, and
		// defaults to the minCount.  A minCount of 0 means
		// the role is optional and can be left out entirely.
		// If minCount == maxCount then we don't have to ask
		// the user how many he wants since there's no choice.
		if(roleDesc.getMinCount() != roleDesc.getMaxCount()) {

		    // For this sample code we want to make 5 attackFromAir roles.
		    if(roleDesc.getRoleDescription().equals(ROLE_DESC_ISR)) {
			roleDesc.setCount(areaList.size());
			Machinetta.Debugger.debug(1,"        set role count for roleDesc "+roleDesc+" to "+roleDesc.getCount());
		    }
		    else {
			// Do nothing - take the default, i.e. minCount.
			Machinetta.Debugger.debug(1,"        left role count as default value "+roleDesc.getCount());
		    }
		}

		// Now that we know how many instances of this role
		// are needed we need to create each and fill in the
		// per role params for each.  
		if(roleDesc.getCount() <= 0) {
		    Machinetta.Debugger.debug(1,"        This role is optional, leaving it out.");
		    continue;
		}
		Machinetta.Debugger.debug(1,"        Filling in role params for "+roleDesc.getCount()+" roles.");
		for(int loopj = 0; loopj < roleDesc.getCount(); loopj++) {

		    
		    MTPTRoleDescriptor newRoleDesc = roleDesc.clone();
		    Machinetta.Debugger.debug(1,"        Created "+loopj+"nth instance of role descriptor with roleID="+roleDesc.getRole().getID()+" new roleID="+newRoleDesc.getRole().getID());

		    // Print out capable proxy IDs
		    Machinetta.Debugger.debug(1,"        ProxyIDs that can perform role "+roleDesc);
		    if(null == newRoleDesc.getCapableProxyList()) {
			Machinetta.Debugger.debug(1,"            capableProxyList is null.");
		    }
		    else {
			for(ProxyID pid: newRoleDesc.getCapableProxyList()) {
			    Machinetta.Debugger.debug(1,"            proxyID="+pid);
			}
		    }


		    // Now we fill in it's params;
		    ArrayList<MTPTFieldDescriptor> roleParams = newRoleDesc.getGenericParams();
		    if(roleParams.size() > 0) {

			for(MTPTFieldDescriptor rParam: roleParams) {

			    Machinetta.Debugger.debug(1,"            role param '"+rParam.getFieldName()+"'"
					       +" ("+rParam.getDescription()+")"
					       +" has type "+rParam.getFieldClass().getName()
					       +" defaults to '"+rParam.getDefaultValue()+"'");
			    
			    // Again, in this sample code we're only
			    // going to set the first parameter
			    // ("Area") of each role.  Any other
			    // params (shouldn't be any for ISR_PLAN)
			    // will all get their defaults.
			    if(newRoleDesc.getRoleDescription().equals(ROLE_DESC_ISR)
			       && rParam.getFieldName().equals(FIELD_DESCRIPTOR_ISR_AREA)
			       ) {
				rParam.setRequiredValue(areaList.get(loopj));
				Machinetta.Debugger.debug(1,"                Set role param for role "+loopj+" to "+rParam.getRequiredValue());
			    }
			    else {
				rParam.setRequiredValue(rParam.getDefaultValue());
				Machinetta.Debugger.debug(1,"                Set role param for role "+loopj+" to default value "+rParam.getDefaultValue());
			    }
			}
		    }

		    Machinetta.Debugger.debug(1,"        Done with role params for this role.");

		    finalRoleDescs.add(newRoleDesc);

		    Machinetta.Debugger.debug(1,"        Added role to final roleDescs list.");
		}
	    }

	}
        
	Machinetta.Debugger.debug(1,"Done with Role descriptors."); 

	Machinetta.Debugger.debug(1,"Dumping plan and role params before generating");
	dumpParams(planParams, finalRoleDescs);
	Machinetta.Debugger.debug(1,"Done Dumping plan and role params before generating");

	Machinetta.Debugger.debug(1,"generating TPT from MTPT");
	
	TeamPlanTemplate template = metaTemplate.generateTemplate(planParams,finalRoleDescs);	
	template.setLocallySensed(true);

	Machinetta.Debugger.debug(1,"Dumping generated TeamPlanTemplate");
	dumpTPT(template);

	Machinetta.Debugger.debug(1,"instantiating PlanBelief from TPT");

	Hashtable<String,Matchable> triggers = new Hashtable<String,Matchable>();
	Machinetta.State.BeliefType.TeamBelief.TeamPlanBelief newPlan = template.instantiatePlan(triggers);
	createAgent(newPlan);
	Machinetta.Debugger.debug(1,"ENDING TEST5");
    }
}
