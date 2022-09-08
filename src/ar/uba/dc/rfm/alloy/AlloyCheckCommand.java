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

public final class AlloyCheckCommand {
	private static class Scope {
		public final int size;

		public final boolean isExactly;

		public Scope(final boolean isExactly, final int size) {
			super();
			this.isExactly = isExactly;
			this.size = size;
		}

		public Scope(int size) {
			this(false, size);
		}

		@Override
		public String toString() {
			return "[" + (isExactly ? "exactly" : "notExactly") + "," + size
					+ "]";
		}

		@Override
		public boolean equals(Object arg0) {
			if (arg0 != null && arg0.getClass().equals(Scope.class)) {
				Scope that = (Scope) arg0;
				return this.isExactly == that.isExactly
						&& this.size == that.size;
			} else
				return false;
		}

		@Override
		public int hashCode() {
			return new Boolean(isExactly).hashCode() + size;
		}

	}

	private Map<String, Scope> map = new HashMap<String, Scope>();

	public void put(String signatureId, boolean isExactly, int size) {
		map.put(signatureId, new Scope(isExactly, size));
	}

	public void put(String signatureId, int size) {
		map.put(signatureId, new Scope(size));
	}

	@Override
	public String toString() {
		return map.toString();
	}

	public boolean contains(String signatureId) {
		return map.containsKey(signatureId);
	}

	public boolean isExactly(String signatureId) {
		if (!contains(signatureId))
			throw new IllegalArgumentException();

		return map.get(signatureId).isExactly;
	}

	public int getSize(String signatureId) {
		if (!contains(signatureId))
			throw new IllegalArgumentException();

		return map.get(signatureId).size;
	}

	public Set<String> signatureSet() {
		return map.keySet();
	}

	@Override
	public boolean equals(Object arg0) {
		if (arg0!=null && arg0.getClass().equals(AlloyCheckCommand.class)) {
			AlloyCheckCommand that =(AlloyCheckCommand)arg0;
			return this.map.equals(that.map);
		} else
		return false;
	}

	@Override
	public int hashCode() {
		return map.hashCode();
	}

}
