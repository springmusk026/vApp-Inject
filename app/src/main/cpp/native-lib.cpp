#include <jni.h>
#include <string>

/*you can use cheat codes from here*/
extern "C" JNIEXPORT jstring JNICALL
Java_com_spring_musk_vappinject_MainActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}