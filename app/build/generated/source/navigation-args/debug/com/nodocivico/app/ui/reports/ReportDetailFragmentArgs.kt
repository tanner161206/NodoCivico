package com.nodocivico.app.ui.reports

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavArgs
import java.lang.IllegalArgumentException
import kotlin.Long
import kotlin.jvm.JvmStatic

public data class ReportDetailFragmentArgs(
  public val reportId: Long,
) : NavArgs {
  public fun toBundle(): Bundle {
    val result = Bundle()
    result.putLong("reportId", this.reportId)
    return result
  }

  public fun toSavedStateHandle(): SavedStateHandle {
    val result = SavedStateHandle()
    result.set("reportId", this.reportId)
    return result
  }

  public companion object {
    @JvmStatic
    public fun fromBundle(bundle: Bundle): ReportDetailFragmentArgs {
      bundle.setClassLoader(ReportDetailFragmentArgs::class.java.classLoader)
      val __reportId : Long
      if (bundle.containsKey("reportId")) {
        __reportId = bundle.getLong("reportId")
      } else {
        throw IllegalArgumentException("Required argument \"reportId\" is missing and does not have an android:defaultValue")
      }
      return ReportDetailFragmentArgs(__reportId)
    }

    @JvmStatic
    public fun fromSavedStateHandle(savedStateHandle: SavedStateHandle): ReportDetailFragmentArgs {
      val __reportId : Long?
      if (savedStateHandle.contains("reportId")) {
        __reportId = savedStateHandle["reportId"]
        if (__reportId == null) {
          throw IllegalArgumentException("Argument \"reportId\" of type long does not support null values")
        }
      } else {
        throw IllegalArgumentException("Required argument \"reportId\" is missing and does not have an android:defaultValue")
      }
      return ReportDetailFragmentArgs(__reportId)
    }
  }
}
