package com.samplecode.twitterfriends.datasource.twitter.response

import com.fasterxml.jackson.annotation.{JsonCreator, JsonProperty}

/**
  * Created by vdonets on 4/8/2017.
  */
private[twitter] case class ErrorResponse @JsonCreator()(
                                                          @JsonProperty("errors") errors: Iterable[ErrorEntry]) {

}

private[twitter] case class ErrorEntry @JsonCreator()(@JsonProperty("message") message: String,
                                                      @JsonProperty("code") code: Int)