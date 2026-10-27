package org.saudigitus.campaign.core.utils

import android.graphics.Bitmap
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import io.ktor.util.encodeBase64
import org.hisp.dhis.android.core.D2


class QRCodeGenerator(
    private val d2: D2
) {

    fun generateUserInfo(): Bitmap? {
        try {
            val user = d2.userModule().user().blockingGet() ?: return null

            val barcodeEncoder = BarcodeEncoder()
            val bitmap = barcodeEncoder.encodeBitmap(
                """{"uid":"${user.uid()}", "name":"${user.displayName()}"}"""
                    .trim()
                    .encodeBase64(),
                BarcodeFormat.QR_CODE,
                400,
                400
            )
            return bitmap
        } catch (_: Exception) {
            return null
        }
    }

}