package org.erasmusmc.collections;


public class BitStack {
  private IntList data;
  private int pointer = maxdatabits-1;
  private static int maxdatabits = 32; // an int has 32 bits
  private int currentInt = 0;
  
  public BitStack(){
    data = new IntList();
  }
  
  public BitStack(int initialCapacity){
    data = new IntList((initialCapacity / maxdatabits)+1);
  }
  
  public void push(int number, int bits){
    for (int i = bits-1; i >= 0; i--){
      int nextbit = (number >>> i)& 1;
      push(nextbit);
    }
  }

  public void push(int bit){
    pointer--;
    if (pointer < 0){
      pointer = maxdatabits-1;
      data.add(currentInt);
      currentInt = 0;
    }
    currentInt |= bit << pointer;
  }
  
  public int pop(int bits){
    int result = 0;
    for (int i = 0; i < bits; i++){
      result |= pop() << i;
    }
    return result;
  }
  
  public int pop(){
    int result = (currentInt >>> pointer) & 1;
    pointer++;
    if (pointer >= maxdatabits){
      pointer = 0;
      currentInt = data.remove(data.size()-1);
    }
    return result;
  }
  
  public void trimToSize(){
    data.trimToSize();
  }

}
