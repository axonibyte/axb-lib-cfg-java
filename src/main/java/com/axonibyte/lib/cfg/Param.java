/*
 * Copyright (c) 2019-2023 Axonibyte Innovations, LLC
 *
 *   Licensed under the Blue Oak Model License 1.0.0.
 *   See LICENSE for the full terms.
 */
package com.axonibyte.lib.cfg;

/**
 * A configuration parameter.
 *
 * @author Caleb L. Power <cpower@axonibyte.com>
 */
public class Param {

  private String path = null;
  private Object detour = null;

  /**
   * Instantiates a configuration parameter.
   *
   * @param path the JSON path to the argument
   */
  public Param(String path) {
    this.path = path;
  }

  /**
   * Instantiates a configuration parameter.
   *
   * @param path the JSON path to the argument
   * @param detour the default value or alternative {@link Param} to look up if
   *        the argument associated with this object hasn't been specified
   */
  public Param(String path, Object detour) {
    this.path = path;
    this.detour = detour;
  }
  
  /**
   * Retrieves the parameter that should be queried if this argument is not
   * properly defined. Alternatively, defines some default value.
   *
   * If this method returns {@code null}, then there is no detour.
   *
   * @return the next place to look if this argument fails
   */
  public Object getDetour() {
    return detour;
  }

  @Override public String toString() {
    return path;
  }
  
}
