/*
 * Copyright (c) Erasmus MC
 *
 * This file is part of TheMatrix.
 *
 * TheMatrix is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.erasmusmc.jerboa.calculations;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;



public class StratifiedExact {
	public static double CONFIDENCELEVEL = 0.95;
	private int MAXITER = 10000;          // Max # of iterations to bracket/converge to a root, originally 10,000
	private double TOLERANCE = 0.0000001 ;   // Relative tolerance in results 
	private static enum DataType {STRATIFIED_CC,MATCHED_CC,STRATIFIED_PERSONTIME};
	private List<Table> tables;
	private List<Big> polyD;
	private List<Big> polyN;
	private double value;
	private int sumA;
	private int minSumA;
	private int maxSumA;

	/*
	public static void main(String[] args) {
		Data data;
		Map<String, Data> key2dataCases = new HashMap<String, Data>();
		data = new Data();
		data.events = 20;
		data.days = 500000;
		key2dataCases.put("a:1", data);

		data = new Data();
		data.events = 90;
		data.days = 400000;
		key2dataCases.put("b:1", data);

		Map<String, Data> key2dataBackground = new HashMap<String, Data>();
		data = new Data();
		data.events = 98;
		data.days = 2000000 + 500000;
		key2dataBackground.put("a:1", data);

		data = new Data();
		data.events = 359;
		data.days = 1200000 + 400000;
		key2dataBackground.put("b:1", data);


		System.out.println(new StratifiedExact().calculateExactP(key2dataCases, key2dataBackground)*2 + " should be 0.979285758233616");
	}
	 */
	public Result calculateExactStats(Map<String, PersonTimeData> key2dataCases, Map<String, PersonTimeData> key2dataBackground) {
		tables = new ArrayList<Table>();
		for (Map.Entry<String, PersonTimeData> entry : key2dataCases.entrySet()){
			PersonTimeData background = key2dataBackground.get(entry.getKey());
			PersonTimeData data = entry.getValue();
			Table table = new Table();
			table.a = data.events;
			table.b = background.events - data.events;
			table.n1 = data.days;
			table.n0 = background.days-data.days;
			table.m1 = table.a + table.b;
			table.freq = 1;
			table.informative = (table.a * table.n0 != 0) || ((table.b * table.n1) != 0);
			tables.add(table);
		}
		return calculateStats(DataType.STRATIFIED_PERSONTIME);
	}
	
	public Result calculateExactStats(Map<String, CaseControlData> key2data) {
		tables = new ArrayList<Table>();
		for (CaseControlData data : key2data.values()){
			Table table = new Table();
			table.a = data.caseAndExposed;
			table.b = data.caseNotExposed;
			int c = data.controlAndExposed;
			int d = data.controlNotExposed;
			table.n1 = table.a+c;
			table.n0 = table.b+d;
			table.m1 = table.a + table.b;
			table.freq = 1;
			table.informative = (table.a * d != 0) || ((table.b * c) != 0);
			tables.add(table);
		}
		return calculateStats(DataType.STRATIFIED_CC);
	}
	
	private Result calculateStats(DataType dataType) {
		calcSums(dataType);
		calcPoly(dataType);
		Result result = new Result();
		calcExactPVals(result);
		result.cmle = calcCmle(1);
		double approx;
		if (Double.isNaN(result.cmle) || Double.isInfinite(result.cmle))
			approx = maxSumA;
		else
			approx = result.cmle;

		if (dataType==DataType.STRATIFIED_PERSONTIME && (tables.get(0).b==0)) 
			approx = result.cmle;	
		
    result.upFishLimit = calcExactLim (false, true, approx, CONFIDENCELEVEL);
    result.upMidPLimit = calcExactLim (false, false, approx, CONFIDENCELEVEL);
    result.loFishLimit = calcExactLim (true, true, approx, CONFIDENCELEVEL);
    result.loMidPLimit = calcExactLim (true, false, approx, CONFIDENCELEVEL);
		return result;
	}

	private void calcSums(DataType dataType) {
		sumA = 0;
		minSumA = 0;
		maxSumA = 0;
		for (Table table : tables)
			if (table.informative){
				sumA += Math.round(table.a * table.freq);
				if (dataType == DataType.STRATIFIED_CC || dataType == DataType.MATCHED_CC )	{
					minSumA += Math.round(Math.max(0, table.m1 - table.n0) * table.freq);
					maxSumA += Math.round(Math.min(table.m1, table.n1) * table.freq); 
				} else if (dataType == DataType.STRATIFIED_PERSONTIME) {
					minSumA = 0;
					maxSumA += Math.round(table.m1 * table.freq); 
				}
			}
	}

	private void calcPoly(DataType dataType){
		//	This routine outputs the "main" polynomial of conditional distribution
		//	coefficients which will subsequently be used to calculate the conditional
		//	maximum likelihood estimate, exact confidence limits, and exact P-values.
		//	The results are placed in the global variables, polyD and .degD.
		//	For a given data set, this routine MUST be called once before calling
		//	CalcExactPVals(), CalcCmle(), and CalcExactLim(). 
		if (dataType == DataType.STRATIFIED_CC)
			polyD = polyStratCC (tables.get(0));
		else if (dataType == DataType.MATCHED_CC)
			polyD = polyMatchCC (tables.get(0));
		else if (dataType == DataType.STRATIFIED_PERSONTIME)
			polyD = polyStratPT (tables.get(0));
		for (int i = 1; i < tables.size(); i++){ 
			Table table = tables.get(i);
			if (table.informative) {
				List<Big> poly1 = new ArrayList<Big>(polyD);
				List<Big> poly2 = null;
				if (dataType == DataType.STRATIFIED_CC)
					poly2 = polyStratCC(table);
				else if (dataType == DataType.MATCHED_CC)
					poly2 = polyMatchCC(table);
				else if (dataType == DataType.STRATIFIED_PERSONTIME)
					poly2 = polyStratPT(table);
				polyD = multiplyPoly(poly1, poly2);
			} 
		}     
	}

	private void calcExactPVals(Result result){
		int difference = sumA - minSumA;
		Big upTail = new Big(polyD.get(polyD.size()-1));
		for (int i = polyD.size()-1 - 1; i >= difference; i--)
			upTail.addToThis(polyD.get(i));

		Big denom = new Big(upTail);
		for (int i = difference - 1; i >= 0; i-=1)
			denom.addToThis(polyD.get(i));

		result.upFishPVal = upTail.divide(denom).asDouble();
		result.loFishPVal = 1.0 - upTail.substract(polyD.get(difference)).divide(denom).asDouble();
		result.upMidPPVal = upTail.substract(polyD.get(difference).multiply(new Big(0.5d))).divide(denom).asDouble();
		result.loMidPPVal = 1.0 - result.upMidPPVal;
	}


	private double calcCmle(double approx) {
		if (minSumA < sumA && sumA < maxSumA)    
			return getCmle(approx);
		else if (sumA == minSumA) 
			return 0;
		else if (sumA == maxSumA) 
			return Double.POSITIVE_INFINITY;
		return Double.NaN;
	}

	private double getCmle(double approx) {
		value = sumA;  //   { The sum of the observed "a" cells }
		polyN = new ArrayList<Big>(polyD.size());
		for(int i = 0; i<polyD.size(); i++) //  { Defines the numerator polynomial }
			polyN.add(polyD.get(i).multiply(new Big(minSumA+i)));
		return converge(approx);  //         { Solves so that Func(cmle) = 0 }
	}

	private double calcExactLim(boolean lower, boolean fisher, double approx, double confLevel)	{
		double limit = 0;
		if (minSumA < sumA && sumA < maxSumA) 
			limit = getExactLim(lower, fisher, approx, confLevel);
		else if (sumA == minSumA) {
			if (lower) 
				limit = 0;
			else
				limit = getExactLim(lower, fisher, approx, confLevel);
		} else if (sumA == maxSumA) {
			if (!lower) 
				limit = Double.POSITIVE_INFINITY;
			else
				limit = getExactLim(lower, fisher, approx, confLevel);
		}
		return limit;
	}

	private double getExactLim(boolean lower, boolean fisher, double approx, double confLevel) {
		double limit;
		int degN;
		if ( lower ) 
			value = 0.5 * (1 + confLevel); 
		else
			value = 0.5 * (1 - confLevel);  

		if (lower && fisher) 
			degN = sumA - minSumA - 1;
		else
			degN = sumA - minSumA;
		polyN = new ArrayList<Big>(degN);

		for(int i = 0; i <= degN; i++)
			polyN.add(polyD.get(i));

		if (!fisher) 
			polyN.set(degN, polyD.get(degN).multiply(new Big(0.5)));//= (0.5) * this.polyD[this.degN] // Mid-P adjustment }
		limit = converge(approx);  //       { Solves so that Func(pvLimit) = 0 }
		return limit;
	}

	private double converge(double approx){
		//Returns the root or an error 
		Nums nums = bracketRoot(approx);
		double rootc=zero(nums);
		if (nums.error==0)
			return rootc;
		else
			return Double.NaN;  
	}

	private Nums bracketRoot(double approx)	{
		Nums nums = new Nums();
		int iter = 0;
		double x1 = Math.max(0.5, approx); //   { X1 is the upper bound }
		double x0 = 0;  //              { X0 is the lower bound }
		double f0 = func(x0); //   { Func at X0 }
		double f1 = func(x1); //  { Func at X1 }
		while ((f1 * f0) > 0.0 && (iter < MAXITER))  {
			iter++;
			x0 = x1; 
			f0 = f1;
			x1 = x1 * 1.5 * (double)iter;
			f1 = func(x1);
		}
		nums.x1=x1;
		nums.x0=x0;
		nums.f1=f1;
		nums.f0=f0;
		return nums;
	}

	private double func(double r)	{
		Big numer = evalPoly(polyN,r);
		Big denom = evalPoly(polyD,r);  
		return numer.divide(denom).asDouble() - (value);
	}

	private Big evalPoly(List<Big> c, double r) {
		if (r == 0) 
			return c.get(0);
		else {
			int degC = c.size()-1;
			Big bigR = new Big(r);
			Big y = c.get(degC);
			for(int i = (degC - 1);i>= 0; i-=1){
				y =  y.multiply(bigR);
				y.addToThis(c.get(i)); //y * (r) + c[i]
			} 
			return y;
		}
	}


	private double zero(Nums nums){
		//Takes in an array of x0,x1,f0, and f1 and returns a root or an error
		double root;
		boolean found=false;//              { Flags that a root has been found }
		double x2, f2, swap; //        { Newest point, Func(X2), storage variable }
		int iter = 0;  //               { Current  of iterations }
		double x0=nums.x0;
		double x1=nums.x1;
		double f0=nums.f0;
		double f1=nums.f1;
		double error = 0; //  { Initialize }
		if (Math.abs(f0) < Math.abs(f1)) {
			//             { Make X1 best approx to root }
			swap = x0;
			x0 = x1;
			x1 = swap;
			swap = f0;
			f0 = f1;
			f1 = swap;
		}
		found = (f1 == 0);    //    { Test for root }
		if (!found && (f0 * f1) > 0) 
			error = 1; //            { Root not bracketed }

		while (!(found) && (iter < MAXITER) && (error == 0)) {
			iter++;
			x2 = x1 - f1 * (x1 - x0) / (f1 - f0);
			f2 = func(x2);
			if ( f1 * f2 < 0 ) {
				//                      { X0 not retained }
				x0 = x1;
				f0 = f1;
			} else {
				//    { X0 retained => modify F0 }
				f0 = f0 * f1 / (f1 + f2); // { The Pegasus modification }
			}
			x1 = x2;
			f1 = f2;
			found = ((Math.abs(x1 - x0) < (Math.abs(x1) * this.TOLERANCE)) || (f1 == 0));
		} //end of while loop
		root = x1; // { Estimated root }
		if ( !(found) && (iter >= this.MAXITER) && (error == 0) ) 
			error=2;                 // Too many iterations 
		nums.error=error;
		return root;
	}

	private List<Big> multiplyPoly(List<Big> p1, List<Big> p2) {
		/*
	  double[] p1D = new double[p1.size()];
		for (int i = 0; i < p1.size(); i++)
			p1D[i] = p1.get(i).asDouble();
		
	  double[] p2D = new double[p2.size()];
		for (int i = 0; i < p2.size(); i++)
			p2D[i] = p2.get(i).asDouble();		
		
		int degree = p1.size()-1 + p2.size()-1;
		double[] resultD = new double[degree+1];
		for (int i = 0; i <= degree; i++)
			resultD[i] = 0;
		for (int i = 0; i < p1.size(); i++)
			for (int j = 0; j < p2.size(); j++)
				resultD[i+j] = resultD[i+j] + (p1D[i]*p2D[j]);	
		List<Big> result = new ArrayList<Big>(degree);
		for (int i = 0; i <= degree; i++)
			result.add(new Big(resultD[i]));
		return result;
		*/
		int degree = p1.size()-1 + p2.size()-1;
		List<Big> result = new ArrayList<Big>(degree+1);
		for (int i = 0; i <= degree; i++)
			result.add(new Big(0d));

		for (int i = 0; i < p1.size(); i++)
			for (int j = 0; j < p2.size(); j++)
				result.get(i+j).multiplyAndAddToThis(p1.get(i),p2.get(j));

		return result;
	}

	private List<Big> polyStratPT(Table table) {
		if (table.informative)
			return binomialExpansion ((table.n0 / (double)table.n1), 1.0, table.m1);
		else {
			List<Big> poly = new ArrayList<Big>(1);
			poly.add(new Big(1d));
			return poly; 
		}
	}

	private List<Big>  binomialExpansion(double c0, double c1, int degree) {
		List<Big> p = new ArrayList<Big>(degree);
		for (int i = 0; i <= degree; i++)
			p.add(null);
		p.set(degree,new Big(c1).power(degree));
		for (int i = degree - 1; i >= 0; i--)
			p.set(i,  p.get(i + 1).multiply(new Big(c0 * (i + 1) / (c1 *(degree - i)))));
		return p;
	}  

	private List<Big> polyMatchCC(Table curTable) {
		// TODO Auto-generated method stub
		return null;
	}

	private List<Big> polyStratCC(Table curTable) {
		List<Big> poly = new ArrayList<Big>(1);
		poly.add(new Big(1d));
		if (curTable.informative){
			long minA = Math.max(0,curTable.m1 - curTable.n0);
			long maxA = Math.min(curTable.m1, curTable.n1);
			long degDi = maxA - minA;
			long aa = minA;
			long bb = curTable.m1 - minA + 1;
			long cc = curTable.n1 - minA + 1;
			long dd = curTable.n0 - curTable.m1 + minA;
			for (int i = 1; i <= degDi; i++)
				poly.add(poly.get(i-1).multiply(new Big(((bb - i) / (double)(aa + i)) * ((cc - i) / (double)(dd + i)))));
		}
		return poly;
	}

	private class Table {
		int a;
		int b;
		long n1;
		long n0;
		int m1;
		long freq;
		boolean informative;
	}

	private class Nums {
		double x0;
		double x1;
		double f0;
		double f1;
		double error;
	}

	public static class Result{
		public double upFishPVal;
		public double loFishPVal;
		public double upMidPPVal;
		public double loMidPPVal;
		public double cmle;
		public double upFishLimit;
		public double loFishLimit;
		public double upMidPLimit;
		public double loMidPLimit;

	}
	/**
	 * This class is a wrapper around a double to increase its exponent size to 64 bits. Needed because of the large internal numbers generated in this class.
	 * @author schuemie
	 *
	 */
	private static class Big {
		private double mantissa;
		private int exp;
		private static final int MANTISSASIZE = 52; //in bits

		public Big(double d){
			if (d == 0){
				exp = 1;
				mantissa = 0;
			} else {
				exp = Math.getExponent(d);
				mantissa = d / Math.pow(2, exp);
			}
		}

		public Big(Big big){
			exp = big.exp;
			mantissa = big.mantissa;
		}

		private static int floorLog2(int d){
			return (int)Math.floor(log2(Math.abs(d)));
		}

		private static double log2(double x) {
			return Math.log(x) / logOf2;
		}

		private static double logOf2 = Math.log(2);

		public double asDouble(){
			return mantissa * Math.pow(2, exp);
		}

		public Big multiply(Big value){
			Big result = new Big(this);
			result.mantissa *= value.mantissa;
			result.exp += value.exp;
			result.normalize();
			return result;
		}

		public Big divide(Big value){
			Big result = new Big(this);
			result.mantissa /= value.mantissa;
			result.exp -= value.exp;
			result.normalize();
			return result;
		}

		public Big power(int value){
			Big result = new Big(this);
			if (value == 0){
				result.mantissa = 1;
				result.exp = 0;
			} else {

				int floor = floorLog2(value);
				for (int i = 0; i < floor; i++){
					result.mantissa *= result.mantissa;
					result.exp += result.exp;
					if (Math.getExponent(result.mantissa) > 100 || Math.getExponent(result.mantissa) < -100)
						result.normalize();
				}
				for (int i = (int)Math.pow(2,floor); i < value;i++){
					result.mantissa *= mantissa;
					result.exp += exp;
					if (Math.getExponent(result.mantissa) > 100 || Math.getExponent(result.mantissa) < -100)
						result.normalize();
				}

			}
			return result;
		}

		private void normalize(){
			long tempExp = Math.getExponent(mantissa);
			exp += tempExp;
			mantissa = mantissa / Math.pow(2, tempExp);//(double)(1<<(tempExp));//Math.pow(2, tempExp);

		}

		public void addToThis(Big value){
			if (exp > value.exp){
				if (exp-value.exp > MANTISSASIZE)
					return; // Number to be added is too small (falls below mantissa precision)
				mantissa += value.mantissa / (double)(1l<<(exp-value.exp));//(double)(1<<(exp-value.exp));//Math.pow(2, exp-value.exp);
			} else {
				if (value.exp - exp > MANTISSASIZE){
					mantissa = value.mantissa;
					exp = value.exp;
				} else {
				  mantissa = value.mantissa + mantissa /(double)(1l<<(value.exp-exp));//(double)(1<<(value.exp-exp));//Math.pow(2, value.exp-exp);
				  exp = value.exp;
				}
			}
		}

		public void multiplyAndAddToThis(Big value1, Big value2){
			int tempExp = value1.exp;
			tempExp += value2.exp;
			if (exp > tempExp){
				if (exp-tempExp > MANTISSASIZE)
					return; // Number to be added is too small (falls below mantissa precision)
				else {
			   double tempMantissa = value1.mantissa;
			   tempMantissa *= value2.mantissa;
			   mantissa += tempMantissa / (double)(1l<<(exp-tempExp));
				}
			} else {
			   double tempMantissa = value1.mantissa;
			   tempMantissa *= value2.mantissa;
				if (tempExp-exp > MANTISSASIZE){
					mantissa = tempMantissa;
					exp = tempExp;		
				} else {
					mantissa = tempMantissa + mantissa / (double)(1l<<(tempExp-exp));
					exp = tempExp;
				}
			}
			normalize();
		}

		public Big substract(Big value){
			Big result = new Big(this);
			if (exp > value.exp){
				result.mantissa = mantissa - value.mantissa / Math.pow(2, exp-value.exp);
				result.exp = exp;
			}else {
				result.mantissa = mantissa / Math.pow(2, value.exp-exp) - value.mantissa;
				result.exp = value.exp;
			}
			return result;
		}
	}

}
