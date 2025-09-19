---
title: "Blockly Lösung: Level 4"
---

## Blöcke
![solution](./img/loesung_level_004.png)

## Code

```java
for(int i = 1; i < 7; i++){
    hero.move();
}
hero.interact(Direction.HERE);
hero.rotate(Direction.LEFT);
for(int i = 1; i < 3; i++){
    hero.move();
}
hero.rotate(Direction.RIGHT);
for(int i = 1; i < 4; i++){
    hero.move();
}
hero.rotate(Direction.RIGHT);
for(int i = 1; i < 4; i++){
    hero.pull();
}
hero.rotate(Direction.LEFT);
for(int i = 1; i < 3; i++){
    hero.move();
    hero.rotate(Direction.RIGHT);
}
for(int i = 1; i < 4; i++){
    hero.pull();
}
hero.rotate(Direction.LEFT);
hero.move();
hero.rotate(Direction.LEFT);
for(int i = 1; i < 6; i++){
    hero.move();
}

Ohne Direction.HERE:
for(int i = 1; i < 6; i++){
    hero.move();
}
hero.interact(Direction.UP);
hero.rotate(Direction.LEFT);
for(int i = 1; i < 3; i++){
    hero.move();
}
hero.rotate(Direction.RIGHT);
for(int i = 1; i < 5; i++){
    hero.move();
}
hero.rotate(Direction.RIGHT);
for(int i = 1; i < 4; i++){
    hero.pull();
}
hero.rotate(Direction.LEFT);
for(int i = 1; i < 3; i++){
    hero.move();
    hero.rotate(Direction.RIGHT);
}
for(int i = 1; i < 4; i++){
    hero.pull();
}
hero.rotate(Direction.LEFT);
hero.move();
hero.rotate(Direction.LEFT);
for(int i = 1; i < 6; i++){
    hero.move();
}
```

## Blockly String
```json
{"blocks":{"languageVersion":0,"blocks":[{"type":"start","id":"={9Mof5xE4x:02,pxjC]","x":35,"y":-173,"deletable":false,"next":{"block":{"type":"repeat","id":"Se{%Y/*hmL(fTP0v*D%;","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"}Og,^ux3K!9[w=G+Sn.l","fields":{"REPEAT_NUMBER":6}}},"DO":{"block":{"type":"move","id":"/;v_CYdzdMyF|;QKuV*J"}}},"next":{"block":{"type":"use","id":"mBASjO@2cJm+crUn#iE1","inputs":{"DIRECTION":{"block":{"type":"direction_here","id":"Rrx;UW`)oX(^.1^V4_Ra"}}},"next":{"block":{"type":"rotate","id":"Y?Z%oGv/=|b[Kx%tcykw","inputs":{"DIRECTION":{"block":{"type":"direction_left","id":"oSrZFfSK}(yzUp2T,cyI"}}},"next":{"block":{"type":"repeat","id":"(e:Bj5OyW?Lq-GRFs{|X","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"Zzu!E7ehYN%dR%W8Cs8s","fields":{"REPEAT_NUMBER":2}}},"DO":{"block":{"type":"move","id":"Wd7+r[y=d$.n*K3ix}Sh"}}},"next":{"block":{"type":"rotate","id":"HG.(-RQPt2:]mGwf%lf,","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"xAz7Cma7J[DB6czmf?s~"}}},"next":{"block":{"type":"repeat","id":")TuGLN)X!hTJ:W:x_A=}","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"?SPA@b2f`O[heBa~kH^]","fields":{"REPEAT_NUMBER":2}}},"DO":{"block":{"type":"move","id":"gS)}i_`nHQ|O8|!(Bt5Y"}}},"next":{"block":{"type":"rotate","id":"UKMSsoniShCue^rHmWRk","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"Y.FDS;8|C@v{|^/dnvR5"}}},"next":{"block":{"type":"move","id":"[(A38(LSWr:5pE;1/Wr6","next":{"block":{"type":"rotate","id":"{rxf#h~t{VGfWq=D9-?J","inputs":{"DIRECTION":{"block":{"type":"direction_left","id":"eMn:Fjh0*NERQJb5|JDq"}}},"next":{"block":{"type":"repeat","id":"/SHw.K6TiRp;nx@f2l@(","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"y.!?r,/?IjfM8l@U]YBZ","fields":{"REPEAT_NUMBER":3}}},"DO":{"block":{"type":"push","id":"(]`2J00]]VH!u7Da:*tM"}}},"next":{"block":{"type":"rotate","id":"[m2FM)j*S5W2f^rj{=(T","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"N8GAnEn-nq4?oCW+g6EP"}}},"next":{"block":{"type":"repeat","id":"$9QeJOz~aW`/iX9GN[Qx","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"BW1+S[i%+Djgw^t!4qsP","fields":{"REPEAT_NUMBER":2}}},"DO":{"block":{"type":"move","id":"C3@7/rKTivp7fwIGeC9X","next":{"block":{"type":"rotate","id":"R0BE)WR:qY7Z~JJDcFYV","inputs":{"DIRECTION":{"block":{"type":"direction_left","id":"];PbSB;0MG0L=Yq}pQ=e"}}}}}}}},"next":{"block":{"type":"repeat","id":",U_V26dZUDR1`:o#/ZZ+","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"cYbVa[/wamT/g@hvkDWp","fields":{"REPEAT_NUMBER":2}}},"DO":{"block":{"type":"push","id":"a;Gmt`NmmhPuR!r~$#4l"}}},"next":{"block":{"type":"rotate","id":"8bH%dK1+=S^.#@yOM$IG","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"PRVpPgCdjasd_Uu;?Uu["}}},"next":{"block":{"type":"repeat","id":"2S^S`7oei(Zk}{`}Y=t{","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"-2uOarA|#Ku5Qz9C$]d5","fields":{"REPEAT_NUMBER":6}}},"DO":{"block":{"type":"move","id":"b3oE|.c%.aA:vm)|T?2)"}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}]}}
```
