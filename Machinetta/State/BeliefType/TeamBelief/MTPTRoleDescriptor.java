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

package Machinetta.State.BeliefType.TeamBelief;
import Machinetta.State.BeliefType.ProxyID;

import java.util.ArrayList;

/**
 *
 * @author pscerri
 */
public class MTPTRoleDescriptor  implements java.io.Serializable {

    private RoleBelief role = null;
    private String roleDescription = null;
    private ArrayList<MTPTFieldDescriptor> genericParams = null;
    
    /** minCount == 0, means this role is optional */
    private int minCount = 0, maxCount = 1;
    
    private int count = 0;

    private ArrayList<ProxyID> capableProxyList = null;
    public ArrayList<ProxyID> getCapableProxyList() { return capableProxyList; }
    public void setCapableProxyList(ArrayList<ProxyID> value) { capableProxyList = value; }

    private ProxyID responsible = null;
    public ProxyID getResponsible() { return responsible; }
    public void setResponsible(ProxyID value) { responsible = value; }
    
    public MTPTRoleDescriptor() {

    }

    public MTPTRoleDescriptor(RoleBelief role, String desc, ArrayList<MTPTFieldDescriptor> genericParams, int minCount, int maxCount) {
        this.role = role;
        roleDescription = desc;
        this.genericParams = genericParams;
        this.minCount = minCount;
        this.maxCount = maxCount;
        
        // Defaults to min
        this.count = minCount;
    }

    public int getCount() {
        return count;
    }

    public ArrayList<MTPTFieldDescriptor> getGenericParams() {
        return genericParams;
    }

    public int getMaxCount() {
        return maxCount;
    }

    public int getMinCount() {
        return minCount;
    }

    public RoleBelief getRole() {
        return role;
    }

    public String getRoleDescription() {
        return roleDescription;
    }

    public void setCount(int count) {
        this.count = count;
    }
        
    public MTPTRoleDescriptor clone() {
        MTPTRoleDescriptor ret = new MTPTRoleDescriptor();
	ret.role = (RoleBelief)role.instantiate(null);
	ret.role.id = null;
        ret.roleDescription = roleDescription;

	ret.genericParams = new ArrayList<MTPTFieldDescriptor>();
	for(MTPTFieldDescriptor field: genericParams) {
	    //	    System.err.println("        Cloning RoleDescriptor's fieldDescriptor "+field.getFieldName());
	    ret.genericParams.add(field.clone());
	}

	ret.minCount = minCount;
	ret.maxCount = maxCount;
	ret.count = count;

	if(null != capableProxyList) {
	    ret.capableProxyList = new ArrayList<ProxyID>();
	    ret.capableProxyList.addAll(capableProxyList);
	}

	ret.responsible = responsible;

	return ret;
    }

    public String toString() {
	StringBuffer buf = new StringBuffer();
	buf.append(
		   "role="
		   +((null == role) ? "null" : role)
		   +" responsible="
		   +((null == responsible) ? "null" : responsible)
		   +" desc="+roleDescription
		   +" minCount="+minCount
		   +" maxCount="+maxCount
		   +" count="+count
		   +" params=");
	if(null == genericParams)
	    buf.append("( null )");
	else {
	    buf.append("( size="+genericParams.size());
	    for(int loopi = 0; loopi < genericParams.size(); loopi++) {
		buf.append(" ");
		MTPTFieldDescriptor f = genericParams.get(loopi);
		if(null == f)
		    buf.append(" [ null ]");
		else 
		    buf.append(" [ ").append(f.toString()).append(" ]");
	    }
	    buf.append(")");
	}
	return buf.toString();
    }
}
