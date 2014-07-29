package org.erasmusmc.jerboa.postProcessing;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.erasmusmc.collections.OneToManySet;
import org.erasmusmc.utilities.ReadCSVFile;
import org.erasmusmc.utilities.WriteCSVFile;

/**
 * Use this tool to convert the normal caseset output into a table with one column per ATC
 * @author schuemie
 *
 */
public class CaseSetExpander {
	
	private static JTextField field1 = new JTextField("Caseset.txt");
	private static JTextField field2 = new JTextField("CasesetExpanded.txt");
	private static JTextArea area1 = new JTextArea("");
	private static JButton button = new JButton("Run");
	public List<String> prefixes; 
	public boolean keepOnly7DigitATCs = true;

	public static void main(String[] args) {
		if (args.length == 2){
			CaseSetExpander caseSetExpander = new CaseSetExpander();
			caseSetExpander.process(args[0], args[1]);
			return;
		}
			
		JFrame f = new JFrame("Caseset expander");
		f.setLayout(new BoxLayout(f.getContentPane(), BoxLayout.PAGE_AXIS));
		f.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		
		JPanel panel1 = new JPanel();
		panel1.setBorder(BorderFactory.createTitledBorder("Input file"));
		panel1.add(field1);
		f.add(panel1);
		
		JPanel panel2 = new JPanel();
		panel2.setBorder(BorderFactory.createTitledBorder("Output file"));
		panel2.add(field2);
		f.add(panel2);

		JPanel panel3 = new JPanel();
		panel3.setBorder(BorderFactory.createTitledBorder("Prefixes"));
		area1.setPreferredSize(new Dimension(100,100));
		panel3.add(area1);
		f.add(panel3);
		
		button.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent arg0) {
				button.setEnabled(false);
				String folder = "";
				CaseSetExpander caseSetExpander =  new CaseSetExpander();
				caseSetExpander.prefixes = new ArrayList<String>();
				for (String line : area1.getText().split("\n")){
					String prefix = line.trim();
					if (prefix.length() != 0)
					  caseSetExpander.prefixes.add(prefix);
				}
				if (caseSetExpander.prefixes.size() == 0)
					caseSetExpander.prefixes = null;
				
				caseSetExpander.process(folder+field1.getText(), folder + field2.getText());
				button.setEnabled(true);
			}});
		f.add(button);
		
		f.pack();
		f.setVisible(true);
		if (args.length >= 2){
			field1.setText(args[0]);
			field2.setText(args[1]);
			if (args.length == 3){
				area1.setText(args[2]);
			}
			button.doClick();
		}
			
	}

	private void process(String source, String target) {
		//Find set of unique ATC codes
		System.out.println("Finding unique ATC codes");
		Iterator<List<String>> iterator = new ReadCSVFile(source).iterator();
		Map<Integer, String> expColumns = findExpColumns(iterator.next());
		OneToManySet<Integer, String> col2atcs = new OneToManySet<Integer, String>();
		while (iterator.hasNext()){
			List<String> cells = iterator.next();
			for (Integer col : expColumns.keySet()){
				String cell = cells.get(col);
				if (cell.length() != 0){
				  for (String atcCombi : cell.split("\\+")){
				  	String atc = atcCombi.split(":")[0];
				  	col2atcs.put(col, atc);
				  }
				}
			}
		}
		
		//Sort ATC codes
		System.out.println("Sorting and selecting ATC codes");
		Map<Integer, Map<String, Integer>> col2atc2col = new HashMap<Integer, Map<String,Integer>>();
		for (Integer col : col2atcs.keySet()){
			Set<String> atcs = col2atcs.get(col);
			List<String> sortedATCs = new ArrayList<String>(atcs);
			if (prefixes != null)
				filter(sortedATCs);
			if (keepOnly7DigitATCs)
				removeNon7DigitATCs(sortedATCs);
			Collections.sort(sortedATCs);
			System.out.println("Identified " + sortedATCs.size() + " unique ATC codes.");
			Map<String, Integer> atc2col = new HashMap<String, Integer>();
			for (int i = 0; i < sortedATCs.size(); i++){
				atc2col.put(sortedATCs.get(i), i);
			}
			col2atc2col.put(col, atc2col);
		}
		
		//Expand table
		WriteCSVFile out = new WriteCSVFile(target);
		
		System.out.println("Generating headers");
		//Generate headers:
		Map<Integer, Integer> col2col = new HashMap<Integer, Integer>();
		iterator = new ReadCSVFile(source).iterator();
		List<String> headers = iterator.next();
		List<String> newHeaders = new ArrayList<String>();
		for (int i = 0; i < headers.size(); i++){
			col2col.put(i, newHeaders.size());
			Map<String,Integer> atc2col = col2atc2col.get(i);
			if (atc2col == null){
				newHeaders.add(headers.get(i));
			} else {
				List<String> tempHeaders = new ArrayList<String>();
				for (int j = 0; j < atc2col.size(); j++)
					tempHeaders.add(null);
				for (Map.Entry<String, Integer> entry : atc2col.entrySet())
					tempHeaders.set(entry.getValue(), headers.get(i)+ ":" + entry.getKey());
				newHeaders.addAll(tempHeaders);
			}
		}
		out.write(newHeaders);
		int columncount = newHeaders.size();
		
		System.out.println("Expanding");
		//Go through rest of table
		while (iterator.hasNext()){
			List<String> cells = iterator.next();
			List<String> newCells = new ArrayList<String>(columncount);
			for (int i = 0; i < columncount; i++)
				newCells.add(null);
			
			for (int col = 0; col < cells.size(); col++){
				int newCol = col2col.get(col);
				String cell = cells.get(col);
				
				Map<String,Integer> atc2col = col2atc2col.get(col);
				if (atc2col == null){
					newCells.set(newCol, cell);
				} else {
					for (int i = 0; i < atc2col.size(); i++)
						newCells.set(newCol + i, "");
					if (cell.length() != 0)
						for (String atcCombi : cell.split("\\+")){
							String[] parts = atcCombi.split(":");
							String atc = parts[0];
							String value;
							if (parts.length == 2)
								value = parts[1];
							else
								value = "1";
							Integer relativeCol = atc2col.get(atc);
							if (relativeCol != null)
							  newCells.set(newCol + relativeCol, value);
						}		
				}
			}
			out.write(newCells);
		}
		
		out.close();
	}

	private void removeNon7DigitATCs(List<String> sortedATCs) {
		Iterator<String> iterator = sortedATCs.iterator();
		while (iterator.hasNext()){
			if (!isLegalATC(iterator.next()))
					iterator.remove();
		}
		
	}
	
	private static boolean isLegalATC(String atc) {
		if (atc.length() == 7)
		  if (Character.isLetter(atc.charAt(0)))
		  	if (Character.isDigit(atc.charAt(1)))
		  		if (Character.isDigit(atc.charAt(2)))
		  			if (Character.isLetter(atc.charAt(3)))
		  				if (Character.isLetter(atc.charAt(4)))
		  					if (Character.isDigit(atc.charAt(5)))
		  						if (Character.isDigit(atc.charAt(6)))
		  							return true;
		return false;
	}

	private void filter(List<String> sortedATCs) {
		Iterator<String> iterator = sortedATCs.iterator();
		while (iterator.hasNext()){
			String atc = iterator.next();
			boolean match = false;
			for (String prefix : prefixes)
				if (atc.startsWith(prefix)){
					match = true;
					break;
				}
			if (!match)
				iterator.remove();
		}
	}

	private Map<Integer, String> findExpColumns(List<String> headers) {
		Map<Integer, String> columns = new HashMap<Integer, String>();
		for (int i = 0; i < headers.size(); i++)
			if (headers.get(i).endsWith("_DaysOfUse") || headers.get(i).endsWith("_DaysSinceUse"))
				columns.put(i, headers.get(i));
			
		if (columns.size() == 0) //guess there are no daysofuse and dayssinceuse data. Look for current instead
			for (int i = 0; i < headers.size(); i++)
				if (headers.get(i).toLowerCase().equals("current"))
					columns.put(i, headers.get(i));
				
		return columns;
	}

}
