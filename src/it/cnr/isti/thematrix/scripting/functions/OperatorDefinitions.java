package it.cnr.isti.thematrix.scripting.functions;

import it.cnr.isti.thematrix.configuration.LogST;
import it.cnr.isti.thematrix.mapping.utils.ProcOutpatStatics;
import it.cnr.isti.thematrix.mapping.utils.ProductCodeStatics;
import it.cnr.isti.thematrix.mapping.utils.WardStatics;
import it.cnr.isti.thematrix.scripting.filtermodule.support.MatchesFilterCondition;
import it.cnr.isti.thematrix.scripting.sys.AbstractOperation2;
import it.cnr.isti.thematrix.scripting.sys.Symbol;
import it.cnr.isti.thematrix.scripting.sys.TheMatrixSys;
import it.cnr.isti.thematrix.scripting.utils.DataType;
import it.cnr.isti.thematrix.scripting.utils.StringUtil;

import java.security.SecureRandom;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * This class holds all definitions of operators for MatrixFilter and
 * ApplyFunction (etc) that are recognized by the interpreter. The class
 * contains code from massimo and edoardo, originally included in TheMatrixSys,
 * now moved here to decouple from main.
 * 
 * 
 * FIXME : apply to the integer functions the new semantics of Float functions
 * (type-casting, return null on any missing values)
 * 
 * @author massimo
 */
public class OperatorDefinitions 
{
	
	// that's not really so useful, as string are interned
	private final static String EMPTY = "";

		
	/**
	 * Define boolean operators used by MatrixFilter. Equalities, relational and
	 * matching operators over various types. All definitions have to explicitly
	 * cope with missing (null) values without crashing and according to the
	 * intended semantics (null / missing is the highest value in each type
	 * range).
	 */
	static public void defineFilterOperators() {

		TheMatrixSys
				.getOpTable()
				.put(DataType.BOOLEAN, "=",
						new AbstractOperation2<Boolean, Boolean>("equals") {
							@Override
							public Boolean apply(Boolean op1, Boolean op2) {
								// LogST.logP(0, "using boolean equals");
								// FIXME probably doesn't manage missing values
								// the same way as stata -- rework
								//
								// op1 == op2 definition
								// both are false, both true or both null
								return op1 != null && op1.equals(op2);
								//
								// this test was breaking the filter, beware!
								// || op1 != null && op2 || op2!=null && op1; //
								// one is null and one is true.
								// both are the same instance (unlikely...) or
								// null
							}
						})
						/****** INT operators ********************************************************/						
				.put(DataType.INT, "=",
						new AbstractOperation2<Integer, Boolean>("equals") {
							@Override
							public Boolean apply(Integer op1, Integer op2) { // missing
																				// handled
							// LogST.logP(0, "using integer equals");
								return op1 == op2 || op1 != null && op2 != null
										&& op1.equals(op2);
							}
						})
				.put(DataType.INT, "!=",
						new AbstractOperation2<Integer, Boolean>("disequal") {
							@Override
							public Boolean apply(Integer op1, Integer op2) { // missing
																				// handled
								return op1 != op2 || op1 != null && op2 != null
										&& !op1.equals(op2);
							}
						})
				.put(DataType.INT,
						">",
						new AbstractOperation2<Integer, Boolean>("greater-than") {
							@Override
							public Boolean apply(Integer op1, Integer op2) { // missing
																				// handled
								return op1 == null && op2 != null
										|| op1 != null && op2 != null
										&& op1 > op2;
							}
						})
				.put(DataType.INT, ">=",
						new AbstractOperation2<Integer, Boolean>("greater-eq") {
							@Override
							public Boolean apply(Integer op1, Integer op2) { // missing
																				// handled
								return op1 == null || op1 != null
										&& op2 != null && op1 >= op2;
							}
						})
				.put(DataType.INT, "<",
						new AbstractOperation2<Integer, Boolean>("lower-than") {
							@Override
							public Boolean apply(Integer op1, Integer op2) { // missing
																				// handled
								return op2 == null && op1 != null
										|| op1 != null && op2 != null
										&& op1 < op2;
							}
						})
				.put(DataType.INT, "<=",
						new AbstractOperation2<Integer, Boolean>("less-eq") {
							@Override
							public Boolean apply(Integer op1, Integer op2) { // missing
																				// handled
								return op2 == null || op1 != null
										&& op2 != null && op1 <= op2;
							}
						})
						/****** FLOAT operators ********************************************************/						
				.put(DataType.FLOAT, "=",
						new AbstractOperation2<Float, Boolean>("equals") {
							@Override
							public Boolean apply(Float op1, Float op2) { // missing
																				// handled
							// LogST.logP(0, "using float equals");
								return op1 == op2 || op1 != null && op2 != null
										&& op1.equals(op2);
							}
						})
				.put(DataType.FLOAT, "!=",
						new AbstractOperation2<Float, Boolean>("disequal") {
							@Override
							public Boolean apply(Float op1, Float op2) { // missing
																				// handled
							// LogST.logP(0, "using float equals");
								return op1 != op2 || op1 != null && op2 != null
										&& !op1.equals(op2);
							}
						})
				.put(DataType.FLOAT, ">",
						new AbstractOperation2<Float, Boolean>("greather-than") {
							@Override
							public Boolean apply(Float op1, Float op2) { // missing
																				// handled
							// LogST.logP(0, "using float equals");
								return op1 == null && op2 != null
										|| op1 != null && op2 != null
										&& op1 > op2;
							}
						})
				.put(DataType.FLOAT, ">=",
						new AbstractOperation2<Float, Boolean>("greather-eq") {
							@Override
							public Boolean apply(Float op1, Float op2) { // missing
																				// handled
							// LogST.logP(0, "using float equals");
								return op1 == null || op1 != null
										&& op2 != null && op1 >= op2;
							}
						})
				.put(DataType.FLOAT, "<",
						new AbstractOperation2<Float, Boolean>("lower-than") {
							@Override
							public Boolean apply(Float op1, Float op2) { // missing
																				// handled
							// LogST.logP(0, "using float equals");
								return op2 == null && op1 != null
										|| op1 != null && op2 != null
										&& op1 < op2;
							}
						})
				.put(DataType.FLOAT, "<=",
						new AbstractOperation2<Float, Boolean>("less-eq") {
							@Override
							public Boolean apply(Float op1, Float op2) { // missing
																				// handled
							// LogST.logP(0, "using float equals");
								return op2 == null || op1 != null
										&& op2 != null && op1 <= op2;
							}
						})
						/****** DATE operators ********************************************************/						
				.put(DataType.DATE, "=",
						new AbstractOperation2<Date, Boolean>("=") {
							@Override
							public Boolean apply(Date op1, Date op2) {// missing
																		// handled
							// LogST.logP(0, "using Date equals");
								return (op1 == null && op2 == null)
										|| (op1 != null && op2 != null && op1
												.equals(op2));
							}
						})
				.put(DataType.DATE, "!=",
						new AbstractOperation2<Date, Boolean>("!=") {
							@Override
							public Boolean apply(Date op1, Date op2) {// missing
																		// handled
								return (op1 == null ^ op2 == null)
										|| (op1 != null && op2 != null && !op1
												.equals(op2));
							}
						})
				.put(DataType.DATE, ">",
						new AbstractOperation2<Date, Boolean>(">") {
							@Override
							public Boolean apply(Date op1, Date op2) {// missing
																		// handled
								return (op1 == null && (op2 != null))
										|| op1 != null && op2 != null
										&& op1.getTime() > op2.getTime();
								/*********** TODO make it uniform, re-test *********/
							}
						})
				.put(DataType.DATE, "<",
						new AbstractOperation2<Date, Boolean>("<") {
							@Override
							public Boolean apply(Date op1, Date op2) { // missing
																		// handled
								return (op2 == null && op1 != null)
										|| op1 != null && op2 != null
										&& op1.compareTo(op2) < 0;
							}
						})
				.put(DataType.DATE, "<=",
						new AbstractOperation2<Date, Boolean>("<=") {
							@Override
							public Boolean apply(Date op1, Date op2) { // missing
																		// handled
								return op1 != null && op2 == null
										|| op1 != null && op2 != null
										&& op1.getTime() <= op2.getTime();
								// TODO make it uniform, re-test
							}
						})
				.put(DataType.DATE, ">=",
						new AbstractOperation2<Date, Boolean>(">=") {
							@Override
							public Boolean apply(Date op1, Date op2) { // missing
																		// handled
								return (op1 == null) || op1 != null
										&& op2 != null
										&& op1.compareTo(op2) >= 0;
								//
								// return op1 != null && op1.equals(op2) ||
								// op1.getTime() >= op2.getTime();
							}
						})
						/****** STRING operators ********************************************************/
						/***
						 * FIXME WARNING : here we have a different semantics for a
						 * MISSING string (e.g. null) and an _empty_ string (i.e. ""), which are however 
						 * are interchangeable when performing I/O from CVS files. This is a dangerous 
						 * form of semantic aliasing...
						 * 
						 * ---> the above have been fixed by considering null as to be equivalent to "" (empty string)
						 * 
						 */
				.put(DataType.STRING, "matches", new MatchesFilterCondition())
				//
				.put(DataType.STRING, "=",
						new AbstractOperation2<String, Boolean>("=") {
							@Override
							public Boolean apply(String op1, String op2) 
							{ 
								LogST.logP(3, "using string equals on |"
												+ op1
												+ "|,|"
												+ op2
												+ "| returning value "
												+ (((op1 == op2) || op1 != null
														&& op2 != null
														&& op1.equals(op2)) ? "true"
														: "false"));
								
								if (op1 == null) op1 = EMPTY;
								if (op2 == null) op2 = EMPTY;
								
								return op1.compareTo(op2) == 0;
							}
						})
				.put(DataType.STRING, "!=", new AbstractOperation2<String, Boolean>("!=") {
							@Override
							public Boolean apply(String op1, String op2) 
							{
								if (op1 == null) op1 = EMPTY;
								if (op2 == null) op2 = EMPTY;
																		
								return op1.compareTo(op2) != 0;
							}
						})
				.put(DataType.STRING, ">", new AbstractOperation2<String, Boolean>(">") {
							@Override
							public Boolean apply(String op1, String op2) 
							{
								if (op1 == null) op1 = EMPTY;
								if (op2 == null) op2 = EMPTY;
								
								return op1.compareTo(op2) > 0;
							}
						})	
				.put(DataType.STRING, ">=", new AbstractOperation2<String, Boolean>(">=") {
							@Override
							public Boolean apply(String op1, String op2) 
							{
								if (op1 == null) op1 = EMPTY;
								if (op2 == null) op2 = EMPTY;
								
								return op1.compareTo(op2) >= 0;
							}
						})		
				.put(DataType.STRING, "<", new AbstractOperation2<String, Boolean>("<") {
							@Override
							public Boolean apply(String op1, String op2)
							{
								if (op1 == null) op1 = EMPTY;
								if (op2 == null) op2 = EMPTY;
								
								return op1.compareTo(op2) < 0;
							}
						})
				.put(DataType.STRING, "<=", new AbstractOperation2<String, Boolean>("<=") {
							@Override
							public Boolean apply(String op1, String op2) {
								
								if (op1 == null) op1 = EMPTY;
								if (op2 == null) op2 = EMPTY;
								
								return op1.compareTo(op2) <= 0;
							}
						});
		;

	};

	/**
	 * Define function operators used by ApplyFunction and other modules. They
	 * are defined in terms of an AbstractNoParams type which allows (and
	 * forces) us to check the actual number of arguments at run-time. Here also
	 * all definitions have to explicitly cope with missing (null) values
	 * without crashing and according to the intended semantics (null / missing
	 * is the highest value in each type range).
	 */
	static public void defineFunctionOperators() {

		defineFunctionOperatorsStatics(); // prepare any static status we may need
		
		TheMatrixSys.getFuncTable()
		/****************************************************
		 * (some) comparison functions on booleans
		 ****************************************************/
		.put(DataType.BOOLEAN, "equalsto", new AbstractNoParamsRecordOperation<Boolean>("EqualsTo") {

			@SuppressWarnings("unchecked")
			@Override
			public Boolean apply(List<Symbol<?>> params, Void v) { // (ignore op2)
				if (params.size()<2) throw new Error("EqualsTo requires two parameters");
				Symbol<Comparable<Object>> s1 = (Symbol<Comparable<Object>>) params.get(0);
				Symbol<Comparable<Object>> s2 = (Symbol<Comparable<Object>>) params.get(1);

				// check the type
				if (s1.type != s2.type)
				{
					// TODO: move on error log
					LogST.logP(0, "** WARNING ** equalsto: "+s1+" and "+s2+" have a different type");
					return false;
				}
				
				if (s1.value == null && s2.value == null)
					return true;
				else if (s1.value == null || s2.value == null)
					return false;
				
				return s1.value.equals(s2.value);
			}
// WRONG IMPLEMENTATION?			
//			@Override
//			public Boolean apply(DatasetRecord op1, List<String> op2) {
//				// get first (and only) column
//				Symbol<String> target = (Symbol<String>) op1.attributes().get(0);
//				return op2.contains(target.value);
//			}

		}).put(DataType.BOOLEAN, "lessthan", new AbstractNoParamsRecordOperation<Boolean>("LessThan") {

			@SuppressWarnings("unchecked")
			@Override
			public Boolean apply(List<Symbol<?>> params, Void v) { // (ignore op2)
				Symbol<Comparable<Object>> s1 = (Symbol<Comparable<Object>>) params.get(0);
				Symbol<Comparable<Object>> s2 = (Symbol<Comparable<Object>>) params.get(1);

				// if s1 == null, then s1 < s2 is always false (and don't call methods...)
				// else if s2 == null then it's "the largest value" so s1<s2 must hold
				// else compare
				return s1.value != null && (s2.value == null || s1.value.compareTo(s2.value) < 0);
			}

		}).put(DataType.BOOLEAN, "greaterthan", new AbstractNoParamsRecordOperation<Boolean>("GreaterThan") {

			@SuppressWarnings("unchecked")
			@Override
			public Boolean apply(List<Symbol<?>> params, Void v) { // (ignore op2)
				Symbol<Comparable<Object>> s1 = (Symbol<Comparable<Object>>) params.get(0);
				Symbol<Comparable<Object>> s2 = (Symbol<Comparable<Object>>) params.get(1);

				// if s2 == null, then s1 > s2 is always false (and don't call methods...)
				// else if s1 == null then it's "the largest value" so s1>s2 must hold
				// else compare
				return s2.value != null && (s1.value == null || (s1.value.compareTo(s2.value) > 0));
			}

		}).put(DataType.BOOLEAN, "notequal", new AbstractNoParamsRecordOperation<Boolean>("NotEqual") {

			@SuppressWarnings("unchecked")
			@Override
			public Boolean apply(List<Symbol<?>> params, Void v) { // (ignore op2)
				Symbol<Comparable<Object>> s1 = (Symbol<Comparable<Object>>) params.get(0);
				Symbol<Comparable<Object>> s2 = (Symbol<Comparable<Object>>) params.get(1);

				return (s1.value == null && s1.value != s2.value) || s1.value != null && !s1.value.equals(s2.value);
			}
		})
		/****************************************************
		 * utility functions on dates
		 ****************************************************/
		.put(DataType.INT, "elapsedtime", new AbstractNoParamsRecordOperation<Integer>("ElapsedTime") {

			@SuppressWarnings("unchecked")
			@Override
			public Integer apply(List<Symbol<?>> op1, Void op2) { // (ignore op2)
				Symbol<Date> olderDate = (Symbol<Date>) op1.get(0);
				Symbol<Date> newerDate = (Symbol<Date>) op1.get(1);
				if (olderDate.value!=null && newerDate.value!=null) {
				// http://stackoverflow.com/a/3491723/7849
					int diffInDays = (int) ((newerDate.value.getTime() - olderDate.value.getTime()) / (1000 * 60 * 60 * 24));
					return diffInDays;
				}
				LogST.logP(2,"ElasedTime function called with a null date");
				return null;

			}

		})

		.put(DataType.INT, "year", new AbstractNoParamsRecordOperation<Integer>("Year") {

			@SuppressWarnings("unchecked")
			@Override
			public Integer apply(List<Symbol<?>> op1, Void op2) { // (ignore op2)
				Symbol<Date> date = (Symbol<Date>) op1.get(0);
				if (date != null && date.value != null) {
					Calendar calendar = Calendar.getInstance();
					calendar.setTime(date.value);
					return calendar.get(Calendar.YEAR);
				} else {
					LogST.logP(2,"Year function called with null date, on symbol "+op1.toString());
					return null;
				}
			}

		})

		.put(DataType.DATE, "min", new AbstractNoParamsRecordOperation<Date>("min") {

			@Override
			public Date apply(List<Symbol<?>> op1, Void op2) { // (ignore op2)

//				Date result = (Date) (op1.get(0).value); //may be null, not harmful
				Date result = null; 
				for (Symbol<?> s : op1) {
					if (s.value == null) continue;
					Date newValue = (Date) s.value;
					if (result == null || newValue.before(result)) {
						result = newValue;
					}
				}
				
				// LogST.logP(1,"DATE.min with args " + op1 + " returns " + result == null ? " missing " : result);
				return result;

			}

		}).put(DataType.BOOLEAN, "elapsedtimeinrange", new AbstractNoParamsRecordOperation<Boolean>("elapsedTimeInRange") {

			@SuppressWarnings("unchecked")
			@Override
			public Boolean apply(List<Symbol<?>> params, Void v) { // (ignore op2)
// 				LogST.logP(0, "elapsedtimeinrange Debug");
//				LogST.logP(0, "elapsedtimeinrange Debug: "+params.get(0).toString()+params.get(1).toString()+params.get(2).toString()+params.get(3).toString());

				Symbol<Date> olderDate = (Symbol<Date>) params.get(0);
				Symbol<Date> newerDate = (Symbol<Date>) params.get(1);

				Symbol<Integer> left = (Symbol<Integer>) params.get(2);
				Symbol<Integer> right = (Symbol<Integer>) params.get(3);

				/**
				 * Semantics:
				 * 
				 * either date null means we cannot compute difference --> return null
				 * 
				 * right.value == null means upper bound +infinite --> no upper limit
				 * 
				 * left.value == null means range lower bound +infinite --> always return false
				 */
				if (olderDate.value!= null && newerDate.value!=null) {
					// http://stackoverflow.com/a/3491723/7849
					int diffInDays = (int) ((newerDate.value.getTime() - olderDate.value.getTime()) / (1000 * 60 * 60 * 24));

					if (left.value==null) 
						return false;
					else 
						return (diffInDays >= left.value) && (right.value==null || diffInDays <= right.value);
				} 
				else return null;
			}

		})
		/****************************************************
		 * string match is the function to be used in the product module and similar cases; 
		 * currently this code is a stub, only instantiating the corresponding functionality from the 
		 * filter module (most likely useful)
		 * 
		 * FIXME rework together with the product module
		 ****************************************************/

		.put(DataType.STRING, "match", new AbstractNoParamsRecordOperation<Boolean>("match") {

			private final MatchesFilterCondition matcher = new MatchesFilterCondition();

			@Override
			public Boolean apply(List<Symbol<?>> patterns, Void v) {
				// Symbol<String> d1 = (Symbol<String>) op1.attributes().get(0);
				// for (String patt: patterns) {
				// if (matcher.apply(d1.value, patt)) return true;
				// }
				throw new UnsupportedOperationException("da implementare");
			}
		})

		/****************************************************
		 * identity functions
		 ****************************************************/
		.put(DataType.STRING, "id", new AbstractNoParamsRecordOperation<String>("Id") {

			@SuppressWarnings("unchecked")
			@Override
			public String apply(List<Symbol<?>> params, Void v) { // (ignore op2)
				Symbol<String> val = (Symbol<String>) params.get(0);
				return val.value;
			}

		}).put(DataType.BOOLEAN, "id", new AbstractNoParamsRecordOperation<Boolean>("Id") {

			@SuppressWarnings("unchecked")
			@Override
			public Boolean apply(List<Symbol<?>> params, Void v) { // (ignore op2)
				Symbol<Boolean> val = (Symbol<Boolean>) params.get(0);
				return val.value;
			}

		}).put(DataType.INT, "id", new AbstractNoParamsRecordOperation<Integer>("Id") {
			@SuppressWarnings("unchecked")
			@Override
			public Integer apply(List<Symbol<?>> params, Void v) { // (ignore op2)
//				if (params.get(0).type == DataType.MISSING) { return null;}

//				DEBUG
//				if (params.get(0).type == DataType.MISSING) {System.err.print("+++ "); return null;}
//				System.err.println("tipo "+ params.get(0).type.toString() + " val " + params.get(0).value);
				
				Symbol<Integer> val = (Symbol<Integer>) params.get(0);
				return val.value;
			}

		}).put(DataType.FLOAT, "id", new AbstractNoParamsRecordOperation<Float>("Id") {

			@SuppressWarnings("unchecked")
			@Override
			public Float apply(List<Symbol<?>> params, Void v) { // (ignore op2)
				Symbol<Float> val = (Symbol<Float>) params.get(0);
				return val.value;
			}

		}).put(DataType.DATE, "id", new AbstractNoParamsRecordOperation<Date>("Id") {

			@SuppressWarnings("unchecked")
			@Override
			public Date apply(List<Symbol<?>> params, Void v) { // (ignore op2)
				Symbol<Date> val = (Symbol<Date>) params.get(0);
				return val.value;
			}
			
			/****************************************************
			 * products mappings
			 ****************************************************/
			
		}).put(DataType.STRING, "atc", new AbstractNoParamsRecordOperation<String>("Atc") {

			@SuppressWarnings("unchecked")
			@Override
			public String apply(List<Symbol<?>> params, Void v) { // (ignore op2)
				Symbol<String> pid = (Symbol<String>) params.get(0);
				
				if (pid.value != null)
					return ProductCodeStatics.getAtc(pid.value);
				
				LogST.logP(0, "Atc called with null/invalid arg "+String.valueOf(pid.value));
				return null;
			}
			
		}).put(DataType.FLOAT, "duration", new AbstractNoParamsRecordOperation<Float>("Duration") {

			@SuppressWarnings("unchecked")
			@Override
			public Float apply(List<Symbol<?>> params, Void v) { // (ignore op2)
				Symbol<String> pid = (Symbol<String>) params.get(0);
				
				if (pid.value != null)
					return ProductCodeStatics.getDuration(pid.value);
				
				LogST.logP(0, "Duration called with null/invalid arg "+String.valueOf(pid.value));
				return null;
			}
			
		}).put(DataType.STRING, "typeofward", new AbstractNoParamsRecordOperation<String>("TypeOfWard") {
			// gets the type of ward from the ward prefix (either 2 characters, or 3 characters with a 0 prefix)
			@SuppressWarnings("unchecked")
			@Override
			public String apply(List<Symbol<?>> params, Void v) { // (ignore op2)
				Symbol<String> pid = (Symbol<String>) params.get(0);
				if (pid.value != null) {
					if (pid.value.length() == 2) return WardStatics.getTYPE_WARD("0"+pid.value);//(pid.value);
					if (pid.value.length() == 3) return WardStatics.getTYPE_WARD(pid.value);//(pid.value.substring(1, 3));
				}
				LogST.logP(0, "TypeOfWard called with null/invalid arg "+String.valueOf(pid.value));
				return null;
			}

		}).put(DataType.STRING, "typeoffullward", new AbstractNoParamsRecordOperation<String>("TypeOfFullWard") {
			// gets the type of ward from the full ward, getting the initial substring of exactly 2 chars
			@SuppressWarnings("unchecked")
			@Override
			public String apply(List<Symbol<?>> params, Void v) { // (ignore op2)
				Symbol<String> pid = (Symbol<String>) params.get(0);
				if (pid.value!=null && pid.value.length()==4)
					return WardStatics.getTYPE_WARD("0"+pid.value.substring(0,2));

				LogST.logP(0, "TypeOfFullWard called with null/invalid arg "+String.valueOf(pid.value));
				return null;
			}

		}).put(DataType.STRING, "typeofproc", new AbstractNoParamsRecordOperation<String>("TypeOfProc") {

			@SuppressWarnings("unchecked")
			@Override
			public String apply(List<Symbol<?>> params, Void v) { // (ignore op2)
				Symbol<String> pid = (Symbol<String>) params.get(0);
				if (pid.value !=null) 
					return ProcOutpatStatics.getTYPE_OUTPAT(pid.value);

				LogST.logP(0, "TypeOfProc called with null/invalid arg "+String.valueOf(pid.value));
				return null;
			}

			/****************************************************
			 * string manipulation functions
			 ****************************************************/

		}).put(DataType.STRING, "concat", new AbstractNoParamsRecordOperation<String>("Concat") {

			@Override
			public String apply(List<Symbol<?>> params, Void v) { // (ignore op2)
				StringBuilder sb = new StringBuilder(1024);
				// we ASSUME all parameters are strings
				for (Symbol<?> s : params) {
					// we reuse the implementation of StringUtil.symbolToString()
					// returns the hold value for strings, convert to string other values
					// the other option would be to check s.type and log an error
					if (s.value!= null) sb.append(StringUtil.symbolToString(s));
				}
				return sb.toString();
			}

		}).put(DataType.STRING, "trim", new AbstractNoParamsRecordOperation<String>("Trim") {

			@Override
			public String apply(List<Symbol<?>> params, Void v) { // (ignore op2)
				if (params.get(0).value!=null)
					return params.get(0).value.toString().trim();
				return null;
			}
		}).put(DataType.STRING, "replace", new AbstractNoParamsRecordOperation<String>("Replace") {

			@SuppressWarnings("unchecked")
			@Override
			public String apply(List<Symbol<?>> params, Void v) {
				Symbol<String> s1= (Symbol<String>)params.get(0);
				Symbol.assertType(s1, DataType.STRING);
				Symbol<String> s2= (Symbol<String>)params.get(1);
				Symbol.assertType(s2, DataType.STRING);
				Symbol<String> s3= (Symbol<String>)params.get(2);
				Symbol.assertType(s3, DataType.STRING);
				return s1.value.replace(s2.value,s3.value);
			}
				
			/*
			 * .put(DataType.DATE, "MIN", new AbstractNoParamsRecordOperation<Date>("MIN") {
			 * 
			 * @Override public Date apply(List<Symbol<?>> params, Void v) { // (ignore op2) Date min = ((Symbol<Date>)
			 * params.get(0)).value; for (Symbol<?> s : params) { Symbol<Date> ss = (Symbol<Date>) s; if (ss.value!=null
			 * && (min==null || ss.value.before(min))) { min = ss.value; // note that still min == null if both args are
			 * null } }
			 * 
			 * return min; }
			 * 
			 * })
			 */

			/****************************************************
			 * integer manipulation functions
			 ****************************************************/

		
		}).put(DataType.INT, "sum", new AbstractNoParamsRecordOperation<Integer>("SUM") {

			@SuppressWarnings("unchecked")
			@Override
			public Integer apply(List<Symbol<?>> params, Void v) { // (ignore op2)
				int accumulator = 0;
				for (Symbol<?> s : params) {
					Symbol<Integer> ss = (Symbol<Integer>) s;
					if (ss.value!=null)
						accumulator += ss.value;
				}

				return accumulator;
			}
		}).put(DataType.INT, "subtract", new AbstractNoParamsRecordOperation<Integer>("SUBTRACT") {
			// expects at least two arguments; subtracts second and following arguments from the first one
			@SuppressWarnings("unchecked")
			@Override
			public Integer apply(List<Symbol<?>> params, Void v) { // (ignore op2)
				int accumulator = 0;
				boolean first = true;
				for (Symbol<?> s : params) {
					Symbol<Integer> ss = (Symbol<Integer>) s;
					if (ss.value!=null)
						if (first)
							{ accumulator = ss.value; first =false; }
						else
							{ accumulator -= ss.value; }
				}

				return accumulator;
			}
		}).put(DataType.INT, "inverse", new AbstractNoParamsRecordOperation<Integer>("INVERSE") {
			// computes the inverse of the sum of its arguments (at least one)
			@SuppressWarnings("unchecked")
			@Override
			public Integer apply(List<Symbol<?>> params, Void v) { // (ignore op2)
				int accumulator = 0;
				for (Symbol<?> s : params) {
					Symbol<Integer> ss = (Symbol<Integer>) s;
					if (ss.value!=null)
							{ accumulator -= ss.value; }
				}

				return accumulator;
			}
		}).put(DataType.INT, "prod", new AbstractNoParamsRecordOperation<Integer>("PROD") {

			/**
			 * FIXME The integer version of Prod() can only accepts integer arguments (correct), 
			 * but there is no error checking code.
			 */
			
			@SuppressWarnings("unchecked")
			@Override
			public Integer apply(List<Symbol<?>> params, Void v) // (ignore op2) 
			{ 
				int accumulator = 1;
				for (Symbol<?> s : params) 
				{
					Symbol<Integer> ss = (Symbol<Integer>) s;
					if (ss.value != null && ss.type != DataType.MISSING) 
						accumulator *= ss.value;
					else
						return null;
				}
				return accumulator;
			}
			// TODO subtract inverse reciprocal division round ceiling floor  

			/****************************************************
			 * float manipulation functions
			 * FIXME these should be able to take also integer symbols!
			 ****************************************************/

		
		}).put(DataType.FLOAT, "sum", new AbstractNoParamsRecordOperation<Float>("SUM") {

			
			@Override
			public Float apply(List<Symbol<?>> params, Void v) { // (ignore op2)
				/***
				 * new semantics: cast integer to float if possible, return null 
				 * (e.g. MISSING) if anyone of the inputs is MISSING/null 
				 */
				float accumulator = 0;
				for (Symbol<?> s : params) {
					if (s==null) return null;
					Float floatValue = castToFloat(s);
					if (floatValue==null) return null;
					accumulator += floatValue;
				}
/*				float accumulator = 0;
				for (Symbol<?> s : params) {
					Symbol<Float> ss = (Symbol<Float>) s;
					if (ss.value!=null)
						accumulator += ss.value;
				}
*/
				return accumulator;
			}
		}).put(DataType.FLOAT, "subtract", new AbstractNoParamsRecordOperation<Float>("SUBTRACT") {
			// expects at least two arguments; subtracts second and following arguments from the first one
			//
			// FIXME semantics to be corrected, return null on any missing input
			@SuppressWarnings("unchecked")
			@Override
			public Float apply(List<Symbol<?>> params, Void v) { // (ignore op2)
				/***
				 * new semantics: cast integer to float if possible, return null 
				 * (e.g. MISSING) if anyone of the inputs is MISSING/null 
				 */
				float accumulator = 0;
				boolean first = true;
				for (Symbol<?> s : params) {
					if (s==null) return null;
					Float floatValue = castToFloat(s);
					if (floatValue==null) return null;
					if (first) {
						accumulator = floatValue; first =false; 
					} else
						accumulator -= floatValue;
				}

/*	OLD			float accumulator = 0;
				boolean first = true;
				for (Symbol<?> s : params) {
					Symbol<Float> ss = (Symbol<Float>) s;
					if (ss.value!=null)
						if (first)
							{ accumulator = ss.value; first =false; }
						else
							{ accumulator -= ss.value; }
				}
*/
				return accumulator;
			}
		}).put(DataType.FLOAT, "inverse", new AbstractNoParamsRecordOperation<Float>("INVERSE") {
			// computes the inverse (w.r.to sign) of the sum of its arguments (at least one)
			// FIXME semantics to be corrected, return null on any missing input
			@SuppressWarnings("unchecked")
			@Override
			public Float apply(List<Symbol<?>> params, Void v) { // (ignore op2)
				/***
				 * new semantics: cast integer to float if possible, return null 
				 * (e.g. MISSING) if anyone of the inputs is MISSING/null 
				 */
				float accumulator = 0;
				for (Symbol<?> s : params) {
					if (s==null) return null;
					Float floatValue = castToFloat(s);
					if (floatValue==null) return null;
					accumulator -= floatValue;
				}
				return accumulator;
			}
		}).put(DataType.FLOAT, "prod", new AbstractNoParamsRecordOperation<Float>("PROD") {

			
			@Override
			public Float apply(List<Symbol<?>> params, Void v) { // (ignore op2)
				/***
				 * new semantics: cast integer to float if possible, return null 
				 * (e.g. MISSING) if anyone of the inputs is MISSING/null
				 * 
				 */
				float accumulator = 1;
				
				for (Symbol<?> s : params) {
					// can we actually have a null s ?
					// shall we raise a warning ?
					if (s==null) 
						return null;
					Float floatValue = castToFloat(s);
					if (floatValue == null) 
						return null; 
					accumulator *= floatValue.floatValue();
				}
				return accumulator;
			}
		}).put(DataType.FLOAT, "division", new AbstractNoParamsRecordOperation<Float>("DIVISION") {

			
			@Override
			public Float apply(List<Symbol<?>> params, Void v) { // (ignore op2)
				/***
				 * new semantics: cast integer to float if possible, return null 
				 * (e.g. MISSING) if any of the two inputs is MISSING/null
				 * 
				 */
				Float numerator = castToFloat(params.get(0));
				if (numerator == null) return null;
				Float denominator = castToFloat(params.get(1));
				if (denominator == null) return null;
				
				if (denominator != 0) return numerator/denominator;
				else {
					LogST.logP(0,"WARNING apply(): division by Zero value");
					return null;
				} //will return a null == missing in case of divide by zero
			}
		}).put(DataType.FLOAT, "reciprocal", new AbstractNoParamsRecordOperation<Float>("RECIPROCAL") {
			// computes the reciprocal of its argument (extra ones are ignored)
			@SuppressWarnings("unchecked")
			@Override
			public Float apply(List<Symbol<?>> params, Void v) { // (ignore op2)
				/***
				 * new semantics: cast integer to float if possible, return null 
				 * (e.g. MISSING) if anyone of the inputs is MISSING/null 
				 */
				Float floatValue = castToFloat(params.get(0));
				if (floatValue == null) return null;
				if (floatValue!=0) return (1/floatValue);
				else {
					LogST.logP(0,"WARNING apply(): reciprocal of Zero value");
					return null;
				} //will return a null == missing in case of divide by zero
				
/*				Symbol<Float> ss = (Symbol<Float>) params.get(0);
				if (ss.value!=null) { 
					fvalue = ss.value;
					if (fvalue!=0) return (1/fvalue);
					else {
						LogST.logP(0,"WARNING apply() reciprocal of Zero value");
						return null;
					} //will return a missing in case of divide by zero
				}
				return null;
*/
			}
			// TODO subtract inverse reciprocal division round ceiling floor  
			
			/****************************************************
			 * FIXME Quantization and approximation functions UNUSED
			 ****************************************************/
/**************
		}).put(DataType.INT, "Quinquennio", new AbstractNoParamsRecordOperation<Integer>("QUINQUENNIO"){ 
			@SuppressWarnings("unchecked")
			@Override
			public Integer apply(List<Symbol<?>> params, Void v) {
				/**
				 * reads in a date, returns the integer value of the first year in the 
				 * quinquennium containing the date
				 * /
				Symbol<Date> date = (Symbol<Date>) params.get(0);
				//List<Symbol<?>> op1, Void op2) { // (ignore op2)
				if (date!= null) {
					Calendar calendar = Calendar.getInstance();
					calendar.setTime(date.value);
					int quinq = calendar.get(Calendar.YEAR);
					return quinq - (quinq % 5);
				} else {
					LogST.logP(2,"quinquennio function called with null date, on symbol "+params.get(0).toString());
					return null;
				}
			}
****/			 
		}).put(DataType.INT, "floor", new AbstractNoParamsRecordOperation<Integer>("FLOOR"){ 
			@SuppressWarnings("unchecked")
			@Override
			public Integer apply(List<Symbol<?>> params, Void v) {
				/** Return as an INT the largest integer less than the float argument. 
				 * Safe on ints, null otherwise
				 **/
				Symbol<?> s= params.get(0);
				switch (s.type) {
				case FLOAT :
					return (int) Math.floor(((Symbol<Float>)s).value);
				case INT :
					return ((Symbol<Integer>)s).value;
				default:
					LogST.logP(2,"floor function called with incorrect type on symbol "+params.get(0).toString());
					return null;
				}
			}
		}).put(DataType.INT, "ceil", new AbstractNoParamsRecordOperation<Integer>("CEIL"){ 
			@SuppressWarnings("unchecked")
			@Override
			public Integer apply(List<Symbol<?>> params, Void v) {
				/** Return as an INT the smallest integer greater than the float argument. 
				 * Safe on ints, null otherwise
				 **/
				Symbol<?> s= params.get(0);
				switch (s.type) {
				case FLOAT :
					return (int) Math.ceil(((Symbol<Float>)s).value);
				case INT :
					return ((Symbol<Integer>)s).value;
				default:
					LogST.logP(2,"floor function called with incorrect type on symbol "+params.get(0).toString());
					return null;
				}
			}
		}).put(DataType.FLOAT, "round", new AbstractNoParamsRecordOperation<Float>("ROUND"){ 
			/**
			 * This is a quick rounding routine, will round a floating point value (first parameter f, FLOAT) 
			 * to the specified number of digits (second parameter K, INTEGER). Limits: 1e-110 < f 1e+110, 0<=k<=7.
			 * 
			 * Will ignore null parameters, but raise a runtime exception for other types or values outside boundaries.
			 * 
			 * FIXME add a table with decimal values instead of powers.
			 */
			@SuppressWarnings("unchecked")
			@Override
			public Float apply(List<Symbol<?>> params, Void v) {
				Symbol<?> s= params.get(0);
				Symbol.assertType(s, DataType.FLOAT);
				Symbol<?> s2= params.get(1);
				Symbol.assertType(s2, DataType.INT);
				float f =((Symbol<Float>) s).value;
				int k =((Symbol<Integer>) s2).value;
				
				if (f<=1e-110 || f>=1e110 || k<0 || k>7) 
					throw new RuntimeException ("round() called with bad parameters "+f+","+k);
				return Math.round(f * powerOf10[k])/powerOf10[k];
				
			}
/***
		}).put(DataType.INT, "quant3", new AbstractNoParamsRecordOperation<Integer>("QUANT3"){ 
			@SuppressWarnings("unchecked")
			@Override
			public Integer apply(List<Symbol<?>> params, Void v) {
				return 0;
			}
		}).put(DataType.INT, "quant4", new AbstractNoParamsRecordOperation<Integer>("QUANT4"){ 
			@SuppressWarnings("unchecked")
			@Override
			public Integer apply(List<Symbol<?>> params, Void v) {
				return 0;
			}
	******************/
		}).put(DataType.INT, "irandom", new AbstractNoParamsRecordOperation<Integer>("IRANDOM"){ 
			@Override
			public Integer apply(List<Symbol<?>> params, Void v) {
				return generator1.nextInt();
			}
		}).put(DataType.FLOAT, "frandom", new AbstractNoParamsRecordOperation<Float>("FRANDOM"){ 
			@Override
			public Float apply(List<Symbol<?>> params, Void v) {
				return generator2.nextFloat();
			}
			
		});
		;
		/************* End of function operator definition ***********/

		
	}

	/************************ function state definitions and support methods ******************************/
	
	/**
	 * array of powers of 10 used by the round() apply() to avoid calling pow().
	 */
	private static float powerOf10[] ={1,10,100,1000,10000,100000,1000000,10000000};
	
	/**
	 * the random generators used by irandom() and frandom()
	 */
	private static SecureRandom generator1, generator2;
	
	/**
	 * Provide support for static stuff that we need to initialize in order to
	 * support functions defined in defineFunctionOperators().
	 * 
	 * Note that some functions do their own init stuff automatically on first
	 * call. That is useful for large data structures (e.g. large maps). For
	 * small objects, we put the initialization here to reduce code size in the
	 * list of anonymous apply() functions.
	 */
	static public void defineFunctionOperatorsStatics() {
		/* for round() we do that statically */

		/* TODO add here for the random() function, a mechanism to state the seed via a CLI parameter */
		// the default uses a subsequence of the hex expansion of pi
		generator1 = new SecureRandom(javax.xml.bind.DatatypeConverter.parseHexBinary("A94461460FD0030EECC8C73EA4751E41E238CD993BEA0E2F3280BBA1183EB3314E548B384F6DB908"));
		generator2 = new SecureRandom(javax.xml.bind.DatatypeConverter.parseHexBinary("6F420D03F60A04BF2CB8129024977C795679B072BCAF89AFDE9A771FD9930810B38BAE12DCCF3F2E"));
		
	}	
	
	/**
	 * Evaluate a {@link Symbol} and return it as a Float value, if possible.
	 * Accepts either a <code>Symbol<float></code> or a <code>Symbol<int></code>
	 * , tolerates a <code>Symbol<MISSING></code>, but will throw exception on
	 * any other input.
	 * 
	 * TODO should maybe be defined inside the Symbol class, but I don't want to
	 * make generally true the convention that symbols can be cast for free into
	 * a different type.
	 * 
	 * @param s
	 *            an unspecified Symbol, must not be a null
	 * @return either a valid Float object, or null if the
	 *         <code>Symbol<?></code> was MISSING or empty.
	 */
	static private Float castToFloat(Symbol<?> s) throws RuntimeException {
		if (s != null) { // do not call us with null
			if (s.type == DataType.MISSING) {
				return null;
			}
			if (s.type == DataType.FLOAT) {
				return (Float) s.getValue();
			}
			if (s.type == DataType.INT) {
				Integer value = (Integer) s.getValue();
				return value.floatValue();
			}
		}
		LogST.logP(1, "OperatorDefinitions.castToFloat() called with Symbol "+ s.toString());
		throw new RuntimeException("Forbidden cast to float");
	}
}
