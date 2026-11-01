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

        final boolean isAccum = request.asBytes || request.asString;

        try (FILE writeFile = FILE.maybeOpen(request.writeFile, "wb");
             Headers headers = Headers.maybeCreate(request.headers);
             Accumulator accum = Accumulator.create(123))
        {
            // headers
            if (headers != null) {
                c.setHeaders(headers);
            }

            // input
            // read file
            // read stream
            // read callback?

            // output
            if (writeFile != null) {
                c.setWriteFile(writeFile);
            } else if (false) {
                c.setWriteStream(null);
            } else if (false) {
                c.setWriteCallback(null);
            } else if (isAccum) {
                c.setAccumulator(accum);
            }

            c.perform();

            // response
            HTTPResponse response = new HTTPResponse();
            response.status = c.getResponseCode();
            response.headers = c.getHeaders();

            // body stream
            // body callback
            // body file

            if (isAccum) {
                if (request.asBytes) {
                    response.bodyBytes = accum.getBytes();
                } else if (request.asString) {
                    response.bodyString = accum.getString();
                }
            }

            return response;
        }


    }

    @Override
    public void close() {
        if (!isClosed) {
            c.close();
            isClosed = true;
        }
    }
}
