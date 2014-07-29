package it.cnr.isti.thematrix.scripting.modules;

import it.cnr.isti.thematrix.configuration.LogST;
import it.cnr.isti.thematrix.scripting.sys.MatrixModule;
import it.cnr.isti.thematrix.scripting.sys.TheMatrixSys;

/**
 * A proxy class for matrix modules which need access to a common BufferModule. It manages the need to identify the
 * reader and keep compatibility with the <code>hasMore(); next()</code> interface (no change to other modules). Each
 * instance will get its name from the basename of its buffer, plus a "_Proxy_num" suffix.
 * 
 * FIXME not sure the BufferModule actually is in TheMatrixSys list of modules. Coding on the assumption that it is. 
 * 
 * FIXME the BufferProxy cannot access two classes easily; either merge them, or get rid of the BufferProxy in one case
 * 
 * @author massimo
 * 
 */
public class BufferProxyModule extends MatrixModule {
	private int myId;
	private BufferModule input;
	private BufferFileModule inputF;

	// either a BufferModule or a BufferFileModule ...
	// TODO we should define a common superclass/interface for buffer modules
	
	/**
	 * Constructor for a proxy, it will access its input and output to set up correct links, but will NOT patch them.
	 * Patching source and destination is left to the BufferModule, which has full info.
	 * 
	 * @param inputTable
	 *            the name of the input BufferModule, i.e. the base name of module 
	 * @param myId
	 *            the integer id of this proxy in its buffer list
	 * @param outputTable
	 *            the name of the output MatrixModule 
	 */
	public BufferProxyModule(String inputTable, int myId, String outputTable) {
		super(inputTable + "_Proxy_" + Integer.toString(myId));
		this.myId = myId;
		MatrixModule inputM=TheMatrixSys.getModule(inputTable);
		// FIXME shall we add ourselves to the table?
		// TheMatrixSys.addModule(this);

		LogST.logP(0,
				"BuferProxyModule " + this.name + " adding consumer "
						+ outputTable + "\nReferencing object\n"
						+ TheMatrixSys.getModule(outputTable));
		
		// add the outputTable module to our receiver list (it will be the only one) 
		this.addConsumer(TheMatrixSys.getModule(outputTable));
		
		this.setSchema(inputM.getSchema());

		if (inputM.getClass() == BufferModule.class)
			this.input=(BufferModule) inputM;
		else
			this.inputF =(BufferFileModule) inputM;
		
	}

	@Override
	public void setup() {
		// we need no setup
	}

	@Override
	public void exec() {
		// we need no exec
	}

	@Override
	public void reset() {
		// we need no reset
	}

	@Override
	public boolean hasMore() 
	{
		if (input!=null) 
			return input.hasMore(myId);
		else return inputF.hasMore(myId);
	}

	@Override
	public void next() 
	{
		if (input != null)
		{
			input.next(myId);
			LogST.logP(3, "BufferProxy "+myId+" redirects on "+input.name);
		}
		else 
		{
			inputF.next(myId);
			LogST.logP(3, "BufferProxy "+myId+" redirects on "+inputF.name);
		}
	}

}
