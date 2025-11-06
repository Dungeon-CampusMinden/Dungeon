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
{"blocks":{"languageVersion":0,"blocks":[{"type":"start","id":"eSBL^x#hKBMT[y1d/6mk","x":0,"y":0,"deletable":false,"next":{"block":{"type":"repeat","id":"/0-htIpeeX%OXj+d3@-c","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"dBNL_0-Jcx9wg~.s:jP5","fields":{"REPEAT_NUMBER":6}}},"DO":{"block":{"type":"move","id":"Y+`(f%#iqKHke5-H8S3)"}}},"next":{"block":{"type":"rotate","id":"{JmvOZX(dQ[KSi/{eW^P","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"a3{;yK|85jjP2;mvLW2O"}}},"next":{"block":{"type":"repeat","id":"ZmWTjvaB}fyJFomNBkls","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"+7^+x[A7.7la7Ca,*#DC","fields":{"REPEAT_NUMBER":4}}},"DO":{"block":{"type":"move","id":"*M[/q9?^/XYG-?Q+BnWF","next":{"block":{"type":"use","id":"]2P^Mif.eFJXbh,S]o]x","inputs":{"DIRECTION":{"block":{"type":"direction_up","id":"WLdEUhD)El]+TT8zls*-"}}}}}}}},"next":{"block":{"type":"rotate","id":"r+acRO{aUl-iXSr`4]BS","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"Ph6swd@G{p~OdF;!0^Eq"}}},"next":{"block":{"type":"rotate","id":"m$dtYmtsa643EVL`{WAA","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"=(buj:$m2.l8=7rI2Gy7"}}},"next":{"block":{"type":"repeat","id":"Plb+`w_K/RfHj:~1^ins","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"Meos~8=;`YO^I|Ct}ITP","fields":{"REPEAT_NUMBER":3}}},"DO":{"block":{"type":"move","id":"n`PLy$G?TTtZgZ@=/xX;"}}},"next":{"block":{"type":"rotate","id":"tBI1W=H3N`8+4Bp~/_yI","inputs":{"DIRECTION":{"block":{"type":"direction_left","id":"LsBp0sDN+Qb3[)i-+`9a"}}},"next":{"block":{"type":"repeat","id":"fAQE#s3L6`nq}z*{pGX2","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"frvONqD(FnMtFzhA.f+7","fields":{"REPEAT_NUMBER":4}}},"DO":{"block":{"type":"move","id":"d,oIHC=X9H$|u4|xJQla"}}},"next":{"block":{"type":"rotate","id":"D_Ie~Yy?X~/31|C9RQVb","inputs":{"DIRECTION":{"block":{"type":"direction_left","id":"?Y)pK4C8v?Oquke1@sMO"}}},"next":{"block":{"type":"repeat","id":"QmetJ-;jM|[N2Hs,3K).","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"jeSrErVeG,94+3I#2%=l","fields":{"REPEAT_NUMBER":3}}},"DO":{"block":{"type":"move","id":"f4!(b9l{=3S5:;F6DiP{"}}},"next":{"block":{"type":"repeat","id":"Ta!V16hhnL735Y.;nms3","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"^|2Mq``~J9%qaWrIz}vA","fields":{"REPEAT_NUMBER":5}}},"DO":{"block":{"type":"pull","id":"pW3[$j0._dBlp$2x7emO"}}},"next":{"block":{"type":"rotate","id":"BmS!?`(7ReGtQIO.CqXQ","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"vWDr~Oc@cWRAW,:Gxt*x"}}},"next":{"block":{"type":"repeat","id":"#h.:72fxOM6#Qel]6!p%","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"3[`Mjri1K!z`j^%*-h|K","fields":{"REPEAT_NUMBER":2}}},"DO":{"block":{"type":"move","id":"cw1M-#:*k.c3%G7Us:Wf","next":{"block":{"type":"rotate","id":"asX)V]6]EjN;z^H,$b9$","inputs":{"DIRECTION":{"block":{"type":"direction_left","id":"!4J4Z(N9Z:kUVET..W*U"}}}}}}}},"next":{"block":{"type":"push","id":"l(CCtbpTLXmkc84L,%)%","next":{"block":{"type":"rotate","id":"F.1q1X!A4o6y!v=PFN6?","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"|}u-GMK}6k^)wWdL:.:@"}}},"next":{"block":{"type":"repeat","id":"`Ewke]f(5fpT+k_5vtDT","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"(w8nHY1nD0?`)UWdsShr","fields":{"REPEAT_NUMBER":2}}},"DO":{"block":{"type":"move","id":"_K+E{xHcNSZbGN]3#1Zx","next":{"block":{"type":"rotate","id":"r~.;0L*[a($~vY,-Tx[;","inputs":{"DIRECTION":{"block":{"type":"direction_left","id":"2G=a!8.AC$-SI4srzY0."}}}}}}}},"next":{"block":{"type":"push","id":"@/*3S*E`*_t=9ge0^5,A","next":{"block":{"type":"push","id":"Cjg{T*bPUl,*:)BhUlNn","next":{"block":{"type":"rotate","id":"}JjUBX/AkrPmT8Zg`#T4","inputs":{"DIRECTION":{"block":{"type":"direction_left","id":"t~,4}vKV7$#mIR;A-AT?"}}},"next":{"block":{"type":"move","id":"%-[qLh4Fp.S2sHHXvHre","next":{"block":{"type":"rotate","id":",aL;L$QCDx[(I)#MPuZc","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"RbGP~A+#lOta$7$nZsU["}}},"next":{"block":{"type":"move","id":"K0NDU04[m@0.{`FNj$:v","next":{"block":{"type":"repeat","id":"u+Vsk:sDy3eK;KCl;TcH","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"Zb;)PwjH=;?W2=8{8f*Y","fields":{"REPEAT_NUMBER":2}}},"DO":{"block":{"type":"move","id":"bDSKGgoZRMWn2#P,!D4B","next":{"block":{"type":"rotate","id":"WefTd*N#_ZsN,cmcBj.s","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"Q$K:rNlmXHIhR8I389k`"}}}}}}}},"next":{"block":{"type":"repeat","id":"uPQ-YG`fhV9Ne^,kir#{","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"{Qd*kNcox}m:W6S0YJ5?","fields":{"REPEAT_NUMBER":7}}},"DO":{"block":{"type":"pull","id":"MHHdlvC1mgae^B)TRC)0"}}},"next":{"block":{"type":"rotate","id":"yec8%`[$C+Aui=rY6SW|","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":".QJqw(*su/AT8(Hzs*)H"}}},"next":{"block":{"type":"repeat","id":"]BI6@uw#|B?Su(,!`9Ce","inputs":{"TIMES":{"block":{"type":"repeat_number","id":":XqInKxo`;}`]/H7~kN~","fields":{"REPEAT_NUMBER":2}}},"DO":{"block":{"type":"move","id":"U40m%U$+CPo#xmX?FYbS","next":{"block":{"type":"rotate","id":"D39Oou~Ku[H3_VGR749x","inputs":{"DIRECTION":{"block":{"type":"direction_left","id":"^JtAum-htsm%u[Vv9[)F"}}}}}}}},"next":{"block":{"type":"push","id":"5nC7inTP80c7c8BJJx]r","next":{"block":{"type":"rotate","id":"2Qc6Ay0EAeW8_MDEKr$O","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"Vbj!g~9][cfh.N)_0/,T"}}},"next":{"block":{"type":"move","id":"EG4A|_@)c{stNhzxPfF)","next":{"block":{"type":"rotate","id":"f5Q+z^kk;Z=iJ;{W^xGz","inputs":{"DIRECTION":{"block":{"type":"direction_left","id":"gERu{2Ts#~SowztQiu?v"}}},"next":{"block":{"type":"repeat","id":"QOu.FRQxlu6nJ[U?4{(G","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"*k4!92Z*4GPf5w`29T|!","fields":{"REPEAT_NUMBER":5}}},"DO":{"block":{"type":"move","id":"u8*{hq#TuRhi%:+l@[VL"}}},"next":{"block":{"type":"rotate","id":":QB6H;~=uRbKOS3AI~1T","inputs":{"DIRECTION":{"block":{"type":"direction_left","id":"=$XG([ETmaS2*X8sTpIN"}}},"next":{"block":{"type":"move","id":";;.C/]7jCh7RHVaKeJ}M","next":{"block":{"type":"rotate","id":"5vK|v..vUv#f5R)CzXx6","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"nqBTpp,vFW**IYNI/lJ1"}}},"next":{"block":{"type":"move","id":"a{M7UOq1Q;$;P_*pGV{N"}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}]}}
```
