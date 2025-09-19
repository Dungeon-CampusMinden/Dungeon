---
title: "Blockly Lösung: Level 6"
---

## Blöcke
![solution](./img/loesung_level_006.png)

## Code

```java
hero.rotate(Direction.LEFT);
for(int i = 1; i <= 3; i++){
    hero.push();
}
for(int i = 1; i <= 3; i++){
    hero.rotate(Direction.RIGHT);
    hero.move();
    hero.move();
}
for(int i = 1; i <= 3; i++){
    hero.push();
}
hero.rotate(Direction.RIGHT);
hero.move();
hero.move();
hero.rotate(Direction.RIGHT);
for(int i = 1; i <= 4; i++){
    for(int x = 1; x <= 9; x++){
    hero.move();
    }
    hero.rotate(Direction.RIGHT);
}
```

## Blockly String
```json
{"blocks":{"languageVersion":0,"blocks":[{"type":"start","id":"={9Mof5xE4x:02,pxjC]","x":-209,"y":-811,"deletable":false,"next":{"block":{"type":"rotate","id":"9])Kyp`LyKlt.]xwnKdX","inputs":{"DIRECTION":{"block":{"type":"direction_left","id":"2m]{K`}R~p+X#{G4?yXU"}}},"next":{"block":{"type":"repeat","id":"`^q*6QAxG2|+he6]wVff","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"8TyEke~CF3DWCJc5N*M0","fields":{"REPEAT_NUMBER":5}}},"DO":{"block":{"type":"repeat","id":"5LzzeU.OV,XV~eJK-rzT","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"duIA%sSBmK)`V0_,}[C.","fields":{"REPEAT_NUMBER":2}}},"DO":{"block":{"type":"push","id":"#HOWi(2bD0JsC7%~5.5Y","next":{"block":{"type":"move","id":"4}#:7v:Nk_)KI7hnYwSn"}}}}},"next":{"block":{"type":"rotate","id":"AP]rJ@nuOWkyP#!12!Ko","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"c,$murp;#r5b0OOJ0SmN"}}}}}}}},"next":{"block":{"type":"repeat","id":"gycr8C/%hE)sb/K#Cd5x","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"U;%~L[2YM.n(~#}MkZgx","fields":{"REPEAT_NUMBER":3}}},"DO":{"block":{"type":"repeat","id":"3+ZB7{wC)4f3jP(S7H{0","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"se~.3kD[XB6ln~1G#q;+","fields":{"REPEAT_NUMBER":8}}},"DO":{"block":{"type":"move","id":"MnuPj|:01j`8Q611-{)%"}}},"next":{"block":{"type":"rotate","id":"}m]PW|QZUGS1MWg^9~VC","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"ps^O4G9]eY3t]B-.VAvA"}}}}}}}}}}}}}}},{"type":"rotate","id":"]m*8J^sC_L^abUsoG#YT","x":360,"y":-767,"disabledReasons":["ORPHANED_BLOCK"],"inputs":{"DIRECTION":{"block":{"type":"direction_left","id":"WmMYMV=)=Xs+=vDpmR)="}}},"next":{"block":{"type":"repeat","id":"|OVjC~1bD!o_h22XJygE","disabledReasons":["ORPHANED_BLOCK"],"inputs":{"TIMES":{"block":{"type":"repeat_number","id":"_:+T(SB1Z`xF0ChxI2l{","fields":{"REPEAT_NUMBER":2}}},"DO":{"block":{"type":"push","id":"utR[$7BEmBxS/~a_2Y?5"}}},"next":{"block":{"type":"repeat","id":"r6?[4F7VRt+n38!OVLe?","disabledReasons":["ORPHANED_BLOCK"],"inputs":{"TIMES":{"block":{"type":"repeat_number","id":"B$vJ?8@0u#fHLK[o,#7o","fields":{"REPEAT_NUMBER":2}}},"DO":{"block":{"type":"rotate","id":"EZ7yWfkuxQ5`1BIUcoMr","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"@P]6T!=95j6+q_dfzh;Q"}}},"next":{"block":{"type":"move","id":"]`Fdcg|LRx%W=a{#C_mA","next":{"block":{"type":"move","id":"Fb$QxSiF+RvFv}HP]*TH"}}}}}}},"next":{"block":{"type":"rotate","id":"h`OiPQ]}[5!`$s8tX/=O","disabledReasons":["ORPHANED_BLOCK"],"inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"Dj2XW9?*/tP:Sg71|#L^"}}},"next":{"block":{"type":"repeat","id":"F}iCQKy;#ae}7,~|($M=","disabledReasons":["ORPHANED_BLOCK"],"inputs":{"TIMES":{"block":{"type":"repeat_number","id":",Ulbs%fd]9+5.aSL:N]b","fields":{"REPEAT_NUMBER":2}}},"DO":{"block":{"type":"push","id":"|+rO*k{4lcJZDhz6OldJ"}}},"next":{"block":{"type":"rotate","id":"=mc(igVRC~2cNf[%kP]J","disabledReasons":["ORPHANED_BLOCK"],"inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"_*K5KB+3!i.F/35m2UoK"}}},"next":{"block":{"type":"move","id":"*x/AasMmV5sDbt_/N`D{","disabledReasons":["ORPHANED_BLOCK"],"next":{"block":{"type":"move","id":"lw.|rKV5ECT=R,J^cfhe","disabledReasons":["ORPHANED_BLOCK"],"next":{"block":{"type":"rotate","id":"mW3J+cSio0MJ?D,0W2A%","disabledReasons":["ORPHANED_BLOCK"],"inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"ok#i2M(C]a{MvZc1keYW"}}},"next":{"block":{"type":"repeat","id":"u!*73@RYy$njeP5fI16J","disabledReasons":["ORPHANED_BLOCK"],"inputs":{"TIMES":{"block":{"type":"repeat_number","id":"XR2Tv6e.!~^H?,BGOoK{","fields":{"REPEAT_NUMBER":3}}},"DO":{"block":{"type":"repeat","id":"!vemO$.gD}XPKSZ|rTlj","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"~eUxY1]*ZC~([Bq~zDm#","fields":{"REPEAT_NUMBER":8}}},"DO":{"block":{"type":"move","id":"h[B{@3#|82?$l!*`lO=m"}}},"next":{"block":{"type":"rotate","id":"3Gf-fAuv8=|WnHg5r1)X","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"J;)D#}yK=HC;)7[iM:Fr"}}}}}}}}}}}}}}}}}}}}}}}}}}},{"type":"rotate","id":";=AQYX)uIT.^0Acc4lHZ","x":52,"y":-285,"disabledReasons":["ORPHANED_BLOCK"],"inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"vOJ}y8azLq=caR]Jq]gQ"}}},"next":{"block":{"type":"repeat","id":"a~aHH-b;:L006:o]JoeS","disabledReasons":["ORPHANED_BLOCK"],"inputs":{"TIMES":{"block":{"type":"repeat_number","id":"X-|b?}nw,y5tB$O4Pk+L","fields":{"REPEAT_NUMBER":2}}},"DO":{"block":{"type":"push","id":"Q=?!eI0$GZ0$S{(,aX=*"}}},"next":{"block":{"type":"rotate","id":"ks-Ke4kMw25+YNva!H7e","disabledReasons":["ORPHANED_BLOCK"],"inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"~Tp_[%Q/nKJv2Ag-jnMR"}}},"next":{"block":{"type":"move","id":"ET.W?5+i;Om.D./T5q.:","disabledReasons":["ORPHANED_BLOCK"],"next":{"block":{"type":"move","id":"7#prHDM{/m{6X~{*0K#-","disabledReasons":["ORPHANED_BLOCK"],"next":{"block":{"type":"rotate","id":":R!|1*/tY{(3,kp4h3T0","disabledReasons":["ORPHANED_BLOCK"],"inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"-sf:%HB)^-L3tNI*x1+%"}}}}}}}}}}}}}}]}}
```
