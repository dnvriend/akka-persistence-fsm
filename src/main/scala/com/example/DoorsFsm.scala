package com.example

import akka.actor.FSM
import akka.persistence.{Persistent, Processor}

class DoorsFsm extends Processor with FSM[String, Int] {

  startWith("closed", 0)

  when("closed") {
    case Event(Persistent("open", _), counter) =>
      goto("open") using (counter + 1)
    case Event("state", counter) => stay() replying ("closed", counter)
  }

  when("open") {
    case Event(Persistent("close", _), counter) =>
      goto("closed") using (counter + 1)
    case Event("state", counter) => stay() replying ("open", counter)
  }

  initialize()
}