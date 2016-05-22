import java.awt.Point;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import net.datastructures.AdjacencyMapGraph;
import net.datastructures.Vertex;

/** @author Josh Kerber and Amos Cariati for CS10 Lab4
 * Shortest Paths Lab
 * 
 * RoadMap Class extends AdjacenyMapGraph with Vertexes as City and Edges as Highway
 * It reads the two supplied files containing information regarding city locations
 * and highways between the cities with weights regarding both distance and time.
 * RoadMap uses this information to create new City objects and add them to a Map 
 * referenced by their names, then adds Highway edges between those Cities
 * This object is an undirected graph
 */

public class RoadMap extends AdjacencyMapGraph<City, Highway> {
	private Map<String, Vertex<City>> vertices = new HashMap<String, Vertex<City>>();	//create new HashMap to store City objects and their names
	
	/**
	 * Constructor for RoadMap class
	 * @param vertexFile is the input file for Cities
	 * @param edgeFile is the input file for the Highways between cities
	 */
	public RoadMap(String vertexFile, String edgeFile) {
		super(false);						//set as undirected graph
		try {
			readVertexFile(vertexFile);		//method call to read file containing cities
			readEdgeFile(edgeFile);			//method call to read file containing highways
		} 
		catch (IOException e) {			//Catches errors regarding files being referenced incorrectly
			System.out.println("File read error. Exception: " + e);
		}
	}
	
	/**
	 * Reads input file for City vertexes and adds them to the vertices HashMap
	 * @param fileName is the input file for Cities
	 * @throws IOException if input file is incorrectly entered or does not exist
	 */
	private void readVertexFile(String fileName) throws IOException {
		BufferedReader inputFile = null;	//initialize a BufferedReader to read the file line by line
		try {
			inputFile = new BufferedReader(new FileReader(fileName));	//create new reader inputFile
			
			while (inputFile.ready()) {			//while there is something to read
				String[] data = inputFile.readLine().split(",");		//splits the file line by line
				
				//inserts new vertex into map (converts number strings to ints), returns vertex
				//adds new City vertex to the HashMap using name and location from file
				vertices.put(data[0], insertVertex(new City(data[0], Integer.parseInt(data[1]), Integer.parseInt(data[2]))));
		
			}
		}
		catch (FileNotFoundException e) {		//catches errors regarding files being referenced incorrectly
			System.out.println("Exception: " + e);
		}
		finally {
			inputFile.close();	//close the file that was being read
		}
	}
	
	/**
	 * Reads input file for Highway edges and adds them to this object, an AdjacencyMapGraph
	 * @param fileName is the input file for Highways
	 * @throws IOException if the input file is incorrectly entered or does not exist
	 */
	private void readEdgeFile(String fileName) throws IOException {
		BufferedReader inputFile = null;	//initializes a BufferedReader to read the file line by line
		try {
			inputFile = new BufferedReader(new FileReader(fileName));	//create new reader inputFile
			while (inputFile.ready()) {		//while there is something to read
				String[] data = inputFile.readLine().split(",");		//splits the file line by line
				
				//creates new Highway object accounting for their weights
				Highway newHighway = new Highway(Double.parseDouble(data[2]), Double.parseDouble(data[3]), Double.parseDouble(data[4]));
				//adds the new Highway edge between two cities from the HashMap to this graph
				insertEdge(vertices.get(data[0]), vertices.get(data[1]), newHighway);	
			}
		} 
		catch (FileNotFoundException e) {		//catches errors regarding files being referenced incorrectly
			System.out.println("Exception: " + e);
		}
		finally {
			inputFile.close();	//close the file that was being read
		}
	}
	
	/**
	 * Determines if a city in this graph is at point @param p 
	 * @return the vertex that point @param p in at or null if not at a vertex
	 */
	public Vertex<City> cityAt(Point p) {
		
		// creates iterator for map of vertices in this object
		Set<String> cities = vertices.keySet(); // cities now contains all keys of map
		Iterator<String> iter = cities.iterator(); // iterator for map
		
		// while loop terminates when iterator has gone through all cities
		while (iter.hasNext()) {
			String city = iter.next(); // sets current city
			// if vertex in map's city element is near point p
			if ((vertices.get(city)).getElement().isNear(p)) // implements isNear method of city object
				return vertices.get(city); // returns vertex point p is near
		}
		return null; // return found if no vertex found - no vertex in graph is at point p
	}
}