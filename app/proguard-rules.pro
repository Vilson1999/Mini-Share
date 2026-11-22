# Repackage obfuscated classes into the root default package (drops package prefix in DEX)
-repackageclasses ''
-overloadaggressively
-mergeinterfacesaggressively
-renamesourcefileattribute ""

# Strip all android.util.Log calls, log tags, and format strings in release builds
-assumevalues class android.util.Log {
    public static boolean isLoggable(java.lang.String, int) return false;
}

-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int) return false;
    public static int v(...) return 0;
    public static int d(...) return 0;
    public static int i(...) return 0;
    public static int w(...) return 0;
    public static int e(...) return 0;
    public static int wtf(...) return 0;
    public static int println(...) return 0;
}

# Strip Kotlin compiler-inserted null-check wrappers and parameter name strings
-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    public static void checkExpressionValueIsNotNull(java.lang.Object, java.lang.String);
    public static void checkNotNullExpressionValue(java.lang.Object, java.lang.String);
    public static void checkReturnedValueIsNotNull(java.lang.Object, java.lang.String);
    public static void checkFieldIsNotNull(java.lang.Object, java.lang.String);
    public static void checkParameterIsNotNull(java.lang.Object, java.lang.String);
    public static void checkNotNullParameter(java.lang.Object, java.lang.String);
}

# Strip Kotlin coroutine DebugMetadata lookups and stack-trace recovery helpers
-assumevalues class kotlinx.coroutines.DebugKt {
    public static boolean getASSERTIONS_ENABLED() return false;
    public static boolean getDEBUG() return false;
    public static boolean getRECOVER_STACK_TRACES() return false;
}

-assumenosideeffects class kotlin.coroutines.jvm.internal.DebugMetadataKt {
    public static java.lang.StackTraceElement getStackTraceElement(kotlin.coroutines.jvm.internal.BaseContinuationImpl) return null;
    public static java.lang.String[] getSpilledVariableFieldMapping(kotlin.coroutines.jvm.internal.BaseContinuationImpl) return null;
}

-assumenosideeffects class kotlinx.coroutines.internal.StackTraceRecoveryKt {
    void <clinit>();
}

-assumenosideeffects class _COROUTINE.ArtificialStackFrames {
    public java.lang.StackTraceElement coroutineBoundary();
    public java.lang.StackTraceElement coroutineCreation();
}

# Disable Compose runtime/inspector tracing and strip source-information metadata
-assumevalues class androidx.compose.runtime.ComposerKt {
    public static boolean isTraceInProgress() return false;
}

-assumevalues class androidx.compose.ui.platform.InspectableValueKt {
    public static boolean isDebugInspectorInfoEnabled() return false;
}

-assumenosideeffects class androidx.compose.runtime.ComposerKt {
    public static void traceEventStart(int, int, int, java.lang.String);
    public static void traceEventStart(int, java.lang.String);
    public static void traceEventEnd();
    public static void sourceInformation(androidx.compose.runtime.Composer, java.lang.String);
    public static void sourceInformationMarkerStart(androidx.compose.runtime.Composer, int, java.lang.String);
    public static void sourceInformationMarkerEnd(androidx.compose.runtime.Composer);
}

-assumenosideeffects class androidx.compose.runtime.Trace {
    public java.lang.Object beginSection(java.lang.String);
    public void endSection(java.lang.Object);
}

-assumevalues class androidx.tracing.Trace {
    public static boolean isEnabled() return false;
}

-assumenosideeffects class androidx.tracing.Trace {
    public static boolean isEnabled() return false;
    public static void beginSection(java.lang.String);
    public static void endSection();
    public static void beginAsyncSection(java.lang.String, int);
    public static void endAsyncSection(java.lang.String, int);
    public static void setCounter(java.lang.String, int);
}

-assumevalues class androidx.tracing.TraceApi29Impl {
    public static boolean isEnabled() return false;
}

-assumenosideeffects class androidx.tracing.TraceApi29Impl {
    public static boolean isEnabled() return false;
    public static void beginSection(java.lang.String);
    public static void endSection();
    public static void beginAsyncSection(java.lang.String, int);
    public static void endAsyncSection(java.lang.String, int);
    public static void setCounter(java.lang.String, int);
}

-assumevalues class androidx.core.os.TraceCompat {
    public static boolean isEnabled() return false;
}

-assumenosideeffects class androidx.core.os.TraceCompat {
    public static boolean isEnabled() return false;
    public static void beginSection(java.lang.String);
    public static void endSection();
    public static void beginAsyncSection(java.lang.String, int);
    public static void endAsyncSection(java.lang.String, int);
    public static void setCounter(java.lang.String, int);
}

-assumevalues class android.os.Trace {
    public static boolean isEnabled() return false;
}

-assumenosideeffects class android.os.Trace {
    public static boolean isEnabled() return false;
    public static void beginSection(java.lang.String);
    public static void endSection();
    public static void beginAsyncSection(java.lang.String, int);
    public static void endAsyncSection(java.lang.String, int);
    public static void setCounter(java.lang.String, long);
}
