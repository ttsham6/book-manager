package com.ttsham6.bookmanager.infrastructure

import com.ttsham6.bookmanager.domain.AuthorNotFoundException
import com.ttsham6.bookmanager.domain.AuthorRepository
import com.ttsham6.bookmanager.jooq.Tables.AUTHORS
import com.ttsham6.bookmanager.support.PostgresContainerTestBase
import org.assertj.core.api.Assertions.assertThat
import org.jooq.DSLContext
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDate
import kotlin.test.assertFailsWith

@SpringBootTest
class AuthorRepositoryImplTest
    @Autowired
    constructor(
        private val authorRepository: AuthorRepository,
        private val dslContext: DSLContext,
    ) : PostgresContainerTestBase() {
        @BeforeEach
        fun setUp() {
            dslContext
                .truncate(AUTHORS)
                .restartIdentity()
                .cascade()
                .execute()
        }

        @Test
        fun `著者を作成して保存できる`() {
            val birthDate = LocalDate.of(1867, 2, 9)

            val author = authorRepository.create("Natsume Soseki", birthDate)

            assertThat(author.id).isEqualTo(1)
            assertThat(author.name).isEqualTo("Natsume Soseki")
            assertThat(author.birthDate).isEqualTo(birthDate)
            assertThat(author.createdAt).isNotNull()
            assertThat(author.updatedAt).isNotNull()

            val persistedAuthors = dslContext.selectFrom(AUTHORS).fetch()
            assertThat(persistedAuthors).hasSize(1)
            assertThat(persistedAuthors.first().id).isEqualTo(author.id)
            assertThat(persistedAuthors.first().name).isEqualTo(author.name)
            assertThat(persistedAuthors.first().birthDate).isEqualTo(author.birthDate)
        }

        @Test
        fun `存在しない著者IDを指定した場合は例外を投げる`() {
            val birthDate = LocalDate.of(1892, 3, 1)

            val exception =
                assertFailsWith<AuthorNotFoundException> {
                    authorRepository.update(10, "Akutagawa Ryunosuke", birthDate)
                }

            assertThat(exception.message).isEqualTo("Author 10 was not found")
            assertThat(dslContext.selectCount().from(AUTHORS).fetchSingle(0, Int::class.java)).isEqualTo(0)
        }

        @Test
        fun `存在する著者IDを指定した場合は著者を更新する`() {
            val author =
                authorRepository.create(
                    name = "Mori Ogai",
                    birthDate = LocalDate.of(1862, 2, 17),
                )
            val updatedBirthDate = LocalDate.of(1862, 2, 19)

            val updatedAuthor =
                authorRepository.update(
                    authorId = author.id,
                    name = "Mori Rintaro",
                    birthDate = updatedBirthDate,
                )

            assertThat(updatedAuthor.id).isEqualTo(author.id)
            assertThat(updatedAuthor.name).isEqualTo("Mori Rintaro")
            assertThat(updatedAuthor.birthDate).isEqualTo(updatedBirthDate)
            assertThat(updatedAuthor.createdAt).isEqualTo(author.createdAt)
            assertThat(updatedAuthor.updatedAt).isAfterOrEqualTo(author.updatedAt)

            val persistedAuthor = dslContext.selectFrom(AUTHORS).fetchSingle()
            assertThat(persistedAuthor.id).isEqualTo(updatedAuthor.id)
            assertThat(persistedAuthor.name).isEqualTo(updatedAuthor.name)
            assertThat(persistedAuthor.birthDate).isEqualTo(updatedAuthor.birthDate)
        }
    }
