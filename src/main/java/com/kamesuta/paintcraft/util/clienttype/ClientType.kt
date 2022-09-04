package com.kamesuta.paintcraft.util.clienttype

import com.kamesuta.paintcraft.util.TimeWatcher

/**
 * クライアントの種類を取得する
 * @param isBedrockEdition BE版(Geyser)かどうか
 */
enum class ClientType(
    interactEntityLimit: Long,
    vehicleMoveLimit: Long,
    drawLimit: Long,
    val isBedrockEdition: Boolean,
) {
    /** 通常のクライアント */
    VANILLA(40, 40, 300, false),

    /** Geyser (Bedrock Edition) */
    GEYSER(80, 80, 500, false),
    ;

    /** 最後のエンティティ右クリックから左クリックを無視し続ける時間 */
    val interactEntityDuration = TimeWatcher(interactEntityLimit)

    /** 最後のエンティティ移動時刻からティックイベントを無視し続ける時間 */
    val vehicleMoveDuration = TimeWatcher(vehicleMoveLimit)

    /** 最後のクリックから書き続ける時間 */
    val drawDuration = TimeWatcher(drawLimit)

    companion object {
        /**
         * クライアントの種類を取得する
         * @param clientBrand クライアントのブランド
         * @return クライアントの種類
         */
        operator fun get(clientBrand: String?): ClientType {
            return if (clientBrand == "Geyser") {
                GEYSER
            } else {
                VANILLA
            }
        }
    }
}
