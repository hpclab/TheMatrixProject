package org.erasmusmc.jerboa.manual;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.Doclet;
import com.sun.javadoc.FieldDoc;
import com.sun.javadoc.RootDoc;


/**
 * Doclet for generating documentation about the Jerboa modules. <br>
 * Usage: In Eclipse, choose Project -> Generate Javadoc...<br>
 * Doclet name: org.erasmusmc.jerboa.manual.ManualDoclet<br>
 * Doclet class path: /Users/schuemie/workspace/Jerboa/bin<br>
 * <br>
 * Compile before use
 * 
 * @author schuemie
 *
 */
public class ManualDoclet extends Doclet{

	public static String outputFilename = "/home/schuemie/Software/Jerboa/modules.html";
	
	private static BufferedWriter bufferedWriter;
	
	public static void main(String[] args){
		System.out.println("Compiled");
	}

	public static boolean start(RootDoc root) {
		startFile(outputFilename);
		writeln("<html>");
		writeln("  <body>");
		ClassDoc[] classes = root.classes();
		Arrays.sort(classes, new ClassDocComparator()); //Sort alphabetically
		for (int i = 0; i < classes.length; ++i) {
			addClass(classes[i]);
			System.out.println(classes[i]);
		}
		writeln("  </body>");
		writeln("</html>");
		closeFile();
		return true;
	}

	private static void addClass(ClassDoc classDoc) {
		if (classDoc.typeName().equals("JerboaModule"))
			return;
		writeln("<h2> " + classDoc.typeName() + "</h2>");
		writeln("<p>" + strip(classDoc.getRawCommentText()) +"</p>");

		List<FieldDoc> inputs = new ArrayList<FieldDoc>();
		List<FieldDoc> parameters = new ArrayList<FieldDoc>();
		
		for (FieldDoc fieldDoc : classDoc.fields())
		  if (fieldDoc.type().typeName().equals("JerboaModule"))
		  	inputs.add(fieldDoc);
		  else
		  	parameters.add(fieldDoc);
		
		if (inputs.size() != 0){
			writeln("<h3>Inputs</h3>");
			writeln("<ul>");
			for (FieldDoc fieldDoc : inputs)
			  writeln("<li><strong>" + fieldDoc.name() +"</strong></li>");
			writeln("</ul>");
		}
		writeln("<h1></h1>"); //Needed to fool text processors when copy/pasting
		if (parameters.size() != 0){
			writeln("<h3>Parameters</h3>");
			writeln("<ul>");
			for (FieldDoc fieldDoc : parameters){
			  writeln("<li><strong>" + fieldDoc.name() +"</strong> (" + fieldDoc.type().typeName() + ")</li>");
			  writeln(strip(fieldDoc.getRawCommentText()));
			  writeln("<br/>&nbsp;<br/>");
			}
	
			writeln("</ul>");
		}
		writeln("<h1></h1>"); //Needed to fool text processors when copy/pasting
	}
	
	
  private static String strip(String text) {
		int i = text.indexOf("@");
		if (i == -1)
			return text;
		else
			return text.substring(0,i);
	}

	private static void writeln(String string){
    try {
      bufferedWriter.write(string);
      bufferedWriter.newLine();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
	
	private static void startFile(String filename){
    FileOutputStream PSFFile;
    try {
      PSFFile = new FileOutputStream(filename);
      bufferedWriter = new BufferedWriter( new OutputStreamWriter(PSFFile, "UTF-8"),10000);      
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (UnsupportedEncodingException e) {
      System.err.println("Computer does not support UTF-8 encoding");
      e.printStackTrace();
    }
	}
	
  private static void closeFile() {
    try {
      bufferedWriter.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  
  private static class ClassDocComparator implements Comparator<ClassDoc>{

		@Override
		public int compare(ClassDoc o1, ClassDoc o2) {
		  return o1.typeName().compareTo(o2.typeName());
		}
  	
  }
}
