package ar.uba.dc.rfm.alloy.util;

import java.util.HashSet;
import java.util.Vector;

import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;
import ar.uba.dc.rfm.alloy.ast.formulas.EqualsFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.FormulaVisitor;
import ar.uba.dc.rfm.alloy.ast.formulas.PredicateFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.QuantifiedFormula;

public class LiteralCollector extends FormulaVisitor {
	
	public HashSet<String> intsCollected = new HashSet<String>();
	public HashSet<String> longsCollected = new HashSet<String>();
	public HashSet<String> charsCollected = new HashSet<String>();
	public HashSet<String> floatsCollected = new HashSet<String>();
	
	public LiteralCollector(LiteralCollectorExpVisitor lce){
		super(lce);
	}
	
	public LiteralCollectorExpVisitor getLiteralCollectorExpVisitor(){
		return (LiteralCollectorExpVisitor)this.getDfsExprVisitor();
	}
	
	@Override
	public Object visit(PredicateFormula n) {
		if (getLiteralCollectorExpVisitor() != null) {
			for (AlloyExpression e : n.getParameters()) {
				e.accept(this.getLiteralCollectorExpVisitor());
				this.intsCollected.addAll(this.getLiteralCollectorExpVisitor().getIntLiterals());
				this.longsCollected.addAll(this.getLiteralCollectorExpVisitor().getLongLiterals());
				this.charsCollected.addAll(this.getLiteralCollectorExpVisitor().getCharLiterals());
				this.floatsCollected.addAll(this.getLiteralCollectorExpVisitor().getFloatLiterals());
			}
		}
		return null;
	}

	
	@Override
	public Object visit(EqualsFormula n) {
		if (getLiteralCollectorExpVisitor() != null) {
			n.getLeft().accept(getLiteralCollectorExpVisitor());
			this.intsCollected.addAll(this.getLiteralCollectorExpVisitor().getIntLiterals());
			this.longsCollected.addAll(this.getLiteralCollectorExpVisitor().getLongLiterals());
			this.charsCollected.addAll(this.getLiteralCollectorExpVisitor().getCharLiterals());
			this.floatsCollected.addAll(this.getLiteralCollectorExpVisitor().getFloatLiterals());

			n.getRight().accept(getLiteralCollectorExpVisitor());
			this.intsCollected.addAll(this.getLiteralCollectorExpVisitor().getIntLiterals());
			this.longsCollected.addAll(this.getLiteralCollectorExpVisitor().getLongLiterals());
			this.charsCollected.addAll(this.getLiteralCollectorExpVisitor().getCharLiterals());
			this.floatsCollected.addAll(this.getLiteralCollectorExpVisitor().getFloatLiterals());
		}
		return null;
	}

	
	@Override
	public Object visit(QuantifiedFormula n) {
		n.getFormula().accept(this);
		return null;
	}

	

}
