#ifndef __WRETECALLBACK_H
#define __WRETECALLBACK_H

struct WriteCallback;

struct WriteCallback * WriteCallback_create(char *bb, size_t bb_size, jobject this);
void * WriteCallback_free(struct WriteCallback *wc);

size_t WriteCallback_handler(char *data, size_t size, size_t nmemb, void *userdata);

#endif
