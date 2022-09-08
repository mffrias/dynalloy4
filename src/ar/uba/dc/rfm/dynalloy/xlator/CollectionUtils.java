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
package ar.uba.dc.rfm.dynalloy.xlator;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

final class CollectionUtils {
	@SuppressWarnings("unchecked")
	public static<Key,Value> Map<Key,Value> map(Object ... args){
		LinkedHashMap<Key, Value> result = new LinkedHashMap<Key, Value>();
		for (int i = 0; i < args.length; i+=2) {
			result.put((Key) args[i], (Value) args[i+1]);
		}
		return result;
	}
	
	public static<T> List<T> list(T ... source){
		List<T> r = new LinkedList<T>();
		for (int i = 0; i < source.length; i++) {
			r.add(source[i]);
		}	
		return r;
	}
	
	public static<T> List<T> vector(T ... source){
		Vector<T> r = new Vector<T>(source.length);
		for (int i = 0; i < source.length; i++) {
			r.add(source[i]);
		}	
		return r;
	}

	@SuppressWarnings("unchecked")
	public static String join(List source, String separator) {
		StringBuffer buffer = new StringBuffer();
		boolean first = true;
		for (Object o : source) {
			if (!first){
				 buffer.append(separator);
			} else {
				first = false;
			}
			buffer.append(o);
		}
		return buffer.toString();
	}
	
	public static<E> boolean sameElements(Collection<E> c1, Collection<E> c2){		
		return includedElements(c1, c2) && includedElements(c2, c1);
	}

	public static<E> boolean includedElements(Collection<E> c1, Collection<E> c2){
		for (E e : c1) {
			if (!c2.contains(e))
				return false;
		}
		
		return true;
	}

	public static<T> Set<T> diff(Set<T> s1,
			Set<T> s2) {
		Set<T> result = new HashSet<T>(s1);
		result.removeAll(s2);
		return result;
	}
	
	public static<T> Set<T> union(Set<T> s1,
			Set<T> s2) {
		Set<T> result = new HashSet<T>(s1);
		result.addAll(s2);
		return result;
	}
	

	public static<T> Set<T> intersect(Set<T> s1, Set<T> s2) {
		Set<T> r = new HashSet<T>(s1);
		r.retainAll(s2);
		return r;
	}
}
