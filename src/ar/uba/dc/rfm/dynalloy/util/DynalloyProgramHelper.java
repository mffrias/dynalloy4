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
package ar.uba.dc.rfm.dynalloy.util;
import java.util.Arrays;
import java.util.LinkedList;

import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;
import ar.uba.dc.rfm.alloy.ast.formulas.PredicateFormula;
import ar.uba.dc.rfm.dynalloy.ast.programs.Choice;
import ar.uba.dc.rfm.dynalloy.ast.programs.Closure;
import ar.uba.dc.rfm.dynalloy.ast.programs.Composition;
import ar.uba.dc.rfm.dynalloy.ast.programs.DynalloyProgram;
import ar.uba.dc.rfm.dynalloy.ast.programs.InvokeAction;
import ar.uba.dc.rfm.dynalloy.ast.programs.Skip;
import ar.uba.dc.rfm.dynalloy.ast.programs.TestPredicate;

public final class DynalloyProgramHelper {
	public static InvokeAction invoke(String actionId, AlloyExpression ... args ){
		LinkedList<AlloyExpression> args_list = new LinkedList<AlloyExpression>();
		for (int i = 0; i < args.length; i++) {
			args_list.add(args[i]);
		}
		return new InvokeAction(null, actionId, args_list);
	}

	public static TestPredicate test(String predId, AlloyExpression ... args ){
		LinkedList<AlloyExpression> args_list = new LinkedList<AlloyExpression>();
		for (int i = 0; i < args.length; i++) {
			args_list.add(args[i]);
		}
		return new TestPredicate(new PredicateFormula(null,predId, args_list));
	}

	public static Choice choice(DynalloyProgram ... args){
		return new Choice(Arrays.<DynalloyProgram>asList(args));
	}
	
	public static Closure closure(DynalloyProgram p){
		return new Closure(p);
	}

	public static Composition composition(DynalloyProgram ... args){
		return new Composition(Arrays.<DynalloyProgram>asList(args));
	}		
	
	public static Skip skip(){
		return new Skip();
	}
}