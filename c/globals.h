#ifndef __GLOBALS_H
#define __GLOBALS_H

#include <jni.h>

struct J_WriteCallback {
    jmethodID callback;
};


struct J_OutputStream {
    jmethodID write_BaII;
    jmethodID close;
};

struct J_InputStream {
    jmethodID read_BaII;
    jmethodID close;
};

struct J_Globals {
    JavaVM *vm;
    struct J_OutputStream OutputStream;
    struct J_InputStream InputStream;
    struct J_WriteCallback WriteCallback;
};

extern struct J_Globals _g;

#endif /* __GLOBALS_H */
