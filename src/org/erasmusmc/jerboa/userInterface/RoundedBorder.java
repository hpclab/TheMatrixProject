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

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.border.AbstractBorder;

public class RoundedBorder extends AbstractBorder {

  private static final long serialVersionUID = 822080507918340840L;

  public Insets getBorderInsets(Component c){
    Insets insets = new Insets(10,20,10,20);
    return insets;
  }
  
  public Insets getBorderInsets(Component c, Insets insets){
    insets.top = 10;
    insets.bottom = 10;
    insets.left = 20;
    insets.right = 20;
    return insets;
  }
  
  public void paintBorder(Component c, Graphics g, int x, int y, int width, int height){
    g.setColor(Color.WHITE);
    g.fillRoundRect(10+x, y, width-20, height, 20, 20);    
  }
  
  public boolean isBorderOpaque(){
    return true;
  }
}