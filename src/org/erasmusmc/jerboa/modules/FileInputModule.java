package org.erasmusmc.jerboa.modules;

import java.io.File;

import javax.swing.JPanel;

import org.erasmusmc.jerboa.userInterface.PickButton;

/**
 * Module that points to an input file.
 * 
 * @author martijn
 * 
 */
public class FileInputModule extends JerboaModule 
{  
  /**
   * Stop the workflow when the file is not found.<BR>
   * default = true
   */
  public boolean stopWhenNotFound = true;
  
  private static final long serialVersionUID = -5582314196609710496L;

  protected JPanel createParameterPanel() 
  {
	  JPanel panel = super.createParameterPanel();
	  PickButton pickButton = new PickButton(PickButton.OPEN, this, "outputFilename");
	  panel.add(pickButton);
	  return panel;
  }

  protected void runModule(String outputFilename)
  {
	  File file = new File(outputFilename);
	  
	  if (file.exists()) 
	  {
		  System.out.println("File found");
		  setResultFilename(outputFilename);
	  } 
	  else 
	  {
		  setResultFilename(null);
      
		  if (stopWhenNotFound)
			  throw new RuntimeException("File not found: "+ outputFilename);
	  }
  }
}
