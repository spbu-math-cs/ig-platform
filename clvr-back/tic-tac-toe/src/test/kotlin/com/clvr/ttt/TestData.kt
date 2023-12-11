package com.clvr.ttt

import com.clvr.ttt.common.TemplateQuestion
import com.clvr.ttt.common.TicTacToeTemplate

val basicTestTemplate = TicTacToeTemplate(
    TicTacToeInstaller.templateId("random id"),
    arrayOf(
        arrayOf(TemplateQuestion("t1", "s1", "a1", listOf()), TemplateQuestion("t2", "s2", "a2", listOf("h21", "h22"))),
        arrayOf(TemplateQuestion("t3", "s3", "a3", listOf("hint hint hint")), TemplateQuestion("kek", "what?", "kek!", listOf("kek1", "kek2", "kek3")))),
    2,
    null,
    "template comment",
    "unstoppablechillmachine"
)