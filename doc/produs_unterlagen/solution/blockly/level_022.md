---
title: "Blockly Lösung: Level 22"
---

## Blöcke
![solution](./img/loesung_level_022.png)

## Code

```java
hero.move();
hero.move();
hero.move();
hero.rotate(Direction.LEFT);
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
{"blocks":{"languageVersion":0,"blocks":[{"type":"start","id":"={9Mof5xE4x:02,pxjC]","x":-918,"y":-880,"deletable":false,"next":{"block":{"type":"repeat","id":"-LeCYkZS:bgMm7/YH+Mt","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"y:cKa=H/;LW-89^}V[f5","fields":{"REPEAT_NUMBER":5}}},"DO":{"block":{"type":"move","id":"thqu+*vXkZzZ59,whcm/"}}},"next":{"block":{"type":"rotate","id":"kHnq#6)8rrK,^TLh~KP,","inputs":{"DIRECTION":{"block":{"type":"direction_left","id":".yATmRhGB+`hYcYl;f`v"}}},"next":{"block":{"type":"repeat","id":"0]7x*uypMH=ZYnY4PK6a","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"l8TrwsaqhngE/-e|vIUh","fields":{"REPEAT_NUMBER":3}}},"DO":{"block":{"type":"move","id":"%w[Fo?+.t}2J@whiZ5dX"}}},"next":{"block":{"type":"rotate","id":":`anO6j/j7,PxK5#iCKE","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"%}D6Bem-h=FT[yC,JK{t"}}},"next":{"block":{"type":"repeat","id":"?{Hs+K[)GL4XbIcb6[A3","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"R7w|Hj=nO}]]$.t(5{nm","fields":{"REPEAT_NUMBER":2}}},"DO":{"block":{"type":"move","id":"K_l])l3.}X$-yzIfu{?h"}}},"next":{"block":{"type":"rotate","id":":eG1b`PG+RA~+f;i0qN]","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"%5-8V#yq+ou2OmwJj0`h"}}},"next":{"block":{"type":"repeat","id":"l2b7I6k?94/7.5eVit?i","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"KZVAC3MfK63UJ6Z(T;^c","fields":{"REPEAT_NUMBER":5}}},"DO":{"block":{"type":"move","id":"*$f::SL:W_S!brDtMFED"}}}}}}}}}}}}}}}}}}]}}
```
