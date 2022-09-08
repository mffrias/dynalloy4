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
public final class Composition extends DynalloyProgram implements
		Iterable<DynalloyProgram> {

	private final List<DynalloyProgram> composition;

	public Composition(DynalloyProgram p1, DynalloyProgram p2) {
		composition = new LinkedList<DynalloyProgram>();
		add(p1);
		add(p2);
	}

	public Composition(DynalloyProgram p1, DynalloyProgram p2, Position pos) {
		this(p1, p2);
		setPosition(pos);
	}

	public Composition(List<DynalloyProgram> children) {
		if (children.size() < 2)
			throw new IllegalArgumentException();

		composition = children;
	}

	public Composition(List<DynalloyProgram> children, Position pos) {
		this(children);
		setPosition(pos);
	}

	public void add(DynalloyProgram p) {
		if (p instanceof Composition)
			add((Composition) p);
		else
			composition.add(p);
	}

	private void add(Composition c) {
		if (c.composition.contains(null))
			throw new IllegalArgumentException();

		composition.addAll(c.composition);
	}

	public Composition sub(int count) {
		if (count < 2)
			throw new IllegalArgumentException("Count should be at least 2.");

		if (count == composition.size()) {
			return this;
		}

		Composition c = new Composition(composition.get(0), composition.get(1),
				getPosition());
		for (int i = 2; i < count; i++) {
			c.add(composition.get(i));
		}
		return c;
	}

	public DynalloyProgram subSequence(int beginIndex) {
		if (beginIndex < composition.size()) {

			if (beginIndex + 1 == this.size()) {
				return composition.get(beginIndex);
			} else {
				Composition c = new Composition(composition.get(beginIndex),
						composition.get(beginIndex + 1), getPosition());
				for (int i = beginIndex + 2; i < composition.size(); i++) {
					c.add(composition.get(i));
				}
				return c;
			}

		} else
			throw new IllegalArgumentException("Illegal beginIndex value: "
					+ beginIndex + " for a composition of size: " + this.size());
	}

	public Object accept(ProgramVisitor visitor) {
		return visitor.visit(this);
	}

	public Iterator<DynalloyProgram> iterator() {
		return composition.iterator();
	}

	@Override
	public boolean equals(Object arg0) {
		if (arg0 != null && arg0.getClass().equals(Composition.class)) {
			Composition that = (Composition) arg0;
			return this.composition.equals(that.composition);
		} else
			return false;
	}

	@Override
	public int hashCode() {
		return composition.hashCode();
	}

	@Override
	public String toString() {
		StringBuffer buff = new StringBuffer();
		for (DynalloyProgram p : composition) {
			if (buff.length() > 0)
				buff.append(";");
			buff.append(p.toString());
		}
		return buff.toString();
	}

	public static Composition buildComposition(DynalloyProgram... ps) {
		List<DynalloyProgram> l = new LinkedList<DynalloyProgram>();
		for (int i = 0; i < ps.length; i++)
			l.add(ps[i]);
		return new Composition(l);
	}

	public DynalloyProgram get(int index) {
		return composition.get(index);
	}

	public int size() {
		return composition.size();
	}
}
