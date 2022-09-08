package ar.uba.dc.rfm.dynalloy.analyzer;

import edu.mit.csail.sdg.alloy4compiler.ast.Command;
import edu.mit.csail.sdg.alloy4compiler.ast.Module;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Solution;

public class AlloyAnalysisResult {

	private final boolean is_satisfiable;
	
	private final A4Solution alloy_solution;
	
	private final Module world;
	
	private Command command;
	
	public AlloyAnalysisResult(boolean is_satisfiable, Module world, Command command, A4Solution alloy_solution) {
		this.alloy_solution = alloy_solution;
		this.is_satisfiable = is_satisfiable;
		this.world = world;
		this.command = command;
	}

	public boolean isSAT() {
		return is_satisfiable;
		
	}

	public boolean isUNSAT() {
		return !is_satisfiable;
	}
	
	public A4Solution getAlloy_solution() {
		return alloy_solution;
	}
	
	public Module getWorld() {
		return world;
	}

	public Command getCommand() {
		return command;
	}
	
	
}
