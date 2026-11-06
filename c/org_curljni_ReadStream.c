#include <jni.h>
#include "ReadStream.h"

JNIEXPORT jlong JNICALL Java_org_curljni_ReadStream__1allocate
  (JNIEnv *env, jobject this, jobject jbb, jint jbb_size) {
    return (jlong) ReadStream_create(env, jbb, jbb_size, this);
}

JNIEXPORT jlong JNICALL Java_org_curljni_ReadStream__1free
  (JNIEnv *env, jclass jcls, jlong jptr) {
    ReadStream_delete(env, (ReadStream *) jptr);
    return 0;
}
