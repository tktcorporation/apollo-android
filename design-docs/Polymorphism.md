_Foreword: This is a document that is intended to be in the public documentation at some point so it's targeted at end user. We're definitely not there yet and some of the things described here are not 100% ready yet but explaining "how" it should work helps me reason about how to implement it._ 

# Polymorphism in Apollo Android

Apollo Android generates typesafe models for your GraphQL queries. That means you can use the generated models with the warm fuzzy feeling that your types will exactly match the types of the schema, null-safety included. While the generated code is relatively straightforward in the simple cases, things can become more complex when polymorphic fields are involved, which this document tries to explain. If you want more details, you can also check the [Compiler overview](Compiler.md). Or continue reading for an overview of how Apollo handles polymorphism.

A polymorphic field is a field that contains fragments (inline or named) and whose json response can take different shapes depending on the concrete type of the object being queried. For an example:

```graphql
{
    animal {
        __typename
        species
        ... on Lion {
            roar
        }
        ... on Cat {
            meow
        }   
    }
}
```

This might result in something like

```json
{
  "animal": {
    "__typename": "Lion",
    "species": "feline",
    "roar": "RrROOoOoOOOooooAAaaaaaaRRrrrr!!!!!!!" 
  }
}
```
or
```json
{
  "animal": {
    "__typename": "Cat",
    "species": "feline",
    "meow": "meoooow..."
  }
}
```

In this case, Apollo Android generates two classes: `class LionAnimal(val species: String, val roar: String)` and `class CatAnimal(val species: String, val meow: String)`. Because we want to know the `species` without having to cast, to a `LionAnimal` or a `CatAnimal`, Apollo Android will generate a `Animal` interface that both classes inherit:

```kotlin
interface Animal {
  val species: String
}
```

Because both `Lion` and `Cat` inherit from `Animal`, the species can be be retrieved without knowing the animal:
```kotlin
println(data.animal.species)
// feline
```

Note that there can be other types of `Animals` in the schema. Or even types that can be added at a later point and for which the generated code has to account for so a 3rd class has to be generated:

```kotlin
class OtherAnimal(val species: String): Animal
```

For this setup, we end up with 4 classes and interfaces being generated (details omitted for clarity):

```kotlin
interface Animal
class CatAnimal: Animal
class LionAnimal: Animal
class OtherAnimal: Animal 
```

## Common interfaces and Type cases

In the above example, the fragments type conditions are concrete types but fragments can also be queried on interfaces:

```graphql
{
    animal {
        __typename
        species
        ... on Lion {
            roar
        }
        ... on Cat {
            meow
        }   
        ... on Pet {
            name
        }
    }
}
```

In this case Apollo Android will generate an additional interface:

```kotlin
interface Pet {
  // from Animal
  val species: String
  // from Pet
  val name: String
} 
```

Then classes are generated for each possible response shape:

```kotlin
interface Animal
interface PetAnimal
class LionAnimal: Animal
class CatAnimal: Pet, Animal
// A Dog type for an example, will be represented with a PetAnimal model
class PetAnimal: Pet, Animal
// This is a fallback type for all other animals like Crocodile for an example.
// But also for any other new type that might be added to the server without the 
// App being recompiled
class OtherAnimal: Animal
```

## Merged compound fields

Meged compound fields make the models more complex. If we go back to the starting point, we can generate simple classes if no polymorphism is involved:

```graphql
{
    # Everything is a just one class:
    # Cat
    cat {
        species
        meow
    }
}
```

Polymorphism introduces interfaces: 
```graphql
{
    # Depending the possible types, we'll have to potentially generate interfaces and classes like:
    # Animal, PetAnimal, CatAnimal, OtherAnimal
    animal {
        ... on Pet {
            name: String 
        }
        ... on Cat {
            meow: String
        }
    }
}

```

Merged compound fields will make these interfaces deep, meaning a class might have to inherit an interface not coming from its parent but from a descend of another further common ancestor:

```graphql
{
    __typename
    habitat {
        name
    }
    ... on Lion {
        habitat {
            countries
        }
    }
    ... on Cat {
        habitat {
            temperature
        }
    }
}
```
leads to responses:
```json
{
  "__typename": "Lion",
  "habitat":  {
    "name": "savannah",
    "countries": ["South Africa", "Kenya"]
  }
}
```
and
```json
{
  "__typename": "Cat",
  "habitat":  {
    "name": "couch",
    "temperature": 21.0
  }
}
```

Whether the animal is a cat or a lion, we want to be able to access `animal.habitat.name`. Apollo Android captures that by generating a common interface:

```kotlin
interface Animal {
  interface Habitat {
    val name: String
  }
}
```

But for `Lion`, we should also have `Habitat.countries` and for can `Habitat.temperature`, which means generating classes:

```kotlin
class LionAnimal: Animal {
  val habitat: Habitat
  // This Habitat has to inherit from the Animal.Habitat in order for Lion 
  // To conform to the Animal interface
  class Habitat: Animal.Habitat {
    override val name: String
    val countries: List<String>
  }
}

class CatAnimal: Animal {
  val habitat: Habitat
  // Same as above
  class Habitat: Animal.Habitat {
    override val name: String
    val temperature: Double
  }
}
```

As you can see, the deeper the merged fields, the more interfaces Apollo will have to generate. 

As it builds the models, Apollo builds 2 trees:
* `Data`: for the actual response shapes as data classes.
* `IData`: tree for all the interfaces.

Each time there is a polymorphic field the tree branches, meaning that the number of classes grows quite quickly as nested polymorphic fields are encountered.
If no polymorphic field is encountered at all, then no interfaces are needed and the `IData` tree will be pruned entirely.

## Named fragments (TODO)

## Conclusion 

Polymorphism is a double-edged sword. While it's powerful, it can also increase your build times and overall complexity of the project. If you find yourself needing polymorphism, try to keep these general principles in mind to keep the generated code maintainable:

* Avoid early polymorphic fields. Since a shadow interface tree has to be built as soon as a polymorphic field is encountered, this multiplies the size of the generated code by 2. It also adds an indirection that makes generated models harder to follow.
* Avoid nested polymorphic fields. The number of interfaces that have to be generated grows exponentially as the polymorphic fields become more nested. Even nesting 2 or 3 polymorphic fields can end up in a huge amount of code being generated. 
* Use field aliases. If several fields in your query have the same name, you will end up with several classes and interfaces using the same name. They will be namespaced so that there is no nameclash but it can still be confusing. Using aliases can make it easier to reason.
* Use the `@passthrough` directive for fields that only contain one subfield. This will effectively skip the generation of one interface and keep the generated code simpler and more performant. (This one is 100% TBC)

