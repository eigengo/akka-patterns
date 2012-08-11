package webapp

import org.springframework.util.FileCopyUtils
import groovy.json.JsonSlurper

class RestClient {
  def url = "http://localhost:9000/"
  def pathVariablePattern = ~/\{([^\}]*)\}/

  enum Method {
    Get, Post, Put, Delete
  }

  private def RestResponse doRest(Method method, String path, params = [:], body = null) {
    def matcher = pathVariablePattern.matcher(path)
    matcher.each {
      def param = it[1]
      def value = params[param]
      if (value == null) throw new Exception("Missing param " + param)
      path = path.replace(it[0], value)
    }

    def result = withRest(uri: url) {
      if (token != null) headers = ["x-token": session["token"]]

      // the server failures still reply with JSON object
      handler.failure = { response, responseBody ->
        def content = FileCopyUtils.copyToString(responseBody)
        def jsonResponseBody = null
        try {
          jsonResponseBody = new JsonSlurper().parse(new StringReader(content))
        } catch (ignored) {
          // this could be OK--the response is not even JSON
        }

        def result = new RestResponse(
                status: response.statusLine.statusCode,
                json: jsonResponseBody,
                text: content
        )
        result
      }
      handler.success = { resp, data ->
        new RestResponse(
                status: 200,
                json: data,
                text: null
        )
      }

      switch (method) {
        case Method.Get:
          get(path: path, body: body, requestContentType: "application/json")
          break
        case Method.Post:
          post(path: path, body: body, requestContentType: "application/json")
          break
        case Method.Put:
          put(path: path, body: body, requestContentType: "application/json")
          break
        case Method.Delete:
          delete(path: path, body: body, requestContentType: "application/json")
          break
      }

    }

    result
  }

  def RestResponse doGet(String path, params = [:], body = null) {
    doRest(Method.Get, path, params, body)
  }

  def RestResponse doPost(String path, params = [:], body = null) {
    doRest(Method.Post, path, params, body)
  }

  def RestResponse doPut(String path, params = [:], body = null) {
    doRest(Method.Put, path, params, body)
  }

  def RestResponse doDelete(String path, params = [:], body = null) {
    doRest(Method.Delete, path, params, body)
  }

  private def nullRemover(Map body) {
    def newBody = [:]
    body.each { k, v ->
      if (v == null) return;
      if (v == "null") return;
      if (v instanceof UUID) v = v.toString()
      if (v instanceof Map) v = nullRemover(v)
      newBody[k] = v
    }

    newBody
  }

  def doPutOrPost(String path, body) {
    if (body instanceof Map) body = nullRemover(body)

    def json

    if (body.id == null) {
      body.id = UUID.randomUUID().toString()
      json = body.encodeAsJSON()
      doPost(path, [:], json)
    } else {
      json = body.encodeAsJSON()
      doPut(path, [:], json)
    }

  }
}