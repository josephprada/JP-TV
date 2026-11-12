package co.jp.tv.homeoverride.domain

/** Which backend brings JP-TV forward. */
enum class DetectionMethod { ACCESSIBILITY, USAGE_STATS }

/** The special accesses the usage-stats backend needs. */
enum class MissingPermission { USAGE_ACCESS, OVERLAY }

/** What the settings row shows: the status, the active method and what is still missing. */
data class HomeOverrideState(
    val status: HomeOverrideStatus,
    val method: DetectionMethod? = null,
    val missing: List<MissingPermission> = emptyList(),
) {
    companion object {
        /** The accessibility service wins when the user has it on; otherwise the usage-stats backend needs both accesses. */
        fun of(toggleOn: Boolean, accessibilityEnabled: Boolean, usageAccess: Boolean, overlay: Boolean): HomeOverrideState = when {
            !toggleOn -> HomeOverrideState(HomeOverrideStatus.DISABLED)
            accessibilityEnabled -> HomeOverrideState(HomeOverrideStatus.ACTIVE, DetectionMethod.ACCESSIBILITY)
            usageAccess && overlay -> HomeOverrideState(HomeOverrideStatus.ACTIVE, DetectionMethod.USAGE_STATS)
            else -> HomeOverrideState(
                HomeOverrideStatus.NEEDS_PERMISSION,
                missing = listOfNotNull(
                    MissingPermission.USAGE_ACCESS.takeIf { !usageAccess },
                    MissingPermission.OVERLAY.takeIf { !overlay },
                ),
            )
        }
    }
}
