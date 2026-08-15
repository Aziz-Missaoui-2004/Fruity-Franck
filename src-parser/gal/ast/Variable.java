package gal.ast;

public class Variable extends Parameter {

	int index;

	public final short UNKNOWN = 0;
	public final short ENTITY = 1;
	public final short DIRECTION = 2;
	public final short CATEGORY = 3;
	short type;

	// CONSTRUCTOR

	public Variable() {
		this(0);
		this.type = UNKNOWN;
	}

	public Variable(int i) {
		this.index = i;
		this.type = ENTITY;
	}

	public static Variable underscore() {
		return new Variable(-1);
	}

	// MODIFIER

	public void entity() {
		this.type = ENTITY;
	}

	public void direction() {
		this.type = DIRECTION;
	}

	public void category() {
		this.type = CATEGORY;
	}

	// OPPOSITE

	boolean opposite;

	public void opposite() {
		opposite = !opposite;
	}

	// EXPORT

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (index == 0)
			switch (type) {
			case UNKNOWN:
				sb.append("_");
				break;
			case DIRECTION:
				sb.append("d");
				break;
			case CATEGORY:
				sb.append("c");
				break;
			default:
				sb.append("?");
				break;
			}
		else {
			sb.append("$" + index);
			switch (type) {
			case ENTITY:
				break;
			case DIRECTION:
				sb.append(".d");
				break;
			case CATEGORY:
				sb.append(".c");
				break;
			default:
				sb.append(".?");
			}
		}
		if (opposite)
			return "opposite(" + sb + ")";
		else
			return sb.toString();
	}


	// VISITOR

	@Override
	Object accept(iVisitor visitor) {
		return visitor.visit(this);
	}

}
