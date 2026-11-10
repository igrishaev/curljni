package org.curljni;

import java.nio.charset.Charset;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CurlEasy implements IPeer {

    static {
        Native.loadLib();
    }

    private final long threadId;
    private final long ptr;
    private final Arena arena;
    private boolean isClosed;
    private final TryLock lock;

    private CurlEasy(long threadId, long ptr, Arena arena, boolean isClosed, TryLock lock) {
        this.threadId = threadId;
        this.ptr = ptr;
        this.arena = arena;
        this.isClosed = isClosed;
        this.lock = lock;
    }

    native private static long _curl_easy_init();
    public static CurlEasy make() {
        final TryLock lock = TryLock.create();
        final long threadId = Thread.currentThread().getId();
        final long ptr = _curl_easy_init();
        if (ptr == Native.NULL) {
            throw Err.error("failed to initialize cURL");
        }
        Arena arena = Arena.create(Const.ARENA_SIZE);
        return new CurlEasy(threadId, ptr, arena, false, lock).setErrorBuffer();
    }

    public CurlEasy setOption(int opt, long value) {
        arena.putInt(opt);
        arena.putLong(value);
        return this;
    }

    @Override
    public long ptr() {
        return ptr;
    }

    @Override
    public boolean isClosed() {
        return isClosed;
    }

    @SuppressWarnings("unused")
    public TryLock lock() {
        return lock.lock();
    }

    private CurlEasy checkClosed() {
        if (isClosed) {
            throw new RuntimeException("This CurlEasy resource has been closed");
        } else {
            return this;
        }
    }

    native private static void _get_errno_and_message(long bb);

    private CurlEasy checkCode(final long code) {
        if (code == CURLE.OK) {
            return this;
        } else {
            String strError = _curl_easy_strerror(code);
            arena.rewind();
            String errBufferStr = arena.getCString();
            _get_errno_and_message(arena.ptr);
            arena.rewind().orderJNI();
            final long errno = arena.getLong();
            final String strerror = arena.getCString();
            throw new CURLException(code, strError, errBufferStr, errno, strerror);
        }
    }

    private CurlEasy checkTread() {
        if (threadId == Thread.currentThread().getId()) {
            return this;
        } else {
            throw Err.error("you cannot call this method from another thread");
        }
    }

    native private static long _set_option_long(long curl, int option, long value);
    public CurlEasy setOptionLong(int option, long value) {
        return checkClosed().checkCode(_set_option_long(ptr, option, value));
    }

    public CurlEasy setOptionBool(int option, boolean value) {
        return setOptionLong(option, value ? 1L : 0L);
    }

    native private static long _set_option_long2(long curl, int opt1, long val1, int opt2, long val2);
    public CurlEasy setOptionLong2(int opt1, long val1, int opt2, long val2) {
        return checkClosed().checkCode(_set_option_long2(ptr, opt1, val1, opt2, val2));
    }

    native private static long _set_option_long3(long curl, int opt1, long val1, int opt2, long val2, int opt3, long val3);
    public CurlEasy setOptionLong3(int opt1, long val1, int opt2, long val2, int opt3, long val3) {
        return checkClosed().checkCode(_set_option_long3(ptr, opt1, val1, opt2, val2, opt3, val3));
    }

    native private static long _set_option_string(long curl, int option, String value);
    public CurlEasy setOptionString(int option, String value) {
        return checkClosed().checkCode(_set_option_string(ptr, option, value));
    }

    native private static long _perform(final long curlPtr);
    public CurlEasy perform() {
        return checkClosed().checkTread().checkCode(_perform(ptr));
    }

    public CurlEasy setUrl(final String url) {
        return setOptionString(CURLOPT.URL, url);
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

    public CurlEasy setWriteFile(IPeer file) {
        Objects.requireNonNull(file, "the file object cannot be null");
        return setWriteParams(file.ptr(), FN.fwrite);
    }

    private CurlEasy setWriteParams(long writeData, long writeFunc) {
        return setOptionLong2(CURLOPT.WRITEDATA, writeData, CURLOPT.WRITEFUNCTION, writeFunc);
    }

    private CurlEasy setReadParams(long readData, long readFunc) {
        return setOptionLong2(CURLOPT.READDATA, readData, CURLOPT.READFUNCTION, readFunc);
    }

    private CurlEasy setSeekParams(long seekData, long seekFunc) {
        return setOptionLong2(CURLOPT.SEEKDATA, seekData, CURLOPT.SEEKFUNCTION, seekFunc);
    }

    public CurlEasy setWriteStream(IPeer stream) {
        Objects.requireNonNull(stream, "the stream object cannot be null");
        return setWriteParams(stream.ptr(), FN.WriteStream_handler);
    }

    public CurlEasy setWriteCallback(IPeer callback) {
        Objects.requireNonNull(callback, "write callback cannot be null");
        return setWriteParams(callback.ptr(), FN.WriteCallback_handler);
    }

    // https://curl.se/libcurl/c/CURLOPT_USERPWD.html
    public CurlEasy setUserPwd(String userPwd) {
        return setOptionString(CURLOPT.USERPWD, userPwd);
    }

    // https://curl.se/libcurl/c/CURLOPT_PROXYUSERPWD.html
    public CurlEasy setProxyUserPwd(String proxyUserPwd) {
        return setOptionString(CURLOPT.PROXYUSERPWD, proxyUserPwd);
    }

    // https://curl.se/libcurl/c/CURLOPT_PROXYPASSWORD.html
    public CurlEasy setProxyPassword(String password) {
        return setOptionString(CURLOPT.PROXYPASSWORD, password);
    }

    // https://curl.se/libcurl/c/CURLOPT_PROXYUSERNAME.html
    public CurlEasy setProxyUsername(String username) {
        return setOptionString(CURLOPT.PROXYUSERNAME, username);
    }

    // CURLOPT_UPLOAD
    public CurlEasy setUpload(boolean flag) {
        return setOptionBool(CURLOPT.UPLOAD, flag);
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

    // CURLOPT_COOKIE
    public CurlEasy setCookie(String cookie) {
        return setOptionString(CURLOPT.COOKIE, cookie);
    }

    public CurlEasy setAccumulator(IPeer acc) {
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

    public CurlEasy setReadFile(IPeer file) {
        Objects.requireNonNull(file, "read file cannot be null");
        return setReadParams(file.ptr(), FN.fread);
        // setSeekParams
        // TODO: windows: read_callback_file
        // TODO: seek file, read_seek_file
        // TODO: CURLOPT_UPLOAD=1
        // TODO: CURLOPT_POSTFIELDSIZE
    }

    // TODO: set3 opts
    // TODO: rename set1opt set2opts
    public CurlEasy setReadStream(ReadStream stream) {
        Objects.requireNonNull(stream, "read stream cannot be null");
        return setReadParams(stream.ptr(), FN.ReadStream_handler)
                .setSeekParams(Native.NULL, FN.ReadStream_seek);
        // TODO: curl_easy_setopt(curl, CURLOPT_UPLOAD, 1);
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

    public long getRetryAfter() {
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

    public long getHTTPVersion() {
        return getInfoLong(CURLINFO.HTTP_VERSION);
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
