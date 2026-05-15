package com.nodocivico.app.ui.reports

import android.os.Bundle
import androidx.navigation.ActionOnlyNavDirections
import androidx.navigation.NavDirections
import com.nodocivico.app.R
import kotlin.Int
import kotlin.Long

public class ReportListFragmentDirections private constructor() {
  private data class ActionReportListToReportDetail(
    public val reportId: Long,
  ) : NavDirections {
    public override val actionId: Int = R.id.action_reportList_to_reportDetail

    public override val arguments: Bundle
      get() {
        val result = Bundle()
        result.putLong("reportId", this.reportId)
        return result
      }
  }

  public companion object {
    public fun actionReportListToReportDetail(reportId: Long): NavDirections =
        ActionReportListToReportDetail(reportId)

    public fun actionReportListToCreateReport(): NavDirections =
        ActionOnlyNavDirections(R.id.action_reportList_to_createReport)
  }
}
