package com.github.polomarcus.utils

import java.util.concurrent.Executors

import com.github.polomarcus.html.{Getter, Parser}
import com.github.polomarcus.html.Getter.browser
import com.typesafe.scalalogging.Logger

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

object FutureService {
  val logger = Logger(this.getClass)
  implicit val ec = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(8)) //http://stackoverflow.com/questions/15285284/how-to-configure-a-fine-tuned-thread-pool-for-futures

  def waitFuture[T](listFuture: Seq[Future[List[T]]]) = {
    val allFutures = Future.sequence(listFuture).map(_.flatten.toList)
    logger.info("waitFuture")

    allFutures.onComplete {
      case Success(r) => logger.info("SUCCESS: AllFutures complete")
      case Failure(e) => logger.info("FAILURE: AllFutures complete")
    }

    logger.info("waitFuture")
    val result = Await.result(allFutures, Duration(20, "minutes"))
    logger.info("waitFuture: done")
    result
  }


  // Future.sequence(documentList).map(_.flatten).map { x => x.flatten}
  // @param parallelizeFactor simulataneous queries (skyrock with 4 --> maybe 20 minutes, 16 --> 7 MIN but lost 6K songs )
  def parallelizeQueries[T](start: Long,
                               end: Long,
                               apiCall: (Long, Long, String) => List[T],
                               parallelizeFactor : Int = 4) = {
    val diff = (end - start) / parallelizeFactor

    val futureList = (0 to (parallelizeFactor - 1) by 1).map { step =>
      logger.debug("parallelizeQueries step:" + step)
      Future {
        apiCall(start + (diff * step), start + (diff * (step + 1)), "future " + step.toString)
      }
    }.toList

    waitFuture[T](futureList)
  }
}