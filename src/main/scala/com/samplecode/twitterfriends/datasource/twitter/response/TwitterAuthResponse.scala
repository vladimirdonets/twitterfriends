package com.samplecode.twitterfriends.datasource.twitter.response

import com.fasterxml.jackson.annotation.{JsonCreator, JsonProperty, JsonValue}

/**
  * Created by vdonets on 4/8/2017.
  */
private[twitter] case class
TwitterAuthResponse @JsonCreator()(
                                    @JsonProperty("token_type") val tokenType: String,
                                    @JsonProperty("access_token") val tokenValue: String)
