# Dependency Charts

We want to be able to make sure that players are able to reach the goal from every state. At the same time, there should be some level of redundancy in the ways of obtaining the goal. Dependency charts are a good way to achieve this. The dungeon's DSL also offers options to achieve something very similar ([dsl example](../../dungeon/doc/dsl/examplescripts/quickstart_task_dependency.dng)), though there seems to be no abstract type for actions.
There's a [guide on dependency charts](https://grumpygamer.com/puzzle_dependency_charts/) from the personal blog of Ron Gilbert (games industry).
[This blog post](https://heterogenoustasks.wordpress.com/2015/01/26/standard-patterns-in-choice-based-games/) attempts to categorize different graph layouts.

## Puzzlon

The [wiki page on the Puzzlon Editor](https://www.ifwiki.org/Puzzlon) of the Interactive Fiction Technology Foundation also details the concept and has an elaborate example from a real game.
This wiki page also shows a snippet of code used to generate the graph:

```
end_of_part_one : goal {
   depends_on      = [ gain_access_to_mansion ]
   end_state       = positive
}

gain_access_to_mansion : goal {
   depends_on = [ smash_lock_on_gate,  calm_dog ]
}

get_beer_mat           : action {
   depends_on= [ talk_to_man, at_tavern ]
}

get_branch             : action {
   depends_on = [ climb_tree ]
}
```

There is also a version of the editor for [running inside the web browser](https://adventuron.io/puzzlon/).

An escape room language that draws from both (DSL and more generic dependency graph) could be the basis for defining escape rooms for use with the dungeon.

## PuzzleGraph

A more interactive tool is [PuzzleGraph](https://hg.sr.ht/~runevision/puzzlegraph) (pre-built binaries linked). It reflects stateful game entities like switches, sensors, and doors. Probably out of scope for us.
