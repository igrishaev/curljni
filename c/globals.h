#ifndef __GLOBALS_H
#define __GLOBALS_H

#include <jni.h>

struct J_WriteCallback {
    jmethodID callback;
};

struct J_WriteStream {
    jmethodID callback;
};

struct J_ReadStream {
    jmethodID callback;
};

struct J_Globals {
    JavaVM *vm;
    struct J_WriteStream WriteStream;
    struct J_ReadStream ReadStream;
    struct J_WriteCallback WriteCallback; // TODO rename
};

extern struct J_Globals _g;

#endif /* __GLOBALS_H */
