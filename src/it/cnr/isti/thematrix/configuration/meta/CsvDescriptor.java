/*
 * Copyright (c) 2010-2014 "HPCLab at ISTI-CNR"
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.cnr.isti.thematrix.configuration.meta;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "csvDescriptor")
public class CsvDescriptor 
{
	@XmlElement
	private String theMatrixVersion;

	@XmlElement
	private String checksumLight;
	
	@XmlElement
	private String checksumHard;
	
	@XmlElement
	private String timestamp;

	@XmlElement
	private String json;
	
	public String getVersion() {
		return theMatrixVersion;
	}

	public void setTheMatrixVersion(String theMatrixVersion) {
		this.theMatrixVersion = theMatrixVersion;
	}

	public String getMetaChecksumLight() {
		return checksumLight;
	}

	public void setChecksumLight(String checksumLight) {
		this.checksumLight = checksumLight;
	}

	public String getMetaChecksumHard() {
		return checksumHard;
	}

	public void setChecksumHard(String checksumHard) {
		this.checksumHard = checksumHard;
	}

	public String getMetaTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public String getJsonDescription() {
		return json;
	}

	public void setJson(String jsonDescription) {
		this.json = jsonDescription;
	}

}
