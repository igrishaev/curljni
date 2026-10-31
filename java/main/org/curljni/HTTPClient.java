package org.curljni;

import java.io.Closeable;

public class HTTPClient implements Closeable {

    private final CurlEasy c;
    private boolean isClosed;

    private HTTPClient(CurlEasy c) {
        this.c = c;
        this.isClosed = false;
    }

    public static HTTPClient make() {
        return new HTTPClient(CurlEasy.make());
    }

    private void checkClosed() {
        if (isClosed) {
            throw new RuntimeException("This HTTPClient has already been closed");
        }
    }

    public static HTTPResponse requestStatic(HTTPRequest request) throws Exception {
        try (HTTPClient c = make()) {
            return c.request(request);
        }
    }

    public HTTPResponse request(HTTPRequest request) throws Exception {
        checkClosed();
        c.setUrl(request.url);
        c.setCustomRequest(request.method);

        try (IResource file = FILE.open(request.writeFile, "wb");
             IResource headers = Headers.create(request.headers)) {
            c.setWriteFile(file);
            if (headers != null) {
                c.setHeaders(headers);
            }
            c.perform();
        }

        HTTPResponse response = new HTTPResponse();
        response.status = c.getResponseCode();
        response.headers = c.getHeaders();
        response.bodyString = "123";
        return response;
    }

    @Override
    public void close() {
        if (!isClosed) {
            c.close();
            isClosed = true;
        }
    }
}
