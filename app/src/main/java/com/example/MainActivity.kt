package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.MainViewModel
import com.example.ui.screens.AIAssistantPanelScreen
import com.example.ui.screens.AboutScreen
import com.example.ui.screens.AccountScreen
import com.example.ui.screens.ApiSettingsScreen
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.ClipboardScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LanguageManagerScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.PermissionScreen
import com.example.ui.screens.PremiumScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.StickerStoreScreen
import com.example.ui.screens.ThemeStoreScreen
import com.example.ui.screens.VoiceTranslatorScreen

sealed class Screen(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Home : Screen("home", "Home", Icons.Filled.Home, Icons.Outlined.Home)
    object Voice : Screen("voice", "Voice", Icons.Filled.Mic, Icons.Outlined.Mic)
    object History : Screen("history", "History", Icons.Filled.History, Icons.Outlined.History)
    object Chat : Screen("chat", "AI Chat", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome)
    object Settings : Screen("settings", "Settings", Icons.Filled.Settings, Icons.Outlined.Settings)

    // Non-tab routes
    object Splash : Screen("splash", "Splash", Icons.Filled.Home, Icons.Outlined.Home)
    object Onboarding : Screen("onboarding", "Onboarding", Icons.Filled.Home, Icons.Outlined.Home)
    object Permission : Screen("permission", "Permission", Icons.Filled.Home, Icons.Outlined.Home)
    object AIAssistant : Screen("ai_assistant", "AI Assistant", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome)
    object Clipboard : Screen("clipboard", "Clipboard", Icons.Filled.Home, Icons.Outlined.Home)
    object ThemeStore : Screen("theme_store", "Themes", Icons.Filled.Home, Icons.Outlined.Home)
    object StickerStore : Screen("sticker_store", "Stickers", Icons.Filled.Home, Icons.Outlined.Home)
    object Languages : Screen("languages", "Languages", Icons.Filled.Home, Icons.Outlined.Home)
    object Account : Screen("account", "Account", Icons.Filled.Home, Icons.Outlined.Home)
    object Premium : Screen("premium", "VIP Pro", Icons.Filled.Home, Icons.Outlined.Home)
    object ApiSettings : Screen("api_settings", "API Config", Icons.Filled.Home, Icons.Outlined.Home)
    object About : Screen("about", "About", Icons.Filled.Home, Icons.Outlined.Home)
}

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            com.example.ui.theme.AIKeyboardTheme {
                MainAppLayout(viewModel)
            }
        }
    }
}

@Composable
fun MainAppLayout(viewModel: MainViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val mainTabs = listOf(
        Screen.Home,
        Screen.Voice,
        Screen.History,
        Screen.Chat,
        Screen.Settings
    )

    val showBottomBar = currentRoute in mainTabs.map { it.route }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    mainTabs.forEach { screen ->
                        val isSelected = currentRoute == screen.route
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                if (currentRoute != screen.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                                    contentDescription = screen.title
                                )
                            },
                            label = { Text(screen.title) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Splash.route) {
                SplashScreen(
                    onContinue = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Onboarding.route) {
                OnboardingScreen(
                    onFinishOnboarding = {
                        navController.navigate(Screen.Permission.route)
                    }
                )
            }
            composable(Screen.Permission.route) {
                PermissionScreen(
                    onPermissionsGranted = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Home.route) {
                HomeScreen(
                    viewModel = viewModel,
                    onNavigateToKeyboardSetup = { navController.navigate(Screen.Permission.route) },
                    onNavigateToThemeStore = { navController.navigate(Screen.ThemeStore.route) },
                    onNavigateToAIAssistant = { navController.navigate(Screen.AIAssistant.route) },
                    onNavigateToTranslate = { navController.navigate(Screen.Chat.route) },
                    onNavigateToClipboard = { navController.navigate(Screen.Clipboard.route) },
                    onNavigateToStickerStore = { navController.navigate(Screen.StickerStore.route) },
                    onNavigateToPremium = { navController.navigate(Screen.Premium.route) },
                    onNavigateToLanguages = { navController.navigate(Screen.Languages.route) }
                )
            }
            composable(Screen.Voice.route) {
                VoiceTranslatorScreen(viewModel = viewModel)
            }
            composable(Screen.History.route) {
                HistoryScreen(viewModel = viewModel)
            }
            composable(Screen.Chat.route) {
                ChatScreen(viewModel = viewModel)
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    viewModel = viewModel,
                    onNavigateToApiSettings = { navController.navigate(Screen.ApiSettings.route) },
                    onNavigateToThemeStore = { navController.navigate(Screen.ThemeStore.route) },
                    onNavigateToLanguages = { navController.navigate(Screen.Languages.route) },
                    onNavigateToAbout = { navController.navigate(Screen.About.route) },
                    onNavigateToPremium = { navController.navigate(Screen.Premium.route) }
                )
            }
            composable(Screen.AIAssistant.route) {
                AIAssistantPanelScreen(viewModel = viewModel)
            }
            composable(Screen.Clipboard.route) {
                ClipboardScreen(viewModel = viewModel)
            }
            composable(Screen.ThemeStore.route) {
                ThemeStoreScreen(viewModel = viewModel)
            }
            composable(Screen.StickerStore.route) {
                StickerStoreScreen(viewModel = viewModel)
            }
            composable(Screen.Languages.route) {
                LanguageManagerScreen(viewModel = viewModel)
            }
            composable(Screen.Account.route) {
                AccountScreen(viewModel = viewModel)
            }
            composable(Screen.Premium.route) {
                PremiumScreen()
            }
            composable(Screen.ApiSettings.route) {
                ApiSettingsScreen(viewModel = viewModel)
            }
            composable(Screen.About.route) {
                AboutScreen()
            }
        }
    }
}
