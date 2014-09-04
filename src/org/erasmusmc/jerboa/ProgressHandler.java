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
package org.erasmusmc.jerboa;

import javax.swing.JLabel;

/**
 * Static class for routing progress messages to a label.
 * @author schuemie
 *
 */
public class ProgressHandler {
  private static JLabel label;
  private static long lastUpdate;
  private static byte dots = 0;
  private static byte maxDots = 5;
  
  public static void setLabel(JLabel label){
    ProgressHandler.label = label;
    reportDone();
  }
  
  public static void reportProgress(){
    if (System.currentTimeMillis() - lastUpdate > 1000) {
      lastUpdate = System.currentTimeMillis(); 
      if (label != null){
        dots++;
        if (dots > maxDots)
          dots = 1;
       StringBuilder sb = new StringBuilder();
       sb.append("Working");
       for (byte b = 0; b < dots; b++)
         sb.append(".");
       label.setText(sb.toString());
      }
    }
  }
  
  public static void reportDone(){
    lastUpdate = 0;
    if (label != null)
      label.setText("Idle");
  }
  
}
