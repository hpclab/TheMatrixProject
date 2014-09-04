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
