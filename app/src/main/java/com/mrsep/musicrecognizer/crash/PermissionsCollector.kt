package com.mrsep.musicrecognizer.crash

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.util.Log
import com.google.auto.service.AutoService
import org.acra.builder.ReportBuilder
import org.acra.collector.Collector
import org.acra.config.CoreConfiguration
import org.acra.data.CrashReportData

@AutoService(Collector::class)
class PermissionsCollector : Collector {

    override fun collect(
        context: Context,
        config: CoreConfiguration,
        reportBuilder: ReportBuilder,
        crashReportData: CrashReportData,
    ) {
        val granted = mutableListOf<String>()
        val denied = mutableListOf<String>()
        try {
            val packageInfo = with(context) {
                packageManager.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS)
            }
            packageInfo.requestedPermissions!!.forEachIndexed { i, permission ->
                if ((packageInfo.requestedPermissionsFlags!![i] and PackageInfo.REQUESTED_PERMISSION_GRANTED)
                    == PackageInfo.REQUESTED_PERMISSION_GRANTED
                ) {
                    granted.add(permission)
                } else {
                    denied.add(permission)
                }
            }
            crashReportData.put(KEY_GRANTED_PERMISSIONS, granted.joinToString())
            crashReportData.put(KEY_DENIED_PERMISSIONS, denied.joinToString())
        } catch (e: Exception) {
            Log.e("ACRA-PermissionsCollector", "Failed to get granted permissions", e)
        }
    }

    companion object {
        const val KEY_GRANTED_PERMISSIONS = "GRANTED_PERMISSIONS"
        const val KEY_DENIED_PERMISSIONS = "DENIED_PERMISSIONS"
    }
}
