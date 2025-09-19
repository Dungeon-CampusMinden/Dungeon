---
title: "Blockly Lösung: Level 8"
---

## Blöcke
![solution](./img/loesung_level_008_1.png)
![solution](./img/loesung_level_008_2.png)
![solution](./img/loesung_level_008_3.png)
![solution](./img/loesung_level_008_4.png)

## Code

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
hero.interact(Direction.UP);
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
for(int i = 1; i <= 9; i++){
    hero.move();
}
hero.rotate(Direction.RIGHT);
for(int i = 1; i <= 4; i++){
    hero.move();
    hero.interact(Direction.UP);
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
{"blocks":{"languageVersion":0,"blocks":[{"type":"start","id":"={9Mof5xE4x:02,pxjC]","x":-108,"y":-458,"deletable":false,"next":{"block":{"type":"repeat","id":"i?pxNs!AMJN).xkHOv%,","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"e66W{0gY#s$Y%-9/%VpJ","fields":{"REPEAT_NUMBER":4}}},"DO":{"block":{"type":"move","id":"p%)eIfef$pKzo:8}BY|H"}}},"next":{"block":{"type":"rotate","id":"!{8Z|)?,at1#1JR?fmqp","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"%qW#9-u,GljC1}hDepHR"}}},"next":{"block":{"type":"repeat","id":"MB4|OLdqt,]}3;]~a_HP","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"U,17Wd!Y2tm=b+FhQYV$","fields":{"REPEAT_NUMBER":5}}},"DO":{"block":{"type":"move","id":"n4O2js]WANFjEU.88lKX"}}},"next":{"block":{"type":"rotate","id":"5F*CV?j?R6U?ZYUumb}/","inputs":{"DIRECTION":{"block":{"type":"direction_left","id":"F*RH!)VBHp?jF*D;(#!w"}}},"next":{"block":{"type":"repeat","id":"4?=;hijg@uxB5!rM4]n?","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"VPFr;@^ErkH.bIUG!DXY","fields":{"REPEAT_NUMBER":7}}},"DO":{"block":{"type":"move","id":"Hrm-!vW!mQLv(1qS#+-G"}}},"next":{"block":{"type":"rotate","id":"jRHYvswDc^#+~v=u?7Bc","inputs":{"DIRECTION":{"block":{"type":"direction_left","id":"fma]T.1/Nwk,=y2SA=ym"}}},"next":{"block":{"type":"repeat","id":"tYYCKVRv=zm@sF{lBnQ`","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"sINP:[ivNh|G^j7/X@UC","fields":{"REPEAT_NUMBER":3}}},"DO":{"block":{"type":"move","id":"-im+}JoK,%eJ;0CY[*tT"}}},"next":{"block":{"type":"rotate","id":"_kV(bp,$U!WsgNf|aShk","inputs":{"DIRECTION":{"block":{"type":"direction_left","id":"hdzlUcZ|w_sf39=q[8Rk"}}},"next":{"block":{"type":"move","id":"l97;^zNHRS=sU!H(.vc2","next":{"block":{"type":"repeat","id":"M@E`es_pV0/ce?Xc|#`6","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"ayjR:j@]_v3[`G|u7ZdB","fields":{"REPEAT_NUMBER":2}}},"DO":{"block":{"type":"push","id":"(ZqM9GUpKNyfJ[HxJ9xu"}}},"next":{"block":{"type":"repeat","id":"Tjn(p_;e{?ey6@Xz6kQO","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"Mx2|~EQ^~!u3TK5y?WA[","fields":{"REPEAT_NUMBER":2}}},"DO":{"block":{"type":"rotate","id":"(ar?,*YU]yWjiidn[{Bv","inputs":{"DIRECTION":{"block":{"type":"direction_left","id":"ZW%a24.G(5NIC5qg(O82"}}}}}},"next":{"block":{"type":"repeat","id":"X?^mlO8feeW2-=p~|oo!","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"g!A+PJ,@*wqN}dY$/E{e","fields":{"REPEAT_NUMBER":3}}},"DO":{"block":{"type":"move","id":"aeaEnN$gXDE$2OZX}p(l"}}},"next":{"block":{"type":"rotate","id":"fukLG{fL2rK)#}PUt):r","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"*|lj1*O[9DZ[bU=fb7F;"}}},"next":{"block":{"type":"repeat","id":"eyQ5iOs4U]@SNSNWk[Zf","inputs":{"TIMES":{"block":{"type":"repeat_number","id":":9:tjMNi(7JS]ODjE4!Y","fields":{"REPEAT_NUMBER":3}}},"DO":{"block":{"type":"move","id":"E$BooNSZ{[l%Lgz[mD?V"}}},"next":{"block":{"type":"rotate","id":"jlwQ8w}5(Iidyu;6lf+L","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"l3bCgt~Rc*i-+V46f`%-"}}},"next":{"block":{"type":"repeat","id":"AQs0gd(hgr@[q(hOoukO","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"~L3rq3gu{u_![InM3%IY","fields":{"REPEAT_NUMBER":7}}},"DO":{"block":{"type":"move","id":"34!=efI8):W,33+DCz:T"}}},"next":{"block":{"type":"rotate","id":"TZbeT}s_wcKXMX[*HxLD","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"Lz_7Zq0QC+DeH3YR.U^D"}}},"next":{"block":{"type":"repeat","id":"nMyp?;Yrka:R:`UdaI.p","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"ug5dyrVpAPw6.xxVYEmn","fields":{"REPEAT_NUMBER":5}}},"DO":{"block":{"type":"move","id":"7bC=+)[3S7UQ$XJ859HN"}}},"next":{"block":{"type":"rotate","id":"q~%12bx-Di+crW80WOiA","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"CEtO%A@]=U3nJ:c@;pn?"}}},"next":{"block":{"type":"repeat","id":"k3sde#()E+l|SQ~Xi4X4","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"Po[~wiSX7jI6Sll$*hrU","fields":{"REPEAT_NUMBER":3}}},"DO":{"block":{"type":"move","id":"+(a6B#A$l.9oo=3i[~()"}}},"next":{"block":{"type":"rotate","id":"=,MsZ+oAXGtLjlVL`E6e","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"R4zDW[^X%[xDn14.:RUZ"}}},"next":{"block":{"type":"move","id":"j|%zXMqfwKBWrfiq#v_A","next":{"block":{"type":"push","id":"eMSn15kO3:+Y?2c!%-:$","next":{"block":{"type":"repeat","id":"7BZ6r!DEcSqZ)ZIq2cMD","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"-r5c6f6:3S,8%dV8zgU6","fields":{"REPEAT_NUMBER":2}}},"DO":{"block":{"type":"rotate","id":")4J:A`Fvn(*Kqg^n%F!;","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"5#}6y3!a2KW/@Ecj2t8X"}}}}}},"next":{"block":{"type":"move","id":"dKRNP?we$C7vRQF8[P`M","next":{"block":{"type":"repeat","id":"1!TH[mtnZZgi/s+o%mQx","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"pGrJ?8qqkd/|-*W2kWN(","fields":{"REPEAT_NUMBER":4}}},"DO":{"block":{"type":"move","id":"-Hn5BKqZVyq0|nA(0np/"}}},"next":{"block":{"type":"rotate","id":"gtz/Q]{.+`^Y?Q}aMwVe","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"}*zju{m`*JQBc={s+Asc"}}},"next":{"block":{"type":"repeat","id":"_ebOO.3p8h!6EguJQ5Qs","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"ZT[cfKk;L#dLdyQlDddh","fields":{"REPEAT_NUMBER":6}}},"DO":{"block":{"type":"move","id":"GZYno.Qp6YH9psAR/Q~C"}}},"next":{"block":{"type":"use","id":"Ep,BVS7HHOCJ=WyMbcW,","inputs":{"DIRECTION":{"block":{"type":"direction_up","id":"|3Wwys9[|5m^,g|r2.rh"}}},"next":{"block":{"type":"repeat","id":"u6.@fKAH4TDQ7Joqv(zN","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"`=[FUrX]X(``grL~=2{W","fields":{"REPEAT_NUMBER":2}}},"DO":{"block":{"type":"rotate","id":"7nW;~f*uReO-kz7~.Zkx","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"XO{W%;JGf27?^.{VB/#["}}}}}},"next":{"block":{"type":"repeat","id":"A{Q11_[Boef%]?cx?s*a","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"Tho]MZ{TvVjX681MFvyb","fields":{"REPEAT_NUMBER":6}}},"DO":{"block":{"type":"move","id":"P?moG%vO00U/_h~=?Nj,"}}},"next":{"block":{"type":"rotate","id":"u%}(2EF11U}0hYFrL!U*","inputs":{"DIRECTION":{"block":{"type":"direction_left","id":")j]XzTbi?|Ym4}s/IC=b"}}},"next":{"block":{"type":"repeat","id":"GtO^d#-fZB4;4|1w~g)3","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"^TR@3BxGZ596av{d_lK*","fields":{"REPEAT_NUMBER":5}}},"DO":{"block":{"type":"move","id":"8lE2f]g4h/9}V!0cY]rY"}}},"next":{"block":{"type":"pull","id":"Pe4zTaH$/]X`oPkP}cuR","next":{"block":{"type":"repeat","id":"(-wNG4+8BdJog+egCiGc","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"h}a8+$Q%O|U$CxosX[j_","fields":{"REPEAT_NUMBER":2}}},"DO":{"block":{"type":"rotate","id":"n@{ztfGG~|4,]w;*K#Sz","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"JMSGWPDq)xvTs*:X8Efv"}}}}}},"next":{"block":{"type":"move","id":"%,)%)B5+fuEK38S7F(Eu","next":{"block":{"type":"rotate","id":"S*#iajrpO{geh=,eS3pM","inputs":{"DIRECTION":{"block":{"type":"direction_left","id":"Jb_KmXz3P*kft[)kWe:m"}}},"next":{"block":{"type":"repeat","id":"o`zH1^KEWXwj:i`OC9ae","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"2P(a%@a+MfE`Uios,K`w","fields":{"REPEAT_NUMBER":3}}},"DO":{"block":{"type":"move","id":"!vpOiM^X(IuT6h]HjT~8"}}},"next":{"block":{"type":"rotate","id":"JUq7A{I`kf7_U_Mx0~dO","inputs":{"DIRECTION":{"block":{"type":"direction_left","id":"WHvJ[=v?oXUs4(Q;bx%`"}}},"next":{"block":{"type":"repeat","id":"~zT*%i|qUmG:{uDT:vEp","inputs":{"TIMES":{"block":{"type":"repeat_number","id":":H+KMoY{1-oHQx:d!am_","fields":{"REPEAT_NUMBER":5}}},"DO":{"block":{"type":"move","id":"GD]UtG#oxB7Y*+|.cUJf"}}},"next":{"block":{"type":"rotate","id":"EL`{H:6?TMZ#y~/`|e6d","inputs":{"DIRECTION":{"block":{"type":"direction_left","id":"n;QF}(xj@|%zsSw2@jjX"}}},"next":{"block":{"type":"repeat","id":"a$`bA:vH%O(7z:);7hDn","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"R5*q,}O/vb0OUtPu6hWZ","fields":{"REPEAT_NUMBER":5}}},"DO":{"block":{"type":"move","id":"6tO0F^c%$:k6Vfi,7Ka$"}}},"next":{"block":{"type":"rotate","id":"L_RiP0Z/c5iHxvC2nVE?","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"RK?2OT:L1}Q!b}OH5H@x"}}},"next":{"block":{"type":"repeat","id":"5!f,3oeSEnd`KU$D~*u`","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"[*c!sB5a{O[.0x7;oafx","fields":{"REPEAT_NUMBER":9}}},"DO":{"block":{"type":"move","id":"(yjB0q+JT3qj?b4Sfi^W"}}},"next":{"block":{"type":"rotate","id":"M5jKnsH[BC@wOdOMtAcN","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"}j?]t6w7SpA;dD0i_=#G"}}},"next":{"block":{"type":"repeat","id":"u?hdYN-_sKlJGL[og?VB","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"~b4Q`4Ny.,?$@U-CyOhS","fields":{"REPEAT_NUMBER":4}}},"DO":{"block":{"type":"move","id":"1KPuDoQ;{nUDf`rrS]2H","next":{"block":{"type":"use","id":"/nzgr3{yi4.wzLWYBijg","inputs":{"DIRECTION":{"block":{"type":"direction_up","id":"//;rfNNO3Lc98tCU{IH_"}}}}}}}},"next":{"block":{"type":"repeat","id":"svtBEq|q)kq|vQ]T.o#l","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"s)1EDlkP,];JRBW~9a7Y","fields":{"REPEAT_NUMBER":2}}},"DO":{"block":{"type":"rotate","id":"Z{|;AJ(f8}Hvlbo8(#90","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"@(1dSDwT`dF}Zui2fx5k"}}}}}},"next":{"block":{"type":"repeat","id":",}Y;z]iLRZCT6dcBc:i$","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"yXow`o3}[T[9KnAIR_Bz","fields":{"REPEAT_NUMBER":3}}},"DO":{"block":{"type":"move","id":"LHHmdvCMo:!)o(.VO(3_"}}},"next":{"block":{"type":"rotate","id":"//b.!5Vr]njqLMEJCq)H","inputs":{"DIRECTION":{"block":{"type":"direction_left","id":"SoMdm2ZjBQQpcVRGTr2`"}}},"next":{"block":{"type":"repeat","id":"ef7,8Hq24FRe|n^Pa0Ye","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"i$;I(7P9y{KhAVpw__*:","fields":{"REPEAT_NUMBER":4}}},"DO":{"block":{"type":"move","id":"Q9icNf,/E}u*z;Lx_hH-"}}},"next":{"block":{"type":"rotate","id":"G##Id1dO7dp^=d1u6EaN","inputs":{"DIRECTION":{"block":{"type":"direction_left","id":"FSFBu3;x!F46w;?8eVV$"}}},"next":{"block":{"type":"repeat","id":"sBkt:]W(/M]Ih)b_OWbd","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"70FFkCn7)T$DFAIaf:x7","fields":{"REPEAT_NUMBER":3}}},"DO":{"block":{"type":"move","id":"OZ+Z?:a4BPYzjR`)2-1s"}}},"next":{"block":{"type":"repeat","id":"6#lDW%zMG8y183e|*z6_","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"M3;9wA@9G#8m1$lZcQu0","fields":{"REPEAT_NUMBER":5}}},"DO":{"block":{"type":"pull","id":"=nX,[%Sx+ej($:KAhOUq"}}},"next":{"block":{"type":"rotate","id":"ZTrW*o%IXUNR$]L:rSqm","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"{~U$b!e{#a3:LURFkBQ8"}}},"next":{"block":{"type":"repeat","id":"xBw)]w`|-W?gWJrg]UlG","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"_jw7{8*)#f:fuSa`rvW[","fields":{"REPEAT_NUMBER":2}}},"DO":{"block":{"type":"move","id":"avtDCFxc@#MJ_%oU8fv^","next":{"block":{"type":"rotate","id":"uY.cywEu{=3M.QS}cKdA","inputs":{"DIRECTION":{"block":{"type":"direction_left","id":"kDXhHlfzunDf!|qx?,)h"}}}}}}}},"next":{"block":{"type":"push","id":":m;QM$D.mlnRx5LWi^8K","next":{"block":{"type":"rotate","id":"5~wQAG@V5!?,Th|)h5~x","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"k%5C1sJ;%~L|.^v]7Rk*"}}},"next":{"block":{"type":"repeat","id":"xU{ZVp`o1e.R82Dm7YLM","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"-e|.]wBd0kjaF:9HjuCE","fields":{"REPEAT_NUMBER":2}}},"DO":{"block":{"type":"move","id":"]Xj=6b0tQr{455(F5JS*","next":{"block":{"type":"rotate","id":"#8;^E7AdMjHojc:Rz+]w","inputs":{"DIRECTION":{"block":{"type":"direction_left","id":"?%0Dn/54%4$w#HAjT(OA"}}}}}}}},"next":{"block":{"type":"repeat","id":"6{4N+p=8.1Z.R!DmhE4d","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"/Myw9_%*Usb[]^HhF]_!","fields":{"REPEAT_NUMBER":2}}},"DO":{"block":{"type":"push","id":"1dM6.`@=k`uo?X`$DbOz"}}},"next":{"block":{"type":"rotate","id":"},d+8$Lrz9^wVjtkwQ~t","inputs":{"DIRECTION":{"block":{"type":"direction_left","id":"ii;J8W/=7pody^h:E[Bl"}}},"next":{"block":{"type":"move","id":"*kJl,?%aNsO!C:BBOr^`","next":{"block":{"type":"rotate","id":"@22[I-VMfz,mnCAcyuwr","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"dkg1K@O`|HuAlmLT/oMM"}}},"next":{"block":{"type":"move","id":"h[SDQO%_+*PLgtaZP}01","next":{"block":{"type":"repeat","id":"@%/xR4B#p`CD`5Nvp?%i","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"Vy1nun4MLf5M}cUcx#fP","fields":{"REPEAT_NUMBER":2}}},"DO":{"block":{"type":"move","id":"QN|-4%T5K)Gy@F_sW}:9","next":{"block":{"type":"rotate","id":"3#QsS7fmg%h$9rQl/bZm","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"a@~r#[S%bGIsf,s};SKZ"}}}}}}}},"next":{"block":{"type":"repeat","id":"Eh#ywW-U7x(1M=EKF~C+","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"=I~{5]%LxPM-WG^YaIAW","fields":{"REPEAT_NUMBER":6}}},"DO":{"block":{"type":"pull","id":"ZO(Tnn/B#u!/tl#+!Jp^"}}},"next":{"block":{"type":"rotate","id":"]8l.s:$I@?0;$%H3f5)s","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"o{p?^.%00^-HyQPCun-v"}}},"next":{"block":{"type":"repeat","id":"Ql]%B#-p_~EDVhTT%6eY","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"=46`-?Fc)Uqg`G{EteUi","fields":{"REPEAT_NUMBER":2}}},"DO":{"block":{"type":"move","id":"4S-xi@MML8XA.]|+!bj~","next":{"block":{"type":"rotate","id":"dIr_2Dn-ctELD6!No35y","inputs":{"DIRECTION":{"block":{"type":"direction_left","id":"vX6}8F0bcVfRZ^%,=R}|"}}}}}}}},"next":{"block":{"type":"push","id":"z*.F3IAkxAm`[~N;b+;[","next":{"block":{"type":"rotate","id":"@OtFvcU|R_;IDbdTydFB","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"4{xF*QI!G$`Xtd,$|D*["}}},"next":{"block":{"type":"move","id":"rRNHBX_c-@UF(P$0at_-","next":{"block":{"type":"rotate","id":":2*K={92*K}Wigfu7.xA","inputs":{"DIRECTION":{"block":{"type":"direction_left","id":"n#?2@%GjACcITL#.Uc/B"}}},"next":{"block":{"type":"repeat","id":"OCPnILfo/ISI?2w50,5k","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"j#xnnoram0_AWMQL9:r*","fields":{"REPEAT_NUMBER":5}}},"DO":{"block":{"type":"move","id":"[PizkoHv:T7,~e2M..`;"}}},"next":{"block":{"type":"rotate","id":"=qZntK{;=7LV%|CKlx^?","inputs":{"DIRECTION":{"block":{"type":"direction_left","id":"2ja7Se5r9aj@T:Pzt:{8"}}},"next":{"block":{"type":"move","id":"^~?2xij0teSPWTJ$^iy8","next":{"block":{"type":"rotate","id":"_Qp,#py[Pwil{JvO`UQ`","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"d,1D?6(TgS)IK$P?KDv`"}}},"next":{"block":{"type":"move","id":"4vOMzW#)LZAq`hVA/MQD"}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}]}}
```
