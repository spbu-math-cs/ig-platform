package com.clvr.nk

import com.clvr.nk.common.NeKahootTemplate
import com.clvr.nk.common.TemplateQuestion

val basicTestTemplate = NeKahootTemplate(
    id = NeKahootInstaller.templateId("random id"),
    questions = listOf(
        TemplateQuestion("q1", "opt2", null, listOf("opt1", "opt2", "opt3"), 1),
        TemplateQuestion("q2", "opt1", "baza", listOf("opt1", "opt2"), 2),
        TemplateQuestion("q3", "opt4", null, listOf("opt1", "opt2", "opt3", "opt4", "opt5", "opt6"), 1),
    ),
    templateTitle = "template title",
    templateComment = "template comment",
    templateAuthor = "frungl",
)

val basicTestTemplate2 = NeKahootTemplate(
        id = NeKahootInstaller.templateId("random id"),
        questions = listOf(
                TemplateQuestion("q1", "opt2", null, listOf("opt1", "opt2", "opt3"), 10),
                TemplateQuestion("q2", "opt1", "baza", listOf("opt1", "opt2"), 20),
                TemplateQuestion("q3", "opt4", null, listOf("opt1", "opt2", "opt3", "opt4", "opt5", "opt6"), 10),
        ),
        templateTitle = "template title",
        templateComment = "template comment",
        templateAuthor = "frungl",
)