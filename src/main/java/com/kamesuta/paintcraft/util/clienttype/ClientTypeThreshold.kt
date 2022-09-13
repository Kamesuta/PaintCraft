package com.kamesuta.paintcraft.util.clienttype

import com.kamesuta.paintcraft.util.TimeWatcher

/**
 * クライアントの種類に応じたしきい値
 */
enum class ClientTypeThreshold(
    interactEntityLimit: Long,
    dropItemLimit: Long,
    vehicleMoveLimit: Long,
    drawLimit: Long,
) {
    /** 通常のクライアント */
    VANILLA(40, 80, 40, 300),

    /** Geyser (BE版) */
    GEYSER(80, 160, 80, 500),
    ;

    /** 最後のエンティティ右クリックから左クリックを無視し続ける時間 */
    val interactEntityDuration = TimeWatcher(interactEntityLimit)

    /** 最後のアイテムドロップから左クリックを無視し続ける時間 */
    val dropItemDuration = TimeWatcher(dropItemLimit)

    /** 最後のエンティティ移動時刻からティックイベントを無視し続ける時間 */
    val vehicleMoveDuration = TimeWatcher(vehicleMoveLimit)

    /** 最後のクリックから書き続ける時間 */
    val drawDuration = TimeWatcher(drawLimit)
}
