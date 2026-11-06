#ifndef __WRETECALLBACK_H
#define __WRETECALLBACK_H

#include <jni.h>

typedef struct WriteCallback WriteCallback;

WriteCallback * WriteCallback_create(JNIEnv *env, jobject jbb, size_t bb_size, jobject this);

void WriteCallback_delete(JNIEnv *env, WriteCallback *wc);

size_t WriteCallback_handler(char *data, size_t size, size_t nmemb, void *userdata);

#endif
