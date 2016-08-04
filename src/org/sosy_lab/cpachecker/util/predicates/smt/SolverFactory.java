/*
 * CPAchecker is a tool for configurable software verification.
 *  This file is part of CPAchecker.
 *
 *  Copyright (C) 2007-2016  Dirk Beyer
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
package org.sosy_lab.cpachecker.util.predicates.smt;

import org.sosy_lab.common.ShutdownNotifier;
import org.sosy_lab.common.configuration.Configuration;
import org.sosy_lab.common.configuration.InvalidConfigurationException;
import org.sosy_lab.common.log.LogManager;
import org.sosy_lab.cpachecker.util.Triple;

import java.util.HashMap;
import java.util.Map;

/**
 * Class for creating solvers, useful for sharing the same solver across
 * multiple CPAs.
 *
 */
public class SolverFactory {
  private final Map<Triple<Configuration, LogManager, ShutdownNotifier>, Solver>
      solverCache = new HashMap<>();

  /**
   * Get-or-create an existing solver object.
   *
   * <p>Guarantees to return the same solver object if same configuration,
   * log manager, and shutdown notifier is passed.
   * Creates a new solver object otherwise.
   **/
  public Solver getOrCreate(
      Configuration pConfiguration,
      LogManager pLogManager,
      ShutdownNotifier pShutdownNotifier
  ) throws InvalidConfigurationException {

    Triple<Configuration, LogManager, ShutdownNotifier> key =
        Triple.of(pConfiguration, pLogManager, pShutdownNotifier);
    Solver out = solverCache.get(key);
    if (out == null) {
      out = buildSolver(pConfiguration, pLogManager, pShutdownNotifier);
      solverCache.put(key, out);
    }
    return out;
  }

  /**
   * Create a new solver objects.
   */
  private Solver buildSolver(
      Configuration pConfiguration,
      LogManager pLogManager,
      ShutdownNotifier pShutdownNotifier) throws InvalidConfigurationException {
    return Solver.create(pConfiguration, pLogManager, pShutdownNotifier);
  }
}
