package com.nodocivico.app.ui.reports

import android.os.Bundle
import androidx.navigation.NavDirections
import com.nodocivico.app.R
import kotlin.Int
import kotlin.Long

public class ReportDetailFragmentDirections private constructor() {
  private data class ActionReportDetailToEditReport(
    public val reportId: Long,
  ) : NavDirections {
    public override val actionId: Int = R.id.action_reportDetail_to_editReport

    public override val arguments: Bundle
      get() {
        val result = Bundle()
        result.putLong("reportId", this.reportId)
        return result
      }
  }

  public companion object {
    public fun actionReportDetailToEditReport(reportId: Long): NavDirections =
        ActionReportDetailToEditReport(reportId)
  }
}
