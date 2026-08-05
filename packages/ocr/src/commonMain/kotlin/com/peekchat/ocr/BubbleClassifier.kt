package com.peekchat.ocr

import com.peekchat.model.BubblePosition
import com.peekchat.model.TextLine

/**
 * 气泡归属判断器。
 *
 * 策略：位置推断（主） + 颜色辅助（辅）
 * - 水平中心点 < 屏幕宽度 50% → LEFT (对方)
 * - 水平中心点 > 屏幕宽度 50% → RIGHT (自己)
 */
class BubbleClassifier {

    /**
     * 对单条文本行判断气泡位置。
     * @param line 文本行（含 bounding box）
     * @param imageWidth 截图宽度 (px)
     * @return 气泡水平位置
     */
    fun classify(line: TextLine, imageWidth: Int): BubblePosition {
        val centerX = (line.left + line.right) / 2f
        return if (centerX < imageWidth / 2f) {
            BubblePosition.LEFT
        } else {
            BubblePosition.RIGHT
        }
    }
}
