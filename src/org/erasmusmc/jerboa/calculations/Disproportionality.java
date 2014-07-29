package org.erasmusmc.jerboa.calculations;

import org.erasmusmc.math.AssociationMeasures;

/**
 * Calculations for disproportionality methods. Input assumes 2 by 2 matrix:
 * 								drug		other drug
 * event						a					b			
 * other event			c					d
 *
 * @author schuemie
 *
 */
public class Disproportionality {

	public static void main(String[] args){
		System.out.println(proportionalReportingRatio(50, 250, 220, 2000));
		System.out.println(reportingOddsRatio(50, 220, 250, 2000));
	}
	
	public static Result proportionalReportingRatio(long a, long b, long c, long d){
		Result result = new Result();
		result.ratio = (a/(double)(a+c))/(b/(double)(b+d));
		
    double denominator = (a+b)*(c+d)*(b+d)*(a+c);
    double x = (a*d-b*c);
    double numerator = x*x*(a+b+c+d); 
    double chiSquared = numerator/denominator;
    result.p = AssociationMeasures.chiSquareToP(chiSquared);
    
    double se = (1/(double)a)-(1/(double)(c+a))+(1/(double)b)-(1/(double)(d+b));
    se = Math.sqrt(se)*1.96;
    result.ci95down = Math.exp(Math.log(result.ratio)-se);
    result.ci95up = Math.exp(Math.log(result.ratio)+se);
		
		return result;
	}
	
	public static Result reportingOddsRatio(long a, long b, long c, long d){
		Result result = new Result();
		result.ratio = (a/(double)c)/(b/(double)d);
		
    double denominator = (a+b)*(c+d)*(b+d)*(a+c);
    double x = (a*d-b*c);
    double numerator = x*x*(a+b+c+d); 
    double chiSquared = numerator/denominator;
    result.p = AssociationMeasures.chiSquareToP(chiSquared);
    
    double se = Math.sqrt((1/(double)a)+(1/(double)b)+(1/(double)c)+(1/(double)d))*1.96;
    result.ci95down = Math.exp(Math.log(result.ratio)-se);
    result.ci95up = Math.exp(Math.log(result.ratio)+se);
		
		return result;
	}
	
	public static class Result {
		public double ratio;
		public double p;
		public double ci95down;
		public double ci95up;
		public String toString(){
			return String.format("Ratio = %s\tP = %s\tCI95Down = %s\tCI95UP = %s",ratio,p,ci95down,ci95up);
		}
	}
}
