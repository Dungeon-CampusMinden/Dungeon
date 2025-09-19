---
title: "Blockly Lösung: Level 7"
---

## Blöcke
![solution](./img/loesung_level_007.png)

## Code

```java
hero.move();
hero.move();
hero.rotate(Direction.RIGHT);
hero.move();
hero.move();
hero.interact(Direction.HERE);
hero.rotate(Direction.RIGHT);
for(int i = 1; i <= 7; i++){
    hero.move();
}
hero.interact(Direction.RIGHT);
for(int i = 1; i <= 2; i++){
    hero.rotate(Direction.RIGHT);
}
for(int i = 1; i <= 13; i++){
    hero.move();
}
hero.interact(Direction.RIGHT);
for(int i = 1; i <= 2; i++){
    hero.rotate(Direction.RIGHT);
}
for(int i = 1; i <= 6; i++){
    hero.move();
}
hero.rotate(Direction.RIGHT);
for(int i = 1; i <= 4; i++){
    hero.move();
}
hero.interact(Direction.LEFT);
for(int i = 1; i <= 2; i++){
    hero.rotate(Direction.RIGHT);
}
for(int i = 1; i <= 11; i++){
    hero.move();
}

Ohne Direction.HERE:
hero.move();
hero.move();
hero.rotate(Direction.RIGHT);
hero.move();
hero.interact(Direction.UP);
hero.move();
hero.rotate(Direction.RIGHT);
for(int i = 1; i <= 7; i++){
    hero.move();
}
hero.interact(Direction.RIGHT);
for(int i = 1; i <= 2; i++){
    hero.rotate(Direction.RIGHT);
}
for(int i = 1; i <= 13; i++){
    hero.move();
}
hero.interact(Direction.RIGHT);
for(int i = 1; i <= 2; i++){
    hero.rotate(Direction.RIGHT);
}
for(int i = 1; i <= 6; i++){
    hero.move();
}
hero.rotate(Direction.RIGHT);
for(int i = 1; i <= 4; i++){
    hero.move();
}
hero.interact(Direction.LEFT);
for(int i = 1; i <= 2; i++){
    hero.rotate(Direction.RIGHT);
}
for(int i = 1; i <= 11; i++){
    hero.move();
}
```

## Blockly String
```json
{"blocks":{"languageVersion":0,"blocks":[{"type":"start","id":"={9Mof5xE4x:02,pxjC]","x":-286,"y":-870,"deletable":false,"next":{"block":{"type":"move","id":"Dso,~;w,N,$QTkH8Iy/|","next":{"block":{"type":"move","id":",D|[.R44TpR^q=DZ9.{5","next":{"block":{"type":"rotate","id":"-?_/OK.RFIL)9wWAsF[=","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"|W.LP-b|uZg:0Iw_,D*B"}}},"next":{"block":{"type":"move","id":"(PINPo`+PEXQ1`APMtN0","next":{"block":{"type":"move","id":"G4B,(G4^upc5aeS_bwzH","next":{"block":{"type":"use","id":"XW)*_4W=%jpZ(J59gyn4","inputs":{"DIRECTION":{"block":{"type":"direction_here","id":"z6s8^]iPAInN01df@CR,"}}},"next":{"block":{"type":"rotate","id":"4jy5athou4R+2+Rf`2(x","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"uC@W#hk{eXR-pIE=f@nL"}}},"next":{"block":{"type":"repeat","id":"]$3Z[f:^=gyAFhckmCg=","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"g$-s)6#:Re8SXOf1bVnY","fields":{"REPEAT_NUMBER":6}}},"DO":{"block":{"type":"move","id":")qU;1uuyG09={qlruQTc"}}},"next":{"block":{"type":"use","id":"iGfZRhtr5BM3$bxS}[{.","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"yxSm4!_a|Z_w{ehbSX!d"}}},"next":{"block":{"type":"repeat","id":"{jrO]QnGX^Jl?ue%gDn;","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"Txi@UcJ09VGCKbyG|L8D","fields":{"REPEAT_NUMBER":2}}},"DO":{"block":{"type":"rotate","id":"hM7ft.A:m#S~mV:+6qk2","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"}fM,khP@QMpc`rU?;IQ,"}}}}}},"next":{"block":{"type":"repeat","id":"DtODg~JZ@HJ,L]CYk$MT","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"8#Ph+2jkvx@LpetB,.?l","fields":{"REPEAT_NUMBER":12}}},"DO":{"block":{"type":"move","id":"-]JuDM9fy2@/=`Vf0cM("}}},"next":{"block":{"type":"use","id":"V%akjIQMiBH[K!DxdnTV","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"Mjr6,RxQ4UB1%`Aug@KI"}}},"next":{"block":{"type":"repeat","id":"?M@#(-IL|R|YD8VDpQo+","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"+kRY_$)zr|D$R$Vl|]X.","fields":{"REPEAT_NUMBER":2}}},"DO":{"block":{"type":"rotate","id":"+l5A-tTec+Klj~,Ed^kq","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"r#J0y5D6~R/.P2[AVu7_"}}}}}},"next":{"block":{"type":"repeat","id":"V8YU^!((*,Jk2VSv,UPM","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"p;w}Hw}X}vjs%x+@/z(y","fields":{"REPEAT_NUMBER":6}}},"DO":{"block":{"type":"move","id":"qjqEpuJAt+@$0!|9oh{w"}}},"next":{"block":{"type":"rotate","id":",yw.PG`e!Cvy!fs`k}dc","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"e.EH5ANr!(^Uo/jT5o^+"}}},"next":{"block":{"type":"repeat","id":"a2bSkPMAT1hU9)#0cISz","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"e40OEI7_tjf2^!nSfZog","fields":{"REPEAT_NUMBER":4}}},"DO":{"block":{"type":"move","id":"~?1Nd2MZ{g-q`NbJ;F#O"}}},"next":{"block":{"type":"use","id":"2tpM4s82R#xp1FB[#N4G","inputs":{"DIRECTION":{"block":{"type":"direction_left","id":"*B$-.y[PPKCz9l2t!/s;"}}},"next":{"block":{"type":"repeat","id":"M(Qv2%9t(0he#)`emJ6k","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"AA=Re1)ArAU,a^/TQZcJ","fields":{"REPEAT_NUMBER":2}}},"DO":{"block":{"type":"rotate","id":"Z_=IdxX?HtdX-)omc/$Z","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"~wJaR[n}7ry2Nzz=-W*s"}}}}}},"next":{"block":{"type":"repeat","id":"5{zW%6ikv{CfN?/)66pz","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"T|^n0GK3|T.rs_0{_~F?","fields":{"REPEAT_NUMBER":10}}},"DO":{"block":{"type":"move","id":"j~!hF}Ey)g?8Z@Vchy-y"}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}]}}
```
