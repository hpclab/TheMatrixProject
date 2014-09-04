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
package org.erasmusmc.jerboa.modules;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.erasmusmc.jerboa.JerboaObjectExchange;
import org.erasmusmc.jerboa.userInterface.RoundedBorder;


/**
 * Generic class for Jerboa modules, incorporating visual and logic aspects.
 * 
 * @author martijn
 *
 */
public abstract class JerboaModule extends JPanel
{
	private int runID = Integer.MIN_VALUE;
	private String resultFilename;
	private String title;
	private JLabel titleLabel;
	private static final long serialVersionUID = -3246695012875282347L;
	private Map<String, Field> parameterName2Field = new HashMap<String, Field>();
	private Map<String, Field> inputName2field = new HashMap<String, Field>();
	private Map<String, JComponent> parameterName2ValueComponent = new HashMap<String, JComponent>();
	private boolean virtual = false;

	/**
	 * Name of the output file (relative to the working folder).<BR>
	 * default = The name of the module, without spaces, with .txt extension
	 */
	public String outputFilename;

	/**
	 * Let the module do its task.  
	 * Stores the name of the file containing the results in resultFilename
	 * Will not run module if already performed in this run
	 */
	public void go(int runID)
	{
		if (this.runID != runID)
		{
			this.runID = runID;
			for (JerboaModule input : getInputs())
				input.go(runID);

			System.out.println("Starting module " + titleLabel.getText());

			if (JerboaObjectExchange.runLabel == null)
				resultFilename = JerboaObjectExchange.workingFolder + outputFilename;
			else
				resultFilename = JerboaObjectExchange.workingFolder + outputFilename.replace("%runLabel%", JerboaObjectExchange.runLabel);
			runModule(resultFilename);	
		}
	}  

	/**
	 * Return all JerboaModules that serve as input to this module
	 * @return
	 */
	public List<JerboaModule> getInputs() 
	{
		List<JerboaModule> inputs = new ArrayList<JerboaModule>();
		for (Field inputField : inputName2field.values())
			try 
			{
				JerboaModule input = (JerboaModule)inputField.get(this);
				if (input != null)
					inputs.add(input);
				
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
			return inputs;
	}

	/**
	 * Set the input module to the given module
	 * @param inputName	Name of the variable that will point to the module
	 * @param module Reference to the module
	 */
	public void setInput(String inputName, JerboaModule module)
	{
		Field inputField = inputName2field.get(inputName);
		if (inputField == null)
			throw new RuntimeException(inputName + " is not an input field of " + this.getClass().getName());
		try 
		{
			inputField.set(this, module);
		} 
		catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	/** 
	 * Used to define custom parse behaviour in derived classes 
	 */
	public void doPostProcess() { }
	
	private void mapParametersAndInputs()
	{
		Class<?> c = this.getClass();
		while (!c.equals(JerboaModule.class.getSuperclass()))
		{
			for (Field field : c.getDeclaredFields())
			{
				if (Modifier.isPublic(field.getModifiers()))
				{
					if (field.getType().equals(JerboaModule.class))
					{
						inputName2field.put(field.getName(),field);
					} 
					else 
					{ // not an input: its a parameter
						parameterName2Field.put(field.getName(), field);
					}
				}
			}
			c = c.getSuperclass();
		}
	}

	/**
	 * Perform the calculations of the module (must override)
	 */
	protected abstract void runModule(String outputFilename);

	/**
	 * @return The name of the file containing the results of this module.
	 */
	public String getResultFilename()
	{
		return resultFilename;
	}

	public void setResultFilename(String filename)
	{
		resultFilename = filename;
	}

	public JerboaModule()
	{
		mapParametersAndInputs();
		setBorder(new RoundedBorder());
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		titleLabel = new JLabel();
		titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD));
		titleLabel.setBackground(Color.WHITE);
		titleLabel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
		add(titleLabel);
		add(Box.createVerticalStrut(5));
		add(createParameterPanel());
	}

	/**
	 * Will update the display to reflect changes in the public variables.
	 */
	@SuppressWarnings("unchecked")
	public void updateParameterValues() 
	{
		try 
		{
			for (String parameterName : parameterName2Field.keySet())
			{
				JComponent valueComponent = parameterName2ValueComponent.get(parameterName);
				JLabel valueLabel = null;
				if (valueComponent instanceof JLabel)
					valueLabel = (JLabel)valueComponent; 
				Field field = parameterName2Field.get(parameterName);
				if (field.getType().equals(int.class))
					valueLabel.setText(Integer.toString(field.getInt(this)));
				else if (field.getType().equals(boolean.class))
					valueLabel.setText(Boolean.toString(field.getBoolean(this)));	
				else if (field.getType().equals(double.class))
					valueLabel.setText(Double.toString(field.getDouble(this)));	
				else if (field.getType().equals(String.class)) 
				{
					if (field.get(this) == null)
						valueLabel.setText("");
					else
						valueLabel.setText(field.get(this).toString());	
				} 
				else if (field.getType().equals(List.class)) 
				{
					//FIXME: GIACOMO QUA CRASHA QUANDO CHIAMA IL SORTMODULE
					List<Object> list = (List<Object>)field.get(this);
					if (valueComponent instanceof JList)
						((JList)valueComponent).setListData(list.toArray());
				}
			}
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Set the parameter to the given value.
	 * @param name	Name of the public variable.
	 * @param value	Value (will be converted to the appropriate type.
	 */
	@SuppressWarnings("unchecked")
	public void setParameter(String name, String value)
	{
		try 
		{
			Field field = parameterName2Field.get(name);
		
			if (field == null)
			{
				throw new RuntimeException(name + " is not a property of " + this.getClass().getName());
			}
			else if (field.getType().equals(int.class))
			{
				field.setInt(this, Integer.parseInt(value));
			}
			else if (field.getType().equals(boolean.class))
			{
				field.setBoolean(this, Boolean.parseBoolean(value));
			}
			else if (field.getType().equals(double.class))
			{
				field.setDouble(this, Double.parseDouble(value));
			}
			else if (field.getType().equals(String.class))
			{
				field.set(this, value);
			}
			else if (field.getType().equals(List.class))
			{
				List<String> list = (List<String>)field.get(this);
				list.add(value);
			} 
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	protected JPanel createParameterPanel() 
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setBackground(Color.WHITE);
		JPanel gridPanel = new JPanel();
		gridPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.ipadx = 5;
		c.anchor = GridBagConstraints.NORTH;
		int y = 0;
		for (String parameterName : parameterName2Field.keySet())
		{
			c.gridy = y++;

			c.gridx = 0;
			c.weightx = 0.5;
			JLabel parameterNameLabel = new JLabel(parameterName);
			gridPanel.add(parameterNameLabel, c);

			c.gridx = 1;
			c.weightx = 1;
			JComponent parameterValueComponent;
			if (parameterName2Field.get(parameterName).getType().equals(List.class)){
				parameterValueComponent = new JList();
				JScrollPane pane = new JScrollPane(parameterValueComponent);
				pane.setPreferredSize(new Dimension(300,75));
				gridPanel.add(pane, c);
			} else {
				parameterValueComponent = new JLabel();
				gridPanel.add(parameterValueComponent, c);
			}

			parameterValueComponent.setForeground(Color.GRAY);
			parameterName2ValueComponent.put(parameterName, parameterValueComponent);
		}

		gridPanel.setBackground(Color.WHITE);
		gridPanel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
		panel.add(gridPanel);
		return panel;
	}

	public void setTitle(String title)
	{
		this.title = title;
		titleLabel.setText(title + (virtual?" (virtual)":""));
		if (outputFilename == null)
		{
			outputFilename = getTitle().replace(" ", "") + ".txt";
			updateParameterValues();
		}
	}

	public String getTitle()
	{
		return title;
	}

	public boolean isVirtual()
	{
		return virtual;
	}

	public void setVirtual(boolean virtual)
	{
		this.virtual = virtual;
		titleLabel.setText(title + (virtual?" (virtual)":""));
	}

	public int getRunID()
	{
		return runID;
	}
}
