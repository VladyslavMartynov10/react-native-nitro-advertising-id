import AdSupport
import AppTrackingTransparency
import NitroModules
import UIKit

class NitroAdvertisingId: HybridNitroAdvertisingIdSpec {

  func requestPermission() throws -> NitroModules.Promise<NitroAdvertisingIdResult> {

    let promise = Promise<NitroAdvertisingIdResult>()

    Task { @MainActor in
      if #available(iOS 14, *) {
        let status = await self.requestTrackingAuthorizationSafe()
        promise.resolve(withResult: self.convertStatus(status))
      } else {
        let enabled = ASIdentifierManager.shared().isAdvertisingTrackingEnabled
        promise.resolve(withResult: enabled ? .authorized : .denied)
      }
    }

    return promise
  }

  @available(iOS 14, *)
  private func requestTrackingAuthorizationSafe() async -> ATTrackingManager.AuthorizationStatus {
    let osVersion = ProcessInfo.processInfo.operatingSystemVersion
    let isAffectedByBug = osVersion.majorVersion == 17 && osVersion.minorVersion >= 4

    if !isAffectedByBug {
      return await ATTrackingManager.requestTrackingAuthorization()
    }

    // Workaround for iOS 17.4 bug where requestTrackingAuthorization()
    // returns .denied while the actual status is still .notDetermined
    // https://developer.apple.com/forums/thread/746432
    var status = await ATTrackingManager.requestTrackingAuthorization()

    if status == .denied,
      ATTrackingManager.trackingAuthorizationStatus == .notDetermined
    {
      await self.waitUntilAppBecomesActive()
      status = ATTrackingManager.trackingAuthorizationStatus
    }

    return status
  }

  @MainActor
  private func waitUntilAppBecomesActive() async {
    let stream = AsyncStream<Void> { continuation in
      let observer = NotificationCenter.default.addObserver(
        forName: UIApplication.didBecomeActiveNotification,
        object: nil,
        queue: .main
      ) { _ in
        continuation.yield()
        continuation.finish()
      }

      continuation.onTermination = { _ in
        NotificationCenter.default.removeObserver(observer)
      }

      if UIApplication.shared.applicationState == .active {
        continuation.yield()
        continuation.finish()
      }
    }

    for await _ in stream { break }
  }

  private func convertStatus(_ status: ATTrackingManager.AuthorizationStatus)
    -> NitroAdvertisingIdResult
  {
    switch status {
    case .authorized: return .authorized
    case .denied: return .denied
    case .restricted: return .restricted
    case .notDetermined: return .notdetermined
    @unknown default: return .unknown
    }
  }

  func getAdvertisingId() throws -> String {
    ASIdentifierManager.shared().advertisingIdentifier.uuidString
  }

  func isAdvertisingTrackingEnabled() -> Bool {
    if #available(iOS 14, *) {
      return ATTrackingManager.trackingAuthorizationStatus == .authorized
    } else {
      return ASIdentifierManager.shared().isAdvertisingTrackingEnabled
    }
  }

  func getPermissionStatus() -> NitroAdvertisingIdResult {
    if #available(iOS 14, *) {
      return convertStatus(ATTrackingManager.trackingAuthorizationStatus)
    } else {
      return ASIdentifierManager.shared().isAdvertisingTrackingEnabled ? .authorized : .denied
    }
  }
}
