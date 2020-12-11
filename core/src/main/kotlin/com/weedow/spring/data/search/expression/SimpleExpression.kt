package com.weedow.spring.data.search.expression

import com.weedow.spring.data.search.join.EntityJoins
import com.weedow.spring.data.search.querydsl.QueryDslBuilder
import com.weedow.spring.data.search.querydsl.specification.QueryDslSpecification
import com.weedow.spring.data.search.utils.Keyword.CURRENT_DATE
import com.weedow.spring.data.search.utils.Keyword.CURRENT_DATE_TIME
import com.weedow.spring.data.search.utils.Keyword.CURRENT_TIME
import com.weedow.spring.data.search.utils.NullValue
import com.weedow.spring.data.search.utils.klogger
import org.springframework.data.jpa.domain.Specification
import javax.persistence.criteria.*

/**
 * Expression to compare the field value with the given [value] according to the given [Operator].
 */
internal data class SimpleExpression(
    private val operator: Operator,
    private val fieldInfo: FieldInfo,
    private val value: Any,
) : Expression {

    companion object {
        private val log by klogger()
    }

    override fun toFieldExpressions(negated: Boolean): Collection<FieldExpression> {
        return listOf(FieldExpressionImpl(fieldInfo, value, operator, negated))
    }

    override fun <T> toSpecification(entityJoins: EntityJoins): Specification<T> {
        return Specification { root: Root<T>, _: CriteriaQuery<*>, criteriaBuilder: CriteriaBuilder ->
            val path = entityJoins.getPath(fieldInfo.fieldPath, root)

            when (operator) {
                Operator.EQUALS -> equals(criteriaBuilder, path, value, fieldInfo)
                Operator.MATCHES -> @Suppress("UNCHECKED_CAST") like(criteriaBuilder, path as Path<String>, value as String)
                Operator.IMATCHES -> @Suppress("UNCHECKED_CAST") ilike(criteriaBuilder, path as Path<String>, value as String)
                Operator.LESS_THAN -> @Suppress("UNCHECKED_CAST") lessThan(criteriaBuilder, path as Path<Comparable<Any>>, value)
                Operator.LESS_THAN_OR_EQUALS -> @Suppress("UNCHECKED_CAST") lessThanOrEquals(criteriaBuilder, path as Path<Comparable<Any>>, value)
                Operator.GREATER_THAN -> @Suppress("UNCHECKED_CAST") greaterThan(criteriaBuilder, path as Path<Comparable<Any>>, value)
                Operator.GREATER_THAN_OR_EQUALS -> @Suppress("UNCHECKED_CAST") greaterThanOrEquals(criteriaBuilder, path as Path<Comparable<Any>>, value)
                Operator.IN -> inPredicate(criteriaBuilder, path, value as List<*>)
            }
        }
    }

    override fun <T> toQueryDslSpecification(entityJoins: EntityJoins): QueryDslSpecification<T> {
        return QueryDslSpecification { builder: QueryDslBuilder<T> ->
            val qpath = entityJoins.getQPath(fieldInfo.fieldPath, builder)

            when (operator) {
                Operator.EQUALS -> equals(builder, qpath.path, value)
                Operator.MATCHES -> like(builder, qpath.path as com.querydsl.core.types.Path<String>, value as String)
                Operator.IMATCHES -> ilike(builder, qpath.path as com.querydsl.core.types.Path<String>, value as String)
                Operator.LESS_THAN -> lessThan(builder, qpath.path, value)
                Operator.LESS_THAN_OR_EQUALS -> lessThanOrEquals(builder, qpath.path, value)
                Operator.GREATER_THAN -> greaterThan(builder, qpath.path, value)
                Operator.GREATER_THAN_OR_EQUALS -> greaterThanOrEquals(builder, qpath.path, value)
                Operator.IN -> inPredicate(builder, qpath.path, value as List<*>)
                else -> throw IllegalArgumentException("Operator not handled: $operator")
            }
        }
    }

    private fun equals(criteriaBuilder: CriteriaBuilder, path: Path<*>, value: Any, fieldInfo: FieldInfo): Predicate {
        return if (Map::class.java.isAssignableFrom(fieldInfo.parentClass)) {
            // Handle the Map special keys (key, value)
            if (value === NullValue) {
                criteriaBuilder.isNull(path)
            } else {
                criteriaBuilder.equal(path, value)
            }
        } else {
            val field = fieldInfo.parentClass.getDeclaredField(fieldInfo.fieldName)
            if (value === NullValue) {
                if (Collection::class.java.isAssignableFrom(field.type) || Map::class.java.isAssignableFrom(field.type)) {
                    @Suppress("UNCHECKED_CAST")
                    criteriaBuilder.isEmpty(path as javax.persistence.criteria.Expression<Collection<*>>)
                } else {
                    criteriaBuilder.isNull(path)
                }
            } else {
                if (Collection::class.java.isAssignableFrom(field.type)) {
                    @Suppress("UNCHECKED_CAST")
                    criteriaBuilder.isMember(value, path as javax.persistence.criteria.Expression<Collection<*>>)
                } else {
                    val expression = convertValueToExpression(criteriaBuilder, value, path.javaType)
                    return criteriaBuilder.equal(path, expression)
                }
            }
        }
    }

    private fun like(criteriaBuilder: CriteriaBuilder, path: Path<String>, value: String): Predicate {
        return criteriaBuilder.like(
            path,
            criteriaBuilder.literal(value.replace("*", "%"))
        )
    }

    private fun ilike(criteriaBuilder: CriteriaBuilder, path: Path<String>, value: String): Predicate {
        return criteriaBuilder.like(
            criteriaBuilder.lower(path),
            criteriaBuilder.lower(criteriaBuilder.literal(value.replace("*", "%")))
        )
    }

    private fun <Y : Comparable<Y>> lessThan(criteriaBuilder: CriteriaBuilder, path: Path<Y>, value: Any): Predicate {
        val expression = convertValueToExpression(criteriaBuilder, value, path.javaType)
        return criteriaBuilder.lessThan(path, expression)
    }

    private fun <Y : Comparable<Y>> lessThanOrEquals(criteriaBuilder: CriteriaBuilder, path: Path<Y>, value: Any): Predicate {
        val expression = convertValueToExpression(criteriaBuilder, value, path.javaType)
        return criteriaBuilder.lessThanOrEqualTo(path, expression)
    }

    private fun <Y : Comparable<Y>> greaterThan(criteriaBuilder: CriteriaBuilder, path: Path<Y>, value: Any): Predicate {
        val expression = convertValueToExpression(criteriaBuilder, value, path.javaType)
        return criteriaBuilder.greaterThan(path, expression)
    }

    private fun <Y : Comparable<Y>> greaterThanOrEquals(criteriaBuilder: CriteriaBuilder, path: Path<Y>, value: Any): Predicate {
        val expression = convertValueToExpression(criteriaBuilder, value, path.javaType)
        return criteriaBuilder.greaterThanOrEqualTo(path, expression)
    }

    @Suppress("UNCHECKED_CAST")
    private fun <Y> convertValueToExpression(
        criteriaBuilder: CriteriaBuilder,
        value: Any,
        @Suppress("UNUSED_PARAMETER") type: Class<Y>
    ): javax.persistence.criteria.Expression<Y> {
        return when {
            CURRENT_DATE === value -> criteriaBuilder.currentDate() as javax.persistence.criteria.Expression<Y>
            CURRENT_TIME === value -> criteriaBuilder.currentTime() as javax.persistence.criteria.Expression<Y>
            CURRENT_DATE_TIME === value -> criteriaBuilder.currentTimestamp() as javax.persistence.criteria.Expression<Y>
            else -> criteriaBuilder.literal(value) as javax.persistence.criteria.Expression<Y>
        }
    }

    private fun inPredicate(criteriaBuilder: CriteriaBuilder, path: Path<*>, values: List<*>): Predicate {
        val inClause = criteriaBuilder.`in`(path)
        values.forEach { inClause.value(it) }
        return inClause
    }

    //////////////////////////////////////

    private fun equals(queryDslBuilder: QueryDslBuilder<*>, path: com.querydsl.core.types.Path<*>, value: Any): com.querydsl.core.types.Predicate {
        return if (value === NullValue) {
            queryDslBuilder.isNull(path)
        } else {
            queryDslBuilder.equal(path, value)
        }
    }

    private fun like(queryDslBuilder: QueryDslBuilder<*>, path: com.querydsl.core.types.Path<String>, value: String): com.querydsl.core.types.Predicate {
        return queryDslBuilder.like(path, value)
    }

    private fun ilike(queryDslBuilder: QueryDslBuilder<*>, path: com.querydsl.core.types.Path<String>, value: String): com.querydsl.core.types.Predicate {
        return queryDslBuilder.ilike(path, value)
    }

    private fun lessThan(queryDslBuilder: QueryDslBuilder<*>, path: com.querydsl.core.types.Path<*>, value: Any): com.querydsl.core.types.Predicate {
        return queryDslBuilder.lessThan(path, value)
    }

    private fun lessThanOrEquals(queryDslBuilder: QueryDslBuilder<*>, path: com.querydsl.core.types.Path<*>, value: Any): com.querydsl.core.types.Predicate {
        return queryDslBuilder.lessThanOrEquals(path, value)
    }

    private fun greaterThan(queryDslBuilder: QueryDslBuilder<*>, path: com.querydsl.core.types.Path<*>, value: Any): com.querydsl.core.types.Predicate {
        return queryDslBuilder.greaterThan(path, value)
    }

    private fun greaterThanOrEquals(queryDslBuilder: QueryDslBuilder<*>, path: com.querydsl.core.types.Path<*>, value: Any): com.querydsl.core.types.Predicate {
        return queryDslBuilder.greaterThanOrEquals(path, value)
    }

    private fun inPredicate(queryDslBuilder: QueryDslBuilder<*>, path: com.querydsl.core.types.Path<*>, value: Collection<*>): com.querydsl.core.types.Predicate {
        return queryDslBuilder.`in`(path, value)
    }

}
