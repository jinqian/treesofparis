package com.treesofparis.navigation

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.treesofparis.TreesApp
import com.treesofparis.core.designsystem.theme.TreesTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/** Navigation behaviour with stub screens: no Hilt, database or map needed. */
@RunWith(RobolectricTestRunner::class)
class TreesAppTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    private val app = @androidx.compose.runtime.Composable {
        TreesTheme {
            TreesApp(
                mapContent = { key -> Text("map tree=${key.treeId}") },
                favoritesContent = { onTreeClick, onExploreMap ->
                    Column {
                        Button(onClick = { onTreeClick(42) }) { Text("open 42") }
                        Button(onClick = onExploreMap) { Text("explore") }
                    }
                },
                aboutContent = { Text("about screen") },
            )
        }
    }

    @Test
    fun bottomBar_showsThreeFrenchTabs() {
        compose.setContent(app)
        compose.onNodeWithText("Carte").assertIsDisplayed().assertIsSelected()
        compose.onNodeWithText("Favoris").assertIsDisplayed()
        compose.onNodeWithText("À propos").assertIsDisplayed()
        compose.onNodeWithText("map tree=null").assertIsDisplayed()
    }

    @Test
    fun selectingFavoris_showsFavoritesScreen() {
        compose.setContent(app)
        compose.onNodeWithText("Favoris").performClick()
        compose.onNodeWithText("open 42").assertIsDisplayed()
        compose.onNodeWithText("Favoris").assertIsSelected()
    }

    @Test
    fun favoriteClick_navigatesToCarteTabWithTree() {
        compose.setContent(app)
        compose.onNodeWithText("Favoris").performClick()
        compose.onNodeWithText("open 42").performClick()
        compose.onNodeWithText("map tree=42").assertIsDisplayed()
        compose.onNodeWithText("Carte").assertIsSelected()
    }

    @Test
    fun exploreMap_fromFavoris_selectsCarte() {
        compose.setContent(app)
        compose.onNodeWithText("Favoris").performClick()
        compose.onNodeWithText("explore").performClick()
        compose.onNodeWithText("map tree=null").assertIsDisplayed()
        compose.onNodeWithText("Carte").assertIsSelected()
    }

    @Test
    fun back_fromAbout_returnsToCarte() {
        compose.setContent(app)
        compose.onNodeWithText("À propos").performClick()
        compose.onNodeWithText("about screen").assertIsDisplayed()
        compose.runOnUiThread { compose.activity.onBackPressedDispatcher.onBackPressed() }
        compose.onNodeWithText("map tree=null").assertIsDisplayed()
        compose.onNodeWithText("Carte").assertIsSelected()
    }

    @Test
    fun tabState_survivesSaveAndRestore() {
        val restoration = StateRestorationTester(compose)
        restoration.setContent(app)
        compose.onNodeWithText("Favoris").performClick()
        restoration.emulateSavedInstanceStateRestore()
        compose.onNodeWithText("open 42").assertIsDisplayed()
    }
}
