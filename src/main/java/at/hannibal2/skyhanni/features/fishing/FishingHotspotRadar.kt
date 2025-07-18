package at.hannibal2.skyhanni.features.fishing

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.ClickType
import at.hannibal2.skyhanni.data.IslandGraphs
import at.hannibal2.skyhanni.data.IslandGraphs.pathFind
import at.hannibal2.skyhanni.data.model.GraphNodeTag
import at.hannibal2.skyhanni.events.ItemClickEvent
import at.hannibal2.skyhanni.events.ReceiveParticleEvent
import at.hannibal2.skyhanni.events.SecondPassedEvent
import at.hannibal2.skyhanni.events.minecraft.SkyHanniRenderWorldEvent
import at.hannibal2.skyhanni.events.minecraft.SkyHanniTickEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.DelayedRun
import at.hannibal2.skyhanni.utils.EntityUtils
import at.hannibal2.skyhanni.utils.EntityUtils.cleanName
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalNameOrNull
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.NeuInternalName.Companion.toInternalName
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.ParticlePathBezierFitter
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.SkyBlockUtils
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.SystemNotificationsUtils
import at.hannibal2.skyhanni.utils.compat.MinecraftCompat
import at.hannibal2.skyhanni.utils.render.WorldRenderUtils.drawDynamicText
import at.hannibal2.skyhanni.utils.render.WorldRenderUtils.drawLineToEye
import at.hannibal2.skyhanni.utils.render.WorldRenderUtils.exactPlayerEyeLocation
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.util.EnumParticleTypes
import kotlin.time.Duration.Companion.seconds


@SkyHanniModule
object FishingHotspotRadar {
    private val config get() = SkyHanniMod.feature.fishing

    private var lastParticle = SimpleTimeMark.farPast()
    private var lastAbilityUse = SimpleTimeMark.farPast()
    private val bezierFitter = ParticlePathBezierFitter(3)
    private var hotspotLocation: LorenzVec? = null
    private val HOTSPOT_RADAR = "HOTSPOT_RADAR".toInternalName()
    private var foundTime = SimpleTimeMark.farPast()
    private var lastUpdate = SimpleTimeMark.farPast()
    private var isUnknown = false

    // Entity ID -> (Location, (Name, FirstSeenTimestampMillis))
    private var mobCache = mutableMapOf<Int, Pair<LorenzVec, Pair<String, Long>>>()

    @HandleEvent(receiveCancelled = true, onlyOnSkyblock = true)
    fun onReceiveParticle(event: ReceiveParticleEvent) {
        if (!isEnabled()) return
        val type = event.type
        if (type != EnumParticleTypes.FLAME) return
        if (event.count != 1 || event.speed != 0f) return

        lastParticle = SimpleTimeMark.now()
        val currLoc = event.location

        if (lastAbilityUse.passedSince() > 1.seconds) return
        if (bezierFitter.isEmpty()) {
            bezierFitter.addPoint(currLoc)
            return
        }
        val distToLast = bezierFitter.getLastPoint()?.distance(currLoc) ?: return

        if (distToLast == 0.0 || distToLast > 3.0) return

        bezierFitter.addPoint(currLoc)

        val guess = bezierFitter.solve() ?: return
        if (!SkyBlockUtils.currentIsland.isInBounds(guess)) {
            hotspotLocation = null
            return
        }
        hotspotLocation = guess
        isUnknown = false
        lastUpdate = SimpleTimeMark.now()
        hotspotLocation?.let {
            DelayedRun.runNextTick { pathFind(it) }
        }
    }

    private fun pathFind(location: LorenzVec) {
        if (!config.guessHotspotRadarPathFind) return

        foundTime = SimpleTimeMark.farFuture()
        val found = IslandGraphs.findClosestNode(
            location,
            condition = { it.hasTag(GraphNodeTag.FISHING_HOTSPOT) },
            radius = 15.0,
        ) ?: run {
            isUnknown = true
            pathFind(
                location, "§cUnknown Fishing Hotspot", LorenzColor.RED.toColor(),
                condition = {
                    config.guessHotspotRadarPathFind && foundTime.passedSince() < 5.seconds
                },
            )
            return
        }
        found.pathFind(
            "§bFishing Hotspot",
            LorenzColor.AQUA.toColor(),
            condition = {
                config.guessHotspotRadarPathFind && foundTime.passedSince() < 5.seconds
            },
        )
    }

    @HandleEvent(onlyOnSkyblock = true)
    fun onSecondPassed(event: SecondPassedEvent) {
        val location = hotspotLocation ?: return
        if (!isUnknown || lastUpdate.passedSince() < 3.seconds) return
        IslandGraphs.reportLocation(
            location,
            userFacingReason = "Found no path to fishing hotspot",
            additionalInternalInfo = "no node with tag 'fishing hotspot' found near the radar hotspot target",
            ignoreCache = true,
            betaOnly = true,
        )
    }

    @HandleEvent(onlyOnSkyblock = true)
    fun onRenderWorld(event: SkyHanniRenderWorldEvent) {
        for ((_, value) in mobCache) {
            val (location, nameWithTime) = value
            val (name, _) = nameWithTime

            event.drawDynamicText(location, name, 1.0)

            if (name.removeColor().lowercase().contains("double hook")) {
                event.drawLineToEye(
                    location,
                    LorenzColor.RED.toColor(),
                    lineWidth = 2,
                    depth = false
                )
            }
        }

        val location = hotspotLocation ?: return
        val distance = location.distance(event.exactPlayerEyeLocation())
        if (config.lineToHotspot) {
            event.drawLineToEye(location, LorenzColor.LIGHT_PURPLE.toColor(), lineWidth = 3, depth = false)
        }
        if (distance > 10) {
            val formattedDistance = distance.toInt().addSeparators()
            event.drawDynamicText(location.add(-0.5, 1.7, -0.5), "§d§lHOTSPOT", 1.7)
            event.drawDynamicText(location.add(-0.5, 1.6 - distance / (12 * 1.7), -0.5), " §r§e${formattedDistance}m", 1.0)
        } else {
            reset()
            foundTime = SimpleTimeMark.now()
        }
    }

    @HandleEvent(onlyOnSkyblock = true)
    fun onUseAbility(event: ItemClickEvent) {
        if (!isEnabled()) return
        if (event.clickType != ClickType.RIGHT_CLICK) return
        val item = event.itemInHand ?: return
        if (item.getInternalNameOrNull() != HOTSPOT_RADAR) return
        if (lastParticle.passedSince() < 0.2.seconds) {
            event.cancel()
            return
        }
        bezierFitter.reset()
        lastAbilityUse = SimpleTimeMark.now()
    }

    @HandleEvent
    fun onWorldChange() {
        reset()
        foundTime = SimpleTimeMark.farPast()
        lastAbilityUse = SimpleTimeMark.farPast()
    }

    private fun reset() {
        hotspotLocation = null
        bezierFitter.reset()
        mobCache.clear()
    }

    private fun isEnabled() = SkyBlockUtils.inSkyBlock && config.guessHotspotRadar

    @HandleEvent
    fun onTick(event: SkyHanniTickEvent) {
        if (!isEnabled()) return

        val now = System.currentTimeMillis()

        val mobs = EntityUtils.getAllEntities()
            .filterIsInstance<EntityArmorStand>()

        val hotspotMobs = mobs.filter { it.cleanName() == "HOTSPOT" }

        val currentEntityIds = hotspotMobs.map { it.entityId }.toSet()

        // Remove entries no longer present → send notification
        val disappearedIds = mobCache.keys - currentEntityIds
        for (id in disappearedIds) {
            val (_, a) = mobCache[id] ?: continue
            val (name, firstSeen) = a
            //#if MC < 1.21
            val distance = MinecraftCompat.localPlayer.getDistanceSq(mobCache[id]!!.first.toBlockPos())
            //#else
            //$$ val distance = MinecraftCompat.localPlayer.squaredDistanceTo(mobCache[id]!!.first.toBlockPos().toCenterPos())
            //#endif
            if (distance < 36) {
                if (config.desktopNotification) {
                    SystemNotificationsUtils.showNotification(
                        "Fishing Hotspot Disappeared",
                        "A fishing hotspot has disappeared."
                    )
                }
            }
            mobCache.remove(id)
        }

        // Remove entries that are too old → no notification
        mobCache.entries.removeIf { (_, entry) ->
            val (_, a) = entry
            val (_, firstSeen) = a
            now - firstSeen > 2 * 60 * 1000 // 2 minutes
        }

        // Add/update entities
        for (mob in hotspotMobs) {
            val id = mob.entityId
            val location = LorenzVec(mob.posX, mob.posY, mob.posZ)

            val name = mobs
                .filter { it.entityId != mob.entityId }
                .minByOrNull {
                    //#if MC < 1.21
                    it.getDistanceToEntity(mob)
                    //#else
                    //$$ it.distanceTo(mob)
                    //#endif
                }
                ?.name ?: "Unknown Hotspot"

            val firstSeen = mobCache[id]?.second?.second ?: now
            mobCache[id] = location to (name to firstSeen)
        }
    }


}
