#ifndef __READSTREAM_H
#define __READSTREAM_H

#include <jni.h>
#include "curl/curl.h"

typedef struct ReadStream ReadStream;

ReadStream * ReadStream_create(JNIEnv *env, jobject jbb, size_t bb_size, jobject this);

void ReadStream_delete(JNIEnv *env, ReadStream *wc);

size_t ReadStream_handler(char *data, size_t size, size_t nmemb, void *userdata);

int ReadStream_seek(void *userdata, curl_off_t offset, int origin);

#endif
