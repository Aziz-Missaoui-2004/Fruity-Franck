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

public class Mode extends Node {

	public State state;
	public Behaviour behaviour;

	public Mode(State source, Behaviour behaviour) {
		this.state = source;
		this.behaviour = behaviour;
	}

	Object accept(iVisitor visitor) {
		visitor.enter(this);
		Object s = state.accept(visitor);
		visitor.visit(this);
		Object b = behaviour.accept(visitor);
		visitor.exit(this);
		return visitor.build(this, s, b);
	}
}
