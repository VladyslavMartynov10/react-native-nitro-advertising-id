package com.margelo.nitro.nitroadvertisingid

import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.facebook.proguard.annotations.DoNotStrip
import com.facebook.react.modules.core.PermissionAwareActivity
import com.facebook.react.modules.core.PermissionListener
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.margelo.nitro.NitroModules
import com.margelo.nitro.core.Promise
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

@DoNotStrip
class NitroAdvertisingId : HybridNitroAdvertisingIdSpec() {

  companion object {
    private const val AD_ID_PERMISSION = "com.google.android.gms.permission.AD_ID"
    private var requestCode = 100
  }

  private fun isAndroid13OrHigher(): Boolean {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
  }

  private fun canRequestPermission(permission: String): Boolean {
    val ctx = NitroModules.applicationContext ?: return false
    val activity = ctx.currentActivity ?: return false
    return !activity.shouldShowRequestPermissionRationale(permission) ||
            activity.shouldShowRequestPermissionRationale(permission)
  }

  private fun getPermissionStatus(permission: String): String {
    val ctx = NitroModules.applicationContext ?: return "undetermined"
    val status = ContextCompat.checkSelfPermission(ctx, permission)
    return when (status) {
      PackageManager.PERMISSION_GRANTED -> "granted"
      PackageManager.PERMISSION_DENIED -> {
        if (canRequestPermission(permission)) "undetermined" else "denied"
      }

      else -> "undetermined"
    }
  }

  override fun requestPermission(): Promise<String> = Promise.async {
    if (!isAndroid13OrHigher()) {
      return@async "granted"
    }

    val currentStatus = getPermissionStatus(AD_ID_PERMISSION)

    if (currentStatus == "granted") {
      return@async "granted"
    }

    val ctx = NitroModules.applicationContext
      ?: throw Error("Application context is not available")

    val activity = ctx.currentActivity
      ?: throw Error("Current activity is not available")

    check(activity is PermissionAwareActivity) {
      "Current Activity does not implement PermissionAwareActivity"
    }

    suspendCancellableCoroutine { cont ->
      val reqCode = requestCode++
      val listener = PermissionListener { code, _, results ->
        if (code != reqCode) return@PermissionListener false
        val result = results.firstOrNull() ?: PackageManager.PERMISSION_DENIED
        val status = when (result) {
          PackageManager.PERMISSION_GRANTED -> "granted"
          else -> if (canRequestPermission(AD_ID_PERMISSION)) "undetermined" else "denied"
        }
        cont.resume(status)
        true
      }
      activity.requestPermissions(arrayOf(AD_ID_PERMISSION), reqCode, listener)
    }
  }

  override fun getAdvertisingId(): String {
    val ctx = NitroModules.applicationContext
      ?: throw Error("Application context is not available")

    return try {
      val adInfo = AdvertisingIdClient.getAdvertisingIdInfo(ctx)

      if (adInfo.isLimitAdTrackingEnabled) {
        "00000000-0000-0000-0000-000000000000"
      } else {
        adInfo.id ?: "00000000-0000-0000-0000-000000000000"
      }
    } catch (e: Exception) {
      throw Error("Failed to get advertising ID: ${e.message}")
    }
  }
}
