#!/usr/bin/env ruby

if ARGV.length == 0
	STDERR.puts "usage: #{$0} CARD_FILE"
	exit 1
end

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

# create a markdown table
def redirection(m, columns = 1)
	a = ["|:-|:-:" * columns + "|\n"]
	m.each_slice(columns) { |e|
		a.push("|")
		e.each { |from,to|
			a.push(from)
			a.push("|")
			a.push(to)
			a.push("|")
		}
		a.push("\n")
	}
	a
end

class Card
	attr_reader :data, :params
	def initialize(data, **params)
		@data = data
		@params = params
	end
end

# create a wrapper around the elements in 'data' and retain the index information in $c
def part(data, **kw)
	data.map { |d|
		if ! d.kind_of?(Card)
			c = Card.new(d, **kw)

			key = $c.key(d)
			$c[key] = c unless key.nil?
		else
			d.params.merge(kw)
			c = d
		end

		c
	}
end

$cards = eval File.read(ARGV[0])

display_cards = $cards.each_with_index.map { |v,i|
	caption = nil
	front = nil

	if v.kind_of?(Card)
		caption = "#{v.params[:caption]}" if v.params.has_key?(:caption)
		front = v.data
	else
		front = v
	end

	if caption
		["*#{i}*<br>#{caption}", front]
	else
		["*#{i}*", front]
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
