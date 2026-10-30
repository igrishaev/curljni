package org.curljni;

import java.io.InputStream;
import java.util.Map;

public class HTTPResponse {
    protected long status;
    protected Map<String, String> headers;
    protected String bodyString;
    protected InputStream bodyStream;
    protected byte[] bodyBytes;
}
