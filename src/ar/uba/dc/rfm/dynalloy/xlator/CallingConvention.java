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

/**
 * How the Alloy predicate of an action or program is called: which indexed copies of its formal
 * parameters and local variables its formula uses. The predicate has, for each formal parameter
 * in order, one parameter per index in the parameter's range in the formula, then the same for
 * each local variable; a variable the formula does not use gets none. So the predicate of an
 * action {@code a[x, y]} whose pre and post use {@code x}, {@code x'} and {@code y} is
 * {@code a[x_0, x_1, y_0]}, and the call {@code a[v, w.f]} becomes
 * {@code a[v_0, v_1, w_0.f_0]}: each actual parameter with all its variables at each index. For
 * a program call the local variables follow, renamed with the call's prefix
 * ({@code l3_temp_1}). The indices are those of the callee's own formula; the caller's
 * {@link CompositionJoiner} shifts them afterwards.
 *
 * <p>{@link DynalloyXlatorVisitor} builds one per action and per program, from the ranges of its
 * formula ({@link IdxRangeCollectHelper}), instantiates it with the formal parameters to write
 * the predicate's header, and registers it in the {@link SpecContext}, under
 * {@code <module>::<id>:<number of parameters>}. {@link SpecContext#invokeAction} and
 * {@link SpecContext#invokeProgram} instantiate it at every call. See docs/ARCHITECTURE.md
 * ("Translation").
 *
 * <p>Static state: {@link #ASSIGMENT_CALLING_CONVENTION}, built when the class is loaded and
 * never changed. Instances are not changed after construction either.
 */
class CallingConvention {

	/**
	 * The convention of an assignment {@code l := r}, as if it were an action with
	 * {@code l} at index 1 and {@code r} at index 0, both of type {@code univ}:
	 * {@link SpecContext#assigment} turns {@code x := e} into {@code x_1 = e_0}. Shared by every
	 * translation in the JVM.
	 */
	public final static CallingConvention ASSIGMENT_CALLING_CONVENTION = buildAssigmentCallingConvention();

	private IdxRangeMap ranges;
	private List<VariableId> formalParameters;
	private List<VariableId> localVariables;
	private AlloyTyping typing;

	/**
	 * Creates the calling convention of an action or a program.
	 *
	 * @param formalParameters the formal parameters, in order
	 * @param localVariables the local variables, in order; empty for an action. For a program,
	 *        {@link DynalloyXlatorVisitor} passes the declared ones and then every other variable
	 *        of the formula that is not a parameter, such as the renamed locals of the programs
	 *        it calls
	 * @param ranges the index range of each variable in the formula; a parameter or local
	 *        without one gets no argument, in the header or in a call
	 * @param typing the types, by plain (unindexed) variable; only read for the local variables
	 */
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

	/**
	 * Instantiates the convention without renaming the local variables, as for an action call
	 * or for a predicate's header (with the formal parameters as the actual ones).
	 *
	 * @param actualParams the actual parameters, in the order of the formal ones
	 * @return the arguments, as {@link #instantiate(List, String)} builds them
	 */
	public List<AlloyExpression> instantiate(List<AlloyExpression> actualParams) {
		CallingConventionResult result = instantiate(actualParams, null);
		return result.getParameters();
	}

	/**
	 * Builds the arguments of one call (see the class comment). For the i-th actual parameter
	 * and each index j in the range of the i-th formal parameter, lowest first: the actual with
	 * every variable in it at index j, except {@code \pre[x]}, which becomes {@code x_0}
	 * marked immutable. Then, for each local variable with a range and each index j, the
	 * variable {@code <tempPrefix><local>_<j>}, which also goes into the returned typing with
	 * the local's type. Returns new lists; the convention is not changed.
	 *
	 * <p>Assumes one actual parameter per formal one: an extra actual parameter throws an
	 * {@code IndexOutOfBoundsException}, and formal parameters past the last actual one are
	 * left out without notice.
	 *
	 * @param actualParams the actual parameters, in the order of the formal ones
	 * @param tempPrefix the prefix of the local variables ({@code l<n>_}, from
	 *        {@link SpecContext#invokeProgram}), or null to keep their names
	 * @return the arguments, and the typing of the local variables by indexed variable
	 * @throws RuntimeException if an actual parameter whose formal one has more than one index
	 *         is not a variable ("The left-hand side of an update must be variable"), or if a
	 *         local variable has no type ("Unknown type for variable:")
	 */
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
