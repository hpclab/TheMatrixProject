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
package org.erasmusmc.jerboa;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.swing.JFrame;

import org.erasmusmc.jerboa.userInterface.Console;
import org.erasmusmc.jerboa.userInterface.WorkflowPanel;
import org.erasmusmc.utilities.StringUtilities;

/**
 * Static class for holding some global objects.
 * 
 * @author schuemie
 * 
 */
public class JerboaObjectExchange
{
	public static WorkflowPanel mainWorkflowPanel;
	public static String workingFolder;
	public static String version = extractVersionIDFromJarDesc();
	public static JFrame frame; // The application frame
	public static BooleanNotifier busyNotifier = new BooleanNotifier();
	public static Console console;
	public static String runLabel;

	public static class BooleanNotifier
	{
		private List<BooleanNoticeListener> listeners = new ArrayList<BooleanNoticeListener>();

		public void addListener(BooleanNoticeListener listener)
		{
			listeners.add(listener);
		}

		public void removeListener(BooleanNoticeListener listener)
		{
			listeners.remove(listener);
		}

		public void notify(boolean value)
		{
			for (BooleanNoticeListener listener : listeners)
				listener.noticeChange(this, value);
		}
	}

	public interface BooleanNoticeListener
	{
		public void noticeChange(BooleanNotifier sender, boolean value);
	}

	private static String extractVersionIDFromJarDesc()
	{
		String result = "Unknown";
		try
		{
			String jarLocation = JerboaObjectExchange.class.getProtectionDomain().getCodeSource().getLocation().getPath().replace("%20", " ");
			BufferedReader bufferedReader = null;
			if (jarLocation.endsWith(".jar"))
			{ // Running from a jar file: extract from jar file:
				// System.out.println("Jarlocation: " + jarLocation);
				ZipFile file = new ZipFile(jarLocation);
				Enumeration<? extends ZipEntry> entries = file.entries();
				while (entries.hasMoreElements())
				{
					ZipEntry entry = entries.nextElement();
					if (entry.getName().toLowerCase().contains("jerboa") && entry.getName().toLowerCase().endsWith(".jardesc"))
					{
						bufferedReader = new BufferedReader(new InputStreamReader(file.getInputStream(entry), "UTF-8"));
						// System.out.println("Jardesc location: " +
						// entry.getName());
						break;
					}
				}

			}
			else
				if (jarLocation.endsWith("/bin/"))
				{ // Running from an extracted environment: load jardesc
				  // directly:
					// FIXME: changed old path from jarDesc to libs for the
					// jerboa.jardesc jar
					// File jarDescFolder = new
					// File(jarLocation.replace("/bin/", "/jarDescs/"));
					File jarDescFolder = new File(jarLocation.replace("/bin/", "/libs/"));
					for (File file : jarDescFolder.listFiles())
						if (file.getName().toLowerCase().contains("jerboa") && file.getName().toLowerCase().endsWith(".jardesc"))
							bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
				}
				else
					throw new RuntimeException("Failed to load version ID from jarDesc. Current location is " + jarLocation);

			if (bufferedReader == null)
				return result;
			String originalJarName = null;
			while (bufferedReader.ready())
			{
				String line = bufferedReader.readLine();
				if (line.contains("<jar path"))
				{
					originalJarName = StringUtilities.findBetween(line, "<jar path=\"", "\"/>");
					break;
				}
			}
			bufferedReader.close();
			result = StringUtilities.findBetween(new File(originalJarName).getName(), "_v", ".jar");
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return result;
	}

	public static List<String> load(InputStream stream)
	{
		List<String> lines = new ArrayList<String>();
		try
		{
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
			while (bufferedReader.ready())
			{
				String line = bufferedReader.readLine();
				lines.add(line);
			}
			bufferedReader.close();
		}
		catch (UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return lines;
	}

}
