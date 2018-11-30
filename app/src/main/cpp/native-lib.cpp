#include <jni.h>
#include <string>

extern "C" JNIEXPORT jstring

JNICALL
Java_com_example_fdkaactest_fdkaac_NativeTest_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "哈哈Hello from C++";
    return env->NewStringUTF(hello.c_str());
}
