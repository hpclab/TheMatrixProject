package org.erasmusmc.collections;

import java.io.Serializable;

public class TrieNode implements Serializable {
  private static final long serialVersionUID = 717802594709731944L;
  private char[] chars = new char[0];
  private Object[] objects = new Object[0];
  public Object value;
  
  public void insert(int index, char key, Object object) {
    insertChar(index, key);
    insertObject(index, object);
  }
  
  public void setObject(int index, Object object){
    objects[index] = object;
  }

  public int indexOf(char key) {
    int index = binarySearch(key, 0, chars.length);
    if (index < chars.length) {
      if (key == chars[index])
        return index;
    }
    return -1;
  }

  private int binarySearch(char key, int low, int high) {
    int middle;
    while (low < high) {
      middle = (low + high) / 2;
      if (key > chars[middle])
        low = middle + 1;
      else
        high = middle;
    }
    return low;
  }
 
  public int binarySearch(char key) {
    int low = 0;
    int high = chars.length;
    return binarySearch(key, low, high);
  }

  public int size() {
    return chars.length;
  }
  
  public char getKeyForIndex(int index) {
    return chars[index];
  }
  
  public Object getObjectForIndex(int index) {
    return objects[index];
  }
  
  private void insertChar(int index, char element) {
    char[] newChars;
    if (chars == null){
      newChars = new char[1];
    } else {
      newChars = new char[chars.length+1];
      System.arraycopy(chars, 0, newChars, 0, index);
      System.arraycopy(chars, index, newChars, index + 1, chars.length - index);
    }
    newChars[index] = element;
    chars = newChars;
  }
  
  private void insertObject(int index, Object element) {
    Object[] newObjects;
    if (objects == null){
      newObjects = new Object[1];
    } else {
      newObjects = new Object[objects.length+1];
      System.arraycopy(objects, 0, newObjects, 0, index);
      System.arraycopy(objects, index, newObjects, index + 1, objects.length - index);
    }
    newObjects[index] = element;
    objects = newObjects;
  }
  
}