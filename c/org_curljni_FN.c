#include <jni.h>
#include "macros.h"
#include "write_data.h"
#include "accum.h"

JNIEXPORT jlongArray JNICALL Java_org_curljni_FN__1get_1values
  (JNIEnv *env, jclass jcls) {

    jlong values[] = {
        (jlong) fwrite,
        (jlong) write_callback_stream,
        (jlong) write_callback_handler,
        (jlong) accum_write_callback
    };

    jsize len = sizeof(values) / sizeof(jlong);

    jlongArray jresult = JNI_CALL(env, NewLongArray, len);
    if (jresult == NULL) {
        return NULL;
    }

    JNI_CALL(env, SetLongArrayRegion, jresult, 0, len, values);
    return jresult;
}
