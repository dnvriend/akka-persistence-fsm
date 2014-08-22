package com.example

import akka.actor.SupervisorStrategy.Stop
import akka.actor.{ReceiveTimeout, FSM}
import akka.contrib.pattern.ShardRegion
import akka.contrib.pattern.ShardRegion.Passivate
import akka.persistence.{Persistent, Processor}

import scala.concurrent.duration._

object ShardCaseFsm {
    
  case class Next(id: String)
  case class State(id: String)
  case class Open(id: String)
  
  val idExtractor: ShardRegion.IdExtractor = {
    case msg @ Persistent(Next(id), _) => (id, msg)
    case msg @ Persistent(Open(id), _) => (id, msg)
    case msg: State => (msg.id, msg)
  }

  val shardResolver: ShardRegion.ShardResolver = {
    case msg @ Persistent(Next(id), _) => (math.abs(id.hashCode) % 100).toString
    case msg @ Persistent(Open(id), _) => (math.abs(id.hashCode) % 100).toString
    case msg: State => (math.abs(msg.id.hashCode) % 100).toString
  }
}

class ShardCaseFsm extends Processor with FSM[String, Boolean] {
  import ShardCaseFsm._

  context.setReceiveTimeout(10 millis)

  startWith("open", false)

  when("open") {
    case Event(Persistent(Next(id), _), closed) => goto("state1") replying "roger"
    case Event(Persistent(Open(_), _), closed) if closed => goto("open") using false replying "roger"
    case Event(Persistent(Open(_), _), closed) if !closed => stay() replying "cannot"
    case Event(State(_), closed) => stay() replying "open"
    case Event(ReceiveTimeout, _) =>
      context.parent ! Passivate(stopMessage = Stop)
      stay()
    case Event(Stop, _) =>
      context.stop(self)
      stay()
  }

  when("state1") {
    case Event(Persistent(Next(_), _), closed) => goto("state2") replying "roger"
    case Event(Persistent(Open(_), _), closed) if closed => goto("open") using false replying "roger"
    case Event(Persistent(Open(_), _), closed) if !closed => stay() replying "cannot"
    case Event(State(_), closed) => stay() replying "state1"
    case Event(ReceiveTimeout, _) =>
      context.parent ! Passivate(stopMessage = Stop)
      stay()
    case Event(Stop, _) =>
      context.stop(self)
      stay()
  }

  when("state2") {
    case Event(Persistent(Next(_), _), closed) => goto("state3") replying "roger"
    case Event(Persistent(Open(_), _), closed) if closed => goto("open") using false replying "roger"
    case Event(Persistent(Open(_), _), closed) if !closed => stay() replying "cannot"
    case Event(State(_), closed) => stay() replying "state2"
    case Event(ReceiveTimeout, _) =>
      context.parent ! Passivate(stopMessage = Stop)
      stay()
    case Event(Stop, _) =>
      context.stop(self)
      stay()
  }

  when("state3") {
    case Event(Persistent(Next(_), _), closed) => goto("closed") using true replying "roger"
    case Event(Persistent(Open(_), _), closed) if closed => goto("open") using false replying "roger"
    case Event(Persistent(Open(_), _), closed) if !closed => stay() replying "cannot"
    case Event(State(_), closed) => stay() replying "state3"
    case Event(ReceiveTimeout, _) =>
      context.parent ! Passivate(stopMessage = Stop)
      stay()
    case Event(Stop, _) =>
      context.stop(self)
      stay()
  }

  when("closed") {
    case Event(Persistent(Next(_), _), closed) => stay() replying "cannot"
    case Event(Persistent(Open(_), _), closed) if closed => goto("open") using false replying "roger"
    case Event(Persistent(Open(_), _), closed) if !closed => stay() replying "cannot"
    case Event(State(_), closed) => stay() replying "closed"
    case Event(ReceiveTimeout, _) =>
      context.parent ! Passivate(stopMessage = Stop)
      stay()
    case Event(Stop, _) =>
      context.stop(self)
      stay()
  }

  initialize()
}