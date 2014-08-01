/*
 * Copyright (c) 2010-2014 "HPCLab at ISTI-CNR"
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.cnr.isti.thematrix.mapping.creator;

import it.cnr.isti.thematrix.common.Enums.CompressionType;
import it.cnr.isti.thematrix.mapping.utils.CSVFile;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import javax.xml.bind.JAXBException;

/**
 * Class holding code to access MySQL database in streaming fashion, version
 * one. Will likely be merged within other classes in the next versions.
 * 
 * @author distefano
 * 
 */
public class MySQLAccess {
  private Connection connect = null;
  private Statement statement = null;
  private PreparedStatement preparedStatement = null;
  private ResultSet resultSet = null;
  private static int DB_FETCH_SIZE = 100;
  public void readDataBase() throws Exception {
    try {
      // This will load the MySQL driver, each DB has its own driver
      Class.forName("com.mysql.jdbc.Driver");
      // Setup the connection with the DB
      connect = DriverManager
          .getConnection("jdbc:mysql://localhost/matricetest?"
              + "user=sqluser&password=sqluser");

      // Statements allow to issue SQL queries to the database
      //statement only in memory
      //statement = connect.createStatement();
      
      //statement in streaming
      statement = connect.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY);
      statement.setFetchSize(Integer.MIN_VALUE);
      
      // Result set get the result of the SQL query
      resultSet = statement
              .executeQuery("select * from matricetest.COMMENTS");
      dumpTable(resultSet);
      
    } catch (Exception e) {
      throw e;
    } finally {
      close();
    }

  }
  private void dumpTable(ResultSet resultSet) throws SQLException, NoSuchAlgorithmException, IOException, JAXBException {
	    // ResultSet is initially before the first data set
	  	CSVFile fileCsv = new CSVFile ("","newCsvFile","",CompressionType.NONE);
	  	
		int recordQueryCount = 0;
		
		int rowCount = 0;
		boolean running = true;
		
		ResultSetMetaData rsmd = resultSet.getMetaData();
		int availableColumns = rsmd.getColumnCount();
		
		//prepare header, in TheMatrix this have to replaced with calling createHeader()!
		ArrayList<String> header = new ArrayList<String>();
		int column = rsmd.getColumnCount();
		for (int columnCount = 1; columnCount <= column; columnCount++) {
			header.add(rsmd.getColumnName(columnCount));
		}
		fileCsv.setHeader(header);
		int iterationCount = 0;
		resultSet.next();
		recordQueryCount++;
		while (running) {
			boolean batchDone = false;
			int nextColumn = 0;
			while (running && !batchDone) { // FIXME here running should not be tested IMHO
				if (rowCount < DB_FETCH_SIZE) {
					
					for (int columnCount = 0; columnCount < availableColumns; columnCount++) {
				    	  fileCsv.setValue(nextColumn + columnCount, resultSet.getString(columnCount + 1));
						}
					running = resultSet.next();
					recordQueryCount++;
					rowCount++;
				}else { // end of a batch
					//LogST.logP(0, "Matrix IadCreator - switching resultset with rowCount " + rowCount);
					batchDone = true;
				}
		    }
			rowCount=0;
		    fileCsv.save(iterationCount > 0);
		    iterationCount++;
		}
		fileCsv.closeFile();
		//System.out.println(recordQueryCount);
	  }
// You need to close the resultSet
private void close() {
  try {
    if (resultSet != null) {
      resultSet.close();
    }

    if (statement != null) {
      statement.close();
    }

    if (connect != null) {
      connect.close();
    }
  } catch (Exception e) {

  }
}

 //for test only!!
  private void writeResultSet(ResultSet resultSet) throws SQLException {
    // ResultSet is initially before the first data set
	int recordQueryCount = 0;
	int nextColumn = 0;
    while (resultSet.next()) {
      recordQueryCount++;
      ResultSetMetaData rsmd = resultSet.getMetaData();
      int availableColumns = rsmd.getColumnCount();
      //String id = resultSet.getString("id");
      //String user = resultSet.getString("myuser");
      //String website = resultSet.getString("webpage");
      //String summary = resultSet.getString("summary");
      //Date date = resultSet.getDate("datum");
      //String comment = resultSet.getString("comments");
      /*
      for (int columnCount = 0; columnCount < availableColumns; columnCount++) {
			//System.out.println(recordQueryCount +": "+id+":"+user+" "+resultSet.getString(columnCount + 1));
		}
      */
	  //resultSet.next();
      // It is possible to get the columns via name
      // also possible to get the columns via the column number
      // which starts at 1
      // e.g. resultSet.getSTring(2);
      /*
      System.out.print("Rec: " + recordQueryCount);
      System.out.print(" # User: " + user);
      System.out.print(" # Website: " + website);
      System.out.print(" # Summary: " + summary);
      System.out.print(" # Date: " + date);
      System.out.println(" # Comment: " + comment);
      */
    }
    System.out.println(recordQueryCount);
  }
  
  

} 