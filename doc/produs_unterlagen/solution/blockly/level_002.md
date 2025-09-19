---
title: "Blockly Lösung: Level 2"
---

## Blöcke
![solution](./img/loesung_level_002.png)

## Code

```java
hero.rotate(Direction.LEFT);
for(int i = 1; i < 5; i++){
    hero.move();
}
hero.rotate(Direction.RIGHT);
for(int i = 1; i < 6; i++){
    hero.move();
}
hero.rotate(Direction.RIGHT);
for(int i = 1; i < 2; i++){
    hero.move();
}
hero.rotate(Direction.LEFT);
for(int i = 1; i < 4; i++){
    hero.move();
}
hero.rotate(Direction.RIGHT);
for(int i = 1; i < 4; i++){
    hero.move();
}
hero.rotate(Direction.LEFT);
for(int i = 1; i < 2; i++){
    hero.move();
}
```

## Blockly String
```json
{"blocks":{"languageVersion":0,"blocks":[{"type":"start","id":"={9Mof5xE4x:02,pxjC]","x":-24,"y":-248,"deletable":false,"next":{"block":{"type":"rotate","id":"[@Q)!:Gn2UZ~`#H)UxDJ","inputs":{"DIRECTION":{"block":{"type":"direction_left","id":"ff$ZLci[jDoUq`Q}v,*q"}}},"next":{"block":{"type":"move","id":"JEr:8az.*Qd`!Jz:jZjq","next":{"block":{"type":"move","id":"XSc4wNfpyw%b0UUneU8_","next":{"block":{"type":"move","id":"SyPwXZ!Ihq,8_/RbW+HQ","next":{"block":{"type":"move","id":"SGd%p-d/?dkx9#,|7(g]","next":{"block":{"type":"rotate","id":"Y{nb;=U+NhBf^PM)TNhV","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"xceO`%vygCyvjuSyC-QD"}}},"next":{"block":{"type":"move","id":"|E*Q(#_L[FsVRQF@ypZP","next":{"block":{"type":"move","id":"!x,ACBg$v@tIfY27vC,j","next":{"block":{"type":"move","id":".C(!m6Wp{UJ$j27/%Vi6","next":{"block":{"type":"move","id":"u4Pxw(%kGQo#c38o4jC{","next":{"block":{"type":"move","id":"!@{.KSLswHy%lpQ8IA+t","next":{"block":{"type":"rotate","id":"iM%V~zZx@rok-Z,yBCt[","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"JvTdu1A[ZXlZBuySewud"}}},"next":{"block":{"type":"move","id":"1Wh2kb}wH[#{y_-_{?`k","next":{"block":{"type":"rotate","id":"}e7c}Qko)U*/HYRX*;d8","inputs":{"DIRECTION":{"block":{"type":"direction_left","id":"4,fecDOL1_u+@wwtyrVN"}}},"next":{"block":{"type":"move","id":"@BtjyUwC9=-mv*dIVk+.","next":{"block":{"type":"move","id":"5a|X2O}Od5{rdLvRAsVx","next":{"block":{"type":"move","id":"7Ca7-M^8-~1aZK7,[gK1","next":{"block":{"type":"rotate","id":"`)vq;/YzOlleO6S7yUa:","inputs":{"DIRECTION":{"block":{"type":"direction_right","id":"q*X$u[01$DQY,P}F2G4J"}}},"next":{"block":{"type":"move","id":"DzPKw/^L[j-IuAX[[8N/","next":{"block":{"type":"move","id":"l,J~]1P{!b;^8i=b!T6%","next":{"block":{"type":"move","id":"7iZ;Zn*UswEEQl_j!@K#","next":{"block":{"type":"rotate","id":"MO2!SzWCl1YAZB#5vD)p","inputs":{"DIRECTION":{"block":{"type":"direction_left","id":":2fC348J])+]/Dm!rsw-"}}},"next":{"block":{"type":"move","id":"#j{h8a/MZ7{5aV_4S-@v"}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}]}}
```
