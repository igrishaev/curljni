#include <jni.h>

jlong JNICALL Foo_getFoo (JNIEnv *env, jclass jcls, jlong foo) {
    return foo * 2;
}
