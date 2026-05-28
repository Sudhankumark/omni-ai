package com.example

import android.app.Application
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import com.example.ui.editor.ReplViewModel
import com.example.ui.editor.StudioForgeMainGui
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @get:Rule
  val composeTestRule = createComposeRule()

  @Test
  fun testMainGuiComposition() {
    val application = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = ReplViewModel(application)
    
    // Disable clock auto-advance to prevent infinite animation ticks from hanging the test runner
    composeTestRule.mainClock.autoAdvance = false
    
    composeTestRule.setContent {
      StudioForgeMainGui(viewModel = viewModel)
    }
    
    composeTestRule.waitForIdle()
  }

  @Test
  fun testWorkspaceCompositionAfterLogin() {
    val application = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = ReplViewModel(application)
    
    // Log in to trigger composition of IdeWorkspaceLayout
    viewModel.loginWithGoogle("Sudhan Bhai", "bhaisudhan035@gmail.com", "https://api.dicebear.com/7.x/pixel-art/svg?seed=Sudhan")
    
    // Disable clock auto-advance to prevent infinite animation ticks from hanging the test runner
    composeTestRule.mainClock.autoAdvance = false
    
    composeTestRule.setContent {
      StudioForgeMainGui(viewModel = viewModel)
    }
    
    composeTestRule.waitForIdle()
  }
}
