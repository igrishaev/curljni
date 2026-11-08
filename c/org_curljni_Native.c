#include <jni.h>
#include "logging.h"
#include "globals.h"
#include "macros.h"
#include "curl/curl.h"
#include "Foo.h"

#define GET_CLASS(env, clsname, clsvar) \
    clsvar = JNI_CALL(env, FindClass, clsname); \
    if (!clsvar) { \
        log_error("JNI failed to find class: " clsname); \
        return JNI_ERR; \
    }

#define SET_FIELD(env, jcls, fname, ftype, fvar) \
    fvar = JNI_CALL(env, GetFieldID, jcls, fname, ftype); \
    if (!fvar) { \
        log_error("JNI failed to find field: " fname " " ftype); \
        return JNI_ERR; \
    }

#define SET_METHOD(env, jcls, name, sig, var) \
    var = JNI_CALL(env, GetMethodID, jcls, name, sig); \
    if (!var) { \
        log_error("JNI failed to find method: " name " " sig); \
        return JNI_ERR; \
    }

int J_VERSION = JNI_VERSION_1_8;

struct J_Globals _g;

static jint register_natives(JNIEnv *env) {

    jclass jcls = JNI_CALL(env, FindClass, "org/curljni/Foo");
    if (!jcls) return JNI_ERR;

    JNINativeMethod methods[] = {
        {"getFoo", "(J)J", Foo_getFoo}
    };
    jint nMethods = sizeof(methods) / sizeof(JNINativeMethod);

    return JNI_CALL(env, RegisterNatives, jcls, methods, nMethods);
}

jint JNI_OnLoad(JavaVM *vm, void *reserved) {

    log_debug("JNI_OnLoad start");

    JNIEnv* env;
    if ((*vm)->GetEnv(vm, (void **) &env, J_VERSION) != JNI_OK) {
        return JNI_ERR;
    }

    jint res = register_natives(env);
    if (res != JNI_OK) {
        return res;
    }

    CURLcode code = curl_global_init(CURL_GLOBAL_DEFAULT);
    if (code != CURLE_OK) {
        return JNI_ERR;
    }
    log_debug("cURL has been initialized globally");

    char * version = curl_version();
    log_debug("cURL version: %s", version);

    jclass jcls;
    jmethodID jmeth;
    jfieldID jfield;

    _g.vm = vm;

    /* WriteCallback */
    GET_CLASS(env, "org/curljni/WriteCallback", jcls);
    SET_METHOD(env, jcls, "callback", "(I)V", _g.WriteCallback.callback);

    /* WriteStream */
    GET_CLASS(env, "org/curljni/WriteStream", jcls);
    SET_METHOD(env, jcls, "callback", "(I)V", _g.WriteStream.callback);

    /* ReadStream */
    GET_CLASS(env, "org/curljni/ReadStream", jcls);
    SET_METHOD(env, jcls, "callback", "(I)I", _g.ReadStream.callback);

    log_debug("JNI_OnLoad end");
    return J_VERSION;
}

JNIEXPORT void JNICALL JNI_OnUnload(JavaVM *vm, void *reserved) {
    log_debug("JNI_OnUnload");
    _g.vm = NULL;
    curl_global_cleanup();
    log_debug("cURL has been globally cleaned up");
}

JNIEXPORT jlong JNICALL Java_org_curljni_Native__1get_1null
(JNIEnv *env, jclass jcls) {
    return (jlong) NULL;
}
