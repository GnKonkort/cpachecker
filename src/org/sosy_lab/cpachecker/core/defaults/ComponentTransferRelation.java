/*
 * CPAchecker is a tool for configurable software verification.
 *  This file is part of CPAchecker.
 *
 *  Copyright (C) 2007-2015  Dirk Beyer
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
package org.sosy_lab.cpachecker.core.defaults;

import java.util.Collection;
import java.util.List;

import org.sosy_lab.cpachecker.cfa.model.CFAEdge;
import org.sosy_lab.cpachecker.core.interfaces.AbstractState;
import org.sosy_lab.cpachecker.core.interfaces.Precision;
import org.sosy_lab.cpachecker.core.interfaces.TransferRelation;
import org.sosy_lab.cpachecker.exceptions.CPATransferException;

/**
 * Component transfer relation base class for CompositeCPA.
 * Transfer relations may choose to extend this class if they are used
 * only inside a {@link org.sosy_lab.cpachecker.cpa.composite.CompositeCPA}
 * and they wish to get access to other successor states calculated before
 * the transition takes place.
 */
public abstract class ComponentTransferRelation implements TransferRelation {

  public abstract Collection<? extends AbstractState> getComponentAbstractSuccessors(
      AbstractState pState,
      Precision pPrecision,
      List<AbstractState> otherStates,
      CFAEdge pEdge
  ) throws CPATransferException, InterruptedException;

  @Override
  public final Collection<? extends AbstractState> getAbstractSuccessors(AbstractState state, Precision precision)
      throws CPATransferException, InterruptedException {
    throw new UnsupportedOperationException("ComponentTransferRelation produces successors only inside CompositeCPA");
  }

  @Override
  public final Collection<? extends AbstractState> getAbstractSuccessorsForEdge(AbstractState state, Precision precision,
      CFAEdge cfaEdge) throws CPATransferException, InterruptedException {
    throw new UnsupportedOperationException("ComponentTransferRelation produces successors only inside CompositeCPA");
  }
}
