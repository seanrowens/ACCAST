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
package Machinetta.State.BeliefType.TeamBelief;

public class MTPTFieldDescriptor implements java.io.Serializable {

    public Class fieldClass = null;
    public String fieldName  = null;
    public String description  = null;
    public Object defaultValue = null;
    public Object minLimit = null;
    public Object maxLimit = null;

    // I'm not entirely sure but I think requiredValue is meant to
    // hold the actual value - i.e. this class describes a value we
    // need, we ask the user or whatever and put the result in here. -
    // SRO Tue Nov 11 21:04:11 EST 2008

    public Object requiredValue = null;
    
    public MTPTFieldDescriptor() {
    }

    // Any of description, default, min and max may be null.
    public MTPTFieldDescriptor(Class fieldClass, String fieldName, String description, Object defaultValue, Object minLimit, Object maxLimit) {
	this.fieldClass = fieldClass;
	this.fieldName = fieldName;
        this.description = description;
	this.defaultValue = defaultValue;
	this.minLimit = minLimit;
	this.maxLimit = maxLimit;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public String getDescription() {
        return description;
    }

    public Class getFieldClass() {
        return fieldClass;
    }

    public String getFieldName() {
        return fieldName;
    }

    public Object getMaxLimit() {
        return maxLimit;
    }

    public Object getMinLimit() {
        return minLimit;
    }

    public Object getRequiredValue() {
        return requiredValue;
    }

    public void setRequiredValue(Object requiredValue) {
        this.requiredValue = requiredValue;
    }
    
    public MTPTFieldDescriptor clone() {
        MTPTFieldDescriptor ret = new MTPTFieldDescriptor();
	ret.fieldClass = fieldClass;
	ret.fieldName = fieldName;
	ret.description = description;
	ret.defaultValue = defaultValue;
	ret.requiredValue = requiredValue;
	ret.minLimit = minLimit;
	ret.maxLimit = maxLimit;
        
        return ret;
    }


    public String toString() {
	return 
	    "fieldClass=" 
	    +((null == fieldClass) ? "null" : fieldClass.getName())
	    +" fieldName="
	    +((null == fieldName) ? "null" : fieldName)
	    +" description="
	    +((null == description) ? "null" : description)
	    +" defaultValue="
	    +((null == defaultValue) ? "null" : defaultValue)
	    +" minLimit="
	    +((null == minLimit) ? "null" : minLimit)
	    +" maxLimit="
	    +((null == maxLimit) ? "null" : maxLimit)
	    ;
    }
}
