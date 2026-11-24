/*
 * This source file is part of BetterModel.
 * Copyright (c) 2026 toxicity188
 * Licensed under the MIT License.
 * See LICENSE.md file for full license text.
 */

package kr.toxicity.model.manager

import com.google.gson.JsonParser
import kr.toxicity.model.api.data.blueprint.BlueprintElement
import kr.toxicity.model.api.data.blueprint.ModelBlueprint
import kr.toxicity.model.api.skin.SkinBoneBinding
import kr.toxicity.model.api.skin.SkinPart
import java.io.File
import java.util.Locale

/** Per-model bindings, independent of bone tags and native model editing. */
internal object SkinRig {
    fun load(file: File, blueprint: ModelBlueprint): Map<String, SkinBoneBinding> {
        if (!file.isFile) return emptyMap()
        val json = file.reader(Charsets.UTF_8).use { JsonParser.parseReader(it).asJsonObject }
        require(json.get("version")?.asInt == 1) { "Unsupported skin rig version: $file" }
        val bones = mutableSetOf<String>()
        fun collect(elements: Collection<BlueprintElement>) {
            elements.forEach {
                if (it is BlueprintElement.Bone) bones += it.name().rawName().lowercase(Locale.ROOT)
                if (it is BlueprintElement.Group) collect(it.children())
            }
        }
        collect(blueprint.elements)
        val mapped = mutableSetOf<String>()
        return json.getAsJsonObject("bones").entrySet().associate { (bone, element) ->
            // BetterModel canonicalizes bone names on import; accept native spelling.
            val canonical = bone.lowercase(Locale.ROOT)
            require(canonical in bones) { "Unknown bone '$bone' in $file" }
            require(mapped.add(canonical)) { "Duplicate bone '$bone' in $file" }
            val binding = element.asJsonObject
            val part = SkinPart.valueOf(binding.get("part").asString.uppercase(Locale.ROOT))
            val offset = binding.getAsJsonArray("offset")
            require(offset == null || offset.size() == 3) { "Offset requires three model pixels for '$bone' in $file" }
            canonical to SkinBoneBinding(part, offset?.get(0)?.asFloat ?: 0F,
                offset?.get(1)?.asFloat ?: 0F, offset?.get(2)?.asFloat ?: 0F,
                binding.get("scale")?.asFloat ?: 1F)
        }
    }
}
