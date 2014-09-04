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

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import org.erasmusmc.jerboa.JerboaObjectExchange;
import org.erasmusmc.jerboa.JerboaObjectExchange.BooleanNoticeListener;
import org.erasmusmc.jerboa.JerboaObjectExchange.BooleanNotifier;
import org.erasmusmc.jerboa.manual.HelpWindow;
import org.erasmusmc.jerboa.userInterface.logo.LogoBorder;
import org.erasmusmc.utilities.TextFileUtilities;

public class JerboaMenuBar extends JMenuBar implements BooleanNoticeListener {
  private JMenu fileMenu;
  private JMenu analysisMenu;
  private JMenu toolsMenu;
  private JMenu helpMenu;
  private JMenuItem exitItem;
  private JMenuItem helpItem; 
  private JMenuItem aboutItem; 
  private JMenuItem incidenceRateItem;
  private JMenuItem relativeRiskItem;
  private JMenuItem testInputFilesItem;
  private JMenuItem encryptFileItem;
  private JMenuItem loadSettingsItem;
  
  public Dimension getPreferredSize(){
    return new Dimension(100, 25);
  }
  public JerboaMenuBar(){
    setBorder(new LogoBorder());
    
    fileMenu = new JMenu("File");
    loadSettingsItem = new JMenuItem("Load workflow parameters");
    loadSettingsItem.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        loadSettings();
      }});   
    fileMenu.add(loadSettingsItem);
    
    exitItem = new JMenuItem("Exit");
    exitItem.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        System.exit(0);
      }});   
    fileMenu.add(exitItem);
    
    add(fileMenu);
    
    analysisMenu = new JMenu("Analysis");
    incidenceRateItem = new JMenuItem("Incidence rate");
    incidenceRateItem.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        AnalysisDialog dialog = new AnalysisDialog(new WorkflowPanel("IncidenceRateWorkflow.txt"), "Incidence Rate");
        dialog.setVisible(true);
      }});
    analysisMenu.add(incidenceRateItem);
    
    relativeRiskItem = new JMenuItem("Relative risks");
    relativeRiskItem.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        AnalysisDialog dialog = new AnalysisDialog(new WorkflowPanel("RelativeRiskWorkflow.txt"), "Relative Risks");
        dialog.setVisible(true);
      }});
    analysisMenu.add(relativeRiskItem);
    add(analysisMenu);
    
    toolsMenu = new JMenu("Tools");
    testInputFilesItem = new JMenuItem("Test input files");
    testInputFilesItem.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        AnalysisDialog dialog = new AnalysisDialog(new WorkflowPanel("TestInputFilesWorkflow.txt"), "Test input files");
        dialog.setVisible(true);
      }});
    toolsMenu.add(testInputFilesItem);
    
    encryptFileItem = new JMenuItem("Encrypt file");
    encryptFileItem.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        AnalysisDialog dialog = new AnalysisDialog(new WorkflowPanel("GiftWrapperWorkflow.txt"), "Encrypt file");
        dialog.setVisible(true);
      }});
    toolsMenu.add(encryptFileItem);
    add(toolsMenu);
    
    helpMenu = new JMenu("Help");
    
    helpItem= new JMenuItem("Manual");
    helpItem.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        new HelpWindow();
      }});
    helpMenu.add(helpItem);
    
    aboutItem = new JMenuItem("About");
    aboutItem.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        AboutBox aboutBox = new AboutBox();
        aboutBox.setModal(true);
        aboutBox.setVisible(true);
      }});
    helpMenu.add(aboutItem);
    add(helpMenu);
    
    JerboaObjectExchange.busyNotifier.addListener(this);
  }
  
  private void loadSettings(){
    final JFileChooser fileChooser = new JFileChooser(new File(JerboaObjectExchange.workingFolder));
    int returnVal = fileChooser.showOpenDialog(this);
    if(returnVal == JFileChooser.APPROVE_OPTION){
    	JerboaObjectExchange.mainWorkflowPanel.setNotice("Loading...");
    	Thread thread = new Thread(){
    		public void run(){
          List<String> settings = TextFileUtilities.loadFromFile(fileChooser.getSelectedFile().getAbsolutePath());
          JerboaObjectExchange.mainWorkflowPanel.setSettings(settings);
    		}
    	};
    	thread.start();
    }
  }

  
  private static final long serialVersionUID = -7365569692517996040L;
  
	public void noticeChange(BooleanNotifier sender, boolean value) {
    incidenceRateItem.setEnabled(!value); 
    relativeRiskItem.setEnabled(!value);
    testInputFilesItem.setEnabled(!value);
    loadSettingsItem.setEnabled(!value);
  }
}
