/*
 *  CPAchecker is a tool for configurable software verification.
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
package org.sosy_lab.cpachecker.cpa.thread;

import java.util.Map;
//import org.matheclipse.core.reflection.system.Map;
import org.sosy_lab.cpachecker.cfa.blocks.Block;
import org.sosy_lab.cpachecker.cfa.model.CFANode;
import org.sosy_lab.cpachecker.cfa.model.FunctionExitNode;
import org.sosy_lab.cpachecker.core.interfaces.AbstractState;
import org.sosy_lab.cpachecker.core.interfaces.Precision;
import org.sosy_lab.cpachecker.core.interfaces.Reducer;
import org.sosy_lab.cpachecker.cpa.thread.ThreadState.ThreadStatus;
import org.sosy_lab.cpachecker.util.AbstractStates;
public class ThreadReducer implements Reducer {

  public ThreadReducer() {}

  //Unnecesary caching. Do not use

  //private HashMap<Block,Map<String, ThreadStatus>> removedThreads = new HashMap<>();

  @Override
  public AbstractState getVariableReducedState(AbstractState pExpandedState, Block pContext,
      CFANode pCallNode) throws InterruptedException {
        //TODO: We are currently assuming, that there is no other operation that can change thread states other that CreateNewThread! That Means, that all threads will remain the same during all the execution, only new one can appear.
        // We've already collected all threads that have been used in current block, so we just
        // remove everything else - this will be our reduced state.
        ThreadState inputElement = (ThreadState) pExpandedState;

        Map<String, ThreadStatus> threadSetsOfInputElement = inputElement.getThreadSet();
        Map<String, ThreadStatus> allOccuredThreads = pContext.getThreads();

        allOccuredThreads.keySet().removeAll(threadSetsOfInputElement.keySet());

        // Do not use! May add unused threads to wrong block!
        //removedThreads = allOccuredThreads;
        // removedThreads.put(pContext,allOccuredThreads);
        threadSetsOfInputElement.keySet().removeAll(allOccuredThreads.keySet());

        // Now, threadSetsOfInputElement contains only those threads, that will be used in the reducing. Now we should add these threads to the new State.
        // TODO: Fix constructor

        ThreadState reducedState =
            new ThreadState(
                ((ThreadState) pExpandedState).getCurrentThread(),
                threadSetsOfInputElement,
                ((ThreadState) pExpandedState).getRemovedSet());



        //return pExpandedState;
        return reducedState;
  }

  @Override
  public AbstractState getVariableExpandedState(AbstractState pRootState, Block pReducedContext,
      AbstractState pReducedState) throws InterruptedException {

        ThreadState rootState = (ThreadState) pRootState;
        // Map<String, ThreadStatus> rootStateThreadSet = rootState.getThreadSet();
        //TODO: We are currently assuming, that there is no other operation that can change thread states other that CreateNewThread! That means, that to expand reduced state back, we must just return unused threads;

        ThreadState expandedState = ((ThreadState) pRootState);
        ThreadState reducedState =
            ((ThreadState) getVariableReducedState(
                pRootState,
                pReducedContext,
                AbstractStates.extractLocation(pRootState)));

        Map<String, ThreadStatus> threadsToBeAdded = expandedState.getThreadSet();

        threadsToBeAdded.keySet().removeAll(reducedState.getThreadSet().keySet());

        // Map<String, ThreadStatus> threadsToBeAdded = removedThreads.get(pReducedContext);
        ((ThreadState) pReducedState).getThreadSet().putAll(threadsToBeAdded);
        ThreadState expandedState1 =
            new ThreadState(
                ((ThreadState) pReducedState).getCurrentThread(),
                ((ThreadState) pReducedState).getThreadSet(),
                ((ThreadState) pReducedState).getRemovedSet());

        return expandedState1;
  }


  //Ignore
  @Override
  public Precision getVariableReducedPrecision(Precision pPrecision, Block pContext) {
    return pPrecision;
  }

  @Override
  public Precision getVariableExpandedPrecision(Precision pRootPrecision, Block pRootContext,
      Precision pReducedPrecision) {
    return pReducedPrecision;
  }

  @Override
  public Object getHashCodeForState(AbstractState pStateKey, Precision pPrecisionKey) {
    return pStateKey;
  }

  @Override
  public int measurePrecisionDifference(Precision pPrecision, Precision pOtherPrecision) {
    return 0;
  }

  @Override
  public AbstractState rebuildStateAfterFunctionCall(AbstractState pRootState,
      AbstractState pEntryState, AbstractState pExpandedState, FunctionExitNode pExitLocation) {
    return pExpandedState;
  }
}
