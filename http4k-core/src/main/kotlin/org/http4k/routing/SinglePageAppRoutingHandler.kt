package org.http4k.routing

import org.http4k.core.Filter
import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status

internal data class SinglePageAppRoutingHandler(
    private val pathSegments: String,
    private val staticHandler: StaticRoutingHttpHandler
) : RoutingHttpHandler {

    override fun invoke(request: Request): Response {
        val matchOnStatic = when (val matchResult = staticHandler.match(request)) {
            is RouterMatch.MatchingHandler -> matchResult(request)
            else -> null
        }

        val matchOnIndex: HttpHandler? = when (val matchResult = staticHandler.match(Request(Method.GET, pathSegments))) {
            is RouterMatch.MatchingHandler -> matchResult
            else -> null
        }

        val fallbackHandler = matchOnIndex ?: { Response(Status.NOT_FOUND) }
        return matchOnStatic ?: fallbackHandler(Request(Method.GET, pathSegments))
    }

    override fun match(request: Request) = RouterMatch.MatchingHandler(this)

    override fun withFilter(new: Filter) = copy(staticHandler = staticHandler.withFilter(new) as StaticRoutingHttpHandler)

    override fun withBasePath(new: String) = SinglePageAppRoutingHandler(new + pathSegments, staticHandler.withBasePath(new) as StaticRoutingHttpHandler)
}
