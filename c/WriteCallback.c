#include <jni.h>
#include <string.h>
#include <stdlib.h>
#include "globals.h"
#include "macros.h"
#include "curl/curl.h"
#include "WriteCallback.h"

struct WriteCallback {
    char *bb;
    size_t bb_size;
    JNIEnv *env;
    jobject this;
    jmethodID callback;
};

WriteCallback * WriteCallback_create(JNIEnv *env, jobject jbb, size_t bb_size, jobject this) {
    WriteCallback *wc = malloc(sizeof(WriteCallback));

    void *bb = JNI_CALL(env, GetDirectBufferAddress, jbb);
    if (!bb) {
        return NULL;
    }

    jobject thisGlobal = JNI_CALL(env, NewGlobalRef, this);
    if (!thisGlobal) {
        return NULL;
    }

    wc->bb = bb;
    wc->bb_size = bb_size;
    wc->env = env;
    wc->this = thisGlobal;
    wc->callback = _g.WriteCallback.callback;
    return wc;
}

void WriteCallback_delete(JNIEnv *env, WriteCallback *wc) {
    JNI_CALL(env, DeleteGlobalRef, wc->this);
    free(wc);
}

size_t WriteCallback_handler(char *data, size_t size, size_t nmemb, void *userdata) {
    WriteCallback *wc = (WriteCallback *) userdata;
    size_t total = size * nmemb;

    memcpy(wc->bb, data, total);
    JNI_CALL(wc->env, CallVoidMethod, wc->this, wc->callback, total);
    if (JNI_CALL(wc->env, ExceptionCheck)) {
        return CURL_WRITEFUNC_ERROR;
    } else {
        return total;
    }
}
