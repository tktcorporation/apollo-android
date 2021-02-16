// AUTO-GENERATED FILE. DO NOT MODIFY.
//
// This class was automatically generated by Apollo GraphQL plugin from the GraphQL queries it found.
// It should not be modified by hand.
//
package com.example.union_fragment.adapter

import com.apollographql.apollo3.api.ResponseAdapterCache
import com.apollographql.apollo3.api.ResponseField
import com.apollographql.apollo3.api.internal.ListResponseAdapter
import com.apollographql.apollo3.api.internal.NullableResponseAdapter
import com.apollographql.apollo3.api.internal.ResponseAdapter
import com.apollographql.apollo3.api.internal.StringResponseAdapter
import com.apollographql.apollo3.api.internal.json.JsonReader
import com.apollographql.apollo3.api.internal.json.JsonWriter
import com.example.union_fragment.TestQuery
import kotlin.Array
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List

@Suppress("NAME_SHADOWING", "UNUSED_ANONYMOUS_PARAMETER", "LocalVariableName",
    "RemoveExplicitTypeArguments", "NestedLambdaShadowedImplicitParameter", "PropertyName",
    "RemoveRedundantQualifierName")
class TestQuery_ResponseAdapter(
  responseAdapterCache: ResponseAdapterCache
) : ResponseAdapter<TestQuery.Data> {
  private val nullableListOfNullableSearchAdapter: ResponseAdapter<List<TestQuery.Data.Search?>?> =
      NullableResponseAdapter(ListResponseAdapter(NullableResponseAdapter(Search(responseAdapterCache))))

  override fun fromResponse(reader: JsonReader): TestQuery.Data {
    var search: List<TestQuery.Data.Search?>? = null
    reader.beginObject()
    while(true) {
      when (reader.selectName(RESPONSE_NAMES)) {
        0 -> search = nullableListOfNullableSearchAdapter.fromResponse(reader)
        else -> break
      }
    }
    reader.endObject()
    return TestQuery.Data(
      search = search
    )
  }

  override fun toResponse(writer: JsonWriter, value: TestQuery.Data) {
    writer.beginObject()
    writer.name("search")
    nullableListOfNullableSearchAdapter.toResponse(writer, value.search)
    writer.endObject()
  }

  companion object {
    val RESPONSE_FIELDS: Array<ResponseField> = arrayOf(
      ResponseField(
        type = ResponseField.Type.List(ResponseField.Type.Named.Object("SearchResult")),
        fieldName = "search",
        arguments = mapOf<String, Any?>(
          "text" to "test"),
        fieldSets = listOf(
          ResponseField.FieldSet("Starship", Search.StarshipSearch.RESPONSE_FIELDS),
          ResponseField.FieldSet(null, Search.OtherSearch.RESPONSE_FIELDS),
        ),
      )
    )

    val RESPONSE_NAMES: List<String> = RESPONSE_FIELDS.map { it.responseName }
  }

  class Search(
    responseAdapterCache: ResponseAdapterCache
  ) : ResponseAdapter<TestQuery.Data.Search> {
    val StarshipSearchAdapter: StarshipSearch =
        com.example.union_fragment.adapter.TestQuery_ResponseAdapter.Search.StarshipSearch(responseAdapterCache)

    val OtherSearchAdapter: OtherSearch =
        com.example.union_fragment.adapter.TestQuery_ResponseAdapter.Search.OtherSearch(responseAdapterCache)

    override fun fromResponse(reader: JsonReader): TestQuery.Data.Search {
      reader.beginObject()
      check(reader.nextName() == "__typename")
      val typename = reader.nextString()

      return when(typename) {
        "Starship" -> StarshipSearchAdapter.fromResponse(reader, typename)
        else -> OtherSearchAdapter.fromResponse(reader, typename)
      }
      .also { reader.endObject() }
    }

    override fun toResponse(writer: JsonWriter, value: TestQuery.Data.Search) {
      when(value) {
        is TestQuery.Data.Search.StarshipSearch -> StarshipSearchAdapter.toResponse(writer, value)
        is TestQuery.Data.Search.OtherSearch -> OtherSearchAdapter.toResponse(writer, value)
      }
    }

    class StarshipSearch(
      responseAdapterCache: ResponseAdapterCache
    ) {
      private val stringAdapter: ResponseAdapter<String> = StringResponseAdapter

      fun fromResponse(reader: JsonReader, __typename: String?):
          TestQuery.Data.Search.StarshipSearch {
        var __typename: String? = __typename
        var id: String? = null
        var name: String? = null
        while(true) {
          when (reader.selectName(RESPONSE_NAMES)) {
            0 -> __typename = stringAdapter.fromResponse(reader)
            1 -> id = stringAdapter.fromResponse(reader)
            2 -> name = stringAdapter.fromResponse(reader)
            else -> break
          }
        }
        return TestQuery.Data.Search.StarshipSearch(
          __typename = __typename!!,
          id = id!!,
          name = name!!
        )
      }

      fun toResponse(writer: JsonWriter, value: TestQuery.Data.Search.StarshipSearch) {
        writer.beginObject()
        writer.name("__typename")
        stringAdapter.toResponse(writer, value.__typename)
        writer.name("id")
        stringAdapter.toResponse(writer, value.id)
        writer.name("name")
        stringAdapter.toResponse(writer, value.name)
        writer.endObject()
      }

      companion object {
        val RESPONSE_FIELDS: Array<ResponseField> = arrayOf(
          ResponseField.Typename,
          ResponseField(
            type = ResponseField.Type.NotNull(ResponseField.Type.Named.Other("String")),
            fieldName = "id",
          ),
          ResponseField(
            type = ResponseField.Type.NotNull(ResponseField.Type.Named.Other("String")),
            fieldName = "name",
          )
        )

        val RESPONSE_NAMES: List<String> = RESPONSE_FIELDS.map { it.responseName }
      }
    }

    class OtherSearch(
      responseAdapterCache: ResponseAdapterCache
    ) {
      private val stringAdapter: ResponseAdapter<String> = StringResponseAdapter

      fun fromResponse(reader: JsonReader, __typename: String?): TestQuery.Data.Search.OtherSearch {
        var __typename: String? = __typename
        while(true) {
          when (reader.selectName(RESPONSE_NAMES)) {
            0 -> __typename = stringAdapter.fromResponse(reader)
            else -> break
          }
        }
        return TestQuery.Data.Search.OtherSearch(
          __typename = __typename!!
        )
      }

      fun toResponse(writer: JsonWriter, value: TestQuery.Data.Search.OtherSearch) {
        writer.beginObject()
        writer.name("__typename")
        stringAdapter.toResponse(writer, value.__typename)
        writer.endObject()
      }

      companion object {
        val RESPONSE_FIELDS: Array<ResponseField> = arrayOf(
          ResponseField.Typename
        )

        val RESPONSE_NAMES: List<String> = RESPONSE_FIELDS.map { it.responseName }
      }
    }
  }
}