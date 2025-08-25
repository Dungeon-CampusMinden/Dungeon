---
title: "AdvancedDungeon: Lösung Array Level"
---

### Solution for `MyArrayCreator`
```java
public int[] countMonstersInRooms() {
     int[] monsterArray = new int[5];
     monsterArray[0] = 1;
     monsterArray[1] = 5;
     monsterArray[2] = 4;
     monsterArray[3] = 3;
     monsterArray[4] = 2;
    return monsterArray;
}
```

### Solution for `MyArrayRemover`
```java
public int[] removePosition() {
    int[] newArray = new int[3];
    newArray[0] = 2;
    newArray[1] = 5;
    newArray[2] = 3;
    return newArray;
}
```

### Solution for `MyArraySummarizer`
```java
public int summarizeArray() {
    int sum = 0;
    for (int i = 0; i < monsterArray.length; i++) {
        if (monsterArray[i] != 0) {
            sum += monsterArray[i];
        }
    }
    return sum;
}
```

### Solution for `MyMonsterSort`
```java
public Monster[] sortMonsters(Monster[] monsterArray) {
    for (int i = 0; i < monsterArray.length - 1; i++) {
        for (int j = 0; j < monsterArray.length - i - 1; j++) {
            if (monsterArray[j].getHealthPoints() > monsterArray[j + 1].getHealthPoints()) {
                // Swap positions in the game world
                monsterArray[j].swapPosition(monsterArray[j + 1]);

                // Swap references in the array
                Monster temp = monsterArray[j];
                monsterArray[j] = monsterArray[j + 1];
                monsterArray[j + 1] = temp;
            }
        }
    }
    return monsterArray;
}
```

