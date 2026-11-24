/*
 * This source file is part of BetterModel.
 * Copyright (c) 2026 toxicity188
 * Licensed under the MIT License.
 * See LICENSE.md file for full license text.
 */

package kr.toxicity.model.manager

import kr.toxicity.model.api.bone.BoneName
import kr.toxicity.model.api.bone.BoneRenderContext
import kr.toxicity.model.api.data.Float3
import kr.toxicity.model.api.data.blueprint.BlueprintElement
import kr.toxicity.model.api.data.blueprint.ModelBlueprint
import kr.toxicity.model.api.data.raw.ModelResolution
import kr.toxicity.model.api.data.renderer.RenderSource
import kr.toxicity.model.api.platform.PlatformItemStack
import kr.toxicity.model.api.platform.PlatformLocation
import kr.toxicity.model.api.skin.SkinBoneBinding
import kr.toxicity.model.api.skin.SkinData
import kr.toxicity.model.api.skin.SkinPart
import kr.toxicity.model.api.util.TransformedItemStack
import org.joml.Vector3f
import java.lang.reflect.Proxy
import java.nio.file.Files
import java.util.UUID
import kotlin.test.*

class SkinRigTest {
    private val bone = BlueprintElement.Group(UUID.randomUUID(), BoneName(emptySet(), "customshoulder", "customshoulder"),
        Float3.ZERO, Float3.ZERO, emptyList(), true)
    private val blueprint = ModelBlueprint("fixture", ModelResolution(64, 64), emptyList(), listOf(bone), emptyMap())

    private fun load(json: String): Map<String, SkinBoneBinding> {
        val file = Files.createTempFile("skin-rig", ".json").toFile()
        try {
            file.writeText(json)
            return SkinRig.load(file, blueprint)
        } finally {
            file.delete()
        }
    }

    @Test fun arbitraryBoneNamesKeepTheirIdentity() {
        val name = bone.name()
        val binding = load("""{"version":1,"bones":{"CustomShoulder":{"part":"right_arm","offset":[1,-5,2]}}}""")
        assertEquals(SkinPart.RIGHT_ARM, binding.getValue("customshoulder").part())
        assertEquals(1F, binding.getValue("customshoulder").scale())
        assertSame(name, bone.name())
        assertEquals("customshoulder", bone.name().rawName())
    }

    @Test fun missingConfigurationKeepsExistingModels() {
        val file = Files.createTempDirectory("no-skin-rig").resolve("missing.json").toFile()
        try { assertTrue(SkinRig.load(file, blueprint).isEmpty()) }
        finally { file.parentFile.delete() }
    }

    @Test fun rejectsUnknownBones() {
        assertFailsWith<IllegalArgumentException> { load("""{"version":1,"bones":{"Typo":{"part":"HEAD"}}}""") }
    }

    @Test fun rejectsDuplicateCanonicalNames() {
        assertFailsWith<IllegalArgumentException> { load("""{"version":1,"bones":{"CustomShoulder":{"part":"HEAD"},"customshoulder":{"part":"BODY"}}}""") }
    }

    @Test fun rejectsUnknownPartsAndVersions() {
        assertFailsWith<IllegalArgumentException> { load("""{"version":1,"bones":{"CustomShoulder":{"part":"TAIL"}}}""") }
        assertFailsWith<IllegalArgumentException> { load("""{"version":2,"bones":{}}""") }
    }

    @Test fun rejectsMalformedOffsetsAndScales() {
        assertFailsWith<IllegalArgumentException> { load("""{"version":1,"bones":{"CustomShoulder":{"part":"HEAD","offset":[1,2]}}}""") }
        assertFailsWith<IllegalArgumentException> { SkinBoneBinding(SkinPart.HEAD, Float.NaN, 0F, 0F, 1F) }
        assertFailsWith<IllegalArgumentException> { SkinBoneBinding(SkinPart.HEAD, 0F, 0F, 0F, 0F) }
    }

    @Test fun convertsOffsetsWithoutMutatingSharedSkinItems() {
        val stack = Proxy.newProxyInstance(PlatformItemStack::class.java.classLoader,
            arrayOf(PlatformItemStack::class.java)) { _, _, _ -> error("Item methods must not be called") } as PlatformItemStack
        val location = Proxy.newProxyInstance(PlatformLocation::class.java.classLoader,
            arrayOf(PlatformLocation::class.java)) { _, _, _ -> error("Location methods must not be called") } as PlatformLocation
        val cached = TransformedItemStack(Vector3f(1F), Vector3f(0.25F), Vector3f(1F), stack)
        var requested: SkinPart? = null
        val skin = Proxy.newProxyInstance(SkinData::class.java.classLoader, arrayOf(SkinData::class.java)) { _, method, args ->
            check(method.name == "fullPart")
            requested = args[0] as SkinPart
            cached
        } as SkinData
        val mapper = SkinBoneBinding(SkinPart.BODY, 2F, -4F, 8F, 2F).toItemMapper()
        val item = mapper.apply(BoneRenderContext(RenderSource.of(location), skin), cached)
        assertEquals(SkinPart.BODY, requested)
        assertEquals(Vector3f(0.375F, 0.25F, 0F), item.offset())
        assertEquals(Vector3f(2F), item.scale())
        assertEquals(Vector3f(0.25F), cached.offset())
        assertEquals(Vector3f(1F), cached.scale())
        assertNotSame(cached.position(), item.position())
    }
}
