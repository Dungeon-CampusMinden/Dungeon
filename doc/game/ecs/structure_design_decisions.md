---
title: "Strukturelle Designentscheidungen"
---

Dieses Dokument erläutert verschiedene Design-Entscheidungen, die im Hinblick auf die Struktur des Codes getroffen wurden.

## Verwendung von `public static` in `Game`

Einige Attribute der Klasse `Game`, wie z.B. das aktuelle Level, sind als `public static` deklariert. Der Grund dafür ist, dass diese Attribute an vielen Stellen benötigt werden und so ein einfacher und lesbarer Zugriff möglich ist. Ein Nachteil ist jedoch, dass die Attribute dadurch auch einfacher manipuliert werden können. Als Lösungsansatz können statische Getter implementiert werden, um den Zugriff auf die Attribute zu kontrollieren. Eine Alternative wäre die Übergabe der Attribute per Methodenparameter, jedoch würde dies den Code schwerer lesbar machen und zu redundantem Code führen.
