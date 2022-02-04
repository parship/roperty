Roperty
=======

Roperty - An advanced property management and retrieval system

Roperty is roughly a hierarchical key-value-store. All keys accessed are cached in memory.
Depending on the persistence implementation, all keys are also preloaded into memory upon startup.
The main task for Roperty is to serve values to a key according to a domain hierarchy.
One typical example is serving different translations for translation keys.

Domain hierarchies can be freely defined. As an example, a possible hierarchy for translation could be something like:

    Language | Country | Partner | A/B-Testgroup

So you want to ask Roperty: What is the translation for the key "GREETING", when the user whats to have language en
in the country CANADA for the partner Google and is in A/B-Testgroup 2.

Roperty will than search for a "best match" to answer this query, according to the data stored in Roperty before.

So maybe for the key "GREETING" there are three values stored:

One for language en, country USA, any Partner, any Testgroup
One for language en, any country, any Partner, Testgroup 1
One for language en, any country, any Partner, Testgroup 2
One for language en, any country, Google, Testgroup 7

When Roperty is queried with the above values (en|CANADA|Google|2), the last translation would be picked, since it's
the best match. You can also ignore a domain in a query by providing no value.
So it's also possible to query for (en|UK|<any>|7), which would find the fourth value. 
(en|UK|<any>|1) on the other hand would find the second value.

When a key is accessed from Roperty, a DomainResolver needs to be provided. The DomainResolver determines the current
values for each domain to use when querying Roperty. So in the above case (en|CANADA|Google|2) the DomainResolver
would return "en" when asked for the domainValue for "Language", "CANADA" when asked for the domainValue for "Country" and so on.
The DomainResolver can return null to ignore a domain, as noted with <any> above.

```java
DomainResolver domainResolver = ....;
roperty.get("keyToQuery", domainResolver);
roperty.get("keyToQuery", "defaultValue", domainResolver);
```

The DomainResolver is queried for domain value resolution for each domain configured with this instance of Roperty.
For example in the scenario described above, the domainResolver would be called four times:

```java
domainResolver.getDomainValue("Language");
domainResolver.getDomainValue("Country");
domainResolver.getDomainValue("Partner");
domainResolver.getDomainValue("A/B-Testgroup");
```

There is a default implementation for the DomainResolver, that just stores mappings from domain to domain value, the `MapBackedDomainResolver`.
It can be configured like this:

```java
DomainResolver domainResolver = new MapBackedDomainResolver()
	.set("Language", "de")
	.set("Country", "DE")
	.set("Partner", "google")
	.set("A/B-Testgroup", "4");
```

Roperty would then try to find the best match for the "keyToQuery" according to the resolved domain values.

So for example, there might be the following values stored in Roperty for "keyToQuery":
(Notation: domain pattern => value)

    de| => Deutsche Übersetzung
    de|DE| => Spezielle Übersetzung für Deutschland
    en| => English translation
    en|US| => Special translation for the US
    en|US|google| => Special translation for the partner account "google"

Accessing this Roperty instance with the given domainResolver would return "Spezielle Übersetzung für Deutschland".

In addition Roperty also supports wildcards for domain keys. For example, I could add a key translation like:

    de|*|google| => Deutscher Text für Google Partner Konto
    *|*|google| => Special translation for google partner account
    *|*|*|3| => A/B-Testgroup 3 translation
    *|CH|*|3| => A/B-Testgroup 3 translation for Switzerland all languages

Accessing this Roperty with the given `domainResolver` instance would return "Deutscher Text für Google Partner Konto".
So Roperty always does a best match resolution, where most of the keys match. Explicit values match better than wildcards.
Values further to the right bind more than values to the left. So in the example above, when a query has a
domain value for "A/B-Testgroup" of 3, the final translation is used, no matter what the language etc. are,
unless country is CH.

## Building

The module can be built using Maven:

    mvn package

## Installing to local Maven repository

To install a snapshot version to your local repository use

    mvn install

## Usage

To add a dependency on Roperty using Maven, use the following:

```xml
<dependency>
  <groupId>com.parship</groupId>
  <artifactId>roperty</artifactId>
  <version>1.1.0</version>
</dependency>
```

To add a dependency using Gradle:

```
dependencies {
  compile 'com.parship:roperty:1.1.0'
}
```

## Links

- [GitHub project](https://github.com/parship/roperty)
- [Issue tracker: Report a defect or feature request](https://github.com/parship/roperty/issues/new)
- [StackOverflow: Ask "how-to" and "why-didn't-it-work" questions](https://stackoverflow.com/questions/ask?tags=roperty)
