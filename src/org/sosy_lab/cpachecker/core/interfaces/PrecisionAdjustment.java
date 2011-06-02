/*
 *  CPAchecker is a tool for configurable software verification.
 *  This file is part of CPAchecker.
 *
 *  Copyright (C) 2007-2010  Dirk Beyer
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
/**
 *
 */
package org.sosy_lab.cpachecker.core.interfaces;


import org.sosy_lab.common.Triple;
import org.sosy_lab.cpachecker.core.reachedset.UnmodifiableReachedSet;
import org.sosy_lab.cpachecker.exceptions.CPAException;

/**
 * Interface for the precision adjustment operator.
 */
public interface PrecisionAdjustment {

  /**
   * The precision adjustment operator can tell the CPAAlgorithm whether
   * to continue with the analysis or whether to break immediately.
   */
  public static enum Action {
    CONTINUE,
    BREAK,
    ;
  }

  /**
   * This method may adjust the current element and precision using information
   * from the current set of reached states.
   *
   * If this method doesn't change anything, it is strongly recommended to return
   * the identical objects for element and precision. This makes it easier for
   * wrapper CPAs.
   *
   * @param element The current abstract element.
   * @param precision The current precision.
   * @param elements The current reached set.
   * @return The new element, new precision and the action flag.
   */
  public Triple<AbstractElement,Precision, Action> prec(
      AbstractElement element, Precision precision, UnmodifiableReachedSet elements)
      throws CPAException;
}
