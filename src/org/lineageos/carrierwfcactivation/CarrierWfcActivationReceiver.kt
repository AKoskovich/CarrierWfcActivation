/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.carrierwfcactivation

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager

import org.lineageos.carrierwfcactivation.vzw.VzwWfcProvider

import kotlin.concurrent.thread

class CarrierWfcActivationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val subId = intent.getIntExtra(
            SubscriptionManager.EXTRA_SUBSCRIPTION_INDEX,
            SubscriptionManager.INVALID_SUBSCRIPTION_ID)
        if (!SubscriptionManager.isValidSubscriptionId(subId)) {
            return
        }

        val carrierId = context.getSystemService(TelephonyManager::class.java)
            .createForSubscriptionId(subId)
            .simCarrierId

        val provider = PROVIDERS.firstOrNull { it.supportsCarrier(carrierId) } ?: return

        val appContext = context.applicationContext
        val result = goAsync()
        thread {
            try {
                provider.activate(appContext, subId)
            } finally {
                result.finish()
            }
        }
    }

    companion object {
        private val PROVIDERS: List<WfcActivationProvider> = listOf(VzwWfcProvider())
    }
}
