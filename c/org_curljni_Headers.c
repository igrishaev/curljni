#include <jni.h>
#include "macros.h"
#include "logging.h"
#include "curl/curl.h"

/*
  https://curl.se/libcurl/c/curl_slist_append.html
 */

JNIEXPORT jlong JNICALL Java_org_curljni_Headers__1allocate
  (JNIEnv *env, jclass cls, jobjectArray jheaders, jint jsize) {

    if (jheaders == NULL) {
        return (jlong) NULL;
    }

    struct curl_slist *slist = NULL;
    struct curl_slist *temp = NULL;
    int i;
    jstring jheader;
    char *header;
    int code = 0;

    for (i = 0; i < jsize; i++) {

        log_debug("processing header: %d", i);
        jheader = (jstring) JNI_CALL(env, GetObjectArrayElement, jheaders, i);
        if (jheader == NULL) {
            continue;
        }

        header = JNI_CALL(env, GetStringUTFChars, jheader, NULL);
        if (!header) {
            log_error("JNI GetStringUTFChars has failed");
            code = -1;
            goto exit;
        }
        temp = curl_slist_append(slist, header);
        log_debug("header: %s", header);
        JNI_CALL(env, ReleaseStringUTFChars, jheader, header);
        JNI_CALL(env, DeleteLocalRef, jheader);
        if (!temp) {
            log_error("curl_slist_append() has failed");
            code = -2;
            goto exit;
        }
        slist = temp;
    }

exit:
    if (code == 0) return (jlong) slist;
    if (slist) curl_slist_free_all(slist);
    return (jlong) NULL;
}

JNIEXPORT jlong JNICALL Java_org_curljni_Headers__1free
  (JNIEnv *env, jclass jcls, jlong jptr) {
    curl_slist_free_all((void *)jptr);
    return 0;
}
