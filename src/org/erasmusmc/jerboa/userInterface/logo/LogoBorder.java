package org.erasmusmc.jerboa.userInterface.logo;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.ImageIcon;
import javax.swing.border.AbstractBorder;

public class LogoBorder extends AbstractBorder {
  private ImageIcon logo = new ImageIcon(LogoBorder.class.getResource("logo.gif"));
  private static final long serialVersionUID = 822080507918340840L;

  public Insets getBorderInsets(Component c){
  	
    Insets insets = new Insets(0,0,0,98);
    return insets;
  }
  
  public Insets getBorderInsets(Component c, Insets insets){
    insets.top = 0;
    insets.bottom = 0;
    insets.left = 0;
    insets.right = 83;
    return insets;
  }
  
  public void paintBorder(Component c, Graphics g, int x, int y, int width, int height){
    g.drawImage(logo.getImage(), width-logo.getIconWidth()-2,2, logo.getIconWidth(), 20, null);
  }
  
  public boolean isBorderOpaque(){
    return true;
  }
}
