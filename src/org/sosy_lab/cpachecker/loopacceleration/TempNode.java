/*
 *  CPAchecker is a tool for configurable software verification.
 *  This file is part of CPAchecker.
 *
 *  Copyright (C) 2007-2019  Dirk Beyer
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
 */
package org.sosy_lab.cpachecker.loopacceleration;

import org.sosy_lab.cpachecker.cfa.model.CFANode;

public class TempNode {

  private CFANode node;
  private int nextEdge;

  public TempNode(CFANode cNode, int nextEdgeT) {
    node = cNode;
    nextEdge = nextEdgeT;
  }

  public void setNextEdge(int nextEdge) {
    this.nextEdge = nextEdge;
  }

  public int getNextEdge() {
    return nextEdge;
  }

  public CFANode getNode() {
    return node;
  }
}
