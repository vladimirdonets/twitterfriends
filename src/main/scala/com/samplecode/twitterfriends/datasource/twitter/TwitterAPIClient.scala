package com.samplecode.twitterfriends.datasource.twitter

import java.net.URI
import java.util.Base64
import java.util.concurrent.atomic.AtomicInteger

import com.samplecode.twitterfriends.classifier.doc.Document
import com.samplecode.twitterfriends.datasource.Datasource
import com.samplecode.twitterfriends.datasource.twitter.response._
import com.samplecode.twitterfriends.exception.{InaccessibleProfileException, ServiceCallFailedException, ThrottledException}
import com.samplecode.twitterfriends.util.Mapper
import com.samplecode.twitterfriends.util.logging.LazyLogging
import org.apache.http.HttpRequest
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods.{CloseableHttpResponse, HttpGet, HttpPost, HttpRequestBase}
import org.apache.http.client.utils.URIBuilder
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClientBuilder

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

/**
  * Twitter implementation of datasource trait
  * Created by vdonets on 4/8/2017.
  */
private[twitter] class TwitterAPIClient(private val config: TwitterClientConfig,
                                        private val mapper: Mapper)
  extends Datasource with LazyLogging {

  private val client = HttpClientBuilder.create()
    .setDefaultRequestConfig(
      RequestConfig.custom()
        .setConnectTimeout(config.connectionTimeout)
        .setSocketTimeout(config.socketTimeout)
        .setConnectionRequestTimeout(config.connRequestTimeout).build()).build();

  private val sampleSize = config.samplesize
  private val apiHost = config.hostName
  override protected type T = TwitterProfile

  //concatinated and encoded consumer key and secret
  private val twitterKey = Base64.getEncoder.encode(
    (config.consumerKey + ":" + config.consumerSecret).getBytes()
  )

  private val bearerToken: String = {
    val token = config.bearerToken
    logger.debug("configured bearer token = [" + token + "]")
    if (token != null && token != 0) {
      token
    } else
      authenticate
  }


  /**
    * Convenience method for logging and error handling
    *
    * @param req
    * @return
    */
  private def executeRequest(req: HttpRequestBase): CloseableHttpResponse = {
    logger.debug("Request URI = " + req.getURI)
    val response = client.execute(setBearerToken(req))
    if (response.getStatusLine.getStatusCode == 200) {
      logger.debug("Successfully executed request")
    } else {
      logger.error("Error: " + response.getStatusLine.getStatusCode + ", message: " + response.getStatusLine.getReasonPhrase)
    }
    return response

  }

  @throws[ServiceCallFailedException]
  private def authenticate: String = {
    val req = new HttpPost(uriBuilder("/oauth2/token").build)
    val body = "grant_type=client_credentials"
    req.setEntity(new StringEntity(body))
    req.setHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8")
    req.setHeader("Authorization", "Basic " + new String(twitterKey))
    try {
      val response = client.execute(req)
      if (response.getStatusLine.getStatusCode == 200) {
        val authResponse = mapper.readValue(response.getEntity.getContent, classOf[TwitterAuthResponse])
        if (authResponse.tokenType.equals("bearer")) {
          logger.debug("Received bearer token [" + authResponse.tokenValue + "]")
          return "Bearer " + authResponse.tokenValue

        }
      }
    } finally {
      req.releaseConnection
    }
    throw new ServiceCallFailedException("Authentication to Twitter failed")
  }

  /**
    * Convenience method to build request URIs
    *
    * @param path
    * @return
    */
  private def uriBuilder(path: String): URIBuilder = {
    new URIBuilder().setScheme("https").setHost(apiHost).setPath(path)
  }

  /**
    * Finds a user's profile based on screen name
    *
    * @param screenName name of user
    * @return user profile
    */
  @throws[InaccessibleProfileException]
  @throws[IllegalStateException]
  override private[twitterfriends] def findUser(screenName: String): T = {
    logger.debug("Finding user [" + screenName + "]")
    if (screenName == null | screenName.length == 0)
      throw new IllegalArgumentException("screenName may not be empty")
    val req = new HttpGet(
      uriBuilder("/1.1/users/show.json").setParameter("screen_name", screenName)
        .build)
    val response = executeRequest(req)

    val profile = try {
      response.getStatusLine.getStatusCode match {
        case 200 => mapper.readValue(response.getEntity.getContent, classOf[TwitterProfile])
        case 404 => throw new InaccessibleProfileException("User [" + screenName + "] does not exist")
        case _ => throw new IllegalStateException("Unable to find user [" + screenName + "] -- " +
          response.getStatusLine.getReasonPhrase)
      }
    } catch {
      case t: Throwable =>
        t.printStackTrace()
        null
    } finally {
      response.close()
    }
    if (profile.isProtected)
      throw new InaccessibleProfileException("User is protected")
    logger.debug("Success")
    return profile
  }

  /**
    * Sets current token into given request
    *
    * @param req
    */
  private def setBearerToken[H <: HttpRequestBase](req: H): H = {
    logger.trace("Setting token [" + bearerToken + "]")
    req.setHeader("Authorization", bearerToken)
    return req
  }

  /**
    * Searches for user profiles based on given IDs
    *
    * @param userIds ids of users to get profiles for
    * @return user profiles
    */
  override private[twitterfriends] def findUsers(userIds: Iterable[Any]): Iterable[T] = {
    if (userIds.isEmpty)
      return Iterable()
    var result = new ArrayBuffer[T](userIds.size)
    //Twitter API only allows getting 100 users at a time
    val groups = userIds.grouped(100)
    groups.foreach(batch => {
      val req = new HttpGet(uriBuilder("/1.1/users/lookup.json")
        .setParameter("user_id", {
          val buffer = new StringBuilder
          val iterator = batch.iterator
          iterator.foreach(e => {
            buffer.append(e)
            if (iterator.hasNext)
              buffer.append(",")
          })
          logger.trace("user query string = " + buffer.mkString)
          buffer.mkString
        })
        .build)

      val response: Iterable[TwitterProfile] = {
        val r = executeRequest(req)
        try {
          if (r.getStatusLine.getStatusCode != 200) {
            logger.error("Unable to retrieve user profiles. Skipping batch of [" +
              batch.size + "] profiles -- " + r.getStatusLine.getStatusCode +
              ", " + r.getStatusLine.getReasonPhrase)
            if (r.getEntity.getContentLength > 0) {
              try {
                val error = mapper.readValue(r.getEntity.getContent, classOf[ErrorResponse])
                logger.error("error = " + error)
              } catch {
                case t: Throwable => t.printStackTrace
              }
            }
            ArrayBuffer()
          } else {
            mapper.readValue(r.getEntity.getContent, classOf[Array[TwitterProfile]])
          }
        } finally {
          r.close()
        }
      }
      if (!groups.hasNext && result.isEmpty)
        return response
      else if (response != null) {
        result.appendAll(response)
      }
    })
    logger.debug("Built [" + result.size + "] profiles")
    return result
  }

  private val FROM_PREFIX = "from:"
  private val OR_SEPARATOR = " OR "
  private val QUERY_LENGTH_THRESHOLD = 400 - FROM_PREFIX.length - OR_SEPARATOR.length

  @throws[ThrottledException]
  private def validateSearchResponse(response: CloseableHttpResponse): TweetSearchResponse = {
    try {
      if (response.getStatusLine.getStatusCode == 200)
        return mapper.readValue(response.getEntity.getContent, classOf[TweetSearchResponse])
      else if (response.getStatusLine.getStatusCode == 429) {
        throw new ThrottledException("Requests throttled")
      } else {
        try {
          val error = mapper.readValue(response.getEntity.getContent, classOf[ErrorResponse])
          //if we are being throttled
          logger.error("Unable to get tweets. Skipping batch -- " +
            response.getStatusLine.getStatusCode + ", "
            + response.getStatusLine.getReasonPhrase)
          logger.error("error = " + error)
        } catch {
          case t: Throwable => t.printStackTrace
        }
        return null
      }
    } finally {
      response.close()
    }
  }

  private def executeTweetQuery(queryString: String): Iterable[Tweet] = {
    val result = new ArrayBuffer[Tweet]()
    logger.debug("executing tweet query")
    logger.trace("queryString = " + queryString + " -- [" + queryString.length + "]")
    val req = new HttpGet(uriBuilder("/1.1/search/tweets.json")
      .setParameter("q", queryString)
      .setParameter("count", "100").build())
    var next: String = null
    var searchResponse: TweetSearchResponse =
      validateSearchResponse(executeRequest(req))
    do {
      if (searchResponse != null) {
        //track next results
        next =
          if (searchResponse.meta != null)
            searchResponse.meta.nextResults
          else null
        logger.trace("next = " + next)
        result.appendAll(searchResponse.statuses)
        if (next != null)
          searchResponse = {
            val req = new HttpGet(new URI("https://"
              + apiHost + "/1.1/search/tweets.json" + next))
            validateSearchResponse(executeRequest(req))
          }
      } else {
        logger.debug("No response received")
        next = null
      }

    } while (next != null && next.length != 0)
    return result


  }

  private def buildResultSet(tweet: Tweet, byId: mutable.HashMap[Long, TwitterProfile],
                             result: mutable.HashMap[TwitterProfile, ArrayBuffer[Tweet]]): Int = {
    val uid = tweet.user.id
    if (byId.contains(uid)) {
      val user = byId(uid)
      if (result.contains(user))
        result(user).append(tweet)
      else
        result(user) = ArrayBuffer(tweet)
    }
    return tweet.text.length
  }

  /**
    * Uses search API to get tweets. Much faster but unpredictable results
    *
    * @param profiles
    * @return
    */
  private def search(profiles: Iterable[TwitterProfile]): Iterable[(T, Document)] = {
    var dataSetSize: Long = 0
    val result = new mutable.HashMap[T, ArrayBuffer[Tweet]]
    try {
      logger.debug("Searching for posts")
      val iterator = profiles.iterator
      val first = iterator.find({
        case profile: T => !profile.isProtected
        case _ => false
      })
      if (first.isEmpty)
        return Iterable()
      val queryBuilder = new StringBuilder
      queryBuilder.append(FROM_PREFIX)
      queryBuilder.append(first.get.screenName)
      //ids mapped to profiles
      val byId = new mutable.HashMap[Long, T]
      byId.put(first.get.id, first.get)
      while (iterator.hasNext) {
        val profile = iterator.next
        if (!profile.isProtected) {
          byId.put(profile.id, profile)
          //build query until max length reached
          if (queryBuilder.length <= QUERY_LENGTH_THRESHOLD) {
            queryBuilder.append(OR_SEPARATOR)
            queryBuilder.append(FROM_PREFIX)
            queryBuilder.append(profile.screenName)
          } else {
            //execute once query is full
            executeTweetQuery(queryBuilder.mkString)
              .foreach(tweet => {
                dataSetSize += buildResultSet(tweet, byId, result)
                if (dataSetSize >= sampleSize) {
                  logger.warn("Sample size exceeded")
                  return result
                }
              })
            //clear and keep only one screen name for next batch
            queryBuilder.clear
            queryBuilder.append(FROM_PREFIX)
            queryBuilder.append(profile.screenName)
          }
        }
      }
      // execute last query for profile OR batch
      if (queryBuilder.nonEmpty)
        executeTweetQuery(queryBuilder.mkString)
          .foreach(tweet => {
            dataSetSize += buildResultSet(tweet, byId, result)
            if (dataSetSize >= sampleSize) {
              logger.warn("Sample size exceeded")
              return result
            }
          })
    } catch {
      case e: ThrottledException =>
        logger.warn("Throttling in affect")
    }
    return result
  }

  private implicit def convert(map: mutable.HashMap[T, ArrayBuffer[Tweet]]): Iterable[(T, Document)] = {
    map.map(tuple => {
      if (logger.isTraceEnabled)
        logger.trace("user [" + tuple._1.screenName + "] has ["
          + tuple._2.length + "] tweets")
      (tuple._1, TwitterTimeline(tuple._2))
    })
  }

  /**
    * Uses timeline API to get all users posts. Very slow but return reliable results
    *
    * @param profiles profiles whose timelines to get
    * @param numPosts number of posts to get for each user.
    * @return
    */
  private def timelines(profiles: Iterable[TwitterProfile], numPosts: Int): Iterable[(T, Document)] = {
    logger.debug("Using timeline API for [" + profiles.size + "] users")
    var dataSetSize: Long = 0
    val result = new ArrayBuffer[(T, Document)](profiles.size)
    profiles.foreach(profile => {
      if (!profile.isProtected) {
        val req = new HttpGet(uriBuilder("/1.1/statuses/user_timeline.json")
          .setParameter("user_id", String.valueOf(profile.id))
          .setParameter("count", String.valueOf(numPosts)).build())
        val response = executeRequest(req)
        try {
          response.getStatusLine.getStatusCode match {
            case 200 => {
              val timeline = mapper.readValue(response.getEntity.getContent, classOf[Array[Tweet]])
              timeline.foreach(tweet => {
                dataSetSize += tweet.text.length
                if (dataSetSize >= sampleSize) {
                  logger.warn("sample size exceeded")
                  return result
                }
              })
              result.append((profile,
                TwitterTimeline(timeline)))
            }
            case 429 => {
              return result
            }
            case _ => {
              logger.error("Unable to get timeline for [" + profile.screenName + "] -- " +
                response.getStatusLine.getStatusCode + ", " + response.getStatusLine.getReasonPhrase)
            }
          }
        } finally {
          response.close()
        }
      }
    })
    logger.debug("Retrieved [" + result.size + "] of [" + profiles.size + "] users")
    return result
  }

  /**
    * Searches for posts made by given users
    *
    * @param profiles user profiles to search posts for
    * @param numPosts number of posts per profile
    * @return profile and posts made by given profile pairs
    */
  private[twitterfriends] def searchPosts(profiles: Iterable[T], numPosts: Int): Iterable[(T, Document)] = {
    if (profiles.isEmpty)
      return Iterable()
    if (numPosts <= 0) {
      return search(profiles)
    } else {
      return timelines(profiles, numPosts)
    }

  }

  /**
    * Finds a user's friends ids
    *
    * @param user
    * @return friend ids for given user
    */
  @throws[ServiceCallFailedException]
  override private[twitterfriends] def findFriendIds(user: TwitterProfile): Iterable[Long] = {
    if (user == null)
      return Iterable()
    logger.debug("Finding friend ids for [" + user.screenName + "]")
    var cursor = "-1"
    val result = new ArrayBuffer[Long]
    while (!cursor.equals("0")) {
      val req = new HttpGet(uriBuilder("/1.1/friends/ids.json")
        .setParameter("screen_name", user.screenName)
        .setParameter("cursor", cursor).build)
      val response = {
        val response = executeRequest(req)
        try {
          if (response.getStatusLine.getStatusCode != 200)
            throw new ServiceCallFailedException("Unable to get friend ids for " + user + " -- " +
              response.getStatusLine.getReasonPhrase)
          else
            mapper.readValue(response.getEntity.getContent, classOf[UserIdsResponse])
        } finally {
          response.close()
        }
      }
      cursor = response.nextCursor
      if (cursor.equals("0") && result.isEmpty) {
        logger.debug("Found one batch with [" + response.ids + "] friend ids")
        return response.ids
      } else
        result.appendAll(response.ids)
    }
    logger.debug("Found [" + result.size + "] friend ids")
    return result
  }
}