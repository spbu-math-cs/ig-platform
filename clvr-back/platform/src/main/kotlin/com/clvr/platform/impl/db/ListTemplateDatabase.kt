package com.clvr.platform.impl.db

import com.clvr.platform.api.Template
import com.clvr.platform.api.TemplateHeader
import com.clvr.platform.api.TemplateId
import com.clvr.platform.api.db.TemplateDatabase

class ListTemplateDatabase: TemplateDatabase {
    private val templates = mutableListOf<Template>()

    override fun <T : Template> addTemplate(template: T, serializer: (T) -> String) {
        templates += template
    }

    override fun removeTemplateById(templateId: TemplateId) {
        templates.removeIf { template -> template.id == templateId }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Template> getTemplatesById(templateId: TemplateId, deserializer: (String) -> T): T? {
        val res = templates.singleOrNull { template -> template.id == templateId }
        return res as? T
    }

    override fun listTemplates(activityId: String): List<TemplateHeader> {
        return templates
            .filter { it.id.activityId == activityId }
            .map { it.header }
            .toList()
    }
}