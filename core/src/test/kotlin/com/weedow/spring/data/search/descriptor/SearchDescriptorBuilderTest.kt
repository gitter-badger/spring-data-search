package com.weedow.spring.data.search.descriptor

import com.nhaarman.mockitokotlin2.mock
import com.weedow.spring.data.search.common.model.Person
import com.weedow.spring.data.search.dto.DefaultDtoMapper
import com.weedow.spring.data.search.join.handler.DefaultEntityJoinHandler
import com.weedow.spring.data.search.query.specification.SpecificationExecutor
import com.weedow.spring.data.search.validation.DataSearchValidator
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
internal class SearchDescriptorBuilderTest {

    @Test
    fun build_SearchDescriptor_with_default_values() {
        val entityClass = Person::class.java

        val searchDescriptor1 = SearchDescriptorBuilder.builder<Person>()
            .build()

        assertThat(searchDescriptor1.id).isEqualTo("person")
        assertThat(searchDescriptor1.entityClass).isEqualTo(entityClass)
        assertThat(searchDescriptor1.validators).isEmpty()
        assertThat(searchDescriptor1.dtoMapper).isNull()
        assertThat(searchDescriptor1.specificationExecutor).isNull()
        assertThat(searchDescriptor1.entityJoinHandlers).isEmpty()

        val searchDescriptor2 = SearchDescriptorBuilder(entityClass)
            .build()

        assertThat(searchDescriptor2.id).isEqualTo("person")
        assertThat(searchDescriptor2.entityClass).isEqualTo(entityClass)
        assertThat(searchDescriptor2.validators).isEmpty()
        assertThat(searchDescriptor2.dtoMapper).isNull()
        assertThat(searchDescriptor1.specificationExecutor).isNull()
        assertThat(searchDescriptor2.entityJoinHandlers).isEmpty()

        assertThat(searchDescriptor1).isEqualTo(searchDescriptor2)
        assertThat(searchDescriptor1).isNotSameAs(searchDescriptor2)
    }

    @Test
    fun build_SearchDescriptor_with_custom_values() {
        val entityClass = Person::class.java

        val validator1 = mock<DataSearchValidator>()
        val dtoMapper1 = DefaultDtoMapper<Person>()
        val specificationExecutor1 = mock<SpecificationExecutor<Person>>()
        val entityJoinHandler1 = DefaultEntityJoinHandler()
        val searchDescriptor1 = SearchDescriptorBuilder.builder<Person>()
            .id("person1")
            .validators(validator1)
            .dtoMapper(dtoMapper1)
            .specificationExecutor(specificationExecutor1)
            .entityJoinHandlers(entityJoinHandler1)
            .build()

        assertThat(searchDescriptor1.id).isEqualTo("person1")
        assertThat(searchDescriptor1.entityClass).isEqualTo(entityClass)
        assertThat(searchDescriptor1.validators).containsExactly(validator1)
        assertThat(searchDescriptor1.dtoMapper).isEqualTo(dtoMapper1)
        assertThat(searchDescriptor1.specificationExecutor).isEqualTo(specificationExecutor1)
        assertThat(searchDescriptor1.entityJoinHandlers).containsExactly(entityJoinHandler1)

        val validator2 = mock<DataSearchValidator>()
        val dtoMapper2 = DefaultDtoMapper<Person>()
        val specificationExecutor2 = mock<SpecificationExecutor<Person>>()
        val entityJoinHandler2 = DefaultEntityJoinHandler()
        val searchDescriptor2 = SearchDescriptorBuilder(entityClass)
            .id("person2")
            .validators(validator2)
            .dtoMapper(dtoMapper2)
            .specificationExecutor(specificationExecutor2)
            .entityJoinHandlers(entityJoinHandler2)
            .build()

        assertThat(searchDescriptor2.id).isEqualTo("person2")
        assertThat(searchDescriptor2.entityClass).isEqualTo(entityClass)
        assertThat(searchDescriptor2.validators).containsExactly(validator2)
        assertThat(searchDescriptor2.dtoMapper).isEqualTo(dtoMapper2)
        assertThat(searchDescriptor2.specificationExecutor).isEqualTo(specificationExecutor2)
        assertThat(searchDescriptor2.entityJoinHandlers).containsExactly(entityJoinHandler2)
    }

}