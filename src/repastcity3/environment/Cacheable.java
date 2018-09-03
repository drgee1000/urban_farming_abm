
package repastcity3.environment;

/**
 * Used by any class which has static cached objects. Static caches must be cleared at the start of
 * each simulation or they will persist over multiple simulation runs unless Simphony is restarted. 
 * @author Nick Malleson
 *
 */
public interface Cacheable {
	
	void clearCaches();

}
