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
package org.erasmusmc.jerboa.dataClasses;

public class Exposure {
  public long start;
  public String caseset;
  public String type;
  public String patientID;  

  public Exposure() {
  }

  public Exposure(Exposure exposure) {
    this.start = exposure.start;
    this.caseset = exposure.caseset;
    this.type = exposure.type;
    this.patientID = exposure.patientID;
  }

}