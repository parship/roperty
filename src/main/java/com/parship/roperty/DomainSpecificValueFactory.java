/*
 * Roperty - An advanced property management and retrival system
 * Copyright (C) 2013 PARSHIP GmbH
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

package com.parship.roperty;

/**
 * @author mfinsterwalder
 * @since 2013-06-03 14:33
 */
public interface DomainSpecificValueFactory {

    DomainSpecificValue create(final Object value, final String changeSet, final String... domainValues);

    DomainSpecificValue createFromPattern(final Object value, final String changeSet, final String pattern);
}
