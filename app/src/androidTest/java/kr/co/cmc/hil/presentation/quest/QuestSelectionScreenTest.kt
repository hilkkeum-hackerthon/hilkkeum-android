package kr.co.cmc.hil.presentation.quest

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import kr.co.cmc.hil.ui.theme.HilkkeumTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class QuestSelectionScreenTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun requiresExactCountAndAllowsReplacingASelection() {
        var submitted: List<Int>? = null
        compose.setContent {
            HilkkeumTheme { QuestSelectionScreen(missionCount = 2, onNext = { submitted = it }) }
        }

        val first = compose.onNodeWithText("미션 1 고르기")
        val second = compose.onNodeWithText("미션 2 고르기")
        val third = compose.onNodeWithText("미션 3 고르기")
        val next = compose.onNodeWithText("다음")
        next.assertIsNotEnabled()
        compose.runOnIdle { assertNull(submitted) }

        first.performClick().assertIsOn()
        next.assertIsNotEnabled()
        second.performClick().assertIsOn()
        third.assertIsNotEnabled().assertIsOff()
        next.assertIsEnabled()
        first.performClick().assertIsOff()
        next.assertIsNotEnabled()
        third.assertIsEnabled().performClick().assertIsOn()
        next.performClick()

        compose.runOnIdle { assertEquals(listOf(2, 3), submitted) }
    }
}
