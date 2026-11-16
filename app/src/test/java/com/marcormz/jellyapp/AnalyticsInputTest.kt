package com.marcormz.jellyapp

import com.marcormz.jellyapp.analytics.AnalyticsInput
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class AnalyticsInputTest {
    @Test
    fun acceptsValidPropertyAndEventNames() {
        assertNull(AnalyticsInput.validateUserPropertyName("plan_prueba"))
        assertNull(AnalyticsInput.validateUserPropertyValue("beta"))
        assertNull(AnalyticsInput.validateEventName("jelly_custom_event"))
        assertNull(AnalyticsInput.validateParameterName("origen"))
        assertNull(AnalyticsInput.validateParameterValue("android"))
    }

    @Test
    fun rejectsNamesOutsideGa4Rules() {
        assertNotNull(AnalyticsInput.validateUserPropertyName(""))
        assertNotNull(AnalyticsInput.validateUserPropertyName("1_empieza_con_numero"))
        assertNotNull(AnalyticsInput.validateUserPropertyName("con espacio"))
        assertNotNull(AnalyticsInput.validateUserPropertyName("a".repeat(25)))
        assertNotNull(AnalyticsInput.validateEventName("evento-con-guiones"))
        assertNotNull(AnalyticsInput.validateEventName("e".repeat(41)))
        assertNotNull(AnalyticsInput.validateParameterValue("x".repeat(101)))
    }
}
