package com.kamesuta.paintcraft.util.clienttype

/**
 * クライアントのバージョンごとの機能の差異を吸収するためのクラス
 */
data class ClientType(
    /** クライアントのブランド名 */
    var clientBrand: String?,

    /** クライアントのバージョン (ViaVersionがインストールされていないときはnull) */
    var clientVersion: Int?,
) {
    /** 未判定 */
    constructor() : this(null, null)

    /** ブロックに垂直な面のみサポート (BE版、Geyser) */
    val isFacingRotationOnly: Boolean
        get() = clientBrand == "Geyser"

    /** ピッチの回転をサポートするか (1.13以上、ViaVersion) */
    val isPitchRotationSupported: Boolean
        get() = clientVersion?.let { it > 340 } ?: true

    /** クライアントのクリックしきい値 */
    val threshold: ClientTypeThreshold
        get() = if (clientBrand == "Geyser") {
            ClientTypeThreshold.GEYSER
        } else {
            ClientTypeThreshold.VANILLA
        }
}
