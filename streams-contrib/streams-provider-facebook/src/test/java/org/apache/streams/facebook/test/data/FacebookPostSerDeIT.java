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

package org.apache.streams.facebook.test.data;

import org.apache.streams.facebook.Post;
import org.apache.streams.facebook.serializer.FacebookActivityUtil;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.pojo.json.Activity;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.BoundedInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.InputStream;
import java.nio.charset.Charset;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Tests serialization of Facebook Post inputs
 */
public class FacebookPostSerDeIT {

  private final static Logger LOGGER = LoggerFactory.getLogger(FacebookPostSerDeIT.class);
  private ObjectMapper mapper = StreamsJacksonMapper.getInstance();

  @Test
  public void Tests()
  {
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, Boolean.TRUE);
    mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, Boolean.TRUE);
    mapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, Boolean.TRUE);

    InputStream is = FacebookPostSerDeIT.class.getResourceAsStream("/testpost.json");
    is = new BoundedInputStream(is, 10000);
    String json;

    try {
      json = String.join(" ", IOUtils.readLines(is, Charset.defaultCharset()));
      LOGGER.debug(json);

      Post ser = mapper.readValue(json, Post.class);

      String de = mapper.writeValueAsString(ser);

      LOGGER.debug(de);

      Post serde = mapper.readValue(de, Post.class);

      assertEquals(ser, serde);

      LOGGER.debug(mapper.writeValueAsString(serde));

      Activity activity = new Activity();
      FacebookActivityUtil.updateActivity(ser, activity);

      assertNotNull(activity);
      assertNotNull(activity.getActor().getId());
      assertNotNull(activity.getActor().getDisplayName());
      assertNotNull(activity.getId());
      assert(activity.getVerb().equals("post"));
      assertNotNull(activity.getObject());
      assertNotNull(activity.getUpdated());
      assertNotNull(activity.getPublished());
      assertEquals(activity.getProvider().getId(), "id:providers:facebook");
      assertEquals(activity.getProvider().getDisplayName(), "Facebook");
      assertEquals(activity.getLinks().size(), 1);
      assertNotNull(activity.getAdditionalProperties().get("facebook"));

    } catch( Exception e ) {
      LOGGER.error("Exception: ", e);
      Assert.fail();
    }
  }
}
