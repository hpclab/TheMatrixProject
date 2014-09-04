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

import org.erasmusmc.math.ChiSquaredProbabilityFunction;
import org.erasmusmc.math.GaussianDistribution;

public class MetaAnalysis {
	
	public static enum Model {RANDOM,FIXED};
	
	public static void main(String[] args) {
		Double[] rrs = new Double[4];
		Double[] ci95ups = new Double[4];
		rrs[0] = 2d;
		rrs[1] = 2d;
		rrs[2] = 5d;
		rrs[3] = 1.2d;
		ci95ups[0] = 4d;
		ci95ups[1] = 7d;
		ci95ups[2] = 7d;
		ci95ups[3] = 4d;
		
		System.out.println("Random effects: ");
		Result result = analyse(rrs, ci95ups2StandardErrors(ci95ups,rrs), Model.RANDOM);
		System.out.println("RR = " + result.rr + " should be 2.516");
		System.out.println("CI95down = " + result.ci95Down);
		System.out.println("CI95up = " + result.ci95Up + " should be 5.174");
		System.out.println("p = " + result.p);
		System.out.println("p for homogeneity = " + result.pForHomogeneity + " should be 0.016");
		System.out.println();
		System.out.println("Fixed effects: ");
		result = analyse(rrs, ci95ups2StandardErrors(ci95ups,rrs), Model.FIXED);
		System.out.println("RR = " + result.rr + " should be 3.764");
		System.out.println("CI95down = " + result.ci95Down);
		System.out.println("CI95up = " + result.ci95Up + " should be 5.010");
		System.out.println("p = " + result.p);
		System.out.println("p for homogeneity = " + result.pForHomogeneity + " should be 0.016");
		
	}
	
	/**
	 * Helper function for converting confidence intervals to standard errors
	 * @param ci95ups	Upper bound of the 95% confidence interval
	 * @param rrs	Point estimate of the relative risk
	 * @return
	 */
	public static Double[] ci95ups2StandardErrors(Double[] ci95ups, Double[] rrs){
		Double[] ses = new Double[ci95ups.length];
		for (int i = 0; i < ci95ups.length; i++)
			ses[i] = ci95up2StandardError(ci95ups[i], rrs[i]);
		return ses;
	}
	
	public static Double ci95up2StandardError(double ci95up, double rr){
		return (Math.log(ci95up)-Math.log(rr))/1.96;
	}
	
	
	/**
	 * Based on http://www.sciencedownload.net/demodownload/Fixed%20effect%20vs.%20random%20effects.pdf
	 * pages 13-
	 * 
	 * Checked with EpiSheet.xls
	 * 
	 * @param rrs
	 * @param ci95ups
	 * @return
	 */
	public static Result analyse(Double[] rrs, Double[] standardErrors, Model model) {
		double[] lnRRs = new double[rrs.length];
		double[] vars = new double[rrs.length];
		double[] w = new double[rrs.length];
		double[] lnRRxW = new double[rrs.length];
		double[] chi2weighted = new double[rrs.length];
		double sumW = 0;
		double sumWsquared = 0;
		double sumWxRR = 0;
		double sumWxRRsquared = 0;
		double sumlnRRxW = 0;
		double sumChi2weighted = 0;
		double betweenStudyVar = 0;
		double df = rrs.length-1;
		for (int i = 0; i < rrs.length; i++){
			lnRRs[i] = Math.log(rrs[i]);
			vars[i] = Math.pow(standardErrors[i],2);
			w[i] = 1/vars[i]; //Weight is inverse variance
			sumW+= w[i];
		}
		
		for (int i = 0; i < rrs.length; i++){
			lnRRxW[i] = lnRRs[i] * w[i] / sumW;
			sumlnRRxW+= lnRRxW[i];
		}
		for (int i = 0; i < rrs.length; i++){
			chi2weighted[i] = Math.pow(lnRRs[i] - sumlnRRxW,2) * w[i];
			sumChi2weighted+= chi2weighted[i];
		}	

		if (model == Model.RANDOM && df != 0){ //Random effects: add between study variance to weights
			for (int i = 0; i < rrs.length; i++){
				sumWsquared+= w[i]*w[i];
				sumWxRR+= w[i]*lnRRs[i];
				sumWxRRsquared+= w[i]*lnRRs[i]*lnRRs[i];
			}

			double q = sumWxRRsquared - sumWxRR*sumWxRR / sumW;
			if (q > df){
				double c = sumW - (sumWsquared/sumW);
				betweenStudyVar = (q-df)/c;
			}

			//recompute weights using between study variance:
			sumW = 0;
			for (int i = 0; i < rrs.length; i++){
				w[i] = 1/(vars[i] + betweenStudyVar);
				sumW += w[i];
			}
			sumlnRRxW = 0;
			for (int i = 0; i < rrs.length; i++){
				lnRRxW[i] = lnRRs[i] * w[i] / sumW;
				sumlnRRxW+= lnRRxW[i];
			}
		}
		double combinedVar = 1/sumW;
		double combinedSE = Math.sqrt(combinedVar);
		Result result = new Result();
		result.rr = Math.exp(sumlnRRxW);
		result.ci95Down = Math.exp(Math.log(result.rr) - 1.96*combinedSE);
		result.ci95Up = Math.exp(Math.log(result.rr) + 1.96*combinedSE);
		result.pForHomogeneity = ChiSquaredProbabilityFunction.chiSquaredProbabilityFunction(sumChi2weighted, (int)df);
		if (Double.isNaN(sumlnRRxW/combinedSE))
			System.err.println("Division produces NaN: " + sumlnRRxW + " / " + combinedSE);
		double cdf = GaussianDistribution.cdf(sumlnRRxW/combinedSE);
		result.p = 2*(Math.min(cdf, 1-cdf));
		return result;
	}
	
	public static class Result {
		public double rr;
		public double pForHomogeneity;
		public double ci95Down;
		public double ci95Up;
		public double p;
	}
}
