package com.clvr.platform.api.db

import com.clvr.platform.api.Template
import com.clvr.platform.api.TemplateHeader
import com.clvr.platform.api.TemplateId
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

interface TemplateDatabase: AutoCloseable {
    fun <T: Template> addTemplate(template: T, serializer: (T) -> String)

    fun removeTemplateById(templateId: TemplateId)

    fun <T: Template> getTemplatesById(templateId: TemplateId, deserializer: (String) -> T): T?

    fun listTemplates(activityId: String): List<TemplateHeader>

    override fun close() { }
}

inline fun <reified T: Template> TemplateDatabase.addTemplate(template: T) {
    addTemplate(template, Json::encodeToString)
}

inline fun <reified T: Template> TemplateDatabase.getTemplatesById(templateId: TemplateId): T? {
    return getTemplatesById(templateId, Json::decodeFromString)
}

inline fun <reified T: Template> TemplateDatabase.preloadTemplates(files: List<File>) {
    files
        .map { file -> Json.decodeFromString<List<T>>(file.readText()) }
        .flatten()
        .forEach { template -> addTemplate<T>(template) }
}