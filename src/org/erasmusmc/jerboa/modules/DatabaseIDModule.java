package org.erasmusmc.jerboa.modules;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;

import org.erasmusmc.jerboa.dataClasses.DatabaseIDFileWriter;

public class DatabaseIDModule extends JerboaModule {

	private static final long serialVersionUID = -9111316426013064140L;
	
	public List<String> databases = new ArrayList<String>();

	private String databaseID = "";

	public static void main(String[] args) {
		DatabaseIDModule module = new DatabaseIDModule(); 
		String path = "D:\\Documents\\IPCI\\SOS\\Test Data\\NSAIDs Specified\\";
		module.databases.add("IPCI");
		module.databases.add("Pedianet");
		module.databases.add("Thin");
		module.databases.add("UB2");
		module.process(path + "DatabaseID.txt");
	}

	
	protected void runModule(String outputFilename){
		process(outputFilename);
	}

	
	public void process(String target) {
		//SelectDatabaseID selectDatabaseID = new SelectDatabaseID();
		new SelectDatabaseID();
		
		DatabaseIDFileWriter dbIDFileWriter = new DatabaseIDFileWriter(target);
		dbIDFileWriter.write(databaseID);
		dbIDFileWriter.close();
	}
	
	
	private class SelectDatabaseID {
		JDialog dialog;
		JLabel label;
		
		public SelectDatabaseID() {
			JRadioButton radioButton;
			int dbCount = 0;
			
			dialog = new JDialog();
			dialog.setTitle("Select your database ID");
			JPanel panel = new JPanel();
			ButtonGroup bg = new ButtonGroup();
			
			Iterator<String> databaseIterator = databases.iterator();
			while (databaseIterator.hasNext()) {
				radioButton = new JRadioButton(databaseIterator.next());
				radioButton.addActionListener(new SelectAction());
				panel.add(radioButton);
				bg.add(radioButton);
				dbCount++;
			}
			
			label = new JLabel();
			label.setText("No database selected!");
			label.setHorizontalAlignment(SwingConstants.CENTER);
			
			JButton okButton = new JButton();
			okButton.setText("OK");
			okButton.addActionListener(new OKAction());
			
			dialog.add(panel, BorderLayout.NORTH);
			dialog.add(label, BorderLayout.CENTER);
			dialog.add(okButton, BorderLayout.SOUTH);
			
			int dialogWidth = Math.max(dbCount * 75,150);
			int dialogHeight = 125;
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			dialog.setBounds((screenSize.width - dialogWidth) / 2 , (screenSize.height - dialogHeight) / 2, dialogWidth, dialogHeight);
			dialog.setModalityType(JDialog.ModalityType.APPLICATION_MODAL);
			dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
			dialog.setVisible(true);
		}
		
		public class SelectAction implements ActionListener {
			public void actionPerformed(ActionEvent e) {
				databaseID = e.getActionCommand();
				label.setText(databaseID);
			}
		}
		
		public class OKAction implements ActionListener {
			public void actionPerformed(ActionEvent e) {
				if (!(databaseID.equals(""))) {
					dialog.setVisible(false);					
				}
			}
		}
	}
	

}
