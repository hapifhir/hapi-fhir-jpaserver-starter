/* eslint-disable regexp/prefer-d */
// https://hl7.org/fhirpath
Prism.languages.fhirpath = {
	'comment': {
		pattern: /\/\/.*|\/\*[\s\S]*?(?:\*\/|$)/,
		greedy: true
	},
	'constant': [
		// This is where I'm going to put in the literals for datetime/date/time/quantity
		/@[0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9]T[0-9][0-9]:[0-9][0-9](:[0-9][0-9])?(\.[0-9]+)?(Z|[+\-][0-9][0-9]:[0-9][0-9])?/,
		/@[0-9][0-9][0-9][0-9](-[0-9][0-9](-[0-9][0-9])?)?/,
		/@T[0-9][0-9]:[0-9][0-9](:[0-9][0-9])?(\.[0-9]+)?/,
		/\b\d+(?:(?:\.\d*)?(?:[eE][+-]?\d+)?)?\b\s+(years|months|weeks|days|hours|minutes|seconds|milliseconds|year|month|week|day|hour|minute|second|millisecond)\b/
	],
	'number': [
		/\b\d+(?:\.\d+)?(?:e[+-]?\d+)?\b/i,
		/\b\d+(?:\.\d+)?L\b/i
	],
	'string': {
		pattern: /(^|[^\\])'(?:\\.|[^\\'\r\n])*'(?!\s*:)/,
		lookbehind: true,
		greedy: true
	},
	'punctuation': /[()[\],.]/,
	'operator': /(>=|<=|!=|!~|[|\\+\-=<>~/*&])/,
	'keyword': [
		/\b(and|as|contains|day|days|div|hour|hours|implies|in|\$index|is|millisecond|milliseconds|minute|minutes|mod|month|months|or|second|seconds|\$this|\$total|week|weeks|xor|year|years)\b/,
		/\{\s*\}/
	],
	'boolean': /\b(?:false|true)\b/,
	'builtin': [
		// section 5.1 http://hl7.org/fhirpath/#existence
		/\b(empty|exists|all|allTrue|anyTrue|allFalse|anyFalse|subsetOf|supersetOf|count|distinct|isDistinct)\b/,
		// section 5.2 http://hl7.org/fhirpath/#filtering-and-projection
		/\b(where|select|repeat|ofType)\b/,
		// section 5.3 http://hl7.org/fhirpath/#subsetting
		/\b(single|first|last|tail|skip|take|intersect|exclude)\b/,
		// section 5.4
		/\b(union|combine)\b/,
		// section 5.5
		/\b(iif|toBoolean|convertsToBoolean|toInteger|convertsToInteger|toDate|convertsToDate|toDateTime|convertsToDateTime|toDecimal|convertsToDecimal|toQuantity|convertsToQuantity|toString|convertsToString|toTime|convertsToTime)\b/,
		// section 5.6
		/\b(indexOf|substring|startsWith|endsWith|contains|upper|lower|replace|matches|replaceMatches|length|toChars|split|join|encode|decode)\b/,
		// section 5.7
		/\b(abs|ceiling|exp|floor|ln|log|power|round|sqrt|truncate)\b/,
		// section 5.8
		/\b(children|descendants)\b/,
		// section 5.9 (not is in section 6.5)
		/\b(trace|now|timeOfDay|today|not)\b/,
		// section 6.3
		/\b(as|is)\b/,
		// section 7
		/\b(aggregate)\b/
	],
	'variable': [
		/(%\w+)\b/,
		/(%`(?:\w|x[0-9a-fA-F]{2}|u[0-9a-fA-F]{4}|[ \-\."\\\/fnrt])+`)/ // this isn;t quite right, but it's a start
	],
	'identifier': [
		{
			pattern: /`(?:\w|x[0-9a-fA-F]{2}|u[0-9a-fA-F]{4}|[ \-\."\\\/fnrt])+`/,
			// lookbehind: true,
			greedy: true
		},
		/\b([A-Za-z]|_)([A-Za-z0-9]|_)*\b/,
	]
};
