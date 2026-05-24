package com.github.xepozz.spiral.router.endpoints

import com.github.xepozz.spiral.SpiralBundle
import com.github.xepozz.spiral.SpiralIcons
import com.github.xepozz.spiral.router.index.RouterIndexUtil
import com.intellij.microservices.endpoints.EndpointType
import com.intellij.microservices.endpoints.EndpointsFilter
import com.intellij.microservices.endpoints.EndpointsProvider
import com.intellij.microservices.endpoints.FrameworkPresentation
import com.intellij.microservices.endpoints.HTTP_SERVER_TYPE
import com.intellij.microservices.endpoints.ModuleEndpointsFilter
import com.intellij.microservices.endpoints.presentation.HttpMethodPresentation
import com.intellij.navigation.ItemPresentation
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiModificationTracker
import com.jetbrains.php.lang.PhpLanguage

class SpiralEndpointsProvider : EndpointsProvider<SpiralGroup, SpiralEndpoint> {
    override val endpointType: EndpointType = HTTP_SERVER_TYPE
    override val presentation = FrameworkPresentation("Spiral", "Spiral Framework", SpiralIcons.SPIRAL)

    override fun getStatus(project: Project) = EndpointsProvider.Status.AVAILABLE

    override fun getModificationTracker(project: Project) =
        PsiModificationTracker.getInstance(project).forLanguage(PhpLanguage.INSTANCE)

    override fun getEndpointGroups(project: Project, filter: EndpointsFilter): Iterable<SpiralGroup> {
        if (filter !is ModuleEndpointsFilter) return emptyList()
        if (DumbService.isDumb(project)) return emptyList()

        val rootLabel = SpiralBundle.message("endpoints.group.root")
        return RouterIndexUtil
            .getAllRoutes(project)
            .flatMap { route -> route.methods.map { SpiralEndpoint(route.uri, it, route.fqn, route.group ?: rootLabel) } }
            .sortedBy { it.url }
            .groupBy { it.group }
            .map { (group, routes) -> SpiralGroup(project, group, routes) }
    }

    override fun getEndpoints(group: SpiralGroup) = group.routes

    override fun isValidEndpoint(group: SpiralGroup, endpoint: SpiralEndpoint): Boolean {
        // Endpoint is valid as long as it still belongs to the group and the
        // owning project hasn't been disposed (the platform may retain group
        // references across reloads via SpiralGroup's WeakReference).
        return group.projectOrNull != null && endpoint in group.routes
    }

    override fun getDocumentationElement(group: SpiralGroup, endpoint: SpiralEndpoint): PsiElement? {
        val project = group.projectOrNull ?: return null
        return RouterIndexUtil.getControllerMethodByFqn(project, endpoint.fqn).firstOrNull()
    }

    override fun getEndpointPresentation(group: SpiralGroup, endpoint: SpiralEndpoint): ItemPresentation {
        return HttpMethodPresentation(
            endpoint.url,
            endpoint.method,
            RouterIndexUtil.toPresentableFqn(endpoint.fqn),
            SpiralIcons.SPIRAL,
        )
    }

    override fun getNavigationElement(group: SpiralGroup, endpoint: SpiralEndpoint): PsiElement? {
        val project = group.projectOrNull ?: return null
        return RouterIndexUtil.getControllerMethodByFqn(project, endpoint.fqn).firstOrNull()
    }
}
