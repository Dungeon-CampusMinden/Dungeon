---
title: "git rebase"
---


Wir nutzen im Projekt einen **Squash-Merge-Workflow**, d.h. jeder Feature-Branch wird per Squash-Merge als ein einziger Commit in den `master`-Branch gemergt. Zusätzlich legen wir Wert auf eine **lineare Historie**, d.h. die zu mergenden Feature-Branches werden vor dem Merge auf die Spitze des aktuellen `master`-Branch rebased.

Diese Anleitung bezieht sich auf das Rebasen in der Workingcopy. Wenn für einen Pull-Request die Situation "einfacher Fall" vorliegt und im PR der `feature`-Branch in den `master`-Branch gemergt werden soll, erledigt Github über den Button "Squash Merge" den Rebase von `feature` auf `master` und anschließend den Squash-Merge. Das hier beschriebene Rebasen muss eigentlich nur gemacht werden, wenn auf dem Server (im PR) nicht der "einfache Fall" vorliegt oder aber wenn man lokal Branches verschieben will, um von Änderungen in anderen Branches zu profitieren.

Da es immer mal wieder zu Komplikationen mit verschiedenen Vorgehensweisen gekommen ist, hier das offizielle _How To Rebase_. Für eine ausführlichere Anleitung mit Beispielen siehe https://git-scm.com/book/de/v2/Git-Branching-Rebasing oder auch `git rebase --help`.

**Rebases werden ausschließlich per Konsole durchgeführt, **NICHT** per IDE oder über GitHub selbst oder über andere Tools.** 


## Rebasen eines Feature-Branches auf den Master-Branch (einfacher Fall)

```
      e---f---g feature
     /
a---b---c---d master
```

1. `git rebase master feature`  (Rebase des `feature`-Branches auf den `master`-Branch (Workingcopy))
2. `git push -f origin feature` (Aktualisieren des Remote-`feature`-Branches auf dem Server: "Force" Push)

```
              e'--f'--g' feature
             /
a---b---c---d master
```


## Rebasen eines Feature-Branches auf den Master-Branch (komplexerer Fall)

```
a---b---c---d---e  master
     \
      f---g---h---i---j  pivot
                       \
                        k---l---m  feature
```

1. `git rebase --onto master pivot feature` (Rebase des `feature`-Branches bis exklusiv Commit `j` auf den `master`-Branch (Workingcopy))
2. `git push -f origin feature`             (Aktualisieren des Remote-`feature`-Branches auf dem Server: "Force" Push)

```
                  k'---l'---m'  feature
                 /
a---b---c---d---e  master
     \
      f---g---h---i---j  pivot
```


## Aktualisieren eines Remote-Branches

Dieser Fall sollte nicht vorkommen, da immer nur eine Person einen Feature-Branch bearbeitet. Falls es dennoch passiert, dass sich ein Feature-Branch remote und lokal weiterenwickelt hat (d.h. divergiert ist), dann sollte das Aktualisieren der Workingcopy mit einem "Pull Rebase" passieren:

```
REMOTE (origin)

a---b---c---d---e  master
     \
      f---g---h---i---j feature


LOKAL (Workingcopy)

a---b---c---d---e  master
     \
      f---g---h---k---l feature
              ^
              |
              origin/feature
```

1. `git fetch --prune`    (Aktualisieren der Workingcopy)
2. `git checkout feature` (`feature`-Branch lokal in der Workingcopy auschecken)
3. `git pull --rebase`    (Lokalen `feature`-Branch mit dem remote `feature`-Branch aktualisieren, lokale zusätzliche Commits auf die Spitze des Remotes rebasen)
4. `git push`             (Aktualisieren des Remote-`feature`-Branches auf dem Server: normaler Push)

```
REMOTE (origin)

a---b---c---d---e  master
     \
      f---g---h---i---j---k'---l' feature


LOKAL (Workingcopy)

a---b---c---d---e  master
     \
      f---g---h---i---j---k'---l' feature
                               ^
                               |
                               origin/feature
```


## Lokales Squash-Mergen eines Feature-Branches

Dieser Fall sollte nicht vorkommen, da niemand direkt Änderungen auf dem `master`-Branch pushen kann und dafür entsprechend Pull-Requests auf Github erstellt werden. Dennoch kurz das Vorgehen:

```
              e--f--g feature
             /
a---b---c---d master
```

1. `git checkout master`        (`master`-Branch in Workingcopy auschecken)
2. `git merge --squash feature` (`feature`-Branch als Squash-Merge in den `master` mergen: Alle Änderungen auf `feature` werden in einem neuen Commit auf `master` zusammengefasst)
3. `git commit -m "MESSAGE"`    (Squash-Merge abschließen!)
4. `git push`                   (Aktualisieren des Remote-`master`-Branches auf dem Server: normaler Push)
5. `git branch -D feature`      (alten `feature`-Branch in Workingcopy entfernen)
6. `git push origin :feature`   (alten `feature`-Branch im Remote entfernen)

```
a---b---c---d---e' master
```

(wobei `e'` die Commits `e, f, g` aus `feature` zusammenfasst)
