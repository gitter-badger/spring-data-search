package com.weedow.spring.data.search.sample.config

import com.weedow.spring.data.search.common.model.Address
import com.weedow.spring.data.search.common.model.Person
import com.weedow.spring.data.search.common.repository.PersonRepository
import com.weedow.spring.data.search.config.SearchConfigurer
import com.weedow.spring.data.search.descriptor.SearchDescriptor
import com.weedow.spring.data.search.descriptor.SearchDescriptorBuilder
import com.weedow.spring.data.search.descriptor.SearchDescriptorRegistry
import com.weedow.spring.data.search.join.handler.FetchingAllEntityJoinHandler
import com.weedow.spring.data.search.join.handler.FetchingEagerEntityJoinHandler
import com.weedow.spring.data.search.sample.dto.PersonDtoMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SampleAppConfiguration : SearchConfigurer {

    override fun addSearchDescriptors(registry: SearchDescriptorRegistry) {
        registry.addSearchDescriptor(personSearchDescriptor())
        registry.addSearchDescriptor(addressSearchDescriptor())
    }

    fun personSearchDescriptor(): SearchDescriptor<Person> = SearchDescriptorBuilder.builder<Person>().build()

    @Bean
    fun person2SearchDescriptor(personRepository: PersonRepository): SearchDescriptor<Person> = SearchDescriptorBuilder.builder<Person>()
            .id("person2")
            .dtoMapper(PersonDtoMapper())
            .jpaSpecificationExecutor(personRepository)
            .entityJoinHandlers(FetchingEagerEntityJoinHandler())
            .build()

    @Bean
    fun person3SearchDescriptor(): SearchDescriptor<Person> = SearchDescriptorBuilder.builder<Person>()
            .id("person3")
            .entityJoinHandlers(FetchingAllEntityJoinHandler())
            .build()

    fun addressSearchDescriptor(): SearchDescriptor<Address> = SearchDescriptorBuilder.builder<Address>().build()

    @Bean
    fun address3SearchDescriptor(): SearchDescriptor<Address> = SearchDescriptorBuilder.builder<Address>()
            .id("address2")
            .entityJoinHandlers(FetchingAllEntityJoinHandler())
            .build()

}