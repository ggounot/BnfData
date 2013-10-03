LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := sqlite-u61
LOCAL_SRC_FILES := sqlite3.c
LOCAL_CFLAGS    := -DSQLITE_ENABLE_FTS4 -DSQLITE_ENABLE_FTS4_UNICODE61

include $(BUILD_STATIC_LIBRARY)

include $(CLEAR_VARS)

LOCAL_MODULE    := sqlite-u61-jni
LOCAL_SRC_FILES := jniquery.c
LOCAL_CFLAGS    := -std=c99
LOCAL_LDLIBS    := -ldl -llog

LOCAL_STATIC_LIBRARIES := sqlite-u61

include $(BUILD_SHARED_LIBRARY)

