#include "com_ilaguna_metrics_ProcessInfo.h"

#include <jni.h>
#include <sys/types.h>
#include <unistd.h>


JNIEXPORT jstring JNICALL
Java_com_ilaguna_metrics_ProcessInfo_getPid(JNIEnv *env, jobject obj)
{
	int pid = (int)getpid();
	char buff[256];
	sprintf(buff, "%d", pid);
	return env->NewStringUTF(buff);
}
