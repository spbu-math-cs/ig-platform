package com.clvr.platform.impl.db.template

import com.clvr.platform.api.Template
import com.clvr.platform.api.TemplateHeader
import com.clvr.platform.api.TemplateId
import com.clvr.platform.api.db.TemplateDatabase
import com.clvr.platform.impl.db.DBQueryExecutor
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

internal class PostgresTemplateDatabase(private val db: DBQueryExecutor) : TemplateDatabase {
    companion object {
        // TODO: make PRIVATE KEY = (activity_id, uuid) (?)
        // TODO: remove header as separate column and take it from template column via JSON-oriented SQL API
        private val createTableQuery: String = """
            CREATE TABLE IF NOT EXISTS template (
                uuid TEXT PRIMARY KEY,
                activity_id TEXT NOT NULL,
                header JSON NOT NULL,
                template JSON NOT NULL
            );
        """.trimIndent()

        private const val addTemplateQuery: String = "INSERT INTO template(uuid, activity_id, header, template) VALUES (?, ?, ?, ?) ON CONFLICT DO NOTHING;"

        private const val deleteTemplateQuery: String = "DELETE FROM template WHERE uuid = ?;"

        private const val getTemplateByUUIDQuery: String = "SELECT template FROM template WHERE uuid = ?;"

        private const val listTemplatesQuery: String = "SELECT header FROM template WHERE activity_id = ?;"
    }

    init {
        createTemplateTable()
    }

    override fun <T : Template> addTemplate(template: T, serializer: (T) -> String) {
        db.update(addTemplateQuery) {
            setString(1, template.id.id)
            setString(2, template.id.activityName)
            setString(3, Json.encodeToString(template.header))
            setString(4, serializer(template))
        }
    }

    override fun removeTemplateById(templateId: TemplateId) {
        db.update(deleteTemplateQuery) {
            setString(1, templateId.id)
        }
    }

    override fun <T : Template> getTemplatesById(templateId: TemplateId, deserializer: (String) -> T): T? {
        return db.queryObject(getTemplateByUUIDQuery, { setString(1, templateId.id) }) {
            deserializer(Json.decodeFromString<String>(getString(1)))
        }
    }

    override fun listTemplates(activityName: String): List<TemplateHeader> {
        return db.query(listTemplatesQuery, { setString(1, activityName) }) {
            Json.decodeFromString<TemplateHeader>(Json.decodeFromString<String>(getString(1)))
        }
    }

    override fun close() {
        db.close()
    }

    private fun createTemplateTable() {
        db.update(createTableQuery) { }
    }
}