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

    native private static int _get_CURL_ERROR_SIZE();

    native private static long _set_option_long(long curl, int option, long value);
    public CurlEasy setOptionLong(int option, long value) {
        return checkClosed().checkCode(_set_option_long(ptr, option, value));
    }

    native private static long _set_option_string(long curl, int option, String value);
    public CurlEasy setOptionString(int option, String value) {
        return checkClosed().checkCode(_set_option_string(ptr, option, value));
    }

    public CurlEasy setUrl(final String url) {
        return setOptionString(CURLOPT.URL, url);
    }

    native private static long _set_method(final long curlPtr, final long method);
    public CurlEasy setMethod(final long method) {
        return checkClosed().checkCode(_set_method(ptr, method));
    }

    private CurlEasy checkTread() {
        if (threadId == Thread.currentThread().getId()) {
            return this;
        } else {
            throw Err.error("you cannot call this method from another thread");
        }
    }

    native private static long _perform(final long curlPtr);
    public CurlEasy perform() {
        return checkClosed().checkTread().checkCode(_perform(ptr));
    }

    private CurlEasy setErrorBuffer() {
        return setOptionLong(CURLOPT.ERRORBUFFER, arena.ptr());
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

    native private static long _set_write_file(long curl, long file);
    public CurlEasy setWriteFile(IResource file) {
        Objects.requireNonNull(file, "the file object cannot be null");
        return checkClosed().checkCode(_set_write_file(ptr, file.ptr()));
    }

    native private static long _set_write_stream(long curl, long stream);
    public CurlEasy setWriteStream(IResource stream) {
        Objects.requireNonNull(stream, "the stream object cannot be null");
        return checkClosed().checkCode(_set_write_stream(ptr, stream.ptr()));
    }

    native private static long _set_write_callback(long curl, long callback);
    public CurlEasy setWriteCallback(IResource callback) {
        Objects.requireNonNull(callback, "write callback cannot be null");
        return checkClosed().checkCode(_set_write_callback(ptr, callback.ptr()));
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
    native private static long _set_tcp_fast_open(long curl, long value);
    public CurlEasy setTCPFastOpen(boolean value) {
        return checkClosed().checkCode(_set_tcp_fast_open(ptr, value ? 1L : 0L));
    }

    // CURLOPT_TCP_KEEPALIVE
    native private static long _set_tcp_keep_alive(long curl, long value);
    public CurlEasy setTPCKeepAlive(boolean value) {
        return checkClosed().checkCode(_set_tcp_keep_alive(ptr, value ? 1L : 0L));
    }

    // CURLOPT_XOAUTH2_BEARER
    native private static long _set_xoauth2_bearer(long curl, String value);
    public CurlEasy setXOauth2Bearer(String value) {
        return checkClosed().checkCode(_set_xoauth2_bearer(ptr, value));
    }

    // CURLOPT_CUSTOMREQUEST
    native private static long _set_custom_request(long curl, String method);
    public CurlEasy setCustomRequest(String method) {
        return checkClosed().checkCode(_set_custom_request(ptr, method));
    }
    
    native private static long _set_accumulator(long curl, long acc);
    public CurlEasy setAccumulator(IResource acc) {
        Objects.requireNonNull(acc, "the accumulator cannot be null");
        return checkClosed().checkCode(_set_accumulator(ptr, acc.ptr()));
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

    native private static long _get_num_connects(long curl, long bb);
    public long getNumConnects() {
        checkClosed().checkCode(_get_num_connects(ptr, arena.ptr()));
        return curlLong();
    }

    native private static long _get_redirect_count(long curl, long bb);
    public long getRedirectCount() {
        checkClosed().checkCode(_get_redirect_count(ptr, arena.ptr()));
        return curlLong();
    }

    native private static long _get_local_port(long curl, long bb);
    public long getLocalPort() {
        checkClosed().checkCode(_get_local_port(ptr, arena.ptr()));
        return curlLong();
    }

    native private static long _get_proxy_error(long curl, long bb);
    public long getProxyError() {
        checkClosed().checkCode(_get_proxy_error(ptr, arena.ptr()));
        return curlLong();
    }

    native private static long _get_primary_port(long curl, long bb);
    public long getPrimaryPort() {
        checkClosed().checkCode(_get_primary_port(ptr, arena.ptr()));
        return curlLong();
    }

    native private static long _get_os_errno(long curl, long bb);
    public long getOsErrno() {
        checkClosed().checkCode(_get_os_errno(ptr, arena.ptr()));
        return curlLong();
    }
    
    native private static String _get_primary_ip(long curl, long bb);
    public String getPrimaryIP() {
        final String result = _get_primary_ip(ptr, arena.ptr());
        checkCode(curlLong());
        return result;
    }

    native private static String _get_effective_url(long curl, long bb);
    public String getEffectiveURL() {
        final String result = _get_effective_url(ptr, arena.ptr());
        checkCode(curlLong());
        return result;
    }

    native private static String _get_local_ip(long curl, long bb);
    public String getLocalIP() {
        final String result = _get_local_ip(ptr, arena.ptr());
        checkCode(curlLong());
        return result;
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
