package com.example

import akka.actor.SupervisorStrategy.Stop
import akka.actor.{ReceiveTimeout, ActorLogging}
import akka.contrib.pattern.ShardRegion
import akka.contrib.pattern.ShardRegion.Passivate
import akka.persistence._

import scala.concurrent.duration._

case object ShardedSnapshotCounter {
  case class CounterState(counter: Long = 0)

  case class DoCount(counter: String)
  case class GetState(counter: String)

  val shardName: String = "counters"

  val idExtractor: ShardRegion.IdExtractor = {
    case msg @ DoCount(counter) => (counter, msg)
    case msg @ GetState(counter) => (counter, msg)
  }

  val shardResolver: ShardRegion.ShardResolver = {
    case msg @ DoCount(counter) => (math.abs(counter.hashCode) % 100).toString
    case msg @ GetState(counter) => (math.abs(counter.hashCode) % 100).toString
  }
}

class ShardedSnapshotCounter extends PersistentActor with ActorLogging {
  import ShardedSnapshotCounter._

  override def persistenceId: String = shardName + "-" + self.path.name

  context.setReceiveTimeout(10 millis)

  var state = CounterState()

  def updateState(msg: DoCount): Unit = {
    log.info("Updating state from DoCount")
    state = state.copy(counter = state.counter + 1)
  }

  override def receiveRecover: Receive = {
    case SnapshotOffer(meta: SnapshotMetadata, snapshot: CounterState) =>
      log.info("Recovered from snapshot entry: {}", meta)
      state = snapshot
    case msg @ DoCount(_) =>
      log.info("Recovering from journal entry")
      updateState(msg)

    case RecoveryCompleted => log.info("Recovery completed")

    case msg@_ => log.info("[Recovery]: missing event: {}", msg)
  }

  override def receiveCommand: Receive = {
    case msg @ DoCount(counter) =>
      persist(msg)(updateState)
      if (state.counter % 3 == 0) {
        log.info("Creating snapshot counter: {}", counter)
        saveSnapshot(state)
      }

    case GetState(counter) =>
      log.info("GettingState: {}, {}, pid: {}", counter, state, persistenceId)
      sender ! state

    case SaveSnapshotSuccess(meta) => log.info("SaveSnapshotSuccess: {}", meta)

    case ReceiveTimeout => context.parent ! Passivate(stopMessage = Stop)

    case Stop => context.stop(self)

    case msg @ _ => log.info("[ReceiveCommand]: missing: {}", msg)
  }
}