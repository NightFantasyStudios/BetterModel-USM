/*
 * This source file is part of BetterModel.
 * Copyright (c) 2026 toxicity188
 * Licensed under the MIT License.
 * See LICENSE.md file for full license text.
 */

package kr.toxicity.model.api.skin;

import kr.toxicity.model.api.bone.BoneItemMapper;
import kr.toxicity.model.api.bone.BoneRenderContext;
import kr.toxicity.model.api.platform.PlatformItemTransform;
import kr.toxicity.model.api.util.TransformedItemStack;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.Objects;

/**
 * Assigns a complete skin piece to an existing bone without changing its identity or pivot.
 * Offsets use Blockbench model pixels (positive X right, Y up, Z back) relative to that pivot.
 * Example: {@code new SkinBoneBinding(SkinPart.BODY, 0, -5, 0, 1).toItemMapper()}.
 * @param part complete skin part
 * @param x local center offset X in model pixels
 * @param y local center offset Y in model pixels
 * @param z local center offset Z in model pixels
 * @param scale uniform piece scale; does not scale the rig or its children
 * @since 3.5.0
 */
public record SkinBoneBinding(@NotNull SkinPart part, float x, float y, float z, float scale) {
    /**
     * Validates a binding. Example: {@code new SkinBoneBinding(SkinPart.HEAD, 0, 4, 0, 1)}.
     * @throws IllegalArgumentException if offsets are non-finite or scale is not finite and positive
     * @throws NullPointerException if part is null
     */
    public SkinBoneBinding {
        Objects.requireNonNull(part);
        if (!Float.isFinite(x) || !Float.isFinite(y) || !Float.isFinite(z)
                || !Float.isFinite(scale) || scale <= 0) {
            throw new IllegalArgumentException("Skin binding requires finite offsets and a positive scale");
        }
    }

    /**
     * Creates a mapper usable by an original bone. Example: {@code binding.toItemMapper()}.
     * @return fixed item mapper using the current render context's skin
     */
    public @NotNull BoneItemMapper toItemMapper() {
        return new BoneItemMapper() {
            @Override
            public @NotNull PlatformItemTransform transform() {
                return PlatformItemTransform.FIXED;
            }

            @Override
            public @NotNull TransformedItemStack apply(@NotNull BoneRenderContext context,
                                                       @NotNull TransformedItemStack original) {
                var item = context.skin().fullPart(part);
                return TransformedItemStack.of(new Vector3f(item.position()),
                        new Vector3f(item.offset()).mul(scale).add(-x / 16F, y / 16F, -z / 16F),
                        new Vector3f(item.scale()).mul(scale), item.itemStack());
            }
        };
    }
}
