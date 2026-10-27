package org.saudigitus.campaign.core.utils

import com.journeyapps.barcodescanner.ScanOptions

object Utils {
    const val PRIMARY_COLOR = "#FF007DEB"

    fun scanOptions(): ScanOptions {
        val options = ScanOptions()
        options.setDesiredBarcodeFormats(ScanOptions.ALL_CODE_TYPES)
        options.setBeepEnabled(true)

        return options
    }

}