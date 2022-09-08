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
package ar.uba.dc.rfm.dynalloy.xlator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import ar.uba.dc.rfm.alloy.AlloyTyping;
import ar.uba.dc.rfm.alloy.AlloyVariable;
import static ar.uba.dc.rfm.alloy.AlloyVariable.asAlloyVariable;
import ar.uba.dc.rfm.alloy.IdxRange;
import ar.uba.dc.rfm.alloy.IdxRangeMap;
import ar.uba.dc.rfm.alloy.VariableId;
import ar.uba.dc.rfm.alloy.ast.AlloyAssertion;
import ar.uba.dc.rfm.alloy.ast.AlloyFact;
import ar.uba.dc.rfm.alloy.ast.AlloyModule;
import ar.uba.dc.rfm.alloy.ast.AlloySig;
import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;
import ar.uba.dc.rfm.alloy.ast.expressions.ThizVarCollectorVisitor;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprVariable;
import static ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression.asAlloyExpression;
import static ar.uba.dc.rfm.dynalloy.xlator.CollectionUtils.intersect;
import ar.uba.dc.rfm.alloy.ast.formulas.AlloyFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.AndFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.FormulaVisitor;
import ar.uba.dc.rfm.alloy.ast.formulas.ImpliesFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.NotFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.PredicateFormula;
import ar.uba.dc.rfm.alloy.util.AlloyBuffer;
import ar.uba.dc.rfm.alloy.util.AlloyPrinter;
import ar.uba.dc.rfm.alloy.util.FormulaBuffer;
import ar.uba.dc.rfm.alloy.util.FormulaMutator;
import ar.uba.dc.rfm.alloy.util.FormulaPrinter;
import ar.uba.dc.rfm.alloy.util.QFPostconditionPostfixTransformer;
import ar.uba.dc.rfm.alloy.util.QFPreconditionPostfixTransformer;
import ar.uba.dc.rfm.alloy.util.QFtransformer;
import ar.uba.dc.rfm.da2a.qremover.QuantifierRemover;
import ar.uba.dc.rfm.da2a.qremover.QuantifierRemover.QuantifierRemoverResult;
import ar.uba.dc.rfm.dynalloy.DynAlloyOptions;
import ar.uba.dc.rfm.dynalloy.ast.ActionDeclaration;
import ar.uba.dc.rfm.dynalloy.ast.AssertionDeclaration;
import ar.uba.dc.rfm.dynalloy.ast.ProgramDeclaration;
import ar.uba.dc.rfm.dynalloy.ast.DynalloyModule;
import ar.uba.dc.rfm.dynalloy.ast.IDynalloyVisitor;
import ar.uba.dc.rfm.dynalloy.ast.programs.Composition;
import ar.uba.dc.rfm.dynalloy.ast.programs.InvokeAction;
import ar.uba.dc.rfm.dynalloy.ast.programs.InvokeProgram;
import ar.uba.dc.rfm.dynalloy.util.DynalloyVisitor;
import ar.uba.dc.rfm.dynalloy.visualization.DynalloyVisualizerException;

public class DynalloyXlatorVisitor extends DynalloyVisitor {

	private boolean prettyPrinting = true;

	private final IdxRangeCollectHelper varCollector = new IdxRangeCollectHelper();

	private final ProgramTranslator programTranslator;

	private final SpecContext context;

	private final DynAlloyOptions options;

	private HashMap<String, AlloyTyping> varsAndTheirTypesComingFromArithmeticConstraintsInContractsByProgram;

	private HashMap<String, List<AlloyFormula>> predsComingFromArithmeticConstraintsInContractsByProgram;

	private HashMap<String, AlloyTyping> varsAndTheirTypesComingFromArithmeticConstraintsInObjectInvariantsByModule = new HashMap<String, AlloyTyping>();

	private HashMap<String, List<AlloyFormula>> predsComingFromArithmeticConstraintsInObjectInvariantsByModule = new HashMap<String, List<AlloyFormula>>();

	private boolean translatingForStryker = false;


	public DynalloyXlatorVisitor(SpecContext _specContext, DynAlloyOptions options, 
			HashMap<String, AlloyTyping> vars, HashMap<String, List<AlloyFormula>> preds,
			HashMap<String, AlloyTyping> varsOI, HashMap<String, List<AlloyFormula>> predsOI,
			boolean forStryker) {		
		super(new ProgramTranslator(_specContext));
		programTranslator = (ProgramTranslator) this.getDfsProgramVisitor();
		context = _specContext;
		this.options = options;
		this.varsAndTheirTypesComingFromArithmeticConstraintsInContractsByProgram = vars;
		this.predsComingFromArithmeticConstraintsInContractsByProgram = preds;
		this.varsAndTheirTypesComingFromArithmeticConstraintsInObjectInvariantsByModule = varsOI;
		this.predsComingFromArithmeticConstraintsInObjectInvariantsByModule = predsOI;
		this.translatingForStryker = forStryker;
	}

	public AlloyModule visit(DynalloyModule n) {

		// create buffer
		StringBuffer buffer = new StringBuffer();

		// gather alloyStr's
		buffer.append(n.getAlloyStr() + "\n");

		// translate atomic actions
		this.context.switchToModule(n.getModuleId());
		for (ActionDeclaration action : n.getActions()) {
			String actionPredicate = (String) action.accept(this);

			buffer.append("\n");
			buffer.append("\n");
			buffer.append(actionPredicate + "\n");

			//			System.out.println(buffer.toString() + "\n");
		}

		// translate programs
		this.context.switchToModule(n.getModuleId());

		for (ProgramDeclaration program : n.getPrograms()) {
			String programPredicate = (String) program.accept(this);
			buffer.append("\n");
			buffer.append("\n");
			buffer.append(programPredicate + "\n");
			AlloyVariable throwVar = new AlloyVariable("throw");
			if (!program.getParameterTyping().contains(throwVar) || !program.getParameterTyping().get(throwVar).equals("java_lang_Throwable + null")) {
				AlloyTyping at = new AlloyTyping();
				String retType = null;
				for (AlloyVariable av : program.getParameterTyping()){
					if (!av.getVariableId().equals("return")){
						at.put(av, at.get(av));
					} else {
						retType = at.get(av);
					}
				}

				String invokedMethodName = program.getProgramId();
				if (invokedMethodName.endsWith("_0"))
					invokedMethodName = invokedMethodName.substring(0, invokedMethodName.length()-2);
				String newAddedFunction = "fun " + invokedMethodName + "[" + at.toString() + "] : " + retType + "{" + program.getParameters().get(0) + "}";
				buffer.append("\n");
				buffer.append("\n");
				buffer.append(newAddedFunction + "\n");
			}
		}

		String compilableA4Spec;
		compilableA4Spec = buffer.toString();
		AlloySig qfSig = new AlloySig(false, true, "QF", new AlloyTyping(), null);

		List<AlloyFact> facts = new LinkedList<AlloyFact>();
		Set<AlloyAssertion> assertions = new HashSet<AlloyAssertion>();

		// translate assertions
		this.context.switchToModule(n.getModuleId());

		assert n.getAssertions().size() == 1;
		AssertionDeclaration assertion = null;
		try {
			assertion = n.getAssertions().iterator().next();
		} catch (Exception e ){
			System.out.println("The method under analysis may be missing a contract.");
		}

		AlloyAssertion alloyAssertion = (AlloyAssertion) assertion.accept(this);

		if (!options.getRemoveQuantifiers()){
			for (String dm : this.varsAndTheirTypesComingFromArithmeticConstraintsInObjectInvariantsByModule.keySet()){
				if (dm.equals(this.options.getModuleUnderAnalysis())){

					AlloyTyping at = this.varsAndTheirTypesComingFromArithmeticConstraintsInObjectInvariantsByModule.get(dm);

					if (at != null){
						for (AlloyVariable av : at.getVarsInTyping()){
							alloyAssertion.getQuantifiedVariables().put(av,at.get(av));
						}
					}
				}
			}
		}

		//from the assertion name we remove the "check_" prefix. 
		String methodUnderAnalysis = this.options.getAssertionId().substring(6,this.options.getAssertionId().length());
		AlloyTyping typingForMethodUnderAnalysis = this.varsAndTheirTypesComingFromArithmeticConstraintsInContractsByProgram.get(methodUnderAnalysis);

		//		if (typingForMethodUnderAnalysis != null){
		//			for (AlloyVariable av : typingForMethodUnderAnalysis.getVarsInTyping()){
		//				qfSig.getFields().put(av,typingForMethodUnderAnalysis.get(av));
		//			}
		//		}




		if (options.getRemoveQuantifiers() && assertion != null && assertion.getAssertionId().equals(options.getAssertionId())) {

			QuantifierRemoverResult qfResult = removeQuantifiers(alloyAssertion);

			for (String dm : this.varsAndTheirTypesComingFromArithmeticConstraintsInObjectInvariantsByModule.keySet()){
				if (dm.equals(this.options.getModuleUnderAnalysis())){

					AlloyTyping at = this.varsAndTheirTypesComingFromArithmeticConstraintsInObjectInvariantsByModule.get(dm);

					if (at != null){
						for (AlloyVariable av : at.getVarsInTyping()){
							qfSig.getFields().put(av,at.get(av));
						}
					}
				}
			}

			if (typingForMethodUnderAnalysis != null){
				for (AlloyVariable av : typingForMethodUnderAnalysis.getVarsInTyping()){
					qfSig.getFields().put(av,typingForMethodUnderAnalysis.get(av));
				}
			}


			for (AlloyVariable av : qfResult.qfSignature.getFields().getVarsInTyping()){
				qfSig.getFields().put(av, qfResult.qfSignature.getFields().get(av));	
			}


			facts.addAll(qfResult.qfFacts);

			alloyAssertion = qfResult.qfAssertion;
		}

		if (!options.getRemoveQuantifiers()){
			for (AlloyVariable av : typingForMethodUnderAnalysis.getVarsInTyping()){
				alloyAssertion.getQuantifiedVariables().put(av,typingForMethodUnderAnalysis.get(av));
			}

			//			for (AlloyVariable av : qfResult.qfSignature.getFields().getVarsInTyping()){
			//				qfSig.getFields().put(av, qfResult.qfSignature.getFields().get(av));	
			//			}

		}

		assertions.add(alloyAssertion);

		AlloyModule alloyAST = new AlloyModule(compilableA4Spec, qfSig, facts, assertions);

		return alloyAST;

	}

	private QuantifierRemoverResult removeQuantifiers(AlloyAssertion assertion) {
		QuantifierRemover remover = new QuantifierRemover();
		QuantifierRemoverResult qfResult = remover.removeQuantifiers(assertion);
		return qfResult;
	}


	private QuantifierRemoverResult removeFactQuantifiers(AlloyAssertion fact) {
		QuantifierRemover remover = new QuantifierRemover();
		QuantifierRemoverResult qfResult = remover.removeFactQuantifiers(fact);
		return qfResult;
	}


	private String appendBuiltInPredicates(String strSpec) {
		StringBuffer a4StrBuff = new StringBuffer();
		a4StrBuff.append(strSpec + "\n");
		if (!strSpec.contains("pred " + AlloyPredicates.TRUE_PRED_ID))
			a4StrBuff.append(AlloyPredicates.TRUE_PRED_SPEC + "\n");
		if (!strSpec.contains("pred " + AlloyPredicates.FALSE_PRED_ID))
			a4StrBuff.append(AlloyPredicates.FALSE_PRED_SPEC + "\n");
		String r = a4StrBuff.toString();
		return r;
	}

	private AlloyTyping buildQuantification(AlloyFormula f, AlloyTyping ps) {
		Map<AlloyVariable, String> result = new HashMap<AlloyVariable, String>();
		IdxRangeMap rs = new IdxRangeCollectHelper().collect(f);
		for (VariableId vId : rs.keySet()) {
			IdxRange r = rs.getIdxRange(vId);
			for (int i = r.getBegin(); i <= r.getEnd(); i++) {
				result.put(new AlloyVariable(vId, i), typeOf(vId, ps));
			}
		}
		return new AlloyTyping(result);
	}

	private String typeOf(VariableId id, AlloyTyping ps) {
		for (AlloyVariable v : ps.varSet()) {
			if (v.getVariableId().equals(id))
				return ps.get(v);
		}
		throw new IllegalArgumentException("variable " + id.toString() + " is not a local variable or a declared field");
	}




	public Object visit(AssertionDeclaration n) {

		programTranslator.setNewProgramId(n.getAssertionId());

		//Get the predicates that have to be added to the assertion. Use the 
		//assertion name to extract the module the predicates belong to.
		List<AlloyFormula> predsToAddThatComeFromInvariant = null;

		for (String st : this.predsComingFromArithmeticConstraintsInObjectInvariantsByModule.keySet()){
			if (n.getAssertionId().contains(st)) {
				predsToAddThatComeFromInvariant = this.predsComingFromArithmeticConstraintsInObjectInvariantsByModule.get(st);
			}
		}

		AlloyFormula pre = addIdxsToPred(n.getPre());
		AlloyFormula aux_af = pre;

		List<AlloyFormula> predsForCurrentProgram = null;
		AlloyTyping typingForCurrentProgram = null;

		for (Iterator<String> programNamesWithAttachedTypings = this.varsAndTheirTypesComingFromArithmeticConstraintsInContractsByProgram.keySet().iterator();
				programNamesWithAttachedTypings.hasNext();){
			String progName = programNamesWithAttachedTypings.next();	
			if (n.getProgram() instanceof InvokeProgram){
				if (((InvokeProgram)n.getProgram()).getProgramId().contains(progName)){
					typingForCurrentProgram = varsAndTheirTypesComingFromArithmeticConstraintsInContractsByProgram.get(progName);
					predsForCurrentProgram = predsComingFromArithmeticConstraintsInContractsByProgram.get(progName);
					break;
				}
			}
			if (n.getProgram() instanceof Composition && ((Composition)n.getProgram()).get(0) instanceof InvokeProgram){
				if (((InvokeProgram)((Composition)n.getProgram()).get(0)).getProgramId().contains(progName)){
					typingForCurrentProgram = varsAndTheirTypesComingFromArithmeticConstraintsInContractsByProgram.get(progName);
					predsForCurrentProgram = predsComingFromArithmeticConstraintsInContractsByProgram.get(progName);
					break;
				}
			} else {
				//throw new DynalloyVisualizerException();
			}
		}


		for (AlloyFormula af : predsToAddThatComeFromInvariant){
			Set<AlloyVariable> varsSet = this.varsAndTheirTypesComingFromArithmeticConstraintsInObjectInvariantsByModule.get(this.options.getModuleUnderAnalysis()).getVarsInTyping();
			HashSet<AlloyVariable> vars = new HashSet<AlloyVariable>();
			for (AlloyVariable av : varsSet){
				vars.add(av);
			}
			AlloyFormula fct = addIdxsToPredsFromInvInPre((PredicateFormula) af, vars);
			aux_af = new AndFormula(aux_af, fct);
		}

		AlloyFormulaWithLocals program = (AlloyFormulaWithLocals) n.getProgram().accept(programTranslator);

		IdxRangeMap irm = varCollector.collect(program.getFormula());

		if (predsForCurrentProgram != null){
			for (AlloyFormula af : predsForCurrentProgram){
				AlloyFormula fct = addIdxsToPost((PredicateFormula) af, irm);
				aux_af = new AndFormula(aux_af, fct);
			}
		}

		aux_af = new AndFormula(aux_af, program.getFormula());

		if (predsToAddThatComeFromInvariant != null){
			for (AlloyFormula af : predsToAddThatComeFromInvariant){
				Set<AlloyVariable> varsSet = this.varsAndTheirTypesComingFromArithmeticConstraintsInObjectInvariantsByModule.get(this.options.getModuleUnderAnalysis()).getVarsInTyping();
				HashSet<AlloyVariable> vars = new HashSet<AlloyVariable>();
				for (AlloyVariable av : varsSet){
					vars.add(av);
				}
				AlloyFormula fct = addIdxsToPredsFromInvInPost((PredicateFormula) af, varCollector.collect(aux_af));
				QFPostconditionPostfixTransformer qfpt = new QFPostconditionPostfixTransformer(vars);
				FormulaMutator fm = new FormulaMutator(qfpt);
				fct = (AlloyFormula) fct.accept(fm);
				aux_af = new AndFormula(aux_af, fct);
			}
		}

		AlloyFormula post = addIdxsToPost(n.getPost(), varCollector.collect(aux_af));
		if (translatingForStryker)
			post = new NotFormula(post);

		AlloyFormula formula = new ImpliesFormula(aux_af, post);		

		// variable "thiz" may not occur explicitly in the program if it is static, nor in the contract. Yet it
		// may occur in the predicates that come from the invariant. I will look up whether "thiz" occurs in these
		// predicates, and in case it does, will add it to the typing.

		AlloyTyping assertionTyping = n.getTyping();
		AlloyTyping programLocalsTyping = program.getLocals();
		AlloyTyping typing = assertionTyping.merge(programLocalsTyping);
		for (AlloyFormula af : predsToAddThatComeFromInvariant){
			ThizVarCollectorVisitor ancv = new ThizVarCollectorVisitor();
			FormulaVisitor fv = new FormulaVisitor(ancv);
			af.accept(fv);
			boolean b = ((ThizVarCollectorVisitor)fv.getDfsExprVisitor()).getResult();
			if (b){
				typing.put(new AlloyVariable("thiz"), this.options.getModuleUnderAnalysis());
			}

		}

		// Since formulas may contain arithmetic type variables that are to be constrained from outside the formula
		// being built, we will use a temporary typing that includes these variables just to avoid an exception for 
		// undefined variable.
		AlloyTyping tempTyping = typing.merge(typingForCurrentProgram);
		AlloyTyping invsTyping = this.varsAndTheirTypesComingFromArithmeticConstraintsInObjectInvariantsByModule.get(this.options.getModuleUnderAnalysis());
		tempTyping = tempTyping.merge(invsTyping);

		AlloyTyping typing_2 = buildQuantification(formula, tempTyping);
		AlloyAssertion assertion = new AlloyAssertion(n.getAssertionId(), typing_2, formula);
		if (context.getMapping() != null) {
			context.getMapping().addAssertion(n, assertion);
		}

		return assertion;

	}


	private AlloyFormula addIdxsToPredsFromInvInPre(PredicateFormula pre, HashSet<AlloyVariable> vars) {
		AlloyFormula f = (AlloyFormula) pre.accept(new FormulaMutator(new FirstSubindexer()));
		QFPreconditionPostfixTransformer qfpt = new QFPreconditionPostfixTransformer(vars);
		FormulaMutator fm = new FormulaMutator(qfpt);
		return (AlloyFormula)f.accept(fm);
	}




	private AlloyFormula addIdxsToPredsFromInvInPost(PredicateFormula post, IdxRangeMap rs) {
		AlloyFormula f = (AlloyFormula) post.accept(new FormulaMutator(new FirstSubindexer()));
		FormulaBuffer buff = new FormulaBuffer(f);
		IdxRangeMap postRs = varCollector.collect(f);
		for (VariableId vId : postRs.keySet()) {
			//		for (VariableId vId : intersect(postRs.keySet(), rs.keySet())) {
			//			if (postRs.getIdxRange(vId).getEnd() == IdxRange.SUBINDEX_1)
			//			if (!vId.getString().contains("SK_jml_pred")){
			buff.replaceIdx(vId, IdxRange.SUBINDEX_0, rs.getIdxRange(vId).getEnd());
			//			} else {
			//				buff.removeIdx(vId);
			//			}
		}

		for (VariableId vId : postRs.keySet()) {
			if (!rs.keySet().contains(vId)/* && !vId.getString().contains("SK_jml_pred")*/) {
				buff.replaceIdx(vId, IdxRange.SUBINDEX_1, IdxRange.SUBINDEX_0);
			} 
			//				else {
			//				if (vId.getString().contains("SK_jml_pred")){
			//					buff.removeIdx(vId);
			//				}
			//			}

			//			if (vId.getString().contains("SK_jml_pred")){
			//				buff.replaceIdx(vId, IdxRange.SUBINDEX_0, IdxRange.SUBINDEX_1);
			//			}
		}
		return buff.toFormula();
	}



	private AlloyFormula addIdxsToPost(PredicateFormula post, IdxRangeMap rs) {
		AlloyFormula f = (AlloyFormula) post.accept(new FormulaMutator(new FirstSubindexer()));
		FormulaBuffer buff = new FormulaBuffer(f);
		IdxRangeMap postRs = varCollector.collect(f);
		for (VariableId vId : intersect(postRs.keySet(), rs.keySet())) {
			if (postRs.getIdxRange(vId).getEnd() == IdxRange.SUBINDEX_1)
				buff.replaceIdx(vId, IdxRange.SUBINDEX_1, rs.getIdxRange(vId).getEnd());
		}

		for (VariableId vId : postRs.keySet()) {
			if (!rs.keySet().contains(vId)) {
				buff.replaceIdx(vId, IdxRange.SUBINDEX_1, IdxRange.SUBINDEX_0);
			}

			//			if (vId.getString().contains("SK_jml_pred")){
			//				buff.replaceIdx(vId, IdxRange.SUBINDEX_0, IdxRange.SUBINDEX_1);
			//			}
		}

		return buff.toFormula();
	}

	public Object visit(ActionDeclaration n) {
		String actionId = n.getActionId();
		List<AlloyExpression> actualPs = AlloyExpression.asAlloyExpression(asAlloyVariable(n.getFormalParameters()));
		List<VariableId> formalPs = context.getFormalParams(actionId);

		PredicateFormula pre = VariableIndexer.substituteAndIndex(context.getActionPre(actionId), formalPs, actualPs);
		PredicateFormula post = VariableIndexer.substituteAndIndex(context.getActionPost(actionId), formalPs, actualPs);

		AlloyFormula alloyFormula = new AndFormula(pre, post);

		AlloyTyping typing = new AlloyTyping();

		//		if (this.options.getRemoveQuantifiers()){
		//			QFtransformer qft = new QFtransformer(this.varsAndTheirTypesComingFromArithmeticConstraintsInContractsByProgram, 
		//					this.varsAndTheirTypesComingFromArithmeticConstraintsInObjectInvariantsByModule);
		//			FormulaMutator fv = new FormulaMutator(qft);
		//			alloyFormula = (AlloyFormula)alloyFormula.accept(fv);
		//			typing.put(new AlloyVariable("QF"), "QF");
		//		}


		typing = typing.merge(buildQuantification(alloyFormula, n.getTyping()));

		String typesSeq = getTypesSeq(n.getFormalParameters());

		CallingConvention callingConvention = new CallingConvention(formalPs, Collections.<VariableId> emptyList(), new IdxRangeCollectHelper()
				.collect(alloyFormula), n.getTyping());
		context.putCallingConvention(context.getCurrentModuleId(), n.getActionId(), typesSeq, callingConvention);

		List<AlloyExpression> exprs = callingConvention.instantiate(actualPs);

		StringBuffer buff = new StringBuffer();

		buff.append("pred " + n.getActionId());
		buff.append("[");

		if (prettyPrinting)
			buff.append("\n");

		for (int i = 0; i < exprs.size(); i++) {
			AlloyExpression ex = exprs.get(i);
			if (i != 0) {
				buff.append(",");
				if (prettyPrinting)
					buff.append("\n");
			}
			String parameterDeclaration = ex.toString() + ":" + typing.get(((ExprVariable) ex).getVariable());

			if (prettyPrinting)
				parameterDeclaration = increaseIdentation(parameterDeclaration);

			buff.append(parameterDeclaration);
		}
		if (prettyPrinting)
			buff.append("\n");

		buff.append("]{");

		if (prettyPrinting)
			buff.append("\n");

		FormulaPrinter formulaPrinter = new FormulaPrinter();
		formulaPrinter.setPrettyPrinting(prettyPrinting);
		String formulaString = alloyFormula.accept(formulaPrinter) + "\n";
		buff.append(increaseIdentation(formulaString));
		buff.append("}");

		/* invoke(List<AlloyExpression> actuals) => */
		return buff.toString();
	}

	/**
	 * 
	 * @param f
	 *            a P(x,..x) formula
	 * @return Let m: variable.(plain+primed) |-> variable.(0+1), returns m(f)
	 */
	private PredicateFormula addIdxsToPred(PredicateFormula f) {
		return (PredicateFormula) f.accept(new FormulaMutator(new FirstSubindexer()));
	}

	public Object visit(ProgramDeclaration p) {
		programTranslator.setNewProgramId(p.getProgramId());

		if (!context.isAlreadyTranslated(null, p.getProgramId(), p.getParameters().size())) {

			Vector<Object> v = (Vector<Object>) super.visit(p);
			AlloyFormulaWithLocals programFormula = (AlloyFormulaWithLocals) v.get(0);

			List<AlloyExpression> actualPs = AlloyExpression.asAlloyExpression(asAlloyVariable(p.getParameters()));

			AlloyTyping localTyping = new AlloyTyping();
			for (AlloyVariable local : programFormula.getLocals()) {
				AlloyVariable var = new AlloyVariable(local.getVariableId());
				String type = programFormula.getLocals().get(local);
				localTyping.put(var, type);
			}

			AlloyTyping merged = p.getParameterTyping().merge(localTyping);



			//			if (this.options != null && this.options.getRemoveQuantifiers()){
			//				QFtransformer qft = new QFtransformer(this.varsAndTheirTypesComingFromArithmeticConstraintsInContractsByProgram, 
			//						this.varsAndTheirTypesComingFromArithmeticConstraintsInObjectInvariantsByModule);
			//				FormulaMutator fv = new FormulaMutator(qft);
			//				AlloyFormula af = programFormula.getFormula();
			//				af = (AlloyFormula)af.accept(fv);
			//				merged.put(new AlloyVariable("QF"), "QF");
			//			}



			AlloyTyping typing = buildQuantification(programFormula.getFormula(), merged);

			IdxRangeMap ranges = new IdxRangeCollectHelper().collect(programFormula.getFormula());

			List<VariableId> allLocals = new LinkedList<VariableId>(p.getLocalVariables());
			for (VariableId vId : ranges.keySet()) {
				if (!allLocals.contains(vId) && !p.getParameters().contains(vId))
					allLocals.add(vId);
			}



			CallingConvention callingConvention = new CallingConvention(p.getParameters(), allLocals, ranges, merged);

			String typesSeq = getTypesSeq(p.getParameters());
			context.putCallingConvention(context.getCurrentModuleId(), p.getProgramId(), typesSeq, callingConvention);

			List<AlloyExpression> exprs = callingConvention.instantiate(actualPs);

			FormulaPrinter printer = new FormulaPrinter();
			printer.setPrettyPrinting(prettyPrinting);

			StringBuffer buff = new StringBuffer();
			buff.append("pred " + p.getProgramId());
			buff.append("[");

			if (prettyPrinting)
				buff.append("\n");

			for (int i = 0; i < exprs.size(); i++) {
				AlloyExpression ex = exprs.get(i);
				if (i != 0) {
					buff.append(",");
					if (prettyPrinting)
						buff.append("\n");
				}

				String parameterDecl = null;;
				parameterDecl = ex.toString() + ":" + typing.get(((ExprVariable) ex).getVariable());		
				if (prettyPrinting)
					parameterDecl = increaseIdentation(parameterDecl);

				buff.append(parameterDecl);
			}

			if (prettyPrinting && !exprs.isEmpty())
				buff.append("\n");

			buff.append("]{");

			if (prettyPrinting)
				buff.append("\n");

			// local definitions
			String locals = "";
			for (VariableId tempVariableId : p.getLocalVariables()) {
				IdxRange tempRange = ranges.getIdxRange(tempVariableId);
				if (tempRange != null) {
					String alloyType = p.getParameterTyping().get(new AlloyVariable(tempVariableId));
					for (int i = tempRange.getBegin(); i <= tempRange.getEnd(); i++) {
						if (!locals.equals(""))
							locals += ",";
						locals += new AlloyVariable(tempVariableId, i).toString() + ":" + alloyType;
					}
				}
			}

			String formulaString = (String) programFormula.getFormula().accept(printer);

			if (prettyPrinting)
				formulaString = increaseIdentation(formulaString);

			buff.append(formulaString + "\n");
			buff.append("}");
			buff.append("\n");

			if (context.getMapping() != null) {
				context.getMapping().addRun(p.getProgramId(), programFormula.getFormula());
			}

			context.putTranslation(p.getProgramId(), buff.toString(), exprs, p.getParameters().size());
		}
		String predicateString = context.getTranslation(null, p.getProgramId(), p.getParameters().size());
		return predicateString;
	}

	private String getTypesSeq(List<VariableId> alloyTyping) {
		//Eventually we will need something more sophisticated. For the
		//time being, we will only add the number of parameters.
		//		String result = "";
		//		for (AlloyVariable av : alloyTyping.getVarsInTyping()){
		//			result = result + ":" + alloyTyping.get(av);
		//		}
		//		return result;
		return "" + alloyTyping.size();
	}

	private static String increaseIdentation(String string) {
		StringBuffer buffer = new StringBuffer();
		String[] lines = string.split("\n");

		if (lines.length == 1)
			buffer.append("  " + lines[0]);
		else
			for (String line : lines) {
				buffer.append("  " + line + "\n");
			}
		return buffer.toString();
	}

}
