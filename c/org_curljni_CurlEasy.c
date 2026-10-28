#include <jni.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "accum.h"
#include "logging.h"
#include "globals.h"
#include "macros.h"
#include "write_data.h"
#include "read_data.h"
#include "bytebuffer.h"
#include "curl/curl.h"


static jlong _curl_easy_set_string(JNIEnv *env, jlong jcurl, CURLoption option, jstring jvalue) {

    CURL *curl = (CURL *) jcurl;
    CURLcode code;
    char *value = NULL;

    if (jvalue == NULL) {
        code = curl_easy_setopt(curl, option, NULL);
        goto cleanup;
    }

    value = JNI_CALL(env, GetStringUTFChars, jvalue, NULL);
    if (!value) {
        code = CURLE_OUT_OF_MEMORY;
        goto cleanup;
    }

    code = curl_easy_setopt(curl, option, value);

cleanup:
    if (value) JNI_CALL(env, ReleaseStringUTFChars, jvalue, value);
    return code;
}


static jlong _curl_easy_set_long(jlong jcurl, CURLoption option, jlong jvalue) {
    CURL *curl = (CURL *) jcurl;
    return curl_easy_setopt(curl, option, jvalue);
}

static jstring _get_info_as_string(JNIEnv *env, jlong jcurl, jlong jbb, CURLINFO info) {
    CURL *curl = (CURL *) jcurl;
    char *bb = (char *) jbb;
    char *ptr;
    CURLcode code = curl_easy_getinfo(curl, info, &ptr);
    put_long(bb, code);
    if (code == CURLE_OK) {
        return JNI_CALL(env, NewStringUTF, ptr);
    } else {
        return NULL;
    }
}

// TODO
static CURLcode _get_info_as_long(jlong jcurl, jlong jbb, CURLINFO info) {
    CURL *curl = (CURL *) jcurl;
    char *bb = (char *) jbb;
    long value;
    CURLcode code = curl_easy_getinfo(curl, info, &value);
    put_long(bb, value);
    return code;
}


//
// JNI
//


JNIEXPORT jlong JNICALL Java_org_curljni_CurlEasy__1curl_1easy_1init
  (JNIEnv *env, jclass jcls) {
    return (jlong) curl_easy_init();
}

JNIEXPORT jlong JNICALL Java_org_curljni_CurlEasy__1curl_1easy_1cleanup
  (JNIEnv *env, jclass jcls, jlong jcurl) {
    curl_easy_cleanup((CURL *) jcurl);
    return 0;
}

JNIEXPORT jlong JNICALL Java_org_curljni_CurlEasy__1set_1method
  (JNIEnv *env, jclass jcls, jlong jcurl, jlong jmethod) {

    // TODO
    CURLcode code;
    CURL *curl = (CURL *) jcurl;

    switch (jmethod) {
        case 1: { // TODO use enum
            code = curl_easy_setopt(curl, CURLOPT_HTTPGET, 1);
            if (code != CURLE_OK) {
                log_error("curl_easy_setopt() CURLOPT_HTTPGET has failed");
                goto exit;
            }
            break;
        }
        case 2: {
            code = curl_easy_setopt(curl, CURLOPT_HTTPPOST, 1);
            if (code != CURLE_OK) {
                log_error("curl_easy_setopt() CURLOPT_HTTPPOST has failed");
                goto exit;
            }
            break;
        }
        default: {
            log_error("unknown HTTP method: %d", jmethod);
            code = CURLE_UNKNOWN_OPTION;
            goto exit;
        }
    }

exit:
    return code;

}

JNIEXPORT jlong JNICALL Java_org_curljni_CurlEasy__1perform
  (JNIEnv *env, jclass jcls, jlong jcurl) {
    CURL *curl = (CURL *) jcurl;
    return curl_easy_perform(curl);
}


JNIEXPORT jstring JNICALL Java_org_curljni_CurlEasy__1curl_1easy_1strerror
  (JNIEnv *env, jclass jcls, jlong jcode) {
    const char *str = curl_easy_strerror(jcode);
    if (str) {
        return JNI_CALL(env, NewStringUTF, str);
    } else {
        return NULL;
    }
}

JNIEXPORT jint JNICALL Java_org_curljni_CurlEasy__1get_1CURL_1ERROR_1SIZE
  (JNIEnv *env, jclass jcls) {
    return CURL_ERROR_SIZE;
}

JNIEXPORT jlong JNICALL Java_org_curljni_CurlEasy__1curl_1easy_1reset
  (JNIEnv *env, jclass jcls, jlong jcurl) {
    CURL *curl = (CURL *) jcurl;
    curl_easy_reset(curl);
    return CURLE_OK;
}

//
// Setters
//

static CURLcode set_write_params(CURL *curl, void *write_data, void *write_function) {
    CURLcode code;
    code = curl_easy_setopt(curl, CURLOPT_WRITEDATA, write_data);
    if (code != CURLE_OK) return code;
    code = curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION, write_function);
    if (code != CURLE_OK) return code;
    return code;
}

JNIEXPORT jlong JNICALL Java_org_curljni_CurlEasy__1set_1write_1file
  (JNIEnv *env, jclass jcls, jlong jcurl, jlong jfile) {
    CURL *curl = (CURL *) jcurl;
    return set_write_params(curl, (FILE *) jfile, fwrite);
}


JNIEXPORT jlong JNICALL Java_org_curljni_CurlEasy__1set_1write_1stream
  (JNIEnv *env, jclass jcls, jlong jcurl, jlong jstream) {
    CURL *curl = (CURL *) jcurl;
    return set_write_params(curl, (void *) jstream, write_callback_stream);
}


JNIEXPORT jlong JNICALL Java_org_curljni_CurlEasy__1set_1write_1callback
  (JNIEnv *env, jclass jcls, jlong jcurl, jlong jptr) {
    CURL *curl = (CURL *) jcurl;
    return set_write_params(curl, (void *) jptr, write_callback_handler);
}


JNIEXPORT jlong JNICALL Java_org_curljni_CurlEasy__1set_1accumulator
  (JNIEnv *env, jclass jcls, jlong jcurl, jlong jacc) {
    CURL *curl = (CURL *) jcurl;
    return set_write_params(curl, (void *) jacc, accum_write_callback);
}

JNIEXPORT jlong JNICALL Java_org_curljni_CurlEasy__1set_1post_1fields_1bytes
  (JNIEnv *env, jclass jcls, jlong jcurl, jbyteArray jbuf, jint jlen) {

    CURLcode code;
    CURL *curl = (CURL *) jcurl;
    char *buf = JNI_CALL(env, GetPrimitiveArrayCritical, jbuf, NULL);
    if (!buf) {
        code = CURLE_OUT_OF_MEMORY;
        goto exit;
    }

    code = curl_easy_setopt(curl, CURLOPT_COPYPOSTFIELDS, buf);
    JNI_CALL(env, ReleasePrimitiveArrayCritical, jbuf, buf, JNI_ABORT);
    if (code != CURLE_OK) goto exit;

    code = curl_easy_setopt(curl, CURLOPT_POSTFIELDSIZE, jlen);
    if (code != CURLE_OK) goto exit;

exit:
    return code;
}

static CURLcode set_read_params(CURL *curl, void *read_data, void *read_function) {
    CURLcode code;
    code = curl_easy_setopt(curl, CURLOPT_READDATA, read_data);
    if (code != CURLE_OK) return code;
    code = curl_easy_setopt(curl, CURLOPT_READFUNCTION, read_function);
    if (code != CURLE_OK) return code;
    return code;
}


static CURLcode set_seek_params(CURL *curl, void *seek_data, void *seek_function) {
    CURLcode code;
    code = curl_easy_setopt(curl, CURLOPT_SEEKDATA, seek_data);
    if (code != CURLE_OK) goto exit;
    code = curl_easy_setopt(curl, CURLOPT_SEEKFUNCTION, seek_function);
    if (code != CURLE_OK) goto exit;
exit:
    return code;
}

JNIEXPORT jlong JNICALL Java_org_curljni_CurlEasy__1set_1tcp_1fast_1open
  (JNIEnv *env, jclass jcls, jlong jcurl, jlong jvalue) {
    return _curl_easy_set_long(jcurl, CURLOPT_TCP_FASTOPEN, jvalue);
}

JNIEXPORT jlong JNICALL Java_org_curljni_CurlEasy__1set_1tcp_1keep_1alive
  (JNIEnv *env, jclass jcls, jlong jcurl, jlong jvalue) {
    return _curl_easy_set_long(jcurl, CURLOPT_TCP_KEEPALIVE, jvalue);
}

JNIEXPORT jlong JNICALL Java_org_curljni_CurlEasy__1set_1xoauth2_1bearer
  (JNIEnv *env, jclass jcls, jlong jcurl, jstring jvalue) {
    return _curl_easy_set_string(env, jcurl, CURLOPT_XOAUTH2_BEARER, jvalue);
}

JNIEXPORT jlong JNICALL Java_org_curljni_CurlEasy__1set_1custom_1request
  (JNIEnv *env, jclass jcls, jlong jcurl, jstring jvalue) {
    return _curl_easy_set_string(env, jcurl, CURLOPT_CUSTOMREQUEST, jvalue);
}

JNIEXPORT jlong JNICALL Java_org_curljni_CurlEasy__1set_1read_1file
  (JNIEnv *env, jclass jcls, jlong jcurl, jlong jfile) {
    CURL *curl = (CURL *) jcurl;
    CURLcode code;

    code = set_read_params(curl, (FILE *) jfile, fread);
    if (code != CURLE_OK) goto exit;

    // TODO windows
    /* code = set_read_params(curl, (FILE *) jfile, read_callback_file); */
    /* if (code != CURLE_OK) goto exit; */

    /* code = set_seek_params(curl, (FILE *) jfile, read_seek_file); */
    /* if (code != CURLE_OK) goto exit; */

    /* code = curl_easy_setopt(curl, CURLOPT_UPLOAD, 1); */
    /* if (code != CURLE_OK) goto exit; */

    // TODO: set size?
    /* code = curl_easy_setopt(curl, CURLOPT_POSTFIELDSIZE, ????); */
    /* if (code != CURLE_OK) goto exit; */

exit:
    return code;
}

JNIEXPORT jlong JNICALL Java_org_curljni_CurlEasy__1set_1read_1stream
  (JNIEnv *env, jclass jcls, jlong jcurl, jlong jstream) {

    CURL *curl = (CURL *) jcurl;
    CURLcode code;

    // TODO
    code = set_read_params(curl, (void *) jstream, read_callback_stream);
    if (code != CURLE_OK) goto exit;

    code = set_seek_params(curl, NULL, read_seek_cannot);
    if (code != CURLE_OK) goto exit;

    code = curl_easy_setopt(curl, CURLOPT_UPLOAD, 1);
    if (code != CURLE_OK) goto exit;

exit:
    return code;
}

JNIEXPORT jlong JNICALL Java_org_curljni_CurlEasy__1set_1option_1long
  (JNIEnv *env, jclass jcls, jlong jcurl, jlong joption, jlong jvalue) {
    CURL *curl = (CURL *) jcurl;
    return curl_easy_setopt(curl, joption, jvalue);
}

JNIEXPORT jlong JNICALL Java_org_curljni_CurlEasy__1set_1option_1string
  (JNIEnv *env, jclass jcls, jlong jcurl, jint joption, jstring jvalue) {

    CURL *curl = (CURL *) jcurl;
    CURLcode code;
    char *value = NULL;

    if (jvalue == NULL) {
        code = curl_easy_setopt(curl, joption, NULL);
        goto cleanup;
    }

    value = JNI_CALL(env, GetStringUTFChars, jvalue, NULL);
    if (!value) {
        code = CURLE_OUT_OF_MEMORY;
        goto cleanup;
    }

    code = curl_easy_setopt(curl, joption, value);

cleanup:
    if (value) JNI_CALL(env, ReleaseStringUTFChars, jvalue, value);
    return code;
}


//
// Getters
//

JNIEXPORT jlong JNICALL Java_org_curljni_CurlEasy__1get_1info_1as_1long
  (JNIEnv *env, jclass jcls, jlong jcurl, jint jinfo, jlong jbb) {
    CURL *curl = (CURL *) jcurl;
    char *bb = (char *) jbb;
    long value;
    CURLcode code = curl_easy_getinfo(curl, jinfo, &value);
    put_long(bb, value);
    return code;
}

JNIEXPORT jlong JNICALL Java_org_curljni_CurlEasy__1get_1num_1connects
  (JNIEnv *env, jclass jcls, jlong jcurl, jlong jbb) {
    return _get_info_as_long(jcurl, jbb, CURLINFO_NUM_CONNECTS);
}

JNIEXPORT jlong JNICALL Java_org_curljni_CurlEasy__1get_1redirect_1count
  (JNIEnv *env, jclass jcls, jlong jcurl, jlong jbb) {
    return _get_info_as_long(jcurl, jbb, CURLINFO_REDIRECT_COUNT);
}

JNIEXPORT jlong JNICALL Java_org_curljni_CurlEasy__1get_1local_1port
  (JNIEnv *env, jclass jcls, jlong jcurl, jlong jbb) {
    return _get_info_as_long(jcurl, jbb, CURLINFO_LOCAL_PORT);
}

JNIEXPORT jlong JNICALL Java_org_curljni_CurlEasy__1get_1proxy_1error
  (JNIEnv *env, jclass jcls, jlong jcurl, jlong jbb) {
    return _get_info_as_long(jcurl, jbb, CURLINFO_PROXY_ERROR);
}

JNIEXPORT jlong JNICALL Java_org_curljni_CurlEasy__1get_1primary_1port
  (JNIEnv *env, jclass jcls, jlong jcurl, jlong jbb) {
    return _get_info_as_long(jcurl, jbb, CURLINFO_PRIMARY_PORT);
}

JNIEXPORT jlong JNICALL Java_org_curljni_CurlEasy__1get_1os_1errno
  (JNIEnv *env, jclass jcls, jlong jcurl, jlong jbb) {
    return _get_info_as_long(jcurl, jbb, CURLINFO_OS_ERRNO);
}

JNIEXPORT jstring JNICALL Java_org_curljni_CurlEasy__1get_1primary_1ip
  (JNIEnv *env, jclass jcls, jlong jcurl, jlong jbb) {
    return _get_info_as_string(env, jcurl, jbb, CURLINFO_PRIMARY_IP);
}

JNIEXPORT jstring JNICALL Java_org_curljni_CurlEasy__1get_1effective_1url
  (JNIEnv *env, jclass jcls, jlong jcurl, jlong jbb) {
    return _get_info_as_string(env, jcurl, jbb, CURLINFO_EFFECTIVE_URL);
}

JNIEXPORT jstring JNICALL Java_org_curljni_CurlEasy__1get_1local_1ip
  (JNIEnv *env, jclass jcls, jlong jcurl, jlong jbb) {
    return _get_info_as_string(env, jcurl, jbb, CURLINFO_LOCAL_IP);
}

JNIEXPORT jlong JNICALL Java_org_curljni_CurlEasy__1curl_1easy_1nextheader
  (JNIEnv *env, jclass jcls, jlong jcurl, jlong jheader) {
    CURL *curl = (CURL *) jcurl;
    struct curl_header *h = (struct curl_header *) jheader;
    return (jlong) curl_easy_nextheader(curl, CURLH_HEADER, -1, h);
}

JNIEXPORT jstring JNICALL Java_org_curljni_CurlEasy__1header_1name
  (JNIEnv *env, jclass jcls, jlong jheader) {
    struct curl_header *h = (struct curl_header *) jheader;
    if (!h) {
        return NULL;
    }
    return JNI_CALL(env, NewStringUTF, h->name);
}

JNIEXPORT jstring JNICALL Java_org_curljni_CurlEasy__1header_1value
  (JNIEnv *env, jclass jcls, jlong jheader) {
    struct curl_header *h = (struct curl_header *) jheader;
    if (!h) {
        return NULL;
    }
    return JNI_CALL(env, NewStringUTF, h->value);
}


// error: proxy error, errno
/* https://curl.se/libcurl/c/CURLINFO_HTTP_VERSION.html */
/* headers */

/* https://curl.se/libcurl/c/CURLOPT_RANGE.html */

/* https://curl.se/libcurl/c/CURLOPT_TCP_KEEPCNT.html */
/* https://curl.se/libcurl/c/CURLOPT_TCP_KEEPIDLE.html */
/* https://curl.se/libcurl/c/CURLOPT_TCP_KEEPINTVL.html */
/* https://curl.se/libcurl/c/CURLOPT_TCP_NODELAY.html */

/* https://curl.se/libcurl/c/CURLOPT_HTTP_VERSION.html */
/* https://curl.se/libcurl/c/CURLOPT_AWS_SIGV4.html */
/* https://curl.se/libcurl/c/CURLOPT_COOKIE.html */
/* https://curl.se/libcurl/c/CURLOPT_COOKIEFILE.html */
/* https://curl.se/libcurl/c/CURLOPT_COOKIEJAR.html */
/* https://curl.se/libcurl/c/CURLOPT_COOKIESESSION.html */
/* https://curl.se/libcurl/c/CURLOPT_DEFAULT_PROTOCOL.html */
/* https://curl.se/libcurl/c/CURLOPT_HTTPPROXYTUNNEL.html */
/* https://curl.se/libcurl/c/CURLOPT_LOGIN_OPTIONS.html */
/* https://curl.se/libcurl/c/CURLOPT_PRE_PROXY.html */
/* https://curl.se/libcurl/c/CURLOPT_PROXYAUTH.html */
/* https://curl.se/libcurl/c/CURLOPT_PROXYPASSWORD.html */
/* https://curl.se/libcurl/c/CURLOPT_PROXYPORT.html */
/* https://curl.se/libcurl/c/CURLOPT_PROXYTYPE.html */
/* https://curl.se/libcurl/c/CURLOPT_PROXYUSERNAME.html */
/* https://curl.se/libcurl/c/CURLOPT_PROXYUSERPWD.html */
/* https://curl.se/libcurl/c/CURLOPT_SSLCERT.html */
