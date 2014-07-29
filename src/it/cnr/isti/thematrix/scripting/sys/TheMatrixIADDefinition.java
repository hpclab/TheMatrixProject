package it.cnr.isti.thematrix.scripting.sys;

import java.util.Date;

import it.cnr.isti.thematrix.configuration.Dynamic;
import it.cnr.isti.thematrix.configuration.Dynamic.selectedIADVersion;
import it.cnr.isti.thematrix.configuration.LogST;
import it.cnr.isti.thematrix.scripting.utils.DataType;
//import it.cnr.isti.thematrix.scripting.sys.SymbolTable;

/**
 * Class packaging definitions for TheMatrix interpreter for the sake of code modularization.
 * 
 * The class has only static methods which are called by the main interpreter class TheMatrixSys. This is defined as a
 * separate class in order to allow quick fixing of a program (unsigned) jar archive during the alpha/beta testing.
 * Thereafter, the methods can either be ported back to TheMatrixSys or, more general solution, an interface creating
 * the schemata from the configuration XML can be designed (this may be too fragile as a solution). Support methods
 * included in the class serialize the internal schemata to System.out either as templates of mappings.xml or as a bunch
 * of toString() calls. The service schemata (used for unit testing) no longer appear here. 
 *  
 * FIXME improve the XML template generation
 * methods
 * 
 * @author massimo
 * 
 */
public final class TheMatrixIADDefinition {

	/**
	 * Method to initialize the schema symbol tables for script execution, called at system init time to prepare the
	 * full definition of the IAD table schemata. <br>
	 * The definitions given are alternative to those in TheMatrixSys, and include all IAD defined fields.<br>
	 * TODO IN THE WORKS: currently incomplete, only defines the IADhosp. Should provide schema definitions for:<br>
	 * IADperson, custom, IADhosp, IADoutpat, IADdrug, IADexe <br>
	 * plus the new schemata RESIDENT and HOME
	 * 
	 * @author edoardovacchi, massimo
	 */
	public static void defineIADFullSchemas() {

		// for now, only partial IAD implementation is available
		if (Dynamic.versionOfIAD != selectedIADVersion.PartialIAD) {
			LogST.logP(0, "TheMatrixIADDefinition : Full IAD not yet implemented");
			throw new AssertionError("TheMatrixIADDefinition : Full IAD not yet implemented");
		}

		SchemaTable predefinedSchemata = TheMatrixSys.getPredefinedSchemata();

		switch (Dynamic.versionOfIAD) {
			case PartialIAD : {
				// schema for HOSP -- recoded on 8/2/2013 to fully match the IAD definition
				/**
				 * predefinedSchemata.create("IADhosp") .put(new Symbol<String>("MAIN_DIAGNOSIS", null,
				 * DataType.STRING)) .put(new Symbol<String>("START_DATE", null, DataType.DATE));
				 */
				predefinedSchemata
				.create("IADhosp")
				// lots of missing fields : 20 here out of 31, plus 2 incorrect fields
				// all missing fields added (to be verified)
				.put(new Symbol<String>("PATIENT_ID", null, DataType.STRING))
				// VARCHAR 30
				.put(new Symbol<String>("REGIME", null, DataType.STRING))
				// VARCHAR 1
				.put(new Symbol<String>("START_DATE", null, DataType.DATE))
				.put(new Symbol<String>("ORIGIN", null, DataType.STRING)) // bug report Ivan Campa
				// VARCHAR 1
				.put(new Symbol<String>("TYPE", null, DataType.STRING))
				// VARCHAR 1
				.put(new Symbol<String>("WARD_DISCHARGE", null, DataType.STRING))
				// VARCHAR 4
				.put(new Symbol<String>("END_DATE", null, DataType.DATE))
				.put(new Symbol<String>("TYPE_DISCHARGE", null, DataType.STRING))
				// VARCHAR 1
				.put(new Symbol<String>("LENGTH_DH", null, DataType.INT))
				.put(new Symbol<String>("MAIN_DIAGNOSIS", null, DataType.STRING))
				// VARCHAR 5
				.put(new Symbol<String>("SECONDARY_DIAGNOSIS_1", null, DataType.STRING))
				// VARCHAR 5
				.put(new Symbol<String>("SECONDARY_DIAGNOSIS_2", null, DataType.STRING))
				// VARCHAR 5
				.put(new Symbol<String>("SECONDARY_DIAGNOSIS_3", null, DataType.STRING))
				// VARCHAR 5
				.put(new Symbol<String>("SECONDARY_DIAGNOSIS_4", null, DataType.STRING))
				// VARCHAR 5
				.put(new Symbol<String>("SECONDARY_DIAGNOSIS_5", null, DataType.STRING))
				// VARCHAR 5
				.put(new Symbol<String>("DATE_MAIN_PROC", null, DataType.DATE))
				.put(new Symbol<String>("MAIN_PROC", null, DataType.STRING))
				// VARCHAR 5
				.put(new Symbol<String>("SECONDARY_PROC_1", null, DataType.STRING))
				// VARCHAR 5
				.put(new Symbol<String>("SECONDARY_PROC_2", null, DataType.STRING))
				// VARCHAR 5
				.put(new Symbol<String>("SECONDARY_PROC_3", null, DataType.STRING))
				// VARCHAR 5
				.put(new Symbol<String>("SECONDARY_PROC_4", null, DataType.STRING))
				// VARCHAR 5
				.put(new Symbol<String>("SECONDARY_PROC_5", null, DataType.STRING))
				// VARCHAR 5
				.put(new Symbol<String>("BOOKING_DATE", null, DataType.DATE))
				.put(new Symbol<String>("PRIORITY", null, DataType.STRING)) // VARCHAR 1
				.put(new Symbol<String>("EXTERNAL_CAUSE", null, DataType.STRING))
				// VARCHAR 5
				.put(new Symbol<String>("MARITAL_STATUS", null, DataType.STRING))
				// VARCHAR 1
				.put(new Symbol<String>("CITIZENSHIP", null, DataType.STRING))
				// VARCHAR 3
				.put(new Symbol<String>("TRAUMA", null, DataType.STRING))
				// VARCHAR 1
				.put(new Symbol<String>("AUTOPTIC", null, DataType.STRING))
				// VARCHAR 1
				.put(new Symbol<String>("DH_CAUSE", null, DataType.STRING))
				// VARCHAR 1
				.put(new Symbol<String>("BIRTH_WEIGTH", null, DataType.INT))
				.put(new Symbol<String>("DRG", null, DataType.STRING)) // ; ; drg ;
				.put(new Symbol<String>("VALUE", null, DataType.FLOAT)) // ; ; ;
				; 
				// ANPRAT and NPRAT reinstated to allow working with ARS datasets
				// .put(new Symbol<String>("ANPRAT", null, DataType.STRING)) // local ARS, to be removed
				// .put(new Symbol<String>("NPRAT", null, DataType.STRING)); // local ARS, to be removed

				/**
				 * The following schemata are _for_sure_ not complete and shall be edited to match the IAD definitions
				 * from ROSA
				 */

				predefinedSchemata
				.create("IADperson")
				.put(new Symbol<String>("PERSON_ID", null, DataType.STRING))
				.put(new Symbol<Date>("DATE_OF_BIRTH", null, DataType.DATE))
				// .put(new Symbol<Integer>("GENDER_CONCEPT_ID", null, DataType.INT)) OLD , now string
				.put(new Symbol<Date>("DATE_OF_DEATH", null, DataType.DATE))
				.put(new Symbol<Date>("ENDDATE", null, DataType.DATE))
				.put(new Symbol<Date>("STARTDATE", null, DataType.DATE))
				// .put(new Symbol<Integer>("GP_ID", null, DataType.INT)) now string
				// .put(new Symbol<Integer>("LOCATION_CONCEPT_ID", null, DataType.INT)) now string
				/* autogenerated entries; duplicates removed by hand */
				.put(new Symbol<String>("REG_CONCEPT_ID", null, DataType.STRING)) // ; ; region_ISTAT ;
				.put(new Symbol<String>("LHU_PROVIDER_CONCEPT_ID", null, DataType.STRING)) // ; ; ;
				// .put(new Symbol<String>("PERSON_ID", null, DataType.STRING)) // ; ; ;
				.put(new Symbol<String>("GENDER_CONCEPT_ID", null, DataType.STRING)) // ; M,F ; ;
				// .put(new Symbol<Date>("DATE_OF_BIRTH", null, DataType.DATE)) //18700101-20201231 ; ; ;
				.put(new Symbol<String>("BIRTH_LOCATION_CONCEPT_ID", null, DataType.STRING)) // ; ;
				// municipality_ISTAT
				// ;
				.put(new Symbol<String>("CENSUS_LOCATION_CONCEPT_ID", null, DataType.STRING)) // ; TBD ; ;
				.put(new Symbol<String>("LOCATION_CONCEPT_ID", null, DataType.STRING)) // ; TBD ;
				// municipality_ISTAT ;
				// address
				.put(new Symbol<String>("LHU_LOCATION_CONCEPT_ID", null, DataType.STRING)) // ; ; ;
				.put(new Symbol<String>("GP_ID", null, DataType.STRING)) // ; ; ;
				.put(new Symbol<Date>("STARTDATE_GP", null, DataType.DATE)) // 19700101-20201231 ; ; ;
				.put(new Symbol<Date>("ENDDATE_GP", null, DataType.DATE)) // 19700101-20201231 ; ; ;
				// .put(new Symbol<Date>("STARTDATE", null, DataType.DATE)) //19700101-20201231 ; ; ;
				// .put(new Symbol<Date>("ENDDATE", null, DataType.DATE)) //19700101-20201231 ; ; ;
				.put(new Symbol<Date>("STARTDATE_LHU", null, DataType.DATE)) // 19700101-20201231 ; ; ;
				.put(new Symbol<Date>("ENDDATE_LHU", null, DataType.DATE)) // 19700101-20201231 ; ; ;
				.put(new Symbol<Boolean>("DIED", null, DataType.BOOLEAN)) // ; ; ;
				// .put(new Symbol<Date>("DATE_OF_DEATH", null, DataType.DATE)) //19700101-20201231 ; ; ;
				;

				// schema for OUTPAT
				/**
				 * predefinedSchemata.create("IADoutpat") .put(new Symbol<String>("PatientID", null, DataType.STRING))
				 * .put(new Symbol<Date>("PROC_DATE", DataType.DATE)) .put(new Symbol<String>("PROC_COD", null,
				 * DataType.STRING));
				 **/
				predefinedSchemata.create("IADoutpat")
				// many missing fields, 6 here out of 21
				/* autogenerated follow */
				.put(new Symbol<String>("REG_PROVIDER_ID", null, DataType.STRING)) // ; ; region_ISTAT ;
				.put(new Symbol<String>("LHU_PROVIDER_CONCEPT_ID", null, DataType.STRING)) // ; ; ;
				.put(new Symbol<String>("PERSON_ID", null, DataType.STRING)) // ; ; ;
				.put(new Symbol<Date>("PROC_PRESCRIPTION", null, DataType.DATE)) // 19900101-20201231 ; ; ;
				.put(new Symbol<Boolean>("FIRST_ACCESS", null, DataType.BOOLEAN)) // ; ; ;
				.put(new Symbol<Boolean>("MAX_WAITING_TIME", null, DataType.BOOLEAN)) // ; ; ;
				.put(new Symbol<String>("EXEMPTION_CODE", null, DataType.STRING)) // ; ; exemption_code ;
				.put(new Symbol<Boolean>("EXEMPTION_LOWWAGE", null, DataType.BOOLEAN)) // ; ; ;
				.put(new Symbol<String>("DIAGNOSIS", null, DataType.STRING)) // ; ; ;
				.put(new Symbol<Integer>("NUMBER_OF_PROC", null, DataType.INT)) // ; ; ;
				.put(new Symbol<Float>("VALUE", null, DataType.FLOAT)) // ; ; ;
				.put(new Symbol<String>("PROC_COD", null, DataType.STRING)) // ; ; proc_OUTPAT ;
				.put(new Symbol<String>("WARD_CODE", null, DataType.STRING)) // ; ; ward ;
				.put(new Symbol<String>("GROUP_CODE", null, DataType.STRING)) // ; ; ;
				.put(new Symbol<Date>("PROC_BOOKING_DATE", null, DataType.DATE)) // 19900101-20201231 ; ; ;
				.put(new Symbol<Date>("PROC_START_DATE", null, DataType.DATE)) // 19900101-20201231 ; ; ;
				.put(new Symbol<Date>("PROC_END_DATE", null, DataType.DATE)) // 19900101-20201231 ; ; ;
				.put(new Symbol<Date>("PROC_DATE", null, DataType.DATE)) // 19900101-20201231 ; ; ;
				.put(new Symbol<String>("TYPE_PROC", null, DataType.STRING)) // ; ; ;
				.put(new Symbol<Integer>("QUANTITY", null, DataType.INT)) // ; ; ;
				;

				// schema for DRUG
				/**
				 * predefinedSchemata.create("IADdrug") .put(new Symbol<String>("PatientID", null, DataType.STRING))
				 * .put(new Symbol<String>("ATC", null, DataType.STRING)) .put(new
				 * Symbol<Date>("DRUG_EXPOSURE_START_DATE", DataType.DATE));
				 */
				predefinedSchemata.create("IADdrug")
				// REG_PROVIDER_ID
				// LHU_PROVIDER_CONCEPT_ID
				/* autogenerated follow */
				.put(new Symbol<String>("REG_PROVIDER_ID", null, DataType.STRING)) // ; ; region_ISTAT ;
				.put(new Symbol<String>("LHU_PROVIDER_CONCEPT_ID", null, DataType.STRING)) // ; ; ;
				.put(new Symbol<String>("PERSON_ID", null, DataType.STRING)) // ; ; ;
				.put(new Symbol<Date>("DRUG_PRESCRIPTION", null, DataType.DATE)) // 19900101-20201231 ; ; ;
				.put(new Symbol<Date>("DRUG_DISPENSING_DATE", null, DataType.DATE)) // 19900101-20201231 ; ; ;
				.put(new Symbol<String>("EXEMPTION_CODE", null, DataType.STRING)) // ; ; exemption_code ;
				.put(new Symbol<Boolean>("EXEMPTION_LOWWAGE", null, DataType.BOOLEAN)) // ; ; ;
				.put(new Symbol<Integer>("NUMBER_OF_BOXES", null, DataType.FLOAT)) // can be fractional ; ; ;
				.put(new Symbol<Float>("COST", null, DataType.FLOAT)) // ; ; ;
				.put(new Symbol<String>("FOREIGN_COUNTRY", null, DataType.STRING)) // ; ; ;
				.put(new Symbol<String>("PRODUCT_CODE", null, DataType.STRING)) // ; ; PRODUCT_CODE_AIFA ;
				.put(new Symbol<String>("ATC", null, DataType.STRING)) // ; ; derived from PRODUCT_CODE_AIFA ;
				.put(new Symbol<Float>("DURATION", null, DataType.FLOAT)) // ; ; derived from PRODUCT_CODE_AIFA
				// ; formula (from
				// PRODUCT_CODE_AIFA)
				// DURATION=duration_of_box *
				// NUMBER_OF_BOXES
				;

				// schema for EXE
				predefinedSchemata.create("IADexe").put(new Symbol<String>("PERSON_ID", null, DataType.STRING))
						.put(new Symbol<String>("EXEMPTION_CODE", null, DataType.STRING))
						.put(new Symbol<String>("EXE_START_DATE", null, DataType.DATE))
						.put(new Symbol<Date>("EXE_END_DATE", null, DataType.DATE));
				// EXE_END_DATE

				/********** new table for schemata previously missing *******/
				predefinedSchemata.create("IADresident")
				// .put(new Symbol<>("PERSON_ID", null, DataType.)) // ; ; ; errore nella definizione!!
				.put(new Symbol<String>("PERSON_ID", null, DataType.STRING)) // ; ; ; PATCH MANUALE
				.put(new Symbol<String>("REG_PROVIDER_ID", null, DataType.STRING)) // ; ; region_ISTAT ;
				.put(new Symbol<String>("LHU_PROVIDER_CONCEPT_ID", null, DataType.STRING)) // ; ; ;
				.put(new Symbol<Date>("ADMISSION_DATE", null, DataType.DATE)) // ; ; ;
				.put(new Symbol<String>("ADMISSION_TYPE", null, DataType.STRING)) // ; ; ;
				.put(new Symbol<String>("PREVIOUS_LOCATION", null, DataType.STRING)) // ; ; ; 1 - Abitazione 2 -
				// Struttura protetta
				// socio-sanitaria 3 -
				// Struttura sociale 4 -
				// Struttura ospedaliera
				// 5 - Struttura di
				// riabilitazione 9 -
				// Altro
				.put(new Symbol<String>("REQUEST", null, DataType.STRING)) // ; ; ; 1 - Propria 2 - Familiare o
				// affine 3 - Soggetto civilmente
				// obbligato 4 - Assistente sociale
				// 5 - Medico di medicina generale 6
				// - Medico Ospedaliero
				.put(new Symbol<String>("EVALUATION", null, DataType.STRING)) // ; ; ; 1 - Si 2 - No 3 - Altro
				.put(new Symbol<String>("MOTIVATION", null, DataType.STRING)) // ; ; ; 1 - Perdita autonomia
				// (decorso degenerativo) 2 -
				// Stabilizzazione stato clinico
				// (post acuzie) 3 -
				// Insufficienza del supporto
				// familiare 4 - Solitudine 5 -
				// Alloggio non idoneo 6 - Altra
				// motivazione sociale
				.put(new Symbol<Date>("DISCHARGE_DATE", null, DataType.DATE)) // ; ; ;
				.put(new Symbol<String>("DISCHARGE_TYPE", null, DataType.STRING)) // ; ; ;
				;

				predefinedSchemata.create("IADhome")
				// .put(new Symbol<>("PERSON_ID", null, DataType.)) // ; ; ; errore nella definizione!!
				.put(new Symbol<String>("PERSON_ID", null, DataType.STRING)) // ; ; ; PATCH MANUALE
				.put(new Symbol<String>("REG_PROVIDER_ID", null, DataType.STRING)) // ; ; region_ISTAT ;
				.put(new Symbol<String>("LHU_PROVIDER_CONCEPT_ID", null, DataType.STRING)) // ; ; ;
				.put(new Symbol<Date>("HOME_ASSISTANCE_START_DATE", null, DataType.DATE)) // 19700101-20201231 ;
				// ; ;
				.put(new Symbol<String>("MAIN_DISEASE", null, DataType.STRING)) // ; ; ICD9CM ;
				.put(new Symbol<String>("SEC_DISEASE", null, DataType.STRING)) // ; ; ICD9CM ;
				;

			}
				break;
			case PreliminaryIAD :
				LogST.logP(0, "TheMatrixIADDefinition : PreliminaryIAD should not be implemented here");
				throw new AssertionError("TheMatrixIADDefinition : PreliminaryIAD unhandled");
				// break;
			case FullIAD2013 :
				LogST.logP(0, "TheMatrixIADDefinition : Full IAD not yet implemented");
				throw new AssertionError("TheMatrixIADDefinition : Full IAD not yet implemented");
		}

	}

}
