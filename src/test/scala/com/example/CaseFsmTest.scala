package com.example

import akka.actor.{ActorSystem, Props}
import akka.persistence.Persistent
import akka.testkit._
import org.scalatest._

class CaseFsmTest extends TestKit(ActorSystem("TestCluster")) with FlatSpecLike with BeforeAndAfterAll {

  "CaseFsm" should "should be in open state" in {
    val testProbe = TestProbe()
    val actor = system.actorOf(Props(new CaseFsm), "case-123")

    testProbe.send(actor, "state")
    testProbe.expectMsg("open")
    system.stop(actor)
  }

  it should "transition to state1" in {
    val testProbe = TestProbe()
    val actor = system.actorOf(Props(new CaseFsm), "case-123")

    testProbe.send(actor, Persistent("next"))
    testProbe.send(actor, "state")
    testProbe.expectMsgAllOf("roger", "state1")
    system.stop(actor)
  }

  it should "recover to state1" in {
    val testProbe = TestProbe()
    val actor = system.actorOf(Props(new CaseFsm), "case-123")

    testProbe.send(actor, "state")
    testProbe.expectMsg("state1")
    system.stop(actor)
  }

  it should "not be able to reopen the already open case" in {
    val testProbe = TestProbe()
    val actor = system.actorOf(Props(new CaseFsm), "case-123")

    testProbe.send(actor, Persistent("open"))
    testProbe.send(actor, "state")
    testProbe.expectMsgAllOf("cannot", "state1")
    system.stop(actor)
  }

  it should "transition to the closed state" in {
    val testProbe = TestProbe()
    val actor = system.actorOf(Props(new CaseFsm), "case-123")

    testProbe.send(actor, "state")
    testProbe.expectMsg("state1")

    testProbe.send(actor, Persistent("next"))
    testProbe.send(actor, "state")
    testProbe.expectMsgAllOf("roger", "state2")
    testProbe.send(actor, Persistent("next"))
    testProbe.send(actor, "state")
    testProbe.expectMsgAllOf("roger", "state3")
    testProbe.send(actor, Persistent("next"))
    testProbe.send(actor, "state")
    testProbe.expectMsgAllOf("roger", "closed")
    system.stop(actor)
  }

  it should "recover in the closed state" in {
    val testProbe = TestProbe()
    val actor = system.actorOf(Props(new CaseFsm), "case-123")

    testProbe.send(actor, "state")
    testProbe.expectMsg("closed")

    system.stop(actor)
  }

  it should "not be able to go to the non-existing next state" in {
    val testProbe = TestProbe()
    val actor = system.actorOf(Props(new CaseFsm), "case-123")

    testProbe.send(actor, Persistent("next"))
    testProbe.expectMsg("cannot")

    system.stop(actor)
  }

  it should "be able to re-open the case" in {
    val testProbe = TestProbe()
    val actor = system.actorOf(Props(new CaseFsm), "case-123")

    testProbe.send(actor, Persistent("open"))
    testProbe.expectMsg("roger")
    testProbe.send(actor, "state")
    testProbe.expectMsg("open")

    system.stop(actor)
  }

  it should "recover in the open state" in {
    val testProbe = TestProbe()
    val actor = system.actorOf(Props(new CaseFsm), "case-123")

    testProbe.send(actor, "state")
    testProbe.expectMsg("open")

    system.stop(actor)
  }

  override def afterAll() = {
    system.shutdown()
  }
}
