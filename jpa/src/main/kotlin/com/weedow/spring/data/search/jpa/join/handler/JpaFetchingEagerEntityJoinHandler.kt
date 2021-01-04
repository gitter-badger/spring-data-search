package com.weedow.spring.data.search.jpa.join.handler

import com.querydsl.core.JoinType
import com.weedow.spring.data.search.context.DataSearchContext
import com.weedow.spring.data.search.join.JoinInfo
import com.weedow.spring.data.search.join.handler.EntityJoinHandler
import com.weedow.spring.data.search.query.querytype.PropertyInfos
import javax.persistence.*

/**
 * [EntityJoinHandler] implementation to fetch all fields (entity fields and nested fields included) with Join Annotation defining [FetchType.EAGER].
 *
 * Technically, it creates a `LEFT JOIN FETCH` in JPQL for Join Annotation with [FetchType.EAGER].
 *
 * _Example: `A` has a relationship with `B` using `@OneToMany` annotation and `FetchType.EAGER`, and `A` has a relationship with `C`
 * using `@OneToMany` annotation and `FetchType.LAZY`. When we search for `A`, we retrieve `A` with just data from `B`, but not `C`._
 *
 * @param dataSearchContext [DataSearchContext]
 */
class JpaFetchingEagerEntityJoinHandler(
    private val dataSearchContext: DataSearchContext
) : EntityJoinHandler {

    override fun supports(propertyInfos: PropertyInfos): Boolean {
        val joinAnnotation = propertyInfos.annotations.firstOrNull { dataSearchContext.isJoinAnnotation(it.annotationClass.java) }
        val fetchType = getFetchType(joinAnnotation)
        return FetchType.EAGER == fetchType
    }

    override fun handle(propertyInfos: PropertyInfos): JoinInfo {
        return JoinInfo(JoinType.LEFTJOIN, true)
    }

    private fun getFetchType(joinAnnotation: Annotation?): FetchType {
        return when (joinAnnotation) {
            is OneToOne -> joinAnnotation.fetch
            is OneToMany -> joinAnnotation.fetch
            is ManyToMany -> joinAnnotation.fetch
            is ElementCollection -> joinAnnotation.fetch
            is ManyToOne -> joinAnnotation.fetch
            else -> FetchType.LAZY
        }
    }

}