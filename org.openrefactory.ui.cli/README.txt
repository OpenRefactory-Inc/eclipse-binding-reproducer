This project contains a demo command-line interface (CLI) for OpenRefactory/C
as well as the CGI backend for the Web demonstration at
http://www.openrefactory.org/demo.html

Both are run using ordemo.jar, which is built using the Ant script build.xml
in the root of this project.

To build ordemo.jar, run:
  ant build.xml

To compile an "ordemo" binary/executable from ordemo.jar using gcj:
  gcj -static-libgcj -static-libgcc -O3 --main=Demo -o ordemo ordemo.jar
