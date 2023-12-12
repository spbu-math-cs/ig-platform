package com.clvr.platform.api

import kotlinx.serialization.Serializable

@Serializable
data class TemplateHeader(val name: String, val id: String, val comment: String)

@Serializable
data class TemplateId(val activityName: String, val id: String)

interface Template {
    val id: TemplateId
    val header: TemplateHeader
}