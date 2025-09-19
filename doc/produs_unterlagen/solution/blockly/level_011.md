---
title: "Blockly Lösung: Level 11"
---

## Blöcke
![solution](./img/loesung_level_011_1.png)
![solution](./img/loesung_level_011_2.png)
![solution](./img/loesung_level_011_3.png)
![solution](./img/loesung_level_011_4.png)

## Code

```java
hero.rotate(Direction.RIGHT);
for(int i = 1; i <= 3; i++){
    hero.move();
}
hero.push();
hero.rotate(Direction.LEFT);
for(int i = 1; i <= 3; i++){
    for(int x = 1; x <= 5; x++){
        hero.move();
    }
    hero.rotate(Direction.RIGHT);
}
for(int i = 1; i <= 3; i++){
    hero.move();
}
for(int i = 1; i <= 2; i++){
    hero.push();
}
hero.rotate(Direction.RIGHT);
for(int i = 1; i <= 2; i++){
    hero.move();
}
hero.rotate(Direction.RIGHT);
for(int i = 1; i <= 3; i++){
    hero.move();
}
hero.pickup();
for(int i = 1; i <= 2; i++){
    hero.rotate(Direction.RIGHT);
}
for(int i = 1; i <= 3; i++){
    hero.move();
}
hero.rotate(Direction.LEFT);
for(int i = 1; i <= 2; i++){
    hero.move();
}
hero.rotate(Direction.RIGHT);
for(int i = 1; i <= 6; i++){
    hero.push();
}
hero.rotate(Direction.RIGHT);
for(int i = 1; i <= 2; i++){
    hero.move();
}
hero.interact(Direction.LEFT);
hero.move();
hero.rotate(Direction.LEFT);
for(int i = 1; i <= 5; i++){
    hero.move();
}
hero.rotate(Direction.LEFT);
for(int i = 1; i <= 2; i++){
    hero.move();
}
for(int i = 1; i <= 2; i++){
    hero.rotate(Direction.RIGHT);
}
hero.pickup();
for(int i = 1; i <= 2; i++){
    hero.move();
}
hero.rotate(Direction.RIGHT);
for(int i = 1; i <= 4; i++){
    hero.move();
}
hero.rotate(Direction.RIGHT);
for(int i = 1; i <= 2; i++){
    hero.move();
}
for(int i = 1; i <= 7; i++){
    hero.pull();
}
hero.rotate(Direction.LEFT);
hero.move();
for(int i = 1; i <= 2; i++){
    hero.rotate(Direction.RIGHT);
    hero.move();
}
for(int i = 1; i <= 3; i++){
    hero.push();
}
hero.rotate(Direction.LEFT);
for(int i = 1; i <= 2; i++){
    hero.move();
}
hero.pickup();
for(int i = 1; i <= 2; i++){
    hero.rotate(Direction.RIGHT);
}
for(int i = 1; i <= 2; i++){
    hero.move();
}
hero.rotate(Direction.RIGHT);
for(int i = 1; i <= 4; i++){
    hero.move();
}
hero.rotate(Direction.LEFT);
for(int i = 1; i <= 3; i++){
    hero.shootFireball();
}
for(int i = 1; i <= 4; i++){
    hero.move();
}
```

## Blockly String
```json
{"blocks":{"languageVersion":0,"blocks":[{"type":"start","id":"={9Mof5xE4x:02,pxjC]","x":-290,"y":-194,"deletable":false,"next":{"block":{"type":"rotate","id":"4I:iW?6TkqXP2V]mlE5%","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"TO2-sa]9bCD+Z@sGT=N0"}}},"next":{"block":{"type":"repeat","id":"/VPmf|nrc1Y+MJEQ5~~o","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"%;aV%^g[we_TC3#`nbhS","fields":{"REPEAT_NUMBER":3}}},"DO":{"block":{"type":"move","id":"ES/HPMd3v?thCm4YEG20"}}},"next":{"block":{"type":"push","id":"![#HV,2k{A3;:t*eFhf+","next":{"block":{"type":"rotate","id":".4cfoXehyiOqa,i1}*~W","inputs":{"DIRECTION":{"block":{"type":"direction_left","id":"ZooQ;9X|ica}y`PiS|ys"}}},"next":{"block":{"type":"repeat","id":"f!96Ll`c.o=.5=]C)RY,","inputs":{"TIMES":{"block":{"type":"repeat_number","id":":Ld}-#3(7E*$9x7G,?*x","fields":{"REPEAT_NUMBER":3}}},"DO":{"block":{"type":"repeat","id":"|DHbf?Lr#zDl~Ucphu%h","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"6$%W,QfCv1t7L(:Ho[#E","fields":{"REPEAT_NUMBER":5}}},"DO":{"block":{"type":"move","id":"x=_B.]I=G3%XcF39O0U?"}}},"next":{"block":{"type":"rotate","id":"0x]?tPR}[Nc8f566_Z}T","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"A!/;~bqz2eV+YRNuPWt8"}}}}}}}},"next":{"block":{"type":"repeat","id":"%?0D2[5V$tSL6@K^W`Ie","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"sJc!csSgnT/H`[i7wMX7","fields":{"REPEAT_NUMBER":3}}},"DO":{"block":{"type":"move","id":"tAT0P2KK35M=C.uc)l,A"}}},"next":{"block":{"type":"repeat","id":"TiV9;:6U$SXMLLqbt%z_","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"5|LkM;A5{=-5(UixlZaU","fields":{"REPEAT_NUMBER":2}}},"DO":{"block":{"type":"push","id":";##}X:mYNc$/In+@o1`n"}}},"next":{"block":{"type":"rotate","id":"/J9iYvNuutgwS_:$JGg-","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"BC$jqd!ahXb(s.XD-`8I"}}},"next":{"block":{"type":"repeat","id":"q?:p;Ied1G!`RmRBr,y$","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"OKd#L~SvPotY4072(QE3","fields":{"REPEAT_NUMBER":2}}},"DO":{"block":{"type":"move","id":"+bf;BG@!oupQH[/nv??("}}},"next":{"block":{"type":"rotate","id":"r%*D2Q3*zwD7m]G=M#3]","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"o?`dXr*}^1_%hz87V!A2"}}},"next":{"block":{"type":"repeat","id":"`XpafXaliiq00wQ;XZYn","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"hy}Mdp$u{i[-FWk.Vk?k","fields":{"REPEAT_NUMBER":3}}},"DO":{"block":{"type":"move","id":"E.v0MhnGkr:gCV3G|#vY"}}},"next":{"block":{"type":"pickup","id":"D=7w,$b5D$0DGUBzQ^FV","next":{"block":{"type":"repeat","id":"36x.:_FkLmR)r=S#9C%G","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"@NKf*^^X5sYC/#67l^9]","fields":{"REPEAT_NUMBER":2}}},"DO":{"block":{"type":"rotate","id":"](T*qN{DzE+?CviY,/wN","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"R/|s$1C}sYLuDi^,Un!/"}}}}}},"next":{"block":{"type":"repeat","id":"+,XOCquFR?FoH(Uw*=#u","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"zTZ3Js~nlNZMLQwp5pKh","fields":{"REPEAT_NUMBER":3}}},"DO":{"block":{"type":"move","id":"NmKG@6{qF2uZGjQud,6_"}}},"next":{"block":{"type":"rotate","id":"k+n]HkBP/VCELA3#jTW4","inputs":{"DIRECTION":{"block":{"type":"direction_left","id":"gu#2AQVVWIk3U@D3?%%9"}}},"next":{"block":{"type":"repeat","id":"ot3f7tLe+UT]@v:)*wh2","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"3JX!kJVLd[}mjReRB2zN","fields":{"REPEAT_NUMBER":2}}},"DO":{"block":{"type":"move","id":"8TjUXxv_;IXr3MCC#v0A"}}},"next":{"block":{"type":"rotate","id":"0M1=%vM]Ca$UuycL{~S3","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"xZTXSKR(nwq/I7on0wKH"}}},"next":{"block":{"type":"repeat","id":"49=y%+Do@i}9uK(^|mt:","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"@8CvTepE7E#QvQ!J`r*X","fields":{"REPEAT_NUMBER":6}}},"DO":{"block":{"type":"push","id":"0*znJNc];~etcmSb7F#T"}}},"next":{"block":{"type":"rotate","id":"eraSA8gOul`h9v~wQA5}","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"|2d;|0-hhpkCrYf8HYY~"}}},"next":{"block":{"type":"repeat","id":";CmrDEd4C}f,jHSd)_Sg","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"T}}fGNQh=jyy8eDV$S6=","fields":{"REPEAT_NUMBER":2}}},"DO":{"block":{"type":"move","id":"lPqZl~t0ddaoA^T9j2Rb"}}},"next":{"block":{"type":"use","id":"]BhbPC0hjG9860J|YULH","inputs":{"DIRECTION":{"block":{"type":"direction_left","id":"Mod.edz.i0zl_~ra`FMW"}}},"next":{"block":{"type":"move","id":"8XA0XMuzcK@2EfPCwLMt","next":{"block":{"type":"rotate","id":"g4_HU@npTjEER9hnx`)e","inputs":{"DIRECTION":{"block":{"type":"direction_left","id":"KuF-lJKV0nT:lW_5~)JH"}}},"next":{"block":{"type":"repeat","id":"@M1wgb~XRUE.O/^3:;5z","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"B7-c:JUQ|!4GPPwUp5U[","fields":{"REPEAT_NUMBER":5}}},"DO":{"block":{"type":"move","id":")/n:d6YYngVha/Hl2c(D"}}},"next":{"block":{"type":"rotate","id":"Oj;G0eCXr=%_aofel`Ka","inputs":{"DIRECTION":{"block":{"type":"direction_left","id":"kB@,mSg4ku[%=q!1CAf$"}}},"next":{"block":{"type":"repeat","id":"#,zlOiWCC*Kz]RD)XVu.","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"Q#qUBC|!#+PWx]]9nF/Z","fields":{"REPEAT_NUMBER":2}}},"DO":{"block":{"type":"move","id":"3aU=)^X,4H65^H5ZjL+%"}}},"next":{"block":{"type":"repeat","id":"mrMt2KNHc@eG%F7kM)[E","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"oFMay;u=h);U~SCTtAao","fields":{"REPEAT_NUMBER":2}}},"DO":{"block":{"type":"rotate","id":"Xnrf@P4_PHq2{#^{~ctT","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"ugD70Gi4EVx!kLbbr3qR"}}}}}},"next":{"block":{"type":"pickup","id":"cWx,!+(|9hLY195lW;z)","next":{"block":{"type":"repeat","id":"q6xV3ki,`-s-$^4XG[Ft","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"YxbmDT^(w_LdK$N-]FFv","fields":{"REPEAT_NUMBER":2}}},"DO":{"block":{"type":"move","id":"+c]y$aiuMU/$aO,JjP2Y"}}},"next":{"block":{"type":"rotate","id":"c7iMxy7S.5*j5SaGLSxU","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"xh^i!|l{JRh/kAklht_k"}}},"next":{"block":{"type":"repeat","id":"D==UpJRgHpZaReGN,,Tv","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"B2#^$x*X_=:co``rpt/7","fields":{"REPEAT_NUMBER":4}}},"DO":{"block":{"type":"move","id":"rHVQ%P2-b,ub.K9v0@V`"}}},"next":{"block":{"type":"rotate","id":"*8u^mv~F{iqaJb;t}6]C","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"ujH]DhkCC*[x4/$iC5e,"}}},"next":{"block":{"type":"repeat","id":"K-WcVE1B|k!~`%5-zFIz","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"H8wANrbxCo7b)HM3Ne4s","fields":{"REPEAT_NUMBER":2}}},"DO":{"block":{"type":"move","id":"gry*2=,Eg8aPI(O(nZvs"}}},"next":{"block":{"type":"repeat","id":"ngLJ8#RM|64h49hp{^P,","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"(J]HFwi%LfF[yDUsy,J4","fields":{"REPEAT_NUMBER":7}}},"DO":{"block":{"type":"pull","id":"!Ck7ACW8#v`Gc?v/s@-!"}}},"next":{"block":{"type":"rotate","id":"er.W~)yKNOztStwFN$Rs","inputs":{"DIRECTION":{"block":{"type":"direction_left","id":"CS0*l;xOE98aCQ~:Vr;]"}}},"next":{"block":{"type":"move","id":"p.*gD{@kkOE;n?lK-/}F","next":{"block":{"type":"repeat","id":"ismckAYBiLeP*:VP_RG?","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"NiMbB$gSLnsjH$[h|iG,","fields":{"REPEAT_NUMBER":2}}},"DO":{"block":{"type":"rotate","id":"1cwxfO1#-XVddCn!ZAOO","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"NdO.=ML6zT6ee=;Nd?OF"}}},"next":{"block":{"type":"move","id":"g?$zP@_ONX[Ht%IVHHog"}}}}},"next":{"block":{"type":"repeat","id":"Bk(bT`LsR,d_7dQa4xgB","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"vA0V|MW*Bh^`,XfGELSE","fields":{"REPEAT_NUMBER":3}}},"DO":{"block":{"type":"push","id":",T8QVxzn?rQ3#bIIPih!"}}},"next":{"block":{"type":"rotate","id":"^FP^1.vRY3]1AYPR=_vd","inputs":{"DIRECTION":{"block":{"type":"direction_left","id":",.~=hPj0^t%gD`hpWg9U"}}},"next":{"block":{"type":"repeat","id":"B%!c3d?Do)W6ul/fnZi0","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"_R+cj_I1**-K]83di%^*","fields":{"REPEAT_NUMBER":2}}},"DO":{"block":{"type":"move","id":"HpSKro=A$#cyS5z$|kxX"}}},"next":{"block":{"type":"pickup","id":"A1Ple*jAy,A$SH.M/uMT","next":{"block":{"type":"repeat","id":"=O;WMvhER9ZImt:Iof,$","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"u([2Kzu4NbFC(ALHO@Q^","fields":{"REPEAT_NUMBER":2}}},"DO":{"block":{"type":"rotate","id":"`/x.D]3*}+[/U=.QHkMp","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"ILf1ioRNtRllFLBK^/t1"}}}}}},"next":{"block":{"type":"repeat","id":"SJ#_40`7m(eub@djt?9j","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"3q${r(*1l${41f|U^d?a","fields":{"REPEAT_NUMBER":2}}},"DO":{"block":{"type":"move","id":"Vl6{F0h=X[6a,7IL[pjj"}}},"next":{"block":{"type":"rotate","id":";H.y|P@Y6K{9bQeKC;o?","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"()qR[~,Zl8N8(q0$O{+I"}}},"next":{"block":{"type":"repeat","id":"=:2b7W[D;2TE(@qaR/*J","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"1O%jahAf}aB+9cwjm[I`","fields":{"REPEAT_NUMBER":4}}},"DO":{"block":{"type":"move","id":"GRO@y3KeD!V{fAc}`,VQ"}}},"next":{"block":{"type":"rotate","id":"z9_u:VkCh%%-cGAw,{kC","inputs":{"DIRECTION":{"block":{"type":"direction_left","id":"*sp5_VGxRI.4KmDWDgp1"}}},"next":{"block":{"type":"repeat","id":"oP*d{~9Z(0}i6,:U(XUI","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"yp4^63%6yc7l^B,N`q9A","fields":{"REPEAT_NUMBER":3}}},"DO":{"block":{"type":"fireball","id":"`/Vo+@7EVhBG9}}V^ud#"}}},"next":{"block":{"type":"repeat","id":"iJ[]^cc+O?1Tb%KAifW#","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"1V_%;.TL78X$8Ikp*q|Z","fields":{"REPEAT_NUMBER":4}}},"DO":{"block":{"type":"move","id":"/9Be%-{/{,AX{|qdkr9!"}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}]}}
```
