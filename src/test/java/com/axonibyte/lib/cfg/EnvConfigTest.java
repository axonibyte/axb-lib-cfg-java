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

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests environment-variable-backed configuration.
 *
 * @author Caleb L. Power &lt;cpower@axonibyte.com&gt;
 */
public class EnvConfigTest {

  @Test public void toEnvName_translatesDottedPaths() {
    Assert.assertEquals(EnvConfig.toEnvName("db.password", null), "DB_PASSWORD");
    Assert.assertEquals(EnvConfig.toEnvName("api.allowedOrigins", null), "API_ALLOWEDORIGINS");
    Assert.assertEquals(EnvConfig.toEnvName("relay.endpoint", null), "RELAY_ENDPOINT");
  }

  @Test public void toEnvName_translatesHyphens() {
    Assert.assertEquals(EnvConfig.toEnvName("some-param.sub-key", null), "SOME_PARAM_SUB_KEY");
  }

  @Test public void toEnvName_appliesPrefix() {
    Assert.assertEquals(
        EnvConfig.toEnvName("db.password", "crowdease"), "CROWDEASE_DB_PASSWORD");
    Assert.assertEquals(EnvConfig.toEnvName("db.password", "  "), "DB_PASSWORD");
    Assert.assertEquals(EnvConfig.toEnvName("db.password", null), "DB_PASSWORD");
  }

  @Test public void loadEnv_populatesDefinedParams() {
    var config = new EnvConfig();
    var dbPassword = new Param("db.password");
    var dbUsername = new Param("db.username");
    config.defineParam(dbPassword);
    config.defineParam(dbUsername);

    Map<String, String> env = Map.of(
        "DB_PASSWORD", "hunter2",
        "DB_USERNAME", "crowdease");

    Assert.assertEquals(config.loadEnv(env::get), 2);
    Assert.assertEquals(config.getString("db.password"), "hunter2");
    Assert.assertEquals(config.getString("db.username"), "crowdease");
  }

  @Test public void loadEnv_leavesUnsetParamsAlone() {
    var config = new EnvConfig();
    // A param with a default: an absent variable must not clobber it.
    config.defineParam(new Param("db.prefix", "finance_"));
    config.defineParam(new Param("db.password"));

    Map<String, String> env = Map.of("DB_PASSWORD", "hunter2");

    Assert.assertEquals(config.loadEnv(env::get), 1);
    Assert.assertEquals(config.getString("db.prefix"), "finance_");
  }

  @Test public void loadEnv_treatsEmptyValueAsUnset() {
    var config = new EnvConfig();
    config.defineParam(new Param("db.prefix", "finance_"));

    Map<String, String> env = Map.of("DB_PREFIX", "");

    // An empty variable in a container spec should not silently blank out a default.
    Assert.assertEquals(config.loadEnv(env::get), 0);
    Assert.assertEquals(config.getString("db.prefix"), "finance_");
  }

  @Test public void loadEnv_honoursPrefix() {
    var config = new EnvConfig("crowdease");
    config.defineParam(new Param("db.password"));

    Assert.assertEquals(config.loadEnv(Map.of("DB_PASSWORD", "wrong")::get), 0);
    Assert.assertEquals(
        config.loadEnv(Map.of("CROWDEASE_DB_PASSWORD", "right")::get), 1);
    Assert.assertEquals(config.getString("db.password"), "right");
  }

  @Test public void merge_letsCommandLineOverrideEnvironment() {
    // The intended layering: file < env < CLI.
    var env = new EnvConfig();
    var dbPassword = new Param("db.password");
    var dbUsername = new Param("db.username");
    env.defineParam(dbPassword);
    env.defineParam(dbUsername);
    env.loadEnv(Map.of("DB_PASSWORD", "from-env", "DB_USERNAME", "from-env")::get);

    var cli = new CLConfig();
    cli.defineParam(dbPassword);
    cli.defineParam(dbUsername);
    try {
      cli.loadArgs(new String[] { "--db.username", "from-cli" });
    } catch(CLConfig.CommandArgException e) {
      Assert.fail("unexpected parse failure", e);
    }

    Config merged = env.merge(cli);

    Assert.assertEquals(merged.getString("db.username"), "from-cli", "CLI should win");
    Assert.assertEquals(
        merged.getString("db.password"), "from-env", "env should survive where CLI is silent");
  }

}
