package org.erasmusmc.collections;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class SortedList<E> implements Iterable<E>{
  protected List<E> elements = new ArrayList<E>();
  protected Comparator<E> comparator;
  
  public SortedList(Comparator<E> comparator) {
    this.comparator = comparator;
  }
  
  protected int binarySearch(E element) {
    int low = 0, middle, high = elements.size();
    
    while (low < high) {
      middle = low + (high - low) / 2;
      
      if (comparator.compare(element, elements.get(middle)) > 0)
        low = middle + 1;
      else
        high = middle;
    }
    
    return low;
  }
  
  public boolean add(E element) {
    int index = binarySearch(element);
    elements.add(index, element);
    return true;
  }
  
  public E get(int index) {
    return elements.get(index);
  }
  
  public int remove(E element) {
    int index = binarySearch(element);
    
    if (index < elements.size() && elements.get(index) == element) {
      elements.remove(index);
      return index;
    }
    else
      return -1;
  }
  
  public void clear() {
    elements.clear();
  }
  
  
  public int indexOf(E element) {
    int index = binarySearch(element);
    
    if (index < elements.size() && elements.get(index) == element)
      return index;
    else
      return -1;
  }
  
  public int size() {
    return elements.size();
  }

  public Iterator<E> iterator() {
    
    return elements.listIterator();
  }


}
