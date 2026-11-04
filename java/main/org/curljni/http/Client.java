package org.curljni.http;

import org.curljni.*;

import java.io.Closeable;

public class Client implements Closeable {

    private final CurlEasy c;
    private boolean isClosed;

    private Client(CurlEasy c) {
        this.c = c;
        this.isClosed = false;
    }

    public static Client make() {
        return new Client(CurlEasy.make());
    }

    private void checkClosed() {
        if (isClosed) {
            throw new RuntimeException("This HTTP Client has already been closed");
        }
    }

    public static Response requestStatic(Request request) {
        try (Client c = make()) {
            return c.request(request);
        }
    }

    public Response request(Request request) {
        checkClosed();
        c.resetOptions();

        c.setUrl(request.url);

        final String method = request.method;
        if (method.equals("GET")) {
            c.setHTTPGet(true);
        } else if (method.equals("POST")) {
            c.setHTTPPost(true);
        } else {
            c.setCustomRequest(method);
        }

        final boolean isAccum = request.asBytes || request.asString;

        try (Headers headers = Headers.maybeCreate(request.headers);
             FILE writeFile = FILE.maybeOpen(request.writeFile, "wb");
             FILE readFile = FILE.maybeOpen(request.readFile, "rb");
             ReadStream readStream = ReadStream.maybeCreate(request.readStream);
             WriteStream writeStream = WriteStream.maybeCreate(request.writeStream);
             WriteCallback writeCallback = WriteCallback.maybeCreate(request.writeHandler);
             Accumulator accum = Accumulator.maybeCreate(isAccum, request.accumSize)
        ) {
            // headers
            if (headers != null) {
                c.setHeaders(headers);
            }

            // input
            // TODO: read callback
            if (readFile != null) {
                c.setReadFile(readFile);
            } else if (readStream != null) {
                c.setReadStream(readStream);
            }

            // output
            if (isAccum) {
                c.setAccumulator(accum);
            } else if (writeFile != null) {
                c.setWriteFile(writeFile);
            } else if (writeStream != null) {
                c.setWriteStream(writeStream);
            } else if (writeCallback != null) {
                c.setWriteCallback(writeCallback);
            }

            c.perform();

            // response
            Response response = new Response();
            response.status = c.getResponseCode();
            response.headers = c.getHeaders();

            if (isAccum) {
                if (request.asBytes) {
                    response.bodyBytes = accum.getBytes();
                } else if (request.asString) {
                    response.bodyString = accum.getString();
                }
            } else if (writeFile != null) {
                response.bodyFile = writeFile.asFile();
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

    public static void main(String... args) {
        Request req = new Request();
        req.method = "GET";
        req.url = "https://habr.com";
        System.out.println(requestStatic(req));
    }
}
