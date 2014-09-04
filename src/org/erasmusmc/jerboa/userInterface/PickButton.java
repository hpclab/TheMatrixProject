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
package org.erasmusmc.jerboa.userInterface;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;

import org.erasmusmc.jerboa.JerboaObjectExchange;
import org.erasmusmc.jerboa.modules.JerboaModule;
import org.erasmusmc.utilities.DirectoryUtilities;

/**
 * Button for picking a file for opening or saving.
 * @author schuemie
 *
 */
public class PickButton extends JButton{
	
	public static int OPEN = 1;
	public static int SAVE = 2;
	
	private JerboaModule module;
	private String variableName;
	private int type;
	private static final long serialVersionUID = 2236991901132817747L;

	/**
	 * 
	 * @param type	Type of button (OPEN or SAVE).
	 * @param module	The module containing the variable to which the selected filename should be saved.
	 * @param variableName	Name of the variable of the module to which the selected filename should be saved.
	 */
	public PickButton(int type, JerboaModule module, String variableName){
		super("Pick file");
	  this.module = module;
	  this.variableName = variableName;
	  this.type = type;
    addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        pickFile();
      }
    });
	}
	
  private void pickFile() {
    JFileChooser fileChooser = new JFileChooser(new File(JerboaObjectExchange.workingFolder));
    int returnVal;
    if (type == SAVE)
    	returnVal = fileChooser.showSaveDialog(module);
    else
    	returnVal = fileChooser.showOpenDialog(module);

    if (returnVal == JFileChooser.APPROVE_OPTION){
      module.setParameter(variableName, DirectoryUtilities.getRelativePath(new File(JerboaObjectExchange.workingFolder), fileChooser.getSelectedFile()));
      module.updateParameterValues();
    }
  }

}
