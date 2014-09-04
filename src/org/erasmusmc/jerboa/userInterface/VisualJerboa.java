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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.erasmusmc.jerboa.JerboaObjectExchange;
import org.erasmusmc.jerboa.ProgressHandler;
import org.erasmusmc.utilities.TextFileUtilities;

/**
 * The main Jerboa class.
 * @author schuemie
 *
 */
public class VisualJerboa 
{
	private JFrame f;
	private WorkflowPanel mainWorkflowPanel;
	private JButton runButton;
	private JTextField folderField;
	private JButton pickButton;
	private Console console;

	/**
	 * Possible parameters:<BR>
	 * -folder &lt;folder&gt; (specifies the working folder)<br/>
	 * -settings &lt;settings file&gt; (specifies the settings file to be loaded)<br/>
	 * -execute (automatically execute the workflow, and terminate Jerboa when done)<br/>
	 * -nogui (do not run the GUI, command line interface only)
	 * 
	 * @param args
	 */
	public static void main(String[] args)
	{
		new VisualJerboa(args);
	}
	
	protected WorkflowPanel createWorkflowPanel()
	{
		return new WorkflowPanel();
	}
	
	public VisualJerboa()
	{
		// Create main workflow panel:
		mainWorkflowPanel = createWorkflowPanel();
		JerboaObjectExchange.mainWorkflowPanel = mainWorkflowPanel;		
	}
	
	public VisualJerboa(String[] args)
	{
		// Create main workflow panel:
		mainWorkflowPanel = createWorkflowPanel();
		
		JerboaObjectExchange.mainWorkflowPanel = mainWorkflowPanel;
		JScrollPane procedureScrollPane = new JScrollPane(mainWorkflowPanel);
		procedureScrollPane.setBorder(BorderFactory.createTitledBorder("Workflow"));
		procedureScrollPane.setPreferredSize(new Dimension(800,400));
		
		//Create console:
		JTextArea consoleArea = new JTextArea();
		console = new Console();
		JerboaObjectExchange.console = console;
		console.setTextArea(consoleArea);
		JScrollPane consoleScrollPane = new JScrollPane(consoleArea);
		consoleScrollPane.setBorder(BorderFactory.createTitledBorder("Console"));
		consoleScrollPane.setPreferredSize(new Dimension(800,200));
		consoleScrollPane.setAutoscrolls(true);

		//Create folder selection area:
		JPanel folderPanel = createFolderPanel();

		//Create buttons panel:
		JPanel buttonsPanel = new JPanel();
		buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.X_AXIS));
		JLabel progressLabel = new JLabel();
		ProgressHandler.setLabel(progressLabel);
		buttonsPanel.add(progressLabel);
		buttonsPanel.add(Box.createHorizontalGlue());
		runButton = new JButton("Run");
		runButton.setToolTipText("Run the workflow");
		mainWorkflowPanel.setRunButton(runButton);
		mainWorkflowPanel.setFolderField(folderField);
		buttonsPanel.add(runButton);

		if (!noGUI(args))
		{
			System.setOut(new PrintStream(console));
			System.setErr(new PrintStream(console));
			f = createFrame();
			f.setJMenuBar(new JerboaMenuBar());
			loadIcons(f);
			f.add(new JSplitPane(JSplitPane.VERTICAL_SPLIT, procedureScrollPane, consoleScrollPane), BorderLayout.CENTER); 
			f.add(folderPanel, BorderLayout.NORTH);
			f.add(buttonsPanel, BorderLayout.SOUTH);
			f.pack();
			f.setVisible(true);
		}
		executeParameters(args);
	}

	private boolean noGUI(String[] args)
	{
		for (String arg : args)
			if (arg.equals("-nogui"))
				return true;
	
		return false;
	}
	
	private JPanel createFolderPanel() 
	{
		JPanel folderPanel = new JPanel();
		folderPanel.setLayout(new BoxLayout(folderPanel, BoxLayout.X_AXIS));
		folderPanel.setBorder(BorderFactory.createTitledBorder("Working folder"));
		folderField = new JTextField();
		folderField.getDocument().addDocumentListener(new DocumentListener(){
			public void changedUpdate(DocumentEvent e) {
				setFolder(folderField.getText());
			}
			public void insertUpdate(DocumentEvent e) {
				setFolder(folderField.getText());
			}
			public void removeUpdate(DocumentEvent e) {
				setFolder(folderField.getText());  
			}});

		folderField.setText((new File("").getAbsolutePath()));
		folderPanel.add(folderField);
		pickButton = new JButton("Pick folder");
		folderPanel.add(pickButton);
		pickButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				pickFile();
			}});
		return folderPanel;
	}
	private JFrame createFrame() {
		JFrame frame = new JFrame("Jerboa");
		JerboaObjectExchange.frame = frame;
		
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		frame.setLayout(new BorderLayout());
		return frame;
	}
	
	private void loadIcons(JFrame f) {
		List<Image> icons = new ArrayList<Image>();
		icons.add(loadIcon("Jerboa-icon16.gif",f));
		icons.add(loadIcon("Jerboa-icon32.gif",f));
		icons.add(loadIcon("Jerboa-icon48.gif",f));
		f.setIconImages(icons);		
	}
	
	private Image loadIcon(String name, JFrame f){
		Image icon = Toolkit.getDefaultToolkit().getImage(VisualJerboa.class.getResource(name));
		MediaTracker mediaTracker = new MediaTracker(f);
		mediaTracker.addImage(icon, 0);
		try {
			mediaTracker.waitForID(0);
			return icon;
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return null;
	}
	
	//private void executeParameters(String[] args) {
	public void executeParameters(String[] args) {
		String mode = null;
		boolean execute = false;
		for (String arg : args){
			if (arg.startsWith("-")){
				if (arg.toLowerCase().equals("-execute"))
					execute = true;
				else 
					mode = arg.toLowerCase();
			} else if (mode == null)
				System.out.println("Illegal parameter: " + arg);
			else {
				if (mode.equals("-folder"))
					folderField.setText(arg);
				else if (mode.equals("-settings")){
					List<String> settings = TextFileUtilities.loadFromFile(arg);
					JerboaObjectExchange.mainWorkflowPanel.setSettings(settings);
				} else if (mode.equals("-output"))
					console.setDebugFile(JerboaObjectExchange.workingFolder + arg);
				mode = null;
			}
		}
		if (execute){
			mainWorkflowPanel.setTerminateWhenDone(true);
			runButton.doClick();
		}   
	}

	private void pickFile(){
		JFileChooser fileChooser = new JFileChooser(new File(folderField.getText()));
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int returnVal = fileChooser.showDialog(f, "Select folder");
		if(returnVal == JFileChooser.APPROVE_OPTION) 
			folderField.setText(fileChooser.getSelectedFile().getAbsolutePath());
	}

	private static void setFolder(String folder){
		if (folder.endsWith("/"))
			JerboaObjectExchange.workingFolder = folder;
		else
			JerboaObjectExchange.workingFolder = folder+"/";
	}
}
