package com.kamesuta.paintcraft.util.clienttype

import com.kamesuta.paintcraft.util.TimeWatcher

/**
 * クライアントの種類を取得する
 */
enum class ClientType(limit: Long) {
    /** 通常のクライアント */
    VANILLA(20),

    /** Geyser (Bedrock Edition) */
    GEYSER(80),
    ;

    /** 最後のエンティティ右クリックから左クリックを無視し続ける時間 */
    val interactEntityDuration = TimeWatcher(limit)

    /** 最後のエンティティ移動時刻からティックイベントを無視し続ける時間 */
    val vehicleMoveDuration = TimeWatcher(limit)

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
