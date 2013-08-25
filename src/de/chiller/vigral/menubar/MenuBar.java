package de.chiller.vigral.menubar;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import de.chiller.vigral.VigralGUI;
import de.chiller.vigral.VigralGUI.Mode;
import de.chiller.vigral.graph.Edge;
import de.chiller.vigral.graph.Graph;
import de.chiller.vigral.graph.Vertex;
import de.chiller.vigral.settings.SettingsDialog;
import de.chiller.vigral.util.FileOperator;


/**
 * this class represents the menubar
 * @author Simon Schiller
 *
 */
public class MenuBar extends JMenuBar {
	
	private static final long serialVersionUID = 1L;
	
	private ActionListener onExit = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			VigralGUI.getInstance().dispose();
		}
	};
	
	private ActionListener onSave = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			FileOperator fo = FileOperator.getInstance();
			fo.saveGraphToFile(VigralGUI.getInstance().getGraphBuilder().getGraph());
		}
	};
	
	private ActionListener onOpen = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			VigralGUI mainInstance = VigralGUI.getInstance();
			
			FileOperator fo = FileOperator.getInstance();
			Graph g = fo.readGraphFromFile();
			if(g != null) {
				if(mainInstance.getActualMode() == VigralGUI.Mode.VISUALISATION)
					mainInstance.changeMode(VigralGUI.Mode.GRAPHCREATION);
				
				mainInstance.getGraphBuilder().setGraph(g);
			}
		}
	};
	
	private ActionListener onNew = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			VigralGUI mainInstance = VigralGUI.getInstance();
			int choice = JOptionPane.showConfirmDialog(VigralGUI.getInstance(), "Save before Closing", "Do you want to save your work?", JOptionPane.YES_NO_CANCEL_OPTION);
			switch (choice) {
			case JOptionPane.CANCEL_OPTION:
				break;
			case JOptionPane.NO_OPTION:
				if(mainInstance.getActualMode() == VigralGUI.Mode.VISUALISATION)
					mainInstance.changeMode(VigralGUI.Mode.GRAPHCREATION);
				
				mainInstance.getGraphBuilder().resetGraph();
				Vertex.VertexFactory.getInstance().resetIdCounter();
				Edge.EdgeFactory.resetIdCounter();
				break;
			case JOptionPane.YES_OPTION:
				if(mainInstance.getActualMode() == VigralGUI.Mode.VISUALISATION)
					mainInstance.changeMode(VigralGUI.Mode.GRAPHCREATION);
				
				FileOperator fo = FileOperator.getInstance();
				fo.saveGraphToFile(mainInstance.getGraphBuilder().getGraph());
				mainInstance.getGraphBuilder().resetGraph();
				Vertex.VertexFactory.getInstance().resetIdCounter();
				Edge.EdgeFactory.resetIdCounter();
				break;
			default:
				break;
			}
		}
	};
	
	
	private ActionListener onSettings = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			SettingsDialog sf = new SettingsDialog();
			sf.setModal(true);
			sf.setVisible(true);
		}
	};
	
	
	private ActionListener onPluginsReload = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			VigralGUI mainInstance = VigralGUI.getInstance();
			if(mainInstance.getActualMode() == VigralGUI.Mode.VISUALISATION)
				mainInstance.changeMode(VigralGUI.Mode.GRAPHCREATION);
			
			mainInstance.updateAlgorithmBox();
		}
	};
	
	
	private ActionListener onHelp = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			HelpMenu.showHelpMenu();
		}
	};
	
	/**
	 * the constructor for the menubar
	 * @param mainWindow the instance of the mainwindow
	 */
	public MenuBar(VigralGUI mainWindow) {
		super();
		
		JMenu fileMenu = new JMenu("File");
		JMenu pluginMenu = new JMenu("PlugIn");
		JMenu helpMenu = new JMenu("Help");
		
		JMenuItem file_new = new JMenuItem("New");
		JMenuItem file_open = new JMenuItem("Open");
		JMenuItem file_save = new JMenuItem("Save");
		JMenuItem file_settings = new JMenuItem("Settings");
		JMenuItem file_exit = new JMenuItem("Exit");
		fileMenu.add(file_new);
		fileMenu.add(file_open);
		fileMenu.add(file_save);
		fileMenu.addSeparator();
		fileMenu.add(file_settings);
		fileMenu.addSeparator();
		fileMenu.add(file_exit);
		
		JMenuItem plugins_reload = new JMenuItem("Reload");
		pluginMenu.add(plugins_reload);
		
		JMenuItem help_show = new JMenuItem("Show");
		helpMenu.add(help_show);
		
		add(fileMenu);
		add(pluginMenu);
		add(helpMenu);
		
		
		file_exit.addActionListener(onExit);
		file_save.addActionListener(onSave);
		file_open.addActionListener(onOpen);
		file_new.addActionListener(onNew);
		file_settings.addActionListener(onSettings);
		
		plugins_reload.addActionListener(onPluginsReload);
		
		help_show.addActionListener(onHelp);
	}
}
