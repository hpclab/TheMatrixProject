package org.erasmusmc.jerboa.postProcessing;

import org.erasmusmc.jerboa.userInterface.PostProcessingFrame;
import org.erasmusmc.jerboa.userInterface.PostProcessingScript;

/**
 * Stand alone application for decrypting, decompressing and splitting Jerboa encrypted files
 * @author schuemie
 *
 */
public class DecryptDecompress implements PostProcessingScript {
	
	public static void main(String[] args){
		new PostProcessingFrame(new DecryptDecompress(), "Jerboa post processor", args); 
	}

	@Override
	public void process(String sourceFile, String folder) {
		System.out.println("Splitting raw tables");
		TableSplitter.split(sourceFile, folder);		
	}
	
}
