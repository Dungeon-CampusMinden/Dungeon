---
title: "Blockly Lösung: Level 20"
---

## Code

```java
hero.move();
hero.rotate(Direction.LEFT);
for(int i = 0; i < 4; i++) {
    hero.move();
}
    hero.rotate(Direction.RIGHT);
hero.move();
hero.move();
hero.rotate(Direction.RIGHT);
for(int i = 0; i < 3; i++) {
    hero.move();
}
    hero.rotate(Direction.LEFT);
hero.move();
hero.move();
while (hero.checkBossViewDirection(Direction.RIGHT)) {
    hero.rest();
}
    while (hero.checkBossViewDirection(Direction.LEFT)) {
    hero.rest();
}
    for(int i = 0; i < 7; i++) {
    hero.move();
}
    while (hero.checkBossViewDirection(Direction.LEFT)) {
    hero.rest();
}
    hero.rotate(Direction.LEFT);
hero.move();
hero.move();
hero.rotate(Direction.RIGHT);
for(int i = 0; i < 4; i++) {
    hero.move();
}
    while (hero.checkBossViewDirection(Direction.RIGHT)) {
    hero.rest();
}
    while (hero.checkBossViewDirection(Direction.LEFT)) {
    hero.rest();
}
    for(int i = 0; i < 9; i++) {
    hero.move();
}
    hero.rotate(Direction.RIGHT);
for(int i = 0; i < 3; i++) {
    hero.move();
}
    hero.rotate(Direction.LEFT);
hero.move();
```
## Blockly String
```json
{"blocks":{"languageVersion":0,"blocks":[{"type":"start","id":"={9Mof5xE4x:02,pxjC]","x":-573,"y":-1452,"deletable":false,"next":{"block":{"type":"move","id":"#em_GD6FvTf[wPFc|VnK","next":{"block":{"type":"rotate","id":"5D7xVn8([{#-wBi7W@9m","inputs":{"DIRECTION":{"block":{"type":"direction_left","id":"Rw4.ks/=P$~gndy6;z9X"}}},"next":{"block":{"type":"repeat","id":"Lf`oY8.,6%rwDc.UpPa)","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"PfIYRS4Sp@b-abL/ZN}6","fields":{"REPEAT_NUMBER":4}}},"DO":{"block":{"type":"move","id":"}a@B#iIxn#K@bWH%IiYv"}}},"next":{"block":{"type":"rotate","id":"PmX5ItTg)^A_*:hQyyNn","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"`Fdwj%~A)F0x3=P5=4aU"}}},"next":{"block":{"type":"move","id":"iYMIP2W@l|10a*pA,w+a","next":{"block":{"type":"move","id":"a9abhqhGJJTz%4Ko59S.","next":{"block":{"type":"rotate","id":"HT[:I;Q!s[z{j5k;9?w]","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"T@xMxdpsoF+H01mxZ({*"}}},"next":{"block":{"type":"repeat","id":"##6/,;rBf-nSFl;I{uG(","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"Gb^~:hk1:ihCPPm|0O$*","fields":{"REPEAT_NUMBER":3}}},"DO":{"block":{"type":"move","id":"]3fJ10yh@*+jiQ#NfJiJ"}}},"next":{"block":{"type":"rotate","id":"F_Q60c%[]f3nagSa=S0N","inputs":{"DIRECTION":{"block":{"type":"direction_left","id":"zE|T@*hyd_1X|{AWYM^^"}}},"next":{"block":{"type":"move","id":"}!9`zU@q`z[}5zwBc1ix","next":{"block":{"type":"move","id":"loa:8nHs_3D6.;v:Tlm9","next":{"block":{"type":"while_loop","id":"jlIllqU}ud7St,UlUi9G","inputs":{"CONDITION":{"block":{"type":"logic_bossView_direction","id":"c,3~1eT#vV_(XSRp*?b)","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"I_5!UfHNz_fH-4S%E/w2"}}}}},"DO":{"block":{"type":"wait","id":"ZUgCDF6wxf0_M.T,[xw5"}}},"next":{"block":{"type":"while_loop","id":"9M(U.h%4l.2m^EgIshcK","inputs":{"CONDITION":{"block":{"type":"logic_bossView_direction","id":"^J$+^{r}G8dK85_x@b!q","inputs":{"DIRECTION":{"block":{"type":"direction_left","id":"9MWEP0}/.vL+ImQpt2If"}}}}},"DO":{"block":{"type":"wait","id":"bV@S%x?Ek`1IIOfztEdS"}}},"next":{"block":{"type":"repeat","id":"@deab4V.Cwz4E2}(J%@8","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"Q5me%j%}1uoz~a/KlAJ|","fields":{"REPEAT_NUMBER":7}}},"DO":{"block":{"type":"move","id":"a4vhS2g~ha+Sq.(QsBL("}}},"next":{"block":{"type":"while_loop","id":"]Z]i%j.iV9Hrc+Hksi7D","inputs":{"CONDITION":{"block":{"type":"logic_bossView_direction","id":"k_KaRaUl/Q;X$5ghg/=(","inputs":{"DIRECTION":{"block":{"type":"direction_left","id":"Tqpg1Lq~6^(z.lUPw^ip"}}}}},"DO":{"block":{"type":"wait","id":"}#;UDS:em1*5@IS_-!8V"}}},"next":{"block":{"type":"rotate","id":"SSbhW6u%oc!V_Ejzp6SR","inputs":{"DIRECTION":{"block":{"type":"direction_left","id":"D2u70$2},K#uY:d,2dM="}}},"next":{"block":{"type":"move","id":"Wxe?|!(;?}S(7s4e{Ww[","next":{"block":{"type":"move","id":"vR1~!aMer[;*[t:{M8AX","next":{"block":{"type":"rotate","id":"aqJxwZEubS:?J6_gmERc","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"Le2reT;F/P[zYZ,OsK+P"}}},"next":{"block":{"type":"repeat","id":"W~_OGwMH(xt`[DItQ*BK","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"qlCJ,%@p3LIHaBO.-SVf","fields":{"REPEAT_NUMBER":4}}},"DO":{"block":{"type":"move","id":"1c-5JrIymp*+)]*W(`@e"}}},"next":{"block":{"type":"while_loop","id":"oqo|L9.Fu3:4=U6uXsQc","inputs":{"CONDITION":{"block":{"type":"logic_bossView_direction","id":"nXnLr,1w@sG_YfqI|RNI","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"P=QI)c9!l}g]+rz(5eie"}}}}},"DO":{"block":{"type":"wait","id":"00$X_AbgG6V*;ZGR2D|="}}},"next":{"block":{"type":"while_loop","id":"NVAsHEZZWvG;gO.KGCaY","inputs":{"CONDITION":{"block":{"type":"logic_bossView_direction","id":"io~V~f@7C1?WIe7X*cI~","inputs":{"DIRECTION":{"block":{"type":"direction_left","id":"`CO:2dyHEECrpkoNo+@y"}}}}},"DO":{"block":{"type":"wait","id":":|y~mS]mIFmjsVl3wq`n"}}},"next":{"block":{"type":"repeat","id":"*?uhmN;mBvU/9B8qY*[%","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"g`m/}8m1m2OZA~0*x7#(","fields":{"REPEAT_NUMBER":9}}},"DO":{"block":{"type":"move","id":"Sw4-3%0p*e.USZlB`i;R"}}},"next":{"block":{"type":"rotate","id":"v.RSlwK*v(l$4t%=eITx","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"cwohqpCrB,OIKZaoI/$1"}}},"next":{"block":{"type":"repeat","id":"fmPz[F[;5bZ{@!PIQ(eS","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"]n{[4*?/n|z`tD!yao]K","fields":{"REPEAT_NUMBER":3}}},"DO":{"block":{"type":"move","id":"]v;eracfDoo#QZ;@{F]X"}}},"next":{"block":{"type":"rotate","id":"sN?hNV21N7B8|P%SxP=H","inputs":{"DIRECTION":{"block":{"type":"direction_left","id":"PL%W4|)Qm%9$F2zs,`bF"}}},"next":{"block":{"type":"move","id":"87i!gt|RVT{xMoGpCD,r"}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}},{"type":"repeat_number","id":"=a@NQDOuY/M^~z0}|ME6","x":16,"y":194,"disabledReasons":["ORPHANED_BLOCK"],"fields":{"REPEAT_NUMBER":1}},{"type":"repeat_number","id":"4PPtdI$qtcZCOi@mFIEk","x":44,"y":250,"disabledReasons":["ORPHANED_BLOCK"],"fields":{"REPEAT_NUMBER":1}},{"type":"repeat_number","id":"ZUORp/|QLpZO2md11;|*","x":72,"y":306,"disabledReasons":["ORPHANED_BLOCK"],"fields":{"REPEAT_NUMBER":1}}]}}
```
