package com.example

import akka.actor.{Props, ActorRef, ActorSystem}
import akka.persistence.Persistent
import akka.testkit._
import org.scalatest._

import scala.concurrent.duration._

class DoorsFsmTest extends TestKit(ActorSystem("TestCluster")) with FlatSpecLike with BeforeAndAfterAll {

  "DoorsFsm" should "should be in closed state" in {
    val testProbe = TestProbe()
    val actor = system.actorOf(Props(new DoorsFsm), "door")

    testProbe.send(actor, "state")
    testProbe.expectMsg(("closed",0))
    system.stop(actor)
  }

  it should "transition to the open state" in {
    val testProbe = TestProbe()
    val actor = system.actorOf(Props(new DoorsFsm), "door")

    testProbe.send(actor, Persistent("open"))
    testProbe.send(actor, "state")
    testProbe.expectMsg(("open", 1))
    system.stop(actor)
  }

  it should "recover and be in the open state" in {
    val testProbe = TestProbe()
    val actor = system.actorOf(Props(new DoorsFsm), "door")

    testProbe.send(actor, "state")
    testProbe.expectMsg(("open", 1))
    system.stop(actor)
  }

  it should "recover to open and transition to closed" in {
    val testProbe = TestProbe()
    val actor = system.actorOf(Props(new DoorsFsm), "door")

    testProbe.send(actor, "state")
    testProbe.expectMsg(("open", 1))

    testProbe.send(actor, Persistent("close"))
    testProbe.send(actor, "state")
    testProbe.expectMsg(("closed", 2))

    system.stop(actor)
  }

  override def afterAll() = {
    system.shutdown()
  }
}
