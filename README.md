# bootleg

Static website generation made simple. A powerful, fast, clojure html templating solution.

Bootleg is a command line tool that rapidly renders clojure based templates. With inbuilt support for html, hiccup, hickory, mustache, enlive, json, yaml and edn, it enables you to pull together disparate elements and glue them together to generate the static website of your dreams.

## Quickstart

Install:

```shell
$ tar xvf bootleg-0.1.1-linux-amd64.tgz
$ mv bootleg ~/bin
```

A simple page:

```clojure
$ bootleg -e '[:html [:body [:h1 "A simple webpage"] [:p "Made with bootleg for maximum powers!"]]]'
<html><body><h1>A simple webpage</h1><p>Made with bootleg for maximum powers!</p></body></html>
```

A dynamic example:

```clojure
$ bootleg -e '[:div.countdown (for [n (range 10 0 -1)] [:p n]) [:p "blast off!"]]'
<div class="countdown"><p>10</p><p>9</p><p>8</p><p>7</p><p>6</p><p>5</p><p>4</p><p>3</p><p>2</p><p>1</p><p>blast off!</p></div>
```

Mustache:

```shell
$ cd examples/quickstart
$ bootleg -e '(mustache "quickstart.html" (yaml "fields.yml"))'
<h1>Bootleg</h1>
<h2>by Crispin</h2>
<p>I'm going to rewrite all my sites with this!</p>
```

Markdown support. Easy downloading of resources by url (for any command):

```clojure
$ bootleg -e '(markdown "https://raw.githubusercontent.com/retrogradeorbit/bootleg/master/README.md")'
<h1>bootleg</h1><p>Static website generation made simple. A powerful, fast, clojure templating solution that rocks!</p>...
```

CSS selector based processing. The magic of enlive:

```clojure
$ bootleg -e '(-> (markdown "examples/quickstart/simple.md") (enlive/at [:p] (enlive/set-attr :style "color:green;")))'
<h1>Markdown support</h1><p style="color:green;">This is some simple markdown</p>
```

Enlive processing:

```clojure
$ bootleg -e '(-> [:div [:h1.blurb] [:p.blurb]] (enlive/at [:.blurb] (enlive/content "blurb content")))'
<div><h1 class="blurb">blurb content</h1><p class="blurb">blurb content</p></div>
```

Data output with -d flag:

```clojure
$ bootleg -d -e '(markdown "examples/quickstart/simple.md")'
([:h1 {} "Markdown support"] [:p {} "This is some simple markdown"])
```

Process files:

```shell
$ echo '[:p#myid "an example"]' > example.clj
$ bootleg -o example.html example.clj
$ cat example.html
<p id="myid">an example</p>
```

## Installation

Bootleg is distributed for linux as a single executable file. Download the latest tarball from https://github.com/retrogradeorbit/bootleg/releases and then extract it. Once extracted, move the binary to your path. For system wide installation try `/usr/local/bin` or for personal use `~/bin`

```shell
$ curl -LO https://github.com/retrogradeorbit/bootleg/releases/download/v0.1.1/bootleg-0.1.1-linux-amd64.tgz
$ tar xvf bootleg-0.1.1-linux-amd64.tgz
$ mv bootleg /usr/local/bin
```

### Other Platforms

Although there is nothing preventing bootleg from running on winodws or MacOS, binary builds are not yet available. Although startup and execution time will be much slower, in the meantime you can use the jar release file and run it as follows:

```shell
$ java -jar bootleg-0.1.1.jar
```

## Usage

Run at the command line for options:

```shell
$ bootleg -h
Static website generation made simple. A powerful, fast, clojure html templating solution.

Usage: bootleg [options] [clj-file]

Options:
  -h, --help           Print the command line help
  -v, --version        Print the version string and exit
  -e, --evaluate CODE  Pass in the hiccup to evaluate on the command line
  -d, --data           Output the rendered template as a clojure form instead of html
  -o, --output FILE    Write the output to the specified file instead of stdout
```

## Overview

`bootleg` loads and evaluates the clj file specified on the command line, or evaluates the `CODE` form specified in the -e flag. The code can return any of the supported data structures listed below. `bootleg` will then automatically convert that format into html and write it out to standard out, or to `FILE` if specified. This conversion to html step can be prevented by calling with the `-d` flag. In this case the output will be a pretty formatted edn form of the output data structure.

`bootleg` supports five main markup data structures. Three are more flexible. And two are limited. We will begin describing the two limited data structures and why they are limited.

### hiccup

Hiccup is a standard clojure DSL syntax for representing markup as nested sequences of vectors, and is represented in option flags by the keyword `:hiccup`. An example of some hiccup: the html `<div><p>This is an example</p></div>` is represented in hiccup as `[:div [:p "This is an example"]]`.

Hiccup is limited in that it can only represent a single root element and its children. This means there are template fragments that *cannot* be represented in hiccup. For example, the html snippet `<p>one</p><p>two</p>` cannot be represented as hiccup. It is comprised of two hiccup forms. `[:p "one"]` and `[:p "two"]`

### hickory

Hickory is a format used to internally represent document trees in clojure for programmatic processing. In option flags it is referenced by the keyword `:hickory`. It is very verbose and not suitable to write by hand. It is supported internally for passing between functions that use it. A simple example of some hickory: the html `<p>one</p>` is represented in hickory as `{:type :element, :attrs nil, :tag :p, :content ["one"]}`.

Both the hickory and enlive clojure projects use this format internally to represent and manipulate DOM trees.

Hickory is limited in that it can only represent a single root element and its children. This means there are template fragments that *cannot* be represented in hickory. For example, the html snippet `<p>one</p><p>two</p>` cannot be represented as hickory. It is comprised of two hickory forms. `{:type :element, :attrs nil, :tag :p, :content ["one"]}` and `{:type :element, :attrs nil, :tag :p, :content ["two"]}`

### hiccup-seq

Hiccup-seq is simply a clojure sequence (or vector) of hiccup forms. In option flags it is referenced by the keyword `:hiccup-seq`. By wrapping multiple hiccup forms in a sequence, hiccup-seq can now represent any single root element (and it's children) *and* any template fragment composed of sibling elements.

For example: the html snippet `<p>one</p><p>two</p>` is represented in hiccup-seq as: `([:p "one"] [:p "two"])`

### hickory-seq

Hickory-seq is simply a clojure sequence (or vector) of hickory forms. In option flags it is referenced by the keyword `:hickory-seq`. By wrapping multiple hickory forms in a sequence, hickory-seq can now represent any single root element (and it's children) *and* any template fragment composed of sibling elements.

For example: the html snippet `<p>one</p><p>two</p>` is represented in hickory-seq as: `({:type :element, :attrs nil, :tag :p, :content ["one"]} {:type :element, :attrs nil, :tag :p, :content ["two"]})`

### html

html (or any type of xml) is represented internally as a string. This is a flexible type and can hold a root element and children, or a number of sibling elements sequentially.

## Inbuilt functions

The following functions are inbuilt into the clojure interpreter:

### Markup Processing Functions

#### markdown

`(markdown source & options)`

Load the markdown from the file specified in `source` and render it. `source` can be a local file path (relative to the executing hiccup file location) or a URL to gather the markdown from.

Options can be used to alter the behaviour of the function. Options are a list of keywords and can be specified in any order after the source parameter. Options can be:

 * `:data` Interpret the `source` argument as markdown data, not a file to load
 * `:hiccup-seq` Return the rendered markdown as a hiccup sequence data structure
 * `:hickory-seq` Return the rendered markdown as a hickory sequence data structure
 * `:html` Return the rendered markdown as an html string

eg.

    $ bootleg -e '(markdown "# heading\nparagraph" :data)'
    <h1>heading</h1><p>paragraph</p>

    $ bootleg -d -e '(markdown "# heading\nparagraph" :data :hickory-seq)'
    ({:type :element, :attrs nil, :tag :h1, :content ["heading"]}
     {:type :element, :attrs nil, :tag :p, :content ["paragraph"]})

    $ bootleg -d -e '(markdown "# heading\nparagraph" :data :hiccup-seq)'
    ([:h1 {} "heading"] [:p {} "paragraph"])

    $ bootleg -d -e '(markdown "# heading\nparagraph" :data :html)'
    "<h1>heading</h1><p>paragraph</p>"

#### mustache

`(mustache source vars & options)`

Load a mustache template from the file specified in `source` and render it substituting the vars from `vars`. `source` can be a local file path (relative to the executing hiccup file location) or a URL to gather the markdown from.

Options can be used to alter the behaviour of the function. Options are a list of keywords and can be specified in any order after the source parameter. Options can be:

 * `:data` Interpret the `source` argument as markdown data, not a file to load
 * `:hiccup` Return the rendered markdown as hiccup
 * `:hiccup-seq` Return the rendered markdown as a hiccup sequence data structure
 * `:hickory` Return the rendered markdown as hickory
 * `:hickory-seq` Return the rendered markdown as a hickory sequence data structure
 * `:html` Return the rendered markdown as an html string

eg.

```clojure
$ bootleg -e '(mustache "<p>{{var1}}</p><div>{{&var2}}</div>" {:var1 "value 1" :var2 "<p>markup</p>"} :data)'
<p>value 1</p><div><p>markup</p></div>
```

```clojure
$ bootleg -d -e '(mustache "<p>{{var1}}</p>" {:var1 "value 1"} :data :hiccup-seq)'
([:p {} "value 1"])
```

#### slurp

`(slurp source)`

Load the contents of a file, from a local or remote source, into memory and return it. This `slurp` can load from URLs. Does no interpretation of the file contents at all. Returns them as is.

#### html

`(html source & options)`

Loads the contents of a html or xml file and returns them in `:hiccup-seq` (by default).

Options can be:

 * `:data` Interpret the `source` argument as markdown data, not a file to load
 * `:hiccup` Return the rendered markdown as hiccup
 * `:hiccup-seq` Return the rendered markdown as a hiccup sequence data structure
 * `:hickory` Return the rendered markdown as hickory
 * `:hickory-seq` Return the rendered markdown as a hickory sequence data structure
 * `:html` Return the rendered markdown as an html string

eg.

```clojure
$ bootleg -d -e '(html "<h1>heading</h1><p>body</p>" :data :hiccup-seq)'
([:h1 {} "heading"] [:p {} "body"])
```

```clojure
$ bootleg -d -e '(html "<div><h1>heading</h1><p>body</p></div>" :data :hiccup)'
[:div {} [:h1 {} "heading"] [:p {} "body"]]
```

#### hiccup

`(hiccup source)`

Loads and evaluates the clojure source from another file.

### Var Loading Functions

#### yaml

`(yaml source)`

#### json

`(json source)`

#### edn

`(edn source)`

### Data Testing

#### is-hiccup?

`(is-hiccup? data)`

#### is-hiccup-seq?

`(is-hiccup-seq? data)`

#### is-hickory?

`(is-hickory? data)`

#### is-hickory-seq?

`(is-hickory-seq? data)`

### Data Conversion

#### convert-to

`(convert-to data type)`

Convert one supported data type to another. Input data may be hiccup, hiccup-seq, hickory, hickory-seq or html.

Type may be `:hiccup`, `:hiccup-seq`, `:hickory`, `:hickory-seq` or `html`.

Example:

```clojure
$ bootleg -d -e '(convert-to [:p#id.class "one"] :hickory)'
{:type :element,
 :attrs {:class "class", :id "id"},
 :tag :p,
 :content ["one"]}
```

```clojure
$ bootleg -d -e '(convert-to "<p class=\"class\" id=\"id\">one</p>" :hiccup)'
[:p {:class "class", :id "id"} "one"]
```

```clojure
$ bootleg -d -e '(convert-to "<p>one</p><p>two</p>" :hiccup-seq)'
([:p {} "one"] [:p {} "two"])
```

```clojure
$ bootleg -d -e '(convert-to {:type :element :tag :p :content ["one"]} :html)'
"<p>one</p>"
```

**Note:** Some conversions are lossy. Converting from html or any *-seq data type to hickory or hiccup may lose forms. Only the last form will be returned.

```clojure
$ bootleg -d -e '(convert-to "<p>one</p><p>two</p>" :hiccup)'
[:p {} "two"]
```

#### as-html

`(as-html data)`

Convert any supported input format passed into `data` to html output. Same as `(convert-to data :html)`

### Enlive Processing

Enlive html functions are to be found in the `enlive` namespace. The enlive macros are not supported (`deftemplate`, `defsnippet`, `clone-for`). A reimplementation of `at` is supplied that provides automatic type coercion for the inputs and outputs.

In addition to this the standard enlive namespaces are available in their usual locations:

 * net.cgrand.enlive-html (but does not include macros)
 * net.cgrand.jsoup
 * net.cgrand.tagsoup
 * net.cgrand.xml

#### at

`(at data selector transform & more)`

#### content

#### html-content

#### wrap

#### unwrap

#### set-attr

#### remove-attr

#### add-class

#### remove-class

#### do->

#### append

#### prepend

#### after

#### before

#### substitute

#### move

### Hickory

The `hickory` namespaces are provided at their usual namespace locations.

 * hickory.convert
 * hickory.hiccup-utils
 * hickory.render
 * hickory.select
 * hickory.utils
 * hickory.zip

## License

Copyright © 2019 Crispin Wellington

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
