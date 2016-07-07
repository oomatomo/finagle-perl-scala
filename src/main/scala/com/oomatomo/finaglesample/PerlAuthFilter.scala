package com.oomatomo.finaglesample

import com.twitter.finagle.http.Request
import com.twitter.finagle.{Service, SimpleFilter, http}
import com.twitter.util.Future

// Perl権限チェック用のfilter
// authサーバにリクエスト後に、認証がおkだった場合にヘッダーを設置してapiサーバに送る
class PerlAuthFilter[Request, Response](authService: Service[http.Request, http.Response])
  extends SimpleFilter[http.Request, http.Response] {

  override def apply(request: http.Request, service: Service[http.Request, http.Response]): Future[http.Response] = {

    val authRequest: http.Request = Request(http.Method.Post, Request.queryString("/account/json"))

    // セッションIDを認証のリクエストに付与する
    if (request.cookies.get("plack_session").isDefined) {
      authRequest.addCookie(request.cookies.get("plack_session").get)
    }

    // hostを付けないと starmanがリクエストを受け付けない
    authRequest.host_=("127.0.0.1")

    authService(authRequest).flatMap { res =>
      // レスポンスからidを抽出する
      // レスポンスの内容： "{ name: 'hoge', id: 1 }"
      val resString = res.encodeString()
      // debug用
      println("authRequest: " + resString)
      // レスポンスの内容から、idの中身だけ取り出す正規表現
      val r = """id":([0-9]+)""".r
      (for {
        regex <- r.findFirstMatchIn(resString)
        if regex.groupCount == 1
      } yield {
        val apiRequest = request
        // Scala側に渡す際にアカウントの情報をヘッダーに付与する
        apiRequest.headerMap.set("X-Hoge-Id", regex.group(1))
        // debug用
        println("PerlAuthFilter :" + apiRequest.headerMap.toString)
        service(apiRequest)
      }).getOrElse(Future.value(http.Response(request.version, http.Status.Unauthorized)))
    }
  }
}
