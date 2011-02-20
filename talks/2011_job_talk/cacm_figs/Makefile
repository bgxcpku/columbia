LATEX=pdflatex

FILES=tree.pdf binary_incremental.pdf trie_vs_tree.pdf

all: $(FILES)

clean:
	rm $(FILES)
	Hoover

binary_incremental.pdf: binary_incremental.tex
#	pdflatex --jobname=binary-incremental-1 $<
	pdflatex $<


%.pdf: %.tex tikz_header.tex 
	$(LATEX) $<
