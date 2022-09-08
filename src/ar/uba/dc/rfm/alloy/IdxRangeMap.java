/*
 * Dynalloy Translator
 * Copyright (c) 2007 Universidad de Buenos Aires
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA,
 * 02110-1301, USA
 */
package ar.uba.dc.rfm.alloy;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public final class IdxRangeMap {
    private Map<VariableId, IdxRange> map;

    public IdxRangeMap() {
        super();
        map = new HashMap<VariableId, IdxRange>();
    }

    public void addIdxRange(VariableId variableId, int begin, int end) {
        map.put(variableId, new IdxRange(begin, end));
    }

    public boolean contains(VariableId variableId) {
        return map.containsKey(variableId);
    }

    public Set<VariableId> keySet() {
        return map.keySet();
    }

    public IdxRange getIdxRange(VariableId variableId) {
        return (IdxRange) map.get(variableId);
    }

    public String toString() {
        return map.toString();
    }

    public boolean equals(Object o) {
        if (o!=null && o.getClass().equals(IdxRangeMap.class))
            return map.equals(((IdxRangeMap) o).map);
        else
            throw new ClassCastException(o.getClass().getName()
                    + " cannot be compared against IdxRangeMap objects");
    }

    public int hashCode() {
        return map.hashCode();
    }

}