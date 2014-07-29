package org.erasmusmc.jerboa.userInterface;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.erasmusmc.jerboa.JerboaObjectExchange;


public class AboutBox extends JDialog {

  private static final long serialVersionUID = -7272048400591388564L;

  public AboutBox(){
    setUndecorated(true);
    Container pane = getContentPane();
    pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
    pane.setBackground(Color.WHITE);
    
    ImageIcon logoTop = new ImageIcon(JerboaMenuBar.class.getResource("Jerboa-aboutTop.gif"));
    JLabel labelTop = new JLabel(logoTop);
    pane.add(labelTop);
    
    
    pane.add(new JLabel(" " + JerboaObjectExchange.version));
    pane.add(new JLabel(" Authors: M.J.Schuemie & B.M.Th.Mosseveld"));

    ImageIcon logoBottom = new ImageIcon(JerboaMenuBar.class.getResource("Jerboa-aboutBottom.gif"));
    JLabel labelBottom = new JLabel(logoBottom);
    pane.add(labelBottom);

    JPanel buttonPanel = new JPanel();
    buttonPanel.setBackground(Color.WHITE);
    
    JButton closeButton = new JButton("Close");
    closeButton.setAlignmentX(JButton.CENTER_ALIGNMENT);
    closeButton.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        closeWindow();
      }});
    buttonPanel.add(closeButton);
    pane.add(buttonPanel);
    pack();
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    setLocation(screenSize.width/2 - getWidth()/2,screenSize.height/2 - getHeight()/2);
  }
  
  private void closeWindow(){
    this.setVisible(false);
  }

}
