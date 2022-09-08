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
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


public class AlloyTyping implements Iterable<AlloyVariable>{

	public boolean isEmpty() {
		return typing.isEmpty();
	}

	private final Map<AlloyVariable, String> typing;

	public AlloyTyping(Map<AlloyVariable, String> typing) {
		super();
		this.typing = typing;
	}

	public AlloyTyping() {
		this(new HashMap<AlloyVariable, String>());
	}
	public Set<AlloyVariable> varSet() {
		return typing.keySet();
	}

	public void put(AlloyVariable v, String t) {
		typing.put(v, t);
	}


	public Set<AlloyVariable> getVarsInTyping(){
		return this.typing.keySet();
	}

	public String get(AlloyVariable v) {
		return typing.get(v);
	}

	public boolean contains(AlloyVariable v) {
		return typing.containsKey(v);
	}


	public void remove(AlloyVariable v){
		typing.remove(v);
	}

	@Override
	public boolean equals(Object arg0) {
		if (arg0 != null && arg0.getClass().equals(AlloyTyping.class)) {
			AlloyTyping that = (AlloyTyping) arg0;
			return this.typing.equals(that.typing);
		} else
			return false;
	}

	@Override
	public int hashCode() {
		return typing.hashCode();
	}

	@Override
	public String toString() {
		StringBuffer buff = new StringBuffer();
		for (AlloyVariable v : typing.keySet()) {
			if (buff.length()!=0)
				buff.append(",");
			buff.append(v + ":" + typing.get(v));
		}
		return buff.toString();
	}

	public Iterator<AlloyVariable> iterator() {
		return varSet().iterator();
	}

	public AlloyTyping merge(AlloyTyping that) {
		AlloyTyping merged = new AlloyTyping();
		for (AlloyVariable v : this) {
			merged.put(v, this.get(v));
		}
		
		if (that != null){
			for (AlloyVariable v : that) {
				merged.put(v, that.get(v));
			}
		}
		return merged;
	}

}
