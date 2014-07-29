package org.erasmusmc.jerboa.userInterface;

import java.util.Iterator;

/**
 * A virtual table is a Jerboa intermediate table that is consumed at the same time as it is created, therefore skipping the read and write steps. 
 * Aimed at reducing harddisk uses and running time.
 * @author schuemie
 *
 * @param <T>
 */
public interface VirtualTable<T> {
  Iterator<T> getIterator();
}
