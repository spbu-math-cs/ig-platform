package com.clvr.server

import com.clvr.server.common.Quiz
import com.clvr.server.common.QuizCellInfo
import com.clvr.server.common.QuizId
import com.clvr.server.common.QuizQuestion
import com.clvr.server.plugins.QuizCreateRequest

val basicTestQuiz = Quiz(
    QuizId("random id"),
    arrayOf(
        arrayOf(QuizQuestion("t1", "s1", "a1", listOf()), QuizQuestion("t2", "s2", "a2", listOf("h21", "h22"))),
        arrayOf(QuizQuestion("t3", "s3", "a3", listOf("hint hint hint")), QuizQuestion("kek", "what?", "kek!", listOf("kek1", "kek2", "kek3")))),
    2,
    null,
    "template comment",
    "unstoppablechillmachine"
)

val createTemplate = QuizCreateRequest(
    "template name",
    "template comment",
    List(3) { row ->
        List(3) { col ->
            QuizCellInfo(
                row,
                col,
                "topic $row $col",
                "question $row $col",
                listOf("hint $row $col 1", "hint $row $col 2"),
                "answer $row $col"
            )
        }
    }.flatten()
)