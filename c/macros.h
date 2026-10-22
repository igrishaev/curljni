#ifndef __MACROS_H
#define __MACROS_H

#define J_HASHMAP    "Ljava/util/HashMap;"
#define J_MAP        "Ljava/util/Map;"
#define J_OBJ        "Ljava/lang/Object;"
#define J_STRING     "Ljava/lang/String;"
#define J_STRING_ARR "[Ljava/lang/String;"
#define J_INT        "I"
#define J_LONG       "J"
#define J_BOOL       "Z"
#define J_OS         "Ljava/io/OutputStream;"
#define J_BA         "[B"

#define _MAX(a, b) (((a) > (b)) ? (a) : (b))
#define _MIN(a, b) (((a) > (b)) ? (b) : (a))

#define JNI_CALL(env, method, ...) ((*env)->method(env, ##__VA_ARGS__))

#endif /* __MACROS_H */
