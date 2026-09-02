package org.saudigitus.semis.transfer.model

data class TransferMessage(
    val text: String,
    val type: TransferMessageType,
)
