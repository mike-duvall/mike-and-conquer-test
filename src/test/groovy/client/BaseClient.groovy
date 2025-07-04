package client

import groovy.json.JsonSlurper
import groovy.json.JsonBuilder
import org.apache.hc.client5.http.classic.methods.HttpGet
import org.apache.hc.client5.http.classic.methods.HttpPost
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient
import org.apache.hc.client5.http.impl.classic.HttpClients
import org.apache.hc.client5.http.config.RequestConfig
import org.apache.hc.core5.http.io.entity.StringEntity
import org.apache.hc.core5.http.io.entity.EntityUtils
import org.apache.hc.core5.http.ContentType
import org.apache.hc.core5.util.Timeout
import java.net.URI
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class BaseClient {

    CloseableHttpClient httpClient
    String baseUrl
    JsonSlurper jsonSlurper = new JsonSlurper()

    BaseClient() {
        // Configure with timeouts
        RequestConfig config = RequestConfig.custom()
            .setConnectionRequestTimeout(Timeout.ofSeconds(5))
            .setResponseTimeout(Timeout.ofSeconds(5))
            .build()

        httpClient = HttpClients.custom()
            .setDefaultRequestConfig(config)
            .build()
    }

    def doGetRestCall(String path) {
        return doGetRestCall(path, null)
    }

    def doGetRestCall(String path, Map<String, Object> queryParams) {
        String fullUrl = buildUrl(path, queryParams)
        HttpGet httpGet = new HttpGet(fullUrl)
        httpGet.setHeader("Content-Type", "application/json")

        def response = httpClient.execute(httpGet) { httpResponse ->
            int statusCode = httpResponse.getCode()
            String responseBody = EntityUtils.toString(httpResponse.getEntity())

            if (statusCode != 200) {
                throw new RuntimeException("HTTP Error ${statusCode}: ${responseBody}")
            }

            return [
                status: statusCode,
                responseData: responseBody ? jsonSlurper.parseText(responseBody) : null
            ]
        }

        return response
    }

    def doPostRestCall(String path, Object body) {
        String fullUrl = baseUrl + path
        HttpPost httpPost = new HttpPost(fullUrl)
        httpPost.setHeader("Content-Type", "application/json")

        if (body != null) {
            String jsonBody = new JsonBuilder(body).toString()
            httpPost.setEntity(new StringEntity(jsonBody, ContentType.APPLICATION_JSON))
        }

        def response = httpClient.execute(httpPost) { httpResponse ->
            int statusCode = httpResponse.getCode()
            String responseBody = EntityUtils.toString(httpResponse.getEntity())

            if (statusCode != 200) {
                throw new RuntimeException("HTTP Error ${statusCode}: ${responseBody}")
            }

            return [
                status: statusCode,
                responseData: responseBody ? jsonSlurper.parseText(responseBody) : null
            ]
        }

        return response
    }

    private String buildUrl(String path, Map<String, Object> queryParams) {
        String fullUrl = baseUrl + path

        if (queryParams && !queryParams.isEmpty()) {
            List<String> params = []
            queryParams.each { key, value ->
                String encodedKey = URLEncoder.encode(key.toString(), StandardCharsets.UTF_8)
                String encodedValue = URLEncoder.encode(value.toString(), StandardCharsets.UTF_8)
                params.add("${encodedKey}=${encodedValue}")
            }
            fullUrl += "?" + params.join("&")
        }

        return fullUrl
    }

    void close() {
        if (httpClient != null) {
            httpClient.close()
        }
    }
}
