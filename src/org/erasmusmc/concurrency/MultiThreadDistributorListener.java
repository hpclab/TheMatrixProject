package org.erasmusmc.concurrency;

public interface MultiThreadDistributorListener<T> {
  public void process(T object);
}
