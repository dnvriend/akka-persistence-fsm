# akka-persistence-fsm
This time, I want to look at Akka Finate State Machines (FSM), but I also want to persist their state,
and shard them ideally over multiple cluster nodes. FSM have a really unique cognitive model and DSL, 
far superior than the become or plain Actor model.

## Usage
FSM can be used for:

* Workflows,
* Process Engines,
* Any state -> transition flow engine

When used together with Akka's Cluster Sharding feature, the FSMs become 'Entries' and can be passified, 
which is a really cool feature of Akka Cluster Sharding to keep resource usage to a bare minimum. Restarting
certain 'Entries' is [upcoming in Akka](https://github.com/akka/akka/pull/15597), thanks to [Dominic Black](https://github.com/DomBlack),
but can be 

