package com.clvr.server

import com.clvr.ttt.QuizCreateRequest
import com.clvr.ttt.common.QuizCellInfo

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