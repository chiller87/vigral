package de.chiller.vigral.gui;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.functors.ConstantTransformer;

import de.chiller.vigral.graph.Edge;
import de.chiller.vigral.graph.ElementState;
import de.chiller.vigral.graph.Graph;
import de.chiller.vigral.graph.Vertex;
import de.chiller.vigral.jung.MyColor;
import de.chiller.vigral.jung.MyModalGraphMouse;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.StaticLayout;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
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
				return MyColor.YELLOW;
			
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
	
	
	public Paint checkStateForColor(ElementState state) {
		
		switch(state) {
		case UNVISITED:
			return MyColor.LIGHT_CYAN;
		case ACTIVE:
			return MyColor.LIGHT_ORANGE;
		case VISITED:
			return new Color(0xFF4444);
		case FINISHED_AND_NOT_RELEVANT:
			return MyColor.LIGHT_GRAY;
		case FINISHED_AND_RELEVANT:
			return MyColor.LIGHT_GREEN;
		default:
			return Color.BLACK;
		}
	}
	
	
	public GraphBuilder() {
		System.out.println("GraphBuilder Creation");
		// create a graph
		mGraph = new Graph();
		mResultGraph = mGraph;
		// add the graph to the layout
		mLayout = new StaticLayout<Vertex, Edge>(mGraph);
		// add the layout to the VisualizationViewer
		mVViewer = new VisualizationViewer<Vertex, Edge>(mLayout);
		
		System.out.println("layout: "+ mLayout.getSize());
		
		mGraphMouse = new MyModalGraphMouse(mVViewer.getRenderContext());
		mVViewer.setGraphMouse(mGraphMouse);
		mGraphMouse.setMode(ModalGraphMouse.Mode.EDITING);		
		
		mVViewer.getRenderContext().setEdgeLabelTransformer(new Transformer<Edge, String>() {
			@Override
			public String transform(Edge e) {
				return ""+ e.getWeight();
			}
		});
		
		mVViewer.getRenderContext().setVertexLabelTransformer(new Transformer<Vertex, String>() {
			@Override
			public String transform(Vertex v) {
				return "<html>"+ v.getLabel() +"<br />"+ v.getLabelAddition() +"</html>";
			}
		});
		
		mVViewer.setBackground(Color.WHITE);
		mVViewer.getRenderContext().getEdgeLabelRenderer().setRotateEdgeLabels(false);
		
		mVViewer.getRenderContext().setVertexShapeTransformer(mVertexShapeTransformer);
		mVViewer.getRenderContext().setVertexFillPaintTransformer(mVertexPaintTransformer);
		mVViewer.getRenderContext().setVertexDrawPaintTransformer(mVertexLineTransformer);
		mVViewer.getRenderContext().setEdgeDrawPaintTransformer(mEdgePaintTransformer);
		//mVViewer.getRenderContext().setEdgeFillPaintTransformer(mEdgePaintTransformer);
		mVViewer.getRenderContext().setEdgeStrokeTransformer(new ConstantTransformer(new BasicStroke(3.0f)));
		mVViewer.getRenderContext().setArrowFillPaintTransformer(mEdgePaintTransformer);
		mVViewer.getRenderContext().setEdgeFontTransformer(new ConstantTransformer(new Font("Helvetica", Font.PLAIN, 16)));
		//mVViewer.getRenderContext().setEdgeShapeTransformer(mEdgeShapeTransformer);

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
	
	
	public Graph getGraph() {
		return new Graph(mGraph);
	}
	
	
	public void setResultingGraph(Graph g) {
		mResultGraph = new Graph(g);
		mLayout.setGraph(mResultGraph);
		updateLocations();

		mVViewer.repaint();
	}
	
	public void resetResultGraph() {
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
	
	public void resetVertexState() {
		for(Vertex v : mGraph.getVertices())
			v.setState(ElementState.UNVISITED);
	}
	
	
	public void setGraph(Graph g) {
		mGraph = new Graph(g);
		showOriginGraph();
	}
	
	public void resetGraph() {
		mGraph = new Graph();
		mLayout.setGraph(mGraph);
		mVViewer.repaint();
	}
	
	public VisualizationViewer<Vertex, Edge> getVisualizationViewer() {
		return mVViewer;
	}
	
	
}
