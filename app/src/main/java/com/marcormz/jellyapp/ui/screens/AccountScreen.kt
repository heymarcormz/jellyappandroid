package com.marcormz.jellyapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.marcormz.jellyapp.analytics.AnalyticsInput
import com.marcormz.jellyapp.ui.JellyUiState
import com.marcormz.jellyapp.ui.components.JellyCard
import com.marcormz.jellyapp.ui.components.JellyPrimaryButton
import com.marcormz.jellyapp.ui.components.JellySecondaryButton
import com.marcormz.jellyapp.ui.components.JellyTextField
import com.marcormz.jellyapp.ui.components.JellyTopBar
import com.marcormz.jellyapp.ui.components.LocalJellyAnalytics
import com.marcormz.jellyapp.ui.components.SectionEyebrow
import com.marcormz.jellyapp.ui.components.StatusBanner
import com.marcormz.jellyapp.ui.theme.JellyCream
import com.marcormz.jellyapp.ui.theme.JellyMuted

private data class EventParameterInput(
    val id: Long,
    val name: String = "",
    val value: String = "",
)

private val EventParametersSaver = listSaver<List<EventParameterInput>, String>(
    save = { list ->
        list.flatMap { listOf(it.id.toString(), it.name, it.value) }
    },
    restore = { saved ->
        saved.chunked(3).mapNotNull { values ->
            if (values.size == 3) {
                EventParameterInput(
                    id = values[0].toLongOrNull() ?: return@mapNotNull null,
                    name = values[1],
                    value = values[2],
                )
            } else {
                null
            }
        }
    },
)

@Composable
fun AccountScreen(
    state: JellyUiState,
    onLogin: (String) -> Unit,
    onLogout: () -> Unit,
) {
    val analytics = LocalJellyAnalytics.current

    var userName by rememberSaveable { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loginMessage by remember { mutableStateOf<String?>(null) }

    var propertyName by rememberSaveable { mutableStateOf("") }
    var propertyValue by rememberSaveable { mutableStateOf("") }
    var propertyAttempted by rememberSaveable { mutableStateOf(false) }
    var propertyMessage by remember { mutableStateOf<String?>(null) }

    var eventName by rememberSaveable { mutableStateOf("") }
    var parameters by rememberSaveable(stateSaver = EventParametersSaver) {
        mutableStateOf(listOf(EventParameterInput(id = 0)))
    }
    var nextParameterId by rememberSaveable { mutableLongStateOf(1L) }
    var eventAttempted by rememberSaveable { mutableStateOf(false) }
    var eventMessage by remember { mutableStateOf<String?>(null) }

    val propertyNameError = if (propertyAttempted) {
        AnalyticsInput.validateUserPropertyName(propertyName)
    } else {
        null
    }
    val propertyValueError = if (propertyAttempted) {
        AnalyticsInput.validateUserPropertyValue(propertyValue)
    } else {
        null
    }

    val eventNameError = if (eventAttempted) {
        AnalyticsInput.validateEventName(eventName)
    } else {
        null
    }
    val parameterNameErrors = if (eventAttempted) {
        parameters.map { AnalyticsInput.validateParameterName(it.name) }
    } else {
        emptyList()
    }
    val parameterValueErrors = if (eventAttempted) {
        parameters.map { AnalyticsInput.validateParameterValue(it.value) }
    } else {
        emptyList()
    }
    val hasDuplicateParameters = eventAttempted &&
        parameters.map { it.name.trim() }.let { names -> names.distinct().size != names.size }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
    ) {
        JellyTopBar(title = "Test lab")
        Spacer(Modifier.height(20.dp))
        SectionEyebrow(index = "01", text = "Cuenta simulada")
        Spacer(Modifier.height(10.dp))
        Text(
            text = "Cualquier llave\nfunciona.",
            style = MaterialTheme.typography.headlineLarge,
            color = JellyCream,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Introduce lo que quieras. No hay autenticación real y la contraseña nunca se mide.",
            style = MaterialTheme.typography.bodyMedium,
            color = JellyMuted,
        )

        Spacer(Modifier.height(18.dp))
        JellyCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (state.loggedIn) {
                    StatusBanner(
                        message = "Sesión simulada: ${state.displayName}. User ID de prueba: ${state.activeUserId ?: "No activo"}.",
                        success = true,
                    )
                }
                JellyTextField(
                    value = userName,
                    onValueChange = { userName = it },
                    label = "Usuario",
                    placeholder = "cualquier usuario",
                    modifier = Modifier.testTag("input_login_username"),
                )
                JellyTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = "Contraseña",
                    placeholder = "cualquier contraseña",
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.testTag("input_login_password"),
                )
                JellyPrimaryButton(
                    text = "Iniciar sesión",
                    buttonId = "btn_login_submit",
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        onLogin(userName)
                        password = ""
                        loginMessage = "Login aceptado sin validación. Se registró el evento login."
                    },
                )
                if (state.loggedIn) {
                    JellySecondaryButton(
                        text = "Cerrar sesión",
                        buttonId = "btn_logout_submit",
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            onLogout()
                            userName = ""
                            password = ""
                            loginMessage = "Sesión cerrada. Logout enviado con el User ID anterior; ahora user_id = null."
                        },
                    )
                }
                loginMessage?.let {
                    StatusBanner(message = it, success = true)
                }
            }
        }

        Spacer(Modifier.height(30.dp))
        SectionEyebrow(index = "02", text = "User property")
        Spacer(Modifier.height(10.dp))
        Text(
            text = "Añade contexto\nal usuario.",
            style = MaterialTheme.typography.headlineMedium,
            color = JellyCream,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Se envía con setUserProperty en Firebase / GA4.",
            style = MaterialTheme.typography.bodyMedium,
            color = JellyMuted,
        )
        Spacer(Modifier.height(14.dp))
        JellyCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                JellyTextField(
                    value = propertyName,
                    onValueChange = {
                        propertyName = it
                        propertyMessage = null
                    },
                    label = "Parámetro",
                    placeholder = "plan_prueba",
                    isError = propertyNameError != null,
                    supportingText = propertyNameError,
                    modifier = Modifier.testTag("input_user_property_name"),
                )
                JellyTextField(
                    value = propertyValue,
                    onValueChange = {
                        propertyValue = it
                        propertyMessage = null
                    },
                    label = "Valor",
                    placeholder = "beta",
                    isError = propertyValueError != null,
                    supportingText = propertyValueError,
                    modifier = Modifier.testTag("input_user_property_value"),
                )
                JellyPrimaryButton(
                    text = "Enviar propiedad",
                    buttonId = "btn_user_property_send",
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        propertyAttempted = true
                        val nameError = AnalyticsInput.validateUserPropertyName(propertyName)
                        val valueError = AnalyticsInput.validateUserPropertyValue(propertyValue)
                        if (nameError == null && valueError == null) {
                            analytics?.setUserProperty(propertyName.trim(), propertyValue.trim())
                            propertyMessage = "Propiedad ${propertyName.trim()} enviada a Firebase."
                        } else {
                            propertyMessage = null
                        }
                    },
                )
                propertyMessage?.let {
                    StatusBanner(message = it, success = true)
                }
            }
        }

        Spacer(Modifier.height(30.dp))
        SectionEyebrow(index = "03", text = "Custom event")
        Spacer(Modifier.height(10.dp))
        Text(
            text = "Inventa una\nseñal.",
            style = MaterialTheme.typography.headlineMedium,
            color = JellyCream,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Se envía a Firebase / GA4 y AppsFlyer. Quantum Metric mantiene su captura automática y eventos configurados en su plataforma.",
            style = MaterialTheme.typography.bodyMedium,
            color = JellyMuted,
        )
        Spacer(Modifier.height(14.dp))
        JellyCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                JellyTextField(
                    value = eventName,
                    onValueChange = {
                        eventName = it
                        eventMessage = null
                    },
                    label = "Nombre del evento",
                    placeholder = "prueba_jelly",
                    isError = eventNameError != null,
                    supportingText = eventNameError,
                    modifier = Modifier.testTag("input_custom_event_name"),
                )

                parameters.forEachIndexed { index, parameter ->
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "PARÁMETRO ${(index + 1).toString().padStart(2, '0')}",
                                style = MaterialTheme.typography.labelSmall,
                                color = JellyMuted,
                            )
                            if (parameters.size > 1) {
                                JellySecondaryButton(
                                    text = "Quitar",
                                    buttonId = "btn_custom_event_remove_parameter_${parameter.id}",
                                    onClick = {
                                        parameters = parameters.filterNot { it.id == parameter.id }
                                        eventMessage = null
                                    },
                                )
                            }
                        }
                        JellyTextField(
                            value = parameter.name,
                            onValueChange = { newValue ->
                                parameters = parameters.map {
                                    if (it.id == parameter.id) it.copy(name = newValue) else it
                                }
                                eventMessage = null
                            },
                            label = "Parámetro",
                            placeholder = "origen",
                            isError = parameterNameErrors.getOrNull(index) != null,
                            supportingText = parameterNameErrors.getOrNull(index),
                            modifier = Modifier.testTag(
                                "input_custom_event_param_key_${parameter.id}",
                            ),
                        )
                        JellyTextField(
                            value = parameter.value,
                            onValueChange = { newValue ->
                                parameters = parameters.map {
                                    if (it.id == parameter.id) it.copy(value = newValue) else it
                                }
                                eventMessage = null
                            },
                            label = "Valor",
                            placeholder = "jellyapp",
                            isError = parameterValueErrors.getOrNull(index) != null,
                            supportingText = parameterValueErrors.getOrNull(index),
                            modifier = Modifier.testTag(
                                "input_custom_event_param_value_${parameter.id}",
                            ),
                        )
                    }
                }

                if (hasDuplicateParameters) {
                    StatusBanner(
                        message = "Los nombres de los parámetros deben ser distintos.",
                        success = false,
                    )
                }

                JellySecondaryButton(
                    text = if (parameters.size >= 10) "Máximo 10 parámetros" else "+ Añadir parámetro",
                    buttonId = "btn_custom_event_add_parameter",
                    modifier = Modifier.fillMaxWidth(),
                    enabled = parameters.size < 10,
                    onClick = {
                        parameters = parameters + EventParameterInput(id = nextParameterId)
                        nextParameterId += 1
                        eventMessage = null
                    },
                )
                JellyPrimaryButton(
                    text = "Enviar custom event",
                    buttonId = "btn_custom_event_send",
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        eventAttempted = true
                        val nameError = AnalyticsInput.validateEventName(eventName)
                        val validParameters = parameters.all {
                            AnalyticsInput.validateParameterName(it.name) == null &&
                                AnalyticsInput.validateParameterValue(it.value) == null
                        }
                        val uniqueNames = parameters.map { it.name.trim() }.distinct().size == parameters.size
                        if (nameError == null && validParameters && uniqueNames) {
                            analytics?.trackCustomEvent(
                                name = eventName.trim(),
                                parameters = parameters.associate { it.name.trim() to it.value.trim() },
                            )
                            eventMessage = "Evento ${eventName.trim()} enviado con ${parameters.size} parámetro(s)."
                        } else {
                            eventMessage = null
                        }
                    },
                )
                eventMessage?.let {
                    StatusBanner(message = it, success = true)
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        Text(
            text = "Los nombres se validan con las reglas de GA4 antes de enviarse. Los valores de los formularios nunca se incluyen en eventos automáticos.",
            style = MaterialTheme.typography.bodySmall,
            color = JellyMuted,
        )
        Spacer(Modifier.height(28.dp))
    }
}
