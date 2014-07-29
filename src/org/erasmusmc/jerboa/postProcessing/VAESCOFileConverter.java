package org.erasmusmc.jerboa.postProcessing;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.security.Key;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.erasmusmc.collections.CountingSet;
import org.erasmusmc.jerboa.modules.GiftWrapperModule;
import org.erasmusmc.utilities.DirectoryUtilities;
import org.erasmusmc.utilities.ReadTextFile;
import org.erasmusmc.utilities.WriteTextFile;

public class VAESCOFileConverter {

	private JTextField sourceField = new JTextField("data.enc");
	public static String outputTableFilename = "VAESCOtable.csv";
	public static String outputInfoFilename = "VAESCOinfo.csv";
	public static int eventCol = 7;
	public static int monthCol = 4;
	private JFrame f;
	
	public static void main(String[] args) {
		new VAESCOFileConverter();
	}
	
	public VAESCOFileConverter(){
		JFrame f = new JFrame("VAESCOE File Converter");
		f.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.add(createInputPanel());
		panel.add(createRunPanel());
		f.add(panel);
		f.pack();
		f.setVisible(true);
	}
	
	private Component createRunPanel() {
    JButton runButton = new JButton("Convert");
    runButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        convert();
      }
    });
    
		return runButton;
	}
	
	private void convert() {
		String sourceFilename = sourceField.getText();
		
		if (sourceFilename.endsWith(".enc"))
			sourceFilename = decrypt(sourceFilename);
		
		convertToTable(sourceFilename, outputTableFilename);
	}
	


	private String decrypt(String sourceFilename) {
		String tempFilename = "temp.txt";
		try {
			Key key = GiftWrapperModule.loadKey("JerboaPrivate.key");
			GiftWrapperModule.decryptAndUnzip(sourceFilename, tempFilename, key);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return tempFilename;
	}

	private void convertToTable(String sourceFile, String targetFile){	
		WriteTextFile out = new WriteTextFile(targetFile);
		boolean hasMonths = false;
		out.writeln("RunInYears,CensorPeriod,EventType,AgeRange,Gender,Year,Month,Days,Subjects,Events");
		CountingSet<String> eventCounts = new CountingSet<String>();
		String prefix = null;
		for (String line : new ReadTextFile(sourceFile)){
			if (line.startsWith("*")){
				if (line.startsWith("*** Start of file")){
					prefix = null;
					if (line.equals("*** Start of file Aggregate0yearsevents1.txt ***"))
						prefix = "0,28 days,";
					if (line.equals("*** Start of file Aggregate1yearevents1.txt ***"))
						prefix = "1,28 days,";
					if (line.equals("*** Start of file Aggregate3yearsevents1.txt ***"))
						prefix = "3,28 days,";
					if (line.equals("*** Start of file Aggregate0yearsevents2.txt ***"))
						prefix = "0,Infinite,";
					if (line.equals("*** Start of file Aggregate1yearevents2.txt ***"))
						prefix = "1,Infinite,";
					if (line.equals("*** Start of file Aggregate3yearsevents2.txt ***"))
						prefix = "3,Infinite,";
					
				}
			} else {
				if (line.startsWith("EventType,"))
					hasMonths = line.contains("Month");
				List<String> cols = Arrays.asList(line.split(","));
				if (!cols.get(0).equals("EventType") && !cols.get(0).equals("All") && !cols.get(0).startsWith("\"")){
					if (!hasMonths){
						List<String> newCols = new ArrayList<String>(cols.size()+1);
						newCols.addAll(cols.subList(0,monthCol));
						newCols.add("");
						newCols.addAll(cols.subList(monthCol, cols.size()));
						cols = newCols;
					}
					out.writeln(prefix + join(cols, ","));
					if (!cols.get(eventCol).equals("0"))
						eventCounts.add(cols.get(0), Integer.parseInt(cols.get(eventCol)));
				}
			}
		}
		out.close();
		eventCounts.printCounts();
	}
	
  public static String join(Collection<?> s, String delimiter) {
    StringBuffer buffer = new StringBuffer();
    Iterator<?> iter = s.iterator();
    if (iter.hasNext()) {
      buffer.append(iter.next().toString());
    }
    while (iter.hasNext()) {
      buffer.append(delimiter);
      buffer.append(iter.next().toString());
    }
    return buffer.toString();
  }
	
  protected JPanel createInputPanel() {
    JPanel panel = new JPanel();
    panel.setBorder(BorderFactory.createTitledBorder("Input file"));
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.add(sourceField);
    
    JButton pickButton = new JButton("Pick file");
    pickButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        pickInputFile();
      }
    });
    panel.add(pickButton);
    return panel;
  }
  
  private void pickInputFile() {
    JFileChooser fileChooser = new JFileChooser(new File("").getAbsolutePath());

    int returnVal = fileChooser.showOpenDialog(f);
    if (returnVal == JFileChooser.APPROVE_OPTION)
      sourceField.setText(DirectoryUtilities.getRelativePath(new File(""), fileChooser.getSelectedFile()));
  }
  

  


}
