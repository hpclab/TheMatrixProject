package org.erasmusmc.jerboa.modules;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.erasmusmc.jerboa.JerboaObjectExchange;
import org.erasmusmc.jerboa.dataClasses.AggregatedDataFileWriter;
import org.erasmusmc.utilities.ReadCSVFile;

/**
 * Merges up to 24 tables into a single file, and will include the settings of the current workflow, and 
 * optionally the console output.<BR>
 * Deprecated: From now on, use ConcatenateFilesModule
 * @author schuemie
 *
 */
@Deprecated
public class MergeAggregatedTablesModule extends JerboaModule{

  public JerboaModule table1;
  public JerboaModule table2;
  public JerboaModule table3;
  public JerboaModule table4;
  public JerboaModule table5;
  public JerboaModule table6;
  public JerboaModule table7;
  public JerboaModule table8;
  public JerboaModule table9;
  public JerboaModule table10;
  public JerboaModule table11;
  public JerboaModule table12;
  public JerboaModule table13;
  public JerboaModule table14;
  public JerboaModule table15;
  public JerboaModule table16;
  public JerboaModule table17;
  public JerboaModule table18;
  public JerboaModule table19;
  public JerboaModule table20; 
  public JerboaModule table21;
  public JerboaModule table22;
  public JerboaModule table23;
  public JerboaModule table24;
  
  /**
   * Include the console output in the merged file.<BR>
   * default = true
   */
  public boolean includeConsoleOutput = true;
  
  private static final long serialVersionUID = 7746608107895964925L;
  
  protected void runModule(String outputFilename){
    List<String> inputFiles = new ArrayList<String>();
    if (table1 != null)
    	inputFiles.add(table1.getResultFilename());
    if (table2 != null)
    	inputFiles.add(table2.getResultFilename());
    if (table3 != null)
    	inputFiles.add(table3.getResultFilename());
    if (table4 != null)
    	inputFiles.add(table4.getResultFilename());
    if (table5 != null)
    	inputFiles.add(table5.getResultFilename());
    if (table6 != null)
    	inputFiles.add(table6.getResultFilename());
    if (table7 != null)
    	inputFiles.add(table7.getResultFilename());
    if (table8 != null)
    	inputFiles.add(table8.getResultFilename());
    if (table9 != null)
    	inputFiles.add(table9.getResultFilename());
    if (table10 != null)
    	inputFiles.add(table10.getResultFilename());
    if (table11 != null)
    	inputFiles.add(table11.getResultFilename());
    if (table12 != null)
    	inputFiles.add(table12.getResultFilename());
    if (table13 != null)
    	inputFiles.add(table13.getResultFilename());
    if (table14 != null)
    	inputFiles.add(table14.getResultFilename());
    if (table15 != null)
    	inputFiles.add(table15.getResultFilename());
    if (table16 != null)
    	inputFiles.add(table16.getResultFilename());
    if (table17 != null)
    	inputFiles.add(table17.getResultFilename());
    if (table18 != null)
    	inputFiles.add(table18.getResultFilename());
    if (table19 != null)
    	inputFiles.add(table19.getResultFilename());
    if (table20 != null)
    	inputFiles.add(table20.getResultFilename());
    if (table21 != null)
    	inputFiles.add(table21.getResultFilename());
    if (table22 != null)
    	inputFiles.add(table22.getResultFilename());
    if (table23 != null)
    	inputFiles.add(table23.getResultFilename());
    if (table24 != null)
    	inputFiles.add(table24.getResultFilename());
    
    process(inputFiles, outputFilename);  
  }

  private void process(List<String> inputFiles, String target) {
    AggregatedDataFileWriter out = new AggregatedDataFileWriter(target);
    if (JerboaObjectExchange.mainWorkflowPanel != null){
    	if (includeConsoleOutput)
        out.writeHeader(JerboaObjectExchange.mainWorkflowPanel.getSettings(), JerboaObjectExchange.version, JerboaObjectExchange.runLabel, JerboaObjectExchange.console.getText());
    	else
    		out.writeHeader(JerboaObjectExchange.mainWorkflowPanel.getSettings(), JerboaObjectExchange.version, JerboaObjectExchange.runLabel, null);
    }
    for (String inputFile : inputFiles){
    	out.writeStartOfTable(new File(inputFile).getName());
      for (List<String> cells : new ReadCSVFile(inputFile))
        out.write(cells);
    }
    out.close();
  }
}
