-repackageclasses 'a'
-allowaccessmodification
-optimizationpasses 5
-mergeinterfacesaggressively
-renamesourcefileattribute SourceFile

-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
}

-assumevalues class androidx.compose.runtime.ComposerKt {
    public static boolean isTraceInProgress() return false;
}

-assumenosideeffects class androidx.compose.runtime.ComposerKt {
    public static void traceEventStart(int, int, int, java.lang.String);
    public static void traceEventEnd();
    public static void sourceInformation(androidx.compose.runtime.Composer, java.lang.String);
    public static void sourceInformationMarkerStart(androidx.compose.runtime.Composer, int, java.lang.String);
    public static void sourceInformationMarkerEnd(androidx.compose.runtime.Composer);
}
