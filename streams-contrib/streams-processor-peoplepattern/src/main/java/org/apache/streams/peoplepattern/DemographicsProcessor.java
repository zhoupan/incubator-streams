/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.streams.peoplepattern;

import org.apache.streams.components.http.HttpProcessorConfiguration;
import org.apache.streams.components.http.processor.SimpleHTTPGetProcessor;
import org.apache.streams.config.ComponentConfigurator;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.pojo.extensions.ExtensionUtil;
import org.apache.streams.pojo.json.Activity;
import org.apache.streams.pojo.json.ActivityObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Enrich actor with demographics.
 */
public class DemographicsProcessor extends SimpleHTTPGetProcessor {

  public static final String STREAMS_ID = "DemographicsProcessor";

  private static final Logger LOGGER = LoggerFactory.getLogger(DemographicsProcessor.class);

  /**
   * DemographicsProcessor constructor - resolves HttpProcessorConfiguration from JVM 'peoplepattern'.
   */
  public DemographicsProcessor() {
    this(new ComponentConfigurator<>(HttpProcessorConfiguration.class)
        .detectConfiguration(StreamsConfigurator.getConfig().getConfig("peoplepattern")));
  }

  /**
   * AccountTypeProcessor constructor - uses supplied HttpProcessorConfiguration.
   * @param peoplePatternConfiguration peoplePatternConfiguration
   */
  public DemographicsProcessor(HttpProcessorConfiguration peoplePatternConfiguration) {
    super(peoplePatternConfiguration);
    LOGGER.info("creating DemographicsProcessor");
    configuration.setProtocol("https");
    configuration.setHostname("api.peoplepattern.com");
    configuration.setResourcePath("/v0.2/demographics/");
    configuration.setEntity(HttpProcessorConfiguration.Entity.ACTOR);
    configuration.setExtension("demographics");
  }

  /**
   Override this to add parameters to the request.
   */
  @Override
  protected Map<String, String> prepareParams(StreamsDatum entry) {
    Activity activity = mapper.convertValue(entry.getDocument(), Activity.class);
    ActivityObject actor = mapper.convertValue(activity.getActor(), ActivityObject.class);
    String username = (String) ExtensionUtil.getInstance().getExtension(actor, "screenName");
    Map<String, String> params = new HashMap<>();
    params.put("id", actor.getId());
    params.put("name", actor.getDisplayName());
    params.put("username", username);
    params.put("description", actor.getSummary());
    return params;
  }

}
