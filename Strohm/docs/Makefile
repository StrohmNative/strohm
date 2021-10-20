# To build, install:
#   - Ruby
#   - Gems: asciidoctor, asciidoctor-diagram, and coderay
# then run `make`

tutorial: docs/Tutorial.html

docs/Tutorial.html: docs/*.adoc docs/*.css docs/*docinfo*
	bundle exec asciidoctor -o docs/Tutorial.html -b html5 -r asciidoctor-diagram docs/Tutorial.adoc

