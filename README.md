# jlex-php

The JLexPHP is a JLex for PHP.

## Requirements

### Build

- Java JDK 1.8 (not tested on other versions)
- Maven

### Generated files

- PHP (no info about version now)
- Composer (or contents of [`jlex-php-lib`][link-jlex-php-lib])

## Build

Via Maven:

``` bash
mvn package
```

## Usage

``` bash
java -jar JLexPHP.jar [<options>] <input file> [<output file>]
```

The `options` is currently no one, one or more options that is listed here:

- `-v`/`--verbose` - Verbose output

The `input file` is a Lex file. The syntax is described [here][link-syntax].

The `output file` is a path to PHP file that was be written by the tool.

### Generated files

Generated files require for working the special PHP classes that are contained in [`progminer/jlex-php-lib`][link-composer] Composer package.

Classes in generated files has only one method `yylex` (or other that you specify by `%function` directive) that fully controls lexing process.

Base usage is:
``` php
$scanner = new Yylex(fopen("file", "r"));
while ($scanner->yylex());
```

If you use generated class as Lexer you can process a return value:
``` php
$lexer = new Yylex(fopen("file", "r"));
while ($token = $lexer->yylex()) {
    var_dump($token);
}
```

### Examples

Some examples is contained in the [`examples`](examples) directory in root of repo.

## Contributing

Please see [CONTRIBUTING](CONTRIBUTING.md) for details.

## Security

If you discover any security related issues, please email eridan200@mail.ru instead of using the issue tracker.

## Credits

- [Eridan Domoratskiy][link-author]
- [Wez Furlong](http://wezfurlong.org/) is author of [original `JLexPHP`][link-original]
- Elliot Joel Berk and [C. Scott Ananian](http://cscott.net/) is authors of [`JLex`][link-jlex]
- [All Contributors][link-contributors]

## License

The MIT License (MIT). Please see [License File](LICENSE.md) for more information.

[link-syntax]: http://www.cs.princeton.edu/~appel/modern/java/JLex/current/manual.html

[link-composer]: https://packagist.org/packages/progminer/jlex-php-lib
[link-jlex-php-lib]: https://github.com/ProgMiner/jlex-php-lib

[link-original]: https://github.com/wez/JLexPHP
[link-jlex]: http://www.cs.princeton.edu/~appel/modern/java/JLex/

[link-author]: https://github.com/ProgMiner
[link-contributors]: ../../contributors