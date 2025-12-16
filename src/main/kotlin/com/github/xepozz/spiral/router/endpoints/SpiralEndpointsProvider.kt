package com.github.xepozz.spiral.router.endpoints

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
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiModificationTracker
import com.jetbrains.php.lang.PhpLangUtil
import com.jetbrains.php.lang.PhpLanguage

class SpiralEndpointsProvider : EndpointsProvider<SpiralGroup, SpiralEndpoint> {
    override val endpointType: EndpointType = HTTP_SERVER_TYPE
    override val presentation = FrameworkPresentation("Spiral", "Spiral Framework", SpiralIcons.SPIRAL)

    override fun getStatus(project: Project) = EndpointsProvider.Status.AVAILABLE

    override fun getModificationTracker(project: Project) =
        PsiModificationTracker.getInstance(project).forLanguage(PhpLanguage.INSTANCE)

    override fun getEndpointGroups(project: Project, filter: EndpointsFilter): Iterable<SpiralGroup> {
        if (filter !is ModuleEndpointsFilter) return emptyList()

        return RouterIndexUtil
            .getAllRoutes(project)
            .flatMap { route -> route.methods.map { SpiralEndpoint(route.uri, it, route.fqn, route.group ?: "Root") } }
            .sortedBy { it.url }
            .groupBy { it.group }
            .map { (group, routes) -> SpiralGroup(project, group, routes) }
            .apply { println("groups $this ${this.map { it.group to it.routes.size }}") }
    }

    override fun getEndpoints(group: SpiralGroup) = group.routes

    override fun isValidEndpoint(group: SpiralGroup, endpoint: SpiralEndpoint) = true//group.isValid()

    override fun getDocumentationElement(group: SpiralGroup, endpoint: SpiralEndpoint): PsiElement? {
        return RouterIndexUtil.getControllerMethodByFqn(group.project, endpoint.fqn).firstOrNull()
    }

    override fun getEndpointPresentation(group: SpiralGroup, endpoint: SpiralEndpoint): ItemPresentation {
        return HttpMethodPresentation(
            endpoint.url,
            endpoint.method,
            PhpLangUtil.toPresentableFQN(endpoint.fqn.replace(".", "::")),
            SpiralIcons.SPIRAL,
        )
    }

    override fun getNavigationElement(group: SpiralGroup, endpoint: SpiralEndpoint): PsiElement? {
        return RouterIndexUtil.getControllerMethodByFqn(group.project, endpoint.fqn).firstOrNull()
    }
}