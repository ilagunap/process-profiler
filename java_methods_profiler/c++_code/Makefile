
# #####################################################
# IMPORTANT:
# Modify these variables
# #####################################################

JNI_PATH=/projects/java/jdk1.6.0_25/include

# To find jawt_md.h and jni_md.h
JNI_LINUX_HEADERS=$(JNI_PATH)/linux

# #####################################################

CXX=g++
CFLAGS=-Wall -g -O2 -fPIC

all: com_ilaguna_metrics_ProcessInfo.o
	$(CXX) -fPIC com_ilaguna_metrics_ProcessInfo.o -shared -o libprocessinfo.so

com_ilaguna_metrics_ProcessInfo.o: com_ilaguna_metrics_ProcessInfo.C com_ilaguna_metrics_ProcessInfo.h
	$(CXX) $(CFLAGS) -I$(JNI_PATH) -I$(JNI_LINUX_HEADERS) -c com_ilaguna_metrics_ProcessInfo.C

clean:
	rm -f *.o libprocessinfo.so
