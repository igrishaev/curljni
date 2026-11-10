#include <jni.h>
#include <string.h>
#include <stdlib.h>
#include "globals.h"
#include "logging.h"
#include "macros.h"
#include "curl/curl.h"
#include "ReadStream.h"

struct ReadStream {
    char *bb;
    size_t bb_size;
    JNIEnv *env;
    jobject this;
    jmethodID callback;
};

ReadStream * ReadStream_create(JNIEnv *env, jobject jbb, size_t bb_size, jobject this) {
    ReadStream *rs = malloc(sizeof(ReadStream));

    void *bb = JNI_CALL(env, GetDirectBufferAddress, jbb);
    if (!bb) {
        return NULL;
    }

    jobject thisGlobal = JNI_CALL(env, NewGlobalRef, this);
    if (!thisGlobal) {
        return NULL;
    }

    rs->bb = bb;
    rs->bb_size = bb_size;
    rs->env = env;
    rs->this = thisGlobal;
    rs->callback = _g.ReadStream.callback;
    return rs;
}

void ReadStream_delete(JNIEnv *env, ReadStream *rs) {
    JNI_CALL(env, DeleteGlobalRef, rs->this);
    free(rs);
}

size_t ReadStream_handler(char *ptr, size_t size, size_t nmemb, void *userdata) {
    size_t limit = size * nmemb;
    ReadStream *rs = (ReadStream *) userdata;

    jint read = JNI_CALL(rs->env, CallIntMethod, rs->this, rs->callback, limit);
    if (JNI_CALL(rs->env, ExceptionCheck)) {
        return CURL_READFUNC_ABORT;
    }
    log_debug("bytes read: %i", read);

    if (read == 0 || read == -1) {
        return 0;
    }
    memcpy(ptr, rs->bb, read);
    return read;
}

int ReadStream_seek(void *userdata, curl_off_t offset, int origin) {
    return CURL_SEEKFUNC_CANTSEEK;
}
