---
title: "Blockly Lösung: Level 10"
---

## Blöcke
![solution](./img/loesung_level_010_1.png)
![solution](./img/loesung_level_010_2.png)

## Code

```java
for(int i = 1; i <= 3; i++){
    hero.move();
}
hero.rotate(Direction.LEFT);
for(int i = 1; i <= 3; i++){
    hero.pickup();
    hero.move();
}
for(int i = 1; i <= 2; i++){
    hero.rotate(Direction.LEFT);
}
for(int i = 1; i <= 2; i++){
    hero.move();
}
hero.shootFireball();
hero.rotate(Direction.LEFT);
hero.shootFireball();
for(int i = 1; i <= 3; i++){
    hero.move();
}
hero.rotate(Direction.RIGHT);
for(int i = 1; i <= 3; i++){
    hero.move();
}
hero.rotate(Direction.LEFT);
for(int i = 1; i <= 4; i++){
    hero.move();
}
hero.shootFireball();
hero.rotate(Direction.LEFT);
for(int i = 1; i <= 3; i++){
    hero.move();
}
hero.pickup();
hero.rotate(Direction.RIGHT);
hero.shootFireball();
hero.rotate(Direction.LEFT);
hero.move();
hero.pickup();
hero.rotate(Direction.RIGHT);
for(int i = 1; i <= 4; i++){
    hero.move();
}
hero.rotate(Direction.RIGHT);
for(int i = 1; i <= 5; i++){
    hero.move();
}
hero.rotate(Direction.RIGHT);
hero.shootFireball();
hero.rotate(Direction.LEFT);
hero.move();
hero.rotate(Direction.RIGHT);
for(int i = 1; i <= 12; i++){
    hero.move();
}
hero.rotate(Direction.RIGHT);
for(int i = 1; i <= 2; i++){
    hero.move();
}
hero.rotate(Direction.LEFT);
for(int i = 1; i <= 3; i++){
    hero.move();
}
```

## Blockly String
```json
{"blocks":{"languageVersion":0,"blocks":[{"type":"start","id":"={9Mof5xE4x:02,pxjC]","x":-50,"y":-430,"deletable":false,"next":{"block":{"type":"repeat","id":"UBCqx;CtC@hwBx.$,7Vr","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"~s3smKE7ZNyfG=%.VIhr","fields":{"REPEAT_NUMBER":3}}},"DO":{"block":{"type":"move","id":"o@hU);srYU(P5it:bZz:"}}},"next":{"block":{"type":"rotate","id":"YnzbK]_*+!k;fm`Z%Xl}","inputs":{"DIRECTION":{"block":{"type":"direction_left","id":"@X5w8nXmAPPl(m(CQkA4"}}},"next":{"block":{"type":"repeat","id":"j46B2+en]XimlK`0R%/*","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"ftz@@8XCgB2|{^j!Sk,Q","fields":{"REPEAT_NUMBER":3}}},"DO":{"block":{"type":"pickup","id":"qD0?EQ}3nZ0_UXzKSbN$","next":{"block":{"type":"move","id":"[cLB14%~y;jiMP+FT%ah"}}}}},"next":{"block":{"type":"repeat","id":"AeBRgz4}Z#^!@mr*V%sc","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"ZSv;Pn`Wb^F#564g8Qx.","fields":{"REPEAT_NUMBER":2}}},"DO":{"block":{"type":"rotate","id":"os`N^5Pj+bMc_w$HRGU]","inputs":{"DIRECTION":{"block":{"type":"direction_left","id":"GSY7`MwG3JEA8RAE|xUs"}}}}}},"next":{"block":{"type":"repeat","id":"!^DTi?ggEjg3mf.c:Rgi","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"m|xZ6RJK.;m=cKNhpX[x","fields":{"REPEAT_NUMBER":2}}},"DO":{"block":{"type":"move","id":";-fPqC^FbPJso1bwN)4*"}}},"next":{"block":{"type":"fireball","id":"2B:,:aWO.9shP3StbPN@","next":{"block":{"type":"rotate","id":"mUdN2+7ZDc!:T}YuZXz+","inputs":{"DIRECTION":{"block":{"type":"direction_left","id":"x70)ug;!bXspF8Bn?`bx"}}},"next":{"block":{"type":"fireball","id":"vRi9!!uRRH*U:}s@`Gwn","next":{"block":{"type":"repeat","id":"`}2t%qzRJiu,)yL_.Ot8","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"K3P)kiYd,gfNI~Fr+:ze","fields":{"REPEAT_NUMBER":3}}},"DO":{"block":{"type":"move","id":"2(e}$E9/e;zcqWqZQj@w"}}},"next":{"block":{"type":"rotate","id":"A?L/JI*7kH1)3XY2t-Lf","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"J[#?_AZrOJPjkS=G.2%v"}}},"next":{"block":{"type":"repeat","id":"/xV8+Z)H8Pe)X7o~hCvk","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"mp}p9yixhza=PX5PPf.a","fields":{"REPEAT_NUMBER":3}}},"DO":{"block":{"type":"move","id":"H2QYuI5MkVdQhXk5L?Wp"}}},"next":{"block":{"type":"rotate","id":"Qsc6a{~-Z:?~nb9ZIN?U","inputs":{"DIRECTION":{"block":{"type":"direction_left","id":"2jT30h+T$RrVE}mmQQ9g"}}},"next":{"block":{"type":"repeat","id":"AzRerQgn_eXn=C#{{ZMn","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"b,i3s4FBag16uRq%^i}u","fields":{"REPEAT_NUMBER":4}}},"DO":{"block":{"type":"move","id":"d0S{*i+DOL0sq#ev6:hq"}}},"next":{"block":{"type":"fireball","id":"rWpK{67GOp!+3K:b$0y~","next":{"block":{"type":"rotate","id":"hNKl3[9I#2RG6DIPC7)f","inputs":{"DIRECTION":{"block":{"type":"direction_left","id":"P2/!x*m*cI($jP)*eNJ;"}}},"next":{"block":{"type":"repeat","id":"OIOh^z3Qh@*+;=5^bq,6","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"~xKR$EDspy[(@*mz`hk-","fields":{"REPEAT_NUMBER":3}}},"DO":{"block":{"type":"move","id":"Xe)TS#G9fVP}J_h@Scq("}}},"next":{"block":{"type":"pickup","id":"=VGcp^E,Zc:TN-+;xWOt","next":{"block":{"type":"rotate","id":"U4CCVD{SgV^8hrW8Tj/v","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"={Q.|82(Q;5I?#jh%6|Z"}}},"next":{"block":{"type":"fireball","id":"k6;a(e,xppD9FUCfbOpY","next":{"block":{"type":"rotate","id":"B1-d==Jldj2%|18bpVWp","inputs":{"DIRECTION":{"block":{"type":"direction_left","id":"xK;wiJ1(h~K6jL_S[#Ry"}}},"next":{"block":{"type":"move","id":"5Lj2O8z1y0V@#mH4x;uG","next":{"block":{"type":"pickup","id":"1}+X]XvD,|uQ,u)oYBD!","next":{"block":{"type":"rotate","id":"KQ1@2P`cl/:u_@J{Mt`X","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"XL#OyoK=ofTp4^;@oJR`"}}},"next":{"block":{"type":"repeat","id":"GY!%6Lm42L+9@Jy^68{q","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"P+f(|UeiJXsH8tu7Uk_2","fields":{"REPEAT_NUMBER":4}}},"DO":{"block":{"type":"move","id":"8g^[:QYawOFZQs`u[Uu{"}}},"next":{"block":{"type":"rotate","id":"T[w@rJ.h(pzMpFx(b+?l","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"ozhm/Czd{*[f{{xlR:E|"}}},"next":{"block":{"type":"repeat","id":"Lp3JJzQN*?j]|`*=M-Nk","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"DdM$Kh0N6:W$5.)xVI{*","fields":{"REPEAT_NUMBER":5}}},"DO":{"block":{"type":"move","id":"HyAJ$MQA8V=mSc@wn%GR"}}},"next":{"block":{"type":"rotate","id":"aQtM2Wq*NaDQ@Y_2kjRL","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"D2~?n/giFa,x/4S7)MJe"}}},"next":{"block":{"type":"fireball","id":"CL9pb6;jRO*NY@No.wG0","next":{"block":{"type":"rotate","id":"Jg{*Az$Zhf[z;;f-fBrA","inputs":{"DIRECTION":{"block":{"type":"direction_left","id":"a6hAuk4!]q=r5F2LJAbx"}}},"next":{"block":{"type":"move","id":"tp=ha*5X9!WzeM+WBHW6","next":{"block":{"type":"rotate","id":"Cd|kJ;JElhw=B1YwQuPk","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"OqKrPuS[5Fyo6r={/ii,"}}},"next":{"block":{"type":"repeat","id":"TA0A.0Do|1*u~0N#^ki/","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"rZhXK@|7s$!jsQW7zSce","fields":{"REPEAT_NUMBER":12}}},"DO":{"block":{"type":"move","id":"}{%T*;Oe@Lvp[,%5bgmr"}}},"next":{"block":{"type":"rotate","id":"ln!yI{$D1|LynT87[bGA","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"DOXpb0JHAUW5Z;Roo*`j"}}},"next":{"block":{"type":"repeat","id":"y)MQq)0N^%]a4Pk_R%%j","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"qON[;}hyeX1pRn=JVdVK","fields":{"REPEAT_NUMBER":2}}},"DO":{"block":{"type":"move","id":"r-Q,NY:$#B6mL:_.z#pN"}}},"next":{"block":{"type":"rotate","id":"Ar~dbS~`QM4i#vk#q-8B","inputs":{"DIRECTION":{"block":{"type":"direction_left","id":"47[6rBQ}?ab69;B(MS=P"}}},"next":{"block":{"type":"repeat","id":"{vp9YEDc:$:?@xiBr_lj","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"gLs5Gtni9{D`bfG1~4|6","fields":{"REPEAT_NUMBER":3}}},"DO":{"block":{"type":"move","id":",;610-6BmS`FgJ~yxe:z"}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}]}}
```
