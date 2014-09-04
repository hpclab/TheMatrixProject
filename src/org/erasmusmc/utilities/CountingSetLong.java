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
package org.erasmusmc.utilities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public class CountingSetLong <T> implements Set<T>{
  
  
  public CountingSetLong(){
    key2count = new HashMap<T,Count>();
  }
  
  public CountingSetLong(int capacity){
    key2count = new HashMap<T,Count>(capacity);
  }
  
  public CountingSetLong(CountingSetLong<T> set){
    key2count = new HashMap<T,Count>(set.key2count);
  }
  
  public long getCount(T key){
    Count count = key2count.get(key);
    if (count == null) return 0; else return count.count;
  }
  
  public int size() {
    return key2count.size();
  }
  public boolean isEmpty() {
    return key2count.isEmpty();
  }
  public boolean contains(Object arg0) {
    return key2count.containsKey(arg0);
  }
  public Iterator<T> iterator() {
    return key2count.keySet().iterator();
  }
  public Object[] toArray() {
    return key2count.keySet().toArray();
  }
  @SuppressWarnings("unchecked")
  public Object[] toArray(Object[] arg0) {
    return key2count.keySet().toArray(arg0);
  }
  public boolean add(T arg0) {
    Count count = key2count.get(arg0);
    if (count == null) {
      count = new Count();
      key2count.put(arg0, count);
      return true;
    } else {
      count.count++;
      return false;
    }
  }
  
  public boolean add(T arg0, long inc) {
    Count count = key2count.get(arg0);
    if (count == null) {
      count = new Count();
      count.count = inc;
      key2count.put(arg0, count);
      return true;
    } else {
      count.count+= inc;
      return false;
    }
  }
  
  public boolean remove(Object arg0) {
    
    return (key2count.remove(arg0) != null);
  }
  public boolean containsAll(Collection<?> arg0) {
    return key2count.keySet().containsAll(arg0);
  }

  public boolean addAll(Collection<? extends T> arg0) {
    boolean changed = false;
    for (T object : arg0){
      if (add(object)) changed = true;
    }
    return changed;
  }
  public boolean retainAll(Collection<?> arg0) {
    return key2count.keySet().retainAll(arg0);
  }
  public boolean removeAll(Collection<?> arg0) {
    return key2count.keySet().removeAll(arg0);
  }
  public void clear() {
    key2count.clear();
  }
  
  public Map<T, Count> key2count;
  
  public static class Count {
    public long count = 1;
  }  
  
  public void printCounts(){
    List<Map.Entry<T, Count>> result = new ArrayList<Map.Entry<T,Count>>(key2count.entrySet());
    Collections.sort(result, new Comparator<Map.Entry<T, Count>>(){
      public int compare(Entry<T, Count> o1, Entry<T, Count> o2) {
        if (o1.getValue().count > o2.getValue().count)
          return -1;
        if (o1.getValue().count < o2.getValue().count)
          return 1;
        return 0;
      }});
    for (Map.Entry<T, Count> entry : result)
      System.out.println(entry.getKey() + "\t" + entry.getValue().count);
  }
}
