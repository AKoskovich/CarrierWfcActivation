/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.carrierwfcactivation

import android.content.Context

interface WfcActivationProvider {
    fun supportsCarrier(carrierId: Int): Boolean

    fun activate(context: Context, subId: Int)
}
