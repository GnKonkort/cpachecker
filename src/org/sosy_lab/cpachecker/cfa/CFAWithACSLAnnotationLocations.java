// This file is part of CPAchecker,
// a tool for configurable software verification:
// https://cpachecker.sosy-lab.org
//
// SPDX-FileCopyrightText: 2020 Dirk Beyer <https://www.sosy-lab.org>
//
// SPDX-License-Identifier: Apache-2.0

package org.sosy_lab.cpachecker.cfa;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Optional;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.sosy_lab.cpachecker.cfa.model.CFAEdge;
import org.sosy_lab.cpachecker.cfa.model.CFANode;
import org.sosy_lab.cpachecker.cfa.model.FunctionEntryNode;
import org.sosy_lab.cpachecker.cfa.types.MachineModel;
import org.sosy_lab.cpachecker.core.algorithm.acsl.ACSLAnnotation;
import org.sosy_lab.cpachecker.util.LiveVariables;
import org.sosy_lab.cpachecker.util.LoopStructure;
import org.sosy_lab.cpachecker.util.dependencegraph.DependenceGraph;
import org.sosy_lab.cpachecker.util.variableclassification.VariableClassification;

public class CFAWithACSLAnnotationLocations implements CFA {

  private CFA delegate;

  private Map<IASTFileLocation, CFAEdge> commentPositions;
  private Multimap<CFAEdge, ACSLAnnotation> edgesToAnnotations = LinkedHashMultimap.create();

  public CFAWithACSLAnnotationLocations(CFA pCFA, Map<IASTFileLocation, CFAEdge> pCommentPositions) {
    delegate = pCFA;
    commentPositions = pCommentPositions;
  }

  public Map<IASTFileLocation, CFAEdge> getCommentPositions() {
    return commentPositions;
  }

  public Multimap<CFAEdge, ACSLAnnotation> getEdgesToAnnotations() {
    return edgesToAnnotations;
  }

  @Override
  public MachineModel getMachineModel() {
    return delegate.getMachineModel();
  }

  @Override
  public boolean isEmpty() {
    return delegate.isEmpty();
  }

  @Override
  public int getNumberOfFunctions() {
    return delegate.getNumberOfFunctions();
  }

  @Override
  public NavigableSet<String> getAllFunctionNames() {
    return delegate.getAllFunctionNames();
  }

  @Override
  public Collection<FunctionEntryNode> getAllFunctionHeads() {
    return delegate.getAllFunctionHeads();
  }

  @Override
  public FunctionEntryNode getFunctionHead(String name) {
    return delegate.getFunctionHead(name);
  }

  @Override
  public NavigableMap<String, FunctionEntryNode> getAllFunctions() {
    return delegate.getAllFunctions();
  }

  @Override
  public Collection<CFANode> getAllNodes() {
    return delegate.getAllNodes();
  }

  @Override
  public FunctionEntryNode getMainFunction() {
    return delegate.getMainFunction();
  }

  @Override
  public Optional<LoopStructure> getLoopStructure() {
    return delegate.getLoopStructure();
  }

  @Override
  public Optional<ImmutableSet<CFANode>> getAllLoopHeads() {
    return delegate.getAllLoopHeads();
  }

  @Override
  public Optional<VariableClassification> getVarClassification() {
    return delegate.getVarClassification();
  }

  @Override
  public Optional<LiveVariables> getLiveVariables() {
    return delegate.getLiveVariables();
  }

  @Override
  public Optional<DependenceGraph> getDependenceGraph() {
    return delegate.getDependenceGraph();
  }

  @Override
  public Language getLanguage() {
    return delegate.getLanguage();
  }

  @Override
  public List<Path> getFileNames() {
    return delegate.getFileNames();
  }
}
