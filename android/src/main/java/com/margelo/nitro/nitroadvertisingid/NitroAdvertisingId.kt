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
    private const val DEFAULT_ADVERTISING_ID = "00000000-0000-0000-0000-000000000000"
    private var requestCode = 100
  }

  private fun isAndroid13OrHigher(): Boolean {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
  }

  private fun canRequestPermission(permission: String): Boolean {
    val context = NitroModules.applicationContext ?: return false
    val activity = context.currentActivity ?: return false
    return !activity.shouldShowRequestPermissionRationale(permission) ||
            activity.shouldShowRequestPermissionRationale(permission)
  }

  private fun getPermissionStatus(permission: String): NitroAdvertisingIdResult {
    val context = NitroModules.applicationContext ?: return NitroAdvertisingIdResult.UNDETERMINED
    val status = ContextCompat.checkSelfPermission(context, permission)
    return when (status) {
      PackageManager.PERMISSION_GRANTED -> NitroAdvertisingIdResult.GRANTED
      PackageManager.PERMISSION_DENIED -> {
        if (canRequestPermission(permission)) NitroAdvertisingIdResult.UNDETERMINED else NitroAdvertisingIdResult.DENIED
      }

      else -> NitroAdvertisingIdResult.UNDETERMINED
    }
  }

  override fun requestPermission(): Promise<NitroAdvertisingIdResult> = Promise.async {
    if (!isAndroid13OrHigher()) {
      return@async NitroAdvertisingIdResult.GRANTED
    }

    val currentStatus = getPermissionStatus(AD_ID_PERMISSION)

    if (currentStatus == NitroAdvertisingIdResult.GRANTED) {
      return@async NitroAdvertisingIdResult.GRANTED
    }

    val context = NitroModules.applicationContext
      ?: throw Error("Application context is not available")

    val activity = context.currentActivity
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
          PackageManager.PERMISSION_GRANTED -> NitroAdvertisingIdResult.GRANTED
          else -> if (canRequestPermission(AD_ID_PERMISSION)) NitroAdvertisingIdResult.UNDETERMINED else NitroAdvertisingIdResult.DENIED
        }
        cont.resume(status)
        true
      }
      activity.requestPermissions(arrayOf(AD_ID_PERMISSION), reqCode, listener)
    }
  }

  override fun getAdvertisingId(): String {
    val context = NitroModules.applicationContext
      ?: throw Error("Application context is not available")

    return try {
      val adInfo = AdvertisingIdClient.getAdvertisingIdInfo(context)

      if (adInfo.isLimitAdTrackingEnabled) {
        DEFAULT_ADVERTISING_ID
      } else {
        adInfo.id ?: DEFAULT_ADVERTISING_ID
      }
    } catch (e: Exception) {
      throw Error("Failed to get advertising ID: ${e.message}")
    }
  }
}
