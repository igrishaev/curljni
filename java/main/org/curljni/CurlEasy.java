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

    native private static long _set_url(final long curlPtr, final String url);
    public CurlEasy setUrl(final String url) {
        return checkClosed().checkCode(_set_url(ptr, url));
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

    native private static long _set_error_buffer(long curl, long bb);
    private CurlEasy setErrorBuffer() {
        return checkClosed().checkCode(_set_error_buffer(ptr, arena.ptr()));
    }

    native private static long _curl_easy_reset(long curlPtr);
    public CurlEasy resetOptions() {
        return checkClosed()
                .checkCode(_curl_easy_reset(ptr))
                .setErrorBuffer();
    }

    native private static String _curl_easy_strerror(long curlCode);

    native private static long _curl_set_headers(long curl, long headers);
    public CurlEasy setHeaders(IResource headers) {
        final long hhPtr = (headers == null) ? Native.NULL : headers.ptr();
        return checkClosed().checkCode(_curl_set_headers(ptr, hhPtr));
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

    native private static long _set_proxy(long curl, String proxy);
    public CurlEasy setProxy(String proxy) {
        return checkClosed().checkCode(_set_proxy(ptr, proxy));
    }

    native private static long _set_user_agent(long curl, String userAgent);
    public CurlEasy setUserAgent(String userAgent) {
        return checkClosed().checkCode(_set_user_agent(ptr, userAgent));
    }

    // CURLOPT_SERVER_RESPONSE_TIMEOUT_MS
    native private static long _set_server_response_timeout_ms(long curl, long ms);
    public CurlEasy setServerResponseTimeoutMs(long ms) {
        return checkClosed().checkCode(_set_server_response_timeout_ms(ptr, ms));
    }
    public CurlEasy setServerResponseTimeout(Duration duration) {
        return setServerResponseTimeoutMs(duration.toMillis());
    }

    // CURLOPT_TIMEOUT_MS
    native private static long _set_timeout_ms(long curl, long ms);
    public CurlEasy setTimeoutMs(long ms) {
        return checkClosed().checkCode(_set_timeout_ms(ptr, ms));
    }
    public CurlEasy setTimeoutMs(Duration duration) {
        return setTimeoutMs(duration.toMillis());
    }

    // CURLOPT_CONNECTTIMEOUT_MS
    native private static long _set_connect_timeout_ms(long curl, long ms);
    public CurlEasy setConnectTimeoutMs(long ms) {
        return checkClosed().checkCode(_set_connect_timeout_ms(ptr, ms));
    }
    public CurlEasy setConnectTimeoutMs(Duration duration) {
        return setConnectTimeoutMs(duration.toMillis());
    }

    // CURLOPT_MAXCONNECTS
    native private static long _set_max_connects(long curl, long amount);
    public CurlEasy setMaxConnects(long amount) {
        return checkClosed().checkCode(_set_max_connects(ptr, amount));
    }

    // CURLOPT_MAXREDIRS
    native private static long _set_max_redirs(long curl, long amount);
    public CurlEasy setMaxRedirects(long amount) {
        return checkClosed().checkCode(_set_max_redirs(ptr, amount));
    }

    // CURLOPT_PASSWORD
    native private static long _set_password(long curl, String password);
    public CurlEasy setPassword(String password) {
        return checkClosed().checkCode(_set_password(ptr, password));
    }

    // CURLOPT_USERNAME
    native private static long _set_username(long curl, String username);
    public CurlEasy setUsername(String username) {
        return checkClosed().checkCode(_set_username(ptr, username));
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

    native private static long _set_verbose(long curl, long value);
    public CurlEasy setVerbose(boolean flag) {
        long value = flag ? 1 : 0;
        return checkClosed().checkCode(_set_verbose(ptr, value));
    }

    native private static long _set_read_file(long curl, long file);
    public CurlEasy setReadFile(IResource file) {
        Objects.requireNonNull(file, "read file cannot be null");
        return checkClosed().checkCode(_set_read_file(ptr, file.ptr()));
    }

    native private static long _set_follow_location(long curl, long value);
    public CurlEasy setFollowLocation(long value) {
        return checkClosed().checkCode(_set_follow_location(ptr, value));
    }

    native private static long _set_read_stream(long curl, long value);
    public CurlEasy setReadStream(ReadStream stream) {
        Objects.requireNonNull(stream, "read stream cannot be null");
        return checkClosed().checkCode(_set_read_stream(ptr, stream.ptr()));
    }

    private long curlLong() {
        return arena.orderJNI().getLong(0);
    }

    native private static long _get_response_code(long curl, long bb);
    public long getResponseCode() {
        checkClosed().checkCode(_get_response_code(ptr, arena.ptr()));
        return curlLong();
    }

    native private static long _get_connect_time_t(long curl, long bb);
    public long getConnectTimeMs() {
        checkClosed().checkCode(_get_connect_time_t(ptr, arena.ptr()));
        return curlLong();
    }

    native private static long _get_content_length_download_t(long curl, long bb);
    public long getContentLengthDownload() {
        checkClosed().checkCode(_get_content_length_download_t(ptr, arena.ptr()));
        return curlLong();
    }

    native private static long _get_redirect_time_t(long curl, long bb);
    public long getRedirectTime() {
        checkClosed().checkCode(_get_connect_time_t(ptr, arena.ptr()));
        return curlLong();
    }

    native private static long _get_retry_after(long curl, long bb);
    public long getRetryCount() {
        checkClosed().checkCode(_get_retry_after(ptr, arena.ptr()));
        return curlLong();
    }

    native private static long _get_speed_download_t(long curl, long bb);
    public long getSpeedDownload() {
        checkClosed().checkCode(_get_speed_download_t(ptr, arena.ptr()));
        return curlLong();
    }

    native private static long _get_speed_upload_t(long curl, long bb);
    public long getSpeedUpload() {
        checkClosed().checkCode(_get_speed_upload_t(ptr, arena.ptr()));
        return curlLong();
    }

    native private static long _get_used_proxy(long curl, long bb);
    public boolean getUsedProxy() {
        checkClosed().checkCode(_get_used_proxy(ptr, arena.ptr()));
        return curlLong() != 0;
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
