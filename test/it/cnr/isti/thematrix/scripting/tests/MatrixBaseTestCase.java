/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.cnr.isti.thematrix.scripting.tests;

import it.cnr.isti.thematrix.scripting.filtermodule.support.MatchesFilterCondition;
import it.cnr.isti.thematrix.scripting.functions.AbstractNoParamsRecordOperation;
import it.cnr.isti.thematrix.scripting.sys.AbstractOperation2;
import it.cnr.isti.thematrix.scripting.sys.SchemaTable;
import it.cnr.isti.thematrix.scripting.sys.Symbol;
import it.cnr.isti.thematrix.scripting.sys.TheMatrixSys;
import it.cnr.isti.thematrix.scripting.utils.DataType;
import it.cnr.isti.thematrix.scripting.utils.DateUtil;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import junit.framework.TestCase;
/**
 *
 * @author edoardovacchi
 */
public class MatrixBaseTestCase extends TestCase {
    

    
    @Override
    @SuppressWarnings("unchecked")
    public void setUp() {        
    	


        
       
        // setup the schema for PERSON
 /*       DatasetSchema schema = predefinedSchemata.create("IADperson");
        
        schema.put(new Symbol<String>("PERSON_ID", "", DataType.STRING));
        schema.put(new Symbol<Date>("DATE_OF_BIRTH", DataType.DATE));
        schema.put(new Symbol<Date>("GENDER_CONCEPT_ID", DataType.INT ));
        schema.put(new Symbol<Date>("DATE_OF_DEATH", DataType.DATE));
        schema.put(new Symbol<Date>("ENDDATE", DataType.DATE));
        schema.put(new Symbol<Date>("STARTDATE", DataType.DATE));
        schema.put(new Symbol<Integer>("GP_ID", DataType.INT));
        schema.put(new Symbol<Integer>("LOCATION_CONCEPT_ID", DataType.INT));
*/

        
        
        // schema for HOSP
/*        predefinedSchemata.create("IADhosp")
                .put(new Symbol<String>("MAIN_DIAGNOSIS", null, DataType.STRING))
                .put(new Symbol<String>("START_DATE", null, DataType.DATE));
  */
        
    	// CODE COMMENTED OUT FOR TESTING -- REMOVE
    	if (false) {
		//  	put there a call to TheMatrixSys.prepareSystem() instead	
		SchemaTable predefinedSchemata = TheMatrixSys.getPredefinedSchemata();
		predefinedSchemata
				.create("IADperson")
				// many missing fields,  8 here out of 22
				.put(new Symbol<String>("PERSON_ID", null, DataType.STRING))
				.put(new Symbol<String>("DATE_OF_BIRTH", null, DataType.DATE))
				.put(new Symbol<String>("GENDER_CONCEPT_ID", null, DataType.INT))
				.put(new Symbol<String>("DATE_OF_DEATH", null, DataType.DATE))
				.put(new Symbol<String>("ENDDATE", null, DataType.DATE))
				.put(new Symbol<String>("STARTDATE", null, DataType.DATE))
				.put(new Symbol<String>("GP_ID", null, DataType.INT))
				.put(new Symbol<String>("LOCATION_CONCEPT_ID", null, DataType.INT));
		// empty schema        
		predefinedSchemata.create("custom");
		predefinedSchemata
				.create("IADhosp")
				// lots of missing fields : 20 here out of 31, plus 2 incorrect fields
				.put(new Symbol<String>("PATIENT_ID", null, DataType.STRING))
				.put(new Symbol<String>("START_DATE", null, DataType.DATE))
				.put(new Symbol<String>("END_DATE", null, DataType.DATE))
				.put(new Symbol<String>("WARD_DISCHARGE", null, DataType.STRING))
				.put(new Symbol<String>("TYPE_DISCHARGE", null, DataType.STRING))
				.put(new Symbol<String>("MAIN_DIAGNOSIS", null, DataType.STRING))
				.put(new Symbol<String>("SECONDARY_DIAGNOSIS_1", null, DataType.STRING))
				.put(new Symbol<String>("SECONDARY_DIAGNOSIS_2", null, DataType.STRING))
				.put(new Symbol<String>("SECONDARY_DIAGNOSIS_3", null, DataType.STRING))
				.put(new Symbol<String>("SECONDARY_DIAGNOSIS_4", null, DataType.STRING))
				.put(new Symbol<String>("SECONDARY_DIAGNOSIS_5", null, DataType.STRING))
				.put(new Symbol<String>("DATE_MAIN_PROC", null, DataType.DATE))
				.put(new Symbol<String>("MAIN_PROC", null, DataType.STRING))
				.put(new Symbol<String>("SECONDARY_PROC_1", null, DataType.STRING))
				.put(new Symbol<String>("SECONDARY_PROC_2", null, DataType.STRING))
				.put(new Symbol<String>("SECONDARY_PROC_3", null, DataType.STRING))
				.put(new Symbol<String>("SECONDARY_PROC_4", null, DataType.STRING))
				.put(new Symbol<String>("SECONDARY_PROC_5", null, DataType.STRING))
				.put(new Symbol<String>("TYPE", null, DataType.STRING))
				.put(new Symbol<String>("REGIME", null, DataType.STRING))
				.put(new Symbol<String>("ANPRAT", null, DataType.STRING)) // local ARS,  to be removed 
				.put(new Symbol<String>("NPRAT", null, DataType.STRING)); // local ARS, to be removed
		// schema for OUTPAT
		/** predefinedSchemata.create("IADoutpat")
		        .put(new Symbol<String>("PatientID", null, DataType.STRING))
		        .put(new Symbol<Date>("PROC_DATE",   DataType.DATE))
		        .put(new Symbol<String>("PROC_COD", null,  DataType.STRING));
		 **/
		predefinedSchemata
				.create("IADoutpat")
				// many missing fields,  6 here out of 21
				.put(new Symbol<String>("PERSON_ID", null, DataType.STRING))
				.put(new Symbol<String>("GROUP_CODE", null, DataType.STRING))
				.put(new Symbol<String>("PROC_COD", null, DataType.STRING))
				.put(new Symbol<String>("PROC_START_DATE", null, DataType.DATE))
				.put(new Symbol<String>("PROC_END_DATE", null, DataType.DATE))
				//              .put(new Symbol<String>("COST", null, DataType.INT)) 
				//              the right name seems to be VALUE, but cost is in the CSV header
				//              however, the xls reports VALUE as INT< while here we have floating point values;
				.put(new Symbol<String>("VALUE", null, DataType.FLOAT));
		// schema for DRUG
		/* predefinedSchemata.create("IADdrug")
		         .put(new Symbol<String>("PatientID", null, DataType.STRING))
		         .put(new Symbol<String>("ATC", null, DataType.STRING))
		         .put(new Symbol<Date>("DRUG_EXPOSURE_START_DATE",   DataType.DATE));
		 */
		predefinedSchemata
				.create("IADdrug")
				// REG_PROVIDER_ID
				// LHU_PROVIDER_CONCEPT_ID
				.put(new Symbol<String>("PERSON_ID", null, DataType.STRING))
				.put(new Symbol<String>("DRUG_EXPOSURE_START_DATE", null, DataType.DATE))
				.put(new Symbol<String>("PRODUCT_CODE", null, DataType.STRING))
				.put(new Symbol<String>("NUMBER_OF_BOXES", null, DataType.INT))
				.put(new Symbol<String>("ATC", null, DataType.STRING))
				.put(new Symbol<String>("DURATION", null, DataType.INT))
				//              .put(new Symbol<String>("COST", null, DataType.INT));
				//              same type mismatch as in hosp, COST is actually a FLOAT
				.put(new Symbol<String>("COST", null, DataType.FLOAT));
		// schema for EXE
		/* predefinedSchemata.create("IADexe").put(new Symbol<String>("PERSON_OD", null, DataType.STRING))
				.put(new Symbol<String>("EXEMPTION_CODE", null, DataType.STRING))
				.put(new Symbol<Date>("EXE_START_DATE", DataType.DATE))
				.put(new Symbol<Date>("EXE_END_DATE", DataType.DATE));
		 */
		predefinedSchemata
				.create("IADexe")
				// EXE_END_DATE
				.put(new Symbol<String>("PERSON_ID", null, DataType.STRING))
				.put(new Symbol<String>("EXEMPTION_CODE", null, DataType.STRING))
				.put(new Symbol<String>("EXE_START_DATE", null, DataType.DATE));
	
		

	    		 
		// FILTERS
        
        TheMatrixSys.getOpTable().put(DataType.BOOLEAN, "=", new AbstractOperation2<Boolean,Boolean>("equals") {

            @Override
            public Boolean apply(Boolean op1, Boolean op2) {
                return op1==op2 || op1 != null && op1.equals(op2);
            }
        
        }).put(DataType.INT, "=", new AbstractOperation2<Integer,Boolean>("equals") {

            @Override
            public Boolean apply(Integer op1, Integer op2) {
                return op1==op2 || op1 != null && op1.equals(op2);
            }
        
        })
        .put(DataType.INT, ">", new AbstractOperation2<Integer,Boolean>("greater-than") {
              @Override
            public Boolean apply(Integer op1, Integer op2) {
                return op1 > op2;
            }
        }) .put(DataType.INT, ">=", new AbstractOperation2<Integer,Boolean>("greater-eq") {
              @Override
            public Boolean apply(Integer op1, Integer op2) {
                return op1 >= op2;
            }
        }) .put(DataType.INT, "<=", new AbstractOperation2<Integer,Boolean>("less-eq") {
              @Override
            public Boolean apply(Integer op1, Integer op2) {
                return op1 <= op2;
            }
        })
        .put(DataType.DATE, "=", new AbstractOperation2<Date,Boolean>("=") {

            @Override
            public Boolean apply(Date op1, Date op2) {
                return op1!=null && op1.equals(op2);
            }
                    
         })
        .put(DataType.DATE, ">", new AbstractOperation2<Date,Boolean>(">") {

            @Override
            public Boolean apply(Date op1, Date op2) {
                return op1!=null && op1.after(op2);
            }
                    
        }).put(DataType.DATE, "<", new AbstractOperation2<Date,Boolean>("<") {

            @Override
            public Boolean apply(Date op1, Date op2) {
                return op1!=null && op1.before(op2);
            }
                    
        }).put(DataType.DATE, "<=", new AbstractOperation2<Date,Boolean>(">") {

            @Override
            public Boolean apply(Date op1, Date op2) {
                return op1!=null && op1.equals(op2) || op1.before(op2);
            }
                    
         }).put(DataType.DATE, ">=", new AbstractOperation2<Date,Boolean>(">") {

             @Override
             public Boolean apply(Date op1, Date op2) {
                 return op1!=null && op1.equals(op2) || op1.after(op2);
             }
                     
          }).put(DataType.STRING, "matches", new MatchesFilterCondition())
        
         .put(DataType.STRING, "=", new AbstractOperation2<String,Boolean>("=") {

            @Override
            public Boolean apply(String op1, String op2) {
                return op1==op2 || op1!=null && op1.equals(op2) ;
            }
                    
         })
            ;
        
    	     
        
        // APPLY FUNCTION OR PRODUCT
          TheMatrixSys.getFuncTable()
//            .put(DataType.STRING, "equalsTo", new AbstractRecordOperation<String,Boolean>("equalsTo") {
//
//            @Override
//            public Boolean apply(DatasetRecord op1, List<String> op2) {
//                // get first (and only) column
//                Symbol<String> target = (Symbol<String>) op1.attributes().get(0);
//                return op2.contains(target.value);
//            }
//                
//            })
            .put(DataType.INT, "elapsedTime", new AbstractNoParamsRecordOperation<Integer>("elapsedTime") {


            @Override
            public Integer apply(List<Symbol<?>> op1, Void op2) { // (ignore op2)
                Symbol<Date> olderDate = (Symbol<Date>) op1.get(0);
                Symbol<Date> newerDate = (Symbol<Date>) op1.get(1);
                
                // http://stackoverflow.com/a/3491723/7849
                int diffInDays = (int)( (newerDate.value.getTime() - olderDate.value.getTime()) 
                 / (1000 * 60 * 60 * 24) );

                return diffInDays;
                
            }
               
                
            })
            
            .put(DataType.INT, "Year", new AbstractNoParamsRecordOperation<Integer>("Year") {


            @Override
            public Integer apply(List<Symbol<?>> op1, Void op2) { // (ignore op2)
                Symbol<Date> date = (Symbol<Date>) op1.get(0);
                Calendar calendar = Calendar.getInstance();  
                calendar.setTime(date.value);  
                return calendar.get(Calendar.YEAR);
             }
               
                
            })
                  
                  
            .put(DataType.DATE, "min", new AbstractNoParamsRecordOperation<Date>("min") {


            @Override
            public Date apply(List<Symbol<?>> op1, Void op2) { // (ignore op2)
                Symbol<Date> d1 = (Symbol<Date>) op1.get(0);
                Symbol<Date> d2 = (Symbol<Date>) op1.get(1);
                
                return d1.value.before(d2.value) ? d1.value : d2.value;
                
            }
               
                
            }).put(DataType.STRING, "match", new AbstractNoParamsRecordOperation<Boolean>("match") {

                private final MatchesFilterCondition matcher = new MatchesFilterCondition();
                
            @Override
            public Boolean apply(List<Symbol<?>> patterns, Void v) {
//               Symbol<String> d1 = (Symbol<String>) op1.attributes().get(0);
//               for (String patt: patterns) {
//                   if (matcher.apply(d1.value, patt)) return true;
//               }
               throw new UnsupportedOperationException("da implementare");
            }
            })
                  
           .put(DataType.DATE, "elapsedTimeInRange", new AbstractNoParamsRecordOperation<Boolean>("elapsedTimeInRange") {


            @Override
            public Boolean apply(List<Symbol<?>> params, Void v) { // (ignore op2)
                Symbol<Date> olderDate = (Symbol<Date>) params.get(0);
                Symbol<Date> newerDate = (Symbol<Date>) params.get(1);
                
                Symbol<Integer> left = (Symbol<Integer>) params.get(2);
                Symbol<Integer> right = (Symbol<Integer>) params.get(3);
                
                // http://stackoverflow.com/a/3491723/7849
                int diffInDays = (int)( (newerDate.value.getTime() - olderDate.value.getTime()) 
                 / (1000 * 60 * 60 * 24) );

                return (diffInDays >= left.value) && (diffInDays <= right.value);
                
            }
               
                
            }).put(DataType.STRING, "Id", new AbstractNoParamsRecordOperation<String>("Id") {


            @Override
            public String apply(List<Symbol<?>> params, Void v) { // (ignore op2)
                Symbol<String> val = (Symbol<String>) params.get(0);
                return val.value;
            }
               
                
            }).put(DataType.BOOLEAN, "Id", new AbstractNoParamsRecordOperation<Boolean>("Id") {


            @Override
            public Boolean apply(List<Symbol<?>> params, Void v) { // (ignore op2)
                Symbol<Boolean> val = (Symbol<Boolean>) params.get(0);
                return val.value;
            }
               
                
            }).put(DataType.STRING, "Concat", new AbstractNoParamsRecordOperation<String>("Concat") {


            @Override
            public String apply(List<Symbol<?>> params, Void v) { // (ignore op2)
                StringBuilder sb = new StringBuilder();
                for (Symbol<?> s : params) {
                    sb.append(s.value);
                }
                return sb.toString();
            }
               
                
            }) .put(DataType.STRING, "Trim", new AbstractNoParamsRecordOperation<String>("Trim") {


            @Override
            public String apply(List<Symbol<?>> params, Void v) { // (ignore op2)
                return params.get(0).value.toString().trim();
            }
               
                
            }).put(DataType.DATE, "Min", new AbstractNoParamsRecordOperation<Date>("Min") {


            @Override
            public Date apply(List<Symbol<?>> params, Void v) { // (ignore op2)
               Date min = ((Symbol<Date>) params.get(0)).value;
               for (Symbol<?> s :params) {
                   Symbol<Date> ss = (Symbol<Date>) s;
                   if (ss.value.before(min)) { min = ss.value; }
               }
               
               return min;
            }
               
                
            }) ;
    }        
    	
    	// The actual test
        //TheMatrixSys.setArguments(new String[] { "1985-12-12", "22210" } );
        TheMatrixSys.setParams(
          new Symbol<Date>("DATE", DateUtil.parse("1985-12-12"), DataType.DATE)
        );    
    }
    
    
    @Override
    public void tearDown() {
        TheMatrixSys.getPredefinedSchemata().clear();
        TheMatrixSys.getOpTable().clear();
        // TheMatrixSys.getParams().clear();
    }
    
    
}

