/*
 * Copyright 2020 ZUP IT SERVICOS EM TECNOLOGIA E INOVACAO SA
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package br.com.zup.beagle.android.utils

import br.com.zup.beagle.android.context.Bind
import br.com.zup.beagle.core.DynamicObject
import java.lang.reflect.ParameterizedType
import java.lang.reflect.WildcardType

fun <I, G> Any.implementsGenericTypeOf(
    interfaceClazz: Class<I>,
    genericType: Class<G>
): Boolean {
    return this::class.java.genericInterfaces.filterIsInstance<ParameterizedType>()
        .filter {
            val rawType = it.rawType as Class<*>
            rawType == interfaceClazz
        }.any {
            val types = it.actualTypeArguments
            val paramType = types[0]
            val type = if (paramType is WildcardType) {
                paramType.upperBounds[0]
            } else {
                paramType
            }
            val typeClass = type as Class<*>
            genericType == typeClass
        }
}

internal fun Any?.toDynamicObject() : DynamicObject<*> {
    return when (this) {
        is DynamicObject<*> -> this
        is Boolean -> DynamicObject.Boolean(this)
        is Int -> DynamicObject.Int(this)
        is Double -> DynamicObject.Double(this)
        is String -> if (this.hasBind()) {
            DynamicObject.Expression(this)
        } else {
            DynamicObject.String(this)
        }
        is List<*> -> {
            DynamicObject.Array(this.map {
                it.toDynamicObject()
            })
        }
        is Map<*, *> -> DynamicObject.Dictionary(this.map {
            it.key as String to it.value.toDynamicObject()
        }.toMap())
        is Bind.Expression<*> -> DynamicObject.Expression(this.value)
        is Bind.Value<*> -> DynamicObject.String(this.value.toString())
        else -> DynamicObject.Empty()
    }
}
