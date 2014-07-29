package org.erasmusmc.jerboa.userInterface;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JPanel;

/** 
 * Visual component for connecting two blocks.
 * @author schuemie
 *
 */
public class ConnectionObject extends JPanel{

  private static final long serialVersionUID = -3914305239122744060L;
  private int type;
  public static int IN = 1;
  public static int OUT = 2;
  public static int LINE = 3;
  public static int CONNECT = 4;
  public ConnectionObject(int type){
    this.type = type;
  }
  public Dimension getPreferredSize(){
    return new Dimension(40,20);
  }
  
  
  public void paintComponent(Graphics g) {
    super.paintComponent(g);
    int width = this.getWidth();
    int height = this.getHeight();
    if (type == IN){
      g.setColor(Color.WHITE);   
      g.fillRect(width/2-10, 0, 20, 10);
      g.fillRect(width/2-20, 10, 40, 20);     
      g.setColor(this.getBackground());
      g.fillOval(width/2-30, 0, 20, 20);
      g.fillOval(width/2+10, 0, 20, 20);
    } else if (type == OUT){
      g.setColor(Color.WHITE);   
      g.fillRect(width/2-20, 0, 40, 10);    
      g.fillRect(width/2-10, 10, 20, height);
      g.setColor(this.getBackground());
      g.fillOval(width/2-30, height-20, 20, 20);
      g.fillOval(width/2+10, height-20, 20, 20);
    } 
    else if (type == CONNECT){
      g.setColor(Color.WHITE);   
      g.fillRect(width/2-20, 0, 40, 20);     
      g.setColor(this.getBackground());
      g.fillOval(width/2-30, height-20, 20, 20);
      g.fillOval(width/2+10, height-20, 20, 20);
    }  if (type == LINE){
      g.setColor(Color.WHITE);   
      g.fillRect(width/2-10, 0, 20, height);     
    }
  }
}

