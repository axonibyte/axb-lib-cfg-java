/*
 * Copyright (c) 2026 Axonibyte Innovations, LLC. All rights reserved.
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

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * Parses a configuration set from environment variables.
 *
 * <p>Command-line arguments are visible to any local process via {@code ps} or
 * {@code /proc/<pid>/cmdline}, so passing database passwords and cryptographic
 * secrets as {@code --db.password hunter2} exposes them to every user on the host.
 * Environment variables avoid that, and are what container runtimes and orchestrators
 * expect to supply.</p>
 *
 * <p>Parameter paths are translated to variable names by upper-casing and replacing
 * each {@code .} or {@code -} with {@code _}, so {@code db.password} is read from
 * {@code DB_PASSWORD}. An optional prefix namespaces the variables, so a prefix of
 * {@code CROWDEASE} reads {@code CROWDEASE_DB_PASSWORD} instead.</p>
 *
 * <p>Typical layering, where later sources win:</p>
 *
 * <pre>
 *   var config = fileConfig.merge(envConfig).merge(clConfig);
 * </pre>
 *
 * @author Caleb L. Power &lt;cpower@axonibyte.com&gt;
 */
public class EnvConfig extends Config {

  /**
   * Translates a parameter path into its conventional environment variable name.
   *
   * @param path the parameter path, e.g. {@code db.password}
   * @param prefix an optional namespace prefix, or {@code null} for none
   * @return the environment variable name, e.g. {@code DB_PASSWORD}
   */
  public static String toEnvName(String path, String prefix) {
    Objects.requireNonNull(path);
    String name = path.strip()
        .replace('.', '_')
        .replace('-', '_')
        .toUpperCase();
    return null == prefix || prefix.isBlank()
        ? name
        : prefix.strip().replace('.', '_').replace('-', '_').toUpperCase() + "_" + name;
  }

  private final String prefix;

  /**
   * Instantiates an environment-backed configuration with no prefix.
   */
  public EnvConfig() {
    this(null);
  }

  /**
   * Instantiates an environment-backed configuration.
   *
   * @param prefix a namespace prefix applied to every variable name, or {@code null}
   */
  public EnvConfig(String prefix) {
    this.prefix = prefix;
  }

  /**
   * Instantiates an environment-backed configuration that inherits the parameter
   * definitions of an existing configuration.
   *
   * @param config the {@link Config} whose parameter definitions should be adopted
   * @param prefix a namespace prefix applied to every variable name, or {@code null}
   */
  public EnvConfig(Config config, String prefix) {
    super(config);
    this.prefix = prefix;
  }

  /**
   * Reads values for every defined parameter from the process environment. Parameters
   * with no corresponding variable are left unset, so they fall through to their
   * default or to another layer of a merged configuration.
   *
   * @return the number of parameters that were populated
   */
  public int loadEnv() {
    return loadEnv(System::getenv);
  }

  /**
   * Reads values for every defined parameter from an arbitrary source. Exposed so
   * that the translation and population logic can be tested without mutating the
   * real process environment, which Java cannot portably do.
   *
   * @param source resolves a variable name to its value, or {@code null} if unset
   * @return the number of parameters that were populated
   */
  public int loadEnv(Function<String, String> source) {
    Objects.requireNonNull(source);

    int populated = 0;
    for(Map.Entry<Param, Object> entry : getParamMap().entrySet()) {
      Param param = entry.getKey();
      String value = source.apply(toEnvName(param.toString(), prefix));

      // An explicitly empty variable is treated as unset. Otherwise an empty value in
      // a container spec would silently override a perfectly good default.
      if(null == value || value.isEmpty()) continue;

      configVals.put(param, value);
      populated++;
    }

    return populated;
  }

}
