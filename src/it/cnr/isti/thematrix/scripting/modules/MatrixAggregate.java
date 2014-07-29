package it.cnr.isti.thematrix.scripting.modules;

import it.cnr.isti.thematrix.configuration.LogST;
import it.cnr.isti.thematrix.scripting.aggregate.support.AggregateFunction;
import it.cnr.isti.thematrix.scripting.aggregate.support.Avg;
import it.cnr.isti.thematrix.scripting.aggregate.support.Count;
import it.cnr.isti.thematrix.scripting.aggregate.support.Max;
import it.cnr.isti.thematrix.scripting.aggregate.support.Min;
import it.cnr.isti.thematrix.scripting.aggregate.support.StDev;
import it.cnr.isti.thematrix.scripting.aggregate.support.Sum;
import it.cnr.isti.thematrix.scripting.sys.DatasetRecord;
import it.cnr.isti.thematrix.scripting.sys.DatasetSchema;
import it.cnr.isti.thematrix.scripting.sys.MatrixModule;
import it.cnr.isti.thematrix.scripting.sys.Symbol;
import it.cnr.isti.thematrix.scripting.sys.TheMatrixSys;
import it.cnr.isti.thematrix.scripting.utils.DataType;
import it.cnr.isti.thematrix.scripting.utils.Tuple2;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Implements the Aggregate module.
 * 
 * FIXME check log output, maybe incorrect and certainly poorly readable
 * 
 * @author edoardovacchi
 */
public class MatrixAggregate extends MatrixModule {
	private static final long serialVersionUID = 8114713558532476534L;

	private MatrixModule input;
	/**
	 * a list of column names by which rows should be grouped
	 */
	private final List<String> groupBy;
	/**
	 * This field will hold the constructor argument with the same name: a list
	 * of pairs (function_name, column_name) where function_name is a key in
	 * {@link #aggregates} and column_name is the name of a column in
	 * {@link #input}
	 **/
	private final List<Tuple2<String, String>> functions;
	/**
	 * list of columns in this module that will contain the aggregated results
	 */
	private final List<Symbol<?>> results;
	private final boolean isInputSorted;

	/**
	 * a list of {@link AggregateFunction} each of which must be applied to one
	 * column of the {@link #input} module
	 */
	private ArrayList<AggregateFunction> funcImpl;

	/**
	 * a list of {@link Symbol} representing the column of the {@link #input}
	 * module to which the n-th aggregate function must be applied
	 */
	private ArrayList<Symbol<?>> arguments = new ArrayList<Symbol<?>>();

	
	/** collections of variables to organzie the computatio 
	 * record: the current read record from input
	 * previous: the row _read_ before the curren record
	 * lastAggregated: last row used to update the aggregators
	 * ahead_buffer: the row read after a change of value in groupby
	 */
	DatasetRecord ahead_buffer = null;
	DatasetRecord previous = null;
	DatasetRecord lastAggregated = null;
	DatasetRecord record = null;
	
	/**
	* init is called after first next
	*/
	boolean inited = false;


	
	
	/**
	 * map function_name -> {@link AggregateFunction}
	 */
	private static HashMap<String, Class<? extends AggregateFunction>> aggregates = new HashMap<String, Class<? extends AggregateFunction>>();
	static {
		aggregates.put("COUNT", Count.class);
		aggregates.put("MIN", Min.class);
		aggregates.put("MAX", Max.class);
		aggregates.put("AVG", Avg.class);
		aggregates.put("STDEV", StDev.class);
		aggregates.put("SUM", Sum.class);
	}

	/**
	 * 
	 * @param name
	 *            name of the module
	 * @param inputTable
	 *            name of the input module
	 * @param schemaName
	 *            schema for the input module
	 * @param groupBy
	 *            list of columns by which the input shall be grouped by
	 * @param functions
	 *            a list of pairs (function_name, column_name) where column_name
	 *            is the column to which the function named function_name should
	 *            be applied
	 * @param results
	 *            a list of freshly-generated {@link Symbol}s where the results
	 *            should be written to.
	 * @param sorted
	 *            true if the input module has been already sorted
	 */
	public MatrixAggregate(String name, String inputTable, String schemaName,
			List<String> groupBy, List<Tuple2<String, String>> functions,
			List<Symbol<?>> results, boolean sorted) {
		super(name);
		this.input = TheMatrixSys.getModule(inputTable);
		input.schemaMatches(schemaName);
		this.groupBy = groupBy;
		this.functions = functions;
		this.funcImpl = new ArrayList<AggregateFunction>();
		this.results = results;
		this.isInputSorted = sorted;
		this.input.addConsumer(this);
	}

	@Override
	public void setup() {
		// DatasetSchema newSchema = DatasetSchema.extend(input.getSchema(),
		// results, this.name);
		List<Symbol<?>> protoSchema = new ArrayList<Symbol<?>>();
		DatasetSchema inputSk = input.getSchema();

		// add to schema each field in groupBy clause
		for (String oldField : groupBy) {
			protoSchema.add(inputSk.get(oldField));
		}

		LogST.logP(1, "matrixaggr-input::" + input.name);
		/* touching attributes here causes problems !! +input.attributes() ); */

		// add each field in the results
		protoSchema.addAll(results);

		DatasetSchema newSchema = new DatasetSchema(this.name + "$custom");
		newSchema.putAll(protoSchema);

		this.setSchema(newSchema);

		for (Symbol<?> result : results) {
			this.put(result.name, result);
		}
		// end of schema def
		
		record = DatasetRecord.emptyRecord(this.input);
	}

	private void prepareFields() {
		for (Tuple2<String, String> tuple : functions) {
			AggregateFunction f = null;
			try {
				/*
				 * FIXME now we will use the Constructor method to call the
				 * 1-parameter constructor and specify the type to it.
				 */
				// get the function named `tuple._1` and instantiate its class
				// / OLD way f = aggregates.get(tuple._1).newInstance();

				Constructor<? extends AggregateFunction> ct = aggregates.get(
						tuple._1).getDeclaredConstructor(DataType.class);
				// ct.setAccessible(true); // TODO check why?
				
				f = ct.newInstance(input.get(tuple._2).type);

			} catch (InstantiationException e) {
				throw new Error(e);
			} catch (IllegalAccessException e) {
				throw new Error(e);
			} catch (NullPointerException ex) {
				LogST.logP(0,
						"[ERROR WARNING: MatrixAggregate().ApplyFunction:"
								+ this.name + "] " + tuple._1
								+ " is not implemented.");
				throw new Error(ex); // Unimplemented shall halt execution
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				// related to finding the right constructor
				// TODO Auto-generated catch block
				// FIXME
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// related to finding the right constructor
				// TODO Auto-generated catch block
				// FIXME
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// We got an exception in the called constructor!!
				LogST.logException(e);
				LogST.logP(-1, e.getTargetException().toString());
			}

			// enqueue this function implementation to the end of the list
			// funcImpl
			this.funcImpl.add(f);

			// enqueue the column reference (Symbol instance) to the list of
			// arguments
			this.arguments.add(input.get(tuple._2));
		}

	}

	@Override
	public void changeInput(MatrixModule m) {
		this.input.schemaMatches(m.getSchema().name);
		this.input = m;
	}

	@Override
	public void exec() {
		LogST.logP(2, "NO-OP MatrixAggregate.exec() called on module "
				+ this.name);
	}

	
	@Override
	public void reset() {
		if (!inited) {
			inited = true;
			this.prepareFields();
		}
	}

	/**
	 * This is the same as input.hasMore() EXCEPT when we are on the last row of
	 * the output, which is inside the lastRecord buffer; only in that case
	 * bufferNeedsFlushing becomes true.
	 * 
	 * (non-Javadoc)
	 * 
	 * @see it.cnr.isti.thematrix.scripting.sys.MatrixModule#hasMore()
	 */
	@Override
	public boolean hasMore() 
	{
		// that was here to be sure to call the hasMore().
		// from revision 1440 -- not sure if still needed
		boolean hasMore = input.hasMore();

		return (ahead_buffer != null || hasMore);
	}


	
	@Override
	public void next()
	{
		if (!inited)
			reset();
		
		boolean stay = true;
			
		while ((input.hasMore() || ahead_buffer != null) && stay) 
		{
			// if the buffer is full, take the data from it
			if (ahead_buffer != null)
			{
				record.setAll(ahead_buffer);
				ahead_buffer = null;
			}
			else // else, read the input
			{
				input.next();
				record.setAll(input);
			}
			
			// now check if the current read record is in the same group
			if (isSameGroup(record, previous))
			{
				updateAggreagators(record);
				
				if (lastAggregated == null)
					lastAggregated =  DatasetRecord.emptyRecord(this.input);
				lastAggregated.setAll(record);
			}
			else // if not put it in the buffer and exit the while
			{
				ahead_buffer = DatasetRecord.emptyRecord(this.input);
				ahead_buffer.setAll(record);
		
				stay = false;
			}
			
			// save the previous
			if (previous == null)
				previous = DatasetRecord.emptyRecord(this.input);
			previous.setAll(record);
		}
		
		
		// copy constant values
		for (String f : groupBy)
			this.set(f, lastAggregated.get(f).value);

		
		// copy each result to its field
		for (int i = 0; i < results.size(); i++) {
			results.get(i).setValue(funcImpl.get(i).getResult());
			funcImpl.get(i).reset();
		}
		
	}
	
	
	/**
	 * compute the right aggregate for each column argument
	 * with the values in the record parameter
	 * @param record
	 */
	private void updateAggreagators(DatasetRecord record)
	{
		for (int i = 0; i < results.size(); i++) {
			// memo: tuple._1 function name
			// tuple._2 columnName
			String columnName = this.functions.get(i)._2;
			Symbol<?> arg = record.get(columnName);
			funcImpl.get(i).compute(arg);
		}
	}
	
	private boolean isSameGroup(DatasetRecord newRecord, DatasetRecord oldRecord) 
	{
		if (oldRecord == null)
			return true;
		
		for (String f : groupBy) {
			Object v1 = oldRecord.get(f).value;
			Object v2 = newRecord.get(f).value;
			
			if (v1 == null && v1 != v2 || v1 != null && !v1.equals(v2)) {
				// LogST.logP(3, v1 + "!=" + v2);
				return false;
			}
		}
		return true;
	}
}
