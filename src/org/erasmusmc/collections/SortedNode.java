package org.erasmusmc.collections;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SortedNode<K, V> extends Node<K> {
  private List<K> sortedChildren;
  private boolean needsToSort = false;
  private Comparator<K> comparator = null;

  public SortedNode(K id, Comparator<K> comparator) {
    super(id);
    this.comparator = comparator;
  }

  public List<K> asList() {
    if(needsToSort && comparator != null) Collections.sort(sortedChildren, comparator);
    return sortedChildren;
  }
  
  public Node<K> add(K newChild) {
    needsToSort = true;
    sortedChildren.add(newChild);
    return super.add(newChild);
  }
  
  
  public Node<K> add(Node<K> newChild) {
    needsToSort = true;
    sortedChildren.add(newChild.id);
    return super.add(newChild);
  }
}

