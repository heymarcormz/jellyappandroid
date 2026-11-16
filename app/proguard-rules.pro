# Firebase Analytics, AppsFlyer and Quantum Metric keep their own consumer rules.
# Reflection is used only to make Quantum Metric optional when Maven credentials are absent.
-keepclassmembers class com.marcormz.jellyapp.analytics.QuantumMetricBridge {
    *;
}
