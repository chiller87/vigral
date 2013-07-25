package de.chiller.vigral.graph;

import org.apache.commons.collections15.Factory;






/**
 * this class represents a single edge of a graph
 * @author Timmae
 *
 */
public class Edge extends GraphElement implements Comparable<Edge> {
	
	
	/**
	 * a unique identifier for the edge
	 */
	private int mID;
	/**
	 * the weight of the edge
	 */
	private double mWeight;
	
	private double mMinCapacity;
	private double mMaxCapacity;
	
	/**
	 * the start vertex
	 */
	private Vertex mStart;
	/**
	 * the end vertex
	 */
	private Vertex mEnd;
	/**
	 * a boolean that determines if this edge is directed or not
	 */
	private boolean mIsDirected;
	
	
	private Edge(int id, Vertex start, Vertex end, boolean directed) {
		this(id, 1.0d, 1.0d, 1.0d, start, end, directed, ElementState.UNVISITED);
	}
	
//	/**
//	 * constructs the edge
//	 * @param id the identifier
//	 * @param weight the weight
//	 * @param start the start vertex
//	 * @param end the end vertex
//	 * @param directed true if it is an directed edge and false else
//	 */
//	private Edge(int id, double weight, Vertex start, Vertex end, boolean directed) {
//		this(id, weight, start, end, directed, ElementState.UNVISITED);
//	}
	
	/**
	 * constructs the edge
	 * @param id the identifier
	 * @param weight the weight
	 * @param start the start vertex
	 * @param end the end vertex
	 * @param directed true if it is an directed edge and false else
	 * @param state the state of the edge
	 */
	private Edge(int id, double weight, double minCapacity, double maxCapacity, Vertex start, Vertex end, boolean directed) {
		this(id, weight, minCapacity, maxCapacity, start, end, directed, ElementState.UNVISITED);
	}
	
	private Edge(int id, double weight, double minCapacity, double maxCapacity, Vertex start, Vertex end, boolean directed, ElementState state) {
		super();
		mID = id;
		mWeight = weight;
		mMinCapacity = minCapacity;
		mMaxCapacity = maxCapacity;
		mStart = start;
		mEnd = end;
		mIsDirected = directed;
		mState = state;
	}
	
	/**
	 * copy constructor
	 * @param e the edge to be copied
	 */
	public Edge(Edge e) {
		super(e);
		mID = e.getId();
		mWeight = e.getWeight();
		mStart = new Vertex(e.getStartVertex());
		mEnd = new Vertex(e.getEndVertex());
		mIsDirected = e.isDirected();
	}
	
	/**
	 * a function that indicates if the edge is directed or not
	 * @return returns true if the edge is directed and false otherwise
	 */
	public boolean isDirected() {
		return mIsDirected;
	}
	
	/**
	 * getter for the weight
	 * @return returns the weight as an integer
	 */
	public double getWeight() {
		return mWeight;
	}
	
	/**
	 * setter for the weight
	 * @param weight the integer representation of the weight
	 */
	public void setWeight(double weight) {
		mWeight = weight;
	}
	
	public double getMinCapacity() {
		return mMinCapacity;
	}
	
	public void setMinCapacity(double capacity) {
		mMinCapacity = capacity;
	}
	
	public double getMaxCapacity() {
		return mMaxCapacity;
	}
	
	public void setMaxCapacity(double capacity) {
		mMaxCapacity = capacity;
	}
	
	/**
	 * getter for the start vertex
	 * @return returns the start vertex
	 */
	public Vertex getStartVertex() {
		return mStart;
	}
	
	/**
	 * getter for the end vertex
	 * @return returns the end vertex
	 */
	public Vertex getEndVertex() {
		return mEnd;
	}
	
	/**
	 * to get the other end of the edge
	 * @param v the one end of the edge as a Vertex
	 * @return returns the other end of this edge as a vertex
	 */
	public Vertex getOtherEnd(Vertex v) {
		if(mStart == v)
			return mEnd;
		else if(mEnd == v)
			return mStart;
		
		return null;
	}

	/**
	 * returns a string representation of the edge
	 */
	public String toString() {
		return "E"+ mID +" ("+ mStart.toString() +", "+ mEnd.toString() +")";
	}
	
	/**
	 * 
	 * @return returns the id of the edge
	 */
	public int getId() {
		return mID;
	}
	
	
	public String[] toStringArray() {
		String[] list = new String[7];
		
		list[0] = ""+ mID;
		list[1] = ""+ mWeight;
		list[2] = ""+ mMinCapacity;
		list[3] = ""+ mMaxCapacity;
		list[4] = ""+ mStart.getId();
		list[5] = ""+ mEnd.getId();
		list[6] = ""+ mIsDirected;
		
		return list;
	}
	
	
	
	// singleton factory to create an edge
	public static class EdgeFactory implements Factory<Edge> {
		
		private static int IDCOUNT = 0;
		private static EdgeFactory mInstance = new EdgeFactory();
		
		public static void resetIdCounter() {
			IDCOUNT = 0;
		}
		
		private void updateIdCount(int val) {
			if(val >= IDCOUNT)
				IDCOUNT = val + 1;
		}
		
		/**
		 * switched the constructor off
		 */
		private EdgeFactory() {}
		
		/**
		 * gets an instance of the edge factory
		 * @return returns the edge factory
		 */
		public static EdgeFactory getInstance() {
			return mInstance;
		}

		
		public Edge copyEdge(Edge e, Vertex start, Vertex end) {
			return new Edge(e.getId(), e.getWeight(), e.getMinCapacity(), e.getMaxCapacity(), start, end, e.isDirected(), e.getState());
		}
		
		public Edge parseEdge(String[] strEdge, Vertex start, Vertex end) {
			int id = Integer.parseInt(strEdge[0]);
			updateIdCount(id);
			return new Edge(id, Double.parseDouble(strEdge[1]), Double.parseDouble(strEdge[2]), 
					Double.parseDouble(strEdge[3]), start, end, Boolean.parseBoolean(strEdge[6]));
		}

		@Override
		public Edge create() {
			return null;
		}
		
		public Edge create(Vertex start, Vertex end, boolean isDirected) {
			return new Edge(IDCOUNT++, start, end, isDirected);
		}
	}



	@Override
	public int compareTo(Edge e) {
		return Integer.compare(mID, e.getId());
	}


	

}
