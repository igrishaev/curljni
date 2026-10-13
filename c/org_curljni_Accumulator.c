#include <jni.h>
#include "macros.h"
#include "logging.h"
#include "accum.h"

JNIEXPORT jlong JNICALL Java_org_curljni_Accumulator__1allocate
  (JNIEnv *env, jclass jcls, jlong jsize) {
    return (jlong) accum_init(jsize, 2);
}

JNIEXPORT jlong JNICALL Java_org_curljni_Accumulator__1free
  (JNIEnv *env, jclass jcls, jlong jptr) {
    accum_free((void *)jptr);
    return 0;
}

JNIEXPORT jbyteArray JNICALL Java_org_curljni_Accumulator__1get_1bytes
  (JNIEnv *env, jclass jcls, jlong jptr) {
    struct accum * acc = (struct accum *) jptr;
    jbyteArray jarr = JNI_CALL(env, NewByteArray, acc->len);
    JNI_CALL(env, SetByteArrayRegion, jarr, 0, acc->len, (jbyte *) acc->buf);
    return jarr;
}
