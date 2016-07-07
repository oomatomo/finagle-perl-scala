package com.oomatomo.finaglesample

import com.twitter.finagle.http.path.{/, Root}
import com.twitter.finagle.http.service.RoutingService
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.{Http, Service, http}
import com.twitter.util.{Await, Future}

object ApiServer extends App {

  // X-Hoge-Id が空ではなかったら、okを返す
  val apiService = new Service[Request, Response] {
    def apply(request: http.Request): Future[http.Response] = {
      val rep = http.Response(request.version, http.Status.Ok)
      // debug用
      println("X-Hoge-Id:" + request.headerMap.get("X-Hoge-Id").toString)
      if (request.headerMap.get("X-Hoge-Id").isDefined) {
        rep.setContentString("ok")
      } else {
        rep.setContentString("ng")
      }
      Future.value(rep)
    }
  }

  val router = RoutingService.byPathObject[Request] {
    case _                             => apiService
  }

  // rpcサーバの起動
  val server = Http.serve(":9000", router)
  Await.ready(server)
}