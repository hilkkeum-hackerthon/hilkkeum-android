package kr.co.cmc.hil.presentation.quest

import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import kr.co.cmc.hil.ui.theme.HilkkeumTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class QuestModeScreenTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun nextReturnsOnlyTheCurrentSelection() {
        var submittedMode: QuestMode? = null
        compose.setContent {
            HilkkeumTheme { QuestModeScreen(onNext = { submittedMode = it }) }
        }

        val together = compose.onNodeWithText("만나서 데이트\n할 거예요")
        val apart = compose.onNodeWithText("따로 시간\n보낼 거예요")
        val next = compose.onNodeWithText("다음")
        together.assertIsNotSelected()
        apart.assertIsNotSelected()
        next.performClick()
        compose.runOnIdle { assertNull(submittedMode) }

        together.performClick().assertIsSelected()
        apart.assertIsNotSelected()
        apart.performClick().assertIsSelected()
        together.assertIsNotSelected()
        next.performClick()
        compose.runOnIdle { assertEquals(QuestMode.Apart, submittedMode) }
    }
}
