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
 * MetaTeamPlanTemplate.java
 *
 * Created on Tue Jun 24 23:59:15 EDT 2008
 */

package Machinetta.State.BeliefType.TeamBelief;

import Machinetta.State.BeliefID;
import Machinetta.State.ProxyState;
import Machinetta.State.BeliefNameID;
import Machinetta.State.BeliefType.Belief;
import Machinetta.State.BeliefType.BooleanBelief;
import Machinetta.State.BeliefType.Belief;
import Machinetta.State.BeliefType.Match.*;
import Machinetta.State.BeliefType.TeamBelief.Constraints.RoleConstraint;
import Machinetta.State.BeliefType.RAPBelief;
import Machinetta.State.BeliefType.ProxyID;

import java.util.*;
import java.lang.reflect.*;

/**
 * The MetaTeamPlanTemplate is a self describing meta template, used
 * to generate at runtime a TeamPlanTemplate based on parameters.
 *
 * TeamPlanTemplates are used to check new beliefs for matches of
 * preconditions, and when a match is found, instantiate
 * TeamPlanBelief objects.
 *
 * TeamPlanBelief objects are then used to check new beliefs for
 * matches of postconditions to halt the plan.
 *
 * The TeamPlanTemplate constructor looks like;
 *
 *  public TeamPlanTemplate(BeliefID id, 
 *                          String planName,
 *                          BeliefID team,
 *                          Hashtable<String,Object> params,
 *                          Vector preconditions,
 *                          Vector postconditions,
 *                          Vector roles,
 *                          Vector roleConstraints) {
 *
 * preconditions is a Vector of Vectors of MatchCondition objects.
 *
 * postcondition is also a Vector of Vectors of MatchConditions.
 * postconditions are passed on to the instantiated TeamPlanBelief and
 * used to end plans when their postconditions are matched.
 *
 * MetaTeamPlanTemplate includes descriptions of each of the
 * parameters of the TeamPlanTemplate.
 *
 * For every new Matchable belief added to ProxyState,
 * PlanAgentFactory calls TeamPlanTemplate.matchPreconditions() on
 * each TeamPlanTemplate.  matchPreconditions() returns a 
 *
 * Vector<Hashtable<String,Matchable>>
 * 
 * which holds each set of preconditions that is matched.  If a match
 * is found, TeamPlanTemplate.instantiatePlan() is called with the
 * matched beliefs.  In instantiatePlan(), the TeamPlanTemplate params
 * are cloned, pre and post conditions are added to the clone, the
 * specific triggers (preconditions) that caused the call to
 * instantiatePlane are added, and finally the set of params plus
 * pre/post conditions plus matches are passed as the params arg to
 * the constructor for TeamPlanBelief.
 *
 * These 
 *
 * @author  owens
 */
public class MetaTeamPlanTemplate extends Belief {
    public final static String PARAMS_GENERATED_BY_META_TEAM_PLAN_TEMPLATE = "GeneratedByMetaTeamPlanTemplate";
    private final static String NAME_OF_PARAMS_HASHTABLE = "params";
    
    private static int beliefIDCounter = 0;

    private ArrayList<MTPTFieldDescriptor> paramDescriptors = null;
    public ArrayList<MTPTFieldDescriptor> getParamDescriptors() {
	ArrayList<MTPTFieldDescriptor> ret = new ArrayList<MTPTFieldDescriptor>();
	for(MTPTFieldDescriptor fieldDescriptor: paramDescriptors) 
	    ret.add(fieldDescriptor.clone());
	return ret;
    }

    /** Name of the meta plan class */
    public String metaPlanName = null;

    /** Belief ID of the team executing this plan */
    public BeliefID team = null;

    /** Table of default parameter values for this template (inherited by all instances) */
    public Hashtable<String,Object> params = new Hashtable<String,Object>();
    
    /** Lists of conditions that trigger initiation/termination of this plan class */
    public Vector preconditions = null;
    public Vector postconditions = null;
    
    /** List of roles to instantiate with the plan */
    public Vector<RoleBelief> roles = null;
    
    /** MTPTRoleDescriptors for the roles 
     *
     * When we instantiate the TPT we create _additional_ roles using
     * these descriptors.
     */
    private ArrayList<MTPTRoleDescriptor> roleDescriptors = null;

    public ArrayList<MTPTRoleDescriptor> getRoleDescriptors() {
	ArrayList<MTPTRoleDescriptor> ret = new ArrayList<MTPTRoleDescriptor>();
	for(MTPTRoleDescriptor roleDescriptor: roleDescriptors) 
	    ret.add(roleDescriptor.clone());
	return ret;
    }

    /** Constraints between roles in the plan 
     *
     * Fri Nov  7 16:51:31 EST 2008 SRO 
     * We're going to have to think about how we create role
     * constraints for roles that don't exist yet since we don't have
     * roles at this point but rather instantiate them from
     * MTPTRoleDescriptors when we instantiate the TPT.
     */
    public Vector<RoleConstraint> roleConstraints = null;
    
    /** Lookup table to facilitate finding (pre)conditions */
    public Hashtable<String,MatchCondition> conditionTable = null;
    
    /** Maximum length of time the plan might be expected to run, before it is reasonable to think there
     * might be a problem. <br>
     *
     * If < 0, then n/a
     */
    public int maximumReasonableCompletionTime = -1;
    
    /** For auto XML */
    public MetaTeamPlanTemplate() {
        preconditions = new Vector();
        postconditions = new Vector();
        params = new Hashtable<String,Object>();
	this.paramDescriptors = new ArrayList<MTPTFieldDescriptor>();
        this.roleConstraints = new Vector();
    }    
    
    /** Creates a new instance of MetaTeamPlanTemplate
     * @param metaPlanName
     * Identifying name for the class of the plan
     * @param team
     * Belief about the team associated with this plan template
     * @param keys
     * Keys indicating template slots to be filled in upon instantiation
     * @param params
     * Parameters specific to this plan instantiation
     */
    public MetaTeamPlanTemplate(String metaPlanName,
				BeliefID team,
				Hashtable<String,Object> params,
				Vector preconditions,
				Vector postconditions,
				Vector<RoleBelief> roles) {
        this(new BeliefNameID("Plan Template "+metaPlanName),metaPlanName,team,params,preconditions,postconditions,roles,null,null,null);
    }

    public MetaTeamPlanTemplate(String metaPlanName,
				BeliefID team,
				Hashtable<String,Object> params,
				Vector preconditions,
				Vector postconditions,
				Vector<RoleBelief> roles,
				Vector constraints) {
        this(new BeliefNameID("Plan Template "+metaPlanName),metaPlanName,team,params,preconditions,postconditions,roles,constraints,null,null);
    }

    /** Creates a new instance of MetaTeamPlanTemplate
     * @param id
     * ID for this belief
     * @param metaPlanName
     * Identifying name for the class of the plan
     * @param team
     * Belief about the team associated with this plan template
     * @param keys
     * Keys indicating template slots to be filled in upon instantiation
     * @param params
     * Parameters specific to this plan instantiation
     */
    public MetaTeamPlanTemplate(BeliefID id,
				String metaPlanName,
				BeliefID team,
				Hashtable<String,Object> params,
				Vector preconditions,
				Vector postconditions,
				Vector roles) {
        this(id, metaPlanName, team, params, preconditions, postconditions, roles, new Vector(), null,null);
    }
    
    /** Creates a new instance of MetaTeamPlanTemplate
     * @param metaPlanName
     * Identifying name for the class of the plan
     * @param team
     * Belief about the team associated with this plan template
     * @param keys
     * Keys indicating template slots to be filled in upon instantiation
     * @param params
     * Parameters specific to this plan instantiation
     * @param paramDescriptors
     * description of param types, default values, and limits
     */
    public MetaTeamPlanTemplate(String metaPlanName,
				BeliefID team,
				Hashtable<String,Object> params,
				Vector preconditions,
				Vector postconditions,
				Vector<RoleBelief> roles,
				ArrayList<MTPTFieldDescriptor> paramDescriptors) {
        this(new BeliefNameID("Plan Template "+metaPlanName),metaPlanName,team,params,preconditions,postconditions,roles,null,paramDescriptors,null);
    }

    public MetaTeamPlanTemplate(String metaPlanName,
				BeliefID team,
				Hashtable<String,Object> params,
				Vector preconditions,
				Vector postconditions,
				Vector<RoleBelief> roles,
				Vector constraints,
				ArrayList<MTPTFieldDescriptor> paramDescriptors) {
        this(new BeliefNameID("Plan Template "+metaPlanName),metaPlanName,team,params,preconditions,postconditions,roles,constraints,paramDescriptors,null);
    }

    /** Creates a new instance of MetaTeamPlanTemplate
     * @param id
     * ID for this belief
     * @param metaPlanName
     * Identifying name for the class of the plan
     * @param team
     * Belief about the team associated with this plan template
     * @param keys
     * Keys indicating template slots to be filled in upon instantiation
     * @param params
     * Parameters specific to this plan instantiation
     * @param paramDescriptors
     * description of param types, default values, and limits
     */
    public MetaTeamPlanTemplate(BeliefID id, 
				String metaPlanName,
				BeliefID team,
				Hashtable<String,Object> params,
				Vector preconditions,
				Vector postconditions,
				Vector roles,
				ArrayList<MTPTFieldDescriptor> paramDescriptors) {
        this(id, metaPlanName, team, params, preconditions, postconditions, roles, new Vector(), paramDescriptors, null);
    }
    
    public MetaTeamPlanTemplate(BeliefID id, 
				String metaPlanName,
				BeliefID team,
				Hashtable<String,Object> params,
				Vector preconditions,
				Vector postconditions,
				Vector roles,
				Vector roleConstraints,
				ArrayList<MTPTFieldDescriptor> paramDescriptors,
				ArrayList<MTPTRoleDescriptor> roleDescriptors) {
        super(id);
        this.metaPlanName = metaPlanName;
        this.team = team;
        this.params = params;
        this.preconditions = preconditions;
        this.roles = roles;
        this.roleConstraints = roleConstraints;
        this.postconditions = postconditions;
	if(null == paramDescriptors) 
	    this.paramDescriptors = new ArrayList<MTPTFieldDescriptor>();
        else 
            this.paramDescriptors = paramDescriptors;
	if(null == roleDescriptors) 
	    this.roleDescriptors = new ArrayList<MTPTRoleDescriptor>();
        else 
            this.roleDescriptors = roleDescriptors;
    }
    
    // @TODO: Note, we do our best to make sure the lists and
    // hashtables aren't shared, and the MTPTFieldDescriptors and
    // MTPTRoleDescriptors are properly deep copied, but other objects
    // in lists and hash tables are probably not deep copied.
    public MetaTeamPlanTemplate(MetaTeamPlanTemplate other) {
	// Note, getParamDescriptors() already does a a deep copy.
	this.paramDescriptors = other.getParamDescriptors();

	this.metaPlanName = other.metaPlanName;
	this.team = other.team;

        this.params = new Hashtable<String,Object>();
	if(null != other.params)
	    this.params.putAll(other.params);

	this.preconditions = new Vector();
	if(null != other.preconditions)
	    this.preconditions.addAll(other.preconditions);

        this.postconditions = new Vector();
	if(null != other.postconditions)
	    this.postconditions.addAll(other.postconditions);

	this.roles = new Vector();
	if(null != other.roles)
	    this.roles.addAll(other.roles);

	// Note, getRoleDescriptors() already does a a deep copy.
	this.roleDescriptors = other.getRoleDescriptors();

        this.roleConstraints = new Vector();
	if(null != other.roleConstraints)
	    this.roleConstraints.addAll(other.roleConstraints);

        this.conditionTable = new Hashtable<String,MatchCondition>();
	if(null != other.conditionTable)
	    this.conditionTable.putAll(other.conditionTable);

	this.maximumReasonableCompletionTime = other.maximumReasonableCompletionTime;
    }    

    public void addParamDescription(MTPTFieldDescriptor descriptor) {
	paramDescriptors.add(descriptor);
    }

    public void addRoleDescription(MTPTRoleDescriptor descriptor) {
	roleDescriptors.add(descriptor);
    }


    public  String getName() { return metaPlanName; }

    public int getMaximumReasonableCompletionTime() {
        return maximumReasonableCompletionTime;
    }

    public String getMetaPlanName() {
        return metaPlanName;
    }

    public Vector<RoleBelief> getRoles() {
        return roles;
    }
    
    private Field getPublicField(Class fromClass, String fieldName, Class fieldClass) {
	Field retField=null;
	try {
	    retField = fromClass.getField(fieldName);
	}
	catch (java.lang.NoSuchFieldException e) {
	    Machinetta.Debugger.debug(1,"No public field "+fieldName+" class "+fieldClass.getName()+" is available on class "+fromClass.getName());
	    return null;
	}

	if(null != retField && null != fieldClass) {
	    int mod = retField.getModifiers();
	    Class retFieldClass = retField.getType();
	    if(Modifier.isPublic(mod) && retFieldClass.equals(fieldClass)) 
		return retField;
	}
	return null;
    }

    private static String teamName = "TeamAll";
    private static ProxyState state;
    private static TeamBelief teamBelief;

    private static TeamBelief getTeam() {
	if(null == state)
	    state = new ProxyState();
	Object o = state.getBelief(new BeliefNameID(teamName));
	if(null == o) {
	    Machinetta.Debugger.debug(1,"Null TeamBelief for teamName "+teamName);
	    return null;
	}
	if(!(o instanceof TeamBelief)) {
	    Machinetta.Debugger.debug(4,"NonNull TeamBelief but wrong class ("+o.getClass().toString()+" instead of TeamBelief) for teamName "+teamName);
	    return null;
	}
	return (TeamBelief)o;
    }

    private static ArrayList<RAPBelief> getTeamRAPBeliefs() {
	teamBelief = getTeam();
	if(null == teamBelief) {
	    Machinetta.Debugger.debug(4,"TeamBelief is null, couldn't get RAPBeliefs for teamName "+teamName);
	    return null;
	}

	ArrayList<RAPBelief> rapBeliefs = new ArrayList<RAPBelief>();
	Iterator iter = teamBelief.getMembers();
	while(iter.hasNext()) {
	    ProxyID pid = (ProxyID)iter.next();
	    Object o = state.getBelief(new BeliefNameID(pid.toString()));
	    if(null != o) {
		if(o instanceof RAPBelief) {
		    rapBeliefs.add((RAPBelief)o);
		}
	    }
	}
	return rapBeliefs;
    }


    public static void setCapableProxyList(String mtptName, MTPTRoleDescriptor roleDesc) {
	ArrayList<RAPBelief> rapBeliefs = getTeamRAPBeliefs();
	if(rapBeliefs == null) {
	    Machinetta.Debugger.debug(4,"CAN'T GET RAPBELIEFS!");
	    return;
	}
	Machinetta.Debugger.debug(1,"Got rapBeliefs size = "+rapBeliefs.size());

	
	ArrayList<ProxyID> capableProxyList = new ArrayList<ProxyID>();
	for(RAPBelief rapBelief: rapBeliefs) {
	    if(rapBelief.canPerformRole(roleDesc.getRole())) {
		Machinetta.Debugger.debug(1,"for mtpt "+mtptName+" role "+roleDesc.getRole().roleName+" rap "+rapBelief.getProxyID()+" can perform.");
		capableProxyList.add(rapBelief.getProxyID());
	    }
	}
	roleDesc.setCapableProxyList(capableProxyList);
    }

    public void setCapableProxyLists() {
	for(MTPTRoleDescriptor roleDesc: roleDescriptors) {
	    setCapableProxyList(metaPlanName, roleDesc);
	}
    }

    public TeamPlanTemplate generateTemplate(ArrayList<MTPTFieldDescriptor> planParams, 
					     ArrayList<MTPTRoleDescriptor> perRoleParams) {
        
	// ok, what we do in here is assemble the parameters that we
	// then pass to the constructor of TeamPlanTemplate.  Some of
	// these parameters are simply fields in our class, we just
	// use them straight up.  (@TODO: Should we clone/deep copy
	// those fields?  Do we need to?)  
	//
	// Other parameters are going to require some work; namely;
	// 
	// 1) we start with a copy of the params hash table but then
	// we iterate over the planParams parameter to this method to
	// add some new params.
	// 
	// 2) we also start with a copy of the roles vector but then
	// we use the perRoleParams to create roles and set their
	// params, and add them to the vector.
	// 
	// 3) Although we won't do this right now, eventually we are
	// going to do something similar with roleConstraints.

	
        Hashtable<String, Object> genParams = new Hashtable<String, Object>();
	
	// NOTE: PlanAgent uses different methods to decide on whether
	// to instantiate a plan, based on the
	// PLAN_INSTANTIATION_POLICY setting Configuration.  If the
	// policy is set to LOCAL then it will only instantiate the
	// plan if there are NO params and NO preconditions and NO
	// postconditions, OR if there is at least one locallySensed
	// belief.  So we add a locallySensed belief here.  Except it
	// wants a beliefID of a locally named belief. Agh.
	//
	// @TODO: this counter incrementing isn't thread safe... 
	BeliefNameID genByID = new BeliefNameID(PARAMS_GENERATED_BY_META_TEAM_PLAN_TEMPLATE+(beliefIDCounter++));
	BooleanBelief genByBelief = new BooleanBelief(genByID, true);
	genByBelief.setLocallySensed(true);
	(new ProxyState()).addBelief(genByBelief);
	genParams.put(PARAMS_GENERATED_BY_META_TEAM_PLAN_TEMPLATE, genByID);

	if(params != null)
	    genParams.putAll(params);
	for(MTPTFieldDescriptor fieldDescriptor: planParams) {
	    genParams.put(fieldDescriptor.getFieldName(), fieldDescriptor.getRequiredValue());
	}
        
        // Set params on the roles

	Class paramsClass = null;
	try {
	    paramsClass = Class.forName("java.util.Hashtable");
	    Machinetta.Debugger.debug(1,"Got Class for java.util.Hashtable");
	}
	catch (java.lang.ClassNotFoundException e) {
	    // this should _never_ happen.
	    Machinetta.Debugger.debug(3,"Can't get class for java.util.Hashtable??? How did THAT happen? Agh!");
	}

	// Start out with any of the roles with prespecified params
        Vector<RoleBelief> genRoles = new Vector<RoleBelief>(roles);


	Machinetta.Debugger.debug(1,"Processing role descriptors");

	// Now generate roles from descriptors - well really we're not
	// generating them as much as we're taking their arrays of per
	// role field descriptors and using reflection to set the
	// fields inside the roles.
        for (MTPTRoleDescriptor roleDesc: perRoleParams) {
	    // Get the RoleBelief and class
	    RoleBelief role = roleDesc.getRole();
	    if(null == role) {
		Machinetta.Debugger.debug( 3,"ERROR! for MTPT "+metaPlanName+", roleDesc param '"+roleDesc.getRoleDescription()+"' has role = null!  This should never happen.  Ignoring it for now.");
		continue;
	    }
	    Class roleClass = role.getClass();

	    Machinetta.Debugger.debug(1,"role ("+roleDesc.getRoleDescription()+") has type "+role.getClass().getName()+ " minCount "+roleDesc.getMinCount() + " maxCount "+roleDesc.getMaxCount()+" responsible "+roleDesc.getResponsible());
	    if(null != roleDesc.getResponsible())
		role.setResponsible(new RAPBelief(roleDesc.getResponsible()));
	    else
		role.setResponsible(null);
// 	    Machinetta.Debugger.debug(1,"    Dumping all public fields from reflection for this roleBelief");
// 	    // Not really necessary - delete later
// 	    Field fieldAry[] = roleClass.getFields();
//             for (int i = 0; i < fieldAry.length; i++) {
// 		Field fld = fieldAry[i];
// 		String fieldName = fld.getName();
// 		Class fieldClass = fld.getType();
// 		int mod = fld.getModifiers();
// 		Machinetta.Debugger.debug(1,"        name= " + fld.getName()+" modifiers= " + Modifier.toString(mod)+" type= " + fld.getType());
//             }
// 	    Machinetta.Debugger.debug(1,"    Done dumping all public fields");

	    // See if this RoleBelief has a params hashtable
	    // This _is_ necessary so don't delete later.   
	    Hashtable paramsHash = null;
	    Field paramsField = getPublicField(roleClass,NAME_OF_PARAMS_HASHTABLE,paramsClass);

	    Machinetta.Debugger.debug(1,"    Checking to see if this role has a public Hashtable named params");

	    if(null != paramsField) {
		try {
		    paramsHash = (Hashtable)paramsField.get(role);
		    Machinetta.Debugger.debug(1,"    Found the public Hashtable named params.");
		    if(null == paramsHash)
		       Machinetta.Debugger.debug(1,"    Aw, nope, it's null.  Hmm.  @TODO:  Should we instantiate it now then?");
		}
		catch (java.lang.IllegalAccessException e) {
		    // @TODO: Can this ever happen under normal circumstances?
		    Machinetta.Debugger.debug(1,"    TODO: Why can't I get the params field?  This should never happen - we shouldn't be trying to get it unless we've already found it.");
		}
	    }
	    else {
		Machinetta.Debugger.debug(1,"    Could not find a public Hashtable named params in this RoleBelief.  No big deal.");
	    }

	    // OK, GREAT, now we have the role params field, if it
	    // exists (and is public).  Whew.  

	    // OK, for each role param (i.e. each field descriptor in
	    // the roleDesc.genericParams list), look for a
	    // corresponding public field of the same name and class
	    // on the RoleBelief object, if found, set it, if not found,
	    // then if params is not null, throw it in there.

	    Machinetta.Debugger.debug(1,"    Processing role params");

	    ArrayList<MTPTFieldDescriptor> roleParams = roleDesc.getGenericParams();
	    if(null != roleParams) {
		for(MTPTFieldDescriptor pDesc : roleParams) {
		    String pName = pDesc.getFieldName();
		    Class pClass = pDesc.getFieldClass();
		    String pClassName = pClass.getName();
		    Object reqValue = pDesc.getRequiredValue();
		    Machinetta.Debugger.debug(1,"        role param '"+pName+"' ("+pDesc.getDescription()+") has type "+pClassName+" value '"+pDesc.getRequiredValue()+"'");

		    Field pField = getPublicField(roleClass, pName, pClass);
		    
		    // Did we find the field?
		    if(null == pField) {
			// No, ok so if a paramsHash was found, add this value to it.
			if(null != paramsHash) {
			    if(null == pName || null == reqValue) {
				Machinetta.Debugger.debug(1,"        name or type is null for role param named "+pName+" of type "+pClassName+" value '"+reqValue+"'");
			    }
 			    else {
				paramsHash.put(pName, reqValue);
				Machinetta.Debugger.debug(1,"        Added role param named "+pName+" of type "+pClassName+" value '"+reqValue+"' to params hash in role.");
 			    }
			}
			else {
			    Machinetta.Debugger.debug(3,"        ERROR, param "+pName+" of type "+pClassName+" value '"+reqValue+"' has NO field in role "+role+" of class "+roleClass.getName()+" and role has no param hash");
			}
			continue;
		    }

		    try {
			pField.set(roleClass, reqValue);
			Machinetta.Debugger.debug(1,"        Set role field for role param named "+pName+" of type "+pClassName+" value '"+reqValue+"'");
		    }
		    catch (java.lang.IllegalAccessException e) {
			// @TODO: Can this ever happen under normal circumstances?
			Machinetta.Debugger.debug(1,"TODO: Why can't I write to this field?  role "+role+" of class "+roleClass.getName()+", param named "+pName+" of type "+pClassName+" value '"+reqValue+"'");
		    }
		}
	    }

	    genRoles.add(role);
        }
        
        TeamPlanTemplate template = new TeamPlanTemplate(metaPlanName, team, genParams, preconditions, postconditions, genRoles, roleConstraints);
        
        return template;
    }
    
    /** Access specific parameters of this plan template
     * @param key
     * The attribute name of the desired parameter
     * @return
     * The object that is the value of the specified parameter
     */
    public  Object getParam(String key) {
        return params.get(key);
    }
    
    public Vector getPreconditions() { return preconditions; }
    public Vector getPostconditions() { return postconditions; }
        
    public BeliefID makeID() {
        return new Machinetta.State.BeliefNameID("MetaPlanTemplate"+metaPlanName);
    }    
    
    public String toString() {
        return getName() + ": " + paramDescriptors + ", " + roleConstraints;
    }
    
    public String dumpToString() {
	StringBuffer buf =new StringBuffer(10000);
	buf.append("TEMPLATE")
	    .append("name="+metaPlanName+"\n")
	    .append("team="+team.toString()+"\n");
	
	if(null == paramDescriptors)
	    buf.append("PlanParams = null\n");
	else {
	    buf.append("PlanParams.size() = "+paramDescriptors.size()+"\n");
	    if(paramDescriptors.size() > 0) {
		for(MTPTFieldDescriptor param : paramDescriptors) {
		    buf.append("    param=("+param+")\n");
		}
	    }
	}

	if(null == roles)
	    buf.append("roles = null\n");
	else {
	    buf.append("roles.size() = "+roles.size()+"\n");
	    if(roles.size() > 0) {
		for(RoleBelief role : roles) {
		    buf.append("    role=("+role+")\n");
		}
	    }
	}

	if(null == roleDescriptors)
	    buf.append("roleDescriptors = null\n");
	else {
	    buf.append("roleDescriptors.size() = "+roleDescriptors.size()+"\n");
	    if(roleDescriptors.size() > 0) {
		for(MTPTRoleDescriptor rd : roleDescriptors) {
		    buf.append("    roleDescriptor=("+rd+")\n");
		}
	    }
	}
	return buf.toString();
    }


    public static final long serialVersionUID = 1L;
}
