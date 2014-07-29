package org.erasmusmc.jerboa.dataClasses;

public class InputFileException extends RuntimeException {
  private static final long serialVersionUID = 273449712524597014L;
  private int line;
  private String filename;
  
  public InputFileException(int line, String filename, String message){
    super(message);
    this.line = line;
    this.filename = filename;
  }
  
  public int getLine(){
    return line;
  }
  
  public String getFilename(){
    return filename;
  }

}
