/*
 * Copyright 2014 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netflix.spinnaker.front50.filters

import com.netflix.spinnaker.amos.AccountCredentialsRepository
import com.netflix.spinnaker.amos.aws.AmazonCredentials
import spock.lang.Specification
import spock.lang.Unroll

import javax.servlet.FilterChain
import javax.servlet.RequestDispatcher
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class AccountHostnameFilterSpec extends Specification {
  @Unroll
  void "if hostname matches vip pattern, internal forward appropriately"() {
    setup:
    def req = Mock(HttpServletRequest)
    def res = Mock(HttpServletResponse)
    def chain = Mock(FilterChain)
    def credentialsRepository = Mock(AccountCredentialsRepository)
    def filter = new AccountHostnameFilter(
        front50Domain: "netflix.net", front50Prefix: "front50", accountCredentialsRepository: credentialsRepository
    )

    when:
    filter.doFilter(req, res, chain)

    then:
    1 * req.getRequestURL() >> new StringBuffer("http://front50.test.netflix.net")
    1 * req.getRequestURI() >> uri
    1 * req.getRequestDispatcher(expectedForward) >> Mock(RequestDispatcher)
    1 * credentialsRepository.getOne("applications") >> null

    where:
    uri                             | expectedForward
    "/applications/"                | "/test/applications/"
    "/applications/name/FOO"        | "/test/applications/name/FOO"
    "/applications/search?name=FOO" | "/test/applications/search?name=FOO"
  }

  void "hostname matches that already have the account partner don't forward"() {
    setup:
    def req = Mock(HttpServletRequest)
    def res = Mock(HttpServletResponse)
    def chain = Mock(FilterChain)
    def credentialsRepository = Mock(AccountCredentialsRepository)
    def filter = new AccountHostnameFilter(
        front50Domain: "netflix.net", front50Prefix: "front50", accountCredentialsRepository: credentialsRepository
    )

    when:
    filter.doFilter(req, res, chain)

    then:
    1 * req.getRequestURL() >> new StringBuffer("http://front50.test.netflix.net")
    1 * req.getRequestURI() >> "/test/applications"
    1 * credentialsRepository.getOne("test") >> new AmazonCredentials("test", "12345", null, [new AmazonCredentials.AWSRegion("us-west-1", ["us-west-1a"])])
  }
}