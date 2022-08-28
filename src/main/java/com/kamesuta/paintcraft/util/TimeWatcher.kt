package com.kamesuta.paintcraft.util

/**
 * 指定した時間以内であるか判定するユーティリティクラス
 * @param limit 時間
 */
class TimeWatcher(private val limit: Long) {
    /**
     * 指定した時間以内であるか判定する
     * @param prevTime 前回の時刻
     * @return 指定した時間以内であるか
     */
    fun isInTime(prevTime: Long): Boolean {
        return now < prevTime + limit
    }

    companion object {
        /**
         * 現在の時刻を取得する
         */
        val now: Long
            get() = System.currentTimeMillis()
    }
}