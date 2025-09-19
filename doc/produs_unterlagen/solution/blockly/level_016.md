---
title: "Blockly Lösung: Level 16"
---

## Blöcke
![solution](./img/loesung_level_016.png)

## Code

```java
while(true){
    if(hero.isNearTile(LevelElement.WALL, Direction.UP)){
        if(hero.isNearTile(LevelElement.WALL, Direction.RIGHT)){
            hero.rotate(Direction.LEFT);
            hero.move();
        }else{
            hero.rotate(Direction.RIGHT);
            hero.move();
        }
    }else{
        if(!hero.isNearTile(LevelElement.WALL, Direction.RIGHT)){
            hero.rotate(Direction.RIGHT);
        }
        hero.move();
    }
}
```

## Blockly String
```json
{"blocks":{"languageVersion":0,"blocks":[{"type":"start","id":"={9Mof5xE4x:02,pxjC]","x":0,"y":0,"deletable":false,"next":{"block":{"type":"while_loop","id":"BoV78}pYfa*e@F#oExez","inputs":{"CONDITION":{"block":{"type":"logic_boolean","id":"FR9v~Zzx^T:/72[C.uXB","fields":{"BOOL":"TRUE"}}},"DO":{"block":{"type":"controls_ifelse","id":"f5GMs,z^jIZ8{B}39?-+","inputs":{"IF0":{"block":{"type":"logic_wall_direction","id":"ke[AqSui}HjU-_TCccE4","inputs":{"DIRECTION":{"block":{"type":"direction_up","id":"j@EQNyLMqM.oTHd;ME]A"}}}}},"DO0":{"block":{"type":"controls_ifelse","id":"]Sw|QX7nXYqBc#0HU.gL","inputs":{"IF0":{"block":{"type":"logic_wall_direction","id":"buFY^UFfC1{{y}jUB1C$","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"6}+00^.s]SsChcL+sO3D"}}}}},"DO0":{"block":{"type":"rotate","id":"/b,Xh51NjINQI6znU=}V","inputs":{"DIRECTION":{"block":{"type":"direction_left","id":"m[u%g60]O{D)@O!Zj1SR"}}}}},"ELSE":{"block":{"type":"rotate","id":"wFN7KD1Z~gM5s_-ND+uz","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"gwx$[nxu2DNifXN)#P2z"}}}}}}}},"ELSE":{"block":{"type":"controls_if","id":"`n$,MLYDl;C7`UL?WT*f","inputs":{"IF0":{"block":{"type":"not_condition","id":"R8m:~-EWq?z:/+VRC`Fw","inputs":{"INPUT_A":{"block":{"type":"logic_wall_direction","id":"b.x{AX:dZt:Y.?c(M1$*","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"l#F*U=$xt599F|#Y=E^O"}}}}}}}},"DO0":{"block":{"type":"rotate","id":"-BB2T#,KNR/:_Q{_9UxC","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"sByX05^^6yY5G76q?L@Y"}}}}}}}}},"next":{"block":{"type":"move","id":"%)Sh/LEXAh{hC~PH:N%J"}}}}}}}}]}}
```
