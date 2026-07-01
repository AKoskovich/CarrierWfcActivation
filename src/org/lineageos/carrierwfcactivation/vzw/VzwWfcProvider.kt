/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.carrierwfcactivation.vzw

import android.content.Context
import android.telephony.PhoneNumberUtils
import android.telephony.SubscriptionManager
import android.telephony.ims.ImsMmTelManager
import android.telephony.ims.ProvisioningManager
import android.util.Log

import org.lineageos.carrierwfcactivation.WfcActivationProvider

class VzwWfcProvider : WfcActivationProvider {
    override fun supportsCarrier(carrierId: Int) = carrierId in CARRIER_IDS

    override fun activate(context: Context, subId: Int) {
        val mdn = getMdn(context, subId)
        if (mdn.isNullOrEmpty()) {
            Log.w(TAG, "sub $subId: MDN unavailable, will retry on next trigger")
            return
        }
        try {
            val provisioningManager = ProvisioningManager.createForSubscriptionId(subId)
            if (mdn == provisioningManager.getProvisioningStringValue(
                    ProvisioningManager.KEY_VOICE_OVER_WIFI_ENTITLEMENT_ID)) {
                return
            }
            provisioningManager.setProvisioningStringValue(
                ProvisioningManager.KEY_VOICE_OVER_WIFI_ENTITLEMENT_ID, mdn)
            ImsMmTelManager.createForSubscriptionId(subId).setVoWiFiSettingEnabled(true)
            Log.d(TAG, "sub $subId: VoWiFi entitlement id provisioned")
        } catch (e: RuntimeException) {
            Log.e(TAG, "sub $subId: failed to provision", e)
        }
    }

    private fun getMdn(context: Context, subId: Int): String? {
        val number = context.getSystemService(SubscriptionManager::class.java).getPhoneNumber(subId)
        if (number.isNullOrEmpty()) {
            return null
        }
        return PhoneNumberUtils.formatNumberToE164(number, "US")?.substring(1)
    }

    companion object {
        private const val TAG = "CarrierWfc-Vzw"
        private val CARRIER_IDS = setOf(1839, 2032, 10008, 2126, 1910, 2654, 2146)
    }
}
