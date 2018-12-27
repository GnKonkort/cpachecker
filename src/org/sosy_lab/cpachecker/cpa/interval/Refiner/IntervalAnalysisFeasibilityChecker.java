/*
 *  CPAchecker is a tool for configurable software verification.
 *  This file is part of CPAchecker.
 *
 *  Copyright (C) 2007-2018  Dirk Beyer
 *  All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 *  CPAchecker web page:
 *    http://cpachecker.sosy-lab.org
 */
package org.sosy_lab.cpachecker.cpa.interval.Refiner;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import org.sosy_lab.common.configuration.Configuration;
import org.sosy_lab.common.configuration.InvalidConfigurationException;
import org.sosy_lab.common.log.LogManager;
import org.sosy_lab.cpachecker.cfa.CFA;
import org.sosy_lab.cpachecker.cfa.model.CFAEdge;
import org.sosy_lab.cpachecker.core.interfaces.ConfigurableProgramAnalysis;
import org.sosy_lab.cpachecker.core.interfaces.Precision;
import org.sosy_lab.cpachecker.cpa.arg.path.ARGPath;
import org.sosy_lab.cpachecker.cpa.arg.path.PathIterator;
import org.sosy_lab.cpachecker.cpa.interval.Interval;
import org.sosy_lab.cpachecker.cpa.interval.IntervalAnalysisPrecision;
import org.sosy_lab.cpachecker.cpa.interval.IntervalAnalysisPrecision.IntervalAnalysisFullPrecision;
import org.sosy_lab.cpachecker.cpa.interval.IntervalAnalysisState;
import org.sosy_lab.cpachecker.exceptions.CPAException;
import org.sosy_lab.cpachecker.exceptions.CPATransferException;
import org.sosy_lab.cpachecker.util.AbstractStates;
import org.sosy_lab.cpachecker.util.Pair;
import org.sosy_lab.cpachecker.util.refinement.GenericFeasibilityChecker;
import org.sosy_lab.cpachecker.util.refinement.StrongestPostOperator;

public class IntervalAnalysisFeasibilityChecker
    extends GenericFeasibilityChecker<IntervalAnalysisState> {

  IntervalAnalysisPrecision precision;
  StrongestPostOperator<IntervalAnalysisState> strongestPostOperator;
  public IntervalAnalysisFeasibilityChecker(
      final StrongestPostOperator<IntervalAnalysisState> pStrongestPostOp,
      final IntervalAnalysisState pInitialState,
      final Class<? extends ConfigurableProgramAnalysis> pCpaToRefine,
      final LogManager pLogger,
      final Configuration pConfig,
      final CFA pCfa)
      throws InvalidConfigurationException {

    super(pStrongestPostOp, pInitialState, pCpaToRefine, pLogger, pConfig, pCfa);
    precision = new IntervalAnalysisFullPrecision();
    strongestPostOperator = pStrongestPostOp;
  }


  public void setPrecision(IntervalAnalysisPrecision pPrecision){
    precision = pPrecision;
  }

  public boolean isFeasible(
      ARGPath pPath, IntervalAnalysisPrecision precisionToUse, Set<String> usedVariables)
      throws InterruptedException, CPAException {

    IntervalAnalysisState next =
        AbstractStates.extractStateByType(pPath.getFirstState(), IntervalAnalysisState.class);
    Deque<IntervalAnalysisState> pCallstack = new ArrayDeque<>();
    PathIterator iterator = pPath.fullPathIterator();

    while (iterator.hasNext()) {
      final CFAEdge edge = iterator.getOutgoingEdge();
      Optional<IntervalAnalysisState> maybeNext =
          strongestPostOperator.step(next, edge, precisionToUse, pCallstack, pPath);
      if (!maybeNext.isPresent()) {
        return false;
      }
      next = maybeNext.get();
      usedVariables.addAll(next.getVariables());
      iterator.advance();
    }

    return true;
  }
}
