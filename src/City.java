 import java.awt.Point;

/** @author Josh Kerber and Amos Cariati for CS10 Lab4
  * Shortest Paths Lab
  * 
  * City Class acts as Vertex for points in graph of cities:
  * Contains variables and get methods for city name, location and distance 
  * from location that a mouse click will register (Tolerance)
  */

public class City {
	private String name;							//Name of city stored as string
	private Point location = new Point();			//Location of city stored as point
	private static final double TOLERENCE = 8.0;	//Mouse click tolerance size in pixels
	
	/**
	 * Constructor for the City (Vertex) class if location is given as X and Y coordinates
	 * @param n is Name of the City vertex
	 * @param xCoord is the x-coordinate of the City vertex
	 * @param yCoord is the y-coordinate of the City vertex
	 */
	public City(String n, int xCoord, int yCoord) {
		name = n;
		location.x = xCoord;
		location.y = yCoord;
	}
	
	/**
	 * Constructor for the City (Vertex) class if location is given as a Point
	 * @param n is Name of the City vertex
	 * @param p is the Point coordnate of the City vertex
	 */
	public City(String n, Point p) {
		name = n;
		location = p;
	}
	
	/**
	 * @return name of the City vertex
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * @return location of the City vertex
	 */
	public Point getLocation() {
		return location;
	}

	/**
	 * Determines if a mouse click is close enough to a City vertex to register
	 * @param pInput is the Point location input from a mouseClicked event
	 * @return boolean indicating true if a City vertex is within the supplied Tolerance range
	 */
	public boolean isNear(Point pInput) {
		return (pInput.distance(location) <= TOLERENCE); // implements distance method of java point class
	}
	
	/**
	 * @return this City element
	 */
	public City getElement() {
		return this;
	}
}