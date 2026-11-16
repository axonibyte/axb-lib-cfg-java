/*
 * Copyright (c) 2019-2023 Axonibyte Innovations, LLC
 *
 *   Licensed under the Blue Oak Model License 1.0.0.
 *   See LICENSE for the full terms.
 */
package com.axonibyte.lib.cfg;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Denotes a driver to read a serialized configuration state from a file.
 *
 * @author Caleb L. Power <cpower@axonibyte.com>
 */
public class FileConfig extends JSONConfig {
  
  private String resource = null;
  
  /**
   * Instantiates a file-based configuration state.
   *
   * @param resource the resource location
   */
  public FileConfig(String resource) {
    this.resource = resource;
  }
  
  /**
   * Loads the file.
   *
   * @throws FileReadException if the file could not be read
   * @throws JSONException     if the file could not be parsed
   */
  public void load() throws FileReadException, JSONException {
    if(null == resource) throw new FileReadException("File not specified.");
    
    String raw = null;
    
    File file = new File(resource);
    try(BufferedReader in = new BufferedReader(
        new InputStreamReader(
            file.canRead() ? new FileInputStream(file) : FileConfig.class.getResourceAsStream(resource),
            StandardCharsets.UTF_8))) {
      StringBuilder stringBuilder = new StringBuilder();
      for(String line; null != (line = in.readLine());)
        stringBuilder.append(line.trim());
      raw = stringBuilder.toString();
      deserialize(new JSONObject(raw));
    } catch(IOException | NullPointerException e) {
      throw new FileReadException("Could not obtain raw config data.");
    }
  }
  
  /**
   * An exception to be thrown if a file cannot be read.
   *
   * @author Caleb L. Power <cpower@axonibyte.com>
   */
  public final class FileReadException extends Exception {
    
    private static final long serialVersionUID = -7176436537758394913L;
    
    private FileReadException(String message) {
      super(message);
    }
    
  }
  
}
