package com.example

import akka.actor.{ActorLogging, FSM}
import akka.persistence.{RecoveryCompleted, Persistent, Processor}
import com.example.Counter._

object Counter {
  sealed trait Data
  case class CounterState(counter: Long = 0) extends Data

  case object DoCount
  case object GetState
}

/**
 * The Counter 'Processor' type, replays all Persistent messages, so when
 * you have a whole lot of DoCount messages, this can take a while.
 * See the 'CounterSnapshot' class for the PersistentActor version
 */
class Counter extends Processor with FSM[String, Data] with ActorLogging {

  startWith("idle", CounterState())

  when("idle") {
    case Event(DoCount, data: CounterState) =>
      self ! Persistent(DoCount)
      stay() using data.copy(counter = data.counter + 1)

    case Event(Persistent(DoCount, sequenceNr), data: CounterState) =>
      log.info("SequenceNr: {}", sequenceNr)
      stay using data.copy(counter = data.counter + 1)

    case Event(GetState, data: CounterState) =>
      stay replying data

    case Event(RecoveryCompleted, _) =>
      log.info("Recovery has been completed")
      stay()
  }

  initialize()
}