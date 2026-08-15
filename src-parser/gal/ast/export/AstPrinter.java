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
 *      Author: Dr. Michaël Périn, Univ. Grenobles-Alpes
 */

package gal.ast.export;

import java.util.List;

import gal.ast.AST;
import gal.ast.Actions;
import gal.ast.Automaton;
import gal.ast.Behaviour;
import gal.ast.BinaryOp;
import gal.ast.Category;
import gal.ast.Condition;
import gal.ast.Direction;
import gal.ast.FunCall;
import gal.ast.IntDegree;
import gal.ast.IntPercent;
import gal.ast.iVisitor;
import gal.ast.Key;
import gal.ast.Mode;
import gal.ast.Node;
import gal.ast.State;
import gal.ast.Transition;
import gal.ast.UnaryOp;
import gal.ast.Underscore;
import gal.ast.Variable;
import gal.ast.IntValue;
import gal.util.Dot;

public class AstPrinter implements iVisitor {

	public AstPrinter() {
		nodes = new StringBuilder();
		edges = new StringBuilder();
	}

	// FIELDS

	StringBuilder nodes;
	StringBuilder edges;

	// EXPORT

	public String to_dot() {
		return Dot.graph("ast", nodes + "\n" + edges);
	}

	// NODES

	protected void dot_node(int id, String label, String options) {
		nodes.append(Dot.declare_node(id, label, options));
	}

	protected void dot_non_terminal_node(int id, String label) {
		dot_node(id, label, "shape=box, fontsize=8");
	}

	protected void dot_terminal_node(int id, String label) {
		dot_node(id, Dot.asString(label), "shape=none, fontname=times,  fontsize=12, fontcolor=blue");
	}

	protected void dot_keyword_node(int id, String label) {
		dot_node(id, label, "shape=none, fontname=comic, fontsize=12, fontcolor=orange");
	}

	// TERMINAL, NON TERMINAL and EDGES

	protected void edge(int src_id, int tgt_id) {
		edges.append(Dot.edge(src_id, tgt_id));
	}

	protected void non_terminal(Node node, String name) {
		dot_non_terminal_node(node.id, name);
	}

	protected void identifier(Node node, String name) {
		dot_non_terminal_node(node.id, name);
		edge(node.id, -node.id);
		dot_terminal_node(-node.id, node.toString());
	}

	protected void keyword(Node node, String name) {
		dot_non_terminal_node(node.id, name);
		edge(node.id, -node.id);
		dot_keyword_node(-node.id, node.toString());
	}

	protected void subtree(Node node, List<Integer> son_ids) {
		for (Integer id : son_ids) {
			edge(node.id, id);
		}
	}

	// REQUIRED BY INTERFACE IVisitor

	// BINOP

	public void enter(BinaryOp binop) {
	}

	public void visit(BinaryOp binop) {
	}

	public void exit(BinaryOp binop) {
	}

	public Object build(BinaryOp binop, Object left, Object right) {
		non_terminal(binop, "BinaryOp");
		edge(binop.id, (Integer) left);
		edge(binop.id, (Integer) right);
		return binop.id;
	}

	// UNOP

	public void enter(UnaryOp unop) {
	}

	public void exit(UnaryOp unop) {
	}

	public Object build(UnaryOp operator, Object expr) {
		non_terminal(operator, "UnaryOp");
		edge(operator.id, (Integer) expr);
		return operator.id;
	}

	// FUNCALL

	public void enter(FunCall funcall) {
		non_terminal(funcall, "FunCall");
		edge(funcall.id, -funcall.id);
		dot_keyword_node(-funcall.id, funcall.name);
	}

	public void visit(FunCall funcall) {
	}

	public void exit(FunCall funcall) {
	}

	public Object build(FunCall funcall, List<Object> parameters) {
		subtree(funcall, (List<Integer>) (Object) parameters);
		return funcall.id;
	}

	// ARGUMENTS

	public Object visit(Underscore u) {
		return null;
	}

	public Object visit(Category terminal) {
		keyword(terminal, "Category");
		return terminal.id;
	}

	public Object visit(Direction terminal) {
		keyword(terminal, "Direction");
		return terminal.id;
	}

	public Object visit(Key terminal) {
		keyword(terminal, "Key");
		return terminal.id;
	}

	@Override
	public Object visit(IntPercent terminal) {
		keyword(terminal, "%");
		return terminal.id;
	}

	@Override
	public Object visit(IntDegree terminal) {
		keyword(terminal, "°");
		return terminal.id;
	}

	public Object visit(IntValue terminal) {
		keyword(terminal, "Int");
		return terminal.id;
	}


	@Override
	public Object visit(Variable terminal) {
		keyword(terminal, "Variable");
		return terminal.id;
	}

	// ACTION

	public void enter(Actions action) {
	}

	public void visit(Actions action) {
	}

	public void exit(Actions action) {
	}

	public Object build(Actions action, String operator, List<Object> funcalls) {
		non_terminal(action, "Action");
		subtree(action, (List<Integer>) (Object) funcalls);
		return action.id;
	}

	// BEHAVIOUR

	public Object visit(Behaviour behaviour, List<Object> transitions) {
		non_terminal(behaviour, "Behaviour");
		subtree(behaviour, (List<Integer>) (Object) transitions);
		return behaviour.id;
	}

	public Object visit(State state) {
		identifier(state, "State");
		return state.id;
	}

	// TRANSITION

	public void enter(Transition transition) {
	}

	public void exit(Transition transition) {
	}

	public Object build(Transition transition, Object condition, Object action, Object target) {
		non_terminal(transition, "Transition");
		edge(transition.id, (Integer) condition);
		edge(transition.id, (Integer) action);
		edge(transition.id, (Integer) target);
		return transition.id;
	}

	// AUTOMATON

	public void enter(Automaton automaton) {
		non_terminal(automaton, "Automaton");
		edge(automaton.id, -automaton.id);
		dot_terminal_node(-automaton.id, automaton.name);
	}

	public void exit(Automaton automaton) {
	}

	public Object build(Automaton automaton, Object initial_state, List<Object> modes) {
		edge(automaton.id, (Integer) initial_state);
		subtree(automaton, (List<Integer>) (Object) modes);
		return automaton.id;
	}

	// AST

	public void enter(AST ast) {
	}

	public void exit(AST ast) {
	}

	public Object build(AST ast, List<Object> automata) {
		non_terminal(ast, "AST");
		subtree(ast, (List<Integer>) (Object) automata);
		return ast.id;
	}

	// MODE

	public void enter(Mode mode) {
	}

	public void visit(Mode mode) {
	}

	public void exit(Mode mode) {
	}

	public Object build(Mode mode, Object state, Object behaviour) {
		non_terminal(mode, "Mode");
		edge(mode.id, (Integer) state);
		edge(mode.id, (Integer) behaviour);
		return mode.id;
	}

	// CONDITION

	public void enter(Condition condition) {
	}

	public void exit(Condition condition) {
	}

	public Object build(Condition condition, Object expr) {
		non_terminal(condition, "Condition");
		edge(condition.id, (Integer) expr);
		return condition.id;
	}
}
