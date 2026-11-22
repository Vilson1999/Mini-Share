# Repackage obfuscated classes into the root default package (drops package prefix in DEX)
-repackageclasses ''
-mergeinterfacesaggressively
-renamesourcefileattribute ""

# Strip all android.util.Log calls, log tags, and format strings in release builds
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
    public static int w(...);
    public static int e(...);
    public static int wtf(...);
    public static int println(...);
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

# Disable Compose runtime/inspector tracing and strip source-information metadata
-assumevalues class androidx.compose.runtime.ComposerKt {
    public static boolean isTraceInProgress() return false;
}

-assumevalues class androidx.compose.ui.platform.InspectableValueKt {
    public static boolean isDebugInspectorInfoEnabled() return false;
}

-assumenosideeffects class androidx.compose.runtime.ComposerKt {
    public static void traceEventStart(int, int, int, java.lang.String);
    public static void traceEventEnd();
    public static void sourceInformation(androidx.compose.runtime.Composer, java.lang.String);
    public static void sourceInformationMarkerStart(androidx.compose.runtime.Composer, int, java.lang.String);
    public static void sourceInformationMarkerEnd(androidx.compose.runtime.Composer);
}

-assumenosideeffects class androidx.compose.runtime.Trace {
    public java.lang.Object beginSection(java.lang.String);
    public void endSection(java.lang.Object);
}

-assumenosideeffects class androidx.tracing.Trace {
    public static boolean isEnabled();
    public static void beginSection(java.lang.String);
    public static void endSection();
    public static void beginAsyncSection(java.lang.String, int);
    public static void endAsyncSection(java.lang.String, int);
    public static void setCounter(java.lang.String, int);
}

-assumenosideeffects class android.os.Trace {
    public static boolean isEnabled();
    public static void beginSection(java.lang.String);
    public static void endSection();
    public static void beginAsyncSection(java.lang.String, int);
    public static void endAsyncSection(java.lang.String, int);
    public static void setCounter(java.lang.String, long);
}
