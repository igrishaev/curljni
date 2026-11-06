#include <string.h>
#include <jni.h>
#include "curl_data.h"
#include "macros.h"
#include "globals.h"
#include "logging.h"
#include "curl/curl.h"
#include "read_data.h"

size_t read_callback_stream(char *ptr, size_t size, size_t nmemb, void *userdata)
{
    size_t total = size * nmemb;
    struct curl_data *cd = (struct curl_data *) userdata;
    JNIEnv *env = cd->env;

    size_t limit = _MIN(total, cd->size);
    log_debug("read callback stream, total: %lu, limit: %lu", total, limit);

    jint read = JNI_CALL(env, CallIntMethod, cd->jobj, _g.InputStream.read_BaII, cd->jbuf, 0, limit);
    if (JNI_CALL(env, ExceptionCheck)) {
        log_error("JNI InputStream.read has failed");
#ifdef DEBUG
        JNI_CALL(env, ExceptionDescribe);
#endif
        return CURL_READFUNC_ABORT;
    }

    log_debug("bytes read: %i:", read);

    if (read == -1) { /* EOF */
        log_debug("read stream EOF is reached");
        return 0;
    }

    char * buf = JNI_CALL(env, GetPrimitiveArrayCritical, cd->jbuf, NULL);
    if (!buf) {
#ifdef DEBUG
        log_error("JNI GetPrimitiveArrayCritical has failed");
        JNI_CALL(env, ExceptionDescribe);
#endif
        return CURL_READFUNC_ABORT;
    }

    memcpy(ptr, buf, limit);

    JNI_CALL(env, ReleasePrimitiveArrayCritical, cd->jbuf, buf, JNI_ABORT);

    cd->i++;
    cd->total += read;

    return read;
}


size_t read_callback_file(char *ptr, size_t size, size_t nmemb, void *userdata) {
    log_debug("read callback file, nmemb: %lu", nmemb);
    FILE *f = (FILE *) userdata;
    size_t result = fread(ptr, size, nmemb, f);
    log_debug("read callback file, read: %lu", result);
    return result;
}

int read_seek_file(void *userdata, curl_off_t offset, int origin) {
    FILE *f = (FILE *) userdata;
    int res = fseek(f, offset, origin);
    if (res) {
        log_error("failed to seek a file, res: %d, offset: %ld, origin: %d",
                  res, offset, origin);
        return CURL_SEEKFUNC_FAIL;
    } else {
        log_debug("seeking a file was OK");
        return CURL_SEEKFUNC_OK;
    }
}

int read_seek_cannot(void *userdata, curl_off_t offset, int origin) {
    log_debug("calling read_seek_cannot");
    return CURL_SEEKFUNC_CANTSEEK;
}
