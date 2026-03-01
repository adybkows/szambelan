package pl.coopsoft.szambelan.core.utils

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Build
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.confirmVerified
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper
import pl.coopsoft.szambelan.R
import pl.coopsoft.testutils.EmptyTestActivity

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.TIRAMISU])
class DialogUtilsTests {
    private val dialogUtils = DialogUtils()
    private val activityScenario = ActivityScenario.launch(EmptyTestActivity::class.java)

    @Test
    fun testShowQuestionDialogAndClickYes() {
        activityScenario.onActivity { activity ->
            val yesClicked = mockk<Runnable>(relaxed = true)
            val cancelClicked = mockk<Runnable>(relaxed = true)

            val dialog = showQuestionDialog(activity, yesClicked, cancelClicked)

            onView(withText(R.string.yes)).perform(click())

            assertFalse(dialog.isShowing)
            verify { yesClicked.run() }
            confirmVerified(cancelClicked)
        }
    }

    @Test
    fun testShowQuestionDialogAndClickCancel() {
        activityScenario.onActivity { activity ->
            val yesClicked = mockk<Runnable>(relaxed = true)
            val cancelClicked = mockk<Runnable>(relaxed = true)

            val dialog = showQuestionDialog(activity, yesClicked, cancelClicked)

            onView(withText(R.string.cancel)).perform(click())

            assertFalse(dialog.isShowing)
            verify { cancelClicked.run() }
            confirmVerified(yesClicked)
        }
    }

    @Test
    fun testShowQuestionDialogAndCancel() {
        activityScenario.onActivity { activity ->
            val yesClicked = mockk<Runnable>(relaxed = true)
            val cancelClicked = mockk<Runnable>(relaxed = true)

            val dialog = showQuestionDialog(activity, yesClicked, cancelClicked)

            dialog.cancel()
            ShadowLooper.idleMainLooper()

            assertFalse(dialog.isShowing)
            verify { cancelClicked.run() }
            confirmVerified(yesClicked)
        }
    }

    private fun showQuestionDialog(
        context: Context, yesClicked: Runnable, cancelClicked: Runnable
    ): AlertDialog {
        val dialog = dialogUtils.showQuestionDialog(
            context, R.string.empty_tank, R.string.empty_tank_question,
            { yesClicked.run() }, { cancelClicked.run() }
        )

        assertTrue(dialog.isShowing)
        onView(withText(R.string.empty_tank)).inRoot(isDialog()).check(matches(isDisplayed()))
        onView(withText(R.string.empty_tank_question)).inRoot(isDialog())
            .check(matches(isDisplayed()))
        onView(withText(R.string.yes)).inRoot(isDialog()).check(matches(isDisplayed()))
        onView(withText(R.string.cancel)).inRoot(isDialog()).check(matches(isDisplayed()))

        return dialog
    }

    @Test
    fun testShowInProgressDialog() {
        activityScenario.onActivity { activity ->
            val cancelClicked = mockk<Runnable>(relaxed = true)

            val dialog = dialogUtils.showInProgressDialog(
                activity, R.string.download_in_progress
            ) { cancelClicked.run() }

            assertTrue(dialog.isShowing)
            onView(withText(R.string.download_in_progress)).inRoot(isDialog())
                .check(matches(isDisplayed()))
            onView(withText(R.string.cancel)).inRoot(isDialog()).check(matches(isDisplayed()))

            onView(withText(R.string.cancel)).perform(click())

            assertFalse(dialog.isShowing)
            verify { cancelClicked.run() }
        }
    }

    @Test
    fun testShowOKDialog() {
        activityScenario.onActivity { activity ->
            val dismissListener = mockk<DialogInterface.OnDismissListener>(relaxed = true)

            val dialog = dialogUtils.showOKDialog(activity, R.string.download_success)
            dialog.setOnDismissListener(dismissListener)

            assertTrue(dialog.isShowing)
            onView(withText(R.string.download_success)).inRoot(isDialog())
                .check(matches(isDisplayed()))
            onView(withText(R.string.ok)).inRoot(isDialog()).check(matches(isDisplayed()))
            confirmVerified(dismissListener)

            onView(withText(R.string.ok)).perform(click())

            assertFalse(dialog.isShowing)
            verify { dismissListener.onDismiss(eq(dialog)) }
        }
    }
}
