package org.erasmusmc.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ListMap<K, V> implements Map<K, V> {
  private List<V> list;
  private Map<K, V> map;
  private Comparator<V> comparator;
  public boolean issorted = true;
  
  public ListMap () {
    list = new ArrayList<V>();
    map = new HashMap<K, V>(); 
  }

  public void clear() {
    list.clear();
    map.clear();
    issorted = true;
  }

  public boolean containsKey(Object key) {
    return map.containsKey(key);
  }

  public boolean containsValue(Object value) {
    return map.containsValue(value);
  }

  public Set<Map.Entry<K, V>> entrySet() {
    return map.entrySet();
  }

  public V get(Object key) {
    return map.get(key);
  }

  public boolean isEmpty() {
    return list.isEmpty();
  }

  public Set<K> keySet() {
    return map.keySet();
  }

  public V put(K key, V value) {
    issorted = false;
    list.add(value);
    return map.put(key, value);
  }

  public V remove(Object key) {
    V object = map.remove(key);
    list.remove(object);
    return object;
  }

  public int size() {
    return map.size();
  }

  public Collection<V> values() {
    return map.values();
  }
  
  public List<V> asList() {
    return list;
  }
  
  public List<V> getSortedList() {
    if(!issorted) {
      Collections.sort(list, comparator);
      issorted = true;
    }
    return list;
  }

  public List<V> getSortedList(Comparator<?> comparator) {
    
    return getSortedList();
  }

	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
    issorted = false;
    map.putAll(m);
    list.addAll(m.values());
	}
}