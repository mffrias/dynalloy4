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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import ar.uba.dc.rfm.dynalloy.parser.splitter.Position;

/**
 * @author jgaleotti
 * 
 */
public final class Choice extends DynalloyProgram implements Iterable<DynalloyProgram> {

	private final List<DynalloyProgram> choice;

	public Choice(List<DynalloyProgram> children) {
		if (children.size() < 2)
			throw new IllegalArgumentException();

		choice = children;
	}
	
	public Choice(List<DynalloyProgram> children, Position pos) {
		this(children);
		setPosition(pos);
	}

	public Choice(DynalloyProgram p1, DynalloyProgram p2) {
		choice = new LinkedList<DynalloyProgram>();
		add(p1);
		add(p2);
	}
	
	public Choice(DynalloyProgram p1, DynalloyProgram p2, Position pos) {
		this(p1, p2);
		setPosition(pos);
	}

	public void add(DynalloyProgram p) {
		if (p instanceof Choice)
			add((Choice) p);
		else
			choice.add(p);
	}
	
	private void add(Choice p) {
		choice.addAll(p.choice);
	}

	public Choice sub(int count) {
		if (count < 2)
			throw new IllegalArgumentException("Count should be at least 2.");
		
		if (count == choice.size()) {
			return this;
		}
		
		Choice c = new Choice(choice.get(0), choice.get(1), getPosition());
		for (int i = 2; i < count; i++) {
			c.add(choice.get(i));
		}
		return c;
	}
	
	public Object accept(ProgramVisitor visitor) {
		return visitor.visit(this);
	}

	public Iterator<DynalloyProgram> iterator() {
		return choice.iterator();
	}

	@Override
	public boolean equals(Object arg0) {
		if (arg0 != null && arg0.getClass().equals(Choice.class)) {
			Choice that = (Choice) arg0;
			return this.choice.equals(that.choice);
		} else
			return false;
	}

	@Override
	public int hashCode() {
		return choice.hashCode();
	}

	@Override
	public String toString() {
		StringBuffer buff = new StringBuffer();
		for (DynalloyProgram p : choice) {
			if (buff.length()>0)
				buff.append("+");
			buff.append(p.toString());
		}
		return buff.toString();
	}

	public int size() {
		return choice.size();
	}
	
	public DynalloyProgram get(int index) {
		return choice.get(index);
	}
	
}
