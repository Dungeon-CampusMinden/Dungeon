---
title: "Blockly Lösung: Level 3"
---

## Blöcke
![solution](./img/loesung_level_003.png)

## Code

```java
for(int i = 1; i < 9; i++){
    hero.move();
}
hero.rotate(Direction.RIGHT);
for(int i = 1; i < 6; i++){
    hero.move();
}
hero.rotate(Direction.LEFT);
for(int i = 1; i < 9; i++){
    hero.move();
}
hero.rotate(Direction.LEFT);
for(int i = 1; i < 11; i++){
    hero.move();
}
hero.rotate(Direction.LEFT);
for(int i = 1; i < 18; i++){
    hero.move();
}
```

## Blockly String
```json
{"blocks":{"languageVersion":0,"blocks":[{"type":"start","id":"={9Mof5xE4x:02,pxjC]","x":-154,"y":-645,"deletable":false,"next":{"block":{"type":"repeat","id":"B9(pj}QDNav9H]gCnx?C","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"i-L37v)x$2pAO4EVQ4WL","fields":{"REPEAT_NUMBER":8}}},"DO":{"block":{"type":"move","id":"w2(AO$J1kSy4iYVRnA^-"}}},"next":{"block":{"type":"rotate","id":"JV-o5*,YA!kJw2U`KaJ;","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"Qc6O;b7FMd_0@dd^BO8)"}}},"next":{"block":{"type":"repeat","id":";X8`i#-f^23+Y[?+bk~X","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"^92jm)SO]h;7,H8@2$jQ","fields":{"REPEAT_NUMBER":5}}},"DO":{"block":{"type":"move","id":"vMJfr^Yo`p9fm8o3GgJD"}}},"next":{"block":{"type":"rotate","id":"hlD*+uA52g(e!*lF2oZg","inputs":{"DIRECTION":{"block":{"type":"direction_left","id":"GoCF^^Xh81R;:;ugz4hM"}}},"next":{"block":{"type":"repeat","id":"EPwafA;2Yr3$W3S+1Ff(","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"qMaS]Drr{h=DdUKcp[i.","fields":{"REPEAT_NUMBER":8}}},"DO":{"block":{"type":"move","id":"3+j(dn%Ky05I}]BQS4eK"}}},"next":{"block":{"type":"rotate","id":"ztkRWSJX`7p)8z~8:SiM","inputs":{"DIRECTION":{"block":{"type":"direction_left","id":"}ZpIu^R[MCWOgVZfO5r_"}}},"next":{"block":{"type":"repeat","id":"]A{CEPTV%*}G~nLqY+l.","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"FZBpBDl,~[/}=ZxSQVnH","fields":{"REPEAT_NUMBER":10}}},"DO":{"block":{"type":"move","id":")sNH_bNHs#1hw%MdC5/T"}}},"next":{"block":{"type":"rotate","id":"hzWn=x-~-,6tC+q1?nE5","inputs":{"DIRECTION":{"block":{"type":"direction_left","id":"ThP#uA8A]2q/zVNMBo]:"}}},"next":{"block":{"type":"repeat","id":"%eqB9uJ?eHmo|Of`74Tq","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"L;H?)_@XJmrbqpk!?F0]","fields":{"REPEAT_NUMBER":17}}},"DO":{"block":{"type":"move","id":"::%x$I=tb|Q3[Zzy.Dap"}}}}}}}}}}}}}}}}}}}}}}]}}
```
