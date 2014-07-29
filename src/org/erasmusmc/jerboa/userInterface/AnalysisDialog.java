package org.erasmusmc.jerboa.userInterface;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * Dialog box for displaying and running an analysis workflow.
 * @author schuemie
 *
 */
public class AnalysisDialog extends JDialog {
  private static final long serialVersionUID = 8002365716956192100L;
  private JButton runButton;
  private JButton cancelButton;
  
  public void run(){
    runButton.doClick();
  }
  
  public AnalysisDialog(WorkflowPanel workflowPanel, String title){
    setLayout(new BorderLayout());
    setTitle(title);
    
    JScrollPane procedureScrollPane = new JScrollPane(workflowPanel);
    procedureScrollPane.setBorder(BorderFactory.createTitledBorder("Workflow"));
    add(procedureScrollPane, BorderLayout.CENTER);
    
    JPanel buttonsPanel = new JPanel();
    buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.X_AXIS));
    buttonsPanel.add(Box.createHorizontalGlue());
    runButton = new JButton("Run");
    runButton.setToolTipText("Run the workflow");
    workflowPanel.setRunButton(runButton);
    buttonsPanel.add(runButton);
    cancelButton = new JButton("Close");
    cancelButton.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        setVisible(false);
      }});
    buttonsPanel.add(cancelButton);
    add(buttonsPanel, BorderLayout.SOUTH);
    pack();     
    setModal(true);
    
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    setLocation(screenSize.width/2 - getWidth()/2,screenSize.height/2 - getHeight()/2);
  }

}
