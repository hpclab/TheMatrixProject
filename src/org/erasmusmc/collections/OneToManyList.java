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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class OneToManyList<K,V> {
  private Map<K,List<V>> map = new HashMap<K, List<V>>();
  
  public void put(K key, V value){
    List<V> list = map.get(key);
    if (list == null){
      list = new ArrayList<V>();
      map.put(key, list);
    }
    list.add(value);
  }
  
  public List<V> get(K key){
    List<V> list = map.get(key);
    if (list == null)
      return Collections.emptyList();
    else
      return list;   
  }
  
  public Set<K> keySet(){
    return map.keySet();
  }
  
  public Collection<List<V>> values(){
    return map.values();
  }
  
  public List<V> remove(K key){
  	return map.remove(key);
  }

}
