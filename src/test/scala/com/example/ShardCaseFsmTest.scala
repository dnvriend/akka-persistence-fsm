package com.example

import java.util.UUID

import akka.actor.{ActorSystem, Props}
import akka.contrib.pattern.ClusterSharding
import akka.persistence.Persistent
import akka.testkit._
import com.example.ShardCaseFsm.{Open, Next, State}
import org.scalatest._

class ShardCaseFsmTest extends TestKit(ActorSystem("TestCluster")) with FlatSpecLike with BeforeAndAfterAll {

  ClusterSharding(system).start(
    typeName = "cases",
    entryProps = Some(Props(new ShardCaseFsm)),
    idExtractor = ShardCaseFsm.idExtractor,
    shardResolver = ShardCaseFsm.shardResolver)

  val casesRegion = ClusterSharding(system).shardRegion("cases")

  val id = UUID.randomUUID.toString

  "CaseFsm" should "should be in open state" in {
    val testProbe = TestProbe()

    testProbe.send(casesRegion, State(id))
    testProbe.expectMsg("open")

    testProbe watch testProbe.sender()
    testProbe.expectTerminated(testProbe.sender())
  }

  it should "transition to state1" in {
    val testProbe = TestProbe()

    testProbe.send(casesRegion, Persistent(Next(id)))
    testProbe.send(casesRegion, State(id))
    testProbe.expectMsgAllOf("roger", "state1")

    testProbe watch testProbe.sender()
    testProbe.expectTerminated(testProbe.sender())
  }

  it should "recover to state1" in {
    val testProbe = TestProbe()

    testProbe.send(casesRegion, State(id))
    testProbe.expectMsg("state1")

    testProbe watch testProbe.sender()
    testProbe.expectTerminated(testProbe.sender())
  }

  it should "not be able to reopen the already open case" in {
    val testProbe = TestProbe()

    testProbe.send(casesRegion, Persistent(Open(id)))
    testProbe.send(casesRegion, State(id))
    testProbe.expectMsgAllOf("cannot", "state1")

    testProbe watch testProbe.sender()
    testProbe.expectTerminated(testProbe.sender())
  }

  it should "transition to the closed state" in {
    val testProbe = TestProbe()

    testProbe.send(casesRegion, State(id))
    testProbe.expectMsg("state1")

    testProbe.send(casesRegion, Persistent(Next(id)))
    testProbe.send(casesRegion, State(id))
    testProbe.expectMsgAllOf("roger", "state2")
    testProbe.send(casesRegion, Persistent(Next(id)))
    testProbe.send(casesRegion, State(id))
    testProbe.expectMsgAllOf("roger", "state3")
    testProbe.send(casesRegion, Persistent(Next(id)))
    testProbe.send(casesRegion, State(id))
    testProbe.expectMsgAllOf("roger", "closed")

    testProbe watch testProbe.sender()
    testProbe.expectTerminated(testProbe.sender())
  }

  it should "recover in the closed state" in {
    val testProbe = TestProbe()

    testProbe.send(casesRegion, State(id))
    testProbe.expectMsg("closed")

    testProbe watch testProbe.sender()
    testProbe.expectTerminated(testProbe.sender())
  }

  it should "not be able to go to the non-existing next state" in {
    val testProbe = TestProbe()

    testProbe.send(casesRegion, Persistent(Next(id)))
    testProbe.expectMsg("cannot")

    testProbe watch testProbe.sender()
    testProbe.expectTerminated(testProbe.sender())
  }

  it should "be able to re-open the case" in {
    val testProbe = TestProbe()

    testProbe.send(casesRegion, Persistent(Open(id)))
    testProbe.expectMsg("roger")
    testProbe.send(casesRegion, State(id))
    testProbe.expectMsg("open")

    testProbe watch testProbe.sender()
    testProbe.expectTerminated(testProbe.sender())
  }

  it should "recover in the open state" in {
    val testProbe = TestProbe()

    testProbe.send(casesRegion, State(id))
    testProbe.expectMsg("open")

    testProbe watch testProbe.sender()
    testProbe.expectTerminated(testProbe.sender())
  }


  override def afterAll() = {
    system.shutdown()
  }
}
