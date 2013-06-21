package de.chiller.vigral.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import de.chiller.vigral.algorithm.AbstractAlgorithm;
import de.chiller.vigral.graph.Edge;
import de.chiller.vigral.graph.ElementType;
import de.chiller.vigral.graph.Graph;
import de.chiller.vigral.graph.Vertex;
import de.chiller.vigral.util.Pair;

public class RequirementDialog extends JDialog {

	private static final int MARGIN = 20;
	private static final Dimension MAX_LABEL_DIMENSION = new Dimension(500, 25);

	private JPanel buttonPane = new JPanel();
	private JButton okButton = new JButton("OK");
	private JButton cancelButton = new JButton("Cancel");
	private AbstractAlgorithm mAlgorithm;
	private Graph mGraph;

	private final JPanel contentPanel = new JPanel();
	private ArrayList<Pair<JLabel, JComboBox>> mComboBoxes = new ArrayList<Pair<JLabel, JComboBox>>();
	private ArrayList<Pair<ElementType, String>> mRequirements;
	private ArrayList<String> vertexLabels = new ArrayList<String>();
	private ArrayList<String> edgeLabels = new ArrayList<String>();

	private ArrayList<Vertex> mVertices;
	private ArrayList<Edge> mEdges;

	private ActionListener mOnOK = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			// onOK:
			// create an array for the required ids
			ArrayList<Integer> required = new ArrayList<Integer>();

			// take the choices of the dialog
			for (int i = 0; i < mComboBoxes.size(); i++) {
				// get the label-combobox combination
				Pair<JLabel, JComboBox> pair = mComboBoxes.get(i);
				String lbl = (String) pair.getR().getSelectedItem();
				ElementType requiredType = mRequirements.get(i).getL();

				// add the id of the chosen element
				switch (requiredType) {

				case OPTIONAL_VERTEX:
					// if the user has chosen 'nothing selected' (the first
					// entry) return -1
					if (lbl.equals("--"))
						required.add(-1);
					// else add the appropriate id
					else
						required.add(mVertices.get(
								pair.getR().getSelectedIndex() - 1).getId());
					break;

				case OPTIONAL_EDGE:
					if (lbl.equals("--"))
						required.add(-1);
					else
						required.add(mEdges.get(pair.getR().getSelectedIndex() - 1).getId());
					break;

				case EDGE:
					required.add(mEdges.get(pair.getR().getSelectedIndex())
							.getId());
					break;

				case VERTEX:
					required.add(mVertices.get(pair.getR().getSelectedIndex())
							.getId());
					break;

				default:
					break;
				}
			}
			// pass the required ids and the to the algorithm
			mAlgorithm.setRequirements(required);
			mAlgorithm.setGraph(mGraph);
			// and tell the gui, that all requirements have been applied
			VigralGUI.getInstance().requirementsApplied(mGraph);

			// close this dialog
			dispose();
		}
	};

	/**
	 * Create the dialog.
	 */
	public RequirementDialog(ArrayList<Pair<ElementType, String>> requirements, Graph graph, AbstractAlgorithm algorithm) {
		mRequirements = requirements;
		mAlgorithm = algorithm;
		mGraph = graph;

		// get the vertices of the graph
		mVertices = new ArrayList<Vertex>(graph.getVertices());
		System.out.println("unsorted: " + mVertices);
		// sort the vertices (by label)
		Collections.sort(mVertices);
		System.out.println("sorted: " + mVertices);

		// get the edges of the graph
		mEdges = new ArrayList<Edge>(graph.getEdges());
		// sort edges (by id)
		Collections.sort(mEdges);

		// put the labels in lists
		for (int i = 0; i < mVertices.size(); i++)
			vertexLabels.add(i, mVertices.get(i).getLabel());
		for (int i = 0; i < mEdges.size(); i++)
			edgeLabels.add(i, mEdges.get(i).toString());

		Rectangle requiredSpace = addRequiredUIElements();
		
		initComponents();
		adaptSizes(requiredSpace);
		pack();
	}

	private void initComponents() {
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(null);

		// initialize button bar
		buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
		getContentPane().add(buttonPane, BorderLayout.SOUTH);

		// initialize ok button
		okButton.addActionListener(mOnOK);
		okButton.setActionCommand("OK");
		buttonPane.add(okButton);
		getRootPane().setDefaultButton(okButton);

		// initialize cancel button
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// onCancel: close this dialog
				dispose();
			}
		});
		cancelButton.setActionCommand("Cancel");
		buttonPane.add(cancelButton);
	}

	private Rectangle addRequiredUIElements() {
		int maxLineWidth = 0;
		int height = 0;
		
		// build the label-combobox combination
		for (int i = 0; i < mRequirements.size(); i++) {

			// init the label
			JLabel lbl = new JLabel();
			ElementType type = mRequirements.get(i).getL();
			String name = mRequirements.get(i).getR();
			if (type == ElementType.VERTEX || type == ElementType.EDGE)
				lbl.setText(name);
			else
				lbl.setText(name + " (optional)");

			// set size of the label
			// lbl.setMaximumSize(MAX_LABEL_DIMENSION);
			int x = MARGIN;
			int y = i * (MAX_LABEL_DIMENSION.height + MARGIN);
			lbl.setBounds(x, y, lbl.getPreferredSize().width, lbl.getPreferredSize().height);

			// init the combobox
			JComboBox box = new JComboBox();
			DefaultComboBoxModel<?> model = null;
			switch (type) {
			case VERTEX:
				model = new DefaultComboBoxModel<Object>(vertexLabels.toArray());
				break;

			case EDGE:
				model = new DefaultComboBoxModel<Object>(edgeLabels.toArray());
				break;

			// for all optional types a new choice will be added, to indicate
			// 'nothing selected'
			case OPTIONAL_VERTEX:
				vertexLabels.add(0, "--");
				model = new DefaultComboBoxModel<Object>(vertexLabels.toArray());
				vertexLabels.remove(0);
				break;

			case OPTIONAL_EDGE:
				edgeLabels.add(0, "--");
				model = new DefaultComboBoxModel<Object>(edgeLabels.toArray());
				edgeLabels.remove(0);
				break;

			default:
				break;
			}

			// set size of combobox
			box.setModel(model);
			x = MARGIN + lbl.getBounds().width + MARGIN;
			y = i * (box.getPreferredSize().height + MARGIN);
			box.setBounds(x, y, box.getPreferredSize().width, box.getPreferredSize().height);
			int lineWidth = MARGIN + lbl.getBounds().width + MARGIN + box.getBounds().width + MARGIN;
			if (lineWidth > maxLineWidth)
				maxLineWidth = lineWidth;

			height = box.getBounds().x + box.getBounds().height + MARGIN;

			// add the label and combobox
			mComboBoxes.add(i, new Pair<JLabel, JComboBox>(lbl, box));
			contentPanel.add(lbl);
			contentPanel.add(box);
		}
		Rectangle rect = new Rectangle();
		rect.x = 0;
		rect.y = 0;
		rect.width = maxLineWidth;
		rect.height = height;
		System.out.println("rect: "+ rect);
		return rect;
	}
	
	private void adaptSizes(Rectangle rect) {
		System.out.println("rect: "+ rect);
		contentPanel.setPreferredSize(new Dimension(rect.width, rect.height));
		//setBounds(VigralGUI.getInstance().getX() + 100, VigralGUI.getInstance().getY() + 100, rect.width, rect.height);
	}

}
