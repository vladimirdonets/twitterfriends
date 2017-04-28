package com.samplecode.twitterfriends.datasource.twitter.response

import com.fasterxml.jackson.annotation.{JsonCreator, JsonProperty, JsonValue}

/**
  * Created by vdonets on 4/8/2017.
  */
private[twitter] case class
UserIdsResponse @JsonCreator()(
                                @JsonProperty("next_cursor_str") nextCursor: String,
                                @JsonProperty("ids") ids: Iterable[Long])