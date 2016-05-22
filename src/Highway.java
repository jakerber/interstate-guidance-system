 /** @author Josh Kerber and Amos Cariati for CS10 Lab4
  * Shortest Paths Lab
  * 
  * Highway Class acts as Edge connecting city Vertexes in graph of US cities:
  * Contains variables and get methods for distance and travel time in hours
  * and minutes between cities. 
  */

public class Highway {
	private double distance, travelHours, travelMinutes;
	
	/**
	 * Constructor for Highway (Edge) class 
	 * @param d is distance between two city vertices
	 * @param h is number of hours between two city vertices
	 * @param m is number of minutes between two city vertices
	 */
	public Highway(double d, double h, double m) {
		distance = d;			//Distance between two city vertexes stored as a Double
		travelHours = h;		//Distance in hours between two city vertexes stored as a Double
		travelMinutes = m;		//Distance in minutes between two city vertexes stored as a Double
	}
	/**
	 * @return distance between two City vertexes
	 */
	public double getDistance() {
		return distance;
	}
	
	/**
	 * @return number of travel hours between to City vertices
	 */
	public double getTravelHours() {
		return travelHours;
	}
	
	/**
	 * @return number of travel minutes between to City vertices
	 */
	public double getTravelMinutes() {
		return travelMinutes;
	}

	/**
	 * @return total travel time between two City vertices
	 */
	public double getTravelTime() {
		return travelHours + (travelMinutes / 60.0);
	}

	/**
	 * @return this Highway element
	 */
	public Highway getElement() {
		return this;
	}
}