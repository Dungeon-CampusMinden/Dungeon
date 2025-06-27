## 🧠 Java Cheat Sheet – Grundlagen

### 🔢 Variablen

```java
int i = 5;                 // Ganze Zahl
float f = 3.5f;            // Kleine Fließkommazahl (mit 'f')
double d = 3.12545123;     // Große Fließkommazahl
char c = 'c';              // Einzelnes Zeichen
String s = "Das ist Text"; // Zeichenkette
```

**Rechnen und Verkettung:**

```java
int i = 5;
int v = i * 2; 
i = 3;
String s = "i ist jetzt " + i;
```

### 🔀 if / else

```java
if (BEDINGUNG) {
    // MACHE DAS
}
// Optional
else {
    // SONST DAS
}
```

**Beispiel:**

```java
Scanner scanner = new Scanner(System.in);
int i = scanner.nextInt(); // Benutzereingabe

if (i > 10) {
    System.out.println("Die Zahl ist größer als 10");
} else {
    System.out.println("Die Zahl ist kleiner oder gleich 10");
}
```

<div style="page-break-after: always;"></div>

### 🔁 for-Schleife

```java
for (STARTWERT; BEDINGUNG; SCHRITT) {
    // MACHE DAS
}
```

**Beispiel:**

```java
for (int i = 0; i < 10; i++) {
    System.out.println(i);
}
```

### 🔁 while-Schleife

```java
while (BEDINGUNG) {
    // MACHE DAS
}
```

**Beispiel:**

```java
int i = 0;
while (i < 10) {
    System.out.println(i);
    i++;
}
```

