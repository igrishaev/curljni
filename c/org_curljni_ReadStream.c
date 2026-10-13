#include <jni.h>
#include "curl_data.h"


JNIEXPORT jlong JNICALL Java_org_curljni_ReadStream__1allocate
  (JNIEnv *env, jclass jcls, jobject jstream) {
    return (jlong) curl_data_init(env, jstream);
}


JNIEXPORT jlong JNICALL Java_org_curljni_ReadStream__1free
  (JNIEnv *env, jclass jcls, jlong jptr) {
    void *ptr = (void *) jptr;
    curl_data_free(ptr);
    return 0;
}
