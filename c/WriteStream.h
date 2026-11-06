#ifndef __WRETESTREAM_H
#define __WRETESTREAM_H

#include <jni.h>

typedef struct WriteStream WriteStream;

WriteStream * WriteStream_create(JNIEnv *env, jobject jbb, size_t bb_size, jobject this);

void WriteStream_delete(JNIEnv *env, WriteStream *wc);

size_t WriteStream_handler(char *data, size_t size, size_t nmemb, void *userdata);

#endif
