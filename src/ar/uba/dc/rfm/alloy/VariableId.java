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

public final class VariableId {

	private String variableId;

	public String getString() {
		return variableId;
	}
	
	public VariableId(String _variableId) {
		variableId = _variableId;
	}

	public VariableId(VariableId _variableId) {
		variableId = _variableId.variableId;
	}
	
	@Override
	public boolean equals(Object arg0) {
		if ((arg0 != null) && arg0.getClass().equals(VariableId.class)) {
			VariableId vi = (VariableId) arg0;
			return vi.variableId.equals(this.variableId);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return variableId.hashCode();
	}

	@Override
	public String toString() {
		return variableId;
	}
	
}
