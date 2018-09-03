
package repastcity3.environment;

import java.util.Arrays;

import repast.simphony.space.graph.EdgeCreator;

public class NetworkEdgeCreator <T> implements EdgeCreator<NetworkEdge<T>, T> {

	  /**
	   * Creates an Edge with the specified source, target, direction and weight.
	   *
	   * @param source     the edge source
	   * @param target     the edge target
	   * @param isDirected whether or not the edge is directed
	   * @param weight     the weight of the edge
	   * @return the created edge.
	   */
	  public NetworkEdge<T> createEdge(T source, T target, boolean isDirected, double weight) {
	    return new NetworkEdge<T>(source, target, isDirected, weight, Arrays.asList(new String[] {"testingEdgeCreator"}));
	  }

	  /**
	   * Gets the edge type produced by this EdgeCreator.
	   *
	   * @return the edge type produced by this EdgeCreator.
	   */
	@SuppressWarnings("rawtypes")
	public Class<NetworkEdge> getEdgeType() {
	    return NetworkEdge.class;
	  }

}
