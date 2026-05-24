package com.github.xepozz.spiral.router.endpoints

import com.intellij.openapi.project.Project
import java.lang.ref.WeakReference

/**
 * Represents a single Spiral router group surfaced in the Endpoints tool window.
 *
 * The owning [Project] is held through a [WeakReference] so this value object
 * (handed to the platform via `EndpointsProvider`) cannot keep a disposed
 * project alive after a plugin reload or project close.
 *
 * Equality and hashCode intentionally exclude the project reference so the
 * value object behaves like a normal data class for tool-window diffing.
 */
class SpiralGroup(
    project: Project,
    val group: String,
    val routes: Collection<SpiralEndpoint>,
) {
    private val projectRef: WeakReference<Project> = WeakReference(project)

    /** The owning project, or `null` if it has been garbage-collected. */
    val projectOrNull: Project?
        get() = projectRef.get()

    /**
     * Convenience accessor for callers that have already verified the project
     * is alive. Throws [IllegalStateException] if the project was collected.
     */
    val project: Project
        get() = projectRef.get()
            ?: error("SpiralGroup project reference has been garbage-collected")

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SpiralGroup) return false
        return group == other.group && routes == other.routes
    }

    override fun hashCode(): Int = 31 * group.hashCode() + routes.hashCode()

    override fun toString(): String = "SpiralGroup(group='$group', routes=${routes.size})"
}
