import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.util.ArrayList;


import javax.swing.JPanel;
import javax.vecmath.Point2d;

import org.apache.commons.collections15.Factory;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.StaticLayout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.visualization.VisualizationServer;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.EditingModalGraphMouse;
import edu.uci.ics.jung.visualization.control.LayoutScalingControl;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import edu.uci.ics.jung.visualization.control.ViewScalingControl;


public class GraphBuilder {

	private Layout<Node, Edge> mLayout;
	private SparseMultigraph<Node, Edge> mGraph;
	private VisualizationViewer<Node, Edge> mVViewer;
	private MyModalGraphMouse<Node, Edge> mGraphMouse; 
	private ScalingControl mScaler;
	
	private Factory<Node> mNodeFactory;
	private Factory<Edge> mEdgeFactory;
	
	private int nodeCount;
	private int edgeCount;
	
	public GraphBuilder(JPanel panel) {
		System.out.println("GraphBuilder Creation");
		
		mGraph = new SparseMultigraph<Node, Edge>();
		mLayout = new StaticLayout<Node, Edge>(mGraph);
		mVViewer = new VisualizationViewer<Node, Edge>(mLayout);
		
		mScaler = new LayoutScalingControl();
		
		System.out.println("layout: "+ mLayout.getSize());
		
		mNodeFactory = new Factory<Node>() {
			@Override
			public Node create() {
				return new Node();
			}
		};
		mEdgeFactory = new Factory<Edge>() {
			@Override
			public Edge create() {
				return new Edge();
			}
		};
		
		mGraphMouse = new MyModalGraphMouse<Node, Edge>(mVViewer.getRenderContext(), mNodeFactory, mEdgeFactory);
		mVViewer.setGraphMouse(mGraphMouse);
		mGraphMouse.setMode(ModalGraphMouse.Mode.EDITING);
		mVViewer.setBackground(Color.green);
		
		panel.add(mVViewer);
		
		onResizePanel(panel);
	}
	
	
	public void onResizePanel(JPanel panel) {
		System.out.println("onResize");
		Dimension dimen = new Dimension(panel.getBounds().width, panel.getBounds().height);
		System.out.println("panel dimension: "+ dimen.toString());
		mVViewer.setPreferredSize(dimen);
		mLayout.setSize(dimen);
		Point2D p = new Point(0, 0);
		//mScaler.scale(mVViewer, new Float(1.001), p);		
		
		resizeGraph();
	}
	
	
	
	public void resizeGraph() {
		Rectangle graphRect = getGraphRect();
		
		if(!fitInBounds(graphRect)) {
			System.out.println("check bounds!!!");
			// TODO resize graph by mdifying the positions of the vertices
			
		}
			
	}
	
	
	public boolean fitInBounds(Rectangle graphRect) {
		
		Dimension dimen = mLayout.getSize();
		if(graphRect == null)
			return true;
		
		if((graphRect.x < 11) || (graphRect.y < 11) || ((graphRect.x + graphRect.width) > (dimen.width - 22)) || ((graphRect.y + graphRect.height) > (dimen.height - 22)))
			return false;
		
		return true;
	}
	
	
	
	public Rectangle getGraphRect() {
		Graph<Node,Edge> graph = mVViewer.getModel().getGraphLayout().getGraph();
		if(!graph.getVertices().isEmpty()) {
			Dimension dimen = mVViewer.getSize();
			double minX = 0;
			double maxX = 0;
			double minY = 0;
			double maxY = 0;
			boolean initialised = false;
			int i = 0;
			
			for(Node v : graph.getVertices()) {
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
	
	
	
	
}
