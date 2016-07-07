package com.oomatomo.finaglesample

import com.twitter.finagle._
import com.twitter.finagle.http.path._
import com.twitter.finagle.http.service.RoutingService
import com.twitter.finagle.http.{Request, Response}
import com.twitter.util.{Await, Future}

object RpcServer extends App {

    val scalaApiService: Service[Request, Response] = Http.newService("scala-api:9000")

    val perlApiService: Service[Request, Response] = Http.newService("perl-api:5000")

    val perlAuthFilter = new PerlAuthFilter(perlApiService)
    val hogeService = perlAuthFilter andThen scalaApiService

    // for other Request
    val blankService = new Service[Request, Response] {
        def apply(request: http.Request): Future[http.Response] = {
            val rep = http.Response(request.version, http.Status.BadRequest)
            rep.setContentString("not match url by finagle")
            Future.value(rep)
        }
    }

    // いい感じの正規表現がないらしい
    val router = RoutingService.byPathObject[Request] {
        case Root / "api" / "test" / _ / _ => hogeService // api/test/hoge/1
        case Root / "api" / "test" / _     => hogeService // api/test/hoge
        case _                             => blankService
    }

    // rpcサーバの起動
    val server = Http.serve(":9999", router)
    Await.ready(server)
}