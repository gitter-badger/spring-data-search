package com.weedow.spring.data.search.querydsl.querytype

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import com.querydsl.core.types.*
import com.querydsl.core.types.dsl.SimplePath
import com.querydsl.core.util.Annotations
import com.weedow.spring.data.search.common.model.Person
import com.weedow.spring.data.search.utils.MAP_KEY
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import java.lang.reflect.AnnotatedElement

@ExtendWith(MockitoExtension::class)
internal class QEntityJoinImplTest {

    @Mock
    private lateinit var qEntity: QEntity<*>

    @Mock
    private lateinit var propertyInfos: PropertyInfos

    @InjectMocks
    private lateinit var qEntityJoin: QEntityJoinImpl<*>

    @ParameterizedTest
    @EnumSource(value = ElementType::class, mode = EnumSource.Mode.EXCLUDE, names = ["MAP"])
    fun get(elementType: ElementType) {
        val fieldName = "myfield"
        val qPath = mock<QPath<*>>()

        whenever(propertyInfos.elementType).thenReturn(elementType)

        whenever(qEntity.get(fieldName)).thenReturn(qPath)

        assertThat(qEntityJoin.get(fieldName)).isSameAs(qPath)
    }

    @Test
    fun get_map_key() {
        val fieldName = MAP_KEY
        val parentClass = Person::class.java
        val keyType = String::class.java
        val valueType = Int::class.javaObjectType

        whenever(propertyInfos.elementType).thenReturn(ElementType.MAP)
        whenever(propertyInfos.parameterizedTypes).thenReturn(listOf(keyType, valueType))
        whenever(propertyInfos.parentClass).thenReturn(parentClass)
        whenever(propertyInfos.fieldName).thenReturn(fieldName)

        val alias = "myalias"
        val element = mock<Any> {
            on { this.toString() }.thenReturn(alias)
        }
        val metadata = mock<PathMetadata> {
            on { this.element }.thenReturn(element)
        }
        whenever(qEntity.metadata).thenReturn(metadata)

        val otherPropertyInfos = mock<PropertyInfos>()
        whenever(
            propertyInfos.copy(
                parentClass = parentClass,
                fieldName = fieldName,
                elementType = ElementType.MAP_KEY,
                type = keyType,
                parameterizedTypes = emptyList(),
                annotations = emptyList(),
                queryType = QEntityImpl::class.java
            )
        ).thenReturn(otherPropertyInfos)

        val qPath = qEntityJoin.get(fieldName)

        assertThat(qPath.propertyInfos).isEqualTo(otherPropertyInfos)

        assertThat(qPath.path).isInstanceOf(SimplePath::class.java)
        assertThat(qPath.path.type).isEqualTo(keyType)
        assertThat(qPath.path.root).isInstanceOf(PathImpl::class.java)
        assertThat(qPath.path.root.type).isEqualTo(keyType)
        assertThat(qPath.path.root.metadata.element).isEqualTo("key($alias)")
        assertThat(qPath.path.annotatedElement).isEqualTo(keyType)
        assertThat(qPath.path.metadata.pathType).isEqualTo(PathType.VARIABLE)
        assertThat(qPath.path.metadata.element).isEqualTo("key($alias)")
        assertThat(qPath.path.metadata.name).isEqualTo("key($alias)")
        assertThat(qPath.path.metadata.isRoot).isEqualTo(true)
        assertThat(qPath.path.metadata.parent).isNull()
        assertThat(qPath.path.metadata.rootPath).isNull()

        verifyNoMoreInteractions(qEntity)
    }

    @Test
    fun accept() {
        val visitor = mock<Visitor<Any, Any>>()
        val context = mock<Any>()

        val result = mock<QPath<*>>()
        whenever(qEntity.accept(visitor, context)).thenReturn(result)

        assertThat(qEntityJoin.accept(visitor, context)).isSameAs(result)
    }

    @Test
    fun getType() {
        val result = Person::class.java
        whenever(qEntity.type).thenReturn(result)

        assertThat(qEntityJoin.type).isSameAs(result)
    }

    @Test
    fun getMetadata() {
        val result = mock<PathMetadata>()
        whenever(qEntity.metadata).thenReturn(result)

        assertThat(qEntityJoin.metadata).isSameAs(result)
    }

    @Test
    fun getMetadataWithParameter() {
        val property = mock<Path<*>>()

        val result = mock<PathMetadata>()
        whenever(qEntity.getMetadata(property)).thenReturn(result)

        assertThat(qEntityJoin.getMetadata(property)).isSameAs(result)
    }

    @Test
    fun getRoot() {
        val result = mock<Path<*>>()
        whenever(qEntity.root).thenReturn(result)

        assertThat(qEntityJoin.root).isSameAs(result)
    }

    @Test
    fun getAnnotatedElement() {
        val result = mock<AnnotatedElement>()
        whenever(qEntity.annotatedElement).thenReturn(result)

        assertThat(qEntityJoin.annotatedElement).isSameAs(result)
    }
}