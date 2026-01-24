package com.margelo.nitro.nitroadvertisingid
  
import com.facebook.proguard.annotations.DoNotStrip

@DoNotStrip
class NitroAdvertisingId : HybridNitroAdvertisingIdSpec() {
  override fun multiply(a: Double, b: Double): Double {
    return a * b
  }
}
