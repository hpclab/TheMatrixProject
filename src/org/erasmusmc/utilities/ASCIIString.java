package org.erasmusmc.utilities;


/**
 * String object implementation with reduced overhead (for ASCII strings only). Also does not cache hashCode.
 * @author schuemie
 *
 */
public class ASCIIString implements Comparable<ASCIIString>{
	private byte[] bytes;

	public ASCIIString(String string){
		bytes = new byte[string.length()];
		for (int i = 0; i < string.length(); i++)
			bytes[i] = (byte)string.charAt(i);
	}

	protected ASCIIString(String string, int start, int end){
		bytes = new byte[end-start];
		for (int i = start; i < end; i++)
			bytes[i-start] = (byte)string.charAt(i);
	}


	private ASCIIString() {
	}

	public int length(){
		return bytes.length;
	}

	public char charAt(int i){
		return (char)bytes[i];
	}

	@Override
	public int compareTo(ASCIIString other) {
		int min = Math.min(this.length(), other.length());
		for (int i = 0; i < min; i++){
			byte t = this.bytes[i];
			byte o = other.bytes[i];
			if (t != o)
				return t-o;
		}
		if (this.length() == other.length())
			return 0;
		if (this.length() < other.length())
			return -1;
		return 1;
	}

	public String toString(){
		StringBuilder string = new StringBuilder();
		for (int i = 0; i < bytes.length; i++)
			string.append((char)bytes[i]);
		return string.toString();
	}

	public boolean equals(Object other){
		if (other instanceof ASCIIString){
			ASCIIString otherAS = (ASCIIString)other;
			if (this.length() != otherAS.length())
				return false;
			for (int i = 0; i < bytes.length; i++){
				if (this.bytes[i] != otherAS.bytes[i])
					return false;
			}
			return true;
		} else
			return false;
	}

	public int hashCode() {
		if (bytes.length == 0) {
			return 0;
		}
		int hash = 0;
		for (int i = 0; i < bytes.length; i++) {
			hash = bytes[i] + ((hash << 5) - hash);
		}
		return hash;
	}

	public ASCIIString substring(int beginIndex, int endIndex){
		ASCIIString substring = new ASCIIString();
		substring.bytes = new byte[endIndex-beginIndex];
		System.arraycopy(bytes, beginIndex, substring.bytes, 0, endIndex-beginIndex);
		return substring;
	}

	public boolean endsWith(ASCIIString string) {
		if (string.length() > length())
			return false;
		for (int i = 0; i < string.length(); i++)
			if (bytes[bytes.length-i-1] != string.bytes[string.bytes.length-i-1])
				return false;
		return true;
	}

	public boolean startsWith(ASCIIString string) {
		if (string.length() > length())
			return false;
		for (int i = 0; i < string.length(); i++)
			if (bytes[i] != string.bytes[i])
				return false;
		return true;
	}

	public int indexOf(ASCIIString string){
		return indexOf(string,0);
	}

	public int indexOf(ASCIIString string, int fromIndex){
		int match = 0;
		for (int i = fromIndex; i < bytes.length; i++){
			if (bytes[i] == string.bytes[match]){
				match++;
				if (match == string.bytes.length)
					return i-string.bytes.length+1;
			} else
				match = 0;

		}
		return -1;
	}

	public ASCIIString replace(ASCIIString oldString, ASCIIString newString){
		int index = 0;
		ASCIIString result = new ASCIIString();
		result.bytes = bytes;
		while (index != -1){
			index = result.indexOf(oldString);
			if (index != -1){
				byte[] tempBytes = new byte[result.bytes.length - oldString.length() + newString.length()];
				System.arraycopy(result.bytes, 0, tempBytes, 0, index); //Copy part before oldString
				System.arraycopy(newString.bytes, 0, tempBytes, index, newString.length()); //Copy newString
				System.arraycopy(result.bytes, index+oldString.length(), tempBytes, index+newString.length(), result.bytes.length-(index+oldString.length())); //Copy part after oldString
				result.bytes = tempBytes;
			}
		}

		return result;
	}

	public boolean contains(ASCIIString string) {
		int match = 0;
		for (int i = 0; i < bytes.length; i++){
			if (bytes[i] == string.bytes[match]){
				match++;
				if (match == string.bytes.length)
					return true;
			} else
				match = 0;

		}
		return false;
	}

	public ASCIIString add(ASCIIString string){
		ASCIIString result = new ASCIIString();
		result.bytes = new byte[bytes.length + string.length()];
		System.arraycopy(bytes, 0, result.bytes, 0, bytes.length);
		System.arraycopy(string.bytes, 0, string.bytes, bytes.length, string.bytes.length);
		return result;
	}

	public ASCIIString toLowerCase() {
		ASCIIString result = new ASCIIString();
		result.bytes = new byte[bytes.length];
		System.arraycopy(bytes, 0, result.bytes, 0, bytes.length);
		for (int i = 0; i < bytes.length; i++)
			if (bytes[i] >= 65 && bytes[i] <= 90)
				result.bytes[i] += 32;				
		return result;
	}
}

