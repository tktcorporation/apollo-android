package com.apollographql.apollo3.compiler.incubating

/*
* Compared to the AST, the IR:
* - interprets directives
*   - moves @include/@skip directives on inline fragments and object fields to their children selections
*   - interprets @deprecated directives
* - coerce argument values and defaultValues
* - infers fragment variables
* - more generally removes all reference to the GraphQL AST
*/
internal data class IR(
    val operations: List<Operation>,
    val fragments: List<Fragment>,
)

data class Operation(
    val operationType: OperationType,
    val name: String,
    val description: String?,
    val variables: List<Variable>,
    val sourceWithFragments: String,
    val dataField: Field,
)

data class Fragment(
    val name: String,
    val description: String?,
    /**
     * Fragments do not have variables per-se but we can infer them from the document
     * Default values will always be null for those
     */
    val variables: List<Variable>,
    val source: String,
    val dataField: Field,
)

data class Field(
    val name: String,
    val alias: String?,
    // from the fieldDefinition
    val description: String?,
    // from the fieldDefinition
    val type: Type,
    // from the GQL directives
    val deprecationReason: String?,
    val arguments: List<Argument>,
    val condition: BooleanExpression,
    val fieldSets: List<FieldSet>,
) {
  val responseName = alias ?: name
}

data class FieldSet(
    val typeCondition: String,
    val fields: List<Field>
)

enum class OperationType {
  Query,
  Mutation,
  Subscription
}

data class Variable(val name: String, val defaultValue: Value?, val type: Type)

sealed class Value

data class IntValue(val value: Int) : Value()
data class FloatValue(val value: Double) : Value()
data class StringValue(val value: String) : Value()
data class BooleanValue(val value: Boolean) : Value()
data class EnumValue(val value: String) : Value()
object NullValue : Value()
data class ObjectValue(val fields: List<ObjectValueField>) : Value()
data class ObjectValueField(val name: String, val value: Value)
data class ListValue(val values: List<Value>) : Value()
data class VariableValue(val name: String) : Value()

data class Argument(
    val name: String,
    /**
     * the GQLValue coerced so that for an example, Ints used in Float positions are correctly transformed
     */
    val value: Value,
    /**
     * The looked-up default value, coerced
     */
    val defaultValue: Value?,
    val type: Type
)

sealed class Type {
  abstract val leafName: String
}

data class NonNullType(val ofType: Type) : Type() {
  override val leafName = ofType.leafName
}

data class ListType(val ofType: Type) : Type() {
  override val leafName = ofType.leafName
}

sealed class NamedType(val name: String) : Type() {
  override val leafName = name
}

object StringType: NamedType("String")
object IntType: NamedType("Int")
object FloatType: NamedType("Float")
object BooleanType: NamedType("Boolean")
object IDType: NamedType("ID")
class CustomScalarType(name: String): NamedType(name)
class EnumType(name: String): NamedType(name)
class UnionType(name: String): NamedType(name)
class ObjectType(name: String): NamedType(name)
class InputObjectType(name: String): NamedType(name)
class InterfaceType(name: String): NamedType(name)