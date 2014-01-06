package de.chiller.vigral;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.prefs.Preferences;

import javax.swing.JPanel;

import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.functors.ConstantTransformer;

import de.chiller.vigral.graph.Edge;
import de.chiller.vigral.graph.ElementState;
import de.chiller.vigral.graph.Graph;
import de.chiller.vigral.graph.Vertex;
import de.chiller.vigral.jung.MyColor;
import de.chiller.vigral.jung.MyModalGraphMouse;
import de.chiller.vigral.settings.Settings;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.StaticLayout;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.decorators.AbstractEdgeShapeTransformer;
import edu.uci.ics.jung.visualization.decorators.ConstantDirectionalEdgeValueTransformer;
import edu.uci.ics.jung.visualization.renderers.Renderer;

public class GraphBuilder {
	
	/**
	 * numerical value that indicates the vertices radius
	 */
	private static final int VERTEXRADIUS = 20;
	/**
	 * numerical value that will be considered in modifying the position of the vertices
	 */
	private static final int PADDING = 10;

	private Layout<Vertex, Edge> mLayout;
	/**
	 * the graph, that inherits the vertices and egdes
	 */
	private Graph mGraph;
	private Graph mResultGraph;
	/**
	 * responsible for the visualization of the graph
	 */
	private VisualizationViewer<Vertex, Edge> mVViewer;
	/**
	 * responsible for the GraphMousePlugins (Drawing with the mouse and context menus)
	 */
	private MyModalGraphMouse mGraphMouse;
	
	
	private Settings mSettings = Settings.getInstance();
	
	private Transformer<Vertex, Paint> mVertexLineTransformer = new Transformer<Vertex, Paint>() {
		@Override
		public Paint transform(Vertex arg0) {
			return MyColor.LIGHT_GRAY;
		}
	};
	
	private Transformer<Vertex, Shape> mVertexShapeTransformer = new Transformer<Vertex, Shape>() {
		@Override
		public Shape transform(Vertex v) {
			Ellipse2D circle = new Ellipse2D.Double(-VERTEXRADIUS, -VERTEXRADIUS, 2*VERTEXRADIUS, 2*VERTEXRADIUS);
			//return AffineTransform.getScaleInstance(2, 2).createTransformedShape(circle);
			return circle;
		}
	};

	private Transformer<Vertex, Paint> mVertexPaintTransformer = new Transformer<Vertex, Paint>() {
		@Override
		public Paint transform(Vertex v) {
			if(v.isPicked())
				return Color.decode(Settings.getInstance().getColor(Settings.COLOR_PICKED));
			
			if(v.getCustomColor() != null)
				return v.getCustomColor();
			
			return checkStateForColor(v.getState());
		}
	};
	
	private Transformer<Edge, Paint> mEdgePaintTransformer = new Transformer<Edge, Paint>() {
		@Override
		public Paint transform(Edge e) {
			if(e.getCustomColor() != null)
				return e.getCustomColor();
			
			return checkStateForColor(e.getState());
		}
	};
	
	
	
	
	private Transformer<Edge, String> mEdgeLabelTransformer = new Transformer<Edge, String>() {
		
		/*
		 * workaround!
		 * 
		 * ISSUE:
		 * changing the label size of edges in SettingsDialog will not take effect. Only if you change the view settings, the label size
		 * the edges is updated too.
		 * 
		 * REASON:
		 * the HTML string, that is returned by this transformer. If you return a static, normal, hardcoded string (e.g. "hello") the label
		 * size is updated every time when it is changed via the SettingsDialog.
		 * 
		 * WORKAROUND:
		 * every second time this transformer is asked to transform an edge to a string, append a whitespace. All the other times return just
		 * the desired string.
		 * This will force repainting of the edges because the label has changed
		 */
		private boolean mBOO = false;
		
		@Override
		public String transform(Edge e) {
			String lbl;
			int offset = 0;
			
			if(e.getCustomLabel().equals("")){
				
				lbl = "<html>";
				Settings settings = Settings.getInstance();
				
				
				if(settings.getView(Settings.VIEW_WEIGHT)) {
					offset++;
					double w = e.getWeight();
					if(w % 1 == 0)
						lbl += "w="+ (int) w;
					else
						lbl += "w="+ w;
				}
				if(settings.getView(Settings.VIEW_MIN_CAPACITY)) {
					if(!lbl.equals("<html>"))
						lbl += "<br />";
					offset++;
					double c = e.getMinCapacity();
					if(c % 1 == 0)
						lbl += "min C="+ (int) c;
					else
						lbl += "min C="+ c;
				}
				if(settings.getView(Settings.VIEW_MAX_CAPACITY)) {
					if(!lbl.equals("<html>"))
						lbl += "<br />";
					offset++;
					double c = e.getMaxCapacity();
					if(c % 1 == 0)
						lbl += "max C="+ (int) c;
					else
						lbl += "max C="+ c;
				}
				lbl += "</html>";
				
				
				if(mBOO) {
					mBOO = false;
					lbl += " ";
				}
				else {
					mBOO = true;
				}
			}
			else
			{
				lbl = e.getCustomLabel();
			}
			
			// set the label offset according to the number of lines of the label and edge label font size (center the label)
			mVViewer.getRenderContext().setLabelOffset(offset * mSettings.getLabelSize(Settings.LABEL_EDGE));
			
			return lbl;
		}
	};
	
	private Transformer<Vertex, String> mVertexLabelTransformer = new Transformer<Vertex, String>() {
		@Override
		public String transform(Vertex v) {
			if(v.getLabel().equals(""))
				return "<html>"+ v.getIdentifier() +"<br />"+ v.getLabelAddition() +"</html>";
			else
				return "<html>"+ v.getLabel() +"<br />"+ v.getLabelAddition() +"</html>";
		}
	};
	
	
	private Transformer<Edge, Font> mEdgeFontTransformer = new Transformer<Edge, Font>() {
		
		@Override
		public Font transform(Edge e) {
			return new Font("Helvetica", Font.PLAIN, mSettings.getLabelSize(Settings.LABEL_EDGE));
		}
	};
	
	
	
	private Transformer<Vertex, Font> mVertexFontTransformer = new Transformer<Vertex, Font>() {
		@Override
		public Font transform(Vertex v) {
			return new Font("Helvetica", Font.PLAIN, mSettings.getLabelSize(Settings.LABEL_VERTEX));
		}
	};
	
	
	/**
	 * this method returns the color according to the given state
	 * @param state the ElementState
	 * @return the color of 'state' set by the user
	 */
	public Paint checkStateForColor(ElementState state) {
		
		switch(state) {
		case UNVISITED:
			return Color.decode(Settings.getInstance().getColor(Settings.COLOR_UNVISITED));
		case ACTIVE:
			return Color.decode(Settings.getInstance().getColor(Settings.COLOR_ACTIVE));
		case VISITED:
			return Color.decode(Settings.getInstance().getColor(Settings.COLOR_VISITED));
		case FINISHED_AND_NOT_RELEVANT:
			return Color.decode(Settings.getInstance().getColor(Settings.COLOR_FINISHED_AND_NOT_RELEVANT));
		case FINISHED_AND_RELEVANT:
			return Color.decode(Settings.getInstance().getColor(Settings.COLOR_FINISHED_AND_RELEVANT));
		default:
			return Color.decode(Settings.getInstance().getColor(Settings.COLOR_UNVISITED));
		}
	}
	
	/**
	 * constructs the GraphBuilder
	 */
	public GraphBuilder() {
		// create a graph
		mGraph = new Graph();
		mResultGraph = mGraph;
		// add the graph to the layout
		mLayout = new StaticLayout<Vertex, Edge>(mGraph);
		// add the layout to the VisualizationViewer
		mVViewer = new VisualizationViewer<Vertex, Edge>(mLayout);
		
		
		mGraphMouse = new MyModalGraphMouse(mVViewer.getRenderContext());
		mVViewer.setGraphMouse(mGraphMouse);
		mVViewer.setFocusable(true);
		mGraphMouse.setMode(ModalGraphMouse.Mode.EDITING);
		
		mVViewer.setBackground(Color.WHITE);
		
		// initialize the edge renderer
		mVViewer.getRenderContext().setEdgeLabelTransformer(mEdgeLabelTransformer);
		mVViewer.getRenderContext().setEdgeDrawPaintTransformer(mEdgePaintTransformer);
		mVViewer.getRenderContext().setEdgeStrokeTransformer(new ConstantTransformer(new BasicStroke(3.0f)));
		mVViewer.getRenderContext().getEdgeLabelRenderer().setRotateEdgeLabels(true);
		mVViewer.getRenderContext().setArrowFillPaintTransformer(mEdgePaintTransformer);
		mVViewer.getRenderContext().setEdgeFontTransformer(mEdgeFontTransformer);
		mVViewer.getRenderContext().setEdgeLabelClosenessTransformer(new ConstantDirectionalEdgeValueTransformer<Vertex, Edge>(.5, .5));
		
		// initialize the vertex renderer
		mVViewer.getRenderContext().setVertexLabelTransformer(mVertexLabelTransformer);
		mVViewer.getRenderContext().setVertexShapeTransformer(mVertexShapeTransformer);
		mVViewer.getRenderContext().setVertexFillPaintTransformer(mVertexPaintTransformer);
		mVViewer.getRenderContext().setVertexDrawPaintTransformer(mVertexLineTransformer);
		mVViewer.getRenderContext().setVertexFontTransformer(mVertexFontTransformer);
		mVViewer.getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.CNTR);
	}

	
	
	/**
	 * adds the VisualisationViewer to the given panel
	 * @param panel the panel represents the graph drawing panel
	 */
	public void addToPanel(JPanel panel) {
		panel.add(mVViewer);
		onResizePanel(panel);
	}
	
	
	/**
	 * called if the panel (the frame) is resized
	 * @param panel the given panel that shows the graph
	 */
	public void onResizePanel(JPanel panel) {
		Dimension dimen = new Dimension(panel.getBounds().width, panel.getBounds().height);
		mVViewer.setPreferredSize(dimen);
		mVViewer.setSize(dimen);
		
		// possible reason for the modifylocationifoutofbounds not functioning properly !!!!! handle with care!!!!!
		//mLayout.setSize(dimen);
		
		//mVViewer.resize(dimen);
		modifyLocationsIfOutOfBounds(mGraph);
		modifyLocationsIfOutOfBounds(mResultGraph);
	}
	
	/**
	 * is called when resizing the frame
	 * checks for all vertices if their position will be gone out of view. if so, the position will
	 * be modified to avoid disappearing of some vertices. This will ensure, that the complete graph
	 * is visible all the time.
	 */
	private void modifyLocationsIfOutOfBounds(Graph graph) {
		
		if(!graph.getVertices().isEmpty()) {
			Dimension dimen = mVViewer.getSize();
			int i = 0;
			
			//System.out.println("dimen: "+ dimen.toString());
			for(Vertex v : graph.getVertices()) {
				Point2D p = mLayout.transform(v);
				p.setLocation(v.getLocation());
				//System.out.println("vertex "+ i++ +": ("+ p.getX() +", "+ p.getY() +")");
				double x = p.getX();
				double y = p.getY();
				double newX = x;
				double newY = y;
				
				int min = PADDING + VERTEXRADIUS;
				int maxW = dimen.width - PADDING -VERTEXRADIUS;
				int maxH = dimen.height - PADDING - VERTEXRADIUS;
				if(x < min)
					newX = min;
				else if(x > maxW)
					newX = maxW;
				if(y < min)
					newY = min;
				else if(y > maxH)
					newY = maxH;
				
				if((newX != x) || (newY != y)) {
					p.setLocation(newX, newY);
					//v.updateLocation(p);
					//mLayout.setLocation(v, v.getLocation());
				}
			}
			mVViewer.repaint();
		}
	}
	
	
	/**
	 * calculates the rectangle of the drawed graph
	 * @return the rectangle of the graph or null, if no vertex is present
	 */
	private Rectangle getGraphRect() {
		if(!mGraph.getVertices().isEmpty()) {
			double minX = 0;
			double maxX = 0;
			double minY = 0;
			double maxY = 0;
			boolean initialised = false;
			int i = 0;
			
			for(Vertex v : mGraph.getVertices()) {
				Point2D p = mLayout.transform(v);
				double x = p.getX();
				double y = p.getY();
				System.out.println("vertex "+ i++ +": ("+ p.getX() +", "+ p.getY() +")");
				if(!initialised) {
					initialised = true;
					minX = x;
					maxX = minX;
					minY = y;
					maxY = minY;
				}
				else {
					if(x < minX)
						minX = x;
					else if(x > maxX)
						maxX = x;
					
					if(y < minY)
						minY = y;
					else if(y > maxY)
						maxY = y;
				}
			}
			return new Rectangle((int)minX, (int)minY, (int)(maxX - minX), (int)(maxY - minY));
		}
		else
			return null;
	}
	
	/**
	 * getter for the graph of graphcreation mode
	 * @return returns a copy of the graph
	 */
	public Graph getGraph() {
		return new Graph(mGraph);
	}
	
	/**
	 * setter for the displayed graph in visualization mode
	 * @param g the graph that will be copied and displayed
	 */
	public void setResultingGraph(Graph g) {
		mResultGraph = new Graph(g);
		showResultGraph();
	}
	

	private void resetResultGraph() {
		mResultGraph = mGraph;
		mVViewer.repaint();
	}
	
	private void showOriginGraph() {
		mLayout.setGraph(mGraph);
		updateLocations();
		mVViewer.repaint();
	}
	
	private void showResultGraph() {
		mLayout.setGraph(mResultGraph);
		updateLocations();
		mVViewer.repaint();
	}
	
	private void updateLocations() {
		for(Vertex v : mVViewer.getGraphLayout().getGraph().getVertices()) {
			//mLayout.setLocation(v, v.getLocation());
			Point2D p = mLayout.transform(v);
			p.setLocation(v.getLocation());
		}
		modifyLocationsIfOutOfBounds((Graph) mVViewer.getGraphLayout().getGraph());
	}
	
	/**
	 * sets the mode
	 * @param mode the mode to be set according to VigralGUI modes
	 */
	public void setMode(int mode) {
		if(mode == VigralGUI.Mode.GRAPHCREATION) {
			mGraphMouse.addEditingFunctionality();
			showOriginGraph();
			resetResultGraph();
		}
		else if(mode == VigralGUI.Mode.VISUALISATION) {
			mGraphMouse.removeEditingFunctionality();
			showResultGraph();
		}
	}
	
//	/**
//	 * 
//	 */
//	public void resetVertexState() {
//		for(Vertex v : mGraph.getVertices())
//			v.setState(ElementState.UNVISITED);
//	}
	
	/**
	 * displays the given graph
	 * @param g the graph that will be copied and displayed
	 */
	public void setGraph(Graph g) {
		mGraph = new Graph(g);
		showOriginGraph();
	}
	
	/**
	 * displays a new, empty graph
	 */
	public void resetGraph() {
		mGraph = new Graph();
		mLayout.setGraph(mGraph);
		mVViewer.repaint();
	}
	
	/**
	 * getter for the VisualizationViewer
	 * @return the VisualizationViewer
	 */
	public VisualizationViewer<Vertex, Edge> getVisualizationViewer() {
		return mVViewer;
	}
	
	/**
	 * tells the graphbuilder to redraw the graph
	 */
	public void redraw() {
		mVViewer.repaint();
	}
	
}
