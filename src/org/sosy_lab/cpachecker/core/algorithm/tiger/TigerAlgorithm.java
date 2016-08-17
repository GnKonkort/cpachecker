/*
 *  CPAchecker is a tool for configurable software verification.
 *  This file is part of CPAchecker.
 *
 *  Copyright (C) 2007-2014  Dirk Beyer
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
package org.sosy_lab.cpachecker.core.algorithm.tiger;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import org.sosy_lab.common.ShutdownManager;
import org.sosy_lab.common.configuration.Configuration;
import org.sosy_lab.common.configuration.ConfigurationBuilder;
import org.sosy_lab.common.configuration.FileOption;
import org.sosy_lab.common.configuration.InvalidConfigurationException;
import org.sosy_lab.common.configuration.Option;
import org.sosy_lab.common.configuration.Options;
import org.sosy_lab.common.io.MoreFiles;
import org.sosy_lab.common.io.PathTemplate;
import org.sosy_lab.common.log.LogManager;
import org.sosy_lab.cpachecker.cfa.CFA;
import org.sosy_lab.cpachecker.cfa.WeavingLocation;
import org.sosy_lab.cpachecker.cfa.ast.AExpressionStatement;
import org.sosy_lab.cpachecker.cfa.ast.AStatement;
import org.sosy_lab.cpachecker.cfa.ast.c.CBinaryExpression;
import org.sosy_lab.cpachecker.cfa.model.CFAEdge;
import org.sosy_lab.cpachecker.core.CPAcheckerResult.Result;
import org.sosy_lab.cpachecker.core.CoreComponentsFactory;
import org.sosy_lab.cpachecker.core.MainCPAStatistics;
import org.sosy_lab.cpachecker.core.algorithm.Algorithm;
import org.sosy_lab.cpachecker.core.algorithm.AlgorithmResult;
import org.sosy_lab.cpachecker.core.algorithm.AlgorithmResult.CounterexampleInfoResult;
import org.sosy_lab.cpachecker.core.algorithm.AlgorithmWithResult;
import org.sosy_lab.cpachecker.core.algorithm.tgar.TGARAlgorithm;
import org.sosy_lab.cpachecker.core.algorithm.tgar.TGARStatistics;
import org.sosy_lab.cpachecker.core.algorithm.tgar.interfaces.TestificationOperator;
import org.sosy_lab.cpachecker.core.algorithm.tiger.fql.PredefinedCoverageCriteria;
import org.sosy_lab.cpachecker.core.algorithm.tiger.fql.ast.Edges;
import org.sosy_lab.cpachecker.core.algorithm.tiger.fql.ast.FQLSpecification;
import org.sosy_lab.cpachecker.core.algorithm.tiger.fql.ecp.SingletonECPEdgeSet;
import org.sosy_lab.cpachecker.core.algorithm.tiger.fql.ecp.translators.GuardedEdgeLabel;
import org.sosy_lab.cpachecker.core.algorithm.tiger.fql.ecp.translators.InverseGuardedEdgeLabel;
import org.sosy_lab.cpachecker.core.algorithm.tiger.fql.translators.ecp.CoverageSpecificationTranslator;
import org.sosy_lab.cpachecker.core.algorithm.tiger.goals.Goal;
import org.sosy_lab.cpachecker.core.algorithm.tiger.goals.clustering.ClusteredElementaryCoveragePattern;
import org.sosy_lab.cpachecker.core.algorithm.tiger.goals.clustering.InfeasibilityPropagation;
import org.sosy_lab.cpachecker.core.algorithm.tiger.goals.clustering.InfeasibilityPropagation.Prediction;
import org.sosy_lab.cpachecker.core.algorithm.tiger.util.PrecisionCallback;
import org.sosy_lab.cpachecker.core.algorithm.tiger.util.TestCase;
import org.sosy_lab.cpachecker.core.algorithm.tiger.util.TestGoalUtils;
import org.sosy_lab.cpachecker.core.algorithm.tiger.util.TestStep;
import org.sosy_lab.cpachecker.core.algorithm.tiger.util.TestStep.AssignmentType;
import org.sosy_lab.cpachecker.core.algorithm.tiger.util.TestStep.VariableAssignment;
import org.sosy_lab.cpachecker.core.algorithm.tiger.util.TestSuite;
import org.sosy_lab.cpachecker.core.algorithm.tiger.util.ThreeValuedAnswer;
import org.sosy_lab.cpachecker.core.algorithm.tiger.util.WorkerRunnable;
import org.sosy_lab.cpachecker.core.algorithm.tiger.util.WorklistEntryComparator;
import org.sosy_lab.cpachecker.core.algorithm.tiger.util.Wrapper;
import org.sosy_lab.cpachecker.core.counterexample.CFAEdgeWithAssumptions;
import org.sosy_lab.cpachecker.core.counterexample.CFAPathWithAssumptions;
import org.sosy_lab.cpachecker.core.counterexample.CounterexampleInfo;
import org.sosy_lab.cpachecker.core.interfaces.AbstractState;
import org.sosy_lab.cpachecker.core.interfaces.CPAFactory;
import org.sosy_lab.cpachecker.core.interfaces.ConfigurableProgramAnalysis;
import org.sosy_lab.cpachecker.core.interfaces.Precision;
import org.sosy_lab.cpachecker.core.interfaces.Property;
import org.sosy_lab.cpachecker.core.interfaces.StateSpacePartition;
import org.sosy_lab.cpachecker.core.interfaces.Statistics;
import org.sosy_lab.cpachecker.core.interfaces.StatisticsProvider;
import org.sosy_lab.cpachecker.core.reachedset.ReachedSet;
import org.sosy_lab.cpachecker.core.reachedset.ReachedSetFactory;
import org.sosy_lab.cpachecker.cpa.arg.ARGCPA;
import org.sosy_lab.cpachecker.cpa.arg.ARGState;
import org.sosy_lab.cpachecker.cpa.arg.ARGToDotWriter;
import org.sosy_lab.cpachecker.cpa.automaton.Automaton;
import org.sosy_lab.cpachecker.cpa.automaton.AutomatonBoolExpr;
import org.sosy_lab.cpachecker.cpa.automaton.AutomatonInternalState;
import org.sosy_lab.cpachecker.cpa.automaton.AutomatonPrecision;
import org.sosy_lab.cpachecker.cpa.automaton.AutomatonTransition;
import org.sosy_lab.cpachecker.cpa.automaton.ControlAutomatonCPA;
import org.sosy_lab.cpachecker.cpa.automaton.InvalidAutomatonException;
import org.sosy_lab.cpachecker.cpa.automaton.MarkingAutomatonBuilder;
import org.sosy_lab.cpachecker.cpa.automaton.PowersetAutomatonCPA;
import org.sosy_lab.cpachecker.cpa.automaton.ReducedAutomatonProduct;
import org.sosy_lab.cpachecker.cpa.automaton.SafetyProperty;
import org.sosy_lab.cpachecker.cpa.bdd.BDDCPA;
import org.sosy_lab.cpachecker.cpa.composite.CompositeCPA;
import org.sosy_lab.cpachecker.cpa.predicate.PredicateCPA;
import org.sosy_lab.cpachecker.cpa.predicate.PredicatePrecision;
import org.sosy_lab.cpachecker.exceptions.CPAException;
import org.sosy_lab.cpachecker.util.AbstractStates;
import org.sosy_lab.cpachecker.util.CPAs;
import org.sosy_lab.cpachecker.util.Pair;
import org.sosy_lab.cpachecker.util.automaton.NondeterministicFiniteAutomaton;
import org.sosy_lab.cpachecker.util.automaton.NondeterministicFiniteAutomaton.State;
import org.sosy_lab.cpachecker.util.globalinfo.GlobalInfo;
import org.sosy_lab.cpachecker.util.presence.ARGPathWithPresenceConditions;
import org.sosy_lab.cpachecker.util.presence.ARGPathWithPresenceConditions.ForwardPathIteratorWithPresenceConditions;
import org.sosy_lab.cpachecker.util.presence.PathReplayEngine;
import org.sosy_lab.cpachecker.util.presence.PresenceConditions;
import org.sosy_lab.cpachecker.util.presence.binary.BinaryPresenceConditionManager;
import org.sosy_lab.cpachecker.util.presence.formula.FormulaPresenceConditionManager;
import org.sosy_lab.cpachecker.util.presence.interfaces.PresenceCondition;
import org.sosy_lab.cpachecker.util.presence.interfaces.PresenceConditionManager;
import org.sosy_lab.cpachecker.util.presence.region.RegionPresenceConditionManager;
import org.sosy_lab.cpachecker.util.resources.ProcessCpuTime;
import org.sosy_lab.cpachecker.util.statistics.AbstractStatistics;
import org.sosy_lab.cpachecker.util.statistics.StatCpuTime;
import org.sosy_lab.cpachecker.util.statistics.StatCpuTime.NoTimeMeasurement;
import org.sosy_lab.cpachecker.util.statistics.StatCpuTime.StatCpuTimer;
import org.sosy_lab.solver.SolverException;
import org.sosy_lab.solver.api.BooleanFormula;

import javax.annotation.Nullable;
import javax.management.JMException;

import java.io.IOException;
import java.io.PrintStream;
import java.io.Writer;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.regex.Pattern;

public class TigerAlgorithm
    implements Algorithm, AlgorithmWithResult, PrecisionCallback<PredicatePrecision>,
    StatisticsProvider, Statistics {

  private class TigerStatistics extends AbstractStatistics {

    final StatCpuTime acceptsTime = new StatCpuTime();
    final StatCpuTime updateTestsuiteByCoverageOfTime = new StatCpuTime();
    final StatCpuTime createTestcaseTime = new StatCpuTime();
    final StatCpuTime addTestToSuiteTime = new StatCpuTime();
    final StatCpuTime handleInfeasibleTestGoalTime = new StatCpuTime();
    final StatCpuTime runAlgorithmWithLimitTime = new StatCpuTime();
    final StatCpuTime runAlgorithmTime = new StatCpuTime();
    final StatCpuTime initializeAlgorithmTime = new StatCpuTime();
    final StatCpuTime initializeReachedSetTime = new StatCpuTime();
    final StatCpuTime composeCPATime = new StatCpuTime();
    final StatCpuTime testGenerationTime = new StatCpuTime();

    public TigerStatistics() {
      super();
    }

    @Override
    public void printStatistics(PrintStream pOut, Result pResult, ReachedSet pReached) {
      super.printStatistics(pOut, pResult, pReached);
      pOut.append("Time for test generation " + testGenerationTime + "\n");
      pOut.append("  Time for composing the CPA " + composeCPATime + "\n");
      pOut.append("  Time for initializing the reached set " + initializeReachedSetTime + "\n");
      pOut.append("  Time for initializing the algorithm " + initializeAlgorithmTime + "\n");
      pOut.append("  Time for running the CPA algorithm " + runAlgorithmTime + "\n");
      pOut.append(
          "    Time for running the CPA algorithm with limit " + runAlgorithmWithLimitTime + "\n");
      pOut.append("    Time for handling infeasible goals " + handleInfeasibleTestGoalTime + "\n");
      pOut.append("    Time for adding a test to the suite " + addTestToSuiteTime + "\n");
      pOut.append("      Time for creating a test case " + createTestcaseTime + "\n");
      pOut.append(
          "      Time for updating the test coverage " + updateTestsuiteByCoverageOfTime + "\n");
      pOut.append("        Time for checking acceptance " + acceptsTime + "\n");
    }

  }

  private TigerStatistics tigerStats = new TigerStatistics();

  public static String originalMainFunction = null;

  private final Configuration config;
  private final TigerConfiguration cfg;
  private final LogManager logger;
  final private ShutdownManager mainShutdownManager;
  private final MainCPAStatistics mainStats;
  private final CFA cfa;

  private ConfigurableProgramAnalysis cpa;
  private TGARStatistics tgarStatistics;

  private CoverageSpecificationTranslator mCoverageSpecificationTranslator;
  private FQLSpecification fqlSpecification;

  private TestSuite testsuite;
  private ReachedSet reachedSet = null;
  private ReachedSet outsideReachedSet = null;
  private Set<String> inputVariables;
  private Set<String> outputVariables;

  private PredicatePrecision reusedPrecision = null;

  private int statistics_numberOfTestGoals;
  private int statistics_numberOfProcessedTestGoals = 0;
  private StatCpuTime statCpuTime = null;

  private Map<Automaton, Automaton> markingAutomataInstances = Maps.newHashMap();

  private Prediction[] lGoalPrediction;

  private String programDenotation;
  private int testCaseId = 0;

  private Map<Goal, List<List<BooleanFormula>>> targetStateFormulas;

  private TestGoalUtils testGoalUtils = null;
  private Map<CFAEdge, List<NondeterministicFiniteAutomaton<GuardedEdgeLabel>>> edgeToTgaMapping;

  private final ReachedSetFactory reachedSetFactory;

  public TigerAlgorithm(ConfigurableProgramAnalysis pCpa,
      ShutdownManager pShutdownManager, CFA pCfa, Configuration pConfig, LogManager pLogger,
      String pProgramDenotation, ReachedSetFactory pReachedSetFactory, MainCPAStatistics pMainStats)
      throws InvalidConfigurationException {

    reachedSetFactory = pReachedSetFactory;
    programDenotation = pProgramDenotation;
    statCpuTime = new StatCpuTime();
    mainStats = pMainStats;
    tgarStatistics = new TGARStatistics(pLogger);

    mainShutdownManager = pShutdownManager;
    logger = pLogger;

    config = pConfig;
    cfg = new TigerConfiguration(pConfig);
    cpa = pCpa;
    cfa = pCfa;

    testsuite = new TestSuite(cfg.printLabels, cfg.useTigerAlgorithm_with_pc);
    inputVariables = new TreeSet<>();
    for (String variable : cfg.inputInterface.split(",")) {
      inputVariables.add(variable.trim());
    }

    outputVariables = new TreeSet<>();
    for (String variable : cfg.outputInterface.split(",")) {
      outputVariables.add(variable.trim());
    }

    assert TigerAlgorithm.originalMainFunction != null;
    mCoverageSpecificationTranslator =
        new CoverageSpecificationTranslator(
            pCfa.getFunctionHead(TigerAlgorithm.originalMainFunction));

    Wrapper wrapper = new Wrapper(pCfa, TigerAlgorithm.originalMainFunction);

    GuardedEdgeLabel alphaLabel =
        new GuardedEdgeLabel(new SingletonECPEdgeSet(wrapper.getAlphaEdge()));
    InverseGuardedEdgeLabel inverseAlphaLabel = new InverseGuardedEdgeLabel(alphaLabel);
    GuardedEdgeLabel omegaLabel =
        new GuardedEdgeLabel(new SingletonECPEdgeSet(wrapper.getOmegaEdge()));

    edgeToTgaMapping = new HashMap<>();
    targetStateFormulas = new HashMap<>();

    testGoalUtils = new TestGoalUtils(logger, cfg.useTigerAlgorithm_with_pc, alphaLabel,
        inverseAlphaLabel, omegaLabel);

    // get internal representation of FQL query
    fqlSpecification = testGoalUtils.parseFQLQuery(cfg.fqlQuery);
  }

  private PresenceConditionManager pcm() {
    return PresenceConditions.manager();
  }

  @Override
  public String getName() {
    return "TigerAlgorithm";
  }

  @Override
  public void setPrecision(PredicatePrecision pNewPrec) {
    reusedPrecision = pNewPrec;
  }

  @Override
  public PredicatePrecision getPrecision() {
    return reusedPrecision;
  }

  public long getCpuTime() {
    long cpuTime = -1;
    try {
      long currentCpuTime = (long) (ProcessCpuTime.read() / 1e6);
      long currentWallTime = System.currentTimeMillis();
      statCpuTime.onMeasurementResult(currentCpuTime - statCpuTime.getCpuTimeSum().asMillis(),
          currentWallTime - statCpuTime.getWallTimeSumMsec());
      cpuTime = statCpuTime.getCpuTimeSum().asMillis();
    } catch (NoTimeMeasurement | JMException e) {
      logger.logUserException(Level.WARNING, e, "Could not get CPU time for statistics.");
    }

    return cpuTime;
  }

  @Override
  public AlgorithmResult getResult() {
    return testsuite;
  }

  @Override
  public AlgorithmStatus run(ReachedSet pReachedSet) throws CPAException, InterruptedException {
    // we empty pReachedSet to stop complaints of an incomplete analysis
    // Problem: pReachedSet does not match the internal CPA structure!
    logger.logf(Level.INFO,
        "We will not use the provided reached set since it violates the internal structure of Tiger's CPAs");
    logger.logf(Level.INFO, "We empty pReachedSet to stop complaints of an incomplete analysis");

    outsideReachedSet = pReachedSet;
    outsideReachedSet.clear();

    statCpuTime.start();
    testsuite.setGenerationStartTime(getCpuTime());

    // Optimization: Infeasibility propagation
    Pair<Boolean, LinkedList<Edges>> lInfeasibilityPropagation =
        initializeInfisabilityPropagation();

    Set<Goal> goalsToCover =
        testGoalUtils.extractTestGoalPatterns(fqlSpecification, lGoalPrediction,
            lInfeasibilityPropagation, mCoverageSpecificationTranslator, cfg.optimizeGoalAutomata,
            cfg.useOmegaLabel, cfg.useTigerAlgorithm_with_pc);
    fillEdgeToTgaMapping(goalsToCover);

    statistics_numberOfTestGoals = goalsToCover.size();
    logger.logf(Level.INFO, "Number of test goals: %d", statistics_numberOfTestGoals);

    // (iii) do test generation for test goals ...
    boolean wasSound = true;
    try {
      if (!testGeneration(goalsToCover, lInfeasibilityPropagation)) {
        logger.logf(Level.WARNING, "Test generation contained unsound reachability analysis runs!");
        wasSound = false;
      }
    } catch (InvalidConfigurationException e1) {
      throw new CPAException("Invalid configuration!", e1);
    }

    // TODO: change testGeneration() such that it returns timedout if there was a timeout
    //    assert (!testsuite.getTimedOutGoals().isEmpty() ? goalsToCover.isEmpty() : true);

    // Write generated test suite and mapping to file system
    dumpTestSuite();

    if (wasSound) {
      return AlgorithmStatus.SOUND_AND_PRECISE;
    } else {
      return AlgorithmStatus.UNSOUND_AND_PRECISE;
    }
  }

  private void fillEdgeToTgaMapping(Set<Goal> pGoalsToCover) {
    for (Goal goal : pGoalsToCover) {
      NondeterministicFiniteAutomaton<GuardedEdgeLabel> automaton = goal.getAutomaton();
      for (NondeterministicFiniteAutomaton<GuardedEdgeLabel>.Edge edge : automaton.getEdges()) {
        if (edge.getSource().equals(edge.getTarget())) {
          continue;
        }

        GuardedEdgeLabel label = edge.getLabel();
        for (CFAEdge e : label.getEdgeSet()) {
          List<NondeterministicFiniteAutomaton<GuardedEdgeLabel>> tgaSet = edgeToTgaMapping.get(e);

          if (tgaSet == null) {
            tgaSet = new ArrayList<>();
            edgeToTgaMapping.put(e, tgaSet);
          }

          tgaSet.add(automaton);
        }
      }
    }
  }

  private void dumpTestSuite() {
    if (cfg.testsuiteFile != null) {
      try (Writer writer = MoreFiles.openOutputFile(cfg.testsuiteFile, Charset.defaultCharset())) {
        writer.write(testsuite.toString());
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  private Pair<Boolean, LinkedList<Edges>> initializeInfisabilityPropagation() {
    Pair<Boolean, LinkedList<Edges>> propagation;

    if (cfg.useInfeasibilityPropagation) {
      propagation = InfeasibilityPropagation.canApplyInfeasibilityPropagation(fqlSpecification);
    } else {
      propagation = Pair.of(Boolean.FALSE, null);
    }

    return propagation;
  }

  private ImmutableSet<Goal> nextTestGoalSet(Set<Goal> pGoalsToCover) {
    final int testGoalSetSize = (cfg.numberOfTestGoalsPerRun <= 0)
        ? pGoalsToCover.size()
        : (pGoalsToCover.size() > cfg.numberOfTestGoalsPerRun) ? cfg.numberOfTestGoalsPerRun
            : pGoalsToCover.size();

    Builder<Goal> builder = ImmutableSet.builder();

    Iterator<Goal> it = pGoalsToCover.iterator();
    for (int i = 0; i < testGoalSetSize; i++) {
      if (it.hasNext()) {
        builder.add(it.next());
      }
    }

    return builder.build();
  }

  private boolean testGeneration(Set<Goal> pGoalsToCover, Pair<Boolean, LinkedList<Edges>> pInfeasibilityPropagation)
      throws CPAException, InterruptedException, InvalidConfigurationException {

    try (StatCpuTimer t = tigerStats.testGenerationTime.start()) {
      boolean wasSound = true;
      int numberOfTestGoals = pGoalsToCover.size();
      testsuite.addGoals(pGoalsToCover);

      NondeterministicFiniteAutomaton<GuardedEdgeLabel> previousAutomaton = null;
      boolean retry = false;

      do {
        if (retry) {
          // retry timed-out goals
          boolean order = true;

          if (cfg.timeoutIncrement > 0) {
            long oldCPUTimeLimitPerGoal = cfg.cpuTimelimitPerGoal;
            cfg.cpuTimelimitPerGoal += cfg.timeoutIncrement;
            logger.logf(Level.INFO, "Incremented timeout from %d to %d seconds.",
                oldCPUTimeLimitPerGoal,
                cfg.cpuTimelimitPerGoal);
            Collection<Entry<Integer, Pair<Goal, PresenceCondition>>> set;
            if (cfg.useOrder) {
              if (cfg.inverseOrder) {
                order = !order;
              }

              // keep original order of goals (or inverse of it)
              if (order) {
                set = new TreeSet<>(WorklistEntryComparator.ORDER_RESPECTING_COMPARATOR);
              } else {
                set = new TreeSet<>(WorklistEntryComparator.ORDER_INVERTING_COMPARATOR);
              }

              set.addAll(testsuite.getTimedOutGoals().entrySet());
            } else {
              set = new LinkedList<>();
              set.addAll(testsuite.getTimedOutGoals().entrySet());
            }

            pGoalsToCover.clear();
            for (Entry<Integer, Pair<Goal, PresenceCondition>> entry : set) {
              pGoalsToCover.add(entry.getValue().getFirst());
            }

            statistics_numberOfProcessedTestGoals -= testsuite.getTimedOutGoals().size();
            testsuite.prepareForRetryAfterTimeout();
          }
        }

        while (!pGoalsToCover.isEmpty()) {
          Set<Goal> goalsToBeProcessed = nextTestGoalSet(pGoalsToCover);
          statistics_numberOfProcessedTestGoals += goalsToBeProcessed.size();
          pGoalsToCover.removeAll(goalsToBeProcessed);

          if (cfg.useTigerAlgorithm_with_pc) {
            /* force that a new reachedSet is computed when first starting on a new TestGoal with initial PC TRUE.
             * This enforces that no very constrained ARG is reused when computing a new ARG for a new testgoal with broad pc (TRUE).
             * This strategy allows us to set option tiger.reuseARG=true such that ARG is reused in testgoals (pcs get only more specific).
             * Keyword: overapproximation
             */
            //assert false;
            reachedSet = null;
          }

          String logString = "Processing test goals ";
          for (Goal g : goalsToBeProcessed) {
            logString += g.getIndex() + " (" + testsuite.getTestGoalLabel(g) + "), ";
          }
          logString = logString.substring(0, logString.length() - 2);

          if (cfg.useTigerAlgorithm_with_pc) {
            //            Region remainingPresenceCondition =
            //                BDDUtils.composeRemainingPresenceConditions(goalsToBeProcessed, testsuite, bddCpaNamedRegionManager);
            logger.logf(Level.FINE, "%s of %d for a PC.", logString, numberOfTestGoals);
          } else {
            logger.logf(Level.FINE, "%s of %d.", logString, numberOfTestGoals);
          }

          // TODO: enable tiger techniques for multi-goal generation in one run
          //        if (lGoalPrediction != null && lGoalPrediction[goal.getIndex() - 1] == Prediction.INFEASIBLE) {
          //          // GoalPrediction does not use the target presence condition (remainingPCforGoalCoverage)
          //          // I think this is OK (any infeasible goal will be even more infeasible when restricted with a certain pc)
          //          // TODO: remainingPCforGoalCoverage could perhaps be used to improve precision of the prediction?
          //          logger.logf(Level.INFO, "This goal is predicted as infeasible!");
          //          testsuite.addInfeasibleGoal(goal, goal.getRemainingPresenceCondition(), lGoalPrediction);
          //          continue;
          //        }
          //
          //        NondeterministicFiniteAutomaton<GuardedEdgeLabel> currentAutomaton = goal.getAutomaton();
          //        if (ARTReuse.isDegeneratedAutomaton(currentAutomaton)) {
          //          // current goal is for sure infeasible
          //          logger.logf(Level.INFO, "Test goal infeasible.");
          //          if (useTigerAlgorithm_with_pc) {
          //            logger.logf(Level.WARNING, "Goal %d is infeasible for remaining PC %s !", goal.getIndex(),
          //                bddCpaNamedRegionManager.dumpRegion(goal.getInfeasiblePresenceCondition()));
          //          }
          //          testsuite.addInfeasibleGoal(goal, goal.getRemainingPresenceCondition(), lGoalPrediction);
          //          continue; // we do not want to modify the ARG for the degenerated automaton to keep more reachability information
          //        }
          //
          //          if (checkCoverage) {
          //            for (Goal goalToBeChecked : goalsToBeProcessed) {
          //              if (checkAndCoverGoal(goalToBeChecked)) {
          //                if (useTigerAlgorithm_with_pc) {
          //                  pGoalsToCover.remove(goalToBeChecked);
          //                }
          //                if (lGoalPrediction != null) {
          //                  lGoalPrediction[goalToBeChecked.getIndex() - 1] = Prediction.FEASIBLE;
          //                }
          //              }
          //            }
          //          }

          //          if (testsuite.areGoalsCoveredOrInfeasible(goalsToBeProcessed)) {
          //            continue;
          //          }

          // goal is uncovered so far; run CPAchecker to cover it
          ReachabilityAnalysisResult result =
              runReachabilityAnalysis(pGoalsToCover, goalsToBeProcessed,
                  pInfeasibilityPropagation);
          if (result.equals(ReachabilityAnalysisResult.UNSOUND)) {
            logger.logf(Level.WARNING, "Analysis run was unsound!");
            wasSound = false;
          }
          //        previousAutomaton = currentAutomaton;

        }

        // reprocess timed-out goals
        if (testsuite.getTimedOutGoals().isEmpty()) {
          logger.logf(Level.INFO, "There were no timed out goals.");
          retry = false;
        } else {
          if (!cfg.timeoutStrategy.equals(TigerConfiguration.TimeoutStrategy.RETRY_AFTER_TIMEOUT)) {
            logger.logf(Level.INFO,
                "There were timed out goals but retry after timeout strategy is disabled.");
          } else {
            retry = true;
          }
        }
      } while (retry);

      return wasSound;
    }
  }

  @Nullable
  private Pair<ARGState, PresenceCondition> findStateAfterCriticalEdge(Goal pCriticalForGoal, ARGPathWithPresenceConditions pPath) {
    ForwardPathIteratorWithPresenceConditions it = pPath.iteratorWithPresenceConditions();

    final CFAEdge criticalEdge = pCriticalForGoal.getCriticalEdge();

    while (it.hasNext()) {
      if (it.getOutgoingEdge().equals(criticalEdge)) {
        ARGState afterCritical = it.getNextAbstractState();
        PresenceCondition afterCriticalPc = it.getPresenceCondition();
        while (it.hasNext() && AbstractStates
            .extractLocation(it.getNextAbstractState()) instanceof WeavingLocation) {
          it.advance();
          afterCritical = it.getNextAbstractState();
          afterCriticalPc = it.getPresenceCondition();
          Preconditions.checkState(afterCritical != null);
        }
        return Pair.of(afterCritical, afterCriticalPc);
      }
      it.advance();
    }

    return null;
  }

  private Set<Goal> updateTestsuiteByCoverageOf(
      TestCase pTestcase,
      ARGPathWithPresenceConditions pArgPath,
      Set<Goal> pCheckCoverageOf)
      throws InterruptedException {

    try (StatCpuTimer t = tigerStats.updateTestsuiteByCoverageOfTime.start()) {
      Set<Goal> checkCoverageOf = new HashSet<>();
      checkCoverageOf.addAll(pCheckCoverageOf);

      Set<Goal> coveredGoals = Sets.newLinkedHashSet();
      Set<Goal> goalsCoveredByLastState = Sets.newLinkedHashSet();

      ARGState lastState = pTestcase.getArgPath().getLastState();

//      if (printPathFormulasPerGoal) {
//        try {
//          List<BooleanFormula> formulas = getPathFormula(pTestcase.getArgPath());
//
//          Set<Property> violatedProperties = lastState.getViolatedProperties();
//
//          for (Property property : violatedProperties) {
//            Preconditions.checkState(property instanceof Goal);
//            Goal g = (Goal) property;
//            List<List<BooleanFormula>> f = targetStateFormulas.get(g);
//            if (f == null) {
//              f = new ArrayList<>();
//              targetStateFormulas.put(g, f);
//            }
//
//            f.add(formulas);
//          }
//        } catch (CPAException | InterruptedException e) {
//
//        }
//
//        return new HashSet<>();
//      }

      for (Property p : lastState.getViolatedProperties()) {
        Preconditions.checkState(p instanceof Goal);
        goalsCoveredByLastState.add((Goal) p);
      }

      checkCoverageOf.removeAll(goalsCoveredByLastState);

      if (!cfg.allCoveredGoalsPerTestCase) {
        for (Goal goal : pCheckCoverageOf) {
          if (testsuite.isGoalCovered(goal)) {
            checkCoverageOf.remove(goal);
          }
        }
      }

      Map<NondeterministicFiniteAutomaton<GuardedEdgeLabel>, AcceptStatus> acceptStati =
          accepts(checkCoverageOf, pTestcase.getErrorPath());

      for (Goal goal : goalsCoveredByLastState) {
        AcceptStatus acceptStatus = new AcceptStatus(goal);
        acceptStatus.answer = ThreeValuedAnswer.ACCEPT;
        acceptStati.put(goal.getAutomaton(), acceptStatus);
      }

      for (NondeterministicFiniteAutomaton<GuardedEdgeLabel> automaton : acceptStati.keySet()) {
        AcceptStatus acceptStatus = acceptStati.get(automaton);
        Goal goal = acceptStatus.goal;

        if (acceptStatus.answer.equals(ThreeValuedAnswer.UNKNOWN)) {
          logger.logf(Level.WARNING,
              "Coverage check for goal %d could not be performed in a precise way!",
              goal.getIndex());
          continue;
        } else if (acceptStatus.answer.equals(ThreeValuedAnswer.REJECT)) {
          continue;
        }

        // test goal is already covered by an existing test case
        if (cfg.useTigerAlgorithm_with_pc) {

          Pair<ARGState, PresenceCondition> critical = findStateAfterCriticalEdge(goal, pArgPath);

          if (critical == null) {
            Path argFile = Paths.get("output",
                "ARG_goal_criticalIsNull_" + Integer.toString(goal.getIndex()) + ".dot");

            final Set<Pair<ARGState, ARGState>> allTargetPathEdges = Sets.newLinkedHashSet();
            allTargetPathEdges.addAll(pTestcase.getArgPath().getStatePairs());

            try (Writer w = MoreFiles.openOutputFile(argFile, Charset.defaultCharset())) {
              ARGToDotWriter.write(w, (ARGState) reachedSet.getFirstState(),
                  reachedSet::getPrecision, ARGState::getChildren,
                  Predicates.alwaysTrue(),
                  Predicates.in(allTargetPathEdges));
            } catch (IOException e) {
              logger.logUserException(Level.WARNING, e, "Could not write ARG to file");
            }

            throw new RuntimeException(String.format(
                "Each ARG path of a counterexample must be along a critical edge! Goal %d has "
                    + "none for edge '%s'",
                goal.getIndex(), goal.getCriticalEdge().toString()));
          }

          Preconditions.checkState(critical.getFirst() != null,
              "Each ARG path of a counterexample must be along a critical edge!");

          PresenceCondition statePresenceCondition = critical.getSecond();

          Preconditions.checkState(statePresenceCondition != null,
              "Each critical state must be annotated with a presence condition!");

          if (cfg.allCoveredGoalsPerTestCase
              || pcm().checkConjunction(testsuite.getRemainingPresenceCondition(goal),
                  statePresenceCondition)) {

            // configurations in testGoalPCtoCover and testcase.pc have a non-empty intersection
            testsuite.addTestCase(pTestcase, goal, statePresenceCondition);

//            logger.logf(Level.WARNING,
//                "Covered some PCs for Goal %d (%s) for a PC %s by test case %d!",
//                goal.getIndex(), testsuite.getTestGoalLabel(goal), PresenceConditions.dump(statePresenceCondition), pTestcase.getId());

            logger.logf(Level.FINE,
                "Covered some PCs for Goal %d (%s) for a PC by test case %d!",
                goal.getIndex(), testsuite.getTestGoalLabel(goal), pTestcase.getId());

            if (pcm().checkEqualsFalse(testsuite.getRemainingPresenceCondition(goal))) {
              coveredGoals.add(goal);
            }
          }

        } else {
          testsuite.addTestCase(pTestcase, goal, null);
          logger.logf(Level.FINE, "Covered Goal %d (%s) by test case %d!",
              goal.getIndex(),
              testsuite.getTestGoalLabel(goal),
              pTestcase.getId());
          coveredGoals.add(goal);
        }
      }

      return coveredGoals;
    }
  }

//  private List<BooleanFormula> getPathFormula(ARGPath pPath)
//      throws CPAException, InterruptedException {
//    List<BooleanFormula> formulas = null;
//
//    Refiner refiner = this.refiner;
//
//    if (refiner instanceof PredicateCPARefiner) {
//      final List<ARGState> abstractionStatesTrace =
//          PredicateCPARefiner.filterAbstractionStates(pPath);
//      formulas =
//          ((PredicateCPARefiner) refiner).createFormulasOnPath(pPath, abstractionStatesTrace);
//    }
//
//    return formulas;
//  }

  private class AcceptStatus {

    private Goal goal;
    private NondeterministicFiniteAutomaton<GuardedEdgeLabel> automaton;
    private Set<NondeterministicFiniteAutomaton.State> currentStates;
    boolean hasPredicates;
    private ThreeValuedAnswer answer;

    public AcceptStatus(Goal pGoal) {
      goal = pGoal;
      automaton = pGoal.getAutomaton();
      currentStates = Sets.newLinkedHashSet();
      hasPredicates = false;

      currentStates.add(automaton.getInitialState());
    }

    @Override
    public String toString() {
      return goal.getName() + ": " + answer;
    }

  }

  private Map<NondeterministicFiniteAutomaton<GuardedEdgeLabel>, AcceptStatus> accepts(
      Collection<Goal> pGoals, List<CFAEdge> pErrorPath) {
    try (StatCpuTimer t = tigerStats.acceptsTime.start()) {

      Map<NondeterministicFiniteAutomaton<GuardedEdgeLabel>, AcceptStatus> map = new HashMap<>();
      Set<NondeterministicFiniteAutomaton.State> lNextStates = Sets.newLinkedHashSet();

      Set<NondeterministicFiniteAutomaton<GuardedEdgeLabel>> automataWithResult = new HashSet<>();

      for (Goal goal : pGoals) {
        AcceptStatus acceptStatus = new AcceptStatus(goal);
        map.put(goal.getAutomaton(), acceptStatus);
        if (acceptStatus.automaton.getFinalStates()
            .contains(acceptStatus.automaton.getInitialState())) {
          acceptStatus.answer = ThreeValuedAnswer.ACCEPT;
          automataWithResult.add(acceptStatus.automaton);
        }
      }

      for (CFAEdge lCFAEdge : pErrorPath) {
        List<NondeterministicFiniteAutomaton<GuardedEdgeLabel>> automata =
            edgeToTgaMapping.get(lCFAEdge);
        if (automata == null) {
          continue;
        }

        for (NondeterministicFiniteAutomaton<GuardedEdgeLabel> automaton : automata) {
          if (automataWithResult.contains(automaton)) {
            continue;
          }

          AcceptStatus acceptStatus = map.get(automaton);
          if (acceptStatus == null) {
            continue;
          }
          for (NondeterministicFiniteAutomaton.State lCurrentState : acceptStatus.currentStates) {
            for (NondeterministicFiniteAutomaton<GuardedEdgeLabel>.Edge lOutgoingEdge : automaton
                .getOutgoingEdges(lCurrentState)) {
              GuardedEdgeLabel lLabel = lOutgoingEdge.getLabel();

              if (lLabel.hasGuards()) {
                acceptStatus.hasPredicates = true;
              } else {
                if (lLabel.contains(lCFAEdge)) {
                  lNextStates.add(lOutgoingEdge.getTarget());
                  lNextStates.addAll(
                      getSuccsessorsOfEmptyTransitions(automaton, lOutgoingEdge.getTarget()));

                  for (State nextState : lNextStates) {
                    // Automaton accepts as soon as it sees a final state (implicit self-loop)
                    if (automaton.getFinalStates().contains(nextState)) {
                      acceptStatus.answer = ThreeValuedAnswer.ACCEPT;
                      automataWithResult.add(automaton);
                    }
                  }
                }
              }
            }
          }

          acceptStatus.currentStates.addAll(lNextStates);
          lNextStates.clear();
        }
      }

      for (NondeterministicFiniteAutomaton<GuardedEdgeLabel> autom : map.keySet()) {
        if (automataWithResult.contains(autom)) {
          continue;
        }

        AcceptStatus accepts = map.get(autom);
        if (accepts.hasPredicates) {
          accepts.answer = ThreeValuedAnswer.UNKNOWN;
        } else {
          accepts.answer = ThreeValuedAnswer.REJECT;
        }
      }

      return map;
    }
  }

  private static Collection<? extends State> getSuccsessorsOfEmptyTransitions(
      NondeterministicFiniteAutomaton<GuardedEdgeLabel> pAutomaton, State pState) {
    Set<State> states = new HashSet<>();
    for (NondeterministicFiniteAutomaton<GuardedEdgeLabel>.Edge edge : pAutomaton
        .getOutgoingEdges(pState)) {
      GuardedEdgeLabel label = edge.getLabel();
      if (Pattern.matches("E\\d+ \\[\\]", label.toString())) {
        states.add(edge.getTarget());
      }
    }
    return states;
  }

  enum ReachabilityAnalysisResult {
    SOUND,
    UNSOUND,
    TIMEOUT
  }

  private PresenceConditionManager createPresenceConditionManager(ConfigurableProgramAnalysis pCpa) {

    PredicateCPA predCpa = CPAs.retrieveCPA(pCpa, PredicateCPA.class);
    BDDCPA bddCpa = CPAs.retrieveCPA(pCpa, BDDCPA.class);

    if (predCpa != null) {
      return new FormulaPresenceConditionManager(predCpa.getPathFormulaManager(), predCpa.getSolver());
    } else if (bddCpa != null) {
      return new RegionPresenceConditionManager(bddCpa.getManager());
    } else {
      return new BinaryPresenceConditionManager();
    }
  }

  private ReachabilityAnalysisResult runReachabilityAnalysis(
      Set<Goal> pUncoveredGoals,
      Set<Goal> pTestGoalsToBeProcessed,
      Pair<Boolean, LinkedList<Edges>> pInfeasibilityPropagation)
      throws CPAException, InterruptedException, InvalidConfigurationException {

    ARGCPA cpa = composeCPA(pTestGoalsToBeProcessed, false);
    PresenceConditionManager pcm = createPresenceConditionManager(cpa);
    GlobalInfo.getInstance().setUpInfoFromCPA(cpa, pcm);

    Preconditions.checkState(cpa.getWrappedCPAs().get(0) instanceof CompositeCPA,
        "CPAcheckers automata should be used! The assumption is that the first component is the automata for the current goal!");

    // TODO: enable tiger techniques for multi-goal generation in one run
    //    if (reuseARG && (reachedSet != null)) {
    //      reuseARG(pTestGoalsToBeProcessed, pPreviousGoalAutomaton, lARTCPA);
    //    } else {
    initializeReachedSet(cpa);
    //    }

    PresenceCondition presenceConditionToCover =
        PresenceConditions.composeRemainingPresenceConditions(
            pTestGoalsToBeProcessed, testsuite);

    ShutdownManager shutdownManager =
        ShutdownManager.createWithParent(mainShutdownManager.getNotifier());
    Algorithm algorithm = initializeAlgorithm(cpa, shutdownManager);

    Preconditions.checkState(algorithm instanceof TGARAlgorithm);
    TGARAlgorithm tgarAlgorithm = (TGARAlgorithm) algorithm;

    return runAlgorithm(pUncoveredGoals, pTestGoalsToBeProcessed, cpa, pInfeasibilityPropagation,
        presenceConditionToCover, shutdownManager, tgarAlgorithm);
  }

  private ReachabilityAnalysisResult runAlgorithm(
      Set<Goal> pUncoveredGoals,
      final Set<Goal> pTestGoalsToBeProcessed,
      final ARGCPA pARTCPA, Pair<Boolean, LinkedList<Edges>> pInfeasibilityPropagation,
      final PresenceCondition pRemainingPresenceCondition,
      final ShutdownManager pShutdownNotifier,
      final TGARAlgorithm pAlgorithm)
      throws CPAException, InterruptedException {
    try (StatCpuTimer t = tigerStats.runAlgorithmTime.start()) {

      ReachabilityAnalysisResult algorithmStatus;

      do {
        final TestificationOperator testifier = new TestificationOperator() {
          @Override
          public void feasibleCounterexample(
              CounterexampleInfo pCounterexample, Set<SafetyProperty> pForProperties)
              throws InterruptedException {

            for (CounterexampleInfo cexi: pCounterexample.getAll()) {
              dumpArgForCex(cexi);

              final Set<Goal> fullyCoveredGoals;
              if (cfg.allCoveredGoalsPerTestCase) {
                fullyCoveredGoals =
                    addTestToSuite(testsuite.getGoals(), cexi, pInfeasibilityPropagation);
              } else if (cfg.checkCoverage) {
                fullyCoveredGoals =
                    addTestToSuite(Sets.union(pUncoveredGoals, pTestGoalsToBeProcessed), cexi,
                        pInfeasibilityPropagation);
              } else {
                fullyCoveredGoals =
                    addTestToSuite(pTestGoalsToBeProcessed, cexi, pInfeasibilityPropagation);
              }

              pUncoveredGoals.removeAll(fullyCoveredGoals);
            }

            // Exclude covered goals from further exploration
            Map<SafetyProperty, Optional<PresenceCondition>> toBlacklist = Maps.newHashMap();
            for (Goal goal : pTestGoalsToBeProcessed) {

              if (testsuite.isGoalCoveredOrInfeasible(goal)) {
                toBlacklist.put(goal, Optional.of(pcm().makeTrue()));
              } else if (cfg.useTigerAlgorithm_with_pc) {
                PresenceCondition remainingPc = testsuite.getRemainingPresenceCondition(goal);
                PresenceCondition coveredFor = pcm().makeNegation(remainingPc);
                toBlacklist.put(goal, Optional.of(coveredFor));
              }
            }

            AutomatonPrecision.updateGlobalPrecision(AutomatonPrecision.getGlobalPrecision()
                .cloneAndAddBlacklisted(toBlacklist));

          }
        };

        pAlgorithm.setTestificationOp(testifier);

        algorithmStatus = runAlgorithmWithLimit(pShutdownNotifier, pAlgorithm, pTestGoalsToBeProcessed.size());

      } while ((reachedSet.hasWaitingState()
          && !testsuite.areGoalsCoveredOrInfeasible(pTestGoalsToBeProcessed))
          && (algorithmStatus != ReachabilityAnalysisResult.TIMEOUT));

      if (algorithmStatus == ReachabilityAnalysisResult.TIMEOUT) {
        logger.logf(Level.FINE, "Test goal timed out!");
        testsuite.addTimedOutGoals(pTestGoalsToBeProcessed);
      } else {
        // set test goals infeasible
        for (Goal goal : pTestGoalsToBeProcessed) {
          if (!testsuite.isGoalCovered(goal)) {
            handleInfeasibleTestGoal(goal, pInfeasibilityPropagation);
          }
        }
      }

      return algorithmStatus;
    }
  }

  private static Optional<CounterexampleInfo> retrieveCounterexampleInfo(
      Algorithm pAlg, ReachedSet pReachedSet) {

    if (pAlg instanceof TGARAlgorithm) {
      TGARAlgorithm alg = (TGARAlgorithm) pAlg;
      CounterexampleInfoResult r = (CounterexampleInfoResult) alg.getResult();
      return r.getCounterexampleInfo();
    } else {
      boolean newTargetFound = (pReachedSet.getLastState() != null)
          && ((ARGState) pReachedSet.getLastState()).isTarget();

      if (pReachedSet.hasWaitingState() && newTargetFound) {
        Preconditions.checkState(pReachedSet.getLastState() instanceof ARGState);
        ARGState lastState = (ARGState) pReachedSet.getLastState();
        Preconditions.checkState(lastState.isTarget());

        return lastState.getCounterexampleInformation();
      }
    }

    return Optional.absent();
  }

  private void dumpArgForCex(CounterexampleInfo cexi) {
    //    Path argFile = Paths.get("output", "ARG_goal_" + Integer.toString(partitionId)  + ".dot");
    //    try (Writer w = Files.openOutputFile(argFile)) {
    //      final Set<Pair<ARGState, ARGState>> allTargetPathEdges = new HashSet<>();
    //      allTargetPathEdges.addAll(cexi.getTargetPath().getStatePairs());
    //
    //      ARGToDotWriter.write(w, AbstractStates.extractStateByType(reachedSet.getFirstState(), ARGState.class),
    //          ARGUtils.CHILDREN_OF_STATE,
    //          Predicates.alwaysTrue(),
    //          Predicates.in(allTargetPathEdges));
    //    } catch (IOException e) {
    //      logger.logUserException(Level.WARNING, e, "Could not write ARG to file");
    //    }
  }

  private ReachabilityAnalysisResult runAlgorithmWithLimit(final ShutdownManager algNotifier,
      final Algorithm algorithm, int numberOfGoals)
    throws CPAException, InterruptedException {

    try (StatCpuTimer t = tigerStats.runAlgorithmWithLimitTime.start()) {
      ReachabilityAnalysisResult status;
      if (cfg.cpuTimelimitPerGoal < 0) {
        // run algorithm without time limit
        if (algorithm.run(reachedSet).isSound()) {
          status = ReachabilityAnalysisResult.SOUND;
        } else {
          status = ReachabilityAnalysisResult.UNSOUND;
        }
      } else {

        long timeout = cfg.cpuTimelimitPerGoal;
        // calculate the timeout
        if (cfg.useDynamicTimeouts) {
          if (cfg.numberOfTestGoalsPerRun <= 0) {
            timeout = statistics_numberOfTestGoals * cfg.cpuTimelimitPerGoal;
          } else {
            timeout = numberOfGoals * cfg.cpuTimelimitPerGoal;
          }
        }

        // run algorithm with time limit
        WorkerRunnable workerRunnable =
            new WorkerRunnable(algorithm, reachedSet, timeout, algNotifier);

        Thread workerThread = new Thread(workerRunnable);

        workerThread.start();
        workerThread.join();

        if (workerRunnable.throwableWasCaught()) {
          // TODO: handle exception
          status = ReachabilityAnalysisResult.UNSOUND;
          //        throw new RuntimeException(workerRunnable.getCaughtThrowable());
        } else {
          if (workerRunnable.analysisWasSound()) {
            status = ReachabilityAnalysisResult.SOUND;
          } else {
            status = ReachabilityAnalysisResult.UNSOUND;
          }

          if (workerRunnable.hasTimeout()) {
            status = ReachabilityAnalysisResult.TIMEOUT;
          }
        }
      }
      return status;
    }
  }

  private Algorithm initializeAlgorithm(ARGCPA lARTCPA, ShutdownManager algNotifier) throws CPAException {

    try (StatCpuTimer t = tigerStats.initializeAlgorithmTime.start()) {
      Algorithm algorithm;
      try {
        Configuration internalConfiguration =
            Configuration.builder().loadFromFile(cfg.algorithmConfigurationFile).build();

        CoreComponentsFactory coreFactory =
            new CoreComponentsFactory(internalConfiguration, logger, algNotifier.getNotifier());

        algorithm = coreFactory.createAlgorithm(lARTCPA, programDenotation, cfa, mainStats);

        Preconditions.checkState(algorithm instanceof TGARAlgorithm, "Only TGAR supported!");
        TGARAlgorithm tgar = (TGARAlgorithm) algorithm;
        tgar.setStats(tgarStatistics);

//        if (algorithm instanceof TGARAlgorithm) {
//          TGARAlgorithm tgarAlg = (TGARAlgorithm) algorithm;
//
//          this.refiner = tgarAlg.getRefiner();
//        }

      } catch (IOException | InvalidConfigurationException e) {
        throw new RuntimeException(e);
      }
      return algorithm;
    }
  }

  private void initializeReachedSet(ARGCPA pArgCPA) {
    try (StatCpuTimer t = tigerStats.initializeReachedSetTime.start()) {
      // Create a new set 'reached' using the responsible factory.
      if (reachedSet != null) {
        reachedSet.clear();
      }
      reachedSet = reachedSetFactory.create();

      AbstractState initialState =
          pArgCPA.getInitialState(cfa.getMainFunction(), StateSpacePartition.getDefaultPartition());
      Precision initialPrec = pArgCPA.getInitialPrecision(cfa.getMainFunction(),
          StateSpacePartition.getDefaultPartition());

      reachedSet.add(initialState, initialPrec);
      outsideReachedSet.add(initialState, initialPrec);
    }
  }

  /**
   * Context:
   *  The analysis has identified a feasible counterexample, i.e., a test case.
   *
   * Add the test case to the test suite. This includes:
   *  * Register the test case for the goals that it reached on its last abstract state.
   *  * Add the test case for the goals that it would also cover;
   *    this gets checked by running all (uncovered) goal automata on the ARG path of the test case.
   *
   * @param pRemainingGoals
   * @param pCex
   * @param pInfeasibilityPropagation
   * @throws InterruptedException
   * @throws SolverException
   */
  private Set<Goal> addTestToSuite(Set<Goal> pRemainingGoals,
      CounterexampleInfo pCex, Pair<Boolean, LinkedList<Edges>> pInfeasibilityPropagation)
      throws InterruptedException {

    try (StatCpuTimer t = tigerStats.addTestToSuiteTime.start()) {

      Preconditions.checkNotNull(pInfeasibilityPropagation);
      Preconditions.checkNotNull(pRemainingGoals);
      Preconditions.checkNotNull(pCex);

      ARGPathWithPresenceConditions argPath = computePathWithPresenceConditions(pRemainingGoals, pCex);

      // TODO check whether a last state might remain from an earlier run and a reuse of the ARG

      PresenceCondition testCasePresenceCondition = argPath.getLastPresenceCondition();
      TestCase testcase = createTestcase(pCex, testCasePresenceCondition);

      return updateTestsuiteByCoverageOf(testcase, argPath, pRemainingGoals);
    }
  }

  private ARGPathWithPresenceConditions computePathWithPresenceConditions(Set<Goal> goals, CounterexampleInfo pCex)
      throws InterruptedException {

    try {
      PathReplayEngine replayer = new PathReplayEngine(logger);
      return replayer.replayPath(pCex.getTargetPath());
    } catch (CPAException e) {
      throw new RuntimeException("CPA for handling features could not be created.");
    }
  }

  private TestCase createTestcase(final CounterexampleInfo pCex,
      final PresenceCondition pPresenceCondition) {
    try (StatCpuTimer t = tigerStats.createTestcaseTime.start()) {

      CFAPathWithAssumptions model = pCex.getCFAPathWithAssignments();
      final List<TestStep> testSteps = calculateTestSteps(model);


      TestCase testcase = new TestCase(testCaseId++,
          testSteps,
          pCex.getTargetPath(),
          pCex.getTargetPath().getInnerEdges(),
          pPresenceCondition,
          pcm(),
          getCpuTime());

      Set<Property> props = pCex.getTargetPath().getLastState().getViolatedProperties();

      if (cfg.useTigerAlgorithm_with_pc) {
        logger.logf(Level.FINE, "Generated new test case %d for %s with a PC in the last state.",
            testcase.getId(), props);
      } else {
        logger.logf(Level.FINE, "Generated new test case %d for %s.", testcase.getId(), props);
      }

      return testcase;
    }
  }

  private List<TestStep> calculateTestSteps(CFAPathWithAssumptions path) {
    List<TestStep> testSteps = new ArrayList<>();

    boolean lastValueWasOuput = true;
    TestStep curStep = null;

    for (CFAEdgeWithAssumptions edge : path) {
      Collection<AExpressionStatement> expStmts = edge.getExpStmts();
      for (AExpressionStatement expStmt : expStmts) {
        if (expStmt.getExpression() instanceof CBinaryExpression) {
          CBinaryExpression exp = (CBinaryExpression) expStmt.getExpression();

          if (inputVariables.contains(exp.getOperand1().toString())) {
            if (lastValueWasOuput) {
              if (curStep != null) {
                testSteps.add(curStep);
              }
              curStep = new TestStep();
            }

            String variableName = exp.getOperand1().toString();
            BigInteger value = new BigInteger(exp.getOperand2().toString());
            VariableAssignment input =
                new VariableAssignment(variableName, value, AssignmentType.INPUT);
            curStep.addAssignment(input);

            lastValueWasOuput = false;
          } else if (outputVariables.contains(exp.getOperand1().toString())) {
            if (curStep == null) {
              curStep = new TestStep();
            }

            String variableName = exp.getOperand1().toString();
            BigInteger value = new BigInteger(exp.getOperand2().toString());
            VariableAssignment input =
                new VariableAssignment(variableName, value, AssignmentType.OUTPUT);
            curStep.addAssignment(input);

            lastValueWasOuput = true;
          }
        }
      }
    }

    if (curStep != null) {
      testSteps.add(curStep);
    }

    return testSteps;
  }

  private void handleInfeasibleTestGoal(Goal pGoal,
      Pair<Boolean, LinkedList<Edges>> pInfeasibilityPropagation) {
    try (StatCpuTimer t = tigerStats.handleInfeasibleTestGoalTime.start()) {
      if (lGoalPrediction != null) {
        lGoalPrediction[pGoal.getIndex() - 1] = Prediction.INFEASIBLE;
      }

      if (cfg.useTigerAlgorithm_with_pc) {
        testsuite.addInfeasibleGoal(pGoal, testsuite.getRemainingPresenceCondition(pGoal),
            lGoalPrediction);
        logger.logf(Level.FINE, "Goal %d is infeasible for remaining PC!", pGoal.getIndex());
      } else {
        logger.logf(Level.FINE, "Goal %d is infeasible!", pGoal.getIndex());
        testsuite.addInfeasibleGoal(pGoal, null, lGoalPrediction);
      }

      // TODO add missing soundness checks!
      if (pInfeasibilityPropagation.getFirst()) {
        logger.logf(Level.INFO, "Do infeasibility propagation!");
        Set<CFAEdge> lTargetEdges = Sets.newLinkedHashSet();
        ClusteredElementaryCoveragePattern lClusteredPattern =
            (ClusteredElementaryCoveragePattern) pGoal.getPattern();
        ListIterator<ClusteredElementaryCoveragePattern> lRemainingPatterns =
            lClusteredPattern.getRemainingElementsInCluster();
        int lTmpIndex = pGoal.getIndex() - 1; // caution lIndex starts at 0
        while (lRemainingPatterns.hasNext()) {
          Prediction lPrediction = lGoalPrediction[lTmpIndex];
          ClusteredElementaryCoveragePattern lRemainingPattern = lRemainingPatterns.next();
          if (lPrediction.equals(Prediction.UNKNOWN)) {
            lTargetEdges.add(lRemainingPattern.getLastSingletonCFAEdge());
          }

          lTmpIndex++;
        }
        Collection<CFAEdge> lFoundEdges =
            InfeasibilityPropagation.dfs2(lClusteredPattern.getCFANode(),
                lClusteredPattern.getLastSingletonCFAEdge(), lTargetEdges);
        lRemainingPatterns = lClusteredPattern.getRemainingElementsInCluster();
        lTmpIndex = pGoal.getIndex() - 1;
        while (lRemainingPatterns.hasNext()) {
          Prediction lPrediction = lGoalPrediction[lTmpIndex];
          ClusteredElementaryCoveragePattern lRemainingPattern = lRemainingPatterns.next();
          if (lPrediction.equals(Prediction.UNKNOWN)) {
            if (!lFoundEdges.contains(lRemainingPattern.getLastSingletonCFAEdge())) {
              //mFeasibilityInformation.setStatus(lTmpIndex+1, FeasibilityInformation.FeasibilityStatus.INFEASIBLE);
              // TODO remove ???
              lGoalPrediction[lTmpIndex] = Prediction.INFEASIBLE;
            }
          }
          lTmpIndex++;
        }
      }
    }
  }

  private void dumpAutomaton(Automaton pA) {
    if (cfg.dumpGoalAutomataTo == null) { return; }

    try (Writer w = MoreFiles.openOutputFile(cfg.dumpGoalAutomataTo.getPath(pA.getName()),
        Charset.defaultCharset())) {

      pA.writeDotFile(w);

    } catch (IOException e) {
      logger.logUserException(Level.WARNING, e, "Could not write the automaton to DOT file");
    }
  }

  private ARGCPA composeCPA(Set<Goal> pGoalsToBeProcessed, boolean addBDDToHandleFeatures)
      throws CPAException, InvalidConfigurationException {
    try (StatCpuTimer t = tigerStats.composeCPATime.start()) {

      Preconditions.checkArgument(cpa instanceof ARGCPA,
          "Tiger: Only support for ARGCPA implemented for CPA composition!");
      ARGCPA oldArgCPA = (ARGCPA) cpa;

      List<Automaton> componentAutomata = Lists.newArrayList();
      {
        List<Automaton> goalAutomata = Lists.newArrayList();

        for (Goal goal : pGoalsToBeProcessed) {
          Automaton a = goal.createControlAutomaton();
          if (cfg.useMarkingAutomata) {
            final Automaton markingAutomata;
            if (markingAutomataInstances.containsKey(a)) {
              markingAutomata = markingAutomataInstances.get(a);
            } else {
              markingAutomata = MarkingAutomatonBuilder.build(a);
              markingAutomataInstances.put(a, markingAutomata);
            }
            a = markingAutomata;
          }

          goalAutomata.add(a);
          dumpAutomaton(a);
          checkAutomaton(a);
        }

        if (cfg.useAutomataCrossProduct) {
          final Automaton productAutomaton;
          try {
            logger.logf(Level.INFO, "Computing the cross product of %d automata.",
                pGoalsToBeProcessed.size());
            productAutomaton = ReducedAutomatonProduct.productOf(goalAutomata, "GOAL_PRODUCT");
            logger.logf(Level.INFO, "Cross product with %d states.",
                productAutomaton.getStates().size());
          } catch (InvalidAutomatonException e) {
            throw new CPAException("One of the automata is invalid!", e);
          }

          dumpAutomaton(productAutomaton);
          componentAutomata.add(productAutomaton);
        } else {
          componentAutomata.addAll(goalAutomata);
        }
      }

      logger.logf(Level.INFO, "Analyzing %d test goals with %d observer automata.",
          pGoalsToBeProcessed.size(),
          componentAutomata.size());

      List<ConfigurableProgramAnalysis> automataCPAs = Lists.newArrayList();

      for (Automaton componentAutomaton : componentAutomata) {

        final CPAFactory automataFactory = cfg.usePowerset
            ? PowersetAutomatonCPA.factory()
            : ControlAutomatonCPA.factory();

        automataFactory.setConfiguration(
            Configuration.copyWithNewPrefix(config, componentAutomaton.getName()));
        automataFactory.setLogger(logger.withComponentName(componentAutomaton.getName()));
        automataFactory.set(cfa, CFA.class);
        automataFactory.set(componentAutomaton, Automaton.class);

        automataCPAs.add(automataFactory.createInstance());
      }

      // Add one automata CPA for each goal
      LinkedList<ConfigurableProgramAnalysis> lComponentAnalyses = new LinkedList<>();
      if (cfg.useComposite) {
        ConfigurationBuilder compConfigBuilder = Configuration.builder();
        compConfigBuilder.setOption("cpa.composite.separateTargetStates", "true");
        Configuration compositeConfig = compConfigBuilder.build();

        CPAFactory compositeCpaFactory = CompositeCPA.factory();
        compositeCpaFactory.setChildren(automataCPAs);
        compositeCpaFactory.setConfiguration(compositeConfig);
        compositeCpaFactory.setLogger(logger);
        compositeCpaFactory.set(cfa, CFA.class);

        ConfigurableProgramAnalysis compositeAutomatonCPA = compositeCpaFactory.createInstance();
        lComponentAnalyses.add(compositeAutomatonCPA);
      } else {
        lComponentAnalyses.addAll(automataCPAs);
      }

      // Add the old composite components
      Preconditions
          .checkState(oldArgCPA.getWrappedCPAs().iterator().next() instanceof CompositeCPA);
      CompositeCPA argCompositeCpa = (CompositeCPA) oldArgCPA.getWrappedCPAs().iterator().next();
      lComponentAnalyses.addAll(argCompositeCpa.getWrappedCPAs());

      // create BBDCPA to handle features in a second step after test generation
      if (addBDDToHandleFeatures) {
        final CPAFactory automataFactory = BDDCPA.factory();

        automataFactory.setConfiguration(config);
        automataFactory.setLogger(logger.withComponentName(BDDCPA.class.toString()));
        automataFactory.set(cfa, CFA.class);
        automataFactory.setShutdownNotifier(mainShutdownManager.getNotifier());

        ConfigurableProgramAnalysis bddCpa = automataFactory.createInstance();
        lComponentAnalyses.add(bddCpa);
      }

      final ARGCPA result;

      try {
        // create composite CPA
        CPAFactory compositeCpaFactory = CompositeCPA.factory();
        compositeCpaFactory.setChildren(lComponentAnalyses);
        compositeCpaFactory.setConfiguration(config);
        compositeCpaFactory.setLogger(logger);
        compositeCpaFactory.set(cfa, CFA.class);

        ConfigurableProgramAnalysis lCPA = compositeCpaFactory.createInstance();

        // create ARG CPA
        CPAFactory lARTCPAFactory = ARGCPA.factory();
        lARTCPAFactory.set(cfa, CFA.class);
        lARTCPAFactory.setChild(lCPA);
        lARTCPAFactory.setConfiguration(config);
        lARTCPAFactory.setLogger(logger);

        result = (ARGCPA) lARTCPAFactory.createInstance();

      } catch (InvalidConfigurationException | CPAException e) {
        throw new RuntimeException(e);
      }

      return result;
    }
  }

  /**
   * Check some properties of the automaton to
   * ensure that it works as expected.
   *
   * @param pAutomaton
   */
  private void checkAutomaton(Automaton pAutomaton) {
    for (AutomatonInternalState q: pAutomaton.getStates()) {
      if (!q.isNonDetState()) {
        Set<Pair<AutomatonBoolExpr, ImmutableList<AStatement>>> distinct = Sets.newHashSet();
        // No similar triggers!
        for (AutomatonTransition t: q.getTransitions()) {
          Pair<AutomatonBoolExpr, ImmutableList<AStatement>> key = Pair.of(t.getTrigger(), t.getAssumptions());
          if (!distinct.add(key)) {
            throw new RuntimeException("Transition not unique on MATCH-FIRST state: " + t);
          }
        }
      }
    }
  }

  @Override
  public void collectStatistics(Collection<Statistics> pStatsCollection) {
    pStatsCollection.add(this);
    pStatsCollection.add(tgarStatistics);
  }

  @Override
  public void printStatistics(PrintStream pOut, Result pResult, ReachedSet pReached) {
    // fix test suite and set timedout goals as timedout
    // TODO: move this fix to a more adequate place
    // TODO: fix for variability aware
    Set<Goal> goals = testsuite.getGoals();
    Set<Goal> timedout = new HashSet<>();
    for (Goal goal : goals) {
      try {
        if (!testsuite.isGoalCoveredOrInfeasible(goal)) {
          timedout.add(goal);
        }
      } catch (InterruptedException e) {}
    }
    testsuite.addTimedOutGoals(timedout);

    pOut.println(
        "Number of test cases:                              " + testsuite.getNumberOfTestCases());
    pOut.println(
        "Number of test goals:                              " + statistics_numberOfTestGoals);
    pOut.println("Number of processed test goals:                    "
        + statistics_numberOfProcessedTestGoals);

    if (cfg.useTigerAlgorithm_with_pc) {
      pOut.println("Number of feasible test goals:                     "
          + testsuite.getNumberOfFeasibleTestGoals());
      pOut.println("Number of partially feasible test goals:           "
          + testsuite.getNumberOfPartiallyFeasibleTestGoals());
      pOut.println("Number of infeasible test goals:                   "
          + testsuite.getNumberOfInfeasibleTestGoals());
      pOut.println("Number of partially infeasible test goals:         "
          + testsuite.getNumberOfPartiallyInfeasibleTestGoals());
      pOut.println("Number of timedout test goals:                     "
          + testsuite.getNumberOfTimedoutTestGoals());
      pOut.println("Number of partially timedout test goals:           "
          + testsuite.getNumberOfPartiallyTimedOutTestGoals());

      if (testsuite.getNumberOfTimedoutTestGoals() > 0
          || testsuite.getNumberOfPartiallyTimedOutTestGoals() > 0) {
        pOut.println("Timeout occured during processing of a test goal!");
      }
    } else {
      pOut.println("Number of feasible test goals:                     "
          + testsuite.getNumberOfFeasibleTestGoals());
      pOut.println("Number of infeasible test goals:                   "
          + testsuite.getNumberOfInfeasibleTestGoals());
      pOut.println("Number of timedout test goals:                     "
          + testsuite.getNumberOfTimedoutTestGoals());

      if (testsuite.getNumberOfTimedoutTestGoals() > 0) {
        pOut.println("Timeout occured during processing of a test goal!");
      }
    }

    tigerStats.printStatistics(pOut, pResult, pReached);

    dumpTestSuite();

    if (cfg.printPathFormulasPerGoal) {
      dumpPathFormulas();
    }

    // write test case generation times to file system
    if (cfg.testcaseGenerationTimesFile != null) {
      try (Writer writer =
          MoreFiles.openOutputFile(cfg.testcaseGenerationTimesFile, Charset.defaultCharset())) {

        List<TestCase> testcases = new ArrayList<>(testsuite.getTestCases());
        Collections.sort(testcases, new Comparator<TestCase>() {

          @Override
          public int compare(TestCase pTestCase1, TestCase pTestCase2) {
            if (pTestCase1.getGenerationTime() > pTestCase2.getGenerationTime()) {
              return 1;
            } else if (pTestCase1.getGenerationTime() < pTestCase2
                .getGenerationTime()) { return -1; }
            return 0;
          }
        });

        if (cfg.useTigerAlgorithm_with_pc) {
          Set<Goal> feasible = Sets.newLinkedHashSet();
          feasible.addAll(testsuite.getFeasibleGoals());
          feasible.addAll(testsuite.getPartiallyFeasibleGoals());
          feasible.removeAll(testsuite.getPartiallyTimedOutGoals());
          for (Goal goal : feasible) {
            List<TestCase> tests = testsuite.getCoveringTestCases(goal);
            TestCase lastTestCase = getLastTestCase(tests);
            lastTestCase.incrementNumberOfNewlyCoveredGoals();
          }
          Set<Goal> partially = Sets.newLinkedHashSet();
          partially.addAll(testsuite.getFeasibleGoals());
          partially.addAll(testsuite.getPartiallyFeasibleGoals());
          partially.removeAll(testsuite.getPartiallyTimedOutGoals());
          for (Goal goal : partially) {
            List<TestCase> tests = testsuite.getCoveringTestCases(goal);
            TestCase lastTestCase = getLastTestCase(tests);
            lastTestCase.incrementNumberOfNewlyPartiallyCoveredGoals();
          }

          writer.write(
              "Test Case;Generation Time;Covered Goals After Generation;Completely Covered Goals After Generation;Partially Covered Goals After Generation\n");
          int completelyCoveredGoals = 0;
          int partiallyCoveredGoals = 0;
          for (TestCase testCase : testcases) {
            int newCoveredGoals = testCase.getNumberOfNewlyCoveredGoals();
            int newPartiallyCoveredGoals = testCase.getNumberOfNewlyPartiallyCoveredGoals();
            completelyCoveredGoals += newCoveredGoals;
            partiallyCoveredGoals += newPartiallyCoveredGoals;

            writer.write(testCase.getId() + ";" + testCase.getGenerationTime() + ";"
                + (completelyCoveredGoals + partiallyCoveredGoals) + ";" + completelyCoveredGoals
                + ";"
                + partiallyCoveredGoals + "\n");
          }
        } else {
          Set<Goal> coveredGoals = Sets.newLinkedHashSet();
          writer.write("Test Case;Generation Time;Covered Goals After Generation\n");
          for (TestCase testCase : testcases) {
            coveredGoals.addAll(testsuite.getTestGoalsCoveredByTestCase(testCase));
            writer.write(testCase.getId() + ";" + testCase.getGenerationTime() + ";"
                + coveredGoals.size() + "\n");
          }
        }
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  private void dumpPathFormulas() {
    if (cfg.pathFormulaFile != null) {
      StringBuffer buffer = new StringBuffer();
      for (Goal goal : targetStateFormulas.keySet()) {
        buffer.append("GOAL " + goal + "\n");
        for (List<BooleanFormula> formulas : targetStateFormulas.get(goal)) {
          buffer.append("FORMULA\n");
          for (BooleanFormula formula : formulas) {
            buffer.append(formula + "\n");
          }
        }
      }

      try (Writer writer = MoreFiles.openOutputFile(cfg.pathFormulaFile, Charset.defaultCharset())) {
        writer.write(buffer.toString());
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  private TestCase getLastTestCase(List<TestCase> pTests) {
    TestCase lastTestCase = null;
    for (TestCase testCase : pTests) {
      if (lastTestCase == null || testCase.getGenerationTime() < lastTestCase.getGenerationTime()) {
        lastTestCase = testCase;
      }
    }
    return lastTestCase;
  }

}
