---
title: "Blockly Lösung: Level 21"
---

## Blöcke
![solution](./img/loesung_level_021.png)

## Code

```java
for(int i = 0; i < 12; i++){
    hero.move();
}
hero.rotate(Direction.LEFT);
for(int i = 0; i < 6; i++){
    hero.move();
}
hero.rotate(Direction.LEFT);
for(int i = 0; i < 2; i++){
    hero.move();
}
hero.rotate(Direction.RIGHT);
for(int i = 0; i < 4; i++){
    hero.move();
}
hero.rotate(Direction.LEFT);
for(int i = 0; i < 6; i++){
    hero.move();
}
hero.rotate(Direction.RIGHT);
for(int i = 0; i < 9; i++){
    hero.move();
}
hero.rotate(Direction.RIGHT);
for(int i = 0; i < 5; i++){
    hero.move();
}
hero.rotate(Direction.LEFT);
for(int i = 0; i < 6; i++){
    hero.move();
}
hero.rotate(Direction.RIGHT);
hero.move();
hero.rotate(Direction.LEFT);
hero.move();
hero.move();
```

## Blockly String
```json
{"blocks":{"languageVersion":0,"blocks":[{"type":"start","id":"={9Mof5xE4x:02,pxjC]","x":-114,"y":-516,"deletable":false,"next":{"block":{"type":"repeat","id":"}{B]Q[[rsv[)bSS2=nc/","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"uox+9b-u?1t^uR0q5^Nu","fields":{"REPEAT_NUMBER":12}}},"DO":{"block":{"type":"move","id":"v-=}[hhp=)eswa]U.%Dz"}}},"next":{"block":{"type":"rotate","id":"V[TtUV!;_KHRcn31YVws","inputs":{"DIRECTION":{"block":{"type":"direction_left","id":"q%-m~o)h|~lL*W3Sl}h+"}}},"next":{"block":{"type":"repeat","id":"i2jE(.[#31{v0D9DY7q0","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"gFV6(Ob4pas!XQFD{+CE","fields":{"REPEAT_NUMBER":6}}},"DO":{"block":{"type":"move","id":")eS/(rOnfw8dZstq!wpo"}}},"next":{"block":{"type":"rotate","id":"CTg)6jNM9N9Z4HwX|pmd","inputs":{"DIRECTION":{"block":{"type":"direction_left","id":"}S$;9N9BRH-od~OYkA~n"}}},"next":{"block":{"type":"repeat","id":"RJl=icQU`aq3=!k=)o{w","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"]mKZTXBg4]xbZExr=e_V","fields":{"REPEAT_NUMBER":2}}},"DO":{"block":{"type":"move","id":"ddtZdq=7%7}o=4$~.F2B"}}},"next":{"block":{"type":"rotate","id":"/9~Df@.]Y6?#fWd@L@3Q","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"O1(..g-;k!PC,Qjcf;!+"}}},"next":{"block":{"type":"repeat","id":"~{^-56~rBL((7^`(zmw7","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"@@A4d!A+|fvpjemV0@oJ","fields":{"REPEAT_NUMBER":4}}},"DO":{"block":{"type":"move","id":"~ZF{lHz_g_|,)K;Dv(fd"}}},"next":{"block":{"type":"rotate","id":"YvG?pVrnFLxe9FpECQ%j","inputs":{"DIRECTION":{"block":{"type":"direction_left","id":"k#`*:#[h3LSOZB5#,/Tg"}}},"next":{"block":{"type":"repeat","id":")x#gU]Q_16_rIWtJ6cW/","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"V3Wn/aWw![FX@X+A[m_2","fields":{"REPEAT_NUMBER":6}}},"DO":{"block":{"type":"move","id":"%c/[DR_/B$.S5]W8mk/0"}}},"next":{"block":{"type":"rotate","id":"I3Rz|nyK7582frfWnHCz","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"ha[6j8UsoeZtC88AI69."}}},"next":{"block":{"type":"repeat","id":"*+Hb$jrdkx{w7QA!!}Rf","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"t~b%w9J7,J3B||mcd%Th","fields":{"REPEAT_NUMBER":9}}},"DO":{"block":{"type":"move","id":"__OF04atCx8,`@8Crkiz"}}},"next":{"block":{"type":"rotate","id":"oNX3!pkbi}!e-1dv7|+r","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"G.)7[LhJ/U_yxVRN=-_-"}}},"next":{"block":{"type":"repeat","id":"gzgN(p3h*Ak`.MtureNj","inputs":{"TIMES":{"block":{"type":"repeat_number","id":";~rBVN^IxIy~ra3OWsE:","fields":{"REPEAT_NUMBER":5}}},"DO":{"block":{"type":"move","id":"?Vc~v@MV*t]m?EQeZ$jw"}}},"next":{"block":{"type":"rotate","id":"+VwgDZH?uwP}+70*1]@.","inputs":{"DIRECTION":{"block":{"type":"direction_left","id":"CYK.KP-`bOd@R[e|9n,O"}}},"next":{"block":{"type":"repeat","id":"[R(~xEu?Sd`Rpxhjv7{n","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"obTL@Za]ipg6{=astc?q","fields":{"REPEAT_NUMBER":6}}},"DO":{"block":{"type":"move","id":"[LOY/glnk*PZ9;t+u@R@"}}},"next":{"block":{"type":"rotate","id":"]AB8/UZ[Gikj{Z?K^q+5","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":")tG3rd;hGeF}`d/;h.?D"}}},"next":{"block":{"type":"move","id":"RgN.y/vm)^,+n:2,J*oQ","next":{"block":{"type":"rotate","id":"nBu_5^x$01^B?^uSO=#$","inputs":{"DIRECTION":{"block":{"type":"direction_left","id":";;h)8TPk}(2D0V0w`ZmS"}}},"next":{"block":{"type":"move","id":"%xiHJv$2wVwbtyS]ig_A","next":{"block":{"type":"move","id":"qch^mZQPE:b``l3a0SUw"}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}]}}
```
