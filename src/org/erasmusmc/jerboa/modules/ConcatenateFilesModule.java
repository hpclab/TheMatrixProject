package org.erasmusmc.jerboa.modules;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.erasmusmc.jerboa.JerboaObjectExchange;
import org.erasmusmc.jerboa.dataClasses.AggregatedDataFileWriter;
import org.erasmusmc.utilities.ReadCSVFile;

/**
 * Merges files into a single file, and will include the settings of the current workflow, and 
 * optionally the console output.<BR> All inputs are named input1, input2, ...
 * @author schuemie
 *
 */
public class ConcatenateFilesModule extends JerboaModule{
  
  /**
   * Include the console output in the merged file.<BR>
   * default = true
   */
  public boolean includeConsoleOutput = true;
  
  private List<JerboaModule> inputs = new ArrayList<JerboaModule>();
  private static final long serialVersionUID = 7746608107895964925L;
  
  protected void runModule(String outputFilename){
    List<String> inputFiles = new ArrayList<String>();
    for (JerboaModule input : inputs)
    	inputFiles.add(input.getResultFilename());   
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
  
  public List<JerboaModule> getInputs() {
		return inputs;
	}
  
  /**
   * Set the input module to the given module
   * @param inputName	Name of the variable that will point to the module
   * @param module Reference to the module
   */
  public void setInput(String inputName, JerboaModule module){
  	inputs.add(module);
  }
}
