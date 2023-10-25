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

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.json.JSONArray;

/**
 * An overloadable configuration driver.
 *
 * @author Caleb L. Power <cpower@axonibyte.com>
 */
public class Config {
  
  /**
   * Configuration values.
   */
  protected final Map<Param, Object> configVals = new HashMap<>();

  private final Map<String, Param> configParams = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
  
  /**
   * Instantiates a new config object.
   */
  protected Config() { }
  
  /**
   * Copy constructor.
   * 
   * @param config the original config
   */
  protected Config(Config config) {
    config.configParams.forEach((k, v) -> configParams.put(k, v));
    config.configVals.forEach((k, v) -> configVals.put(k, v));
  }

  /**
   * Retrieves the set of all known parameters and their respective values.
   *
   * @return a map of parameters and their respective values; parameters without
   *         an explcit value will be mapped to {@code null}
   */
  Map<Param, Object> getParamMap() {
    Map<Param, Object> map = new HashMap<>();
    for(var param : configParams.entrySet())
      map.put(
          param.getValue(),
          configVals.containsKey(param.getValue()) ? configVals.get(param.getValue()) : null);
    return map;
  }

  /**
   * Retrieves the {@link Param} object associated with the provided key.
   *
   * @param key the string associated with the parameter
   */
  Param getParam(String key) {
    return configParams.get(key);
  }
  
  /**
   * Resolves a configuration parameter into its respective argument, its
   * default (if such an argument does not exist), or the argument of its
   * detoured parameter, if one has been specified.
   *
   * @param param the parameter to query
   * @return the argument, if one exists; otherwise, {@code null}
   * @throws BadParamException if there was no argument or appropriate detour
   */
  public Object resolve(String param) throws BadParamException {
    Object val = null;
    if(null == param
        || !configParams.containsKey(param)
        || null == (val = resolve(configParams.get(param))))
      throw new BadParamException(param);
    return val;
  }

  private Object resolve(Param param) {
    if(configVals.containsKey(param)) return configVals.get(param);
    var detour = param.getDetour();
    return (detour instanceof Param) ? resolve((Param)detour) : detour;
  }

  /**
   * Retrieves the String value of the requested config value.
   *
   * @param param the configuration parameter
   * @return a String denoting the requested configuration value
   * @throws BadParamException iff the argument that corresponds with the
   *         provided parameter is either undefined or {@code null}
   */
  public String getString(String param) throws BadParamException {
    return String.valueOf(resolve(param));
  }

  /**
   * Retrieves the char value of the requested config option.
   *
   * @param param the configuration parameter
   * @return a char denoting the value of the requested config option
   * @throws BadParamException if the value that corresponds with the provided
   *         parameter is undefined or {@code null}, or if said value could not be
   *         converted to a char
   */
  public char getChar(String param) throws BadParamException {
    var arg = getString(param);
    if(1 != arg.length()) throw new BadParamException(param);
    return arg.charAt(0);
  }

  /**
   * Retrieves the boolean value of the requested config option.
   *
   * @param param the configuration parameter
   * @return a boolean denoting the value of the requested config option
   * @throws BadParamException if the value that corresponds with
   *         the provided parameter is undefined or {@code null}
   */
  public boolean getBoolean(String param) throws BadParamException {
    return Boolean.parseBoolean(getString(param));
  }

  /**
   * Retrieves the integer value of the requested config option.
   *
   * @param param the configuration parameter
   * @return an integer denoting the value of the requested config option
   * @throws BadParamException if the value that corresponds with
   *         the provided parameter is undefined or {@code null}, or if
   *         said value could not be converted to an integer
   */
  public int getInteger(String param) throws BadParamException {
    try {
      return Integer.parseInt(getString(param));
    } catch(NumberFormatException e) {
      throw new BadParamException(param);
    }
  }

  /**
   * Retrieves the long value of the requested config option.
   *
   * @param param the configuration parameter
   * @return a long datum denoting the value of the requested config option
   * @throws BadParamException if the value that corresponds with
   *         the provided parameter is undefined or {@code null}, or
   *         if said value could not be converted to a long datum
   */
  public long getLong(String param) throws BadParamException {
    try {
      return Long.parseLong(getString(param));
    } catch(NumberFormatException e) {
      throw new BadParamException(param);
    }
  }

  /**
   * Retrieves the double value of the requested config option.
   *
   * @param param the configuration parameter
   * @return a double datum denoting the value of the requested config option
   * @throws BadParamException if the value that corresponds with
   *         the provided parameter is undefined or {@code null}, or if
   *         said value could not be converted to a double datum
   */
  public double getDouble(String param) throws BadParamException {
    try {
      return Double.parseDouble(getString(param));
    } catch(NumberFormatException e) {
      throw new BadParamException(param);
    }
  }

  /**
   * Retrieves the JSONArray associated with the requested config option.
   *
   * @param param the configuration parameter
   * @return a JSONArray containing a list of objects
   * @throws BadParamException if the value that corresponds with
   *         the provided parameter is not of type {@link JSONArray}
   */
  public JSONArray getArr(String param) throws BadParamException {
    Object arr = resolve(param);
    if(!(arr instanceof JSONArray)) throw new BadParamException(param);
    return (JSONArray)arr;
  }

  /**
   * Merges this config and another config into a new Config object. Note that
   * neither this Config object nor the Config argument are mutated via this
   * procedure. Furthermore, the values of the Config argument supersede that
   * of the values of this object, unless the value of the Config argument is
   * {@code null} or is not set, in which case the value of this Config object
   * remains.
   *
   * @param config the config that should be merged into this one
   * @return a new Config representation of the two merged configurations
   */
  public Config merge(Config config) {
    Config merger = new Config(this);
    config.configVals.forEach((k, v) -> {
        if(null != v) {
          if(merger.configVals.containsKey(k))
            merger.configVals.replace(k, v);
          else merger.configVals.putIfAbsent(k, v);
        }
      });
    return merger;
  }

  /**
   * An Exception that is thrown if an argument could not be retrieved. This is
   * most likely to be thrown if the configuration argument corresponding to
   * the specified parameter is not defined.
   *
   * @author Caleb L. Power <cpower@axonibyte.com>
   */
  public final class BadParamException extends RuntimeException {
    private static final long serialVersionUID = 9128342008991622490L;

    /**
     * Instantiates the BadParamException.
     *
     * @param param the configuration parameter that could not be retrieved
     */
    public BadParamException(String param) {
      super(
          String.format(
              "Argument for parameter %1$s was not defined or has the wrong type.",
              param));
    }
  }
  
}
