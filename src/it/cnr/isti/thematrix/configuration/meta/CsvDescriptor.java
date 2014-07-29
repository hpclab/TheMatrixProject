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
