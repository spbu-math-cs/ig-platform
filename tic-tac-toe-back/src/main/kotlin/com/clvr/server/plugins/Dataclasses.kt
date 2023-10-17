package com.clvr.server.plugins

import com.clvr.server.GameTemplate
import kotlinx.serialization.Serializable

@Serializable
data class Id(val id: String)

@Serializable
data class GameSession(val session: Id)

@Serializable
data class QuizRequest(val quiz: Id)

typealias QuizDatabase = List<GameTemplate>