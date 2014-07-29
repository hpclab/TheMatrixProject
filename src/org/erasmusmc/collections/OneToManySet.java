package org.erasmusmc.collections;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class OneToManySet<K,V> {
  private Map<K,Set<V>> map = new HashMap<K, Set<V>>();
  
  public boolean put(K key, V value){
    Set<V> set = map.get(key);
    if (set == null){
      set = new HashSet<V>();
      map.put(key, set);
    }
    return set.add(value);
  }
  
  public void set(K key, Set<V> set){
  	map.put(key, set);
  }
  
  public Set<V> get(K key){
    Set<V> set = map.get(key);
    if (set == null)
      return Collections.emptySet();
    else
      return set;   
  }
  
  public Set<K> keySet(){
    return map.keySet();
  }
  
  public Collection<Set<V>> values(){
    return map.values();
  }
  
  public Set<Map.Entry<K, Set<V>>> entrySet(){
  	return map.entrySet();
  }
  
  public int size(){
  	return map.size();
  }
  

}

