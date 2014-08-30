package com.example

import akka.actor.{ActorSystem, Props}
import akka.testkit._
import org.scalatest._

class CounterSnapshotTest extends TestKit(ActorSystem("TestCluster")) with FlatSpecLike with BeforeAndAfterAll {

  "CounterSnapshot" should "should count" in {
    val testProbe = TestProbe()
    val actor = system.actorOf(Props(new CounterSnapshot), "1")

    testProbe.send(actor, CounterSnapshot.DoCount())
    testProbe.send(actor, CounterSnapshot.GetState)
    testProbe.expectMsg(CounterSnapshot.CounterState(1))
    system.stop(actor)

    testProbe watch actor
    testProbe.expectTerminated(actor)
  }

  it should "restore its state to 2" in {
    val testProbe = TestProbe()
    val actor = system.actorOf(Props(new CounterSnapshot), "1")

    testProbe.send(actor, CounterSnapshot.DoCount())
    testProbe.send(actor, CounterSnapshot.GetState)
    testProbe.expectMsg(CounterSnapshot.CounterState(2))
    system.stop(actor)

    testProbe watch actor
    testProbe.expectTerminated(actor)
  }

  it should "restore its state to 3" in {
    val testProbe = TestProbe()
    val actor = system.actorOf(Props(new CounterSnapshot), "1")

    testProbe.send(actor, CounterSnapshot.DoCount())
    testProbe.send(actor, CounterSnapshot.GetState)
    testProbe.expectMsg(CounterSnapshot.CounterState(3))
    system.stop(actor)

    testProbe watch actor
    testProbe.expectTerminated(actor)
  }

  it should "restore its state to 4" in {
    val testProbe = TestProbe()
    val actor = system.actorOf(Props(new CounterSnapshot), "1")

    testProbe.send(actor, CounterSnapshot.DoCount())
    testProbe.send(actor, CounterSnapshot.GetState)
    testProbe.expectMsg(CounterSnapshot.CounterState(4))
    system.stop(actor)

    testProbe watch actor
    testProbe.expectTerminated(actor)
  }

  it should "restore its state to 5" in {
    val testProbe = TestProbe()
    val actor = system.actorOf(Props(new CounterSnapshot), "1")

    testProbe.send(actor, CounterSnapshot.DoCount())
    testProbe.send(actor, CounterSnapshot.GetState)
    testProbe.expectMsg(CounterSnapshot.CounterState(5))
    system.stop(actor)

    testProbe watch actor
    testProbe.expectTerminated(actor)
  }


  override def afterAll() = {
    system.shutdown()
  }
}
