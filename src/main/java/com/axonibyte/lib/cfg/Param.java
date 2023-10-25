/*
 * Copyright (c) 2019-2023 Axonibyte Innovations, LLC. All rights reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
