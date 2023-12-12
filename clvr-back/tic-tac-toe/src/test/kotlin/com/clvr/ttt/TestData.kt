package com.clvr.ttt

import com.clvr.ttt.common.QuizQuestion
import com.clvr.ttt.common.TicTacToeTemplate

val basicTestTemplate = TicTacToeTemplate(
    TicTacToeInstaller.templateId("random id"),
    arrayOf(
        arrayOf(QuizQuestion("t1", "s1", "a1", listOf()), QuizQuestion("t2", "s2", "a2", listOf("h21", "h22"))),
        arrayOf(QuizQuestion("t3", "s3", "a3", listOf("hint hint hint")), QuizQuestion("kek", "what?", "kek!", listOf("kek1", "kek2", "kek3")))),
    2,
    null,
    "template comment",
    "unstoppablechillmachine"
)