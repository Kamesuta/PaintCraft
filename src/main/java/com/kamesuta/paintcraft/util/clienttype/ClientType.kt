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

    /** BE版、Geyser */
    private val isBedrockEdition: Boolean
        get() = clientBrand == "Geyser"

    /** ブロックに垂直な面のみサポート (BE版、Geyser) */
    val isFacingRotationOnly get() = isBedrockEdition

    /** 透明な額縁をサポート (BE版、Geyser) */
    val isInvisibleFrameSupported get() = isBedrockEdition

    /** ピッチの回転をサポートするか (1.13以上、ViaVersion) */
    val isPitchRotationSupported get() = clientVersion?.let { it > 340 } ?: true

    /** 旧仕様の4方向回転であるか (1.7.10以下、ViaVersion) */
    val isLegacyRotation get() = clientVersion?.let { it <= 5 } ?: true

    /** クライアントのクリックしきい値 */
    val threshold
        get() = when (isBedrockEdition) {
            true -> ClientTypeThreshold.GEYSER
            false -> ClientTypeThreshold.VANILLA
        }
}
