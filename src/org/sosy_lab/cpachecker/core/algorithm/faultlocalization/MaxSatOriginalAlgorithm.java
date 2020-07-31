// This file is part of CPAchecker,
// a tool for configurable software verification:
// https://cpachecker.sosy-lab.org
//
// SPDX-FileCopyrightText: 2007-2020 Dirk Beyer <https://www.sosy-lab.org>
//
// SPDX-License-Identifier: Apache-2.0

package org.sosy_lab.cpachecker.core.algorithm.faultlocalization;

import com.google.common.base.VerifyException;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Set;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.sosy_lab.cpachecker.core.CPAcheckerResult.Result;
import org.sosy_lab.cpachecker.core.algorithm.faultlocalization.formula.FormulaContext;
import org.sosy_lab.cpachecker.core.algorithm.faultlocalization.formula.Selector;
import org.sosy_lab.cpachecker.core.algorithm.faultlocalization.formula.TraceFormula;
import org.sosy_lab.cpachecker.core.interfaces.Statistics;
import org.sosy_lab.cpachecker.core.reachedset.UnmodifiableReachedSet;
import org.sosy_lab.cpachecker.exceptions.CPATransferException;
import org.sosy_lab.cpachecker.util.faultlocalization.Fault;
import org.sosy_lab.cpachecker.util.faultlocalization.FaultContribution;
import org.sosy_lab.cpachecker.util.predicates.smt.Solver;
import org.sosy_lab.cpachecker.util.statistics.StatCounter;
import org.sosy_lab.cpachecker.util.statistics.StatKind;
import org.sosy_lab.cpachecker.util.statistics.StatTimer;
import org.sosy_lab.cpachecker.util.statistics.StatisticsWriter;
import org.sosy_lab.java_smt.api.BooleanFormula;
import org.sosy_lab.java_smt.api.BooleanFormulaManager;
import org.sosy_lab.java_smt.api.SolverException;

public class MaxSatOriginalAlgorithm implements FaultLocalizationAlgorithmInterface, Statistics {

  private Solver solver;
  private BooleanFormulaManager bmgr;

  //Statistics
  private StatTimer totalTime = new StatTimer(StatKind.SUM, "Total time to find all subsets");
  private StatCounter unsatCalls = new StatCounter("Total calls to sat solver");
  private StatCounter savedCalls = new StatCounter("Total calls prevented by subset check");
  @Override
  public Set<Fault> run(FormulaContext pContext, TraceFormula tf)
      throws CPATransferException, InterruptedException, SolverException, VerifyException {

    solver = pContext.getSolver();
    bmgr = solver.getFormulaManager().getBooleanFormulaManager();

    Set<Fault> hard = new HashSet<>();

    //if selectors are reduced the set ensures to remove duplicates
    Set<FaultContribution> soft = new HashSet<>(tf.getEntries().toSelectorList());
    //if a selector is true (i. e. enabled) it cannot be part of the result set. This usually happens if the selector is a part of the pre-condition
    soft.removeIf(fc -> bmgr.isTrue(((Selector)fc).getFormula()) || bmgr.isFalse(((Selector)fc).getFormula()));

    Fault complement;
    totalTime.start();
    // loop as long as new maxsat cores are found.
    while(true){
      complement = coMSS(soft, tf, hard);
      if (complement.isEmpty()) {
        break;
      }
      hard.add(complement);
      soft.removeAll(complement);
    }
    totalTime.stop();
    return hard;
  }

  /**
   * Get the complement of a maximal satisfiable set considering the already found ones
   *
   * @param pTraceFormula TraceFormula to the error
   * @param pHardSet already found minimal sets
   * @return new minimal set
   * @throws SolverException thrown if tf is satisfiable
   * @throws InterruptedException thrown if interrupted
   */
  private Fault coMSS(
      Set<FaultContribution> pSoftSet, TraceFormula pTraceFormula, Set<Fault> pHardSet)
      throws SolverException, InterruptedException {
    Set<FaultContribution> selectors = new HashSet<>(pSoftSet);
    Fault result = new Fault();
    BooleanFormula composedFormula =
        bmgr.and(
            pTraceFormula.getTraceFormula(),
            hardSetFormula(pHardSet));
    boolean changed;
    do {
      changed = false;
      for (FaultContribution fc : selectors) {
        Selector s = (Selector)fc;
        Fault copy = new Fault(new HashSet<>(result));
        copy.add(s);
        unsatCalls.inc();
        if (!solver.isUnsat(bmgr.and(composedFormula, softSetFormula(copy)))) {
          changed = true;
          result.add(s);
          selectors.remove(s);
          break;
        }
      }
    } while (changed);
    return new Fault(selectors);
  }

  /**
   * Conjunct of all selector-formulas
   * @param softSet left selectors
   * @return boolean formula as conjunct of all selector formulas
   */
  private BooleanFormula softSetFormula(Fault softSet) {
    return softSet.stream().map(f -> ((Selector)f).getFormula()).collect(bmgr.toConjunction());
  }

  /**
   * Creates the formula (a1 or a2 or a3) and (b1 or b2) ... for the input [[a1,a2,a3],[b1,b2]]
   *
   * @param hardSet the current hard set
   * @return conjunction of the disjunction of the sets
   */
  private BooleanFormula hardSetFormula(Set<Fault> hardSet) {
    return hardSet.stream()
        .map(l -> l.stream().map(f -> ((Selector)f).getFormula()).collect(bmgr.toDisjunction()))
        .collect(bmgr.toConjunction());
  }

  @Override
  public void printStatistics(
      PrintStream out, Result result, UnmodifiableReachedSet reached) {
    StatisticsWriter w0 = StatisticsWriter.writingStatisticsTo(out);
    w0.put("Total time", totalTime).put("Total calls to solver", unsatCalls)
        .put("Total calls saved", savedCalls);
  }

  @Override
  public @Nullable String getName() {
    return "MAX-SAT algorithm";
  }
}
