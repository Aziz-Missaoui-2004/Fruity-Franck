/*
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  Created on: March, 2020
 *      Author: Dr. Michael PÉRIN, Verimag / Univ. Grenoble-Alpes
 */
package gal.ast;

public class Condition extends Node {

  public Condition(Expression expression) {
    this.expression = expression;
  }

  // FIELD

  Expression expression;

  // REQUIRED BY INTERFACE Visitable

  Object accept(iVisitor visitor) {
    visitor.enter(this);
    Object o = expression.accept(visitor);
    visitor.exit(this);
    return visitor.build(this, o);
  }

  // EXPORT

  public String toString() {
    return expression.toString();
  }
}