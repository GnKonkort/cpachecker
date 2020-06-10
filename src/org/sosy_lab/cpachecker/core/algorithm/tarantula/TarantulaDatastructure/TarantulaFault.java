/*
 *  CPAchecker is a tool for configurable software verification.
 *  This file is part of CPAchecker.
 *
 *  Copyright (C) 2007-2020  Dirk Beyer
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
package org.sosy_lab.cpachecker.core.algorithm.tarantula.TarantulaDatastructure;

import org.sosy_lab.cpachecker.util.faultlocalization.Fault;
import org.sosy_lab.cpachecker.util.faultlocalization.FaultContribution;

public class TarantulaFault implements Comparable<TarantulaFault> {

  private final double score;
  private final Fault fault;
  private final FaultContribution faultContribution;

  public TarantulaFault(double pScore, Fault pFault, FaultContribution pFaultContribution) {

    this.score = pScore;
    this.fault = pFault;
    this.faultContribution = pFaultContribution;
  }

  public double getScore() {
    return score;
  }

  public FaultContribution getFaultContribution() {
    return faultContribution;
  }

  public Fault getFault() {
    return fault;
  }

  @Override
  public String toString() {
    return "TarantulaFault{"
        + "score="
        + score
        + ", fault="
        + fault
        + ", faultContribution="
        + faultContribution
        + '}';
  }

  public String getDescription() {
    return this.faultContribution.correspondingEdge().getDescription();
  }

  @Override
  public int compareTo(TarantulaFault o) {
    if (o.getScore() < this.getScore()) {
      return -1;
    } else if (this.getScore() < o.getScore()) {
      return 1;
    }
    return 0;
  }
}