package org.curljni.http;

import org.curljni.IWriteHandler;
import org.curljni.WriteCallback;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

public class Request {
    protected String method;
    protected String url;
    protected Map<String, String> headers;
    protected String writeFile;
    protected OutputStream writeStream;
    protected IWriteHandler writeHandler;
    protected boolean asBytes;
    protected boolean asString;
    protected int accumSize;
    protected String readFile;
    protected InputStream readStream;
}
