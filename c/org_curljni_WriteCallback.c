#include <jni.h>
#include "WriteCallback.h"

JNIEXPORT jlong JNICALL Java_org_curljni_WriteCallback__1allocate
  (JNIEnv *env, jclass jcls, jobject jhandler) {
    return (jlong) WriteCallback_create(env, jbb, jbb_size, this);
}

JNIEXPORT jlong JNICALL Java_org_curljni_WriteCallback__1free
  (JNIEnv *env, jclass jcls, jlong jptr) {
    WriteCallback_delete(env, (struct WriteCallback *) jptr);
    return 0;
}
