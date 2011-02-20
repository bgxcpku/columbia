# latex build
NAME=proposal

default:
	latex $(NAME)
	bibtex $(NAME)
	latex $(NAME)
	latex $(NAME)
	dvipdf $(NAME).dvi $(NAME).pdf

# works within CSAIL, where ~/public_html exists
web: default
	mv $(NAME).pdf $(HOME)/public_html/misc
	chmod a+r $(HOME)/public_html/misc/$(NAME).pdf

clean:
	rm -f *.aux *.log *.dvi *.toc *.bbl *.blg *~ ./#*
