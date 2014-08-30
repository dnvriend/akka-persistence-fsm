package com.example

import akka.actor.ActorLogging
import akka.persistence._

case object CounterSnapshot {
  case class CounterState(counter: Long = 0)

  case class DoCount()
  case object GetState
}

/**
 * Granted, this counter does not use the clean FSM DSL, but it can
 * use Snapshotting, and this saves a whole lot of work for something
 * like a counter. Check out state 5, it uses a snapshot with counter = 3,
 * and gets one extra journal entry. So 2 cycles, instead of 4 cycles.
 *
 * When you have a whole lot of journal entries, use a PersistentActor
 * and bite the bullet, but for say max 50 journal entries, use the FSM.
 *
 */
class CounterSnapshot extends PersistentActor with ActorLogging {
  import CounterSnapshot._
  override def persistenceId: String = "counter"

  var state = CounterState()

  def updateState(msg: DoCount): Unit = {
    log.info("Updating state from DoCount")
    state = state.copy(counter = state.counter + 1)
  }

  override def receiveRecover: Receive = {
    case SnapshotOffer(meta: SnapshotMetadata, snapshot: CounterState) =>
      log.info("Recovered from snapshot entry: {}", meta )
      state = snapshot
    case msg: DoCount =>
      log.info("Recovering from journal entry")
      updateState(msg)

    case RecoveryCompleted => log.info("Recovery completed")

    case msg @ _ => log.info("[Recovery]: missing event: {}", msg)
  }

  override def receiveCommand: Receive = {
    case msg: DoCount =>
      persist(msg)(updateState)
      if(state.counter % 3 == 0) {
        log.info("Creating snapshot")
        saveSnapshot(state)
      }

    case GetState => sender ! state

    case SaveSnapshotSuccess(meta) => log.info("SaveSnapshotSuccess: {}", meta)

    case msg @ _ => log.info("[ReceiveCommand]: missing: {}", msg)
  }

}
