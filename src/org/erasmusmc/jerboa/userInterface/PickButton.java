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
