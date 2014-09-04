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

import java.io.IOException;
import java.io.OutputStream;

import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;

import org.erasmusmc.utilities.WriteTextFile;

/**
 * Captures output to the standard output (and error), and displays it in the app console.
 * @author schuemie
 *
 */
public class Console extends OutputStream{

	private StringBuffer buffer = new StringBuffer();
	private WriteTextFile debug = null;
	private JTextArea textArea;

	public void println(String string){
		textArea.append(string+"\n");
		textArea.repaint();  
		System.out.println(string);
	}

	public void setTextArea(JTextArea textArea){
		this.textArea = textArea; 
	}

	public void setDebugFile(String filename){
		debug = new WriteTextFile(filename);  	
	}

	public String getText(){
		try {
			return textArea.getDocument().getText(0, textArea.getDocument().getLength());
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void write(int b) throws IOException {
		buffer.append((char)b);
		if ((char)b == '\n'){
			if (textArea != null){
				textArea.append(buffer.toString());
				textArea.setCaretPosition(textArea.getDocument().getLength() );
			}
			if (debug != null){
				debug.writeln(buffer.toString());
				debug.flush();
			}
			buffer = new StringBuffer();
		}
	}

}
