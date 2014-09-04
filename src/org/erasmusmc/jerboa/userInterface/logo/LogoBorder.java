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
