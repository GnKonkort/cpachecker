/*
 * CPAchecker is a tool for configurable software verification.
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
package org.sosy_lab.cpachecker.cpa.value.symbolic.type;

import java.util.Objects;
import java.util.Optional;
import org.sosy_lab.cpachecker.cfa.types.Type;
import org.sosy_lab.cpachecker.cfa.types.c.CType;
import org.sosy_lab.cpachecker.cpa.interval.NumberInterface;
import org.sosy_lab.cpachecker.cpa.value.type.NumericValue;
import org.sosy_lab.cpachecker.util.states.MemoryLocation;

/**
 * An expression containing {@link SymbolicValue}s.
 */
public abstract class SymbolicExpression implements SymbolicValue {

  private static final long serialVersionUID = 2228733300503173691L;

  private final Optional<MemoryLocation> representedLocation;

  protected SymbolicExpression(final MemoryLocation pRepresentedLocation) {
    representedLocation = Optional.of(pRepresentedLocation);
  }

  protected SymbolicExpression() {
    representedLocation = Optional.empty();
  }

  @Override
  public abstract SymbolicExpression copyForLocation(MemoryLocation pRepresentedLocation);

  @Override
  public Optional<MemoryLocation> getRepresentedLocation() {
    return representedLocation;
  }

  /**
   * Accepts the given {@link SymbolicValueVisitor}.
   *
   * @param pVisitor the visitor to accept
   * @param <VisitorReturnT> the return type of the visitor's specific <code>visit</code> method
   * @return the value returned by the visitor's <code>visit</code> method
   */
  @Override
  public abstract <VisitorReturnT> VisitorReturnT accept(
      SymbolicValueVisitor<VisitorReturnT> pVisitor);

  /**
   * Returns the expression type of this <code>SymbolicExpression</code>.
   *
   * @return the expression type of this <code>SymbolicExpression</code>
   */
  public abstract Type getType();

  /**
   * Returns whether this <code>SymbolicExpression</code> is always true and does only contain
   * explicit values.
   *
   * @return <code>true</code> if this <code>SymbolicExpression</code> is always true and does only
   * contain explicit
   * values, <code>false</code> otherwise
   */
  public abstract boolean isTrivial();

  @Override
  public boolean isNumericValue() {
    return false;
  }

  @Override
  public boolean isUnknown() {
    return false;
  }

  @Override
  public boolean isExplicitlyKnown() {
    return false;
  }

  @Override
  public NumericValue asNumericValue() {
    throw new UnsupportedOperationException(
        "Symbolic expressions can't be expressed as numeric values");
  }

  @Override
  public Long asLong(CType type) {
    throw new UnsupportedOperationException(
        "Symbolic expressions can't be expressed as numeric values");
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(representedLocation);
  }

  @Override
  public boolean equals(final Object pObj) {
    return pObj != null
        && pObj.getClass().equals(getClass())
        && Objects.equals(representedLocation,
                          ((SymbolicExpression) pObj).representedLocation);
  }
  @Override
  public boolean intersects(NumberInterface pOther) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public NumberInterface plus(NumberInterface pInterval) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public NumberInterface minus(NumberInterface pOther) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public NumberInterface times(NumberInterface pOther) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public NumberInterface divide(NumberInterface pOther) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public NumberInterface shiftLeft(NumberInterface pOffset) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public NumberInterface shiftRight(NumberInterface pOffset) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public NumberInterface unsignedDivide(NumberInterface pOther) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public NumberInterface unsignedModulo(NumberInterface pOther) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public NumberInterface unsignedShiftRight(NumberInterface pOther) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public NumberInterface modulo(NumberInterface pOther) {
    // TODO Auto-generated method stub
    return null;
  }
  @Override
  public NumberInterface union(NumberInterface pOther) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean contains(NumberInterface pOther) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public NumberInterface intersect(NumberInterface pOther) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public NumberInterface limitUpperBoundBy(NumberInterface pOther) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public NumberInterface limitLowerBoundBy(NumberInterface pOther) {
    // TODO Auto-generated method stub
    return null;
  }
  @Override
  public NumberInterface binaryAnd(NumberInterface pRNum) {
      // TODO Auto-generated method stub
      return null;
  }

  @Override
  public NumberInterface binaryOr(NumberInterface pRNum) {
      // TODO Auto-generated method stub
      return null;
  }

  @Override
  public NumberInterface binaryXor(NumberInterface pRNum) {
      // TODO Auto-generated method stub
      return null;
  }
  @Override
  public boolean covers(NumberInterface pSign) {
      // TODO Auto-generated method stub
      return false;
  }

  @Override
  public boolean isSubsetOf(NumberInterface pSign) {
      // TODO Auto-generated method stub
      return false;
  }

  @Override
  public NumberInterface evaluateNonCommutativePlusOperator(NumberInterface pRight) {
      // TODO Auto-generated method stub
      return null;
  }

  @Override
  public NumberInterface evaluateMulOperator(NumberInterface pRight) {
      // TODO Auto-generated method stub
      return null;
  }

  @Override
  public NumberInterface evaluateNonCommutativeMulOperator(NumberInterface pRight) {
      // TODO Auto-generated method stub
      return null;
  }

  @Override
  public NumberInterface evaluateDivideOperator(NumberInterface pRight) {
      // TODO Auto-generated method stub
      return null;
  }

  @Override
  public NumberInterface evaluateModuloOperator(NumberInterface pRight) {
      // TODO Auto-generated method stub
      return null;
  }

  @Override
  public NumberInterface evaluateAndOperator(NumberInterface pRight) {
      // TODO Auto-generated method stub
      return null;
  }

  @Override
  public NumberInterface evaluateLessOperator(NumberInterface pRight) {
      // TODO Auto-generated method stub
      return null;
  }

  @Override
  public NumberInterface evaluateLessEqualOperator(NumberInterface pRight) {
      // TODO Auto-generated method stub
      return null;
  }

  @Override
  public NumberInterface evaluateEqualOperator(NumberInterface pRight) {
      // TODO Auto-generated method stub
      return null;
  }
}
