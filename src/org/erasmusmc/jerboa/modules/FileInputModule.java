/*
 * Copyright (c) Erasmus MC
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
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
