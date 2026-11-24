/*
 * This source file is part of BetterModel.
 * Copyright (c) 2025 toxicity188
 * Licensed under the MIT License.
 * See LICENSE.md file for full license text.
 */

package kr.toxicity.model.api.bone;

import com.google.gson.JsonDeserializer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NonNull;
import kr.toxicity.model.api.platform.PlatformItemTransform;
import kr.toxicity.model.api.player.PlayerLimb;
import kr.toxicity.model.api.util.TransformedItemStack;
import org.joml.Vector3f;

import java.util.Objects;
import java.util.Set;

/**
 * A tagged name of some bone
 * @param tags tags
 * @param name name
 * @param rawName original name
 */
public record BoneName(
    @NotNull @Unmodifiable Set<BoneTag> tags,
    @NotNull String name,
    @NotNull String rawName
) implements Comparable<BoneName> {

    /**
     * A JSON deserializer for parsing BoneName from a string.
     * @since 2.0.1
     */
    public static final JsonDeserializer<BoneName> PARSER = (json, _, _) -> BoneName.of(json.getAsString());

    /**
     * Internal constructor for BoneName.
     */
    @ApiStatus.Internal
    public BoneName {
    }

    /**
     * Creates a new BoneName by parsing the raw name string.
     * @param rawName the raw string to parse
     * @since 2.0.1
     * @return a parsed BoneName instance
     */
    public static @NotNull BoneName of(@NotNull String rawName) {
        return BoneTag.REGISTRY.parse(rawName);
    }

    /**
     * Checks this name has some tags
     * @param tags tags
     * @return any match
     */
    public boolean tagged(@NotNull BoneTag... tags) {
        for (BoneTag boneTag : tags) {
            if (this.tags.contains(boneTag)) return true;
        }
        return false;
    }

    /**
     * Gets an item mapper of this bone name.
     * Skin limb names with the {@code px} tag, such as {@code px_ph_head}, retain
     * full-pixel rig proportions instead of BetterModel's default 0.9375 skin scale.
     * @return item mapper
     */
    public @NotNull BoneItemMapper toItemMapper() {
        var mapper = tags.isEmpty() ? BoneItemMapper.EMPTY : tags.stream().map(BoneTag::itemMapper).filter(Objects::nonNull).findFirst().orElse(BoneItemMapper.EMPTY);
        if (!tagged(BoneTags.PLAYER_PIXEL) || !(mapper instanceof PlayerLimb.LimbItemMapper)) return mapper;
        return new BoneItemMapper() {
            @Override
            public @NotNull PlatformItemTransform transform() {
                return mapper.transform();
            }

            @Override
            public @NotNull TransformedItemStack apply(
                    @NotNull BoneRenderContext context,
                    @NotNull TransformedItemStack original) {
                var item = mapper.apply(context, original);
                return TransformedItemStack.of(new Vector3f(item.position()),
                        new Vector3f(item.offset()).div(0.9375F),
                        new Vector3f(item.scale()).div(0.9375F), item.itemStack());
            }
        };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BoneName boneName)) return false;
        return rawName.equals(boneName.rawName);
    }

    @Override
    public int compareTo(@NonNull BoneName o) {
        return rawName.compareTo(o.rawName);
    }

    @Override
    public int hashCode() {
        return rawName.hashCode();
    }

    @Override
    public @NotNull String toString() {
        return rawName;
    }
}
