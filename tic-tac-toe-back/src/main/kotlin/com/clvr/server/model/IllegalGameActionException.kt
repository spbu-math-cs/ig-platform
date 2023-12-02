package com.clvr.server.model

sealed class IllegalGameActionException(message: String): IllegalArgumentException(message)
class IllegalCellContentException :
    IllegalGameActionException("Changing result in the cell is forbidden")
class MultipleQuestionsOpeningException :
    IllegalGameActionException("Set mark in the opened cell before opening the next one")
class OpenAlreadyMarkedQuestionException :
    IllegalGameActionException("Cannot open already marked question")