package gal.ast;

import java.util.LinkedList;

public class AST extends Node {

	public LinkedList<Automaton> aut_list;

	public AST(LinkedList<Automaton> aut_list) {
		this.aut_list = aut_list;
	}

	public Object accept(iVisitor visitor) {
		visitor.enter(this);
		LinkedList<Object> object_list = new LinkedList<Object>();
		for (Automaton aut : this.aut_list) {
			Object o = aut.accept(visitor);
			object_list.add(o);
		}
		visitor.exit(this);
		return visitor.build(this, object_list);
	}

}