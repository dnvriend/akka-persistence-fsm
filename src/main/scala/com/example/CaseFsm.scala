package com.example

import akka.actor.FSM
import akka.persistence.{Persistent, Processor}

class CaseFsm extends Processor with FSM[String, Boolean] {

  startWith("open", false)

  when("open") {
    case Event(Persistent("next", _), closed) => goto("state1") replying "roger"
    case Event(Persistent("open", _), closed) if closed => goto("open") using false replying "roger"
    case Event(Persistent("open", _), closed) if !closed => stay() replying "cannot"
    case Event("state", closed) => stay() replying "open"
  }

  when("state1") {
    case Event(Persistent("next", _), closed) => goto("state2") replying "roger"
    case Event(Persistent("open", _), closed) if closed => goto("open") using false replying "roger"
    case Event(Persistent("open", _), closed) if !closed => stay() replying "cannot"
    case Event("state", closed) => stay() replying "state1"
  }

  when("state2") {
    case Event(Persistent("next", _), closed) => goto("state3") replying "roger"
    case Event(Persistent("open", _), closed) if closed => goto("open") using false replying "roger"
    case Event(Persistent("open", _), closed) if !closed => stay() replying "cannot"
    case Event("state", closed) => stay() replying "state2"
  }

  when("state3") {
    case Event(Persistent("next", _), closed) => goto("closed") using true replying "roger"
    case Event(Persistent("open", _), closed) if closed => goto("open") using false replying "roger"
    case Event(Persistent("open", _), closed) if !closed => stay() replying "cannot"
    case Event("state", closed) => stay() replying "state3"
  }

  when("closed") {
    case Event(Persistent("next", _), closed) => stay() replying "cannot"
    case Event(Persistent("open", _), closed) if closed => goto("open") using false replying "roger"
    case Event(Persistent("open", _), closed) if !closed => stay() replying "cannot"
    case Event("state", closed) => stay() replying "closed"
  }

  initialize()
}