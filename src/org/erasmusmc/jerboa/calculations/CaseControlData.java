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
package org.erasmusmc.jerboa.calculations;

public class CaseControlData {
  public int caseAndExposed = 0;
  public int caseNotExposed = 0;
  public int controlAndExposed = 0;
  public int controlNotExposed = 0;
  public double total(){
    return caseAndExposed + caseNotExposed + controlAndExposed + controlNotExposed;
  }
  public double cases(){
    return caseAndExposed + caseNotExposed;
  }
  
  public double controls(){
    return controlAndExposed + controlNotExposed;
  }
  
  public double exposed(){
    return caseAndExposed + controlAndExposed;
  }
  
  public double notExposed(){
    return caseNotExposed + controlNotExposed;
  }
  
  public void add(CaseControlData other){
    caseAndExposed += other.caseAndExposed;
    caseNotExposed += other.caseNotExposed;
    controlAndExposed += other.controlAndExposed;
    controlNotExposed += other.controlNotExposed;
  }
}