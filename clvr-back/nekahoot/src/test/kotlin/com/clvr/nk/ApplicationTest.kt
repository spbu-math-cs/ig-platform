package com.clvr.nk

import com.clvr.nk.NeKahootInstaller.Companion.templateId
import com.clvr.nk.common.TemplateCompleteInfo
import com.clvr.platform.api.TemplateHeader
import com.clvr.platform.api.TemplateId
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import io.ktor.server.testing.*
import com.clvr.platform.configurePlatform
import com.clvr.platform.installActivity
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive

class ApplicationTest {
    @Test
    fun `basic test for API`() = testApplication {
        setupServer()
        val client = getClient()
        assertEquals(TemplateListResponse(listOf()), client.get("/nekahoot/template-list").body<TemplateListResponse>())

        val templateID = addTemplate(client)
        /*assertEquals(TemplateListResponse(listOf(
                TemplateHeader(
                        basicTestTemplate.templateTitle ?: "",
                        templateID.id,
                        basicTestTemplate.templateComment ?: ""
                ))), client.get("/nekahoot/template-list").body<TemplateListResponse>()
        )*/

        assertEquals(TemplateCompleteInfo(
                templateID.id,
                basicTestTemplate.templateTitle ?: "",
                basicTestTemplate.templateComment ?: "",
                basicTestTemplate.questions
        ), client.get("/nekahoot/template/${templateID.id}").body<TemplateCompleteInfo>())

       assertEquals(HttpStatusCode.OK, client.post("/nekahoot/game") {
           contentType(ContentType.Application.Json)
           setBody(Json.encodeToString(TemplateRequest(templateID.id)))
       }.status)
    }

    private fun ApplicationTestBuilder.setupServer() {
        application {
            configurePlatform()
            installActivity(NeKahootInstaller(listOf()))
        }
    }

    private fun ApplicationTestBuilder.getClient(): HttpClient {
        return createClient {
            install(WebSockets)
            install(ContentNegotiation) {
                json()
            }
        }
    }

    private suspend fun addTemplate(client: HttpClient): TemplateId {
        client.post("/nekahoot/template") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(TemplateCreateRequest(
                    basicTestTemplate.templateTitle ?: "",
                    basicTestTemplate.templateComment ?: "",
                    basicTestTemplate.questions
            )))
        }.apply {
            assertEquals(HttpStatusCode.OK, status)

            val jsonObject: JsonObject = Json.decodeFromString(bodyAsText())
            val template = jsonObject["template-id"] as JsonObject
            return templateId(
                template["id"]?.jsonPrimitive?.content ?: throw IllegalStateException("id cannot be null")
            )
        }
    }
}