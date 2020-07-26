package ju.ma.app.graphql

import com.coxautodev.graphql.tools.GraphQLMutationResolver
import com.coxautodev.graphql.tools.GraphQLQueryResolver
import com.querydsl.core.BooleanBuilder
import java.time.Instant
import java.util.Optional
import javax.persistence.EntityNotFoundException
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import ju.ma.app.Faq
import ju.ma.app.FaqRepository
import ju.ma.app.Post
import ju.ma.app.PostRepository
import ju.ma.app.QFaq
import ju.ma.app.QPost
import ju.ma.graphql.BooleanComparison
import ju.ma.graphql.KeyValuePair
import ju.ma.graphql.LongComparision
import ju.ma.graphql.PaginationInput
import ju.ma.graphql.TextComparison
import ju.ma.graphql.TimeComparison
import ju.ma.toNullable
import org.springframework.data.domain.Page
import org.springframework.data.domain.Sort
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.annotation.Validated

@Component
class BlogQueries(
    private val posts: PostRepository,
    private val faqs: FaqRepository
) : GraphQLQueryResolver {
    @PreAuthorize("hasAnyAuthority('read:content')")
    fun postsConnection(page: PaginationInput?, where: PostWhereInput?): Page<Post> {
        val f: PaginationInput = page ?: PaginationInput()
        if (where == null) {
            return posts.findAll(f.create())
        }
        return posts.findAll(where.build(), f.create())
    }

    @PreAuthorize("hasAnyAuthority('read:content')")
    fun posts(where: PostWhereInput, orderBy: MutableList<KeyValuePair>?): List<Post> {
        return if (orderBy != null) {
            val sort: Sort = Sort.by(orderBy.map { Sort.Order(Sort.Direction.fromString(it.value), it.key) })
            posts.findAll(where.build(), sort).toList()
        } else {
            posts.findAll(where.build()).toList()
        }
    }

    fun post(where: PostWhereInput): Post? {
        val post = posts.findOne(where.build()).toNullable() ?: return null
        if (post.secured) {
            SecurityContextHolder.getContext().authentication?.let { auth ->
                auth.authorities.firstOrNull { it.authority == "read:content" } ?: return null
            }
        }
        return post
    }

    @PreAuthorize("hasAnyAuthority('read:content')")
    fun faqsConnection(page: PaginationInput?, where: FaqWhereInput?): Page<Faq> {
        val f: PaginationInput = page ?: PaginationInput()
        if (where == null) {
            return faqs.findAll(f.create())
        }
        return faqs.findAll(where.build(), f.create())
    }

    fun faqs(where: FaqWhereInput, orderBy: MutableList<KeyValuePair>?): List<Faq> {
        return if (orderBy != null) {
            val sort: Sort = Sort.by(orderBy.map { Sort.Order(Sort.Direction.fromString(it.value), it.key) })
            faqs.findAll(where.build(), sort).toList()
        } else {
            faqs.findAll(where.build()).toList()
        }
    }

    fun faq(where: FaqWhereInput): Optional<Faq> = faqs.findOne(where.build())
}

@Component
@Transactional
@Validated
@PreAuthorize("hasAnyAuthority('write:content')")
class BlogMutations(
    private val posts: PostRepository,
    private val faqs: FaqRepository
) : GraphQLMutationResolver {
    fun createPost(@Valid data: CreatePostInput): Post {
        return posts.save(data.entity)
    }

    fun updatePost(@Valid data: UpdatePostInput): Post {
        val entity = posts.findById(data.id).toNullable()
            ?: throw EntityNotFoundException("Unable to locate a post with id: ${data.id}")
        return data.apply(entity)
    }

    fun deletePost(id: Long): Boolean {
        posts.deleteById(id)
        return true
    }

    fun createFaq(@Valid data: CreateFaqInput): Faq {
        return faqs.save(data.entity)
    }

    fun updateFaq(@Valid data: UpdateFaqInput): Faq {
        val entity = faqs.findById(data.id).toNullable()
            ?: throw EntityNotFoundException("Unable to locate a faq with id: ${data.id}")
        return data.apply(entity)
    }

    fun deleteFaq(id: Long): Boolean {
        faqs.deleteById(id)
        return true
    }
}

data class CreatePostInput(
    @get:NotBlank
    val title: String,
    @get:NotBlank
    val slug: String,
    val content: String,
    val secured: Boolean = false,
    val published: Boolean = true
) {
    val entity by lazy {
        Post(
            slug = slug,
            content = content,
            published = published,
            secured = secured,
            title = title
        )
    }
}

data class UpdatePostInput(
    val id: Long,
    @get:NotBlank
    val slug: String,
    @get:NotBlank
    val title: String,
    val content: String,
    val secured: Boolean = false,
    val published: Boolean = true
) {
    fun apply(entity: Post): Post {
        val self = this
        return entity.apply {
            slug = self.slug
            content = self.content
            published = self.published
            title = self.title
            secured = self.secured
        }
    }
}

data class CreateFaqInput(
    @get:NotBlank
    val title: String,
    val content: String,
    val position: Int = 0,
    val published: Boolean = true
) {
    val entity by lazy {
        Faq(
            title = title,
            content = content,
            published = published,
            position = position
        )
    }
}

data class UpdateFaqInput(
    val id: Long,
    @get:NotBlank
    val title: String,
    val content: String,
    val position: Int,
    val published: Boolean = true
) {
    fun apply(entity: Faq): Faq {
        val self = this
        return entity.apply {
            title = self.title
            content = self.content
            published = self.published
            position = self.position
        }
    }
}

class PostWhereInput(
    var id: LongComparision? = null,
    var content: TextComparison? = null,
    var slug: TextComparison? = null,
    var title: TextComparison? = null,
    var published: BooleanComparison? = null,
    var createdAt: TimeComparison<Instant>? = null,
    var updatedAt: TimeComparison<Instant>? = null,
    var AND: MutableList<PostWhereInput> = mutableListOf(),
    var OR: MutableList<PostWhereInput> = mutableListOf(),
    var NOT: MutableList<PostWhereInput> = mutableListOf()
) {
    fun build(entity: QPost = QPost.post): BooleanBuilder {
        val builder = BooleanBuilder()
        id?.let { builder.and(it.build(entity.id)) }
        content?.let { builder.and(it.build(entity.content)) }
        slug?.let { builder.and(it.build(entity.slug)) }
        title?.let { builder.and(it.build(entity.title)) }
        published?.let { builder.and(it.build(entity.published)) }
        createdAt?.let { builder.and(it.build(entity.createdAt)) }
        updatedAt?.let { builder.and(it.build(entity.updatedAt)) }
        if (AND.isNotEmpty()) {
            AND.forEach {
                builder.and(it.build(entity))
            }
        }

        if (OR.isNotEmpty()) {
            OR.forEach {
                builder.or(it.build(entity))
            }
        }

        if (NOT.isNotEmpty()) {
            NOT.forEach {
                builder.andNot(it.build(entity))
            }
        }
        return builder
    }
}

class FaqWhereInput(
    var id: LongComparision? = null,
    var content: TextComparison? = null,
    var title: TextComparison? = null,
    var published: BooleanComparison? = null,
    var createdAt: TimeComparison<Instant>? = null,
    var updatedAt: TimeComparison<Instant>? = null,
    var AND: MutableList<FaqWhereInput> = mutableListOf(),
    var OR: MutableList<FaqWhereInput> = mutableListOf(),
    var NOT: MutableList<FaqWhereInput> = mutableListOf()
) {
    fun build(entity: QFaq = QFaq.faq): BooleanBuilder {
        val builder = BooleanBuilder()
        id?.let { builder.and(it.build(entity.id)) }
        content?.let { builder.and(it.build(entity.content)) }
        title?.let { builder.and(it.build(entity.title)) }
        published?.let { builder.and(it.build(entity.published)) }
        createdAt?.let { builder.and(it.build(entity.createdAt)) }
        updatedAt?.let { builder.and(it.build(entity.updatedAt)) }
        if (AND.isNotEmpty()) {
            AND.forEach {
                builder.and(it.build(entity))
            }
        }

        if (OR.isNotEmpty()) {
            OR.forEach {
                builder.or(it.build(entity))
            }
        }

        if (NOT.isNotEmpty()) {
            NOT.forEach {
                builder.andNot(it.build(entity))
            }
        }
        return builder
    }
}
