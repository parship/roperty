Roperty
=======

Roperty - An advanced property management and retrival system

Roperty is roughly a hierarchical key-value-store. All keys accessed are cached in memory.
Depending of the persistence implementation, all keys are also preloaded into memory upon startup.
The main task for Roperty is to serve values to a key according to a domain hierarchy.

Domain hierarchies can be freely defined. As an example, a possible hierarchy for translation could be something like:

Language | Country | Partner | A/B-Testgroup  

When a key is accessed from Roperty, a DomainResolver needs to be provided.

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

So for example, there might be the following values stored in roperty for "keyToQuery":
(Notation: domain pattern => value)

de| => Deutsche Übersetzung  
de|DE| => Spezielle Übersetzung für Deutschland  
en| => English translation  
en|US| => Special translation for the US  
en|US|google| => Special translation for the partner account "google"  

Accessing this Roperty instance with the given domainResolver would return "Spezielle Übersetzung für Deutschland".

In addition Roperty also supports wildcards for domain keys. For example I could add a key translation like:

de|*|google| => Deutscher Text für Google Partner Konto  
\*|*|google| => Special translation for google partner account  
\*|*|*|3| => A/B-Testgroup 3 translation
\*|CH|*|3| => A/B-Testgroup 3 translation for Switzerland all languages

Accessing this Roperty with the given domainResolver instance would return "Deutscher Text für Google Partner Konto".
So Roperty always does a best match resolution, where most of the keys match. Explicit values match better than wildcards.
Values further to the right bind more than values to the left. So in the example above, when a query has a
domain value for "A/B-Testgroup" of 3, the final translation is used, no matter what the language etc. are,
unless country is CH.

