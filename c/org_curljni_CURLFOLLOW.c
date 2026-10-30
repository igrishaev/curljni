#include <jni.h>
#include "macros.h"
#include "curl/curl.h"

JNIEXPORT jlongArray JNICALL Java_org_curljni_CURLFOLLOW__1get_1values
  (JNIEnv *env, jclass jcls) {
    long values[] = {
        0, /* OFF */
        CURLFOLLOW_ALL,
        CURLFOLLOW_OBEYCODE,
        CURLFOLLOW_FIRSTONLY
    };

    jsize len = sizeof(values) / sizeof(long);

    jlongArray jresult = JNI_CALL(env, NewLongArray, len);
    if (jresult == NULL) {
        return NULL;
    }

    JNI_CALL(env, SetLongArrayRegion, jresult, 0, len, (const jlong *) values);

    return jresult;
}
