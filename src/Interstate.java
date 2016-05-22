/**
 * Interstate.java
 * GUI for CS 10 Lab Assignment 4.
 * Creates a window for a roadmap of the interstate highway system.
 * Allows the user to click on buttons to select a source city,
 * a destination city, use distances for edge weights, use time
 * for edge weights, and quit.
 * 
 * @author Yu-Han Lyu and Tom Cormen
 */

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
 
public class Interstate extends JPanel {
  private static final long serialVersionUID = 1L;
  private JLabel infoLabel = new JLabel("No source"); // information back to the user
  private ScrollableMap graphicalMap; // object for the graphical map
  JButton destButton;                 // button for setting the destinaton
  
  // Constructor.  Creates the GUI and sets up listeners for the buttons.
  public Interstate() {
    // Create the GUI components and lay them out.
    JPanel topPanel = new JPanel();
    topPanel.setLayout(new BorderLayout());
    JPanel selectionPanel = new JPanel();
    JButton sourceButton = new JButton("Source");
    destButton = new JButton("Destination");
    destButton.setEnabled(false);     // disabled until there is a source vertex
    JButton distButton = new JButton("Use distance");
    JButton timeButton = new JButton("Use time");
    selectionPanel.add(sourceButton);
    selectionPanel.add(destButton);
    selectionPanel.add(distButton);
    selectionPanel.add(timeButton);
    selectionPanel.add(infoLabel);
    JPanel quitPanel = new JPanel();
    JButton quitButton = new JButton("Quit");
    quitPanel.add(quitButton);
    topPanel.add(selectionPanel, BorderLayout.WEST);
    topPanel.add(quitPanel, BorderLayout.EAST);
    setLayout(new BorderLayout());
    add(topPanel, BorderLayout.NORTH);
    
    // Make listeners for the buttons.
    sourceButton.addActionListener(new SourceButtonListener());
    destButton.addActionListener(new DestButtonListener());
    distButton.addActionListener(new DistButtonListener());
    timeButton.addActionListener(new TimeButtonListener());
    quitButton.addActionListener(new QuitButtonListener());
 
    // Get the image to use.
    ImageIcon interstatesMap = createImageIcon("us-interstates.jpg");
    
    // Make the representation of the roadmap.
    RoadMap roadmap = new RoadMap("src/interstate-cities.csv", "src/interstate-links.csv");
 
    // Set up the scroll pane.
    graphicalMap = new ScrollableMap(interstatesMap, 1, infoLabel, destButton, roadmap);
    JScrollPane pictureScrollPane = new JScrollPane(graphicalMap);
    pictureScrollPane.setPreferredSize(new Dimension(1700, 900));
    pictureScrollPane.setViewportBorder(BorderFactory.createLineBorder(Color.black));
  
    // Put it in this panel.
    add(pictureScrollPane);
    setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
  }
 
  /** Returns an ImageIcon, or null if the path was invalid. */
  protected static ImageIcon createImageIcon(String path) {
    java.net.URL imgURL = Interstate.class.getResource(path);
    if (imgURL != null)
        return new ImageIcon(imgURL);
    System.err.println("Couldn't find file: " + path);
    return null;
  }
 
  /**
   * Create the GUI and show it.  For thread safety,
   * this method should be invoked from the
   * event-dispatching thread.
   */
  private static void createAndShowGUI() {
    // Create and set up the window.
    JFrame frame = new JFrame("Interstate");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
    // Create and set up the content pane.
    JComponent newContentPane = new Interstate();
    newContentPane.setOpaque(true); // content panes must be opaque
        frame.setContentPane(newContentPane);
 
    // Display the window.
    frame.pack();
    frame.setVisible(true);
  }
 
  // The main method.  Gets things started.
  public static void main(String[] args) {
    // Schedule a job for the event-dispatching thread:
    // creating and showing this application's GUI.
    javax.swing.SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        createAndShowGUI();
      }
    });
  }
    
  // Listener for the source button.
  private class SourceButtonListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      graphicalMap.findSource();
      destButton.setEnabled(false);
    }
  }
    
  // Listener for the destination button.
  private class DestButtonListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      graphicalMap.findDest();
    }
  }
  
  // Listener for the distance button.
  private class DistButtonListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      graphicalMap.useDistance();
    }
  }
  
  // Listener for the time button.
  private class TimeButtonListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      graphicalMap.useTime();
    }
  }
  
  // Listener for the quit button.
  private class QuitButtonListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      System.exit(0);
    }
  }
}