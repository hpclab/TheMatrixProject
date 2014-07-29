package org.erasmusmc.jerboa.userInterface;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.PrintStream;
import java.security.Key;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.erasmusmc.jerboa.JerboaObjectExchange;
import org.erasmusmc.jerboa.ProgressHandler;
import org.erasmusmc.jerboa.modules.GiftWrapperModule;
import org.erasmusmc.utilities.StringUtilities;
import org.erasmusmc.utilities.TextFileUtilities;

/**
 * Application frame used in post-processing applications.
 * @author schuemie
 *
 */
public class PostProcessingFrame extends JFrame {

	private static final long serialVersionUID = -9101733658025970163L;
	private Console console;
	private JTextField sourceField = new JTextField();
	private JTextField keyField = new JTextField();
	private JTextField targetField = new JTextField();
	private JButton runButton = new JButton("Run");
	private PostProcessingScript script;
	public static boolean supportEncryption = true;
	public static boolean outputToFolder = true;

	public PostProcessingFrame(PostProcessingScript script, String title, String[] args){
		super(title);
		this.script = script;
		setLayout(new BorderLayout());
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});

		//Create file selection area:
		JPanel folderPanel = createFilesPanel();
		add(folderPanel, BorderLayout.NORTH);

		//Create console:
		JTextArea consoleArea = new JTextArea();
		console = new Console();
		JerboaObjectExchange.console = console;
		console.setTextArea(consoleArea);
		System.setOut(new PrintStream(console));
		System.setErr(new PrintStream(console));
		JScrollPane consoleScrollPane = new JScrollPane(consoleArea);
		consoleScrollPane.setBorder(BorderFactory.createTitledBorder("Console"));
		consoleScrollPane.setPreferredSize(new Dimension(800,200));
		consoleScrollPane.setAutoscrolls(true);
		add(consoleScrollPane, BorderLayout.CENTER);

		//Create buttons panel:
		JPanel buttonsPanel = new JPanel();
		buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.X_AXIS));
		JLabel progressLabel = new JLabel();
		ProgressHandler.setLabel(progressLabel);
		buttonsPanel.add(progressLabel);
		buttonsPanel.add(Box.createHorizontalGlue());
		buttonsPanel.add(runButton);
		runButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				RunThread thread = new RunThread();
				thread.start();				
			}
		});
		add(buttonsPanel, BorderLayout.SOUTH);

		pack();
		setVisible(true);
		executeParameters(args);
	}
	public class RunThread extends Thread {
		public void run() {
			try{
				runButton.setEnabled(false);
				sourceField.setEnabled(false);
				keyField.setEnabled(false);
				targetField.setEnabled(false);
				ProgressHandler.reportProgress();
				checkIfExists(sourceField);
				if (sourceField.getText().toLowerCase().endsWith(".enc"))
					checkIfExists(keyField);
				if (outputToFolder)
				  checkIfExists(targetField);

				String unencryptedFile;
				System.out.println(StringUtilities.now() + "\tStarting process");
				if (sourceField.getText().toLowerCase().endsWith(".enc")){
					System.out.println("Decrypting and decompressing");
					Key key = GiftWrapperModule.loadKey(keyField.getText());
					unencryptedFile = targetField.getText() + "/AllData.txt";
					GiftWrapperModule.decryptAndUnzip(sourceField.getText(), unencryptedFile, key);
					ProgressHandler.reportProgress();
				} else
					unencryptedFile = sourceField.getText();
				script.process(unencryptedFile, targetField.getText()+(outputToFolder?"/":""));
			} catch (Exception e){
				System.err.println(e.getMessage());
				JOptionPane.showMessageDialog(JerboaObjectExchange.frame, e.getLocalizedMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			} finally {
				System.out.println(StringUtilities.now() + "\tDone");
				ProgressHandler.reportDone();
				runButton.setEnabled(true);
				sourceField.setEnabled(true);
				keyField.setEnabled(true);
				targetField.setEnabled(true);
			}
		}
	}

	private void checkIfExists(JTextField field) {
		if (!(new File(field.getText()).exists()))
			throw new RuntimeException("File not found: " + field.getText());
	}

	private JPanel createFilesPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		String defaultFolder = (new File("").getAbsolutePath());
		
		if (supportEncryption){
			panel.add(createFilePanel("(Encrypted) input file", sourceField, defaultFolder + "/data.enc", JFileChooser.FILES_ONLY));
		  panel.add(createFilePanel("Private key", keyField, defaultFolder + "/JerboaPrivate.key", JFileChooser.FILES_ONLY));
		} else
			panel.add(createFilePanel("Input file", sourceField, defaultFolder + "/data.enc", JFileChooser.FILES_ONLY));
		if (outputToFolder)
		  panel.add(createFilePanel("Target folder", targetField, defaultFolder, JFileChooser.DIRECTORIES_ONLY));
		else
			panel.add(createFilePanel("Target file", targetField, defaultFolder, JFileChooser.FILES_ONLY));
		return panel;
	}

	private JPanel createFilePanel(String title, JTextField field, String defaultPath, int selectionMode) {
		JPanel folderPanel = new JPanel();
		folderPanel.setLayout(new BoxLayout(folderPanel, BoxLayout.X_AXIS));
		folderPanel.setBorder(BorderFactory.createTitledBorder(title));
		field.setText(defaultPath);
		folderPanel.add(field);
		String label;
		if (selectionMode == JFileChooser.FILES_ONLY)
			label = "Pick file";
		else
			label = "Pick folder";
		JButton pickButton = new JButton(label);
		folderPanel.add(pickButton);
		pickButton.addActionListener(new ButtonActionListener(field, selectionMode, this));
		return folderPanel;
	}

	private class ButtonActionListener implements ActionListener {

		private JTextField field;
		private int selectionMode;
		private JFrame frame;

		public ButtonActionListener(JTextField field,int selectionMode,JFrame frame){
			this.field = field;
			this.selectionMode = selectionMode;
			this.frame = frame;
		}
		@Override
		public void actionPerformed(ActionEvent e) {
			File current = new File(field.getText());
			if (!current.exists())
				current = current.getParentFile();
			JFileChooser fileChooser = new JFileChooser(current);
			fileChooser.setFileSelectionMode(selectionMode);
			String text;
			if (selectionMode == JFileChooser.FILES_ONLY)
				text = "Select file";
			else
				text = "Select folder";
				
			int returnVal = fileChooser.showDialog(frame, text);
			if(returnVal == JFileChooser.APPROVE_OPTION) 
				field.setText(fileChooser.getSelectedFile().getAbsolutePath());
		}
	}

	private void executeParameters(String[] args) {
		String mode = null;
		boolean execute = false;
		String folder = null;
		for (String arg : args){
			if (arg.startsWith("-")){
				if (arg.toLowerCase().equals("-execute"))
					execute = true;
				else if (arg.toLowerCase().equals("-debug"))
					console.setDebugFile(JerboaObjectExchange.workingFolder + "debug.txt");
				else 
					mode = arg.toLowerCase();
			} else if (mode == null)
				System.out.println("Illegal parameter: " + arg);
			else {
				if (mode.equals("-folder"))
					folder = arg;
				else if (mode.equals("-settings")){
					List<String> settings = TextFileUtilities.loadFromFile(arg);
					JerboaObjectExchange.mainWorkflowPanel.setSettings(settings);
				}
				mode = null;
			}
		}
		if (folder != null){
			if (!folder.endsWith("/") && !folder.endsWith("\\"))
				folder = folder + "/";
			sourceField.setText(folder + "data.enc");
			targetField.setText(folder);
			keyField.setText(folder + "JerboaPrivateSOS.key");
		}
		if (execute){
			runButton.doClick();
		}   
	}
}
