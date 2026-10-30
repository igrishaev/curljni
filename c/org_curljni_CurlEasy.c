#include <jni.h>
#include <errno.h>
#include <string.h>
#include "logging.h"
#include "macros.h"
#include "bytebuffer.h"
#include "curl/curl.h"


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

JNIEXPORT jlong JNICALL Java_org_curljni_CurlEasy__1curl_1easy_1reset
  (JNIEnv *env, jclass jcls, jlong jcurl) {
    CURL *curl = (CURL *) jcurl;
    curl_easy_reset(curl);
    return CURLE_OK;
}


JNIEXPORT void JNICALL Java_org_curljni_CurlEasy__1get_1errno_1and_1message
  (JNIEnv *env, jclass jcls, jlong jbb) {
    char *bb = (char *) jbb;
    char *err = strerror(errno);
    bb = bb_put_long(bb, errno);
    bb = bb_put_char(bb, err);
}


//
// Setters
//


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

JNIEXPORT jlong JNICALL Java_org_curljni_CurlEasy__1set_1option_1long
  (JNIEnv *env, jclass jcls, jlong jcurl, jlong joption, jlong jvalue) {
    CURL *curl = (CURL *) jcurl;
    return curl_easy_setopt(curl, joption, jvalue);
}

JNIEXPORT jlong JNICALL Java_org_curljni_CurlEasy__1set_1option_1long2
  (JNIEnv *env, jclass jcls, jlong jcurl, jint jopt1, jlong jval1, jint jopt2, jlong jval2) {
    CURL *curl = (CURL *) jcurl;
    CURLcode code;
    code = curl_easy_setopt(curl, jopt1, jval1);
    if (code != CURLE_OK) return code;
    code = curl_easy_setopt(curl, jopt2, jval2);
    if (code != CURLE_OK) return code;
    return code;
}

JNIEXPORT jlong JNICALL Java_org_curljni_CurlEasy__1set_1option_1long3
  (JNIEnv *env, jclass jcls, jlong jcurl, jint jopt1, jlong jval1, jint jopt2, jlong jval2, jint jopt3, jlong jval3) {
    CURL *curl = (CURL *) jcurl;
    CURLcode code;
    code = curl_easy_setopt(curl, jopt1, jval1);
    if (code != CURLE_OK) return code;
    code = curl_easy_setopt(curl, jopt2, jval2);
    if (code != CURLE_OK) return code;
    code = curl_easy_setopt(curl, jopt3, jval3);
    if (code != CURLE_OK) return code;
    return code;
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
    bb_put_long(bb, value);
    return code;
}


JNIEXPORT jstring JNICALL Java_org_curljni_CurlEasy__1get_1info_1as_1string
  (JNIEnv *env, jclass jcls, jlong jcurl, jint jinfo, jlong jbb) {
    CURL *curl = (CURL *) jcurl;
    char *bb = (char *) jbb;
    char *ptr;
    CURLcode code = curl_easy_getinfo(curl, jinfo, &ptr);
    bb_put_long(bb, code);
    if (code == CURLE_OK) {
        return JNI_CALL(env, NewStringUTF, ptr);
    } else {
        return NULL;
    }
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


/* https://curl.se/libcurl/c/CURLOPT_COOKIEFILE.html */
/* https://curl.se/libcurl/c/CURLOPT_COOKIEJAR.html */
/* https://curl.se/libcurl/c/CURLOPT_COOKIESESSION.html */
/* https://curl.se/libcurl/c/CURLOPT_RANGE.html */
/* https://curl.se/libcurl/c/CURLOPT_HTTP_VERSION.html */
/* https://curl.se/libcurl/c/CURLOPT_AWS_SIGV4.html */
/* https://curl.se/libcurl/c/CURLOPT_DEFAULT_PROTOCOL.html */
/* https://curl.se/libcurl/c/CURLOPT_HTTPPROXYTUNNEL.html */
/* https://curl.se/libcurl/c/CURLOPT_LOGIN_OPTIONS.html */
/* https://curl.se/libcurl/c/CURLOPT_PRE_PROXY.html */
/* https://curl.se/libcurl/c/CURLOPT_PROXYAUTH.html */
/* https://curl.se/libcurl/c/CURLOPT_PROXYPORT.html */
/* https://curl.se/libcurl/c/CURLOPT_PROXYTYPE.html */
