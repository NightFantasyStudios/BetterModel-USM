/*
 * This source file is part of BetterModel.
 * Copyright (c) 2025 toxicity188
 * Licensed under the MIT License.
 * See LICENSE.md file for full license text.
 */

package kr.toxicity.model.api.player;

import kr.toxicity.model.api.armor.PlayerArmor;
import kr.toxicity.model.api.bone.BoneItemMapper;
import kr.toxicity.model.api.bone.BoneRenderContext;
import kr.toxicity.model.api.nms.Profiled;
import kr.toxicity.model.api.platform.PlatformItemTransform;
import kr.toxicity.model.api.skin.SkinData;
import kr.toxicity.model.api.util.TransformedItemStack;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Player limb data
 */
@RequiredArgsConstructor
@Getter
public enum PlayerLimb {
    /**
     * Head
     */
    HEAD(
        SkinData::head,
        PlatformItemTransform.FIXED
    ),
    /**
     * Right arm
     */
    RIGHT_ARM(
        SkinData::rightArm,
        PlatformItemTransform.FIXED
    ),
    /**
     * Right forearm
     */
    RIGHT_FOREARM(
        (d, _) -> d.rightForeArm(),
        PlatformItemTransform.FIXED
    ),
    /**
     * Left arm
     */
    LEFT_ARM(
        SkinData::leftArm,
        PlatformItemTransform.FIXED
    ),
    /**
     * Left forearm
     */
    LEFT_FOREARM(
        (d, _) -> d.leftForeArm(),
        PlatformItemTransform.FIXED
    ),
    /**
     * Right arm turning about the shoulder, as vanilla's and the Better Combat/PlayerAnimationLib
     * rig's arm does: its pivot sits 2 pixels below the top of the arm and 1 pixel in from its middle
     * (half a pixel for a slim arm). {@link #RIGHT_ARM} turns about the top of the arm instead, so an
     * animation authored on that rig swung the arm about the wrong point.
     */
    RIGHT_SHOULDER_ARM(
        (d, a) -> shoulderPivot(d, d.rightArm(a), -1F),
        PlatformItemTransform.FIXED
    ),
    /**
     * Left arm turning about the shoulder, see {@link #RIGHT_SHOULDER_ARM}.
     */
    LEFT_SHOULDER_ARM(
        (d, a) -> shoulderPivot(d, d.leftArm(a), 1F),
        PlatformItemTransform.FIXED
    ),
    /**
     * Hip
     */
    HIP(
        SkinData::hip,
        PlatformItemTransform.FIXED
    ),
    /**
     * Waist
     */
    WAIST(
        SkinData::waist,
        PlatformItemTransform.FIXED
    ),
    /**
     * Chest
     */
    CHEST(
        SkinData::chest,
        PlatformItemTransform.FIXED
    ),
    /**
     * Right leg
     */
    RIGHT_LEG(
        SkinData::rightLeg,
        PlatformItemTransform.FIXED
    ),
    /**
     * Right foreleg
     */
    RIGHT_FORELEG(
        SkinData::rightForeLeg,
        PlatformItemTransform.FIXED
    ),
    /**
     * Left leg
     */
    LEFT_LEG(
        SkinData::leftLeg,
        PlatformItemTransform.FIXED
    ),
    /**
     * Left foreleg
     */
    LEFT_FORELEG(
        SkinData::leftForeLeg,
        PlatformItemTransform.FIXED
    ),
    ;

    /**
     * Moves an arm piece so its bone's origin is the shoulder. The piece hangs from its own top; the
     * offset turns with the bone, so the arm swings about the origin. Render space has x negated
     * against Blockbench's, so the right arm's outside is -x there. Offsets are in skin pixels, which
     * the limb pieces draw at 0.9375 of a model pixel.
     *
     * @param data skin
     * @param arm the arm piece
     * @param outward the sign of the arm's outside along render x
     * @return the moved piece
     */
    private static @NotNull TransformedItemStack shoulderPivot(@NotNull SkinData data, @NotNull TransformedItemStack arm, float outward) {
        var inset = data.profile().skin().slim() ? 0.5F : 1F;
        return arm.offset(new Vector3f(outward * inset, 2F, 0F).mul(SKIN_PIXEL));
    }

    private static final float SKIN_PIXEL = 0.9375F / 16F;

    private final @NotNull BiFunction<SkinData, PlayerArmor, TransformedItemStack> skinMapper;
    private final @NotNull PlatformItemTransform transform;

    @Getter
    private final @NotNull LimbItemMapper itemMapper = new LimbItemMapper(this::createItem);

    /**
     * Generates transformed item from player
     * @param context context
     * @return item
     */
    public @NotNull TransformedItemStack createItem(@NotNull BoneRenderContext context) {
        return skinMapper.apply(context.skin(), context.source() instanceof Profiled profiled ? profiled.armors() : PlayerArmor.EMPTY);
    }

    /**
     * Limb item mapper
     */
    @RequiredArgsConstructor
    public class LimbItemMapper implements BoneItemMapper {

        private final Function<BoneRenderContext, TransformedItemStack> playerMapper;

        @NotNull
        @Override
        public PlatformItemTransform transform() {
            return transform;
        }

        @Override
        public @NotNull TransformedItemStack apply(@NotNull BoneRenderContext context, @NotNull TransformedItemStack transformedItemStack) {
            return playerMapper.apply(context);
        }
    }
}
