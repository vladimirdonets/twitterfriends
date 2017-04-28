package com.samplecode.twitterfriends.datasource.twitter

import com.fasterxml.jackson.annotation.{JsonCreator, JsonIgnoreProperties, JsonProperty, JsonValue}
import com.samplecode.twitterfriends.beans.UserProfile

/**
  * Created by vdonets on 4/8/2017.
  */
@JsonIgnoreProperties(ignoreUnknown = true)
private[twitter] case class
TwitterProfile @JsonCreator()(
                               @JsonProperty("protected") isProtected: Boolean,
                               @JsonProperty("id") id: Long,
                               @JsonProperty("name") name: String,
                               @JsonProperty("screen_name") screenName: String,
                               @JsonProperty("profile_image_url") imgUrl: String) extends UserProfile {

}
