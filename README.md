# Funcraft - a Clojure port of Minicraft

This is a fun little project to learn how to rewrite an highly
imperative Java program in a functional way in Clojure.

Markus Persson (alias Notch, known as the creator of Minecraft)
created [Minicraft](https://en.wikipedia.org/wiki/Minicraft) in just
48 hours for the 22nd [Ludum Dare](https://ldjam.com/about)
Competition. Despite the short development time it is a completely
functional and high quality game. At the same time it is small enough
to be ported or enhanced in a reasonable amount of time. Indeed lots
of ports exists already (see the list below).

While he released the source code, he didn't assign any license to it,
so technically the original source and artwork still falls under his
copyright, but he gave permissions to make clones under a different
name.

Since this is just a learning project to see how to rewrite a highly
imperative OO program in a functional way in Clojure while at least
trying to keep similar performance characteristics as the original I
kept the style of just using the swing libraries in low-level way as
opposed to using a gaming library like libgdx.

I'm not sure how far I go with this and currently you can not much
besides walking and swimming around.

## Installation

Clone from https://github.com/bitti/funcraft

    $ git clone git@github.com:bitti/funcraft.git

Build with

    $ lein uberjar

## Usage

After building start with

    $ java -jar target/uberjar/funcraft-0.1.0-SNAPSHOT-standalone.jar

## Alternative clones

- https://playminicraft.com/
- http://www.indiedb.com/games/blockscraft2d
- https://github.com/chrisj42/minicraft-plus-revived
- https://github.com/masato462/Minicraft-Rebuild

## License

Distributed under the GNU Public License, Version 3

The original work is Copyright © 2011 By Markus Persson
