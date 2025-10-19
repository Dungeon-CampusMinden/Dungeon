---
title: "Blockly Lösung: Level 9"
---

## Blöcke
![solution](./img/loesung_level_008_1.png)
![solution](./img/loesung_level_008_2.png)
![solution](./img/loesung_level_008_3.png)
![solution](./img/loesung_level_008_4.png)

## Code

```java
for(int i = 1; i <= 8; i++){
    hero.move();
}
hero.rotate(Direction.RIGHT);
for(int i = 1; i <= 4; i++){
    hero.move();
    hero.interact(Direction.INFRONT);
}
for(int i = 1; i <= 2; i++){
    hero.rotate(Direction.RIGHT);
}
for(int i = 1; i <= 3; i++){
    hero.move();
}
hero.rotate(Direction.LEFT);
for(int i = 1; i <= 4; i++){
    hero.move();
}
hero.rotate(Direction.LEFT);
for(int i = 1; i <= 3; i++){
    hero.move();
}
for(int i = 1; i <= 5; i++){
    hero.pull();
}
hero.rotate(Direction.RIGHT);
for(int i = 1; i <= 2; i++){
    hero.move();
    hero.rotate(Direction.LEFT);
}
hero.push();
hero.rotate(Direction.RIGHT);
for(int i = 1; i <= 2; i++){
    hero.move();
    hero.rotate(Direction.LEFT);
}
for(int i = 1; i <= 2; i++){
    hero.push();
}
hero.rotate(Direction.LEFT);
hero.move();
hero.rotate(Direction.RIGHT);
hero.move();
for(int i = 1; i <= 2; i++){
    hero.move();
    hero.rotate(Direction.RIGHT);
}
for(int i = 1; i <= 7; i++){
    hero.pull();
}
hero.rotate(Direction.RIGHT);
for(int i = 1; i <= 2; i++){
    hero.move();
    hero.rotate(Direction.LEFT);
}
hero.push();
hero.rotate(Direction.RIGHT);
hero.move();
hero.rotate(Direction.LEFT);
for(int i = 1; i <= 5; i++){
    hero.move();
}
hero.rotate(Direction.LEFT);
hero.move();
hero.rotate(Direction.RIGHT);
hero.move();
```

## Blockly String

```json
{"blocks":{"languageVersion":0,"blocks":[{"type":"start","id":"={9Mof5xE4x:02,pxjC]","x":-119,"y":-571,"deletable":false,"next":{"block":{"type":"repeat","id":"5!f,3oeSEnd`KU$D~*u`","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"[*c!sB5a{O[.0x7;oafx","fields":{"REPEAT_NUMBER":8}}},"DO":{"block":{"type":"move","id":"(yjB0q+JT3qj?b4Sfi^W"}}},"next":{"block":{"type":"rotate","id":"M5jKnsH[BC@wOdOMtAcN","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"}j?]t6w7SpA;dD0i_=#G"}}},"next":{"block":{"type":"repeat","id":"u?hdYN-_sKlJGL[og?VB","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"~b4Q`4Ny.,?$@U-CyOhS","fields":{"REPEAT_NUMBER":4}}},"DO":{"block":{"type":"move","id":"1KPuDoQ;{nUDf`rrS]2H","next":{"block":{"type":"use","id":"/nzgr3{yi4.wzLWYBijg","inputs":{"DIRECTION":{"block":{"type":"direction_up","id":"//;rfNNO3Lc98tCU{IH_"}}}}}}}},"next":{"block":{"type":"repeat","id":"svtBEq|q)kq|vQ]T.o#l","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"s)1EDlkP,];JRBW~9a7Y","fields":{"REPEAT_NUMBER":2}}},"DO":{"block":{"type":"rotate","id":"Z{|;AJ(f8}Hvlbo8(#90","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"@(1dSDwT`dF}Zui2fx5k"}}}}}},"next":{"block":{"type":"repeat","id":",}Y;z]iLRZCT6dcBc:i$","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"yXow`o3}[T[9KnAIR_Bz","fields":{"REPEAT_NUMBER":3}}},"DO":{"block":{"type":"move","id":"LHHmdvCMo:!)o(.VO(3_"}}},"next":{"block":{"type":"rotate","id":"//b.!5Vr]njqLMEJCq)H","inputs":{"DIRECTION":{"block":{"type":"direction_left","id":"SoMdm2ZjBQQpcVRGTr2`"}}},"next":{"block":{"type":"repeat","id":"ef7,8Hq24FRe|n^Pa0Ye","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"i$;I(7P9y{KhAVpw__*:","fields":{"REPEAT_NUMBER":4}}},"DO":{"block":{"type":"move","id":"Q9icNf,/E}u*z;Lx_hH-"}}},"next":{"block":{"type":"rotate","id":"G##Id1dO7dp^=d1u6EaN","inputs":{"DIRECTION":{"block":{"type":"direction_left","id":"FSFBu3;x!F46w;?8eVV$"}}},"next":{"block":{"type":"repeat","id":"sBkt:]W(/M]Ih)b_OWbd","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"70FFkCn7)T$DFAIaf:x7","fields":{"REPEAT_NUMBER":3}}},"DO":{"block":{"type":"move","id":"OZ+Z?:a4BPYzjR`)2-1s"}}},"next":{"block":{"type":"repeat","id":"6#lDW%zMG8y183e|*z6_","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"M3;9wA@9G#8m1$lZcQu0","fields":{"REPEAT_NUMBER":5}}},"DO":{"block":{"type":"pull","id":"=nX,[%Sx+ej($:KAhOUq"}}},"next":{"block":{"type":"rotate","id":"ZTrW*o%IXUNR$]L:rSqm","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"{~U$b!e{#a3:LURFkBQ8"}}},"next":{"block":{"type":"repeat","id":"xBw)]w`|-W?gWJrg]UlG","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"_jw7{8*)#f:fuSa`rvW[","fields":{"REPEAT_NUMBER":2}}},"DO":{"block":{"type":"move","id":"avtDCFxc@#MJ_%oU8fv^","next":{"block":{"type":"rotate","id":"uY.cywEu{=3M.QS}cKdA","inputs":{"DIRECTION":{"block":{"type":"direction_left","id":"kDXhHlfzunDf!|qx?,)h"}}}}}}}},"next":{"block":{"type":"push","id":":m;QM$D.mlnRx5LWi^8K","next":{"block":{"type":"rotate","id":"5~wQAG@V5!?,Th|)h5~x","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"k%5C1sJ;%~L|.^v]7Rk*"}}},"next":{"block":{"type":"repeat","id":"xU{ZVp`o1e.R82Dm7YLM","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"-e|.]wBd0kjaF:9HjuCE","fields":{"REPEAT_NUMBER":2}}},"DO":{"block":{"type":"move","id":"]Xj=6b0tQr{455(F5JS*","next":{"block":{"type":"rotate","id":"#8;^E7AdMjHojc:Rz+]w","inputs":{"DIRECTION":{"block":{"type":"direction_left","id":"?%0Dn/54%4$w#HAjT(OA"}}}}}}}},"next":{"block":{"type":"repeat","id":"6{4N+p=8.1Z.R!DmhE4d","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"/Myw9_%*Usb[]^HhF]_!","fields":{"REPEAT_NUMBER":2}}},"DO":{"block":{"type":"push","id":"1dM6.`@=k`uo?X`$DbOz"}}},"next":{"block":{"type":"rotate","id":"},d+8$Lrz9^wVjtkwQ~t","inputs":{"DIRECTION":{"block":{"type":"direction_left","id":"ii;J8W/=7pody^h:E[Bl"}}},"next":{"block":{"type":"move","id":"*kJl,?%aNsO!C:BBOr^`","next":{"block":{"type":"rotate","id":"@22[I-VMfz,mnCAcyuwr","inputs":{"DIRECTION":{"block":{"type":"direction_right","id
```
