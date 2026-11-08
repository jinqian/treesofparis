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
                mapContent = { key -> Text("map tree=${key.treeId} genus=${key.genus}") },
                favoritesContent = { onTreeClick, onExploreMap ->
                    Column {
                        Button(onClick = { onTreeClick(42) }) { Text("open 42") }
                        Button(onClick = onExploreMap) { Text("explore") }
                    }
                },
                herbierContent = { onGenusClick ->
                    Column {
                        Text("herbier screen")
                        Button(onClick = { onGenusClick("Tilia") }) { Text("tilleul") }
                    }
                },
                aboutContent = { onOpenLibraries, onOpenFloweringSources ->
                    Column {
                        Text("about screen")
                        Button(onClick = onOpenLibraries) { Text("libraries") }
                        Button(onClick = onOpenFloweringSources) { Text("flowering sources") }
                    }
                },
                librariesContent = { onBack ->
                    Column {
                        Text("libraries screen")
                        Button(onClick = onBack) { Text("back") }
                    }
                },
                floweringSourcesContent = { onBack ->
                    Column {
                        Text("flowering sources screen")
                        Button(onClick = onBack) { Text("back") }
                    }
                },
            )
        }
    }

    @Test
    fun bottomBar_showsFourFrenchTabs() {
        compose.setContent(app)
        compose.onNodeWithText("Carte").assertIsDisplayed().assertIsSelected()
        compose.onNodeWithText("Herbier").assertIsDisplayed()
        compose.onNodeWithText("Favoris").assertIsDisplayed()
        compose.onNodeWithText("À propos").assertIsDisplayed()
        compose.onNodeWithText("map tree=null genus=null").assertIsDisplayed()
    }

    @Test
    fun selectingHerbier_showsHerbierScreen() {
        compose.setContent(app)
        compose.onNodeWithText("Herbier").performClick()
        compose.onNodeWithText("herbier screen").assertIsDisplayed()
        compose.onNodeWithText("Herbier").assertIsSelected()
    }

    @Test
    fun genusFromHerbier_opensTheMapFilteredToIt() {
        compose.setContent(app)
        compose.onNodeWithText("Herbier").performClick()
        compose.onNodeWithText("tilleul").performClick()
        compose.onNodeWithText("map tree=null genus=Tilia").assertIsDisplayed()
        compose.onNodeWithText("Carte").assertIsSelected()
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
        compose.onNodeWithText("map tree=42 genus=null").assertIsDisplayed()
        compose.onNodeWithText("Carte").assertIsSelected()
    }

    @Test
    fun exploreMap_fromFavoris_selectsCarte() {
        compose.setContent(app)
        compose.onNodeWithText("Favoris").performClick()
        compose.onNodeWithText("explore").performClick()
        compose.onNodeWithText("map tree=null genus=null").assertIsDisplayed()
        compose.onNodeWithText("Carte").assertIsSelected()
    }

    @Test
    fun libraries_openFromAbout_andBackReturnsToAbout() {
        compose.setContent(app)
        compose.onNodeWithText("À propos").performClick()
        compose.onNodeWithText("libraries").performClick()
        compose.onNodeWithText("libraries screen").assertIsDisplayed()
        compose.onNodeWithText("À propos").assertIsSelected()
        compose.onNodeWithText("back").performClick()
        compose.onNodeWithText("about screen").assertIsDisplayed()
    }

    @Test
    fun floweringSources_openFromAbout_andBackReturnsToAbout() {
        compose.setContent(app)
        compose.onNodeWithText("À propos").performClick()
        compose.onNodeWithText("flowering sources").performClick()
        compose.onNodeWithText("flowering sources screen").assertIsDisplayed()
        compose.onNodeWithText("back").performClick()
        compose.onNodeWithText("about screen").assertIsDisplayed()
    }

    @Test
    fun systemBack_fromLibraries_returnsToAbout() {
        compose.setContent(app)
        compose.onNodeWithText("À propos").performClick()
        compose.onNodeWithText("libraries").performClick()
        compose.runOnUiThread { compose.activity.onBackPressedDispatcher.onBackPressed() }
        compose.onNodeWithText("about screen").assertIsDisplayed()
    }

    @Test
    fun back_fromAbout_returnsToCarte() {
        compose.setContent(app)
        compose.onNodeWithText("À propos").performClick()
        compose.onNodeWithText("about screen").assertIsDisplayed()
        compose.runOnUiThread { compose.activity.onBackPressedDispatcher.onBackPressed() }
        compose.onNodeWithText("map tree=null genus=null").assertIsDisplayed()
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
