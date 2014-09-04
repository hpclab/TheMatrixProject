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
package org.erasmusmc.jerboa.dataClasses;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.erasmusmc.utilities.ReadTextFile;


public class InputFileExceptionViewer extends JDialog {
  
  public static int maxLines = 20;
  private static final long serialVersionUID = 486838477754437076L;
  private InputFileException exception;
  
  public InputFileExceptionViewer(InputFileException inputFileException){
    this.exception = inputFileException;
    setLayout(new BorderLayout());
    setTitle("Error viewer");
    add(createExceptionPanel(), BorderLayout.NORTH);
    add(new JScrollPane(createFileViewPanel()), BorderLayout.CENTER);
    add(createButtonPanel(), BorderLayout.SOUTH);
    pack();     
    setModal(true);
  }

  private Component createButtonPanel() {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
    panel.add(Box.createHorizontalGlue());
    JButton closeButton = new JButton("Close");
    closeButton.addActionListener(new ActionListener(){

      public void actionPerformed(ActionEvent e) {
        closeDialog();
      }});
    panel.add(closeButton);
    return panel;
  }
  
  private void closeDialog(){
    this.setVisible(false);
  }

  private Component createFileViewPanel() {
    JPanel panel = new JPanel();
    panel.setBorder(BorderFactory.createTitledBorder("Contents of file: " + exception.getFilename()));
    panel.setLayout(new GridBagLayout());
    int start = Math.max(1, exception.getLine() - maxLines);
    int end = exception.getLine();
    List<String> lines = getLines(start, end);
    GridBagConstraints c = new GridBagConstraints();
    c.anchor = GridBagConstraints.LINE_START;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.ipadx = 10;
    for (int i = start; i <= end; i++){
      c.gridx = 0;  
      c.weightx = 0;
      c.gridy = i-start;
      JLabel numberLabel = new JLabel(Integer.toString(i));
      numberLabel.setForeground(Color.GRAY);
      panel.add(numberLabel, c);
      
      c.gridx = 1;
      c.weightx = 1;
      JLabel textLabel = new JLabel(lines.get(i-start));
      if (i == end)
        textLabel.setForeground(Color.RED);
      panel.add(textLabel, c);
    }
    return panel;
  }

  private List<String> getLines(int start, int end) {
    List<String> result = new ArrayList<String>();
    int lineNr = 0;    
    for (String line : new ReadTextFile(exception.getFilename())){
      lineNr++;
      if (lineNr >= start && lineNr <= end)
        result.add(line);
      if (lineNr == end)
        break;
    }
    return result;
  }

  private JPanel createExceptionPanel() {
    JPanel panel = new JPanel();
    panel.setBorder(BorderFactory.createTitledBorder("Error message"));
    
    JLabel label = new JLabel(exception.getLocalizedMessage());
    panel.add(label);
    return panel;
  }
}
