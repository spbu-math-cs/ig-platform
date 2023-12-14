package com.clvr.platform.impl.db

import com.clvr.platform.api.Template
import com.clvr.platform.api.TemplateHeader
import com.clvr.platform.api.TemplateId
import com.clvr.platform.api.db.TemplateDatabase
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class PostgresTemplateDatabase(private val db: DBQueryExecutor) : TemplateDatabase {
    companion object {
        // TODO: make PRIVATE KEY = (activity_id, uuid) (?)
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
            setString(2, template.id.activityId)
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

    override fun listTemplates(activityId: String): List<TemplateHeader> {
        return db.query(listTemplatesQuery, { setString(1, activityId) }) {
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