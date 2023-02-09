

Ziel ist es, viele kleinere und schneller zu integrierende Pull-Requests anstelle von einigen großen und langsam zu integrierenden Pull-Requests zu haben. 

Ein Pull-Request resultiert in einem Squash-Merge-Commit im Master, d.h. die Änderungen eines PR können nach dem Merge nur gemeinsam adressiert werden (Cherry-Pick oder Reset oder ...). Deshalb darauf achten, dass die Änderungen in einem PR eine zusammenhängende logische Einheit bilden.

Bei der Arbeit im Repository müssen folgende Schritte berücksichtigt werden:  
1. Issues sind so zu formulieren, dass das Problem und die Aufgabenstellung klar verständlich sind. Die Verwendung passender Labels und Tags ist verpflichtend. Jedes Issue sollte nach Möglichkeit auch den passenden Projekten zugewiesen sein.
2. Die Bearbeitung eines Issues sollte maximal eine Woche Arbeit kosten. Größere Issues sind daher in Sub-Issues aufzuteilen, ggf. kann dies auch während der Bearbeitung eines Issues geschehen. 
3. Bevor ein Issue bearbeitet wird, muss ein entsprechender Asignee gesetzt werden. 
4. Diskussionen über das Problem und mögliche Lösungsansätze finden im entsprechenden Issue statt, NICHT im Pull-Request. 
5. Zu jedem Pull-Request gehört GENAU EIN Issue.
6. Ein Pull-Request muss klar und deutlich beschrieben werden. Es sollte klar werden, welche Methodik genutzt wird, um das Issue zu lösen. 
7. Ein Review ist erst dann anzufordern, wenn keine weiteren Änderungen zu erwarten sind. 
8. In einem Pull-Request/Review wird nur die konkrete Umsetzung der im Ticket vereinbarten Lösung diskutiert. Sollten Fragen/Anregungen/Diskussionen darüber hinaus aufkommen, ist diese Diskussion in das Ticket zu verlagern oder ein entsprechendes Issue zu erstellen.
9. Pull-Requests sollten schnell bearbeitet und in den `master` integriert werden. Pull-Requests, die aus einem Grund über einen längeren Zeitraum existieren, werden regelmäßig auf den aktuellen `master`-Branch rebased: `git rebase master feature; git push -f origin feature`. 
10. Es arbeitet immer nur eine Person an einem Pull-Request bzw. Feature-Branch. 
