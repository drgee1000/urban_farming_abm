package repastcity3.environment;

import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;

import repastcity3.agent.IAgent;

public class School extends Building{
	
	@Override
	public String toString() {
		return "School: " + this.identifier;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof School))
			return false;
		School b = (School) obj;
		return this.identifier.equals(b.identifier);
	}
}
