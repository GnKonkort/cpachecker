/*
 *  CPAchecker is a tool for configurable software verification.
 *  This file is part of CPAchecker.
 *
 *  Copyright (C) 2007-2017  Dirk Beyer
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
package org.sosy_lab.cpachecker.cpa.bam;

import com.google.common.base.Preconditions;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.sosy_lab.common.ShutdownNotifier;
import org.sosy_lab.common.configuration.ClassOption;
import org.sosy_lab.common.configuration.Configuration;
import org.sosy_lab.common.configuration.FileOption;
import org.sosy_lab.common.configuration.InvalidConfigurationException;
import org.sosy_lab.common.configuration.Option;
import org.sosy_lab.common.configuration.Options;
import org.sosy_lab.common.log.LogManager;
import org.sosy_lab.common.time.Timer;
import org.sosy_lab.cpachecker.cfa.CFA;
import org.sosy_lab.cpachecker.cfa.blocks.BlockPartitioning;
import org.sosy_lab.cpachecker.cfa.blocks.BlockToDotWriter;
import org.sosy_lab.cpachecker.cfa.blocks.builder.BlockPartitioningBuilder;
import org.sosy_lab.cpachecker.cfa.blocks.builder.ExtendedBlockPartitioningBuilder;
import org.sosy_lab.cpachecker.cfa.blocks.builder.FunctionAndLoopPartitioning;
import org.sosy_lab.cpachecker.cfa.blocks.builder.PartitioningHeuristic;
import org.sosy_lab.cpachecker.core.defaults.AbstractSingleWrapperCPA;
import org.sosy_lab.cpachecker.core.interfaces.ConfigurableProgramAnalysis;
import org.sosy_lab.cpachecker.core.interfaces.ConfigurableProgramAnalysisWithBAM;
import org.sosy_lab.cpachecker.core.interfaces.Reducer;
import org.sosy_lab.cpachecker.exceptions.CPAException;

@Options(prefix = "cpa.bam")
public abstract class AbstractBAMCPA extends AbstractSingleWrapperCPA {

  @Option(
    secure = true,
    description =
        "Type of partitioning (FunctionAndLoopPartitioning or DelayedFunctionAndLoopPartitioning)\n"
            + "or any class that implements a PartitioningHeuristic"
  )
  @ClassOption(packagePrefix = "org.sosy_lab.cpachecker.cfa.blocks.builder")
  private PartitioningHeuristic.Factory blockHeuristic = FunctionAndLoopPartitioning::new;

  @Option(secure = true, description = "export blocks")
  @FileOption(FileOption.Type.OUTPUT_FILE)
  private Path exportBlocksPath = Paths.get("block_cfa.dot");

  @Option(secure = true,
      description = "This flag determines which precisions should be updated during refinement. "
      + "We can choose between the minimum number of states and all states that are necessary "
      + "to re-explore the program along the error-path.")
  private boolean doPrecisionRefinementForAllStates = false;

  @Option(
    secure = true,
    description = "Use more fast partitioning builder, which can not handle loops"
  )
  private boolean useExtendedPartitioningBuilder = false;

  final Timer blockPartitioningTimer = new Timer();

  protected final LogManager logger;
  protected final ShutdownNotifier shutdownNotifier;
  protected final BlockPartitioning blockPartitioning;
  private final TimedReducer reducer;

  public AbstractBAMCPA(
      ConfigurableProgramAnalysis pCpa,
      Configuration pConfig,
      LogManager pLogger,
      ShutdownNotifier pShutdownNotifier,
      CFA pCfa)
      throws InvalidConfigurationException, CPAException {
    super(pCpa);
    pConfig.inject(this, AbstractBAMCPA.class);

    if (!(pCpa instanceof ConfigurableProgramAnalysisWithBAM)) {
      throw new InvalidConfigurationException("BAM needs CPAs that are capable for BAM");
    }

    logger = pLogger;
    shutdownNotifier = pShutdownNotifier;

    blockPartitioningTimer.start();
    blockPartitioning = buildBlockPartitioning(pCfa, pConfig);
    blockPartitioningTimer.stop();

    Reducer wrappedReducer = ((ConfigurableProgramAnalysisWithBAM) pCpa).getReducer();
    reducer = new TimedReducer(wrappedReducer);
  }

  private BlockPartitioning buildBlockPartitioning(CFA pCfa, Configuration pConfig)
      throws InvalidConfigurationException, CPAException {
    final BlockPartitioningBuilder blockBuilder;
    if (useExtendedPartitioningBuilder) {
      blockBuilder = new ExtendedBlockPartitioningBuilder();
    } else {
      blockBuilder = new BlockPartitioningBuilder();
    }
    PartitioningHeuristic heuristic = blockHeuristic.create(logger, pCfa, pConfig);
    BlockPartitioning partitioning = heuristic.buildPartitioning(pCfa, blockBuilder);
    if (exportBlocksPath != null) {
      BlockToDotWriter writer = new BlockToDotWriter(partitioning);
      writer.dump(exportBlocksPath, logger);
    }
    getWrappedCpa().setPartitioning(partitioning);
    return partitioning;
  }

  @Override
  protected ConfigurableProgramAnalysisWithBAM getWrappedCpa() {
    // override for visibility
    return (ConfigurableProgramAnalysisWithBAM) super.getWrappedCpa();
  }

  public BlockPartitioning getBlockPartitioning() {
    Preconditions.checkNotNull(blockPartitioning);
    return blockPartitioning;
  }

  LogManager getLogger() {
    return logger;
  }

  TimedReducer getReducer() {
    return reducer;
  }

  abstract BAMDataManager getData();

  boolean doPrecisionRefinementForAllStates() {
    return doPrecisionRefinementForAllStates;
  }
}
