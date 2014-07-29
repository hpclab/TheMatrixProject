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
