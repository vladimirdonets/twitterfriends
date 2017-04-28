package com.samplecode.twitterfriends.datasource

import com.samplecode.twitterfriends.beans.UserProfile
import com.samplecode.twitterfriends.classifier.doc.Document

/**
  * Base trait for a social media datasource/client.
  * Retrieves posts and profiles.
  * Created by vdonets on 4/8/2017.
  */
private[twitterfriends] trait Datasource {
  /**
    * Searches for posts made by given users
    *
    * @param profiles user profiles to search posts for
    * @param numPosts number of posts per profile
    * @return profile and posts made by given profile pairs
    */
  private[twitterfriends] def searchPosts(profiles: Iterable[T], numPosts: Int): Iterable[(T, Document)]

  /**
    * Finds a user's friends
    *
    * @param user
    * @return friend ids for given user
    */
  private[twitterfriends] def findFriendIds(user: T): Iterable[Any]

  /**
    * Searches for user profiles based on given IDs
    *
    * @param userIds ids of users to get profiles for
    * @return user profiles
    */
  private[twitterfriends] def findUsers(userIds: Iterable[Any]): Iterable[T]


  protected type T <: UserProfile

  /**
    * Finds a user's profile based on screen name
    *
    * @param screenName name of user
    * @return user profile
    */
  private[twitterfriends] def findUser(screenName: String): T

}
