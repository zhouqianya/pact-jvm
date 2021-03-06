package au.com.dius.pact.matchers

import au.com.dius.pact.model.HttpPart
import au.com.dius.pact.model.Request
import au.com.dius.pact.model.matchingrules.MatchingRules
import au.com.dius.pact.model.matchingrules.MatchingRulesImpl

object Matching {

  private val lowerCaseComparator = Comparator<String> { a, b -> a.toLowerCase().compareTo(b.toLowerCase()) }

  @JvmStatic
  fun matchRequestHeaders(expected: Request, actual: Request) =
    matchHeaders(expected.headersWithoutCookie(), actual.headersWithoutCookie(), expected.matchingRules)

  @JvmStatic
  fun matchHeaders(expected: HttpPart, actual: HttpPart): List<HeaderMismatch> =
    matchHeaders(expected.headers.orEmpty(), actual.headers.orEmpty(), expected.matchingRules)

  @JvmStatic
  fun matchHeaders(
    expected: Map<String, List<String>>,
    actual: Map<String, List<String>>,
    matchers: MatchingRules?
  ): List<HeaderMismatch> = compareHeaders(expected.toSortedMap(lowerCaseComparator),
    actual.toSortedMap(lowerCaseComparator), matchers)

  fun compareHeaders(
    e: Map<String, List<String>>,
    a: Map<String, List<String>>,
    matchers: MatchingRules?
  ): List<HeaderMismatch> {
    return e.entries.fold(listOf()) { list, values ->
      if (a.containsKey(values.key)) {
        val actual = a[values.key].orEmpty()
        list + values.value.mapIndexed { index, headerValue ->
          HeaderMatcher.compareHeader(values.key, headerValue, actual.getOrElse(index) { "" }, matchers ?: MatchingRulesImpl())
        }.filterNotNull()
      } else {
        list + HeaderMismatch(values.key, values.value.joinToString(separator = ", "), "", "Expected a header '${values.key}' but was missing")
      }
    }
  }
}
