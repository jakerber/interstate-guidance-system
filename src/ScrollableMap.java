/**
 * ScrollableMap.java
 * Class for a scrollable roadmap that responds to user actions.
 * For CS 10 Lab Assignment 4.
 * 
 * @author Yu-Han Lyu, Tom Cormen
 * 
 * @author Josh Kerber and Amos Cariati
 * 
 * Implements Dijkstra's algorithm
 */

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Pattern;

import net.datastructures.*;

public class ScrollableMap extends JLabel implements Scrollable,
  MouseMotionListener, MouseListener {
  private static final long serialVersionUID = 1L;
  
  // The first two instance variables are independent of our roadmap application.
  private int maxUnitIncrement = 1;         // increment for scrolling by dragging
  private boolean missingPicture = false;   // do we have an image to display
  private JLabel infoLabel;                 // where to display the result, in words
  private JButton destButton;               // the destination button, so that it can be enabled
  private RoadMap roadmap;                  // the roadmap
  
  // set by user
  private Vertex<City> sourceCity, destinationCity; // current condition of the map // finds and displays path if both do not equal null
  private boolean findingSource = true, useDistance = true; // identifies if the next click will find a source or a destination city
  
  /** Instance variables for Dijkstra's algorithm -- @method Dijkstras
  * Helps find shortest path from a source vertex in graph roadmap to all other vertices in that graph based on edge weight
  * 
  * Instance variables...
  *  @variable queue priority queue will contain vertices with distance values from source
  *  @variable pqTokens keeps track of vertices in priority queue while their distance values are being updated, to help add back to queue
  *  @variable distanceMap and @variable travelTimeMap keep track of path length values from source
  *  
  *  @variable prevMap contains a vertex as a key and its previous vertex (in the shortest path) as a value...
  *  	...prevMap will be updated every time a new source city is clicked on the map
  */
  private HeapAdaptablePriorityQueue<Double, Vertex<City>> queue = new HeapAdaptablePriorityQueue<Double, Vertex<City>>();
  private ProbeHashMap<Vertex<City>, Entry<Double,Vertex<City>>> pqTokens = new ProbeHashMap<>();
  private HashMap<Vertex<City>, Double> pathWeight;
  private HashMap<Vertex<City>, Vertex<City>> prevMap = new HashMap<Vertex<City>, Vertex<City>>();
  
  /**
   * Constructor.
   * @param i the highway roadmap image
   * @param m increment for scrolling by dragging
   * @param infoLabel where to display the result
   * @param destButton the destination button
   * @param roadmap the RoadMap object, a graph
   */
  public ScrollableMap(ImageIcon i, int m, JLabel infoLabel, JButton destButton, RoadMap roadmap) {
    super(i);
    if (i == null) {
      missingPicture = true;
      setText("No picture found.");
      setHorizontalAlignment(CENTER);
      setOpaque(true);
      setBackground(Color.white);
    }
    maxUnitIncrement = m;
    this.infoLabel = infoLabel;
    this.destButton = destButton;
    this.roadmap = roadmap;

    // Let the user scroll by dragging to outside the window.
    setAutoscrolls(true);         // enable synthetic drag events
    addMouseMotionListener(this); // handle mouse drags
    addMouseListener(this);
    this.requestFocus();
    findSource();     // start off by having the user click a source city
  }

  // Methods required by the MouseMotionListener interface:
  @Override
  public void mouseMoved(MouseEvent e) { }

  @Override
  public void mouseDragged(MouseEvent e) {
    // The user is dragging us, so scroll!
    Rectangle r = new Rectangle(e.getX(), e.getY(), 1, 1);
    scrollRectToVisible(r);
  }

  // Draws the map and shortest paths, as appropriate.
  // If shortest paths have been computed, draws either the entire shortest-path tree
  // or just a shortest path from the source vertex to the destination vertex.
  @Override
  public void paintComponent(Graphics page) {
    Graphics2D page2D = (Graphics2D) page;
    setRenderingHints(page2D);
    super.paintComponent(page2D);
    Stroke oldStroke = page2D.getStroke();  // save the current stroke
    page2D.setStroke(new BasicStroke(5.0f, BasicStroke.CAP_BUTT,
        BasicStroke.JOIN_MITER));
    
    /**
     * If there is a current source city but no destination city selected
     * draw shortest paths from source city to all destination cities using prevMap
     * prevMap already updated for current source city in @method mouseClicked 
     */
    if (sourceCity != null && destinationCity == null) {
    	// for each vertex in the roadmap graph
    	for (Vertex<City> v : roadmap.vertices()) {
    		// check if there is a previous vertex from current vertex v
    		if (prevMap.get(v) != null)
    			// draw line on the page from current vertex v to it's predecessor 
    			page2D.drawLine(v.getElement().getLocation().x, v.getElement().getLocation().y, 
    					prevMap.get(v).getElement().getLocation().x, prevMap.get(v).getElement().getLocation().y);
    	}
    }
    
    /**
     * If there is a current source city and current destination city selected
     * draw shortest path from source city to destination city using prevMap
     * prevMap already updated for current source city in @method mouseClicked 
     */
	if (sourceCity != null && destinationCity != null) {	
		// set current vertex city to the current destination city
		Vertex<City> current = destinationCity;
		// while loop terminates when prevMap identifies that there is no additional predecessor from the current vertex
		while (prevMap.get(current) != null) {
			// draw a line on the page from current vertex to it's predecessor city
			page2D.drawLine(current.getElement().getLocation().x, current.getElement().getLocation().y, 
				prevMap.get(current).getElement().getLocation().x, prevMap.get(current).getElement().getLocation().y);
			// current vertex set to its predecessor in preparation to draw next line in path
			current = prevMap.get(current);
		}
	}
    page2D.setStroke(oldStroke);    // restore the saved stroke
  }

  // Enable all rendering hints to enhance the quality.
  public static void setRenderingHints(Graphics2D page) {
    page.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
        RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
    page.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON);
    page.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING,
        RenderingHints.VALUE_COLOR_RENDER_QUALITY);
    page.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
        RenderingHints.VALUE_INTERPOLATION_BICUBIC);
  }

  // Methods required by the MouseListener interface.

  // When the mouse is clicked, find which vertex it's over.
  // If it's over a vertex and we're finding the source,
  // record the source, clear the destination, enable the destination
  // button, and find and draw the shortest paths from the source.
  // If it's over a vertex and we're finding the destination, record
  // the destination, and find and draw a shortest path from the source
  // to the destination.

  public void mouseClicked(MouseEvent e) {
	  
	// if user is finding a source // and the point the user clicked contains a vertex in roadmap
	if (findingSource && (roadmap.cityAt(e.getPoint()) != null)) {
		// the point the user clicked becomes the source city of the current map state
		sourceCity = roadmap.cityAt(e.getPoint());
		// once a source city is selected there becomes no destination city (until user clicks one)
		destinationCity = null;
		
		// run Dijkstra's algorithm to find shortest paths from source city to all other vertices in the map
		// Dijkstras method does not return anything // updates the instance variable 'prevMap' which will be used to draw path accordingly
		Dijkstras(sourceCity);
		
		if (useDistance)
			// info label updated to tell user the current state of the map
			infoLabel.setText("Shortest paths from " + sourceCity.getElement().getName() + " displayed using DISTANCE. "
				+ "Please select destination.");
		
		if (!useDistance)
			// info label updated to tell user the current state of the map
			infoLabel.setText("Shortest paths from " + sourceCity.getElement().getName() + " displayed using TIME. "
				+ "Please select destination.");
				
		// destination button becomes activated once a source city is selected
		destButton.setEnabled(true);
	}
	
	// if user is not finding a source -- finding a destination // and the point the user clicked contains a vertex in roadmap
	if (!findingSource && (roadmap.cityAt(e.getPoint()) != null)) {
		// the point the user clicked becomes the destination city of the current map state
		destinationCity = roadmap.cityAt(e.getPoint());
				
		if (useDistance) {
			String miles = Double.toString(pathWeight.get(destinationCity));
			String[] placeHolder = miles.split(Pattern.quote("."));
			if (placeHolder[1].length() > 2) {
				placeHolder[1] = placeHolder[1].substring(0, 2);
				miles = placeHolder[0] + "." + placeHolder[1];
			}
			// info label updated to tell user the current state of the map
			infoLabel.setText("Shortest path displayed from " + sourceCity.getElement().getName() + " to " + 
					destinationCity.getElement().getName() + " using DISTANCE. Length: " + miles + " miles.");
		}
		
		if (!useDistance) {
			String timeString = Double.toString(pathWeight.get(destinationCity));
			String[] placeHolder = timeString.split(Pattern.quote("."));
			String hoursString = placeHolder[0];
			if (Integer.parseInt(hoursString) == 1)
				hoursString = hoursString + " hour and ";
			else
				hoursString = hoursString + " hours and ";
			
			String minutesString = placeHolder[1];
			if (placeHolder[1].length() > 2) {
				Integer minutesInt = (int) (Integer.parseInt(placeHolder[1].substring(0, 2)) * .60);
				if (minutesInt == 1)
					minutesString = minutesInt.toString() + " minute.";
				else
					minutesString = minutesInt.toString() + " minutes.";
			}
			else {
				Integer minutesInt = ((int) ((int) Integer.parseInt(placeHolder[1]) * .60));
				if (minutesInt == 1)
					minutesString = minutesInt.toString() + " minute.";
				else
					minutesString = minutesInt.toString() + " minutes.";
			}
		
			// info label updated to tell user the current state of the map
			infoLabel.setText("Shortest path displayed from " + sourceCity.getElement().getName() + " to " + 
					destinationCity.getElement().getName() + " using TIME. Travel time: " + hoursString + minutesString);
		}
		
		
	}
	// update graphics
	repaint();
  }

  public void mousePressed(MouseEvent e) { }
  public void mouseReleased(MouseEvent e) { }
  public void mouseEntered(MouseEvent e) { }
  public void mouseExited(MouseEvent e) { }

  // Return the preferred size of this component.
  @Override
  public Dimension getPreferredSize() {
    if (missingPicture)
      return new Dimension(320, 480);
    else
      return super.getPreferredSize();
  }

  // Needs to be here.
  @Override
  public Dimension getPreferredScrollableViewportSize() {
    return getPreferredSize();
  }

  // Needs to be here.
  @Override
  public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation,
      int direction) {
    // Get the current position.
    int currentPosition = 0;
    if (orientation == SwingConstants.HORIZONTAL)
      currentPosition = visibleRect.x;
    else
      currentPosition = visibleRect.y;

    // Return the number of pixels between currentPosition
    // and the nearest tick mark in the indicated direction.
    if (direction < 0) {
      int newPosition = currentPosition - (currentPosition / maxUnitIncrement)
          * maxUnitIncrement;
      return (newPosition == 0) ? maxUnitIncrement : newPosition;
    }
    else
      return ((currentPosition / maxUnitIncrement) + 1) * maxUnitIncrement
          - currentPosition;
  }

  // Needs to be here.
  @Override
  public int getScrollableBlockIncrement(Rectangle visibleRect,
      int orientation, int direction) {
    if (orientation == SwingConstants.HORIZONTAL)
      return visibleRect.width - maxUnitIncrement;
    else
      return visibleRect.height - maxUnitIncrement;
  }

  // Needs to be here.
  @Override
  public boolean getScrollableTracksViewportWidth() {
    return false;
  }

  // Needs to be here.
  @Override
  public boolean getScrollableTracksViewportHeight() {
    return false;
  }
  
  // Needs to be here.
  public void setMaxUnitIncrement(int pixels) {
    maxUnitIncrement = pixels;
  }

  // Called when the source button is pressed.
  public void findSource() {
	  // user must be finding a source city now // set findingSource variable to true establishing such state
	  findingSource = true;
	  // destination button becomes inactive now (until after the user clicks a source city)
	  destButton.setEnabled(false);
  }

  // Called when the destination button is pressed.
  public void findDest() {
	  // user must be finding a destination city now // set findingSource variable to false establishing such state
	  findingSource = false;
  }

  // Called when the time button is pressed.  Tells the roadmap to use time
  // for edge weights, and finds and draws shortest paths.
  public void useTime() {
	// user must be finding paths based on time now // set useDistance variable to false establishing such state
    useDistance = false;
    
    // if a source city has been selected
    if (sourceCity != null) {
    	Dijkstras(sourceCity); // re-run to find shortest paths from travel time now
    	if (destinationCity == null) // if a destination city has not been selected
    		// update label establishing use of time
    		infoLabel.setText("Shortest paths from " + sourceCity.getElement().getName() + " displayed using TIME. "
    				+ "Please select destination.");
    	else // if a destination city has been selected
    		// update label establishing use of time
    		infoLabel.setText("Shortest path displayed from " + sourceCity.getElement().getName() + " to " + 
					destinationCity.getElement().getName() + " using TIME.");
    	repaint(); // update path graphics
    }
  }

  // Called when the distance button is pressed.  Tells the roadmap to use distance
  // for edge weights, and finds and draws shortest paths.
  public void useDistance() {
	// user must be finding paths based on distance now // set useDistance variable to true establishing such state
    useDistance = true;
    
    // if a source city has been selected
    if (sourceCity != null) {
    	Dijkstras(sourceCity); // re-run to find shortest paths from distance now
    	if (destinationCity == null) // if a destination city has not been selected
    		// update label establishing use of distance
    		infoLabel.setText("Shortest paths from " + sourceCity.getElement().getName() + " displayed using DISTANCE. "
    				+ "Please select destination.");
    	else // if a destination city has been selected
    		// update label establishing use of time
    		infoLabel.setText("Shortest path displayed from " + sourceCity.getElement().getName() + " to " + 
					destinationCity.getElement().getName() + " using DISTANCE.");
    	repaint(); // update path graphics
    }
  }
  
  /**
  * Implement Dijikstra's algorithm on a graph with a given source variable @param sourceCity
  * @return a map that holds the shortest path from the source to all other vertices in the graph
  * Return map will have vertex as a key and its previous vertex (in the shortest path) as a value
  */
  public void Dijkstras(Vertex<City> sourceCity) {
	pathWeight = new HashMap<Vertex<City>, Double>(); // initialize map to contain vertices path weights from the source
	
	// creates iterator for all the vertices in the graph
	Iterable<Vertex<City>> cities = roadmap.vertices(); // cities now contains all keys of map
	Iterator<Vertex<City>> citiesIter = cities.iterator(); // iterator for map

	// loop terminates when the iterator has iterated through all vertices in the map
	while (citiesIter.hasNext()) {
		// set current iterator city as vertex<city> variable "city"
		Vertex<City> city = citiesIter.next();
		
		if (city == sourceCity) // if the beginning of the path
			pathWeight.put(city, 0.0); // source vertex is the beginning of the path -- distance so far = 0
		else 
				// set all vertex distances to infinity to identify it hasn't been analyzed yet
			pathWeight.put(city, Double.POSITIVE_INFINITY); 
			// insert city in pq tokens map with current distance // insert city into the queue with current distance
		pqTokens.put(city, queue.insert(pathWeight.get(city), city)); // entry into queue becomes value of tokens map

		// set previous vertex of all vertices to null
		// no previous until vertex is analyzed
		prevMap.put(city, null);
	}
	
	// begin analyzing and creating path with previous map
	while (!queue.isEmpty()) { // while loop terminates when all vertices have been removed from queue && shortest paths found
		Vertex<City> u = queue.removeMin().getValue(); // pop vertex with shortest path value 
		pqTokens.remove(u); // remove same vertex from tokens map
		
		// follow edges from source
			
		// creates iterator for all the vertices in the graph
		Iterable<Edge<Highway>> edges = roadmap.outgoingEdges(u); // edges now contains all edges leaving vertex u
		Iterator<Edge<Highway>> edgesIter = edges.iterator(); // iterator for map
			
		while (edgesIter.hasNext()) { // while loop terminates when all outgoing edges of vertex u reached
			Edge<Highway> edge = edgesIter.next(); // current edge set
			Vertex<City> v = roadmap.opposite(u, edge); // vertex v is the vertex at the end of that edge, adjacent to vertex u
			
			// relaxation step
			// find out if edge(u, v) is a better path to v
			relax(u, v); 
		}
	}
  }
	
 /**
 * Relaxation step in Dijkstra's algorithm
 * @param vertex of current edge being analyzed
 * @param other vertex of current edge being analyzed
 */
  public void relax(Vertex<City> u, Vertex<City> v) {
	if (useDistance) { // if analyzing path by shortest distance
		// if path from source vertex to v following edge(u, v) is shorter than current distance value from source to v
		if ((pathWeight.get(u) + roadmap.getEdge(u, v).getElement().getDistance()) < pathWeight.get(v)) {
			// shorter path found
			// update v's distance with distance from source vertex to v following edge(u, v)
			pathWeight.put(v, (roadmap.getEdge(u, v).getElement().getDistance() + pathWeight.get(u)));	
			// set v's previous vertex (in path) to vertex u
			prevMap.put(v, u);
			// replace v's distance value in queue
			// reference distance map for new updated shorter distance value
			// reference distance map for help inserting vertex v back into queue -- value of tokens map is entry variable
			queue.replaceKey(pqTokens.get(v), pathWeight.get(v));
		}
	}
	if (!useDistance) { // if analyzing path by shortest travel time
		// if path from source vertex to v following edge(u, v) is shorter than current travel time value from source to v
		if ((pathWeight.get(u) + roadmap.getEdge(u, v).getElement().getTravelTime()) < pathWeight.get(v)) {
			// shorter path found
			// update v's travel time with travel time from source vertex to v following edge(u, v)
			pathWeight.put(v, (roadmap.getEdge(u, v).getElement().getTravelTime() + pathWeight.get(u)));
			// set v's previous vertex (in path) to vertex u
			prevMap.put(v, u);
			// replace v's travelTime value in queue
			// reference travelTime map for new updated shorter travelTime value
			// reference tokens map for help inserting vertex v back into queue -- value of tokens map is entry variable
			queue.replaceKey(pqTokens.get(v), pathWeight.get(v));
		}
	}
  }
}