package com.samplecode.twitterfriends.datasource.twitter

import org.springframework.boot.context.properties.ConfigurationProperties

/**
  * Created by vdonets on 4/8/2017.
  */
@ConfigurationProperties(prefix = "twitter")
private[twitter] class TwitterClientConfig {
  private[twitter] var bearerToken: String = null

  private[twitter] var socketTimeout: Int = 1500
  private[twitter] var connectionTimeout: Int = 1500
  private[twitter] var connRequestTimeout: Int = 4000


  private[twitter] var samplesize: Int = 1000000
  private[twitter] var consumerSecret: String = null
  private[twitter] var consumerKey: String = null
  private[twitter] var hostName: String = null


  def setSamplesize(size: Int) = samplesize = size

  def setConsumerSecret(secret: String) = consumerSecret = secret

  def setConsumerKey(key: String) = consumerKey = key

  def setHost(host: String) = hostName = host

  def setBearerToken(token: String) = bearerToken = token

  def setSocketTimeout(timeout: Int) = socketTimeout = timeout

  def setConnectionTimeout(timeout: Int) = connectionTimeout = timeout

  def setConnectionRequestTimeout(timeout: Int) = connRequestTimeout = timeout

}
