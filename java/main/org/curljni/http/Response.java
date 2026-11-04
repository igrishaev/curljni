package org.curljni.http;

import java.io.File;
import java.io.InputStream;
import java.util.Map;

public class Response {
    protected long status;
    protected Map<String, String> headers;
    protected String bodyString;
    protected InputStream bodyStream;
    protected byte[] bodyBytes;
    protected File bodyFile;
}
