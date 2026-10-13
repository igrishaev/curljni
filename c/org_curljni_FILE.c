#include <jni.h>
#include <stdio.h>
#include "macros.h"
#include "logging.h"

JNIEXPORT jlong JNICALL Java_org_curljni_FILE__1fopen
  (JNIEnv *env, jclass jcls, jstring jpath, jstring jmode) {

    const char *path = NULL;
    const char *mode = NULL;
    FILE *fp = NULL;
    int result = 0;

    path = JNI_CALL(env, GetStringUTFChars, jpath, NULL);
    if (!path) {
        log_error("JNI GetStringUTFChars error");
        return -1;
        goto exit;
    }

    mode = JNI_CALL(env, GetStringUTFChars, jmode, NULL);
    if (!mode) {
        log_error("JNI GetStringUTFChars error");
        result = -2;
        goto exit;
    }

    fp = fopen(path, mode);
    if (!fp) {
        log_error("failed to open a file, mode: %s, path: %s", mode, path);
        result = -3;
        goto exit;
    }

exit:
    if (path) JNI_CALL(env, ReleaseStringUTFChars, jpath, path);
    if (mode) JNI_CALL(env, ReleaseStringUTFChars, jmode, mode);

    if (result == 0) {
        return (jlong) fp;
    } else {
        return result;
    }
}

JNIEXPORT jlong JNICALL Java_org_curljni_FILE__1fclose
  (JNIEnv *env, jclass jcls, jlong jfile) {
    FILE *fp = (FILE *) jfile;
    return fclose(fp);
}
