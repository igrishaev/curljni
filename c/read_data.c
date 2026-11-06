#include <string.h>
#include <jni.h>
#include "macros.h"
#include "globals.h"
#include "logging.h"
#include "curl/curl.h"
#include "read_data.h"

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
