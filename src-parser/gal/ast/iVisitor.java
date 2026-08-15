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

import java.util.List;

public interface iVisitor {

  public Object visit(Category cat);

  public Object visit(Direction dir);
  
  public Object visit(IntPercent per);
  
  public Object visit(IntDegree deg);

  public Object visit(Key key);

  public Object visit(IntValue v);

  public Object visit(Underscore u);
 
  public Object visit(Variable v);
  

  // FUNCALL

  public void enter(FunCall funcall);

  public void visit(FunCall funcall);

  public void exit(FunCall funcall);

  public Object build(FunCall funcall, List<Object> parameters);

  // BINOP

  public void enter(BinaryOp binop);

  public void visit(BinaryOp binop);

  public void exit(BinaryOp binop);

  public Object build(BinaryOp binop, Object left, Object right);

  // UNOP

  public void enter(UnaryOp unop);

  public void exit(UnaryOp unop);

  public Object build(UnaryOp unop, Object expression);

  // STATE

  public Object visit(State state);

  // MODE

  public void enter(Mode mode);

  public void visit(Mode mode);

  public void exit(Mode mode);

  public Object build(Mode mode, Object source_state, Object behaviour);

  // BEHAVIOUR

  public Object visit(Behaviour behaviour, List<Object> transitions);

  // CONDITION

  public void enter(Condition condition);

  public void exit(Condition condition);

  public Object build(Condition condition, Object expression);

  // ACTION

  public void enter(Actions action);

  public void visit(Actions action);

  public void exit(Actions action);

  public Object build(Actions action, String operator, List<Object> funcalls);

  // TRANSITION

  public void enter(Transition transition);

  public void exit(Transition transition);

  public Object build(Transition transition, Object condition, Object action, Object target_state);

  // AUTOMATON

  public void enter(Automaton automaton);

  public void exit(Automaton automaton);

  public Object build(Automaton automaton, Object initial_state, List<Object> modes);

  // AST

  public void enter(AST ast);

  public void exit(AST ast);

  public Object build(AST ast, List<Object> automata);

}
