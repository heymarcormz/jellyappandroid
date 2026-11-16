package com.marcormz.jellyapp.analytics

import android.app.Application
import android.util.Log
import com.marcormz.jellyapp.BuildConfig

/**
 * Quantum Metric is resolved through its official SDK API. Reflection keeps the sample
 * buildable while its authenticated Maven repository is not configured.
 *
 * Android integration reference:
 * https://community.quantummetric.com/s/article/Android-Integration-Instructions
 */
class QuantumMetricBridge(private val application: Application) {
    @Volatile
    private var initialized = false

    fun initialize() {
        if (!BuildConfig.QM_CONFIGURED) {
            Log.i(TAG, "Quantum Metric omitido: faltan QM_SUBSCRIPTION, QM_UID o credenciales Maven.")
            return
        }

        runCatching {
            val quantumMetric = Class.forName("com.quantummetric.QuantumMetric")
            val initialize = quantumMetric.getMethod(
                "initialize",
                String::class.java,
                String::class.java,
                Application::class.java,
            )
            var builder = initialize.invoke(
                null,
                BuildConfig.QM_SUBSCRIPTION,
                BuildConfig.QM_UID,
                application,
            ) ?: error("QuantumMetric.initialize devolvió null")

            builder = invokeOptional(builder, "withBrowserName", "JellyApp QA")
            if (BuildConfig.QM_TEST_MODE) {
                builder = invokeOptional(builder, "enableTestMode")
            }

            builder.javaClass.getMethod("start").invoke(builder)
            initialized = true
            Log.i(TAG, "Quantum Metric inicializado.")
        }.onFailure { error ->
            initialized = false
            Log.w(TAG, "No se pudo inicializar Quantum Metric.", error)
        }
    }

    private fun invokeOptional(target: Any, methodName: String, vararg args: Any): Any {
        return runCatching {
            val method = target.javaClass.methods.firstOrNull {
                it.name == methodName && it.parameterTypes.size == args.size
            } ?: return target

            method.invoke(target, *args) ?: target
        }.getOrDefault(target)
    }

    companion object {
        private const val TAG = "JellyAnalytics"
    }
}
