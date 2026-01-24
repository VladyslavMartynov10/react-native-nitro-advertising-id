#include <jni.h>
#include "nitroadvertisingidOnLoad.hpp"

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void*) {
  return margelo::nitro::nitroadvertisingid::initialize(vm);
}
