#include <jni.h>
#include "globals.h"
#include "macros.h"

struct WriteCallback {
    char *bb;
    size_t bb_size;
    JNIEnv *env;
    jobject this;
    jmethodID callback;
};

struct WriteCallback * WriteCallback_create(JNIEnv *env, char *bb, size_t bb_size, jobject this) {
    struct WriteCallback *wc = malloc(sizeof(struct WriteCallback));

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

void * WriteCallback_delete(JNIEnv *env struct WriteCallback *wc) {
    JNI_CALL(env, DeleteLocalRef, wc->this);
    free(wc);
}

size_t WriteCallback_handler(char *data, size_t size, size_t nmemb, void *userdata) {
    struct WriteCallback *wc = (struct WriteCallback *) userdata;
    size_t total = size * nmemb;

    memcpy(wc->bb, data, size);
    JNI_CALL(wc->env, CallVoidMethod, wc->this, wc->callback, total);
    if (JNI_CALL(wc->env, ExceptionCheck)) {
        return CURL_WRITEFUNC_ERROR;
    } else {
        return size;
    }
}
