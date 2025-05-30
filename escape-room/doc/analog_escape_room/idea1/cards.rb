$blanks = part(Array.new(8, ["Sackgasse. Bitte sortiert diese Karte zurück ins Deck ein."])).each_with_index.map { |b, index|
	name = "blank_#{index}".to_sym
	c[name] = b
	[b, i(name)]
}.to_h

[\
	*part([
		["Hallo! Dieser Stapel ist bewusst sortiert und sollte mit der Rückseite zu euch gucken.\nDas Spiel fordert euch gezielt auf, neue Karten zu nehmen. *Stellt einen Timer auf 30 Minuten und nehmt die Karte ", i(:intro), " um zu beginnen.*"],
		c[:intro] = ["Ihr seid eine Gruppe von Studenten, die bei der kurzfristigen Verlegung eines Tutoriums wegen Staatsbesuchs in den Raum C12 vergeblich auf den Beginn des Tutoriums gewartet haben. Die Raumnummer war schon richtig, nur ging es um Minden und nicht um Bielefeld. Da das nicht ganz eure Schuld war und damit ihr trotzdem eine anrechenbare Leistung erbringen könnt, bietet euch eure Dozentin Dr. Kordula Igel als Alternative die Ablegung eines kurzen E-Trainings an. *Nehmt die Karte ", i(:i1_intro), ".*"],
		c[:i1_intro] = ['**"Guten Tag und danke, dass sie diesem Termin zugestimmt haben. Natürlich möchte ich Sie alle nicht deswegen im nächsten Jahr wiedersehen. Leider verlangt die Prüfungsordnung trotzdem, dass sie eine Leistung erbringen müssen. Meine Tutoren sind nur dummerweise gerade bei einem Team-Building-Event und ich habe in fünf Minuten das nächste Meeting. Deshalb habe ich ein sehr kurzes E-Training vorbereitet. Geht dafür einfach in der Computerraum. Viel Spaß!"**', "*Nehmt die Karten ", i(:i1_task1), " und ", i(:i1_task2), ".*"],
		c[:i1_task1] = ["Wendet den Bubblesort-Algorithmus an und notiert dabei die Anzahl der Tauschoperationen. *Nehmt die Karten ", i(:i1_first_number), " bis ", i(:i1_last_number), " und legt sie in der Reihenfolge der Entnahme nebeneinander auf eine ebene Fläche. Zu Beginn sollen die Nummern auf den Rückseiten der Karten von links nach rechts aufsteigen. Nun dreht die Karten um und führt den Algorithmus durch, um die Vorderseiten zu sortieren. Vergesst nicht das Zählen der Schritte!*"],
		c[:i1_task2] = ["Die Eingabemaske erwartet eine einzelne Zahl, die berechnet wird, indem die sortierten Zahlen abwechseln addiert und subtrahiert werden. Am Ende wird die Zahl der Tauschoperationen addiert (`z1 + z2 - z3 + .. + c`). *Gebt das Ergebnis über die Karte ", i(:i1_enter_solution), " ein.*"],
		c[:i1_first_number] = 3,
		1,
		0,
		5,
		4,
		2,
		c[:i1_last_number] = 7, # + 7 Tauschoperationen, sollte insgesamt 2 rauskommen
		c[:i1_correct_solution] = ["**Falsches Ergebnis!**, sagt der Computer, aber ihr seid euch sicher, dass ihr keinen Fehler gemacht habt. *Nehmt die Karte ", i(:i2_found_correct), ".*<br>Ihr könnt eine weitere Eingabe probieren - oder abbrechen. *Zum Abbrechen nehmt die Karten ", i(:i2_start), " und ", i(:i2_aborted), "*"],
		c[:i1_phony_solution] = ["**Richtiges Ergebnis!**, sagt der Computer, aber irgendwie sieht das nicht richtig aus.<br>Ihr könnt eine weitere Eingabe probieren - oder das Assessment beenden. *Zum Beenden nehmt die Karten ", i(:i2_start), " und ", i(:i2_finished), "*"],
	], caption: "Das E-Training"),
	*part([\
		c[:i2_found_correct] = "*Das richtige Ergebis wurde gefunden.*<br>**Behaltet diese Karte bis zur Meldung beim Dozenten.**",
		c[:i2_start] = 'Die Tür von Dr. K. Igel steht offen: **"Mir ist vorhin eingefallen, dass die Frist zur Einreichung meines Forschungsantrags heute um Mitternacht abläuft, also habe ich nicht viel Zeit. Lasst mich nur schnell euer Ergebnis angucken.."**<br>*Legt alle Karten für "Das E-Training" beiseite.*',
		c[:i2_aborted] = ['**"Hmm. Stimmt. Das ist nicht gut! Ich überlege mir etwas wegen der Anerkennung der Leistung, aber jetzt haben wir ein ganz anderes Problem!"** *Nehmt die Karte', i(:i2_briefing), ".*"],
		c[:i2_finished] = ["*Habt ihr die Karte ", i(:i2_found_correct), " gefunden? Wenn ja, nehmt jetzt Karte ", i(:i2_finished_found), ". Ansonsten nehmt Karte ", i(:i2_finished_not_found), "*"],
		c[:i2_finished_found] = ['**"Praktischerweise kann ich das als Leistung anerkennen. Nur muss der Fehler dringend behoben werden!"** *Nehmt die Karte', i(:i2_briefing), ".*"],
		c[:i2_finished_not_found] = ['**"Das ist nicht die richtig. Dafür sollte ich euch eigentlich keine Punkte geben. Andererseits ist die Software kaputt und damit kriegen noch viel größere Probleme!"** *Nehmt die Karte ', i(:i2_briefing), ".*"],
		c[:i2_briefing] = ['**"In einer Stunde kommt ein Auditor vom BSI um die Assessments zu zertifizieren. Dieser Fehler muss behoben werden! Ohne die Zertifizierung werden alle damit abgenommen Prüfungen rückwirkend für ungültig erklärt - eine Katastrophe! Bald wird der nächste Studiengangsleiter gewählt und ich vermute, dass einer der Kandidaten dieses Debakel verwenden wird, um seine Wahl zu begünstigen. Deshalb bitte ich euch, den Fehler unauffällig zu beheben und der Ursache für den Bug diskret auf den Grund zu gehen."**<br>*Nehmt die Karte ', i(:i2_briefing2), ".*"],
		c[:i2_briefing2] = ['**"Hier habt ihr den Schlüssel zum Serverraum. Die Container-Images für das ÖLIAS werden dort an einem Computer generiert, der nur vor Ort bedient wird."**<br>*Nehmt die Karte ', i(:i3_intro), '.*<br>**"Eine Sache noch: Kürzlich wurde ein Feature für das Lernen in Gruppen eingebaut. Dabei hat jede Person eine eigene Eingabemaske. Das brauchen wir auch für den Audit, also könnt ihr nicht einfach alles zurückrollen."**'],
	], caption: "Eine neue Aufgabe"),
	*part([\
		c[:i3_intro] = ['"Eisige Luft zieht an euren Ohren vorbei während die Tür hinter euch zufällt. Ein einzelner Bildschirm mit Maus und Tastatur steht auf einem ansonsten leeren Schreibtisch zwischen den Racks. Nach dem Einschalten des Bildschirms zeigt sich euch ein Terminal. Jemand hat `git gc` eingegeben, aber noch nicht mit [Enter] bestätigt.<br>*Legt alle Karten für "Eine neue Aufgabe" ab und nehmt die Karte ', i(:i3_start), ".*"],
		c[:i3_start] = ["TODO mehrere Wege"],
	], caption: "Bugsuche"),
	*part([
		# bild von der eingabemaske wäre klasse
		# TODO die 'besonderen' karten erkennt man sofort:
		c[:i1_enter_solution] = redirection({"2" => i(:i1_correct_solution), "3" => i(:i1_phony_solution)}.merge([-2,1,4,5,0].zip($blanks.values.shuffle[..4]).sort.to_h), 2),
# 0ca0641 (HEAD -> main) qwer
# 4df0584 add TODO
# ad1b56e accept multiple answers per question
# b9d38a1 fix typo
# 6eb33e3 store answer list id before actual answers
# 20cbf1d use function for parsing query
# 409e423 read answer from array
# 8e69d38 convert result handler
		c[:i4_file_selection] = redirection({
}),
    *$blanks.keys,
	], caption: "?").shuffle,
]
