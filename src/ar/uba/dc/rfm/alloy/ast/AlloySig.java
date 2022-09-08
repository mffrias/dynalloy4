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
package ar.uba.dc.rfm.alloy.ast;

import ar.uba.dc.rfm.alloy.AlloyTyping;

public class AlloySig {

	
	private static final boolean DEFAULT_IS_ONE_VALUE = false;

	public Object accept(IAlloyVisitor v) {
		return v.visit(this);
	}

	private final boolean isAbstract;

	private final boolean isOne;

	private final AlloyTyping fields;

	private final String signatureId;

	private final String extendedSignatureId;

	public AlloySig(String _signatureId) {
		this(false, DEFAULT_IS_ONE_VALUE, _signatureId, new AlloyTyping(), null);
	}

	public AlloySig(boolean _isAbstract, boolean _isOne, String _signatureId,
			AlloyTyping _fields, String _extends) {
		super();
		isAbstract = _isAbstract;
		isOne = _isOne;
		signatureId = _signatureId;
		fields = _fields;
		extendedSignatureId = _extends;
	}

	@Override
	public boolean equals(Object arg0) {
		if (arg0 != null && arg0.getClass().equals(AlloySig.class)) {
			AlloySig that = (AlloySig) arg0;
			return isAbstract().equals(that.isAbstract())
					&& isOne().equals(that.isOne())
					&& getSignatureId().equals(that.getSignatureId())
					&& getFields().equals(that.getFields())
					&& (isExtension() ? this.getExtSigId().equals(
							that.getExtSigId()) : isExtension() == (that
							.isExtension()));
		} else
			return false;
	}

	@Override
	public int hashCode() {
		return isAbstract().hashCode() + isOne().hashCode()
				+ signatureId.hashCode() + fields.hashCode()
				+ (isExtension() ? getExtSigId().hashCode() : 0);
	}

	@Override
	public String toString() {
		return "[" + this.getSignatureId() + "," + this.isAbstract + ","
				+ this.isOne + "," + this.extendedSignatureId + ","
				+ this.fields.toString() + "]";
	}

	public Boolean isOne() {
		return new Boolean(isOne);
	}

	public Boolean isAbstract() {
		return new Boolean(isAbstract);
	}

	public AlloyTyping getFields() {
		return fields;
	}

	public String getSignatureId() {
		return signatureId;
	}

	public String getExtSigId() {
		return extendedSignatureId;
	}

	public boolean isExtension() {
		return extendedSignatureId != null;
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}
}
