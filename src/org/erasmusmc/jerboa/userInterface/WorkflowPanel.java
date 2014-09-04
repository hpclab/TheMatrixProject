/*
 * Copyright (c) Erasmus MC
 *
 * This file is part of TheMatrix.
 *
 * TheMatrix is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.erasmusmc.jerboa.userInterface;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.erasmusmc.collections.OneToManySet;
import org.erasmusmc.collections.Pair;
import org.erasmusmc.jerboa.JerboaObjectExchange;
import org.erasmusmc.jerboa.ProgressHandler;
import org.erasmusmc.jerboa.SettingsParser;
import org.erasmusmc.jerboa.SettingsParser.ModuleSettings;
import org.erasmusmc.jerboa.SettingsParser.ParsedSettings;
import org.erasmusmc.jerboa.modules.JerboaModule;
import org.erasmusmc.utilities.StringUtilities;

/**
 * Class for loading, showing, and running workflows
 * 
 * @author schuemie
 * 
 */
public class WorkflowPanel extends JPanel
{
	protected List<JerboaModule> roots;

	private int runID = 0;
	private JButton runButton = null;
	private List<String> settings;
	private boolean terminateWhenDone = false;
	private List<String> databaseNames = new ArrayList<String>();
	private String runLabelTemplate;
	private JTextField folderField;
	private JLabel noticeLabel;

	public WorkflowPanel()
	{
		setLayout(new FlowLayout());
		noticeLabel = new JLabel("Please load workflow parameters.");
		add(noticeLabel);
	}

	public WorkflowPanel(String workflowResource)
	{
		List<String> settings = getResource(workflowResource);
		setSettings(settings);
	}

	public void runWorkFlow()
	{
		runID++;

		if (buildRunLabel())
			for (JerboaModule root : roots)
				root.go(runID);

		ProgressHandler.reportDone();
	}

	private boolean buildRunLabel()
	{
		if (runLabelTemplate == null)
		{
			JerboaObjectExchange.runLabel = "Jerboa";
			return true;
		}
		else
			if (runLabelTemplate.contains("%databaseName%"))
			{
				RunInfoDialog dialog = new RunInfoDialog(databaseNames);
				dialog.setVisible(true);
				if (dialog.getResult())
				{
					JerboaObjectExchange.runLabel = runLabelTemplate.replace("%databaseName%", dialog.getDatabaseName()).replace("%version%",
							JerboaObjectExchange.version);
					return true;
				}
				else
					return false;
			}
			else
			{
				JerboaObjectExchange.runLabel = runLabelTemplate;
				return true;
			}
	}

	public void setRunButton(JButton runButton)
	{
		this.runButton = runButton;
		runButton.setEnabled(roots != null);

		runButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				RunThread thread = new RunThread();
				thread.start();
			}
		});
	}

	public void setFolderField(JTextField folderField)
	{
		this.folderField = folderField;
	}

	public class RunThread extends Thread
	{
		public void run()
		{
			File folder = new File(JerboaObjectExchange.workingFolder);
			if (!folder.exists())
			{
				JOptionPane.showMessageDialog(JerboaObjectExchange.frame, "Working folder does not exist!");
			}
			else
			{
				runButton.setEnabled(false);

				if (folderField != null)
					folderField.setEnabled(false);

				JerboaObjectExchange.busyNotifier.notify(true);
				System.out.println(StringUtilities.now() + "\tStarting workflow");

				try
				{
					runWorkFlow();
				}
				catch (RuntimeException e)
				{
					System.err.println(e.getMessage());
					JOptionPane.showMessageDialog(JerboaObjectExchange.frame, e.getLocalizedMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				}
				finally
				{
					runButton.setEnabled(true);

					if (folderField != null)
						folderField.setEnabled(true);

					JerboaObjectExchange.busyNotifier.notify(false);
					ProgressHandler.reportDone();
					System.out.println(StringUtilities.now() + "\tDone");

					if (terminateWhenDone)
						System.exit(0);
				}
			}
		}
	}

	public List<String> getSettings()
	{
		return settings;
	}

	public void setNotice(String notice)
	{
		noticeLabel.setText(notice);
		this.repaint();
	}

	public void setSettings(List<String> settings) throws RuntimeException
	{
		this.settings = settings;

		ParsedSettings parsedSettings = SettingsParser.parse(settings);
		Map<String, JerboaModule> name2module = new HashMap<String, JerboaModule>();
		databaseNames = parsedSettings.metaData.databaseNames;
		runLabelTemplate = parsedSettings.metaData.runLabelTemplate;

		// Build all modules:
		this.removeAll();
		for (ModuleSettings moduleSettings : parsedSettings.moduleSettings)
		{
			try
			{
				JerboaModule module = null;
				try
				{
					module = (JerboaModule) Class.forName("org.erasmusmc.jerboa.modules." + moduleSettings.type).newInstance();
				}
				catch (InstantiationException e)
				{
					throw new RuntimeException("Problem instantiating class " + moduleSettings.type + ":" + e.getLocalizedMessage());
				}
				catch (IllegalAccessException e)
				{
					throw new RuntimeException("Problem instantiating class " + moduleSettings.type + ":" + e.getLocalizedMessage());
				}
				catch (ClassNotFoundException e)
				{
					module = handleClassNotFoundException(e, moduleSettings);
				}

				module.setTitle(moduleSettings.name);
				module.setVirtual(moduleSettings.virtual);
				for (Pair<String, String> parameter : moduleSettings.parameters)
					module.setParameter(parameter.object1, parameter.object2);

				module.updateParameterValues();
				name2module.put(moduleSettings.name.toLowerCase(), module);
			}
			catch (RuntimeException e)
			{
				System.err.println(e.getMessage());
				//Questo messaggio mostrava l'errore graficamente
//				JOptionPane.showMessageDialog(JerboaObjectExchange.frame,
//						"Error creating module " + moduleSettings.name + ":" + e.getLocalizedMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			}
		}

		// Connect all modules:
		for (ModuleSettings moduleSettings : parsedSettings.moduleSettings)
		{
			JerboaModule module = name2module.get(moduleSettings.name.toLowerCase());
			for (String input : moduleSettings.inputs.keySet())
			{
				String inputName = moduleSettings.inputs.get(input);
				JerboaModule inputModule = name2module.get(inputName.toLowerCase());
				if (inputModule == null)
					throw new RuntimeException("Cannot find input module " + inputName + " for module " + moduleSettings.name);

				module.setInput(input, inputModule);
			}
		}

		// Do layout:
		layoutModules(name2module);

		// Find roots:
		roots = findRoots(name2module);

		if (runButton != null)
			runButton.setEnabled(roots != null);

		setVisible(false);
		setVisible(true);
	}

	protected JerboaModule handleClassNotFoundException(ClassNotFoundException e, ModuleSettings moduleSettings)
	{
		throw new RuntimeException("Could not find class " + moduleSettings.type);
	}

	private List<JerboaModule> findRoots(Map<String, JerboaModule> name2module)
	{
		Set<String> inputs = new HashSet<String>();
		for (JerboaModule module : name2module.values())
		{
			for (JerboaModule input : module.getInputs())
			{
				inputs.add(input.getTitle().toLowerCase());
			}
		}

		Set<String> rootNames = new HashSet<String>(name2module.keySet());
		rootNames.removeAll(inputs);

		List<JerboaModule> roots = new ArrayList<JerboaModule>();
		for (String rootName : rootNames)
			roots.add(name2module.get(rootName));

		return roots;
	}

	private void layoutModules(Map<String, JerboaModule> name2module)
	{
		final int NONE = 0;
		final int MODULE = 1;
		final int LINE = 2;
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		int y = 0;
		int moduleCount = name2module.size();
		int placedModuleCount = 0;

		OneToManySet<String, Integer> name2column = new OneToManySet<String, Integer>();
		List<JerboaModule> leaves = findLeaves(name2module);
		int columnCount = leaves.size();
		int[] columnStatus = new int[columnCount];

		// Add leaves:
		c.weightx = 1;
		c.weighty = 1;
		for (int x = 0; x < columnCount; x++)
		{
			c.gridx = x;
			c.gridy = y;
			add(leaves.get(x), c);
			name2column.put(leaves.get(x).getTitle().toLowerCase(), x);
			placedModuleCount++;
			columnStatus[x] = MODULE;
		}
		y++;

		while (placedModuleCount < moduleCount)
		{
			Set<Integer> neededColumns = findNeededColumns(name2column, name2module);
			OneToManySet<JerboaModule, Integer> parents = findValidParents(name2column, name2module);
			Set<Integer> useColumns = removeConflicts(parents, name2column);

			// Generate connectors:
			c.weighty = 0;
			c.gridy = y;
			for (int x = 0; x < columnCount; x++)
			{
				c.gridx = x;
				if (neededColumns.contains(x))
				{
					if (columnStatus[x] == MODULE)
						if (useColumns.contains(x))
							add(new ConnectionObject(ConnectionObject.CONNECT), c);
						else
							add(new ConnectionObject(ConnectionObject.OUT), c);
					else
						if (columnStatus[x] == LINE)
						{
							if (useColumns.contains(x))
								add(new ConnectionObject(ConnectionObject.IN), c);
							else
								add(new ConnectionObject(ConnectionObject.LINE), c);
						}
				}
				columnStatus[x] = NONE;
			}
			y++;

			// Position modules and lines:
			c.weighty = 1;
			c.gridy = y;
			for (int x = 0; x < columnCount; x++)
			{
				c.gridx = x;
				if (useColumns.contains(x))
				{
					for (JerboaModule parent : parents.keySet())
					{
						Set<Integer> columns = parents.get(parent);
						int min = Collections.min(columns);
						if (min == x)
						{
							int max = Collections.max(columns);
							c.gridx = min;
							c.gridwidth = max - min + 1;
							add(parent, c);
							placedModuleCount++;
							for (int i = min; i <= max; i++)
								columnStatus[i] = MODULE;
							name2column.set(parent.getTitle().toLowerCase(), columns);
						}
					}
				}
				else
					if (neededColumns.contains(x) && !useColumns.contains(x))
					{
						add(new ConnectionObject(ConnectionObject.LINE), c);
						columnStatus[x] = LINE;
					}
			}

			y++;
		}
	}

	private Set<Integer> removeConflicts(OneToManySet<JerboaModule, Integer> parents, OneToManySet<String, Integer> name2column)
	{
		Set<Integer> usedColumns = new HashSet<Integer>();
		Iterator<JerboaModule> iterator = parents.keySet().iterator();
		while (iterator.hasNext())
		{
			JerboaModule module = iterator.next();
			Set<Integer> neededColumns = parents.get(module);
			int min = Collections.min(neededColumns);
			int max = Collections.max(neededColumns);
			boolean conflict = false;
			for (int column = min; column <= max; column++)
				if (usedColumns.contains(column))
				{
					conflict = true;
					break;
				}

			if (conflict)
				iterator.remove();
			else
				usedColumns.addAll(neededColumns);
		}
		return usedColumns;
	}

	private Set<Integer> findNeededColumns(OneToManySet<String, Integer> name2column, Map<String, JerboaModule> name2module)
	{
		Set<Integer> neededColumns = new HashSet<Integer>();
		for (JerboaModule module : name2module.values())
		{
			String name = module.getTitle().toLowerCase();
			if (!name2column.keySet().contains(name))
			{
				for (JerboaModule input : module.getInputs())
				{
					Collection<Integer> columns = name2column.get(input.getTitle().toLowerCase());
					if (columns.size() != 0)
						neededColumns.addAll(columns);
				}
			}
		}

		return neededColumns;
	}

	private OneToManySet<JerboaModule, Integer> findValidParents(OneToManySet<String, Integer> name2column, Map<String, JerboaModule> name2module)
	{
		OneToManySet<JerboaModule, Integer> parents = new OneToManySet<JerboaModule, Integer>();

		for (JerboaModule module : name2module.values())
		{
			String name = module.getTitle().toLowerCase();
			if (!name2column.keySet().contains(name))
			{
				Set<Integer> neededColumns = new HashSet<Integer>();
				boolean valid = true;
				for (JerboaModule input : module.getInputs())
				{
					Collection<Integer> columns = name2column.get(input.getTitle().toLowerCase());
					if (columns.size() == 0)
					{
						valid = false;
						break;
					}
					else
						neededColumns.addAll(columns);
				}
				if (valid)
					parents.set(module, neededColumns);
			}
		}

		return parents;
	}

	private List<JerboaModule> findLeaves(Map<String, JerboaModule> name2module)
	{
		List<JerboaModule> leaves = new ArrayList<JerboaModule>();

		for (JerboaModule module : name2module.values())
			if (module.getInputs().size() == 0)
				leaves.add(module);

		return leaves;
	}

	private List<String> getResource(String resourceName)
	{
		List<String> result = new ArrayList<String>();
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream(resourceName)));
		try
		{
			while (bufferedReader.ready())
			{
				result.add(bufferedReader.readLine());
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return result;
	}

	public void setTerminateWhenDone(boolean terminateWhenDone)
	{
		this.terminateWhenDone = terminateWhenDone;
	}

	public boolean isTerminateWhenDone()
	{
		return terminateWhenDone;
	}

	private static final long serialVersionUID = -5682083136832883747L;
}
