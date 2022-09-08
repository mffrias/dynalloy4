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

public final class IdxRange {
	private int begin;

	private int end;

	public static final int SUBINDEX_0 = 0;

	public static final int SUBINDEX_1 = 1;

	public IdxRange(int _begin, int _end) {
		super();
		if (_begin < SUBINDEX_0)
			throw new IllegalArgumentException(
					"Cannot instantiate a IdxRange with begining at: "
							+ _begin);

		if (_end < _begin)
			throw new IllegalArgumentException(
					"Cannot instantiate a IdxRange with a negative range: ["
							+ _begin + "," + _end + "]");

		begin = _begin;
		end = _end;
	}

	public int getBegin() {
		return begin;
	}

	public int getEnd() {
		return end;
	}

	public String toString() {
		return "[" + begin + "-" + end + "]";
	}

	public boolean isInitSingleton() {
		return isSingleton() && getEnd()==SUBINDEX_0;
	}

	public boolean isSingleton() {
		return end == begin;
	}
	
	public boolean equals(Object o) {
		if (o!=null && o.getClass().equals(IdxRange.class)) {
			IdxRange idxR = (IdxRange)o;
			return ((this.begin == idxR.begin) && (this.end == idxR.end));
		}
		return false;
	}

	public int hashCode() {
		return begin + end;
	}
}