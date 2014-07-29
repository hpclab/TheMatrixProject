package it.cnr.isti.thematrix.configuration.setting;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import it.cnr.isti.thematrix.dbms.Kind.*;

@XmlRootElement(name = "theMatrix")
public class TheMatrix 
{
	@XmlElement
	private String version;

	@XmlElement
	private DbConnection dbConnection;
	
	@XmlElement
	private Path path;

	/**
	 * String used to select the compression method to be used by BufferFileModule. Defaults to NONE if element is not
	 * provided. Used value is parsed and stored in Dynamic. See the file parsing code in Dynamic for accepted values.
	 * 
	 * FIXME add to user documentation
	 */
	@XmlElement
	private String BufferCompression = "NONE";
	
	/*** 
	 * This is the size limit for test runs. Value here is from XML, unchecked.
	 * FIXME : add to user documentation 
	 **/
	@XmlElement
	private int TestQuerySizeLimit=0;
	
	/**
	 * When true, tells the CSV manager to not perform checksum of whole files;
	 * Currently unsafe, useful for tests and very large files. 
	 */
	@XmlElement
	private boolean skipCSVFullChecksums;
	
	public String getVersion() 
	{ return version; }

	public DbConnection getDbConnection() 
	{ return dbConnection; }

	public Path getPath() 
	{ return path; }

	public Integer getTestQuerySizeLimit()
	{ return new Integer(TestQuerySizeLimit); }
	
	public String getBufferCompression()
	{ return BufferCompression; }

	public boolean getSkipCSVFullChecksums()
	{return skipCSVFullChecksums;}
	

	/***
	 * FIXME should this be here or inside the DbConnection ?
	 * @return the DBMS driver in use as an enum value
	 */
	public DbDriver getDbDriver ()
	{
		// FIXME here we should perform the conversion
//		return DbDriver.oracleDriver;
		return dbConnection.getType();
	}
}
