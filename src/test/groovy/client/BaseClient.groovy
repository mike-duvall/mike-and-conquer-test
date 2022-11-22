package client

import groovyx.net.http.HttpResponseException

class BaseClient {

    def doGetRestCall(String path) {

        def resp
        try {
            resp = restClient.get(
                    path: path,
                    requestContentType: 'application/json')
            assert resp.status == 200
        }
        catch(HttpResponseException e) {
            throw e
        }

        return resp
    }

    def doGetRestCall(String path, Object query) {

        def resp
        try {
            resp = restClient.get(
                    path: path,
                    query: query,
                    requestContentType: 'application/json')
            assert resp.status == 200
        }
        catch(HttpResponseException e) {
            throw e
        }

        return resp
    }



    def doPostRestCall(String path, Object body) {
        def resp
        try {
            resp = restClient.post(
                    path: path,
                    body: body,
                    requestContentType: 'application/json')


            assert resp.status == 200
        }
        catch(HttpResponseException e) {
            throw e
        }

        return resp

    }

}
