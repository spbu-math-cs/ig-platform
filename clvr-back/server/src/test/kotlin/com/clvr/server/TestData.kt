package com.clvr.server

import com.clvr.ttt.TemplateCreateRequest
import com.clvr.ttt.common.TemplateCellInfo

val createTemplate = TemplateCreateRequest(
    "template name",
    "template comment",
    List(3) { row ->
        List(3) { col ->
            TemplateCellInfo(
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