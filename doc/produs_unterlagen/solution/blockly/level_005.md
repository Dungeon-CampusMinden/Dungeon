---
title: "Blockly Lösung: Level 5"
---

## Blöcke
![solution](./img/loesung_level_005.png)

## Code

```java
hero.move();
for(int i = 1; i < 3; i++){
    hero.push();
}
hero.rotate(Direction.RIGHT);
hero.move();
for(int i = 1; i < 4; i++){
    hero.push();
}
hero.rotate(Direction.LEFT);
for(int i = 1; i < 3; i++){
    hero.move();
    hero.rotate(Direction.RIGHT);
}
for(int i = 1; i < 6; i++){
    hero.pull();
}
hero.rotate(Direction.LEFT);
for(int i = 1; i < 3; i++){
    hero.move();
    hero.rotate(Direction.RIGHT);
}
for(int i = 1; i < 3; i++){
    hero.push();
}
hero.rotate(Direction.LEFT);
for(int i = 1; i < 3; i++){
    hero.move();
    hero.rotate(Direction.RIGHT);
}
for(int i = 1; i < 3; i++){
    hero.push();
}
for(int i = 1; i < 3; i++){
    hero.rotate(Direction.LEFT);
    hero.move();
    hero.rotate(Direction.RIGHT);
    hero.move();
}
hero.move();
```

## Blockly String
```json
{"blocks":{"languageVersion":0,"blocks":[{"type":"start","id":"={9Mof5xE4x:02,pxjC]","x":-374,"y":-581,"deletable":false,"next":{"block":{"type":"move","id":"2jGtT:9Au`li@vsm4Gh/","next":{"block":{"type":"repeat","id":"dh%Bo=,Mc;J8~|D1YpB^","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"%1Ug8:Rhtx,mBCQ1kujo","fields":{"REPEAT_NUMBER":2}}},"DO":{"block":{"type":"push","id":"W/p[cEGfc]NX0q+=xFPz"}}},"next":{"block":{"type":"rotate","id":"jKluZ[pTWVG,[.,w}R)2","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"#(.CRNHy#z|xGwkjHim+"}}},"next":{"block":{"type":"move","id":"VarRwkt=;Ya4_Sev-K)6","next":{"block":{"type":"repeat","id":"A;lFQ:e93[iR.43B|z|g","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"h9BG_5i:ZgZ*f~ZBC9?z","fields":{"REPEAT_NUMBER":3}}},"DO":{"block":{"type":"push","id":"eDlw!1^NG=yb%1OCY,ti"}}},"next":{"block":{"type":"rotate","id":"e8m`BQrySnP~W61G@Zto","inputs":{"DIRECTION":{"block":{"type":"direction_left","id":"km@e~1,,l1b0N:jy%zOd"}}},"next":{"block":{"type":"repeat","id":"Ys,Mifp|(U6?)b*?UbeB","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"8o7C}5!0bbiQ!uUXr?hY","fields":{"REPEAT_NUMBER":2}}},"DO":{"block":{"type":"move","id":"KHOF.2rB{Q08I/-D_BVt","next":{"block":{"type":"rotate","id":".@^Sw8~bD5oBk5cY*4!6","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"o^S`a|T{;UWK5Iw5^iR/"}}}}}}}},"next":{"block":{"type":"repeat","id":"3#,66jo+oe96w|]aXED=","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"M#eW@T`BhNn*a[*]Jpp[","fields":{"REPEAT_NUMBER":5}}},"DO":{"block":{"type":"pull","id":"V@AVlE.$el+6.v-hK_na"}}},"next":{"block":{"type":"rotate","id":"ynDV5;2$)4zzG@w/]_OU","inputs":{"DIRECTION":{"block":{"type":"direction_left","id":"VT-zm*1k7!,/tF3=.(xp"}}},"next":{"block":{"type":"repeat","id":"o|pxsZ[ORUW%JIrv3KNR","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"8lBEyKoeS_2JfD]01cK.","fields":{"REPEAT_NUMBER":2}}},"DO":{"block":{"type":"move","id":"P|1#/JXQ|J)?=e;h@?sU","next":{"block":{"type":"rotate","id":"tks,G1-1S;nFu:,S88jP","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"yH^)K9*e3n5.m22_~d#Z"}}}}}}}},"next":{"block":{"type":"repeat","id":"?91)fNBHV?Z[Ui(M[DnO","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"D{tj{UrYwNa+qC%uLUcw","fields":{"REPEAT_NUMBER":2}}},"DO":{"block":{"type":"push","id":"IK+8}P}WMuMBm-=nqR}7"}}},"next":{"block":{"type":"rotate","id":"#LlBCvEaJFKX!Atr7A_V","inputs":{"DIRECTION":{"block":{"type":"direction_left","id":"na`D%~[b{I)nGUvx4u@t"}}},"next":{"block":{"type":"repeat","id":"~8[Iwyh2H^8MoR5B,?|w","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"%6h?jMe17YtxSmk%ct?7","fields":{"REPEAT_NUMBER":2}}},"DO":{"block":{"type":"move","id":"8PhXJwY)}wK~#1uy?2OQ","next":{"block":{"type":"rotate","id":"BI6Lk{Q8(oU1V9lgyMe;","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"fEqqbDJR9C5A|tEqWU?C"}}}}}}}},"next":{"block":{"type":"repeat","id":"f8Dn[9Ppz]7[[k/UUn1v","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"E7KL9./mjky1=DF,`0XH","fields":{"REPEAT_NUMBER":2}}},"DO":{"block":{"type":"push","id":"s!r`mzfy!d*-y(k}oi[p"}}},"next":{"block":{"type":"repeat","id":"yTn_[R0|k}_AS%/~f+Az","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"gr2ACwzZhloOg$tdz+-K","fields":{"REPEAT_NUMBER":2}}},"DO":{"block":{"type":"rotate","id":"_R:@(:=n4kgS?:FIAp~a","inputs":{"DIRECTION":{"block":{"type":"direction_left","id":"XmbB.=TkG`wkqFrliHo("}}},"next":{"block":{"type":"move","id":";bXv]0}R{t3U*I5|Z#^b","next":{"block":{"type":"rotate","id":"oWP%8%r62G9lMfmCo|H)","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"OKtHk#jmt(hBV+xU;xf}"}}},"next":{"block":{"type":"move","id":"KT5LZRh.;3xN-Su0BEYW"}}}}}}}}},"next":{"block":{"type":"move","id":"qW{VE4Ma[;#]Qr^ANr],"}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}},{"type":"repeat","id":"6}2ptZL+pNMdWzlx=?Kc","x":-5,"y":-338,"disabledReasons":["ORPHANED_BLOCK"],"inputs":{"TIMES":{"block":{"type":"repeat_number","id":"J2gzg4r)0(VzkE()pxtC","fields":{"REPEAT_NUMBER":3}}},"DO":{"block":{"type":"push","id":"dasB,yEP7k|Szgxurx6m"}}},"next":{"block":{"type":"rotate","id":"_e@a-[6{VIpg:b]U.A,m","disabledReasons":["ORPHANED_BLOCK"],"inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"4q*X#?~oM=cM_}(0zWbV"}}},"next":{"block":{"type":"move","id":"{AxjZJ7wwx(7~+-,U,~s","disabledReasons":["ORPHANED_BLOCK"],"next":{"block":{"type":"repeat","id":"-{:B#X^DP9wS~Lv4!dSn","disabledReasons":["ORPHANED_BLOCK"],"inputs":{"TIMES":{"block":{"type":"repeat_number","id":"(:M$7m]1ayZIiyVJ|[HW","fields":{"REPEAT_NUMBER":5}}},"DO":{"block":{"type":"move","id":"7vUMAXh=dQo@~^%kQbVe"}}}}}}}}}}]}}
```
