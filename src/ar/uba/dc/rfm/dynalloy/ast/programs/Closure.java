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
package ar.uba.dc.rfm.dynalloy.ast.programs;

import ar.uba.dc.rfm.dynalloy.parser.splitter.Position;



/**
 * @author jgaleotti
 * 
 */
public final class Closure extends DynalloyProgram {

    private final DynalloyProgram program;

    public Closure(DynalloyProgram _p) {
        if (_p instanceof Closure) {
            program = ((Closure) _p).program;
        } else
            program = _p;
    }
    
    public Closure(DynalloyProgram _p, Position pos) {
    	this(_p);
    	setPosition(pos);
    }
    
    public Object accept(ProgramVisitor visitor) {
        return visitor.visit(this);
    }

    public DynalloyProgram getProgram() {
        return program;
    }

	@Override
	public boolean equals(Object arg0) {
		if (arg0!=null && arg0.getClass().equals(Closure.class)) {
			Closure that = (Closure)arg0;
			return this.getProgram().equals(that.getProgram());
		}
		else		
			return false;
	}

	@Override
	public int hashCode() {
		return getProgram().hashCode();
	}

	@Override
	public String toString() {
		return "(" + getProgram().toString() + ")*";
	}
    
}
