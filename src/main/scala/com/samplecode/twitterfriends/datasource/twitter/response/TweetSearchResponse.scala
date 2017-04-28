package com.samplecode.twitterfriends.datasource.twitter.response

import com.fasterxml.jackson.annotation.{JsonCreator, JsonProperty}

/**
  * Data structure containing all necessary data to map tweets
  * to user accounts that made them
  * Created by vdonets on 4/8/2017.
  */
private[twitter] case class
TweetSearchResponse @JsonCreator()(@JsonProperty("statuses") statuses: Array[Tweet],
                                   @JsonProperty("search_metadata") meta: SearchMetadata)


/**
  * Actual tweet
  *
  * @param id
  * @param text
  * @param user
  */
private[twitter] case class
Tweet @JsonCreator()(@JsonProperty("id") id: Long,
                     @JsonProperty("text") text: String,
                     @JsonProperty("user") user: User)

/**
  * User data - will help to map tweets to user accounts
  *
  * @param id
  */
private[twitter] case class
User @JsonCreator()(@JsonProperty("id") id: Long)

/**
  * Metadata holding next result field. If this field is present,
  * the response can be paginated further
  *
  * @param nextResults
  */
private[twitter] case class
SearchMetadata @JsonCreator()(@JsonProperty("next_results") nextResults: String)