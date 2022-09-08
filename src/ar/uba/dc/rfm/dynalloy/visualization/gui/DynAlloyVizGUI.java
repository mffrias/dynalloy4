package ar.uba.dc.rfm.dynalloy.visualization.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Locale;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import javax.swing.border.EtchedBorder;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.BadLocationException;

import ar.uba.dc.rfm.alloy.ast.formulas.AlloyFormula;
import ar.uba.dc.rfm.dynalloy.DynAlloyOptions;
import ar.uba.dc.rfm.dynalloy.trace.DynAlloySolution;
import ar.uba.dc.rfm.dynalloy.trace.DynalloyExecutionTrace;
import ar.uba.dc.rfm.dynalloy.visualization.AlloyCommand;
import ar.uba.dc.rfm.dynalloy.visualization.DynalloyVisualizerController;
import ar.uba.dc.rfm.dynalloy.visualization.DynalloyVisualizerException;
import ar.uba.dc.rfm.dynalloy.visualization.VizException;
import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.OurUtil;
import edu.mit.csail.sdg.alloy4.Runner;
import edu.mit.csail.sdg.alloy4.Util;
import edu.mit.csail.sdg.alloy4.XMLNode;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Options;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Solution;
import edu.mit.csail.sdg.alloy4compiler.translator.A4SolutionReader;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Options.SatSolver;

public class DynAlloyVizGUI extends JFrame {

	private static final long serialVersionUID = -7664247407383664094L;

	private JTextArea editor;
	private JToolBar toolbar;
	private OpenFileAction openFileAction;
	private ReloadFileAction reloadFileAction;
	private ExecuteAction executeAction;
	private JFileChooser dalsFileChooser;

	private JFileChooser xmlFileChooser;

	private JTabbedPane extraPanes;
	private DebuggerPanel debuggerPanel;
	private JLabel statusBar;
	private File selectedFile;

	private DynalloyVisualizerController controller;

	private DynAlloyOptions options = new DynAlloyOptions();

	private JMenu filemenu;

	private JMenu executeMenu;

	private JMenu optionsMenu;

	private QuitAction quitAction;

	private AboutAction aboutAction;

	private QuickGuideAction quickGuideAction;

	private CopyrightAction copyrightAction;

	private JMenu helpMenu;

	private UnrollAction unrollAction;

	private UnrollIsStrictAction unrollIsStrictAction;

	private SkolemizeAction skolemizeAction;

	private SATSolverAction satSolver;

	private JTextArea outputArea;

	private JTable variablesTable;

	private JTable watchesTable;

	private SaveFileAction saveFileAction;

	private VariablesTableModel variablesTableModel;

	private WatchesTableModel watchesTableModel;

	private JMenu offlineMenu;

	private GenerateAlsAction generateAlsAction;

	private Object loadXmlAction;

	public DynAlloyVizGUI(String title) throws HeadlessException {
		super(title);
		controller = new DynalloyVisualizerController();

	}

	public static JMenu makeMenu(JMenuBar parent, String label, int mnemonic,
			final Runnable func) {
		final JMenu x = new JMenu(label, false);
		if (mnemonic != -1 && !Util.onMac())
			x.setMnemonic(mnemonic);
		x.addMenuListener(new MenuListener() {
			public final void menuSelected(MenuEvent e) {
				if (func != null)
					func.run();
			}

			public final void menuDeselected(MenuEvent e) {
				OurUtil.enableAll(x);
			}

			public final void menuCanceled(MenuEvent e) {
				OurUtil.enableAll(x);
			}
		});
		if (parent != null)
			parent.add(x);
		return x;
	}

	private void initializeActions() {
		openFileAction = new OpenFileAction();
		reloadFileAction = new ReloadFileAction();
		saveFileAction = new SaveFileAction();
		executeAction = new ExecuteAction();
		quitAction = new QuitAction();
		unrollAction = new UnrollAction();
		unrollIsStrictAction = new UnrollIsStrictAction();
		skolemizeAction = new SkolemizeAction();
		satSolver = new SATSolverAction();
		generateAlsAction = new GenerateAlsAction();
		loadXmlAction = new LoadXmlAction();
		aboutAction = new AboutAction(this);
		quickGuideAction = new QuickGuideAction(this);
		copyrightAction = new CopyrightAction(this);
	}

	private void initializeFileChooser() {
		dalsFileChooser = new JFileChooser();
		FileNameExtensionFilter dalsFilter = new FileNameExtensionFilter(
				"DynAlloy models", "dals");
		dalsFileChooser.setFileFilter(dalsFilter);

		xmlFileChooser = new JFileChooser();
		FileNameExtensionFilter xmlFilter = new FileNameExtensionFilter(
				"XML file", "xml");
		xmlFileChooser.setFileFilter(xmlFilter);
	}

	private void initialize() {
		// Initialize look and feel
		if (onMac() || onWindows()) {
			try {
				UIManager.setLookAndFeel(UIManager
						.getSystemLookAndFeelClassName());
			} catch (Throwable e) {
			}
		}
		initializeActions();
		initializeFileChooser();

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());

		addMenuBar();

		JPanel mainArea = new JPanel();
		mainArea.setLayout(new GridBagLayout());

		addEditor(mainArea);
		addExtraPanels(mainArea);
		addDebuggerPanel(mainArea);
		addStatusBar();

		pack();
		setExtendedState(getExtendedState() | JFrame.MAXIMIZED_BOTH);
	}

	private void addStatusBar() {
		statusBar = new JLabel();
		statusBar.setText(" Ready");
		statusBar.setBorder(BorderFactory
				.createEtchedBorder(EtchedBorder.RAISED));
		add(statusBar, BorderLayout.SOUTH);
	}

	private void addMenuBar() {
		JMenuBar menuBar = new JMenuBar();
		filemenu = makeMenu(menuBar, "File", KeyEvent.VK_F, null);

		OurUtil.menuItem(filemenu, "Open...", true, KeyEvent.VK_O,
				KeyEvent.VK_O, openFileAction);

		OurUtil.menuItem(filemenu, "Reload...", true, KeyEvent.VK_R,
				KeyEvent.VK_R, reloadFileAction);

		OurUtil.menuItem(filemenu, "Save", true, KeyEvent.VK_S, KeyEvent.VK_S,
				saveFileAction);

		OurUtil.menuItem(filemenu, "Quit", true, KeyEvent.VK_Q,
				(Util.onMac() ? -1 : KeyEvent.VK_Q), quitAction);

		executeMenu = makeMenu(menuBar, "Execute", KeyEvent.VK_X, null);
		OurUtil.menuItem(executeMenu, "Execute...", true, KeyEvent.VK_E,
				KeyEvent.VK_E, executeAction);

		optionsMenu = makeMenu(menuBar, "Options", KeyEvent.VK_O,
				doRefreshOptions());

		offlineMenu = makeMenu(menuBar, "Offline", KeyEvent.VK_I, null);
		OurUtil.menuItem(offlineMenu, "Generate ALS", null, null,
				generateAlsAction);
		OurUtil.menuItem(offlineMenu, "Load XML", null, null, loadXmlAction);

		helpMenu = makeMenu(menuBar, "Help", KeyEvent.VK_H, null);
		OurUtil.menuItem(helpMenu, "About DynAlloy...", true, KeyEvent.VK_A,
				null, aboutAction);
		OurUtil.menuItem(helpMenu, "Quick Guide", true, KeyEvent.VK_Q, null,
				quickGuideAction);
		OurUtil.menuItem(helpMenu, "See Copyright notices", true, null, null,
				copyrightAction);

		add(menuBar, BorderLayout.PAGE_START);
	}

	private Runnable doRefreshOptions() {
		return new Runner() {

			@Override
			public void run() {
				optionsMenu.removeAll();

				String solverName;
				if (options.getRunAlloyAnalyzer() == false)
					solverName = "Output to file";
				else
					solverName = options.alloyOptions.solver.toString();

				OurUtil.menuItem(optionsMenu, "SAT Solver: " + solverName,
						true, null, null, satSolver);
				OurUtil.menuItem(optionsMenu, "Unroll: "
						+ options.getLoopUnroll(), true, KeyEvent.VK_U, null,
						unrollAction);
				OurUtil.menuItem(optionsMenu, "Unroll is stric: "
						+ (options.getStrictUnrolling() ? "Yes" : "No"), true,
						KeyEvent.VK_S, null, unrollIsStrictAction);
				OurUtil.menuItem(optionsMenu, "Skolemize: "
						+ (options.getRemoveQuantifiers() ? "Yes" : "No"),
						true, KeyEvent.VK_Z, null, skolemizeAction);

			}

			@Override
			public void run(Object arg) {
				// TODO Auto-generated method stub

			}

		};
	}

	private void addDebuggerPanel(JPanel mainArea) {
		GridBagConstraints c;
		debuggerPanel = new DebuggerPanel(this.controller, this.outputArea);
		debuggerPanel.initialize();
		debuggerPanel.addTraceSelectionListener(new TraceSelectionListener() {
			@Override
			public void traceSelectionChanged(TraceSelectionEvent evt) {
				try {
					if (evt.getTraceNode() != null) {
						DynalloyExecutionTrace traceNode = evt.getTraceNode();

						if (traceNode.getProgram() != null) {

							int line = traceNode.getProgram().getPosition()
									.getLineNumber();
							line--; // Correction for 0-based index in editor
							int offset = editor.getLineStartOffset(line);
							editor.setCaretPosition(offset);

							variablesTableModel.setTraceNode(traceNode);
							watchesTableModel.setTraceNode(traceNode);

						}

					} else {
						AlloyFormula formula = evt.getFormula();
						int line = formula.getPosition().getLineNumber();
						line--; // Correction for 0-based index in editor
						int offset = editor.getLineStartOffset(line);
						editor.setCaretPosition(offset);

					}

				} catch (BadLocationException e) {
					// Intentionally blank
				} catch (NullPointerException e) {
					// Intentionally blank
					// A few causes are possible:
					// - The program's getPosition returns null, probably
					// because it comes from a simplification
					// - The program is null, because this node is a
					// DynalloyExecutionTraceNull object
				}
			}
		});

		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 1;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.weightx = 1.0;
		c.weighty = 1.0;
		c.fill = GridBagConstraints.BOTH;
		mainArea.add(debuggerPanel, c);
		add(mainArea, BorderLayout.CENTER);
	}

	private void addExtraPanels(JPanel mainArea) {
		GridBagConstraints c;
		extraPanes = new JTabbedPane();
		outputArea = new JTextArea();
		outputArea.setColumns(80);
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 0;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.weightx = 1.0;
		c.weighty = 1.0;
		c.fill = GridBagConstraints.BOTH;
		controller.setReporterPrintStream(new PrintStream(
				new TextAreaOutputStream(outputArea)));
		extraPanes.addTab("Output", new JScrollPane(outputArea));

		variablesTableModel = new VariablesTableModel();
		variablesTable = new JTable(variablesTableModel);
		variablesTable.setFillsViewportHeight(true);
		variablesTable.setVisible(false);
		// extraPanes.addTab("Variables", new JScrollPane(variablesTable));

		watchesTableModel = new WatchesTableModel(controller);
		watchesTable = new JTable(watchesTableModel);
		watchesTable.setDefaultRenderer(Object.class, new ColorRenderer());
		JTextField textField = new JTextField();
		DefaultCellEditor defaultCellEditor = new DefaultCellEditor(textField);
		defaultCellEditor.addCellEditorListener(new CellEditorListener() {

			@Override
			public void editingCanceled(ChangeEvent e) {
				e.toString();
				// TODO Auto-generated method stub

			}

			@Override
			public void editingStopped(ChangeEvent e) {
				e.toString();
				// TODO Auto-generated method stub

			}
		});
		watchesTable.setCellEditor(defaultCellEditor);
		watchesTable.setFillsViewportHeight(true);

		extraPanes.addTab("Watches", new JScrollPane(watchesTable));

		mainArea.add(extraPanes, c);
	}

	private void addEditor(JPanel mainArea) {
		GridBagConstraints c;
		editor = new JTextArea();
		Font font = new Font("Monospaced", Font.PLAIN, 12);
		editor.setFont(font);
		editor.setColumns(80);
		editor.setRows(20);
		editor.addCaretListener(new CurrentLineHighlighter());
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		c.gridheight = 2;
		c.weightx = 1.0;
		c.weighty = 1.0;
		c.fill = GridBagConstraints.BOTH;
		JScrollPane editorScroll = new JScrollPane(editor);
		editorScroll.setRowHeaderView(new LineNumberView(editor));
		mainArea.add(editorScroll, c);
	}

	private class ColorRenderer extends DefaultTableCellRenderer implements
			TableCellRenderer {
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int col) {
			this.setBackground(Color.white);
			super.getTableCellRendererComponent(table, value, isSelected,
					hasFocus, row, col);
			if (!isSelected && !hasFocus && col == 1) {
				List<Integer> highlightsRows = ((WatchesTableModel) watchesTable
						.getModel()).getHighlightsRows();
				if (highlightsRows.contains(row))
					this.setBackground(Color.green);
			}
			return this;
		}
	}

	private class LoadXmlAction extends AbstractAction {

		@Override
		public void actionPerformed(ActionEvent e) {
			int returnVal = xmlFileChooser.showOpenDialog(null);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File xmlFile = xmlFileChooser.getSelectedFile();
				try {

					XMLNode xmlRoot = new XMLNode(xmlFile);
					A4Solution ans = A4SolutionReader.read(null, xmlRoot);

					throw new UnsupportedOperationException();
				} catch (Err e1) {
					e1.printStackTrace();
				} catch (IOException ex) {
					ex.printStackTrace();
				}

			}

		}
	}

	private class SaveFileAction extends AbstractAction {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			try {
				editor.write(new FileWriter(selectedFile));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}

	private class ReloadFileAction extends AbstractAction {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			try {
				editor.read(new FileReader(selectedFile), selectedFile);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}

	private class OpenFileAction extends AbstractAction {

		private static final long serialVersionUID = 923624206382281715L;

		public OpenFileAction() {
			super("Open");
			// TODO: Ver como agregar keyboard shortcut
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			int returnVal = dalsFileChooser.showOpenDialog(null);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				selectedFile = dalsFileChooser.getSelectedFile();
				try {
					editor.read(new FileReader(selectedFile), selectedFile);
				} catch (FileNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
	}

	private class QuitAction extends AbstractAction {

		@Override
		public void actionPerformed(ActionEvent e) {
			System.exit(0);
		}

	}

	private class AboutAction extends AbstractAction {

		private DynAlloyVizGUI frame;

		public AboutAction(DynAlloyVizGUI frame) {
			this.frame = frame;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			JOptionPane
					.showMessageDialog(
							frame,
							"DynAlloy Analyzer Version 1.0.\n\n"
									+ "Many thanks to Felix Chang, Emina Torlak, Greg Dennis, Kuat Yessenov and Daniel Jackson\n\n"
									+ "Please post your comments to jgaleotti AT dc DOT uba DOT ar");
		}

	}

	private class QuickGuideAction extends AbstractAction {

		private DynAlloyVizGUI frame;

		public QuickGuideAction(DynAlloyVizGUI frame) {
			this.frame = frame;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			JOptionPane
					.showMessageDialog(frame,
							"Please visit http://www.dc.uba.ar/dynalloy for a online guide on DynAlloy");
		}

	}

	private class CopyrightAction extends AbstractAction {

		private DynAlloyVizGUI frame;

		public CopyrightAction(DynAlloyVizGUI frame) {
			this.frame = frame;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			JOptionPane
					.showMessageDialog(
							frame,
							"The source code of the DynAlloy Analyzer is licensed under \n"
									+ "the GNU GENERAL PUBLIC LICENSE Version 3\n\n"
									+ "Alloy license and all other third-party software licenses \n"
									+ "are stored in the LICENSES folder");
		}

	}

	private class UnrollAction extends AbstractAction {

		@Override
		public void actionPerformed(ActionEvent arg0) {

			String unrollStr = null;
			boolean exit = false;

			do {
				unrollStr = (String) JOptionPane.showInputDialog(null,
						"Please enter the number of loop unroll:",
						"Loop Unroll", JOptionPane.INFORMATION_MESSAGE);

				if (unrollStr != null) {
					try {
						Integer unroll = Integer.valueOf(unrollStr);
						options.setLoopUnroll(unroll);
						exit = true;
					} catch (NumberFormatException ex) {
						JOptionPane.showMessageDialog(null, unrollStr
								+ " is not an integer value");
					}
				} else
					exit = true;

			} while (!exit);

		}

	}

	private class SkolemizeAction extends AbstractAction {

		private static final String NO = "No";
		private static final String YES = "Yes";

		@Override
		public void actionPerformed(ActionEvent arg0) {

			Object[] skolemize_options = new Object[] { YES, NO };

			String skolemizeStr = (String) JOptionPane.showInputDialog(null,
					"Please enter if apply Alloy skolemization:", "Skolemize",
					JOptionPane.INFORMATION_MESSAGE, null, skolemize_options,
					skolemize_options[options.getRemoveQuantifiers() ? 0 : 1]);

			if (skolemizeStr != null) {
				if (skolemizeStr.equals(YES))
					options.setRemoveQuantifier(true);
				else
					options.setRemoveQuantifier(false);
			}
		}

	}

	private class UnrollIsStrictAction extends AbstractAction {

		private static final String NO = "No";
		private static final String YES = "Yes";

		@Override
		public void actionPerformed(ActionEvent arg0) {

			Object[] unroll_options = new Object[] { YES, NO };

			String unrollStr = (String) JOptionPane.showInputDialog(null,
					"Please enter if the loop unroll is strict or not:",
					"Loop Unroll", JOptionPane.INFORMATION_MESSAGE, null,
					unroll_options,
					unroll_options[options.getStrictUnrolling() ? 0 : 1]);

			if (unrollStr != null) {
				if (unrollStr.equals(YES))
					options.setStrictUnroll(true);
				else
					options.setStrictUnroll(false);
			}
		}

	}

	private class SATSolverAction extends AbstractAction {

		@Override
		public void actionPerformed(ActionEvent e) {

			final String OUTPUT_TO_FILE = "Output to file";

			// A4Options.SatSolver

			Object[] outputs = new Object[] { A4Options.SatSolver.SAT4J,
					A4Options.SatSolver.MiniSatJNI, OUTPUT_TO_FILE };

			Object selectedOutput = JOptionPane.showInputDialog(null,
					"Please select the SAT Solver :", "SAT Solver",
					JOptionPane.INFORMATION_MESSAGE, null, outputs, outputs[0]);

			if (selectedOutput != null) {

				if (selectedOutput instanceof A4Options.SatSolver) {
					options.setRunAlloyAnalyzer(true);
					options.alloyOptions.solver = (SatSolver) selectedOutput;
				} else if (selectedOutput.equals(OUTPUT_TO_FILE)) {
					options.setRunAlloyAnalyzer(false);
				}
			}

		}
	}

	private class GenerateAlsAction extends AbstractAction {

		public GenerateAlsAction() {
			super("Generate");
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {

			boolean oldValue = options.getRunAlloyAnalyzer();
			options.setRunAlloyAnalyzer(false);

			executeAction.actionPerformed(arg0);

			options.setRunAlloyAnalyzer(oldValue);

		}

	}

	private class ExecuteAction extends AbstractAction {

		private static final long serialVersionUID = 3618829598473090530L;

		public ExecuteAction() {
			super("Run");
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			try {

				if (selectedFile == null) {
					outputArea.append("No DynAlloy model was loaded!\n");
					return;
				}

				List<AlloyCommand> commands = controller
						.getAvailableCommands(selectedFile);
				AlloyCommand command = (AlloyCommand) JOptionPane
						.showInputDialog(null,
								"Please select the command to execute:",
								"Command", JOptionPane.INFORMATION_MESSAGE,
								null, commands.toArray(), commands.get(0));
				// String assertionName =
				// JOptionPane.showInputDialog("Please enter the assertion name:");
				if (command != null && selectedFile != null
						&& selectedFile.exists()) {

					if (options.getRunAlloyAnalyzer()==false) {

						outputArea.append("---------\n");
						outputArea.append("Translation started of "
								+ selectedFile.getName() + "\n");
						controller.translateAndExecute(selectedFile, command,
								options);
						outputArea
								.append("Translation finished successfully \n");
						outputArea.append("---------\n");

					} else {

						if (command.getAssertionName()!=null) {
							options.setAssertionToCheck(command.getAssertionName());
						}
						
						DynAlloySolution solution = controller
								.translateAndExecute(selectedFile, command,
										options);

						watchesTableModel.setDynAlloySolution(solution);
						debuggerPanel.setDynAlloySolution(solution);
					}
				}
			} catch (DynalloyVisualizerException ex) {
				outputArea.append(ex.getMessage() + "\n");
			} catch (VizException ex) {
				outputArea.append(ex.getMessage() + "\n");
			}
		}
	}

	/**
	 * Create the GUI and show it. For thread safety, this method should be
	 * invoked from the event-dispatching thread.
	 */
	private static void createAndShowGUI() {
		// Create and set up the window.
		String version = "1.0";

		String frame_label = String.format("DynAlloy Analyzer %s", version);

		DynAlloyVizGUI frame = new DynAlloyVizGUI(frame_label);
		frame.initialize();

		// Display the window.
		// frame.pack();
		frame.setVisible(true);
	}

	/** Returns true iff running on Windows **/
	public static boolean onWindows() {
		return System.getProperty("os.name").toLowerCase(Locale.US).startsWith(
				"windows");
	};

	/** Returns true iff running on Mac OS X. **/
	public static boolean onMac() {
		return System.getProperty("mrj.version") != null
				|| System.getProperty("os.name").toLowerCase(Locale.US)
						.startsWith("mac ");
	}

	public static void main(String[] args) {
		// Schedule a job for the event-dispatching thread:
		// creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
	}

}
