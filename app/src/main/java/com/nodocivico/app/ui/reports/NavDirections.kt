package com.nodocivico.app.ui.reports

import androidx.navigation.NavDirections

/**
 * Clase generada manualmente con los mismos argumentos que produciría SafeArgs.
 * SafeArgs la genera automáticamente al compilar con el plugin
 * 'androidx.navigation.safeargs.kotlin'. Este archivo sirve como referencia
 * y se puede eliminar una vez que Gradle genere las clases automáticamente.
 */
object ReportListFragmentDirections {
    fun actionReportListToReportDetail(reportId: Long): NavDirections =
        object : NavDirections {
            override val actionId = com.nodocivico.app.R.id.action_reportList_to_reportDetail
            override val arguments = android.os.Bundle().apply { putLong("reportId", reportId) }
        }
}

object ReportDetailFragmentDirections {
    fun actionReportDetailToEditReport(reportId: Long): NavDirections =
        object : NavDirections {
            override val actionId = com.nodocivico.app.R.id.action_reportDetail_to_editReport
            override val arguments = android.os.Bundle().apply { putLong("reportId", reportId) }
        }
}
