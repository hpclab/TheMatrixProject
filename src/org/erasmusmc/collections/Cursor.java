package org.erasmusmc.collections;

public interface Cursor<D> {
  public boolean isValid();
  public void next();
  public void delete();
  public D get();
  public void put(D d);
}
