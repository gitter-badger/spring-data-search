package com.weedow.spring.data.search.querydsl.querytype

import com.querydsl.core.types.Path
import com.querydsl.core.types.PathMetadata
import com.querydsl.core.types.PathMetadataFactory.forVariable
import com.querydsl.core.types.dsl.ArrayPath
import com.querydsl.core.types.dsl.EntityPathBase
import com.querydsl.core.types.dsl.PathInits
import com.weedow.spring.data.search.context.DataSearchContext

class QEntity<T>(
        private val dataSearchContext: DataSearchContext,
        private val entityClass: Class<out T>,
        private val pathMetadata: PathMetadata,
        private val inits: PathInits = INITS,
) : EntityPathBase<T>(entityClass, pathMetadata, inits) {

    constructor(
            dataSearchContext: DataSearchContext,
            entityClass: Class<out T>,
            variable: String,
            inits: PathInits = INITS,
    ) : this(dataSearchContext, entityClass, forVariable(variable), inits)

    companion object {
        private val INITS = PathInits.DIRECT2
    }

    private val fieldPaths = mutableMapOf<String, QPath<*>>()

    init {
        dataSearchContext.getAllPropertyInfos(type).forEach { propertyInfos ->
            addField(propertyInfos)
        }
    }

    fun get(fieldName: String): QPath<*> {
        return fieldPaths.getOrElse(fieldName) {
            throw IllegalArgumentException("Could not found the Path related to the given field name '$fieldName'")
        }
    }

    private fun addField(propertyInfos: PropertyInfos) {
        val path: Path<*>? = when (propertyInfos.elementType) {
            ElementType.BOOLEAN -> createBoolean(propertyInfos.fieldName)
            ElementType.STRING -> createString(propertyInfos.fieldName)
            ElementType.NUMBER -> createNumber(propertyInfos.fieldName, propertyInfos.type)
            ElementType.DATE -> createDate(propertyInfos.fieldName, propertyInfos.type)
            ElementType.DATETIME -> createDateTime(propertyInfos.fieldName, propertyInfos.type)
            ElementType.TIME -> createTime(propertyInfos.fieldName, propertyInfos.type)
            ElementType.ENUM -> @Suppress("UNCHECKED_CAST") createEnum(propertyInfos.fieldName, propertyInfos.type as Class<Enum<*>>)
            ElementType.ARRAY -> createArray(propertyInfos.fieldName, propertyInfos.type, propertyInfos.parametrizedTypes[0])
            ElementType.LIST -> createList(propertyInfos.fieldName, propertyInfos.parametrizedTypes[0], propertyInfos.queryType, PathInits.DIRECT2)
            ElementType.SET -> createSet(propertyInfos.fieldName, propertyInfos.parametrizedTypes[0], propertyInfos.queryType, PathInits.DIRECT2)
            ElementType.COLLECTION -> createCollection(propertyInfos.fieldName, propertyInfos.parametrizedTypes[0], propertyInfos.queryType, PathInits.DIRECT2)
            ElementType.MAP -> createMap(propertyInfos.fieldName, propertyInfos.parametrizedTypes[0], propertyInfos.parametrizedTypes[1], propertyInfos.queryType)
            ElementType.COMPARABLE -> createComparable(propertyInfos.fieldName, propertyInfos.type)
            ElementType.ENTITY -> if (inits.isInitialized(propertyInfos.fieldName)) QEntity(dataSearchContext, propertyInfos.type, forProperty(propertyInfos.fieldName), inits.get(propertyInfos.fieldName)) else null
            else -> createSimple(propertyInfos.fieldName, propertyInfos.type)
        }
        if (path != null) {
            fieldPaths[propertyInfos.fieldName] = QPathImpl(path, propertyInfos)
        }
    }

    private fun <A, E> createArray(fieldName: String, type: Class<A>, @Suppress("UNUSED_PARAMETER") componentType: Class<E>): ArrayPath<A, E> {
        return createArray(fieldName, type)
    }

}
