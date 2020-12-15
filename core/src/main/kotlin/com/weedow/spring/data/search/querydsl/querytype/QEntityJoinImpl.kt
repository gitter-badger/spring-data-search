package com.weedow.spring.data.search.querydsl.querytype

import com.querydsl.core.types.Path
import com.querydsl.core.types.PathMetadata
import com.querydsl.core.types.PathMetadataFactory
import com.querydsl.core.types.Visitor
import com.querydsl.core.types.dsl.Expressions
import com.querydsl.core.types.dsl.SimpleExpression
import com.weedow.spring.data.search.utils.MAP_KEY
import com.weedow.spring.data.search.utils.MAP_VALUE
import java.lang.reflect.AnnotatedElement

class QEntityJoinImpl<T>(
    private val qEntity: QEntity<out T>,
    private val propertyInfos: PropertyInfos
) : QEntityJoin<T> {

    override fun get(fieldName: String): QPath<*> {
        return if (propertyInfos.elementType == ElementType.MAP) {
            val alias = qEntity.metadata.element.toString()
            when (fieldName) {
                MAP_KEY -> createQPath(
                    ElementType.MAP_KEY,
                    propertyInfos.parameterizedTypes[0],
                    PathMetadataFactory.forVariable("key($alias)"),
                    propertyInfos
                )
                MAP_VALUE -> createQPath(
                    ElementType.MAP_VALUE,
                    propertyInfos.parameterizedTypes[1],
                    PathMetadataFactory.forVariable(alias),
                    propertyInfos
                )
                else -> throw IllegalArgumentException("The attribute name '$fieldName' is not authorized for a parent Map")
            }
        } else {
            qEntity.get(fieldName)
        }
    }

    override fun <R : Any?, C : Any?> accept(v: Visitor<R, C>?, context: C?): R? = qEntity.accept(v, context)

    override fun getType(): Class<out T> = qEntity.type

    override fun getMetadata(property: Path<*>?): Any = qEntity.getMetadata(property)

    override fun getMetadata(): PathMetadata = qEntity.metadata

    override fun getRoot(): Path<*> = qEntity.root

    override fun getAnnotatedElement(): AnnotatedElement = qEntity.annotatedElement

    private fun createQPath(elementType: ElementType, parameterizedType: Class<*>, pathMetadata: PathMetadata, propertyInfos: PropertyInfos): QPathImpl<*> {
        return QPathImpl(
            Expressions.path(parameterizedType, pathMetadata),
            getParameterizedPropertyInfos(parameterizedType, elementType, propertyInfos)
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun getParameterizedPropertyInfos(parameterizedType: Class<*>, elementType: ElementType, propertyInfos: PropertyInfos): PropertyInfos {
        return propertyInfos.copy(
            parentClass = propertyInfos.parentClass,
            fieldName = propertyInfos.fieldName,
            elementType = elementType,
            type = parameterizedType,
            parameterizedTypes = emptyList(),
            annotations = emptyList(),
            queryType = elementType.pathClass as Class<out SimpleExpression<*>>
        )
    }
}