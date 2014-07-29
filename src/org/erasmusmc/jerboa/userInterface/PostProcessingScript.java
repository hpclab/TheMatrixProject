package org.erasmusmc.jerboa.userInterface;

/**
 * Interface for post-processing applications.
 * @author schuemie
 *
 */
public interface PostProcessingScript {
  public void process(String sourceFile, String targetFolder);
}
