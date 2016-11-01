/**
 * Class that represents a pair of values.
 * In this case, Cartesian coordinates
 * @author Team 14
 *
 * @param <X>
 * @param <Y>
 */
public class Pair<X, Y> {
	X x;
	Y y;

	public Pair(X x, Y y) {
		this.x = x;
		this.y = y;
	}
	
	public String toString() {
		return x + " " + y;
		
	}
	
	@Override
	public boolean equals(Object obj) {
	    if (obj == null) return false;
	    if (obj == this) return true;
	    if (!(obj instanceof Pair)) return false;
	    Pair<X,Y> o = (Pair<X,Y>) obj;
	    return o.x == this.x && o.y == this.y;
	}
}
