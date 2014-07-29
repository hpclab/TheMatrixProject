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
