import AdSupport
import AppTrackingTransparency
import NitroModules
import UIKit

class NitroAdvertisingId: HybridNitroAdvertisingIdSpec {
  @MainActor
  private var pendingContinuation:
    CheckedContinuation<ATTrackingManager.AuthorizationStatus, Error>?

  override init() {
    super.init()

    NotificationCenter.default.addObserver(
      self,
      selector: #selector(onApplicationDidBecomeActive),
      name: UIApplication.didBecomeActiveNotification,
      object: nil
    )
  }

  deinit {
    NotificationCenter.default.removeObserver(
      self,
      name: UIApplication.didBecomeActiveNotification,
      object: nil
    )
  }

  func requestPermission() throws -> NitroModules.Promise<NitroAdvertisingIdResult> {
    let promise = Promise<NitroAdvertisingIdResult>()

    Task {
      do {
        if #available(iOS 14, *) {
          let currentStatus = ATTrackingManager.trackingAuthorizationStatus

          if currentStatus != .notDetermined {
            promise.resolve(withResult: self.convertStatus(currentStatus))
            return
          }

          let status = try await self.requestTrackingAuthorization()
          promise.resolve(withResult: self.convertStatus(status))
        } else {
          if ASIdentifierManager.shared().isAdvertisingTrackingEnabled {
            promise.resolve(withResult: .authorized)
          } else {
            promise.resolve(withResult: .denied)
          }
        }
      } catch {
        promise.reject(withError: error)
      }
    }

    return promise
  }

  func getAdvertisingId() -> String {
    let idfa = ASIdentifierManager.shared().advertisingIdentifier
    return idfa.uuidString
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
      if ASIdentifierManager.shared().isAdvertisingTrackingEnabled {
        return .authorized
      } else {
        return .denied
      }
    }
  }

  @available(iOS 14, *)
  private func requestTrackingAuthorization() async throws -> ATTrackingManager.AuthorizationStatus
  {
    return try await withCheckedThrowingContinuation {
      (continuation: CheckedContinuation<ATTrackingManager.AuthorizationStatus, Error>) in

      Task { @MainActor in
        if UIApplication.shared.applicationState == .active {
          let status = await ATTrackingManager.requestTrackingAuthorization()
          continuation.resume(returning: status)
        } else {
          self.pendingContinuation = continuation
        }
      }
    }
  }

  @MainActor
  @objc private func onApplicationDidBecomeActive() {
    guard let continuation = pendingContinuation else { return }

    pendingContinuation = nil

    if #available(iOS 14, *) {
      Task {
        let authStatus = await ATTrackingManager.requestTrackingAuthorization()
        continuation.resume(returning: authStatus)
      }
    }
  }

  private func convertStatus(_ status: ATTrackingManager.AuthorizationStatus)
    -> NitroAdvertisingIdResult
  {
    switch status {
    case .authorized:
      return .authorized
    case .denied:
      return .denied
    case .restricted:
      return .restricted
    case .notDetermined:
      return .notdetermined
    @unknown default:
      return .unknown
    }
  }
}
