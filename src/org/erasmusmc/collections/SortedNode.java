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

