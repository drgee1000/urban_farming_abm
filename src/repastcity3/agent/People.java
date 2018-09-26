package repastcity3.agent;

import java.util.List;

public interface People extends IAgent {
	public <T> void addToMemory(List<T> objects, Class<T> clazz);
	public List<String> getTransportAvailable();
}
