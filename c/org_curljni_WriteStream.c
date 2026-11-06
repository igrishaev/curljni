#include <jni.h>
#include "WriteStream.h"

JNIEXPORT jlong JNICALL Java_org_curljni_WriteStream__1allocate
  (JNIEnv *env, jobject this, jobject jbb, jint jbb_size) {
    return (jlong) WriteStream_create(env, jbb, jbb_size, this);
}

JNIEXPORT jlong JNICALL Java_org_curljni_WriteStream__1free
  (JNIEnv *env, jclass jobj, jlong jptr) {
    WriteStream_delete(env, (struct WriteStream *) jptr);
    return 0;
}
