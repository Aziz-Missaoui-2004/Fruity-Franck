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

public class Transition extends Node {

	public Condition condition;
	public Actions action;
	public State target;

	public Transition(Condition condition, Actions action, State target) {
		this.condition = condition;
		this.action = action;
		this.target = target;
	}

	public String toString() {
		return condition.toString() + "? " + action.toString();
	}

	Object accept(iVisitor visitor) {
		visitor.enter(this);
		Object c = condition.accept(visitor);
		Object a = action.accept(visitor);
		visitor.exit(this);
		Object t = target.accept(visitor);
		return visitor.build(this, c, a, t);
	}

}