ArrayCreateLevel
```java
@Override

public int[] countMonstersInRooms() {
     int[] monsterArray = new int[5];
     monsterArray[0] = 1;
     monsterArray[1] = 5;
     monsterArray[2] = 3;
     monsterArray[3] = 2;
     monsterArray[4] = 4;

    return monsterArray;
}

```
ArrayRemoveLevel
```java
@Override
public int[] entfernePositionen() {

    int[] neuesArray = new int[3];

    neuesArray[0] = 2;
    neuesArray[1] = 5;
    neuesArray[2] = 3;

    return neuesArray;
}


```
ArrayIterateLevel
```java
@Override
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
SortLevel (bubblesort)
```java
public Monster[] sortMonsters() {
    for (int i = 0; i < monsterArray.length - 1; i++) {
        for (int j = 0; j < monsterArray.length - i - 1; j++) {
            if (monsterArray[j].hp() > monsterArray[j + 1].hp()) {
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




