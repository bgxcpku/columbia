LATEX=pdflatex

FILES=coagulation.pdf

all: $(FILES)

clean:
	rm $(FILES)
	Hoover

%.pdf: %.tex tikz_header.tex 
	$(LATEX) $<
