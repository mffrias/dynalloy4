package ar.uba.dc.rfm.dynalloy.xlator;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import ar.uba.dc.rfm.alloy.AlloyTyping;
import ar.uba.dc.rfm.alloy.AlloyVariable;
import ar.uba.dc.rfm.alloy.IdxRange;
import ar.uba.dc.rfm.alloy.IdxRangeMap;
import ar.uba.dc.rfm.alloy.VariableId;
import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprVariable;
import ar.uba.dc.rfm.alloy.util.FormulaMutator;
import ar.uba.dc.rfm.alloy.util.VarSubstitutor;

class CallingConvention {

	public final static CallingConvention ASSIGMENT_CALLING_CONVENTION = buildAssigmentCallingConvention();

	private IdxRangeMap ranges;
	private List<VariableId> formalParameters;
	private List<VariableId> localVariables;
	private AlloyTyping typing;

	public CallingConvention(List<VariableId> formalParameters,
			List<VariableId> localVariables, IdxRangeMap ranges,
			AlloyTyping typing) {
		super();
		this.formalParameters = formalParameters;
		this.ranges = ranges;
		this.localVariables = localVariables;
		this.typing = typing;
	}

	private static CallingConvention buildAssigmentCallingConvention() {
		VariableId l = new VariableId("l");
		VariableId r = new VariableId("r");
		List<VariableId> formalParameters = Arrays.asList(new VariableId[] { l,
				r });
		List<VariableId> localVariables = Collections.<VariableId> emptyList();
		IdxRangeMap ranges = new IdxRangeMap();
		ranges.addIdxRange(l, 1, 1);
		ranges.addIdxRange(r, 0, 0);
		AlloyTyping typing = new AlloyTyping();
		typing.put(new AlloyVariable(l), "univ");
		typing.put(new AlloyVariable(r), "univ");
		CallingConvention assigmentCallingConvention = new CallingConvention(
				formalParameters, localVariables, ranges, typing);
		return assigmentCallingConvention;
	}

	private static final class IndexAppender extends VarSubstitutor {

		private int index;

		public IndexAppender(int index) {
			super();
			this.index = index;
		}

		@Override
		protected AlloyExpression getExpr(AlloyVariable variable) {
			AlloyVariable v;
			if (variable.isPreStateVar()) {
				v = new AlloyVariable(variable.getVariableId(),
						IdxRange.SUBINDEX_0);
				v.setMutable(false);

			} else {
				v = new AlloyVariable(variable.getVariableId(), index);
			}
			return new ExprVariable(v);
		}

	}

	public List<AlloyExpression> instantiate(List<AlloyExpression> actualParams) {
		CallingConventionResult result = instantiate(actualParams, null);
		return result.getParameters();
	}

	public CallingConventionResult instantiate(
		List<AlloyExpression> actualParams, String tempPrefix) {
		List<AlloyExpression> parameterList = new LinkedList<AlloyExpression>();
		AlloyTyping locals = new AlloyTyping();

		// parameters
		for (int i = 0; i < actualParams.size(); i++) {
			AlloyExpression e = actualParams.get(i);
			VariableId f = formalParameters.get(i);
			IdxRange r = ranges.getIdxRange(f);
			if (r != null) {
				int low = r.getBegin();
				int high = r.getEnd();
//mfrias-mffrias 12-04-2013: bug left side of update must be a variable			
				for (int j = low; j <= high; j++) {
					if (j != low && !(e instanceof ExprVariable))
						throw new RuntimeException(
								"The left-hand side of an update must be variable");

					IndexAppender indexAppender = new IndexAppender(j);
					FormulaMutator formulaMutator = new FormulaMutator(
							indexAppender);
					indexAppender.setFormulaVisitor(formulaMutator);
					AlloyExpression x = (AlloyExpression) e
							.accept(indexAppender);
					parameterList.add(x);
				}
			}	
		}
		// locals
		for (int i = 0; i < localVariables.size(); i++) {
			VariableId f = localVariables.get(i);
			IdxRange r = ranges.getIdxRange(f);
			if (r != null) {
				int low = r.getBegin();
				int high = r.getEnd();
				for (int j = low; j <= high; j++) {
					String localVarId;
					if (tempPrefix != null)
						localVarId = tempPrefix + f.getString();
					else
						localVarId = f.getString();

					AlloyVariable localVar = new AlloyVariable(localVarId);
					ExprVariable exprLocalVar = new ExprVariable(localVar);
					ExprVariable x = (ExprVariable) exprLocalVar
							.accept(new IndexAppender(j));
					parameterList.add(x);
					String type = typing.get(new AlloyVariable(f));
					if (type == null)
						throw new RuntimeException("Unknown type for variable:"
								+ x.getVariable().toString());

					locals.put(x.getVariable(), type);

				}
			}

		}
		return new CallingConventionResult(parameterList, locals);
	}
}
