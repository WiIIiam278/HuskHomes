HuskHomes supports a number of community-sourced translations of the plugin locales into different languages. The default language is [`en-gb`](https://github.com/WiIIiam278/HuskHomes/blob/master/common/src/main/resources/locales/en-gb.yml) (English). The messages file is formatted using [MineDown](https://github.com/Phoenix616/MineDown).

You can change which preset language option to use by changing the top-level `language` setting in the plugin config.yml file. You must change this to one of the supported language codes. You can [view a list of the supported languages](https://github.com/WiIIiam278/HuskHomes/tree/master/common/src/main/resources/locales) by looking at the locales source folder.

## Contributing Locales
You can contribute locales by submitting a pull request with a yaml file containing translations of the [default locales](https://github.com/WiIIiam278/HuskHomes/blob/master/common/src/main/resources/locales/en-gb.yml) into your language. Here's a few pointers for doing this: 
* Do not translate the locale keys themselves (e.g. `teleporting_offline_player`)
* Your pull request should be for a file in the [locales folder](https://github.com/WiIIiam278/HuskHomes/tree/master/common/src/main/resources/locales)
* Do not translate the [MineDown](https://github.com/Phoenix616/MineDown) syntax itself or commands and their parameters; only the english interface text
* Each locale should be on one line, and the header should be removed.
* Use the correct ISO 639-1 [locale code](https://en.wikipedia.org/wiki/List_of_ISO_639-1_codes) for your language and dialect
* If you are able to, you can add your name to the `AboutMenu` translators credit list yourself, otherwise this can be done for you

Thank you for your interest in making HuskHomes more accessible around the world!
