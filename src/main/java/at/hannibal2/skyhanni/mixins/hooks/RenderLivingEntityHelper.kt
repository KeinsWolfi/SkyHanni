package at.hannibal2.skyhanni.mixins.hooks

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.RenderEntityOutlineEvent
import at.hannibal2.skyhanni.events.minecraft.SkyHanniRenderWorldEvent
import at.hannibal2.skyhanni.events.minecraft.SkyHanniTickEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.test.SkyHanniDebugsAndTests
import at.hannibal2.skyhanni.utils.SpecialColor.toSpecialColorInt
import at.hannibal2.skyhanni.utils.collection.CollectionUtils.removeIfKey
import at.hannibal2.skyhanni.utils.expand
import at.hannibal2.skyhanni.utils.render.WorldRenderUtils.drawEdges
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import org.lwjgl.opengl.GL11
import java.awt.Color
//#if MC < 1.21
import net.minecraftforge.client.event.RenderLivingEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
//#endif

@SkyHanniModule
object RenderLivingEntityHelper {

    private val config get() = SkyHanniMod.feature.gui.chroma.chamsChromaConfig

    private val entityColorMap = mutableMapOf<EntityLivingBase, () -> Color>()
    private val entityColorCondition = mutableMapOf<EntityLivingBase, () -> Boolean>()

    private val entityNoHurtTimeCondition = mutableMapOf<EntityLivingBase, () -> Boolean>()

    @JvmStatic
    var areMobsHighlighted = false
    @JvmStatic
    var currentGlowEvent: RenderEntityOutlineEvent? = null

    private fun isEntityInGlowEvent(entity: Entity): Int {
        return currentGlowEvent?.entitiesToOutline?.get(entity)?.rgb ?: 0
    }

    @JvmStatic
    fun check() {
        areMobsHighlighted = false
        val conditions = entityColorCondition.values
        for (entry in conditions) {
            if (entry.invoke()) {
                areMobsHighlighted = true
                return
            }
        }
        if (currentGlowEvent?.entitiesToOutline?.isNotEmpty() == true) areMobsHighlighted = true
    }

    @JvmStatic
    fun getEntityGlowColor(entity: Entity): Int? {
        val livingEntity = entity as? EntityLivingBase ?: return null
        val color = internalSetColorMultiplier(livingEntity, 0)
        if (color == 0) {
            val eventColor = isEntityInGlowEvent(entity)
            if (eventColor == 0) {
                return null
            }
            return eventColor
        }
        return color
    }

    private val entityChamsMap = mutableMapOf<EntityLivingBase, () -> Boolean>()
    private val entityEspMap = mutableMapOf<EntityLivingBase, () -> Boolean>()

    private const val CHROMA_COLOR = "249:255:255:85:85"

    @HandleEvent
    fun onWorldChange() {
        entityColorMap.clear()
        entityColorCondition.clear()

        entityNoHurtTimeCondition.clear()

        entityChamsMap.clear()
        entityEspMap.clear()
    }

    @HandleEvent(SkyHanniTickEvent::class)
    fun onTick() {
        entityColorMap.removeIfKey { it.isDead }
        entityColorCondition.removeIfKey { it.isDead }
        entityNoHurtTimeCondition.removeIfKey { it.isDead }
    }

    fun <T : EntityLivingBase> removeEntityColor(entity: T) {
        entityColorMap.remove(entity)
        entityColorCondition.remove(entity)
    }

    fun <T : EntityLivingBase> setEntityColor(entity: T, color: Color, condition: () -> Boolean) {
        if (color.rgb == 0) return
        entityColorMap[entity] = { color }
        entityColorCondition[entity] = condition
    }

    private fun <T : EntityLivingBase> setEntityNoHurtTime(entity: T, condition: () -> Boolean) {
        entityNoHurtTimeCondition[entity] = condition
    }

    fun <T : EntityLivingBase> setEntityColorWithNoHurtTime(entity: T, color: Color, condition: () -> Boolean) {
        setEntityColor(entity, color, condition)
        setEntityNoHurtTime(entity, condition)
    }

    fun <T : EntityLivingBase> setEntityColorWithNoHurtTimeChroma(entity: T, condition: () -> Boolean) {
        entityColorMap[entity] = { config.color }
        entityColorCondition[entity] = condition
        entityNoHurtTimeCondition[entity] = condition
    }

    fun <T : EntityLivingBase> removeNoHurtTime(entity: T) {
        entityNoHurtTimeCondition.remove(entity)
    }

    fun <T : EntityLivingBase> removeCustomRender(entity: T) {
        removeEntityColor(entity)
        removeNoHurtTime(entity)
    }

    fun <T : EntityLivingBase> setEntityChams(entity: T, condition: () -> Boolean) {
        entityChamsMap[entity] = condition
    }

    fun <T : EntityLivingBase> setEntityEsp(entity: T, condition: () -> Boolean) {
        entityEspMap[entity] = condition
    }

    @JvmStatic
    fun <T : EntityLivingBase> internalSetColorMultiplier(entity: T, default: Int): Int {
        if (!SkyHanniDebugsAndTests.globalRender) return default
        if (entityColorMap.containsKey(entity)) {
            val condition = entityColorCondition[entity] ?: return default
            if (condition.invoke()) {
                return entityColorMap[entity]?.invoke()?.rgb ?: return default
            }
        }
        return default
    }

    @JvmStatic
    fun <T : EntityLivingBase> internalChangeHurtTime(entity: T): Int {
        if (!SkyHanniDebugsAndTests.globalRender) return entity.hurtTime
        run {
            val condition = entityNoHurtTimeCondition[entity] ?: return@run
            if (condition.invoke()) {
                return 0
            }
        }
        return entity.hurtTime
    }

    @JvmStatic
    fun <T : EntityLivingBase> internalChams(entity: T): Boolean {
        if (!SkyHanniDebugsAndTests.globalRender) return false
        if (entityChamsMap.containsKey(entity)) {
            val condition = entityChamsMap[entity]!!
            if (condition.invoke()) {
                return true
            }
        }
        return false
    }

    //#if MC < 1.21
    @SubscribeEvent
    fun onRenderLivingEntities(event: RenderLivingEvent.Pre<*>) {
        val entity = event.entity
        if (!internalChams(entity)) return
        GlStateManager.depthFunc(GL11.GL_ALWAYS)
        GL11.glPolygonOffset(1.0F, -1100000.0F)
    }

    @SubscribeEvent
    fun onRenderLivingEntities(event: RenderLivingEvent.Post<*>) {
        val entity = event.entity
        if (!internalChams(entity)) return
        GlStateManager.depthFunc(GL11.GL_LEQUAL)
        GL11.glPolygonOffset(1.0F, 1100000.0F)
    }
    //#endif

    @HandleEvent
    fun onRenderWorld(event: SkyHanniRenderWorldEvent) {
        val color = (255 shl 24) or (CHROMA_COLOR.toSpecialColorInt() and 0xFFFFFF)
        for ((entity, condition) in entityEspMap) {
            if (condition.invoke()) {
                event.drawEdges(
                    entity.entityBoundingBox.expand(0.2),
                    Color(color, true),
                    2,
                    false
                )
            }
        }
    }
}
