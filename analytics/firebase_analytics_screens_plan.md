# Firebase Analytics screen tracking

All user-facing Compose destinations are tracked manually. Automatic Activity screen
reporting is disabled in `AndroidManifest.xml` to avoid duplicate `screen_view` events.

| Task | Done | Screen name | Description | Entry point |
| --- | --- | --- | --- | --- |
| 1 | [x] | `home` | Landing and QA flow overview | `HomeScreen.kt` |
| 2 | [x] | `products` | Five-product catalog and `view_item_list` | `ProductsScreen.kt` |
| 3 | [x] | `product_detail` | Product detail and `view_item` | `ProductDetailScreen.kt` |
| 4 | [x] | `cart` | Cart, quantity changes and `view_cart` | `CartScreen.kt` |
| 5 | [x] | `checkout` | Simulated checkout and `begin_checkout` | `CheckoutScreen.kt` |
| 6 | [x] | `thank_you` | Purchase confirmation | `ThankYouScreen.kt` |
| 7 | [x] | `webview` | Remote page at `https://marcormz.com/jellyapp/` | `WebViewScreen.kt` |
| 8 | [x] | `account` | Login without validation, user properties and custom events | `AccountScreen.kt` |

Central tracking implementation: `ui/JellyApp.kt` (`LaunchedEffect(currentRoute)`) and
`analytics/FirebaseAppsFlyerAnalytics.kt`.
