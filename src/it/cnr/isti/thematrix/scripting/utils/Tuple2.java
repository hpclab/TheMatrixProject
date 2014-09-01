/*
 * Copyright (c) 2010-2014 "HPCLab at ISTI-CNR"
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.cnr.isti.thematrix.scripting.utils;

/**
 *
 * @author edoardovacchi
 * @param <A> 
 * @param <B> 
 */
public class Tuple2<A,B> {
    public final  A _1;
    public final B _2;
    public Tuple2(A a, B b) {
        _1=a; _2=b;
    }
    public String toString() {
        return String.format("(%s,%s)", _1, _2);
    }
}
