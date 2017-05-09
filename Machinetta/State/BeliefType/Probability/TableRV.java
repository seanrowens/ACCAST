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
 * TableRV.java
 *
 * Created on October 14, 2002, 2:21 PM
 */

package Machinetta.State.BeliefType.Probability;

import java.util.Hashtable;
import java.util.Enumeration;
/**
 *
 * @author  pynadath
 */
public class TableRV extends RandomVariable {

    /** The table containing the distribution */
    public Hashtable probTable = null;
    
    /** The maximum domain value, stored here for convenience */
    public double maxDomainValue = Double.NEGATIVE_INFINITY;
    
    /** For auto XML */
    public TableRV() {}
    
    public Machinetta.State.BeliefID makeID() {
        return new Machinetta.State.BeliefNameID("AnonymousRandomVariable");
    }    
    
    /** Creates a new instance of TableRV
     * NOTE: Does not support domains with negative values !!!
     * @param prob A hashtable specifying the distribution of this RV where P(RV=key)=value (with both key, value being of type Double)
     */
    public TableRV(Hashtable prob) {
        probTable = prob;
        /** Let's figure out what the maximum allowed domain value is */
        for (Enumeration keyList = prob.keys(); keyList.hasMoreElements(); ) {
            double domainValue = ((Double)keyList.nextElement()).doubleValue();;
            if (domainValue > maxDomainValue)
                maxDomainValue = domainValue;
        }
    }
    
    /** @return The largest possible domain value  */
    public double getMaxValue() { return maxDomainValue; }
    
    /** @return An enumeration of the domain elements */
    public Enumeration getDomain() { return probTable.keys(); }
    
    /** Accesses the cumulative distribution function of this RV
     * @param threshold
     * @return For RV X, returns F_X(threshold)
     */
    public double getProbLessThan(double threshold) {
        double probability = 0.0;
        for (Enumeration keyList = probTable.keys(); keyList.hasMoreElements(); ) {
            Double domainValue = (Double)keyList.nextElement();
            if (domainValue.doubleValue() < threshold) {
                Double probValue = (Double)probTable.get(domainValue);
                probability = probability + probValue.doubleValue();
            }
        }
        return probability;
    }
    
    /** Access the probability mass function of this RV
     * @param domainValue
     * @return For RV X, returns f_X(domainValue)
     */
    public double getProbability(double domainValue) {
        Double probValue = (Double)probTable.get(new Double(domainValue));
        if (probValue == null)
            return 0.0;
        else
            return probValue.doubleValue();
    }
    
    
    public String toString() { return probTable.toString(); }
        
    /** @return The expected value of this random variable  */
    public double getExpectation() {
        double expectedValue = 0.0;
        for (Enumeration domain = getDomain(); domain.hasMoreElements(); ) {
            double domainValue = ((Double)domain.nextElement()).doubleValue();
            double probValue = getProbability(domainValue);
            expectedValue = expectedValue + probValue*domainValue;
        }
        return expectedValue;
    }    
    
    public static final long serialVersionUID = 1L;
}
