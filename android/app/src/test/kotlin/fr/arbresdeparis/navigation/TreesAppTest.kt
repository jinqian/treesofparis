package fr.arbresdeparis.navigation

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.retain.RetainObserver
import androidx.compose.runtime.retain.retain
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import fr.arbresdeparis.TreesApp
import fr.arbresdeparis.core.designsystem.theme.TreesTheme
import fr.arbresdeparis.feature.herbier.HerbierSection
import org.junit.Assert.assertEquals
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
                mapContent = { key, inFront, onOpenStory ->
                    val map = retain { StubMap(created = mapsCreated++) }
                    Column {
                        Text("map tree=${key.treeId} genus=${key.genus}" + if (inFront) "" else " behind")
                        Text("map instance ${map.created}")
                        Button(onClick = { onOpenStory(124914) }) { Text("story 124914") }
                    }
                },
                remarkableStoryContent = { treeId, onBack ->
                    Column {
                        Text("story of $treeId")
                        Button(onClick = onBack) { Text("back") }
                    }
                },
                favoritesContent = { onTreeClick, onExploreMap ->
                    Column {
                        Button(onClick = { onTreeClick(42) }) { Text("open 42") }
                        Button(onClick = onExploreMap) { Text("explore") }
                    }
                },
                herbierContent = { onOpenSection ->
                    Column {
                        Text("herbier screen")
                        Button(onClick = { onOpenSection(HerbierSection.GENERA) }) { Text("essences") }
                    }
                },
                herbierSectionContent = { section, onBack, onGenusClick, onTreeClick ->
                    Column {
                        Text("section $section")
                        Button(onClick = { onGenusClick("Tilia") }) { Text("tilleul") }
                        Button(onClick = { onTreeClick(7) }) { Text("remarkable 7") }
                        Button(onClick = onBack) { Text("back") }
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
    fun aRemarkableTreesStory_opensOverTheMap_andBackKeepsTheSameMap() {
        compose.setContent(app)
        compose.onNodeWithText("story 124914").performClick()
        compose.onNodeWithText("story of 124914").assertIsDisplayed()
        compose.onNodeWithText("Carte").assertIsSelected()
        compose.onNodeWithText("back").performClick()
        compose.onNodeWithText("map instance 0").assertIsDisplayed()
        assertEquals(0, mapsRetired)
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
    fun switchingTabs_isImmediate_soTheMapsSystemButtonsDoNotLinger() {
        // The system location buttons sit above the window: a cross-fade would leave them over the next tab.
        compose.setContent(app)
        compose.mainClock.autoAdvance = false
        compose.onNodeWithText("Herbier").performClick()
        compose.mainClock.advanceTimeBy(TAB_SWITCH_BUDGET_MS)
        compose.onNodeWithText("herbier screen").assertIsDisplayed()
        compose.onNodeWithText("map tree=null genus=null", substring = true).assertDoesNotExist()
        compose.onNodeWithText("Carte").performClick()
        compose.mainClock.advanceTimeBy(TAB_SWITCH_BUDGET_MS)
        compose.onNodeWithText("map tree=null genus=null").assertIsDisplayed()
        compose.onNodeWithText("herbier screen").assertDoesNotExist()
    }

    @Test
    fun theMap_isKeptWhileAnotherTabIsShown_andReleasedWhenItsEntryIsPopped() {
        compose.setContent(app)
        compose.onNodeWithText("map instance 0").assertIsDisplayed()
        compose.onNodeWithText("Herbier").performClick()
        compose.onNodeWithText("herbier screen").assertIsDisplayed()
        compose.onNodeWithText("Carte").performClick()
        // Same map, not rebuilt: no reload (and no flash) when coming back to Carte.
        compose.onNodeWithText("map instance 0").assertIsDisplayed()
        assertEquals(0, mapsRetired)
        // Opening a tree from Favoris restarts the Carte stack: the old map entry is popped and its map released.
        compose.onNodeWithText("Favoris").performClick()
        compose.onNodeWithText("open 42").performClick()
        compose.onNodeWithText("map instance 1").assertIsDisplayed()
        compose.waitUntil(timeoutMillis = 5_000) { mapsRetired == 1 }
    }

    @Test
    fun herbierSection_stillSlidesIn_andTheMapIsInFrontOnceBack() {
        compose.setContent(app)
        compose.onNodeWithText("Herbier").performClick()
        compose.onNodeWithText("herbier screen").assertIsDisplayed()
        compose.mainClock.autoAdvance = false
        compose.onNodeWithText("essences").performClick()
        compose.mainClock.advanceTimeBy(TAB_SWITCH_BUDGET_MS)
        // Inside a tab, screens keep their transition: the Herbier home is still on its way out.
        compose.onNodeWithText("herbier screen").assertExists()
        compose.mainClock.autoAdvance = true
        compose.onNodeWithText("herbier screen").assertDoesNotExist()
        compose.onNodeWithText("Carte").performClick()
        compose.onNodeWithText("map tree=null genus=null").assertIsDisplayed()
    }

    @Test
    fun herbierSection_pushedOnTheHerbierStack_andBackReturnsHome() {
        compose.setContent(app)
        compose.onNodeWithText("Herbier").performClick()
        compose.onNodeWithText("essences").performClick()
        compose.onNodeWithText("section GENERA").assertIsDisplayed()
        compose.onNodeWithText("Carte").performClick()
        compose.onNodeWithText("Herbier").performClick()
        compose.onNodeWithText("section GENERA").assertIsDisplayed()
        compose.runOnUiThread { compose.activity.onBackPressedDispatcher.onBackPressed() }
        compose.onNodeWithText("herbier screen").assertIsDisplayed()
    }

    @Test
    fun remarkableTreeFromHerbier_opensItOnTheMap() {
        compose.setContent(app)
        compose.onNodeWithText("Herbier").performClick()
        compose.onNodeWithText("essences").performClick()
        compose.onNodeWithText("remarkable 7").performClick()
        compose.onNodeWithText("map tree=7 genus=null").assertIsDisplayed()
        compose.onNodeWithText("Carte").assertIsSelected()
    }

    @Test
    fun genusFromHerbier_opensTheMapFilteredToIt() {
        compose.setContent(app)
        compose.onNodeWithText("Herbier").performClick()
        compose.onNodeWithText("essences").performClick()
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

    private var mapsCreated = 0
    private var mapsRetired = 0

    /** Stands in for the retained MapLibre state: counts how many are made and released. */
    private inner class StubMap(
        val created: Int,
    ) : RetainObserver {
        override fun onRetained() = Unit

        override fun onEnteredComposition() = Unit

        override fun onExitedComposition() = Unit

        override fun onRetired() {
            mapsRetired++
        }

        override fun onUnused() = Unit
    }

    private companion object {
        /** Well under the 700 ms cross-fade Navigation 3 uses by default. */
        const val TAB_SWITCH_BUDGET_MS = 100L
    }
}
