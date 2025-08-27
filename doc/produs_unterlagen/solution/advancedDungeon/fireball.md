---
title: "AdvancedDungeon: Lösung Feuerball"
---


### Solution for `MyFireball`

```java
public Fireball(FireballSkill fireballSkill) {
    super(fireballSkill);

    // Overpower values for fireball
    fireballSkill.setCoolDown(1);
    fireballSkill.setRange(9999);
    fireballSkill.setSpeed(100);
    fireballSkill.setDamage(9999);
    fireballSkill.setTexture(FireballSkill.DEBUG_TEXTURE); // 'Easter egg' texture
}

public void onBerryHit(Berry berry) {
    // Mark toxic berries with red color and safe berries with green color
    if (berry.isToxic()) berry.tintColor(0xFF0000FF);
    else berry.tintColor(0x00FF00FF);

    // Safe berries are now donuts
    if (!berry.isToxic()) berry.changeTexture(Berry.DONUT_TEXTURE);

    // Destroy the berry if toxic
    if (berry.isToxic()) berry.destroy();
}

```

