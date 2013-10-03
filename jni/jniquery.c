#include <jni.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "sqlite3.h"

/* Cached classes. */
static jclass j_string_class = NULL;
static jclass j_mc_class = NULL;

/* Cached method IDs. */
static jmethodID j_mc_constructor;
static jmethodID j_mc_add_row;

jclass lookup_class(JNIEnv *env, const char *name)
{
    jclass class_local_ref = (*env)->FindClass(env, name);
    if (class_local_ref == NULL)
        return NULL;
    jclass class_global_ref = (*env)->NewGlobalRef(env, class_local_ref);
    (*env)->DeleteLocalRef(env, class_local_ref);

    return class_global_ref;
}

/*
 * boolean nativeInit()
 *
 * Lookup and cache classes and methods.
 */
JNIEXPORT jboolean JNICALL Java_eu_gounot_bnfdata_provider_SuggestionsProvider_nativeInit
  (JNIEnv *env, jobject j_this)
{
    j_string_class = lookup_class(env, "java/lang/String");
    if (j_string_class == NULL)
        return JNI_FALSE;

    j_mc_class = lookup_class(env, "android/database/MatrixCursor");
    if (j_mc_class == NULL)
        return JNI_FALSE;

    j_mc_constructor = (*env)->GetMethodID(env, j_mc_class, "<init>", "([Ljava/lang/String;)V");
    if (j_mc_constructor == NULL)
        return JNI_FALSE;

    j_mc_add_row = (*env)->GetMethodID(env, j_mc_class, "addRow", "([Ljava/lang/Object;)V");
    if (j_mc_add_row == NULL)
        return JNI_FALSE;

    return JNI_TRUE;
}

sqlite3 *open_db(JNIEnv *env, jstring j_filename)
{
    sqlite3 *db;

    const char *filename = (*env)->GetStringUTFChars(env, j_filename, NULL);
    if (filename == NULL)
        return NULL;

    int ret = sqlite3_open(filename, &db);

    (*env)->ReleaseStringUTFChars(env, j_filename, filename);

    return (ret == SQLITE_OK) ? db : NULL;
}

sqlite3_stmt *prepare_stmt(JNIEnv *env, sqlite3 *db, jstring j_sql)
{
    sqlite3_stmt *stmt;

    const char *sql = (*env)->GetStringUTFChars(env, j_sql, NULL);
    if (sql == NULL)
        return NULL;

    int ret = sqlite3_prepare_v2(db, sql, strlen(sql) + 1, &stmt, NULL);

    (*env)->ReleaseStringUTFChars(env, j_sql, sql);

    return (ret == SQLITE_OK) ? stmt : NULL;
}

jobjectArray create_col_array(JNIEnv *env, sqlite3_stmt *stmt)
{
    int col_count = sqlite3_column_count(stmt);

    jobjectArray j_col_array = (*env)->NewObjectArray(env, col_count, j_string_class, NULL);
    if (j_col_array == NULL)
        return NULL;

    for (int i = 0; i < col_count; i++) {
        const char *col_name = sqlite3_column_name(stmt, i);
        if (col_name == NULL)
            return NULL;

        jstring j_col_name = (*env)->NewStringUTF(env, col_name);
        if (j_col_name == NULL)
            return NULL;

        (*env)->SetObjectArrayElement(env, j_col_array, i, j_col_name);
        if ((*env)->ExceptionCheck(env))
            return NULL;

        (*env)->DeleteLocalRef(env, j_col_name);
    }

    return j_col_array;
}

int bind_stmt_values(JNIEnv *env, sqlite3_stmt *stmt, jobjectArray j_values)
{
    jsize j_values_count = (*env)->GetArrayLength(env, j_values);

    for (int i = 0; i < j_values_count; i++) {
        jstring j_value = (jstring) (*env)->GetObjectArrayElement(env, j_values, i);
        if ((*env)->ExceptionCheck(env))
            return 0;

        const char *value = (*env)->GetStringUTFChars(env, j_value, NULL);
        if (value == NULL)
            return 0;

        int ret = sqlite3_bind_text(stmt, i + 1, value, strlen(value), SQLITE_TRANSIENT);

        (*env)->ReleaseStringUTFChars(env, j_value, value);

        if (ret != SQLITE_OK)
            return 0;
    }

    return 1;
}

jobject exec_query(JNIEnv *env, sqlite3_stmt *stmt, jobjectArray j_col_array)
{
    jobject j_mc = (*env)->NewObject(env, j_mc_class, j_mc_constructor, j_col_array);
    if (j_mc == NULL)
        return NULL;

    jsize j_col_count = (*env)->GetArrayLength(env, j_col_array);

    while (sqlite3_step(stmt) == SQLITE_ROW) {
        jobjectArray j_row = (*env)->NewObjectArray(env, j_col_count, j_string_class, NULL);
        if (j_row == NULL)
            return NULL;

        for (int i = 0; i < j_col_count; i++) {
            const unsigned char *val = sqlite3_column_text(stmt, i);

            jstring j_val = (*env)->NewStringUTF(env, val);
            if (j_val == NULL)
                return NULL;

            (*env)->SetObjectArrayElement(env, j_row, i, j_val);
            if ((*env)->ExceptionCheck(env))
                return NULL;

            (*env)->DeleteLocalRef(env, j_val);
        }

        (*env)->CallVoidMethod(env, j_mc, j_mc_add_row, j_row);
        if ((*env)->ExceptionCheck(env))
            return NULL;

        (*env)->DeleteLocalRef(env, j_row);
    }

    return j_mc;
}

/*
 * Cursor sqliteU61Query(String filename, String sql, String[] values)
 *
 * Query an SQLite database and return the results in a Cursor.
 */
JNIEXPORT jobject JNICALL Java_eu_gounot_bnfdata_provider_SuggestionsProvider_sqliteU61Query
  (JNIEnv *env, jobject j_this, jstring j_filename, jstring j_sql, jobjectArray j_values)
{
    sqlite3 *db;
    sqlite3_stmt *stmt = NULL;
    jobjectArray j_col_array;
    jobject j_cursor = NULL;

    if ((db = open_db(env, j_filename)) == NULL)
        goto finish;

    if ((stmt = prepare_stmt(env, db, j_sql)) == NULL)
        goto finish;

    if ((j_col_array = create_col_array(env, stmt)) == NULL)
        goto finish;

    if (!bind_stmt_values(env, stmt, j_values))
        goto finish;

    j_cursor = exec_query(env, stmt, j_col_array);

finish:

    sqlite3_finalize(stmt);
    sqlite3_close(db);

    return j_cursor;
}

