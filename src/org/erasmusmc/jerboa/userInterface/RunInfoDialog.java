package org.erasmusmc.jerboa.userInterface;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class RunInfoDialog extends JDialog {

	private static final long serialVersionUID = 4515079238458686550L;
	private JComboBox databaseNameField;
	private JButton okButton = new JButton("Ok");
	private JButton cancelButton = new JButton("Cancel");
	private boolean result;
	
	
	public RunInfoDialog(List<String> databaseNames){
		setTitle("Run identification");
		result = false;
		
		JPanel panel = new JPanel();
		
		panel.setLayout(new GridBagLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

		
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.ipadx = 5;
		constraints.ipady = 5;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.gridx = 0;
		constraints.gridy = 0;
		panel.add(new JLabel("Database name:"),constraints);
		
		constraints.gridx = 1;
		
		databaseNameField = new JComboBox(databaseNames.toArray());
		databaseNameField.setToolTipText("Please select the name of your database.");
		panel.add(databaseNameField,constraints);
		constraints.gridx = 0;
		constraints.gridy = 1;
		constraints.gridwidth = 2;
		panel.add(createButtonPanel(), constraints);

		add(panel);
    pack();     
    setModal(true);
    
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    setLocation(screenSize.width/2 - getWidth()/2,screenSize.height/2 - getHeight()/2);
	}
	
	private Component createButtonPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
		panel.add(Box.createHorizontalGlue());
		panel.add(okButton);
		okButton.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
      	result = true;
        setVisible(false);
      }});
		panel.add(cancelButton);
    cancelButton.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        setVisible(false);
      }});
		return panel;
	}
	
	public boolean getResult(){
		return result;
	}
	
	public String getDatabaseName(){
		return databaseNameField.getSelectedItem().toString();
	}

}
