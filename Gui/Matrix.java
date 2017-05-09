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
package Gui;

// @author      Sean R. Owens
// @version     $Id: Matrix.java,v 1.2 2006/07/10 21:54:22 owens Exp $ 

public class Matrix 
{
    private int iDF = 0;

    //--------------------------------------------------------------
    public double[][] adjoint(double[][] originalMatrix) throws Exception
    {
	int matrixWidth = originalMatrix.length;
	double resultMatrix[][] = new double[matrixWidth][matrixWidth];
	int ii, jj, ia, ja;
	double det;
	for (int loopi=0; loopi < matrixWidth; loopi++)
	    for (int loopj=0; loopj <matrixWidth; loopj++) {
		ia = ja = 0;
		double ap[][] = new double[matrixWidth-1][matrixWidth-1];
		for (ii=0; ii < matrixWidth; ii++) {
		    for (jj=0; jj < matrixWidth; jj++) {
			if ((ii != loopi) && (jj != loopj)) {
			    ap[ia][ja] = originalMatrix[ii][jj];
			    ja++;
			}
		    }
		    if ((ii != loopi ) && (jj != loopj)) {
			ia++; 
		    }
		    ja=0; 
		}
		det = determinant(ap);
		resultMatrix[loopi][loopj] = (double)Math.pow(-1, loopi+loopj) * det;
	    }
	resultMatrix = transpose(resultMatrix);
	return resultMatrix;
    }

    //--------------------------------------------------------------
    public double[][] upperTriangle(double[][] matrix) {
	double f1 = 0;   double temp = 0;
	int matrixWidth = matrix.length;  // get This Matrix Size (could be smaller than global)
	int v=1;
	iDF=1;
	for (int col=0; col < matrixWidth-1; col++) {
	    for (int row=col+1; row < matrixWidth; row++) {
		v=1;
		outahere:
		// check if 0 in diagonal
		while(matrix[col][col] == 0) {
		    // if so switch until not
		    // check if switched all rows
		    if (col+v >= matrixWidth) {
			iDF=0;
			break outahere; 
		    }
		    else {
			for(int c=0; c < matrixWidth; c++) {
			    temp = matrix[col][c];
			    matrix[col][c] = matrix[col+v][c];       // switch rows
			    matrix[col+v][c] = temp;   
			}
			v++;                            // count row switchs
			iDF = iDF * -1;                 // each switch changes determinant factor
		    }
		}
		if ( matrix[col][col] != 0 ) {
		    try {
			f1 = (-1) * matrix[row][col] / matrix[col][col];
			for (int loopi=col; loopi < matrixWidth; loopi++) {
			    matrix[row][loopi] = f1*matrix[col][loopi] + matrix[row][loopi]; 
			}
		    }
		    catch(Exception e) {
			System.out.println(e);
			e.printStackTrace();
		    }
		}
	    }
	}
	return matrix;
    }

    //--------------------------------------------------------------
    public double determinant(double[][] matrix) {
	int matrixWidth = matrix.length;
	double det=1;
	matrix = upperTriangle(matrix);
	for (int loopi=0; loopi < matrixWidth; loopi++) {
	    det = det * matrix[loopi][loopi]; 
	}
	// multiply down diagonal
	det = det * iDF;                    // adjust w/ determinant factor
	return det;
    }

    //--------------------------------------------------------------
    public double[][] transpose(double[][] originalMatrix) {
	int matrixWidth = originalMatrix.length;
	double transposed[][] = new double[matrixWidth][matrixWidth];
	for (int loopi=0; loopi < matrixWidth; loopi++)
	    for (int loopj=0; loopj < matrixWidth; loopj++) {
		transposed[loopi][loopj] = originalMatrix[loopj][loopi];
	    }
	return transposed;
    }

    //--------------------------------------------------------------
    public double[][] inverse(double[][] originalMatrix) throws Exception
    {

	// Formula used to Calculate Inverse:
	// inv(A) = 1/det(ORIGINALMATRIX) * adj(ORIGINALMATRIX)
	int matrixWidth = originalMatrix.length;
	double[][] matrix = new double[matrixWidth][matrixWidth];
	for(int loopi = 0; loopi < matrixWidth; loopi++) 
	    for(int loopj = 0; loopj < matrixWidth; loopj++) 
		matrix[loopi][loopj] = originalMatrix[loopi][loopj];

	double inverted[][] = new double[matrixWidth][matrixWidth];
	double adjointed[][] =  adjoint(matrix);
	double det = determinant(matrix);
	double dd = 0;
	if (det == 0) {
	    //        statusBar.setText("determinant Equals 0, Not Invertible."); 
	    return null;
	}
	else {
	    dd = 1/det;
	}
	for (int loopi=0; loopi < matrixWidth; loopi++)
	    for (int loopj=0; loopj <matrixWidth; loopj++) {
		inverted[loopi][loopj] = dd * adjointed[loopi][loopj]; 
	    }
	return inverted;
    }
}
