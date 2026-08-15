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
 *  Created  on: March, 2020
 *  Modified on: June, 2024
 *  Author: Dr. Michael PÉRIN, Verimag / Univ. Grenoble-Alpes
 */

package gal.ast;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import gal.util.Pretty;

public class Actions extends Node {

  public Actions() {
    this.actions = new LinkedList<FunCall>();
    this.operator = ";";
  }

  // FIELD

  public String operator;

  public LinkedList<FunCall> actions;

  // BUILDER

  public void add(FunCall call) {
    actions.add(call);
  }

  public void set(String op) {
    operator = op;
  }

  // REQUIRED BY INTERFACE

  Object accept(iVisitor visitor) {
    visitor.enter(this);
    LinkedList<Object> list = new LinkedList<Object>();
    Iterator<FunCall> iter = actions.iterator();
    while (iter.hasNext()) {
      Object o = iter.next().accept(visitor);
      list.add(o);
      if (iter.hasNext())
        visitor.visit(this);
    }
    visitor.exit(this);
    return visitor.build(this, operator, list);
  }

  // EXPORT

  public String toString() {
    List<String> strings = new LinkedList<String>();
    for (FunCall funcall : this.actions) {
      strings.add(funcall.percent() + funcall.toString());
    }
    return Pretty.separate_with(strings, " " + operator + " ");
  }

}