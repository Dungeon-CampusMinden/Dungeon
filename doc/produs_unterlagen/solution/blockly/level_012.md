---
title: "Blockly Lösung: Level 12"
---

## Blöcke
![solution](./img/loesung_level_012.png)

## Code

```java
hero.rotate(Direction.LEFT);
hero.move();
hero.rotate(Direction.RIGHT);
for(int i = 1; i <= 5; i++){
    if(hero.active(Direction.LEFT)){
        for(int x = 1; x <= 2; x++){
            hero.move();
        }
    }else{
        hero.interact(Direction.LEFT);
        for(int x = 1; x <= 2; x++){
            hero.move();
        }
    }
}
hero.rotate(Direction.RIGHT);
for(int i = 1; i <= 2; i++){
    hero.move();
}
hero.rotate(Direction.RIGHT);
for(int i = 1; i <= 5; i++){
    if(hero.active(Direction.LEFT)){
        for(int x = 1; x <= 2; x++){
            hero.move();
        }
    }else{
        hero.interact(Direction.LEFT);
        for(int x = 1; x <= 2; x++){
            hero.move();
        }
    }
}
hero.rotate(Direction.RIGHT);
hero.move();
hero.rotate(Direction.RIGHT);
for(int i = 1; i <= 11; i++){
    hero.move();
}
```

## Blockly String
```json
{"blocks":{"languageVersion":0,"blocks":[{"type":"start","id":"={9Mof5xE4x:02,pxjC]","x":-8,"y":-649,"deletable":false,"next":{"block":{"type":"rotate","id":"%Ma1!!144U24+,%Q`ZHk","inputs":{"DIRECTION":{"block":{"type":"direction_left","id":"wkQJP3cPyLVS{fFWC;`v"}}},"next":{"block":{"type":"move","id":"#!+Yg[_}f6y)d?4~Pt:/","next":{"block":{"type":"rotate","id":"K{T!Nhi}uD*.e33+F*,I","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"|v_qkKfN5Iv$1N]G8uKw"}}},"next":{"block":{"type":"repeat","id":"zUx2r]nmasC6RR5FNOw)","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"HN!c*EBKg3+RVbLmC+N^","fields":{"REPEAT_NUMBER":4}}},"DO":{"block":{"type":"move","id":"v4Iiqih+?fu5j[zR)84=","next":{"block":{"type":"move","id":"a+.gz;j=W0={/w0tkPD0","next":{"block":{"type":"controls_if","id":"mr;)!]L)d-Hv2*4LY:]H","inputs":{"IF0":{"block":{"type":"not_condition","id":"}*{^bnO,pD!aeBr#X^~H","inputs":{"INPUT_A":{"block":{"type":"logic_active_direction","id":"%L#/cD%(=uEW4Zx!*`+@","inputs":{"DIRECTION":{"block":{"type":"direction_left","id":"}4iH03:lb=RM+kqqV4Uf"}}}}}}}},"DO0":{"block":{"type":"use","id":"QM|GK.w;0B8PP6}[okeF","inputs":{"DIRECTION":{"block":{"type":"direction_left","id":";6oC969h[qr42(D5S@75"}}}}}}}}}}}}},"next":{"block":{"type":"rotate","id":"uCAaGmuXCwrac}j:/9kB","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"$jA^*:3I@Fv9vVmvs0J`"}}},"next":{"block":{"type":"move","id":"uTd~]@pR6AZzfD{4e,F_","next":{"block":{"type":"move","id":"=A#r}@#[M$=~E]+;GM?N","next":{"block":{"type":"rotate","id":":BT}QEY!KqE(o%oZW]/r","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"2VW@0w9fPwZ*i@vTqoG)"}}},"next":{"block":{"type":"repeat","id":"B?Ln?:(3_%#:=0x=(|Br","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"vD.580yD(~Z/tJkQ*]r9","fields":{"REPEAT_NUMBER":4}}},"DO":{"block":{"type":"controls_if","id":"p4@4h=%taME{~G*Y-b.s","inputs":{"IF0":{"block":{"type":"not_condition","id":"ju#K^OpdQ:?7R_@hOw/b","inputs":{"INPUT_A":{"block":{"type":"logic_active_direction","id":"VX9Z4CHA{11-Wfi9XftK","inputs":{"DIRECTION":{"block":{"type":"direction_left","id":"P/?oJ.Q[7k7-KRnU.6ct"}}}}}}}},"DO0":{"block":{"type":"use","id":"Zqg*b3oMpU]v(JaU;T4h","inputs":{"DIRECTION":{"block":{"type":"direction_left","id":"6x}e]%/ahml%vn~[DAWR"}}}}}},"next":{"block":{"type":"move","id":"#=4sqm`Fwlfz}mO%kC/D","next":{"block":{"type":"move","id":"n%fi4|4wmz4ttk%ETK,K"}}}}}}},"next":{"block":{"type":"rotate","id":"KI0~?N#Ql$2y7k4Y8s.D","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"nPPK7PvOq,d[3ops#3QS"}}},"next":{"block":{"type":"move","id":"#s11iG2ny{MvKp2V0bc+","next":{"block":{"type":"rotate","id":"F}y,**j8y[m=z+dkpy5*","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"}WXj5QY|}(K^/+WRfK5~"}}},"next":{"block":{"type":"repeat","id":"B4vXvmELq#?x3w^q*RJ!","inputs":{"TIMES":{"block":{"type":"repeat_number","id":".!LN?)/xe{U.,:[+r`Jy","fields":{"REPEAT_NUMBER":11}}},"DO":{"block":{"type":"move","id":"GAaFhuJ|[##;vdLoHfB`"}}}}}}}}}}}}}}}}}}}}}}}}}}}}}},{"type":"repeat","id":"iSvQQYD/G`^aNduu{`oy","x":-594,"y":-393,"disabledReasons":["ORPHANED_BLOCK"],"inputs":{"TIMES":{"block":{"type":"repeat_number","id":"t?Ya[A8:fi0.hzyeHMir","fields":{"REPEAT_NUMBER":3}}},"DO":{"block":{"type":"repeat","id":"_NU[a4X*9qP^wsjG!Y;e","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"W+y?QB:69A=oWy56aDUK","fields":{"REPEAT_NUMBER":4}}},"DO":{"block":{"type":"controls_if","id":"9E.Cd*`|aTx5w:n4VW`C","inputs":{"IF0":{"block":{"type":"not_condition","id":"sH/B%O5:M}4I;]$^+I;{","inputs":{"INPUT_A":{"block":{"type":"logic_active_direction","id":"vBCh+k6hdcR+5]MuQY9F","inputs":{"DIRECTION":{"block":{"type":"direction_left","id":"T`qy3s|O}}8_Hhj]?/tn"}}}}}}}},"DO0":{"block":{"type":"use","id":"4oF6@xXt2AE1rq#7N;#G","inputs":{"DIRECTION":{"block":{"type":"direction_left","id":"eb_XdL1*c_())K?RC^sR"}}}}}},"next":{"block":{"type":"move","id":"Jo]u(kx~F{S+I62WDJ:m","next":{"block":{"type":"move","id":",B#7wB|9dR$UcHkj7$C4"}}}}}}},"next":{"block":{"type":"rotate","id":"NfKf~i=i:*UF|PdC32ti","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"HzC=(K~)K6SQ0]f*c|)F"}}}}}}}},"next":{"block":{"type":"move","id":"z`:hJ!#I;U--9]l9/4Jm","disabledReasons":["ORPHANED_BLOCK"],"next":{"block":{"type":"rotate","id":"0~vB*x4^SG@2U8y#o`1g","disabledReasons":["ORPHANED_BLOCK"],"inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"Uc4;*|t5v[}R}_|aN6,M"}}},"next":{"block":{"type":"repeat","id":"[#]{/VAQnIUy+bik8-=t","disabledReasons":["ORPHANED_BLOCK"],"inputs":{"TIMES":{"block":{"type":"repeat_number","id":"bdT+|:2]D={d.E4(qI%,","fields":{"REPEAT_NUMBER":11}}},"DO":{"block":{"type":"move","id":"7#fBY4%t8EA_D6rsU|+)"}}}}}}}}}},{"type":"repeat","id":"TTHL63{|vf=QVWVm$7{c","x":-565,"y":55,"disabledReasons":["ORPHANED_BLOCK"],"inputs":{"TIMES":{"block":{"type":"repeat_number","id":"Lu!BX?Un4E8jFSj);Yr8","fields":{"REPEAT_NUMBER":3}}},"DO":{"block":{"type":"controls_ifelse","id":"|ztaoa!93j|*moWa}/]y","inputs":{"IF0":{"block":{"type":"logic_switch_direction","id":"F2a.=vxO:X!u%Yg1G~;)","inputs":{"DIRECTION":{"block":{"type":"direction_left","id":"(7;Bbb~$M3.qwP}m1!UJ"}}}}},"DO0":{"block":{"type":"repeat","id":"[;PYgv,6~42nrd.m6d(]","inputs":{"TIMES":{"block":{"type":"repeat_number","id":"I0~s~Ird~B4W-u_XcaSa","fields":{"REPEAT_NUMBER":4}}},"DO":{"block":{"type":"controls_if","id":"P:0?qS2YW7MhQr:emG}m","inputs":{"IF0":{"block":{"type":"not_condition","id":"*nxQ8sl957TF)1``IhZ?","inputs":{"INPUT_A":{"block":{"type":"logic_active_direction","id":"1CNO.qKiLu1q%CY.?H+R","inputs":{"DIRECTION":{"block":{"type":"direction_left","id":"CQruiJ-:BU-pl:la:dd5"}}}}}}}},"DO0":{"block":{"type":"use","id":"X|jBT_lWeAF9W;HF+7`_","inputs":{"DIRECTION":{"block":{"type":"direction_left","id":"?};2b]Cr}lAp|nvAu8Zn"}}}}}},"next":{"block":{"type":"move","id":"^yKg:d6QQPxbJ3Xez/,`","next":{"block":{"type":"move","id":"}vn.Zak{6!BccZUXw(Z]"}}}}}}},"next":{"block":{"type":"rotate","id":"Vc+D)}m]fc{a/Me5FE80","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"~75eth3~5Krj2L*61pb0"}}}}}}},"ELSE":{"block":{"type":"move","id":"r1def|m(@ReWxw-}7%iD","next":{"block":{"type":"move","id":"EC?xTtBXVv9{~rW`O}~6","next":{"block":{"type":"rotate","id":"h4%2:jMSCg/9cV2UQ{K^","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"kg.|52V|lg2zseEsR4nF"}}}}}}}}}}}}},"next":{"block":{"type":"move","id":"3+#Ju7hkFLqP{u8E=06K","disabledReasons":["ORPHANED_BLOCK"],"next":{"block":{"type":"rotate","id":"TK[~Lpx]d#GXxK)Y!Qh{","disabledReasons":["ORPHANED_BLOCK"],"inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"1R3vG#q[apJ96ML|q;|q"}}},"next":{"block":{"type":"repeat","id":"d1JxG`2ksmBgh0RLuNnm","disabledReasons":["ORPHANED_BLOCK"],"inputs":{"TIMES":{"block":{"type":"repeat_number","id":"$X,Yc_K^MV7QZX`f2,tT","fields":{"REPEAT_NUMBER":11}}},"DO":{"block":{"type":"move","id":"2)*.0#gO+6$4%w^3d.37"}}}}}}}}}},{"type":"move","id":"[IXhL(zv;xqIRnH4Ob_T","x":-229,"y":-556,"disabledReasons":["ORPHANED_BLOCK"],"next":{"block":{"type":"move","id":";FPO,zc@a=nMdU63tc+2","disabledReasons":["ORPHANED_BLOCK"]}}}]}}
```
