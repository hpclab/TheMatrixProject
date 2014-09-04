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

import java.util.Iterator;

import org.erasmusmc.jerboa.dataClasses.Prescription;
import org.erasmusmc.jerboa.dataClasses.PrescriptionFileReader;
import org.erasmusmc.jerboa.dataClasses.PrescriptionFileWriter;
import org.erasmusmc.jerboa.userInterface.VirtualTable;

/** 
 * Module for concatenating 2-4 prescription files.
 * @author schuemie
 *
 */
public class ConcatenatePrescriptionsModule extends JerboaModule {

	public JerboaModule prescriptions1;
	public JerboaModule prescriptions2;
	public JerboaModule prescriptions3;
	public JerboaModule prescriptions4;

	private static final long serialVersionUID = 6548289298818280455L;

	@SuppressWarnings("unchecked")
	protected void runModule(String outputFilename) {
		Iterator<Prescription> iterator1;	
		Iterator<Prescription> iterator2;	
		Iterator<Prescription> iterator3 = null;	
		Iterator<Prescription> iterator4 = null;

		if (prescriptions1.isVirtual())
			iterator1 = ((VirtualTable<Prescription>) prescriptions1).getIterator();
		else
			iterator1 = new PrescriptionFileReader(prescriptions1.getResultFilename()).iterator();

		if (prescriptions2.isVirtual())
			iterator2 = ((VirtualTable<Prescription>) prescriptions2).getIterator();
		else
			iterator2 = new PrescriptionFileReader(prescriptions2.getResultFilename()).iterator();

		if (prescriptions3 != null){
			if (prescriptions3.isVirtual())
				iterator3 = ((VirtualTable<Prescription>) prescriptions3).getIterator();
			else
				iterator3 = new PrescriptionFileReader(prescriptions3.getResultFilename()).iterator();
		}

		if (prescriptions4 != null){
			if (prescriptions4.isVirtual())
				iterator4 = ((VirtualTable<Prescription>) prescriptions4).getIterator();
			else
				iterator4 = new PrescriptionFileReader(prescriptions4.getResultFilename()).iterator();
		}
		process(iterator1,iterator2,iterator3,iterator4,outputFilename);
	}

	public void process(Iterator<Prescription> iterator1, Iterator<Prescription> iterator2, Iterator<Prescription> iterator3, Iterator<Prescription> iterator4, String target){
		PrescriptionFileWriter out = new PrescriptionFileWriter(target);
		while (iterator1.hasNext())
			out.write(iterator1.next());
		while (iterator2.hasNext())
			out.write(iterator2.next());
		if (iterator3 != null)
			while (iterator3.hasNext())
				out.write(iterator3.next());
		if (iterator4 != null)
			while (iterator4.hasNext())
				out.write(iterator4.next());
		out.close();
	}
}
