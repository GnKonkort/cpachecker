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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;

import org.sosy_lab.common.Pair;
import org.sosy_lab.common.configuration.Configuration;
import org.sosy_lab.common.configuration.InvalidConfigurationException;
import org.sosy_lab.common.configuration.Option;
import org.sosy_lab.common.configuration.Options;
import org.sosy_lab.common.io.Files;
import org.sosy_lab.common.io.Path;
import org.sosy_lab.common.io.Paths;
import org.sosy_lab.common.log.LogManager;
import org.sosy_lab.cpachecker.cfa.CFA;
import org.sosy_lab.cpachecker.cfa.CParser;
import org.sosy_lab.cpachecker.cfa.CParser.FileToParse;
import org.sosy_lab.cpachecker.cfa.CSourceOriginMapping;
import org.sosy_lab.cpachecker.cfa.ParseResult;
import org.sosy_lab.cpachecker.cfa.ast.IADeclaration;
import org.sosy_lab.cpachecker.cfa.ast.c.CParameterDeclaration;
import org.sosy_lab.cpachecker.cfa.model.CFAEdge;
import org.sosy_lab.cpachecker.cfa.model.CFANode;
import org.sosy_lab.cpachecker.cfa.model.FunctionEntryNode;
import org.sosy_lab.cpachecker.cfa.model.c.CFunctionEntryNode;
import org.sosy_lab.cpachecker.core.CounterexampleInfo;
import org.sosy_lab.cpachecker.core.ShutdownNotifier;
import org.sosy_lab.cpachecker.core.algorithm.Algorithm;
import org.sosy_lab.cpachecker.core.algorithm.CEGARAlgorithm;
import org.sosy_lab.cpachecker.core.algorithm.CPAAlgorithm;
import org.sosy_lab.cpachecker.core.algorithm.testgen.util.StartupConfig;
import org.sosy_lab.cpachecker.core.algorithm.tiger.fql.FQLSpecificationUtil;
import org.sosy_lab.cpachecker.core.algorithm.tiger.fql.PredefinedCoverageCriteria;
import org.sosy_lab.cpachecker.core.algorithm.tiger.fql.ast.FQLSpecification;
import org.sosy_lab.cpachecker.core.algorithm.tiger.fql.ecp.ElementaryCoveragePattern;
import org.sosy_lab.cpachecker.core.algorithm.tiger.fql.ecp.SingletonECPEdgeSet;
import org.sosy_lab.cpachecker.core.algorithm.tiger.fql.ecp.translators.GuardedEdgeLabel;
import org.sosy_lab.cpachecker.core.algorithm.tiger.fql.ecp.translators.GuardedLabel;
import org.sosy_lab.cpachecker.core.algorithm.tiger.fql.ecp.translators.InverseGuardedEdgeLabel;
import org.sosy_lab.cpachecker.core.algorithm.tiger.fql.ecp.translators.ToGuardedAutomatonTranslator;
import org.sosy_lab.cpachecker.core.algorithm.tiger.fql.translators.ecp.CoverageSpecificationTranslator;
import org.sosy_lab.cpachecker.core.algorithm.tiger.fql.translators.ecp.IncrementalCoverageSpecificationTranslator;
import org.sosy_lab.cpachecker.core.algorithm.tiger.goals.Goal;
import org.sosy_lab.cpachecker.core.algorithm.tiger.util.ARTReuse;
import org.sosy_lab.cpachecker.core.algorithm.tiger.util.ThreeValuedAnswer;
import org.sosy_lab.cpachecker.core.algorithm.tiger.util.Wrapper;
import org.sosy_lab.cpachecker.core.counterexample.Model;
import org.sosy_lab.cpachecker.core.counterexample.Model.AssignableTerm;
import org.sosy_lab.cpachecker.core.interfaces.AbstractState;
import org.sosy_lab.cpachecker.core.interfaces.AbstractWrapperState;
import org.sosy_lab.cpachecker.core.interfaces.CPAFactory;
import org.sosy_lab.cpachecker.core.interfaces.ConfigurableProgramAnalysis;
import org.sosy_lab.cpachecker.core.interfaces.Precision;
import org.sosy_lab.cpachecker.core.interfaces.Statistics;
import org.sosy_lab.cpachecker.core.interfaces.WrapperCPA;
import org.sosy_lab.cpachecker.core.reachedset.LocationMappedReachedSet;
import org.sosy_lab.cpachecker.core.reachedset.ReachedSet;
import org.sosy_lab.cpachecker.core.waitlist.Waitlist;
import org.sosy_lab.cpachecker.cpa.arg.ARGCPA;
import org.sosy_lab.cpachecker.cpa.arg.ARGState;
import org.sosy_lab.cpachecker.cpa.arg.ARGStatistics;
import org.sosy_lab.cpachecker.cpa.arg.ARGUtils;
import org.sosy_lab.cpachecker.cpa.bdd.BDDCPA;
import org.sosy_lab.cpachecker.cpa.bdd.BDDState;
import org.sosy_lab.cpachecker.cpa.composite.CompositeCPA;
import org.sosy_lab.cpachecker.cpa.guardededgeautomaton.GuardedEdgeAutomatonCPA;
import org.sosy_lab.cpachecker.cpa.guardededgeautomaton.productautomaton.ProductAutomatonCPA;
import org.sosy_lab.cpachecker.cpa.predicate.PredicateCPARefiner;
import org.sosy_lab.cpachecker.cpa.predicate.PredicateRefiner;
import org.sosy_lab.cpachecker.exceptions.CPAException;
import org.sosy_lab.cpachecker.exceptions.CParserException;
import org.sosy_lab.cpachecker.exceptions.PredicatedAnalysisPropertyViolationException;
import org.sosy_lab.cpachecker.util.automaton.NondeterministicFiniteAutomaton;
import org.sosy_lab.cpachecker.util.predicates.NamedRegionManager;
import org.sosy_lab.cpachecker.util.predicates.interfaces.Region;

import com.google.common.collect.SortedSetMultimap;
import com.google.common.collect.TreeMultimap;

@Options(prefix = "tiger")
public class TigerAlgorithm implements Algorithm {

  public static String originalMainFunction = null;

  @Option(name = "fqlQuery", description = "Coverage criterion given as an FQL query")
  private String fqlQuery = PredefinedCoverageCriteria.BASIC_BLOCK_COVERAGE; // default is basic block coverage

  @Option(name = "optimizeGoalAutomata", description = "Optimize the test goal automata")
  private boolean optimizeGoalAutomata = true;

  @Option(name = "printARGperGoal", description = "Print the ARG for each test goal")
  private boolean printARGperGoal = false;

  @Option(name = "checkCoverage", description = "Checks whether a test case for one goal covers another test goal")
  private boolean checkCoverage = true;

  @Option(name = "reuseARG", description = "Reuse ARG across test goals")
  private boolean reuseARG = true;

  @Option(name = "testsuiteFile", description = "Filename for output of generated test suite")
  private String testsuiteFile = "output/teststuite.txt";

  private LogManager logger;
  private StartupConfig startupConfig;

  private ConfigurableProgramAnalysis cpa;
  private CFA cfa;

  private CoverageSpecificationTranslator mCoverageSpecificationTranslator;
  private FQLSpecification fqlSpecification;

  private Wrapper wrapper;
  private GuardedEdgeLabel mAlphaLabel;
  private GuardedEdgeLabel mOmegaLabel;
  private InverseGuardedEdgeLabel mInverseAlphaLabel;

  // TODO replace by a proper class
  class TestCase {

    private List<BigInteger> inputs;
    private List<CFAEdge> path;

    private Region pc;

    public TestCase(List<BigInteger> pInputs, List<CFAEdge> pPath) {
      inputs = pInputs;
      path = pPath;
    }

    public TestCase(List<BigInteger> pInputs, Region pPresenceCondition,
        List<CFAEdge> pPath) {
      inputs = pInputs;
      path = pPath;
      pc = pPresenceCondition;
    }

    public List<CFAEdge> getPath() {
      return path;
    }

    public List<BigInteger> getInputs() {
      return inputs;
    }

    public String toCode() {
      String str = "int input() {\n  static int index = 0;\n  switch (index) {\n";

      int index = 0;
      for (BigInteger input : inputs) {
        str += "  case " + index + ":\n    index++;\n    return " + input + ";\n";
        index++;
      }

      str += "  default:\n    return 0;\n  }\n}\n";

      return str;
    }

    @Override
    public String toString() {
      return inputs.toString() + " with configurations " + bddCpaNamedRegionManager.dumpRegion(pc);
    }

    @Override
    public boolean equals(Object o) {
      if (o instanceof TestCase) {
        TestCase other = (TestCase)o;
        return (inputs.equals(other.inputs) && path.equals(other.path));
      }

      return false;
    }

    @Override
    public int hashCode() {
      return 38495 + 33 * inputs.hashCode() + 13 * path.hashCode();
    }
  }

  class TestSuite {
    private Map<TestCase, List<Goal>> mapping;
    private List<Goal> infeasibleGoals;

    public TestSuite() {
      mapping = new HashMap<>();
      infeasibleGoals = new LinkedList<>();
    }

    public void addInfeasibleGoal(Goal goal) {
      infeasibleGoals.add(goal);
    }

    public boolean addTestCase(TestCase testcase, Goal goal) {
      List<Goal> goals = mapping.get(testcase);

      boolean testcaseExisted = true;

      if (goals == null) {
        goals = new LinkedList<>();
        mapping.put(testcase, goals);
        testcaseExisted = false;
      }

      goals.add(goal);

      return testcaseExisted;
    }

    public Set<TestCase> getTestCases() {
      return mapping.keySet();
    }

    @Override
    public String toString() {
      StringBuffer str = new StringBuffer();

      for (Map.Entry<TestCase, List<Goal>> entry : mapping.entrySet()) {
        str.append("Testcase ");
        str.append(entry.getKey().toString());
        str.append(" covers\n");

        for (Goal goal : entry.getValue()) {
          str.append("Goal ");
          str.append(goal.getIndex());
          str.append(" ");
          str.append(goal.toSkeleton());
          str.append(" with PC ");
          str.append(bddCpaNamedRegionManager.dumpRegion(goal.getPresenceCondition()));
          str.append("\n");
        }

        str.append("\n");
      }

      str.append("infeasible:\n");

      for (Goal goal : infeasibleGoals) {
        str.append("Goal ");
        str.append(goal.getIndex());
        str.append(" ");
        str.append(goal.toSkeleton());
        str.append(" with PC ");
        str.append(bddCpaNamedRegionManager.dumpRegion(goal.getPresenceCondition()));
        str.append("\n");
      }

      str.append("\n");

      return str.toString();
    }
  }

  private TestSuite testsuite;
  ReachedSet reachedSet = null;

  NamedRegionManager bddCpaNamedRegionManager = null;

  public TigerAlgorithm(Algorithm pAlgorithm, ConfigurableProgramAnalysis pCpa, ShutdownNotifier pShutdownNotifier,
      CFA pCfa, Configuration pConfig, LogManager pLogger) throws InvalidConfigurationException {

    startupConfig = new StartupConfig(pConfig, pLogger, pShutdownNotifier);
    startupConfig.getConfig().inject(this);

    logger = pLogger;

    cpa = pCpa;
    cfa = pCfa;

    if (cpa instanceof WrapperCPA) {
      BDDCPA bddcpa = ((WrapperCPA)cpa).retrieveWrappedCpa(BDDCPA.class);
      if (bddcpa != null) {
        bddCpaNamedRegionManager = bddcpa.getManager();
      } else {
        throw new InvalidConfigurationException("CPAtiger-variability-aware started without BDDCPA. We need BDDCPA!");
      }
    } else if (cpa instanceof BDDCPA) {
      bddCpaNamedRegionManager = ((BDDCPA)cpa).getManager();
    }

    testsuite = new TestSuite();


    assert originalMainFunction != null;
    mCoverageSpecificationTranslator = new CoverageSpecificationTranslator(pCfa.getFunctionHead(originalMainFunction));


    wrapper = new Wrapper(pCfa, originalMainFunction);

    mAlphaLabel = new GuardedEdgeLabel(new SingletonECPEdgeSet(wrapper.getAlphaEdge()));
    mInverseAlphaLabel = new InverseGuardedEdgeLabel(mAlphaLabel);
    mOmegaLabel = new GuardedEdgeLabel(new SingletonECPEdgeSet(wrapper.getOmegaEdge()));


    // get internal representation of FQL query
    logger.logf(Level.INFO, "FQL query string: %s", fqlQuery);

    fqlSpecification = FQLSpecificationUtil.getFQLSpecification(fqlQuery);

    logger.logf(Level.INFO, "FQL query: %s", fqlSpecification.toString());

    // TODO fix this restriction
    if (fqlSpecification.hasPassingClause()) {
      logger.logf(Level.SEVERE, "No PASSING clauses supported at the moment!");

      throw new InvalidConfigurationException("No PASSING clauses supported at the moment!");
    }

    // TODO fix this restriction
    if (fqlSpecification.hasPredicate()) {
      logger.logf(Level.SEVERE, "No predicates in FQL queries supported at the moment!");

      throw new InvalidConfigurationException("No predicates in FQL queries supported at the moment!");
    }
  }

  @Override
  public boolean run(ReachedSet pReachedSet) throws CPAException, InterruptedException,
      PredicatedAnalysisPropertyViolationException {

    // we empty pReachedSet to stop complaints of an incomplete analysis
    // Problem: pReachedSet does not match the internal CPA structure!
    logger.logf(Level.INFO, "We will not use the provided reached set since it violates the internal structure of Tiger's CPAs");
    logger.logf(Level.INFO, "We empty pReachedSet to stop complaints of an incomplete analysis");
    pReachedSet.clear();


    // (ii) translate query into set of test goals
    // I didn't move this operation to the constructor since it is a potentially expensive operation.
    ElementaryCoveragePattern[] lGoalPatterns = extractTestGoalPatterns(fqlSpecification);

    // each test goal needs to be covered in all (if possible) products.
    // Therefore we add a "todo" presence-condition TRUE to each test goal
    Pair<ElementaryCoveragePattern, Region>[] goalsToCover = new Pair[lGoalPatterns.length];
    for (int i = 0; i < lGoalPatterns.length; i++) {
      goalsToCover[i] = Pair.of(lGoalPatterns[i], bddCpaNamedRegionManager.makeTrue());
    }

    // (iii) do test generation for test goals ...
    boolean wasSound = true;
    if (!testGeneration(goalsToCover)) {
      logger.logf(Level.WARNING, "Test generation contained unsound reachability analysis runs!");
      wasSound = false;
    }

    // write generated test suite and mapping to file system
    try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(testsuiteFile), "utf-8"))) {
      writer.write(testsuite.toString());
      writer.close();
    } catch (IOException e){
      throw new RuntimeException(e);
    }

    return wasSound;
  }

  private ElementaryCoveragePattern[] extractTestGoalPatterns(FQLSpecification pFQLQuery) {
    logger.logf(Level.INFO, "Extracting test goals.");


    // TODO check for (temporarily) unsupported features

    // TODO enable use of infeasibility propagation


    IncrementalCoverageSpecificationTranslator lTranslator = new IncrementalCoverageSpecificationTranslator(mCoverageSpecificationTranslator.mPathPatternTranslator);

    int lNumberOfTestGoals = lTranslator.getNumberOfTestGoals(pFQLQuery.getCoverageSpecification());
    logger.logf(Level.INFO, "Number of test goals: %d", lNumberOfTestGoals);

    Iterator<ElementaryCoveragePattern> lGoalIterator = lTranslator.translate(pFQLQuery.getCoverageSpecification());
    ElementaryCoveragePattern[] lGoalPatterns = new ElementaryCoveragePattern[lNumberOfTestGoals];

    for (int lGoalIndex = 0; lGoalIndex < lNumberOfTestGoals; lGoalIndex++) {
      lGoalPatterns[lGoalIndex] = lGoalIterator.next();
    }

    return lGoalPatterns;
  }

  private boolean testGeneration(Pair<ElementaryCoveragePattern, Region>[] pGoalsToCover) throws CPAException, InterruptedException {
    boolean wasSound = true;

    int goalIndex = 0;

    NondeterministicFiniteAutomaton<GuardedEdgeLabel> previousAutomaton = null;

    for (Pair<ElementaryCoveragePattern, Region> testGoalToCover : pGoalsToCover) {
      goalIndex++;
      ElementaryCoveragePattern lTestGoalPattern = testGoalToCover.getFirst();
      Region remainingPCforGoalCoverage = testGoalToCover.getSecond();

      while (! remainingPCforGoalCoverage.isFalse()) {
        logger.logf(Level.INFO, "Processing test goal %d of %d for PC %s.", goalIndex, pGoalsToCover.length, bddCpaNamedRegionManager.dumpRegion(remainingPCforGoalCoverage));

        Goal lGoal = constructGoal(goalIndex, lTestGoalPattern, mAlphaLabel, mInverseAlphaLabel, mOmegaLabel,  optimizeGoalAutomata, remainingPCforGoalCoverage);

        NondeterministicFiniteAutomaton<GuardedEdgeLabel> currentAutomaton = lGoal.getAutomaton();

        if (ARTReuse.isDegeneratedAutomaton(currentAutomaton)) {
          // current goal is for sure infeasible
          logger.logf(Level.INFO, "Test goal infeasible.");
          continue; // we do not want to modify the ARG for the degenerated automaton to keep more reachability information
        }

        if (checkCoverage) {
          boolean wasCovered = false;

          for (TestCase testcase : testsuite.getTestCases()) {
            ThreeValuedAnswer isCovered = TigerAlgorithm.accepts(currentAutomaton, testcase.getPath());
            if (isCovered.equals(ThreeValuedAnswer.ACCEPT) &&
                !bddCpaNamedRegionManager.makeAnd(remainingPCforGoalCoverage, testcase.pc).isFalse()) { // configurations in testGoalPCtoCover and testcase.pc have a non-empty intersection
              // test goal is already (at least for some PCs) covered by an existing test case
              // remove those PCs from todo
              remainingPCforGoalCoverage = bddCpaNamedRegionManager.makeAnd(remainingPCforGoalCoverage, bddCpaNamedRegionManager.makeNot(testcase.pc));

              if (remainingPCforGoalCoverage.isFalse()) {
                logger.logf(Level.INFO, "Test goal %d is already covered by an existing test case.", goalIndex);
                testsuite.addTestCase(testcase, lGoal);
                wasCovered = true;
                break;
              } else {
                logger.logf(Level.INFO, "Test goal %d is already partly covered by an existing test case.", goalIndex , " Remaining PC: ", bddCpaNamedRegionManager.dumpRegion(remainingPCforGoalCoverage));
                testsuite.addTestCase(testcase, lGoal);
              }
            }
            else if (isCovered.equals(ThreeValuedAnswer.UNKNOWN)) {
              logger.logf(Level.WARNING, "Coverage check for goal %d could not be performed in a precise way!", goalIndex);
            }
          }

          if (wasCovered) {
            continue;
          }
        }
        // goal is uncovered so far; run CPAchecker to cover it
        if (!runReachabilityAnalysis(goalIndex, lGoal, previousAutomaton, remainingPCforGoalCoverage)) {
          logger.logf(Level.WARNING, "Analysis run was unsound!");
          wasSound = false;
        }
        previousAutomaton = currentAutomaton;

        // update PC coverage todo
        if (testsuite.infeasibleGoals.contains(lGoal)) {
          logger.logf(Level.WARNING, "Goal %d is infeasible for remaining PC %s !", goalIndex, bddCpaNamedRegionManager.dumpRegion(remainingPCforGoalCoverage));
          remainingPCforGoalCoverage = bddCpaNamedRegionManager.makeFalse();
        } else {
          // now we need to cover all remaining configurations
          // the remaining configs are represented by the negation of the already covered pcs (in conjunction with the previous testGoalPCtoCover)
          remainingPCforGoalCoverage = bddCpaNamedRegionManager.makeAnd(remainingPCforGoalCoverage, bddCpaNamedRegionManager.makeNot(getGoalCoverage(lGoal)));
          logger.logf(Level.WARNING, "Covered some PCs for Goal %d. Remaining PC %s !", goalIndex, bddCpaNamedRegionManager.dumpRegion(remainingPCforGoalCoverage));
        }

      }
    }

    return wasSound;
  }

  private Region getGoalCoverage(Goal pGoal) {
    Region totalCoverage = bddCpaNamedRegionManager.makeFalse();
    for (Entry<TestCase, List<Goal>> entry : testsuite.mapping.entrySet()) {
      if (entry.getValue().contains(pGoal)) {
        totalCoverage = bddCpaNamedRegionManager.makeOr(totalCoverage, entry.getKey().pc);
      }
    }
    return totalCoverage;
  }

  private boolean runReachabilityAnalysis(int goalIndex, Goal pGoal, NondeterministicFiniteAutomaton<GuardedEdgeLabel> pPreviousGoalAutomaton, Region remainingPCforGoalCoverage) throws CPAException, InterruptedException {
    GuardedEdgeAutomatonCPA lAutomatonCPA = new GuardedEdgeAutomatonCPA(pGoal.getAutomaton());

    List<ConfigurableProgramAnalysis> lAutomatonCPAs = new ArrayList<>(1);//(2);

    /*if (pPassingCPA != null) {
      lAutomatonCPAs.add(pPassingCPA);
    }*/

    lAutomatonCPAs.add(lAutomatonCPA);

    LinkedList<ConfigurableProgramAnalysis> lComponentAnalyses = new LinkedList<>();
    // TODO what is the more efficient order for the CPAs? Can we substitute a placeholder CPA? or inject an automaton in to an automaton CPA?
    //int lProductAutomatonIndex = lComponentAnalyses.size();
    int lProductAutomatonIndex = lComponentAnalyses.size();
    lComponentAnalyses.add(ProductAutomatonCPA.create(lAutomatonCPAs, false));

    // TODO experiment
    if (cpa instanceof CompositeCPA) {
      CompositeCPA compositeCPA = (CompositeCPA)cpa;
      lComponentAnalyses.addAll(compositeCPA.getWrappedCPAs());
    }
    else {
      lComponentAnalyses.add(cpa);
    }


    ARGCPA lARTCPA;
    try {
      // create composite CPA
      CPAFactory lCPAFactory = CompositeCPA.factory();
      lCPAFactory.setChildren(lComponentAnalyses);
      lCPAFactory.setConfiguration(startupConfig.getConfig());
      lCPAFactory.setLogger(logger);
      lCPAFactory.set(cfa, CFA.class);

      ConfigurableProgramAnalysis lCPA = lCPAFactory.createInstance();

      // create ART CPA
      CPAFactory lARTCPAFactory = ARGCPA.factory();
      lARTCPAFactory.set(cfa, CFA.class);
      lARTCPAFactory.setChild(lCPA);
      lARTCPAFactory.setConfiguration(startupConfig.getConfig());
      lARTCPAFactory.setLogger(logger);

      lARTCPA = (ARGCPA)lARTCPAFactory.createInstance();
    } catch (InvalidConfigurationException | CPAException e) {
      throw new RuntimeException(e);
    }

    if (reuseARG && (reachedSet != null)) {
      ARTReuse.modifyReachedSet(reachedSet, cfa.getMainFunction(), lARTCPA, lProductAutomatonIndex, pPreviousGoalAutomaton, pGoal.getAutomaton());
    }
    else {
      reachedSet = new LocationMappedReachedSet(Waitlist.TraversalMethod.BFS); // TODO why does TOPSORT not exist anymore?

      AbstractState lInitialElement = lARTCPA.getInitialState(cfa.getMainFunction());
      Precision lInitialPrecision = lARTCPA.getInitialPrecision(cfa.getMainFunction());

      reachedSet.add(lInitialElement, lInitialPrecision);
    }

    ShutdownNotifier algNotifier = ShutdownNotifier.createWithParent(startupConfig.getShutdownNotifier());

    CPAAlgorithm cpaAlg;

    try {
      cpaAlg = CPAAlgorithm.create(lARTCPA, logger, startupConfig.getConfig(), algNotifier);
    } catch (InvalidConfigurationException e1) {
      throw new RuntimeException(e1);
    }


    PredicateCPARefiner lRefiner;
    try {
      //lRefiner = PredicateRefiner.cpatiger_create(lARTCPA, this);
      lRefiner = PredicateRefiner.create(lARTCPA);
    } catch (CPAException | InvalidConfigurationException e) {
      throw new RuntimeException(e);
    }

    CEGARAlgorithm cegarAlg;
    try {
      cegarAlg = new CEGARAlgorithm(cpaAlg, lRefiner, startupConfig.getConfig(), logger);
    } catch (InvalidConfigurationException | CPAException e) {
      throw new RuntimeException(e);
    }

    ARGStatistics lARTStatistics;
    try {
      lARTStatistics = new ARGStatistics(startupConfig.getConfig(), lARTCPA);
    } catch (InvalidConfigurationException e) {
      throw new RuntimeException(e);
    }
    Set<Statistics> lStatistics = new HashSet<>();
    lStatistics.add(lARTStatistics);
    cegarAlg.collectStatistics(lStatistics);

    // inject goal Presence Condition in BDDCPA
    BDDCPA bddcpa = null;
    if (cpa instanceof WrapperCPA) {
      // must be non-null, otherwise Exception in constructor of this class
      bddcpa = ((WrapperCPA)cpa).retrieveWrappedCpa(BDDCPA.class);
    } else if (cpa instanceof BDDCPA) {
      bddcpa = (BDDCPA)cpa;
    }
    bddcpa.getTransferRelation().setGlobalConstraint(remainingPCforGoalCoverage);

    // start CPAchecker to find a path to the test goal
    boolean analysisWasSound = cegarAlg.run(reachedSet);

    if (printARGperGoal) {
      Path argFile = Paths.get("output", "ARG_goal_" + goalIndex + ".dot");

      try (Writer w = Files.openOutputFile(argFile)) {
        ARGUtils.writeARGAsDot(w, (ARGState) reachedSet.getFirstState());
      } catch (IOException e) {
        logger.logUserException(Level.WARNING, e, "Could not write ARG to file");
      }
    }

    Map<ARGState, CounterexampleInfo> counterexamples = lARTCPA.getCounterexamples();

    if (counterexamples.isEmpty()) {
      // test goal is not feasible
      logger.logf(Level.INFO, "Test goal infeasible with PC " + bddCpaNamedRegionManager.dumpRegion(remainingPCforGoalCoverage)+ ".");

      testsuite.addInfeasibleGoal(pGoal);
      // TODO add missing soundness checks!
    }
    else {
      // test goal is feasible
      logger.logf(Level.INFO, "Test goal is feasible with PC " + bddCpaNamedRegionManager.dumpRegion(remainingPCforGoalCoverage)+".");

      // TODO add missing soundness checks!

      assert counterexamples.size() == 1;

      for (Map.Entry<ARGState, CounterexampleInfo> lEntry : counterexamples.entrySet()) {
        //ARGState state = lEntry.getKey();
        CounterexampleInfo cex = lEntry.getValue();

        if (cex.isSpurious()) {
          logger.logf(Level.WARNING, "Counterexample is spurious!");
        }
        else {
          Model model = cex.getTargetPathModel();

          Comparator<Map.Entry<Model.AssignableTerm, Object>> comp = new Comparator<Map.Entry<Model.AssignableTerm, Object>>() {

            @Override
            public int compare(Entry<AssignableTerm, Object> pArg0, Entry<AssignableTerm, Object> pArg1) {
              assert pArg0.getKey().getName().equals(pArg1.getKey().getName());
              assert pArg0.getKey() instanceof Model.Variable;
              assert pArg1.getKey() instanceof Model.Variable;

              Model.Variable v0 = (Model.Variable)pArg0.getKey();
              Model.Variable v1 = (Model.Variable)pArg1.getKey();

              return (v0.getSSAIndex() - v1.getSSAIndex());
            }

          };

          TreeSet<Map.Entry<Model.AssignableTerm, Object>> inputs = new TreeSet<>(comp);

          for (Map.Entry<Model.AssignableTerm, Object> e : model.entrySet()) {
            if (e.getKey() instanceof Model.Variable) {
              Model.Variable v = (Model.Variable)e.getKey();

              if (v.getName().equals(TigerAlgorithm.CPAtiger_INPUT + "::__retval__")) {
                inputs.add(e);
              }
            }
          }

          List<BigInteger> inputValues = new ArrayList<>(inputs.size());

          for (Map.Entry<Model.AssignableTerm, Object> e : inputs) {
            assert e.getValue() instanceof BigInteger;
            inputValues.add((BigInteger)e.getValue());
          }
          BDDState wrappedBDDState = getWrappedBDDState(reachedSet.getLastState());
          if (wrappedBDDState!=null) {
            Region testGoalRegion = wrappedBDDState.getRegion();
            assert wrappedBDDState.getNamedRegionManager() == bddCpaNamedRegionManager;
            TestCase testcase = new TestCase(inputValues, testGoalRegion, cex.getTargetPath().asEdgesList());
            logger.logf(Level.INFO, " generated Testcase with PC " + bddCpaNamedRegionManager.dumpRegion(testGoalRegion));
            testsuite.addTestCase(testcase, pGoal);
          } else {
            logger.logf(Level.INFO, "could not determine PC for test");
            TestCase testcase = new TestCase(inputValues,  cex.getTargetPath().asEdgesList());
            testsuite.addTestCase(testcase, pGoal);
          }
        }
      }
    }

    return analysisWasSound;
  }
  BDDState getWrappedBDDState(AbstractState inState) {
    if (inState instanceof BDDState) {
      return (BDDState)inState;
    } else if (inState instanceof AbstractWrapperState) {
      for (AbstractState subState : ((AbstractWrapperState)inState).getWrappedStates()) {
        if (subState instanceof BDDState) {
          return (BDDState)subState;
        } else if (subState instanceof AbstractWrapperState) {
          BDDState res = getWrappedBDDState(subState);
          if (res != null) {
            return res;
          }
        }
      }
    }
    return null;
  }

  public static ThreeValuedAnswer accepts(NondeterministicFiniteAutomaton<GuardedEdgeLabel> pAutomaton, List<CFAEdge> pCFAPath) {
    Set<NondeterministicFiniteAutomaton.State> lCurrentStates = new HashSet<>();
    Set<NondeterministicFiniteAutomaton.State> lNextStates = new HashSet<>();

    lCurrentStates.add(pAutomaton.getInitialState());

    boolean lHasPredicates = false;

    for (CFAEdge lCFAEdge : pCFAPath) {
      for (NondeterministicFiniteAutomaton.State lCurrentState : lCurrentStates) {
        // Automaton accepts as soon as it sees a final state (implicit self-loop)
        if (pAutomaton.getFinalStates().contains(lCurrentState)) {
          return ThreeValuedAnswer.ACCEPT;
        }

        for (NondeterministicFiniteAutomaton<GuardedEdgeLabel>.Edge lOutgoingEdge : pAutomaton.getOutgoingEdges(lCurrentState)) {
          GuardedEdgeLabel lLabel = lOutgoingEdge.getLabel();

          if (lLabel.hasGuards()) {
            lHasPredicates = true;
          }
          else {
            if (lLabel.contains(lCFAEdge)) {
              lNextStates.add(lOutgoingEdge.getTarget());
            }
          }
        }
      }

      lCurrentStates.clear();

      Set<NondeterministicFiniteAutomaton.State> lTmp = lCurrentStates;
      lCurrentStates = lNextStates;
      lNextStates = lTmp;
    }

    for (NondeterministicFiniteAutomaton.State lCurrentState : lCurrentStates) {
      // Automaton accepts as soon as it sees a final state (implicit self-loop)
      if (pAutomaton.getFinalStates().contains(lCurrentState)) {
        return ThreeValuedAnswer.ACCEPT;
      }
    }

    if (lHasPredicates) {
      return ThreeValuedAnswer.UNKNOWN;
    }
    else {
      return ThreeValuedAnswer.REJECT;
    }
  }

  /**
   * Constructs a test goal from the given pattern.
   * @param pGoalPattern
   * @param pAlphaLabel
   * @param pInverseAlphaLabel
   * @param pOmegaLabel
   * @param pUseAutomatonOptimization
   * @param pTestGoalPCtoCover
   * @return
   */
  private Goal constructGoal(int pIndex, ElementaryCoveragePattern pGoalPattern, GuardedEdgeLabel pAlphaLabel,
      GuardedEdgeLabel pInverseAlphaLabel, GuardedLabel pOmegaLabel, boolean pUseAutomatonOptimization, Region pTestGoalPCtoCover) {

    NondeterministicFiniteAutomaton<GuardedEdgeLabel> automaton = ToGuardedAutomatonTranslator.toAutomaton(pGoalPattern, pAlphaLabel, pInverseAlphaLabel, pOmegaLabel);
    automaton = FQLSpecificationUtil.optimizeAutomaton(automaton, pUseAutomatonOptimization);

    Goal lGoal = new Goal(pIndex, pGoalPattern, automaton, pTestGoalPCtoCover);

    return lGoal;
  }

  // TODO move all these wrapper related code into TigerAlgorithmUtil class
  public static final String CPAtiger_MAIN = "__CPAtiger__main";
  public static final String CPAtiger_INPUT = "input";

  public static FileToParse getWrapperCFunction(CFunctionEntryNode pMainFunction) throws IOException {

    StringWriter lWrapperFunction = new StringWriter();
    PrintWriter lWriter = new PrintWriter(lWrapperFunction);

    // TODO interpreter is not capable of handling initialization of global declarations

    lWriter.println(pMainFunction.getFunctionDefinition().toASTString());
    lWriter.println();
    lWriter.println("extern int __VERIFIER_nondet_int();");
    lWriter.println();
    lWriter.println("int " +  CPAtiger_INPUT + "() {");
    lWriter.println("  return __VERIFIER_nondet_int();");
    lWriter.println("}");
    lWriter.println();
    lWriter.println("void " + CPAtiger_MAIN + "()");
    lWriter.println("{");

    for (CParameterDeclaration lDeclaration : pMainFunction.getFunctionParameters()) {
      lWriter.println("  " + lDeclaration.toASTString() + ";");
    }

    for (CParameterDeclaration lDeclaration : pMainFunction.getFunctionParameters()) {
      // TODO do we need to handle lDeclaration more specifically?
      lWriter.println("  " + lDeclaration.getName() + " = " +  CPAtiger_INPUT + "();");
    }

    lWriter.println();
    lWriter.print("  " + pMainFunction.getFunctionName() + "(");

    boolean isFirst = true;

    for (CParameterDeclaration lDeclaration : pMainFunction.getFunctionParameters()) {
      if (isFirst) {
        isFirst = false;
      }
      else {
        lWriter.print(", ");
      }

      lWriter.print(lDeclaration.getName());
    }

    lWriter.println(");");
    lWriter.println("  return;");
    lWriter.println("}");
    lWriter.println();

    File f = File.createTempFile(CPAtiger_MAIN, ".c", null);
    f.deleteOnExit();

    Writer writer = null;

    try {
        writer = new BufferedWriter(new OutputStreamWriter(
              new FileOutputStream(f), "utf-8"));
        writer.write(lWrapperFunction.toString());
    } catch (IOException ex) {
      // TODO report
    } finally {
       try {writer.close();} catch (Exception ex) {}
    }

    return new FileToParse(f.getAbsolutePath(), CPAtiger_MAIN + "__");
  }

  public static ParseResult addWrapper(CParser cParser, ParseResult tmpParseResult, CSourceOriginMapping sourceOriginMapping) throws IOException, CParserException, InvalidConfigurationException, InterruptedException {
    // create wrapper code
    CFunctionEntryNode entryNode = (CFunctionEntryNode)tmpParseResult.getFunctions().get(TigerAlgorithm.originalMainFunction);

    List<FileToParse> tmpList = new ArrayList<>();
    tmpList.add(TigerAlgorithm.getWrapperCFunction(entryNode));

    ParseResult wrapperParseResult = cParser.parseFile(tmpList, sourceOriginMapping);

    // TODO add checks for consistency
    SortedMap<String, FunctionEntryNode> mergedFunctions = new TreeMap<>();
    mergedFunctions.putAll(tmpParseResult.getFunctions());
    mergedFunctions.putAll(wrapperParseResult.getFunctions());

    SortedSetMultimap<String, CFANode> mergedCFANodes = TreeMultimap.create();
    mergedCFANodes.putAll(tmpParseResult.getCFANodes());
    mergedCFANodes.putAll(wrapperParseResult.getCFANodes());

    List<Pair<IADeclaration, String>> mergedGlobalDeclarations = new ArrayList<> (tmpParseResult.getGlobalDeclarations().size() + wrapperParseResult.getGlobalDeclarations().size());
    mergedGlobalDeclarations.addAll(tmpParseResult.getGlobalDeclarations());
    mergedGlobalDeclarations.addAll(wrapperParseResult.getGlobalDeclarations());

    return new ParseResult(mergedFunctions, mergedCFANodes, mergedGlobalDeclarations, tmpParseResult.getLanguage());
  }

}
