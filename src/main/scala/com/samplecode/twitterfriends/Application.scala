package com.samplecode.twitterfriends

import java.net.SocketTimeoutException
import com.samplecode.twitterfriends.beans.ServiceResponse
import com.samplecode.twitterfriends.exception.{InaccessibleProfileException, ServiceCallFailedException}
import com.samplecode.twitterfriends.util.logging.LazyLogging
import org.apache.http.conn.ConnectTimeoutException
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.ImportResource
import org.springframework.http.{HttpStatus, ResponseEntity}
import org.springframework.web.bind.annotation._


/**
  * Created by vdonets on 4/8/2017.
  */
@EnableAutoConfiguration
@ImportResource(value = Array("classpath*:applicationContext.xml"))
@RestController
class Application(private val service: Service) extends LazyLogging {

  @RequestMapping(value = Array("/friends/{screenName}"),
    method = Array(RequestMethod.GET),
    produces = Array("application/json"))
  def relevantFriends(@PathVariable screenName: String,
                      @RequestParam(required = false) numPosts: Integer): ResponseEntity[AnyRef] = {
    logger.info("Getting relevant friends for [" + screenName +
      "] with [" + numPosts + "] posts")
    try {
      val sortedFriends = service.relevantFriends(screenName,
        if (numPosts != null)
          numPosts.intValue()
        else -1)
      return new ResponseEntity[AnyRef](ServiceResponse(sortedFriends, null), HttpStatus.OK)
    } catch {
      case e: SocketTimeoutException => {
        e.printStackTrace()
        return new ResponseEntity[AnyRef](ServiceResponse(null, "Twitter API response timeout")
          , HttpStatus.TOO_MANY_REQUESTS)
      }
      case e: InaccessibleProfileException => {
        e.printStackTrace
        return new ResponseEntity[AnyRef](ServiceResponse(null, e.getMessage)
          , HttpStatus.NO_CONTENT)
      }
      case e: ServiceCallFailedException => {
        e.printStackTrace
        return new ResponseEntity[AnyRef](ServiceResponse(null, e.getMessage)
          , HttpStatus.INTERNAL_SERVER_ERROR)
      }
      case e: ConnectTimeoutException => {
        return new ResponseEntity[AnyRef](ServiceResponse(null, "Connection timed out")
          , HttpStatus.INTERNAL_SERVER_ERROR)
      }
      case t: Throwable => {
        t.printStackTrace
        return new ResponseEntity[AnyRef](ServiceResponse(null, "Twitter may be throttling request or timing out")
          , HttpStatus.I_AM_A_TEAPOT)
      }
    }

  }
}

object Application {

  def main(args: Array[String]): Unit = {
    SpringApplication.run(classOf[Application], args: _*);

  }
}