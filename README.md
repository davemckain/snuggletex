# SnuggleTeX

SnuggleTeX is a free and open-source Java library for converting
LaTeX to XML (usually XHTML + MathML).

I initially wrote it in 2008 and I'm no longer working on it,
however I'm aiming to make sure it continues to work.

## Code Examples

There are some self-contained examples of using SnuggleTeX included
within the source code:

[Browse example codes](https://github.com/davemckain/snuggletex/tree/development_1_2_x/snuggletex-core/src/main/java/uk/ac/ed/ph/snuggletex/samples)

## Documentation & demos

The SnuggleTeX source code includes a documentation & demos webapp.

I used to provide a public instance of this webapp, however I shut
that down in early 2024.

If you're happy accessing the SnuggleTeX source code (and have
Java & Maven installed), then you can run the webapp on your own
computer by typing the following from the top of the SnuggleTeX
source tree:

```shell
mvn install
cd snuggletex-webapp
mvn tomcat7:run
```

You should then be able to access the webapp via http://localhost:8080.

