package org.erasmusmc.collections;

import java.util.Comparator;

public class SortedTree<K> extends Tree<K> {
  Comparator<K> comparator;
  public SortedTree(K id, Comparator<K> comparator) {
    super(id);
    this.comparator = comparator;
  }
  

}
