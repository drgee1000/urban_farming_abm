package repastcity3.environment;

public class Supermarket extends Building {
	@Override
	public String toString() {
		return "Supermarket: " + this.identifier;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Supermarket))
			return false;
		Supermarket b = (Supermarket) obj;
		return this.identifier.equals(b.identifier);
	}
}
