package com.nodocivico.app.ui.home

import androidx.navigation.ActionOnlyNavDirections
import androidx.navigation.NavDirections
import com.nodocivico.app.R

public class HomeFragmentDirections private constructor() {
  public companion object {
    public fun actionHomeToReportList(): NavDirections =
        ActionOnlyNavDirections(R.id.action_home_to_reportList)

    public fun actionHomeToCreateReport(): NavDirections =
        ActionOnlyNavDirections(R.id.action_home_to_createReport)
  }
}
