#include <jni.h>
#include "bytebuffer.h"

JNIEXPORT jint JNICALL Java_org_curljni_Arena__1init_1byte_1buffer
  (JNIEnv *env, jclass jcls, jobject jbb) {

    void *addr = (*env)->GetDirectBufferAddress(env, jbb);
    if (!addr) {
        return -1;
    }

    char *bb = (char *) addr;
    bb = bb_put_byte(bb, 1);
    bb = bb_put_long(bb, (long) NULL);
    bb = bb_put_long(bb, (long) addr);

    return 0;
}
