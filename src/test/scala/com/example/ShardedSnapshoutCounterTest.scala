package com.example

import akka.actor.{ActorSystem, Props}
import akka.contrib.pattern.ClusterSharding
import akka.testkit._
import org.scalatest._

import scala.concurrent.duration._

class ShardedSnapshoutCounterTest extends TestKit(ActorSystem("TestCluster")) with FlatSpecLike with BeforeAndAfterAll {

  ClusterSharding(system).start(
    typeName = ShardedSnapshotCounter.shardName,
    entryProps = Some(Props(new ShardedSnapshotCounter)),
    idExtractor = ShardedSnapshotCounter.idExtractor,
    shardResolver = ShardedSnapshotCounter.shardResolver)

  val actor = ClusterSharding(system).shardRegion(ShardedSnapshotCounter.shardName)

  def testCounter(counterName: String, counterState: Long): Unit = {
    val testProbe = TestProbe()

    testProbe.send(actor, ShardedSnapshotCounter.DoCount(counterName))
    testProbe.expectNoMsg(100 millis)
    testProbe.send(actor, ShardedSnapshotCounter.GetState(counterName))
    testProbe.expectMsg(ShardedSnapshotCounter.CounterState(counterState))

    testProbe watch testProbe.sender()
    testProbe.expectTerminated(testProbe.sender())
  }

  "ShardedSnapshotCounter 'A'" should "count from 1 until 5" in {
    (1 until 5).foreach(testCounter("a", _))
  }

  "ShardedSnapshotCounter 'B'" should "count from 1 until 5" in {
    (1 until 5).foreach(testCounter("b", _))
  }

  "ShardedSnapshotCounter 'C'" should "count from 1 until 5" in {
    (1 until 5).foreach(testCounter("c", _))
  }

  override def afterAll() = {
    system.shutdown()
  }
}
