#!/usr/bin/env ruby

# collect objects from the list of cards that are referenced
$c = {}

# return an object that yields the index of the object belonging to the key 's' when .to_s is called
# (to_s is called only after the array is constructed)
def i(s)
	o = Object.new
	o.define_singleton_method(:to_s) {
		index = $cards.index($c[s])
		raise "unknown card: #{s}" if index.nil?
		index.to_s
	}
	o
end

# copy $c to a local variable for ease of use
c = $c

class Part
	attr_reader :num, :data
	def initialize(num, data)
		@num = num
		@data = data
	end
end

# create a wrapper around the elements in 'data' that lets us know later which part of the escape room this card belongs to
def part(num, data)
	data.map { |d|
		Part.new(num, d)
	}
end

$cards = [
	*part(1, [
		["Hallo! Dieser Stapel ist bewusst sortiert und sollte mit der Rückseite zu euch gucken.\nDas Spiel fordert euch gezielt auf, neue Karten zu nehmen. *Stellt einen Timer auf 30 Minuten und nehmt die Karte ", i(:intro), " um zu beginnen.*"],
		c[:intro] = ["Ihr seid eine Gruppe von Studenten, die bei der kurzfristigen Verlegung eines Tutoriums wegen Staatsbesuchs in den Raum C12 vergeblich auf den Beginn des Tutoriums gewartet haben. Die Raumnummer war schon richtig, nur ging es um Minden und nicht um Bielefeld. Da das nicht ganz eure Schuld war und damit ihr trotzdem eine anrechenbare Leistung erbringen könnt, bietet euch eure Dozentin Dr. Kordula Igel als Alternative die Ablegung eines kurzen E-Assessments an. *Nehmt die Karte ", i(:i1_intro), ".*"],
		c[:i1_intro] = ['**"Guten Tag und danke, dass sie diesem Termin zugestimmt haben. Natürlich möchte ich Sie alle nicht deswegen im nächsten Jahr wiedersehen. Leider verlangt die Prüfungsordnung trotzdem, dass sie eine Leistung erbringen müssen. Meine Tutoren sind nur dummerweise gerade bei einem Team-Building-Event und ich habe in fünf Minuten das nächste Meeting. Deshalb habe ich ein sehr kurzes E-Assessment vorbereitet. Geht dafür einfach in der Computerraum. Viel Spaß!"**', "*Nehmt die Karten ", i(:i1_task1), " und ", i(:i1_task2), ".*"],
		c[:i1_task1] = ["Wendet den Bubblesort-Algorithmus an und notiert dabei die Anzahl der Tauschoperationen. *Nehmt die Karten ", i(:i1_first_number), " bis ", i(:i1_last_number), " und legt sie in der Reihenfolge der Entnahme nebeneinander auf eine ebene Fläche. Zu Beginn sollen die Nummern auf den Rückseiten der Karten von links nach rechts aufsteigen. Nun dreht die Karten um und führt den Algorithmus durch, um die Vorderseiten zu sortieren. Vergesst nicht das Zählen der Schritte!*"],
		c[:i1_task2] = ["Die Eingabemaske erwartet eine einzelne Zahl, die berechnet wird, indem die sortierten Zahlen abwechseln addiert und subtrahiert werden. Am Ende wird die Zahl der Tauschoperationen addiert (`z1 + z2 - z3 + .. + c`). *Die Hilfskarte ", i(:i1_enter_solution), " leitet euch weiter.*"],
		c[:i1_first_number] = 3,
		1,
		0,
		5,
		4,
		2,
		c[:i1_last_number] = 7, # + 7 Tauschoperationen, sollte insgesamt 2 rauskommen
	]),
	*part(2, [\
		c[:i2_correct] = "*Das richtige Ergebis wurde nicht akzeptiert*.<br>**Behaltet diese Karte bis zur Meldung beim Dozenten.**",
	]),
	*part("Hilfskarte", [
		# bild von der eingabemaske wäre klasse
		c[:i1_enter_solution] = [],
	])
]

display_cards = $cards.each_with_index.map { |v,i|
	if v.kind_of?(Part)
		key = $c.key(v.data)
		$c[key] = v unless key.nil?

		part = if v.num.kind_of?(Numeric)
			"(Teil #{v.num})"
		else
			"(#{v.num})"
		end

		["*#{i}*<br>#{part}", v.data]
	else
		["*#{i}*", v]
	end
}.to_h

display_cards.each { |i,v|
	s = if v.kind_of?(Array)
		v.map { |e|
			e.to_s
		}.join
	else
		v.to_s
	end

	# TODO render front and back using markdown for printing
	puts "#{i}: #{s}"
	puts
}
