---
title: "Blockly Lösung: Level 17"
---

## Blöcke
![solution](./img/loesung_level_017.png)

## Code

```java
hero.move();
hero.move();
hero.move();
while(true){
    if(hero.isNearTile(LevelElement.FLOOR, Direction.RIGHT)){
        hero.rotate(Direction.RIGHT);
        hero.move();
    }else if(hero.isNearTile(LevelElement.FLOOR, Direction.UP)){
        hero.move();
    }else if(hero.isNearTile(LevelElement.FLOOR, Direction.LEFT)){
        hero.rotate(Direction.LEFT);
        hero.move();
    }else if(hero.isNearTile(LevelElement.EXIT, Direction.UP)){
        hero.move();
    }else{
        hero.rotate(Direction.LEFT);
        hero.rotate(Direction.LEFT);
        hero.move();
    }
}
```

## Blockly String
```json
{"blocks":{"languageVersion":0,"blocks":[{"type":"start","id":"={9Mof5xE4x:02,pxjC]","x":8,"y":-267,"deletable":false,"next":{"block":{"type":"repeat","id":"D8skCVnw$QV$kt].QGw5","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"Q]G@Ka*F2R4y4)mW58AB","fields":{"REPEAT_NUMBER":3}}},"DO":{"block":{"type":"move","id":"DAH1_!ArS?t!-]zr/ZqN"}}},"next":{"block":{"type":"while_loop","id":"POuXwg2]ejR=5{xwZ6mE","inputs":{"CONDITION":{"block":{"type":"logic_boolean","id":"o}cwrZ~$Aufu}zB^YvL!","fields":{"BOOL":"TRUE"}}},"DO":{"block":{"type":"controls_ifelse","id":"*J*j8)nl^/zW+S9NCBf)","inputs":{"IF0":{"block":{"type":"not_condition","id":"PbrriO2d1l!H0(wWGXyj","inputs":{"INPUT_A":{"block":{"type":"logic_pit_direction","id":"Buf?}`O?_89q|+eo;P,^","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"Ff}$^bO^q}y-,zARIv]a"}}}}}}}},"DO0":{"block":{"type":"rotate","id":"u|?QU:vv=Mkn8~DvQ^_2","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"I/n}0H?+`2E[+~{@1kM@"}}}}},"ELSE":{"block":{"type":"controls_if","id":"5ISSwtR3y?L[yF.(ij/2","inputs":{"IF0":{"block":{"type":"logic_pit_direction","id":"*kN,bx^1Xh=Ir5}vQ3pt","inputs":{"DIRECTION":{"block":{"type":"direction_up","id":"e=e5kVNpWQ^=@WV[DgW$"}}}}},"DO0":{"block":{"type":"controls_ifelse","id":"~RdwS[fBakcpiVZaFw4,","inputs":{"IF0":{"block":{"type":"not_condition","id":"6nlTQjh]VzyIq{DeEO{/","inputs":{"INPUT_A":{"block":{"type":"logic_pit_direction","id":",W}i22wYxJ`^4y!`*Wqa","inputs":{"DIRECTION":{"block":{"type":"direction_left","id":"i7nfVIAc$QN$Y=J4P@-2"}}}}}}}},"DO0":{"block":{"type":"rotate","id":"L*6Kr_e;6/76C3NLA!lp","inputs":{"DIRECTION":{"block":{"type":"direction_left","id":"0=Cdx%YgzwDqvk;M^k32"}}}}},"ELSE":{"block":{"type":"rotate","id":"j=y]:FQVATw1I^igB-C0","inputs":{"DIRECTION":{"block":{"type":"direction_left","id":"ApxpZc4~rs39zv)(Oy~,"}}},"next":{"block":{"type":"rotate","id":"Y(649ba2@h8L4]`*G:ev","inputs":{"DIRECTION":{"block":{"type":"direction_left","id":"tLl=.x1V$8*4ZO0%jakM"}}}}}}}}}}}}}},"next":{"block":{"type":"move","id":"F2=WJ6ua3z[L{nxm?BE,"}}}}}}}}}},{"type":"while_loop","id":"R9HO{-.DgJJlq5[Mi7ZY","x":-480,"y":-177,"disabledReasons":["ORPHANED_BLOCK"],"inputs":{"CONDITION":{"block":{"type":"logic_boolean","id":"=BB/~e`(tn+A}m(4Oyv-","fields":{"BOOL":"TRUE"}}},"DO":{"block":{"type":"controls_ifelse","id":"knux=1AmU$s?f?!nZBZ]","inputs":{"IF0":{"block":{"type":"logic_floor_direction","id":"|cu0c1@JzMb1`i?4QjW2","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"6B6b9jn#*cMM47-KE[hY"}}}}},"DO0":{"block":{"type":"rotate","id":"NnQ@1QsYK$!2ehRKn;K=","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"$S)dtvK=Ne){HlE:VGa{"}}}}},"ELSE":{"block":{"type":"controls_if","id":"D|=[SK^x%g~s*wLXJeP,","inputs":{"IF0":{"block":{"type":"not_condition","id":"LaH`L//nGT.t?GSweY_T","inputs":{"INPUT_A":{"block":{"type":"logic_floor_direction","id":"rk4Ke[x8Cfo,K@@l/u^}","inputs":{"DIRECTION":{"block":{"type":"direction_up","id":"TU8R)MnVHz;jGDDaOU9E"}}}}}}}},"DO0":{"block":{"type":"controls_ifelse","id":"(PCX2UoN@mXn?Z$?/(7a","inputs":{"IF0":{"block":{"type":"logic_floor_direction","id":"Vn:Yd+xx9/[9swf}a~n$","inputs":{"DIRECTION":{"block":{"type":"direction_left","id":"h*@d[Eg$OK^[McgnGOr_"}}}}},"DO0":{"block":{"type":"rotate","id":"yji2D1*W+UOf0CI!|s-W","inputs":{"DIRECTION":{"block":{"type":"direction_left","id":"?KW-v$L,Hqb28QR(i*H3"}}}}},"ELSE":{"block":{"type":"rotate","id":"O.h2C~s:q1%.BXa~.4{N","inputs":{"DIRECTION":{"block":{"type":"direction_left","id":"UK_zYdxU/)V6-6v*St43"}}},"next":{"block":{"type":"rotate","id":"zvEc^2gS3WMNA@BKY$x%","inputs":{"DIRECTION":{"block":{"type":"direction_left","id":"+_?toVj%Udv=6ZwEI~ig"}}}}}}}}}}}}}},"next":{"block":{"type":"move","id":"1WBw{N7eq^Qdqt0[J(q4"}}}}}}]}}
```
