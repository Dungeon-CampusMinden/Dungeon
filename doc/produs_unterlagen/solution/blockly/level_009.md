---
title: "Blockly Lösung: Level 9"
---

## Blöcke
![solution](./img/loesung_level_009.png)

## Code

```java
hero.rotate(Direction.LEFT);
for(int i = 1; i <= 3; i++){
    hero.move();
}
hero.pickup();
hero.rotate(Direction.RIGHT);
hero.move();
hero.pickup();
hero.rotate(Direction.RIGHT);
for(int i = 1; i <= 3; i++){
    hero.move();
}
hero.rotate(Direction.LEFT);
hero.shootFireball();
for(int i = 1; i <= 4; i++){
    hero.move();
}
hero.rotate(Direction.LEFT);
for(int i = 1; i <= 3; i++){
    hero.move();
}
hero.rotate(Direction.RIGHT);
hero.shootFireball();
for(int i = 1; i <= 5; i++){
    hero.move();
}
hero.rotate(Direction.LEFT);
hero.move();
```

## Blockly String
```json
{"blocks":{"languageVersion":0,"blocks":[{"type":"start","id":"={9Mof5xE4x:02,pxjC]","x":-189,"y":-11,"deletable":false,"next":{"block":{"type":"rotate","id":"G~4|3$/3BfrHcI(zl^`_","inputs":{"DIRECTION":{"block":{"type":"direction_left","id":"(FqvexqV#m.%SbM-oy$P"}}},"next":{"block":{"type":"repeat","id":"#Vd;+C]7_:jrF_tjcR~+","inputs":{"TIMES":{"block":{"type":"repeat_number","id":";#7-i(+z=}6me1;-I}V}","fields":{"REPEAT_NUMBER":3}}},"DO":{"block":{"type":"move","id":"3l7p=xeTl-_UHpw)Qw%8"}}},"next":{"block":{"type":"pickup","id":"VNFi:TuBn`$Zz#i}FMU+","next":{"block":{"type":"rotate","id":"R]%SG~aCBk=+pxJ-za=c","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"F)6rVLR=+C!^nD%H_h[4"}}},"next":{"block":{"type":"move","id":"SJV#g-MVP;Vs:n`A@hsn","next":{"block":{"type":"pickup","id":"Do6jr%.I=wU[`J_^e7U.","next":{"block":{"type":"rotate","id":"g?uf|-Z%owr#*Sl!-}x9","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"|X$39SD|ky0#R45.0Rj`"}}},"next":{"block":{"type":"repeat","id":",@u!d)5Ew(CHufai6)AT","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"RJ}1#`e[JJH.a#l8=r!3","fields":{"REPEAT_NUMBER":3}}},"DO":{"block":{"type":"move","id":"O*|-bcL}LWaPcM,=Xmj_"}}},"next":{"block":{"type":"rotate","id":"?0ivQY8czzd+zkt4I*oP","inputs":{"DIRECTION":{"block":{"type":"direction_left","id":"}Di%hdRe4_PJYJ)3n#B5"}}},"next":{"block":{"type":"fireball","id":"/%FJ^xk/+9kFX;s`mC|0","next":{"block":{"type":"repeat","id":"^o~XkY.cEEo4`R^^T:$x","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"rw}(~?zHM7NCZkHJktDw","fields":{"REPEAT_NUMBER":4}}},"DO":{"block":{"type":"move","id":"n^/;xbhoulX*3gmnF4_u"}}},"next":{"block":{"type":"rotate","id":"tM-,aD:G5)cwIT9VSX7`","inputs":{"DIRECTION":{"block":{"type":"direction_left","id":"fR9E,b~@Cc+XYH+527Uv"}}},"next":{"block":{"type":"repeat","id":"#oldplv#]Ix-nT,,b[Wa","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"492z!]K`SOO^{h]O^#`=","fields":{"REPEAT_NUMBER":3}}},"DO":{"block":{"type":"move","id":"|Kj/0)%R-g$4J*c0L`[N"}}},"next":{"block":{"type":"rotate","id":"=MnPmlv7bM[zzPsq-$~?","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"]CE},woyV_U[_Ox9R+Ff"}}},"next":{"block":{"type":"fireball","id":"Wd}95w;IVogNAb`d!(OB","next":{"block":{"type":"repeat","id":"E4`$S}^QC]W[k2csw)zT","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"v7pVNKxC!7J^yj+A]PtF","fields":{"REPEAT_NUMBER":5}}},"DO":{"block":{"type":"move","id":"L9$z$^boVCz-1mYGaDib"}}},"next":{"block":{"type":"rotate","id":"CQ={sUSoizPqeSdc5R!F","inputs":{"DIRECTION":{"block":{"type":"direction_left","id":"`_5(Lw?{7^)#z{Lwb$)["}}},"next":{"block":{"type":"move","id":"!uVl1(ZTO9=^{r,9;T1L"}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}]}}
```
