package com.clvr.nk

sealed class IllegalGameActionException(message: String): IllegalArgumentException(message)
class LateAnswerException :
    IllegalGameActionException("Answer was sent after the time was up")
class HostAnswerException :
    IllegalGameActionException("Host can't answer the question")
class ClientStartGameException :
    IllegalGameActionException("Client can't start the game")
class ClientOpenQuestionException :
    IllegalGameActionException("Client can't open the question")
class GameFinishedException :
    IllegalGameActionException("Game is already finished")