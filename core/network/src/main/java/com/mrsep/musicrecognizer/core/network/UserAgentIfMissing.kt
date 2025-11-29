package com.mrsep.musicrecognizer.core.network

import io.ktor.client.plugins.api.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.utils.io.*

@KtorDsl
internal class UserAgentIfMissingConfig(var agent: String = "Ktor http-client")

/**
 * Adds a `User-Agent` header to requests only when the request doesn't already have it.
 */
internal val UserAgentIfMissing: ClientPlugin<UserAgentIfMissingConfig> =
    createClientPlugin("UserAgent", ::UserAgentIfMissingConfig) {

        val agent = pluginConfig.agent

        onRequest { request, _ ->
            if (request.headers[HttpHeaders.UserAgent] == null) {
                request.header(HttpHeaders.UserAgent, agent)
            }
        }
    }
