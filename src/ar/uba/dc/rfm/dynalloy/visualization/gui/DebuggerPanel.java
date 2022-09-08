package ar.uba.dc.rfm.dynalloy.visualization.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.event.EventListenerList;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import edu.mit.csail.sdg.alloy4.Computer;
import edu.mit.csail.sdg.alloy4viz.VizGUI;

import ar.uba.dc.rfm.alloy.ast.formulas.AlloyFormula;
import ar.uba.dc.rfm.dynalloy.trace.AlloyEvaluator;
import ar.uba.dc.rfm.dynalloy.trace.DynAlloySolution;
import ar.uba.dc.rfm.dynalloy.trace.DynalloyExecutionTrace;
import ar.uba.dc.rfm.dynalloy.visualization.AlloyComputer;
import ar.uba.dc.rfm.dynalloy.visualization.DynalloyVisualizerController;
import ar.uba.dc.rfm.dynalloy.visualization.VizException;

public class DebuggerPanel extends JPanel {

	private static final long serialVersionUID = 5124197421170011484L;

	private JToolBar toolbar;
	private JTree traceTree;
	private Action stepIntoAction;
	private Action stepOverAction;
	private Action stepReturnAction;
	private DefaultMutableTreeNode rootTreeNode;
	private DefaultTreeModel treeModel;

	protected EventListenerList traceSelectionListenerList = new EventListenerList();

	private Action showAlloyAction;

	private Action nextTraceAction;

	private DynalloyVisualizerController vizController;

	private JTextArea outputArea;

	private class StepIntoAction extends AbstractAction {

		private static final long serialVersionUID = 865009328646062630L;

		public StepIntoAction() {
			super("Step Into");
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			TreePath path = traceTree.getSelectionPath();
			if (path != null) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) path
						.getLastPathComponent();
				DefaultMutableTreeNode next = node.getNextNode();
				if (next != null) {
					traceTree.setSelectionPath(new TreePath(next.getPath()));
				}
			}
		}
	}

	private class StepOverAction extends AbstractAction {

		private static final long serialVersionUID = 8233134496065271643L;

		public StepOverAction() {
			super("Step Over");
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			TreePath path = traceTree.getSelectionPath();
			if (path != null) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) path
						.getLastPathComponent();
				DefaultMutableTreeNode next = null;
				while (node != null && next == null) {
					next = node.getNextSibling();
					node = (DefaultMutableTreeNode) node.getParent();
				}
				if (next != null) {
					traceTree.setSelectionPath(new TreePath(next.getPath()));
				}
			}
		}

	}

	private class ShowAlloyAction extends AbstractAction {

		public ShowAlloyAction() {
			super("Show Alloy");
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			if (xmlFilename != null) {
				AlloyComputer computer = new AlloyComputer();
				computer.setMainname(alsFilename);
				VizGUI viz = new VizGUI(false, xmlFilename, null, null,
						computer);
			}
		}

	}

	private class NextTraceAction extends AbstractAction {

		public NextTraceAction() {
			super("Next Trace");
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {

			try {
				if (!currentSolution.isUNSAT()) {
					DynAlloySolution nextSolution = vizController
							.nextSolution(currentSolution);
					setDynAlloySolution(nextSolution);
				} else {
					outputArea.append("\n");
					outputArea.append("No more solutions available");
					outputArea.append("\n");
				}
					
			} catch (VizException e) {
				outputArea.append(e.getMessage() + "\n");
			}
		}

	}

	private class StepReturnAction extends AbstractAction {

		private static final long serialVersionUID = 2819947569835716560L;

		public StepReturnAction() {
			super("Step Return");
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			TreePath path = traceTree.getSelectionPath();
			if (path != null) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) path
						.getLastPathComponent();
				node = (DefaultMutableTreeNode) node.getParent();
				DefaultMutableTreeNode next = null;
				while (node != null && next == null) {
					next = node.getNextSibling();
					node = (DefaultMutableTreeNode) node.getParent();
				}
				if (next != null) {
					traceTree.setSelectionPath(new TreePath(next.getPath()));
				}
			}
		}

	}

	private class TraceLocationListener implements TreeSelectionListener {

		@Override
		public void valueChanged(TreeSelectionEvent e) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) traceTree
					.getLastSelectedPathComponent();
			if (node != null) {
				Object nodeInfo = node.getUserObject();
				if (nodeInfo != null
						&& nodeInfo instanceof DynalloyExecutionTrace) {
					DynalloyExecutionTrace trace = (DynalloyExecutionTrace) nodeInfo;
					TraceSelectionEvent event = new TraceSelectionEvent(this);
					event.setTraceNode(trace);
					fireTraceSelection(event);
				} else if (nodeInfo != null && nodeInfo instanceof AlloyFormula) {
					AlloyFormula formula = (AlloyFormula) nodeInfo;
					TraceSelectionEvent event = new TraceSelectionEvent(this);
					event.setAlloyFormula(formula);
					fireTraceSelection(event);

				}
			}
		}

	}

	public DebuggerPanel(DynalloyVisualizerController controller,
			JTextArea outputArea) {
		super();
		setLayout(new BorderLayout());
		this.vizController = controller;
		this.outputArea = outputArea;
	}

	public void initialize() {
		initializeActions();
		initializeToolbar();
		initializeTree();

		setBorder(BorderFactory.createLineBorder(Color.RED));
	}

	public void initializeActions() {
		stepIntoAction = new StepIntoAction();
		stepOverAction = new StepOverAction();
		stepReturnAction = new StepReturnAction();
		showAlloyAction = new ShowAlloyAction();
		nextTraceAction = new NextTraceAction();
	}

	public void initializeToolbar() {
		toolbar = new JToolBar();
		toolbar.add(new JButton(stepIntoAction));
		toolbar.add(new JButton(stepOverAction));
		toolbar.add(new JButton(stepReturnAction));
		toolbar.add(new JButton(showAlloyAction));
		toolbar.add(new JButton(nextTraceAction));

		add(toolbar, BorderLayout.PAGE_START);
	}

	public void initializeTree() {
		rootTreeNode = new DefaultMutableTreeNode(
				"No trace information available");
		treeModel = new DefaultTreeModel(rootTreeNode);
		traceTree = new JTree(treeModel);
		traceTree.setExpandsSelectedPaths(true);
		traceTree.addSelectionRow(0);
		traceTree.getSelectionModel().setSelectionMode(
				TreeSelectionModel.SINGLE_TREE_SELECTION);
		traceTree.addTreeSelectionListener(new TraceLocationListener());
		//traceTree.setRootVisible(false);
		add(new JScrollPane(traceTree), BorderLayout.CENTER);
	}

	private String xmlFilename;

	private String alsFilename;

	private DynAlloySolution currentSolution;

	public void setDynAlloySolution(DynAlloySolution solution) {
		this.currentSolution = solution;

		DefaultMutableTreeNode rootTreeNode;
		DynalloyExecutionTrace trace = solution.getExecutionTrace();
		rootTreeNode = createDefaultTreeModel(trace);

		if (!solution.isUNSAT()) {
			xmlFilename = solution.getXmlFilename();
			alsFilename = solution.getAlsFilename();
		} else {
			xmlFilename = null;
			alsFilename = null;
		}

		setRootTreeNode(rootTreeNode);

	}

	private void setRootTreeNode(DefaultMutableTreeNode newRootTreeNode) {
		traceTree.setVisible(false);
		rootTreeNode = newRootTreeNode;
		treeModel.setRoot(rootTreeNode);
		traceTree.setRootVisible(false);
		traceTree.setShowsRootHandles(true);
		traceTree.setSelectionRow(0);
		traceTree.setVisible(true);
	}

	public DefaultMutableTreeNode createDefaultTreeModel(
			DynalloyExecutionTrace trace) {
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(trace);
		for (DynalloyExecutionTrace child : trace.getChildren()) {
			node.add(createDefaultTreeModel(child));
		}
		return node;
	}

	public void addTraceSelectionListener(TraceSelectionListener listener) {
		traceSelectionListenerList.add(TraceSelectionListener.class, listener);
	}

	public void removeTraceSelectionListener(TraceSelectionListener listener) {
		traceSelectionListenerList.remove(TraceSelectionListener.class,
				listener);
	}

	void fireTraceSelection(TraceSelectionEvent event) {
		TraceSelectionListener[] listeners = traceSelectionListenerList
				.getListeners(TraceSelectionListener.class);

		for (int i = 0; i < listeners.length; i++) {
			listeners[i].traceSelectionChanged(event);
		}
	}
}
