package com.nodocivico.app.ui.auth

import androidx.navigation.ActionOnlyNavDirections
import androidx.navigation.NavDirections
import com.nodocivico.app.R

public class SplashFragmentDirections private constructor() {
  public companion object {
    public fun actionSplashToLogin(): NavDirections =
        ActionOnlyNavDirections(R.id.action_splash_to_login)

    public fun actionSplashToHome(): NavDirections =
        ActionOnlyNavDirections(R.id.action_splash_to_home)
  }
}
