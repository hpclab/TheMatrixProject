package org.erasmusmc.collections;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
public class Tree<K> extends Node<K> {
  private Map<K, Set<K>> parentMap = new HashMap<K, Set<K>>();
  private boolean redundancyremoved = true;

  public Tree(K root) {
    super(root);
  }
  
 
  public void addParentChildRelation(K parent, K child) {
    redundancyremoved = false;
    Node<K> parentNode = this.children.get(parent);
    Node<K> childNode = this.children.get(child);
    
    
    if(parentNode == null) 
      parentNode = this.add(parent);

    if(childNode == null) 
      childNode = this.add(child);
    
    parentNode.add(childNode);
    
  }
  
  public Node<K> getRootNode() {
    Set<K> children = new HashSet<K>();
    for (Node<K> node : this.children.values()){
      for (Node<K> child : node.children.values()){
        children.add(child.id);
      }
    }
    for (K child : children)
      this.removeChild(child);
    
    if(!redundancyremoved) this.removeReduncancy();
    return this;
  }
  
  public Set<K> getParents(K child) {
    if(!redundancyremoved) removeReduncancy();
    return parentMap.get(child);
  }
  
  public void removeReduncancy() {
    super.removeReduncancy();
    this.redundancyremoved = true;
    buildParentsMap(this);
  }

  private void buildParentsMap(Node<K> parent) {
    Map<K, Node<K>> childrenMap = parent.getChildrenTree();
    for(Node<K> child: childrenMap.values()) {
      Set<K> parentList = parentMap.get(child.id);
      if(parentList == null) {
        parentList = new HashSet<K>();
        parentMap.put(child.id, parentList);
      }
      parentList.add(parent.id);
      buildParentsMap(child);
    }
  }
}
