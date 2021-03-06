/*
 * Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License").
 *   You may not use this file except in compliance with the License.
 *   A copy of the License is located at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   or in the "license" file accompanying this file. This file is distributed
 *   on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *   express or implied. See the License for the specific language governing
 *   permissions and limitations under the License.
 */

package com.amazon.crud4dynamo.compatibility.mapperannotation;

import static org.assertj.core.api.Assertions.assertThat;

import com.amazon.crud4dynamo.CrudForDynamo;
import com.amazon.crud4dynamo.compatibility.testmodel.Book;
import com.amazon.crud4dynamo.compatibility.testmodel.Book.Attributes;
import com.amazon.crud4dynamo.compatibility.testmodel.Book.CustomDate;
import com.amazon.crud4dynamo.compatibility.testmodel.Book.Picture;
import com.amazon.crud4dynamo.crudinterface.CompositeKeyCrud;
import com.amazon.crud4dynamo.testbase.SingleTableDynamoDbTestBase;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BasicCrudMethodsMapperAnnotationCompatibleTest extends SingleTableDynamoDbTestBase<Book> {

  private CompositeKeyCrud<String, Integer, Book> bookDao;

  @Override
  protected Class<Book> getModelClass() {
    return Book.class;
  }

  @Override
  @BeforeEach
  public void setUp() throws Exception {
    super.setUp();

    bookDao = new CrudForDynamo(getDynamoDbClient()).createComposite(Book.class);
  }

  /**
   * Test basic
   * annotations: @DynamoDBHashKey @DynamoDBRangeKey @DynamoDBAttribute @DynamoDBAutoGeneratedKey @DynamoDBVersionAttribute
   */
  @Test
  void basic_annotations() {
    final String dummyAuthor = "dummy author";
    final String dummyTitle = "dummy title";
    final Book bookItem = Book.builder().author(dummyAuthor).title(dummyTitle).build();
    // DynamoDBMapper will perform in-place update for bookItem, so the auto-generated id
    // is reflected in bookItem
    bookDao.save(bookItem);

    final List<Book> books = Lists.newArrayList(bookDao.groupBy(dummyAuthor));

    assertThat(books).hasSize(1);
    assertThat(books.get(0)).isEqualTo(bookItem);
    assertThat(books.get(0).getVersionNumber()).isEqualTo(1);
  }

  @Test
  void annotation_DynamoDBIgnore() {
    final String dummyAuthor = "dummy author";
    bookDao.save(Book.builder().author(dummyAuthor).ignored("ignored").build());

    final List<Book> books = Lists.newArrayList(bookDao.groupBy(dummyAuthor));
    assertThat(books).hasSize(1);

    final Book book = books.get(0);
    assertThat(book.getIgnored()).isNull();
  }

  @Test
  void annotation_DynamoDBTyped() {
    final String dummyAuthor = "dummy author";
    final int dummyInteger = 123;
    bookDao.save(Book.builder().author(dummyAuthor).integerStoredAsString(dummyInteger).build());

    final List<Book> books = Lists.newArrayList(bookDao.groupBy(dummyAuthor));
    assertThat(books).hasSize(1);

    final Book book = books.get(0);
    final Optional<Item> item =
        getItem(
            new GetItemSpec()
                .withPrimaryKey(
                    Attributes.HASH_KEY, dummyAuthor, Attributes.RANGE_KEY, book.getId()));

    assertThat(item).isPresent();
    assertThat(item.get().asMap())
        .containsEntry(Attributes.INTEGER_STORED_AS_STRING, String.valueOf(dummyInteger));
  }

  @Test
  void annotation_DynamoDBDocument() {
    final String dummyAuthor = "dummy author";
    final Book bookItem =
        Book.builder()
            .author(dummyAuthor)
            .cover(Picture.builder().height(100).width(100).sourcePath("a/path").build())
            .build();
    bookDao.save(bookItem);

    final List<Book> books = Lists.newArrayList(bookDao.groupBy(dummyAuthor));

    assertThat(books).hasSize(1);
    assertThat(books.get(0)).isEqualTo(bookItem);
  }

  @Test
  void annotation_DynamoDBTypeConverted() {
    final String dummyAuthor = "dummy author";
    final Book bookItem =
        Book.builder()
            .author(dummyAuthor)
            .customDate(CustomDate.builder().year(1984).month(4).day(1).build())
            .build();
    bookDao.save(bookItem);

    final List<Book> books = Lists.newArrayList(bookDao.groupBy(dummyAuthor));
    assertThat(books).hasSize(1);
    assertThat(books.get(0)).isEqualTo(bookItem);

    final Optional<Item> item =
        getItem(
            new GetItemSpec()
                .withPrimaryKey(
                    Attributes.HASH_KEY, dummyAuthor, Attributes.RANGE_KEY, bookItem.getId()));

    assertThat(item).isPresent();
    assertThat(item.get().asMap()).containsEntry(Attributes.CUSTOM_DATE, "1984-4-1");
  }
}
