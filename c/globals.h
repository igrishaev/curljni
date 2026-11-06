#ifndef __GLOBALS_H
#define __GLOBALS_H

#include <jni.h>

struct J_WriteCallback {
    jmethodID callback;
};

struct J_WriteStream {
    jmethodID callback;
};

struct J_InputStream {
    jmethodID read_BaII;
    jmethodID close;
};

struct J_Globals {
    JavaVM *vm;
    struct J_InputStream InputStream;   // TODO
    struct J_WriteStream WriteStream;
    struct J_WriteCallback WriteCallback; // TODO rename
};

extern struct J_Globals _g;

#endif /* __GLOBALS_H */
