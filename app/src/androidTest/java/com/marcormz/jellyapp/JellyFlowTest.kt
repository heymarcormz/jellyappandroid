package com.marcormz.jellyapp

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import org.junit.Rule
import org.junit.Test

class JellyFlowTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun homeExposesStableResourceIds() {
        composeRule.onNodeWithTag("btn_home_start_shopping").assertIsDisplayed()
        composeRule.onNodeWithTag("btn_nav_home").assertExists()
        composeRule.onNodeWithTag("btn_nav_products").assertExists()
        composeRule.onNodeWithTag("btn_nav_cart").assertExists()
        composeRule.onNodeWithTag("btn_nav_account").assertExists()
        composeRule.onNodeWithText("No activo").assertExists()
    }

    @Test
    fun completePurchaseReachesThankYou() {
        composeRule.onNodeWithTag("btn_nav_products").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("btn_product_add_JELLY_001").performClick()
        composeRule.onNodeWithTag("btn_nav_cart").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("btn_cart_checkout").assertIsDisplayed().performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("btn_checkout_purchase")
            .assertIsDisplayed()
            .performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText("Pedido\nregistrado.").assertExists()
        composeRule.onNodeWithTag("btn_thankyou_home").assertExists()
    }

    @Test
    fun outOfStockProductShowsBackendErrorAndNeverChangesCart() {
        composeRule.onNodeWithTag("btn_nav_products").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("products_list")
            .performScrollToNode(hasTestTag("btn_product_add_JELLY_006"))
        composeRule.onNodeWithTag("btn_product_add_JELLY_006").performClick()

        composeRule.waitUntil(timeoutMillis = 20_000) {
            composeRule.onAllNodesWithTag("stock_error_JELLY_006")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeRule.onNodeWithTag("stock_error_JELLY_006").assertExists()

        composeRule.onNodeWithTag("btn_nav_cart").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("btn_cart_empty_products").assertExists()
    }

    @Test
    fun productDetailCanAddAndOpenCart() {
        composeRule.onNodeWithTag("btn_nav_products").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("btn_product_open_JELLY_001").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText("Auriculares Nébula").assertExists()
        composeRule.onNodeWithTag("btn_detail_add_JELLY_001")
            .performScrollTo()
            .performClick()
        composeRule.onNodeWithTag("btn_detail_view_cart")
            .performScrollTo()
            .performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("btn_cart_checkout").assertIsDisplayed()
    }

    @Test
    fun loginAndCustomFormsWorkWithoutValidationOrCredentials() {
        composeRule.onNodeWithTag("btn_nav_account").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("User ID de prueba:", substring = true).assertDoesNotExist()

        composeRule.onNodeWithTag("input_login_username")
            .performScrollTo()
            .performTextInput("cualquier_usuario")
        composeRule.onNodeWithTag("input_login_password")
            .performScrollTo()
            .performTextInput("cualquier_clave")
        composeRule.onNodeWithTag("btn_login_submit")
            .performScrollTo()
            .performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText(
            "Login aceptado sin validación. Se registró el evento login.",
        ).assertExists()
        composeRule.onNodeWithText(
            "User ID de prueba: cualquier_usuario.",
            substring = true,
        ).assertExists()

        composeRule.onNodeWithTag("input_user_property_name")
            .performScrollTo()
            .performTextInput("plan_prueba")
        composeRule.onNodeWithTag("input_user_property_value")
            .performScrollTo()
            .performTextInput("beta")
        composeRule.onNodeWithTag("btn_user_property_send")
            .performScrollTo()
            .performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Propiedad plan_prueba enviada a Firebase.").assertExists()

        composeRule.onNodeWithTag("input_custom_event_name")
            .performScrollTo()
            .performTextInput("prueba_jelly")
        composeRule.onNodeWithTag("input_custom_event_param_key_0")
            .performScrollTo()
            .performTextInput("origen")
        composeRule.onNodeWithTag("input_custom_event_param_value_0")
            .performScrollTo()
            .performTextInput("android")
        composeRule.onNodeWithTag("btn_custom_event_send")
            .performScrollTo()
            .performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Evento prueba_jelly enviado con 1 parámetro(s).").assertExists()

        composeRule.onNodeWithTag("btn_logout_submit")
            .performScrollTo()
            .performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText(
            "Sesión cerrada. Logout enviado con el User ID anterior; ahora user_id = null.",
        ).assertExists()
        composeRule.onNodeWithText("User ID de prueba:", substring = true).assertDoesNotExist()
        composeRule.onNodeWithTag("btn_login_submit").assertExists()
    }
}
