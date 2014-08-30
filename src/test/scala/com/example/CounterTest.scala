package com.example

import akka.actor.{ActorSystem, Props}
import akka.testkit._
import org.scalatest._

class CounterTest extends TestKit(ActorSystem("TestCluster")) with FlatSpecLike with BeforeAndAfterAll {

  "Counter" should "should count" in {
    val testProbe = TestProbe()
    val actor = system.actorOf(Props(new Counter), "1")

    testProbe.send(actor, Counter.DoCount)
    testProbe.send(actor, Counter.GetState)
    testProbe.expectMsg(Counter.CounterState(1))
    system.stop(actor)

    testProbe watch actor
    testProbe.expectTerminated(actor)
  }

  it should "restore its state to 2" in {
    val testProbe = TestProbe()
    val actor = system.actorOf(Props(new Counter), "1")

    testProbe.send(actor, Counter.DoCount)
    testProbe.send(actor, Counter.GetState)
    testProbe.expectMsg(Counter.CounterState(2))
    system.stop(actor)

    testProbe watch actor
    testProbe.expectTerminated(actor)
  }

  it should "restore its state to 3" in {
    val testProbe = TestProbe()
    val actor = system.actorOf(Props(new Counter), "1")

    testProbe.send(actor, Counter.DoCount)
    testProbe.send(actor, Counter.GetState)
    testProbe.expectMsg(Counter.CounterState(3))
    system.stop(actor)

    testProbe watch actor
    testProbe.expectTerminated(actor)
  }

  it should "restore its state to 4" in {
    val testProbe = TestProbe()
    val actor = system.actorOf(Props(new Counter), "1")

    testProbe.send(actor, Counter.DoCount)
    testProbe.send(actor, Counter.GetState)
    testProbe.expectMsg(Counter.CounterState(4))
    system.stop(actor)

    testProbe watch actor
    testProbe.expectTerminated(actor)
  }

  it should "restore its state to 5" in {
    val testProbe = TestProbe()
    val actor = system.actorOf(Props(new Counter), "1")

    testProbe.send(actor, Counter.DoCount)
    testProbe.send(actor, Counter.GetState)
    testProbe.expectMsg(Counter.CounterState(5))
    system.stop(actor)

    testProbe watch actor
    testProbe.expectTerminated(actor)
  }


  override def afterAll() = {
    system.shutdown()
  }
}
