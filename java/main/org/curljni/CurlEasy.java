package org.curljni;

import java.nio.charset.Charset;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CurlEasy implements IResource {

    static {
        Native.loadLib();
        ERROR_SIZE = _get_CURL_ERROR_SIZE(); // TODO?
    }

    private final long threadId;
    private final long ptr;
    private final Arena arena;
    private boolean isClosed;
    private final static int ERROR_SIZE;

    private CurlEasy(long threadId, long ptr, Arena arena, boolean isClosed) {
        this.threadId = threadId;
        this.ptr = ptr;
        this.arena = arena;
        this.isClosed = isClosed;
    }

    native private static long _curl_easy_init();
    public static CurlEasy make() {
        final long threadId = Thread.currentThread().getId();
        final long ptr = _curl_easy_init();
        if (ptr == Native.NULL) {
            throw Err.error("failed to initialize cURL");
        }
        Arena arena = Arena.create(Const.ARENA_SIZE);
        return new CurlEasy(threadId, ptr, arena, false).setErrorBuffer();
    }

    @Override
    public long ptr() {
        return ptr;
    }

    private CurlEasy checkClosed() {
        if (isClosed) {
            throw new RuntimeException("This CurlEasy resource has been closed");
        } else {
            return this;
        }
    }

    private CurlEasy checkCode(final long code) {
        if (code == 0) {
            return this;
        } else {
            String errorDescription = _curl_easy_strerror(code);
            arena.rewind();
            String errorExplanation = arena.readCString();
            // TODO: check errno?
            throw new RuntimeException(
                    String.format("curl code: %d, description: %s, explanation: %s",
                            code, errorDescription, errorExplanation));
        }
    }

    private CurlEasy checkTread() {
        if (threadId == Thread.currentThread().getId()) {
            return this;
        } else {
            throw Err.error("you cannot call this method from another thread");
        }
    }

    native private static int _get_CURL_ERROR_SIZE();

    native private static long _set_option_long(long curl, int option, long value);
    public CurlEasy setOptionLong(int option, long value) {
        return checkClosed().checkCode(_set_option_long(ptr, option, value));
    }

    native private static long _set_option_long2(long curl, int opt1, long val1, int opt2, long val2);
    public CurlEasy setOptionLongLong(int opt1, long val1, int opt2, long val2) {
        return checkClosed().checkCode(_set_option_long2(ptr, opt1, val1, opt2, val2));
    }

    native private static long _set_option_string(long curl, int option, String value);
    public CurlEasy setOptionString(int option, String value) {
        return checkClosed().checkCode(_set_option_string(ptr, option, value));
    }

    public CurlEasy setUrl(final String url) {
        return setOptionString(CURLOPT.URL, url);
    }

    // TODO
    native private static long _set_method(final long curlPtr, final long method);
    public CurlEasy setMethod(final long method) {
        return checkClosed().checkCode(_set_method(ptr, method));
    }

    native private static long _perform(final long curlPtr);
    public CurlEasy perform() {
        return checkClosed().checkTread().checkCode(_perform(ptr));
    }

    private CurlEasy setErrorBuffer() {
        return setOptionLong(CURLOPT.ERRORBUFFER, arena.ptr);
    }

    native private static long _curl_easy_reset(long curlPtr);
    public CurlEasy resetOptions() {
        return checkClosed()
                .checkCode(_curl_easy_reset(ptr))
                .setErrorBuffer();
    }

    native private static String _curl_easy_strerror(long curlCode);

    public CurlEasy setHeaders(IResource headers) {
        final long hhPtr = (headers == null) ? Native.NULL : headers.ptr();
        return setOptionLong(CURLOPT.HTTPHEADER, hhPtr);
    }

    public CurlEasy setWriteFile(IResource file) {
        Objects.requireNonNull(file, "the file object cannot be null");
        return setWriteParams(file.ptr(), FN.fwrite);
    }

    private CurlEasy setWriteParams(long writeData, long writeFunc) {
        return setOptionLongLong(CURLOPT.WRITEDATA, writeData, CURLOPT.WRITEFUNCTION, writeFunc);
    }

    public CurlEasy setWriteStream(IResource stream) {
        Objects.requireNonNull(stream, "the stream object cannot be null");
        return setWriteParams(stream.ptr(), FN.write_callback_stream);
    }

    public CurlEasy setWriteCallback(IResource callback) {
        Objects.requireNonNull(callback, "write callback cannot be null");
        return setWriteParams(callback.ptr(), FN.write_callback_handler);
    }

    public CurlEasy setProxy(String proxy) {
        return setOptionString(CURLOPT.PROXY, proxy);
    }

    public CurlEasy setUserAgent(String userAgent) {
        return setOptionString(CURLOPT.USERAGENT, userAgent);
    }

    // SERVER_RESPONSE_TIMEOUT_MS
    public CurlEasy setServerResponseTimeoutMs(long ms) {
        return setOptionLong(CURLOPT.SERVER_RESPONSE_TIMEOUT_MS, ms);
    }
    public CurlEasy setServerResponseTimeout(Duration duration) {
        return setServerResponseTimeoutMs(duration.toMillis());
    }

    // TIMEOUT_MS
    public CurlEasy setTimeoutMs(long ms) {
        return setOptionLong(CURLOPT.TIMEOUT_MS, ms);
    }
    public CurlEasy setTimeoutMs(Duration duration) {
        return setTimeoutMs(duration.toMillis());
    }

    // CONNECTTIMEOUT_MS
    public CurlEasy setConnectTimeoutMs(long ms) {
        return setOptionLong(CURLOPT.CONNECTTIMEOUT_MS, ms);
    }
    public CurlEasy setConnectTimeoutMs(Duration duration) {
        return setConnectTimeoutMs(duration.toMillis());
    }

    // MAXCONNECTS
    public CurlEasy setMaxConnects(long amount) {
        return setOptionLong(CURLOPT.MAXCONNECTS, amount);
    }

    // MAXREDIRS
    public CurlEasy setMaxRedirects(long amount) {
        return setOptionLong(CURLOPT.MAXREDIRS, amount);
    }

    // PASSWORD
    public CurlEasy setPassword(String password) {
        return setOptionString(CURLOPT.PASSWORD, password);
    }

    // USERNAME
    public CurlEasy setUsername(String username) {
        return setOptionString(CURLOPT.USERNAME, username);
    }

    // BUFFERSIZE
    public CurlEasy setBufferSize(long size) {
        return setOptionLong(CURLOPT.BUFFERSIZE, size);
    }

    // NOBODY
    public CurlEasy setNoBody(boolean value) {
        return setOptionLong(CURLOPT.NOBODY, value ? 1L : 0L);
    }

    // FAILONERROR
    public CurlEasy setFailOnError(boolean value) {
        return setOptionLong(CURLOPT.FAILONERROR, value ? 1L : 0L);
    }

    // CURLOPT_TCP_FASTOPEN
    public CurlEasy setTCPFastOpen(boolean value) {
        return setOptionLong(CURLOPT.TCP_FASTOPEN, value ? 1 : 0);
    }

    // CURLOPT_TCP_KEEPALIVE
    public CurlEasy setTPCKeepAlive(boolean value) {
        return setOptionLong(CURLOPT.TCP_KEEPALIVE, value ? 1 : 0);
    }

    // CURLOPT_XOAUTH2_BEARER
    public CurlEasy setXOauth2Bearer(String value) {
        return setOptionString(CURLOPT.XOAUTH2_BEARER, value);
    }

    // CURLOPT_CUSTOMREQUEST
    public CurlEasy setCustomRequest(String method) {
        return setOptionString(CURLOPT.CUSTOMREQUEST, method);
    }

    public CurlEasy setAccumulator(IResource acc) {
        Objects.requireNonNull(acc, "the accumulator cannot be null");
        return setWriteParams(acc.ptr(), FN.accum_write_callback);
    }

    // TODO: check for null
    native private static long _set_post_fields_bytes(long curl, byte[] buf, int len);
    public CurlEasy setPostData(byte[] buf) {
        return checkClosed().checkCode(_set_post_fields_bytes(ptr, buf, buf.length));
    }

    public CurlEasy setPostData(String string, Charset charset) {
        return setPostData(string.getBytes(charset));
    }

    public CurlEasy setPostData(String string, String charset) {
        return setPostData(string, Charset.forName(charset)); // TODO fallback?
    }

    public CurlEasy setVerbose(boolean flag) {
        return setOptionLong(CURLOPT.VERBOSE, flag ? 1 : 0);
    }

    native private static long _set_read_file(long curl, long file);
    public CurlEasy setReadFile(IResource file) {
        Objects.requireNonNull(file, "read file cannot be null");
        return checkClosed().checkCode(_set_read_file(ptr, file.ptr()));
    }

    public CurlEasy setFollowLocation(long value) {
        return setOptionLong(CURLOPT.FOLLOWLOCATION, value);
    }

    native private static long _set_read_stream(long curl, long value);
    public CurlEasy setReadStream(ReadStream stream) {
        Objects.requireNonNull(stream, "read stream cannot be null");
        return checkClosed().checkCode(_set_read_stream(ptr, stream.ptr()));
    }

    private long curlLong() {
        return arena.orderJNI().getLong(0);
    }

    native private static long _get_info_as_long(long curl, int info, long bb);
    public long getInfoLong(int info) {
        checkClosed().checkCode(_get_info_as_long(ptr, info, arena.ptr));
        return arena.orderJNI().getLong(0);
    }

    native private static String _get_info_as_string(long curl, int info, long bb);
    public String getInfoString(int info) {
        final String result = checkClosed()._get_info_as_string(ptr, info, arena.ptr);
        final long code = arena.orderJNI().getLong(0);
        checkCode(code);
        return result;
    }


    public long getResponseCode() {
        return getInfoLong(CURLINFO.RESPONSE_CODE);
    }

    public long getConnectTimeMs() {
        return getInfoLong(CURLINFO.CONNECT_TIME_T);
    }

    public long getContentLengthDownload() {
        return getInfoLong(CURLINFO.CONTENT_LENGTH_DOWNLOAD_T);
    }

    public long getRedirectTime() {
        return getInfoLong(CURLINFO.REDIRECT_TIME_T);
    }

    public long getRetryCount() {
        return getInfoLong(CURLINFO.RETRY_AFTER);
    }

    public long getSpeedDownload() {
        return getInfoLong(CURLINFO.SPEED_DOWNLOAD_T);
    }

    public long getSpeedUpload() {
        return getInfoLong(CURLINFO.SPEED_UPLOAD_T);
    }

    public boolean getUsedProxy() {
        return getInfoLong(CURLINFO.USED_PROXY) != 0;
    }

    public long getNumConnects() {
        return getInfoLong(CURLINFO.NUM_CONNECTS);
    }

    public long getRedirectCount() {
        return getInfoLong(CURLINFO.REDIRECT_COUNT);
    }

    public long getLocalPort() {
        return getInfoLong(CURLINFO.LOCAL_PORT);
    }

    public long getProxyError() {
        return getInfoLong(CURLINFO.PROXY_ERROR);
    }

    public long getPrimaryPort() {
        return getInfoLong(CURLINFO.PRIMARY_PORT);
    }

    public long getOsErrno() {
        return getInfoLong(CURLINFO.OS_ERRNO);
    }

    public String getPrimaryIP() {
        return getInfoString(CURLINFO.PRIMARY_IP);
    }

    public String getEffectiveURL() {
        return getInfoString(CURLINFO.EFFECTIVE_URL);
    }

    public String getLocalIP() {
        return getInfoString(CURLINFO.LOCAL_IP);
    }

    native private static long _curl_easy_nextheader(long curl, long prev);
    native private static String _header_name(long header);
    native private static String _header_value(long header);

    public Map<String, String> getHeaders() {
        checkClosed();
        final Map<String, String> result = new HashMap<>();
        String name, value;
        long headerPtr = _curl_easy_nextheader(ptr, Native.NULL);
        while (headerPtr != Native.NULL) {
            name = _header_name(headerPtr);
            value = _header_value(headerPtr);
            result.put(name, value);
            headerPtr = _curl_easy_nextheader(ptr, headerPtr);
        }
        return result;
    }

    native private static long _curl_easy_cleanup(final long curlPtr);
    @Override
    public void close() {
        if (isClosed) return;
        _curl_easy_cleanup(ptr);
        isClosed = true;
    }
}
