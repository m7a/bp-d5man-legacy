#LANG   = tex
#TARGET = main.pdf
#include /usr/share/mdvl/make.mk

main.pdf: main.tex
	pdflatex main
	pdflatex main
	pdflatex main
