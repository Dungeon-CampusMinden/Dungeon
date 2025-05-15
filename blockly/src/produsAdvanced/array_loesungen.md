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





