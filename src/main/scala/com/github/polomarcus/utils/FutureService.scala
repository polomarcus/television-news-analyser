package com.github.polomarcus.utils

import java.util.concurrent.Executors

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

  def waitFutureTF1[T](listFuture: Seq[Future[Option[T]]]) = {
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
}