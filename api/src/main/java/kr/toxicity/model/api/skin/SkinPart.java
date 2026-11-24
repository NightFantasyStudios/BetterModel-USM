/*
 * This source file is part of BetterModel.
 * Copyright (c) 2026 toxicity188
 * Licensed under the MIT License.
 * See LICENSE.md file for full license text.
 */

package kr.toxicity.model.api.skin;

/**
 * Complete, rigid skin pieces for rigs without elbow, knee or torso segmentation.
 * Each piece is centered at zero and uses one model pixel per skin pixel.
 * Example: {@code skin.fullPart(SkinPart.BODY)}.
 * @since 3.5.0
 */
public enum SkinPart {
    /** Complete head, including the hat layer. */
    HEAD,
    /** Complete torso, including the jacket layer. */
    BODY,
    /** Complete right arm; respects the profile's slim flag. */
    RIGHT_ARM,
    /** Complete left arm; respects the profile's slim flag. */
    LEFT_ARM,
    /** Complete right leg, including the outer layer. */
    RIGHT_LEG,
    /** Complete left leg, including the outer layer. */
    LEFT_LEG
}
