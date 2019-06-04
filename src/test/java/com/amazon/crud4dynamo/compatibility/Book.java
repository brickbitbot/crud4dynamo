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

package com.amazon.crud4dynamo.compatibility;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAutoGeneratedKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIgnore;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperFieldModel.DynamoDBAttributeType;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverted;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTyped;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamoDBTable(tableName = Book.TABLE_NAME)
public class Book {

  public static final String TABLE_NAME = "Book";

  public static class Attributes {

    public static final String HASH_KEY = "Author";
    public static final String RANGE_KEY = "Id";
    public static final String INTEGER_STORED_AS_STRING = "IntegerStoredAsString";
    public static final String CUSTOM_DATE = "CustomDate";
  }

  @DynamoDBHashKey(attributeName = Attributes.HASH_KEY)
  private String author;

  @DynamoDBAutoGeneratedKey
  @DynamoDBRangeKey(attributeName = Attributes.RANGE_KEY)
  private String id;

  @DynamoDBAttribute(attributeName = "Title")
  private String title;

  @DynamoDBIgnore
  private String ignored;

  @DynamoDBAttribute(attributeName = "Cover")
  private Picture cover;

  @DynamoDBTyped(value = DynamoDBAttributeType.S)
  @DynamoDBAttribute(attributeName = Attributes.INTEGER_STORED_AS_STRING)
  private int integerStoredAsString;

  @DynamoDBTypeConverted(converter = CustomDateConverter.class)
  @DynamoDBAttribute(attributeName = Attributes.CUSTOM_DATE)
  private CustomDate customDate;

  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  @DynamoDBDocument
  public static class Picture {

    @DynamoDBAttribute(attributeName = "Width")
    private int width;
    @DynamoDBAttribute(attributeName = "Height")
    private int height;
    @DynamoDBAttribute(attributeName = "Src")
    private String sourcePath;
  }

  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class CustomDate {

    private int year;
    private int month;
    private int day;
  }

  public static class CustomDateConverter implements DynamoDBTypeConverter<String, CustomDate> {

    @Override
    public String convert(CustomDate object) {
      return String.format("%d-%d-%d", object.year, object.month, object.day);
    }

    @Override
    public CustomDate unconvert(String str) {
      final String[] splits = str.split("-");
      return CustomDate.builder()
          .year(Integer.valueOf(splits[0]))
          .month(Integer.valueOf(splits[1]))
          .day(Integer.valueOf(splits[2]))
          .build();
    }
  }
}