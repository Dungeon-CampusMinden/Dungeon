---
title: "Blockly Lösung: Level 8"
---

## Blöcke
![solution](./img/loesung_level_008_1.png)
![solution](./img/loesung_level_008_2.png)
![solution](./img/loesung_level_008_3.png)
![solution](./img/loesung_level_008_4.png)

## Code

### Part 1

```java
for(int i = 1; i <= 4; i++){
    hero.move();
}
hero.rotate(Direction.RIGHT);
for(int i = 1; i <= 5; i++){
    hero.move();
}
hero.rotate(Direction.LEFT);
for(int i = 1; i <= 7; i++){
    hero.move();
}
hero.rotate(Direction.LEFT);
for(int i = 1; i <= 3; i++){
    hero.move();
}
hero.rotate(Direction.LEFT);
hero.move();
for(int i = 1; i <= 2; i++){
    hero.push();
}
for(int i = 1; i <= 2; i++){
    hero.rotate(Direction.LEFT);
}
for(int i = 1; i <= 3; i++){
    hero.move();
}
hero.rotate(Direction.RIGHT);
for(int i = 1; i <= 3; i++){
    hero.move();
}
hero.rotate(Direction.RIGHT);
for(int i = 1; i <= 7; i++){
    hero.move();
}
hero.rotate(Direction.RIGHT);
for(int i = 1; i <= 5; i++){
    hero.move();
}
hero.rotate(Direction.RIGHT);
for(int i = 1; i <= 3; i++){
    hero.move();
}
hero.rotate(Direction.RIGHT);
hero.move();
hero.push();
for(int i = 1; i <= 2; i++){
    hero.rotate(Direction.RIGHT);
}
hero.move();
for(int i = 1; i <= 4; i++){
    hero.move();
}
hero.rotate(Direction.RIGHT);
for(int i = 1; i <= 6; i++){
    hero.move();
}
hero.interact(Direction.INFRONT);
for(int i = 1; i <= 2; i++){
    hero.rotate(Direction.RIGHT);
}
for(int i = 1; i <= 6; i++){
    hero.move();
}
hero.rotate(Direction.LEFT);
for(int i = 1; i <= 5; i++){
    hero.move();
}
hero.pull();
for(int i = 1; i <= 2; i++){
    hero.rotate(Direction.RIGHT);
}
hero.move();
hero.rotate(Direction.LEFT);
for(int i = 1; i <= 3; i++){
    hero.move();
}
hero.rotate(Direction.LEFT);
for(int i = 1; i <= 5; i++){
    hero.move();
}
hero.rotate(Direction.LEFT);
for(int i = 1; i <= 5; i++){
    hero.move();
}
hero.rotate(Direction.RIGHT);
hero.move();
```

## Blockly String

### Part 1

```json
{"blocks":{"languageVersion":0,"blocks":[{"type":"start","id":"eSBL^x#hKBMT[y1d/6mk","x":-28,"y":-162,"deletable":false,"next":{"block":{"type":"repeat","id":"Sf4#Vh{Z1=2cX5Rf/[.{","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"m$jGpKYX?=J)8u{jzOK(","fields":{"REPEAT_NUMBER":4}}},"DO":{"block":{"type":"move","id":"I`I6Ta]`pODYh`4eV2^n"}}},"next":{"block":{"type":"rotate","id":";ZjLBsqdw7Ol_oZ(Fw:3","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"a*)e-Qrd`53140V14-Z5"}}},"next":{"block":{"type":"repeat","id":"0cq;/(`__#e,i.WP`#5t","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"K-p)q*!hjBiDe^7q-)|2","fields":{"REPEAT_NUMBER":5}}},"DO":{"block":{"type":"move","id":";k4BWJT]iAGrEm4M-og%"}}},"next":{"block":{"type":"rotate","id":"T:;A8/-rq}I:1)BGrfVB","inputs":{"DIRECTION":{"block":{"type":"direction_left","id":"I|~~@IE1}x{jdvg/ObD("}}},"next":{"block":{"type":"repeat","id":"*,n}7!]))!4pbn*D5E*.","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"#ww?!aqQ2H0Tl6N@*SxY","fields":{"REPEAT_NUMBER":7}}},"DO":{"block":{"type":"move","id":"Z[mbmn48Xr9JG5^K7sL*"}}},"next":{"block":{"type":"rotate","id":"6:[dYPiAuO9sdir/6^r,","inputs":{"DIRECTION":{"block":{"type":"direction_left","id":".?pYbhy,Szg];j9M;E%F"}}},"next":{"block":{"type":"repeat","id":"{vDFUu[X7!5Vk7kupczE","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"V813?)8;9eNeUNZ!pryw","fields":{"REPEAT_NUMBER":3}}},"DO":{"block":{"type":"move","id":"oH4Z0j`EB-g@K8MmkPzd"}}},"next":{"block":{"type":"rotate","id":"}Sgk_mzz%~@#09?:xXrZ","inputs":{"DIRECTION":{"block":{"type":"direction_left","id":".HyTnl*ib~V{PvB|Vskl"}}},"next":{"block":{"type":"move","id":"pfcuG}3=Yixnk:bK#j+j","next":{"block":{"type":"repeat","id":"2Pp|/aPm,P{/(h#w6Z7k","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"{mWUr.gc|/L{YE06Tb2x","fields":{"REPEAT_NUMBER":2}}},"DO":{"block":{"type":"push","id":"!$wF:z=BtHMpbCb9Q64,"}}},"next":{"block":{"type":"rotate","id":"Z;*`EhZAk6,B_eN%zxw~","inputs":{"DIRECTION":{"block":{"type":"direction_left","id":"2SXC+_*/s#ixm-=k?|I{"}}},"next":{"block":{"type":"rotate","id":"RdnRvjzA$[#8.vY2G[V0","inputs":{"DIRECTION":{"block":{"type":"direction_left","id":"]q^WNyhPzj#1/ZZr+rS`"}}},"next":{"block":{"type":"repeat","id":"!zssQlBrD+opzsw2x|~K","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"FtXK6+I:se}qV,C5uRJ[","fields":{"REPEAT_NUMBER":3}}},"DO":{"block":{"type":"move","id":"/|wgY2x}=-O*l#VWtd[="}}},"next":{"block":{"type":"rotate","id":"*}g(c*#1iMj%ia~N{}[u","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"UR?diBzae~8:sww^V#73"}}},"next":{"block":{"type":"repeat","id":"t6Kw3y}YjpjAB!R]jyN=","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"ee0UHl#FXmbT?pPceC[7","fields":{"REPEAT_NUMBER":3}}},"DO":{"block":{"type":"move","id":"`XzYSHM25AaRa1pb6$(Y"}}},"next":{"block":{"type":"rotate","id":"/aI8d;,C?Rxnx^qRIuu+","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"Jm]*W6,9xnjNltK{*Y}["}}},"next":{"block":{"type":"repeat","id":"[3m,[H0#eymx;5#]{u$d","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"mqY#zZ{ihUfG:}ji4|N#","fields":{"REPEAT_NUMBER":7}}},"DO":{"block":{"type":"move","id":"`8zwA0buR2o)sJA{M;8@"}}},"next":{"block":{"type":"rotate","id":"d}V[G/4}tyE7_(f.Ec{v","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"BXL7v8L`.^B@p~z1-%?K"}}},"next":{"block":{"type":"repeat","id":"VYCWry33$?*0Z6(9X{`a","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"!qZd/40ly3:`3pU}pH|7","fields":{"REPEAT_NUMBER":5}}},"DO":{"block":{"type":"move","id":"@`LQtRv6$#7yU^FDQI3,"}}},"next":{"block":{"type":"rotate","id":"TMaLm4=^4SPT8_^+QxKi","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"2T-^u#nhO`V(O.fW0p2~"}}},"next":{"block":{"type":"repeat","id":"Pp$nRbZX4C7Z$ay2,tyQ","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"0*~}0hc`oZ43ii|,{|`p","fields":{"REPEAT_NUMBER":3}}},"DO":{"block":{"type":"move","id":"k!gT_zism%i4eVLNL9Vy"}}},"next":{"block":{"type":"rotate","id":"f%ROD]ww!U=RGp[%57j=","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"0vpK7zrOG1fSBUL8f#6y"}}},"next":{"block":{"type":"move","id":"_^.YQ_Z1ZLV+i+nhZ|rF","next":{"block":{"type":"push","id":"`1zM;vu6S;7^QrHq?@gj","next":{"block":{"type":"rotate","id":"a/WDtv[w1JDa~R`@EvHd","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"/PS3^0BxUXa[dpgSx5a7"}}},"next":{"block":{"type":"rotate","id":"Vw=n6T_~g(4-):djOo{S","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"rK$``0ofEZHSz=b)7%Fg"}}},"next":{"block":{"type":"move","id":"Pv=n|e*{k}./Hkv0MF-,","next":{"block":{"type":"repeat","id":"v)NJ6*n1}S=3m-WLVlg$","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"w;~E3UzIPTPwjSt/6{J2","fields":{"REPEAT_NUMBER":4}}},"DO":{"block":{"type":"move","id":"-2]3okik~3ojb?Gh.}#5"}}},"next":{"block":{"type":"rotate","id":":KOr%#7(Ksi_~tF~^`Uy","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"RV#%4)s7mA9.ZtGFW`Bv"}}},"next":{"block":{"type":"repeat","id":"#CPS.azCX:{+~_eiJYFC","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"PI%d)=ogfN9Ax$n(^lTT","fields":{"REPEAT_NUMBER":6}}},"DO":{"block":{"type":"move","id":"k{qF=zE6mE$uLwC0:P17"}}},"next":{"block":{"type":"use","id":"/T4x`y_I8`kubrpFgx[Z","inputs":{"DIRECTION":{"block":{"type":"direction_up","id":"l}6w$rM(JUP81yq+dUFT"}}},"next":{"block":{"type":"rotate","id":"PS1O58odq#}1EM[BA?w-","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"Cc9*Bjgpo1XT~.^vX]v_"}}},"next":{"block":{"type":"rotate","id":"WGNrD26FCS(`{DviggaC","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"}@noxfcjpq6DzHTRW,x3"}}},"next":{"block":{"type":"repeat","id":"JN#1)C3-@lRww0Z(HL7)","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"+/[v7s?pEguNM@YF(!2c","fields":{"REPEAT_NUMBER":6}}},"DO":{"block":{"type":"move","id":"Bz}?mJNs8@MpQT$FII1p"}}},"next":{"block":{"type":"rotate","id":"LM6m~#.ez=H9U=A|w]f_","inputs":{"DIRECTION":{"block":{"type":"direction_left","id":"PS_UXYUFS@@_7ZolD#fY"}}},"next":{"block":{"type":"repeat","id":"yQs7aacr%a[O@?CVx_5h","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"Pgy6;@Kg;eszL!:KF%td","fields":{"REPEAT_NUMBER":5}}},"DO":{"block":{"type":"move","id":"%kWJ:xLtEJ#GcJOAU2(m"}}},"next":{"block":{"type":"pull","id":"XZ*v,#zZTUI3GQ!TmtW~","next":{"block":{"type":"rotate","id":"AnJgBn,(0DDi|p78)[Ac","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"A.L0Mi=@MgA_=qP2W`v3"}}},"next":{"block":{"type":"rotate","id":"pl8ZK:J45)#+94J%#Dn{","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"dk:XmS5Z_oX^#_5U$|Y{"}}},"next":{"block":{"type":"move","id":":44QrfUU@g`io|[-g~h}","next":{"block":{"type":"rotate","id":"qTa%qe#gXX0*!O6QP18s","inputs":{"DIRECTION":{"block":{"type":"direction_left","id":"K7Olv:jGhpx1Pc%L)C{k"}}},"next":{"block":{"type":"repeat","id":"`.tn8~OfJNj5wQC0p,x%","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"T2L)cEPvcU/Eb/mh8?`P","fields":{"REPEAT_NUMBER":3}}},"DO":{"block":{"type":"move","id":"N%VgJx9%[aVS.CHmpu$I"}}},"next":{"block":{"type":"rotate","id":"+P{OqCg=GNi`$st|P3,;","inputs":{"DIRECTION":{"block":{"type":"direction_left","id":"*nELVNwB]ojG#g+j*jhY"}}},"next":{"block":{"type":"repeat","id":"3e](;YJIkhi`hCN`Y(5n","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"iJQn*DK8fE#)syfaB[g6","fields":{"REPEAT_NUMBER":5}}},"DO":{"block":{"type":"move","id":"Jzx#2~s3j;Qz%(_evXp/"}}},"next":{"block":{"type":"rotate","id":"]W%G6Ns`1AMXHXObDxSx","inputs":{"DIRECTION":{"block":{"type":"direction_left","id":"gu29gl+[N~w{VCe`#^=Z"}}},"next":{"block":{"type":"repeat","id":"BeCQJ0o6nrZ@A@(Z%?!F","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"F3}5b5^b}6*0H}M#,$LF","fields":{"REPEAT_NUMBER":5}}},"DO":{"block":{"type":"move","id":"p?2KtAGHF7vW+LqBx)G@"}}},"next":{"block":{"type":"rotate","id":"YjoyVh=y8DA39fex,kiP","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"rW(dHF%+iWYx71%NO=~E"}}},"next":{"block":{"type":"repeat","id":"Hn4[Rs^}Z}IuF[2gs/Xs","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"P=o^`Z?lE)W7iA0CSX#i","fields":{"REPEAT_NUMBER":3}}},"DO":{"block":{"type":"move","id":"0wa:t[!*=*be%og.*y]V"}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}]}}
```
